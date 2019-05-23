package com.javaexcel.automation.core.swagger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.json.*;
import org.json.simple.JSONArray; 
import org.json.simple.JSONObject; 

import org.json.simple.*;
import org.json.simple.parser.*;


import java.util.TreeMap;
import java.util.HashMap;
import java.util.Iterator;

import com.google.inject.matcher.Matcher;
import com.javaexcel.automation.core.data.Config;
import com.javaexcel.automation.core.data.Configurables;
import com.javaexcel.automation.core.data.TestInstance;
import com.javaexcel.automation.core.enums.ParameterType;
import com.javaexcel.automation.core.enums.ParameterDataType;
import com.javaexcel.automation.core.table.Field;
import com.javaexcel.automation.core.table.Record;
import com.javaexcel.automation.core.table.Table;
import com.javaexcel.automation.core.utils.Excel;
import com.javaexcel.automation.core.utils.Utils;
//import net.bytebuddy.description.modifier.EnumerationState;
import com.javaexcel.automation.core.utils.Constants;
//1672
public class SwaggerData {
	//TreeMap data = new TreeMap<String, ArrayList<ParameterInfo>>(String.CASE_INSENSITIVE_ORDER);
	
	/*String        String  			ArrayList<ParameterInfo> 
	 * "status/"   "bankRountingNumber" "{ParameterInfo RequestBody  bankRountingNumber, IsRequired...}
	 *                                  "{ParameterInfo ResponseBody bankRountingNumber, IsRequired...}
	 *                      
	 *                      
	 *                       
	 */
	static TreeMap data = new TreeMap<String,TreeMap<String, ArrayList<ParameterInfo>>>(String.CASE_INSENSITIVE_ORDER);
	private static HashMap<String, Record> autoTestcaseMap = new HashMap<String, Record> ();
	
	private static HashMap<String, ErrorInfo> errorData = new HashMap<String, ErrorInfo>();
	static int autoTestCaseIdCounter=10000;
	private static boolean swaggerRead = false;
	//
	
	//
	private void addPath(String path)
	{
		if(!data.containsKey(path))
		{
			TreeMap<String, ArrayList<ParameterInfo>> dataMap = new TreeMap<String, ArrayList<ParameterInfo>>();
			data.put(path, dataMap);	
		}		
	}

	private ParameterInfo getParameterInfo(String path, String parameterName, ParameterType parameterType)
	{		
		addPath(path);
		
		TreeMap<String, ArrayList<ParameterInfo>> dataMap = (TreeMap<String, ArrayList<ParameterInfo>>)data.get(path);
		
		ParameterInfo parameterInfo=null;
		if(dataMap.containsKey(parameterName))
		{
			for(ParameterInfo paramInfo:dataMap.get(parameterName))
			{
				if(paramInfo.getParameterType() == parameterType)				
					parameterInfo = paramInfo;				
			}		
			if(parameterInfo == null)
			{	
				parameterInfo = new ParameterInfo(); 
				parameterInfo.setParameterName(parameterName);
				parameterInfo.setParameterType(parameterType);				
				((ArrayList)dataMap.get(parameterName)).add(parameterInfo);				
			}
		}
		else
		{
			ArrayList<ParameterInfo> pInfoList = new ArrayList<ParameterInfo>();
			dataMap.put(parameterName, pInfoList);
			parameterInfo = new ParameterInfo(); 
			parameterInfo.setParameterName(parameterName);
			parameterInfo.setParameterType(parameterType);
			pInfoList.add(parameterInfo);
		}
		return parameterInfo;	
	}
	
	private void processRef(String refPath, JSONObject jsonRoot, String path, String paramName, ParameterType parameterType)
	{
		
		refPath = refPath.replace("#", "");
		String[] arrRef = refPath.split("/");
		if(arrRef.length==3 && jsonRoot.get(arrRef[1]) != null)
		{
			JSONObject jsonReferal =(JSONObject)jsonRoot.get(arrRef[1]);
			jsonReferal = (JSONObject)jsonReferal.get(arrRef[2]);
			//if(paramName != "")paramName = paramName + "$";
			if(jsonReferal.get("name") != null)
				paramName = jsonReferal.get("name").toString().trim();//.toLowerCase();			
			if(paramName.equals("content-type"))return;
			if(jsonReferal.get("required") != null && jsonReferal.get("type").toString().equals("object"))
			{
				this.setRequired((JSONArray)jsonReferal.get("required"), path, paramName, parameterType);
			}
			if(jsonReferal.get("properties") != null)
			{
				this.setProperties(jsonRoot, (JSONObject)jsonReferal.get("properties"), path,paramName, parameterType);
			}
			if(jsonReferal.get("in") != null)
			{
				if(jsonReferal.get("in").toString().trim().matches("header"))
				{
					parameterType = ParameterType.RequestHeaderAttribute;
				}
			}
			
			if (excludedValidationsMap.containsKey(paramName) 
					&& excludedValidationsMap.get(paramName).equalsIgnoreCase("true")){
				//do nothing
			}
			else{
				processProperties(jsonRoot, jsonReferal,paramName, path, parameterType);
			}
			
		}
		
	}
	private void processProperties(JSONObject jsonRoot, JSONObject jsonProperties, String paramName, String path, ParameterType parameterType)
	{
		//System.out.println("Debug params*********** "+paramName+"->"+parameterType);
		//paramName = paramName.toLowerCase();
		if(paramName.equalsIgnoreCase("content-type"))return;
		
			ParameterInfo paramInfo = this.getParameterInfo(path, paramName, parameterType);
			JSONObject jsonParamDetails =jsonProperties;// (JSONObject)paramEntry.getValue();
			for(Map.Entry<Object, Object> parameterData: ((Map<Object, Object>)jsonParamDetails).entrySet())
			{
				String key = parameterData.getKey().toString();//.trim().toLowerCase();
				String value = parameterData.getValue().toString().trim();
				if(key.matches("type"))
				{
					boolean isEnum = false;
					if(jsonParamDetails.get("enum") != null)
						isEnum = true;	    
					
					if(datatypeOverrideMap.containsKey(paramInfo.getParameterName()))
					//String dataTypeOverride = Config.getProp("DataTypeOverride");
					//if(dataTypeOverride != null && dataTypeOverride.contains(paramInfo.getParameterName()+"#"))
					{								
						String dtType = datatypeOverrideMap.get(paramInfo.getParameterName());
						if(dtType != null && dtType.toLowerCase().equals( "string_number"))
						{							
							paramInfo.setParameterDataType(ParameterDataType.String_Number);
						}
						else if(dtType != null && dtType.equalsIgnoreCase("integer"))
						{							
							paramInfo.setParameterDataType(ParameterDataType.Integer);
						}
					}
					else if(value.matches("string"))
					{
						if(isEnum == true)
							paramInfo.setParameterDataType(ParameterDataType.StringEnum);
						else
							paramInfo.setParameterDataType(ParameterDataType.String);
					}
					else if(value.matches("integer"))
					{
						if(isEnum == true)
							paramInfo.setParameterDataType(ParameterDataType.IntegerEnum);
						else
							paramInfo.setParameterDataType(ParameterDataType.Integer);
					}
					else if(value.matches("number"))
					{
						paramInfo.setParameterDataType(ParameterDataType.Number);
					}
				}
				else if(key.equalsIgnoreCase("$ref"))
				{
					processRef(value, jsonRoot, path, paramName,  parameterType);
				}
				else if(key.equalsIgnoreCase("minlength"))
				{
					paramInfo.setMinLength(Integer.valueOf(value));
				}
				else if(key.equalsIgnoreCase("maxlength"))
				{
					paramInfo.setMaxLength(Integer.valueOf(value));
				}
				else if(key.equalsIgnoreCase("required"))
				{
					if(requiredOverrideMap.containsKey(paramInfo.getParameterName()))
					{
						String val = requiredOverrideMap.get(paramInfo.getParameterName());
						if(val.matches("true") || val.matches("false"))
							value = val;
					}					
					paramInfo.setIsRequired(Boolean.valueOf(value));
				}
			}		
	}
	
	private HashMap<String, String> getOverrideConfigData(String overrideString)
	{		
		HashMap<String, String> overrideMap = new HashMap<String, String>();
		
		if(overrideString != null)
		{
			if(!overrideString.equals(""))
			{
				String[] arrReq = overrideString.split("!");
				for(String col: arrReq)
				{
					if(col.split("#").length==2)
						overrideMap.put(col.split("#")[0], col.split("#")[1].toLowerCase());
				}
			}
		}
		
		return overrideMap;
	}
	private void setRequired(JSONArray requiredParameters, String path, String paramName, ParameterType parameterType)
	{		
		for(int j=0;j<requiredParameters.size();j++)
		{
			
			String requiredParam = requiredParameters.get(j).toString();
			if(paramName != "")
				requiredParam = paramName +"$" + requiredParam;
			
			if(requiredOverrideMap.containsKey(requiredParam))
			{
				String val = requiredOverrideMap.get(requiredParam);
				if(val.matches("true") ||  val.matches("false"))
				{
					ParameterInfo paramInfo = this.getParameterInfo(path, requiredParam, parameterType);
					paramInfo.setIsRequired(Boolean.valueOf(val));	
				}					
			}			
			else
			{
			ParameterInfo paramInfo = this.getParameterInfo(path, requiredParam, parameterType);
			paramInfo.setIsRequired(true);	
			}
		}
	}
		
	private void setProperties(JSONObject jsonRoot, JSONObject jsonProperties, String path, String paramName, ParameterType parameterType)
	{
		for(Map.Entry<Object, Object> paramEntry: ((Map<Object, Object>)jsonProperties).entrySet())
		{	            						
			String parameterName = ((String)paramEntry.getKey()).trim();
			if(paramName != "")
				parameterName = paramName + "$" + parameterName;
			processProperties(jsonRoot, (JSONObject)paramEntry.getValue(), parameterName, path, parameterType);
	    }
	}
	
	private JSONObject readFileAndGetJSONObject(String file)
	{		
		BufferedReader br = null;
		StringBuilder jsonString = new StringBuilder();
		JSONObject jsonObj = null;
		try
		{	
			if(file.contains(".yaml")){
				return Utils.convertYamlToJson("src/test/resources/" + file);
			}
			else{
				br = new BufferedReader(new FileReader(new File("src/test/resources/" + file))); 
				{
					String line="";
					while ((line = br.readLine()) != null) 
					{
						jsonString.append(line);
					}
				}			
				jsonObj  = (JSONObject)new JSONParser().parse(jsonString.toString());			
				}
			}
		catch(Exception exp)
		{
			//exp.printStackTrace();
			System.err.println(this.resourceNameConfig +" - File read error - missing or invalid: " + exp.getMessage()+"\nSkipping auto test case generation for "+this.resourceNameConfig);
			
		}
		finally
		{
			try
			{
				if(br != null)
					br.close();
			}
			catch(Exception ex){}
		}
		
		return jsonObj;
	}
	private static TreeMap<String, TreeMap<String, ArrayList<String>>> templateFieldCollection = new  TreeMap<String, TreeMap<String, ArrayList<String>>>(String.CASE_INSENSITIVE_ORDER);
	private void readTemplate(String templateName)
	{		
		if(templateFieldCollection.containsKey(templateName) == false)
		{
			JSONObject jsonRoot = readFileAndGetJSONObject("json-templates/"+templateName);
			if(jsonRoot != null)
				getTemplateFields(jsonRoot, templateName, "");
		}
	}
	
	private void getTemplateFields(JSONObject jsonObj, String templateName, String fieldPath)
	{
		for(Map.Entry<Object, Object> pathEntry: ((Map<Object, Object>)jsonObj).entrySet())			
		{	
			String fieldPath1="";			
			if(fieldPath != "")
				fieldPath1 = fieldPath + "$" + (String) pathEntry.getKey();
			else
				fieldPath1 = (String) pathEntry.getKey();
			
			if(pathEntry.getValue() instanceof JSONObject)
			{
				getTemplateFields((JSONObject)pathEntry.getValue(),templateName, fieldPath1);
			}
			else
			{
				TreeMap<String, ArrayList<String>> templateFields = templateFieldCollection.get(templateName);
				if(templateFields == null)
				{					
					templateFields = new TreeMap<String, ArrayList<String>>(String.CASE_INSENSITIVE_ORDER);
					templateFieldCollection.put(templateName, templateFields);					
				}
				templateFields.put(fieldPath1, new ArrayList<String>());
			}			
		}
	}
	private HashMap<String, String> requiredOverrideMap = new HashMap<String, String>();
	private HashMap<String, String> datatypeOverrideMap = new HashMap<String, String>();
	private HashMap<String, String> excludedValidationsMap = new HashMap<String, String>();
	private HashMap<String, String> pathHTTPMethodMap = new HashMap<String, String>();
	public void readSwagger()
	{		
		requiredOverrideMap = getOverrideConfigData(this.requiredOverrideConfig);
		datatypeOverrideMap = getOverrideConfigData(this.dataTypeOverrideConfig);
		excludedValidationsMap = getOverrideConfigData(this.excludedValidations);
		
		if(swaggerRead==true)return;		
		
		try
		{	
			String swaggerFile = this.swaggerFile;
			//JSONObject jsonRoot = readFileAndGetJSONObject("swagger/" + Config.getProp("SwaggerFile"));
			JSONObject jsonRoot = readFileAndGetJSONObject("swagger/" + swaggerFile);
			JSONObject pathsJSON = null;
			if(jsonRoot!=null){
				 pathsJSON = ((JSONObject)jsonRoot.get("paths"));
			
			
			for(Map.Entry<Object, Object> pathEntry: ((Map<Object, Object>)pathsJSON).entrySet())			
			{				
	            String path = (String) pathEntry.getKey();	            
	            this.addPath(path);
	            Map<Object, Object> pathDetails = (Map<Object, Object>)pathEntry.getValue();
	            for(Map.Entry<Object, Object> pathDetailEntry: pathDetails.entrySet())
	            {
	            	//this.addPath((String)pathDetailEntry.getKey());
	            	if(pathHTTPMethodMap.containsKey(path) == false)
	            		pathHTTPMethodMap.put(path, (String)pathDetailEntry.getKey());
	            	Map<Object, Object> pathMethodDetails = (Map<Object, Object>)pathDetailEntry.getValue();
	            	
	            	if(pathMethodDetails.get("responses") != null)
	            	{
	            		if(((JSONObject)pathMethodDetails.get("responses")).get("400") != null)
	            		{
	            			if ( (((JSONObject)((JSONObject)pathMethodDetails.get("responses")).get("400")).get("headers") != null))
	            			{
	            				JSONObject headersJSON = (JSONObject)((JSONObject)((JSONObject)pathMethodDetails.get("responses")).get("400")).get("headers");	            				
	            				for(Map.Entry<Object, Object> headerData: ((Map<Object, Object>)headersJSON).entrySet())
	            				{
	            					String headerName = headerData.getKey().toString();
	            					if (headerData.getValue() != null && headerData.getValue()  instanceof JSONObject)
	            					{
	            						if(((JSONObject)headerData.getValue()).get("type") != null)
	            						{
	            							String headerDataType = ((JSONObject)headerData.getValue()).get("type").toString();
	            							ParameterInfo headerParameter = this.getParameterInfo(path, headerName, ParameterType.ResponseHeaderAttribute);
	            							if(headerDataType.equalsIgnoreCase("string"))
	            							headerParameter.setParameterDataType(ParameterDataType.String);
	            							int[] statusCodes = {400};
	            							headerParameter.setStatusCodes(statusCodes);
	            									
	            						}
	            					}	            					
	            				}           				
	            			}
	            		}
	            	}
	            	//
	            	JSONArray parameterDetails = (JSONArray)pathMethodDetails.get("parameters");
	            	//System.out.println("Params Array********* "+parameterDetails);
	            	Object obj=new Object();
	            	ParameterType parameterType = ParameterType.RequestHeaderAttribute;
	            	for(int i=0;i<parameterDetails.size();i++)
	            	{
	            		obj = parameterDetails.get(i);
	            		if(obj != null)
	            		{
	            			Object objParameterType = ((JSONObject)obj).get("in");
	            			if(objParameterType != null)
	            			{
	            				if(objParameterType.toString().trim().equalsIgnoreCase("body"))
	            				{
	            					parameterType = ParameterType.RequestBodyAttribute;
	            				}
	            				else if(objParameterType.toString().trim().equalsIgnoreCase("query") ||
	            						objParameterType.toString().trim().equalsIgnoreCase("path"))
	            				{
	            					parameterType = ParameterType.RequestQueryAttribute;
	            					if(obj instanceof JSONObject)
	            					{
	            						ParameterInfo paramInfo = null;
	            						for(Map.Entry<Object, Object> getParamtersData: ((Map<Object, Object>)(JSONObject)obj).entrySet())
	            						{
	            							String queryKey = getParamtersData.getKey().toString();
	            							String queryVal = "";
	            							if(getParamtersData.getValue() != null)
	            								queryVal = getParamtersData.getValue().toString();
	            							
	            							if(queryKey.equalsIgnoreCase("name"))
	            							{
	            								paramInfo = this.getParameterInfo(path, queryVal, parameterType);
	            								this.processProperties(jsonRoot, (JSONObject)obj, queryVal, path, parameterType);	            								
	            							}	            							
	            						}
	            					}
	            				} else if(objParameterType.toString().trim().equalsIgnoreCase("header"))
	            				{
	            					//parameterType = ParameterType.RequestHeaderAttribute;
	            					if(obj instanceof JSONObject)
	            					{
	            						ParameterInfo paramInfo = null;
	            						for(Map.Entry<Object, Object> getParamtersData: ((Map<Object, Object>)(JSONObject)obj).entrySet())
	            						{
	            							String queryKey = getParamtersData.getKey().toString();
	            							String queryVal = "";
	            							if(getParamtersData.getValue() != null)
	            								queryVal = getParamtersData.getValue().toString();
	            							
	            							if (excludedValidationsMap.containsKey(queryVal) 
	            									&& excludedValidationsMap.get(queryVal).equalsIgnoreCase("true")){
	            								continue;
	            							}
	            							
	            							if(queryKey.equalsIgnoreCase("name"))
	            							{
	            								paramInfo = this.getParameterInfo(path, queryVal, parameterType);
	            								this.processProperties(jsonRoot, (JSONObject)obj, queryVal, path, parameterType);	            								
	            							}	            							
	            						}
	            					}
	            				}
	            			}
	            			
	            			if(((JSONObject)obj).get("$ref") != null)
	            			{
	            				String ref = ((JSONObject)obj).get("$ref").toString();	            				
	            				processRef(ref, jsonRoot, path, "", parameterType);
	            			}
	            			
	            			Object objSchema = ((JSONObject)obj).get("schema");
	            			if(objSchema != null)
	            			{	            				
	            				Object objRequired = ((JSONObject)objSchema).get("required");
	            				if(objRequired != null)	            				
	            					setRequired((JSONArray)objRequired, path, "", parameterType);	            					
	            					            				
	            				Object objProperties = ((JSONObject)objSchema).get("properties");
	            				if(objProperties != null)
	            				{	    	            					
	            					JSONObject jsonProperties = (JSONObject)objProperties;
	            					setProperties(jsonRoot, jsonProperties, path, "", parameterType);	            					
	            				}
	            			}
	            		}	            		
	            	}
	            }	           
			}	
			}
		}
		catch(Exception exp)
		{
			exp.printStackTrace();
			System.err.println(exp.getMessage());
		}
		finally
		{
			//swaggerRead = true;
			if(Config.getProp("multipleSwaggerSupport").equalsIgnoreCase("yes")){
					swaggerRead = false;
				}
			else{
					swaggerRead = true;
			}
			buildErrorData();
		}
	}

	private void buildErrorData()
	{
		errorData.put(Constants.PARAMETER_HEADER_REQUIRED, new ErrorInfo("HTTP/1.1 400 Bad Request", 
				"errors[%d]error_code#400-007",
				"errors[%d]description#'%s' is missing from the request header." )); 
		
		errorData.put(Constants.PARAMETER_STRING_REQUIRED, new ErrorInfo("HTTP/1.1 400 Bad Request", 
				"errors[%d]error_code#400-008",
				"errors[%d]description#'%s' is missing from the body of the request." )); 	

		errorData.put(Constants.PARAMETER_QUERY_STRING_REQUIRED, new ErrorInfo("HTTP/1.1 400 Bad Request", 
				"errors[%d]error_code#400-009",
				"errors[%d]description#'%s' is missing from the query string." )); 	
		
		
		errorData.put(Constants.PARAMETER_DATATYPE, new ErrorInfo("HTTP/1.1 400 Bad Request", 
				"errors[%d]error_code#400-010",
		"errors[%d]description#Incorrect data type. '%s' is not a valid %s."));

		errorData.put(Constants.PARAMETER_STRING_ENUM, new ErrorInfo("HTTP/1.1 400 Bad Request", 
				"errors[%d]error_code#400-012",
				"errors[%d]description#'%s' contains an invalid value." ));
		
		errorData.put(Constants.PARAMETER_INTEGER_MAXLENGTH, new ErrorInfo("HTTP/1.1 400 Bad Request", 
				"errors[%d]error_code#400-013", 
				"errors[%d]description#'%s' is not in the range of allowed values." ));
		
		errorData.put(Constants.PARAMETER_INTEGER_MINLENGTH, new ErrorInfo("HTTP/1.1 400 Bad Request", 
				"errors[%d]error_code#400-013", 
				"errors[%d]description#'%s' is not in the range of allowed values." ));
		
		errorData.put(Constants.PARAMETER_STRING_MAXLENGTH, new ErrorInfo("HTTP/1.1 400 Bad Request", 
				"errors[%d]error_code#400-014", 
				"errors[%d]description#'%s' cannot have more than %d character(s)." )); 
		
		errorData.put(Constants.PARAMETER_STRING_MINLENGTH, new ErrorInfo("HTTP/1.1 400 Bad Request", 
				"errors[%d]error_code#400-015", 
				"errors[%d]description#'%s' cannot have fewer than %d character(s)." ));
		
		errorData.put(Constants.PARAMETER_STRING_NUMBER_MINLENGTH, new ErrorInfo("HTTP/1.1 400 Bad Request", 
				"errors[%d]error_code#400-029",
				"errors[%d]description#'%s' contains a number outside the range of allowed values. The value submitted may be too large, too small, or may include too many digits after the decimal point." ));
		
		errorData.put(Constants.PARAMETER_STRING_NUMBER_MAXLENGTH, new ErrorInfo("HTTP/1.1 400 Bad Request", 
				"errors[%d]error_code#400-029",
				"errors[%d]description#'%s' contains a number outside the range of allowed values. The value submitted may be too large, too small, or may include too many digits after the decimal point." ));
	}

	private void addParameter(String path, String parameterName, ParameterType parameterType, ParameterDataType parameterDataType,  Boolean isRequired, int minLength, int maxLength, int[] statusCodes)
	{		
		TreeMap<String, ArrayList<ParameterInfo>> parameterMap;
		ArrayList<ParameterInfo> parameterInfoList;
		ParameterInfo pInfo = new ParameterInfo(parameterName, parameterType, parameterDataType, isRequired, minLength, maxLength, statusCodes);
		
		//addParameter()
		if(data.containsKey(path))
		{
			parameterMap = (TreeMap<String, ArrayList<ParameterInfo>>)data.get(path);
			parameterInfoList = parameterMap.get(parameterName);
			if(parameterInfoList==null)
				parameterInfoList=new ArrayList<ParameterInfo>();
			parameterMap.put(parameterName, parameterInfoList);
		}
		else
		{
			parameterInfoList = new ArrayList<ParameterInfo>();
			parameterMap = new TreeMap<String, ArrayList<ParameterInfo>> ();
			parameterMap.put(parameterName, parameterInfoList);
			data.put(path, parameterMap);
		}
		
		parameterInfoList.add(pInfo);
	}
	private String makeTemplateParameterUrl(String url, ArrayList<String> list)
	{
		String[] arr = url.split("/");
		for(int i=0;i<arr.length;i++)
		{
			if(arr[i].startsWith("{") && arr[i].endsWith("}"))
			{
				if(list != null)
					list.add(arr[i].replace("{","").replace("}", ""));
				arr[i] = "{*}";
			}
		}
		return String.join("/", arr);
	}
	public TreeMap<String, ArrayList<ParameterInfo>> getParameterMap(String resourcePath)
	{
		return getParameterMap(resourcePath, null);
	}
	public TreeMap<String, ArrayList<ParameterInfo>> getParameterMap(String resourcePath,  ArrayList<String> pathColumnsCorrected)
	{
		TreeMap<String, ArrayList<ParameterInfo>> paramMap = null;
		resourcePath = resourcePath.toLowerCase();
		if(resourcePath != null)
		{			
			for( Object entry : data.entrySet())
			{
				String path = ((Map.Entry<String,ArrayList<ParameterInfo>>)(entry)).getKey();
				
				if(this.requestTypeConfig.equalsIgnoreCase("GET"))
				{			
					ArrayList<String> urlParamList = new ArrayList<String>();
					String path1 = makeTemplateParameterUrl(path, urlParamList);
					if(pathColumnsCorrected!=null){
						pathColumnsCorrected.clear();
					}
					String resourcePath1 = makeTemplateParameterUrl(resourcePath, pathColumnsCorrected);
						

					//
					if(resourcePath1.endsWith(path1) && pathColumnsCorrected != null)
					{
						pathColumnsCorrected.clear();
						pathColumnsCorrected.addAll(urlParamList);
						return (TreeMap<String, ArrayList<ParameterInfo>>)data.get(path);
					}
					else
					{
						//
						//"path1"	/accounts/{*}/statements	
						//"resourcePath1"	/accountstatements-v1/accounts/01s6253jhjkurwrbufbs5trstpy7gyyqm5rh55whqzyjrblholxgvifjzpv74bpyx3l2uyeoagtd636/statements	
						String[]path1Arr = path1.split("/");
						ArrayList<String> path1List = new ArrayList<String>(Arrays.asList(path1Arr));
						//Pattern.compile("/accounts/(.*)/statements").matcher("/accounts/aads/aa/statements").matches()
						
						String[]resourcePath1Arr = resourcePath1.split("/");
						ArrayList<String> resourcePath1List = new ArrayList<String>(Arrays.asList(resourcePath1Arr));
						int nResourcePathListIndex = -100;
						for(int i=0; i<path1List.size(); i++)
						{
							String part = path1List.get(i);
							if(part.equals("")) continue;
							if(part.equalsIgnoreCase("{*}"))
							{	
								if(nResourcePathListIndex != -100)
									nResourcePathListIndex++;
							}
							else 
							{						
								int nIndex = resourcePath1List.indexOf(part);	
								if(nResourcePathListIndex == -100)nResourcePathListIndex = nIndex;
								if(nIndex != nResourcePathListIndex || nIndex == -1)break;	
								nResourcePathListIndex++;
	
							}
						}
						if(nResourcePathListIndex >=0)
							return (TreeMap<String, ArrayList<ParameterInfo>>)data.get(path);
					}
					
					//
				}
				if(resourcePath.endsWith(path))
				{
					
					return  (TreeMap<String, ArrayList<ParameterInfo>>)data.get(path);
				}
			}			
		}
		return paramMap; 			
	}
	
	public String[] getTestCaseIDSplit(Table tbl)
	{
		StringJoiner strTestCases = new StringJoiner("///");
		for(Record record: tbl.getRecords())						
			strTestCases.add(record.getField(Configurables.excelTestIDColumn).toString().toUpperCase());						
		return strTestCases.toString().split("///");
	}
	private static int referenceScenarioCounter=0;
	private String resourceNameConfig = "";
	private String testSuiteNameConfig = "";
	private String requestTemplateConfig = "";
	private String dataTypeOverrideConfig = "";
	private String excludedValidations = "";
	private String swaggerFile = "";
	private String requiredOverrideConfig = "";
	private String apiSpecificationUrlConfig = "";
	private String resourcePathConfig = "";
	private String requestTypeConfig = "";//POST/GET
	public void generateTestCases(String dataSource, String testSuiteID, Table tbl, String resourceName, String testSuiteName)
	{
		this.resourceNameConfig = resourceName;		
		
		//Table testConfigData = new Excel(Configurables.testsFilePath+Configurables.testFileName).querySheetRecords("SELECT * FROM " + Configurables.gateway_TestConfigSheetName + " WHERE " + Configurables.gateway_EnvironmentCoulmnName + " = '" + Configurables.gateway_Environment + "' and Resource = '" + resourceName + "'");
		//List<Record> testConfigList = testConfigData.getRecords();
		

		
		/*
		 * Load filtering criteria
		 */
		Map<String, String> conditionsMap = new HashMap<String, String>();
		conditionsMap.put("Resource", resourceName);
		conditionsMap.put("Environment", Configurables.gateway_Environment);
		List<Record> testConfigList = Configurables.globalConfigTable.getRecords(conditionsMap).getRecords();
		
		if(testConfigList.size()>0)
		{
			if(testConfigList.get(0).getField("RequestTemplate") != null)
				this.requestTemplateConfig = testConfigList.get(0).getField("RequestTemplate").toString();
			if(testConfigList.get(0).getField("DataTypeOverride") != null)
				this.dataTypeOverrideConfig = testConfigList.get(0).getField("DataTypeOverride").toString();
			if(testConfigList.get(0).getField("ExcludedCheck") != null)
				this.excludedValidations = testConfigList.get(0).getField("ExcludedCheck").toString();
			if(testConfigList.get(0).getField("SwaggerFile") != null)
				this.swaggerFile = testConfigList.get(0).getField("SwaggerFile").toString();
			if(testConfigList.get(0).getField("RequiredOverride") != null)
				this.requiredOverrideConfig = testConfigList.get(0).getField("RequiredOverride").toString();
			if(testConfigList.get(0).getField("ApiSpecificationUrl") != null)
				this.apiSpecificationUrlConfig = testConfigList.get(0).getField("ApiSpecificationUrl").toString();
			
			/*
			 * Selects appropriate resource path i.e. tomcat vs. wso2
			 */
			String baseURI = testConfigList.get(0).getField("BaseURI").toString();
			String resourcePath = "Wso2 Resource Path";
			if(baseURI.contains(Config.getProp("tomcatPortNo"))){
				resourcePath = "Tomcat Resource Path";
			}
			if(testConfigList.get(0).getField(resourcePath) != null)
				this.resourcePathConfig = testConfigList.get(0).getField(resourcePath).toString();
			
			if(testConfigList.get(0).getField("RequestType") != null)
				this.requestTypeConfig = testConfigList.get(0).getField("RequestType").toString();
			
		}
		
		if(testConfigList.get(0).getField("AutoGenerateTCs?")!=null && testConfigList.get(0).getField("AutoGenerateTCs?").toString().equalsIgnoreCase("Y")){
			readSwagger();
		}
		//System.out.println("requestTemplateConfig:**** "+requestTemplateConfig);
		
		List<Record> autoRecordList = new ArrayList<Record>();
		List<Record> records = tbl.getRecords();
		//System.out.println("Records****** "+records.size());
		for(Record record:records)
		{
			String scenarioName = record.getValue("Scenario_Name");
			if(scenarioName != null && (scenarioName.endsWith("{{SWAGGER VALIDATION}}")
					||scenarioName.endsWith("{{SWAGGER VALIDATION*}}")))
			{
				record.getField("Scenario_Name").setValue("(" + String.valueOf(++referenceScenarioCounter) + ") " + scenarioName.replace("{{SWAGGER VALIDATION}}", ""));
				referenceScenarioTCCounter = 0;
				boolean generateAllTestCases = false;
				if(scenarioName.endsWith("{{SWAGGER VALIDATION*}}")) generateAllTestCases = true;
				autoRecordList.addAll(autogenerateTestCases(tbl, record, generateAllTestCases));
			}			
		}

		//Write in CSV file
		/*
		StringBuilder sb = null;//new StringBuilder();
		for(Record autoRecord:autoRecordList)
		{
			if(sb==null)
			{
				sb = new StringBuilder();
				for(String column:autoRecord.getColumns())
				{
					sb.append("\""+column+"\"" + ",");
				}
				sb.append("\n");
			}			
			for(String column:autoRecord.getColumns())
			{
				sb.append("\"" + autoRecord.getValue(column) + "\"" + ",");
			}
			sb.append("\n");			
		}
		if(sb != null)
			writeUsingFileWriter(sb.toString());
		
		*/
		//
		if(autoRecordList != null && autoRecordList.size()>0)
		{
			for(Record autoRecord:autoRecordList)
			{
				tbl.addRecord(autoRecord);
				autoTestcaseMap.put(generateKey(dataSource, testSuiteID, autoRecord.getValue(Configurables.excelTestIDColumn)), 
						autoRecord);
			}
			
			/*
			 * Clear data from previous test suite to support multiple swagger files
			 */
			requiredOverrideMap.clear();
			datatypeOverrideMap.clear();
			excludedValidationsMap.clear();
		}		
	}
	
	 private static void writeUsingFileWriter(String data) {
	        File file = new File("src/test/resources/test-data/"+ DateTime.now().toString().replace(":",  ".")+".csv");
	        FileWriter fr = null;
	        try {
	            fr = new FileWriter(file);
	            fr.write(data);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }finally{
	            //close resources
	            try {
	                fr.close();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }
	private static String generateKey(String dataSource, String testSuiteID, String testCaseID)
	{
		return dataSource + "|" + testSuiteID + "|" + testCaseID;
	}
	public static Record getRecord(String dataSource, String testSuiteID, String testCaseID)
	{
		return autoTestcaseMap.get(generateKey(dataSource, testSuiteID, testCaseID));
	}
	
	
	private List<Record> prepareRequiredFieldBlankTestCase(ParameterInfo paramInfo, Record record)
	{
		
		List<Record> autoRecords = new ArrayList<Record>();
		if(paramInfo.getIsRequired()==true)
		{		
			Record autoRecord = new Record();
			String category="";
			if(paramInfo.getParameterType() == ParameterType.RequestBodyAttribute)
				category = Constants.PARAMETER_STRING_REQUIRED ;
			else if(paramInfo.getParameterType() == ParameterType.RequestHeaderAttribute)
			{
				category = Constants.PARAMETER_HEADER_REQUIRED;
				if(record.getField("EXCLUDE HEADER") == null)
					record.add("EXCLUDE HEADER","");
			}
			else if(paramInfo.getParameterType() == ParameterType.RequestQueryAttribute)
				category = Constants.PARAMETER_QUERY_STRING_REQUIRED;
			
			
				
			ErrorInfo errInfo = errorData.get(category);
			String parameterName = paramInfo.getParameterName().toUpperCase();
			String invalidValue = "";
			for(String column: record.getColumns())
			{				
				if(column.equalsIgnoreCase("TEST CASE ID"))//TODO-ATUL
					autoRecord.add(column, String.valueOf(autoTestCaseIdCounter++));
				else if(column.equalsIgnoreCase("SCENARIO_NAME"))		//TODO-ATUL				
					autoRecord.add(column, String.format("(" + String.valueOf(referenceScenarioCounter) + "." + String.valueOf(++referenceScenarioTCCounter) + " Auto) Send Invalid Data with %s field missing", paramInfo.getParameterName()));
				else if(column.equalsIgnoreCase("$"+parameterName))
				{
					autoRecord.add(column, "");					
				}
				else if(paramInfo.getParameterType()== ParameterType.RequestHeaderAttribute && column.endsWith("EXCLUDE HEADER"))
				{
					autoRecord.add(column, paramInfo.getParameterName());	//lower case header-name is required
				}

				else if(column.equalsIgnoreCase("EXPECTED RESULT"))
				{
					autoRecord.add(column,"Error Response");
				}
				//else if(column.startsWith("$")==false && column.endsWith("VALIDATESTATUSLINE"))
				else if(column.equalsIgnoreCase("VALIDATESTATUSLINE"))
				{
					autoRecord.add(column, errInfo.getStatusLine());
				}
				else if(column.equalsIgnoreCase("VALIDATEERRORCODE"))
				{
					autoRecord.add(column, String.format(errInfo.getErrorCode(), 0));
				}
				else if(column.equalsIgnoreCase("VALIDATEERRORDESCRIPTION"))
				{
					String[] paramNameHierarchyArr = paramInfo.getParameterName().split("\\$");
					int size = paramNameHierarchyArr.length;
					String fieldName = paramInfo.getParameterName();
					if(size>0)
						fieldName = paramNameHierarchyArr[size-1];
					autoRecord.add(column, String.format(errInfo.getErrorDescription(), 0, fieldName));//, paramInfo.getMinLength()));					
				}
				else if(column.matches("VALIDATION1"))
				{
					autoRecord.add(column, String.format(errInfo.getFieldNameValidation(),0, paramInfo.getParameterName().replace("$",".")));
				}				
				else if(column.matches("VALIDATION3"))
				{
					if(this.apiSpecificationUrlConfig != null && this.apiSpecificationUrlConfig.isEmpty()==false)
						autoRecord.add(column, String.format(errInfo.getAPISpecificationUrl(),0, this.apiSpecificationUrlConfig));					
				}
				else if(column.startsWith("$")==false && column.startsWith("VALIDAT"))
				{
					autoRecord.add(column, "");
				}				
				else
				{				
					autoRecord.add(column, record.getValue(column));
				}				
			}
			autoRecords.add(autoRecord);
		}
		return autoRecords;
	}
	
	//
	private List<Record> prepareEnumTestCase(ParameterInfo paramInfo, Record record, String overrideHeaderColName)
	{
		ParameterDataType parameterDataType = paramInfo.getParameterDataType();
		List<Record> autoRecords = new ArrayList<Record>();
		if(parameterDataType == ParameterDataType.StringEnum)
		{		
			Record autoRecord = new Record();		
			
			String category="";
			if(parameterDataType== ParameterDataType.StringEnum)
			{
				category = Constants.PARAMETER_STRING_ENUM ;
			}
			
			ErrorInfo errInfo = errorData.get(category);
			String parameterName = paramInfo.getParameterName().toUpperCase();
			String invalidValue = "";
			for(String column: record.getColumns())
			{				
				if(column.equalsIgnoreCase("TEST CASE ID"))
					autoRecord.add(column, String.valueOf(autoTestCaseIdCounter++));
				else if(column.contains("SCENARIO_NAME"))					
					autoRecord.add(column, String.format("(" + String.valueOf(referenceScenarioCounter) + "." + String.valueOf(++referenceScenarioTCCounter) + " Auto) Send Invalid enumeration value for %s", paramInfo.getParameterName()));   
				                                                                                                                                                         //Status-Send validation channel value with invalid enumeration data

				else if(column.equalsIgnoreCase("$"+parameterName))
				{
					String colValue = record.getValue(column);
					if(colValue== null)colValue = "";
					if(colValue != null)
					{
						invalidValue = "CC";
						/*
						if(colValue.startsWith("{{") && colValue.endsWith("}}")) //JavaXL framework keyword
							invalidValue = StringUtils.rightPad("1", maxLength+1, "12345");
						else
							invalidValue = StringUtils.rightPad(colValue, maxLength+1, colValue);
						*/
						autoRecord.add(column, invalidValue);
					}
				}
				else if(paramInfo.getParameterType()== ParameterType.RequestHeaderAttribute && column.equalsIgnoreCase(overrideHeaderColName))
				{
					//gateway-entity-id#216557531
					
					invalidValue = "HH";
					autoRecord.add(column, paramInfo.getParameterName()+"#" + invalidValue);
				}
				
				else if(column.equalsIgnoreCase("EXPECTED RESULT"))
				{
					autoRecord.add(column,"Error Response");
				}
				else if(column.startsWith("$")==false && column.equalsIgnoreCase("VALIDATESTATUSLINE"))
				{
					autoRecord.add(column, errInfo.getStatusLine());
				}
				else if(column.startsWith("$")==false && column.equalsIgnoreCase("VALIDATEERRORCODE"))
				{
					autoRecord.add(column, String.format(errInfo.getErrorCode(), 0));
				}
				else if(column.startsWith("$")==false && column.equalsIgnoreCase("VALIDATEERRORDESCRIPTION"))
				{			
					String[] paramNameHierarchyArr = paramInfo.getParameterName().split("\\$");
					int size = paramNameHierarchyArr.length;
					String fieldName = paramInfo.getParameterName();
					if(size>0)
						fieldName = paramNameHierarchyArr[size-1];
					autoRecord.add(column, String.format(errInfo.getErrorDescription(), 0, fieldName));//, paramInfo.getMinLength()));
					
				//	autoRecord.add(column, String.format(errInfo.getErrorDescription(), 0, paramInfo.getParameterName()));
				}
				else if(column.startsWith("$")==false && column.equalsIgnoreCase("VALIDATION1"))
				{
					autoRecord.add(column, String.format(errInfo.getFieldNameValidation(),0, paramInfo.getParameterName().replace("$",".")));
				}
				
				else if(column.startsWith("$")==false && column.endsWith("VALIDATION2"))
				{
					if(paramInfo.getParameterDataType()== ParameterDataType.String)
						autoRecord.add(column, String.format(errInfo.getFieldValueValidation(),0, invalidValue));
				}
				else if(column.startsWith("$")==false && column.endsWith("VALIDATION3"))
				{
					autoRecord.add(column, String.format(errInfo.getAPISpecificationUrl(),0, this.apiSpecificationUrlConfig));
				}
				else if(column.startsWith("$")==false && column.startsWith("VALIDAT"))
				{
					autoRecord.add(column, "");
				}
				
				else
				{
					//
					autoRecord.add(column, record.getValue(column));
				}				
			}
			
			/*
			if(autoRecord.getField("VALIDATEERRORDESCRIPTION") == null)
			{
				if(paramInfo.getParameterDataType()== ParameterDataType.String)
				{
					String[] paramNameHierarchyArr = paramInfo.getParameterName().split("\\$");
					int size = paramNameHierarchyArr.length;
					String fieldName = paramInfo.getParameterName();
					if(size>0)
						fieldName = paramNameHierarchyArr[size-1];
					autoRecord.add("VALIDATEERRORDESCRIPTION", String.format(errInfo.getErrorDescription(), 0, fieldName, paramInfo.getMaxLength()));
				}
				else
					autoRecord.add("VALIDATEERRORDESCRIPTION", String.format(errInfo.getErrorDescription(), 0, paramInfo.getParameterName()));
			}*/
			autoRecords.add(autoRecord);
		}
		return autoRecords;
	}
	
	private static int referenceScenarioTCCounter=0;	
	private List<Record> prepareMaxLengthTestCase(ParameterInfo paramInfo, Record record, String overrideHeaderColName)
	{
		int maxLength = paramInfo.getMaxLength();
		List<Record> autoRecords = new ArrayList<Record>();
		if(maxLength>-1)
		{		
			Record autoRecord = new Record();		
			
			String category="";
			if(paramInfo.getParameterDataType()== ParameterDataType.String)
			{
				category = Constants.PARAMETER_STRING_MAXLENGTH ;
			}
			else if(paramInfo.getParameterDataType() == ParameterDataType.Integer)
			{
				category = Constants.PARAMETER_INTEGER_MAXLENGTH;
			}
			else if(paramInfo.getParameterDataType() == ParameterDataType.String_Number)
			{
				category = Constants.PARAMETER_STRING_NUMBER_MAXLENGTH;
			}
			ErrorInfo errInfo = errorData.get(category);
			String parameterName = paramInfo.getParameterName().toUpperCase();
			String invalidValue = "";
			for(String column: record.getColumns())
			{				
				if(column.equalsIgnoreCase("TEST CASE ID"))//TODO-ATUL
					autoRecord.add(column, String.valueOf(autoTestCaseIdCounter++));
				else if(column.contains("SCENARIO_NAME"))		//TODO-ATUL				
					autoRecord.add(column, String.format("(" + String.valueOf(referenceScenarioCounter) + "." + String.valueOf(++referenceScenarioTCCounter) + " Auto) Send Invalid Data with %s more than the required length", paramInfo.getParameterName()));
				else if(column.equalsIgnoreCase("$"+parameterName))
				{
					String colValue = record.getValue(column);
					if(colValue== null)colValue = "";
					if(colValue != null)
					{
						if(colValue.startsWith("{{") && colValue.endsWith("}}")) //JavaXL framework keyword
							invalidValue = StringUtils.rightPad("1", maxLength+1, "12345");
						else
							invalidValue = StringUtils.rightPad(colValue, maxLength+1, colValue);												
						autoRecord.add(column, invalidValue);
					}
				}
				else if(paramInfo.getParameterType()== ParameterType.RequestHeaderAttribute && column.equalsIgnoreCase(overrideHeaderColName))
				{
					//gateway-entity-id#216557531
					
					invalidValue = StringUtils.rightPad("",maxLength + 5,"12345");
					autoRecord.add(column, paramInfo.getParameterName()+"#" + invalidValue);
				}
				
				else if(column.endsWith("EXPECTED RESULT"))
				{
					autoRecord.add(column,"Error Response");
				}
				else if(column.startsWith("$")==false && column.endsWith("VALIDATESTATUSLINE"))
				{
					autoRecord.add(column, errInfo.getStatusLine());
				}
				else if(column.startsWith("$")==false && column.endsWith("VALIDATEERRORCODE"))
				{
					autoRecord.add(column, String.format(errInfo.getErrorCode(), 0));
				}
				else if(column.startsWith("$")==false && column.endsWith("VALIDATEERRORDESCRIPTION"))
				{
					if(paramInfo.getParameterDataType()== ParameterDataType.String)
					{
						String[] paramNameHierarchyArr = paramInfo.getParameterName().split("\\$");
						int size = paramNameHierarchyArr.length;
						String fieldName = paramInfo.getParameterName();
						if(size>0)
							fieldName = paramNameHierarchyArr[size-1];
						autoRecord.add(column, String.format(errInfo.getErrorDescription(), 0, fieldName, paramInfo.getMaxLength()));
					}
					else
						autoRecord.add(column, String.format(errInfo.getErrorDescription(), 0, paramInfo.getParameterName()));
				}
				else if(column.startsWith("$")==false && column.endsWith("VALIDATION1"))
				{
					autoRecord.add(column, String.format(errInfo.getFieldNameValidation(),0, paramInfo.getParameterName().replace("$",".")));
				}
				
				else if(column.startsWith("$")==false && column.endsWith("VALIDATION2"))
				{
					if(paramInfo.getParameterDataType()== ParameterDataType.String)
						autoRecord.add(column, String.format(errInfo.getFieldValueValidation(),0, invalidValue));
				}
				else if(column.startsWith("$")==false && column.endsWith("VALIDATION3"))
				{
					autoRecord.add(column, String.format(errInfo.getAPISpecificationUrl(),0, this.apiSpecificationUrlConfig));
				}
				else if(column.startsWith("$")==false && column.startsWith("VALIDAT"))
				{
					autoRecord.add(column, "");
				}
				
				else
				{
					//
					autoRecord.add(column, record.getValue(column));
				}				
			}
			
			/*
			if(autoRecord.getField("VALIDATEERRORDESCRIPTION") == null)
			{
				if(paramInfo.getParameterDataType()== ParameterDataType.String)
				{
					String[] paramNameHierarchyArr = paramInfo.getParameterName().split("\\$");
					int size = paramNameHierarchyArr.length;
					String fieldName = paramInfo.getParameterName();
					if(size>0)
						fieldName = paramNameHierarchyArr[size-1];
					autoRecord.add("VALIDATEERRORDESCRIPTION", String.format(errInfo.getErrorDescription(), 0, fieldName, paramInfo.getMaxLength()));
				}
				else
					autoRecord.add("VALIDATEERRORDESCRIPTION", String.format(errInfo.getErrorDescription(), 0, paramInfo.getParameterName()));
			}*/
			autoRecords.add(autoRecord);
		}
		return autoRecords;
	}
	
	private List<Record> prepareMinLengthTestCase(ParameterInfo paramInfo, Record record, String overrideHeaderColName)
	{
		int minLength = paramInfo.getMinLength();
		List<Record> autoRecords = new ArrayList<Record>();
		if(minLength>-1)
		{		
			Record autoRecord = new Record();
			//ErrorInfo errInfo = errorData.get(paramInfo.getParameterType()== ParameterType.RequestBodyAttribute ? Constants.PARAMETER_MINLENGTH : Constants.PARAMETER_HEADER_MINLENGTH);
			String category="";
			if(paramInfo.getParameterDataType()== ParameterDataType.String)
			{
				category = Constants.PARAMETER_STRING_MINLENGTH ;
			}
			else if(paramInfo.getParameterDataType() == ParameterDataType.Integer)
			{
				category = Constants.PARAMETER_INTEGER_MINLENGTH;
			}
			else if(paramInfo.getParameterDataType() == ParameterDataType.String_Number)
			{
				category = Constants.PARAMETER_STRING_NUMBER_MINLENGTH;
			}
			ErrorInfo errInfo = errorData.get(category);
			
			String parameterName = paramInfo.getParameterName().toUpperCase();
			String invalidValue = "";
			for(String column: record.getColumns())
			{				
				if(column.equalsIgnoreCase("TEST CASE ID"))//TODO-ATUL
					autoRecord.add(column, String.valueOf(autoTestCaseIdCounter++));
				else if(column.equalsIgnoreCase("SCENARIO_NAME"))		//TODO-ATUL				
					autoRecord.add(column, String.format("(" + String.valueOf(referenceScenarioCounter) + "." + String.valueOf(++referenceScenarioTCCounter) + " Auto) Send Invalid Data with %s less than the required length", paramInfo.getParameterName()));
				else if(column.equalsIgnoreCase("$"+parameterName))
				{
					String colValue = record.getValue(column);
					if(colValue != null && colValue.length()>=minLength)
					{
						invalidValue = colValue.substring(0, minLength-1);		
						if(requestTypeConfig.equalsIgnoreCase("post"))
							autoRecord.add(column, (minLength==1)?"{{EMPTY}}":invalidValue);
						else
							autoRecord.add(column, (minLength==1)?"":invalidValue);
					}
				}
				else if(paramInfo.getParameterType()== ParameterType.RequestHeaderAttribute && column.equalsIgnoreCase(overrideHeaderColName))
				{					
					invalidValue = StringUtils.rightPad("",minLength-1,"123456");
					autoRecord.add(column, paramInfo.getParameterName()+"#" + ((invalidValue=="")?"{{EMPTY}}":invalidValue));
				}			
				else if(column.equalsIgnoreCase("EXPECTED RESULT"))
				{
					autoRecord.add(column,"Error Response");
				}
				else if(column.startsWith("$")==false && column.equalsIgnoreCase("VALIDATESTATUSLINE"))
				{
					autoRecord.add(column, errInfo.getStatusLine());
				}
				else if(column.startsWith("$")==false && column.equalsIgnoreCase("VALIDATEERRORCODE"))
				{
					autoRecord.add(column, String.format(errInfo.getErrorCode(), 0));
				}
				else if(column.startsWith("$")==false && column.equalsIgnoreCase("VALIDATEERRORDESCRIPTION"))
				{
					if(paramInfo.getParameterDataType()== ParameterDataType.String)
					{
						String[] paramNameHierarchyArr = paramInfo.getParameterName().split("\\$");
						int size = paramNameHierarchyArr.length;
						String fieldName = paramInfo.getParameterName();
						if(size>0)
							fieldName = paramNameHierarchyArr[size-1];
						autoRecord.add(column, String.format(errInfo.getErrorDescription(), 0, fieldName, paramInfo.getMinLength()));						
					}
					else
						autoRecord.add(column, String.format(errInfo.getErrorDescription(), 0, paramInfo.getParameterName() ));
				}
				else if(column.startsWith("$")==false && column.equalsIgnoreCase("VALIDATION1"))
				{
					autoRecord.add(column, String.format(errInfo.getFieldNameValidation(),0, paramInfo.getParameterName().replace("$", ".")));
				}
				
				else if(column.startsWith("$")==false && column.equalsIgnoreCase("VALIDATION2"))
				{
					if(paramInfo.getParameterDataType()== ParameterDataType.String)
					{
						if(minLength==1)
							autoRecord.add(column, String.format(errInfo.getFieldValueValidationForEmpty(),0));//, invalidValue));
						else
						autoRecord.add(column, String.format(errInfo.getFieldValueValidation(),0, invalidValue));
					}
				}
				else if(column.startsWith("$")==false && column.equalsIgnoreCase("VALIDATION3"))
				{
					autoRecord.add(column, String.format(errInfo.getAPISpecificationUrl(),0, this.apiSpecificationUrlConfig));
				}
				else if(column.startsWith("$")==false && column.startsWith("VALIDAT"))
				{
					autoRecord.add(column, "");
				}
				
				else
				{
					//
					autoRecord.add(column, record.getValue(column));
				}				
			}
			
			autoRecords.add(autoRecord);
		}
		return autoRecords;
	}
	
	ArrayList<String> fieldsTCGenerated = new ArrayList<String>();
	HashMap<String, ArrayList<String>> fieldResourcePathsTCsGenerated = new HashMap<String, ArrayList<String>>(); 
	private boolean checkTemplateFields(ParameterInfo paramInfo, String resourcePath, String templateName, boolean generateAllTestCases)
	{
		if(paramInfo.getParameterType() == ParameterType.RequestHeaderAttribute || paramInfo.getParameterType() == ParameterType.RequestQueryAttribute)
		{
			if(fieldResourcePathsTCsGenerated.containsKey(paramInfo.getParameterName()))
			{				
				ArrayList<String> pathList = fieldResourcePathsTCsGenerated.get(paramInfo.getParameterName());
				if(pathList != null && pathList.contains(resourcePath))
				{
					if(generateAllTestCases == true)
						return true;
					return false;
				}
				else if(pathList != null)
				{
					pathList.add(resourcePath);					
					return true;
				}
			}
			else
			{
				ArrayList<String> pathList = new ArrayList<String>();
				pathList.add(resourcePath);
				fieldResourcePathsTCsGenerated.put(paramInfo.getParameterName(), pathList);	
				return true;
			}
		}
		else if(paramInfo.getParameterType() == ParameterType.RequestBodyAttribute)// || paramInfo.getParameterType() == ParameterType.RequestHeaderAttribute)
		{
			if (templateFieldCollection.get(templateName) != null)
			{
				TreeMap<String, ArrayList<String>> templateFields = templateFieldCollection.get(templateName);
				if(templateFields.get(paramInfo.getParameterName()) != null)
				{
				//	if(fieldsTCGenerated.contains(paramInfo.getParameterName()) && generateAllTestCases == false)
				//		return false;
				//	
					if(fieldResourcePathsTCsGenerated.containsKey(paramInfo.getParameterName()))
					{
						
						ArrayList<String> pathList = fieldResourcePathsTCsGenerated.get(paramInfo.getParameterName());
						if(pathList != null && pathList.contains(resourcePath))
						{
							return generateAllTestCases;
						}
						else if(pathList != null)
						{							
							pathList.add(resourcePath);					
							return true;
						}
					}
					else
					{
						ArrayList<String> pathList = new ArrayList<String>();
						pathList.add(resourcePath);
						fieldResourcePathsTCsGenerated.put(paramInfo.getParameterName(), pathList);	
						return true;
					}
				}					
			}
		}
		return false;
	}	

	private List<Record> autogenerateTestCases(Table tbl, Record record, boolean generateAllTestCases)
	{		
		String resourcePath = record.getValue("Override Resource Path") + record.getValue("Add to Path")!=null?record.getValue("Add to Path"):"";
		if(resourcePath == null || resourcePath.equals(""))		
			resourcePath = this.resourcePathConfig;			
		
		String templateName = record.getValue("Override Template");
		if(templateName == null || templateName.equals(""))		
			templateName = this.requestTemplateConfig;
				
		TreeMap<String, ArrayList<ParameterInfo>> parameterMap = null;
		String masterGETPathUrl = "", masterQueryString = "";
		
		
		ArrayList<String> pathColumnsAdded = new ArrayList<String>();		
		ArrayList<String> pathColValuesAdded = new ArrayList<String>();
		
		ArrayList<String> queryStringParamAdded = new ArrayList<String>();
		ArrayList<String> queryStringValAdded = new ArrayList<String>();
		
		Record rec = new Record();
		String uriPart = "", getColName="";
		
		if(this.requestTypeConfig.equalsIgnoreCase("GET"))
		{	
			String[] urlParts = resourcePath.split("/");
			ArrayList<String> queryStringParams = new ArrayList<String>(); 
			if(urlParts != null)
			{
				for(int i=0;i<urlParts.length;i++)
				{
					String urlQueryStringPart = "";					
					if(urlParts[i].startsWith("{") && urlParts[i].endsWith("}"))
					{
						String colName = urlParts[i].replace("{","").replace("}", "");
						String colValue = record.getValue("#" + colName);
						if(colValue != null && colValue.equals("")==false)
						{								
							String[] splitByQuest = colValue.split("\\?");
							
							if(splitByQuest.length>1 && i==urlParts.length-1)									
							{
								urlParts[i]=splitByQuest[0];
								urlQueryStringPart = "?" + splitByQuest[1];
								uriPart = urlParts[i];	
								getColName="#"+colName;
							}
							else if(splitByQuest.length==1)							
								pathColumnsAdded.add(urlParts[i].replace("{","").replace("}", ""));														
						}								
					}
					if(i==urlParts.length-1 && record.getValue("Query Params") != null && record.getValue("Query Params") != "")
					{
						urlQueryStringPart += record.getValue("Query Params");
						getColName = "Query Params";
						
					}
					if(i==urlParts.length-1 && (urlParts[i].contains("?") || urlQueryStringPart.equals("") == false))
					{
						String url = (urlQueryStringPart != null && urlQueryStringPart != "")? urlQueryStringPart:urlParts[i];
						
						 String[] params = url.split("\\?")[1].split("\\&");
						 for(int j=0;j<params.length;j++)
						 {
							 String param = params[j];
							 if(param.split("=").length==2)
							 {
								 queryStringParamAdded.add(param.split("=")[0]);
								 queryStringValAdded.add(param.split("=")[1]);
								 
								 rec.add("$"+param.split("=")[0], param.split("=")[1]);
								 params[j]= "[" + param.split("=")[0] + "]=[" + param.split("=")[1] +"]";
							}
						 }
						 masterQueryString = String.join("&", params);						
					}
				} 
			}
			masterGETPathUrl = String.join("/", urlParts);
			if(masterGETPathUrl.contains("?"))
				masterGETPathUrl = masterGETPathUrl.split("\\?")[0];			
		}
		
		ArrayList<String> pathColumnsCorrected = new ArrayList<String>();
		parameterMap = this.getParameterMap(masterGETPathUrl, pathColumnsCorrected);
		if( parameterMap!= null)
		{
			for(int i=0;i<pathColumnsCorrected.size();i++)
			{
				String pathColName = pathColumnsAdded.get(i);
				String colValue = record.getValue("#" + pathColName);
				rec.add("$" + pathColumnsCorrected.get(i), colValue);
				pathColValuesAdded.add(colValue);				
			}			
		}		
		
		
		if(!this.requestTypeConfig.equalsIgnoreCase("GET")){
			this.readTemplate(templateName);	
		}
			
		ArrayList<String> columnsAdded = new ArrayList<String>();		
		
		for(String column: record.getColumns())			
			if(column.startsWith("#")==false) rec.add(column, record.getValue(column));
				
		record = rec;
		List<Record> autoRecordList = new ArrayList<Record>();
		referenceScenarioTCCounter =0;
		
		if(parameterMap == null)
			parameterMap = getParameterMap(resourcePath);
		
		TreeMap<String, ArrayList<ParameterInfo>> RRHeaderParamMap = new TreeMap<String, ArrayList<ParameterInfo>>();
		if(parameterMap != null)
		{
			
			for(Map.Entry<String, ArrayList<ParameterInfo>> paramEntry: parameterMap.entrySet())
			{
				String parameterName = paramEntry.getKey();
				ArrayList<ParameterInfo> paramInfoList = paramEntry.getValue();
			
				for(ParameterInfo paramInfo : paramInfoList)
				{						
					if(paramInfo.getParameterType()== ParameterType.RequestHeaderAttribute
							|| paramInfo.getParameterType() == ParameterType.ResponseHeaderAttribute)
					{
						ArrayList<ParameterInfo> RRParamList = null;
						if( RRHeaderParamMap.containsKey(paramInfo.getParameterName()))						
							RRParamList = RRHeaderParamMap.get(paramInfo.getParameterName());						
						else
						{
							RRParamList = new ArrayList<ParameterInfo>();
							RRHeaderParamMap.put(paramInfo.getParameterName(), RRParamList);
						}
						RRParamList.add(paramInfo);
					}
						
					if( 
							(paramInfo.getParameterType()== ParameterType.RequestHeaderAttribute
							&& (paramInfo.getParameterName() != "content-type")) 
							||
							(paramInfo.getParameterType()== ParameterType.RequestBodyAttribute || paramInfo.getParameterType()== ParameterType.RequestQueryAttribute)
							&& 
							(rec.getValue("$" + paramInfo.getParameterName()) != "" &&
							rec.getValue("$" + paramInfo.getParameterName()) != null)
						)
					{						
						if(checkTemplateFields(paramInfo, resourcePath,templateName,generateAllTestCases )==false)continue;
						
						//If Excel file already have multiple columns of Override Header and if current Header (paramInfo) is already exists as Override Header, then skip it from auto-generating-test cases
						String overrideHeaderColName = "OVERRIDE HEADER";
						if(paramInfo.getParameterType() == ParameterType.RequestHeaderAttribute)
						{							
							boolean continueOuterLoop = false;							
							for(int i=0;i<10;i++) //assumption: any excel file will not have more than 10 "OVERRIDE HEADER" Columns. If any file contains more than 10, just increase the FOR loop counter. Here attempt is to avoid end-less loop, instead definite (10) max-counter value allocated
							{
								if(i>0) overrideHeaderColName = "OVERRIDE HEADER_" + Integer.toString(i);
								String str = rec.getValue(overrideHeaderColName);
								
								if(str=="")
								{
									String headerVal = overrideHeaderColName;
									for(int j=i+1;j<10;j++)
									{
										overrideHeaderColName = "OVERRIDE HEADER_" + Integer.toString(j);	
										
										if(rec.getValue(overrideHeaderColName) != null)
										{
											if(rec.getValue(overrideHeaderColName).toLowerCase().contains(paramInfo.getParameterName().toLowerCase() + "#"))
											{
												continueOuterLoop = true;
												break;										
											}
										}	
										else
										{
											overrideHeaderColName = headerVal;
											break;
										}
									}
									overrideHeaderColName = headerVal;
									break;
									
								}
								if(str == null)
								{							
									rec.add(overrideHeaderColName, "");
									break;
								}
								
								if(str != null)
								{
									if(str.toLowerCase().contains(paramInfo.getParameterName().toLowerCase() + "#"))
									{
										continueOuterLoop = true;
										break;										
									}
								}	
							}
							if(continueOuterLoop)continue;
							
						}
						if(this.requestTypeConfig.equalsIgnoreCase("POST") ||
								(pathColumnsCorrected.contains(paramInfo.getParameterName().replace("#","")) == false && paramInfo.getMinLength()>1))
						{
							List<Record> autoMinLengthTestCases = prepareMinLengthTestCase(paramInfo, rec, overrideHeaderColName);
							if(autoMinLengthTestCases != null && autoMinLengthTestCases.size()>0)
								autoRecordList.addAll(autoMinLengthTestCases);
						}
						
						List<Record> autoMaxLengthTestCases = prepareMaxLengthTestCase(paramInfo, rec, overrideHeaderColName);
						if(autoMaxLengthTestCases != null && autoMaxLengthTestCases.size()>0)
							autoRecordList.addAll(autoMaxLengthTestCases);
						
						if(pathColumnsCorrected.contains(paramInfo.getParameterName().replace("#","")) == false)
						{
							List<Record> autoRequiredFieldBlankTestCases = prepareRequiredFieldBlankTestCase(paramInfo, rec);
							if(autoRequiredFieldBlankTestCases != null && autoRequiredFieldBlankTestCases.size()>0)
								autoRecordList.addAll(autoRequiredFieldBlankTestCases);	
						}
						List<Record> autoDataTypeTestCases = prepareDataTypeTestCase(paramInfo, rec);
						if(autoDataTypeTestCases != null && autoDataTypeTestCases.size()>0)
							autoRecordList.addAll(autoDataTypeTestCases);
												
						List<Record> autoEnumTestCases = prepareEnumTestCase(paramInfo, rec, overrideHeaderColName);
						if(autoEnumTestCases != null && autoEnumTestCases.size()>0)
							autoRecordList.addAll(autoEnumTestCases);					
					}	
					
				}				
			}
			
			ArrayList<Record> revisedRecordList = new ArrayList<Record>();
			for(Record autoRecord: autoRecordList)
			{
				String queryString="";
				columnsAdded = queryStringParamAdded;
				for(String columnAdded: columnsAdded)
				{
					if(queryString=="")
						queryString = columnAdded.replace("$", "") +"="+autoRecord.getValue("$" + columnAdded);
					else
						queryString += "&" + columnAdded.replace("$", "") +"="+autoRecord.getValue("$"+columnAdded);					
				}
				if(uriPart != null && uriPart.isEmpty()==false)
					queryString = uriPart + "?" + queryString;			
				
				Record processedGetRecord = new Record();
				if(getColName != null && getColName.isEmpty()==false)
				{
					if(getColName.equalsIgnoreCase("Query Params") && queryString.contains("?")==false)
						queryString = "?" + queryString;
					processedGetRecord.add(getColName, queryString );
				}				
				
				for(int i=0;i<pathColumnsCorrected.size();i++)
				{
					Field fld = autoRecord.getField("$"+pathColumnsCorrected.get(i));					
					if(fld != null && pathColumnsAdded.size()>=i )				
						processedGetRecord.add("#"+pathColumnsAdded.get(i), fld.getValue());				
				}				
				
				for(String column: autoRecord.getColumns())
				{
					int nIndex = pathColumnsCorrected.indexOf(column.replace("$",""));
					if(nIndex >=0 && pathColumnsAdded.size()>=nIndex && pathColumnsCorrected.get(nIndex) != pathColumnsAdded.get(nIndex)) continue;
					
					if(columnsAdded.contains(column.replace("$","")) == false && processedGetRecord.getColumns().contains(column) == false)
						processedGetRecord.add(column, autoRecord.getValue(column));
				}
				revisedRecordList.add(processedGetRecord);
				
			}
			if(revisedRecordList.size()>0)
				autoRecordList = revisedRecordList;
			for(Record autoRecord: autoRecordList)
			{		
				int nHeaderCount=1;
				for(Map.Entry<String, ArrayList<ParameterInfo>> paramEntry: RRHeaderParamMap.entrySet())
				{
					String parameterName = paramEntry.getKey();
					ArrayList<ParameterInfo> paramInfoList = paramEntry.getValue();
					
					for(ParameterInfo paramInfo : paramInfoList)
					{	
						if(paramInfo.getParameterType() == ParameterType.ResponseHeaderAttribute)
						{
							int[] statusCodes = paramInfo.getStatusCodes();
							if(statusCodes != null && statusCodes.length>0)
							{
								IntStream i = IntStream.of(statusCodes);
								boolean match400 = i.anyMatch(n->n==400);								
								
								if(autoRecord.getValue("EXCLUDE HEADER") == null || autoRecord.getValue("EXCLUDE HEADER").equalsIgnoreCase(paramInfo.getParameterName()) == false)
								{
									autoRecord.add("VALIDATEHEADER" + String.valueOf(nHeaderCount),paramInfo.getParameterName() + "$Exist" );
									nHeaderCount++;
								}
							}								
						}
					}					
				}
			}
		}
		return autoRecordList;
	}
	
	private List<Record> prepareDataTypeTestCase(ParameterInfo paramInfo, Record record)
	{
		ParameterDataType paramDataType = paramInfo.getParameterDataType();
		List<Record> autoRecords = new ArrayList<Record>();
		
		if(paramInfo.getParameterType() == ParameterType.RequestBodyAttribute && 
				(paramDataType==ParameterDataType.String || paramDataType==ParameterDataType.String_Number
				||paramDataType == ParameterDataType.Integer))
		{		
			Record autoRecord = new Record();		
			
			String category=Constants.PARAMETER_DATATYPE;
			ErrorInfo errInfo = errorData.get(category);
			
			String parameterName = paramInfo.getParameterName().toUpperCase();
			
			for(String column: record.getColumns())
			{				
				if(column.equalsIgnoreCase("TEST CASE ID"))//TODO-ATUL
					autoRecord.add(column, String.valueOf(autoTestCaseIdCounter++));
				else if(column.equalsIgnoreCase("SCENARIO_NAME"))						
					autoRecord.add(column, String.format("(" + String.valueOf(referenceScenarioCounter) + "." + String.valueOf(++referenceScenarioTCCounter) + " Auto) Send %s value with integer data type", paramInfo.getParameterName()));
				else if(column.equals("$"+parameterName))
				{
					String colValue = "1";					
					int minLength = paramInfo.getMinLength();					
					colValue = "##"+ StringUtils.rightPad("1", minLength, "10");					
					if(paramDataType == ParameterDataType.Integer)
					{
						colValue = record.getValue(column);						
						if(colValue == "")colValue="##\"1\"";
						else colValue = "##\"" + colValue +"\"";
							
					}
					autoRecord.add(column, colValue);
				}				

				else if(column.equals("EXPECTED RESULT"))				
					autoRecord.add(column,"Error Response");
				
				else if(column.equals("VALIDATESTATUSLINE"))				
					autoRecord.add(column, errInfo.getStatusLine());
				
				else if(column.equals("VALIDATEERRORCODE"))				
					autoRecord.add(column, String.format(errInfo.getErrorCode(), 0));
				
				else if(column.equals("VALIDATEERRORDESCRIPTION"))
				{
					String[] paramNameHierarchyArr = paramInfo.getParameterName().split("\\$");
					int size = paramNameHierarchyArr.length;
					String fieldName = paramInfo.getParameterName();
					if(size>0)
						fieldName = paramNameHierarchyArr[size-1];
					autoRecord.add(column, String.format(errInfo.getErrorDescription(), 0, fieldName, (paramInfo.getParameterDataType()== ParameterDataType.Integer)?"integer":"string"));					
				}
				else if(column.equals("VALIDATION1"))				
					autoRecord.add(column, String.format(errInfo.getFieldNameValidation(),0, paramInfo.getParameterName().replace("$",".")));
				
				else if(column.startsWith("$")==false && column.equals("VALIDATION3"))				
					autoRecord.add(column, String.format(errInfo.getAPISpecificationUrl(),0, this.apiSpecificationUrlConfig));
				
				else if(column.startsWith("$")==false && column.startsWith("VALIDAT"))				
					autoRecord.add(column, "");				
				
				else				
					autoRecord.add(column, record.getValue(column));
								
			}
			autoRecords.add(autoRecord);
		}
		return autoRecords;
	}
}
