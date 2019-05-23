package com.javaexcel.automation.core.data;

import java.util.HashMap;
import java.util.Map;

import com.javaexcel.automation.core.utils.Excel;

public class ExcelConfig {
	
	Excel configFile;
	
	private String downloadFileFromALM() {
		
		//TODO: download file from alm and return the new local file location
		
		return null;
	}

	public Map<String, String> getConfigMap() {
		if (configFile == null) return null;
		
		Map<String, String> configMap = new HashMap<>();
		
		//TODO: query excel object to populate map
		
		return configMap;
	}
	
	public void close(){
		configFile.close();
	}

}
