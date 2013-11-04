package org.neo4j.traversal.steps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;

public class NodeFilterDescriptor implements PropertyContainerFilterDescriptor<NodeFilterDescriptor>
{
    private Set<Label> labels = new HashSet<Label>();

    private final PropertyContainerFilterDescriptor<PropertyContainerFilterDescriptorImpl> propertyFilters;

    NodeFilterDescriptor()
    {
        this.propertyFilters = new PropertyContainerFilterDescriptorImpl();
    }

    /**
     * OR semantics
     * 
     * @param label
     * @return
     */
    public NodeFilterDescriptor hasLabel( Label label )
    {
        labels.add( label );
        return this;
    }

    public Label[] labels()
    {
        return labels.toArray( new Label[labels.size()] );
    }

    @Override
    public List<PropertyContainerPredicate> getFilterPredicates()
    {
        List<PropertyContainerPredicate> propertyContainerPredicates = propertyFilters.getFilterPredicates();
        List<PropertyContainerPredicate> nodePredicates = new ArrayList<PropertyContainerPredicate>();
        if ( false == labels.isEmpty() )
        {
            final Label[] hasLabelsArray = labels.toArray( new Label[labels.size()] );
            nodePredicates.add( new PropertyContainerPredicate()
            {
                @Override
                public boolean apply( PropertyContainer node )
                {
                    for ( int i = 0; i < hasLabelsArray.length; i++ )
                    {
                        if ( ( (Node) node ).hasLabel( hasLabelsArray[i] ) ) return true;
                    }
                    return false;
                }

            } );
        }
        propertyContainerPredicates.addAll( nodePredicates );
        return propertyContainerPredicates;
    }

    @Override
    public String toString()
    {
        return "NodeFilterDescriptor [propertyKeys=" + propertyKeys() + ", propertyValues=" + propertyValues()
               + ", genericChecks=" + genericChecks() + ", labels=" + labels + ", predicates="
               + getFilterPredicates().toString() + "]";
    }

    @Override
    public NodeFilterDescriptor hasPropertyKey( String propertyKey )
    {
        propertyFilters.hasPropertyKey( propertyKey );
        return this;
    }

    @Override
    public Set<String> propertyKeys()
    {
        return propertyFilters.propertyKeys();
    }

    @Override
    public NodeFilterDescriptor propertyEquals( String propertyKey, Object propertyValue )
    {
        propertyFilters.propertyEquals( propertyKey, propertyValue );
        return this;
    }

    @Override
    public Set<PropertyValue> propertyValues()
    {
        return propertyFilters.propertyValues();
    }

    @Override
    public NodeFilterDescriptor conformsTo( PropertyContainerPredicate check )
    {
        propertyFilters.conformsTo( check );
        return this;
    }

    @Override
    public Set<PropertyContainerPredicate> genericChecks()
    {
        return propertyFilters.genericChecks();
    }

    @Override
    public NodeFilterDescriptor inSet( Set<? extends PropertyContainer> set )
    {
        propertyFilters.inSet( set );
        return this;
    }

    @Override
    public Set<Set<? extends PropertyContainer>> inSets()
    {
        return propertyFilters.inSets();
    }

    @Override
    public NodeFilterDescriptor notInSet( Set<? extends PropertyContainer> set )
    {
        propertyFilters.notInSet( set );
        return this;
    }

    @Override
    public Set<Set<? extends PropertyContainer>> notInSets()
    {
        return propertyFilters.notInSets();
    }
}
