package org.neo4j.traversal.steps;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.traversal.steps.StepTraversalTest.Labels;
import org.neo4j.traversal.steps.exception.StepsException;
import org.neo4j.traversal.steps.exception.StepsExceptionType;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class StepsBuilderTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    public static GraphDatabaseService db = null;
    public static StepsBuilder stepsBuilder = null;
    public static TraversalDescription baseTraversalDescription = null;

    @Before
    public void openDb() throws IOException {
        String tempDir = temporaryFolder.newFolder().getAbsolutePath();
        db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(tempDir).newGraphDatabase();
        stepsBuilder = new StepsBuilder();
        baseTraversalDescription = db.traversalDescription().uniqueness(Uniqueness.NONE).breadthFirst();
    }

    @After
    public void closeDb() {
        db.shutdown();
    }

    @Test
    public void stepsBuildingShouldWhenInnerStepsDoNotHaveRelationshipDescriptor() {
        try {
            stepsBuilder.build(baseTraversalDescription,
                    // Steps
                    Step.one(Filters.node().hasLabel(Labels.Comment)),
                    Step.one(Filters.node().hasLabel(Labels.Post)));
        } catch (StepsException e) {
            assertThat(e.type(), equalTo(StepsExceptionType.ONLY_LAST_STEP_MAY_OMIT_RELATIONSHIP_DESCRIPTOR));
        }
    }

    @Test
    public void stepsBuildingShouldWhenInnerStepsDoNotHaveRelationshipDescriptor_() {
        try {
            stepsBuilder.build(baseTraversalDescription,
                    // Steps
                    Step.one(Filters.node().hasLabel(Labels.Comment), Filters.relationship()),
                    Step.one(Filters.node().hasLabel(Labels.Post)));
        } catch (StepsException e) {
            assertThat(e.type(), equalTo(StepsExceptionType.ONLY_LAST_STEP_MAY_OMIT_RELATIONSHIP_DESCRIPTOR));
        }
    }
}
