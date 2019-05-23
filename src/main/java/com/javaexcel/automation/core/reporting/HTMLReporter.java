package com.javaexcel.automation.core.reporting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.testng.IClass;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.xml.XmlSuite;
import org.uncommons.reportng.AbstractReporter;
import org.uncommons.reportng.ReportNGException;

import com.google.common.io.Files;
import com.javaexcel.automation.core.data.Config;
import com.javaexcel.automation.core.data.Configurables;
import com.javaexcel.automation.core.execution.TestManager;
import com.javaexcel.automation.core.listeners.CustomLogger;
import com.javaexcel.automation.core.utils.Utils;


public class HTMLReporter extends AbstractReporter{
	private static final String FRAMES_PROPERTY = "org.uncommons.reportng.frames";
	private static final String ONLY_FAILURES_PROPERTY = "org.uncommons.reportng.failures-only";

	private static final String TEMPLATES_PATH = "templates/html/";
	private static final String INDEX_FILE = "index.html";
	private static final String SUITES_FILE = "suites.html";
	private static final String OVERVIEW_FILE = "overview.html";
	private static final String GROUPS_FILE = "groups.html";
	private static final String RESULTS_FILE = "results.html";
	private static final String OUTPUT_FILE = "output.html";
	private static final String CUSTOM_STYLE_FILE = "custom.css";
	private static final String CLASS_RESULTS_TEMPLATE_PATH = TEMPLATES_PATH + "class-results.html.vm";
	private static final String CLASS_RESULTS_TEMPLATE_KEY = "classResultsTemplatePath";
	private static final String SUITE_KEY = "suite";
	private static final String SUITES_KEY = "suites";
	private static final String GROUPS_KEY = "groups";
	private static final String RESULT_KEY = "result";
	private static final String FAILED_CONFIG_KEY = "failedConfigurations";
	private static final String SKIPPED_CONFIG_KEY = "skippedConfigurations";
	private static final String FAILED_TESTS_KEY = "failedTests";
	private static final String SKIPPED_TESTS_KEY = "skippedTests";
	private static final String PASSED_TESTS_KEY = "passedTests";
	private static final String REQ_ID = "reqId";
	private static final String ALL_TESTS_KEY = "allTests";
	private static final String ONLY_FAILURES_KEY = "onlyReportFailures";
	private static final String REPORT_DIRECTORY = Configurables.uploadDirectory;

	private static final Comparator<ITestNGMethod> METHOD_COMPARATOR = new TestMethodComparator();
	private static final Comparator<ITestResult> RESULT_COMPARATOR = new TestResultComparator();
	private static final Comparator<IClass> CLASS_COMPARATOR = new TestClassComparator();
	public String fileName;

	public HTMLReporter()
	{
		super(TEMPLATES_PATH);
	}


	/**
	 * Generates a set of HTML files that contain data about the outcome of
	 * the specified test suites.
	 * @param suites Data about the test runs.
	 * @param outputDirectoryName The directory in which to create the report.
	 */
	@Override
	public void generateReport(List<XmlSuite> xmlSuites,
			List<ISuite> suites,
			String outputDirectoryName)
	{
		outputDirectoryName = REPORT_DIRECTORY;
		System.out.println("Report Path: "+outputDirectoryName);
		removeEmptyDirectories(new File(outputDirectoryName));

		System.setProperty("org.uncommons.reportng.escape-output", "false");

		boolean useFrames = Config.getProp(FRAMES_PROPERTY, "true").equals("true");
		boolean onlyFailures = Config.getProp(ONLY_FAILURES_PROPERTY, "false").equals("true");

		String id = Configurables.runID == null ? TestManager.defaultRunId : Configurables.runID;
		File outputDirectory = new File(outputDirectoryName);
		outputDirectory.mkdirs();

		try
		{
			if (useFrames)
			{
				createFrameset(outputDirectory);
			}
			createOverview(suites, outputDirectory, !useFrames, onlyFailures);
			createSuiteList(suites, outputDirectory, onlyFailures);
			createGroups(suites, outputDirectory);
			createResults(suites, outputDirectory, onlyFailures);
			createLog(outputDirectory, onlyFailures);
			copyResources(outputDirectory);
			copyIconFiles("src/test/resources/reporting/home_button.ico",outputDirectory+"/home_button.ico");
			copyIconFiles("src/test/resources/reporting/back_button.ico",outputDirectory+"/back_button.ico");
			copyIconFiles("src/test/resources/reporting/forward_button.ico",outputDirectory+"/forward_button.ico");
			copyIconFiles("src/test/resources/reporting/jenkins.ico",outputDirectory+"/jenkins.ico");
			copyIconFiles("src/test/resources/reporting/pdf.ico",outputDirectory+"/pdf.ico");
			copyIconFiles("src/test/resources/reporting/log.ico",outputDirectory+"/log.ico");
			copyIconFiles("src/test/resources/reporting/alm.ico",outputDirectory+"/alm.ico");

			
			//Utils.zipFolder(outputDirectory, "test-output/latest-results.zip");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ReportNGException("Failed generating HTML report.", ex);
		}
	}


	/**
	 * Create the index file that sets up the frameset.
	 * @param outputDirectory The target directory for the generated file(s).
	 */
	private void createFrameset(File outputDirectory) throws Exception
	{
		VelocityContext context = createContext();
		generateFile(new File(outputDirectory, INDEX_FILE),INDEX_FILE + TEMPLATE_EXTENSION,context);
	}


	private void createOverview(List<ISuite> suites,
			File outputDirectory,
			boolean isIndex,
			boolean onlyFailures) throws Exception
	{
		VelocityContext context = createContext();
		context.put(SUITES_KEY, suites);
		context.put(ONLY_FAILURES_KEY, onlyFailures);
		context.put("HTTP_STATUS_PASS", CustomLogger.httpStatusPass);
		context.put("HTTP_STATUS_FAIL", CustomLogger.httpStatusFail);
		Set<String> httpStatusKeys = new HashSet<>();
		httpStatusKeys.addAll(CustomLogger.httpStatusPass.keySet());
		httpStatusKeys.addAll(CustomLogger.httpStatusFail.keySet());
		context.put("HTTP_STATUS_KEYS", httpStatusKeys);
		context.put("numberTool", new NumberTool());
		context.put("math", new MathTool()) ;
		
		//System.out.println("Debug status keys: **** "+httpStatusKeys);
		
		
		generateFile(new File(outputDirectory, isIndex ? INDEX_FILE : OVERVIEW_FILE),
				OVERVIEW_FILE + TEMPLATE_EXTENSION,
				context);
	}


	/**
	 * Create the navigation frame.
	 * @param outputDirectory The target directory for the generated file(s).
	 */
	private void createSuiteList(List<ISuite> suites,
			File outputDirectory,
			boolean onlyFailures) throws Exception
	{
		VelocityContext context = createContext();
		context.put(SUITES_KEY, suites);
		context.put(ONLY_FAILURES_KEY, onlyFailures);
		generateFile(new File(outputDirectory, SUITES_FILE),
				SUITES_FILE + TEMPLATE_EXTENSION,
				context);
	}


	/**
	 * Generate a results file for each test in each suite.
	 * @param outputDirectory The target directory for the generated file(s).
	 */
	private void createResults(List<ISuite> suites,
			File outputDirectory,
			boolean onlyShowFailures) throws Exception
	{
		int index = 1;
		for (ISuite suite : suites)
		{
			
			int index2 = 1;
			for (ISuiteResult result : suite.getResults().values())
			{
				//System.out.println("Suite: "+result.getTestContext().getName());
				boolean failuresExist = result.getTestContext().getFailedTests().size() > 0
						|| result.getTestContext().getFailedConfigurations().size() > 0;
						if (!onlyShowFailures || failuresExist)
						{
							VelocityContext context = createContext();
							
							context.put(CLASS_RESULTS_TEMPLATE_KEY, CLASS_RESULTS_TEMPLATE_PATH);
							context.put(RESULT_KEY, result);
							
							context.put(FAILED_CONFIG_KEY, sortByTestClass(result.getTestContext().getFailedConfigurations()));
							context.put(SKIPPED_CONFIG_KEY, sortByTestClass(result.getTestContext().getSkippedConfigurations()));
							context.put(FAILED_TESTS_KEY, sortByTestClass(result.getTestContext().getFailedTests()));
							context.put(SKIPPED_TESTS_KEY, sortByTestClass(result.getTestContext().getSkippedTests()));
							context.put(PASSED_TESTS_KEY, sortByTestClass(result.getTestContext().getPassedTests()));
							
							
							Map<IClass, List<ITestResult>> resultMap = sortByTestClass(result.getTestContext().getFailedConfigurations());
							resultMap.putAll(sortByTestClass(result.getTestContext().getSkippedConfigurations()));
							resultMap.putAll(sortByTestClass(result.getTestContext().getFailedTests()));
							resultMap.putAll(sortByTestClass(result.getTestContext().getSkippedTests()));
							resultMap.putAll(sortByTestClass(result.getTestContext().getPassedTests()));
							
							

							context.put(ALL_TESTS_KEY, resultMap);
							//System.out.println("Results: "+(resultMap));
							 fileName = String.format("suite%d_test%d_%s", index, index2, RESULTS_FILE);
							
							generateFile(new File(outputDirectory, fileName),
									RESULTS_FILE + TEMPLATE_EXTENSION,
									context);
							
							/*
							 * Update test headers in the results report
							 */
							//updateTestRpt("Components","Test Steps");
						}
						++index2;
			}
			++index;
			
		}
		
		
		
	}


	/**
	 * Group test methods by class and sort alphabetically.
	 */
	private SortedMap<IClass, List<ITestResult>> sortByTestClass(IResultMap results)
	{
		SortedMap<IClass, List<ITestResult>> sortedResults = new TreeMap<IClass, List<ITestResult>>(CLASS_COMPARATOR);
		for (ITestResult result : results.getAllResults())
		{
			List<ITestResult> resultsForClass = sortedResults.get(result.getTestClass());
			if (resultsForClass == null)
			{
				resultsForClass = new ArrayList<ITestResult>();
				sortedResults.put(result.getTestClass(), resultsForClass);
			}
			int index = Collections.binarySearch(resultsForClass, result, RESULT_COMPARATOR);
			if (index < 0)
			{
				index = Math.abs(index + 1);
			}
			resultsForClass.add(index, result);
		}
		return sortedResults;
	}



	/**
	 * Generate a groups list for each suite.
	 * @param outputDirectory The target directory for the generated file(s).
	 */
	private void createGroups(List<ISuite> suites,
			File outputDirectory) throws Exception
	{
		int index = 1;
		for (ISuite suite : suites)
		{
			SortedMap<String, SortedSet<ITestNGMethod>> groups = sortGroups(suite.getMethodsByGroups());
			if (!groups.isEmpty())
			{
				VelocityContext context = createContext();
				context.put(SUITE_KEY, suite);
				context.put(GROUPS_KEY, groups);
				String fileName = String.format("suite%d_%s", index, GROUPS_FILE);
				generateFile(new File(outputDirectory, fileName),
						GROUPS_FILE + TEMPLATE_EXTENSION,
						context);
			}
			++index;
		}
	}


	/**
	 * Generate a groups list for each suite.
	 * @param outputDirectory The target directory for the generated file(s).
	 */
	private void createLog(File outputDirectory, boolean onlyFailures) throws Exception
	{
		if (!Reporter.getOutput().isEmpty())
		{
			VelocityContext context = createContext();
			context.put(ONLY_FAILURES_KEY, onlyFailures);
			generateFile(new File(outputDirectory, OUTPUT_FILE),
					OUTPUT_FILE + TEMPLATE_EXTENSION,
					context);
		}
	}


	/**
	 * Sorts groups alphabetically and also sorts methods within groups alphabetically
	 * (class name first, then method name).  Also eliminates duplicate entries.
	 */
	private SortedMap<String, SortedSet<ITestNGMethod>> sortGroups(Map<String, Collection<ITestNGMethod>> groups)
	{
		SortedMap<String, SortedSet<ITestNGMethod>> sortedGroups = new TreeMap<String, SortedSet<ITestNGMethod>>();
		for (Map.Entry<String, Collection<ITestNGMethod>> entry : groups.entrySet())
		{
			SortedSet<ITestNGMethod> methods = new TreeSet<ITestNGMethod>(METHOD_COMPARATOR);
			methods.addAll(entry.getValue());
			sortedGroups.put(entry.getKey(), methods);
		}
		return sortedGroups;
	}


	/**
	 * Reads the CSS and JavaScript files from the JAR file and writes them to
	 * the output directory.
	 * @param outputDirectory Where to put the resources.
	 * @throws IOException If the resources can't be read or written.
	 */
	private void copyResources(File outputDirectory) throws IOException
	{
		copyClasspathResource(outputDirectory, "reportng.css", "reportng.css");
		copyClasspathResource(outputDirectory, "reportng.js", "reportng.js");
		
		File customStylesheet = META.getStylesheetPath();

		if (customStylesheet != null)
		{
			if (customStylesheet.exists())
			{
				copyFile(outputDirectory, customStylesheet, CUSTOM_STYLE_FILE);
			}
			else
			{
				// If not found, try to read the file as a resource on the classpath
				// useful when reportng is called by a jarred up library
				InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(customStylesheet.getPath());
				if (stream != null)
				{
					copyStream(outputDirectory, stream, CUSTOM_STYLE_FILE);
				}
			}
		}
	}
	
	public void updateTestRpt(String oldStr, String newStr){
		try {
	       
			String filePath = REPORT_DIRECTORY+"/"+fileName;
	        BufferedReader file = new BufferedReader(new FileReader(filePath));
	        String line;
	        StringBuffer inputBuffer = new StringBuffer();

	        while ((line = file.readLine()) != null) {
	            inputBuffer.append(line);
	            inputBuffer.append('\n');
	        }
	        String inputStr = inputBuffer.toString();

	        file.close();
	        
	        /*
	         * Replace String
	         */
	        inputStr = inputStr.replace(">"+oldStr+"<", ">"+newStr+"<"); 
	
	        // write the new String with the replaced line OVER the same file
	        FileOutputStream fileOut = new FileOutputStream(filePath);
	        fileOut.write(inputStr.getBytes());
	        fileOut.close();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
	/**
	 *  Copies icon file
	 * @throws IOException
	 */
    public static void copyIconFiles(String source, String target) throws IOException {

        try{
	    	Path sourceDirectory = Paths.get(source);
	        Path targetDirectory = Paths.get(target);
	        java.nio.file.Files.copy(sourceDirectory, targetDirectory);
        }
        catch(Exception e){
        	e.getMessage();
        }
        

    }
    
}