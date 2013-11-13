package org.neo4j.traversal.steps;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.kernel.impl.util.FileUtils;
import org.neo4j.traversal.steps.Step;
import org.neo4j.traversal.steps.StepsBuilder;
import org.neo4j.traversal.steps.execution.StepsUtils;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import static org.junit.Assert.*;
import static org.neo4j.traversal.steps.Filters.*;
import static org.hamcrest.CoreMatchers.*;

public class StepTraversalTest
{
    enum RelTypes implements RelationshipType
    {
        REPLY_OF
    }

    enum Labels implements Label
    {
        Comment,
        Post
    }

    public static String TEMP_DB_DIR = "tempDb";
    public static GraphDatabaseService db = null;
    public static ExecutionEngine engine = null;
    public static StepsBuilder stepsBuilder = null;
    public static TraversalDescription baseTraversalDescription = null;

    @Before
    public void openDb() throws IOException
    {
        FileUtils.deleteRecursively( new File( TEMP_DB_DIR ) );
        db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder( TEMP_DB_DIR ).newGraphDatabase();
        engine = new ExecutionEngine( db );
        stepsBuilder = new StepsBuilder();
        baseTraversalDescription = db.traversalDescription().uniqueness( Uniqueness.NONE ).breadthFirst();
    }

    @After
    public void closeDb()
    {
        db.shutdown();
    }

    @Test
    public void shouldReturnExpectedResultForPathLength0MatchSuccess()
    {
        long startNodeId = createGraph( "CREATE (post:Post)\n" + "RETURN id(post) AS id" );

        TraversalDescription td = stepsBuilder.build( baseTraversalDescription,
        // Steps
                Step.one( node().hasLabel( Labels.Post ) ) );

        Map<Integer, Integer> expectedPathLengthCounts = new HashMap<Integer, Integer>();
        expectedPathLengthCounts.put( 0, 1 );
        assertThatExpectedNumberAndLengthOfPathsAreDiscovered( db, startNodeId, td, expectedPathLengthCounts );
    }

    @Test
    public void shouldReturnExpectedResultForPathLength0MatchFailure()
    {
        long startNodeId = createGraph( "CREATE (post:Post)\n" + "RETURN id(post) AS id" );

        TraversalDescription td = stepsBuilder.build( baseTraversalDescription,
        // Steps
                Step.one( node().hasLabel( Labels.Comment ) ) );

        Map<Integer, Integer> expectedPathLengthCounts = new HashMap<Integer, Integer>();
        assertThatExpectedNumberAndLengthOfPathsAreDiscovered( db, startNodeId, td, expectedPathLengthCounts );
    }

    @Test
    public void shouldReturnExpectedResultForPathLength1MatchSuccess()
    {
        long startNodeId = createGraph( "CREATE (comment:Comment)-[:REPLY_OF]->(:Post)\n" + "RETURN id(comment) AS id" );

        TraversalDescription td = stepsBuilder.build(
                baseTraversalDescription,
                // Steps
                Step.one( node().hasLabel( Labels.Comment ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ) ),
                Step.one( node().hasLabel( Labels.Post ) ) );

        Map<Integer, Integer> expectedPathLengthCounts = new HashMap<Integer, Integer>();
        expectedPathLengthCounts.put( 1, 1 );
        assertThatExpectedNumberAndLengthOfPathsAreDiscovered( db, startNodeId, td, expectedPathLengthCounts );
    }

    @Test
    public void shouldReturnExpectedResultForPathLength1MatchFailure()
    {
        long startNodeId = createGraph( "CREATE (comment:Comment)-[:REPLY_OF]->(:Post)\n" + "RETURN id(comment) AS id" );

        TraversalDescription td = stepsBuilder.build(
                baseTraversalDescription,
                // Steps
                Step.one( node().hasLabel( Labels.Comment ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ) ),
                Step.one( node().hasLabel( Labels.Comment ) ) );

        Map<Integer, Integer> expectedPathLengthCounts = new HashMap<Integer, Integer>();
        assertThatExpectedNumberAndLengthOfPathsAreDiscovered( db, startNodeId, td, expectedPathLengthCounts );
    }

    @Test
    public void shouldReturnExpectedResultForPathLength2MatchSuccess()
    {
        long startNodeId = createGraph( "CREATE (comment:Comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Post)\n"
                                        + "RETURN id(comment) AS id" );

        TraversalDescription td = stepsBuilder.build( baseTraversalDescription,
                // Steps
                Step.manyExact( node().hasLabel( Labels.Comment ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ), 1 ),
                Step.one( node().hasLabel( Labels.Post ) ) );

        Map<Integer, Integer> expectedPathLengthCounts = new HashMap<Integer, Integer>();
        expectedPathLengthCounts.put( 2, 1 );
        assertThatExpectedNumberAndLengthOfPathsAreDiscovered( db, startNodeId, td, expectedPathLengthCounts );
    }

    @Test
    public void shouldReturnExpectedResultForPathLength2MatchFailure()
    {
        long startNodeId = createGraph( "CREATE (comment:Comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Post)\n"
                                        + "RETURN id(comment) AS id" );

        TraversalDescription td = stepsBuilder.build( baseTraversalDescription,
                // Steps
                Step.manyExact( node().hasLabel( Labels.Comment ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ), 2 ),
                Step.one( node().hasLabel( Labels.Comment ) ) );

        Map<Integer, Integer> expectedPathLengthCounts = new HashMap<Integer, Integer>();
        assertThatExpectedNumberAndLengthOfPathsAreDiscovered( db, startNodeId, td, expectedPathLengthCounts );
    }

    @Test
    public void shouldReturnExpectedResultForUnboundedPathLengthSuccess()
    {
        long startNodeId = createGraph( "CREATE (comment:Comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Comment)\n"
                                        + "RETURN id(comment) AS id" );

        TraversalDescription td = stepsBuilder.build( baseTraversalDescription,
        // Steps
                Step.manyRange( node().hasLabel( Labels.Comment ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ), 0,
                        Step.UNLIMITED ) );

        Map<Integer, Integer> expectedPathLengthCounts = new HashMap<Integer, Integer>();
        expectedPathLengthCounts.put( 0, 1 );
        expectedPathLengthCounts.put( 1, 1 );
        expectedPathLengthCounts.put( 2, 1 );
        expectedPathLengthCounts.put( 3, 1 );
        assertThatExpectedNumberAndLengthOfPathsAreDiscovered( db, startNodeId, td, expectedPathLengthCounts );
    }

    @Test
    public void shouldReturnExpectedResultForBoundedRangePathLengthSuccess()
    {
        long startNodeId = createGraph( "CREATE (comment:Comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Comment)\n"
                                        + "RETURN id(comment) AS id" );

        TraversalDescription td = stepsBuilder.build( baseTraversalDescription,
        // Steps
                Step.manyRange( node().hasLabel( Labels.Comment ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ), 0, 2 ) );

        Map<Integer, Integer> expectedPathLengthCounts = new HashMap<Integer, Integer>();
        expectedPathLengthCounts.put( 0, 1 );
        expectedPathLengthCounts.put( 1, 1 );
        expectedPathLengthCounts.put( 2, 1 );
        assertThatExpectedNumberAndLengthOfPathsAreDiscovered( db, startNodeId, td, expectedPathLengthCounts );
    }

    @Test
    public void shouldReturnExpectedResultForUnboundedPathLengthFailure()
    {
        long startNodeId = createGraph( "CREATE (comment:Comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Comment)\n"
                                        + "RETURN id(comment) AS id" );

        TraversalDescription td = stepsBuilder.build( baseTraversalDescription,
        // Steps
                Step.manyRange( node().hasLabel( Labels.Post ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ), 0,
                        Step.UNLIMITED ) );

        Map<Integer, Integer> expectedPathLengthCounts = new HashMap<Integer, Integer>();
        assertThatExpectedNumberAndLengthOfPathsAreDiscovered( db, startNodeId, td, expectedPathLengthCounts );
    }

    @Test
    public void shouldReturnExpectedResultForBranchedGraphSuccess()
    {
        long startNodeId = createGraph( "CREATE (comment:Comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Post),\n"
                                        + "(comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Post)"
                                        + "RETURN id(comment) AS id" );

        TraversalDescription td = stepsBuilder.build( baseTraversalDescription,
                // Steps
                Step.manyRange( node().hasLabel( Labels.Comment ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ), 1, 2 ),
                Step.one( node().hasLabel( Labels.Post ) ) );

        Map<Integer, Integer> expectedPathLengthCounts = new HashMap<Integer, Integer>();
        expectedPathLengthCounts.put( 2, 1 );
        expectedPathLengthCounts.put( 3, 1 );
        assertThatExpectedNumberAndLengthOfPathsAreDiscovered( db, startNodeId, td, expectedPathLengthCounts );
    }

    @Test
    public void shouldNotWorkWhenDirectionsAreReversed()
    {
        long startNodeId = createGraph( "CREATE (comment:Comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Post),\n"
                                        + "(comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Post)"
                                        + "RETURN id(comment) AS id" );

        TraversalDescription td = stepsBuilder.build( baseTraversalDescription,
                // Steps
                Step.manyRange( node().hasLabel( Labels.Comment ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.INCOMING ), 1, 2 ),
                Step.one( node().hasLabel( Labels.Post ) ) );

        Map<Integer, Integer> expectedPathLengthCounts = new HashMap<Integer, Integer>();
        assertThatExpectedNumberAndLengthOfPathsAreDiscovered( db, startNodeId, td, expectedPathLengthCounts );
    }

    @Test
    public void shouldWorkForPatternGraph()
    {
        long startNodeId = createGraph( "CREATE (comment:Comment)-[:REPLY_OF]->(:Comment)-[:REPLY_OF]->(:Post)-[:REPLY_OF]->(post1:Post),\n"
                                        + "(post1)-[:REPLY_OF]->(:Post)-[:REPLY_OF]->(:Post),\n"
                                        + "(post1)-[:REPLY_OF]->(:Post)-[:REPLY_OF]->(:Post)-[:REPLY_OF]->(:Post)\n"
                                        + "RETURN id(comment) AS id" );

        TraversalDescription td = stepsBuilder.build( baseTraversalDescription,
                // Steps
                Step.manyRange( node().hasLabel( Labels.Comment ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ), 0, 1 ),
                Step.manyRange( node().hasLabel( Labels.Post ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ), 2, 3 ) );

        /*
        -------C1---------===============P2============
        (C)-[R]->(C)-[R]->(P)-[R]->(p:P)-[R]->(P)-[R]->(P)
                          ===============P2============
                          ===============P3=====================
                                   (p:P)-[R]->(P)-[R]->(P)-[R]->(P)
         */

        Map<Integer, Integer> expectedPathLengthCounts = new HashMap<Integer, Integer>();
        expectedPathLengthCounts.put( 4, 2 );
        expectedPathLengthCounts.put( 5, 2 );
        assertThatExpectedNumberAndLengthOfPathsAreDiscovered( db, startNodeId, td, expectedPathLengthCounts );
    }

    @Test
    public void complexExample()
    {
        long startNodeId = createGraph(

        "CREATE (comment:Comment)-[:REPLY_OF]->(:Comment {content : 'hi'})-[:REPLY_OF]->(:Post)-[:REPLY_OF]->(post:Post),\n"

        + "(post)-[:REPLY_OF]->(:Post {title : 'non-things'})-[:REPLY_OF]->(:Post),\n"

        + "(post)-[:REPLY_OF]->(:Post {title : 'things'})-[:REPLY_OF]->(:Post)-[:REPLY_OF]->(:Post)\n"

        + "RETURN id(comment) AS id" );

        TraversalDescription td = stepsBuilder.build( baseTraversalDescription,
        // Steps
                Step.one( node().hasLabel( Labels.Comment ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ) ),

                Step.one( node().hasLabel( Labels.Comment ).propertyEquals( "content", "hi" ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ) ),

                Step.manyExact( node().hasLabel( Labels.Post ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ), 1 ),

                Step.one( node().hasLabel( Labels.Post ).propertyEquals( "title", "things" ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ) ),

                Step.manyRange( node().hasLabel( Labels.Post ),
                        relationship().hasType( RelTypes.REPLY_OF ).hasDirection( Direction.OUTGOING ), 0,
                        Step.UNLIMITED ) );

        Map<Integer, Integer> expectedPathLengthCounts = new HashMap<Integer, Integer>();
        /*
        (comment:Comment)-[:REPLY_OF]->(:Comment {content : 'hi'})-[:REPLY_OF]->(:Post)-[:REPLY_OF]->(post:Post)
        (post)-[:REPLY_OF]->(:Post {title : 'things'})-[:REPLY_OF]->(:Post)
         */
        expectedPathLengthCounts.put( 5, 1 );
        /*
        (comment:Comment)-[:REPLY_OF]->(:Comment {content : 'hi'})-[:REPLY_OF]->(:Post)-[:REPLY_OF]->(post:Post)
        (post)-[:REPLY_OF]->(:Post {title : 'things'})-[:REPLY_OF]->(:Post)-[:REPLY_OF]->(:Post)
         */
        expectedPathLengthCounts.put( 6, 1 );
        assertThatExpectedNumberAndLengthOfPathsAreDiscovered( db, startNodeId, td, expectedPathLengthCounts );
    }

    Map<Integer, Integer> pathLengthCounts( Iterable<Path> paths )
    {
        Iterable<Integer> pathLengths = Iterables.transform( paths, new Function<Path, Integer>()
        {
            @Override
            public Integer apply( Path path )
            {
                return path.length();
            }
        } );
        return StepsUtils.count( pathLengths.iterator() );
    }

    void assertThatExpectedNumberAndLengthOfPathsAreDiscovered( GraphDatabaseService db, long startNodeId,
            TraversalDescription td, Map<Integer, Integer> expectedPathLengthCounts )
    {
        boolean exceptionThrown = false;
        try (Transaction tx = db.beginTx())
        {
            Node startNode = db.getNodeById( startNodeId );
            Map<Integer, Integer> actualPathLengthCounts = pathLengthCounts( td.traverse( startNode ) );
            assertThat( actualPathLengthCounts, equalTo( expectedPathLengthCounts ) );
            tx.success();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            exceptionThrown = true;
        }
        assertThat( exceptionThrown, is( false ) );
    }

    long createGraph( String query )
    {
        long id = -1;
        try (Transaction tx = db.beginTx())
        {
            ExecutionResult result = engine.execute( query );
            id = (long) result.iterator().next().get( "id" );
            tx.success();
        }
        catch ( Exception e )
        {
            throw e;
        }
        return id;
    }
}
