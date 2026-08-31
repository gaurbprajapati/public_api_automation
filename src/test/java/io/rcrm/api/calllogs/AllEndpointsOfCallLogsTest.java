package io.rcrm.api.calllogs;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCallLog;
import io.rcrm.api.javafaker.JavaFakerTask;
import io.rcrm.api.pojo.Call_Log;
import io.rcrm.api.pojo.Task;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class AllEndpointsOfCallLogsTest extends TestBase {

	public AllEndpointsOfCallLogsTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	commanFunction function = new commanFunction();
	JavaFakerCallLog fakerakerCallLog = new JavaFakerCallLog();
	String contactNumber = fakerakerCallLog.getContactNumber();
	String callNotes = fakerakerCallLog.getCall_notes();
	String pastDate = fakerakerCallLog.getPastDate();
	int callLogId;

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void createNewCallLog() {

		JsonPath json;
		String entitySlug = "";
		json = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		entitySlug = json.get("slug");
		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type("CALL_INCOMING");
		callLog.setContact_number(contactNumber);
		callLog.setRelated_to(entitySlug);
		callLog.setRelated_to_type("candidate");
		callLog.setCall_started_on(pastDate);
		callLog.setEnable_auto_populate_teams(0);

		Response response = RestClient.doPost("JSON", baseURL, "call-logs", ThreadManager.getAccountApiKey(), null, true, callLog);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//calllog//createCalllog.json"));
		response.then().statusCode(200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		callLogId = jp.get("id");

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();

		String createdOn = jp.get("created_on");
		String startedOn = jp.get("call_started_on");

		Assert.assertNotSame(createdOn, startedOn, "Created on and started on are not same");

		response.then().body("call_notes", Matchers.containsString(callNotes));
		response.then().body("contact_number", Matchers.is(contactNumber));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is("candidate"));
		response.then().body("collaborator_users", Matchers.containsInAnyOrder(teamsAdded.toArray()));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void showAllCalLogs() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "call-logs", ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//calllog//getAllCalllogs.json"));
		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchCallLogByFields() {
		JsonPath json;

		json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		String entitySlug = json.get("related_to");
		String callNote = json.get("call_notes");

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("related_to", entitySlug);
		queryParameters.put("related_to_type", "candidate");
		queryParameters.put("call_type", "CALL_INCOMING");
		String basePath = "call-logs/search";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("data.call_notes[0]", Matchers.is(callNote));
		response.then().body("data.call_type[0]", Matchers.is("CALL_INCOMING"));
		response.then().body("data.related_to[0]", Matchers.is(entitySlug));
		response.then().body("data.related_to_type[0]", Matchers.is("candidate"));
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void searchCallLogByID() {
		JsonPath json;
		String callLogID = "";

		json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int callLogID_int = json.get("id");
		callLogID = String.valueOf(callLogID_int);
		String entitySlug = json.get("related_to");

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("callLog", callLogID);
		String basePath = "call-logs/{callLog}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is("candidate"));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void editCallLogByID() {

		JsonPath json;
		String entitySlug = "";
		String callLogID = "";

		json = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		entitySlug = json.get("slug");

		json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int callLogID_int = json.get("id");

		callLogID = String.valueOf(callLogID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("callLog", callLogID);
		String basePath = "call-logs/{callLog}";

		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setContact_number(contactNumber);
		callLog.setCall_started_on(pastDate);
		callLog.setCall_type("CALL_INCOMING");
		callLog.setRelated_to(entitySlug);
		callLog.setRelated_to_type("candidate");
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				callLog);

		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//calllog//editCalllog.json"));

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		callLogId = jp.get("id");
		response.then().body("related_to_type", Matchers.is("candidate"));
		response.then().body("contact_number", Matchers.is(contactNumber));
		response.then().body("call_notes", Matchers.containsString(callNotes));
		response.then().body("related_to", Matchers.is(entitySlug));
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void deleteCallLogByID() {
		JsonPath json;
		String callLogID = "";

		json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int callLogID_int = json.get("id");

		callLogID = String.valueOf(callLogID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("callLog", callLogID);
		String basePath = "call-logs/{callLog}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);
		Assert.assertEquals(response.getStatusCode(), 200);

		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains("Deleted Successfully!"), "Deleted Successfully!");
	}

}
