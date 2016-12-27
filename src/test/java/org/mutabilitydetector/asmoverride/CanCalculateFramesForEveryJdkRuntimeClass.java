package org.mutabilitydetector.asmoverride;

import com.google.classpath.ClassPath;
import com.google.classpath.ClassPathFactory;
import com.google.classpath.RegExpResourceFilter;
import junit.framework.TestCase;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.NonClassloadingSimpleVerifier;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

import static com.google.classpath.RegExpResourceFilter.ANY;
import static com.google.classpath.RegExpResourceFilter.ENDS_WITH_CLASS;

public class CanCalculateFramesForEveryJdkRuntimeClass extends TestCase {

    ClassPath classPath = new ClassPathFactory().createFromPath("/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/rt.jar");
    RegExpResourceFilter regExpResourceFilter = new RegExpResourceFilter(ANY, ENDS_WITH_CLASS);
    String[] findResources = classPath.findResources("", regExpResourceFilter);

    public void testWithClassesBeingLoaded() throws Exception {
        for (String resourcePath: findResources) {
            new ClassReader(ClassLoader.getSystemResourceAsStream(resourcePath)).accept(new DelegatesToMethodVisitor(new SimpleVerifier()), 0);
        }
    }

    public void testWithoutLoadingClasses() throws Exception {
        for (String resourcePath: findResources) {
            new ClassReader(ClassLoader.getSystemResourceAsStream(resourcePath)).accept(new DelegatesToMethodVisitor(new NonClassloadingSimpleVerifier()), 0);
        }
    }

    static class DelegatesToMethodVisitor extends ClassVisitor {

        private String ownerName;
        private SimpleVerifier verifier;

        public DelegatesToMethodVisitor(SimpleVerifier verifier) {
            super(Opcodes.ASM5);
            this.verifier = verifier;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.ownerName = name;
            System.out.println(ownerName);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new RequiresPopulatedStackFrames(ownerName, access, name, desc, signature, exceptions, verifier);
        }
    }

    static class RequiresPopulatedStackFrames extends MethodNode {
        private String ownerName;
        private SimpleVerifier verifier;

        RequiresPopulatedStackFrames(String ownerName, int access, String name, String desc, String signature, String[] exceptions, SimpleVerifier verifier){
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
                throw new RuntimeException(forwarded);
            }
        }
    }


}
