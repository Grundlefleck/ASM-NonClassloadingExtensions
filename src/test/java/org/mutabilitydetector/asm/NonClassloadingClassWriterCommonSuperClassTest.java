package org.mutabilitydetector.asm;

import junit.framework.TestCase;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class NonClassloadingClassWriterCommonSuperClassTest extends TestCase {

    public void testGetCommonSuperClass_shouldBeObjectForUnrelatedClasses() throws Exception {
        assertCommonSuperclass(Object.class, Superclass.class, UnrelatedType.class);
    }

    public void testGetCommonSuperClass_shouldBeClosestSharedSuperclass() throws Exception {
        assertCommonSuperclass(Superclass.class, Subclass.class, OtherSubclass.class);
    }

    public void testGetCommonSuperClass_shouldBeSameTypeWhenBothAreEqual() throws Exception {
        assertCommonSuperclass(UnrelatedType.class, UnrelatedType.class, UnrelatedType.class);
    }

    public void testGetCommonSuperClass_shouldBeSuperclassOfTwoGivenTypes() throws Exception {
        assertCommonSuperclass(Superclass.class, Superclass.class, Subclass.class);
    }

    public void testGetCommonSuperClass_shouldBeObjectForUnrelatedInterfaces() throws Exception {
        assertCommonSuperclass(Object.class, Interface.class, OtherInterface.class);
    }

    public void testGetCommonSuperClass_shouldBeClosestSharedInterface() throws Exception {
        assertCommonSuperclass(Object.class, ImplementsSeveralInterfaces.class, AlsoImplementsSubInterface.class);
    }

    public void testGetCommonSuperClass_shouldBeObjectForTwoInterfacesWhoShareCommonSuperInterface() throws Exception {
        assertCommonSuperclass(Object.class, SubInterface.class, OtherSubInterface.class);
    }

    private void assertCommonSuperclass(Class<?> expected, Class<?> first, Class<?> second) {
        String expectedType = slashedName(expected);
        String type1 = slashedName(first);
        String type2 = slashedName(second);
        assertEquals("Assertion is not consistent with ClassWriter.getCommonSuperClass",
            expectedType, new MoreVisibleClassWriter().getCommonSuperClass(type1, type2));
        assertEquals("TypeHierarchyReader is not consistent with ClassWriter.getCommonSuperClass",
            expectedType, new MoreVisibleNonClassloadingClassWriter().getCommonSuperClass(type1, type2));
    }

    private String slashedName(Class<?> cls) {
        return cls.getName().replace(".", "/");
    }
    
    static class UnrelatedType { }
    
    static class Superclass { }
    static class Subclass extends Superclass { }
    static class OtherSubclass extends Superclass { }

    interface Interface { }
    interface OtherInterface { }

    interface SuperInterface { }
    interface SubInterface extends SuperInterface { }
    interface OtherSubInterface { }
    
    static class ImplementsSeveralInterfaces implements Interface, SubInterface { }
    static class AlsoImplementsSubInterface implements SubInterface { }


    // extends ClassWriter in order to make getCommonSuperClass visible in test

    private static class MoreVisibleNonClassloadingClassWriter extends NonClassloadingClassWriter {

        MoreVisibleNonClassloadingClassWriter() {
            super(Opcodes.ASM5);
        }

        @Override
        public String getCommonSuperClass(String type1, String type2) {
            return super.getCommonSuperClass(type1, type2);
        }
    }

    private static class MoreVisibleClassWriter extends ClassWriter {

        MoreVisibleClassWriter() {
            super(Opcodes.ASM5);
        }

        @Override
        public String getCommonSuperClass(String type1, String type2) {
            return super.getCommonSuperClass(type1, type2);
        }
    }
}
