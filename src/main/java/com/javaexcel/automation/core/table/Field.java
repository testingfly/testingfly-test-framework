package com.javaexcel.automation.core.table;

public class Field {

	private String value;
	private Object pointer;
	private String columnName;

	public Field(String value, Object pointer, String columnName){
		this.value = value;
		this.pointer = pointer;
		this.columnName = columnName;
	}

	public Field(String value, String columnName){
		this(value, null, columnName);
	}

	public Field(String value){
		this(value, null, "");
	}

	public Field(Field field) {
		this(field.getValue(), field.getPointer(), field.getColumnName());
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Object getPointer() {
		return pointer;
	}

	public void setPointer(Object pointer) {
		this.pointer = pointer;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	@Override
	public String toString(){
		return value;
	}
}
