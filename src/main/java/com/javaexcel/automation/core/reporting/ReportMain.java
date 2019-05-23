package com.javaexcel.automation.core.reporting;
//package com.wellsfargo.automation.core.reporting;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FilenameFilter;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.InetAddress;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.nio.file.attribute.BasicFileAttributes;
//import java.sql.SQLException;
//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//import org.apache.commons.exec.InputStreamPumper;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.filefilter.DirectoryFileFilter;
//import org.apache.commons.io.filefilter.FalseFileFilter;
//
//import com.itextpdf.text.log.SysoCounter;
//import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
//import com.javaexcel.automation.core.data.Config;
//import com.javaexcel.automation.core.data.Configurables;
//import com.wellsfargo.automation.core.enums.ExecutionLogType;
//import com.wellsfargo.automation.core.utils.Utils;
//
//public class ReportMain {
//	private String statusMain="PASS";
//	
////	private StringBuilder configIdsOfSummary=new StringBuilder();
//	
//	/*public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, NoSuchFieldException, SecurityException {
//		ReportMain rm = new ReportMain();
//		String testRunID = "e69d8038-e520-4f3b-b564-2266749b723e";
//		
//	}*/
//	@SuppressWarnings({ "deprecation", "unchecked" })
//	public void generateHtml(String testRunID) {
//		//System.exit(1);
//		try{
//			if(null !=testRunID){
//			//System.out.println("Inside generateHtml method with the runId"+ testRunID);
//			
//		File file = new File(Utils.addSlashIfNone(Configurables.almUploadDirectory)+ Utils.addSlashIfNone(Configurables.runID)+Configurables.summaryFile);
//		//File file2 = new File("htmlReports/"+testRunID+"_"+"new.html");
//		//File file3 = new File("htmlReports/summary_"+testRunID+"_"+"new.html");
//		File file2 = new File("htmlReports/"+"WFG.html");
//		File file3 = new File("htmlReports/Summary"+".html");
//		//System.out.println("File Name: "+file3.getName());
//		//File htmlTemplateFile = new File("//DTC230042A7FE82/GEExecutionResults/testEx_report_template/template.html");
//		//File htmlTemplateSummaryFile = new File("//DTC230042A7FE82/GEExecutionResults/testEx_report_template/summary_template.html");
//		File htmlTemplateFile = new File("templates/template.html");
//		File htmlTemplateSummaryFile = new File("templates/summary_template.html");
//		
//		String htmlSummaryString = FileUtils.readFileToString(htmlTemplateSummaryFile);
//		String htmlString = FileUtils.readFileToString(htmlTemplateFile);
//		TestReports testReports = new TestReports();
//		htmlString = htmlString.replace("$model.projectInfo.machineName", InetAddress.getLocalHost().getHostName());
//		htmlSummaryString = htmlSummaryString.replace("$model.projectInfo.machineName", InetAddress.getLocalHost().getHostName());
//        htmlString = htmlString.replace("$model.projectInfo.executionUser",System.getProperty("user.name"));
//        htmlSummaryString = htmlSummaryString.replace("$model.projectInfo.executionUser",System.getProperty("user.name"));
//        //System.out.println("Run ID: "+testRunID);
//		Map<String, Object> map = testReports.index(testRunID);
//		ProjectInfo projectInfo = (ProjectInfo) map.get("projectInfo");
////		htmlString = htmlString.replace("$model.projectInfo.project", projectInfo.getProject());
////		htmlSummaryString = htmlSummaryString.replace("$model.projectInfo.project", projectInfo.getProject());
////		if(projectInfo.getEnvironment()!=null){
////		htmlString=htmlString.replace("$model.projectInfo.environment", projectInfo.getEnvironment());
////		htmlSummaryString=htmlSummaryString.replace("$model.projectInfo.environment", projectInfo.getEnvironment());
////		}
////		htmlString=htmlString.replace("$model.projectInfo.testRunId", projectInfo.getTestRunId());
////		htmlSummaryString=htmlSummaryString.replace("$model.projectInfo.testRunId", projectInfo.getTestRunId());
////		htmlString=htmlString.replace("$model.projectInfo.machineName", projectInfo.getMachineName());
////		htmlSummaryString=htmlSummaryString.replace("$model.projectInfo.machineName", projectInfo.getMachineName());
////		htmlString=htmlString.replace("$model.projectInfo.executionUser", projectInfo.getExecutionUser());
////		htmlSummaryString=htmlSummaryString.replace("$model.projectInfo.executionUser", projectInfo.getExecutionUser());
////		htmlString=htmlString.replace("$model.projectInfo.operatingSystem", projectInfo.getOperatingSystem());
////		htmlSummaryString=htmlSummaryString.replace("$model.projectInfo.operatingSystem", projectInfo.getOperatingSystem());
//		
//		//ross
//		htmlString = htmlString.replace("$model.projectInfo.project", Config.getProp("ProjectName"));
//		htmlSummaryString = htmlSummaryString.replace("$model.projectInfo.project", Config.getProp("ProjectName"));
//		
//		htmlString=htmlString.replace("$model.projectInfo.environment", Config.getProp("Environment"));
//		htmlSummaryString=htmlSummaryString.replace("$model.projectInfo.environment", Config.getProp("Environment"));
//		
//		htmlString=htmlString.replace("$model.projectInfo.testRunId", testRunID);
//		htmlSummaryString=htmlSummaryString.replace("$model.projectInfo.testRunId", testRunID);
//		htmlString=htmlString.replace("$model.projectInfo.machineName", InetAddress.getLocalHost().getHostName());
//		htmlSummaryString=htmlSummaryString.replace("$model.projectInfo.machineName", InetAddress.getLocalHost().getHostName());
//		htmlString=htmlString.replace("$model.projectInfo.executionUser", System.getProperty("user.name"));
//		htmlSummaryString=htmlSummaryString.replace("$model.projectInfo.executionUser", System.getProperty("user.name"));
//		//htmlString=htmlString.replace("$model.projectInfo.operatingSystem", projectInfo.getOperatingSystem());
//		//htmlSummaryString=htmlSummaryString.replace("$model.projectInfo.operatingSystem", projectInfo.getOperatingSystem());
//		
//		
//		
//		//BrowserInfo browserInfo=(BrowserInfo)map.get("browserInfo");
//		BrowserInfo browserInfo=(BrowserInfo)map.get("browserInfo");
//		Integer failCount=0;
//		List<TestCaseDetails> listTestCaseDetails= (List<TestCaseDetails>) map.get("listTestCaseDetails");
//		StringBuilder builder = new StringBuilder();
//		StringBuilder summaryBuilder = new StringBuilder();
//		String configHeader="<tbody style='width:88.8%'>"+
//				"<tr>"+
//				"<th style='width:8%!important;'>Config ID</th>"+
//				"<th style='width:44.7%!important;'>Config Name</th>"+	
//				"<th style='width:7%!important;'>Status</th>"+
//				"<th style='width:11.5%!important;'>Total Validated</th>"+
//				"<th style='width:10.2%!important;'>Total Pass</th>"+
//				"<th style='width:10%!important;'>Total Fail</th>"+
//				"<th style='width:12%!important;white-space: nowrap;'>Execution Duration</th>"+
//			"</tr>"+
//		"</tbody>";
//		
//		for(int i=0;i<listTestCaseDetails.size();i++){ 
//			TestCaseDetails testCaseDetails=listTestCaseDetails.get(i);
//			TestReports tr=new TestReports();
//			String testCaseTableID=String.valueOf(i);
//			List<ConfigDetail>listConfigDetail= tr.getListOfConfigDetails(testCaseDetails.getTestCaseID(),testRunID);
//			String configDetail=new String();
//			if(listConfigDetail !=null && listConfigDetail.size()>0)
//			configDetail= this.getListOfConfigDetails(listConfigDetail,testCaseDetails.getTestCaseID(), testRunID,testCaseTableID);
//			String status= tr.getStatus(testCaseDetails.getTestCaseID(), testRunID);
//			if(status!=null && status.equals("FAIL"))
//				failCount++;
//			PassFailValidateCount pfv= tr.getPassFailCount(testCaseDetails.getTestCaseID(), testRunID);
//			pfv.setTotalExeTimeOfTestCase(tr.getExecutionTime(pfv.getTotalExeTimeOfTestCase()));
//			String myFun="myFunction("+testCaseTableID+")";
//			String detailsHeaderRow="<td style='width:5%!important;' class="+"'ttStatus "+status+"'"+">"+status+"</td>"
//					+ "<td style='width:44%!important;text-align:left !important;'>"+pfv.getTestName()+"</td>"+
//					"<td class='ttTotalValidate'>"+pfv.getTotalValidated()+"</td>"+
//					"<td class='ttTotalPass'>"+pfv.getTotalPass()+"</td>"+
//					"<td class='ttTotalFail'>"+pfv.getTotalFail()+"</td>"+
//					"<td class='ttExeDuration'>"+pfv.getTotalExeTimeOfTestCase()+"</td>";
//			String headerRow="<tr style='height:auto !important;'>"+	
//					"<td class='ttapplicationID' style='text-align:left !important;'>"+testCaseDetails.getApplciationID()+"</td>"+
//					"<td style='width:8%!important;'><a href="+'#'+testCaseTableID+" onclick="+myFun+">"+testCaseDetails.getTestCaseID()+"</a></td>"+
//					detailsHeaderRow+
//				"</tr>"+"<tr>"+
//				"<td colspan='8'>"+
//				"<table id="+testCaseTableID +" class='configDetail'  style='display: none;margin-left:11.2%;width:88.8%'>"+
//					configHeader;
//			builder.append(headerRow+
//		"<tbody style='width:88.8%'>"+configDetail+
//		"</tbody>"+
//	"</table>"+
//"</td>"+
//"</tr>"
//					 );
//			String headerRowForSummary="<tr style='height:auto !important;'>"+	
//					"<td class='ttapplicationID' style='text-align:left !important;'>"+testCaseDetails.getApplciationID()+"</td>"+
//					"<td style='width:10%!important;'>"+testCaseDetails.getTestCaseID()+"</td>"+
//					detailsHeaderRow+
//				"</tr>";
//			summaryBuilder.append(headerRowForSummary);
//		}
//		
//		Integer totalTc=listTestCaseDetails.size();
//		if(browserInfo.getTotalFailedTestCases() !=null){
//			 browserInfo.setTotalPassedTestCases(String.valueOf(totalTc.intValue()-failCount.intValue()));
//		}
//		browserInfo.setTotalTestCases(totalTc.toString());
//		if(browserInfo.getBrowser() !=null){
////		htmlString = htmlString.replace("$model.browserInfo.browser", browserInfo.getBrowser());
////		htmlSummaryString = htmlSummaryString.replace("$model.browserInfo.browser", browserInfo.getBrowser());
//		htmlString = htmlString.replace("$model.browserInfo.browser", Config.getProp("API"));
//		htmlSummaryString = htmlSummaryString.replace("$model.browserInfo.browser", Config.getProp("API"));
//		}else{
//			String browser=new String();
////			htmlString = htmlString.replace("$model.browserInfo.browser",browser );
////			htmlSummaryString = htmlSummaryString.replace("$model.browserInfo.browser",browser );
//			htmlString = htmlString.replace("$model.browserInfo.browser",Config.getProp("API") );
//			htmlSummaryString = htmlSummaryString.replace("$model.browserInfo.browser",Config.getProp("API") );
//		}
//		htmlString = htmlString.replace("$model.browserInfo.executionDate", browserInfo.getExecutionDate()+ " EST" );
//		htmlSummaryString= htmlSummaryString.replace("$model.browserInfo.executionDate", browserInfo.getExecutionDate() + " EST");
//		htmlString = htmlString.replace("$model.browserInfo.totalTestCases", browserInfo.getTotalTestCases());
//		htmlSummaryString= htmlSummaryString.replace("$model.browserInfo.totalTestCases", browserInfo.getTotalTestCases());
//		htmlString = htmlString.replace("$model.browserInfo.totalPassedTestCases", browserInfo.getTotalPassedTestCases());
//		htmlSummaryString = htmlSummaryString.replace("$model.browserInfo.totalPassedTestCases", browserInfo.getTotalPassedTestCases());
//		htmlString = htmlString.replace("$model.browserInfo.totalFailedTestCases",failCount.toString());
//		htmlSummaryString= htmlSummaryString.replace("$model.browserInfo.totalFailedTestCases",failCount.toString());
//		htmlString = htmlString.replace("$model.browserInfo.totalExecutionDuration", browserInfo.getTotalExecutionDuration());
//		htmlSummaryString= htmlSummaryString.replace("$model.browserInfo.totalExecutionDuration", browserInfo.getTotalExecutionDuration());
//		htmlString=htmlString.replace("$testCases", builder.toString());
//		htmlSummaryString=htmlSummaryString.replace("$testCases", summaryBuilder.toString());
//		htmlString = htmlString.replace("$passHidden", browserInfo.getTotalPassedTestCases());
//		htmlString = htmlString.replace("$totalTestCasesHidden", browserInfo.getTotalTestCases());
//		htmlString = htmlString.replace("$failHidden", failCount.toString());
//		
//		File newHtmlFile = new File(Utils.addSlashIfNone(Configurables.almUploadDirectory)+ Utils.addSlashIfNone(Configurables.runID)+Configurables.summaryFile);
//		FileUtils.writeStringToFile(newHtmlFile, htmlString);
//		File newHtmlFileOfSummary = new File(Utils.addSlashIfNone(Configurables.almUploadDirectory)+Utils.addSlashIfNone(Configurables.runID)+ Utils.addSlashIfNone("summary")+Configurables.summaryFile);
//		//File newHtmlFileOfSummary = new File("//DTC230042A7FE82/GEExecutionResults/testEx_report_result/"+"summary_"+testRunID+"_"+"new.html");
//		FileUtils.writeStringToFile(newHtmlFileOfSummary, htmlSummaryString);
//		
//		//File newHtmlFileInProjectFolder = new File("./htmlReports/"+testRunID+"_"+"new.html");
//		File newHtmlFileInProjectFolder = new File("./htmlReports/"+"WFG.html");
//		FileUtils.writeStringToFile(newHtmlFileInProjectFolder, htmlString);
//		//File newHtmlFileInProjectFolderOfSummary = new File("./htmlReports/summary_"+testRunID+"_"+"new.html");
//		File newHtmlFileInProjectFolderOfSummary = new File("./htmlReports/Summary.html");
//		FileUtils.writeStringToFile(newHtmlFileInProjectFolderOfSummary, htmlSummaryString);
//		
//		// Added by Praveen for sending report through Jenkins
//		
//		/*
//		 * Commented for Test Purpose - Ross
//		 */
////		String strJenkinsAttcFileName=Config.getProp("SummaryFile").trim();
////		
////		File summaryFile = new File(System.getProperty("user.dir")+"/"+strJenkinsAttcFileName);
////		File emailBodySummary = new File(System.getProperty("user.dir")+"/Summary.html");
////		if(summaryFile.exists()){FileUtils.forceDelete(new File(strJenkinsAttcFileName));}
////		if(emailBodySummary.exists()){FileUtils.forceDelete(new File("Summary.html"));}
////		FileUtils.copyFile(newHtmlFile, new File(strJenkinsAttcFileName));
////		
////		String strSummaryFileContents=FileUtils.readFileToString(newHtmlFileInProjectFolderOfSummary);
////		if(strSummaryFileContents.contains(">PASS<")){strSummaryFileContents=strSummaryFileContents.replace(">PASS<", "><b><font color='green'>PASS</font></b><");}
////		if(strSummaryFileContents.contains(">FAIL<")){strSummaryFileContents=strSummaryFileContents.replace(">FAIL<", "><b><font color='red'>FAIL</font></b><");}
////		
////		FileUtils.writeStringToFile(new File("Summary.html"), strSummaryFileContents);
////		
////		CopyReportToDashBoardPath(browserInfo.getExecutionDate().trim(), browserInfo.getTotalExecutionDuration(),newHtmlFile);
//		
//		
//		//FileUtils.copyFile(newHtmlFileInProjectFolderOfSummary, new File("Summary.html"));
//		
//	}
//		
//		else{
//			System.out.println("TestRunId is Null Hence Report didn't Generated. ");
//		}
//	}
//		catch( IOException| ClassNotFoundException| SQLException e){
//			e.printStackTrace();
//			
//		}
//	}
//		
//	private void CopyReportToDashBoardPath(String strExeEndTime,String strExeDuration, File fileToCopy) 
//	{
//		if(Config.getProp("SaveReport")!=null)
//		{
//			String strReportFileName=Config.getProp("SummaryFile");
//			if(strReportFileName.toLowerCase().contains(".html")){strReportFileName=strReportFileName.replace(".html", "");}
//			if(Config.getProp("SaveReport").toLowerCase().equals("yes"))
//			{
//				String strDashBoardPath="";
//				strDashBoardPath=getDashboardPathBasedOnTime(strExeEndTime, strExeDuration);
//				System.out.println("Dashboard path: "+strDashBoardPath);
//							
//				if(TakeBackUpOfOldFileAndDelete(strDashBoardPath,Config.getProp("ResultArchieve"),strReportFileName))
//				{
//					try{FileUtils.copyFile(fileToCopy, new File(strDashBoardPath+"/"+strReportFileName+".html"));}catch(Exception e){System.out.println("Unable to Copy Execution report to Dashboard Location. Reason: "+e);}
//				}
//				
//			}
//		}
//		else
//		{
//			System.out.println("Flag to copy Execution report to Dashboard location is missing in TestDataconfig.txt");
//		}
//	
//	}
//
//	private String getListOfConfigDetails(List<ConfigDetail> listConfigDetail,String testCaseID,String testRunID,String testCaseTableID) throws SQLException{
//		TestReports tr=new TestReports();
//		StringBuilder configIds=new StringBuilder();
//		for(int i=0;i<listConfigDetail.size();i++){
//			ConfigDetail cd=listConfigDetail.get(i);
//			ConfigPassFailValidateCount cpf= tr.getCofigPassFailCount(testCaseID, testRunID, cd.getContextCofigID());
//			String innerConfigTableID= cd.getContextCofigID()+testCaseTableID;
//			String myFun="myFunction("+innerConfigTableID+")";
//			String status= tr.getConfigStatus(testCaseID, testRunID, cd.getContextCofigID());
//			List<TestCaseIDDetails> listIdsDetail=tr.getTestCaseIDsDetails(testCaseID,testRunID,cd.getContextCofigID());
//			String stepsDetails=new String();
//			if(listIdsDetail !=null && listIdsDetail.size()>0)
//			stepsDetails= this.getTestRecordByConfigID(listIdsDetail,innerConfigTableID);
//			
//			if(cpf !=null){
//				String configDetailsCommon="<td class='configDetailsName'>"+cd.getConfigName() +"</td>"+
//						"<td class="+"'configDetailsStatus "+status+"'"+">"+status +"</td>"+
//						"<td class='configDetailsTValidate'>"+cpf.getTotalValidated() +"</td>"+
//						"<td class='configDetailsTPass'>"+cpf.getTotalPass() +"</td>"+
//						"<td class='configDetailsTFail'>"+cpf.getTotalFail() +"</td>"+
//						"<td class='configDetailsExeTime'>"+tr.getExecutionTime(cpf.getTotalExeTimeOfTestCase()) +"</td>"+
//						"</tr>";
//			configIds.append(	"<tr class='clickable-row'>"+
//					"<td class='configDetailsID'><a href='#"+innerConfigTableID+"' onclick="+myFun+">"+cd.getContextCofigID()+"</a></td>"+
//					configDetailsCommon+stepsDetails);		
//			}
//		}
//		return configIds.toString();
//	}
//	private String getTestRecordByConfigID(List<TestCaseIDDetails> listTestCaseIDDetails,String innerConfigTableID){
//		StringBuilder idsDetail=new StringBuilder();
//		idsDetail.append("<tr>"+
//				"<td colspan='8'>"+
//				"<table id="+innerConfigTableID+" class='Detail'  style='display:none;margin-left:7%;width:93%'>"+
//					"<tbody style='width:100%'>"+
//						"<tr>"+
//							"<th class='detailConfigIDwidth'>Config ID</th>"+	
//							"<th class='detailStepNameWidth'>Step Name</th>"+
//							"<th class='detailStepDetailWidth'>Step Details</th>"+
//							"<th class='detailStatus'>Status</th>"+
//							"<th class='detailValidationType'>Validation Type</th>"+
//							"<th class='detailFormatDate'>Execution Time</th>"+
//						"</tr>"+
//					"</tbody>"+
//					"<tbody style='width:93%'>"+this.getTestCaseIDsDetails(listTestCaseIDDetails,innerConfigTableID)
//					+"</tbody>"+
//				"</table>"+
//
//			"</td>"+
//		"</tr>");
//		return idsDetail.toString();
//	}
//	private String getTestCaseIDsDetails(List<TestCaseIDDetails>testCaseIdsDetails,String innerConfigTableID){
//		StringBuilder testIds=new StringBuilder(); 
//		for(int j=0;j<testCaseIdsDetails.size();j++){
//			TestCaseIDDetails testCasesid=testCaseIdsDetails.get(j);
//			String status=testCasesid.getStepResult().toUpperCase();
//			String description=new String();
//			if(status !=null && status.equals("FAIL")){
//				String link=testCasesid.getTestCaseID()+""+j+""+innerConfigTableID;
//				String screenShotImgPath=testCasesid.getScreenShotPath();
//				String errorDesc=new String();
//				if(testCasesid.getErrorDescription() !=null)
//				errorDesc=" ErrorDescription : "+testCasesid.getErrorDescription()+"";
//				String path="<a href='"+screenShotImgPath+"' target='_blank' style='color:indigo'>screen shot   </a>";
//				description="<tr class='errDetail'>"
//						+ "<td colspan='7'>"
//						+ "<table class='errorTable' bgcolor='#FFC1CC' id='"+link+"' style='display:none;width:82.5%;margin-left:17.5%;table-layout: fixed;border: 1px solid black;'>"
//						+"<tbody style=width: 82.5%;background-color:#FFC1CC !important;'>"
//						+ "<tr style='width: 82.5%;background-color:#FFC1CC !important;'>"
//						+ "<td class='failRow' colspan='7' style='width: 82.5%!important;background-color:#FFC1CC !important;text-align:left !important;'>"+path+
//						"<br><br><div style='width:82.5%!important;background-color:#FFC1CC !important;color:maroon !important;text-align:left !important;' class='errordes'>"+
//						errorDesc
//								+ "</div>"
//								+ "</td></tr></tbody></table></td></tr>";
//			}
//			testIds.append(	"<tr>"+
//			"<td class='detailConfigIDwidth'>"+testCasesid.getConfigID()+"</td>"+
//			"<td class='detailStepNameWidth'>"+testCasesid.getStepName()+"</td>"+
//			"<td  class='detailStepDetailWidth' style='text-align:left !important;'>"+testCasesid.getStepDetails()+"</td>"+
//			this.passFailUnderDetails(status,testCasesid.getTestCaseID(),j,innerConfigTableID)+
//			"<td class='detailValidationType'>"+testCasesid.getValidationType()+"</td>"+
//			"<td class='detailFormatDate'>"+this.getFromatedDate(testCasesid.getExecutionTime())+"</td>"+
//		"</tr>"+description
//					);
//		}
//	return	testIds.toString();		
//	}
//	private String passFailUnderDetails(String status,String testCaseid,Integer j,String innerConfigTableID){
//		String passFail=new String();
//		if(status !=null && status.equalsIgnoreCase("fail")){
//			String link=testCaseid+""+j.toString()+""+innerConfigTableID;
//			String myFun="myFunction("+link+")";
//			passFail="<td  class=" +"'detailStatus  "+status+"'"+">"+"<a href='#"+link+"' onclick='"+myFun+"' style='color:red'>FAIL</a>"+"</td>";
//		}else{
//			passFail="<td  class="+"'detailStatus "+status+"'"+">"+status+"</td>";
//		}
//		return passFail;
//	}
//	private String getFromatedDate(String input){
//		try {
//			return new SimpleDateFormat("KK:mm a").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(input)).toString();
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//		return "";
//	}
//	
//	/*private String[] getFolderNames()
//	{
//		File file = new File("/path/to/directory");
//		String[] directories = file.list(new FilenameFilter() {
//		  @Override
//		  public boolean accept(File current, String name) {
//		    return new File(current, name).isDirectory();
//		  }
//		});
//		System.out.println(Arrays.toString(directories));
//	}*/
//	
//	private String getDashboardPathBasedOnTime(String strExecutionEndTime,String strExeDuration)
//	{
//		String strDashBoardPath=null;
//		try
//		{
//			SimpleDateFormat sf=new SimpleDateFormat("hh:mm a");
//			
//			Date date1=sf.parse(sf.format(new SimpleDateFormat("hh:mm:ss").parse(strExeDuration)));
//			
//			Calendar cal= Calendar.getInstance();
//			//cal.setTime((new SimpleDateFormat("hh:mm a")).parse("12:10"));
//			cal.setTime((new SimpleDateFormat("hh:mm a")).parse(strExecutionEndTime.substring(strExecutionEndTime.indexOf("|")+1).trim()));
//			cal.add(Calendar.HOUR,  -(date1.getHours()));
//			cal.add(Calendar.MINUTE,  -(date1.getMinutes()));
//			
//			Calendar cal2= Calendar.getInstance();
//			Calendar cal3= Calendar.getInstance();
//			
//			cal2.setTime(sf.parse(Config.getProp("ExecutionTime1")));
//			cal3.setTime(sf.parse(Config.getProp("ExecutionTime2")));	
//			
//			//if(Config.getProp("ExecutionTime1").contains("pm") && Config.getProp("ExecutionTime2").contains("am")){cal3.add(Calendar.DATE, 1);}
//			
//			if(Config.getProp("ExecutionTime1").toLowerCase().contains("pm") && Config.getProp("ExecutionTime2").toLowerCase().contains("am"))
//			{
//				cal2.add(Calendar.DATE, -1);
//				cal3.add(Calendar.DATE, 1);
//				if(sf.format(cal.getTime()).toLowerCase().contains("am")){cal.add(Calendar.DATE, 1);}
//				else{cal.add(Calendar.DATE, -1);}
//				
//			}
//			
//			
//			if(cal.getTime().after(cal2.getTime()) && cal.getTime().before(cal3.getTime())){strDashBoardPath=Config.getProp("Result1Path");}
//			else{strDashBoardPath=Config.getProp("Result2Path");}
//			
//			//System.out.println(strDashBoardPath);
//			
//		}
//		catch(Exception e){System.out.println("Unable to Save Execution logs to Dashboard location. Reason: "+e);}
//		
//		return strDashBoardPath;
//	}
//	
//	private boolean TakeBackUpOfOldFileAndDelete(String strFilePath,String strBackUpPath,String strFileName)
//	{
//		try
//		{
//			if(new File(strFilePath+"/"+strFileName+".html").exists())
//			{
//				
//				BasicFileAttributes attr=Files.readAttributes(Paths.get(new File(strFilePath+"/"+strFileName+".html").getAbsoluteFile().toURI()),BasicFileAttributes.class);
//				String strCreationTime=new SimpleDateFormat("dd_MMM_yyyy").format(attr.creationTime().toMillis())+"_"+ new SimpleDateFormat("HH_mm_ss").format(attr.lastModifiedTime().toMillis());
//				//strCreationTime=strCreationTime.replace("-", "_").replace(":", "_").substring(0,strCreationTime.indexOf("T"))+"_"+ (new SimpleDateFormat("hh:mm:ss").parse(arg0));
//				
//				try
//				{
//					FileUtils.copyFile(new File(strFilePath+"/"+strFileName+".html"), new File(strBackUpPath+"/"+strFileName+"_"+strCreationTime+".html"));
//					FileUtils.forceDelete(new File(strFilePath+"/"+strFileName+".html"));
//					
//				}
//				catch(Exception e)
//				{
//					System.out.println("Unable to delete old dashboard report.Reason: "+e);
//					return false;
//				}
//			}
//			
//		}
//		catch(Exception e)
//		{
//			System.out.println("Unable to take Back of Previous file.Reason: "+e);
//			return false;
//		}
//		
//		return true;
//		
//	}
//	
//}
