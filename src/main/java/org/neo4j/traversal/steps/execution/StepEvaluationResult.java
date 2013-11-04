package org.neo4j.traversal.steps.execution;

import org.neo4j.traversal.steps.execution.StepsEvaluator.StepEvaluation;

public class StepEvaluationResult
{
    public static final StepEvaluationResult REJECT_STAY_EXCLUDE_PRUNE = new StepEvaluationResult(
            StepEvaluation.REJECT_STAY_EXCLUDE_PRUNE, 0 );
    public static final StepEvaluationResult REJECT_ADVANCE_EXCLUDE_CONTINUE = new StepEvaluationResult(
            StepEvaluation.REJECT_ADVANCE_EXCLUDE_CONTINUE, 0 );
    public static final StepEvaluationResult ACCEPT_ADVANCE_EXCLUDE_CONTINUE = new StepEvaluationResult(
            StepEvaluation.ACCEPT_ADVANCE_EXCLUDE_CONTINUE, 0 );
    public static final StepEvaluationResult ACCEPT_STAY_EXCLUDE_CONTINUE = new StepEvaluationResult(
            StepEvaluation.ACCEPT_STAY_EXCLUDE_CONTINUE, 0 );
    public static final StepEvaluationResult ACCEPT_STAY_INCLUDE_CONTINUE = new StepEvaluationResult(
            StepEvaluation.ACCEPT_STAY_INCLUDE_CONTINUE, 0 );
    public static final StepEvaluationResult ACCEPT_ADVANCE_INCLUDE_PRUNE = new StepEvaluationResult(
            StepEvaluation.ACCEPT_ADVANCE_INCLUDE_PRUNE, 0 );

    private final StepEvaluation stepEvaluation;
    private final int stepState;

    public static StepEvaluationResult acceptStayExcludeContinue( int stepState )
    {
        return new StepEvaluationResult( StepEvaluation.ACCEPT_STAY_EXCLUDE_CONTINUE, stepState );
    }

    public static StepEvaluationResult acceptStayIncludeContinue( int stepState )
    {
        return new StepEvaluationResult( StepEvaluation.ACCEPT_STAY_INCLUDE_CONTINUE, stepState );
    }

    private StepEvaluationResult( StepEvaluation stepEvaluation, int stepState )
    {
        this.stepEvaluation = stepEvaluation;
        this.stepState = stepState;
    }

    public StepEvaluation stepEvaluation()
    {
        return stepEvaluation;
    }

    public int stepState()
    {
        return stepState;
    }
}
