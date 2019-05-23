package com.javaexcel.automation.core.utils;

public class ComponentContext {

	private String className;
	private String methodName;
	private String packagePath;
	private String projectName;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public ComponentContext(String methodName, String className, String packagePath,String projectName){
		this.className = className;
		this.methodName = methodName;
		this.packagePath = packagePath;
		this.projectName=projectName;
	}
	public ComponentContext(String methodName, String className, String packagePath){
		this.className = className;
		this.methodName = methodName;
		this.packagePath = packagePath;
	}
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getPackagePath() {
		return packagePath;
	}

	public void setPackagePath(String packagePath) {
		this.packagePath = packagePath;
	}


}
