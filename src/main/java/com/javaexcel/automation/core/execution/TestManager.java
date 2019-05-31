package com.javaexcel.automation.core.execution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.itextpdf.text.DocumentException;
import com.javaexcel.automation.core.data.Config;
import com.javaexcel.automation.core.data.Configurables;
import com.javaexcel.automation.core.data.ITestInstance;
import com.javaexcel.automation.core.data.ITestSet;
import com.javaexcel.automation.core.data.TestInstance;
import com.javaexcel.automation.core.data.TestSet;
import com.javaexcel.automation.core.enums.DataInputType;
import com.javaexcel.automation.core.enums.TestFilterType;
import com.javaexcel.automation.core.listeners.CustomLogger;
import com.javaexcel.automation.core.reporting.TestReportComparator;
import com.javaexcel.automation.core.table.Record;
import com.javaexcel.automation.core.table.Table;
import com.javaexcel.automation.core.testngdata.TestNGSuite;
import com.javaexcel.automation.core.utils.Excel;
import com.javaexcel.automation.core.utils.TestUtils;
import com.javaexcel.automation.core.utils.Utils;
import com.javaexcel.automation.core.utils.XMLGenerator;
import com.javaexcel.automation.core.swagger.SwaggerData;

public class TestManager implements ITestManager {

	public static final String defaultRunId = Configurables.projectName + "_" + Utils.getDateTimeForFileName();
	public static String[] testCaseIDSplit;
	public static int tcCount = 0;
	long startTime = System.currentTimeMillis();
	long elapsedTime = System.currentTimeMillis();
	public static int totalTC = 0;
	public static String envName = "";
	public Map<String, ITestInstance> testCaseMap = new HashMap<>();

	@Override
	public void execute() throws Exception {

		/*
		 * Clear old reports
		 */
		Utils.deleteFolderContent("reports/html/local");
		Utils.deleteFolderContent("reports/json/request");
		Utils.deleteFolderContent("reports/json/response");
		Utils.deletePastFolderContent("reports/pdf");
		Utils.deleteFolderContent("test-output");
		Utils.deleteFolderContent("test-output/junitreports");
		Utils.deleteFolderContent("test-output/old");
		Utils.deleteFolderContent("test-output/xml");
		Utils.deleteFolderContent("test-output/" + Config.getProp("API_Name"));
		Utils.deleteFolderContent("test-output/old/" + Config.getProp("API_Name"));
		Utils.delteAllFolderContents("test-output");

		String jobName = Config.getProp("jobName");

		/*
		 * Update html report upload folder
		 */
		if (jobName.equals("local")) {
			Configurables.uploadDirectory = Configurables.uploadDirectory + "/local";
		} else {
			String build = Config.getProp("build");
			if (build.equals("") || build == null) {
				Configurables.uploadDirectory = Configurables.uploadDirectory + "/local";
			} else {
				Configurables.uploadDirectory = Configurables.uploadDirectory + "/" + build;
			}

		}

		/*
		 * Initialize PDF filename for the current run
		 */
		String ts = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		Utils.pdfFileName = "API_Test_Result_" + Config.getProp("Environment") + "_" + Config.getProp("tagName") + "_"
				+ ts + ".pdf";

		if (Config.getBoolProp("UseDefaultRunID", "Yes")) {
			Configurables.runID = defaultRunId;
		} else {
			Configurables.runID = Configurables.runID == null ? defaultRunId : Configurables.runID;
		}

		String suiteName = Configurables.projectName;

		List<String> listeners = new ArrayList<String>(Arrays.asList(
				"com.javaexcel.automation.core.reporting.HTMLReporter", "org.testng.reporters.JUnitXMLReporter",
				"com.javaexcel.automation.core.listeners.CustomLogger"));
		listeners.addAll(TestUtils.getCustomListeners());

		List<ITestSet> testSets = getTestSets();
		addTestSetsToMap(testSets);

		if (Configurables.dataInputType != DataInputType.XML) {
			SuiteCreator suiteCreator = null;

			switch (Configurables.dataInputType) {
			case EXCEL:
				suiteCreator = new ExcelSuite();
				break;

			}

			// Execution parameters
			String parallel = Configurables.parallel ? "tests" : "";
			TestNGSuite suite = suiteCreator.createSuite(testSets);
			suite.setName(suiteName);
			suite.setParallel(parallel);
			suite.setThreadCount(Configurables.threadCount);
			XMLGenerator.generateXML2(suite, suiteName, listeners);

			// Execute suite
			System.out.println(Utils.getDateTime() + " Starting test...");
			Main.executeSuite(suite);

			if (CustomLogger.totalFail > 0) {
				System.err.println("Failed Test Cases: \n" + CustomLogger.failedTCs);
			}

			System.out.println(Utils.getDateTime() + " Execution Complete. Generating PDF Report...");
			try {
				Utils.createPDF();
				if (Utils.pdfReport) {
					System.out.println(Utils.getDateTime() + " PDF Report generated successfully. \nTotal Time(mm:ss): "
							+ Utils.totalTime((System.currentTimeMillis() - elapsedTime)));
				}
			} catch (DocumentException | IOException e) {
				System.err.println("Error creating PDF report... " + e.getMessage());
			}

			/*
			 * Log Test Summary to Console
			 */
			TimeUnit.MILLISECONDS.sleep(100);
			if (CustomLogger.totalFail > 0) {
				System.err.println(Utils.getDateTime() + " *****TEST EXECUTION SUMMARY*****");
				System.err.println("=====================================================");
				System.err.println("     Total Executed: " + totalTC);
				System.err.println("     Total Passed: " + CustomLogger.totalPass);
				System.err.println("     Total Failed: " + CustomLogger.totalFail);
				System.err.println("     Pass %: " + (CustomLogger.totalPass * 100) / totalTC + "%");
				System.err.println("=====================================================");

			} else {
				System.out.println(Utils.getDateTime() + " *****TEST EXECUTION SUMMARY*****");
				System.out.println("=====================================================");
				System.out.println("     Total Executed: " + totalTC);
				System.out.println("     Total Passed: " + CustomLogger.totalPass);
				System.out.println("     Total Failed: " + CustomLogger.totalFail);
				System.out.println("     Pass %: " + (CustomLogger.totalPass * 100) / totalTC + "%");
				System.out.println("=====================================================");

			}
			// String jobName=Config.getProp("jobName");
			String jenkinsURL = Config.getProp("jenkinsURL");

			TimeUnit.MILLISECONDS.sleep(100);
			if (jobName.equals("local")) {
				System.out.println("\n\nFor details, review the Test Report available at "
						+ Configurables.uploadDirectory + "/index.html\n");
				System.out.println("Link to PDF report: " + "/reports/pdf/" + Utils.pdfFileName);

			} else {
				System.out.println("\n\nFor details, review the HTML Test Report available at " + jenkinsURL + "/job/"
						+ jobName + "/ws/" + Configurables.uploadDirectory + "/index.html\n");
				System.out.println("Link to PDF report: " + jenkinsURL + "/job/" + jobName + "/ws/reports/pdf/"
						+ Utils.pdfFileName);

				/*
				 * copy latest html report to lastestBuildDirectory
				 */
				String source = Configurables.uploadDirectory;
				File srcDir = new File(source);
				String destination = Configurables.lastestBuildDirectory;
				File destDir = new File(destination);
				try {
					FileUtils.copyDirectory(srcDir, destDir);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			/**
			 * Upload JUNIT test results to ALM
			 */
			// Utils.updateReqALM();
			Utils.uploadToALM();

		} else {
			Main.executeTest(suiteName);
		}

	}

	/**
	 * Creates a summary report of all the Test cases executed with pass n fail
	 * status. This Summary report will be included in the Email body as part of the
	 * email notification post a jenkins daily execution job completion
	 */
	public void createSummaryReport() {
		String strSummaryFileContent = "<!DOCTYPE HTML><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><title>Test Case Name</title></head><body><table width=\"80%\" border=\"2\" align=\"center\">"
				+ "<tr><td colspan=\"2\" align=\"left\"><b>Info: " + Config.getProp("URL")
				+ "</b></td><td colspan=\"1\" align=\"left\"><b>BrowserName: " + Config.getProp("Browser")
				+ "</b></td></td><td colspan=\"1\" align=\"left\"><b>"
				+ (new SimpleDateFormat("MM/dd/yyyy").format(new Date(System.currentTimeMillis()))) + "</b></td></tr>"
				+ "<tr><td align=\"center\" width=\"10%\" bgcolor=\"#deb887\"><b>S/No.</b></td></td><td align=\"left\" width=\"40%\" bgcolor=\"#deb887\"><b>TESTCASE NAME</b></td><td align=\"left\" width=\"40%\" bgcolor=\"#deb887\"><b>TEST ID</b></td><td align=\"center\" width=\"10%\" bgcolor=\"#deb887\"><b><a>STATUS</a></b></td></tr>";

		String strRelativePath = System.getProperty("user.dir");

		Iterator mapIterator = testCaseMap.entrySet().iterator();

		int intTestCounter = 0;
		int intPassCounter = 0;
		int intFailCounter = 0;
		String strFonColor = "";

		while (mapIterator.hasNext()) {
			intTestCounter++;
			Map.Entry pair = (Map.Entry) mapIterator.next();

			if (testCaseMap.get(pair.getKey()).getStatus().toLowerCase().equals("passed")) {
				intPassCounter++;
				strFonColor = "green";
			}
			if (testCaseMap.get(pair.getKey()).getStatus().toLowerCase().equals("failed")) {
				intFailCounter++;
				strFonColor = "red";
			}

			strSummaryFileContent = strSummaryFileContent
					+ "<tr><td align=\"center\" width=\"10%\" bgcolor=\"#deb887\"><b>Test " + intTestCounter
					+ "</b></td><td align=\"left\" width=\"40%\" bgcolor=\"#f0e68c\"><b>"
					+ testCaseMap.get(pair.getKey()).getName() + "</b></td>"
					+ "<td align=\"left\" width=\"40%\" bgcolor=\"#f0e68c\"><b>"
					+ testCaseMap.get(pair.getKey()).getTestID()
					+ "</b></td><td align=\"center\" width=\"10%\" bgcolor=\"#deb887\"><b><font color=\"" + strFonColor
					+ "\"><a>" + testCaseMap.get(pair.getKey()).getStatus() + "</a></font></b></td></tr>";
			;

		}
		strSummaryFileContent = strSummaryFileContent
				+ "<tr><td colspan=\"4\" align=\"left\" width=\"100%\" bgcolor=\"#deb887\"><b>Execution Logs @ "
				+ Config.getProp("UploadDirectory").replace("/", "\\") + "\\" + Config.getProp("runid")
				+ "</b></td></tr>"
				+ "<tr><td colspan=\"4\" align=\"right\" width=\"100%\" bgcolor=\"#deb887\"><b><font color=\"green\"><a>PASSED</a></font>: "
				+ intPassCounter + "<font color=\"red\"><a> FAILED</a></font>: " + intFailCounter
				+ "</b></td></tr></table></body></html>";

		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream("Summary.html"), "utf-8"))) {
			writer.write(strSummaryFileContent);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Builds Test Sets based on input params from the test data spreadsheet
	 **/
	private List<ITestSet> getTestSets() {
		List<ITestSet> testSets = new ArrayList<ITestSet>();

		{

			String testSuites = Configurables.testCases;
			String[] testSuiteSplit = Utils.splitTrim(testSuites, Configurables.testCaseDelimiter);
			ITestSet testSet = new TestSet();
			TestFilterType filterType = TestUtils.getTestFilterType();

			int id = 0;
			System.out.println(Utils.getDateTime() + " Loading Test Parameters:");
			String tagName = Config.getProp("tagName");
			envName = Config.getProp("Environment");
			System.out.println("Environment: " + envName);
			System.out.println("tagName: " + tagName);
			System.out.println("Test File: " + Config.getProp("TestFile"));

			if (System.getProperty("UserID") == null)
				System.setProperty("UserID", "");
			if (System.getProperty("Authentication_Key") == null)
				System.setProperty("Authentication_Key", "");
			if (System.getProperty("EntityID") == null)
				System.setProperty("EntityID", "");
			/*
			 * Flag to turn on/off forced use of individual accounts
			 */
			Boolean accountEnabled = false;
			if (envName.contains("SANDBOX") || envName.contains("CERTIFICATION") || envName.contains("PRODUCTION")) {
				try {
					accountEnabled = Boolean.parseBoolean(Config.getProp("Enable_Individual_Account"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (accountEnabled && (System.getProperty("Authentication_Key").equals(""))
						|| accountEnabled && (System.getProperty("EntityID").equals(""))
						|| accountEnabled && (System.getProperty("UserID").equals(""))) {
					System.out.println(
							"Aborting test run... Please enter the Authentication_Key, EntityId and UserID and re-run");
					System.exit(1);
				}

			}

			/*
			 * Get In-Scope Resource Names from the Tests tab
			 */

			String temp_qry = "Select * from Tests Where Flag='Y'";
			Table resources = new Excel(Configurables.testsFilePath + Configurables.testFileName)
					.querySheetRecords(temp_qry);

			String test_names[] = resources.toString().replace("}", ",").split(",");
			int j = 0, k = 0, m = 0;
			HashMap<Integer, String> dataSources = new HashMap<Integer, String>();
			HashMap<Integer, String> resourceNames = new HashMap<Integer, String>();
			HashMap<Integer, String> testSuiteNames = new HashMap<Integer, String>();

			for (int i = 0; i < test_names.length; i++) {
				String tmp[] = test_names[i].replace("{", "").replace("}", "").split("=");

				if (tmp[0].trim().equalsIgnoreCase("Data Source")) {
					dataSources.put(j++, tmp[1].trim());
				}

				if (tmp[0].trim().equalsIgnoreCase("API Resource Name")) {
					resourceNames.put(k++, tmp[1].trim());

				}

				if (tmp[0].trim().equalsIgnoreCase("Test Suite Name")) {
					testSuiteNames.put(m++, tmp[1].trim());
				}

			}

			Set<String> setDataSources = new HashSet<>();
			Set<String> setTestSuites = new HashSet<>();

			String query = "";
			System.out.println("\n" + Utils.getDateTime() + " Identifying Test Suites...");
			for (int i = 0; i < dataSources.size(); i++) {

				String dataSource = dataSources.get(i);
				String resourceName = resourceNames.get(i);
				String testSuiteName = testSuiteNames.get(i);

				for (String testSuite : testSuiteSplit) {
					if (setDataSources.contains(dataSource)) {
						continue;
					}

					if (setTestSuites.contains(testSuite)) {
						continue;
					}

					setTestSuites.add(testSuite);

					int queryType = 1;
					while (queryType < 2) {
						if (queryType == 1) {
							query = "Select * from " + Utils.quote(dataSource) + " Where IncludeInRun='Y' and "
									+ Utils.quote("tagName") + " like " + Utils.sQuote("%" + tagName + "%") + "and "
									+ Utils.quote("Test Suite ID") + " = " + Utils.sQuote(testSuite);

						} else if (queryType == 2) {
							query = "Select * from " + Utils.quote(dataSource) + " Where IncludeInRun='Y' and "
									+ Utils.quote("tagName") + " = " + Utils.sQuote("Wso2, " + tagName) + " and "
									+ Utils.quote("Test Suite ID") + " = " + Utils.sQuote(testSuite);

						} else if (queryType == 3) {
							query = "Select * from " + Utils.quote(dataSource) + " Where IncludeInRun='Y' and "
									+ Utils.quote("tagName") + " = " + Utils.sQuote(tagName + ", Sandbox") + " and "
									+ Utils.quote("Test Suite ID") + " = " + Utils.sQuote(testSuite);
						}

						System.out.println(Utils.getDateTime() + " Loading Test Suite data: " + query);
						Table testSheetData1 = new Excel(Configurables.testsFilePath + Configurables.testFileName)
								.querySheetRecords(query);
						Configurables.testTableMap.put(dataSource, testSheetData1);

						/*
						 * Get Test Case IDs
						 */
						dataSource = dataSource.trim();
						testSheetData1 = getTestCaseIDs(dataSource, testSheetData1);
						String TestCaseID = Configurables.testIDs;
						testCaseIDSplit = Utils.splitTrim(TestCaseID, Configurables.testCaseDelimiter);

						/*
						 * loads resource and env tables
						 */
						Utils.loadGlobalRefTbls();

						/*
						 * Invoke auto test case generation based on swagger inputs
						 */
						if (testSheetData1.recordCount() > 0
								&& Config.getProp("enableAutoTCs").equalsIgnoreCase("yes")) {
							SwaggerData swagger = new SwaggerData();
							swagger.generateTestCases(dataSource, testSuite, testSheetData1, resourceName,
									testSuiteName);
							testCaseIDSplit = swagger.getTestCaseIDSplit(testSheetData1);

						}

						for (String testCase : testCaseIDSplit) {
							TestInstance ti = new TestInstance(testSet);

							if (filterType == TestFilterType.TestID) {
								if (!Configurables.TestWithMultiConfig) {
									ti.setTestID(testSuite);
								} else {

									List<Record> records = testSheetData1.getRecords("Test Case ID", testCase);

									for (Record record : records) {

										ti = new TestInstance(testSet);
										ti.setTestID(testSuite);
										ti.setTCID(testCase);
										ti.setConfigID(record.getValue("Test Suite ID"));
										ti.setName(record.getValue("Scenario_Name"));
										ti.setResource(resourceName);
										ti.setID(String.valueOf(id++));
										testSet.add(ti);

									}

									tcCount++;
								}
							} else if (filterType == TestFilterType.Name) {

								ti.setName(testSuite);

							}

							if (!Configurables.TestWithMultiConfig) {
								ti.setID(String.valueOf(id++));

								testSet.add(ti);

							}

						}

						queryType++;
					}

					setDataSources.add(dataSource);

				}

			}

			testSets.add(testSet);

			totalTC = testSet.size();
			System.out.println(Utils.getDateTime() + " Total Test Cases Found: " + totalTC);
			if (testSet.size() > 0) {
				System.out.println(Utils.getDateTime() + " Building Test Sets...");
			} else {
				System.out.println("Aborting test run... Please verify the test scope and re-run.");
				System.exit(1);
			}

		}

		return testSets;
	}

	private void addTestSetsToMap(List<ITestSet> testSets) {
		for (ITestSet ts : testSets) {

			/*
			 * Sort test by IDs
			 */
			Collection<ITestInstance> tsort = ts.getInstances();
			List<ITestInstance> list = new ArrayList<>(tsort);
			Collections.sort(list, new TestReportComparator());

			for (ITestInstance ti : list) {
				String strTempKeyValue = ti.getKeyValue();

				while (testCaseMap.containsKey(strTempKeyValue)) {
					strTempKeyValue = strTempKeyValue + "$";

				}

				testCaseMap.put(ti.getID(), ti);
		
			}

		}

	}

	public Table getTestCaseIDs(String datasheet, Table testSheetData1) {

		Table testIDs = testSheetData1;
		List<Record> records = testIDs.getRecords();
		StringJoiner strTestCases = new StringJoiner("///");

		if (records.toString().contains("TEST CASE ID")) {

			/*
			 * Loops through each resource sheet to build the test set
			 */
			int itr = 1;
			ListIterator<Record> it = records.listIterator();
			while (it.hasNext()) {

				Record t = it.next();
				String temp1 = t.getField("SCENARIO_NAME").toString();
				String temp2 = t.getField("test case id").toString();
				int count = 0;
				if (t.getValue("Loop") != null) {
					String loop_count = t.getValue("Loop");
					count = loop_count.equals("") ? 0 : Integer.parseInt(loop_count);

				}

				if (count == 0) {
					strTestCases.add(t.getField(Configurables.excelTestIDColumn).toString().toUpperCase());
					Utils.loopFlg = false;
				} else {
					Utils.loopFlg = true;
				}

				/*
				 * For repeated TCs, duplicate data as needed.
				 */
				while (count-- > 0 && testSheetData1.getRecords().size() > 0) {
					Record record_tmp = new Record();
					record_tmp.append(t);
					record_tmp.add("SCENARIO_NAME", temp1 + "_" + String.valueOf(itr));
					record_tmp.add("TEST CASE ID", temp2 + "_" + String.valueOf(itr));
					testSheetData1.addRecord(record_tmp);
					strTestCases.add(record_tmp.getField(Configurables.excelTestIDColumn).toString().toUpperCase());
					itr++;

				}

			}

		}

		Configurables.testIDs = strTestCases.toString();
		return testSheetData1;

	}
}
