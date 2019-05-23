package com.javaexcel.automation.core.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.javaexcel.automation.core.utils.Utils;

public class TextConfigurer implements IConfigurer{
	
	private String filePath;
	
	public TextConfigurer(String filePath){
		this.filePath = filePath;
	}

	@Override
	public Map<String, String> getConfigMap() {
		Map<String, String> map = new HashMap<String, String>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			Properties prop = new Properties();
			prop.load(reader);
			reader.close();
			
			for(String name: prop.stringPropertyNames()){
				name = name.trim();
				String value = map.get(name.toLowerCase());
				
				if (value == null){
					map.put(name.toLowerCase(), prop.getProperty(name).trim());
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Utils.print("Cannot find " + filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return map;
	}

}
