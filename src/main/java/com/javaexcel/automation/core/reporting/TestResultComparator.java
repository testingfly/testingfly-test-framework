package com.javaexcel.automation.core.reporting;

import java.util.Comparator;

import org.testng.ITestResult;

class TestResultComparator implements Comparator<ITestResult>
{
	@Override
	public int compare(ITestResult result1, ITestResult result2)
	{
		return result1.getEndMillis() > result2.getEndMillis() ? 1 : -1;
	}
}
