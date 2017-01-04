package org.mutabilitydetector.asm.typehierarchy;

import com.google.classpath.ClassPath;
import com.google.classpath.ClassPathFactory;
import com.google.classpath.RegExpResourceFilter;
import junit.framework.TestCase;
import org.mutabilitydetector.asm.tree.analysis.NonClassloadingSimpleVerifier;
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
import org.objectweb.asm.tree.analysis.SimpleVerifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.classpath.RegExpResourceFilter.ANY;
import static com.google.classpath.RegExpResourceFilter.ENDS_WITH_CLASS;

public class CanCalculateFramesForEveryJdkRuntimeClassTest extends TestCase {

    private ClassPath classPath = new ClassPathFactory().createFromPath(System.getProperty("java.home") + "/lib/rt.jar");
    private RegExpResourceFilter regExpResourceFilter = new RegExpResourceFilter(ANY, ENDS_WITH_CLASS);
    private String[] findResources = classPath.findResources("", regExpResourceFilter);
    private final IsAssignableDifferencesVerifier verifier = new IsAssignableDifferencesVerifier(
        new NonClassloadingSimpleVerifier());


    public void ignore_testWithClassesBeingLoadedWithSimpleVerifier() throws Exception {
        for (String resourcePath: findResources) {
            new ClassReader(ClassLoader.getSystemResourceAsStream(resourcePath)).accept(new DelegatesToMethodVisitor(new SimpleVerifier()), 0);
        }
    }

    public void ignore_testWithoutLoadingClasses() throws Exception {
        for (String resourcePath: findResources) {
            new ClassReader(ClassLoader.getSystemResourceAsStream(resourcePath)).accept(new DelegatesToMethodVisitor(new NonClassloadingSimpleVerifier()), 0);
        }
    }

    public void ignore_testThereAreNoDifferences() throws Exception {
        for (String resourcePath: findResources) {
            new ClassReader(ClassLoader.getSystemResourceAsStream(resourcePath)).accept(new DelegatesToMethodVisitor(verifier), 0);
        }

        assertNoDifferences(verifier.differences);
    }

    public void ignore_testFailingClass() throws Exception {
        verifier.isAssignableFrom(Type.getType("[Ljava/lang/Object;"), Type.getType("[Z"));
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

    private static class DelegatesToMethodVisitor extends ClassVisitor {

        private String ownerName;
        private BasicVerifier verifier;

        DelegatesToMethodVisitor(BasicVerifier verifier) {
            super(Opcodes.ASM5, new InnerClassVisitor(verifier));
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

    private static final class InnerClassVisitor extends ClassVisitor {
        private final BasicVerifier verifier;
        private String ownerName;

        InnerClassVisitor(BasicVerifier verifier) {
            super(Opcodes.ASM5);
            this.verifier = verifier;
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(name, outerName, innerName, access);
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

        private NonClassloadingSimpleVerifier nonClassloadingSimpleVerifier;
        private Set<Difference> differences = new HashSet<Difference>();

        IsAssignableDifferencesVerifier(NonClassloadingSimpleVerifier nonClassloadingSimpleVerifier) {
            this.nonClassloadingSimpleVerifier = nonClassloadingSimpleVerifier;
        }

        @Override
        protected boolean isAssignableFrom(Type t, Type u) {
            boolean simpleVerifierResult = super.isAssignableFrom(t, u);

            boolean nonClassloadingResult = !simpleVerifierResult;
            Exception nonClassloadingException = null;
            try {
                nonClassloadingResult = nonClassloadingSimpleVerifier.isAssignableFrom(t, u);

            } catch (Exception e) {
                nonClassloadingException = e;
            }

            boolean classResult = getClass(t).isAssignableFrom(getClass(u));

            if (nonClassloadingException != null) {
                differences.add(new IsAssignableException(t, u, nonClassloadingException));
            } else if (simpleVerifierResult != nonClassloadingResult) {
                differences.add(new IsAssignableResultDifference(t, u, simpleVerifierResult, classResult, nonClassloadingResult));
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
        final Exception nonClassLoadingException;


        private IsAssignableException(Type t, Type u, Exception nonClassLoadingException) {
            super(t, u);
            this.nonClassLoadingException = nonClassLoadingException;
        }

        @Override
        public String toString() {
            return "IsAssignableException{" +
                "t=" + t +
                ", u=" + u +
                ", nonClassLoadingException=" + nonClassLoadingException +
                '}';
        }
    }

    private static final class IsAssignableResultDifference extends Difference {
        final boolean simpleVerifierResult, classResult, nonClassloadingVerifierResult;

        IsAssignableResultDifference(Type t, Type u, boolean simpleVerifierResult, boolean classResult, boolean nonClassloadingVerifierResult) {
            super(t, u);
            this.simpleVerifierResult = simpleVerifierResult;
            this.classResult = classResult;
            this.nonClassloadingVerifierResult = nonClassloadingVerifierResult;
        }


        @Override
        public String toString() {
            return "IsAssignableResultDifference{" +
                "t=" + t +
                ", u=" + u +
                ", simpleVerifierResult=" + simpleVerifierResult +
                ", classResult=" + classResult +
                ", nonClassloadingVerifierResult=" + nonClassloadingVerifierResult +
                '}';
        }
    }
}
