package com.javaexcel.test.steps;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringEscapeUtils;
import org.testng.Reporter;
import org.testng.annotations.Test;

import junit.framework.Assert;
import com.javaexcel.automation.core.data.Config;
import com.javaexcel.automation.core.data.Configurables;
import com.javaexcel.automation.core.utils.RestClient;
import com.javaexcel.automation.core.utils.Utils;
import com.javaexcel.tests.base.APITestBase;
/**
 * 
 * @author rossmeitei
 * Processes resource path, header params, chained request data
 * Sends API request
 */
public class SendAPIRequest extends APITestBase {

	String dataFilePath = System.getProperty("user.dir");
	String responseFolder = Config.getProp("UploadDirectory");
	String testRunId = Configurables.runID;
	String cr_reponse_header_id;
	//String contentType="application/json";
	
	  
	@Test(dataProvider = "methodparameters")
	public void sendRequest(Map<String, String> params) throws Exception {

		String strCertificatePath = dataFilePath + "/"+ params.get("CertificatePath");
		String strCertiPass = params.get("CertificatePassword");
		String strRootCerti = dataFilePath + "/"+ params.get("RootCertificatePath");
		String strRootPass = params.get("RootPassword");		
		String tc_id = params.get("test suite id")+"_"+params.get("test case id");
		String requestPath = "reports/json/request/"+ tc_id+".json";
		String responseXMLDataPath = "reports/json/response/" + tc_id+".json";
		
		File responsedestinationpath = new File(responseXMLDataPath);
		if (!responsedestinationpath.exists()) {
			responsedestinationpath.mkdirs();
		}

		RestClient rs = new RestClient();
		String resourcePath=params.get("Wso2 Resource Path");
		
		if(params.get("BaseURI").contains(Config.getProp("tomcatPortNo"))){
			resourcePath=params.get("Tomcat Resource Path");
		}
		

		if(params.get("Override Resource Path")!=null && !params.get("Override Resource Path").equals("")){
			resourcePath = params.get("Override Resource Path");
		}
		
		if(params.get("Add to Path")!=null && !params.get("Add to Path").equals("")){
			resourcePath = resourcePath+params.get("Add to Path");
		}
		
		String baseURI="";
		if(params.get("Override Base URI")!=null && !params.get("Override Base URI").equals("")){
			baseURI = params.get("Override Base URI");
		}
		else{
			baseURI = params.get("BaseURI");
		}
		
		String uri = baseURI+resourcePath;

		/*
		 * Set response header id for chained request (cr)
		 */
		cr_reponse_header_id = "reports/json/response/"+tc_id+"_header.json"; 

		/*
		 * Override certificate path
		 */
		if(params.get("Override CertificatePath")!=null && !params.get("Override CertificatePath").equals("")){
			strCertificatePath = params.get("Override CertificatePath");
		}
		else{
			strCertificatePath = params.get("CertificatePath");
		}
		
		/*
		 * Override certificate password
		 */
		if(params.get("Override CertificatePassword")!=null && !params.get("Override CertificatePassword").equals("")){
			strCertiPass = params.get("Override CertificatePassword");
		}
		else{
			strCertiPass = params.get("CertificatePassword");
		}

		/*
		 * Retrieve Header Values
		 */
		String headerValue = new String();
		headerValue = filterParam("header",params);

		if (System.getProperty("EntityID")!=null && !System.getProperty("EntityID").equals("")){
			Pattern pattern = Pattern.compile("entity-id#(.*?)\\||entity-id#(.*)", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(headerValue);
			if(matcher.find())
			{			    	
				headerValue = headerValue.replaceAll(matcher.group(0).replace("|", ""),"entity-id#"+System.getProperty("EntityID"));		    	
			}		    	
		}

		if (System.getProperty("UserID")!=null && !System.getProperty("UserID").equals("")){
			Pattern pattern = Pattern.compile("user-id#(.*?)\\||user-id#(.*)", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(headerValue);		    
			if(matcher.find())
			{
				headerValue = headerValue.replaceAll(matcher.group(0).replace("|", ""),"user-id#"+System.getProperty("UserID"));
			}
		}

		/*
		 * Add default Content-Type to headers"
		 */
		//headerValue = headerValue+"|Content-Type#"+contentType;
		//System.out.println("Content-Type: "+contentType);


		/*
		 * Remove excluded headers
		 */
		//System.out.println("Header value*** "+headerValue);
		headerValue = removeExcludedHeaders(params, headerValue);
		
		
		/*
		 * Retrieve Query Params from test sheet
		 * u381126 - Updated on 3/12/2019 to support multiple query parameters lookup
		 */
		String queryParam = params.get("query params");
		
		if(queryParam==null || queryParam==""){
			queryParam="";
		}
		else{
			queryParam = Utils.processKeywords(queryParam);
			String[] paramsArr=null;
			Map<String, String> qParamMap = new LinkedHashMap<>();
			
			if(queryParam.contains("&")){
				paramsArr=queryParam.split("&");	
				for(int i=0;i<paramsArr.length;i++){
					if(i>0)
						qParamMap.put("&"+paramsArr[i].split("=")[0], paramsArr[i].split("=")[1]);
					else
						qParamMap.put(paramsArr[i].split("=")[0], paramsArr[i].split("=")[1]);
				}
			}else{
				qParamMap.put(queryParam.split("=")[0], queryParam.split("=")[1]);
			}
				
			queryParam = processQueryParams(qParamMap);
		}
		

		/*
		 * Retrieve Chained Request Data from test sheet
		 */
		GenerateJSONFile gj = new GenerateJSONFile();
		gj.getChainedRequestDataHeader(params);
		gj.getChainedRequestDataBody(params);
		
		/*
		 * Update header to invalid value if applicable
		 * Override header
		 */
		headerValue = updateHeaderValue(params, headerValue);
		
		/*
		 * Add new header at TC level as needed
		 */
		headerValue = addNewHeaderValue(params, headerValue);





		/*
		 * Retrieve the API Call Method Type
		 */
		String request_type = params.get("requesttype");

		/*
		 * Retrieve overridden request-type from data sheet
		 */
		String overriddenMethod = filterParam("override method",params);
		if(!overriddenMethod.equals("")){
			request_type=overriddenMethod;
		}
		
		

		/*
		 * Process resource path params including chained request data used in the resource path.
		 */
		Map<String, String> resource_params = processResourcePath("#",params);


		//Retrieve Auth Key
		String authToken = null;
		if (!System.getProperty("Authentication_Key").equals(""))
		{
			authToken = System.getProperty("Authentication_Key");
		}
		else 
		{
			String ver = params.get("Version");
			authToken = params.get("Auth Key"+ver);
		}
		
		/*
		 * Override authentication token if required
		 */
		if(params.get("Override Auth Token")!=null && !params.get("Override Auth Token").equals("")){
			authToken = params.get("Override Auth Token").trim();
			if(authToken.contains("#") && !authToken.contains("#body")){
				authToken = Utils.processAuthToken(authToken);
			}
			
			
			System.out.println("***Overwriting auth token with: "+authToken);

		}

		if(!authToken.contains("Basic")){
			authToken = "Basic "+authToken;
		}

		/*
		 * Override scope
		 */
		String scopeOverride = Config.getProp("API_Scopes");
		if(params.get("Override Scope")!=null && !params.get("Override Scope").equals("")){
			scopeOverride = params.get("Override Scope");
		}

		/*
		 * Process delay param provided in seconds
		 */
		if(params.get("delay")!=null && !params.get("delay").equals("")){
			int delay = Integer.parseInt(params.get("delay"));
			System.out.println("Adding delay (seconds): "+delay);
			Reporter.log("Adding delay (seconds): "+delay);
			Utils.sleep(delay);
		}

		if(request_type.equals("GET")){
			rs.postDataToServer(cr_reponse_header_id,resource_params,queryParam,request_type,uri,"",responseXMLDataPath,strCertificatePath,strCertiPass,strRootCerti,strRootPass,headerValue,params.get("TOKEN_URL"),authToken, scopeOverride);			
			Reporter.log("<details> <summary>View Request Headers</summary><p style=\"font-size:11px\">"+rs.headersStr+"</p></details>");
		}
		else{
			if(request_type.equals("PATCH")){				
				rs.postDataToServer(cr_reponse_header_id,resource_params,queryParam,request_type,uri,requestPath,responseXMLDataPath,strCertificatePath,strCertiPass,strRootCerti,strRootPass,headerValue,params.get("TOKEN_URL"),authToken, scopeOverride);
			}
			else
				if(request_type.equals("DELETE")){
					rs.postDataToServer(cr_reponse_header_id,resource_params,queryParam,request_type,uri,"",responseXMLDataPath,strCertificatePath,strCertiPass,strRootCerti,strRootPass,headerValue,params.get("TOKEN_URL"),authToken, scopeOverride);
				}
				else if(request_type.equals("POST")){
					rs.postDataToServer(cr_reponse_header_id,resource_params,queryParam,request_type,uri,requestPath,responseXMLDataPath,strCertificatePath,strCertiPass,strRootCerti,strRootPass,headerValue,params.get("TOKEN_URL"),authToken, scopeOverride);
				}

				else if(request_type.equals("PUT")){
					rs.postDataToServer(cr_reponse_header_id,resource_params,queryParam,request_type,uri,requestPath,responseXMLDataPath,strCertificatePath,strCertiPass,strRootCerti,strRootPass,headerValue,params.get("TOKEN_URL"),authToken, scopeOverride);
				}
				else {
					System.err.println("***Error - Invalid HTTP Method used - " + request_type+ ". Please validate the setup in the test data sheet.");
					Assert.fail("***Error - Invalid HTTP Method used - " + request_type+ ". Please validate the setup in the test data sheet.");
				}

			/*
			 * Log request content to report
			 */
			Reporter.log("<details> <summary>View Request Headers</summary><p style=\"font-size:11px\"><pre>"+rs.headersStr+"</pre></p></details>");
			Reporter.log("<details> <summary>View Request Body</summary><p style=\"font-size:11px\"><pre>"+StringEscapeUtils.escapeHtml(rs.reqBodyHTML)+"</pre></p></details>");
			


		}


	}

	public String removeExcludedHeaders(Map<String, String> params, String headers){
		
		String excludedHeaders = filterParam("exclude header",params);
		String excludedHeaderArr[] = excludedHeaders.split("\\|");
		

		for(String updatedStr : excludedHeaderArr){

			if (!excludedHeaders.equals("")){
				String header = "excluded-header_"+updatedStr.split("#")[0].trim();
				headers = headers.replace(updatedStr, header);
			}
		}
		return headers;

	}



	
	/**
	 * Used to add new headers at TC level as needed
	 * @param params
	 * @param headers
	 * @return
	 */
	public String addNewHeaderValue(Map<String, String> params, String headers){
		String allHeaderStrs[] = headers.split("\\|");
		String newHeaders = filterParam("add header",params);
		String newHeaderStrs[] = newHeaders.split("\\|");
		
		//System.out.println("Headers***"+Arrays.toString(newHeaderStrs));
		
		for(String updatedStr : newHeaderStrs){

			if (!newHeaders.equals("")){
				String header = updatedStr.split("#")[0].trim();
				String value = updatedStr.split("#")[1].trim();
				
				if (value.equals("{{EMPTY}}")){
					value = "";
				}
				value = Utils.processKeywords(value);
				HashMap <String, String> hdrMap = new HashMap<>();

				if(updatedStr.split("#")[0].length()>1){
						hdrMap.put(header.trim(), value.trim());
						
					}
					
				for(Object str : hdrMap.keySet().toArray() ){
					headers = headers + "|"+str+"#"+hdrMap.get(str);
				}
				
			}
		}
		return headers;


	}
	
	public String updateHeaderValue(Map<String, String> params, String headers){
		String headerStrs[] = headers.split("\\|");
		String updatedHeaders = filterParam("override header",params);
		String updatedHeaderStrs[] = updatedHeaders.split("\\|");
		
		if(updatedHeaders.contains("#body")||updatedHeaders.contains("#header")){
			
			try {
				/*
				 * Override headers if applicable
				 */
				if(updatedHeaders.contains("Authorization")){
					updatedHeaders = filterParam("override header",params);
					updatedHeaders = updatedHeaders.split("#")[0]+"#Bearer "+updatedHeaders.split("#")[1];
					updatedHeaderStrs = updatedHeaders.split("\\|");
				}
				else{
					updatedHeaders = filterParam("override header",params);
					updatedHeaderStrs = updatedHeaders.split("\\|");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		for(String updatedStr : updatedHeaderStrs){

			if (!updatedHeaders.equals("")){
				String header = updatedStr.split("#")[0].trim();
				String value = updatedStr.split("#")[1].trim();
				
				if (value.equals("{{EMPTY}}")){
					value = "";
				}
				
				value = Utils.processKeywords(value);

				HashMap <String, String> hdrMap = new HashMap<>();

				for(int i=0;i<headerStrs.length;i++){
					
					if(headerStrs[i].split("#")[0].equals(header)){
						hdrMap.put(header.trim(), value.trim());
						headerStrs[i]=header.trim()+ "#" +value.trim();
						
					}
					else if(headerStrs[i].split("#").length==1){
						
						hdrMap.put(headerStrs[i].split("#")[0].trim(), "");
						
					}
					else{
						hdrMap.put(headerStrs[i].split("#")[0].trim(), headerStrs[i].split("#")[1].trim());
					}


				}
				
				
				
				headers = (hdrMap.entrySet().toString().replaceAll(",", "\\|").replace("[", "").replace("]", "").replace("| ", "|"));//.replaceAll("==.*?", "^^^").replaceAll("=.*?", "#").replace("^^^", "=="));
				for(Object str : hdrMap.keySet().toArray() ){
					headers = headers.replaceAll(str+"=", str+"#");
				}
				
			}
		}

		return headers;


	}

	public static String filterParam(String filter, Map<String, String> params)
	{
		Set<String> set = params.keySet().stream()
				.filter(s -> s.startsWith(filter))
				.collect(Collectors.toSet());
		StringJoiner headerJoiner = new StringJoiner("|");
		for (String s : set) {
			if (params.get(s).length() > 0) {
				headerJoiner.add(params.get(s));
			}
		}
		String filterValue = headerJoiner.toString();
		return filterValue;

	}

	/**
	 * Process resource path including chained request data
	 * @param filter
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> processResourcePath(String filter, Map<String, String> params) throws IOException
	{
		
		Map<String, String> resourceVars = new HashMap<String, String>();
		Set<String> set = params.keySet().stream()
				.filter(s -> s.startsWith(filter))
				.collect(Collectors.toSet());

		for (String s : set) {
			String updatedStr="";
			try{
				if(params.get(s)!=null && params.get(s)!="" && params.get(s).contains("header_")){
					updatedStr= Utils.getChainedRequestDataHeader(params.get(s));
					resourceVars.put(s, updatedStr);
				}
				else if(params.get(s)!=null && params.get(s)!="" && params.get(s).contains("body_")){
						updatedStr= Utils.getChainedRequestDataBody(params.get(s).replace("\"", ""));
						//System.out.println("***updatedStr: "+updatedStr+", Keyword: "+params.get(s));
						resourceVars.put(s, updatedStr);
				}
				else if(params.get(s)!=null && params.get(s)!=""){
						resourceVars.put(s, params.get(s));
				}
			}
			catch(Exception e){
				System.err.println("Error reading input param in the resource path - "+s+". Undefined or data not found. Check "
						+ "the value passed for this parameter. \n The lookup returned "+e.getMessage());
				
			}



		}

		return resourceVars;

	}
	
	/**
	 * Processes query params with lookups based on chained data request.
	 * @param filter
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static String processQueryParams(Map<String, String> params) throws IOException
	{
		
		Map<String, String> queryParams = new HashMap<String, String>();
		Set<String> set = params.keySet();
		for (String s : set) {
			String updatedStr="";
			try{
				if(params.get(s)!=null && params.get(s)!="" && params.get(s).contains("header_")){
					updatedStr= Utils.getChainedRequestDataHeader(params.get(s));
					queryParams.put(s, updatedStr);
				}
				else if(params.get(s)!=null && params.get(s)!="" && params.get(s).contains("body_")){
						updatedStr= Utils.getChainedRequestDataBody(params.get(s).replace("\"", ""));
						//System.out.println("***updatedStr: "+updatedStr+", Keyword: "+params.get(s));
						queryParams.put(s, updatedStr);
				}
				else if(params.get(s)!=null && params.get(s)!=""){
						queryParams.put(s, params.get(s));
				}
			}
			catch(Exception e){
				System.err.println("Error reading input param in the resource path - "+s+". Undefined or data not found. Check "
						+ "the value passed for this parameter. \n The lookup returned "+e.getMessage());
				
			}



		}
		
		String queryParamsStr="";
		for (String s : set) {
			queryParamsStr = queryParamsStr+s+"="+queryParams.get(s).replaceAll("\"", "");
		}
		
		return queryParamsStr;

	}



}
