package com.javaexcel.automation.core.data;

import java.util.Collection;
import com.javaexcel.automation.core.data.ITestInstance;

public interface ITestSet {

	int size();

	Collection<ITestInstance> getInstances();

	ITestInstance getTestInstanceByName(String testName);

	ITestInstance getTestInstanceByID(String testInstanceID);

	void add(ITestInstance instance);

	String getID();

	void setID(String id);
	
	String getName();
	
	void setName(String name);
}
