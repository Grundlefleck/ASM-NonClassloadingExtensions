package org.mutabilitydetector.classloadingresolution;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.TypeHierarchyReader;
import org.objectweb.asm.tree.analysis.TypeHierarchyReader.TypeHierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static org.objectweb.asm.Type.getType;


public class ClassloaderDependencyResolution {

    /**
     * Will return a list of Type, ordered such that iterating the list and loading each class will not result in
     * attempting to load a class whose superclass, or interfaces that it implements, have not been loaded.
     *
     * It does not de-duplicate entries, where a type appears twice in the hierarchy of the given type.
     *
     * c.f. https://twitter.com/ztellman/status/812341264720883712
     *
     */
    public List<Type> hierarchy(Type type) {
        /**
         * Starts with the given type, pushes it on a stack, then traverses all it's supertypes, pushing them on to the
         * same stack. The resultant list will be ordered with the given type last, and all it's supertypes coming in
         * order before it.
         *
         * Excuse the use of java.util.Stack, this particular codebase is not on Java 1.6 (for reasons) and it should
         * really be a java.util.Deque.
         *
         * Alternatively, what you might want to do is adapt this method to produce a tree containing every class that
         * is to be reloaded. Where java.lang.Object is the root, and subclasses are represented as child nodes.
         * Producing the correct order for classloading can then be achieved with a breadth-first search. Using the
         * same tree instances for all the types that need to be reloaded would then give an easier way of
         * de-duplicating classes, i.e. registering a type as a subclass can first search the tree for the presence
         * of its superclass, and be added to it's children. There's nothing specific to bytecode parsing there, so I've
         * left that as an exercise for the reader.
         *
         * The associated classes TypeHierarchyReader/TypeHierarchy are overkill for what you want to do. They needed
         * to be more complicated to support an isAssignableFrom method, but you only really need the bit that pulls
         * out superclass + interfaces. All the stuff for primitive and array types is unnecessary, since they are not
         * something you will load directly. They do have predefined hooks for caching results though, which might be
         * handy.
         */
        Stack<Type> loadingOrder = new Stack<Type>();
        loadingOrder.push(type);

        TypeHierarchy typeHierarchy = new TypeHierarchyReader().hierarchyOf(type);

        if (!typeHierarchy.isInterface()) {
            traverseToObject(typeHierarchy.getSuperType(), loadingOrder);
        }

        traverseInterfaces(typeHierarchy.getInterfaces(), loadingOrder);

        loadingOrder.push(getType(Object.class));

        ArrayList<Type> asList = new ArrayList<Type>(loadingOrder);
        Collections.reverse(asList);
        return asList;

    }

    private Stack<Type> traverseToObject(Type superClass, Stack<Type> loadingOrder) {

        TypeHierarchy superType = new TypeHierarchyReader().hierarchyOf(superClass);
        if ("java/lang/Object".equals(superClass.getInternalName())) {
            return loadingOrder;
        } else {
            loadingOrder.push(superClass);
            return traverseToObject(superType.getSuperType(), loadingOrder);
        }
    }

    private Stack<Type> traverseInterfaces(List<Type> interfaces, Stack<Type> loadingOrder) {
        if (interfaces.isEmpty()) {
            return loadingOrder;
        } else {
            for (Type extendedInterface : interfaces) {
                loadingOrder.push(extendedInterface);
                traverseInterfaces(new TypeHierarchyReader().hierarchyOf(extendedInterface).getInterfaces(), loadingOrder);
            }
            return loadingOrder;
        }
    }
}
