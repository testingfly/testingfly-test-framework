package com.javaexcel.automation.core.execution;

import java.util.List;

import com.javaexcel.automation.core.data.ITestSet;
import com.javaexcel.automation.core.testngdata.TestNGSuite;

public interface SuiteCreator {

	TestNGSuite createSuite(List<ITestSet> testSets);

	

}