package org.neo4j.traversal.steps;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.google.common.base.Function;

public class RelationshipFilterDescriptor implements
        PropertyContainerFilterDescriptor<RelationshipFilterDescriptor, Relationship>, CanExpand
{
    private Set<RelationshipType> relationshipTypes = new HashSet<RelationshipType>();
    private Direction direction = Direction.BOTH;
    private final PropertyContainerFilterDescriptorImpl<Relationship> propertyFilters;

    RelationshipFilterDescriptor()
    {
        this.propertyFilters = new PropertyContainerFilterDescriptorImpl<Relationship>();
    }

    /**
     * OR semantics
     * 
     * @param type
     * @return
     */
    public RelationshipFilterDescriptor hasType( RelationshipType type )
    {
        relationshipTypes.add( type );
        return this;
    }

    public RelationshipType[] types()
    {
        return relationshipTypes.toArray( new RelationshipType[relationshipTypes.size()] );
    }

    /**
     * Overwrites last set value, default Direction.BOTH
     * 
     * @param direction
     * @return
     */
    public RelationshipFilterDescriptor hasDirection( Direction direction )
    {
        this.direction = direction;
        return this;
    }

    public Direction direction()
    {
        return direction;
    }

    @Override
    public Function<Node, Iterator<Relationship>> expandFun()
    {
        final Direction direction = this.direction;
        final RelationshipType[] types = types();
        if ( types.length == 0 )
            return new Function<Node, Iterator<Relationship>>()
            {
                @Override
                public Iterator<Relationship> apply( Node node )
                {
                    return node.getRelationships( direction ).iterator();
                }
            };
        else
            return new Function<Node, Iterator<Relationship>>()
            {
                @Override
                public Iterator<Relationship> apply( Node node )
                {
                    return node.getRelationships( direction, types ).iterator();
                }
            };
    }

    @Override
    public String toString()
    {
        return "RelationshipFilterDescriptor [hasPropertyKeys=" + propertyFilters.propertyKeys() + ", propertyValues="
               + propertyFilters.propertyValues() + ", genericChecks=" + propertyFilters.genericChecks()
               + ", relationshipTypes=" + relationshipTypes + ", direction=" + direction + ", predicates="
               + getFilterPredicates().toString() + "]";
    }

    @Override
    public List<PropertyContainerPredicate> getFilterPredicates()
    {
        return propertyFilters.getFilterPredicates();
    }

    @Override
    public RelationshipFilterDescriptor hasPropertyKey( String propertyKey )
    {
        propertyFilters.hasPropertyKey( propertyKey );
        return this;
    }

    @Override
    public RelationshipFilterDescriptor propertyEquals( String propertyKey, Object propertyValue )
    {
        propertyFilters.propertyEquals( propertyKey, propertyValue );
        return this;
    }

    @Override
    public RelationshipFilterDescriptor propertyNotEquals( String propertyKey, Object propertyValue )
    {
        propertyFilters.propertyNotEquals( propertyKey, propertyValue );
        return this;
    }

    @Override
    public RelationshipFilterDescriptor conformsTo( PropertyContainerPredicate check )
    {
        propertyFilters.conformsTo( check );
        return this;
    }

    @Override
    public RelationshipFilterDescriptor inSet( Set<Relationship> set )
    {
        propertyFilters.inSet( set );
        return this;
    }

    @Override
    public RelationshipFilterDescriptor notInSet( Set<Relationship> set )
    {
        propertyFilters.notInSet( set );
        return this;
    }
}
