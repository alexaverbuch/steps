package org.neo4j.traversal.steps.execution;

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.PathEvaluator;

public class StepsEvaluator implements PathEvaluator<StepState>
{
    static enum StepEvaluation
    {
        /*
         * Match        REJECT/ACCEPT       Evaluator succeeded?
         * Evaluator    STAY/ADVANCE        Try next evaluator?
         * Path         EXCLUDE/INCLUDE     Include current path in result?
         * Proceed      PRUNE/CONTINUE      Continue traversing?
         */
        REJECT_STAY_EXCLUDE_PRUNE,
        REJECT_ADVANCE_EXCLUDE_CONTINUE,
        ACCEPT_ADVANCE_EXCLUDE_CONTINUE,
        ACCEPT_STAY_EXCLUDE_CONTINUE,
        ACCEPT_STAY_INCLUDE_CONTINUE,
        ACCEPT_ADVANCE_INCLUDE_PRUNE
    }

    private final StepEvaluator[] stepEvaluators;

    public StepsEvaluator( StepEvaluator... stepEvaluators )
    {
        this.stepEvaluators = stepEvaluators;
    }

    @Override
    public Evaluation evaluate( Path path )
    {
        // TODO deal with this in better way
        throw new UnsupportedOperationException();
    }

    @Override
    public Evaluation evaluate( Path path, BranchState<StepState> state )
    {
        int currentStep = state.getState().step();
        int stepStateState = state.getState().state();
        while ( true )
        {
            StepEvaluationResult result = stepEvaluators[currentStep].evaluate( path, stepStateState );
            switch ( result.stepEvaluation() )
            {
            case REJECT_STAY_EXCLUDE_PRUNE:
                /*
                 * evaluator failed fatally
                 */
                return Evaluation.EXCLUDE_AND_PRUNE;
            case REJECT_ADVANCE_EXCLUDE_CONTINUE:
                /*
                 * evaluator failed & exhausted
                 * advance evaluator immediately (current step) & retry
                 */
                currentStep++;
                stepStateState = result.stepState();
                continue;
            case ACCEPT_ADVANCE_EXCLUDE_CONTINUE:
                /*
                 * evaluator succeeded & exhausted
                 * advance evaluator for next step
                 */
                currentStep++;
                state.setState( new StepState( currentStep, result.stepState() ) );
                return Evaluation.EXCLUDE_AND_CONTINUE;
            case ACCEPT_STAY_EXCLUDE_CONTINUE:
                /*
                 * evaluator succeeded but not exhausted
                 * reuse evaluator @ next step
                 */
                state.setState( new StepState( currentStep, result.stepState() ) );
                return Evaluation.EXCLUDE_AND_CONTINUE;
            case ACCEPT_STAY_INCLUDE_CONTINUE:
                /*
                 * final evaluator succeeded but not exhausted
                 * return path & reuse evaluator @ next step
                 */
                state.setState( new StepState( currentStep, result.stepState() ) );
                return Evaluation.INCLUDE_AND_CONTINUE;
            case ACCEPT_ADVANCE_INCLUDE_PRUNE:
                /*
                 * final evaluator succeeded & exhausted
                 * return path
                 */
                return Evaluation.INCLUDE_AND_PRUNE;
            }
        }
    }

    public static interface StepEvaluator
    {
        StepEvaluationResult evaluate( Path path, int stepState );
    }
}
