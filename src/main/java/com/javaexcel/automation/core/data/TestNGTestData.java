package com.javaexcel.automation.core.data;

import java.util.HashMap;
import java.util.Map;

public class TestNGTestData {
	public String ID, almID, testName, description;
	public String[] className;
	public String[][] methodNames, parameters, options;
	public String[][][] methodParameters;
	public Map<String, String> beforeTestParameters;
	
	public TestNGTestData(String ID, String almID, String testName, String[] className, String[][] methodNames, String[][][] methodParameters, String[][] parameters, String[][] options, String description){
		this.ID = ID;
		this.almID = almID;
		this.testName = testName;
		this.className = className;
		this.description = description;
		this.methodNames = methodNames;
		this.methodParameters = methodParameters;
		this.parameters = parameters;
		this.options = options;
		beforeTestParameters = new HashMap<String, String>();
	}
	
	public int getNumOfClasses(){
		return className.length;
	}
	
	public int getNumOfMethods(int classIndex){
		return methodNames[classIndex].length;
	}
	
	public int getNumOfMethodParameters(int classIndex, int methodIndex){
		if (methodParameters.length == 0) return 0;
		if (methodParameters[classIndex].length == 0) return 0;
		if (methodParameters[classIndex][methodIndex] == null) return 0;
		return methodParameters[classIndex][methodIndex].length;
//		try{
//			return methodParameters[classIndex][methodIndex].length;
//		}catch (Exception e){
//			return 0;
//		}
	}
	
	public int getNumOfParameters(int classIndex){
		try{
			return parameters[classIndex].length;
		}catch (Exception e){
			return 0;
		}
	}
}
