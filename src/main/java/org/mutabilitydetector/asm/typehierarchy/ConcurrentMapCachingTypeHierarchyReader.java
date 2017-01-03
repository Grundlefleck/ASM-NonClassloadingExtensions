package org.mutabilitydetector.asm.typehierarchy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.objectweb.asm.Type;


public class ConcurrentMapCachingTypeHierarchyReader extends TypeHierarchyReader {

    private final TypeHierarchyReader baseReader;
    private final ConcurrentMap<Type, TypeHierarchy> typeHierarchyCache;

    public ConcurrentMapCachingTypeHierarchyReader(TypeHierarchyReader baseReader, ConcurrentMap<Type, TypeHierarchy> initiallyEmptyCache) {
        this.baseReader = baseReader;
        this.typeHierarchyCache = initiallyEmptyCache;
    }

    public ConcurrentMapCachingTypeHierarchyReader(TypeHierarchyReader baseReader) {
    	this(baseReader, new ConcurrentHashMap<Type, TypeHierarchy>());
    }
    
    @Override
    public TypeHierarchy hierarchyOf(final Type t) {
        if (!typeHierarchyCache.containsKey(t)) {
            typeHierarchyCache.put(t, baseReader.hierarchyOf(t));
        }
        return typeHierarchyCache.get(t);
    }
}
