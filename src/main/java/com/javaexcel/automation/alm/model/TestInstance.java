package com.javaexcel.automation.alm.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Entity")
public class TestInstance extends Entity
{
    public TestInstance(Entity entity)
    {
        super(entity);
    }

    public TestInstance()
    {
        type("test-instance");
    }

    public String cycleId()
    {
        return fieldValue("cycle-id");
    }

    public void cycleId(String value)
    {
        fieldValue("cycle-id", value);
    }

    public String testId()
    {
        return fieldValue("test-id");
    }

    public void testId(String value)
    {
        fieldValue("test-id", value);
    }

    public String iterations()
    {
        return fieldValue("iterations");
    }

    public void iterations(String value)
    {
        fieldValue("iterations", value);
    }
    
    /**
    *
    * @return
    */
   public String subtypeId()
   {
       return fieldValue("subtype-id");
   }


   public void subtypeId(String value)
   {
       fieldValue("subtype-id", value);
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
