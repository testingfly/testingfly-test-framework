package com.javaexcel.automation.core.testngdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestNGTest extends TestNGStructBase {

	private TestNGSuite suite;
	private List<TestNGClass> classes;
	private Map<String, String> groupDependencies;

	public TestNGTest(TestNGSuite suite, String name){
		super(name);
		this.suite = suite;
		classes = new ArrayList<TestNGClass>();
		suite.addTest(this);
	}

	public TestNGTest(TestNGTest test, String name){
		super(name);
		this.suite = test.getSuite();
		this.classes = test.getClasses();
		suite.addTest(this);
	}

	public TestNGSuite getSuite(){
		return suite;
	}

	public void setSuite(TestNGSuite suite){
		this.suite = suite;
	}

	public List<TestNGClass> getClasses() {
		return classes;
	}

	public void setClasses(List<TestNGClass> classes) {
		this.classes = classes;
	}

	public void addClass(TestNGClass cls){
		classes.add(cls);
	}

	public Map<String, String> getGroupDependencies() {
		return groupDependencies;
	}

	public void addDependency(String dependee, String dependsOn) {
		groupDependencies.put(dependee, dependsOn);
	}
}
