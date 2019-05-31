package com.javaexcel.automation.core.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import com.javaexcel.automation.core.utils.Utils;

public class Table {

	private Set<String> columns;
	private List<Record> records;

	public Table() {
		records = new ArrayList<Record>();
		columns = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	}

	public Table(List<Record> records) {
		this.records = records;
		if (records.size() > 0) {
			setColumns(records.get(0));
		}
	}

	public Record getRecord(int index) {
		return records.get(index);
	}

	public void setRecords(List<Record> records) {
		this.records = records;
	}

	public void addRecord(Record record) {
		records.add(record);

	}

	public Set<String> getColumns() {
		return columns;
	}

	public void setColumns(Collection<String> columns) {
		this.columns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		this.columns.addAll(columns);
	}

	public void setColumns(Record record) {
		columns.addAll(record.getColumns());

	}

	public int recordCount() {
		return records.size();
	}

	public List<Record> getRecords() {
		return records;
	}

	/**
	 * Returns list of records from table based on specified criteria.
	 * 
	 * @param conditionColumns - Columns to check values for.
	 * @param conditionValues  - Values that corresponding columns should have.
	 * @return - List of records matching criteria.
	 */
	public List<Record> getRecords(String[] conditionColumns, String[] conditionValues) {
		if (conditionColumns == null || conditionColumns.length == 0) {
			return getRecords();
		} else if (conditionColumns[0].equalsIgnoreCase("#random")) {
			return Arrays.asList(getRandomRecord());
		} else if (conditionColumns.length != conditionValues.length) {
			Utils.printDebug("Error - Number of conditions in table query do not match number of values.");
			return null;
		}

		List<Record> queriedRecords = new ArrayList<Record>();

		for (Record record : records) {
			boolean check = true;
			for (int i = 0; i < conditionColumns.length; i++) {
				String checkValue = record.getValue(conditionColumns[i]);
				if (!checkValue.equalsIgnoreCase(conditionValues[i])) {
					check = false;
					break;
				}
			}
			if (check) {
				queriedRecords.add(record);
			}
		}

		return queriedRecords;
	}

	/**
	 * Returns list of records from table based on specified criteria.
	 * 
	 * @param conditionColumn - Column to check value for.
	 * @param conditionValue  - Value that conditionValue should have.
	 * @return - List of records matching criteria.
	 */
	public List<Record> getRecords(String conditionColumn, String conditionValue) {
		return getRecords(new String[] { conditionColumn }, new String[] { conditionValue });
	}

	public Table getRecords(Map<String, String> conditions) {
		List<Record> records = getRecords(conditions.keySet().toArray(new String[conditions.size()]),
				conditions.values().toArray(new String[conditions.size()]));
		Table t = new Table();
		t.setRecords(records);
		return t;
	}

	public Record getRandomRecord() {
		Random rng = new Random();
		int rn = rng.nextInt(records.size());
		return records.get(rn);
	}

	/**
	 * Returns field from table based on specified criteria.
	 * 
	 * @param columnName       - Column name of field to return.
	 * @param conditionColumns - Columns to check values for.
	 * @param conditionValues  - Values that corresponding columns should have.
	 * @return - Field matching criteria.
	 */
	public Field getField(String columnName, String[] conditionColumns, String[] conditionValues) {
		List<Record> queriedRecords = getRecords(conditionColumns, conditionValues);
		if (queriedRecords.size() == 0) {
			Utils.printDebug("Query on table returned no results.");
			return null;
		}
		if (queriedRecords.size() > 1) {
			Utils.printDebug("Query on table returned multiple results.");
		}
		return queriedRecords.get(0).getField(columnName);
	}

	/**
	 * Returns field value from table based on specified criteria.
	 * 
	 * @param columnName       - Column name of field to return.
	 * @param conditionColumns - Columns to check values for.
	 * @param conditionValues  - Values that corresponding columns should have.
	 * @return - Field value matching criteria.
	 */
	public String getValue(String columnName, String conditionColumn, String conditionValue) {
		Field field = getField(columnName, conditionColumn, conditionValue);
		return field.getValue();
	}

	/**
	 * Returns field from table based on specified criteria.
	 * 
	 * @param columnName      - Column name of field to return.
	 * @param conditionColumn - Column to check value for.
	 * @param conditionValue  - Value that conditionColumn should have.
	 * @return - Field matching criteria.
	 */
	public Field getField(String columnName, String conditionColumn, String conditionValue) {
		return getField(columnName, new String[] { conditionColumn }, new String[] { conditionValue });
	}

	/**
	 * Returns field value from table based on specified criteria.
	 * 
	 * @param columnName      - Column name of field to return.
	 * @param conditionColumn - Column to check value for.
	 * @param conditionValue  - Value that conditionColumn should have.
	 * @return - Field value matching criteria.
	 */
	public String getValue(String columnName, String[] conditionColumns, String[] conditionValues) {
		return getField(columnName, conditionColumns, conditionValues).getValue();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Record r : records) {
			sb.append(r.toString()).append("\n");
		}
		return sb.toString();
	}

	public void append(Table t) {
		for (int i = 0; i < t.getRecords().size(); i++) {
			if (records.size() > i) {
				records.get(i).append(t.getRecord(i));
			} else {
				addRecord(t.getRecord(i));
			}
		}
	}

	public void append(Record r) {
		if (records.size() > 0) {
			getRecord(0).append(r);
		} else {
			addRecord(r);
		}
	}

}
