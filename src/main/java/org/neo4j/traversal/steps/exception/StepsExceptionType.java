package org.neo4j.traversal.steps.exception;

public enum StepsExceptionType
{
    MIN_REPETITIONS_MUST_BE_FINITE_POSITIVE_INT,
    MIN_REPETITIONS_MUST_BE_LOWER_THAN_MAX_REPETITIONS,
    ONLY_LAST_STEP_MAY_OMIT_RELATIONSHIP_DESCRIPTOR
}