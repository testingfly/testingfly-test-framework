package com.javaexcel.automation.alm;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import com.javaexcel.automation.core.utils.Utils;

/*
 * # ALM connection properties
 *
 * host=localhost
 * port=8181
 * domain=b2b
 * project=rx
 * username=admin
 * password=admin
 */

public class Config
{
    private Properties properties;

    public Config(Properties properties) throws Exception
    {
        this.properties = properties;
        

        if (!check())
        {
            throw new Exception("Invalid ALM config.");
        }
    }

    public Config(String content) throws Exception
    {	
    	this(createPropertyFile(content));
    	
    }

    public String host()
    {
        return properties.getProperty("host", "");
    }

    public String port()
    {
        return properties.getProperty("port", "");
    }

    public String domain()
    {
        return properties.getProperty("domain", "");
    }

    public String project()
    {
        return properties.getProperty("project", "");
    }

    public String username()
    {
    	String prop = System.getProperty("username");
    	if(prop!=null && prop.length()>6 ){
			return prop;
		}
    	return properties.getProperty("username", "");
    }

    public String password()
    {
    	String prop = System.getProperty("password");
    	if(prop!=null && prop.length()>4 ){
			return prop;
		}
    	return properties.getProperty("password", "");
    }
    
    public String authToken()
    {
    	return Utils.encodeBase64(username(), password());
    	//return properties.getProperty("authToken", "");
    }
    
    public String testFolder()
    {
        return properties.getProperty("parentFolder", "");
    }
    
    public String getValue(String val)
    {
    	return properties.getProperty(val, "");
    }
    
    public String getValue(String val, String defaultVal)
    {
    	return properties.getProperty(val, defaultVal);
    }
    
    public String testLabFolder()
    {
        //return properties.getProperty("testLabFolder", "");
        return properties.getProperty("parentFolder", "");
    }
    
//    public String jUnitTestFolder()
//    {
//        //return properties.getProperty("jUnitTestFolder", "");
//        return "test-output/"+com.javaexcel.automation.core.data.Config.getProp("ProjectName");
//    }

    private static Properties createPropertyFile(String content) throws IOException
    {
        Properties almProperties = new Properties();
        almProperties.load(new FileReader(content));
        return almProperties;
    }

    private boolean check()
    {
        return properties.containsKey("host")
//            && properties.containsKey("port")
            && properties.containsKey("domain")
            && properties.containsKey("project")
            && properties.containsKey("username");
//            && properties.containsKey("password");
//        	&& properties.containsKey("authToken");
    }
}
