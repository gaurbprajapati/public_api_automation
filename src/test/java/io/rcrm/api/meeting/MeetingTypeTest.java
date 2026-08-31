package io.rcrm.api.meeting;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerMeeting;
import io.rcrm.api.pojo.Meeting;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class MeetingTypeTest extends TestBase {
	
	String slug = "";
	commanFunction function = new commanFunction();
	int meetingId;
	int meetingTypeId;

	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	JavaFakerMeeting fakerMeeting = new JavaFakerMeeting();
	String meetingName = fakerMeeting.getMeetingName();
	String meetingDescription = fakerMeeting.getDescription();
	String startDate = fakerMeeting.getDelayedFutureDate(15, TimeUnit.MINUTES);
	String endDate = fakerMeeting.getEndDateWithReferenceDate(startDate);
	String address = fakerMeeting.getAddress();
	String pastDate = fakerMeeting.getPastDate();

	@Owner("Harika")
	@Test(priority = 0, groups = "nightly-build")
	public void getMeetingTypes() {
		allCrudFunctions.createCustomMeeting(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
		Response response = RestClient.doGet("JSON", baseURL, "meeting-types", ThreadManager.getAccountApiKey(), null,null, true);
		

		response.then().statusCode(200);
		
		JsonPath jp = response.jsonPath();

		meetingTypeId = jp.get("[0].id");

	}

	@Owner("Harika")
	@Test(priority = 1, groups = "nightly-build")
	public void createMeetingWithMeetingType() {

		JsonPath json;

		json = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		slug = json.get("slug");

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);
		meeting.setReminder(15);
		meeting.setRelated_to(slug);
		meeting.setRelated_to_type("candidate");
		meeting.setStart_date(startDate);
		meeting.setEnd_date(endDate);
		meeting.setMeeting_type_id(meetingTypeId);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", ThreadManager.getAccountApiKey(), null, true, meeting);

		String responseBody = response.getBody().asString();

		JsonPath jp = response.jsonPath();

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("meeting_type.id", Matchers.equalTo(meetingTypeId));

		meetingId = jp.get("id");

	}

	@Owner("Harika")
	@Test(priority = 2, groups = "nightly-build")
	public void verifyMeetingTypeInShowAllMeeting() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "meetings", ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		
		String responseBody = response.getBody().asString();
		
		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("meeting_type"));
	}

	@Owner("Harika")
	@Test(priority = 3, groups = "nightly-build")
	public void verifyMeetingTypeInSearchMeetingByID() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("meeting", String.valueOf(meetingId));
		String basePath = "meetings/{meeting}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);
 
		response.then().body("meeting_type.id", Matchers.equalTo(meetingTypeId));
		
	}

	@Owner("Harika")
	@Test(priority = 4, groups = "nightly-build")
	public void editMeetingType() {
		
        Response getMeetingtypeResponse = RestClient.doGet("JSON", baseURL, "meeting-types", ThreadManager.getAccountApiKey(), null,null, true);

        getMeetingtypeResponse.then().statusCode(200);
		
		JsonPath jp = getMeetingtypeResponse.jsonPath();

		int meetingTypeId = jp.get("[1].id");

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("meeting", String.valueOf(meetingId));
		String basePath = "meetings/{meeting}";

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName + "- Edited");
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);
		meeting.setRelated_to(slug);
		meeting.setRelated_to_type("candidate");
		meeting.setStart_date(startDate);
		meeting.setEnd_date(endDate);
		
        meeting.setMeeting_type_id(meetingTypeId);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				meeting);
		Assert.assertEquals(response.getStatusCode(), 200);

		
		response.then().body("id", Matchers.notNullValue());
		response.then().body("meeting_type.id", Matchers.equalTo(meetingTypeId));
	}
	
	@Owner("Harika")
	@Test(priority = 5, groups = "nightly-build")
	public void createMeetingWithInvalidMeetingType() {

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
		meeting.setStart_date(startDate);
		meeting.setEnd_date(endDate);
		meeting.setMeeting_type_id(123);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", ThreadManager.getAccountApiKey(), null, true, meeting);
		response.then().statusCode(422);
		
		response.then().body("meeting_type_id[0]", Matchers.equalTo("Invalid meeting type id"));

	}


}
