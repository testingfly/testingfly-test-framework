package com.javaexcel.automation.alm;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;


import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javaexcel.automation.alm.model.*;
import com.javaexcel.automation.core.data.Configurables;
import com.javaexcel.automation.core.table.Record;
import com.javaexcel.automation.core.utils.Utils;
/**
 * 
 * @author rossmeitei
 *
 */
public class Client
{
    private final Config config;
    private static Boolean uploadFlg = true;
    private static Boolean getIterFlg = true;
    private static String currentIter;
    public Client(Config almConfig)
    {
        config = almConfig;

        RestConnector.instance().init(config.host(), config.port(), config.domain(), config.project());
        
    }

    /**
     * Login to HP ALM platform using basic authentication.
     *
     * @throws Exception
     */
    public void login() throws Exception
    {
        System.out.println(String.format("Logging in as '%s' ...", config.username()));
        Dao.login(Dao.isAuthenticated(), config.authToken());

    }

    /**
     * Close session on server and clean session cookies on client.
     *
     * @throws Exception
     */
    public void logout() throws Exception
    {
        Dao.logout();
        //System.out.println("Successfully logged out");
    }

    /**
     * Read test set entity.
     *
     * @param testSetId
     * @return
     * @throws Exception
     */
    public TestSet loadTestSet(String testSetId) throws Exception
    {
         TestSet testSet = Dao.readTestSet(testSetId);
        return testSet;
    }

    /**
     * Read test instance entities
     *
     * @param testSet
     * @return
     * @throws Exception
     */
    public TestInstances loadTestInstances(TestSet testSet) throws Exception
    {
        TestInstances testInstances = Dao.readTestInstances(testSet.id());
        return testInstances;
    }

    /**
     * Read test entity.
     *
     * @param testInstance
     * @return
     * @throws Exception
     */
    public Test loadTest(TestInstance testInstance) throws Exception
    {
        Test test = Dao.readTest(testInstance.testId());
        return test;
    }

    /**
     * Create run entity.
     *
     * @param testInstance
     * @param test
     * @return
     * @throws Exception
     */
    public Run createRun(TestInstance testInstance, Test test, int i) throws Exception
    {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final String runName = "Run " + dateFormat.format(new Date());
    	HashMap <Integer, HashMap <String, String>> allTestMap = XmlParser.allTestMap;
    	
    	Run prepRun = new Run();
        //prepRun.testInstanceId(testInstance.id());
        prepRun.name(runName);
        prepRun.testType(Run.TEST_TYPE_AUTO);
        prepRun.testCycleId(testInstance.cycleId());
        prepRun.status(allTestMap.get(i).get("status"));
        prepRun.testId(test.id());
        prepRun.testcycl_id(testInstance.id());//testInstance ID
        prepRun.owner(config.username());
        
        Run run = Dao.createRun(prepRun);
        return run;
    }

    /**
     * Create run step entities.
     *
     * @param run
     * @param runSteps
     * @throws Exception
     */
    public void createRunSteps(Run run, RunSteps runSteps) throws Exception
    {
        for (RunStep runStep : runSteps.entities())
        {
            RunStep prepRunStep = new RunStep();

            prepRunStep.runId(run.id());
            prepRunStep.name(runStep.name());
            prepRunStep.status(runStep.status());
            Dao.createRunStep(prepRunStep);
        }
    }

    private static String hostName() throws UnknownHostException
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        }
        catch(UnknownHostException e)
        {
            return "Unknown hostname";
        }
    }
    
    
    public Test createTest(int index) throws Exception{
    	String folderName = com.javaexcel.automation.core.data.Config.getProp("API_Name");
    	String parentId = getFolderId("test-folders",config.testFolder(), folderName);
    	//System.out.println(folderName+"-> Debug***------------> "+parentId);
    	if(parentId==null){
    		createTestFolder(folderName);
    		parentId = getFolderId("test-folders",folderName);    		
    	}
    	
    	HashMap <Integer, HashMap <String, String>> allTestMap = XmlParser.allTestMap;
    	int i = index;
    	Test t = new Test();
    	t.name(allTestMap.get(i).get("testName"));
    	t.description(allTestMap.get(i).get("testDesc"));
    	t.execStatus(allTestMap.get(i).get("status"));
    	t.owner(config.username());
    	t.user_template_xx("user-template-01","1-Test Scenario");
    	t.user_template_xx("user-template-02","N/A");
    	t.user_template_xx("user-template-03",config.getValue("Application", "N/A"));
    	t.user_template_xx("user-template-04",config.getValue("Component", "N/A"));
    	t.user_template_xx("user-template-05",config.getValue("TestSubject","Regression"));
    	t.user_template_xx("user-template-06",config.getValue("TestPhase", "6-Auto Regression"));
    	t.user_template_xx("user-template-17",config.getValue("TeamCategory", "TechQA"));
    	t.parentId(parentId);
    	t.subtype_id("EXTERNAL-TEST");
    	
    	String testId = getTestId(t);
    	
    	if(testId==null){
    		Dao.createTest(t);
    		t.id(getTestId(t));
    	}
    	else{
    		t.id(testId);
    		Dao.updateTest(t, testId);
    		
    	}
    	
    	
    	
    	return t;
    	
    }
    
    
    public Requirement createRequirement(Record rec) throws Exception{
    	
    	Requirement req = new Requirement();
    	req.type_id("3");
    	req.owner(config.username());
    	req.name(rec.getValue("NAME"));
    	req.user_template_xx("user-template-09",rec.getValue("REQ_ID"));
    	req.description(rec.getValue("DESCRIPTION"));
    	req.user_template_xx("user-template-02",rec.getValue("APPLICATION"));
    	req.user_template_xx("user-template-04",rec.getValue("COMPONENT"));
    	req.user_template_xx("user-template-53",rec.getValue("DUE_DATE"));
    	req.user_template_xx("user-template-10",rec.getValue("PROJECT_PACKAGE"));
    	
    	String reqId = getReqId(req);
    	
    	if(reqId==null){
    		Dao.createRequirement(req);
    		req.id(getReqId(req));
    	}
    	else{
    		req.id(reqId);
    		Dao.updateRequirement(req, reqId);
    		
    	}
    	
    	return req;
    	
    }
    
    
    public TestSet createTestSet(int i) throws Exception{
    	String folderName = com.javaexcel.automation.core.data.Config.getProp("API_Name");
    	String parentId = getFolderId("test-set-folders",folderName);
    	if(parentId==null){
    		createTestSetFolder(folderName);
    		parentId = getFolderId("test-set-folders",folderName);    		
    	}
    	HashMap <Integer, HashMap <String, String>> allTestMap = XmlParser.allTestMap;
    	TestSet t = new TestSet();
    	
    	t.name(allTestMap.get(i).get("testSet")+"-"+com.javaexcel.automation.core.data.Config.getProp("Environment"));
    	t.parentId(parentId);
    	t.subtype_id("hp.qc.test-set.external");
    	t.user_template_xx("user-template-01","3-QA");
    	t.user_template_xx("user-template-02","N/A");
    	t.user_template_xx("user-template-03",config.getValue("TeamCategory", "TechQA"));
    	t.user_template_xx("user-template-04",config.getValue("TestPhase", "6-Auto Regression"));
    	t.user_template_xx("user-template-05","N/A");
    	t.user_template_xx("user-template-17","N/A");
    	t.comment("This test result was published via the JavaExcel framework.");

  
    	String testId = getTestSetId(t);
    	
    	if(testId==null){
    		Dao.createTestSet(t);
    		t.id(getTestSetId(t));
    	}
    	else{
    		t.id(testId);
    		Dao.updateTestSet(t, testId);
    	}
    	
    	return t;
    	
    }
    
    public void attachResults(TestSet t) throws Exception{
    	byte[] buffer = Dao.readFile("reports/pdf/"+Utils.pdfFileName);
    	Dao.createTestSetAttachment(t.id(), Utils.pdfFileName.replace(".pdf", "_Iter_"+currentIter+".pdf"), buffer);
    	uploadFlg  = false;
    }
    
    public TestInstance createTestInstance(Test test, TestSet testSet) throws Exception{
    	String cycleId = testSet.id();
    	String testId = test.id();
    	TestInstance t = new TestInstance();
    	t.subtypeId("hp.qc.test-instance.external-test");
    	t.cycleId(cycleId);
    	t.testId(testId);
    	t.owner(config.username());
    	
    	if(getIterFlg){
    		currentIter = getTestInstanceId(testSet);	
    		getIterFlg = false;
    	}
    	t.iterations(currentIter);
    	
    	if(uploadFlg){
    		attachResults(testSet);
    	}
    		
    	
    	if(testId!=null && cycleId!=null){
    		String id = Dao.createTestIntance(t).id();
    		t.id(id);
    	}
    	return t;
    }
    
    /**
     * Get max iteration id based on test set passed
     * @param ts
     * @return max iterations
     * @throws Exception
     */
    public String getTestInstanceId(TestSet ts) throws Exception{
    	String instanceId = Dao.readTestInstance(ts,"iterations");
    	if (instanceId==null || instanceId.equals("")){
    		return "1";
    	}else{
    		return Integer.toString(Integer.parseInt(instanceId)+1);
    	}
    }
    
  
    
    public String getTestId(Test test){
    	try {
    		return Dao.getTestId(test.name())!=null?Dao.getTestId(test.name()):null;
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    /**
     * Gets Requirement ID from ALM
     * @param req
     * @return
     */
    public String getReqId(Requirement req){
    	try {
    		return Dao.getReqId(req.name())!=null?Dao.getReqId(req.name()):null;
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    public String getTestSetId(TestSet testSet){
    	try {
    		return Dao.getTestSetId(testSet.name())!=null?Dao.getTestSetId(testSet.name()):null;
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return null;
    }
    

    public void checkDuplicate(String testName){
    	
    }
    
    public String getFolderId(String type, String folderName){
 
    	try {
			return Dao.getFolderId(type, folderName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
    
    public String getFolderId(String type, String pFolderName, String folderName){
    	 
    	try {
			return Dao.getFolderId(type, pFolderName, folderName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
    
    public String createTestFolder(String folderName){
    	 
    	try {
			
    		TestFolder tf = new TestFolder("test-folder");
    		String parentId = Dao.getFolderId("test-folders", config.testFolder());
    		tf.parentId(parentId);
        	tf.name(folderName);
        	tf.description("Auto created by JavaExcel Framework");
        	Dao.createTestFolder(tf);
        	
    	} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
    
    public String createTestSetFolder(String folderName){
   	 
    	try {
			
    		TestFolder tsf = new TestFolder("test-set-folder");
    		String parentId = Dao.getFolderId("test-set-folders", config.testFolder());
        	tsf.parentId(parentId);
        	tsf.name(folderName);
        	tsf.description("Auto created by JavaExcel Framework");
        	Dao.createTestSetFolder(tsf);
        	
    	} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
    

}
