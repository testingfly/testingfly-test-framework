package com.javaexcel.automation.alm;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.apache.commons.lang.StringUtils;

import com.javaexcel.automation.alm.model.*;



public final class Dao
{
    private Dao()
    {
    }

    /**
     * @return null if authenticated.<br> a url to authenticate against if not authenticated.
     * @throws Exception
     */
    public static String isAuthenticated() throws Exception
    {
        String isAuthenticatedUrl = "qcbin/rest/is-authenticated";

        try
        {
            connector().get(isAuthenticatedUrl, Response.class, null, null);

            return null;
        }
        catch (ResponseException ex)
        {
            Response response = ex.response();

            if (response.getStatus() != Status.UNAUTHORIZED.getStatusCode())
            {
                throw ex;
            }

            String authPoint = response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE);

            if (StringUtils.isNotBlank(authPoint))
            {
                authPoint = authPoint.split("=")[1].replace("\"", "");
                authPoint += "/authenticate";

                return authPoint;
            }

            throw new Exception("Invalid authentication point");
        }
    }

    /**
     * Client sends a valid Basic Authorization header to the authentication point
     * and server set cookies on client.
     *
     * @param authenticationPoint to authenticate at
     * @param username
     * @param password
     * @throws Exception
     */
    public static void login(String authenticationPoint, String username, String password) throws Exception
    {
        connector().get(authenticationPoint, Response.class, RestConnector.createBasicAuthHeader(username, password), null);
        
    }
    
    public static void login(String authenticationPoint, String authToken) throws Exception
    {
    	URI uri = new URI(authenticationPoint);
    	connector().get(uri.getPath(), Response.class, RestConnector.createBasicAuthHeader(authToken), null);
    	session("qcbin/rest/site-session");
        
    }

    /**
     * Make a call to is-authenticated resource to obtain authenticationPoint and do login.
     *
     * @param username
     * @param password
     * @throws Exception
     */
//    public static void login(String username, String password) throws Exception
//    {
//        String authenticationPoint = isAuthenticated();
//
//        if (authenticationPoint != null)
//        {
//            URI uri = new URI(authenticationPoint);
//
//            login(uri.getPath(), username, password);
//            session(uri.getPath());
//            System.exit(1);
//        }
//    }
    
    public static void session(String path) throws Exception
    {
        connector().post(path, Response.class,null, null, null);   
        
    }


    /**
     * Close session on server and clean session cookies on client
     *
     * @throws Exception
     */
    public static void logout() throws Exception
    {
        String logoutUrl = "qcbin/authentication-point/logout";

        connector().get(logoutUrl, Response.class, null, null);
    }

    /**
     * Read the test entity with the specified ID
     *
     * @param id
     * @return
     * @throws Exception
     */
    public static Test readTest(String id) throws Exception
    {
        String testUrl = connector().buildEntityUrl("test", id);

        return connector().get(testUrl, Test.class, null, null);
    }

    /**
     * Read the test set entity with the specified ID
     *
     * @param id
     * @return
     * @throws Exception
     */
    public static TestSet readTestSet(String id) throws Exception
    {
        String testSetUrl = connector().buildEntityUrl("test-set", id);
        return new TestSet(connector().get(testSetUrl, TestSet.class, null, null));
    }

    /**
     * Read the test instance set entity with the specified ID
     *
     * @param id
     * @return
     * @throws Exception
     */
    public static TestInstance readTestInstance(String id) throws Exception
    {
        String testInstanceUrl = connector().buildEntityUrl("test-instance", id);

        return connector().get(testInstanceUrl, TestInstance.class, null, null);
    }

    /**
     * Read the test instance entities wuth the specified testSetId
     *
     * @param testSetId
     * @return
     * @throws Exception
     */
    public static TestInstances readTestInstances(String testSetId) throws Exception
    {
        String testInstancesUrl = connector().buildEntityCollectionUrl("test-instances");

        Map<String, String> criteria = new HashMap<String, String>();
        criteria.put("query", "{cycle-id[" + testSetId + "]}");

        return connector().get(testInstancesUrl, TestInstances.class, null, criteria);
    }
    
    
    public static String readTestInstance(TestSet ts, String fieldName) throws Exception
    {
    	String query = "{cycle-id[\""+ts.id()+ "\"]}";
    	String testUrl = connector().buildEntityCollectionUrl("test-instances?query="+URLEncoder.encode(query,"UTF-8")+"&order-by="+URLEncoder.encode("{id[DESC]}","UTF-8"));
    	try{
    	EntitiesResult er =  connector().get(testUrl, EntitiesResult.class, null, null);
    	
    	if (er.entity()==null)
    		return null;
    	else{
    		int itr = 1;
    		for (Entity entity : er.entity()){
    			//System.out.println("ALM Itr**** "+entity.fieldValue("iterations"));
    			if(Integer.parseInt(entity.fieldValue("iterations"))>itr){
    				itr = Integer.parseInt(entity.fieldValue("iterations"));
    				
    			}
    		}
    		return Integer.toString(itr);
//    		System.out.println("Debug*** "+er.entity().get(er.entity().size()-1).fieldValue(fieldName));
//    		return er.entity().get(er.entity().size()-1).fieldValue(fieldName);
    	   }
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	return null;
        
    }

    /**
     * Create an attachment for run entity
     *
     * @param runId
     * @param fileName to use on serverside
     * @param fileData content of file
     * @return the xml of the metadata on the created attachment
     * @throws Exception
     */
//    public static Attachment createRunAttachment(String runId, String fileName, byte[] fileData) throws Exception
//    {
//        String attachmentsUrl =  connector().buildEntityUrl("run", runId) + "/attachments";
//
//        return createAttachment(attachmentsUrl, fileName, fileData);
//    }
//    
    public static void createTestSetAttachment(String ts_id, String fileName, byte[] fileData) throws Exception
    {
        String attachmentsUrl =  connector().buildEntityUrl("test-sets", ts_id) + "/attachments";

         createAttachment(attachmentsUrl, fileName, fileData);
    }

    /**
     * Reads file content and returns in byte
     * @param filePath
     * @return
     */
    public static byte[] readFile(String filePath) {
    	byte[] buffer = null;
    	try{
	    	InputStream inputstream = new FileInputStream(filePath);
	    	buffer = new byte[inputstream.available()];
	    	inputstream.read(buffer);
	    	
	    	inputstream.close();
	    }

		catch (Exception e) {
			e.printStackTrace();
		}
		return buffer;
    	
	}
    
    /**
     * Create an attachment for run step entity
     *
     * @param runStepId
     * @param fileName to use on serverside
     * @param fileData content of file
     * @return the xml of the metadata on the created attachment
     * @throws Exception
     */
//    public static Attachment createRunStepAttachment(String runStepId, String fileName, byte[] fileData) throws Exception
//    {
//        String attachmentsUrl =  connector().buildEntityUrl("run-step", runStepId) + "/attachments";
//
//        return createAttachment(attachmentsUrl, fileName, fileData);
//    }

    /**
     * Create run entity
     *
     * @param run
     * @return
     * @throws Exception
     */
    public static Run createRun(Run run) throws Exception
    {
        String runsUrl =  connector().buildEntityCollectionUrl("runs");

        return new Run(connector().post(runsUrl, Run.class, null, null, run));
    }

    /**
     * Update run entity
     *
     * @param run
     * @return
     * @throws Exception
     */
    public static Run updateRun(Run run) throws Exception
    {
        String runUrl =  connector().buildEntityUrl("run", run.id());

        run.clearBeforeUpdate();

        return connector().put(runUrl, Run.class, null, null, run);
    }

    /**
     * Read a collection of run-steps of the specified run
     *
     * @param runId
     * @return
     * @throws Exception
     */
    public static RunSteps readRunSteps(String runId) throws Exception
    {
        String runStepsUrl =  connector().buildEntityUrl("run", runId) + "/run-steps";

        return connector().get(runStepsUrl, RunSteps.class, null, null);
    }

    /**
     * Create a new run step
     *
     * @param runStep
     * @return
     * @throws Exception
     */
    public static RunStep createRunStep(RunStep runStep) throws Exception
    {
        String runStepsUrl = connector().buildEntityUrl("run", runStep.runId()) + "/run-steps";

        return connector().post(runStepsUrl, RunStep.class, null, null, runStep);
    }

    /**
     * Update a run step
     *
     * @param runStep
     * @return
     * @throws Exception
     */
    public static RunStep updateRunStep(RunStep runStep) throws Exception
    {
        String runStepUrl = connector().buildEntityUrl("run", runStep.runId()) + "/run-steps/" + runStep.id();

        runStep.clearBeforeUpdate();

        return connector().put(runStepUrl, RunStep.class, null, null, runStep);
    }

    /**
     * Gets an instance of RestConnector
     *
     * @return
     */
    private static RestConnector connector()
    {
        return RestConnector.instance();
    }

    /**
     * Create attachment
     *
     * @param entityUrl url of entity to attach the file to
     * @param fileName to use on serverside
     * @param payload content of file
     * @return the xml of the metadata on the created attachment
     * @throws Exception
     */
    private static void createAttachment(String entityUrl, String fileName, byte[] fileData) throws Exception
    {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<String, Object>();
        headers.add("Slug", fileName);

         connector().post(entityUrl, Attachment.class, headers, null, fileData, "application/octet-stream");
    }
    
    /**
     * Create a new test
     *
     * @param test
     * @return
     * @throws Exception
     */
    public static void createTest(Test test) throws Exception
    {
        String testUrl = connector().buildEntityCollectionUrl("tests");
        connector().post(testUrl, Test.class, null, null, test);
       
    }
    
    /**
     * Creates Requirement
     * @param req
     * @throws Exception
     */
    public static void createRequirement(Requirement req) throws Exception
    {
        String testUrl = connector().buildEntityCollectionUrl("requirements");
        connector().post(testUrl, Requirement.class, null, null, req);
        
    }
    
    public static TestInstance createTestIntance(TestInstance testInstance) throws Exception
    {
        String testUrl = connector().buildEntityCollectionUrl("test-instances");
        TestInstance ti = new TestInstance(connector().post(testUrl, TestInstance.class, null, null, testInstance));
        return ti;
        
    }
    

    
    public static void createTestSet(TestSet testSet) throws Exception
    {
        String testUrl = connector().buildEntityCollectionUrl("test-sets");
        connector().post(testUrl, TestSet.class, null, null, testSet);
        
    }
    
    public static void createTestFolder(TestFolder testFolder) throws Exception
    {
        String testUrl = connector().buildEntityCollectionUrl("test-folders");
        connector().post(testUrl, TestFolder.class, null, null, testFolder);
        
    }
    
    public static void createTestSetFolder(TestFolder testFolder) throws Exception
    {
        String testUrl = connector().buildEntityCollectionUrl("test-set-folders");
        connector().post(testUrl, TestFolder.class, null, null, testFolder);
        
    }
    
    public static void updateTest(Test test, String testId) throws Exception
    {
        String testUrl = connector().buildEntityCollectionUrl("tests/")+testId;
        connector().put(testUrl, Test.class, null, null, test);
        
    }
    
    /**
     * Updates Requirement in ALM
     * @param req
     * @param reqId
     * @throws Exception
     */
    public static void updateRequirement(Requirement req, String reqId) throws Exception
    {
        String testUrl = connector().buildEntityCollectionUrl("requirements/")+reqId;
        connector().put(testUrl, Requirement.class, null, null, req);
        
    }
    
    public static void updateTestSet(TestSet testSet, String testSetId) throws Exception
    {
        String testUrl = connector().buildEntityCollectionUrl("test-sets/")+testSetId;
        connector().put(testUrl, Test.class, null, null, testSet);
        
    }
    
    public static String getFolderId(String type, String folderName) throws Exception
    {
        //add logic here to parse folder path and return the immediate parent folder
    	String query = "{name[\""+ folderName+ "\"]}";
        //String testUrl = connector().buildEntityCollectionUrl("test-folders");
        String testUrl = connector().buildEntityCollectionUrl(type+ "?query="+URLEncoder.encode(query,"UTF-8")+"&fields=id");
    	EntitiesResult er =  connector().get(testUrl, EntitiesResult.class, null, null);
    	String folderId = getEntityVal(er,folderName,"id");
        return folderId;
    }
    
    public static String getFolderId(String type, String pFolderName, String folderName) throws Exception
    {
        String query = "{name[\""+ folderName+ "\"];parent-id["+getFolderId("test-folders",pFolderName)+"]}";
        //String testUrl = connector().buildEntityCollectionUrl("test-folders");
        String testUrl = connector().buildEntityCollectionUrl(type+ "?query="+URLEncoder.encode(query,"UTF-8")+"&fields=id");
    	EntitiesResult er =  connector().get(testUrl, EntitiesResult.class, null, null);
    	String folderId = getEntityVal(er,folderName,"id");
        return folderId;
    }
    
    public static String getTestId(String testName) throws Exception
    {
        String query = "{name[\""+ testName+ "\"]}";
        //String testUrl = connector().buildEntityCollectionUrl("test-folders");
        String testUrl = connector().buildEntityCollectionUrl("tests?query="+URLEncoder.encode(query,"UTF-8")+"&fields=id");
    	EntitiesResult er =  connector().get(testUrl, EntitiesResult.class, null, null);
    	String testId = getEntityVal(er,testName,"id");
        return testId;
    }
    
    public static String getReqId(String reqName) throws Exception
    {
        String query = "{name[\""+ reqName+ "\"]}";
        //String testUrl = connector().buildEntityCollectionUrl("test-folders");
        String testUrl = connector().buildEntityCollectionUrl("requirements?query="+URLEncoder.encode(query,"UTF-8")+"&fields=id");
    	EntitiesResult er =  connector().get(testUrl, EntitiesResult.class, null, null);
    	String reqId = getEntityVal(er,reqName,"id");
        return reqId;
    }
    
    public static String getTestSetId(String testName) throws Exception
    {
        String query = "{name[\""+ testName+ "\"]}";
        //String testUrl = connector().buildEntityCollectionUrl("test-folders");
        String testUrl = connector().buildEntityCollectionUrl("test-sets?query="+URLEncoder.encode(query,"UTF-8")+"&fields=id");
    	EntitiesResult er =  connector().get(testUrl, EntitiesResult.class, null, null);
    	String testId = getEntityVal(er,testName,"id");
        return testId;
    }
    
    /**
     * 
     * @param er EntitiesResult
     * @param fieldCriteria - Lookup Criteria 1 to be matched
     * @param fieldTarget - Lookup Field
     * @return Value of Lookup Field
     */
    public static String getEntityVal(EntitiesResult er, String fieldCriteria, String fieldTarget){
    	if(er.entity()==null)
    		return null;
    			
    	if(er.entity().size()==1){
    		return er.entity().get(0).fieldValue(fieldTarget);
    	}
    	for(Entity entity : er.entity()){
    		String currentField = entity.name();
    		if(currentField.equalsIgnoreCase(fieldCriteria)){
    			return entity.fieldValue(fieldTarget);
    		}    		
    	}
    	return null;
    }
    

}
