package io.recruitcrm.albatross.job;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.*;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerTrigger;
import io.rcrm.api.pojo.albatross.emailtrigger.Emailtriggersetting;
import io.rcrm.api.pojo.albatross.emailtrigger.GetEmailTriggerList;
import io.rcrm.api.pojo.albatross.emailtrigger.NewEmailTrigger;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class GetAllAssignedCandidates_Test extends TestBase {

	String albatrossToken;
	int accountId;
	String authToken;

	@BeforeClass(alwaysRun = true)	public void getAccountAPI() {
		albatrossToken = ThreadManager.getOwnerAlbatrossToken();
		accountId = ThreadManager.getAccount().getAccountId();
		authToken = ThreadManager.getAccountApiKey();
	}

	JavaFakerTrigger fakerTrigger = new JavaFakerTrigger();
	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

	@Owner("Divya")
	@Test(dataProvider = "getTriggerId", groups = "nightly-build")
	public void getAllAssignedCandidatesForValidJobIdTest_200(int trigger, int jobId, int hiringStageId) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("jobId", String.valueOf(jobId));
		Map<String, String> queryParamters = new HashMap<String, String>();
		queryParamters.put("triggerType", String.valueOf(trigger));
		queryParamters.put("firstBatchOnlineLink", String.valueOf(1));
		queryParamters.put("hiringStageChangeId", String.valueOf(hiringStageId));
		Response response = RestClient.doGet("JSON", albatrossURL, "jobs/{jobId}/get-all-assigned-candidates/get",
				albatrossToken, queryParamters, pathParamters, true);
		Assert.assertEquals("Request Failure", response.statusCode(), 200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("getAllAssignedCandidates.json"));

	}

	@Owner("Divya")
	@Test(dataProvider = "getTriggerId", groups = "nightly-build")
	public void getAllAssignedCandidatesForInvalidJobIdTest_200(int trigger, int jobId, int hiringStageId) {
		// Returns data with null candidates
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("jobId", String.valueOf(fakerTrigger.getStageId()));
		Map<String, String> queryParamters = new HashMap<String, String>();
		queryParamters.put("triggerType", String.valueOf(trigger));
		queryParamters.put("firstBatchOnlineLink", String.valueOf(1));
		queryParamters.put("hiringStageChangeId", String.valueOf(hiringStageId));
		Response response = RestClient.doGet("JSON", albatrossURL, "jobs/{jobId}/get-all-assigned-candidates/get",
				albatrossToken, queryParamters, pathParamters, true);
		Assert.assertEquals("Request Failure", response.statusCode(), 200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("getAllAssignedCandidates.json"));

	}

	@Owner("Divya")
	@Test(dataProvider = "getTriggerId", groups = "nightly-build")
	public void getAllAssignedCandidatesForInvalidTriggerTypeTest_500(int trigger, int jobId, int hiringStageId) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("jobId", String.valueOf(jobId));
		Map<String, String> queryParamters = new HashMap<String, String>();
		queryParamters.put("triggerType", String.valueOf(fakerTrigger.getTriggerId()));
		queryParamters.put("firstBatchOnlineLink", String.valueOf(1));
		queryParamters.put("hiringStageChangeId", String.valueOf(hiringStageId));
		Response response = RestClient.doGet("JSON", albatrossURL, "jobs/{jobId}/get-all-assigned-candidates/get",
				albatrossToken, queryParamters, pathParamters, true);
		Assert.assertEquals("Request Failure", response.statusCode(), 500);

	}

	@Owner("Divya")
	@Test(dataProvider = "getTriggerId", groups = "nightly-build")
	public void unauthorizedUserAccessToGetAllAssignedCandidatesTest_401(int trigger, int jobId, int hiringStageId) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("jobId", String.valueOf(jobId));
		Map<String, String> queryParamters = new HashMap<String, String>();
		queryParamters.put("triggerType", String.valueOf(trigger));
		queryParamters.put("firstBatchOnlineLink", String.valueOf(1));
		queryParamters.put("hiringStageChangeId", String.valueOf(fakerTrigger.getStageId()));
		Response response = RestClient.doGet("JSON", albatrossURL, "jobs/{jobId}/get-all-assigned-candidates/get",
				albatrossToken + 123, queryParamters, pathParamters, true);
		Assert.assertEquals("Request Failure", response.statusCode(), 401);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertTrue("Response body differs", jsonPath.get("error").toString().contains("Unauthorized"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@DataProvider(parallel = true)
	public Object[][] getTriggerId() {

		List<Integer> hiringStageIds = new ArrayList<>();
		JsonPath jsonPath1 = RestClient.doPost("JSON", albatrossURL,
				"global/get-hiring-pipeline-stages-for-usermode/get", albatrossToken, null, true, null).jsonPath();
		for (int i = 0; i < 3; i++) {
			hiringStageIds.add(jsonPath1.get("data[" + i + "].id"));
		}
		int hiringStageId = hiringStageIds.get(fakerTrigger.getRandomIntValue(0, 2));
		Emailtriggersetting emailtriggersetting = new Emailtriggersetting(fakerTrigger.getTriggerName(), 3,
				hiringStageId, 0);
		NewEmailTrigger newEmailTrigger = new NewEmailTrigger(emailtriggersetting);
		Response response = RestClient.doPost("JSON", albatrossURL, "email-triggers", albatrossToken, null, true,
				newEmailTrigger);
		Assert.assertEquals("Request Failure", response.statusCode(), 200);
		GetEmailTriggerList getEmailTriggerList = new GetEmailTriggerList();
		getEmailTriggerList.setPage_size(fakerTrigger.getRandomIntValue(2, 20));
		JsonPath jsonPath = RestClient
				.doPost("JSON", albatrossURL, "email-triggers/get", albatrossToken, null, true, getEmailTriggerList)
				.jsonPath();
		int triggerId = jsonPath.get("data.records[0].trigger");
		commanFunction function = new commanFunction();
		JsonPath jsonPath2 = function.assignCandidateToJob(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String jobSlug = jsonPath2.getString("job_slug");
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("job", jobSlug);
		JsonPath jsonPath3 = (RestClient.doGet("JSON", baseURL, "jobs/{job}", ThreadManager.getAccountApiKey(), null,
				pathParamters, true)).jsonPath();
		int jobId = jsonPath3.getInt("id");
		Object[][] data = { { triggerId, jobId, hiringStageId } };
		return data;
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "assignCandidatesToJob", groups = "nightly-build")
	public void verifyNoDuplicateCandidatesInHiringStage_Test(int jobId) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", jobId);
		jsonObject.put("limit", 10);
		jsonObject.put("type", "assigned");
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("jobId", String.valueOf(jobId));

		Response response = RestClient.doPost1("JSON", albatrossURL, "jobs/{jobId}/get-assigned-candidates/get",
				albatrossToken, null, pathParamters, true, jsonObject);
		response.then().statusCode(200);
		response.then().body("data.candidates", Matchers.not(Matchers.empty()));
		ArrayList<Integer> candidateIds = new ArrayList<>();
		candidateIds.addAll(response.jsonPath().getList("data.candidates.id"));
		
		jsonObject.put("offset", 10);
		jsonObject.put("candidatestatusid", 1);
		Response response1 = RestClient.doPost1("JSON", albatrossURL, "jobs/{jobId}/get-assigned-candidates/get",
				albatrossToken, null, pathParamters, true, jsonObject);
		response1.then().statusCode(200);
		response1.then().body("data.candidates", Matchers.not(Matchers.empty()));
		ArrayList<Integer> candidateIds2 = new ArrayList<>();
		candidateIds2.addAll(response1.jsonPath().getList("data.candidates.id"));
		Assert.assertTrue("Same candidate id is appearing twice", Collections.disjoint(candidateIds, candidateIds2));
	}

	@DataProvider
	public Object[][] assignCandidatesToJob() {
		JsonPath companyJsonPath = function.createNewCompanyWithMandatoryFields(baseURL, authToken).jsonPath();
		String companySlug = companyJsonPath.get("slug");
		JsonPath contactJsonPath = function.createNewContact_POST(baseURL, authToken, companySlug).jsonPath();
		String contactSlug = contactJsonPath.get("slug");
		JsonPath jobJsonPath = function.createNewJob(baseURL, authToken, companySlug, contactSlug).jsonPath();
		String jobSlug = jobJsonPath.get("slug");
		int jobId = allCrudFunctions.getJobResponse(albatrossURL, albatrossToken, jobSlug).jsonPath().getInt("data.job.id");
		ReaperIntegration.insertBulkRecords(accountId, "candidate", 20);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("page_size", 50);
		jsonObject.put("page", 1);
		JSONObject columns = new JSONObject();
		JSONObject id = new JSONObject();
		id.put("entity", "candidate");
		id.put("field", "id");
		columns.put("id",id);
		jsonObject.put("columns", columns);

		Response response3 = RestClient.doPost1("JSON", albatrossURL, "candidates/search/get",
				albatrossToken, null, null, true, jsonObject);
		response3.then().statusCode(200);
		response3.then().body("data.records", Matchers.not(Matchers.empty()));
		ArrayList<Integer> candidateIds = new ArrayList<>();
		candidateIds.addAll(response3.jsonPath().getList("data.records.id"));

		function.assignMultipleCandidatesToJob(albatrossURL, albatrossToken, candidateIds, jobId);
		
		Object[][] data = { { jobId } };
		return data;
	}
}
