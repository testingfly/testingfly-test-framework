package com.javaexcel.automation.core.data;

import java.util.HashMap;

import com.javaexcel.automation.core.data.Config;
import com.javaexcel.automation.core.enums.DataInputType;
import com.javaexcel.automation.core.table.Table;

public class Configurables {

	
	//General Config
	public static String projectName = 						Config.getProp("API_Name","WFG-API-TEST"); 
	public static String testFileName = 					Config.getProp("TestFile");
	public static String testsFilePath = 					Config.getProp("Path","src/test/resources/test-data/");
	public static String dataFilePath = 					Config.getProp("DataFile", testsFilePath);
	public static String componentFilePath = 				Config.getProp("ComponentFile", testsFilePath);
	public static String testFilterType = 					Config.getProp("TestFilterType", "TestiD"); //Type of field to specify test cases by.  Can be Name, TestID, or ConfigID
	//*** Obsolete. Use dataInputType instead ***//
	//public static boolean useExcel = 						Config.getBoolProp("UseExcel"); //Whether to use Excel for test data (otherwise will use projectName.xml)
	public static DataInputType dataInputType =				DataInputType.parse(Config.getProp("DataInputType", "Excel"));
	public static String runID = 							Config.getProp("RunID"); //Unique run id for execution
	public static String jenkinsURL = 							Config.getProp("jenkinsURL");
	public static boolean parallel = 						Config.getBoolProp("Parallel");
	//public static String browser = 							Config.getProp("Browser").toLowerCase();
	public static boolean useJavascriptText = 				Config.getBoolProp("JavascriptText", "Yes"); //If true, calling the type method will use javascript to set the locator's text rather than Selenium's set method.  Usually faster, especially on IE, but not always ideal.
	public static String uploadDirectory = 					Config.getProp("UploadDirectory","reports/html"); //Upload directory for html reports
	public static String lastestBuildDirectory = 			Config.getProp("LatestUploadDirectory","reports/html/latestBuild"); //Upload directory for html reports
	public static String screenshotDirectory = 				Config.getProp("ScreenshotDirectory"); //Upload directory for screenshots
	public static String summaryFile = 						Config.getProp("SummaryFile"); //Which file to treat as the "summary" file for uploading to ALM etc
	public static String threadCount =						Config.getProp("ThreadCount"); //Number of threads to use when running in parallel
	public static boolean TestWithMultiConfig=				Config.getBoolProp("TestWithMultiConfig", "true");
	public static boolean TestScenarioSheet=				Config.getBoolProp("TestScenarioSheet", "false");
	public static String ScenarioSheetName=				Config.getProp("ScenarioSheetName", "ScenarioFlow");
	//Added for excel file location
	public static String excelFile = 						Config.getProp("ExcelSummaryFile"); 
	
	//This is for Test config sheet(test) which has run flag column---for gateway api's
	public static boolean TestFileWithExeFlag = 		    Config.getBoolProp("TestFileWithExeFlag", "true");
	//public static String gateway_Environment = 		   Config.getProp("gateway_Environment");
	public static String gateway_Environment = 		   Config.getProp("Environment");
	public static String gateway_Resource = 		    Config.getProp("gateway_Resource");
	public static String gateway_version = 		    Config.getProp("gateway_version");
	public static String gateway_resource_Column=Config.getProp("gateway_resource_Column");
	public static String gateway_flag=Config.getProp("gateway_flag","Flag");
	public static String gateway_flag_value=Config.getProp("gateway_flag_value","Y");
	public static String gateway_IncludeInRun=Config.getProp("gateway_IncludeInRun","IncludeInRun");
	public static String gateway_IncludeInRun_value=Config.getProp("gateway_IncludeInRun_value","Y");
	public static String gateway_sheet_ID=Config.getProp("gateway_sheet_ID");
	public static String gateway_ResourceTable=Config.getProp("Resources");
	public static String gateway_EnvTable=Config.getProp("API_Env_Properties");
	public static String gateway_EnvironmentCoulmnName=Config.getProp("gateway_EnvironmentCoulmnName");
	public static String gateway_TestConfigSheetName=Config.getProp("gateway_TestConfigSheetName");
	public static Boolean envFlg = true;
	public static Boolean reqFlg = true;
	public static Table globalEnvTable = new Table();
	public static Boolean resourceFlg = true;
	public static Table globalResourceTable = new Table();
	public static Table globalConfigTable = new Table();
	public static Boolean tcQueryFlg = true;
	public static Table globalTCsTable = new Table();
	public static Table globalReqsTable = new Table();
	public static String tcQuery=null;
	public static HashMap<String, Table> testTableMap = new HashMap<String, Table>();
	
	//Local execution
	public static String testCases = 						Config.getProp("TestCases");
	public static String testIDs = 							Config.getProp("TestCases");
	public static String testCasesFormQuery = 				Config.getProp("testCasesFormQuery");
	public static boolean useGrid = 						Config.getBoolProp("Grid", "no"); //Whether to use Selenium Grid
	public static String gridHubURL = 						Config.getProp("GridHubUrl"); //Url of the Selenium Grid Hub
	public static String testCaseDelimiter = 				Config.getProp("TestCaseDelimiter", "///"); //Delimiter separating test cases in "testCases" field
	public static String configIDDelimiter = 				Config.getProp("ConfigIDDelimiter", "\\$"); //Delimiter separating test id and configuration id in "testCases" field

	//Object repository DB
	public static String objectRepositoryPageField = 		Config.getProp("ObjectPageField"); //Page name, used to fetch page wise data from database
	public static String objectRepositoryNameField = 		Config.getProp("ObjectNameField"); // Logical name for object
	public static String objectRepositoryDescriptionField = Config.getProp("ObjectDescriptionField"); // Description of obeject
	public static String objectRepositoryTypeField = 		Config.getProp("ObjectTypeField"); //Object locator type 
	public static String objectRepositoryContentField = 	Config.getProp("ObjectContentField");//Object locator value 
	public static String objectRepositoryProjectField = 	Config.getProp("ProjectField"); //Project name, just for display
	public static String objectRepositoryTable = 			Config.getProp("ObjectTable"); //Project wise object table name 
	
	//MultiUser and Application Tables details for Credentials and URL Details
	public static String objectRepositoryApplAreaField=    	Config.getProp("applicationArea");
	public static String objectRepositoryEnvironmentField= 	Config.getProp("environment_MultiUserInfo");
	public static String objectRepositoryApplNameField= 	Config.getProp("applicationName");
	public static String objectRepositoryRoleField=   		Config.getProp("role");
	public static String objectRepositoryStatusField=    	Config.getProp("status");
	public static String userCredentialTable = 			Config.getProp("userCredentialsTable"); 
	public static String urlDetailsTable = 			Config.getProp("urlDetilsTable"); 
	public static String objectRepositoryParalleLogineField= Config.getProp("paralellLogin"); 
	
	
	
	//Automation DB
//	public static String automationDBServer = 				Config.getProp("Server");
//	public static String automationDBName = 				Config.getProp("DatabaseName");
//	public static String automationDBUser = 				Config.getProp("User");
//	public static String automationDBPassword = 			Config.getProp("Password");
	
	//Excel test data
	public static String excelTestSheet = 					Config.getProp("TestSheet", "Tests");
	public static String excelReqSheet = 					Config.getProp("Requirements", "Requirements");
	public static String excelComponentSheet = 				Config.getProp("ComponentSheet", "Steps");
	public static String excelIdColumn = 					Config.getProp("IDColumn", "ID");
	public static String excelTestsColumn = 				Config.getProp("TestsColumn", "Test Case Name");
	public static String excelTestParentColumn = 			Config.getProp("TestParentColumn","Test Suite Name");
	public static String excelTestIDColumn = 				Config.getProp("TestIDColumn", "Test Case ID");
	public static String excelTestSuiteIDColumn = 			Config.getProp("TestSuiteIDColumn", "Test Suite ID");
	public static String excelConfigIDColumn = 				Config.getProp("ConfigIDColumn", "Configuration ID");
	public static String excelOptionColumn = 				Config.getProp("OptionColumn", "Options");
	public static String excelDataSourceColumn = 			Config.getProp("DataSourceColumn", "Data Source");
	public static String excelDataIDColumn = 				Config.getProp("DataIDColumn", "Data ID");
	public static String excelMethodColumn = 				Config.getProp("MethodColumn", "Method");
	public static String excelProjectColumn = 				Config.getProp("ProjectColumn", "Project");
	public static String excelPackageColumn = 				Config.getProp("PackageColumn", "Package");
	public static String excelClassColumn = 				Config.getProp("ClassColumn", "Class");
	public static String excelComponentColumn = 			Config.getProp("ComponentColumn", "Steps");
	public static String excelConfigPointerKeyword = 		Config.getProp("ExcelConfigPointer", "ConfigID"); //Keyword used for replacing Excel data with that mapped to Config ID instead of specified value.
	public static String excelComponentMatchColumn = 		Config.getProp("ComponentMatchColumn", "Test Step ID"); //Column for which components and the test case they belong to match
	public static String stepIDColumn =						Config.getProp("StepIDColumn", "Internal StepId"); //Column used for keeping the component order correct during queries
	public static String tagName = 							Config.getProp("tagName", "tagName"); //tagName to drive test

	
	//ALM
	public static String almUploadDirectory = 				Config.getProp("uploaddirectory");
	public static String almAttachTarget = 					Config.getProp("ALMAttachTarget", "run");
	public static String almUrl = 							Config.getProp("almurl");
	public static String almCredentials = 					Config.getProp("almcredentials");
	public static String almDomain = 						Config.getProp("almdomain");
	public static String almProject = 						Config.getProp("almproject");
	public static String testSetIDs = 						Config.getProp("TestSetIDs");
	public static String almRunFlagColumn = 				Config.getProp("RunFlagColumn");
	public static String almTargetRunFlag = 				Config.getProp("RunFlag");
	public static String almStatusRequirement = 			Config.getProp("ALMStatus", "Any");
	public static String almApplicationIDField = 			Config.getProp("ALMApplicationIDField");
	
	//Misc
	public static String excelConfigPath = 					Config.getProp("ExcelConfig");
	public static boolean allComponentsRequiredByDefault = 	Config.getBoolProp("DefaultRequired", "True"); //If true, by default tests will fail if any component fails, otherwise dependencies must be manually specified.
	public static boolean useCustomFirefoxProfile = 		Config.getBoolProp("UseCustomFirefoxProfile", "No"); //This is a legacy fix for old version of Firefox in some applications.
	public static boolean updateAutomationDB = 				Config.getBoolProp("DBStatus", "False"); //If true, updates general status in automation database.
	public static boolean dbReporting = 					Config.getBoolProp("DBReporting", "false"); //If true, updates step-wise status in automation database and generates report based on that information.
	public static boolean useDBData = 						Config.getBoolProp("UseDBData", "No"); //If true, allows components to put and get data to and from automation database, otherwise uses local map.
	
	//Browser specific
	public static boolean enableChromePopup= Config.getBoolProp("EnableChromePopUp","false");
	
	
	
	/***
	 * This method updates the Configurables property value to the updated Value
	 *  once we updated the ConfigMap based on different project.
	 */
	public static void updateConfigurablesValue() {

		projectName = Config.getProp("API_Name","WFG-API-TEST");
		testFileName = Config.getProp("TestFile");
		testsFilePath = Config.getProp("TestFilePath","src/test/resources/test-data/");
		dataFilePath = Config.getProp("DataFile", testsFilePath);
		componentFilePath = Config.getProp("ComponentFile", testsFilePath);
		testFilterType = Config.getProp("TestFilterType", "TestiD"); 
		parallel = Config.getBoolProp("Parallel");
		//browser = Config.getProp("Browser").toLowerCase();
		useJavascriptText = Config.getBoolProp("JavascriptText", "Yes"); 
		uploadDirectory = Config.getProp("UploadDirectory"); 
		screenshotDirectory = Config.getProp("ScreenshotDirectory"); 
		summaryFile = Config.getProp("SummaryFile"); 
		threadCount = Config.getProp("ThreadCount"); 
		excelFile = Config.getProp("ExcelSummaryFile");
		testCases = Config.getProp("TestCases");
		testCasesFormQuery = Config.getProp("testCasesFormQuery");
		useGrid = Config.getBoolProp("Grid", "no"); 
		gridHubURL = Config.getProp("GridHubUrl");
		testCaseDelimiter = Config.getProp("TestCaseDelimiter", "///");
		configIDDelimiter = Config.getProp("ConfigIDDelimiter", "\\$"); 
		objectRepositoryPageField = Config.getProp("ObjectPageField"); 
		objectRepositoryNameField = Config.getProp("ObjectNameField");
		objectRepositoryDescriptionField = Config.getProp("ObjectDescriptionField"); 
		objectRepositoryTypeField = Config.getProp("ObjectTypeField"); 
		objectRepositoryContentField = Config.getProp("ObjectContentField");
		objectRepositoryProjectField = Config.getProp("ProjectField"); 
		objectRepositoryTable = Config.getProp("ObjectTable");
		objectRepositoryApplAreaField = Config.getProp("applicationArea");
		objectRepositoryEnvironmentField = Config.getProp("environment_MultiUserInfo");
		objectRepositoryApplNameField = Config.getProp("applicationName");
		objectRepositoryRoleField = Config.getProp("role");
		objectRepositoryStatusField = Config.getProp("status");
		userCredentialTable = Config.getProp("userCredentialsTable");
		urlDetailsTable = Config.getProp("urlDetilsTable");
		objectRepositoryParalleLogineField = Config.getProp("paralellLogin");
		// Excel test data
		excelTestSheet = Config.getProp("TestSheet", "Tests");
		excelComponentSheet = Config.getProp("ComponentSheet", "Test Steps");
		excelIdColumn = Config.getProp("IDColumn", "ID");
		excelTestsColumn = Config.getProp("TestsColumn", "Test Case Name");
		excelTestParentColumn = Config.getProp("TestParentColumn");
		excelTestIDColumn = Config.getProp("TestIDColumn", "Test Case ID");
		excelConfigIDColumn = Config.getProp("ConfigIDColumn", "Configuration ID");
		excelOptionColumn = Config.getProp("OptionColumn", "Options");
		excelDataSourceColumn = Config.getProp("DataSourceColumn", "Data Source");
		excelDataIDColumn = Config.getProp("DataIDColumn", "Data ID");
		excelMethodColumn = Config.getProp("MethodColumn", "Method");
		excelProjectColumn = Config.getProp("ProjectColumn", "Project");
		excelPackageColumn = Config.getProp("PackageColumn", "Package");
		excelClassColumn = Config.getProp("ClassColumn", "Class");
		excelComponentColumn = Config.getProp("ComponentColumn", "Component");
		excelConfigPointerKeyword = Config.getProp("ExcelConfigPointer", "ConfigID"); 
		excelComponentMatchColumn = Config.getProp("ComponentMatchColumn", "Internal Test Case Id");
		stepIDColumn = Config.getProp("StepIDColumn", "Internal StepId");
		tagName = Config.getProp("tagName","tagName");

		
		//for API gateway..
		//gateway_Environment=Config.getProp("gateway_Environment");
		gateway_Environment=Config.getProp("Environment");
		gateway_Resource= Config.getProp("gateway_Resource");
		gateway_version=Config.getProp("gateway_version");
		gateway_resource_Column=Config.getProp("gateway_resource_Column");
		gateway_flag=Config.getProp("gateway_flag");
		gateway_flag_value=Config.getProp("gateway_flag_value");
		gateway_IncludeInRun=Config.getProp("gateway_IncludeInRun");
		gateway_IncludeInRun_value=Config.getProp("gateway_IncludeInRun_value");
		gateway_sheet_ID=Config.getProp("gateway_sheet_ID","Test Suite ID");
		gateway_EnvTable = Config.getProp("gateway_EnvTable","API_Env_Properties");
		gateway_ResourceTable = Config.getProp("gateway_ResourceTable","Resources");
		gateway_EnvironmentCoulmnName=Config.getProp("gateway_EnvironmentCoulmnName","Environment");
		gateway_TestConfigSheetName=Config.getProp("gateway_TestConfigSheetName","TestConfig");
		


	}
}
