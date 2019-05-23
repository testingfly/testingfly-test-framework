package com.javaexcel.automation.core.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

//import Exception.FilloException;
//import Fillo.Connection;
//import Fillo.Fillo;
//import Fillo.Recordset;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Recordset;
import com.javaexcel.automation.core.table.Record;
import com.javaexcel.automation.core.table.Table;
import com.codoid.products.fillo.Fillo;

public class Excel {

	public enum ExcelType{
		XSSF,
		HSSF
	}

	private Fillo fillo;
	private Connection connection;
	private String filePath;

	public Excel(String filePath){
		fillo = new Fillo();
		try {
			connection = fillo.getConnection(filePath);
		} catch (FilloException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.err.println("Error reading test data sheet... "+e.getMessage()+"\nAbortng test run.");
			System.exit(1);
			
		}
		this.filePath = filePath;
	}

	public String getFilePath(){
		return filePath;
	}

	public void close(){
		connection.close();
	}

	public void excelError(String query){
		Utils.printDebug("Error fetching data from excel.");
		Utils.printDebug("Query: (" + query + ")");
	}

	public Connection getConnection(String fileName){
		try {
			return fillo.getConnection(fileName);
		} catch (FilloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates a workbook object from the Excel filename.
	 * 
	 * @param fileName - The location and name of the Excel file.
	 * @return A workbook object.
	 */
	public Workbook openWorkbook(String fileName) {
		try{
			FileInputStream fileInputStream = new FileInputStream(fileName);

			Workbook workbook;
			ExcelType type;
			type = determineExcelType(fileName);
			if (type == ExcelType.HSSF){
				workbook = new HSSFWorkbook(fileInputStream);
			}else{
				workbook = new XSSFWorkbook(fileInputStream);
			}

			return workbook;
		}catch (IOException e){
			Utils.print("Error opening workbook.");
			return null;
		}
	}

	/**
	 * Determines the Excel type based on its file extension.
	 * 
	 * @param fileName - The location and name of the Excel file.
	 * @return The ExcelType enum corresponding to the file name.
	 */
	public ExcelType determineExcelType(String fileName){
		if (fileName.endsWith(".xlsx")){
			return ExcelType.XSSF;
		}else{
			return ExcelType.HSSF;
		}
	}

	/**
	 * Queries an Excel sheet.  Returns multiple values.
	 * 
	 * @param query - The query to be executed.
	 * @return An array list of strings containing each record in the column in the specified field.
	 */
	public ArrayList<String> querySheetColumn(String query){
		ArrayList<String> results = new ArrayList<String>();
		try{
			//			Connection connection = fillo.getConnection(fileName);
			Recordset recordset = connection.executeQuery(query);

			while (recordset.next()){
				results.add(recordset.getField(query.split(" ")[1]));
			}

			recordset.close();
			//			connection.close();
		}catch (FilloException e){
			e.printStackTrace();
			excelError(query);
		}

		return results;
	}

	public List<String> getSheetColumnNames(String sheetName){
		ArrayList<String> results = null;
		String query = "Select * From " + sheetName;
		try{
			//			Connection connection = fillo.getConnection(fileName);
			Recordset recordset = connection.executeQuery(query);

			if (recordset.next()){
				results = recordset.getFieldNames();
			}

			recordset.close();
			//			connection.close();
		}catch (FilloException e){
			e.printStackTrace();
			excelError(query);
		}

		return results;
	}

	/**
	 * Queries an Excel sheet.  Returns a single value.
	 * 
	 * @param fileName - The location and name of the Excel file.
	 * @param query - The query to be executed.
	 * @return The result of the query.
	 */
	public String querySheet(String query){
		String result = "";
		try{
			//			Connection connection = fillo.getConnection(fileName);
			Recordset recordset = connection.executeQuery(query);

			if (recordset.next()) {
				result = recordset.getField(query.split(" ")[1]);
			}

			recordset.close();
			//			connection.close();
		}catch (FilloException e){
			excelError(query);
		}

		return result;
	}

	public String[] querySheetRow(String query){
		String[] result = null;
		try{
			//			Connection connection = fillo.getConnection(fileName);
			Recordset recordset = connection.executeQuery(query);
			int columnCount = recordset.getFieldNames().size();

			if (recordset.next()){
				result = new String[columnCount];
				for (int i = 0; i < columnCount; i ++){
					result[i] = recordset.getField(i).value();
				}
			}

			recordset.close();
			//			connection.close();
		}catch (FilloException e){
			excelError(query);
		}

		return result;
	}

	public Record querySheetRecord(String query){
		Record result = null;
		try{
			Recordset recordset = connection.executeQuery(query);
			if (recordset.next()){
				result = new Record(recordset);
			}

			recordset.close();
		}catch (FilloException e){
			excelError(query);
		}

		return result;
	}

	public Table querySheetRecords(String query){
		Table table = new Table();
		Record result = null;
		try{
			Recordset recordset = connection.executeQuery(query);
			while (recordset.next()){
				result = new Record(recordset);
				table.addRecord(result);
			}

			recordset.close();
			
			table.setColumns(result);
		}catch (FilloException e){
			excelError(query);
		}

		return table;
	}
	/**
	 * Returns a Recordset by executing the query in Excel
	 * @param query
	 * @return
	 */
		public Recordset executeQuerySheetRecords(String query){
			Recordset recordset= null;
			try{
				 recordset = connection.executeQuery(query);
				 //recordset.close();
				}
			catch (FilloException e){
				excelError(query);
			}
	return recordset;
		}
	public List<String[]> querySheetRows(String query){
		List<String[]> result = new ArrayList<String[]>();
		try{
			//			Connection connection = fillo.getConnection(fileName);
			Recordset recordset = connection.executeQuery(query);
			int columnCount = recordset.getFieldNames().size();

			while (recordset.next()){
				String[] row = new String[columnCount];
				for (int i = 0; i < columnCount; i ++){
					row[i] = recordset.getField(i).value();
				}
				result.add(row);
			}

			recordset.close();
			//			connection.close();
		}catch (FilloException e){
			excelError(query);
		}

		return result;
	}

	/**
	 * Does nothing yet.
	 * 
	 * @param filename
	 * @param sheetName
	 * @param data
	 */
	public void writeToSheet(String sheetName, String[][] data){
		//		StringBuilder sb = new StringBuilder();
		//		for (String[] subdata: data){
		//			sb.append(StringUtils.join(subdata, ",") + "\n");
		//		}
		//		String csv = sb.toString();

		//		Workbook workbook = openWorkbook("C:\\Users\\Public\\Test.xls");
		//		Sheet sheet = workbook.createSheet();
		//
		//		for (int i = 0; i < data[0].length; i ++){
		//			for (int j = 0; j < data.length; j++){
		//				sheet.
		//			}
		//		}
	}
}
