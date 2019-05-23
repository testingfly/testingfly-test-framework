package com.javaexcel.automation.core.table;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

//import Exception.FilloException;
//import Fillo.Recordset;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Recordset;
//import com.codoid.products.fillo.Field;



public class Record {

	private Map<String, Field> data;

	public Record(){
		data = new TreeMap<String, Field>(String.CASE_INSENSITIVE_ORDER);
	}

	public Record(Recordset recordset){
		data = new TreeMap<String, Field>(String.CASE_INSENSITIVE_ORDER);

		int fieldCount;
		try {
			fieldCount = recordset.getFieldNames().size();
			for (int i = 0; i < fieldCount; i ++){

				String key = recordset.getField(i).name();
				String value = recordset.getField(i).value();
				add(key, value);
			}
		} catch (FilloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Record(Record record){
		this();
		for (String fieldName : record.getData().keySet()){
			data.put(fieldName, new Field(record.getField(fieldName)));
		}
	}

	public void set(Map<String, Field> data){
		this.data = data;
	}

	public String getValue(String columnName){
		if (data.get(columnName) != null) {
			return data.get(columnName).getValue();
		} else {
			return null;
		}
	}

	public Field getField(String columnName){
		return data.get(columnName);
	}

	public void add(String key, Field field){
		data.put(key, field);
	}

	public void add(Field field){
		data.put(field.getColumnName(), field);
	}

	public void add(String key, String value){
		data.put(key, new Field(value, key));
	}

	public void add(String key, String value, Object pointer){
		data.put(key, new Field(value, pointer, key));
	}

	public Set<String> getColumns(){
		return data.keySet();
	}
	
	public Collection<Field> getFields(){
		return data.values();
	}
	
	public String toString(){
		return data.toString();
	}
	
	public Map<String, Field> getData(){
		return data;
	}
	
	public void append(Record r){
		data.putAll(r.getData());
	}
	
	public void clear(){
		data.clear();
	}
}
