package io.rcrm.api.candidate;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.UpdatePitchStage;
import io.rcrm.api.pojo.albatross.PitchCandidatePipeline;
import io.rcrm.api.pojo.albatross.PitchPipelineStages;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class PitchCandidateTest extends TestBase {

    commanFunction commanFunction = new commanFunction();
    Random random=new Random();
    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void pitchCandidateToContact(){
        String candidateSlug= commanFunction.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
        String companySlug=commanFunction.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
        String contactSlug=commanFunction.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().getString("slug");
        String path= "pitch/{candidate}/contact/{contact}";
        Map<String,String> pathParameter= new HashMap<>();
        pathParameter.put("candidate", candidateSlug);
        pathParameter.put("contact", contactSlug);

        Response response= RestClient.doPost1("JSON",baseURL,path, ThreadManager.getAccountApiKey(),null, pathParameter,true, null);
        response.prettyPrint();
        Assert.assertEquals(response.getStatusCode(), 200);
        String message= response.jsonPath().getString("message");
        String success= response.jsonPath().getString("success");
        if(!success.equals("true") && !message.equals("Pitched successfully")) {
            Assert.fail("Pitching candidate to contact failed");
        }
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//pitchCandidateToContact.json"));

    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void pitchCandidateToContactWithInvalidSlug(){
        String candidateSlug= commanFunction.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
        String companySlug=commanFunction.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
        String contactSlug=commanFunction.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().getString("slug");
        String path= "pitch/{candidate}/contact/{contact}";
        Map<String,String> pathParameter= new HashMap<>();
        pathParameter.put("candidate", candidateSlug+"1234");
        pathParameter.put("contact", contactSlug+"1234");

        Response response= RestClient.doPost1("JSON",baseURL,path, ThreadManager.getAccountApiKey(),null, pathParameter,true, null);
        response.prettyPrint();
        Assert.assertEquals(response.getStatusCode(), 422);
        String message1= response.jsonPath().getList("candidate").get(0).toString();
        String message2= response.jsonPath().getList("contact").get(0).toString();
        Assert.assertEquals(message1, "Invalid candidate");
        Assert.assertEquals(message2, "Invalid contact");

    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void updatePitchStage(){
        ArrayList<String> slugs=pitchCandidatesToContact();
        String candidateSlug=slugs.get(0);
        String contactSlug=slugs.get(1);
        String pitchStagesPath="pitch-candidate-pipeline";
        String generatedString = RandomStringUtils.randomAlphabetic(4);

        //Creating Custom Pitch Stage
        String pitchStageName="Test Pitch Stage";
        PitchPipelineStages pitchPipelineStages=new PitchPipelineStages();
        pitchPipelineStages.setLabel(pitchStageName);
        ArrayList<PitchPipelineStages> pitchPipelineStagesArrayList=new ArrayList<>();
        pitchPipelineStagesArrayList.add(pitchPipelineStages);
        PitchCandidatePipeline pitchCandidatePipeline=new PitchCandidatePipeline();
        pitchCandidatePipeline.setPitchPipelineStages(pitchPipelineStagesArrayList);

        Response response=RestClient.doPost("JSON",albatrossURL,pitchStagesPath,ThreadManager.getOwnerAlbatrossToken(),null,true,pitchCandidatePipeline);
        Assert.assertEquals(response.getStatusCode(), 200);
        String message=response.jsonPath().getString("message");
        String success=response.jsonPath().getString("message_type");


        //Getting custom pitch stage ID
        Response response1=RestClient.doGet("JSON",albatrossURL,pitchStagesPath,ThreadManager.getOwnerAlbatrossToken(),null,null,true);
        Assert.assertEquals(response1.getStatusCode(), 200);
        response1.prettyPrint();
        int customStageID=response1.jsonPath().get("data.pitchPipelineStages[1].id");

        //Updating Pitch Stage
        String updatePitchStagePath="pitch/{candidate}/updated-stage/{contact}";
        Map<String,String> pathParameter= new HashMap<>();
        pathParameter.put("candidate", candidateSlug);
        pathParameter.put("contact", contactSlug);
        UpdatePitchStage updatePitchStage=new UpdatePitchStage();
        updatePitchStage.setStatus_id(customStageID);
        updatePitchStage.setStage_date(DateUtil.getTodayDateString("yyyy-MM-dd"));
        updatePitchStage.setRemark("Update Pitch Stage Remark "+generatedString);
        Response response2=RestClient.doPost1("JSON",baseURL,updatePitchStagePath,ThreadManager.getAccountApiKey(),null,pathParameter,true,updatePitchStage);
        response2.prettyPrint();
        String messageUpdate=response2.jsonPath().getString("message");
        String successUpdate=response2.jsonPath().getString("success");
        Assert.assertEquals(response2.getStatusCode(), 200);
        if(!successUpdate.equals("true") && !messageUpdate.equals("Stage Updated Successfully")) {
            Assert.fail("Updating Pitch stage failed");
        }
        response2.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//pitchCandidateToContact.json"));
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void updatePitchStageWithInvalidStageId(){
        ArrayList<String> slugs=pitchCandidatesToContact();
        String candidateSlug=slugs.get(0);
        String contactSlug=slugs.get(1);
        String updatePitchStagePath="pitch/{candidate}/updated-stage/{contact}";
        Map<String,String> pathParameter= new HashMap<>();
        pathParameter.put("candidate", candidateSlug);
        pathParameter.put("contact", contactSlug);
        UpdatePitchStage updatePitchStage=new UpdatePitchStage();
        updatePitchStage.setStatus_id(random.nextInt(1000));
        updatePitchStage.setStage_date(DateUtil.getTodayDateString("yyyy-MM-dd"));
        updatePitchStage.setRemark("Update Pitch Stage Remark");
        Response response2=RestClient.doPost1("JSON",baseURL,updatePitchStagePath,ThreadManager.getAccountApiKey(),null,pathParameter,true,updatePitchStage);
        Assert.assertEquals(response2.getStatusCode(), 422);
        String message=response2.jsonPath().getList("status_id").get(0).toString();
        Assert.assertEquals(message, "Invalid status id");
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void GetContactWhereCandidateIsPitched(){
        ArrayList<String> slugs=pitchCandidatesToContact();
        String candidateSlug=slugs.get(0);
        String contactSlug=slugs.get(1);
        String path="pitch/{entity}/pitch-stage/{entitySlug}";
        Map<String,String> pathParameter= new HashMap<>();
        pathParameter.put("entity", "candidate");
        pathParameter.put("entitySlug", candidateSlug);
        Response response=RestClient.doGet("JSON",baseURL,path,ThreadManager.getAccountApiKey(),null,pathParameter,true);
        response.prettyPrint();
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("data.records[0].contact_slug"), contactSlug);
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void GetCandidatesPitchedToContact(){
        ArrayList<String> slugs=pitchCandidatesToContact();
        String candidateSlug=slugs.get(0);
        String contactSlug=slugs.get(1);
        String path="pitch/{entity}/pitch-stage/{entitySlug}";
        Map<String,String> pathParameter= new HashMap<>();
        pathParameter.put("entity", "contact");
        pathParameter.put("entitySlug", contactSlug);
        Response response=RestClient.doGet("JSON",baseURL,path,ThreadManager.getAccountApiKey(),null,pathParameter,true);
        response.prettyPrint();
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("data.records[0].candidate_slug"), candidateSlug);
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void GetContactWhereCandidateIsPitchedVerify404(){
        ArrayList<String> slugs=pitchCandidatesToContact();
        String candidateSlug=slugs.get(0);
        String contactSlug=slugs.get(1);
        String path="pitch/{entity}/pitch-stage/{entitySlug}";
        Map<String,String> pathParameter= new HashMap<>();
        pathParameter.put("entity", "candidate");
        pathParameter.put("entitySlug", candidateSlug+RandomStringUtils.randomAlphabetic(3));
        Response response=RestClient.doGet("JSON",baseURL,path,ThreadManager.getAccountApiKey(),null,pathParameter,true);
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getInt("'error code'"),404);
        Assert.assertEquals(response.jsonPath().getString("errorMessage"), "candidate not found");
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void GetCandidatesPitchedToContactVerify404(){
        ArrayList<String> slugs=pitchCandidatesToContact();
        String candidateSlug=slugs.get(0);
        String contactSlug=slugs.get(1);
        String path="pitch/{entity}/pitch-stage/{entitySlug}";
        Map<String,String> pathParameter= new HashMap<>();
        pathParameter.put("entity", "contact");
        pathParameter.put("entitySlug", contactSlug+RandomStringUtils.randomAlphabetic(3));
        Response response=RestClient.doGet("JSON",baseURL,path,ThreadManager.getAccountApiKey(),null,pathParameter,true);
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getInt("'error code'"),404);
        Assert.assertEquals(response.jsonPath().getString("errorMessage"), "contact not found");
    }
    
    
    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void pitchHistoryOfACandidateVerify200() {
        ArrayList<String> slugs=pitchCandidatesToContact();
        String candidateSlug=slugs.get(0);
        String contactSlug=slugs.get(1);

    	String path ="pitch/pitch-candidate-history/{candidate}";
        Map<String,String> parameter= new HashMap<>();
        parameter.put("candidate", candidateSlug);
   
        Response response= RestClient.doGet("JSON",baseURL,path, ThreadManager.getAccountApiKey(),null,  parameter,true);
        response.prettyPrint();

        // Assert the status code
        Assert.assertEquals(200, response.getStatusCode(), "Expected status code is 200");

        // Parsing the JSON response
        String status = response.jsonPath().getString("status");
        String message = response.jsonPath().getString("message");
        String actualContactSlug = response.jsonPath().getString("data.records[0].contact_slug");
        int statusId = response.jsonPath().getInt("data.records[0].status_id");
        String candidateStatus = response.jsonPath().getString("data.records[0].candidate_status");
       
        // Assert the response body
        Assert.assertEquals("success", status, "Expected status is 'success'");
        Assert.assertEquals("History Fetched successfully", message, "Expected message is 'History Fetched successfully'");
        Assert.assertEquals(contactSlug,actualContactSlug, "Expected contact_slug is "+contactSlug);
        Assert.assertEquals(1, statusId, "Expected status_id is 1");
        Assert.assertEquals("Pitched", candidateStatus, "Expected candidate_status is 'Pitched'");
     
    }
    
    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void pitchHistoryOfACandidateWithUnauthorizedVerify401() {
        ArrayList<String> slugs=pitchCandidatesToContact();
        String candidateSlug=slugs.get(0);
        String contactSlug=slugs.get(1);

    	String path ="pitch/pitch-candidate-history/{candidate}";
        Map<String,String> parameter= new HashMap<>();
        parameter.put("candidate", candidateSlug);
   
        Response response= RestClient.doGet("JSON",baseURL,path, ThreadManager.getAccountApiKey()+random.nextInt(),null,  parameter,true);
        response.prettyPrint();

        // Assert the status code is 401
        Assert.assertEquals(401, response.getStatusCode(), "Expected status code is 401");

        // Assert the error message
        String error = response.jsonPath().getString("error");
        Assert.assertEquals("Unauthorized", error, "Expected error message is 'Unauthorized'");
     
    }
    
    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void pitchHistoryOfANonExistentCandidateVerify404() {
        ArrayList<String> slugs=pitchCandidatesToContact();
        String candidateSlug=slugs.get(0);
        String contactSlug=slugs.get(1);

        // Deleting the candidate to ensure it doesn't exist for the 404 test
        List<String> deleteEntity = new ArrayList<>();
        deleteEntity.add(candidateSlug);
		commanFunction.deleteEntities(baseURL,ThreadManager.getAccountApiKey(),deleteEntity);

        String path = "pitch/pitch-candidate-history/{candidate}";
        Map<String, String> parameter = new HashMap<>();
        parameter.put("candidate", candidateSlug);

        Response response = RestClient.doGet("JSON", baseURL, path, ThreadManager.getAccountApiKey(), null, parameter, true);
        response.prettyPrint();

        // Parsing the JSON response
        boolean error = response.jsonPath().getBoolean("error");
        int errorCode = response.jsonPath().getInt("errorCode");
        String errorMessage = response.jsonPath().getString("errorMessage");

        // Assert the response body
        Assert.assertTrue(error, "Expected error to be true");
        Assert.assertEquals(404, errorCode, "Expected error code is 404");
        Assert.assertEquals("Candidate doesn't exist", errorMessage, "Expected error message is 'Candidate doesn't exist'");
    }
    
    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void pitchHistoryOfAContactVerify200() {
        ArrayList<String> slugs=pitchCandidatesToContact();
        String candidateSlug=slugs.get(0);
        String contactSlug=slugs.get(1);
    	String path ="pitch/pitch-contact-history/{contact}";
        Map<String,String> parameter= new HashMap<>();
        parameter.put("contact", contactSlug);
   
        Response response= RestClient.doGet("JSON",baseURL,path, ThreadManager.getAccountApiKey(),null,  parameter,true);
        response.prettyPrint();

        // Assert the status code
        Assert.assertEquals(200, response.getStatusCode(), "Expected status code is 200");

        // Parsing the JSON response
        String status = response.jsonPath().getString("status");
        String message = response.jsonPath().getString("message");
        String actualCandidatetSlug = response.jsonPath().getString("data.records[0].candidate_slug");
        int statusId = response.jsonPath().getInt("data.records[0].status_id");
        String candidateStatus = response.jsonPath().getString("data.records[0].candidate_status");
       
        // Assert the response body
        Assert.assertEquals("success", status, "Expected status is 'success'");
        Assert.assertEquals("History Fetched successfully", message, "Expected message is 'History Fetched successfully'");
        Assert.assertEquals(candidateSlug,actualCandidatetSlug, "Expected candidatetSlug is "+candidateSlug);
        Assert.assertEquals(1, statusId, "Expected status_id is 1");
        Assert.assertEquals("Pitched", candidateStatus, "Expected candidate_status is 'Pitched'");
     
    }
    
    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void pitchHistoryOfANonExistentContactVerify404() {
        ArrayList<String> slugs=pitchCandidatesToContact();
        String candidateSlug=slugs.get(0);
        String contactSlug=slugs.get(1);
        // Deleting the contact to ensure it doesn't exist for the 404 test
        List<String> deleteEntity = new ArrayList<>();
        deleteEntity.add(contactSlug);
		commanFunction.deleteEntities(baseURL,ThreadManager.getAccountApiKey(),deleteEntity);

        String path = "pitch/pitch-contact-history/{contact}";
        Map<String, String> parameter = new HashMap<>();
        parameter.put("contact", contactSlug);

        
        Response response = RestClient.doGet("JSON", baseURL, path, ThreadManager.getAccountApiKey(), null, parameter, true);
        response.prettyPrint();

        // Parsing the JSON response
        boolean error = response.jsonPath().getBoolean("error");
        int errorCode = response.jsonPath().getInt("errorCode");
        String errorMessage = response.jsonPath().getString("errorMessage");

        // Assert the response body
        Assert.assertTrue(error, "Expected error to be true");
        Assert.assertEquals(404, errorCode, "Expected error code is 404");
        Assert.assertEquals("Contact doesn't exist", errorMessage, "Expected error message is 'Contact doesn't exist'");
    }
    
    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void pitchHistoryOfACandidateAndContactVerify200() {
        ArrayList<String> slugs=pitchCandidatesToContact();
        String candidateSlug=slugs.get(0);
        String contactSlug=slugs.get(1);
        Map<String,String> pathParameter= new HashMap<>();
        pathParameter.put("candidate", candidateSlug);
        pathParameter.put("contact", contactSlug);
        
        String path ="pitch/{candidate}/history/{contact}";
        Response response= RestClient.doGet("JSON",baseURL,path, ThreadManager.getAccountApiKey(),null,pathParameter,true);
        response.prettyPrint();

        // Assert the status code
        Assert.assertEquals(200, response.getStatusCode(), "Expected status code is 200");
        
     // Parsing the JSON response
        String status = response.jsonPath().getString("status");
        String message = response.jsonPath().getString("message");
        int statusId = response.jsonPath().getInt("data.records[0].status_id");
        String candidateStatus = response.jsonPath().getString("data.records[0].candidate_status");
       
        // Assert the response body
        Assert.assertEquals("success", status, "Expected status is 'success'");
        Assert.assertEquals("History Fetched successfully", message, "Expected message is 'History Fetched successfully'");
        Assert.assertEquals(1, statusId, "Expected status_id is 1");
        Assert.assertEquals("Pitched", candidateStatus, "Expected candidate_status is 'Pitched'");
    	
    }
    
    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void pitchHistoryOfNonExistentCandidateOrContactVerify404() {
        ArrayList<String> slugs=pitchCandidatesToContact();
        String candidateSlug=slugs.get(0);
        String contactSlug=slugs.get(1);
        
        
        Map<String, String> parameter = new HashMap<>();
        parameter.put("candidate", candidateSlug);
        parameter.put("contact", contactSlug);

        
        // Deleting the contact to ensure it doesn't exist for the 404 test
        List<String> deleteEntity = new ArrayList<>();
        //to have more test coverage some times candidate some times contact slug is corrupted
        if(random.nextBoolean()) {
        	deleteEntity.add(candidateSlug);
        }else {
        	deleteEntity.add(contactSlug);
        }
        commanFunction.deleteEntities(baseURL,ThreadManager.getAccountApiKey(),deleteEntity);

        String path ="pitch/{candidate}/history/{contact}";
        Response response= RestClient.doGet("JSON",baseURL,path, ThreadManager.getAccountApiKey(),null,parameter,true);
        response.prettyPrint();

        // Parsing the JSON response
        boolean error = response.jsonPath().getBoolean("error");
        int errorCode = response.jsonPath().getInt("errorCode");
        String errorMessage = response.jsonPath().getString("errorMessage");

        // Assert the response body
        Assert.assertTrue(error, "Expected error to be true");
        Assert.assertEquals(404, errorCode, "Expected error code is 404");

        // Smart assertion to handle both error messages
        Assert.assertTrue(
            errorMessage.equals("Contact doesn't exist") || errorMessage.equals("Candidate doesn't exist"),
            "Expected error message to be either 'Contact doesn't exist' or 'Candidate doesn't exist'"
        );
    	
    }

    public ArrayList<String> pitchCandidatesToContact(){
        String candidateSlug= commanFunction.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
        String companySlug=commanFunction.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
        String contactSlug=commanFunction.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().getString("slug");
        String path= "pitch/{candidate}/contact/{contact}";
        Map<String,String> pathParameter= new HashMap<>();
        pathParameter.put("candidate", candidateSlug);
        pathParameter.put("contact", contactSlug);

        Response response= RestClient.doPost1("JSON",baseURL,path, ThreadManager.getAccountApiKey(),null, pathParameter,true, null);
        response.prettyPrint();
        Assert.assertEquals(response.getStatusCode(), 200);
        ArrayList<String> slugs=new ArrayList<>();
        slugs.add(candidateSlug);
        slugs.add(contactSlug);
        return slugs;
    }





}
