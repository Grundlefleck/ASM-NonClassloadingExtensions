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

package org.mutabilitydetector.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

import java.util.List;

/**
 * An extended {@link SimpleVerifier} that guarantees not to load classes.
 * 
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
 * @see SimpleVerifier
 * @see Type
 * @see TypeHierarchyReader
 * 
 * @author Graham Allan
 * 
 */
public class NonClassloadingSimpleVerifier extends SimpleVerifier {

    private final Type currentClass;
    private final Type currentSuperClass;
    private final List<Type> currentClassInterfaces;
    private final boolean isInterface;
    /**
     * Used to obtain hierarchy information used in verification.
     */
    protected final TypeHierarchyReader typeHierarchyReader;

    /**
     * Default constructor which chooses a naive {@link TypeHierarchyReader}.
     */
    public NonClassloadingSimpleVerifier() {
        this(new TypeHierarchyReader());
    }

    public NonClassloadingSimpleVerifier(final Type currentClass, final Type currentSuperClass, boolean isInterface) {
        this(currentClass, currentSuperClass, null, isInterface, new TypeHierarchyReader());
    }

    /**
     * Constructor which uses the given {@link TypeHierarchyReader} to obtain
     * hierarchy information for given {@link Type}s.
     */
    public NonClassloadingSimpleVerifier(TypeHierarchyReader reader) {
        this(null, null, null, false, reader);
    }

    public NonClassloadingSimpleVerifier(final Type currentClass,
                                         final Type currentSuperClass,
                                         final List<Type> currentClassInterfaces,
                                         final boolean isInterface,
                                         TypeHierarchyReader reader) {
        this.currentClass = currentClass;
        this.currentSuperClass = currentSuperClass;
        this.currentClassInterfaces = currentClassInterfaces;
        this.isInterface = isInterface;
        this.typeHierarchyReader = reader;
    }


    /**
     * Unconditionally throws an {@link Error}. This method should never be
     * called.
     */
    @Override
    protected Class< ? > getClass(Type t) {
        throw new Error("Programming error: this verifier should "
                + "not be attempting to load classes.");
    }

    /**
     * Immediately delegates and returns the result of the equivalent method of
     * the underlying {@link TypeHierarchyReader}.
     * 
     * @see TypeHierarchyReader#isInterface(Type)
     */
    @Override
    protected boolean isInterface(final Type t) {
        if (currentClass != null && t.equals(currentClass)) {
            return isInterface;
        }
        return typeHierarchyReader.isInterface(t);
    }

    /**
     * Immediately delegates and returns the result of the equivalent method of
     * the underlying {@link TypeHierarchyReader}.
     * 
     * @see TypeHierarchyReader#getSuperClass(Type)
     */
    @Override
    protected Type getSuperClass(final Type t) {
        if (currentClass != null && t.equals(currentClass)) {
            return currentSuperClass;
        }
        return typeHierarchyReader.getSuperClass(t);
    }

    /**
     * Immediately delegates and returns the result of the equivalent method of
     * the underlying {@link TypeHierarchyReader}.
     * 
     * @see TypeHierarchyReader#isAssignableFrom(Type, Type)
     */
    @Override
    public boolean isAssignableFrom(Type toType, Type fromType) {
        if (toType.equals(fromType)) {
            return true;
        }

        if (currentClass != null && toType.equals(currentClass)) {
            if (getSuperClass(fromType) == null) {
                return false;
            } else {
                if (isInterface) {
                    return fromType.getSort() == Type.OBJECT
                        || fromType.getSort() == Type.ARRAY;
                }
                return isAssignableFrom(toType, getSuperClass(fromType));
            }
        }
        if (currentClass != null && fromType.equals(currentClass)) {
            if (isAssignableFrom(toType, currentSuperClass)) {
                return true;
            }
            if (currentClassInterfaces != null) {
                for (int i = 0; i < currentClassInterfaces.size(); ++i) {
                    Type v = currentClassInterfaces.get(i);
                    if (isAssignableFrom(toType, v)) {
                        return true;
                    }
                }
            }
            return false;
        }

        TypeHierarchyReader.TypeHierarchy tc = typeHierarchyReader.hierarchyOf(toType);
        if (tc.isInterface()) {
            tc = TypeHierarchyReader.TypeHierarchy.JAVA_LANG_OBJECT;
        }
        return tc.isAssignableFrom(fromType, typeHierarchyReader);
    }
}
