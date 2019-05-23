package com.javaexcel.automation.core.reporting;

import java.util.Comparator;

import org.testng.IClass;

class TestClassComparator implements Comparator<IClass>
{
	@Override
	public int compare(IClass class1, IClass class2)
	{
		String class1Name = class1.getXmlClass().getUniqueClass().getName();
		String class2Name = class2.getXmlClass().getUniqueClass().getName();
		if (class1Name.equals(class2Name)){
			return 0;
		}
		if (class1.getEndMillis() == class2.getEndMillis()){
			return class1Name.compareTo(class2Name);
		}
		return class1.getEndMillis() > class2.getEndMillis() ? 1 : -1;
		//return 0;
	}
}
