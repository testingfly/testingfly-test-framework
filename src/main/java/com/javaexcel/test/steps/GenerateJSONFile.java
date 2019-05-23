package com.javaexcel.test.steps;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONObject;
import org.testng.annotations.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.javaexcel.automation.core.utils.GlobalHashMap;
import com.javaexcel.automation.core.utils.RestClient;
import com.javaexcel.automation.core.utils.Utils;
import com.javaexcel.tests.base.APITestBase;
//import com.wellsfargo.wfgAPI.tests.base.WFGAPITestBase;
//import Exception.FilloException;
import com.codoid.products.exception.FilloException;
/**
 * 
 * @author u381126
 * Generates the test payload if applicable in the JSON format.
 * Processes chained request data
 * Does NOT process header paths
 *
 */
public class GenerateJSONFile extends APITestBase 
{

	
	@SuppressWarnings("deprecation")
	@Test(dataProvider = "methodparameters")
	public void createJSONFile(Map<String, String> params) throws FilloException, IOException
	{
		
		Boolean templateDefaulted = false;
		if(params.get("use template defaults")!=null && (params.get("use template defaults").trim().equals("Y"))){
			templateDefaulted = true;
		}

		
		if(!templateDefaulted){
			/*
			 * Process chain request data
			 */
			getChainedRequestDataHeader(params);
			getChainedRequestDataBody(params);
			
		
		}
		
		
		File jsonFile;
		if(params.get("override template")==null||(params.get("override template").equals(""))){
			jsonFile = new File("src/test/resources/json-templates/"+params.get("RequestTemplate")); 
			
		}
		else{
			jsonFile = new File("src/test/resources/json-templates/"+params.get("override template")); 
		}
		
		if(!Utils.checkCaseSensitive(jsonFile)){
			System.err.println("WARNING: Template file name mismatch found! This is not cross platform compatible.");
		}
		
		if(!jsonFile.exists()){
			System.err.println("\nERROR loading json payload template. Please verify that the file exist or the file name matches (case sensitive).");
		}
		
		String xmlString = null;		
		JsonNode jsonNode = null;		
		int intRetry=0;
		
		if (jsonFile.isFile() && !templateDefaulted ){
				while(intRetry<1)
				{
					try {
						
						jsonNode = new ObjectMapper().readTree(jsonFile);
						
						break;
					} catch (IOException e) {
						System.err.println("Error reading template file... "+ e.getMessage());
						e.printStackTrace();
						if(e.getMessage().contains("UTF-8"))
						{
							xmlString = FileUtils.readFileToString(jsonFile).replace("\u00A0","");
							FileUtils.writeStringToFile(jsonFile, xmlString.trim());
						}
						intRetry++;
					}
				}
				
				setJsonFieldValues("", jsonNode, params);
		}
		

		String requestType = params.get("requesttype");
		
		if(requestType.equalsIgnoreCase("POST") || requestType.equalsIgnoreCase("PATCH") || requestType.equalsIgnoreCase("PUT")){
			String payload;
			if (jsonFile!=null && !jsonFile.exists() || templateDefaulted){
				payload = "";
			}
			else{
				payload=jsonNode.toString();
			}
			String fileName = params.get("test suite id")+"_"+params.get("test case id");
			File newHtmlFileInProjectFolderOfSummary = new File("reports/json/request/"+ fileName +".json");
			if(templateDefaulted){
				FileUtils.writeStringToFile(newHtmlFileInProjectFolderOfSummary, RestClient.prettyPrint(FileUtils.readFileToString(jsonFile,"UTF-8"), false), Charset.defaultCharset(), false);
			}
			else{
				FileUtils.writeStringToFile(newHtmlFileInProjectFolderOfSummary,RestClient.prettyPrint(payload, false),Charset.defaultCharset(), false);
			}
		}
		
	}
	
	private void setJsonFieldValues(String currentPath, JsonNode jsonNode, Map<String, String> params) {
		  
		if(params != null && !params.isEmpty())
		  {
			  if (jsonNode.isObject()) {
			      ObjectNode objectNode = (ObjectNode) jsonNode;
			      Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
			      String pathPrefix = currentPath.isEmpty() ? "" : currentPath;
			      
			      while (iter.hasNext()) {
			        Map.Entry<String, JsonNode> entry = iter.next();
			        String key = null;
			        if(entry.getValue().isValueNode())
			        {
			        	key = pathPrefix+entry.getKey();
			        	if(params.containsKey("$"+key))
			        	{
			        		try{
			        			setJsonValue(params, objectNode, entry, key, iter);
			        		}
			        		catch(Exception e){
			        			System.err.println("Please check inputs for Field/Value: " + entry.getKey()+"/"+params.get("$"+entry.getKey())
			        			+"\nDefaulting to template defined value.");
			        			e.printStackTrace();
			        		}
			        	}
			        	else
			        	{
			        		String[] parents = pathPrefix.split("$");
				        	for(int i = parents.length-1; (i >= 0 && parents.length > 0); i--)
				        	{
				        		key = "$"+parents[i]+key;
				        		if(params.containsKey(key))
					        	{
					        		try{
					        			setJsonValue(params, objectNode, entry, key, iter);
					        		}
					        		catch(Exception e){
					        			System.out.println("Field/Value: " + entry.getKey()+"/"+params.get("$"+entry.getKey()));
					        		}
				        			break;
					        	}
				        	}
			        	}
			        }
			        else
			        {
			        	setJsonFieldValues(pathPrefix + entry.getKey()+"$",entry.getValue(), params);
			        }
			      }
			    } else if (jsonNode.isArray() && !jsonNode.toString().contains(":")){
			    				
			    				String fieldName="";
			    				try{
				    				fieldName = "$"+currentPath.substring(0, currentPath.length()-1);
				    				String arrayStr[] = params.get(fieldName).split(",");
				    				ArrayNode arrayNode = (ArrayNode) jsonNode;	
				    				arrayNode.removeAll();
				    				for (int i = 0; i < arrayStr.length; i++) {
				    					arrayNode.add(arrayStr[i].trim());
				    					setJsonFieldValues(currentPath, arrayNode.get(0), params);
				    				}
			    				}
			    				catch(Exception e){
			    					System.err.println("***WARNING: Invalid entry for "+fieldName+". Using default value from template.***");
			    				}
			    				
						 }
			    	 		   else if (jsonNode.isArray()) {
			    	 			   		ArrayNode arrayNode = (ArrayNode) jsonNode;
						      
									      for (int i = 0; i < arrayNode.size(); i++) {
									    	  setJsonFieldValues(currentPath + "[" + i + "]", arrayNode.get(i), params);	
									      }
						    }			  
		   
		  }
	}

	

	private void setJsonValue(Map<String, String> params,
			ObjectNode objectNode, Map.Entry<String, JsonNode> entry, String key, Iterator<Map.Entry<String, JsonNode>> iter) {
		Object entryValue = entry.getValue();
		String jsonObject = params.get("$"+key);
		jsonObject = Utils.processKeywords(jsonObject);
		
		if(jsonObject != null )
    	{
    		if (jsonObject.equals("") )
    		{
    			iter.remove();
    		} else if (jsonObject.contains("DEFAULT"))
	    		{
	    			jsonObject = "";
	    		}
				
	    		else if(jsonObject.contains("$") && jsonObject.contains("_") && !jsonObject.contains("RANUSERID")) {
	    			
	    			objectNode.put(entry.getKey(),String.valueOf(GlobalHashMap.getValue(jsonObject)));
	    		}
	    		else if(!jsonObject.equals(""))
	    		{
	    			try {

    				if(jsonObject.contains("EMPTY")){
    					objectNode.put(entry.getKey(),"");
    				}
    				
    				else if(jsonObject.startsWith("##") && !jsonObject.contains("\"") && jsonObject.contains(".")){
    					objectNode.put(entry.getKey(),Double.valueOf(jsonObject.replace("##", "")));
    				}
    				
    				else if(jsonObject.startsWith("##") && !jsonObject.contains("\"") && !jsonObject.contains(".") 
    						&& jsonObject.contains("true")  || jsonObject.contains("false")){
    					objectNode.put(entry.getKey(),Boolean.valueOf(jsonObject.replace("##", "")));
    					
    					
    				}
    				
    				else if(jsonObject.startsWith("##") && jsonObject.contains("null") ){
    					objectNode.put(entry.getKey(), "$NULL");
    					
    				}
    				
    				else if(jsonObject.startsWith("##") && !jsonObject.contains("\"") && !jsonObject.contains(".")){
    					objectNode.put(entry.getKey(),Integer.valueOf(jsonObject.replace("##", "")));
    					
    					
    				}
    				
    				else if(jsonObject.startsWith("##") && jsonObject.contains("\"") && !jsonObject.contains(".")){
    					objectNode.put(entry.getKey(),String.valueOf(jsonObject.replace("##", "").replace("\"", "")));
    					
    				}
    				

    				
    				else if(jsonObject.contains("{{$guid}}")||jsonObject.contains("{{guid}}")){
    					jsonObject = UUID.randomUUID().toString();//.replace("{{", "").replace("}}", "").replace("$", "");
    					objectNode.put(entry.getKey(),String.valueOf(jsonObject));
    					 
    				}

    				else if(jsonObject.contains("RANUSERID")){
    					jsonObject = Utils.getRandomUserID(jsonObject);
    					objectNode.put(entry.getKey(),String.valueOf(jsonObject));
    				}
    				
    				else if(jsonObject.contains("RANUSERID")){
    					jsonObject = "gateway-"+RandomStringUtils.random(8, "123abc678");
    					objectNode.put(entry.getKey(),String.valueOf(jsonObject));
    				}
    				
    				else if(jsonObject.contains("RANEMAIL")){
    					jsonObject = Utils.getRandomEmail();
    					objectNode.put(entry.getKey(),String.valueOf(jsonObject));
    				}
    				
    				
    				else if(jsonObject.contains("RANADDRESS")){
    					jsonObject = RandomStringUtils.random(4,"1234")+" "+RandomStringUtils.random(1, "M")+RandomStringUtils.random(10, "malcomn")+ " St " +RandomStringUtils.random(2,"NY");
    					objectNode.put(entry.getKey(),String.valueOf(jsonObject));
    				}
    				

    				else if(entryValue instanceof DoubleNode || entryValue instanceof FloatNode)
					{
						objectNode.put(entry.getKey(),Double.valueOf(jsonObject));
					}
					else if(entryValue instanceof FloatNode)
					{
						objectNode.put(entry.getKey(),Float.valueOf(jsonObject));
					}
					else if(entryValue instanceof LongNode)
					{
						objectNode.put(entry.getKey(),Long.valueOf(jsonObject));
					}
					else if(entryValue instanceof IntNode)
					{
						objectNode.put(entry.getKey(),Integer.valueOf(jsonObject));
					}
					else if(entryValue instanceof BooleanNode)
					{
						objectNode.put(entry.getKey(),Boolean.valueOf(jsonObject));
					}
					else
					{
						objectNode.put(entry.getKey(),jsonObject.toString().replace("\\\"", "\"").trim());	
					}
    				
    				 

				} catch (NumberFormatException e) {
					e.printStackTrace();
					throw new NumberFormatException(jsonObject + " is not a number. Please check the value provided for the '"+key+"' Column."); 
				}
    		}
    	}
		
	}
	

	
	/*
	 * Get chained request data from response header based on input in test data sheet
	 */
	public void getChainedRequestDataHeader(Map<String, String> params) throws IOException{
	
		String fileName;
		Set<String> KeySet= params.keySet();
		
		for (String key:KeySet){
			if (params.get(key).contains("#header") && !key.contains("#")){
				String temp=params.get(key).split("#")[0]+"#";
				String field = params.get(key).replaceAll(".*#", "#").split(":")[1].trim();
				fileName = params.get(key).replaceAll(".*#", "#").split(":")[0].replace("[", "").replace("]","").replace("#header_", "") + "_header.json".trim(); 
				String val = readFile(fileName, field);//get data from file
				params.put(key, temp+val);
			}
		}
		
	 }
	
	
	/*
	 * Get chained request data from response body based on input in test data sheet
	 */
	public void getChainedRequestDataBody(Map<String, String> params) throws IOException{

		String fileName;

		Set<String> KeySet= params.keySet();
		Utils.flgWhereValFound = false;
		
		for (String key:KeySet){
			
			if (params.get(key).contains("#body") && !key.contains("#") && !key.contains("query params")){
				String temp=params.get(key).split("#")[0]+"#";
				String field = params.get(key).replaceAll(".*#", "#").split(":")[1].trim();
				fileName = params.get(key).replaceAll(".*#", "#").split(":")[0].replace("#body_", "") + ".json".trim(); 
				try{
				String val = readResponseBody(fileName, field).replace("\"", "");//get data from file
				params.put(key, temp.length()>1?temp+val:val);
				}
				catch(Exception e){
					//e.printStackTrace();
					System.err.println("Error parsing response data... "+e.getLocalizedMessage());
				}
				
				
			}
		}
		
	 }

	
	
	
	/*
	 * Get data from response file
	 */
	@SuppressWarnings("deprecation")
	public String readFile(String filename, String key){
		File jsonFile = new File("reports/json/response/"+filename);
		String xmlString[] = null;
		try {
			xmlString = FileUtils.readFileToString(jsonFile).split(",");
		} catch (IOException e) {			
			System.err.println("Error reading input data. \n"+e.getMessage());
		}
		String keyData="";
		for(String data:xmlString){
			if(data.split(":")[0].trim().equals(key)){
				keyData = data.split(":")[1].trim().replace("]", "").replace("[", "");
			}
		}
		
		return keyData;
	}
	
	
	
	/*
	 * Get data from response body
	 */
	@SuppressWarnings("deprecation")
	public String readResponseBody(String filename, String expkey) throws IOException{
		File jsonFile = new File("reports/json/response/"+filename);
		JsonNode jsonNode = null;
		boolean fileBodyExist=false;
		if (jsonFile.exists()){
			try{
			jsonNode = new ObjectMapper().readTree(jsonFile);
			}
			catch(Exception e){
				System.err.println("ERROR: Parsing exception encountered. Failed to look up value.");
				return "error";
			}
			fileBodyExist = true;
		}
		Utils.flgWhereValFound = false;
		String keyData = fileBodyExist?Utils.readRespBody("",jsonNode,expkey,"","", 0):"File not Found!";
		if(keyData==null){
			System.err.println("WARNING: Lookup returned null value. Please verify the data parameter passed.");
		}
		
		return keyData;
	}
	

}
	

