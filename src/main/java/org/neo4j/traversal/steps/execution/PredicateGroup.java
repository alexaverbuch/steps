package org.neo4j.traversal.steps.execution;

import java.util.Arrays;

import com.google.common.base.Predicate;

public class PredicateGroup<T> implements Predicate<T>
{
    private final Predicate<T>[] checks;

    public PredicateGroup( Predicate<T>[] checks )
    {
        this.checks = checks;
    }

    @Override
    public boolean apply( T input )
    {
        for ( int i = 0; i < checks.length; i++ )
        {
            if ( false == checks[i].apply( input ) ) return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "PredicateGroup [checks=" + Arrays.toString( checks ) + "]";
    }
}
