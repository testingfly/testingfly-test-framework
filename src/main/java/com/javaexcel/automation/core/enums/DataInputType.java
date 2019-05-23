package com.javaexcel.automation.core.enums;

public enum DataInputType {
	EXCEL,
	XML,
	AUTOMATIONDATABASE;

	public static DataInputType parse(String prop) {
		switch(prop.toLowerCase()){
		case "excel":
			return EXCEL;
		case "db":
			return AUTOMATIONDATABASE;
		case "xml":
		default:
			return XML;
		}
	}
}