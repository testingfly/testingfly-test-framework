package com.javaexcel.automation.core.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;

//import Exception.FilloException;
//import Fillo.Recordset;
import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Recordset;
import com.javaexcel.automation.core.data.Config;
import com.javaexcel.automation.core.data.Configurables;
import com.javaexcel.automation.core.enums.TestFilterType;
import com.javaexcel.automation.core.testngdata.TestNGTest;

public class TestUtils {
	
	private String testName="";

	public static Map<String, String> getMethodParameters(ITestResult result, int componentIndex) {
		return getMethodParameters(result.getTestContext(), componentIndex);
	}

	public static Map<String, String> getMethodParameters(ITestContext context, int componentIndex) {
		Map<String, String> parameters = context.getCurrentXmlTest().getClasses().get(componentIndex).getIncludedMethods().get(0).getAllParameters();
		return parameters;
	}

	public static Map<String, String> getTestParameters(ITestResult result) {
		return getTestParameters(result.getTestContext());
	}

	public static Map<String, String> getTestParameters(ITestContext context) {
		Map<String, String> parameters = context.getCurrentXmlTest().getAllParameters();
		return parameters;
	}

	public static int getComponentIndexFromID(String id) {
		return Integer.parseInt(id.split("-")[1]) - 1;
	}

	public static String getStatusString(int status) {
		switch (status) {
		case 1:
			return "Passed";
		case 2:
			return "Failed";
		case 3:
			return "Skipped";
		case 4:
			return "SUCCESS PERCENTAGE FAILURE";
		case 16:
			return "STARTED";
		default:
			return "UNKNOWN";
		}
	}

	public static String getALMStatusString(int status) {
		switch (status) {
		case 1:
			return "Passed";
		case 2:
		case 4:
			return "Failed";
		case 3:
		case 16:
			return "Not Completed";
		default:
			return "No Run";
		}
	}

	public static int getStatusFromContext(ITestContext context) {
		int failedComponents = context.getFailedTests().size();
		int failedConfiguration = context.getFailedConfigurations().size();

		if (failedComponents > 0) {
			return 2;
		} else if (failedConfiguration > 0) {
			return 3;
		} else {
			return 1;
		}
	}

	public static String getALMStatusFromContext(ITestContext context) {
		return getALMStatusString(getStatusFromContext(context));
	}
	


	public static String createURL(String sourceFilePath, String destinationFilePath) {
		String content = "[InternetShortcut]\nURL=" + sourceFilePath;
		String finalPath = destinationFilePath + "/results.URL";
		File file = new File(finalPath);

		try {
			FileUtils.writeStringToFile(file, content, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return finalPath;
	}

	public static TestFilterType getTestFilterType() {
		switch (Configurables.testFilterType.toLowerCase()) {
		case "name":
		default:
			return TestFilterType.Name;
		case "id":
		case "testid":
		case "test id":
			return TestFilterType.TestID;
		case "configurationid":
		case "configuration id":
		case "configid":
		case "config id":
			return TestFilterType.ConfigurationID;
		}
	}

	public static String getInvalidTestName(TestNGTest test, String keyColumn, String keyValue) {
		if (test.getName() != null)
			return "Invalid Test - Name: " + test.getName();
		if (test.getConfigID() != null)
			return "Invalid Test - ConfigID: " + test.getConfigID();
		if (test.getID() != null)
			return "Invalid Test - ID: " + test.getID();
		return "Invalid Test - " + keyColumn + ": " + keyValue;
	}

	public static String getTestIdentifier(ITestContext context) {
		switch (getTestFilterType()) {
		case Name:
			return context.getCurrentXmlTest().getName();
		case TestID:
			return getTestParameters(context).get("TestID");
		case ConfigurationID:
			return getTestParameters(context).get("ConfigID");
		}
		return null;
	}

	/***
	 * Download the file from ALM to get the Excel Config details
	 * 
	 * @return
	 */

	public static String downloadFileFromALM() {

		int returnValue = -1;
		try {

			if (Utils.checkExists(Config.getProp("ALMDownloadExeFile"))
					&& Utils.checkExists(Config.getProp("ALMDomain")) && Utils.checkExists(Config.getProp("ALMProject"))
					&& Utils.checkExists(Config.getProp("ALMUserID"))
					&& Utils.checkExists(Config.getProp("ALMPassword"))
					&& Utils.checkExists(Config.getProp("DownloadExcelFile"))
					&& Utils.checkExists(Config.getProp("ALMOperation"))
					&& Utils.checkExists(Config.getProp("encrptionKey"))) {

				// rebuild the Secret key using SecretKeySpec
				byte[] decodedKey = Base64.getDecoder().decode(Config.getProp("encrptionKey"));
				SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

				ProcessBuilder pb = new ProcessBuilder(Config.getProp("ALMDownloadExeFile"),
						Config.getProp("ALMDomain"), Config.getProp("ALMProject"), Config.getProp("ALMUserID"),
						PasswordGenerator.decrypt(Config.getProp("ALMPassword"), originalKey),
						Config.getProp("DownloadExcelFile"), Config.getProp("ALMOperation"));

				Process p = pb.start();
				p.waitFor();
				returnValue = p.exitValue();
			}
			if (returnValue == 0) {
				String downloadPath = Utils.addSlashIfNone(System.getenv("TEMP").toString())
						+ Utils.addSlashIfNone("ALMUtility") + Config.getProp("DownloadExcelFile");
				String userDir = Utils.addSlashIfNone(System.getProperty("user.dir")) + "Config.xlsx";

				// Checking whether all the keys are same in ALM Downloaded file
				// and workspace file.

				Map<String, String> almConfigFile = checkConfigFile(downloadPath);
				Map<String, String> workspaceConfigFile = checkConfigFile(userDir);
				if (!almConfigFile.keySet().equals(workspaceConfigFile.keySet())) {
					Reporter.log(
							"WARNING: Both ALM downloaded Config file and Workspace Config File have different Keys");
					differenceKeysInMap(almConfigFile, workspaceConfigFile);
				}

				File src = new File(downloadPath);
				File dest = new File(userDir);
				FileUtils.copyFile(src, dest);
				return userDir;
			}
		} catch (Exception e) {

			Reporter.log("Exception Caught of Type:- " + e.getLocalizedMessage(), true);
			e.printStackTrace();
		}
		return null;
	}

	/***
	 * Get the config Details from the DB
	 */
	public static void getConfigDetailsFromDB() {

		String command = "";

		/*try {
			AutomationDatabase.executeCommand(command);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	public static List<String> getCustomListeners() {
		String listeners = Config.getProp("CustomListeners", "");
		if (listeners.isEmpty()) return new ArrayList<>();
		
		return Arrays.asList(listeners.split(","));
	}
	/**Get the Project specific data from the excel file
	 * 
	 * @param excelFilePath
	 * @param projectName
	 * @return
	 */
	public static  Map<String, String> getProjectConfigMap(String excelFilePath,String projectName) {
		Map<String,String> projectConfigMap= new HashMap<>();
		Excel configFile = new Excel(excelFilePath);
		String testConfigQuery = "";
		String sheet = "TestConfig";
		testConfigQuery = String.format("SELECT * FROM  %s where ProjectCode ='%s'  ", sheet,projectName);
		Recordset recordset = configFile.executeQuerySheetRecords(testConfigQuery);
		String key = "";
		String value = "";
		try {
			while (recordset.next()) {
				key = recordset.getField(2).value();
				value = recordset.getField(3).value();
				projectConfigMap.put(key.toLowerCase(), value);
			}
		} catch (FilloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return projectConfigMap;
	}
/***
 * Returns a map object which contains all the key present in the excel sheet.
 * 
 * @param file
 * @return
 */
	public static Map<String, String> checkConfigFile(String file) {
		Map<String, String> configMap = new HashMap<>();
		Excel almDownloadedFile = new Excel(file);

		String query1 = "";
		String query2 = "";
		String query3 = "";
		String sheet1 = "TestConfig";
		String sheet2 = "Common_Config";
		String sheet3 = "ProjectNames";

		query1 = String.format("SELECT * FROM  %s ", sheet1);
		query2 = String.format("SELECT * FROM  %s ", sheet2);
		query3 = String.format("SELECT * FROM  %s ", sheet3);

		Recordset recordset = almDownloadedFile.executeQuerySheetRecords(query1);
		Recordset recordset1 = almDownloadedFile.executeQuerySheetRecords(query2);
		Recordset recordset2 = almDownloadedFile.executeQuerySheetRecords(query3);

		String key = "";
		String value = "";
		try {
			while (recordset.next()) {
				key = recordset.getField(2).value();
				value = recordset.getField(3).value();
				configMap.put(key.toLowerCase(), value);
			}
			while (recordset1.next()) {
				key = recordset1.getField(1).value();
				value = recordset1.getField(2).value();
				configMap.put(key.toLowerCase(), value);
			}
			while (recordset2.next()) {
				key = recordset2.getField(1).value();
				value = recordset2.getField(2).value();
				configMap.put(key.toLowerCase(), value);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return configMap;
	}
	

	
	/***
	 * Compares two  map and returns the difference key in both the maps.
	 * @param almConfigFile
	 * @param workspaceConfigFile
	 */
	public static void differenceKeysInMap(Map<String, String> almConfigFile, Map<String, String> workspaceConfigFile) {
		for (Map.Entry<String, String> almConfigKey : almConfigFile.entrySet()) {
			if (!workspaceConfigFile.containsKey(almConfigKey.getKey())) {
				Reporter.log("WARNING: KEY:- " + almConfigKey.getKey()
						+ " is not present in  workspaceConfigFile but  present in almConfigFile", true);
			}
		}

		for (Map.Entry<String, String> wsconfigKey : workspaceConfigFile.entrySet()) {
			if (!almConfigFile.containsKey(wsconfigKey.getKey())) {
				Reporter.log("WARNING: KEY:- " + wsconfigKey.getKey()
						+ " is not present in almConfigFile but present in workspaceConfigFile", true);
			}
		}
	}
	
	public void setTestName(ITestContext context) {
		testName = context.getAttribute("scenario_name").toString();
	}
	
	public String getTestName() {
		return testName+"TEST337";
	}
	
}
