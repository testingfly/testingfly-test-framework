package com.javaexcel.automation.core.testngdata;

public class TestNGMethod extends TestNGStructBase{

	private TestNGClass cls;
	private int methodIndex;

	public TestNGMethod(TestNGClass cls, String name){
		super(name);
		this.cls = cls;
		methodIndex = 0;
		cls.addMethod(this);
	}

	public TestNGMethod(TestNGClass cls, String name, int methodIndex){
		super(name);
		this.cls = cls;
		this.methodIndex = methodIndex;
	}

	public TestNGClass getCls() {
		return cls;
	}

	public void setCls(TestNGClass cls) {
		this.cls = cls;
	}

	public int getMethodIndex() {
		return methodIndex;
	}

	public void setMethodIndex(int methodIndex) {
		this.methodIndex = methodIndex;
	}
}
