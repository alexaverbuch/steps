package org.neo4j.traversal.steps.execution;

public class StepState
{
    private static final int INITIAL_STEP = 0;
    private static final int INITIAL_STEP_STATE = 0;
    public static final StepState INITIAL_STATE = new StepState( INITIAL_STEP, INITIAL_STEP_STATE );
    private final Integer step;
    private final Integer state;

    public StepState( Integer step, Integer state )
    {
        this.step = step;
        this.state = state;
    }

    public Integer step()
    {
        return step;
    }

    public Integer state()
    {
        return state;
    }

    @Override
    public String toString()
    {
        return "StepState [step=" + step + ", state=" + state + "]";
    }
}
