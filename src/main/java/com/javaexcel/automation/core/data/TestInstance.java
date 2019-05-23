package com.javaexcel.automation.core.data;

import com.javaexcel.automation.core.utils.TestUtils;

public class TestInstance implements ITestInstance{

	protected String id;
	protected String testCaseID;
	protected String testID;
	protected String configID;
	protected String status;
	protected String name;
	protected String resource;
	
	public String getResource() {
		return this.resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}
	

	protected ITestSet testSet;

	public TestInstance(ITestSet testSet){
		this.testSet = testSet;
	}

	public TestInstance(ITestSet testSet, String name){
		this(testSet);
		this.name = name;
		this.status = "No Run";
	}

	@Override
	public String getID() {
		return id;
	}
	


	@Override
	public void setID(String id) {
		this.id = id;
	}
	
	@Override
	public String getTCID() {
		return testCaseID;
	}
	

	@Override
	public void setTCID(String testCaseID) {
		this.testCaseID = testCaseID;
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getTestID() {
		return testID;
	}

	@Override
	public void setTestID(String testID) {
		this.testID = testID;
	}

	@Override
	public ITestSet getTestSet() {
		return testSet;
	}

	@Override
	public void setTestSet(ITestSet testSet) {
		this.testSet = testSet;
	}

	@Override
	public String getKeyValue() {
		switch(TestUtils.getTestFilterType()){
		case Name:
		default:
			return getName();
		case TestID:
			return getTestID();
		//case TestCaseID:
			//return getTCID();
		case ConfigurationID:
			return getConfigID();
		}
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
