package com.javaexcel.automation.alm.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Entities")
@XmlAccessorType(XmlAccessType.FIELD)
public class EntitiesResult extends Entity
{
    
    @XmlElement(name = "Entity", required = true)
    private List<Entity> entity;
	
	@XmlElement(name = "Field", required = true)
    @XmlElementWrapper(name = "Fields")
    private List<Field> fields;

	@XmlAttribute(name = "TotalResults", required = true)
    private String totalResults;
	
	EntitiesResult(){
		
	}
	

    
    public EntitiesResult(EntitiesResult result)
    {
        this.entity = result.entity;
    	this.fields = result.fields;

        
    }
    
    public String totalResults()
    {
        return totalResults;
    }
    
    public List<Entity> entity()
    {
        return entity;
    }

    
  


}
