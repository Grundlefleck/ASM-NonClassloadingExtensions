package org.mutabilitydetector.asm.typehierarchy;

import org.objectweb.asm.Type;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.objectweb.asm.Type.getType;

/**
 * Wraps a {@link Type} to provide equivalents of methods found on {@link Class}, e.g. isInterface, getSuperClass,
 * but without loading any instances of Class.
 * <br>
 * TypeHierarchy aims to provide an API that is equivalent to a subset of java.lang.Class. TypeHierarchy should provide
 * the same results as if the equivalent method is called on the relevant instances of Class.
 *
 * @see Class#isInterface()
 * @see Class#isArray()
 * @see Class#isAssignableFrom(Class)
 * @see Class#getInterfaces()
 * @see Class#getSuperclass()
 */
public class TypeHierarchy {

    private static final List<Type> IMPLEMENTS_NO_INTERFACES = Collections.emptyList();
    private static final List<Type> IMPLICIT_ARRAY_INTERFACES = unmodifiableList(
            asList(getType(Cloneable.class), getType(Serializable.class)));
    public static final TypeHierarchy JAVA_LANG_OBJECT = new TypeHierarchy(Type.getType(Object.class),
            null,
            IMPLEMENTS_NO_INTERFACES,
            false);

    public static final TypeHierarchy BOOLEAN_HIERARCHY = typeHierarchyForPrimitiveType(Type.BOOLEAN_TYPE);
    public static final TypeHierarchy BYTE_HIERARCHY = typeHierarchyForPrimitiveType(Type.BYTE_TYPE);
    public static final TypeHierarchy CHAR_HIERARCHY = typeHierarchyForPrimitiveType(Type.CHAR_TYPE);
    public static final TypeHierarchy SHORT_HIERARCHY = typeHierarchyForPrimitiveType(Type.SHORT_TYPE);
    public static final TypeHierarchy INT_HIERARCHY = typeHierarchyForPrimitiveType(Type.INT_TYPE);
    public static final TypeHierarchy LONG_HIERARCHY = typeHierarchyForPrimitiveType(Type.LONG_TYPE);
    public static final TypeHierarchy FLOAT_HIERARCHY = typeHierarchyForPrimitiveType(Type.FLOAT_TYPE);
    public static final TypeHierarchy DOUBLE_HIERARCHY = typeHierarchyForPrimitiveType(Type.DOUBLE_TYPE);
    public static final TypeHierarchy VOID_HIERARCHY = typeHierarchyForPrimitiveType(Type.VOID_TYPE);

    public static TypeHierarchy hierarchyForArrayOfType(Type t) {
        return new TypeHierarchy(t, JAVA_LANG_OBJECT.type(), IMPLICIT_ARRAY_INTERFACES, false);
    }

    /**
     * Use constant values declared in this class as an alternative.
     *
     * @see TypeHierarchy#BOOLEAN_HIERARCHY
     * @see TypeHierarchy#BYTE_HIERARCHY
     * @see TypeHierarchy#CHAR_HIERARCHY
     * @see TypeHierarchy#SHORT_HIERARCHY
     * @see TypeHierarchy#INT_HIERARCHY
     * @see TypeHierarchy#LONG_HIERARCHY
     * @see TypeHierarchy#FLOAT_HIERARCHY
     * @see TypeHierarchy#DOUBLE_HIERARCHY
     * @see TypeHierarchy#VOID_HIERARCHY
     */
    private static TypeHierarchy typeHierarchyForPrimitiveType(Type primitiveType) {
        return new TypeHierarchy(primitiveType, null, IMPLEMENTS_NO_INTERFACES, false);
    }

    private final Type thisType;
    private final Type superClass;
    private final List<Type> interfaces;
    private final boolean isInterface;

    public TypeHierarchy(
        Type thisType,
        Type superClass,
        List<Type> interfaces,
        boolean isInterface)
    {
        this.thisType = thisType;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.isInterface = isInterface;
    }

    public Type type() {
        return thisType;
    }

    public boolean representsType(Type t) {
        return t.equals(thisType);
    }

    /**
     * Equivalent to {@link Class#isInterface()}.
     *
     * @see Class#isInterface()
     */
    public boolean isInterface() {
        return isInterface;
    }

    /**
     * Equivalent to {@link Class#isArray()}
     *
     * @see Class#isArray()
     */
    public boolean isArray() {
        return thisType.getSort() == Type.ARRAY;
    }


    /**
     * Equivalent to {@link Class#getSuperclass()}.
     *
     * For a given {@link Class} 'A extends B', and the {@link Type} that represents it, 'A', returns the Type
     * that represents Class B.
     *
     * @see Class#getSuperclass()
     */
    public Type getSuperClass() {
        return superClass;
    }


    /**
     * Equivalent to {@link Class#getInterfaces()}.
     *
     * @see Class#getInterfaces()
     */
    public List<Type> getInterfaces() {
        return interfaces;
    }

    /**
     * Equivalent to {@link Class#isAssignableFrom(Class)}.
     * <br>
     * Uses the given {@link TypeHierarchyReader} to obtain information about any types that are needed to determine
     * if this type is assignable from the given type. This can include superclasses and interfaces.
     *
     * @see Class#isAssignableFrom(Class)
     */
    public boolean isAssignableFrom(Type type, TypeHierarchyReader reader) {
        return isAssignableFrom(reader.hierarchyOf(type), reader);
    }

    /**
     * Equivalent to {@link Class#isAssignableFrom(Class)}.
     * <br>
     * Uses the given {@link TypeHierarchyReader} to obtain information about any types that are needed to determine
     * if this type is assignable from the given type. This can include superclasses and interfaces.
     *
     * @see Class#isAssignableFrom(Class)
     */
    public boolean isAssignableFrom(
        TypeHierarchy u,
        TypeHierarchyReader typeHierarchyReader)
    {
        if (assigningToObject()) {
            return true;
        }

        if (this.isSameType(u)) {
            return true;
        } else if (this.isSuperTypeOf(u)) {
            return true;
        } else if (this.isInterfaceImplementedBy(u)) {
            return true;
        } else if (bothAreArrayTypes(u) && haveSameDimensionality(u)) {
            return (JAVA_LANG_OBJECT.representsType(typeOfArray()) && u.isReferenceArrayType())
                || arrayTypeIsAssignableFrom(u, typeHierarchyReader);
        } else if (bothAreArrayTypes(u)
                && isObjectArrayWithSmallerDimensionalityThan(u))
        {
            return true;
        } else if (u.extendsObject() && !u.implementsAnyInterfaces()) {
            return false;
        }

        if (u.hasSuperClass()
                && isAssignableFrom(u.getSuperClass(), typeHierarchyReader))
        {
            return true;
        } else if (u.implementsAnyInterfaces()
                && isAssignableFromAnyInterfaceImplementedBy(u,
                        typeHierarchyReader))
        {
            return true;
        }

        return false;
    }

    private boolean assigningToObject() {
        return JAVA_LANG_OBJECT.representsType(type());
    }

    private boolean isAssignableFromAnyInterfaceImplementedBy(
        TypeHierarchy u,
        TypeHierarchyReader typeHierarchyReader)
    {
        for (Type ui : u.interfaces) {
            if (isAssignableFrom(ui, typeHierarchyReader)) {
                return true;
            }
        }
        return false;
    }

    private boolean haveSameDimensionality(TypeHierarchy u) {
        return arrayDimensionality() == u.arrayDimensionality();
    }

    private boolean isObjectArrayWithSmallerDimensionalityThan(TypeHierarchy u) {
        return JAVA_LANG_OBJECT.representsType(typeOfArray())
                && arrayDimensionality() <= u.arrayDimensionality();
    }

    private boolean arrayTypeIsAssignableFrom(
        TypeHierarchy u,
        TypeHierarchyReader reader)
    {

        TypeHierarchy thisArrayType = reader.hierarchyOf(typeOfArray());
        return typeOfArray().getSort() == u.typeOfArray().getSort()
            && thisArrayType.isAssignableFrom(reader.hierarchyOf(u.typeOfArray()), reader);
    }

    private boolean bothAreArrayTypes(TypeHierarchy u) {
        return this.isArray() && u.isArray();
    }

    private Type typeOfArray() {
        return Type.getType(thisType.getInternalName()
                .substring(thisType.getDimensions()));
    }

    private int arrayDimensionality() {
        return thisType.getDimensions();
    }

    public boolean isReferenceArrayType() {
        return isArray() && typeOfArray().getSort() == Type.OBJECT;
    }

    public boolean isInterfaceImplementedBy(TypeHierarchy u) {
        return u.interfaces.contains(type());
    }

    public boolean isSuperTypeOf(TypeHierarchy u) {
        return type().equals(u.getSuperClass());
    }

    public boolean hasSuperClass() {
        return getSuperClass() != null
                && !JAVA_LANG_OBJECT.representsType(getSuperClass());
    }

    public boolean implementsAnyInterfaces() {
        return !interfaces.isEmpty();
    }

    public boolean extendsObject() {
        return getSuperClass() != null
                && JAVA_LANG_OBJECT.representsType(getSuperClass());
    }

    public boolean isSameType(TypeHierarchy u) {
        return u.type().equals(type());
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * thisType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }

        TypeHierarchy other = (TypeHierarchy) obj;
        return thisType.equals(other.thisType);
    }

    @Override
    public String toString() {
        return String.format("%s [type=%s]",
                getClass().getSimpleName(),
                thisType.toString());
    }

}
