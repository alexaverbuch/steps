package org.neo4j.traversal.steps;

class PropertyValue
{
    private final String key;
    private final Object value;

    public PropertyValue( String key, Object value )
    {
        this.key = key;
        this.value = value;
    }

    public String key()
    {
        return key;
    }

    public Object value()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return "PropertyValue [key=" + key + ", value=" + value + "]";
    }
}
