package org.mutabilitydetector.asm;

import org.mutabilitydetector.asm.typehierarchy.TypeHierarchyReader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;



/**
 * Overrides {@link ClassWriter} to provide an implementation that prevents loading classes.
 * <br>
 * Delegates to an underlying {@link TypeHierarchyReader} to perform the
 * necessary visiting of class files to find the information required for
 * verification.
 *
 * The default implementation of {@link TypeHierarchyReader} will attempt to
 * read classes using a {@link ClassReader} which reads class files using
 * {@link ClassLoader#getSystemResourceAsStream(String)}. Unlike with native
 * classloading, there is no caching used in this verifier, which will almost
 * certainly degrade performance. To maintain performance, supply an alternative
 * {@link TypeHierarchyReader} which can using a caching strategy best suited to
 * your environment.
 *
 * @see ClassReader#ClassReader(String)
 * @see ClassWriter
 * @see Type
 * @see TypeHierarchyReader
 *
 *
 */
public class NonClassloadingClassWriter extends ClassWriter {

    /**
     * Used to obtain hierarchy information used in verification.
     */
    protected final TypeHierarchyReader typeHierarchyReader;

    /**
     * Constructor which chooses a naive {@link TypeHierarchyReader}.
     */
    public NonClassloadingClassWriter(int flags) {
        super(flags);
        this.typeHierarchyReader = new TypeHierarchyReader();
    }

    /**
     * Constructor which chooses a naive {@link TypeHierarchyReader}.
     */
    public NonClassloadingClassWriter(ClassReader classReader, int flags) {
        this(classReader, flags, new TypeHierarchyReader());
    }

    /**
     * Constructor which uses the given {@link TypeHierarchyReader} to obtain
     * hierarchy information for given {@link Type}s.
     */
    public NonClassloadingClassWriter(ClassReader classReader, int flags, TypeHierarchyReader typeHierarchyReader) {
        super(classReader, flags);
        this.typeHierarchyReader = typeHierarchyReader;
    }

    /**
     * An implementation consistent with {@link ClassWriter#getCommonSuperClass(String, String)}
     * that does uses {@link TypeHierarchyReader} to avoid loading classes.
     */
    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        Type c = Type.getObjectType(type1);
        Type d = Type.getObjectType(type2);

        if (typeHierarchyReader.isAssignableFrom(c, d)) {
            return type1;
        }
        if (typeHierarchyReader.isAssignableFrom(d, c)) {
            return type2;
        }
        if (typeHierarchyReader.isInterface(c) || typeHierarchyReader.isInterface(d)) {
            return "java/lang/Object";
        } else {
            do {
                c = typeHierarchyReader.getSuperClass(c);
            } while (!typeHierarchyReader.isAssignableFrom(c, d));
            return c.getInternalName();
        }
    }
}
