package com.javaexcel.test.steps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;
import org.testng.Reporter;
import org.testng.annotations.Test;


import com.javaexcel.automation.core.data.Config;
import com.javaexcel.automation.core.execution.TestManager;
import com.javaexcel.automation.core.utils.RestClient;
import com.javaexcel.automation.core.utils.Utils;
import com.javaexcel.tests.base.APITestBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class ValidateAPIResponse extends APITestBase
{
	
	
	public SoftAssert sa = new SoftAssert();
	String actValStr=null;
	String defaultExpVal = "NULL";
	String assertResultsHdrsConsole="";
	public String gExpKey="";
	public long RT=RestClient.elapsedTime;
	public String actualSortedString="";
	

	
	@SuppressWarnings("unchecked")
	@Test(dataProvider = "methodparameters")
	public void validateAPIResponse(Map<String, String> params) throws FileNotFoundException, IOException, ParseException
	{
		
		try {
			int sleep = Integer.parseInt(Config.getProp("sleep"));
			TimeUnit.SECONDS.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		HashMap<String, String> ModifyNodeValue = new HashMap<String, String>();
		String tc_id = params.get("test suite id")+"_"+params.get("test case id");
		String responseJsonDataPath ="reports/json/response/"+tc_id+".json";//params.get("ResponseJSON");
		boolean fileBodyExist=false;
		JsonNode jsonNode = null;
		
		
	/*
	 * Updated Validation
	 */
	try{
		File jsonFile = new File(responseJsonDataPath); 
		if (jsonFile.exists()){
			jsonNode = new ObjectMapper().readTree(jsonFile);
			fileBodyExist = true;
		}
	}
	catch(Exception e){
		System.err.println("Error reading/writing response data... "+e.getMessage());
		Reporter.log("<details open> <summary>View API Response</summary><p style=\"font-size:11px\"> Status Code: "+RestClient.statusCode.trim()+"<br>Response Time (ms): "+RestClient.elapsedTime+"<br><pre>"+"Response Body: "+Jsoup.parse(RestClient.resBodyHTML).text()+"</pre></p></details>");
		Assert.fail();
	}
	
	
	
	
	
	String assertResults="";
	
	String assertResultsConsole="";
	String status;
	String tmp="";
	Boolean assertFlg =false;	
	String jsonStr;
	String file;
	JSONParser parser = new JSONParser();
	JSONArray array = new JSONArray();	
	File tempFile= new File(responseJsonDataPath);
	boolean fileExist = tempFile.isFile();
	
		
    if(fileExist){
	    	Object object = parser.parse(new FileReader(responseJsonDataPath));
    	array.add(object);
    }
        
        /*
         * Log API Response to the Report
         */
        file = array.toString();
        if(!file.equals("{}") && !file.equals(null)){
        	assertFlg=true;
        }
        else{
        	file="";
        }
        
        
        String statusCode = RestClient.statusCode.trim();
        jsonStr = StringEscapeUtils.unescapeJava(RestClient.prettyPrint(file, false));

        
        Reporter.log("<details> <summary>View Response Headers</summary><p style=\"font-size:11px\"><pre>"+RestClient.resHeadersHTML+"</pre></p></details>");
        Reporter.log("<details open> <summary>View API Response</summary><p style=\"font-size:11px\"> Status Code: "+statusCode+"<br>Response Time (ms): "+RestClient.elapsedTime+"<br><pre>"+jsonStr+"</pre></p></details>");
        Reporter.log("<details> <summary>Expected Result</summary><p style=\"font-size:11px\">"+params.get("expected result")+"</p></details>");
        
    
        
        /*
         *Validate Response Headers 
         */
        String HeaderValidations = validateResHeaders(params);
        if(params.get("ValidateStatusLine")==null || params.get("ValidateStatusLine")=="" && params.get("ValidateStatusCode")!=null){
        	RestClient.expectedStatus = params.get("ValidateStatusCode").replace("200", "200 OK").replace("400", "400 Bad Request")
        			.replace("201","201 Created").replace("406", "406 Not Acceptable").replace("403", "403 Forbidden")
        			.replace("500", "500 Internal Server Error").replace("401", "401 Unauthorized").replace("404", "404 Not Found");
        }
        else{
        	RestClient.expectedStatus = params.get("ValidateStatusLine").replace("HTTP/1.1 ", "");
        }
        
        
        /*
         * Validate Response Body
         */
        file = file.replaceAll("\\\\/", "/").replaceAll("]", "").replace("[", "").replace("https:", "https$").replace("{\"\":", "").replaceAll("errors\":", "");
        
        
        
        String[] keysplit=file.split(",");
        String []keystemp={};
        
        
        for(int i=0;i<keysplit.length;i++)
        {
        	String val="";
        	String key="";
        	try{
        	keystemp=	keysplit[i].split(":");
        
        	
        	if (keystemp[0]!=null && assertFlg){
        		key=keystemp[0].replace("\"","").replace("{", "").replace("[{", "").replace("}", "");//.replace("errors", "description");
        	}
        	else{
        		keystemp[0]="NULL";
        	}
        	
        	if (keystemp.length>1){
        		val=keystemp[1].replace("\"","").replace("{", "").replace("}", "").replace("https$", "https:");
        		
        	}

        	ModifyNodeValue.put(key,val);
	    	}
        	catch(Exception e){
        		e.printStackTrace();
        	}          	                       	
        	
        	
         }
        
        
          	String[] sr=params.keySet().toString().split(", |=");
          	List<String> currentValidations = new ArrayList<>();
          
          /*
           * Set Validation Scope
           */
          for(int i=0;i<sr.length;i++)
          {
          if(sr[i].contains("Validation") || sr[i].contains("validation")||sr[i].contains("validate")&&!sr[i].contains("validateheader"))
          {
        	  
        	  currentValidations.add(sr[i]);
          }
          
          }
                   
        	          	
       /*
        * Test Assertions block
        */
       for(String key:currentValidations)
       {
    	      /*
	    	    * Assertions for key-value pair in the response body
	    	    */
	    	   if(!params.get(key).equals("") && params.get(key).contains("#") &&fileBodyExist && !params.get(key).contains("$OR") && !params.get(key).contains("NOTEXIST") && !params.get(key).contains("SORT"))
	    	   {
	    		   
		    		  try{ 
		    			  
		    			   actValStr = null;
		    			   String expvalue=(params.get(key)).split("#")[1];
		    		
		    			   /*
				    	    * Process chained request data used in the expected value
				    	    */
				    	   if(expvalue.contains("header_") || expvalue.contains("body_")){
				    		   expvalue = processChainedData(expvalue);   
				    	   }
				    	   
				    	   if(expvalue.equalsIgnoreCase("{{$Environment}}")){
				    		   expvalue = TestManager.envName;
				    	   }
				    	   
				    	   if(expvalue.equalsIgnoreCase("$AssertCertificate") && TestManager.envName.contains("DEV") || expvalue.equalsIgnoreCase("$AssertCertificate") && TestManager.envName.contains("SANDBOX") ){
				    		   expvalue = "NOT TESTED";
				    	   }
				    	   else if(expvalue.equalsIgnoreCase("$AssertCertificate")){
				    		   expvalue = "true";
				    	   }
				    	   
				    	   if(expvalue.equalsIgnoreCase("$AssertEnvironment") && TestManager.envName.toUpperCase().contains("DEV") || expvalue.equalsIgnoreCase("$AssertEnvironment") && TestManager.envName.toUpperCase().contains("SANDBOX") ){
				    		   expvalue = "SANDBOX";
				    	   }
				    	   else if(expvalue.equalsIgnoreCase("$AssertEnvironment") && TestManager.envName.toUpperCase().contains("VALIDATION")||expvalue.equalsIgnoreCase("$AssertEnvironment") && TestManager.envName.toUpperCase().contains("CERTIFICATION")){
				    		   expvalue = "VALIDATION";
				    	   }
				    	   else if (expvalue.equalsIgnoreCase("$AssertEnvironment") && TestManager.envName.toUpperCase().contains("PROD") || expvalue.equalsIgnoreCase("$AssertEnvironment") && TestManager.envName.toUpperCase().contains("SIT") ){
				    		   expvalue = "PRODUCTION";
				    	   }
				    	   
				    	   
				    	   String fieldName=(params.get(key)).split("#")[0];
				    	   String expkey=(params.get(key)).split("#")[0];
				    	   
				    	   String actval = parseResponseFile("",jsonNode,expkey, expvalue, "", 0);
				    	   
				    	   try{
					    	   if(actval!=null){
					    		   actval = actval.replace(".00", ".0");
					    	   }
					    	   if(!expvalue.contains("\"")){
					    		   actval = actval.replace("\"", "");
					    	   }
					
					    	   if(actval!=null && actval.contains(",")){
					    		   actval = actval.replace("\\\\\\\\q", "").trim();//.replaceAll(" +", " ");
					    	   }
					    	   else if(actval!=null && !actval.contains(",")){
					    		   actval = actval.replace("\\\\q", "").trim();//.replaceAll(" +", " ");
					    	   }
					    	   expkey = expkey.trim().replaceAll(" +", " ");
					    	   
				    	   }
				    	   catch(Exception e){
				    		   e.getMessage();
				    	   }
				    	   
				    	   sa.assertEquals(actval, expvalue,"Unexpected field value for \'"+expkey+"\'");
				    	   if(expvalue.equals(actval))
		   		       			status = "PASS";
		   		       		else
		   		       			status = "FAIL";
				    	   
				    	   assertResultsConsole = assertResultsConsole + "FIELD:VALUE for " +fieldName +" - Expected:"+expvalue+", Actual: "+actval+"   "+status+"||";
				    	   
		   		       		
			    	   /*
			    	    * Conditional formating for html report
			    	    */
			    	   
			    	   if(status.equalsIgnoreCase("FAIL")){
	   		       			tmp = setFontColor("FIELD:VALUE for " +fieldName +" - Expected:"+expvalue+", Actual: "+actval+"   "+status,"red");
	   		       			
	   		       		}
	   		       		else{
	   		       		tmp = setFontColor("FIELD:VALUE for " +fieldName +" - Expected:"+expvalue+", Actual: "+actval+"   "+status,"green");
	   		       		}
	   		       		assertResults = assertResults+ tmp;
			    	   
		    		  }
			    	   

			    		  catch(Exception e)
			    		  {
			    			  e.printStackTrace();
			    		  }
	    	   }
	    	   
	    	   
	    	   /*
	    	    * Assertions for key-value pair with OR condition in the response body
	    	    * Example - transactions[]$currency_code#$OR{"USD","EUR"}
	    	    */
	    	   if(!params.get(key).equals("") && params.get(key).contains("#$OR{") &&fileBodyExist)
	    	   {
	    		   
		    		  try{ 
		    			  
		    			   
		    			   actValStr = null;
				    	   String expvalue=(params.get(key)).split("#")[1];
				    	   /*
				    	    * Process chained request data used in the expected value
				    	    */
				    	   if(expvalue.contains("header_") || expvalue.contains("body_")){
				    		   expvalue = processChainedData(expvalue);   
				    	   }
				    	   
				    	   if(expvalue.contains("$OR{")){
				    		   expvalue = expvalue.replace("$OR{", "").replaceAll("}", "");
				    		   //System.out.println("Debug*** "+expvalue);
				    	   }
				    	   
				    	  
				    	   
				    	   String fieldName=(params.get(key)).split("#")[0];
				    	   String expkey=(params.get(key)).split("#")[0];
				    	   
				    	   
				    	   String actval = parseResponseFile("",jsonNode,expkey, expvalue, "$OR", 0);
				    	   try{
					    	   if(actval!=null){
					    		   actval = actval.replace(".00", ".0");
					    		   
					    	   }
					    	   if(!expvalue.contains("\"")){
					    		   actval = actval.replace("\"", "");
					    		  
					    	   }
					    	   
					    	   if(actval!=null && actval.contains(",")){
					    		   actval = actval.trim().replaceAll(" +", " ");
					    	   }
					    	   else if(actval!=null && !actval.contains(",")){
					    		   actval = actval.trim().replaceAll(" +", " ");
					    	   }
					    	   

					    	   expkey = expkey.trim().replaceAll(" +", " ");
				    	   }
				    	   catch(Exception e){
				    		   e.getMessage();
				    	   }
				    	   
				    	  
				    	   String expArr[] = expvalue.split(",");
				    	   String temp="";
				    	   for(String str : expArr){
				    		   if(actval.equals(str))
				    			   temp = str.trim();
				    	   }
				    	   sa.assertEquals(actval, temp,"Unexpected field value for \'"+expkey+"\'");
				    	   if(temp.equals(actval))
		   		       			status = "PASS";
		   		       		else
		   		       			status = "FAIL";
				    	   
				    	   assertResultsConsole = assertResultsConsole + "FIELD:VALUE with OR condition for " +fieldName +" - Expected (OR):"+expvalue+", Actual: "+actval+"   "+status+"||";
				    	   
		   		       		
			    	   /*
			    	    * Conditional formating for html report
			    	    */
			    	   
			    	   if(status.equalsIgnoreCase("FAIL")){
	   		       			tmp = setFontColor("FIELD:VALUE with OR condition for " +fieldName +" - Expected (OR):"+expvalue+", Actual: "+actval+"   "+status,"red");
	   		       			
	   		       		}
	   		       		else{
	   		       		tmp = setFontColor("FIELD:VALUE with OR condition for " +fieldName +" - Expected (OR):"+expvalue+", Actual: "+actval+"   "+status,"green");
	   		       		}
	   		       		assertResults = assertResults+ tmp;
			    	   
			    	   
		    		  }
			    	   

			    		  catch(Exception e)
			    		  {
			    			  e.printStackTrace();
			    		  }
	    	   }
	    	   
	    	   
	    	   /*
	    	    * Assertions for key-value pair with ORALL condition in the response body
	    	    * Example - transactions[]$currency_code#$ORALL{"USD","EUR"}
	    	    */
	    	   if(!params.get(key).equals("") && params.get(key).contains("#$ORALL") &&fileBodyExist)
	    	   {
	    		   
		    		  try{ 
		    			  
		    			   
		    			   actValStr = null;
				    	   String expvalue=(params.get(key)).split("#")[1];
				    	   /*
				    	    * Process chained request data used in the expected value
				    	    */
				    	   if(expvalue.contains("header_") || expvalue.contains("body_")){
				    		   expvalue = processChainedData(expvalue);   
				    	   }
				    	   
				    	   if(expvalue.contains("$ORALL{")){
				    		   expvalue = expvalue.replace("$ORALL{", "").replaceAll("}", "");
				    		   //System.out.println("Debug*** "+expvalue);
				    	   }
				    	   
				    	  
				    	   
				    	   String fieldName=(params.get(key)).split("#")[0];
				    	   String expkey=(params.get(key)).split("#")[0];
				    	   
				    	   
				    	   String actval = parseResponseFile("",jsonNode,expkey, expvalue, "ORALL", 0);
				    	   try{
					    	   if(actval!=null){
					    		   actval = actval.replace(".00", ".0");
					    		   
					    	   }
					    	   if(!expvalue.contains("\"")){
					    		   actval = actval.replace("\"", "");
					    		  
					    	   }
					    	   
					    	   if(actval!=null && actval.contains(",")){
					    		   actval = actval.trim().replaceAll(" +", " ");
					    	   }
					    	   else if(actval!=null && !actval.contains(",")){
					    		   actval = actval.trim().replaceAll(" +", " ");
					    	   }
					    	   

					    	   expkey = expkey.trim().replaceAll(" +", " ");
				    	   }
				    	   catch(Exception e){
				    		   e.getMessage();
				    	   }
				    	   
				    	   if("PASS".equals(actval))
		   		       			status = "PASS";
		   		       		else
		   		       			status = "FAIL";
				    	   
				    	   assertResultsConsole = assertResultsConsole + "FIELD:VALUE with OR condition for " +fieldName +" - Expected (OR):"+expvalue+"   "+status+"||";
				    	   
		   		       		
			    	   /*
			    	    * Conditional formating for html report
			    	    */
			    	   
			    	   if(status.equalsIgnoreCase("FAIL")){
	   		       			tmp = setFontColor("FIELD:VALUE with OR condition for " +fieldName +" - Expected (OR):"+expvalue+", Actual: "+actval+"   "+status,"red");
	   		       			
	   		       		}
	   		       		else{
	   		       		tmp = setFontColor("FIELD:VALUE with OR condition for " +fieldName +" - Expected (OR):"+expvalue+", Actual: "+actval+"   "+status,"green");
	   		       		}
	   		       		assertResults = assertResults+ tmp;
			    	   
			    	   
		    		  }
			    	   

			    		  catch(Exception e)
			    		  {
			    			  e.printStackTrace();
			    		  }
	    	   }
	    	   
	    	   
	    	   
	    	   /*
	    	    * Assertions for sort order based on a specific field
	    	    * Example - accounts[]$transactions$transaction_datetime#$SORTASCEND
	    	    */
	    	   if(!params.get(key).equals("") && params.get(key).contains("$SORT") &&fileBodyExist)
	    	   {
	    		   
		    		  try{ 
		    			  
		    			   
		    			   actValStr = null;
		    			   String sortType = "ASCEND";
				    	   String expvalue=(params.get(key)).split("#")[1].split("\\$")[0];
				    	   
				    	   /*
				    	    * Process chained request data used in the expected value
				    	    */
				    	   if(expvalue.contains("header_") || expvalue.contains("body_")){
				    		   expvalue = processChainedData(expvalue);   
				    	   }
				    	   

				    	  
				    	   
				    	   String fieldName=(params.get(key)).split("#")[0]+expvalue;
				    	   String expkey=(params.get(key)).split("#")[0];
				    	   
				    	   
				    	   String actval = parseResponseFile("",jsonNode,expkey, expvalue, "SORT", 0);
				    	   
				    	   String actualSort[] = actualSortedString.split(",");
				    	   String expectedSort[] =  actualSortedString.split(",");
				    	   if(params.get(key).contains("ASCEND")){
				    		 Arrays.sort(expectedSort);
				    	   }
				    	   else{
				    		   Arrays.sort(expectedSort,Collections.reverseOrder());
				    		   sortType = "DESCEND";
				    	   }
				    	   
				    	   if(Arrays.equals(actualSort, expectedSort) && actualSort.length>1){
				    		   actval = "true";				    	   		
				    	   }
				    	   else{
				    		   actval = "false";
				    		   if (!(actualSort.length>1)){
				    			   sa.fail("Empty Array! Unexpected API Response.");
				    		   }
				    	   }
				    	   expvalue = "true";
				    	   
				    	   if("true".equals(actval))
		   		       			status = "PASS";
		   		       		else
		   		       			status = "FAIL";
				    	   
				    	   assertResultsConsole = assertResultsConsole + sortType + " SORT validation for " +fieldName +" - Expected: "+expvalue+", Actual: "+actval+"   "+status+"||";
				    	   
		   		       		
			    	   /*
			    	    * Conditional formating for html report
			    	    */
			    	   
			    	   if(status.equalsIgnoreCase("FAIL")){
	   		       			tmp = setFontColor(sortType + "SORT validation for " +fieldName +" - Expected: "+expvalue+", Actual: "+actval+"   "+status,"red");
	   		       			
	   		       		}
	   		       		else{
	   		       		tmp = setFontColor(sortType + "SORT validation for " +fieldName +" - Expected: "+expvalue+", Actual: "+actval+"   "+status,"green");
	   		       		}
	   		       		assertResults = assertResults+ tmp;
			    	   
			    	   
		    		  }
			    	   

			    		  catch(Exception e)
			    		  {
			    			  e.printStackTrace();
			    		  }
	    	   }
	    	   
	    	   
	    	   
	    	   /*
	    	       * Assertion for PATTERN MATCHING
	    	       */
	    		  if(!params.get(key).equals("") && params.get(key).toUpperCase().contains("$MATCH$"))
			       	   {
			              	try{       		
					       			actValStr = null;
						       	   String expkey=(params.get(key)).split("\\$MATCH\\$")[0];
						    	   String ptrnStr=(params.get(key)).split("\\$MATCH\\$")[1];
						    	   String actval = parseResponseFile("",jsonNode,expkey, "dummy","",0);
						    	   if(actval==null){
						    		   actval="NULL";
					    	   }
					    	   String fieldName=(params.get(key)).split("\\$MATCH\\$")[0];
					    	   
					    	   sa.assertTrue(!actval.equals("NULL"),"PATTERN validation failed - \'"+""+"\'");
					    	   
					    	   Boolean ptrnFlg = Utils.validateRegEx(actval, ptrnStr);
			   		       		if(actval.equals("NULL")||ptrnFlg==false)
			   		       			status = "FAIL";
			   		       		else
			   		       			status = "PASS";
			   		       		
			   		       		assertResultsConsole = assertResultsConsole + "PATTERN validation -> "+ptrnStr+" for " + fieldName + " - Expected: true, Actual: "+ ptrnFlg+ "   "+status+"||";
				   		       	
			   		       		if(status.equalsIgnoreCase("FAIL")){
			   		       			tmp = setFontColor("PATTERN validtion -> "+ptrnStr+" for " + fieldName + " - Expected: true, Actual: "+ ptrnFlg+ "   "+status,"red");
			   		       		}
			   		       		else{
			   		       		tmp = setFontColor("PATTERN validation -> "+ptrnStr+" for " + fieldName + " - Expected: true, Actual: "+ ptrnFlg + "   "+status,"green");
			   		       		}
			   		       		assertResults = assertResults+ tmp;
			   		    	   
					       	}
					       	catch(Exception e){
					       		e.printStackTrace();
					       	}
					       	   	
			       	   }
    	   
    	      /*
    	       * Assertion for existence of fields in the response
    	       */
    		  if(!params.get(key).equals("") && params.get(key).toUpperCase().contains("$EXIST"))
		       	   {
		       		   
				       	try{       		
				       			actValStr = null;
					       	   String expkey=(params.get(key)).toUpperCase().split("\\$EXIST")[0];
					    	   String actval = parseResponseFile("",jsonNode,expkey, "dummy","EXIST",0);
					    	   if(actval==null){
					    		   actval="NULL";
				    	   }
				    	   String fieldName=(params.get(key)).toUpperCase().split("\\$EXIST")[0];
				    	   
				    	   sa.assertTrue(!actval.equals("NULL"),"Missing field in the Response - \'"+""+"\'");
				    	   
		   		       		if(actval.equals("NULL"))
		   		       			status = "FAIL";
		   		       		else
		   		       			status = "PASS";
		   		       		
		   		       		assertResultsConsole = assertResultsConsole + "FIELD EXIST for " + fieldName + " - Expected: true, Actual: "+ (!actval.equals("NULL"))+ "   "+status+"||";
			   		       	
		   		       		if(status.equalsIgnoreCase("FAIL")){
		   		       			tmp = setFontColor("FIELD EXIST for " + fieldName + " - Expected: true, Actual: "+ (!actval.equals("NULL"))+ "   "+status,"red");
		   		       		}
		   		       		else{
		   		       		tmp = setFontColor("FIELD EXIST for " + fieldName + " - Expected: true, Actual: "+ (!actval.equals("NULL"))+ "   "+status,"green");
		   		       		}
		   		       		assertResults = assertResults+ tmp;
		   		       		
				       	}
				       	catch(Exception e){
				       		e.printStackTrace();
				       	}
				       	   	
		       	   }
    		  
    		  
    		  /*
    	       * Assertion for non-existence ($NOTEXIST) of fields in the response
    	       * Example: webhooks[]$webhook_id#G232-233$NOTEXIST
    	       */
    		  if(!params.get(key).equals("") && params.get(key).toUpperCase().contains("$NOTEXIST"))
		       	   {
		       		   
				       	try{       		
				       			actValStr = null;
					       	   String assertValue=(params.get(key)).split("#")[1].replace("$NOTEXIST", "");
					       	   String expkey=(params.get(key)).split("#")[0].replace("$NOTEXIST", "");
					    	   String actval = parseResponseFile("",jsonNode,expkey, assertValue,"NOTEXIST",0);
					    	   if(actval==null){
					    		   actval="NULL";
				    	   }
				    	   String fieldName=(params.get(key)).toUpperCase().split("\\$NOTEXIST")[0];
				    	   
				    	   sa.assertTrue(actval.equals("NULL"),"field present in the Response - \'"+""+"\'");
				    	   
		   		       		if(!actval.equals("NULL"))
		   		       			status = "FAIL";
		   		       		else
		   		       			status = "PASS";
		   		       		
		   		       		assertResultsConsole = assertResultsConsole + "FIELD DOES NOT EXIST for " + fieldName + " - Expected: true, Actual: "+ (actval.equals("NULL"))+ "   "+status+"||";
			   		       	
		   		       		if(status.equalsIgnoreCase("FAIL")){
		   		       			tmp = setFontColor("FIELD DOES NOT EXIST for " + fieldName + " - Expected: true, Actual: "+ (actval.equals("NULL"))+ "   "+status,"red");
		   		       		}
		   		       		else{
		   		       		tmp = setFontColor("FIELD DOEST NOT EXIST for " + fieldName + " - Expected: true, Actual: "+ (actval.equals("NULL"))+ "   "+status,"green");
		   		       		}
		   		       		assertResults = assertResults+ tmp;
		   		       		
				    	   
				       	}
				       	catch(Exception e){
				       		e.printStackTrace();
				       	}
				       	   	
		       	   }
    		  
    		  /*
    	       * Assertion for FIELDS not NULL
    	       */
    		  if(!params.get(key).equals("") && params.get(key).contains("$NOTNULL"))
		       	   {
		       		 //System.out.println("Param Key: "+params.get(key));
		       		   
				       	try{       		
				       			actValStr = null;
					       	   String expkey=(params.get(key)).split("\\$NOTNULL")[0];
					    	   String actval = parseResponseFile("",jsonNode,expkey, "dummy", "",0);
					    	   if(actval==null){
					    		   actval="NULL";
				    	   }
				    	   String fieldName=(params.get(key)).split("\\$NOTNULL")[0];
				    	   
				    	   sa.assertTrue(!actval.equals("NULL"),"Field NULL in the Response - \'"+""+"\'");
				    	   
		   		       		if(actval.equals("NULL"))
		   		       			status = "FAIL";
		   		       		else
		   		       			status = "PASS";
		   		       		
		   		       		assertResultsConsole = assertResultsConsole + "FIELD NOT NULL for " + fieldName + " - Expected: true, Actual: "+ (!actval.equals("NULL"))+ "   "+status+"||";
			   		       	
		   		       		if(status.equalsIgnoreCase("FAIL")){
		   		       			tmp = setFontColor("FIELD NOT NULL for " + fieldName + " - Expected: true, Actual: "+ (!actval.equals("NULL"))+ "   "+status,"red");
		   		       		}
		   		       		else{
		   		       		tmp = setFontColor("FIELD NOT NULL for " + fieldName + " - Expected: true, Actual: "+ (!actval.equals("NULL"))+ "   "+status,"green");
		   		       		}
		   		       		assertResults = assertResults+ tmp;
		   		       		
				       	}
				       	catch(Exception e){
				       		e.printStackTrace();
				       	}
				       	   	
		       	   }
    		  
    		  
    		  /*
    	       * Assertion for FIELDS not $IFNOTNULL
    	       */
    		  if(!params.get(key).equals("") && params.get(key).contains("$IFNOTNULL"))
		       	   {
		              	try{       		
				       			actValStr = null;
					       	   String expkey=(params.get(key)).split("\\$IFNOTNULL")[0];
					    	   String actkey = parseResponseFile("",jsonNode,expkey, "dummy","EXIST",0);
					    	   String actval = parseResponseFile("",jsonNode,expkey, "dummy","",0);
					    	   if(actkey==null){
					    		   actkey="NULL";
					    	   }
					    	   
					    	   if(actval==null){
					    		   actval="NULL";
					    	   }
					    	   
				    	   String fieldName=(params.get(key)).split("\\$IFNOTNULL")[0];
				    	   
				    	   if(!actkey.equals("NULL")){
				    		   sa.assertTrue(!actval.equals("NULL"),"Optional Field NULL in the Response - \'"+""+"\'");
				    		   status="FAIL";
				    	   }

				       		if(!actkey.equals("NULL") && actval.equals("NULL"))
		   		       			status = "FAIL";
		   		       		else if(actkey.equals("NULL"))
		   		       				status = "IGNORED";
		   		       		else
		   		       				status = "PASS";
		   		       		
		   		       		
		   		       		assertResultsConsole = assertResultsConsole + "OPTIONAL FIELD NOT NULL for " + fieldName + " - Expected: true, Actual: "+ (!actval.equals("NULL"))+ "   "+status+"||";
			   		       	
		   		       		if(status.equalsIgnoreCase("FAIL")){
		   		       			tmp = setFontColor("OPTIONAL FIELD NOT NULL for " + fieldName + " - Expected: true, Actual: "+ (!actval.equals("NULL"))+ "   "+status,"red");
		   		       		}
		   		       		else if(status.equalsIgnoreCase("IGNORED")){
		   		       			tmp = setFontColor("OPTIONAL FIELD NOT NULL for " + fieldName + " - Expected: true, Actual: "+ (!actval.equals("NULL"))+ "   "+status,"green");
		   		       		}
		   		       		else {
		   		       		tmp = setFontColor("OPTIONAL FIELD NOT NULL for " + fieldName + " - Expected: true, Actual: "+ (!actval.equals("NULL"))+ "   "+status,"green");
		   		       		}
		   		       		assertResults = assertResults+ tmp;
		   		       		
				       	}
				       	catch(Exception e){
				       		e.printStackTrace();
				       	}
				       	   	
		       	   }
    		  
    		  
    		  /*
    	       * Assertion for FIELDS not $ALLIFNOTNULL
    	       */
    		  if(!params.get(key).equals("") && params.get(key).contains("$ALLIFNOTNULL"))
		       	   {
		      		   
				       	try{       		
				       		   actValStr = null;				       			
					       	   String expkey=(params.get(key)).split("\\$ALLIFNOTNULL")[0];
					    	   String actval = parseResponseFile("",jsonNode,expkey, "dummy","ALLIFNOTNULL",0).trim().replace("\"", "");
										    	   
					    	   if(actval==null){
					    		   actval="NULL";					    		  
					    	   }
					    	    
				    	   String fieldName=(params.get(key)).split("\\$ALLIFNOTNULL")[0];
				    	   
				    	   if(actval.equalsIgnoreCase("NULL")){				    		   
				    		   sa.assertTrue(!actval.equalsIgnoreCase("NULL"),"Optional Field NULL in the Response - \'"+""+"\'");
				    		   status="FAIL";
				    	   }

				    	   
				    	   
		   		       		if(actval.equalsIgnoreCase("NULL"))
		   		       			status = "FAIL";
		   		       		else
		   		       			if(actval.equals("IGNORE")){
		   		       				status = "IGNORE";
		   		       			}
		   		       			else{
		   		       				status="PASS";
		   		       			}
		   		       		
		   		       		
		   		       		assertResultsConsole = assertResultsConsole + "OPTIONAL FIELD NOT NULL for " + fieldName + " - Expected: true, Actual: "+ (!actval.equalsIgnoreCase("NULL"))+ "   "+status+"||";
			   		       	
		   		       		if(status.equalsIgnoreCase("FAIL")){
		   		       			tmp = setFontColor("OPTIONAL FIELD NOT NULL for " + fieldName + " - Expected: true, Actual: "+ (!actval.equalsIgnoreCase("NULL"))+ "   "+status,"red");
		   		       		}
		   		       		else if(status.equalsIgnoreCase("IGNORED")){
		   		       			tmp = setFontColor("OPTIONAL FIELD NOT NULL for " + fieldName + " - Expected: true, Actual: "+ (!actval.equalsIgnoreCase("NULL"))+ "   "+status,"green");
		   		       		}
		   		       		else {
		   		       		tmp = setFontColor("OPTIONAL FIELD NOT NULL for " + fieldName + " - Expected: true, Actual: "+ (!actval.equalsIgnoreCase("NULL"))+ "   "+status,"green");
		   		       		}
		   		       		assertResults = assertResults+ tmp;
		   		       		
				       	}
				       	catch(Exception e){
				       		e.printStackTrace();
				       	}
				       	   	
		       	   }
    		  
    		  
    		  /*
    	       * Assertion for FIELDS not $ALLNOTNULL
    	       */
    		  if(!params.get(key).equals("") && params.get(key).contains("$ALLNOTNULL"))
		       	   {
		      		   
				       	try{       		
				       			actValStr = null;				       			
					       	   String expkey=(params.get(key)).split("\\$ALLNOTNULL")[0];
					    	   String actval = parseResponseFile("",jsonNode,expkey, "dummy","ALLNOTNULL",0);
					    	   if(actval==null){
					    		   actval="NULL";
					    	   }
					    	   
				    	   String fieldName=(params.get(key)).split("\\$ALLNOTNULL")[0];
				    	   
				    	   if(actval.equals("NULL")){				    		   
				    		   sa.assertTrue(!actval.equals("NULL"),"Mandatory Field NULL in the Response - \'"+""+"\'");
				    		   status="FAIL";
				    	   }

				    	   
				    	   
		   		       		if(actval.equals("NULL"))
		   		       			status = "FAIL";
		   		       		else{
		   		  
		   		       				status="PASS";
		   		       			}
		   		       		
		   		       		
		   		       		assertResultsConsole = assertResultsConsole + "MANDATORY FIELD NOT NULL for " + fieldName + " - Expected: true, Actual: "+ (!actval.equals("NULL"))+ "   "+status+"||";
			   		       	
		   		       		if(status.equalsIgnoreCase("FAIL")){
		   		       			tmp = setFontColor("MANDATORY FIELD NOT NULL for " + fieldName + " - Expected: true, Actual: "+ (!actval.equals("NULL"))+ "   "+status,"red");
		   		       		}
		   		       		
		   		       		else {
		   		       			tmp = setFontColor("MANDATORY FIELD NOT NULL for " + fieldName + " - Expected: true, Actual: "+ (!actval.equals("NULL"))+ "   "+status,"green");
		   		       		}
		   		       		assertResults = assertResults+ tmp;
		   		    	   
				       	}
				       	catch(Exception e){
				       		e.printStackTrace();
				       	}
				       	   	
		       	   }
    		  
    		  
    		/*
    		 * JSON Schema validation
    		 */
  			if(key.contains("JsonSchema".toLowerCase())&&!params.get(key).equals(""))			
  			{
  				try{      	
  					actValStr = null;
  					JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
  					JsonSchema schema = factory.getJsonSchema(JsonLoader.fromFile(new File("src/test/resources/jsonSchema/"+params.get(key))));
  					JsonLoader.fromString(jsonNode.toString());					
  					
  					ProcessingReport report = schema.validate(JsonLoader.fromString(jsonNode.toString()));
  				     					
  					sa.assertEquals(report.isSuccess(), true,"JSON schema Validation for the response body");
  					if(report.isSuccess())
  					{
  						status = "PASS";
  						tmp = setFontColor("JSON Scehma validation for the response body   "+status,"green");
  						assertResultsConsole = assertResultsConsole + "Json Schema Validation   "+status+"||";
  					}
  					else
  					{
  						status = "FAIL";		
  						tmp = setFontColor("JSON Scehma validation Error:"+report.toString().split("\\n")[2]+"   "+status,"red");
  						assertResultsConsole = assertResultsConsole + "Json Schema Validation   "+status+ " Error:" +report.toString()+"||";
  					}

  					assertResults = assertResults+ tmp;
  				}
  				catch(Exception e){
  					System.err.print("****ERROR - "+e.getMessage()+" ****");
  				}
  			}
    		  
    		  
    		 /*
    		  * Validate HTTP Response Status Line
    		  */
    		  if(key.contains("StatusLine".toLowerCase())&&!params.get(key).equals(""))
          	   {
               		try{
   		       		String expvalue=(params.get(key));
   		       		String actualkey = RestClient.statusCode.trim();
   		       		
   		       		sa.assertEquals(actualkey,expvalue, "Unexpected Response Code - ");
   		       		
   		       		if(expvalue.equals(actualkey))
   		       			status = "PASS";
   		       		else
   		       			status = "FAIL";
   		       		
   		       		assertResultsConsole = assertResultsConsole +"STATUS LINE - Expected: "+expvalue+", Actual: "+actualkey+ "   "+status+"||";
   		       		
   		       		if(status.equalsIgnoreCase("FAIL")){
   		       			tmp = setFontColor("STATUS LINE - Expected: "+expvalue+", Actual: "+actualkey+ "   "+status, "red");
   		       		}
   		       		else{
   		       			tmp = setFontColor("STATUS LINE - Expected: "+expvalue+", Actual: "+actualkey+ "   "+status, "green");
   		       		}
   		       		assertResults = assertResults+ tmp;
   		       		
   		       		}
   		       		catch (Exception e){
   		       			e.printStackTrace();
   		       		}
   		       	   
       	
          	   }
    		  
    		  /*
    		   * Validate Status Code
    		   */
    		  if(key.contains("StatusCode".toLowerCase())&&!params.get(key).equals(""))
         	   {
     	       		try{
  		       		String expvalue=(params.get(key));
  		       		String actualkey = RestClient.statusCode.substring(9,12).trim();
  		       		
  		       		sa.assertEquals(actualkey,expvalue, "Unexpected Response Code - ");
  		       		
  		       		if(expvalue.equals(actualkey))
  		       			status = "PASS";
  		       		else
  		       			status = "FAIL";
  		       		
  		       		assertResultsConsole = assertResultsConsole +"STATUS CODE - Expected: "+expvalue+", Actual: "+actualkey+ "   "+status+"||";
  		       		
  		       		if(status.equalsIgnoreCase("FAIL")){
  		       			tmp = setFontColor("STATUS CODE - Expected: "+expvalue+", Actual: "+actualkey+ "   "+status, "red");
  		       		}
  		       		else{
  		       			tmp = setFontColor("STATUS CODE - Expected: "+expvalue+", Actual: "+actualkey+ "   "+status, "green");
  		       		}
  		       		assertResults = assertResults+ tmp;
  		       		
  		       		}
  		       		catch (Exception e){
  		       			e.printStackTrace();
  		       		}
  		       	   
      	
         	   }
    		  
    		  /*
    		   * Validate Response Time
    		   */
    		  if(key.contains("ResponseTime".toLowerCase())&&!params.get(key).equals(""))
         	   {
               		try{
  		       		Long expRT=Long.parseLong((params.get(key).substring(1)));  		       		
  		       		Long actualRT = RestClient.elapsedTime;
  		       		String compStr = params.get(key).substring(0,1);
  		       		
  		       		
  		       		
  		       		if(compStr.equals("<") && actualRT<expRT){
  		       			status = "PASS";
  		       		}
  		       		else if (compStr.equals(">") && actualRT>expRT){
  		       			status = "PASS";
  		       		}
  		       			else{
  		       				sa.fail("RESPONSE TIME - Expected "+compStr +" "+expRT+", Actual: "+actualRT);
  		       				status="FAIL";
  		       			}
  		       				
  		       		
  		       	assertResultsHdrsConsole = assertResultsHdrsConsole +"RESPONSE TIME - Expected"+ compStr + " "+expRT+", Actual: "+actualRT+ "   "+status+"||";
  		       		
  		       		if(status.equalsIgnoreCase("FAIL")){
  		       			tmp = setFontColor("RESPONSE TIME - Expected "+compStr +" "+expRT+", Actual: "+actualRT+ "   "+status, "red");
  		       		}
  		       		else{
  		       			tmp = setFontColor("RESPONSE TIME - Expected "+compStr +" "+expRT+", Actual: "+actualRT+ "   "+status, "green");
  		       		}
  		       	HeaderValidations = HeaderValidations+ tmp;
  		       		
  		       		}
  		       		catch (Exception e){
  		       			e.printStackTrace();
  		       		}
  		       	   
      	
         	   }
    		  
    		  
    }
	
	
       /*
        * Log Assertion Results to TestNG report
        */
       Reporter.log("<details> <summary>Response Headers Assertions</summary> <p style=\"font-size:11px\">"+HeaderValidations+"</p></details>");
       Reporter.log("<details open> <summary>Response Body Assertions</summary> <p style=\"font-size:11px\">"+assertResults+"</p></details>");
       
       /*
        * Log response headers assertions to console
        */
       String []headStrs = assertResultsHdrsConsole.split("\\|\\|");
       System.out.println("\nASSERTIONS:");
       String msg = "Header-> ";
       for(String headStr : headStrs){
    	   if(headStr.toUpperCase().contains("RESPONSE TIME")){
    		   msg="";
    	   }
	       if(headStr.contains("FAIL")){
	    	   System.err.println(msg+headStr);
	    	   
	       }
	       else {
		    	   System.out.println(msg+headStr);
		       }
       }
       
  
       
       /*
        * Log response body assertions to the console
        */
       String []bodyStrs = assertResultsConsole.split("\\|\\|");
       for(String bodyStr : bodyStrs){
	       if(bodyStr.contains("FAIL")){
	    	   System.err.println("Body-> "+bodyStr);
	    	   
	       }
	       else{
	    	   System.out.println("Body-> "+bodyStr);
	       }
	       
       }
      
     
       sa.assertAll();
       
       
	}
	

	
	public String setFontColor(String msg, String color){
		String temp = "<p style=\"font-size:11px; color:"+color+"\">" +msg+"<p>";
		return temp;
	}
	
	@SuppressWarnings("deprecation")
	public String filetoString(String path){
        String jsonStr=null;
		File respFile = new File(path);
        try {
        	jsonStr = FileUtils.readFileToString(respFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonStr;
	}
	
	public String validateResHeaders(Map<String, String> params){
		String headersStr = RestClient.respHeadersStr.replace("[", "").replace("]", "");
		HashMap<String, String> headersMap = new HashMap<String, String>();
    	String[] keysplit=headersStr.split(", ");
    	
    	String status;
    	String assertResults="";
    	
    	/*
    	 * Load key value pairs to Map
    	 */
    	for(String key_val:keysplit){
    		if(key_val.length()>1){
    			headersMap.put(key_val.split(":")[0].trim(), key_val.split(":")[1].trim());
    		}
    	}
    	
    	
      	String[] sr=params.keySet().toString().split(", |=");
      	List<String> currentValidations = new ArrayList<>();
      
	      /*
	       * Set Validation Scope
	       */
	      for(int i=0;i<sr.length;i++)
	      {
		      if(sr[i].contains("validateheader"))
		      {
		    	  
		    	  currentValidations.add(sr[i]);
		      }
	      
	      }
	      
	      
	      /*
	        * Test Assertions block
	        */
	       for(String key:currentValidations)
	       {
	           String tmp;
	           
		    	   /*
		    	    * Assertions for key-value pair in the response body
		    	    */
		    	   if(!params.get(key).equals("") && params.get(key).contains("#") )
		    	   {
		    		   
			    		  try{ 
			    			  
				    	   String expValue=(params.get(key)).split("#")[1];
				    	   String fieldName=(params.get(key)).split("#")[0];
				    	   String expKey=(params.get(key)).split("#")[0];
				    	   String actVal=headersMap.get(expKey);
				    	   sa.assertEquals(actVal, expValue,"Unexpected field value for \'"+expKey+"\'");
				    	   if(expValue.equals(actVal))
		   		       			status = "PASS";
		   		       		else
		   		       			status = "FAIL";
				    	   
				    	   
				    	   /*
				    	    * Conditional formating for html report
				    	    */
				    	   
				    	   assertResultsHdrsConsole = assertResultsHdrsConsole+"FIELD:VALUE for " +fieldName +" - Expected: "+expValue+", Actual: "+actVal+"   "+status+"||";
				    	   if(status.equalsIgnoreCase("FAIL")){
		   		       			tmp = setFontColor("FIELD:VALUE for " +fieldName +" - Expected: "+expValue+", Actual: "+actVal+"   "+status,"red");
		   		       			
		   		       		}
		   		       		else{
		   		       			tmp = setFontColor("FIELD:VALUE for " +fieldName +" - Expected: "+expValue+", Actual: "+actVal+"   "+status,"green");
		   		       		}
		   		       		assertResults = assertResults+ tmp;
				    	   
				    	   
			    		  }
				    	   

				    		  catch(Exception e)
				    		  {
				    			  e.printStackTrace();
				    		  }
		    	   }
		    	   
	    	   
	    	      /*
	    	       * Assertion for existence of fields in the response
	    	       */
	    		  if(!params.get(key).equals("") && params.get(key).contains("$"))
			       	   {
	    			  	String msg;
	    			       	try{       			  
					       	   String expValue=(params.get(key)).split("\\$")[0];
					    	   String actVal=headersMap.get(expValue);
					    	   if(actVal==null){
					    		   actVal="NULL";
					    	   }
					    	   String fieldName=(params.get(key)).split("\\$")[0];
					    	   
					    	   sa.assertTrue(!actVal.equals("NULL"),"Missing field in the Response - \'"+expValue+"\'");
					    	   
			   		       		if(actVal.equals("NULL"))
			   		       			status = "FAIL";
			   		       		else
			   		       			status = "PASS";
			   		       		msg = "FIELD NOT NULL for ";
					    	   
			   		       		/*
			   		       		 * Assert for Null value in the response
			   		       		 */
					    	   if(params.get(key).split("\\$")[1].equalsIgnoreCase("NotNull")){
					    		   sa.assertTrue(!actVal.equals(""),"Null value in the Response - \'"+expValue+"\'");
					    		   if(actVal.equals("")||actVal.equals("NULL"))
					    			   status = "FAIL";
					    		   else
					    			   status = "PASS";
					    		   
					    		   msg = "FIELD NOT MISSING for ";
					    	   }
					    	   
					    	   assertResultsHdrsConsole = assertResultsHdrsConsole+msg + fieldName + " - Expected: true, Actual: "+ (!actVal.equals("NULL"))+ "   "+status+"||";
			   		       		if(status.equalsIgnoreCase("FAIL")){
			   		       			tmp = setFontColor(msg + fieldName + " - Expected: true, Actual: "+ (!actVal.equals("NULL"))+ "   "+status,"red");
			   		       		}
			   		       		else{
			   		       		tmp = setFontColor(msg  + fieldName + " - Expected: true, Actual: "+ (!actVal.equals("NULL"))+ "   "+status,"green");
			   		       		}
			   		       		
			   		       		assertResults = assertResults+ tmp;
			   		       		
					    	   
					       	}
					       	catch(Exception e){
					       		e.printStackTrace();
					       	}
					       	   	
			       	   }
	    		
	   		       	   
	       	
	          	   }
	    		  
	
	 
	 return assertResults;
	 
	}
	
	public String parseResponseFile(String currentPath, JsonNode jsonNode, String expKey, String expVal, String type, int itr) throws JsonProcessingException, IOException{
		expKey=expKey.replace("$[", "[").replace("]$", "]");	
		
		if(expVal != null && !expVal.isEmpty())
		  {
			  if (jsonNode.isObject()) {
			      ObjectNode objectNode = (ObjectNode) jsonNode;
			      Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
			      String pathPrefix = currentPath.isEmpty() ? "" : currentPath;
			      
			      if(type.equalsIgnoreCase("ALLIFNOTNULL") || type.equalsIgnoreCase("ALLNOTNULL")|| type.equalsIgnoreCase("SORT")){
				        expKey = expKey.replace("]$","]");
				    }
			      
			      while (iter.hasNext()) {		    	  
			        Map.Entry<String, JsonNode> entry = iter.next();
			        
			        String actKey = pathPrefix+entry.getKey();
			        String actVal = entry.getValue().toString();
			        
			        
			        String key = null;
			        if(entry.getValue().isValueNode())
			        {
			        	key = entry.getKey();
			        	if(type.equals("NOTEXIST")) {
			        		expKey=expKey.replace("[]", "["+itr+"]");
			        	}
			        	
			        	if(type.equals("SORT")  && key.equals(expVal)) {
			        		expKey=expKey.replace("[]", "["+itr+"]");
			        		actualSortedString = actualSortedString+actVal+",";
			        	}
			        	
			        	if(actKey.replace("$[", "[").equalsIgnoreCase(expKey))
			        	{
			        			if(type.equals("EXIST")){
			        				return actKey;
			        			}
			        			
			        			if(type.equals("NOTEXIST")){
			        				actVal = actVal.replace("\"", "");
			        				if( actVal.equalsIgnoreCase(expVal)){
			        					defaultExpVal = actVal;
			        					return actVal;
			        				}
			        				else{
			        					actVal = defaultExpVal;
			        				}
			        			}
			        			
			        			
			        			if(type.equalsIgnoreCase("ALLIFNOTNULL")){
			        				sa.assertNotNull(actVal,"Null value found for "+actKey);
			        				System.out.println("Conditional Check for key:value-> "+actKey+":"+actVal);			        				
			        				return actVal==null?null:actVal;
			        			}
			        			else if(type.equalsIgnoreCase("ALLNOTNULL")){
			        				sa.assertNotNull(actVal,"Null value found for "+actKey);
			        				System.out.println("Mandatory Check for key:value-> "+actKey+":"+actVal);			        				
			        				return actVal==null?null:"PASS";
			        			}
			        			
			        			else if(type.equalsIgnoreCase("ORALL")){
			        				
			        				String expArr[] = expVal.split(",");
							    	   String temp="";
							    	   for(String str : expArr){
							    		   if(actVal.equals(str))
							    			   temp = str.trim();
							    	   }
							    	   sa.assertEquals(actVal, temp,"Key-Val Assertion with OR condition failed for "+actKey);
				        				System.out.println("Key-Val Assertion with OR condition for "+actKey+":"+actVal);			        				
				        				return actVal.equalsIgnoreCase(temp)?"PASS":"FAIL";
			        			}
			        			else{
			        				actVal = formatStr(entry, actVal);
			        				return actVal;
			        			}
			        	}
			        	else
			        	{
			        		String[] parents = pathPrefix.split("$");
				        	for(int i = parents.length-1; (i >= 0 && parents.length > 0); i--)
				        	{
				        		key = "$"+parents[i]+key;
				        		if(actKey.equalsIgnoreCase(key))
					        	{
					        		try{
					        		}
					        		catch(Exception e){
					        			e.printStackTrace();
					        		}
				        			break;
					        	}
				        	}
			        	}
			        }
			        else
			        {
			        	try {
							actValStr = parseResponseFile(pathPrefix + entry.getKey()+"$", entry.getValue(), expKey, expVal,type, itr).replace("\\", "\\\\");
						} catch (Exception e) {
							//e.printStackTrace();
						}
			        	
			        }
			      }
			    } else if (jsonNode.isArray() && !jsonNode.toString().contains(":")){
			    				if(currentPath.equalsIgnoreCase(expKey+"$")){
			 	    				String arrayStr[] = jsonNode.toString().split(",");
				    				actValStr="";
				    				for (int i = 0; i < arrayStr.length; i++) {
				    					if(i<arrayStr.length-1)
				    						actValStr = actValStr+""+arrayStr[i].replace("[", "").replace("]", "")+",";
				    					else
				    						actValStr = actValStr+""+arrayStr[i].replace("[", "").replace("]", "")+"";
				    				}
				    				
			    				}
			    				
			    						    				
						 }
			    	 		   else if (jsonNode.isArray()) {
			    	 			   		ArrayNode arrayNode = (ArrayNode) jsonNode;
			    	 				      for (int i = 0; i < arrayNode.size(); i++) {
									    	  itr=i;
									    	  if(expKey.equalsIgnoreCase(currentPath+"["+i+"]")){
									    		  actValStr=arrayNode.get(i).toString();
									    	  }
									    	  else if (type.equalsIgnoreCase("ALLIFNOTNULL")||type.equalsIgnoreCase("ALLNOTNULL")||type.equalsIgnoreCase("ORALL")){
									    		  		actValStr = parseResponseFile(currentPath + "[" + (i+0) + "]", arrayNode.get(i), expKey.replace("[]", "[" + (i+0) + "]"), expVal,type, itr);
									    	  		}
									    	  		else{
									    	  			actValStr = parseResponseFile(currentPath + "[" + (i+0) + "]", arrayNode.get(i), expKey, expVal,type, itr);
									    	  		}
									      }
						    }			  
			 
		   
		  }
		
		if(type.equalsIgnoreCase("ALLIFNOTNULL") && actValStr==null){
			return "IGNORE";
		}
		else if(type.equalsIgnoreCase("ALLNOTNULL") && actValStr==null){
			return null;
		}
		else if(type.equalsIgnoreCase("ORALL") && actValStr==null){
			return null;
		}
		else
		{
			return actValStr==null?null:(actValStr);//.replace("\"", ""));
		}
		
	}
	
	
	String formatStr(Map.Entry<String, JsonNode> entry, String jsonObject){
		
		Object entryValue = entry.getValue();
		String actVal="";
		NumberFormat dformat = new DecimalFormat("#0.00");
		
		try{

					if(entryValue instanceof DoubleNode || entryValue instanceof FloatNode)
					{
						actVal = dformat.format(Double.valueOf(jsonObject)).toString();
					}
					else if(entryValue instanceof FloatNode)
					{
						actVal = dformat.format(Float.valueOf(jsonObject).toString());
					}
					else if(entryValue instanceof IntNode)
					{
						actVal = Integer.valueOf(jsonObject).toString();
					}
					else if(entryValue instanceof BooleanNode)
					{
						actVal = Boolean.valueOf(jsonObject).toString();
					}
					else
					{
						actVal = jsonObject.replace("\\\\", "\\").trim();						
					}
					
		}
		catch (Exception e){
			System.out.println("***Error handling numeric conversion to String.");
		}
		return actVal;
	
	}
	
	String processChainedData(String expVal) throws IOException{
 	   if(expVal.contains("header_") ){
		   expVal = Utils.getChainedRequestDataHeader("#"+expVal);
 	   }
 	   else if (expVal.contains("body_")){
 		  expVal = Utils.getChainedRequestDataBody("#"+expVal);
 	   }
		return expVal;
		
	}


}



	


	

