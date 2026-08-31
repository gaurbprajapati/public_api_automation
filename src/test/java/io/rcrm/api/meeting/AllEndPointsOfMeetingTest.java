package io.rcrm.api.meeting;

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
import io.rcrm.api.javafaker.JavaFakerMeeting;
import io.rcrm.api.pojo.Meeting;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class AllEndPointsOfMeetingTest extends TestBase {

	public AllEndPointsOfMeetingTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	commanFunction function = new commanFunction();
	int meetingId;

	JavaFakerMeeting fakerMeeting = new JavaFakerMeeting();
	String meetingName = fakerMeeting.getMeetingName();
	String meetingDescription = fakerMeeting.getDescription();
	String startDate = fakerMeeting.getFutureDate();
	String endDate = fakerMeeting.getEndDate();
	String address = fakerMeeting.getAddress();
	String pastDate = fakerMeeting.getPastDate();


	@Owner("Ajendra Singh")
	@Test(priority = 0, groups = "nightly-build")
	public void createNewMeeting() {

		JsonPath json;
		String entitySlug = "";

		json = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		entitySlug = json.get("slug");

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type("candidate");
		meeting.setStart_date(pastDate);
		meeting.setEnd_date(endDate);
		meeting.setEnable_auto_populate_teams(0);
		meeting.setDo_not_send_calendar_invites(0);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", ThreadManager.getAccountApiKey(), null, true, meeting);

		// get the response body:
		String responseBody = response.getBody().asString();

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();

		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//meeting//createMeeting.json"));
		response.then().body("id", Matchers.notNullValue());
		response.then().body("title", Matchers.containsString(meetingName));
		response.then().body("address", Matchers.containsString(address));
		response.then().body("description", Matchers.containsString(meetingDescription));

		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is("candidate"));
		response.then().body("do_not_send_calendar_invites", Matchers.is(0));
		response.then().body("collaborator_users", Matchers.containsInAnyOrder(teamsAdded.toArray()));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));

	}
	

	@Owner("Harika")
	@Test(priority = 1, groups = "nightly-build")
	public void showAllMeeting() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "meetings", ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//meeting//getAllMeetings.json"));
		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
	}

	@Owner("Ajendra Singh")
	@Test(priority = 2, groups = "nightly-build")
	public void searchMeetingByID() {

		JsonPath json;
		String meetingID = "";

		json = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int meetingID_int = json.get("id");

		meetingID = String.valueOf(meetingID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("meeting", meetingID);
		String basePath = "meetings/{meeting}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("related_to_type", Matchers.is("candidate"));
		response.then().body("description", Matchers.containsString(json.get("description")));
	}

	@Owner("Harika")
	@Test(priority = 3, groups = "nightly-build")
	public void editMeetingByID() {
		JsonPath json;
		String entitySlug = "";
		String meetingID = "";

		json = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		entitySlug = json.get("slug");

		json = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int meetingID_int = json.get("id");

		meetingID = String.valueOf(meetingID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("meeting", meetingID);
		String basePath = "meetings/{meeting}";

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName + "- Edited");
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type("candidate");
		meeting.setStart_date(pastDate);
		meeting.setEnd_date(startDate);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				meeting);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//meeting//editMeeting.json"));

		// get the response body:
		String responseBody = response.getBody().asString();
		Assert.assertEquals(response.getStatusCode(), 200);
//		
//		// Verify Response using Assertion and Jsonpath
//		JsonPath jp = response.jsonPath();
//
//		meetingID = jp.get("id");
//		// 2295174

		response.then().body("title", Matchers.containsString("Edited"));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is("candidate"));
	}

	@Owner("Ajendra Singh")
	@Test(priority = 4, groups = "nightly-build")
	public void searchMeetingByFields() {
		JsonPath json;
		String meetingID = "";

		json = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int meetingID_int = json.get("id");
		String entitySlug = json.get("related_to");

		meetingID = String.valueOf(meetingID_int);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("related_to", entitySlug);
		queryParameters.put("related_to_type", "candidate");

		String basePath = "meetings/search";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.related_to_type[0]", Matchers.is("candidate"));
	}

	@Owner("Harika")
	@Test(priority = 5, groups = "nightly-build")
	public void deleteMeetingByID() {
		JsonPath json;
		String meetingID = "";

		json = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int meetingID_int = json.get("id");

		meetingID = String.valueOf(meetingID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("meeting", meetingID);
		String basePath = "meetings/{meeting}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), 200);

		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains("Deleted Successfully!"), "Deleted Successfully!");
	}

}
