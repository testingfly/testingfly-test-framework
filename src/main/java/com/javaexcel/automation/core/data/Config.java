package com.javaexcel.automation.core.data;


import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.javaexcel.automation.core.utils.PasswordGenerator;
import com.javaexcel.automation.core.utils.Utils;


public class Config {

	public static boolean debug = true;
	public static String connectString;

	private static Map<String, String> properties = new HashMap<>();

	public static void addProps(Map<String, String> inputMap){
		properties.putAll(inputMap);		
	}
	
	public static void addPropsIfAbsent(Map<String, String> inputMap){
		for (String key : inputMap.keySet()){
			properties.putIfAbsent(key, inputMap.get(key));
		}
	}

	/**
	 * Gets configuration value from testdataconfig or injected parameters.
	 * 
	 * @param key - Parameter name
	 * @return value of defined parameter
	 */
	public static String getProp(String key){
		String keyStr = key.toLowerCase();
		String prop = System.getProperty(key);
		
		if(key.equals("Branch") && prop!=null){
			return prop;
		}
		
		if(key.equals("Branch") && prop==null){
			
				return "N/A";
			
		}
		
		if(key.equals("jobName") && prop!=null){
			return prop;
		}
		
		if(key.equals("jenkinsURL") && prop!=null){
			return prop;
		}
		
		if(key.equals("build") && prop!=null){
			return prop;
		}

		if (!properties.containsKey(keyStr)) {
			return null;
		}		
		
		if(key.equals("Environment") && prop!=null){
			return prop;
		}
		
		if(key.equals("API_Name") && prop!=null){
			if (prop.equalsIgnoreCase("Default") || prop.equalsIgnoreCase("") || prop.equalsIgnoreCase("$API_Name") ){
				return properties.get(keyStr);
			}
			else{
				return prop;
			}
		}
		
		if(key.equals("enableAutoTCs") && prop!=null){
			return prop;
		}
		
		
		if(key.equals("tagName") && prop!=null){
			if(Config.getProp("Environment").contains("PROD")){
				return "Wso2PROD";
			}
			return prop;
		}else{
			if(key.equals("tagName") && Config.getProp("Environment").contains("PROD")){
				//return "Wso2PROD";
				
			}
		}
		
		if(key.equals("uploadToALM") && prop!=null){
			return prop;
		}
		
		if(key.equals("TestFile") && prop!=null && prop.length()>4 ){
			return prop;
		}
		
		return properties.get(keyStr);
		
		
	}
	
	/**
	 * Gets configuration value from testdataconfig or injected parameters.
	 * 
	 * @param key - Parameter name
	 * @param valueIfNull - Value returned if parameter key hasn't been defined
	 * @return value of defined parameter (or valueIfNull if parameter key hasn't been defined)
	 */
	public static String getProp(String key, String valueIfNull){
		key = key.toLowerCase();
		if (!properties.containsKey(key)) {
			properties.put(key, valueIfNull);
			return valueIfNull;
		}
		return properties.get(key);
	}
	
	/**
	 * Gets configuration value from testdataconfig or injected parameters, then converts it into a boolean value.
	 * 
	 * @param key - Parameter name
	 * @return value of defined parameter, converted to boolean
	 */
	public static boolean getBoolProp(String key){
		return Utils.isAffirmative(getProp(key));
	}
	
	/**
	 * Gets configuration value from testdataconfig or injected parameters, then converts it into a boolean value.
	 * 
	 * @param key - Parameter name
	 * @param valueIfNull - Value returned if parameter key hasn't been defined
	 * @return value of defined parameter (or valueIfNull if parameter key hasn't been defined), converted to boolean
	 */
	public static boolean getBoolProp(String key, String valueIfNull){
		return Utils.isAffirmative(getProp(key, valueIfNull));
	}

	private static String getPassword() {
		byte[] decodedKey = Base64.getDecoder().decode(Config.getProp("QTPencrptionKey"));
		SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		try {
			return PasswordGenerator.decrypt(Config.getProp("Password"), originalKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
