package org.mutabilitydetector.asm.typehierarchy;

import junit.framework.TestCase;
import org.mutabilitydetector.asm.typehierarchy.TypeHierarchyReader;
import org.objectweb.asm.Type;

import java.io.Serializable;

public class TypeHierarchyReaderConsistentWithJavaLangClassTest extends TestCase {
    
    private final TypeHierarchyReader typeHierarchyReader = new TypeHierarchyReader();
    
    public void testSuperClassOfConcreteClassExtendingObjectImplicitlyIsTypeRepresentingJavaLangObject() {
        assertSuperClass(Object.class, UnrelatedType.class);
    }

    public void testSuperClassOfSubclass() {
        assertSuperClass(Superclass.class, Subclass.class);
    }

    public void testSuperClassOfInterfaceWithNoSuperInterfaceIsObject() {
        assertSuperClass(null, Interface.class);
    }

    public void testSuperClassOfSubInterfaceIsJavaLangObject() {
        assertSuperClass(null, SubInterface.class);
    }
    
    public void testSuperclassOfArrayClassHasSameSemanticsAsJavaLangClass_getSuperClass() throws Exception {
        assertSuperClass(Object.class, Object[].class);
        assertSuperClass(Object.class, Interface[].class);
        assertSuperClass(Object.class, Superclass[].class);
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

    public void testArrayDimensionAssignment() throws Exception {
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

    public void testAssignmentOfPrimitiveArrayTypesToObjectType() throws Exception {
        assertIsNotAssignableFrom(Object[].class, boolean[].class);
        assertIsNotAssignableFrom(Object[].class, byte[].class);
        assertIsNotAssignableFrom(Object[].class, char[].class);
        assertIsNotAssignableFrom(Object[].class, short[].class);
        assertIsNotAssignableFrom(Object[].class, int[].class);
        assertIsNotAssignableFrom(Object[].class, long[].class);
        assertIsNotAssignableFrom(Object[].class, float[].class);
        assertIsNotAssignableFrom(Object[].class, double[].class);
    }

    public void testAssignmentOfObjectArrayToPrimitiveArrayTypes() throws Exception {
        assertIsNotAssignableFrom(boolean[].class, Object[].class);
        assertIsNotAssignableFrom(byte[].class, Object[].class);
        assertIsNotAssignableFrom(char[].class, Object[].class);
        assertIsNotAssignableFrom(short[].class, Object[].class);
        assertIsNotAssignableFrom(int[].class, Object[].class);
        assertIsNotAssignableFrom(long[].class, Object[].class);
        assertIsNotAssignableFrom(float[].class, Object[].class);
        assertIsNotAssignableFrom(double[].class, Object[].class);
    }

    private void assertSuperClass(Class<?> expectedSuperClass, Class<?> subclass) {
        assertEquals("Assertion is not consistent with Class.getSuperclass",
             expectedSuperClass, subclass.getSuperclass());
        Type superclassType = expectedSuperClass == null ? null : Type.getType(expectedSuperClass);
        Type subclassType = Type.getType(subclass);
        Type actualSuperclassType = typeHierarchyReader.getSuperClass(subclassType);
        assertTrue("Type Hierarchy visitor is not consistent with Class.getSuperclass",
            actualSuperclassType == null ? superclassType == null : actualSuperclassType.equals(superclassType));
    }

    private void assertIsAssignableFrom(Class<?> to, Class<?> from) {
        assertTrue("Assertion is not consistent with Class.isAssignableFrom", to.isAssignableFrom(from));
        Type toType = Type.getType(to);
        Type fromType = Type.getType(from);
        assertTrue("Type Hierarchy visitor is not consistent with Class.isAssignableFrom",
                typeHierarchyReader.isAssignableFrom(toType, fromType));
    }

    private void assertIsNotAssignableFrom(Class<?> to, Class<?> from) {
        assertFalse("Assertion is not consistent with Class.isAssignableFrom", to.isAssignableFrom(from));
        Type toType = Type.getType(to);
        Type fromType = Type.getType(from);
        assertFalse("Type Hierarchy visitor is not consistent with Class.isAssignableFrom",
                typeHierarchyReader.isAssignableFrom(toType, fromType));
    }
    

    static class AssignableFromItself { }
    
    static class UnrelatedType { }
    
    static class Superclass { }
    static class Subclass extends Superclass { }
    static class OtherSubclass extends Superclass { }
    static class SubSubclass extends Subclass { }
    
    interface Interface { }
    static class ImplementsInterface implements Interface { }
    static class ExtendsImplementsInterface extends ImplementsInterface { }
    
    interface SuperInterface { }
    interface SubInterface extends SuperInterface { }

    static class ImplementsSeveralInterfaces implements Interface, SubInterface { }
    static class OtherImplementsInterface implements Interface { }
    static class ExtendsClassOutwithInterfaceHierarchy extends UnrelatedType implements SubInterface { }
    
}
