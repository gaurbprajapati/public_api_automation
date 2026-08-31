package io.rcrm.api.subscriptions.receiveWebhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.api.util.DateUtil;
import com.qa.api.util.WebhookHelper;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.RetryOn500OrSkippedAnalyzer;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.Subscription;
import io.rcrm.api.pojo.UpdatePitchStage;
import io.rcrm.api.pojo.albatross.PitchCandidatePipeline;
import io.rcrm.api.pojo.albatross.PitchPipelineStages;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.*;
import io.rcrm.api.testbase.TestBase.AccountType;

import java.util.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CandidateEventsTest extends TestBase{
	 	commanFunction commonFunction=new commanFunction();
	    WebhookHelper webhookHelper;
	    JsonPath responseFromWebhook;
	    String entitySlug;
	    @BeforeMethod
	    public void setUp(){
	    	commonFunction.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
	        webhookHelper = new WebhookHelper();
	    }
	    
	        @Owner("Rahul Shibu")
	        @Test(retryAnalyzer = RetryOn500OrSkippedAnalyzer.class, groups = "nightly-build")
    public void eventCandidatePitched() {
        String event = "candidate.pitched";
        
        // Delete all existing subscriptions
        commonFunction.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
        
        // Create a new subscription for the candidate.pitched event
        Subscription subscription = new Subscription(event, webhookHelper.getTargetURL());
        RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);
        
        // Trigger the event by pitching a candidate
        Response response = (Response) pitchCandidate("response");
        
        entitySlug = response.jsonPath().getString("data.candidate_slug");
        
        JsonPath responseFromWebhook;
        try {
            // Fetch the webhook data
            responseFromWebhook = new JsonPath(webhookHelper.getData("candidate_slug"));
        } catch (Exception e) {
            Assert.fail("Failed to fetch Webhook Data for event " + event);
            return;  // Ensure the test fails and exits if fetching data fails
        }
		try {
			webhookAssertion(responseFromWebhook,response);
		}
		catch (Exception e) {
			Assert.fail("Failed to assert webhook response");
		}
        responseFromWebhook.prettyPrint();
        // Verify the response
        Assert.assertEquals(responseFromWebhook.getString("candidate_slug"), entitySlug);
    }
	        @Owner("Rahul Shibu")
	        @Test(retryAnalyzer = RetryOn500OrSkippedAnalyzer.class, groups = "nightly-build")
    public void eventPitchUpdated() {
        String event = "candidate.pitch.updated";
	        
	        // Delete all existing subscriptions
	        commonFunction.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
	        
	        // Create a new subscription for the pitch.updated event
	        Subscription subscription = new Subscription(event, webhookHelper.getTargetURL());
	        RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);
	        
	        // Trigger the event by updating a pitch
			Map<String, String> pathParameter = (Map<String, String>) pitchCandidate("map");
			
	        
	        //Updating Pitch Stage
	        String updatePitchStagePath="pitch/{candidate}/updated-stage/{contact}";

	        UpdatePitchStage updatePitchStage=new UpdatePitchStage();
	        int customStageID = createCustomPitchStage();
	        updatePitchStage.setStatus_id(customStageID);
	        updatePitchStage.setStage_date(DateUtil.getTodayDateString("yyyy-MM-dd"));
	        String generatedString = RandomStringUtils.randomAlphabetic(4);
	        updatePitchStage.setRemark("Update Pitch Stage Remark "+generatedString);
	        Response response=RestClient.doPost1("JSON",baseURL,updatePitchStagePath,ThreadManager.getAccountApiKey(),null,pathParameter,true,updatePitchStage);
	        response.prettyPrint();
	        Assert.assertEquals(response.getStatusCode(), 200);
	        entitySlug = response.jsonPath().getString("data.candidate_slug");
	        
	        JsonPath responseFromWebhook;
	        try {
	            // Fetch the webhook data
	
	            responseFromWebhook = new JsonPath(webhookHelper.getData("candidate_slug"));
	        } catch (Exception e) {
	            Assert.fail("Failed to fetch Webhook Data for event " + event);
	            return;  // Ensure the test fails and exits if fetching data fails
	        }
			try {
				webhookAssertion(responseFromWebhook,response);
			}
			catch (Exception e) {
				Assert.fail("Failed to assert webhook response");
			}
	     // Verify the response
	        responseFromWebhook.prettyPrint();
	        // Verify the response
	        Assert.assertEquals(responseFromWebhook.getString("candidate_slug"), entitySlug);
	       
	    }
	    
	        @Owner("Rahul Shibu")
	        @Test(retryAnalyzer = RetryOn500OrSkippedAnalyzer.class, groups = "nightly-build")
    public void eventCandidateUnassigned() {

    	String event = "candidate.unassigned";
	    	
	    	Response response1=  commonFunction.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey());
	    	entitySlug =response1.jsonPath().getString("slug");
	    	
			String jobSlug = commonFunction.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
	        
	        // Delete all existing subscriptions
	        commonFunction.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
	        
	        // Create a new subscription for the candidate.unassigned event
	        Subscription subscription = new Subscription(event, webhookHelper.getTargetURL());
	        RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);

	        Map<String, String> pathParamters = new HashMap<String, String>();
			pathParamters.put("candidate", entitySlug);
			String basePath1 = "candidates/{candidate}/assign";
			Map<String, String> queryParameters = new HashMap<String, String>();
			queryParameters.put("job_slug", jobSlug);
			
			Response response2 = RestClient.doPost1("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
					true, null);

			response2.prettyPrint();
			Assert.assertEquals(response1.getStatusCode(), 200);

			String basePath = "candidates/{candidate}/unassign";
			
			Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
					true, null);

			Assert.assertEquals(response.getStatusCode(), 200);

	        JsonPath responseFromWebhook;
	        try {
	            // Fetch the webhook data — search by actual slug value in body content
	            responseFromWebhook = new JsonPath(webhookHelper.getData(entitySlug));
	        } catch (Exception e) {
	            Assert.fail("Failed to fetch Webhook Data for event " + event);
	            return;
	        }
	        // Verify the webhook delivered the correct candidate
	        Assert.assertEquals(responseFromWebhook.getString("slug"), entitySlug,
	        		"Webhook candidate slug must match the unassigned candidate");
	    	
	    }

		public Object pitchCandidate(String whatToReturn) {

	    	Response response1=  commonFunction.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey());
	    	entitySlug =response1.jsonPath().getString("slug");
			String companySlug = commonFunction
					.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.getString("slug");
			String contactSlug = commonFunction
					.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath()
					.getString("slug");
			String path = "pitch/{candidate}/contact/{contact}";
			Map<String, String> pathParameter = new HashMap<>();
			pathParameter.put("candidate", entitySlug);
			pathParameter.put("contact", contactSlug);
			Response response= RestClient.doPost1("JSON",baseURL,path, ThreadManager.getAccountApiKey(),null, pathParameter,true, null);

	        Assert.assertEquals(response.getStatusCode(), 200);
	        if(whatToReturn.contains("response")) {
	        	return response;
	        }else {
	        	return pathParameter;
	        }
			
		}
		public int createCustomPitchStage() {
	        String pitchStagesPath="pitch-candidate-pipeline";
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


	        //Getting custom pitch stage ID
	        Response response1=RestClient.doGet("JSON",albatrossURL,pitchStagesPath,ThreadManager.getOwnerAlbatrossToken(),null,null,true);
	        Assert.assertEquals(response1.getStatusCode(), 200);

	        return response1.jsonPath().get("data.pitchPipelineStages[0].id");
		}

		public void webhookAssertion(JsonPath webhookRes,Response apiRes) throws JsonProcessingException {
			List<String> fields=new ArrayList<>();
			JsonPath apiJson=apiRes.jsonPath();
			ObjectMapper objectMapper= new ObjectMapper();
			JSONObject obj= new JSONObject(objectMapper.writeValueAsString(apiJson.get("data")));
			Iterator<String> keys = obj.keys();
			while(keys.hasNext()) {
				fields.add(keys.next());
			}
			for(String key:fields) {
				if(webhookRes.getString(key).contains(apiJson.getString("data."+key))) {
					continue;
				}
				else{
					Assert.fail("API response and Webhook response do not match for field "+key);
				}
			}
		}

	public void webhookAssertionForCandidateUnassigned(JsonPath webhookRes, Response apiRes) throws JsonProcessingException {
		List<String> fields = new ArrayList<>();
		JsonPath apiJson = apiRes.jsonPath();
		ObjectMapper objectMapper = new ObjectMapper();
		JSONObject obj = new JSONObject(objectMapper.writeValueAsString(apiJson.getMap("")));
		Iterator<String> keys = obj.keys();
		while (keys.hasNext()) {
			fields.add(keys.next());
		}
		for (String key : fields) {
			if ((webhookRes.getString(key) == null && apiJson.getString(key) == null) || (webhookRes.getString(key).contains(apiJson.getString(key)) || webhookRes.getString(key).equals(apiJson.getString(key)))) {
				continue;
			} else if (key.equals("resume")) {
				Assert.assertTrue(webhookRes.getString(key + ".filename").equals(apiJson.getString(key + ".filename")));
			} else if (key.equals("avatar")) {
				String apiFileLink = apiJson.getString(key);
				String webhookFileLink = webhookRes.getString(key);
				int index = apiFileLink.indexOf("/avatar/");
				String apiTrimmedUrl = apiFileLink.substring(0, index + "/avatar/".length());
				String webhookTrimmedUrl = webhookFileLink.substring(0, index + "/avatar/".length());
				Assert.assertEquals(apiTrimmedUrl, webhookTrimmedUrl);
			} else {
				Assert.fail("API response and Webhook response do not match for field " + key);
			}
		}
	}

	    @AfterMethod(alwaysRun = true)
	    public void tearDown() {
	        webhookHelper.clear();
	    }

}
