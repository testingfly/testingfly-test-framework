package com.javaexcel.automation.core.utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.ListIterator;

import com.googlecode.jatl.Html;
import com.javaexcel.automation.core.data.Config;
import com.javaexcel.automation.core.data.TestNGTestData;
import com.javaexcel.automation.core.listeners.CustomLogger;
import com.javaexcel.automation.core.testngdata.TestNGClass;
import com.javaexcel.automation.core.testngdata.TestNGMethod;
import com.javaexcel.automation.core.testngdata.TestNGSuite;
import com.javaexcel.automation.core.testngdata.TestNGTest;

public class XMLGenerator {

	public static void generateXML(final List<TestNGTestData> testCases){
		

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("testng.xml"), "utf-8"))){

			new Html(writer) {{
				raw("<?xml version='1.0' encoding='UTF-8'?>");
				raw("<!DOCTYPE suite SYSTEM 'http://testng.org/testng-1.0.dtd'>");

				start("suite").attr("name","Suite");
				start("listeners");
				//				start("listener").attr("class-name","core.CustomLogger").end();
				//				start("listener").attr("class-name","core.CustomReporter").end();
				end();

				for(TestNGTestData tc : testCases){
					for (String parameter : tc.beforeTestParameters.keySet()){
						start("parameter").attr("name",parameter,"value",tc.beforeTestParameters.get(parameter)).end();
					}
					start("test").attr("name",tc.testName);
					start("classes");
					for (int i = 0; i < tc.getNumOfClasses(); i++){
						start("class").attr("name",tc.className[i]);
						start("methods");
						for (int j = 0; j < tc.getNumOfMethods(i); j++){
							System.out.println(tc.getNumOfMethodParameters(i, j));
							for (int k = 0; k < tc.getNumOfMethodParameters(i, j); k++){
								start("parameter").attr("name",tc.methodNames[i][j] + (k+1),"value",tc.methodParameters[i][j][k]).end();
							}
							start("include").attr("name",tc.methodNames[i][j]).end();
						}
						end();
						end();
					}
					end();
					end();
				}
				endAll();
			}};

			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void generateXML2(TestNGSuite suite, String fileName, List<String> listeners){
		String[] listenerArray = new String[listeners.size()];
		listenerArray = listeners.toArray(listenerArray);
		generateXML2(suite, fileName, listenerArray);
	}

	public static void generateXML2(TestNGSuite suite, String fileName, String[] listeners){
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName + ".xml"), "utf-8"))){
			

			new Html(writer) {{
				raw("<?xml version='1.0' encoding='UTF-8'?>");
				raw("<!DOCTYPE suite SYSTEM 'http://testng.org/testng-1.0.dtd'>");

				start("suite").attr("name", suite.getName());
				String parallel = suite.getParallel();
				if (parallel != null && !parallel.isEmpty()){
					attr("parallel", parallel);
				}
				String threadCount = suite.getThreadCount();
				if (threadCount != null && !threadCount.isEmpty()){
					attr("thread-count", threadCount);
				}
				String configFailiurePolicy = suite.getConfigFailurePolicy();
				if (configFailiurePolicy != null && !configFailiurePolicy.isEmpty()){
					attr("configfailurepolicy", configFailiurePolicy);
				}
				
				start("parameter").attr("name", "apiname", "value",Config.getProp("API_Name")).end();
				start("parameter").attr("name", "pdfname", "value",Utils.pdfFileName).end();
				start("parameter").attr("name", "tagName", "value",Config.getProp("tagName")).end();
				start("parameter").attr("name", "branch", "value",Config.getProp("Branch")).end();
				start("parameter").attr("name", "alm", "value",Config.getProp("ALM_Link")).end();
				
				
				if(Config.getProp("jobName").contains("local")){
					start("parameter").attr("name", "jobname", "value","").end();
					start("parameter").attr("name", "jenkinsurl", "value",Config.getProp("jenkinsURL")+"/view/TEST").end();
				}else {
					start("parameter").attr("name", "jobname", "value",Config.getProp("jobName")).end();
					start("parameter").attr("name", "jenkinsurl", "value",Config.getProp("jenkinsURL")).end();
					start("parameter").attr("name", "build_number", "value",Config.getProp("build")).end();
					start("parameter").attr("name", "build_number_previous", "value",Integer.toString(Integer.parseInt(Config.getProp("build"))-1)).end();
					start("parameter").attr("name", "build_number_next", "value",Integer.toString(Integer.parseInt(Config.getProp("build"))+1)).end();
				}
				
				for (String parameter : suite.getParameters().keySet()){
					String paramValue = suite.getParameters().get(parameter);
					if (paramValue == null) paramValue = "Null";
					start("parameter").attr("name", parameter, "value", paramValue).end();
					
				}
				start("listeners");
				for (int i = 0; i < listeners.length; i++){
					start("listener").attr("class-name",listeners[i]).end();
				}
				end();
				int ctr = 1;
				for (TestNGTest test : suite.getTests()){
					start("test").attr("name",test.getName());
					if(ctr>1){
						start("parameter").attr("name", "previous", "value", Integer.toString(ctr-1)).end();
					}else{
						start("parameter").attr("name", "previous", "value", Integer.toString(ctr)).end();
					}

					if(ctr<suite.getTests().size()){
						start("parameter").attr("name", "next", "value", Integer.toString(++ctr)).end();
					}else{
						start("parameter").attr("name", "next", "value", Integer.toString(ctr)).end();
					}
					
					for (String parameter : test.getParameters().keySet()){
						String paramValue = test.getParameters().get(parameter);
						if (paramValue == null) paramValue = "Null";
						start("parameter").attr("name", parameter, "value", paramValue).end();
						
					}
					
					start("classes");
					for (TestNGClass cls : test.getClasses()){
						start("class").attr("name", cls.getName());
						

						if (cls.getMethods().size() > 0){
							start("methods");
							for(TestNGMethod method : cls.getMethods()){
								start("include").attr("name", method.getName());
								//start("include").attr("name", test.getParameters().get("scenario_name")+": "+method.getName());
								for (String parameter : method.getParameters().keySet()){
									String paramValue = method.getParameters().get(parameter);
									if (paramValue == null) paramValue = "Null";
									start("parameter").attr("name", parameter, "value", paramValue).end();
									//start("parameter").attr("name", parameter, "value", test.getParameters().get("scenario_name")+": "+paramValue).end();
									
								}
								end();
							}
							end();
						}

						end();
					}
					end();
					end();

				}
				endAll();
			}};

			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
