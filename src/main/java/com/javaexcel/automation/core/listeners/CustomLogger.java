package com.javaexcel.automation.core.listeners;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import com.javaexcel.automation.core.data.Configurables;
import com.javaexcel.automation.core.execution.Main;
import com.javaexcel.automation.core.execution.TestManager;
import com.javaexcel.automation.core.utils.Constants;
import com.javaexcel.automation.core.utils.RestClient;
import com.javaexcel.automation.core.utils.TestUtils;
import com.javaexcel.automation.core.utils.Utils;

public class CustomLogger implements ITestListener {

	ThreadLocal<Boolean> keepRunning = new ThreadLocal<Boolean>();
	boolean updateDBStatus = Configurables.updateAutomationDB;
	boolean defaultRequired = Configurables.allComponentsRequiredByDefault;
	boolean firstUpdate = true;
	static int count=1;
	static int totalTC = TestManager.totalTC;
	public static int totalFail=0;
	public static int totalPass=0;
	public static String failedTCs="";
	public static Map<String, Integer> httpStatusPass = new HashMap<>();
	public static Map<String, Integer> httpStatusFail = new HashMap<>();

	//TODO: parameterize these
	String dbTable = "dbo.Jenkins_Job_Status";
	String idColumn = "ID";
	String statusColumn = "ExecutionStatus";
	String lastUpdateColumn = "LastUpdate";
	String lastTestColumn = "LastTest";
	String lastComponentColumn = "LastComponent";
	String lastStatusColumn = "LastStatus";
	
	ThreadLocal<Map<String, String>> componentStatus = new ThreadLocal<Map<String, String>>();

	@Override
	public void onTestStart(ITestResult result) {

		Map<String, String> parameters = TestUtils.getMethodParameters(result, Main.componentIndex.get());
		String skip = parameters.get("skip");
		if (Utils.isAffirmative(skip)){
			//skip component if skip parameter is affirmative
			throw new SkipException("Not running this component.");
		}else if (!keepRunning.get()){
			//skip component if previous required component failed
			throw new SkipException("Previous component failed. Not running this component.");
		}else if (checkDependencyFailed(parameters)){
			throw new SkipException("Component dependency failed/did not run. Not running this component.");
		}else{
			startComponent(parameters.get("componentname"));
		}
		
	}
	
	private boolean checkDependencyFailed(Map<String, String> parameters){
		String dependsOn = parameters.get("dependson") == null ? "" : parameters.get("dependson");
		if (!dependsOn.isEmpty()){
			String[] dependencies = Utils.splitTrim(dependsOn, ",");
			for (String dependency : dependencies){			
				String status = componentStatus.get().get(dependency);
				if (!status.equalsIgnoreCase("Passed")){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		Map<String, String> parameters = TestUtils.getMethodParameters(result, Main.componentIndex.get());
		String componentName = parameters.get("componentname");
		passComponent(componentName);
		onTestFinish(result, componentName, parameters.get("componentid"));
		
	}

	@Override
	public void onTestFailure(ITestResult result) {
		Map<String, String> parameters = TestUtils.getMethodParameters(result, Main.componentIndex.get());
		String componentName = parameters.get("componentname");
		failComponent(componentName);

		//stop running test if this component is marked as required (default)
		String required = parameters.get("required");
		boolean isRequired = Utils.isAffirmative(required, defaultRequired);
		keepRunning.set(!isRequired);

		onTestFinish(result, componentName, parameters.get("componentid"));
	}

	private synchronized void onTestFinish(ITestResult result, String componentName, String id){
		int val = Main.componentIndex.get();
		Main.componentIndex.set(++val);
		
		String status = TestUtils.getStatusString(result.getStatus());
		Map<String, String> statusMap = componentStatus.get();
		statusMap.put(id, status);
		statusMap.put("above", status);
		statusMap.put("previous", status);
		
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		Map<String, String> parameters = TestUtils.getMethodParameters(result, Main.componentIndex.get());
		String componentName = parameters.get("componentname");

		skipComponent(componentName);
		onTestFinish(result, componentName, parameters.get("componentid"));
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
	}

	@Override
	public void onStart(ITestContext context) {
		String parentName = TestUtils.getTestParameters(context).get(Constants.TEST_PARENT_NAME);
		componentStatus.set(new HashMap<String, String>());
		keepRunning.set(true);
		Main.componentIndex.set(0);
		startTest(context);
		try {
			TimeUnit.MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Utils.print("******SCENARIO NAME: " +count+". "+parentName +":" +context.getName().toUpperCase()+"*******" );
		
		Reporter.log("******SCENARIO NAME: " +count+". "+"<a name=\""+count+"\">"+parentName +":" +context.getName().toUpperCase()+"*******" +"</a>");
		
	}

	@Override
	public void onFinish(ITestContext context) {
		int status = TestUtils.getStatusFromContext(context);
		String statusStr = TestUtils.getStatusString(status).toUpperCase();
		String parentName = TestUtils.getTestParameters(context).get(Constants.TEST_PARENT_NAME);
		double progressFlt = Math.round((float)count/totalTC*100*100.0)/100.0;
		
		endTest(context);
		if(statusStr.contains("FAIL")){		
			System.err.println("******Finished Running Test - ".toUpperCase() + count+" ("+progressFlt+"%) "+ parentName + ":" + context.getName().toUpperCase()+"\nSTATUS: " + TestUtils.getStatusString(status).toUpperCase()+"******\n");
			totalFail++;
			failedTCs = failedTCs+count+". "+parentName+":"+context.getName().toUpperCase()+"\n";
		}
		else{
			System.out.println("******Finished Running Test - ".toUpperCase() + count+" ("+progressFlt+"%) "+ parentName + ":" + context.getName().toUpperCase()+"\nSTATUS: " + TestUtils.getStatusString(status).toUpperCase()+"******\n");
			totalPass++;
		}
			
		Reporter.log("******Finished Running Test - ".toUpperCase() + count+". "+parentName + ":" + context.getName().toUpperCase() + 
				"\nSTATUS: " + TestUtils.getStatusString(status).toUpperCase()+"******<br><br>");
		
		/**
		 * Gather status for HTTP Status Codes for reporting
		 */
		int httpCount = 0; 
		if(RestClient.expectedStatus!=null && RestClient.expectedStatus!=""){
			if(TestUtils.getStatusString(status).toUpperCase().equals("PASSED")){
				httpCount = httpStatusPass.get(RestClient.expectedStatus)!=null?(httpStatusPass.get(RestClient.expectedStatus)):0;			
				httpStatusPass.put(RestClient.expectedStatus, ++httpCount);	
				//httpStatusPass.keySet().on
			}else{
				httpCount = httpStatusFail.get(RestClient.expectedStatus)!=null?(httpStatusFail.get(RestClient.expectedStatus)):0;			
				httpStatusFail.put(RestClient.expectedStatus, ++httpCount);
			}
		}
		
		
		if (parentName != null){
			context.setName(count++ +". " + parentName + ":	" + context.getName());
		}

	}



	protected void updateDBStatus(String newStatus){
		Map<String, String> values = new HashMap<>();
		values.put(statusColumn, newStatus);
		values.put(lastUpdateColumn, Utils.getDateTime());
		//updateDBStatus(values);
	}

	protected void insertDBStatus() throws SQLException{
		Map<String, String> values = new HashMap<>();
		values.put(idColumn, Configurables.runID);
		values.put(statusColumn, "Beginning automation execution");
		values.put("Machine", Utils.getMachineName());
		values.put("TestFile", Configurables.testsFilePath);
		values.put("DataFile", Configurables.dataFilePath);
		values.put("StartTime", Utils.getDateTime());
	}

	protected void passComponent(String componentName){
		int compIndex = Main.componentIndex.get();
		String status = "Component " + compIndex + ": " + componentName + " passed.";
		updateComponent(status, componentName, "Passed");
	}

	protected void failComponent(String componentName){
		int compIndex = Main.componentIndex.get();
		String status = "Component " + compIndex + ": " + componentName + " failed.";
		updateComponent(status, componentName, "Failed");
	}

	protected void startComponent(String componentName){
		int compIndex = Main.componentIndex.get();
		String status = "Starting component " + compIndex + ": " + componentName + ".";
		updateComponent(status, componentName, "Started");
	}

	protected void skipComponent(String componentName){
		int compIndex = Main.componentIndex.get();
		String status = "Component " + compIndex + ": " + componentName + " skipped.";
		updateComponent(status, componentName, "Skipped");
	}

	protected void updateComponent(String executionStatus, String componentName, String componentStatus){
		Map<String, String> values = new HashMap<>();
		values.put(statusColumn, executionStatus);
		values.put(lastUpdateColumn, Utils.getDateTime());
		values.put(lastComponentColumn, componentName);
		values.put(lastStatusColumn, componentStatus);
		if(componentStatus.equals("Failed")){
			componentStatus = "Fail";
		}
	}

	protected void startTest(ITestContext context){
		String status = "Starting test " + context.getName();
		updateTest(status, context.getName(), "Started");
	}

	protected void endTest(ITestContext context){
		String statusText = TestUtils.getALMStatusFromContext(context);
		String status = "Finished test " + context.getName() + ": " + statusText;
		updateTest(status, context.getName(), statusText);
	}

	protected void updateTest(String executionStatus, String testName, String testStatus){
		Map<String, String> values = new HashMap<>();
		values.put(statusColumn, executionStatus);
		values.put(lastUpdateColumn, Utils.getDateTime());
		values.put(lastTestColumn, testName);
		values.put(lastStatusColumn, testStatus);
	}
}
