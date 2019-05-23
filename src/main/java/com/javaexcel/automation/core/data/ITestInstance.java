package com.javaexcel.automation.core.data;

//import com.wellsfargo.automation.core.data.ITestSet;

public interface ITestInstance {

	String getID();
	
	void setID(String id);
	
	String getTCID();
	
	void setTCID(String testCaseID);
	
	String getConfigID();
	
	void setConfigID(String configID);

	String getStatus();

	void setStatus(String status);

	String getName();

	void setName(String name);

	String getTestID();

	void setTestID(String testID);

	ITestSet getTestSet();

	void setTestSet(ITestSet testSet);

	String getKeyValue();
	
	String getResource();
	


}