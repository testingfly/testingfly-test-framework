package com.javaexcel.automation.core.testngdata;

import java.util.ArrayList;
import java.util.List;

public class TestNGSuite extends TestNGStructBase {

	private String parallel;
	private String threadCount;
	private String configFailurePolicy;

	private List<TestNGTest> tests;

	public TestNGSuite(){
		super();
		tests = new ArrayList<TestNGTest>();
	}

	public List<TestNGTest> getTests() {
		return tests;
	}

	public void setTests(List<TestNGTest> tests) {
		this.tests = tests;
	}

	public void addTest(TestNGTest test){
		tests.add(test);
	}

	public String getParallel() {
		return parallel;
	}

	public void setParallel(String parallel) {
		this.parallel = parallel;
	}

	public String getConfigFailurePolicy() {
		return configFailurePolicy;
	}

	public void setConfigFailurePolicy(String configFailurePolicy) {
		this.configFailurePolicy = configFailurePolicy;
	}

	public String getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(String threadCount) {
		this.threadCount = threadCount;
	}
	
	public boolean containsTest(String testName){
		for (TestNGTest test : tests){
			if (test.getName() != null && test.getName().equalsIgnoreCase(testName)){
				return true;
			}
		}
		return false;
	}
}
