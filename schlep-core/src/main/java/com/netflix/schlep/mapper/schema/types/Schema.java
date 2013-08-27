package com.netflix.schlep.mapper.schema.types;

/**
 * A schema that captures the definition of an entity by breaking
 * it down into properties.  Note that for entities that contained
 * nested elements (i.e. lists of other entities) the child entity
 * schema will become part of this schema.  The alternative is to
 * keep track of ids to child entities in which case on the ID is
 * included with the schema and the child entity type remains 
 * outside of the context of this schema and can therefore change
 * independently.
 * 
 * @author elandau
 */
public abstract class Schema  {
    private String description;
    
    /**
     * Return the core type for this schema or property.  A top level schema
     * will normally be of type OBJECT whereas properties can be any one of
     * of SchemaType.  
     * 
     * @return
     */
    public abstract SchemaType getType();

    /**
     * Set an arbitrary description 
     * 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * @return  Get the description
     */
    public String getDescription() {
        return this.description;
    }
}
