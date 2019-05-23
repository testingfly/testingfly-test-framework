package com.javaexcel.automation.alm.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Entity")
public class TestFolder extends Entity
{
    public TestFolder(Entity entity)
    {
        super(entity);
    }

    public TestFolder()
    {
        
    }

    public TestFolder(String type)
    {
        type(type);
    }

    public String parent_id()
    {
        return fieldValue("parent-id");
    }

    /**
     *
     * @param value
     */
    public void subtype_id(String value)
    {
        fieldValue("parent-id", value);
    }
    
    public String name()
    {
        return fieldValue("name");
    }

    public void name(String value)
    {
        fieldValue("name", value);
    }
    
    public String description()
    {
        return fieldValue("description");
    }

    public void description(String value)
    {
        fieldValue("description", value);
    }
    
}
