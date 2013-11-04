package org.neo4j.traversal.steps.execution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

// TODO test
public class StepsUtils
{
    // TODO generalize to "aggregate" where input function defined what happens:
    // TODO count, group, distinct
    // TODO current impl -> Function<GROUP_TYPE, Map<GROUP_TYPE,INTEGER>>
    // overwrites aggregate
    // TODO then build helpers on top of "aggregate" -> count, group
    public static <THING> Map<THING, Integer> count( Iterator<THING> thingsToCount )
    {
        Map<THING, Integer> countedThings = new HashMap<THING, Integer>();
        while ( thingsToCount.hasNext() )
        {
            THING thing = thingsToCount.next();
            if ( countedThings.containsKey( thing ) )
            {
                int count = countedThings.get( thing );
                countedThings.put( thing, count + 1 );
            }
            else
            {
                countedThings.put( thing, 1 );
            }
        }
        return countedThings;
    }

    public static Iterator<Node> projectNodesFromPath( Iterable<Path> paths, int indexInPath )
    {
        return projectNodesFromPath( paths.iterator(), indexInPath );
    }

    public static Iterator<Node> projectNodesFromPath( Iterator<Path> paths, final int indexInPath )
    {
        return Iterators.transform( paths, new Function<Path, Node>()
        {
            @Override
            public Node apply( Path path )
            {
                return Iterables.get( path.nodes(), indexInPath );
            }
        } );
    }

    public static <THING> Iterator<THING> distinct( Iterable<THING> withDupicates )
    {
        return distinct( withDupicates.iterator() );
    }

    public static <THING> Iterator<THING> distinct( Iterator<THING> withDupicates )
    {
        Predicate<THING> distinctFun = new Predicate<THING>()
        {
            Set<THING> alreadySeen = new HashSet<THING>();

            @Override
            public boolean apply( THING input )
            {
                return alreadySeen.add( input );
            }
        };
        return Iterators.filter( withDupicates, distinctFun );
    }

    public static String pathString( Path path )
    {
        StringBuilder sb = new StringBuilder();
        Node lastNode = null;
        for ( PropertyContainer thing : path )
        {
            if ( thing instanceof Node )
            {
                lastNode = (Node) thing;
                sb.append( "(" ).append(
                        Sets.newHashSet( lastNode.getLabels() ).toString().replace( "[", "" ).replace( "]", "" ) ).append(
                        ")" );
            }
            if ( thing instanceof Relationship )
            {
                Relationship relationship = (Relationship) thing;
                if ( relationship.getStartNode().equals( lastNode ) )
                {
                    sb.append( "-[" ).append( relationship.getType().name() ).append( "]->" );
                }
                else
                {
                    sb.append( "<-[" ).append( relationship.getType().name() ).append( "]-" );
                }
            }
        }
        return sb.toString();
    }
}
