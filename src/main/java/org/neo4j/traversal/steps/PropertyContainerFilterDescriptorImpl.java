package org.neo4j.traversal.steps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.PropertyContainer;

class PropertyContainerFilterDescriptorImpl<PROPERTY_CONTAINER_TYPE extends PropertyContainer>
        implements
        PropertyContainerFilterDescriptor<PropertyContainerFilterDescriptorImpl<PROPERTY_CONTAINER_TYPE>, PROPERTY_CONTAINER_TYPE>
{
    private Set<String> propertyKeys = new HashSet<String>();
    private Set<PropertyValue> propertyValues = new HashSet<PropertyValue>();
    private Set<PropertyValue> notPropertyValues = new HashSet<PropertyValue>();
    private Set<PropertyContainerPredicate> genericChecks = new HashSet<PropertyContainerPredicate>();
    private Set<PROPERTY_CONTAINER_TYPE> inSet = new HashSet<PROPERTY_CONTAINER_TYPE>();
    private Set<PROPERTY_CONTAINER_TYPE> notInSet = new HashSet<PROPERTY_CONTAINER_TYPE>();

    @Override
    public PropertyContainerFilterDescriptorImpl<PROPERTY_CONTAINER_TYPE> hasPropertyKey( String propertyKey )
    {
        propertyKeys.add( propertyKey );
        return this;
    }

    public Set<String> propertyKeys()
    {
        return propertyKeys;
    }

    @Override
    public PropertyContainerFilterDescriptorImpl<PROPERTY_CONTAINER_TYPE> propertyEquals( String propertyKey,
            Object propertyValue )
    {
        propertyValues.add( new PropertyValue( propertyKey, propertyValue ) );
        return this;
    }

    Set<PropertyValue> propertyValues()
    {
        return propertyValues;
    }

    @Override
    public PropertyContainerFilterDescriptorImpl<PROPERTY_CONTAINER_TYPE> propertyNotEquals( String propertyKey,
            Object propertyValue )
    {
        notPropertyValues.add( new PropertyValue( propertyKey, propertyValue ) );
        return this;
    }

    Set<PropertyValue> notPropertyValues()
    {
        return propertyValues;
    }

    @Override
    public PropertyContainerFilterDescriptorImpl<PROPERTY_CONTAINER_TYPE> conformsTo( PropertyContainerPredicate check )
    {
        genericChecks.add( check );
        return this;
    }

    Set<PropertyContainerPredicate> genericChecks()
    {
        return genericChecks;
    }

    @Override
    public PropertyContainerFilterDescriptorImpl<PROPERTY_CONTAINER_TYPE> inSet( Set<PROPERTY_CONTAINER_TYPE> set )
    {
        inSet = set;
        return this;
    }

    Set<PROPERTY_CONTAINER_TYPE> inSet()
    {
        return inSet;
    }

    @Override
    public PropertyContainerFilterDescriptorImpl<PROPERTY_CONTAINER_TYPE> notInSet( Set<PROPERTY_CONTAINER_TYPE> set )
    {
        notInSet = set;
        return this;
    }

    Set<PROPERTY_CONTAINER_TYPE> notInSet()
    {
        return notInSet;
    }

    @Override
    public List<PropertyContainerPredicate> getFilterPredicates()
    {
        List<PropertyContainerPredicate> propertyContainerPredicates = new ArrayList<PropertyContainerPredicate>();
        if ( false == propertyKeys.isEmpty() )
        {
            final String[] hasPropertyKeysArray = propertyKeys.toArray( new String[propertyKeys.size()] );
            propertyContainerPredicates.add( new PropertyContainerPredicate()
            {
                @Override
                public boolean apply( PropertyContainer propertyContainer )
                {
                    for ( int i = 0; i < hasPropertyKeysArray.length; i++ )
                    {
                        if ( false == propertyContainer.hasProperty( hasPropertyKeysArray[i] ) ) return false;
                    }
                    return true;
                }
            } );
        }
        if ( false == propertyValues.isEmpty() )
        {
            final PropertyValue[] propertysValuesArray = propertyValues.toArray( new PropertyValue[propertyValues.size()] );

            propertyContainerPredicates.add( new PropertyContainerPredicate()
            {
                @Override
                public boolean apply( PropertyContainer propertyContainer )
                {
                    for ( int i = 0; i < propertysValuesArray.length; i++ )
                    {
                        PropertyValue expectedPropertyValue = propertysValuesArray[i];
                        if ( false == propertyContainer.getProperty( expectedPropertyValue.key() ).equals(
                                expectedPropertyValue.value() ) ) return false;
                    }
                    return true;
                }
            } );
        }
        /*
         * 
         */
        if ( false == notPropertyValues.isEmpty() )
        {
            final PropertyValue[] notPropertysValuesArray = notPropertyValues.toArray( new PropertyValue[notPropertyValues.size()] );

            propertyContainerPredicates.add( new PropertyContainerPredicate()
            {
                @Override
                public boolean apply( PropertyContainer propertyContainer )
                {
                    for ( int i = 0; i < notPropertysValuesArray.length; i++ )
                    {
                        PropertyValue disallowedPropertyValue = notPropertysValuesArray[i];
                        if ( propertyContainer.getProperty( disallowedPropertyValue.key() ).equals(
                                disallowedPropertyValue.value() ) ) return false;
                    }
                    return true;
                }
            } );
        }
        /*
         * 
         */
        if ( false == inSet.isEmpty() )
        {
            PropertyContainerPredicate predicate = null;
            if ( 1 == inSet.size() )
            {
                final PROPERTY_CONTAINER_TYPE desiredPropertyContainer = inSet.iterator().next();
                predicate = new PropertyContainerPredicate()
                {
                    @Override
                    public boolean apply( PropertyContainer propertyContainer )
                    {
                        return propertyContainer.equals( desiredPropertyContainer );
                    }
                };
            }
            else
            {
                predicate = new PropertyContainerPredicate()
                {
                    @Override
                    public boolean apply( PropertyContainer propertyContainer )
                    {
                        return inSet.contains( propertyContainer );
                    }
                };
            }
            propertyContainerPredicates.add( predicate );
        }
        if ( false == notInSet.isEmpty() )
        {
            PropertyContainerPredicate predicate = null;
            if ( 1 == inSet.size() )
            {
                final PROPERTY_CONTAINER_TYPE desiredPropertyContainer = notInSet.iterator().next();
                predicate = new PropertyContainerPredicate()
                {
                    @Override
                    public boolean apply( PropertyContainer propertyContainer )
                    {
                        return false == propertyContainer.equals( desiredPropertyContainer );
                    }
                };
            }
            else
            {
                predicate = new PropertyContainerPredicate()
                {
                    @Override
                    public boolean apply( PropertyContainer propertyContainer )
                    {
                        return false == notInSet.contains( propertyContainer );
                    }
                };
            }
            propertyContainerPredicates.add( predicate );
        }
        propertyContainerPredicates.addAll( genericChecks );
        return propertyContainerPredicates;
    }

    @Override
    public String toString()
    {
        return "PropertyContainerFilterDescriptorImpl[hasPropertyKeys=" + propertyKeys + ", propertyValues="
               + propertyValues + ", genericChecks=" + genericChecks() + "]";
    }
}
