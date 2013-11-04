package org.neo4j.traversal.steps;

public class Step
{
    public static final int UNLIMITED = Integer.MAX_VALUE;
    private final NodeFilterDescriptor nodeDescriptor;
    private final RelationshipFilterDescriptor relationshipDescriptor;
    private final int minRepetitions;
    private final int maxRepetitions;

    public static Step one( NodeFilterDescriptor nodeDescriptor )
    {
        return new Step( nodeDescriptor, null, 0, 0 );
    }

    public static Step one( NodeFilterDescriptor nodeDescriptor, RelationshipFilterDescriptor relationshipDescriptor )
    {
        return new Step( nodeDescriptor, relationshipDescriptor, 0, 0 );
    }

    public static Step manyExact( NodeFilterDescriptor nodeDescriptor,
            RelationshipFilterDescriptor relationshipDescriptor, int repetitions )
    {
        return new Step( nodeDescriptor, relationshipDescriptor, repetitions, repetitions );
    }

    public static Step manyRange( NodeFilterDescriptor nodeDescriptor,
            RelationshipFilterDescriptor relationshipDescriptor, int minRepetitions, int maxRepetitions )
    {
        return new Step( nodeDescriptor, relationshipDescriptor, minRepetitions, maxRepetitions );
    }

    private Step( NodeFilterDescriptor nodeDescriptor, RelationshipFilterDescriptor relationshipDescriptor,
            int minRepetitions, int maxRepetitions )
    {
        this.nodeDescriptor = nodeDescriptor;
        this.relationshipDescriptor = relationshipDescriptor;
        this.minRepetitions = minRepetitions;
        this.maxRepetitions = maxRepetitions;
    }

    NodeFilterDescriptor nodeDescriptor()
    {
        return nodeDescriptor;
    }

    RelationshipFilterDescriptor relationshipDescriptor()
    {
        return relationshipDescriptor;
    }

    int minRepetitions()
    {
        return minRepetitions;
    }

    int maxRepetitions()
    {
        return maxRepetitions;
    }

    @Override
    public String toString()
    {
        return "Step [nodeDescriptor=" + nodeDescriptor + ", relationshipDescriptor=" + relationshipDescriptor
               + ", minRepetitions=" + minRepetitions + ", maxRepetitions=" + maxRepetitions + "]";
    }
}
