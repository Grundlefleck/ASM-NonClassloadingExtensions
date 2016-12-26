package org.example;

import junit.framework.TestCase;
import org.mutabilitydetector.classloadingresolution.ClassloaderDependencyResolution;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Type.getType;

public class ClassloaderDependencyResolutionTest extends TestCase {


    public void testDeepClassHierarchyResolvesUpToObject() {
        List<Type> hierarchy = new ClassloaderDependencyResolution().hierarchy(getType(D.class));

        assertEquals(Arrays.asList(getType(Object.class), getType(A.class), getType(B.class), getType(C.class), getType(D.class)),
                hierarchy);
    }

    public static class A { }
    public static class B extends A { }
    public static class C extends B { }
    public static class D extends C { }

    public void testDeepInterfaceHierarchyResolvesToNoInterface() {
        List<Type> hierarchy = new ClassloaderDependencyResolution().hierarchy(getType(H.class));

        assertEquals(Arrays.asList(getType(Object.class), getType(E.class), getType(F.class), getType(G.class), getType(H.class)),
                hierarchy);
    }

    static interface E {}
    static interface F extends E {}
    static interface G extends F {}
    static interface H extends G {}

    public void testCombinationOfClassAndInterfaceHierarchyAlwaysPutsSupertypesBeforeSubtypes() {
        List<Type> allTypesInClassloadingOrder = new ClassloaderDependencyResolution().hierarchy(getType(All.class));

        for (Type type: allTypesInClassloadingOrder) {
            System.out.println(type);
        }

        assertEquals(allTypesInClassloadingOrder.get(0), getType(Object.class));

        assertOrdered(A.class, B.class, allTypesInClassloadingOrder);
        assertOrdered(B.class, C.class, allTypesInClassloadingOrder);
        assertOrdered(C.class, D.class, allTypesInClassloadingOrder);

        assertOrdered(E.class, F.class, allTypesInClassloadingOrder);
        assertOrdered(F.class, G.class, allTypesInClassloadingOrder);
        assertOrdered(G.class, H.class, allTypesInClassloadingOrder);

        assertOrdered(H.class, All.class, allTypesInClassloadingOrder);
        assertOrdered(D.class, All.class, allTypesInClassloadingOrder);
    }

    static class All extends D implements H {}

    private void assertOrdered(Class<?> expectBefore, Class<?> expectAfter, List<Type> loadingOrder) {
        assertTrue(expectBefore + " should appear in class loading list", loadingOrder.indexOf(getType(expectBefore)) > 0);
        assertTrue(expectAfter + " should appear in class loading list", loadingOrder.indexOf(getType(expectAfter)) > 0);
        assertTrue(expectBefore + "should appear before " + expectAfter, loadingOrder.indexOf(getType(expectBefore)) < loadingOrder.indexOf(getType(expectAfter)));
    }

    public void testCanLoadClassesInReturnedOrder() throws Exception {
        List<Type> allTypesInClassloadingOrder = new ClassloaderDependencyResolution().hierarchy(Type.getType("Lorg/example/ClassloaderDependencyResolutionTest$All;"));

        for (Type type : allTypesInClassloadingOrder) {
            getClass().getClassLoader().loadClass(type.getClassName());
        }
    }

}