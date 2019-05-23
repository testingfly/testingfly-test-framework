package com.javaexcel.automation.alm.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Entity")
public class TestSet extends Entity
{
    public TestSet(Entity entity)
    {
        super(entity);
    }

    public TestSet()
    {
        type("test-set");
    }

    public String status()
    {
        return fieldValue("status");
    }

    public void status(String value)
    {
        fieldValue("status", value);
    }


    public String subtype_id()
    {
        return fieldValue("subtype-id");
    }

    /**
     *
     * @param value
     */
    public void subtype_id(String value)
    {
        fieldValue("subtype-id", value);
    }
    
    public String comment()
    {
        return fieldValue("comment");
    }

    public void comment(String value)
    {
        fieldValue("comment", value);
    }

    public String linkage()
    {
        return fieldValue("linkage");
    }

    public void linkage(String value)
    {
        fieldValue("linkage", value);
    }
    
    public void user_template_xx(String name, String value)
    {
        fieldValue(name, value);
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
