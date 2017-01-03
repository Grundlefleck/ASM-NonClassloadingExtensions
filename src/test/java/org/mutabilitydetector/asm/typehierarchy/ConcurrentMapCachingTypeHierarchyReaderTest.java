package org.mutabilitydetector.asm.typehierarchy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.objectweb.asm.Type;

public class ConcurrentMapCachingTypeHierarchyReaderTest extends TestCase {

    private final TypeHierarchyReader baseReader = mock(TypeHierarchyReader.class);
    private final Type toType = Type.getType(List.class);
    private final Type fromType = Type.getType(ArrayList.class);
    private final ConcurrentMapCachingTypeHierarchyReader reader = new ConcurrentMapCachingTypeHierarchyReader(baseReader);

    public void testUsesUnderlyingReaderToCalculateTypeHierarchy() throws Exception {
        TypeHierarchyReader.TypeHierarchy fromTypeHierarchy = new TypeHierarchyReader.TypeHierarchy(fromType, toType, Collections.<Type>emptyList(), false);
        TypeHierarchyReader.TypeHierarchy toTypeHierarchy = new TypeHierarchyReader.TypeHierarchy(toType, null, Collections.<Type>emptyList(), true);

        when(baseReader.hierarchyOf(fromType)).thenReturn(fromTypeHierarchy);
        when(baseReader.hierarchyOf(toType)).thenReturn(toTypeHierarchy);
        
        assertSame(reader.hierarchyOf(fromType), fromTypeHierarchy);
        assertSame(reader.hierarchyOf(toType), toTypeHierarchy);
    }

    public void testCachesReturnValueOfUnderlyingReader() throws Exception {
        TypeHierarchyReader.TypeHierarchy fromTypeHierarchy = new TypeHierarchyReader.TypeHierarchy(fromType, toType, Collections.<Type>emptyList(), false);
        
        when(baseReader.hierarchyOf(fromType)).thenReturn(fromTypeHierarchy);
        
        reader.hierarchyOf(fromType);
        reader.hierarchyOf(fromType);
        
        verify(baseReader, times(1)).hierarchyOf(fromType);
    }
    
    public void testUsesUnderlyingReaderToCalculateIsInterface() throws Exception {
        TypeHierarchyReader.TypeHierarchy anInterfaceType = new TypeHierarchyReader.TypeHierarchy(toType, null, Collections.<Type>emptyList(), true);
        when(baseReader.hierarchyOf(toType)).thenReturn(anInterfaceType);

        assertTrue(reader.isInterface(toType));
    }
    
    public void testUsesUnderlyingReaderToGetSuperClass() throws Exception {
        TypeHierarchyReader.TypeHierarchy typeHierarchy = new TypeHierarchyReader.TypeHierarchy(fromType, toType, Collections.<Type>emptyList(), true);
        when(baseReader.hierarchyOf(fromType)).thenReturn(typeHierarchy);
            
        assertEquals(toType, reader.getSuperClass(fromType));
    }
    
}
