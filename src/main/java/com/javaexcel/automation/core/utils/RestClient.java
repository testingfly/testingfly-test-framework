package com.javaexcel.automation.core.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
//import org.testng.Reporter;
import org.json.simple.JSONObject;
import org.testng.Reporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.javaexcel.automation.core.data.Config;

import junit.framework.Assert;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * 
 * @author rossmeitei
 *
 */

public class RestClient {

	   public String responseStatus="";
	   public static String statusCode;
	   public static String expectedStatus;
	   public String []HeaderList={};
	   public static String headersStr="";
	   public static String resHeadersHTML;
	   public String reqBody;
	   public String reqBodyHTML;
	   public static String resBodyHTML;
	   public static String respHeadersStr;
	   static long startTime = System.currentTimeMillis();
	   public static long elapsedTime = System.currentTimeMillis();;
	
	
	public static String readFile(String filename) {
	    String result = "";
	 
	    try {
	    	File file = new File(filename);
	    	if(file.exists())
	    	{
		        BufferedReader br = new BufferedReader(new FileReader(filename));
		        StringBuilder sb = new StringBuilder();
		        String line = br.readLine();
		        while (line != null) {
		            sb.append(line);
		            line = br.readLine();
		        }
		        result = sb.toString();
	    	}
	    	else{
	    		System.err.println("WARNING: Request payload not found.");
	    		result = "";
	    	}
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    
	    return result;
	}
	

/**
 * 
 * @param cr_reponse_header_id
 * @param resource_params
 * @param queryParams
 * @param strRequestMethod
 * @param url
 * @param inputFile
 * @param responseXmlFilePath
 * @param certificateFilePath
 * @param certificatePassword
 * @param jksfilePath
 * @param jkspwd
 * @param Header
 * @param tokenUrl
 * @param tokenAuthKey
 * @param contentType
 * @param scopeOverride
 */
	@SuppressWarnings({ "unused", "deprecation" })
	public void postDataToServer(String cr_reponse_header_id,Map<String, String> resource_params, String queryParams, String strRequestMethod,String url,String inputFile,String responseXmlFilePath,String certificateFilePath,String certificatePassword,
			String jksfilePath,String jkspwd,String Header,String tokenUrl,String tokenAuthKey, String scopeOverride)
	{
	
		File oFile= null;
		SSLContext sslcontext = null;
		PrintWriter pw = null;
		String headerNonHtml ="";
		
		try {
			oFile = new File(responseXmlFilePath);
			if (oFile.exists()) {
				
				oFile.delete();
				
			}
			
			if (certificateFilePath != null && !certificateFilePath.isEmpty() && certificatePassword != null
					&& !certificatePassword.isEmpty()) {

				if (jksfilePath != null && !jksfilePath.isEmpty() && jkspwd != null && !jkspwd.isEmpty()) {
					
				} else {
					
					File f = new File(certificateFilePath);
					if(f.exists() && !f.isDirectory()) { 
						sslcontext = installCert(certificatePassword, certificateFilePath, oFile);
					}
					
					
				}

				
			} else {
				sslcontext = SSLContext.getInstance("TLSv1.2");
				sslcontext.init(null, getTrustAllCert(), new java.security.SecureRandom());
			}
			
			SSLConnectionSocketFactory sslsf;
			CloseableHttpClient httpclient = HttpClients.custom().build();
			if(sslcontext!=null){
				 sslsf = new SSLConnectionSocketFactory(sslcontext,
					new String[] { "TLSv1.2", "SSLv3" }, null, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);// BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
				 httpclient = HttpClients.custom().setSSLSocketFactory((LayeredConnectionSocketFactory) sslsf).build();
			}
			else{
				 httpclient = HttpClients.custom().build();
			}
			
			
			
			/*
			 * Setup data based on request type
			 */
			String postData=null;
			byte[] data=null;
			StringEntity se=null;
			if (strRequestMethod.equalsIgnoreCase("post")||strRequestMethod.equalsIgnoreCase("patch")||strRequestMethod.equalsIgnoreCase("put")){
				try {
					postData = readFile((inputFile));
					postData = updatePostData(postData);
					se = new StringEntity(postData);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			

		
			
			TokenDetails td = null;
			
			if (!url.contains(Config.getProp("tomcatPortNo"))){
				td = generateToken(tokenUrl, tokenAuthKey, httpclient, scopeOverride);
			}
			
			/*
			 * Process params in the resource path
			 */
			
			String[] urlParts = url.split("/");
			for (String str : urlParts){				
				if(str.contains("{")){
					String val = null;
					try{
					val = resource_params.get("#"+str.replace("{","").replace("}","")).replace("\"", "");
					}
					catch(Exception e){
						System.err.print("Failed to look up "+"#"+str.replace("{","").replace("}","").replace("\"", "")
								+". Please validate the field name declared in the resource path.\n");
					}
					
					if(val==null||val==""){
						val=str;
						
					}
					url = url.replace(str,val);
				}
				
			}
			
			if(url.contains("{")){
				System.err.println("Error reading input param in the resource path. Check "
						+ "the value passed in the resource path.\nRequest Path: "+url); 
				Assert.fail();
			}

			
			/*
			 * Process Query Params
			 */
			String queryParamsStr[] = queryParams.split("¦");
			if(Arrays.toString(queryParamsStr).contains("#header")){
				queryParams = Utils.getChainedRequestDataHeader(queryParamsStr,url);
			}
			
			if(Arrays.toString(queryParamsStr).contains("#body")){
				queryParams = Utils.getChainedRequestDataBody(queryParamsStr,url);
			}

			HttpPost httppost = new HttpPost(url);
			HttpPatch httppatch = new HttpPatch(url);
			HttpPut httpput = new HttpPut(url);
			HttpDelete httpdelete = new HttpDelete(url);
			HttpGet httpGet= new HttpGet(url.trim()+queryParams.trim());
			headersStr="REQUEST PATH: "+strRequestMethod+" "+url+queryParams+"<br>";
			
			if(queryParams==null){
				queryParams="";
			}
			System.out.println("\n*****Request Path: "+strRequestMethod.toUpperCase()+" "+url+queryParams);
			
			if(!(Header.equalsIgnoreCase("NO")))
			{


			String[] HeaderList_Values=Header.split("\\|");
				for(int i=0;i<HeaderList_Values.length;i++){

					/*
					 * Handle empty header values to avoid throwing array exception
					 */
					if (HeaderList_Values[i].split("\\#").length==1){
						HeaderList_Values[i]=HeaderList_Values[i]+" ";
						System.err.println("\n****WARNING: Missing environment or header information in the API_Env_Properties tab.\n Field: "+HeaderList_Values[i]+"\n");
					}
					
					
					HeaderList=	HeaderList_Values[i].split("\\#");
					
					if(HeaderList[1].equalsIgnoreCase("{guid}"))
					{
						HeaderList[1]=	UUID.randomUUID().toString(); 
					}	
					
					else if(HeaderList[1].equalsIgnoreCase("{TestConfig$AuthKey}"))
					{
						
							
						try{	
							HeaderList[1]=td.getToken_type()+" "+td.getAccess_token();
						}
						catch(Exception e){
							HeaderList[1] = "Bearer";
							
						}
						
					}
					
					if(HeaderList[0].contains("excluded-header")){
						continue;
					}
					
					String processedHeader = Utils.processKeywords(HeaderList[1]);
					if(strRequestMethod.equalsIgnoreCase("post")||strRequestMethod.equalsIgnoreCase("patch")||strRequestMethod.equalsIgnoreCase("put"))
					{
						
						httppost.addHeader(HeaderList[0],processedHeader);
						httppatch.addHeader(HeaderList[0],processedHeader);
						httpput.addHeader(HeaderList[0],processedHeader);
					}
					else
					{
						httpGet.addHeader(HeaderList[0],processedHeader);
						httpdelete.addHeader(HeaderList[0],processedHeader);
						
						
					}
					
						headerNonHtml = headerNonHtml+HeaderList[0]+": "+processedHeader+"\n"; 
						headersStr=headersStr+HeaderList[0].toUpperCase()+": "+processedHeader+"<br>"; 
					
					
				}
				
			}		
			

			CloseableHttpResponse closeableHttpResponse=null;	
			

			
			String headersStr;
			startTime = System.currentTimeMillis();
			elapsedTime=0;
			
			
			
			if(strRequestMethod.equalsIgnoreCase("post"))
			{
				httppost.setEntity(se);
				headersStr = Arrays.toString(httppost.getAllHeaders());
				printRequestDetails(headersStr, postData);
				closeableHttpResponse = httpclient.execute((HttpUriRequest) httppost);
				elapsedTime = System.currentTimeMillis() - startTime;
				
				
				reqBody = prettyPrint(postData, false);
				reqBodyHTML = prettyPrint(postData,false);
				
			}
			else
				if(strRequestMethod.equalsIgnoreCase("patch"))
				{
					httppatch.setEntity(se);
					headersStr = Arrays.toString(httppatch.getAllHeaders());
					printRequestDetails(headersStr, postData);
					closeableHttpResponse = httpclient.execute((HttpUriRequest) httppatch);
					
					elapsedTime = System.currentTimeMillis() - startTime;
					
					reqBody = prettyPrint(postData, false);
					reqBodyHTML = prettyPrint(postData,true);

				}
				else if(strRequestMethod.equalsIgnoreCase("put"))
				{
					httpput.setEntity(se);
					headersStr = Arrays.toString(httpput.getAllHeaders());
					printRequestDetails(headersStr, postData);
					closeableHttpResponse = httpclient.execute((HttpUriRequest) httpput);
					elapsedTime = System.currentTimeMillis() - startTime;
					
					reqBody = prettyPrint(postData, false);
					reqBodyHTML = prettyPrint(postData,true);
	
				}
				else 
					if(strRequestMethod.equalsIgnoreCase("delete"))
					{
						headersStr = Arrays.toString(httpdelete.getAllHeaders());
						printRequestDetails(headersStr, "");
						closeableHttpResponse = httpclient.execute((HttpUriRequest) httpdelete);
						elapsedTime = System.currentTimeMillis() - startTime;
					}
					else
					{
						closeableHttpResponse = httpclient.execute((HttpUriRequest) httpGet);
						elapsedTime = System.currentTimeMillis() - startTime;
						headersStr = Arrays.toString(httpGet.getAllHeaders());
						printRequestDetails(headersStr, "");
					}
			
			/*
			 * Response Time
			 */
			responseStatus=closeableHttpResponse.toString();
			HttpEntity entity = closeableHttpResponse.getEntity();
			

			/*
			 * Log Status Code and Response Headers
			 */
			respHeadersStr = Arrays.toString(closeableHttpResponse.getAllHeaders());
			statusCode = closeableHttpResponse.getStatusLine().toString();
			System.out.println("\nHTTP Status Code: "+statusCode);
			System.out.println("Response Time (ms): "+ elapsedTime);
			System.out.println("\nAPI Response Headers:\n"+ respHeadersStr.replaceAll(",", "\n"));
			resHeadersHTML = Arrays.toString(closeableHttpResponse.getAllHeaders()).replaceAll(",", "<br>");
			
			
			BufferedWriter bw;
			
			if (entity != null) {
					String line;
					bw = new BufferedWriter(new FileWriter(oFile)); 
					StringBuffer response = new StringBuffer();
					InputStream is = entity.getContent();	
					BufferedReader rd = new BufferedReader(new InputStreamReader(is));
					while ((line = rd.readLine()) != null) {
						response.append(line);
						response.append('\n');
					}
	
				

				String jsonStr = StringEscapeUtils.unescapeJava(prettyPrint(response.toString(), false));
				
				System.out.println("API Response Body:\n"+ prettyPrint(response.toString(), false).replace("\\u0027", "\'"));
				resBodyHTML=prettyPrint(response.toString(), false).replace("\\u0027", "\'");
				
				

				bw.write(prettyPrint(response.toString(), false));
				
				bw.close();
				rd.close();
				
			}
			
			/*
			 * Write response headers to file
			 */
			oFile = new File(cr_reponse_header_id);				
			bw = new BufferedWriter(new FileWriter(oFile));
			bw.write(prettyPrint(respHeadersStr, false));
			
			bw.close();
			
			
			

			closeableHttpResponse.close();
		
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			Reporter.log(e.getLocalizedMessage());
			RestClient.resBodyHTML = e.getMessage();
			RestClient.statusCode = "N/A";
			try {
				pw = new PrintWriter(new FileWriter(oFile), true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace(pw);

		} 
	}
	
	private String updatePostData(String postData) {
		postData = postData.replace("\"{{empty}}\"", "").replace("\"$NULL\"", "null");
		return postData;
	}


	public  String getRandomHexString(int numchars){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < numchars){
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }
	
	public String responseText()
	{
		return responseStatus;
	}
	

	/***
	 * Reads a file convert its contents to byte array.
	 * 
	 * @param file
	 * @return array of byte of file content
	 * @throws IOException
	 */
	
	public static byte[] read(File file) throws IOException {
		ByteArrayOutputStream ous= null;
		InputStream ios = null;
		try {
			byte[] buffer = new byte[4096];
			ous = new ByteArrayOutputStream();
			ios = new FileInputStream(file);
			int read = 0;
			while ((read = ios.read(buffer)) != -1) {
				ous.write(buffer, 0, read);
			}
		} finally {
			try {
				if (ous != null) {
					ous.close(); }
			} catch (IOException var6_8) {}
			try {
				if (ios != null) {
					ios.close();
				}
			} catch (IOException var6_9) {	}
		}
		return ous.toByteArray();
	}

	/***
	 * Resolve certificate issue to connect to SSL layer
	 * 
	 * @return
	 */
	public static TrustManager[] getTrustAllCert() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}

		} };
		return trustAllCerts;
	}
	
/***
 * Install the certificate using the provided Cert details
 * @param certificatePassword
 * @param certificateFilePath
 * @return
 */

public static SSLContext installCert(String certificatePassword,String certificateFilePath,File oFile){
	
	KeyStore trustStore;
	PrintWriter pw = null;
	FileInputStream instream= null ;
	SSLContext  sslcontext= null;
	
	try {
		trustStore = KeyStore.getInstance("PKCS12");
		instream = new FileInputStream(new File(certificateFilePath));

		trustStore.load(
				new FileInputStream(certificateFilePath), certificatePassword.toCharArray());
	        
		
		sslcontext= SSLContexts.custom()
				.loadKeyMaterial(trustStore, certificatePassword.toCharArray()).build();
	} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException |IOException | CertificateException e) {
		 try {
			pw= new PrintWriter(new FileWriter(oFile),true);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		e.printStackTrace(pw);

	} finally {
		try {
			instream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	 return sslcontext;
	 } 



/**
 * 
 * @param jsonStr
 * @param EscFlg if false, it disables html escape chars
 * @return
 */
public static String prettyPrint(String jsonStr, Boolean EscFlg){
	
	Gson gson = null;
	
	if(!EscFlg){
		gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();
	}
	else {
		gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	}
	JsonParser jp = new JsonParser();
	
	try{
		JsonElement je = jp.parse(jsonStr);
		return gson.toJson(je);
	}
	catch(Exception e){
		return jsonStr;
	}
	
	
}

/**
 * convert json Data to structured Html text
 * 
 * @param json
 * @return string
 */
public String jsonToHtml( Object obj ) {
    StringBuilder html = new StringBuilder( );

    try {
        if (obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject)obj;
            String[] keys = (String[]) jsonObject.keySet().toArray();

            html.append("<div class=\"json_object\">");

            if (keys.length > 0) {
                for (String key : keys) {
                    html.append("<div><span class=\"json_key\">")
                        .append(key).append("</span> : ");

                    Object val = jsonObject.get(key);
                    html.append( jsonToHtml( val ) );
                    html.append("</div>");
	                }
	            }
	
	            html.append("</div>");
	
	        } else if (obj instanceof JSONArray) {
	            JSONArray array = (JSONArray)obj;
	            for ( int i=0; i < ((CharSequence) array).length( ); i++) {
	                html.append( jsonToHtml( array.get(i) ) );                    
	            }
	        } else {
	            html.append( obj );
	        }                
	    } catch (Exception e) { return e.getLocalizedMessage( ) ; }
	
	    return html.toString( );
	}


	public TokenDetails generateToken(String tokenUrl, String tokenAuthKey, CloseableHttpClient httpclient, String scope){
		String json="";
		
		if(tokenUrl.contains(Config.getProp("tomcatPortNo"))){
			return null;
		}
		try{
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
	        postParameters.add(new BasicNameValuePair("scope", scope));
	        
	        HttpPost httppostForToken = new HttpPost(tokenUrl); 
	        httppostForToken.addHeader("Content-Type", "application/x-www-form-urlencoded");
	        httppostForToken.addHeader("Authorization", tokenAuthKey);
	        httppostForToken.setEntity(new UrlEncodedFormEntity(postParameters,"UTF-8"));
	        CloseableHttpResponse closeableHttpResponseForToken = null;
	        HttpEntity entityToken = null;
	        
        	closeableHttpResponseForToken = httpclient.execute((HttpUriRequest) httppostForToken);
			entityToken = closeableHttpResponseForToken.getEntity();
			json = EntityUtils.toString(entityToken);
			if(closeableHttpResponseForToken.toString().contains("400 Error")){
				throw new Exception(closeableHttpResponseForToken.toString());
			}
			
			
        }
        catch(Exception e){
        	System.err.println("ERROR generating token. "+e.getMessage());
        	
        }
		
		ObjectMapper mapper = new ObjectMapper();
		
		TokenDetails td = null;
		if(json.toString().length()>0){
			try{
			 td= mapper.readValue(json.getBytes(), TokenDetails.class);
			}
			catch (Exception e){
				System.err.println("****ERROR generating token. Please validate the Token URL and Auth Token used.");
				System.err.println(json.toString());
			}
		}
		
		
		return td;
		
	}
	
	void printRequestDetails(String headers, String body){
		
		/*
		 * Print Request Headers and Body
		 */
		System.out.println("Request Headers:\n"+ headers.replace(",", "\n"));
		if(body.length()>1){
			System.out.println("Request Body:\n"+ prettyPrint(body, false));
		}
		
		
	}

}


