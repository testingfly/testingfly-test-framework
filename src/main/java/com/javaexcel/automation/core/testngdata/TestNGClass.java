package com.javaexcel.automation.core.testngdata;

import java.util.ArrayList;
import java.util.List;

public class TestNGClass extends TestNGStructBase{

	private TestNGTest test;
	private List<TestNGMethod> methods;

	public TestNGClass(TestNGTest test, String name){
		super(name);
		this.test = test;
		methods = new ArrayList<TestNGMethod>();

		test.addClass(this);
	}

	public TestNGTest getTest() {
		return test;
	}

	public void setTest(TestNGTest test) {
		this.test = test;
	}

	public List<TestNGMethod> getMethods() {
		return methods;
	}

	public void setMethods(List<TestNGMethod> methods) {
		this.methods = methods;
	}

	public void addMethod(TestNGMethod method){
		methods.add(method);
	}
}
