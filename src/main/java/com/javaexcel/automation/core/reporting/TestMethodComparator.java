package com.javaexcel.automation.core.reporting;

import java.util.Comparator;

import org.testng.ITestNGMethod;

class TestMethodComparator implements Comparator<ITestNGMethod>
{
	@Override
	public int compare(ITestNGMethod method1,
			ITestNGMethod method2)
	{
		int compare = method1.getUniqueClass().getName().compareTo(method2.getUniqueClass().getName());
		if (compare == 0)
		{
			compare = method1.getEndMillis() > method2.getEndMillis() ? 1 : -1;
		}
		return compare;
		//return 0;
	}
}
