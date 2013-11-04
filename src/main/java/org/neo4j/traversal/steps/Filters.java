package org.neo4j.traversal.steps;

public class Filters
{
    public static NodeFilterDescriptor node()
    {
        return new NodeFilterDescriptor();
    }

    public static RelationshipFilterDescriptor relationship()
    {
        return new RelationshipFilterDescriptor();
    }
}
