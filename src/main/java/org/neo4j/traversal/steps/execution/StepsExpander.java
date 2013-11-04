package org.neo4j.traversal.steps.execution;

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.BranchState;

import com.google.common.collect.ImmutableList;

public class StepsExpander implements PathExpander<StepState>
{
    private final StepExpander[] stepExpanders;

    public StepsExpander( StepExpander... stepExpanders )
    {
        this.stepExpanders = stepExpanders;
    }

    @Override
    public Iterable<Relationship> expand( Path path, BranchState<StepState> state )
    {
        StepExpander stepExpander = stepExpanders[state.getState().step()];
        /*
         * Return List because Iterable should be capable of returning multiple Iterator instances
         * Wrapping Iterator in an Iterable that can only consume it once would break the Iterable contract
         */
        return ImmutableList.copyOf( stepExpander.expand( path, state ) );
    }

    @Override
    public PathExpander<StepState> reverse()
    {
        // TODO deal with this in better way
        throw new UnsupportedOperationException( "reverse not implemented by " + getClass().getSimpleName() );
    }

    public abstract static class StepExpander implements PathExpander<StepState>
    {
        @Override
        public PathExpander<StepState> reverse()
        {
            // TODO deal with this in better way
            throw new UnsupportedOperationException( "reverse not implemented by " + getClass().getSimpleName() );
        }
    }
}
