package com.javaexcel.automation.alm.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Entity")
public class Requirement extends Entity
{
    public Requirement(Entity entity)
    {
        super(entity);
    }

    public Requirement()
    {
        type("requirement");
    }


    public String type_id()
    {
        return fieldValue("type-id");
    }

    /**
     *
     * @param value
     */
    public void type_id(String value)
    {
        fieldValue("type-id", value);
    }
    
    public String comment()
    {
        return fieldValue("comment");
    }

    public void comment(String value)
    {
        fieldValue("comment", value);
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
    
    public String owner()
    {
        return fieldValue("owner");
    }

    public void owner(String value)
    {
        fieldValue("owner", value);
    }
    
}
