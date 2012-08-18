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

import static org.objectweb.asm.Type.getType;

import java.io.Serializable;

import junit.framework.TestCase;

import org.objectweb.asm.Type;


public class SimpleVerifierAssignabilityUnitTest extends TestCase {
    
    private final SimpleVerifier verifier = new SimpleVerifier();

    public void testSuperClassOfObjectIsNull() {
        assertSuperClass(null, Object.class);
    }

    public void testSuperClassOfConcreteClassExtendingObjectImplicitlyIsTypeRepresentingJavaLangObject() {
        assertSuperClass(Object.class, UnrelatedType.class);
    }

    public void testSuperClassOfSubclass() {
        assertSuperClass(Superclass.class, Subclass.class);
    }

    public void testSuperClassOfInterfaceWithNoSuperInterfaceIsNull() {
        assertSuperClass(null, Interface.class);
    }

    public void testSuperClassOfSubInterfaceIsNull() {
        assertSuperClass(null, SubInterface.class);
    }
 
    public void testSuperclassOfArrayClassHasSameSemanticsAsJavaLangClass_getSuperClass() throws Exception {
        assertSuperClass(Object[].class.getSuperclass(), Object[].class);
        assertSuperClass(Interface[].class.getSuperclass(), Interface[].class);
        assertSuperClass(Superclass[].class.getSuperclass(), Superclass[].class);
    }
    
    public void testClassIsAssignableFromItself() {
        assertIsAssignableFrom(AssignableFromItself.class, AssignableFromItself.class);
    }

    public void testClassIsNotAssignableToUnrelatedType() {
        assertIsNotAssignableFrom(AssignableFromItself.class, UnrelatedType.class);
        assertIsNotAssignableFrom(UnrelatedType.class, AssignableFromItself.class);
    }
    
    public void testSuperclassIsAssignableFromSubclass() throws Exception {
        assertIsAssignableFrom(Superclass.class, Subclass.class);
    }
    
    public void testIndirectSubclassIsAssignableToSuperclass() throws Exception {
        assertIsAssignableFrom(Superclass.class, SubSubclass.class);
    }
    
    public void testSubclassIsNotAssignableToOtherClassWithSameSuperclass() throws Exception {
        assertIsNotAssignableFrom(Subclass.class, OtherSubclass.class);
    }

    public void testSubclassIsNotAssignableFromSuperclass() throws Exception {
        assertIsNotAssignableFrom(Subclass.class, Superclass.class);
    }
    
    public void testInterfaceIsAssignableFromImplementingClass() throws Exception {
        assertIsAssignableFrom(Interface.class, ImplementsInterface.class);
    }
    
    public void testInterfaceIsAssignableFromSubclassOfImplementingClass() throws Exception {
        assertIsAssignableFrom(Interface.class, ExtendsImplementsInterface.class);
        assertIsNotAssignableFrom(ExtendsImplementsInterface.class, Interface.class);
    }
    
    public void ignore_testSuperInterfaceIsAssignableFromSubInterface() throws Exception {
        assertIsAssignableFrom(SuperInterface.class, SubInterface.class);
        assertIsNotAssignableFrom(SubInterface.class, SuperInterface.class);
    }
    
    public void testImplementingClassIsNotAssignableFromInterface() throws Exception {
        assertIsNotAssignableFrom(ImplementsInterface.class, Interface.class);
    }
    
    public void testObjectIsAssignableFromAnything() throws Exception {
        assertIsAssignableFrom(Object.class, Superclass.class);
        assertIsAssignableFrom(Object.class, Subclass.class);
        assertIsAssignableFrom(Object.class, Interface.class);
        assertIsAssignableFrom(Object.class, SubInterface.class);
    }
    
    public void testAllImplementedInterfacesAreAssignableFromImplementingClass() throws Exception {
        assertIsAssignableFrom(Interface.class, ImplementsSeveralInterfaces.class);
        assertIsAssignableFrom(SubInterface.class, ImplementsSeveralInterfaces.class);
        assertIsAssignableFrom(SuperInterface.class, ImplementsSeveralInterfaces.class);
        assertIsNotAssignableFrom(OtherImplementsInterface.class, ImplementsSeveralInterfaces.class);
    }
    
    public void testInterfaceIsAssignableFromClassWithSuperclassOutwithInterfaceHierarchy() throws Exception {
        assertIsAssignableFrom(SuperInterface.class, ExtendsClassOutwithInterfaceHierarchy.class);
    }
    
    public void testArrayTypeAssignment() throws Exception {
        assertIsAssignableFrom(Object.class, Interface[].class);
        assertIsAssignableFrom(Cloneable.class, Interface[].class);
        assertIsAssignableFrom(Serializable.class, Interface[].class);
        assertIsAssignableFrom(Object[].class, Interface[].class);
        assertIsAssignableFrom(Object[].class, Interface[].class);
        assertIsAssignableFrom(Interface[].class, Interface[].class);
        assertIsAssignableFrom(Interface[].class, ImplementsInterface[].class);
        assertIsNotAssignableFrom(ImplementsInterface[].class, Interface[].class);
        assertIsAssignableFrom(Interface[].class, ExtendsImplementsInterface[].class);
        assertIsAssignableFrom(Object[].class, Superclass[].class);
        assertIsAssignableFrom(Object[].class, Subclass[].class);
        assertIsAssignableFrom(Superclass[].class, Subclass[].class);
        assertIsNotAssignableFrom(Subclass[].class, Superclass[].class);
    }
    
    public void ignore_testArrayDimensionAssignment() throws Exception {
        assertIsAssignableFrom(Object.class, Object[].class);
        assertIsNotAssignableFrom(Object[].class, Object.class);
        assertIsAssignableFrom(Object.class, Interface[].class);
        assertIsAssignableFrom(Object[].class, Interface[][].class);
        assertIsAssignableFrom(Object[][].class, Interface[][].class);
        assertIsNotAssignableFrom(Interface.class, Interface[].class);
        assertIsNotAssignableFrom(Interface[].class, Interface.class);
        assertIsNotAssignableFrom(Interface[].class, Interface[][].class);
        assertIsNotAssignableFrom(Interface[][].class, Interface[].class);
    }
    
    public void testAnonymousInnerClasses() throws Exception {
        assertIsAssignableFrom(Interface.class, new Interface() { }.getClass());
        assertIsNotAssignableFrom(new Interface() { }.getClass(), Interface.class);
    }

    public void testAssignmentOfPrimitiveArrayTypes() throws Exception {
        assertIsAssignableFrom(boolean[].class, boolean[].class);
        assertIsAssignableFrom(byte[].class, byte[].class);
        assertIsAssignableFrom(char[].class, char[].class);
        assertIsAssignableFrom(short[].class, short[].class);
        assertIsAssignableFrom(int[].class, int[].class);
        assertIsAssignableFrom(long[].class, long[].class);
        assertIsAssignableFrom(float[].class, float[].class);
        assertIsAssignableFrom(double[].class, double[].class);
    }
   
    public void testMergingTwoBasicValuesRepresentingObjectResultsInObjectBasicValue() {
        assertMergeResult(Object.class, Object.class, Object.class);
    }

    public void testMergingUnrelatedClassTypesResultsInObjectBasicValue() {
        assertMergeResult(Object.class, Superclass.class, UnrelatedType.class);
    }

    public void ignore_testMergingUnrelatedInterfaceTypesResultsInObjectBasicValue() {
        assertMergeResult(Object.class, Interface.class, OtherInterface.class);
        assertMergeResult(Object.class, OtherInterface.class, Interface.class);
    }

    public void testMergingObjectArrayTypesResultsInObjectArrayBasicValue() {
        assertMergeResult(Object[].class, Object[].class, Object[].class);
    }
    
    public void testMergingObjectArrayTypeAndOtherClassArrayTypeResultsInObjectArrayBasicValue() {
        assertMergeResult(Object[].class, Object[].class, Superclass[].class);
        assertMergeResult(Object[].class, Superclass[].class, Object[].class);
    }

    public void ignore_testMergingInterfaceArrayTypeAndUnrelatedInterfaceArrayTypeResultsInObjectArrayBasicValue() {
        assertMergeResult(Object[].class, Interface[].class, OtherInterface[].class);
        assertMergeResult(Object[].class, Superclass[].class, Interface[].class);
    }

    public void testMergingSuperclassAndSubclassResultsInSuperclassBasicValue() {
        assertMergeResult(Superclass.class, Superclass.class, Subclass.class);
        assertMergeResult(Superclass.class, Subclass.class, Superclass.class);
    }

    public void ignore_testMergingClassTypesWhichImplementSameInterfaceResultsInInterfaceBasicValue() {
        assertMergeResult(Interface.class, ImplementsInterface.class, ImplementsSeveralInterfaces.class);
    }
    
    public void testMergingMultidimensionalArray() {
        assertMergeResult(Superclass[][].class, Superclass[][].class, Subclass[][].class);
    }
    
    
    private void assertMergeResult(Class<?> expected, Class<?> first, Class<?> second) {
        BasicValue expectedBasicValue = new BasicValue(getType(expected));
        
        assertEquals("Verifier produced incorrect merge result.",
                expectedBasicValue, verifier.merge(new BasicValue(getType(first)), new BasicValue(getType(second))));
        
    }

    private void assertIsAssignableFrom(Class<?> to, Class<?> from) {
        assertTrue("Assertion is not consistent with Class.isAssignableFrom", to.isAssignableFrom(from));
        Type toType = Type.getType(to);
        Type fromType = Type.getType(from);
        assertTrue("Verifier is not consistent with Class.isAssignableFrom", 
                verifier.isAssignableFrom2(toType, fromType));
        assertTrue("Verifier is not consistent with Class.isAssignableFrom", 
                verifier.isAssignableFrom(toType, fromType));
    }

    private void assertIsNotAssignableFrom(Class<?> to, Class<?> from) {
        assertFalse("Assertion is not consistent with Class.isAssignableFrom", to.isAssignableFrom(from));
        Type toType = Type.getType(to);
        Type fromType = Type.getType(from);
        assertFalse("Verifier is not consistent with Class.isAssignableFrom", 
                verifier.isAssignableFrom2(toType, fromType));
        assertFalse("Verifier is not consistent with Class.isAssignableFrom", 
                verifier.isAssignableFrom(toType, fromType));
    }
    
    private void assertSuperClass(Class<?> expected, Class<?> actual) {
        assertEquals("getSuperClass assertion is inconsistent with java.lang.Class", expected, actual.getSuperclass());
        assertEquals(expected == null ?  null : Type.getType(expected), verifier.getSuperClass(getType(actual)));
    }
    
    public static class AssignableFromItself { }
    
    public static class UnrelatedType { }
    
    public static class Superclass { }
    public static class Subclass extends Superclass { }
    public static class OtherSubclass extends Superclass { }
    public static class SubSubclass extends Subclass { }
    
    public static interface Interface { }
    public static interface OtherInterface { }
    public static class ImplementsInterface implements Interface { }
    public static class ExtendsImplementsInterface extends ImplementsInterface { }
    
    public static interface SuperInterface { }
    public static interface SubInterface extends SuperInterface { }
    public static interface OtherSubInterface { }
    
    public static class ImplementsSeveralInterfaces implements Interface, SubInterface { }
    public static class OtherImplementsInterface implements Interface { }
    public static class AlsoImplementsSubInterface implements SubInterface { }
    
    public static class ExtendsClassOutwithInterfaceHierarchy extends UnrelatedType implements SubInterface { }

}
