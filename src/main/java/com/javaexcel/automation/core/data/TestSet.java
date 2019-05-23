package com.javaexcel.automation.core.data;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import com.javaexcel.automation.core.data.ITestInstance;
import com.javaexcel.automation.core.data.ITestSet;

public class TestSet implements ITestSet, Comparator<ITestSet>{

	protected String id;
	protected String name;
	protected Map<String, ITestInstance> testInstances;
	protected Map<String, String> instanceIDToNameMap; //key: test name, value: test id

	public TestSet(String id, String name){
		setID(id);
		setName(name);
		testInstances = new HashMap<>();
		instanceIDToNameMap = new HashMap<>();
	}
	
	public TestSet(String id){
		this(id, null);
	}

	public TestSet(){
		this(null, null);
	}

	@Override
	public int size(){
		return testInstances.size();
	}

	@Override
	public Collection<ITestInstance> getInstances(){
		return testInstances.values();
	}

	@Override
	public ITestInstance getTestInstanceByName(String testName){
		return testInstances.get(instanceIDToNameMap.get(testName));
	}

	@Override
	public ITestInstance getTestInstanceByID(String testInstanceID){
		return testInstances.get(testInstanceID);
	}

	@Override
	public void add(ITestInstance instance){
		testInstances.put(instance.getID(), instance);
		instanceIDToNameMap.put(instance.getName(), instance.getID());
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
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	
	@Override
	public int compare(ITestSet ts1, ITestSet ts2) {
		String id1 = ts1.getName();
		String id2 = ts2.getName();
		return id1.compareTo(id2);
	}




}
