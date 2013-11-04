package org.neo4j.traversal.steps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.PropertyContainer;

class PropertyContainerFilterDescriptorImpl implements
        PropertyContainerFilterDescriptor<PropertyContainerFilterDescriptorImpl>
{
    private Set<String> propertyKeys = new HashSet<String>();
    private Set<PropertyValue> propertyValues = new HashSet<PropertyValue>();
    private Set<PropertyContainerPredicate> genericChecks = new HashSet<PropertyContainerPredicate>();
    private Set<Set<? extends PropertyContainer>> inSets = new HashSet<Set<? extends PropertyContainer>>();
    private Set<Set<? extends PropertyContainer>> notInSets = new HashSet<Set<? extends PropertyContainer>>();

    @Override
    public PropertyContainerFilterDescriptorImpl hasPropertyKey( String propertyKey )
    {
        propertyKeys.add( propertyKey );
        return this;
    }

    public Set<String> propertyKeys()
    {
        return propertyKeys;
    }

    @Override
    public PropertyContainerFilterDescriptorImpl propertyEquals( String propertyKey, Object propertyValue )
    {
        propertyValues.add( new PropertyValue( propertyKey, propertyValue ) );
        return this;
    }

    @Override
    public Set<PropertyValue> propertyValues()
    {
        return propertyValues;
    }

    @Override
    public PropertyContainerFilterDescriptorImpl conformsTo( PropertyContainerPredicate check )
    {
        genericChecks.add( check );
        return this;
    }

    @Override
    public Set<PropertyContainerPredicate> genericChecks()
    {
        return genericChecks;
    }

    @Override
    public PropertyContainerFilterDescriptorImpl inSet( Set<? extends PropertyContainer> set )
    {
        inSets.add( set );
        return this;
    }

    @Override
    public Set<Set<? extends PropertyContainer>> inSets()
    {
        return inSets;
    }

    @Override
    public PropertyContainerFilterDescriptorImpl notInSet( Set<? extends PropertyContainer> set )
    {
        notInSets.add( set );
        return this;
    }

    @Override
    public Set<Set<? extends PropertyContainer>> notInSets()
    {
        return notInSets;
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
        if ( false == inSets.isEmpty() )
        {
            propertyContainerPredicates.add( new PropertyContainerPredicate()
            {
                @Override
                public boolean apply( PropertyContainer propertyContainer )
                {
                    for ( Set<? extends PropertyContainer> inSet : inSets )
                    {
                        if ( false == inSet.contains( propertyContainer ) ) return false;
                    }
                    return true;
                }
            } );
        }
        if ( false == notInSets.isEmpty() )
        {
            propertyContainerPredicates.add( new PropertyContainerPredicate()
            {
                @Override
                public boolean apply( PropertyContainer propertyContainer )
                {
                    for ( Set<? extends PropertyContainer> notInSet : notInSets )
                    {
                        if ( true == notInSet.contains( propertyContainer ) ) return false;
                    }
                    return true;
                }
            } );
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
