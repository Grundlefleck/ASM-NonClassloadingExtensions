package org.mutabilitydetector.asm.typehierarchy;

import org.objectweb.asm.Type;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class IsAssignableFromCachingTypeHierarchyReader extends TypeHierarchyReader {

    private final ConcurrentMap<TypeAssignability, Boolean> isAssignableFromCache;
    private final TypeHierarchyReader baseReader;

    public IsAssignableFromCachingTypeHierarchyReader(TypeHierarchyReader baseReader) {
        this.baseReader = baseReader;
        this.isAssignableFromCache =  new ConcurrentHashMap<TypeAssignability, Boolean>();
    }
    
    @Override
    public TypeHierarchy hierarchyOf(Type t) {
        return baseReader.hierarchyOf(t);
    }
    
    @Override
    public Type getSuperClass(Type t) {
        return baseReader.getSuperClass(t);
    }
    
    @Override
    public boolean isInterface(Type t) {
        return baseReader.isInterface(t);
    }
    
    @Override
    public boolean isAssignableFrom(final Type t, final Type u) {
        TypeAssignability assignability = new TypeAssignability(t, u);
        if (!isAssignableFromCache.containsKey(assignability)) {
            isAssignableFromCache.put(assignability, baseReader.isAssignableFrom(t, u));
        }
        return isAssignableFromCache.get(assignability);
    }
    
    private static class TypeAssignability {
        private final Type toType, fromType;
        private final int hashCode;
        
        TypeAssignability(Type toType, Type fromType) {
            this.toType = toType;
            this.fromType = fromType;
            this.hashCode = calculateHashCode();
        }

        private int calculateHashCode() {
            int result = toType.hashCode();
            result = 31 * result + fromType.hashCode();
            return result;
        }

        @Override
        public int hashCode() {
            return hashCode;
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
            
            TypeAssignability other = (TypeAssignability) obj;
            return toType.equals(other.toType) && fromType.equals(other.fromType);
        }
        
    }
    
}
