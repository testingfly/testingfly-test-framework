package com.javaexcel.automation.core.reporting;

import java.util.Comparator;

import com.javaexcel.automation.core.data.ITestInstance;

public class TestReportComparator implements Comparator<ITestInstance>
{
	@Override
	public int compare(ITestInstance ts1, ITestInstance ts2) {
		// TODO Auto-generated method stub
//		String id1 = ts1.getName();
//		String id2 = ts2.getName();
		
		int id1 = Integer.parseInt(ts1.getID());
		int id2 = Integer.parseInt(ts2.getID());
		
		//return id1.compareTo(id2);
		return id1-id2;
	}
}
