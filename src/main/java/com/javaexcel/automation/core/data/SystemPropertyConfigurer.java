package com.javaexcel.automation.core.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.javaexcel.automation.core.utils.Utils;

public class SystemPropertyConfigurer implements IConfigurer{

	@Override
	public Map<String, String> getConfigMap() {
		Map<String, String> map = new HashMap<>();
		
		Properties systemProp = System.getProperties();
		for (String name: systemProp.stringPropertyNames()){
			map.put(name.toLowerCase(), systemProp.getProperty(name));
		}
		
		//additional parameters can be passed in the "config" tag, delimited by either commas or semicolons
		String additionalConfig = System.getProperty("AdditionalConfig");
		if (additionalConfig != null && !additionalConfig.trim().isEmpty()){
			String delimiter = additionalConfig.contains(";") ? ";" : ",";
			String[] configSplit = Utils.splitTrim(additionalConfig, delimiter);
			
			for (String item : configSplit){
				String[] pair = Utils.splitTrim(item, ":|=");
				map.put(pair[0].toLowerCase(), pair[1]);
			}
		}
		
		return map;
	}

}
