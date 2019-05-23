package com.javaexcel.automation.core.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.poi.openxml4j.opc.internal.FileHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.testng.Assert;
import org.testng.Reporter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.javaexcel.automation.alm.Client;
import com.javaexcel.automation.alm.XmlParser;
import com.javaexcel.automation.alm.model.Test;
import com.javaexcel.automation.alm.model.TestInstance;
import com.javaexcel.automation.alm.model.TestSet;
import com.javaexcel.automation.core.data.Config;
import com.javaexcel.automation.core.data.Configurables;
import com.javaexcel.automation.core.data.IConfigurer;
import com.javaexcel.automation.core.data.TextConfigurer;
import com.javaexcel.automation.core.execution.TestManager;
import com.javaexcel.automation.core.listeners.CustomLogger;
import com.javaexcel.automation.core.table.Field;
import com.javaexcel.automation.core.table.Record;
import com.javaexcel.automation.core.table.Table;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import com.javaexcel.automation.alm.model.*;

public class Utils {

	//Maximum number of characters for printing.  Used for inserting spaces after "::" characters for clean printing
	private static final int maxPrintLength = 50;
	public static String actValStr = null;
	public static Boolean pdfReport = false;
	public static Boolean xlsReport = false;
	public static String pdfFileName;
	public static Map<String, String> respMap = new HashMap<>();
	public static Boolean flgWhereValFound=false;
	public static int item_count = 0;
	public static Boolean loopFlg = false;
	

	/**
	 * Splits a string based on a specified delimiter and returns an array where each member is trimmed.
	 * 
	 * @param s - The string to be split.
	 * @param delimiter - The character to split by.
	 * @return The split string as an array.
	 */
	public static String[] splitTrim(String s, String delimiter){
		return s.trim().split("\\s*" + delimiter + "\\s*");
	}

	/**
	 * Trims string, including no-breaking spaces.
	 * @param s - String to trim.
	 * @return - The trimmed string.
	 */
	public static String trim(String s){
		return s.replace(String.valueOf((char) 160), " ").trim();
	}

	/**
	 * Checks if a string is null, empty, or consists of nothing but spaces.
	 * 
	 * @param s - The string to be checked.
	 * @return True if the string is not null or not empty, false otherwise.
	 */
	public static boolean checkExists(String s){
		if (s == null) {
			return false;
		}
		if (s.trim().equals("")) {
			return false;
		}
		return true;
	}

	public static String fullAlign(String s, int length){
		if (s.contains("::")){
			int totalLength = s.length();
			if (totalLength < length){
				int tabLength = length - (totalLength-1);
				String spaces = new String(new char[tabLength]).replace('\0', ' ');
				s = s.replace("::", ":" + spaces);
			}
		}
		return s;
	}

	public static String leftAlign(String s, int length){
		if (s.contains("::")){
			int totalLength = s.split("::")[0].length();
			if (totalLength < length){
				int tabLength = length - (totalLength-1);
				String spaces = new String(new char[tabLength]).replace('\0', ' ');
				s = s.replace("::", ":" + spaces);
			}
		}
		return s;
	}

	public static String getDateTime(){
		//Might have problems with multithreading?
		DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	public static ZonedDateTime getDateTimeEST(){
		return ZonedDateTime.now(ZoneId.of("America/New_York"));
	}
	
	public static LocalDateTime getDateTimeLocal(){
		return LocalDateTime.now();
	}
	
	public static String getStringFromDate(ZonedDateTime date){
		return DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss").format(date);
	}
	
	public static String getStringFromDate(LocalDateTime date){
		return DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss").format(date);
	}
	
	public static String getDateTimeESTAsString(){
		return getStringFromDate(getDateTimeEST());
	}
	
	public static String getDateTimeLocalAsString(){
		return getStringFromDate(getDateTimeLocal());
	}

	public static String getDateTimeForFileName(){
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
	}
	
	public static Date getDateFromString(String date, String format){
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String getDateString(Date date, String format){
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		
		return formatter.format(date);
	}
	/***
	 * This method will convert the current date timezone to EST timezone
	 * @param date
	 * @return Date and time in EST format
	 */
	public static String getEstDateTime(Date date){
    	SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    	dateFormat.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
    	String estTime= dateFormat.format(date);
    	return estTime;
    }
	

	public static String getTimeFromMillis(long millis){
		long second = (millis / 1000) % 60;
		long minute = (millis / (1000 * 60)) % 60;
		long hour = (millis / (1000 * 60 * 60)) % 24;

		return String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
	}

	public static String getMachineName(){
		try {
			String computername = InetAddress.getLocalHost().getHostName();
			return computername;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Unknown";
	}



	public static String sQuote(String s){
		return "'" + s + "'";
	}

	/**
	 * Same as System.out.println, just shorter.
	 * 
	 * @param s - The string to print.
	 */
	public static void print(String s){
		//Special character which will make output cleaner.
		s = fullAlign(s, maxPrintLength);

		System.out.println(s);
	}

	public static void print(String[] s){
		for (int i = 0; i < s.length; i ++){
			print(s[i]);
		}
	}

	public static void print(List<String> s){
		for (int i = 0; i < s.size(); i ++){
			print(s.get(i));
		}
	}
	
	public static void print(Table t){
		StringBuilder sb = new StringBuilder();
		for (String column : t.getColumns()){
			sb.append(column + " ");
		}
		sb.append("\n");
		
		for (Record r : t.getRecords()){
			for (String column : t.getColumns()){
				sb.append(r.getValue(column) + " ");
			}
			sb.append("\n");			
		}
		print(sb.toString());
	}

	public static void print(int s){
		print(s);
	}

	public static void print(double s){
		print(s);
	}

	/**
	 * Prints to console only if Config.debug is set to true.
	 * 
	 * @param s - The string to print.
	 */
	public static void printDebug(String s){
		return;
//		if (!Config.debug) {
//			return;
//		}
//		print(s);
	}
	
	public static <T> String collectionToString(Collection<T> c, String delimiter){
		StringBuilder sb = new StringBuilder();
		String prefix = "";
		for (T item : c){
			sb.append(prefix + item.toString());
			prefix = delimiter;
		}
		return sb.toString();
	}
	
	public static <T> String collectionToString(T[] c, String delimiter){
		StringBuilder sb = new StringBuilder();
		String prefix = "";
		for (T item : c){
			sb.append(prefix + item.toString());
			prefix = delimiter;
		}
		return sb.toString();
	}

	/**
	 * Pauses thread for specified number of seconds.
	 * 
	 * @param seconds - Number of seconds to wait.
	 */
	public static void sleep(double seconds){
		try {
			Thread.sleep((long)(seconds*1000));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String quote(String inputText) {
		return "\"" + inputText + "\"";
	}

	public static String quote(int inputText) {
		return "\"" + inputText + "\"";
	}

	public static boolean isAffirmative(String input, boolean valueIfNull){
		if (input == null){
			return valueIfNull;
		}

		switch(input.toLowerCase()){
		case "yes":
		case "true":
		case "y":
		case "on":
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isAffirmative(String input){
		return isAffirmative(input, false);
	}
	
	public static boolean areAnyAffirmative(String... inputs){
		for (String input : inputs){
			if (isAffirmative(input)){
				return true;
			}
		}
		return false;
	}

	public static String getSystemProperty(String propertyName, String valueIfNull){
		String prop = System.getProperty(propertyName.toLowerCase());
//				Config.getProp(propertyName.toLowerCase());
		return prop == null ? valueIfNull : prop;
	}

	public static String addSlashIfNone(String path) {
		return path.endsWith("/") ? path : path + "/";
	}

	public static Map<String, String> argsToMap(String[] args){
		Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for (String arg : args){
			if (arg.contains("=")){
				String[] argSplit = splitTrim(arg, "=");
				String key = argSplit[0];
				String value = argSplit[1];
				map.put(key, value);
			}else{
				map.put(arg, "true");
			}
		}
		return map;
	}

	public static Object getFirstValueFromMap(Map<?, ?> map, Object... keys){
		for (Object key : keys){
			if (map.containsKey(key)){
				return map.get(key);
			}
		}
		return null;
	}

	public static Map<String, String> arraysToMap(String[] keys, String[] values) {
		if (keys.length != values.length){
			print("Number of keys does not match number of values");
			return null;
		}
		
		Map<String, String> returnValues = new HashMap<>();
		for (int i = 0; i < keys.length; i++){
			returnValues.put(keys[i], values[i]);
		}
		return returnValues;
	}
	
	private static long timer;
	
	public static void startTimer(){
		timer = System.currentTimeMillis();
	}
	
	public static void stopTimer(){
		print("Timer: " + (timer - System.currentTimeMillis()));
	}
	
	public static <T> Set<T> listToSet(List<T> list){
		Set<T> set = new HashSet<>();
		for (T t : list){
			set.add(t);
		}
		return set;
	}
	
	public static void zipFolder(String sourceDirectory, String targetFilePath){
		zipFolder(new File(sourceDirectory), targetFilePath);
	}
	
	public static void zipFolder(File sourceDirectory, String targetFilePath){
		//ZipUtil.pack(sourceDirectory, new File(targetFilePath));
	}
	
	public static String findRegEx(String string, String pattern, int index){
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(string);
		if (m.matches()){
			return m.group(index + 1);
		}
		return "Unable to find group";
	}
	
	/**
	 * Processes keywords used in the test data sheet
	 * @param inputStr Keyword used in the test data sheet. Example - {UTC5D}
	 * @return UTC date format in String
	 */
	public static String processKeywords(String inputStr){
		//Example - /transactions?start_datetime=2016-01-19T00:00:00Z&end_datetime={UTC5D}
		Pattern regexStr1 = Pattern.compile("\\{UTC([^}]*)\\}");
		Matcher m1 = regexStr1.matcher(inputStr);
		while(m1.find()){
			String strKey = m1.group(0);
			inputStr = (inputStr.replace(strKey,getUTCdate(strKey))).replace("+T","T");
		}
		
		Pattern regexStr2 = Pattern.compile("\\{TODAY([^}]*)\\}");//("\\{([^}]*)\\}");
		Matcher m2 = regexStr2.matcher(inputStr);
		while(m2.find()){
			String strKey = m2.group(0);
			inputStr = (inputStr.replace(strKey,getUTCdate(strKey)).replace("+T","T"));
		}

		
		/*
		 * Get UTC dates based on keyword used in the test sheet.
		 * Example {UTC1D}
		 */
		if(inputStr.contains("{{UTC}}")||inputStr.contains("TODAY")){
			inputStr = Utils.getUTCdate(inputStr);
			
		}
		
		/*
		 * Generate randam URL
		 */
		if(inputStr.contains("RANDOMURL")){
			inputStr = Utils.getRandomStr(inputStr.replace("}}", "8N}}"));
		}
		
		if(inputStr.contains("RANDOM")){
			inputStr = Utils.getRandomStr(inputStr);
		}
		
		//Get local machine IP Address
		if(inputStr.contains("{{IPADDRESS}}")){
			inputStr = Utils.getIPAddress();
		}
		
		//UUID.randomUUID().toString()
		if(inputStr.contains("{{$guid}}")){
			inputStr = UUID.randomUUID().toString();			
		}
		
		
		//System.out.println("Output***: "+inputStr);
		return inputStr.replace("{", "").replace("}", "");
	}
	
	
	
	/**
	 * Extracts keyword used in the test data sheet
	 * @return keyword used based  on param passed
	 * @param sample keyword used in the test sheet
	 */
	
	public void extractKeyword(){
		String keyStr = "{fly|testing#1{testingfly{{UTC30D}testing{TODAY30}tet}}}}sflkjsdf}}";
		
		System.out.println(keyStr.substring(keyStr.indexOf("{UTC"),2));
		System.out.println(keyStr.substring(keyStr.indexOf("{TODAY")+1));
		
	
	}
	
	
	/**
	 * Compute UTC date based on keyword used in the test data sheet
	 * @return current date in the UTC format
	 * Example keyword format - {UTC1D}
	 * Example keyword format - {TODAY5D}
	 **/
	public static String getUTCdate(String dateKey){

		int num = 0;
		SimpleDateFormat sdf = null;
		String dateKeyword = dateKey;
		
		TimeZone tz = TimeZone.getTimeZone("UTC");
		Boolean flg = false;
		if(dateKey.contains("+")){
			dateKeyword = dateKey.split("\\+")[0];
			flg=true;
		}
		
		
		if (dateKeyword.contains("UTC")){
			num = Integer.parseInt(dateKeyword.replace("{{UTC", "").replace("CD}}", "").replace("D}}", "").replace("{UTC", "").replace("D}", "").replace("C", "").replace("D", "").replace("-", ""));
			sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(tz);
		}
		else if(dateKeyword.contains("TODAYX")){
			num = Integer.parseInt(dateKeyword.replace("{{TODAYX", "").replace("CD}}", "").replace("D}}", "").replace("{TODAYX", "").replace("D}", "").replace("C", "").replace("D", "").replace("-", ""));
			sdf = new SimpleDateFormat("yyyyMMdd");
		}
		else if(dateKeyword.contains("TODAY")){
			num = Integer.parseInt(dateKeyword.replace("{{TODAY", "").replace("CD}}", "").replace("D}}", "").replace("{TODAY", "").replace("D}", "").replace("C", "").replace("D", "").replace("-", ""));
			sdf = new SimpleDateFormat("yyyy-MM-dd");
		}
		
		
		
		//System.out.println("Timezone*** "+tz.toString());
		Calendar c = Calendar.getInstance();
		Date today = new Date();		
		c.setTime(today);
		c.set(Calendar.HOUR_OF_DAY,c.get(Calendar.HOUR_OF_DAY));
		c.add(Calendar.SECOND, 15);
		c.set(Calendar.MINUTE,c.get(Calendar.MINUTE));
		c.set(Calendar.SECOND,c.get(Calendar.SECOND));
		c.set(Calendar.MILLISECOND,c.get(Calendar.MILLISECOND));
	
		int ctr=num;
		int addDay=1;
		int wDays = 5;
		if(dateKeyword.contains("-")){
			addDay=-1;
		}
		
		if(dateKeyword.contains("CD")){
			wDays = 7;
		}
		
		for(int i=0;i<ctr;)
		    {
		        
		        if(c.get(Calendar.DAY_OF_WEEK)>wDays)
		        {
		        	c.add(Calendar.DAY_OF_MONTH, addDay);
		        }
		        else{
		        	c.add(Calendar.DAY_OF_MONTH, addDay);
		        	i++;
		        }

		    }
		
		String date = null;
		try{
			date = sdf.format(c.getTime());
			if (flg==true){
				date = date+"T"+dateKey.split("\\+T")[1];
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return date;
	}
	

	/**
	 * Generates random values based on strKeyword passed
	 * @param strKeyword
	 * @return Generated random value
	 */
	public static String getRandomStr(String strKeyword){
		/**Example format - {{RANDOM12S}}, {{RANDOMURL}}**/
		
		int num = Integer.parseInt(strKeyword.replaceAll(".*RANDOM", "").replace("URL", "").replace("S}}", "").replace("N}}", "").replace("{RANDOM", "").replace("S}", "").replace("N}", "").trim());
		String strVal=strKeyword.replaceAll("\\{\\{RANDOM.*", "");
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        
		if(strKeyword.contains("URL")){
			strVal = "https://test."+RandomStringUtils.random(num, "109pw")+dateFormat.format(date)+".wellsfargo.com";
			}
		else if(strKeyword.contains("S")){
			strVal = strVal+RandomStringUtils.random(num, "1027872Wn9P2");
			}
		
		else {
			strVal = strVal+RandomStringUtils.random(num, "102787228922");
			}
		
		return strVal;
	}
	
	/**
	 * 
	 * @param strKeyword keyword supplied to be part of the user id
	 * @return Random user ID
	 * Example - {{someText$RANUSERID}}
	 */
	public static String getRandomUserID(String strKeyword){
		String strInput = strKeyword.split("\\$")[0].replace("{{", "");
		strInput = strInput+"-"+RandomStringUtils.random(8, "1027872Wn9P2");
		return strInput;
	}
	
	/**
	 * 
	 * @return Random email address
	 * Example - {{RANEMAIL}}
	 */
	public static String getRandomEmail(){
		String randEmail = RandomStringUtils.random(10, "a12defghi")+"@"+RandomStringUtils.random(6, "abcdehi")+".com";
		return randEmail;
	}
	
	
	/**
	 * Deletes files in a folder
	 * @param filePath
	 */
	public static void deleteFolderContent(String filePath){
		File fileNames = new File(filePath);
		String[]entries = fileNames.list();
		if(entries!=null)
			for(String s: entries){
			    File currentFile = new File(fileNames.getPath(),s);
			    currentFile.delete();
			}
	}
	

	/**
	 * List all directories in a folder
	 * @param folderPath
	 */
	public static void delteAllFolderContents(String folderPath){
		File file = new File(folderPath);
		String[] directories = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		if(directories!=null){
		for (String dir:directories){
			deleteFolderContent(folderPath+"/"+dir);
		}
		deleteFolderContent(folderPath);
		}
		
	}
	
	/**
	 * Delete Past folder content i.e. any files not of the current date
	 * @param filePath
	 * @throws ParseException
	 */
	public static void deletePastFolderContent(String filePath) throws ParseException{
		File fileNames = new File(filePath);
		String[]entries = fileNames.list();
		long days = Integer.parseInt(Config.getProp("daysToRetainPDFs"));
		
		if(entries!=null)
			for(String s: entries){
			    File currentFile = new File(fileNames.getPath(),s);
			    long diff = new Date().getTime() - currentFile.lastModified();
			    
				
			    if (diff > days * 24 * 60 * 60 * 1000) {
			    	currentFile.delete();
			    }
			    
			}
	}
	
	public static String readRespBody(String currentPath, JsonNode jsonNode, String expKey, String whereField, String whereVal, int itr) throws JsonProcessingException, IOException{
		
		expKey=expKey.replace("$[", "[").replace("]$", "]");

		
		if(expKey != null && !expKey.isEmpty())
		  {
			  if (jsonNode.isObject()) {
			      ObjectNode objectNode = (ObjectNode) jsonNode;			      
			      Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
			      String pathPrefix = currentPath.isEmpty() ? "" : currentPath;
			      //System.out.println("Path1->"+pathPrefix);
			      
			      
			      String lookupID = "";
			      while (iter.hasNext()) {		    	  
			        Map.Entry<String, JsonNode> entry = iter.next(); 
			        
			        String actKey = pathPrefix+entry.getKey();
			        String actVal = entry.getValue().toString();
			        
			        //System.out.println("ExpKey: "+expKey+":"+actKey+"#"+actVal);
					if(!whereField.equals("")){
						
						expKey = expKey.replaceAll("\\[.*?\\]","["+itr +"]");
						//System.out.println("ExpKey: "+expKey+":"+actKey+"#"+actVal);
						}
					
			        String key = null;
			        if(entry.getValue().isValueNode())
			        {
			        	//System.out.println("****lookupID: "+lookupID);
			        	key = entry.getKey();
			        	if(whereVal.length()>1)	
			        	{
			        		
			        		if(expKey.contains(entry.getKey())){
			        			lookupID = actVal.replace("\"", "");		
			        			
			        		}
			        		
			        		
			        		if(actKey.contains(whereField) && whereVal.equalsIgnoreCase("ANY")){
			        			whereVal = actVal;
			        			
			        		}
			        		
			        		if(actKey.contains(whereField) && actVal.contains(whereVal) && !flgWhereValFound){
			        			//System.out.println("***Entry Key: "+entry.getKey()+"***actVal: "+actVal+"***whereVal: "+whereVal + "***whereField: "+whereField);
			        			flgWhereValFound = true;
			        			respMap.put("whereField", entry.getKey());	
			        			respMap.put("whereVal", actVal);
			        			respMap.put("key", lookupID);			        			
			        			return lookupID;
			        			
			        		}
			        		
			        		/**
			        		 * Filter data by date
			        		 */
			        		if(actKey.contains(whereField)  && !flgWhereValFound && whereField.contains("date")){
			        			Date whereDate = Utils.strToDate("", whereVal.replace(">", ""));
			        			Date actDate  = Utils.strToDate("", actVal.replace("\"", ""));
			        			//System.out.println("***Entry Key: "+entry.getKey()+"***actVal: "+actVal+"***whereVal: "+whereVal + "***whereField: "+whereField);
			        			
			        			if(actDate.after(whereDate)){
			        				flgWhereValFound = true;
				        			respMap.put("whereField", entry.getKey());	
				        			respMap.put("whereVal", actVal);
				        			respMap.put("key", lookupID);			        			
				        			return lookupID;
			        			}
			        			
			        			
			        		}
			        		
		        		}
		        		else if((actKey.replace("$[", "[")).equalsIgnoreCase(expKey) && whereVal.equals(""))
			        	{
			        			return actVal;
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
							
			        		actValStr = readRespBody(pathPrefix + entry.getKey()+"$", entry.getValue(), expKey,whereField,whereVal,itr).replace("\\", "\\\\");
						
						} catch (Exception e) {
							//e.printStackTrace();
							
						}
			        	
			        }
			        
			      }
			    } else if (jsonNode.isArray() && !jsonNode.toString().contains(":")){
			    		 }
			    	 		   else if (jsonNode.isArray()) {
			    	 			   		ArrayNode arrayNode = (ArrayNode) jsonNode;
			    	 			   		itr = Utils.loopFlg?item_count++:0;
									    for (int i = itr; i < arrayNode.size(); i++) {
									    	  
									    	  if(expKey.equalsIgnoreCase(currentPath+"["+i+"]")){
									    		  actValStr=arrayNode.get(i).toString();
									    	  }
									    	  else{
									    		  
									    		  actValStr = readRespBody(currentPath + "[" + (i+0) + "]", arrayNode.get(i), expKey,whereField, whereVal, i);
									    	  }									    	 
									      }
						    }			  
			 
		   
		  }
		
		return actValStr==null?null:actValStr.replace("\"", "");
		
	}
	
	/**
	 * Log Test Summary to Console
	 */
	public static void logSummaryConsole(){
		if(CustomLogger.totalFail>0){
			System.err.println("\n"+Utils.getDateTime()+" *****TEST EXECUTION SUMMARY*****");
			System.err.println("=====================================================");
			System.err.println("     Total Executed: "+TestManager.totalTC);
			System.err.println("     Total Passed: "+CustomLogger.totalPass);
			System.err.println("     Total Failed: "+CustomLogger.totalFail);				
			System.err.println("     Pass %: "+(CustomLogger.totalPass*100)/TestManager.totalTC+"%");
			System.err.println("=====================================================");
			
		}
		else{
			System.out.println("\n"+Utils.getDateTime()+" *****TEST EXECUTION SUMMARY*****");
			System.out.println("=====================================================");
			System.out.println("     Total Executed: "+TestManager.totalTC);				
			System.out.println("     Total Passed: "+CustomLogger.totalPass);
			System.out.println("     Total Failed: "+CustomLogger.totalFail);
			System.out.println("     Pass %: "+(CustomLogger.totalPass*100)/TestManager.totalTC+"%");
			System.out.println("=====================================================");
			
		}
	}
   
	/**
	 * Create PDF report
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static void createPDF() throws DocumentException, IOException
    {
      
        String filePath = Configurables.uploadDirectory;
        String fileName = "overview.html";
        stripURL(filePath,fileName);
        stripInvalidTags(filePath, "output.html");
		String HTML = filePath+"/"+"PDF_"+fileName;		
        File PDF = new File("reports/pdf");
        FileUtils.forceMkdir(PDF);
               
		Document document = new Document();        
//		String ts = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
//		pdfFileName = "API_Test_Result_"+Config.getProp("Environment")+"_"+Config.getProp("tagName")+"_"+ts+".pdf";
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(PDF+"/"+pdfFileName));
        writer.setInitialLeading(10);        
        document.setPageSize(PageSize.B4);        
        document.open();
        
                
        
        try {
			XMLWorkerHelper.getInstance().parseXHtml(writer, document, 
        			new FileInputStream(HTML), new FileInputStream("src/test/resources/reporting/pdf.css"));
        	
        	/*
        	 * Append Log Output to report
        	 */
        	HTML = filePath+"/"+"PDF_output.html";
        	
        	if(Boolean.parseBoolean(Config.getProp("LogOutputToPDFReport"))){
        			XMLWorkerHelper.getInstance().parseXHtml(writer, document, 
        					new FileInputStream(HTML), new FileInputStream("src/test/resources/reporting/pdf.css"));
        	}
			pdfReport = true;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error creating PDF report... "+e.getMessage()+"\nRemove \"<...>\" from the TC description.");
			pdfReport = false;
		}
        	    
        document.close();
 
    }
	

	
	//Remove URL from Text
	public static File stripURL(String filePath, String fileName) throws IOException
    {
		File htmlFile = new File(filePath+"/"+fileName);
		String htmlText = FileUtils.readFileToString(htmlFile, Charset.defaultCharset());
		
		File newFile = new File(filePath+"/"+"PDF_"+fileName);
		
		//System.out.println("Debug*** "+htmlText);
		String urlPattern = "<a.*suite.*_test1.*?>";
		Pattern p = Pattern.compile(urlPattern);
        Matcher m = p.matcher(htmlText);
        
        htmlText = htmlText.replaceAll("Generated.*", "Report generated in "+Config.getProp("Environment")+" (with tagName = "
        		+ Config.getProp("tagName")+" and branch = "+Config.getProp("Branch")+")");
        int i = 1;
        while (m.find()) {
        	htmlText = htmlText.replaceAll(m.group(0),"<a href=\"#"+i+"\">").trim();
        	i++;
        	p = Pattern.compile("<a.*suite.*_test"+i+".*?>");
        	m = p.matcher(htmlText);
        	
        	
        }
        
        
        //Clean Up
        htmlText = htmlText.replace("Previous", "");
        htmlText = htmlText.replace("Latest", "");
        FileUtils.writeStringToFile(newFile,htmlText,Charset.defaultCharset(), false);
        return newFile;
    }
	
	/*
	 * Remove Invalid Tags
	 */
	public static File stripInvalidTags(String filePath, String fileName) throws IOException
    {
		File htmlFile = new File(filePath+"/"+fileName);
		String htmlText = FileUtils.readFileToString(htmlFile, Charset.defaultCharset());
		
		File newFile = new File(filePath+"/"+"PDF_"+fileName);
		
		
        /*
         * Format Output Logs
         */
		htmlText = htmlText.replaceAll("Bearer.*?br>", "Bearer ******<br>");
		htmlText = htmlText.replaceAll("END-USER-AUTHORIZATION.*?br>", "END-USER-AUTHORIZATION: *****<br>");
		htmlText = htmlText.replace("<br>", "<br />");
        htmlText = htmlText.replace("<p><p", "</p><p");
        htmlText = htmlText.replace("<p></p>", "</p></p>");
        htmlText = htmlText.replace("<head>", "<div class=\"pagebreak\"> </div><head>");
        htmlText = htmlText.replace("<summary>", "<span>").replace("</summary>", "</span><br />");
        htmlText = htmlText.replace("<details>", "").replace("</details>", "").replace("<details open>", "");
        
        String urlPattern = "</pre>|<pre>"; //<p>|</p>|<p.*?>|
        Pattern p = Pattern.compile(urlPattern);
        Matcher m = p.matcher(htmlText);
        int i = 0;
        while (m.find()) {
        	htmlText = htmlText.replaceAll(m.group(0),"").trim();
            i++;
        }
        
        

        
        
        FileUtils.writeStringToFile(newFile,htmlText,Charset.defaultCharset(), false);
        return newFile;
    }
	
	public static String executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = 
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}

	
	
	public static String totalTime(long millis){
		return String.format("%02d min: %02d sec", 
			    TimeUnit.MILLISECONDS.toMinutes(millis),
			    TimeUnit.MILLISECONDS.toSeconds(millis) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
			);
		
	}
	
	//@Test
    public static Boolean validateRegEx(String egStr, String ptrnStr) {
        Pattern pattern = Pattern.compile(ptrnStr);
        Boolean matchFlg = false;
        System.out.println("***Input String: "+egStr);
        System.out.println("***Test pattern: "+ptrnStr);
        
        Matcher matcher = pattern.matcher(egStr);
        
        while (matcher.find()) {
            matchFlg = true;
        }

        if(matchFlg)
        	System.out.println("Pattern validation passed.");
        else
        	System.out.println("Pattern validation failed.");
        
        return matchFlg;
	}
    

    /**
     * Get chained request data from response header based on input in test data sheet
     * @param qParam
     * @param url
     * @return
     * @throws IOException
     * Example - search=id¦#header_1_1:Location¦IN
     */
	public static String getChainedRequestDataHeader(String[] qParam, String url) throws IOException{
		if (Arrays.toString(qParam).length()<2)
				return "";
		String updatedParams = "";
		String fileName;
		for (String key:qParam){
			//System.out.println("Key: "+key);
			if (key.startsWith("#header")){
				String field = key.split(":")[1].trim();
				fileName = key.split(":")[0].replace("[", "").replace("]","").replace("#header_", "") + "_header.json".trim(); 
				key = readFile(fileName, field);
				
			}
			
				updatedParams = updatedParams+"¦"+key;
			
		}
		
		//System.out.println("Debug*** "+updatedParams);
		return updatedParams.replace("¦?", "?");
		
	 }
	
	/*
	 * Get chained request data from response header based on input in test data sheet
	 */
	public static String getChainedRequestDataHeader(String qParam) throws IOException{
		//Example - search=id¦#header_1_1:Location¦IN
		//System.out.println("***Query Param*** "+qParam.length);
		
		if (qParam=="")
				return "";
		String updatedParams = "";
		
		try{
			String fileName;
				if (qParam.startsWith("#header")){
					String field = qParam.split(":")[1].trim();
					fileName = qParam.split(":")[0].replace("[", "").replace("]","").replace("#header_", "") + "_header.json".trim(); 
					qParam = readFile(fileName, field);
					
				}
				
			updatedParams = qParam;
		}
		catch(Exception e){
			System.err.println("Error reading file. Check "
					+ "the value passed for this parameter. \n"+e.getMessage());
		}
		return updatedParams.replace("¦?", "?");
		
	 }
	
	
	/*
	 * Get chained request data from response body based on input in test data sheet
	 */
	public static String getChainedRequestDataBody(String[] qParam, String url) throws IOException{

		if (Arrays.toString(qParam).length()<2)
			return "";
		String updatedParams = "";
		String fileName;
		for (String key:qParam){
			//System.out.println("Key: "+key);
			if (key.startsWith("#body")){
				String field = key.split(":")[1].trim();
				fileName = key.split(":")[0].replace("[", "").replace("]","").replace("#body_", "") + ".json".trim(); 
				key = readResponseBody(fileName, field);
				
			}
			
				updatedParams = updatedParams+"¦"+key;
			
		}
		
		//System.out.println("Debug*** "+updatedParams);
		return updatedParams.replace("¦?", "?");
		
	 }
	
	/**
	 * 
	 * @param strWhere - Data to be parsed.
	 * @key Key to parse data with.
	 * @return data as array parsed by keyword passed.
	 */
	public static String[] convertToArray(String strWhere, String key){
		return null;
	}
	
	/**
	 * Get input data from a previous response body incuding data for header params or resource path
	 * @param keyStr Input values passed based on which the chained request data will be retrieved.
	 * @return Requested data from a previous response body.
	 * @throws IOException
	 */
	public static String getChainedRequestDataBody(String keyStr) throws IOException{

		if (keyStr=="")
				return "";
		String updatedkeyStr = "";
		String whereField="";
		String whereVal ="";
		
		
		String fileName;
			if (keyStr.startsWith("#body")){
				String field = keyStr.split(":")[1].trim();
				
				if(keyStr.contains("WHERE")){
					whereField = field.split("WHERE")[1].split("#")[0].replace("$", "");
					whereVal = field.split("WHERE")[1].split("#")[1];
					field = field.split("WHERE")[0].replace("$", "");
							
				}
				
				fileName = keyStr.split(":")[0].replace("[", "").replace("]","").replace("#body_", "") + ".json".trim(); 
				whereVal = Utils.processKeywords(whereVal);
				keyStr = readResponseBody(fileName, field, whereField, whereVal);
				
				//System.out.println("keyStr:"+keyStr+", field**** "+field+", whereField: "+whereField+", whereVal:"+whereVal);
				
			}
			
			if (Utils.flgWhereValFound || whereField.equals("")){
				updatedkeyStr = keyStr;
			}
			else{
				updatedkeyStr = null;
			}
			
		return updatedkeyStr.replace("¦?", "?");
		
	 }
	
	/*
	 * Get data from response file
	 */
	@SuppressWarnings("deprecation")
	public static String readFile(String filename, String key) throws IOException{
		File jsonFile = new File("reports/json/response/"+filename);
		String xmlString[] = FileUtils.readFileToString(jsonFile, Charset.defaultCharset()).split(",");
		String keyData="";
		for(String data:xmlString){
			//System.out.println("Data: "+data);
			if(data.split(":")[0].trim().equals(key)){
				keyData = data.split(":")[1].trim().replace("]", "").replace("[", "");
			}
		}
		//System.out.println("Data fetched: "+keyData);
		
		return keyData;
	}
    
	public static String readResponseBody(String filename, String expkey) throws IOException{
		File jsonFile = new File("reports/json/response/"+filename);
		JsonNode jsonNode = null;
		boolean fileBodyExist=false;
		if (jsonFile.exists()){
			jsonNode = new ObjectMapper().readTree(jsonFile);
			fileBodyExist = true;
		}
		
		String keyData = fileBodyExist?Utils.readRespBody("",jsonNode,expkey, "", "",0):"Value not Found!!";
		
		return keyData;
	}
	
	public static String readResponseBody(String filename, String expkey, String whereField, String whereVal) throws IOException{
		File jsonFile = new File("reports/json/response/"+filename);
		JsonNode jsonNode = null;
		boolean fileBodyExist=false;
		if (jsonFile.exists()){
			jsonNode = new ObjectMapper().readTree(jsonFile);
			fileBodyExist = true;
		}
		
		String keyData = fileBodyExist?Utils.readRespBody("",jsonNode,expkey, whereField, whereVal,0):"Value not Found!!";
		//System.out.println("****whereField "+whereField+" ***whereVal "+whereVal + "**keyData: "+keyData);
		
		return keyData;
	}
	
	public static int processDataType(String str){
		//Integer.to
		return 1;
	}
	
	/**
	 * 
	 * @param authToken
	 * @return Env specific Auth Token
	 */
	public static String processAuthToken(String authToken){
		String temp[] = authToken.split(",");
		String env = TestManager.envName;
		String finalToken = "";
		//System.out.println("debug*** "+Arrays.toString(temp));
		
		for(String tmp:temp){
			if(tmp.contains(env)){
				finalToken = tmp.split("#")[1];
			}
		}
		return finalToken.trim();
	}
	
	public static String getConfigValue(String key){
		Properties prop = new Properties();
		String propFile = "config.properties";
		java.io.InputStream inputStr = FileHelper.class.getResourceAsStream(propFile);
		if(inputStr!=null)
		{
			try {
				prop.load(inputStr);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		//System.out.println("Test Config*** "+prop.getProperty(key));
		return (String) prop.getProperty(key);
		
		
	}
	
	
	/*
	 * Process special chars
	 */
	public static String processSpecialChars(String jsonObject){

		System.out.println("***Request***\n"+jsonObject);
		if(jsonObject.contains("\\u003")){
			System.out.println("*****proessing special chars...");
			jsonObject = jsonObject.replace("\\u003c", "<");
			jsonObject = jsonObject.replace("\\u003e", ">");
			jsonObject = jsonObject.replace("\\u003d", "=");
			System.out.println("***Processed***\n"+jsonObject);
		}
		return jsonObject;
	
	}

	/**
	 * 
	 * @return local IP address
	 */
	public static String getIPAddress(){
		
		String ip = null;
		try{
			InetAddress inet = InetAddress.getLocalHost();
			ip = inet.getHostAddress();
			
		}
		
		catch(Exception e){
			e.printStackTrace();
		}
		return ip;
	}
	
	/**
	 * Converts dates in String to Date format
	 * @param date_format
	 * @param strDate
	 * @return Date type
	 */
	public static Date strToDate(String date_format, String strDate){
		Date date = null;
		if(date_format.equals("")){
			date_format = "yyyy-MM-dd'T'HH:mm:ss'Z'";
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(date_format);
		try {
			date = sdf.parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//System.out.println("Date: "+date.toString());
		return date;
	}
	

	
	/**
	 * Generates the index.html report in the .xls format
	 * @param file  - name of the file with path
	 */
	public static void htmlToXls(String file) {

	     BufferedReader br = null;
	     try {
	        br = new BufferedReader(new FileReader(new File(file)));
	          XSSFWorkbook xwork = new XSSFWorkbook();
	          XSSFSheet xsheet = xwork.createSheet("Test Summary");
	          XSSFRow xrow  = null;
	          int rowid =0;
	          String line ;
	          while (( line =br.readLine())!= null) {
	             String split[] = line.split("<br>");
	             Cell cell;
	             for (int i = 0; i < split.length; i++) {
	                 xrow = xsheet.createRow(rowid);
	                 cell = xrow.createCell(2);
	                 cell.setCellValue(split[i]);
	                 String[] columnSplit = split[i].split("\\W+");
	                 int columnCount = 3;
	                 for (int j = 0; j < columnSplit.length; j++) {
	                     cell = xrow.createCell(columnCount++);
	                     cell.setCellValue(columnSplit[j]);
	                }
	                rowid++;
	            }
	          } 

	            FileOutputStream fout = new FileOutputStream(new File("reports/test_summmary.xls"));
	            xwork.write(fout);
	            fout.close();
	            xlsReport = true;
	     }
	     catch (Exception e) {
	        e.printStackTrace();
	    }
	  }
	

    
	    /**
	     * Loads properties from config.properties
	     */
	    public static void loadProps(){
			IConfigurer textConfig = new TextConfigurer("config.properties");
			Map<String, String> configMap = textConfig.getConfigMap();
			Config.addProps(configMap);
	    }
	    
	    /**
	     * Upload requirement details to ALM
	     * @throws Exception 
	     */
	    public static void updateReqALM() throws Exception{
	    	
	    	System.out.println(Utils.getDateTime()+" Uploading Requirements to ALM... \n");
	    	Client client = new Client(new com.javaexcel.automation.alm.Config("alm.properties"));
	    	client.login();
	    	for (Record rec : Configurables.globalReqsTable.getRecords()){
	    		//System.out.println("\nReq:*** "+ rec);
	    		client.createRequirement(rec);
	    	}
	    	client.logout();
	    	System.out.println(Utils.getDateTime() + " ...Requirements uploaded successfully.");
	    	
	    }
	    
	    /**
	     * Upload test results to ALM
	     * @throws Exception
	     */
	    public static void uploadToALM() {
	    	if(Config.getProp("uploadToALM").equalsIgnoreCase("Yes")){
	    		//System.out.println("\n"+Utils.getDateTime()+" Uploading Requirements to ALM...");
	    		//Utils.updateReqALM();
	    		System.out.println("\n"+Utils.getDateTime()+" Uploading JUnit test results to ALM...");
	    		try {
					XmlParser.loadXMLs();
				} catch (Exception e) {
					System.err.println(Utils.getDateTime()+" Failed to parse XMLs... error returned: "+e.getMessage());
					
				}
	    		
	    		try{
			    	Client client = new Client(new com.javaexcel.automation.alm.Config("alm.properties"));
			    	client.login();
			    	System.out.println(Utils.getDateTime()+" Publishing results to ALM...");
			    	
			    	for (int i=1;i<=XmlParser.allTestMap.size();i++){
				    	Test test = client.createTest(i);
				    	TestSet testSet = client.createTestSet(i);
				    	TestInstance testInstance = client.createTestInstance(test, testSet);
				    	Run run = client.createRun(testInstance, test, i);
			    	}
		
			    	
			    	client.logout();
			    	System.out.println(Utils.getDateTime()+" Test results successfully published to ALM.");
	    		}
	    		catch(Exception e){
	    			System.err.println(Utils.getDateTime()+" Failed to upload to ALM... \n"+e.getMessage()+"\n\nPlease verify the login credentials including the ALM attributes in the alm.properties file. Aborting task.");
	    		}
		    }
	    }
	    
	    /**
	     * 
	     * @param username
	     * @param password
	     * @return Base64 encoded String
	     */
	    public static String encodeBase64(String username, String password){
	    	String originalInput = username+":"+password;
	    	//System.out.println("64Encoded: "+Base64.getEncoder().encodeToString(originalInput.getBytes()));
	    	return Base64.getEncoder().encodeToString(originalInput.getBytes());
	    	
	    }
	    
	    /**
	     * Retrieve Env and resource details based on environment selected and resource in scope
	     */
	    public static void loadGlobalRefTbls(){
	    	Excel dataFile = new Excel(Configurables.testsFilePath+Configurables.testFileName);
	    	
	    	if(Configurables.envFlg){
				Configurables.envFlg=false;
				//System.out.println(Utils.getDateTime()+" Loading environment details... ");//Configurables.globalEnvTable);
				
				String resource_table_query ="Select * From \"" + "Resources" + "\"";
				Configurables.globalResourceTable = dataFile.querySheetRecords(resource_table_query);
				
				String env_table_query = "Select * from " + Configurables.gateway_EnvTable + " Where  "
						+ Utils.quote(Configurables.gateway_EnvironmentCoulmnName) + " = " + Utils.sQuote(Configurables.gateway_Environment);
				Configurables.globalEnvTable = dataFile.querySheetRecords(env_table_query);
				
				if (Configurables.globalEnvTable.recordCount()==0){
					System.err.println("Error loading environment data. Please review enviroment details provided in the config.properties and test sheet. \nAborting test run.");
					System.exit(1);
				}
				int i=1;
				for (Record record : Configurables.globalResourceTable.getRecords()){
					//Configurables.globalConfigTable.addRecord(Configurables.globalEnvTable.getRecord(0));
					
					record.add("ID", Integer.toString(i++));
					for(Field f : Configurables.globalEnvTable.getRecord(0).getFields()){
						record.add(f.getColumnName(), f.getValue());
					}
					Configurables.globalConfigTable.addRecord(record);
					
				}
				
			}
	    	

			
	    }
	    
	    /**
	     * Used to convert yaml file to json object
	     * @param yaml file
	     * @return JSONObject
	     * @throws IOException 
	     * @throws JsonMappingException 
	     * @throws JsonParseException 
	     * @throws org.json.simple.parser.ParseException 
	     */
	    public static JSONObject convertYamlToJson(String yamlFile) throws JsonParseException, JsonMappingException, IOException, org.json.simple.parser.ParseException {
	    	File yaml = new File(yamlFile);
			String yamlStr = FileUtils.readFileToString(yaml, Charset.defaultCharset());
	        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
	        Object obj = yamlReader.readValue(yamlStr, Object.class);
	        ObjectMapper jsonWriter = new ObjectMapper();
	        JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonWriter.writeValueAsString(obj));
	        //System.out.println("JSONObject****** "+jsonObj);
	        return jsonObj;
	        
	    }
	
	    /**
	     * Used for junit testing for encoding issues.
	     * @throws FileNotFoundException
	     * @throws IOException
	     * @throws ParseException
	     */
	    //@org.junit.Test
		public void validateAPIResponse() throws FileNotFoundException, IOException, ParseException
		{
			

			HashMap<String, String> ModifyNodeValue = new HashMap<String, String>();
			
			String responseJsonDataPath ="reports/json/response/"+"1_1"+".json";//params.get("ResponseJSON");
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
		System.out.println("Successful\n"+jsonNode.toString());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		}
	    
		public static boolean checkCaseSensitive(File file) throws IOException{
		    String fileName = file.getCanonicalPath().replaceAll(".*json-templates/.*?", "");
		    if(fileName.equals(file.getName()))
		    	return true;
		    return false;
		}
}

