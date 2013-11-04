package org.neo4j.traversal.steps;

import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.PropertyContainer;

import com.google.common.base.Predicate;

public interface PropertyContainerFilterDescriptor<FILTER_DESCRIPTOR_TYPE extends PropertyContainerFilterDescriptor<?>>
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

    Set<String> propertyKeys();

    /**
     * AND semantics
     * 
     * @param propertyKey
     * @param propertyValue
     * @return
     */
    FILTER_DESCRIPTOR_TYPE propertyEquals( String propertyKey, Object propertyValue );

    Set<PropertyValue> propertyValues();

    FILTER_DESCRIPTOR_TYPE conformsTo( PropertyContainerPredicate check );

    Set<PropertyContainerPredicate> genericChecks();

    /**
     * AND semantics
     * 
     * @param set
     * @return
     */
    FILTER_DESCRIPTOR_TYPE inSet( Set<? extends PropertyContainer> set );

    Set<Set<? extends PropertyContainer>> inSets();

    /**
     * AND semantics
     * 
     * @param set
     * @return
     */
    FILTER_DESCRIPTOR_TYPE notInSet( Set<? extends PropertyContainer> set );

    Set<Set<? extends PropertyContainer>> notInSets();
}
