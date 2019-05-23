package com.javaexcel.automation.core.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GlobalHashMap {
	 // Note that I've typed to Map instead of LinkedHashMap, and that it is now static
    private static Map<String, Object> globalHashMap;

    /*static {
    	//globalHashMap = new LinkedHashMap<>(); // Diamond operator requires Java 1.7+
    	getGlobalhashmap().put("QUOTEID", "1234564");
    	
    }*/
    
    protected GlobalHashMap(){
    	
    }

	public static Map<String, Object> getInstance() {
		if(globalHashMap == null)
		{
			globalHashMap = new HashMap<String, Object>();
		}
		return globalHashMap;
	}
	
	public static void convertResponseJsonToObject(String responseJsonDataPath, Map<String, String> params) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<Map<String, Object>> typeRef 
        = new TypeReference<Map<String, Object>>() {};
        Map<String, Object> jsonObj = mapper.readValue(responseJsonDataPath, typeRef);		
        globalHashMap.put(params.get("TestID")+"_"+params.get("test configuration id"), jsonObj);
	}
	
	public static String getValue(String compositeKey) {
		String keys[]=compositeKey.split("\\$");
		Map<String, Object> globalMap = globalHashMap;
		Map<String, Object> tempMap = globalMap;
		int i=1;
		for(String key: keys) {			
			if(i == keys.length) {
				return (String)tempMap.get(key);
			}
			tempMap = (Map<String, Object>)tempMap.get(key);
			i++;
		}
		return null;
	}

}