package org.mutabilitydetector.asmoverride;

import com.google.classpath.ClassPath;
import com.google.classpath.ClassPathFactory;
import com.google.classpath.RegExpResourceFilter;
import junit.framework.TestCase;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.NonClassloadingSimpleVerifier;
import org.objectweb.asm.tree.analysis.PatchedSimpleVerifier;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.classpath.RegExpResourceFilter.ANY;
import static com.google.classpath.RegExpResourceFilter.ENDS_WITH_CLASS;

public class CanCalculateFramesForEveryJdkRuntimeClass extends TestCase {

    private ClassPath classPath = new ClassPathFactory().createFromPath("/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/rt.jar");
    private RegExpResourceFilter regExpResourceFilter = new RegExpResourceFilter(ANY, ENDS_WITH_CLASS);
    private String[] findResources = classPath.findResources("", regExpResourceFilter);
    private final IsAssignableDifferencesVerifier verifier = new IsAssignableDifferencesVerifier(
        new PatchedSimpleVerifier(), new NonClassloadingSimpleVerifier());


    public void testWithClassesBeingLoadedWithSimpleVerifier() throws Exception {
        for (String resourcePath: findResources) {
            new ClassReader(ClassLoader.getSystemResourceAsStream(resourcePath)).accept(new DelegatesToMethodVisitor(new SimpleVerifier()), 0);
        }
    }

    public void testWithClassesBeingLoadedWithPatchedSimpleVerifier() throws Exception {
        for (String resourcePath: findResources) {
            new ClassReader(ClassLoader.getSystemResourceAsStream(resourcePath)).accept(new DelegatesToMethodVisitor(new PatchedSimpleVerifier()), 0);
        }
    }

    public void testWithoutLoadingClasses() throws Exception {
        for (String resourcePath: findResources) {
            new ClassReader(ClassLoader.getSystemResourceAsStream(resourcePath)).accept(new DelegatesToMethodVisitor(new NonClassloadingSimpleVerifier()), 0);
        }
    }

    public void testThereAreNoDifferences() throws Exception {
        for (String resourcePath: findResources) {
            new ClassReader(ClassLoader.getSystemResourceAsStream(resourcePath)).accept(new DelegatesToMethodVisitor(verifier), 0);
        }

        assertNoDifferences(verifier.differences);
    }

    private void assertNoDifferences(Collection<Difference> differences) {
        if (!differences.isEmpty()) {
            StringBuilder differencesFormated = new StringBuilder();
            for (Difference diff: differences) {
                differencesFormated.append("\n\t").append(diff);

            }
            throw new AssertionError("Expected no differences, but found: "
                + differences.size() + " differences\n"
                + differencesFormated);
        }
    }

    public void testFailingClass() throws Exception {
        new ClassReader(ClassLoader.getSystemResourceAsStream("com/sun/beans/TypeResolver.class"))
            .accept(new DelegatesToMethodVisitor(verifier), 0);

        assertNoDifferences(verifier.differences);
    }

    private static class DelegatesToMethodVisitor extends ClassVisitor {

        private String ownerName;
        private BasicVerifier verifier;

        DelegatesToMethodVisitor(BasicVerifier verifier) {
            super(Opcodes.ASM5);
            this.verifier = verifier;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.ownerName = name;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new RequiresPopulatedStackFrames(ownerName, access, name, desc, signature, exceptions, verifier);
        }
    }

    private static class RequiresPopulatedStackFrames extends MethodNode {
        private String ownerName;
        private BasicVerifier verifier;

        RequiresPopulatedStackFrames(String ownerName, int access, String name, String desc, String signature, String[] exceptions, BasicVerifier verifier){
            super(Opcodes.ASM5, access, name, desc, signature, exceptions);
            this.ownerName = ownerName;
            this.verifier = verifier;
        }

        @Override
        public void visitEnd() {
            super.visitEnd();

            Analyzer<BasicValue> a = new Analyzer<BasicValue>(verifier);
            try {
                a.analyze(ownerName, this);
            } catch (AnalyzerException forwarded) {
                throw new RuntimeException("Error trying to analyze " + ownerName, forwarded);
            }
        }
    }

    private static class IsAssignableDifferencesVerifier extends SimpleVerifier {

        private PatchedSimpleVerifier patchedSimpleVerifier;
        private NonClassloadingSimpleVerifier nonClassloadingSimpleVerifier;
        private Set<Difference> differences = new HashSet<Difference>();

        IsAssignableDifferencesVerifier(PatchedSimpleVerifier patchedSimpleVerifier, NonClassloadingSimpleVerifier nonClassloadingSimpleVerifier) {
            this.patchedSimpleVerifier = patchedSimpleVerifier;
            this.nonClassloadingSimpleVerifier = nonClassloadingSimpleVerifier;
        }

        @Override
        protected boolean isAssignableFrom(Type t, Type u) {
            boolean simpleVerifierResult = super.isAssignableFrom(t, u);

            boolean patchedVerifierResult = !simpleVerifierResult;
            Exception patchedVerifierException = null;
            try {
                patchedVerifierResult = patchedSimpleVerifier.isAssignableFrom2(t, u);

            } catch (Exception e) {
                patchedVerifierException = e;
            }

            boolean nonClassloadingResult = !simpleVerifierResult;
            Exception nonClassloadingException = null;
            try {
                nonClassloadingResult = nonClassloadingSimpleVerifier.isAssignableFrom(t, u);

            } catch (Exception e) {
                nonClassloadingException = e;
            }

            boolean classResult = getClass(t).isAssignableFrom(getClass(u));

            if (patchedVerifierException != null || nonClassloadingException != null) {
                differences.add(new IsAssignableException(t, u, patchedVerifierException, nonClassloadingException));
            } else if (simpleVerifierResult != patchedVerifierResult || simpleVerifierResult != nonClassloadingResult) {
                differences.add(new IsAssignableResultDifference(t, u, simpleVerifierResult, classResult, patchedVerifierResult, nonClassloadingResult));
            }

            return simpleVerifierResult;
        }

    }

    private static abstract class Difference {
        final Type t, u;

        Difference(Type t, Type u) {
            this.t = t;
            this.u = u;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Difference that = (Difference) o;

            return t.equals(that.t) && u.equals(that.u);

        }

        @Override
        public int hashCode() {
            int result = t.hashCode();
            result = 31 * result + u.hashCode();
            return result;
        }
    }

    private static final class IsAssignableException extends Difference {
        final Exception patchedVerifierException, nonClassLoadingException;


        private IsAssignableException(Type t, Type u, Exception patchedVerifierException, Exception nonClassLoadingException) {
            super(t, u);
            this.patchedVerifierException = patchedVerifierException;
            this.nonClassLoadingException = nonClassLoadingException;
        }

        @Override
        public String toString() {
            return "IsAssignableException{" +
                "t=" + t +
                ", u=" + u +
                "patchedVerifierException=" + patchedVerifierException +
                ", nonClassLoadingException=" + nonClassLoadingException +
                '}';
        }
    }

    private static final class IsAssignableResultDifference extends Difference {
        final boolean simpleVerifierResult, classResult, patchedVerifierResult, nonClassloadingVerifierResult;

        IsAssignableResultDifference(Type t, Type u, boolean simpleVerifierResult, boolean classResult, boolean patchedVerifierResult, boolean nonClassloadingVerifierResult) {
            super(t, u);
            this.simpleVerifierResult = simpleVerifierResult;
            this.classResult = classResult;
            this.patchedVerifierResult = patchedVerifierResult;
            this.nonClassloadingVerifierResult = nonClassloadingVerifierResult;
        }


        @Override
        public String toString() {
            return "IsAssignableResultDifference{" +
                "t=" + t +
                ", u=" + u +
                ", simpleVerifierResult=" + simpleVerifierResult +
                ", classResult=" + classResult +
                ", patchedVerifierResult=" + patchedVerifierResult +
                ", nonClassloadingVerifierResult=" + nonClassloadingVerifierResult +
                '}';
        }
    }
}
