package com.javaexcel.automation.core.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.Reporter;

import com.javaexcel.automation.core.data.Config;
import com.javaexcel.automation.core.data.Configurables;
import com.javaexcel.automation.core.data.ITestInstance;
import com.javaexcel.automation.core.data.ITestSet;
import com.javaexcel.automation.core.data.TestSet;
import com.javaexcel.automation.core.reporting.TestReportComparator;
import com.javaexcel.automation.core.table.Field;
import com.javaexcel.automation.core.table.Record;
import com.javaexcel.automation.core.table.Table;
import com.javaexcel.automation.core.testngdata.ITestNGStruct;
import com.javaexcel.automation.core.testngdata.TestNGClass;
import com.javaexcel.automation.core.testngdata.TestNGMethod;
import com.javaexcel.automation.core.testngdata.TestNGSuite;
import com.javaexcel.automation.core.testngdata.TestNGTest;
import com.javaexcel.automation.core.utils.ComponentContext;
import com.javaexcel.automation.core.utils.Excel;
import com.javaexcel.automation.core.utils.TestUtils;
import com.javaexcel.automation.core.utils.Utils;
import com.javaexcel.automation.core.swagger.SwaggerData;

public class ExcelSuite implements SuiteCreator {

	private Map<String, ComponentContext> componentMap = new HashMap<>();
	private Map<Integer, String> componentMapSort = new HashMap<>();

	private Excel testsFile, dataFile, componentFile;
	private String keyColumn;

	public ExcelSuite() {

		// This sets which column is used for filtering
		switch (TestUtils.getTestFilterType()) {
		case Name:
		default:
			keyColumn = Configurables.excelTestsColumn;
			break;
		case TestID:
			keyColumn = Configurables.excelTestSuiteIDColumn;
			break;
		case ConfigurationID:
			keyColumn = Configurables.excelConfigIDColumn;
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wellsfargo.automation.core.execution.SuiteCreator#createSuite(java.
	 * util.List)
	 */
	@Override
	public TestNGSuite createSuite(List<ITestSet> testSets) {

		// Create suite
		TestNGSuite suite = new TestNGSuite();

		// Open data files
		testsFile = new Excel(Configurables.testsFilePath + Configurables.testFileName);
		dataFile = testsFile;

		if (!Configurables.componentFilePath.equals(Configurables.testsFilePath)) {
			componentFile = new Excel(Configurables.testsFilePath + Configurables.testFileName);
		} else {
			componentFile = testsFile;
		}

		// Set Test Steps/Components
		setComponentMap();

		for (ITestSet testSet : testSets) {
			createTests(testSet, suite);
		}

		// Close data files
		testsFile.close();
		try {
			dataFile.close();
		} catch (Exception e) {
			// if same file, so do nothing
		}
		try {
			componentFile.close();
		} catch (Exception e) {
			// if same file, so do nothing
		}

		return suite;
	}

	private void createTests(ITestSet testSet, TestNGSuite suite) {

		/*
		 * Sort TCs by IDs
		 */
		Collection<ITestInstance> tsort = testSet.getInstances();
		List<ITestInstance> list = new ArrayList<>(tsort);
		Collections.sort(list, new TestReportComparator());

		for (ITestInstance instance : list) {
			try {
				createTest2(suite, instance);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void createTest2(TestNGSuite suite, ITestInstance instance) {
		TestNGTest test = new TestNGTest(suite, instance.getName());

		/*
		 * Retrieve Details of Test Suites in scope
		 */
		String query = "Select * from " + Configurables.excelTestSheet + " Where Flag='Y'";
		Table testSheetData = testsFile.querySheetRecords(query);
		List<Record> records = testSheetData.getRecords(keyColumn, instance.getKeyValue());

		/*
		 * Retrieve Test Suite ID
		 */
		String matchVal = records.get(0).getValue(Configurables.excelTestSuiteIDColumn);
		String tmp_query = "Select * From " + Configurables.excelTestSheet + " Where "
				+ Utils.quote(Configurables.excelTestSuiteIDColumn) + " = " + Utils.sQuote(matchVal) + "and Flag='Y'";
		records = testSheetData.getRecords(Configurables.excelTestSuiteIDColumn, matchVal);

		// Assign test id, config id, and instance id as parameters to test
		String configID = instance.getConfigID();
		String testID = instance.getTestID();
		String instanceID = instance.getID();
		String testCaseID = instance.getTCID();
		String instanceResource = instance.getResource();

		/*
		 * Retrieve Requirement details
		 */
		if (Configurables.reqFlg) {
			Configurables.reqFlg = false;
			query = "Select * From " + Utils.quote(Configurables.excelReqSheet) + " Where Scope='Y'";
			Configurables.globalReqsTable = dataFile.querySheetRecords(query);

		}

		try {

			Configurables.globalEnvTable = Configurables.globalConfigTable;
			testSheetData.setColumns(Configurables.globalEnvTable.getRandomRecord());
			testSheetData.setRecords(Configurables.globalEnvTable.getRecords("Resource", instanceResource));

			String strTestConfigSheetID = testSheetData.getRecords().get(0).getField("ID").toString();

			Record record = records.get(0);
			record.add("DATA SOURCE", record.getField("DATA SOURCE") + ";" + Configurables.gateway_TestConfigSheetName);
			record.add("TEST SUITE ID",
					record.getField(Configurables.excelTestSuiteIDColumn) + ";" + strTestConfigSheetID);

			if (test.getName() == null) {
				String name = record.getValue(Configurables.excelTestsColumn);
				if (suite.containsTest(name)) {
					name = name + "_" + instance.getConfigID();
				}
				test.setName(name);
			}

			if (testID == null) {
				testID = record.getValue(Configurables.excelTestSuiteIDColumn);
			}

			if (testCaseID == null) {
				testCaseID = record.getValue(Configurables.testIDs);
				System.out.println("Testing ID: " + instance.getTCID());
			}

			if (instanceID == null) {
				instanceID = record.getValue(testID);
			}

			if (configID == null) {
				configID = record.getValue(Configurables.excelConfigIDColumn);
			}
			test.setID(testID);
			test.setConfigID(configID);
			test.addParameter("testCaseID", testCaseID);
			test.addParameter("TestID", testID);
			test.addParameter("InstanceID", instanceID);
			test.addParameter("ConfigID", configID);
			addParameters(test, record, instanceResource);
			if (Configurables.excelTestParentColumn != null) {
				test.addParameter("testparentname", record.getValue(Configurables.excelTestParentColumn));
			}

		} catch (Exception e) {
			System.err.println("Error loading test configurations... " + e.getMessage()
					+ " Please verify the following - "
					+ "\n1. Resource(s) are defined  correctly in the Test data sheet (TestConfig and the respective resource tabs)."
					+ "\n2. Correct environment name is defined in the config.properties file. "
					+ "\n...exiting test run.");
			e.printStackTrace();
			System.exit(1);
		}

		int key = 1;

		for (int index : componentMapSort.keySet()) {
			Record record = new Record();

			String componentName = componentMapSort.get(key++);
			record.add("STEPS", componentName);
			record.add("Data Source", "");
			record.add("Test Suite ID", "");

			ComponentContext componentContext = componentMap.get(componentName);

			/*
			 * Skip Generate JSON file step if not applicable
			 */
			Boolean skip = true;
			if (test.getParameters().get("override method") != null
					&& test.getParameters().get("override method").equals("GET")
					&& componentName.contains("Generate JSON file")
					|| test.getParameters().get("override method") != null
							&& test.getParameters().get("override method").equals("DELETE")
							&& componentName.contains("Generate JSON file")) {
				continue;
			} else if (test.getParameters().get("override method") != null
					&& !test.getParameters().get("override method").equals("")) {
				skip = false;
			}
			if (skip && test.getParameters().get("requesttype").equals("GET")
					&& componentName.contains("Generate JSON file")
					|| skip && test.getParameters().get("requesttype").equals("DELETE")
							&& componentName.contains("Generate JSON file")) {
				continue;
			}

			if (componentContext != null) {
				String fullPath = componentContext.getPackagePath() + "." + componentContext.getClassName();

				TestNGClass cls = new TestNGClass(test, fullPath);

				String methodName = componentContext.getMethodName();
				TestNGMethod method = new TestNGMethod(cls, methodName);

				String id = records.get(0).getValue(Configurables.excelIdColumn.toUpperCase());

				method.setID(id);
				method.setConfigID(configID);
				method.addParameter("componentid", id);
				method.addParameter("componentname", componentName);
				addParameters(method, record, instanceResource);

			} else {
				throw new NullPointerException();
			}

		}

	}

	private void addParameters(ITestNGStruct struct, Record record, String instanceResource) {

		String testID = record.getValue(Configurables.excelTestSuiteIDColumn.toUpperCase());
		String dataSource = record.getValue(Configurables.excelDataSourceColumn.toUpperCase());
		String query;

		if (!testID.isEmpty() || !dataSource.isEmpty()) {
			if (dataSource.isEmpty()) {
				dataSource = struct.getName();
			}
			if (testID.isEmpty()) {
				testID = struct.getID();
			}
			if (testID.equalsIgnoreCase(Configurables.excelConfigPointerKeyword)) {
				testID = struct.getConfigID();
			}

			String[] dataSources = Utils.splitTrim(dataSource, ";");
			String[] dataIDs = Utils.splitTrim(testID, ";");

			// if using same data id for multiple sources
			if (dataSources.length > dataIDs.length) {
				dataIDs = new String[dataSources.length];
				for (int i = 0; i < dataIDs.length; i++) {
					dataIDs[i] = dataIDs[0];
				}
			}

			// for each sheet/data source
			String currentSource = null;
			String nextSource = dataSources[0];
			for (int i = 0; i < dataSources.length; i++) {

				List<String> ids = new ArrayList<>();
				String[] idStrings = Utils.splitTrim(dataIDs[i], ",");
				for (String id : idStrings) {
					if (id.contains("-")) {
						String[] idRange = Utils.splitTrim(id, "-");
						int startID = Integer.valueOf(idRange[0]);
						int endID = Integer.valueOf(idRange[1]);
						for (int j = startID; j <= endID; j++) {
							ids.add(String.valueOf(j));
						}
					} else {
						ids.add(id);
					}
				}

				/*
				 * Check for overridden environment per test suite
				 */
				String env = Utils.sQuote(Configurables.gateway_Environment);
				if (record.getValue("OVERRIDE ENVIRONMENT") != null
						&& !record.getValue("OVERRIDE ENVIRONMENT").equals("")) {
					env = Utils.sQuote(record.getValue("OVERRIDE ENVIRONMENT"));

				}

				/*
				 * Retrieve API Resource details
				 */
				if (!Configurables.resourceFlg) {
					Configurables.resourceFlg = false;
					query = "Select * From \"" + "Resources" + "\"";
				}

				/*
				 * Repeat data load for repeated TCs.
				 */
				String testCaseID = struct.getParameters().get("testCaseID");
				if (testCaseID != null && testCaseID.contains("_")) {
					testCaseID = testCaseID.split("_")[0];
				}

				HashMap<String, String> criteria = new HashMap<String, String>(); // filtering criteria
				Table sheetData = new Table();

				if (Configurables.gateway_IncludeInRun.equals("IncludeInRun")
						&& !dataSources[i].equals(Configurables.gateway_TestConfigSheetName)) {
					query = "Select * From " + Utils.quote(dataSources[i]) + " where "
							+ Utils.quote(Configurables.gateway_IncludeInRun) + " = "
							+ Utils.sQuote(Configurables.gateway_IncludeInRun_value) + " and "
							+ Utils.quote(Configurables.gateway_sheet_ID) + " = "
							+ struct.getParameters().get("TestID");

					sheetData.setColumns(Configurables.testTableMap.get(dataSources[i]).getRandomRecord());
					sheetData.setRecords(Configurables.testTableMap.get(dataSources[i])
							.getRecords(Configurables.excelTestIDColumn, testCaseID));

				} else if (dataSources[i].equals(Configurables.gateway_TestConfigSheetName)) {
					query = "Select * From \"" + dataSources[i] + "\"" + " where "
							+ Utils.quote(Configurables.gateway_EnvironmentCoulmnName) + " = " + env + " and "
							+ Utils.quote("Resource") + " = " + Utils.sQuote(instanceResource);
					sheetData.setColumns(Configurables.globalEnvTable.getRandomRecord());
					;
					sheetData.setRecords(Configurables.globalEnvTable.getRecords("Resource", instanceResource));
				} else {
					query = "Select * From \"" + dataSources[i] + "\"";
				}

				// new code for swagger
				if (sheetData.recordCount() == 0) {
					Record autoGeneratedRecord = SwaggerData.getRecord(dataSources[i],
							struct.getParameters().get("TestID"), struct.getParameters().get("testCaseID"));
					if (autoGeneratedRecord != null) {
						sheetData.addRecord(autoGeneratedRecord);
					}
				}
				// new code ends

				Table results = new Table();
				results.setColumns(sheetData.getColumns());

				for (String id : ids) {
					String tempID = "ID";

					if (sheetData.getColumns().contains("ENVIRONMENT")) {
						tempID = Configurables.excelIdColumn.toUpperCase();
					} else {
						tempID = Configurables.excelTestSuiteIDColumn.toUpperCase();
					}

					List<Record> records = sheetData.getRecords(tempID, id);
					for (Record r : records) {
						results.addRecord(r);
					}
				}

				// if any of the results match config id, only use those
				if (struct.getConfigID() != null && results.getColumns().contains(Configurables.excelConfigIDColumn)) {
					Table configResults = new Table();
					for (Record r : results.getRecords()) {
						String recordConfigID = r.getValue(Configurables.excelConfigIDColumn);
						if (recordConfigID != null && recordConfigID.equalsIgnoreCase(struct.getConfigID())) {
							configResults.addRecord(r);
						}
					}
					if (configResults.recordCount() > 0) {
						results = configResults;
					}
				}

				// add all matching records' fields as parameters, appending _${index} if
				// multiple records
				for (int j = 0; j < results.getRecords().size(); j++) {
					Record data = results.getRecord(j);
					for (String key : data.getColumns()) {
						String value = data.getValue(key);
						if (j > 0) {
							key += "_$" + j;
						}
						struct.addParameter(key.toLowerCase(), value);
					}
				}
			}
		}
	}


	private void setComponentMap() {
		ComponentContext context = new ComponentContext("createJSONFile", "GenerateJSONFile",
				"com.javaexcel.test.steps", "API TEST");
		componentMap.put("Generate JSON file based on Input Parameters", context);
		componentMapSort.put(1, "Generate JSON file based on Input Parameters");
		context = new ComponentContext("sendRequest", "SendAPIRequest", "com.javaexcel.test.steps", "API TEST");
		componentMap.put("Send API Request", context);
		componentMapSort.put(2, "Send API Request");
		context = new ComponentContext("validateAPIResponse", "ValidateAPIResponse", "com.javaexcel.test.steps",
				"API TEST");
		componentMap.put("Validate the API Response based on Expected Results defined.", context);
		componentMapSort.put(3, "Validate the API Response based on Expected Results defined.");

	}
}
