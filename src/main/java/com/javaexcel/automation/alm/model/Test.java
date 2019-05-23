package com.javaexcel.automation.alm.model;

import javax.xml.bind.annotation.XmlRootElement;

//@XmlRootElement
@XmlRootElement(name = "Entity")
public class Test extends Entity
{
    public Test(Entity entity)
    {
        super(entity);
    }

    public Test()
    {
        type("test");
    }

    /**
     *
     * @return
     */
    public String execStatus()
    {
        return fieldValue("exec-status");
    }

    /**
     *
     * @param value
     */
    public void execStatus(String value)
    {
        fieldValue("exec-status", value);
    }

    /**
     *
     * @return
     */
    public String owner()
    {
        return fieldValue("owner");
    }

    /**
     *
     * @param value
     */
    public void owner(String value)
    {
        fieldValue("owner", value);
    }

    /**
     *
     * @return
     */
    public String status()
    {
        return fieldValue("status");
    }

    /**
     *
     * @param value
     */
    public void status(String value)
    {
        fieldValue("status", value);
    }

    /**
     *
     * @return
     */
    public String subtypeId()
    {
        return fieldValue("subtype-id");
    }

    /**
     *
     * @param value
     */
    public void subtypeId(String value)
    {
        fieldValue("subtype-id", value);
    }

    /**
     *
     * @return
     */
    public String description()
    {
        return fieldValue("description");
    }

    /**
     *
     * @param value
     */
    public void description(String value)
    {
        fieldValue("description", value);
    }
    
    /**
    *
    * @return
    */
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
   
   
   public String user_template_xx(String name)
   {
       return fieldValue(name);
   }


   public void user_template_xx(String name, String value)
   {
       fieldValue(name, value);
   }
   }
