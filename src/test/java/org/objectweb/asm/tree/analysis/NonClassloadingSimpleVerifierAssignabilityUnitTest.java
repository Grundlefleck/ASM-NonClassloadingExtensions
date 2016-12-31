package org.objectweb.asm.tree.analysis;

import junit.framework.TestCase;
import org.objectweb.asm.Type;

import java.io.Serializable;

import static org.objectweb.asm.Type.getType;


public class NonClassloadingSimpleVerifierAssignabilityUnitTest extends TestCase {
    
    private final NonClassloadingSimpleVerifier nonClassloadingSimpleVerifier = new NonClassloadingSimpleVerifier();
    private final SimpleVerifier simpleVerifier = new SimpleVerifier();

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
    
    public void testSuperInterfaceIsAssignableFromSubInterface() throws Exception {
        assertIsAssignableFrom(SuperInterface.class, SubInterface.class);
        assertIsAssignableFrom(SubInterface.class, SuperInterface.class);
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
    
    public void testArrayDimensionAssignment() throws Exception {
        assertIsAssignableFrom(Object.class, Object[].class);
        assertIsNotAssignableFrom(Object[].class, Object.class);
        assertIsAssignableFrom(Object.class, Interface[].class);
        assertIsAssignableFrom(Object[].class, Interface[][].class);
        assertIsAssignableFrom(Object[][].class, Interface[][].class);
        assertIsAssignableFrom(Interface.class, Interface[].class);
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

    public void testAssignmentOfPrimitiveArrayTypesToArrayOfObject() throws Exception {
        assertIsNotAssignableFrom(Object[].class, boolean[].class);
        assertIsNotAssignableFrom(Object[].class, byte[].class);
        assertIsNotAssignableFrom(Object[].class, char[].class);
        assertIsNotAssignableFrom(Object[].class, short[].class);
        assertIsNotAssignableFrom(Object[].class, int[].class);
        assertIsNotAssignableFrom(Object[].class, long[].class);
        assertIsNotAssignableFrom(Object[].class, float[].class);
        assertIsNotAssignableFrom(Object[].class, double[].class);
    }
   
    public void testMergingTwoBasicValuesRepresentingObjectResultsInObjectBasicValue() {
        assertMergeResult(Object.class, Object.class, Object.class);
    }

    public void testMergingUnrelatedClassTypesResultsInObjectBasicValue() {
        assertMergeResult(Object.class, Superclass.class, UnrelatedType.class);
    }

    public void testMergingUnrelatedInterfaceTypesResultsInObjectBasicValue() {
        assertMergeResult(Interface.class, Interface.class, OtherInterface.class);
        assertMergeResult(OtherInterface.class, OtherInterface.class, Interface.class);
    }

    public void testMergingObjectArrayTypesResultsInObjectArrayBasicValue() {
        assertMergeResult(Object[].class, Object[].class, Object[].class);
    }
    
    public void testMergingObjectArrayTypeAndOtherClassArrayTypeResultsInObjectArrayBasicValue() {
        assertMergeResult(Object[].class, Object[].class, Superclass[].class);
        assertMergeResult(Object[].class, Superclass[].class, Object[].class);
    }

    public void testMergingInterfaceArrayTypeAndUnrelatedInterfaceArrayTypeResultsInObjectArrayBasicValue() {
        assertMergeResult(Object.class, Interface[].class, OtherInterface[].class);
        assertMergeResult(Object.class, Superclass[].class, Interface[].class);
    }

    public void testMergingSuperclassAndSubclassResultsInSuperclassBasicValue() {
        assertMergeResult(Superclass.class, Superclass.class, Subclass.class);
        assertMergeResult(Superclass.class, Subclass.class, Superclass.class);
    }

    public void testMergingClassTypesWhichImplementSameInterfaceResultsInInterfaceBasicValue() {
        assertMergeResult(Object.class, ImplementsInterface.class, ImplementsSeveralInterfaces.class);
    }
    
    public void testMergingMultidimensionalArray() {
        assertMergeResult(Superclass[][].class, Superclass[][].class, Subclass[][].class);
    }
    
    private void assertMergeResult(Class<?> expected, Class<?> first, Class<?> second) {
        BasicValue expectedBasicValue = new BasicValue(getType(expected));

        assertEquals("Assertion is not consistent with SimpleVerifier.merge",
                expectedBasicValue, simpleVerifier.merge(new BasicValue(getType(first)), new BasicValue(getType(second))));
        assertEquals("Verifier produced incorrect merge result.",
                expectedBasicValue, nonClassloadingSimpleVerifier.merge(new BasicValue(getType(first)), new BasicValue(getType(second))));
    }

    private void assertIsAssignableFrom(Class<?> to, Class<?> from) {
        Type toType = Type.getType(to);
        Type fromType = Type.getType(from);
        assertTrue("Assertion is not consistent with SimpleVerifier.isAssignableFrom",
                simpleVerifier.isAssignableFrom(toType, fromType));
        assertTrue("NonClassloadingSimpleVerifier is not consistent with SimpleVerifier.isAssignableFrom",
                nonClassloadingSimpleVerifier.isAssignableFrom(toType, fromType));
    }

    private void assertIsNotAssignableFrom(Class<?> to, Class<?> from) {
        Type toType = Type.getType(to);
        Type fromType = Type.getType(from);
        assertFalse("Assertion is not consistent with SimpleVerifier.isAssignableFrom",
                simpleVerifier.isAssignableFrom(toType, fromType));
        assertFalse("NonClassloadingSimpleVerifier is not consistent with SimpleVerifier.isAssignableFrom",
                nonClassloadingSimpleVerifier.isAssignableFrom(toType, fromType));
    }
    
    private void assertSuperClass(Class<?> expectedSuperClass, Class<?> subClass) {
        Type expectedType = expectedSuperClass == null ? null : Type.getType(expectedSuperClass);
        assertEquals("getSuperClass assertion is inconsistent with SimpleVerifier.getSuperClass",
            expectedType, simpleVerifier.getSuperClass(getType(subClass)));
        assertEquals("NonClassloadingSimpleVerifier.getSuperClass is inconsistent with SimpleVerifier.getSuperClass",
            expectedType, nonClassloadingSimpleVerifier.getSuperClass(getType(subClass)));
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
