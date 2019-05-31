package com.javaexcel.automation.core.execution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.testng.Reporter;
import org.testng.TestNG;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;

import com.javaexcel.automation.core.data.Config;
import com.javaexcel.automation.core.data.Configurables;
import com.javaexcel.automation.core.data.ExcelConfigurer;
import com.javaexcel.automation.core.data.IConfigurer;
import com.javaexcel.automation.core.data.SystemPropertyConfigurer;
import com.javaexcel.automation.core.data.TestNGTestData;
import com.javaexcel.automation.core.data.TextConfigurer;
import com.javaexcel.automation.core.table.Record;
import com.javaexcel.automation.core.table.Table;
import com.javaexcel.automation.core.testngdata.TestNGSuite;
import com.javaexcel.automation.core.utils.Excel;
import com.javaexcel.automation.core.utils.TestUtils;
import com.javaexcel.automation.core.utils.Utils;

public class Main {

	public static Map<XmlSuite, TestNGSuite> suiteMap;
	public static ITestManager testManager;
	public static ThreadLocal<Integer> componentIndex = new ThreadLocal<Integer>();

	public static void main(String[] args) {

		execute();

	}

	// Default configuration entry point
	public static void execute() {
		try {
			Utils.loadProps();

			// Adds injected system variables to Config
			Configurables.updateConfigurablesValue();
			Config.debug = false;

			if (Configurables.TestFileWithExeFlag) {
				Excel testsFile = new Excel(Configurables.testsFilePath + Configurables.testFileName);

				String query = "Select * from " + Configurables.excelTestSheet + " Where  "
						+ Utils.quote(Configurables.gateway_flag) + " = "
						+ Utils.sQuote(Configurables.gateway_flag_value);

				Table testSheetData = testsFile.querySheetRecords(query);
				List<Record> records = testSheetData.getRecords();

				testsFile.close();
				StringJoiner strTestSuites = new StringJoiner("///");

				for (Record record : records) {
					strTestSuites.add(record.getField(Configurables.excelTestSuiteIDColumn).toString());

				}

				Configurables.testCases = strTestSuites.toString();

			}
			testManager = new TestManager();
			testManager.execute();

		} catch (Exception e) {

			Reporter.log("Exception Caught of Type: - " + e.getLocalizedMessage(), true);
			e.printStackTrace();
		}
	}

	public static void execute(IConfigurer... configurers) throws Exception {
		// Adds injected system variables to Config
		IConfigurer systemConfig = new SystemPropertyConfigurer();
		Map<String, String> configMap = systemConfig.getConfigMap();
		Config.addPropsIfAbsent(configMap);

		// Adds any additional configurers to Config
		for (IConfigurer configurer : configurers) {
			configMap = configurer.getConfigMap();
			Config.addPropsIfAbsent(configMap);
		}
		Config.debug = false;
		testManager = new TestManager();
		testManager.execute();
	}

	public static void executeTest() {
		TestNG testNG = new TestNG();
		try {
			testNG.setXmlSuites(new ArrayList<XmlSuite>(new Parser("testng.xml").parse()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		testNG.run();
	}

	public static void executeTest(String... suiteNames) {
		TestNG testNG = new TestNG();
		for (String suiteName : suiteNames) {
			try {
				testNG.setXmlSuites(new ArrayList<XmlSuite>(new Parser(suiteName + ".xml").parse()));
				System.out.println("TC " + testNG.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public static void executeSuites(List<TestNGSuite> suites) {
		TestNG testNG = new TestNG();
		List<XmlSuite> parsedSuites = getSuites(suites);
		testNG.setXmlSuites(parsedSuites);
		testNG.setUseDefaultListeners(false);

		try {
			testNG.run();
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage() + "\nAborting test.");
			System.exit(1);
		}
	}

	public static void executeSuite(TestNGSuite suite) {
		List<TestNGSuite> suites = new ArrayList<TestNGSuite>();
		suites.add(suite);
		executeSuites(suites);
	}

	private static List<XmlSuite> getSuites(List<TestNGSuite> suites) {
		suiteMap = new HashMap<XmlSuite, TestNGSuite>();
		List<XmlSuite> parsedSuites = new ArrayList<XmlSuite>();
		for (TestNGSuite suite : suites) {
			String suiteName = suite.getName();
			try {
				Collection<XmlSuite> c = new Parser(suiteName + ".xml").parse();
				parsedSuites.addAll(c);
				for (XmlSuite item : c) {
					suiteMap.put(item, suite);
				}

			} catch (IOException e) {
				e.printStackTrace();

			}
		}

		return parsedSuites;
	}

	public static List<TestNGTestData> getSingleClassTestCases(String testName, String className, String[] methodNames,
			String[][] methodParameters) {
		List<TestNGTestData> cases = new ArrayList<TestNGTestData>();

		String[] arrClassName = new String[] { className };
		String[][] arrMethodNames, arrParameters, arrOptions;
		String[][][] arrMethodParameters;

		arrMethodNames = new String[][] { methodNames };
		arrMethodParameters = new String[][][] { methodParameters };
		arrParameters = new String[][] { {} };

		arrOptions = new String[][] { {} };
		cases.add(new TestNGTestData("0", "0", testName, arrClassName, arrMethodNames, arrMethodParameters,
				arrParameters, arrOptions, ""));

		return cases;
	}
}
