package org.neo4j.traversal.steps;

import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.PropertyContainer;

import com.google.common.base.Predicate;

public interface PropertyContainerFilterDescriptor<FILTER_DESCRIPTOR_TYPE extends PropertyContainerFilterDescriptor<?, ?>, PROPERTY_CONTAINER_TYPE extends PropertyContainer>
{
    public static abstract class PropertyContainerPredicate implements Predicate<PropertyContainer>
    {
    }

    List<PropertyContainerPredicate> getFilterPredicates();

    /**
     * AND semantics
     * 
     * @param propertyKey
     * @return
     */
    FILTER_DESCRIPTOR_TYPE hasPropertyKey( String propertyKey );
    
    // TODO notHasPropertyKey

    /**
     * AND semantics
     * 
     * @param propertyKey
     * @param propertyValue
     * @return
     */
    FILTER_DESCRIPTOR_TYPE propertyEquals( String propertyKey, Object propertyValue );

    /**
     * AND semantics
     * 
     * @param propertyKey
     * @param propertyValue
     * @return
     */
    FILTER_DESCRIPTOR_TYPE propertyNotEquals( String propertyKey, Object propertyValue );

    /**
     * AND semantics
     * 
     * @param check
     * @return
     */
    FILTER_DESCRIPTOR_TYPE conformsTo( PropertyContainerPredicate check );

    /**
     * AND semantics
     * 
     * @param set
     * @return
     */
    FILTER_DESCRIPTOR_TYPE inSet( Set<PROPERTY_CONTAINER_TYPE> set );

    /**
     * AND semantics
     * 
     * @param set
     * @return
     */
    FILTER_DESCRIPTOR_TYPE notInSet( Set<PROPERTY_CONTAINER_TYPE> set );
}
