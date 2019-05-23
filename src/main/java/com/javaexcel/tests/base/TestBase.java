package com.javaexcel.tests.base;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.testng.ITest;
import org.testng.annotations.BeforeMethod;
//import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.ITestContext;
//import com.javaexcel.automation.core.data.ITestContext;
import org.testng.annotations.DataProvider;
import com.javaexcel.automation.core.data.Config;
import com.javaexcel.automation.core.data.Configurables;
import com.javaexcel.automation.core.data.MultiUserInfo;
import com.javaexcel.automation.core.execution.Main;
import com.javaexcel.automation.core.utils.TestUtils;
import com.javaexcel.automation.core.utils.Utils;

public abstract class TestBase implements ITest{
	protected static Integer threadCount = 0;
	protected MultiUserInfo multiUserInfo=new MultiUserInfo();
	protected String projectName;
	public static Map<String,Map<String,String>> configDetailsProjectWise= new HashMap<>();
	protected ThreadLocal<Integer> threadID = new ThreadLocal<Integer>();
	protected ThreadLocal<Map<String, Object>> threadMap = new ThreadLocal<Map<String, Object>>();
	private static ThreadLocal<Map<String, String>> sharedData = new ThreadLocal<Map<String, String>>();
	private ThreadLocal<String> testName = new ThreadLocal<>();

	
	public void setSharedData(String key, String value){
		sharedData.get().put(key, value);
	}
	
	public String getSharedData(String key){
		return sharedData.get().get(key);
	}
	
	/**
	 * TestNG data provider that returns the parameters of the current test as a case-insensitive tree map.
	 */
	@DataProvider(name = "testparameters")
	public Object[][] getTestParameters(ITestContext context){
		Map<String, String> parameters = TestUtils.getTestParameters(context);
		Map<String, String> newParameters = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		newParameters.putAll(parameters);
		return new Object[][] {{
			newParameters
		}};
	}

	/**
	 * TestNG data provider that returns the parameters of the current method/component as a case-insensitive tree map.
	 * Will also return any non-overridden parameters from the parent test.
	 */
	@DataProvider(name = "methodparameters")
	public Object[][] getParameters(ITestContext context){
		Map<String, String> newParameters = (Map<String, String>) getTestParameters(context)[0][0];
		Map<String, String> methodParameters = TestUtils.getMethodParameters(context, Main.componentIndex.get());
		newParameters.putAll(methodParameters);

		return new Object[][] {{
			newParameters
		}};
	}
	
	/**
	 * TestNG data provider that returns the parameters of the current method/component as a list of case-insensitive tree maps.
	 * "_$" is the delimiter of the parameter key, with the previous being the parameter name * and the following number being the 
	 * index in the list to be placed.  Any parameters without this delimiter are placed in first index of list.
	 */
	@DataProvider(name = "methodparametersmulti")
	public Object[][] getParametersMulti(ITestContext context){
		Map<String, String> parameters = (Map<String, String>) getParameters(context)[0][0];
		List<Map<String, String>> newParameters = new ArrayList<Map<String, String>>();
		 
		int index;
		String key;
		for (String param : parameters.keySet()){
			if (param.contains("_$")){
				String[] keys = param.split("_\\$");
				index = Integer.parseInt(keys[1]);
				key = keys[0];
			}else{
				index = 0;
				key = param;
			}
			while (index > newParameters.size() - 1){
				newParameters.add(new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER));
			}
			String value = parameters.get(param);
			newParameters.get(index).put(key, value);
		}
		
		return new Object[][] {{
			newParameters
		}};
	}

	
	public String getProjectName() {
		return projectName;
	}

	/***
	 * This method checks every time when we executes any component to check the current project Name
	 *  if it's not then it sets to the projectName to current Project Name.
	 * @param projectName
	 */
	public void setProjectName(String projectName) {
		this.projectName=projectName;
		if (!Configurables.projectName.equalsIgnoreCase(projectName.toString()) && Utils.checkExists(Config.getProp("ConfigSourceType"))
				&& Config.getProp("ConfigSourceType").equalsIgnoreCase("Excel")) {
			Map<String, String> updatedMap= new HashMap<>();
			Map<String,Map<String, String>> projectInfo= new HashMap<>();
			
			 updatedMap= TestUtils.getProjectConfigMap("./Config.xlsx",projectName);
				Config.addProps(updatedMap);
				//Below method used for explicitly change the Configurables value to the updated value.
				Configurables.updateConfigurablesValue();
				configDetailsProjectWise.put(projectName, updatedMap);
				//PageBase.configDetailsProjectWise=configDetailsProjectWise;
		}
	
	}
	/***
	 * To get the project specific Config details from the Config excel file.
	 * @param projectName
	 * @return
	 */
	public  Map<String,String> getConfigDetailsProjectWise(String projectName){
				return TestUtils.getProjectConfigMap("./Config.xlsx",projectName);
		
	}
	
	@BeforeMethod
	public void BeforeMethod(Method method, Object[] testData, ITestContext context) {
	   if (testData.length > 0) {
	      testName.set(context.getName()+": "+method.getName());
	      context.setAttribute("testName", testName.get());
	      context.getCurrentXmlTest().setName(Config.getProp("projectname"));
	   } else
		   context.setAttribute("testName", method.getName());
	   
		
	}
	 
	 @Override
	   public String getTestName() {
	       return testName.get();
	   }
	 


}
