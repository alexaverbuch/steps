package org.neo4j.traversal.steps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.InitialBranchState;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.traversal.steps.PropertyContainerFilterDescriptor.PropertyContainerPredicate;
import org.neo4j.traversal.steps.exception.StepsException;
import org.neo4j.traversal.steps.exception.StepsExceptionType;
import org.neo4j.traversal.steps.execution.PredicateGroup;
import org.neo4j.traversal.steps.execution.StepEvaluationResult;
import org.neo4j.traversal.steps.execution.StepState;
import org.neo4j.traversal.steps.execution.StepsEvaluator;
import org.neo4j.traversal.steps.execution.StepsExpander;
import org.neo4j.traversal.steps.execution.StepsEvaluator.StepEvaluator;
import org.neo4j.traversal.steps.execution.StepsExpander.StepExpander;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

public class StepsBuilder
{
    /**
     * filters must start with @NodeFilterDescriptor and alternate between @NodeFilterDescriptor
     * and @RelationshipFilterDescriptor
     * 
     * @param td
     * @param steps
     * @return copy of td, with @StepsEvaluator and @StepsExpander attached
     */
    public final TraversalDescription build( TraversalDescription td, Step... steps )
    {
        List<StepExpander> stepExpanders = new ArrayList<StepExpander>();
        List<StepEvaluator> stepEvaluators = new ArrayList<StepEvaluator>();

        for ( int i = 0; i < steps.length - 1; i++ )
        {
            Step step = steps[i];

            if ( step.minRepetitions() == Step.UNLIMITED )
                throw new StepsException( StepsExceptionType.MIN_REPETITIONS_MUST_BE_FINITE_POSITIVE_INT );

            if ( step.minRepetitions() > step.maxRepetitions() )
                throw new StepsException( StepsExceptionType.MIN_REPETITIONS_MUST_BE_LOWER_THAN_MAX_REPETITIONS );

            if ( null == step.relationshipDescriptor() )
                throw new StepsException( StepsExceptionType.ONLY_LAST_STEP_MAY_OMIT_RELATIONSHIP_DESCRIPTOR );

            stepEvaluators.add( buildStepEvaluator( step.nodeDescriptor(), step.minRepetitions(),
                    step.maxRepetitions(), false ) );
            stepExpanders.add( buildStepExpander( step.relationshipDescriptor() ) );
        }

        Step lastStep = steps[steps.length - 1];
        stepEvaluators.add( buildStepEvaluator( lastStep.nodeDescriptor(), lastStep.minRepetitions(),
                lastStep.maxRepetitions(), true ) );
        // if last step only contains node descriptor expander not necessary
        if ( false == ( lastStep.relationshipDescriptor() == null ) )
            stepExpanders.add( buildStepExpander( lastStep.relationshipDescriptor() ) );

        StepsExpander ex = new StepsExpander( stepExpanders.toArray( new StepExpander[stepExpanders.size()] ) );
        StepsEvaluator ev = new StepsEvaluator( stepEvaluators.toArray( new StepEvaluator[stepEvaluators.size()] ) );
        return td.evaluator( ev ).expand( ex, INITIAL_BRANCH_STATE );
    }

    private StepEvaluator buildStepEvaluator( NodeFilterDescriptor nodeDescriptor, int minRepetitions,
            int maxRepetitions, boolean isLastStep )
    {
        // getFilterPredicates performs computation, only call once
        List<PropertyContainerPredicate> startNodePredicateList = nodeDescriptor.getFilterPredicates();

        PredicateGroup<PropertyContainer> startNodePredicates = null;
        if ( false == startNodePredicateList.isEmpty() )
        {
            startNodePredicates = new PredicateGroup<PropertyContainer>(
                    startNodePredicateList.toArray( new PropertyContainerPredicate[startNodePredicateList.size()] ) );
        }

        if ( isLastStep )
        {
            if ( minRepetitions == maxRepetitions )
                return new LastOnceStepEvaluator( minRepetitions, maxRepetitions, startNodePredicates );
            else
                return new LastRangeStepEvaluator( minRepetitions, maxRepetitions, startNodePredicates );
        }
        else
        {
            if ( minRepetitions == maxRepetitions )
                return new NotLastOnceStepEvaluator( minRepetitions, maxRepetitions, startNodePredicates );
            else
                return new NotLastRangeStepEvaluator( minRepetitions, maxRepetitions, startNodePredicates );
        }
    }

    private class NotLastOnceStepEvaluator implements StepEvaluator
    {
        private final PredicateGroup<PropertyContainer> startNodePredicates;
        private final Function<Integer, RepetitionState> getRepetitionStateFun;

        public NotLastOnceStepEvaluator( int minRepetitions, int maxRepetitions,
                PredicateGroup<PropertyContainer> startNodePredicates )
        {
            super();
            this.startNodePredicates = startNodePredicates;
            this.getRepetitionStateFun = new OnceStepGetRepetitionStateFun( minRepetitions, maxRepetitions );
        }

        @Override
        public StepEvaluationResult evaluate( Path path, int stepState )
        {
            Node startNode = path.endNode();
            int currentStepState = stepState + 1;
            RepetitionState repetitionState = getRepetitionState( stepState );

            switch ( repetitionState )
            {
            case TOO_LOW:
                if ( isMatch( startNode ) )
                    return StepEvaluationResult.acceptStayExcludeContinue( currentStepState );
                else
                    return StepEvaluationResult.REJECT_STAY_EXCLUDE_PRUNE;
            case ONLY_IN_RANGE:
                if ( isMatch( startNode ) )
                    return StepEvaluationResult.ACCEPT_ADVANCE_EXCLUDE_CONTINUE;
                else
                    return StepEvaluationResult.REJECT_STAY_EXCLUDE_PRUNE;
            case TOO_HIGH:
                return StepEvaluationResult.REJECT_STAY_EXCLUDE_PRUNE;
            default:
                throw new RuntimeException( "Should never get here" );
            }
        }

        private boolean isMatch( Node node )
        {
            return ( null == startNodePredicates ) || ( startNodePredicates.apply( node ) );
        }

        private RepetitionState getRepetitionState( int step )
        {
            return getRepetitionStateFun.apply( step );
        }
    }

    private class LastOnceStepEvaluator implements StepEvaluator
    {
        private final PredicateGroup<PropertyContainer> startNodePredicates;
        private final Function<Integer, RepetitionState> getRepetitionStateFun;

        public LastOnceStepEvaluator( int minRepetitions, int maxRepetitions,
                PredicateGroup<PropertyContainer> startNodePredicates )
        {
            super();
            this.startNodePredicates = startNodePredicates;
            this.getRepetitionStateFun = new OnceStepGetRepetitionStateFun( minRepetitions, maxRepetitions );
        }

        @Override
        public StepEvaluationResult evaluate( Path path, int stepState )
        {
            Node startNode = path.endNode();
            int currentStepState = stepState + 1;
            RepetitionState repetitionState = getRepetitionState( stepState );

            switch ( repetitionState )
            {
            case TOO_LOW:
                if ( isMatch( startNode ) )
                    return StepEvaluationResult.acceptStayExcludeContinue( currentStepState );
                else
                    return StepEvaluationResult.REJECT_STAY_EXCLUDE_PRUNE;
            case ONLY_IN_RANGE:
                if ( isMatch( startNode ) )
                    return StepEvaluationResult.ACCEPT_ADVANCE_INCLUDE_PRUNE;
                else
                    return StepEvaluationResult.REJECT_STAY_EXCLUDE_PRUNE;
            case TOO_HIGH:
                return StepEvaluationResult.REJECT_STAY_EXCLUDE_PRUNE;
            default:
                throw new RuntimeException( "Should never get here" );
            }
        }

        private boolean isMatch( Node node )
        {
            return ( null == startNodePredicates ) || ( startNodePredicates.apply( node ) );
        }

        private RepetitionState getRepetitionState( int step )
        {
            return getRepetitionStateFun.apply( step );
        }
    }

    private class NotLastRangeStepEvaluator implements StepEvaluator
    {
        private final PredicateGroup<PropertyContainer> startNodePredicates;
        private final Function<Integer, RepetitionState> getRepetitionStateFun;

        public NotLastRangeStepEvaluator( int minRepetitions, int maxRepetitions,
                PredicateGroup<PropertyContainer> startNodePredicates )
        {
            super();
            this.startNodePredicates = startNodePredicates;
            this.getRepetitionStateFun = new RangeStepGetRepetitionStateFun( minRepetitions, maxRepetitions );
        }

        @Override
        public StepEvaluationResult evaluate( Path path, int stepState )
        {
            Node startNode = path.endNode();
            int currentStepState = stepState + 1;
            RepetitionState repetitionState = getRepetitionState( stepState );

            switch ( repetitionState )
            {
            case TOO_LOW:
                if ( isMatch( startNode ) )
                    return StepEvaluationResult.acceptStayExcludeContinue( currentStepState );
                else
                    return StepEvaluationResult.REJECT_STAY_EXCLUDE_PRUNE;
            case IN_RANGE:
                if ( isMatch( startNode ) )
                    return StepEvaluationResult.acceptStayExcludeContinue( currentStepState );
                else
                    return StepEvaluationResult.REJECT_ADVANCE_EXCLUDE_CONTINUE;
            case LAST_IN_RANGE:
                if ( isMatch( startNode ) )
                    return StepEvaluationResult.ACCEPT_ADVANCE_EXCLUDE_CONTINUE;
                else
                    return StepEvaluationResult.REJECT_ADVANCE_EXCLUDE_CONTINUE;
            case TOO_HIGH:
                return StepEvaluationResult.REJECT_STAY_EXCLUDE_PRUNE;
            default:
                throw new RuntimeException( "Should never get here" );
            }
        }

        private boolean isMatch( Node node )
        {
            return ( null == startNodePredicates ) || ( startNodePredicates.apply( node ) );
        }

        private RepetitionState getRepetitionState( int step )
        {
            return getRepetitionStateFun.apply( step );
        }
    }

    private class LastRangeStepEvaluator implements StepEvaluator
    {
        private final PredicateGroup<PropertyContainer> startNodePredicates;
        private final Function<Integer, RepetitionState> getRepetitionStateFun;

        public LastRangeStepEvaluator( int minRepetitions, int maxRepetitions,
                PredicateGroup<PropertyContainer> startNodePredicates )
        {
            super();
            this.startNodePredicates = startNodePredicates;
            this.getRepetitionStateFun = new RangeStepGetRepetitionStateFun( minRepetitions, maxRepetitions );
        }

        @Override
        public StepEvaluationResult evaluate( Path path, int stepState )
        {
            Node startNode = path.endNode();
            int currentStepState = stepState + 1;
            RepetitionState repetitionState = getRepetitionState( stepState );

            switch ( repetitionState )
            {
            case TOO_LOW:
                if ( isMatch( startNode ) )
                    return StepEvaluationResult.acceptStayExcludeContinue( currentStepState );
                else
                    return StepEvaluationResult.REJECT_STAY_EXCLUDE_PRUNE;
            case IN_RANGE:
                if ( isMatch( startNode ) )
                    return StepEvaluationResult.acceptStayIncludeContinue( currentStepState );
                else
                    return StepEvaluationResult.REJECT_STAY_EXCLUDE_PRUNE;
            case LAST_IN_RANGE:
                if ( isMatch( startNode ) )
                    return StepEvaluationResult.ACCEPT_ADVANCE_INCLUDE_PRUNE;
                else
                    return StepEvaluationResult.REJECT_STAY_EXCLUDE_PRUNE;
            case TOO_HIGH:
                return StepEvaluationResult.REJECT_STAY_EXCLUDE_PRUNE;
            default:
                throw new RuntimeException( "Should never get here" );
            }
        }

        private boolean isMatch( Node node )
        {
            return ( null == startNodePredicates ) || ( startNodePredicates.apply( node ) );
        }

        private RepetitionState getRepetitionState( int step )
        {
            return getRepetitionStateFun.apply( step );
        }
    }

    private class OnceStepGetRepetitionStateFun implements Function<Integer, RepetitionState>
    {
        private final int minRepetitions;
        private final int maxRepetitions;

        public OnceStepGetRepetitionStateFun( int minRepetitions, int maxRepetitions )
        {
            super();
            this.minRepetitions = minRepetitions;
            this.maxRepetitions = maxRepetitions;
        }

        @Override
        public RepetitionState apply( Integer step )
        {
            if ( step < minRepetitions ) return RepetitionState.TOO_LOW;
            if ( step > maxRepetitions ) return RepetitionState.TOO_HIGH;
            return RepetitionState.ONLY_IN_RANGE;
        }
    }

    private class RangeStepGetRepetitionStateFun implements Function<Integer, RepetitionState>
    {
        private final int minRepetitions;
        private final int maxRepetitions;

        public RangeStepGetRepetitionStateFun( int minRepetitions, int maxRepetitions )
        {
            super();
            this.minRepetitions = minRepetitions;
            this.maxRepetitions = maxRepetitions;
        }

        @Override
        public RepetitionState apply( Integer step )
        {
            if ( step < minRepetitions ) return RepetitionState.TOO_LOW;
            if ( step > maxRepetitions ) return RepetitionState.TOO_HIGH;
            if ( step == maxRepetitions ) return RepetitionState.LAST_IN_RANGE;
            return RepetitionState.IN_RANGE;
        }
    }

    private StepExpander buildStepExpander( RelationshipFilterDescriptor relationshipDescriptor )
    {
        List<PropertyContainerPredicate> relationshipPredicateList = relationshipDescriptor.getFilterPredicates();

        final PredicateGroup<PropertyContainer> relationshipPredicates = ( relationshipPredicateList.isEmpty() ) ? null
                : new PredicateGroup<PropertyContainer>(
                        relationshipPredicateList.toArray( new PropertyContainerPredicate[relationshipPredicateList.size()] ) );

        final Function<Node, Iterator<Relationship>> expandFun = relationshipDescriptor.expandFun();

        return new StepExpander()
        {
            @Override
            public Iterable<Relationship> expand( Path path, BranchState<StepState> state )
            {
                final Node startNode = path.endNode();
                Iterator<Relationship> relationships = ( null == relationshipPredicates ) ? expandFun.apply( startNode )
                        : Iterators.filter( expandFun.apply( startNode ), relationshipPredicates );
                return ImmutableList.copyOf( relationships );
            }
        };
    }

    private static final InitialBranchState<StepState> INITIAL_BRANCH_STATE = new InitialBranchState<StepState>()
    {
        @Override
        public StepState initialState( Path path )
        {
            return StepState.INITIAL_STATE;
        }

        public InitialBranchState<StepState> reverse()
        {
            // TODO deal with this in better way
            throw new UnsupportedOperationException();
        }
    };

    enum RepetitionState
    {
        TOO_LOW,
        IN_RANGE,
        ONLY_IN_RANGE,
        LAST_IN_RANGE,
        TOO_HIGH
    }

}
