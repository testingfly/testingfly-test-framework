package com.javaexcel.automation.core.testngdata;

import java.util.HashMap;
import java.util.Map;

public class TestNGStructBase implements ITestNGStruct{
	protected String id;
	protected String configID;
	protected String name;
	protected String description;
	protected Map<String, String> parameters;

	public TestNGStructBase(String name) {
		this();
		setName(name);
	}
	
	public TestNGStructBase() {
		parameters = new HashMap<>();
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	
	public void addParameter(String parameter, String value){
		parameters.put(parameter, value);
	}

	@Override
	public String getConfigID() {
		return configID;
	}

	@Override
	public void setConfigID(String configID) {
		this.configID = configID;
	}
}