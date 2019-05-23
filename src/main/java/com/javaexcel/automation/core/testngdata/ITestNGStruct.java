package com.javaexcel.automation.core.testngdata;

import java.util.Map;

public interface ITestNGStruct {

	public String getName();

	public void setName(String name);

	public Map<String, String> getParameters();

	public void setParameters(Map<String, String> parameters);

	public void addParameter(String name, String value);

	public String getID();

	public void setID(String id);
	
	public String getConfigID();
	
	public void setConfigID(String configID);
}
