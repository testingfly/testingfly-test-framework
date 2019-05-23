package com.javaexcel.automation.core.data;

import java.util.HashMap;
import java.util.Map;

//import Exception.FilloException;
//import Fillo.Recordset;
import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Recordset;
import com.javaexcel.automation.core.utils.Excel;

public class ExcelConfigurer implements IConfigurer {
	
	private String filePath;
	
	public ExcelConfigurer(String filePath){
		this.filePath = filePath;
	}
/***
 * Iterating the Excel  sheet key Value from different sheet and put
 * into a map object. 
 * If ConfigSourceType value equal to Excel then this value will be taking 
 * precedence over default Config.txt file's key value.
 */
	@Override
	public Map<String, String> getConfigMap() {
		// TODO Auto-generated method stub
		Map<String, String> configMap = new HashMap<>();
		try {
			if (filePath != null) {
				Excel configFile = new Excel(filePath);

				// TODO: query excel object to populate map
				String testConfigQuery = "";
				String sheet = "TestConfig";
				testConfigQuery = String.format("SELECT * FROM  %s where ProjectCode ='%s'  ", sheet,
						Config.getProp("ProjectName"));
				Recordset recordset = configFile.executeQuerySheetRecords(testConfigQuery);

				if (recordset != null) {
					String key = "";
					String value = "";
					while (recordset.next()) {
						key = recordset.getField(2).value();
						value = recordset.getField(3).value();
						configMap.put(key.toLowerCase(), value);
					}
				}

				String commonSheet = "Common_Config";
				String commonConfigSheetQuery = String.format("SELECT * FROM %s ", commonSheet);

				Recordset recordset2 = configFile.executeQuerySheetRecords(commonConfigSheetQuery);

				if (recordset2 != null) {
					String sheet2Key = "";
					String sheet2value = "";
					while (recordset2.next()) {
						sheet2Key = recordset2.getField(1).value();
						sheet2value = recordset2.getField(2).value();
						configMap.put(sheet2Key.toLowerCase(), sheet2value);
					}
				}
				//Added as part of multi project integration to get the individual project Name
				
				String projectNames = "ProjectNames";
				String projNameQuery = String.format("SELECT * FROM %s ", projectNames);
				Recordset recordset3 = configFile.executeQuerySheetRecords(projNameQuery);

				if (recordset3 != null) {
					String projectNameKey = "";
					String projectNameValue = "";
					while (recordset3.next()) {
						projectNameKey = recordset3.getField(1).value();
						projectNameValue = recordset3.getField(2).value();
						configMap.put(projectNameKey.toLowerCase(), projectNameValue);
					}
				}
				configFile.close();
			}
			
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return configMap;
	
	}
	

}
