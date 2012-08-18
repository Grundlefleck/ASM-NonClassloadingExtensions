/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.objectweb.asm.tree.analysis;

import junit.framework.TestCase;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public class NonClassloadingSimpleVerifierUnitTest extends TestCase {

	public void testAllowSingleIgnoredTestMethod() throws Exception { }
	
    public void ignore_testDoesntThrowExceptionOnAnalyzing() throws Exception {
        ClassReader classReader = new ClassReader(InvokesMethodWithSeveralImplementingClasses.class.getName());
        classReader.accept(new VerifyEachMethodClassVisitor(), 0);
    }

    public static class InvokesMethodWithSeveralImplementingClasses {
        
        public void method() {
            MyInterface myInterface = new FirstImplementation();
            if (System.currentTimeMillis() % 2 == 0) {
                myInterface = new SecondImplementation();
            }
            
            takesInterface(myInterface);
        }
        
        private void takesInterface(MyInterface m) {
            System.out.println(m);
        }
        
    }
    
    private static interface MyInterface { }
    private static class FirstImplementation implements MyInterface { }
    private static class SecondImplementation implements MyInterface { }
    
    private static class VerifyEachMethodClassVisitor extends ClassVisitor {

        private String owner;

        public VerifyEachMethodClassVisitor() {
            super(Opcodes.ASM4);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.owner = name;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            super.visitMethod(access, name, desc, signature, exceptions);
            return new MethodNodeWithVisitEnd(access, name, desc, signature, exceptions, owner);
        }

    }

    private static class MethodNodeWithVisitEnd extends MethodNode {

        private final String owner;

        public MethodNodeWithVisitEnd(int access, String name, String desc, String signature, String[] exceptions,
                String owner) {
            super(access, name, desc, signature, exceptions);
            this.owner = owner;
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            try {
                new Analyzer<BasicValue>(new NonClassloadingSimpleVerifier()).analyze(owner, this);
            } catch (AnalyzerException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
}
