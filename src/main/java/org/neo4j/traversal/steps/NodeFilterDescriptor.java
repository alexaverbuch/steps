package org.neo4j.traversal.steps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;

public class NodeFilterDescriptor implements PropertyContainerFilterDescriptor<NodeFilterDescriptor, Node>
{
    private Set<Label> labels = new HashSet<Label>();

    private final PropertyContainerFilterDescriptorImpl<Node> propertyFilters;

    NodeFilterDescriptor()
    {
        this.propertyFilters = new PropertyContainerFilterDescriptorImpl<Node>();
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
        List<PropertyContainerPredicate> propertyContainerPredicates = new ArrayList<PropertyContainerPredicate>();
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
        propertyContainerPredicates.addAll( propertyFilters.getFilterPredicates() );
        return propertyContainerPredicates;
    }

    @Override
    public String toString()
    {
        return "NodeFilterDescriptor [propertyKeys=" + propertyFilters.propertyKeys() + ", propertyValues="
               + propertyFilters.propertyValues() + ", genericChecks=" + propertyFilters.genericChecks() + ", labels="
               + labels + ", predicates=" + getFilterPredicates().toString() + "]";
    }

    @Override
    public NodeFilterDescriptor hasPropertyKey( String propertyKey )
    {
        propertyFilters.hasPropertyKey( propertyKey );
        return this;
    }

    @Override
    public NodeFilterDescriptor propertyEquals( String propertyKey, Object propertyValue )
    {
        propertyFilters.propertyEquals( propertyKey, propertyValue );
        return this;
    }

    @Override
    public NodeFilterDescriptor propertyNotEquals( String propertyKey, Object propertyValue )
    {
        propertyFilters.propertyNotEquals( propertyKey, propertyValue );
        return this;
    }

    @Override
    public NodeFilterDescriptor conformsTo( PropertyContainerPredicate check )
    {
        propertyFilters.conformsTo( check );
        return this;
    }

    @Override
    public NodeFilterDescriptor inSet( Set<Node> set )
    {
        propertyFilters.inSet( set );
        return this;
    }

    @Override
    public NodeFilterDescriptor notInSet( Set<Node> set )
    {
        propertyFilters.notInSet( set );
        return this;
    }
}
