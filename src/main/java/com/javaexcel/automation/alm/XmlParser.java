package com.javaexcel.automation.alm;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import com.javaexcel.automation.alm.Config;
import com.javaexcel.automation.core.utils.Utils;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class XmlParser  {
	
	public static HashMap <Integer, HashMap <String, String>> allTestMap;
	
	
	public static void loadXMLs() throws Exception {
		String folderPath = "test-output/"+com.javaexcel.automation.core.data.Config.getProp("API_Name");
		System.out.println("Junit folder: "+folderPath);
		String filePath ="";
		HashMap <String, String> testMap = new HashMap<String, String>();
		allTestMap = new HashMap<Integer, HashMap <String, String>>();
		File fileNames = new File(folderPath);
		String[]entries = fileNames.list();
		int testId=1;
		
		if(entries!=null && entries.length!=0){
			System.out.println(Utils.getDateTime()+" Parsing JUnit XMLs...");
			for(String s: entries){
			    if(!s.contains(".html")){
			    	filePath = folderPath + "/" + s;
			    	testMap = parseXML(filePath);
			    	allTestMap.put(testId++,testMap);
			    	
			    }
			}
			System.out.println(Utils.getDateTime()+" Parsing Complete. "+"Total TCs found: "+allTestMap.size());
		}
		else{
			System.err.println(Utils.getDateTime()+" Error uploading test results to ALM... aborting task.");
			System.exit(1);
		}
		

	}
	
	
	//public static void parseXML(String xmlStr, String attr, String fileName) {
	public static HashMap <String, String> parseXML(String fileName) {
		//String fileName="test-output/WFG-API-TEST/Happy Path_ Send GET request to view list of Webhooks.xml";
		String xmlStr="";
		String attr="testcase";
		String testName = null, testDesc = "", hostName=null, status=null, testSet=null, execTime=null;
		HashMap <String, String> testMap = new HashMap<String, String>();
	    try {

		File fXmlFile = new File(fileName);
		DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xmlStr));
		Document doc;
		if(xmlStr!=""){
			doc = dBuilder.parse(is);
		}
		else{
			doc = dBuilder.parse(fXmlFile);
		}
		doc.getDocumentElement().normalize();
		
		testName = fXmlFile.getName().replace(".xml", "");
		//testSet = doc.getDocumentElement().getAttribute("name");
		testSet = com.javaexcel.automation.core.data.Config.getProp("API_Name");
		hostName = doc.getDocumentElement().getAttribute("hostname");
		execTime = formatDate(doc.getDocumentElement().getAttribute("timestamp").replace("GMT", "").trim());
		
		
		if(Integer.parseInt(doc.getDocumentElement().getAttribute("failures"))>0){
			status = "Failed";
		}
		else{
			status = "Passed";
		}
				
		NodeList nList = doc.getElementsByTagName(attr);
		
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				testDesc = testDesc+"\nStep "+(temp+1)+": " + eElement.getAttribute("name");
			}
		}
	    
	    } 
	    
	    catch (Exception e) {
		e.printStackTrace();
	    }


		testMap.put("testName", StringEscapeUtils.escapeHtml(testName));
		testMap.put("testSet", StringEscapeUtils.escapeHtml(testSet));
		testMap.put("hostName", hostName);
		testMap.put("execTime", execTime);
		testMap.put("status", status);
		testMap.put("testDesc", StringEscapeUtils.escapeHtml(testDesc));
				
		return testMap;
	    
	  }
	
	public static String formatDate(String strDate){
		Date date = null;
		String date_format = "d MMM yyyy HH:mm:ss";//'T'HH:mm:ss'Z'";		
		SimpleDateFormat sdf = new SimpleDateFormat(date_format);
		try {
			date = sdf.parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		date_format = "yyyy-MM-dd";
		sdf = new SimpleDateFormat(date_format);
		return sdf.format(date);
	}

	}

