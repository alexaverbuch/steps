package org.neo4j.traversal.steps.exception;

public class StepsException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    private final StepsExceptionType type;

    public StepsException( StepsExceptionType type )
    {
        super();
        this.type = type;
    }

    public StepsException( Throwable cause, StepsExceptionType type )
    {
        super( cause );
        this.type = type;
    }

    public StepsException( String message, Throwable cause, StepsExceptionType type )
    {
        super( message, cause );
        this.type = type;
    }

    public StepsExceptionType type()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return super.toString() + "\ntype=" + type;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( type == null ) ? 0 : type.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        StepsException other = (StepsException) obj;
        if ( type != other.type ) return false;
        return true;
    }
}
