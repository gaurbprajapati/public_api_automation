package io.rcrm.api.meeting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
public class EditMeetingTest extends TestBase {

	public EditMeetingTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	int meetingId;
	int accountOwnerid;
	int adminId;
	int team1Id;

	JavaFakerMeeting fakerMeeting = new JavaFakerMeeting();
	String meetingName = fakerMeeting.getMeetingName();
	String meetingDescription = fakerMeeting.getDescription();
	String startDate = fakerMeeting.getFutureDate();
	String endDate = fakerMeeting.getEndDate();
	String address = fakerMeeting.getAddress();
	String pastDate = fakerMeeting.getPastDate();

	@Owner("Harika")
	@Test(dataProvider = "getMeetingValidData", groups = "nightly-build")
	public void editMeetingByID(String realtedToType, int statusCode, String meetingID, String meetingDescription,
			String entitySlug, String companySlug) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("meeting", meetingID);
		String basePath = "meetings/{meeting}";

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName + "- Edited");
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(pastDate);
		meeting.setEnd_date(startDate);
		meeting.setCollaborator_user_ids(String.valueOf(accountOwnerid));
		meeting.setAssociated_companies(companySlug);
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				meeting);

		// get the response body:
		String responseBody = response.getBody().asString();

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();
		teamsAdded.add(team1Id);

		ArrayList<Integer> collaboratorsAdded = new ArrayList<Integer>();
		collaboratorsAdded.add(adminId);
		collaboratorsAdded.add(accountOwnerid);

		if (statusCode == 200) {

			response.then().statusCode(statusCode);
			response.then().body("title", Matchers.containsString("Edited"));
			response.then().body("related_to", Matchers.is(entitySlug));
			response.then().body("related_to_type", Matchers.is(realtedToType));
			response.then().body("collaborator_users", Matchers.containsInAnyOrder(collaboratorsAdded.toArray()));
			response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));
		} else {
			verify422Endpoint(response, statusCode, "Meeting doesn't exist", true);
		}
	}

	@Owner("Harika")
	@Test(dataProvider = "getSingleEntityMeetingData", groups = "nightly-build")
	public void editMeetingCalendarInviteOption(String realtedToType, String meetingID, String meetingDescription,
			String entitySlug) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("meeting", meetingID);
		String basePath = "meetings/{meeting}";

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName + "- Edited");
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(pastDate);
		meeting.setEnd_date(startDate);
		meeting.setDo_not_send_calendar_invites(1);
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				meeting);

		// get the response body:
		String responseBody = response.getBody().asString();

		response.then().statusCode(422);
		response.then().body("errorMessage", Matchers.is("Invitation already sent to attendees and related to"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotEditMeetingByID() {
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

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"x001", null, pathParamters,
				true, meeting);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	public void verify422Endpoint(Response response, int httpStatus, String errorMessage, boolean isTrue) {
		response.then().body("errorMessage", Matchers.is(errorMessage));
		response.then().body("errorCode", Matchers.is(httpStatus));
		response.then().body("error", Matchers.is(isTrue));
	}

	@DataProvider
	public Object[][] getMeetingValidData() {

		createTeams();

		String userid = String.valueOf(adminId);
		String teamid = String.valueOf(team1Id);

		JsonPath jsonCandidateMeeting = function
				.createNewMeetingsWithCollaborators(baseURL, ThreadManager.getAccountApiKey(), "candidate", userid, teamid).jsonPath();
		int candidateMeetingID = jsonCandidateMeeting.get("id");
		String meetingDescription = jsonCandidateMeeting.get("description");
		String candidateSlug = jsonCandidateMeeting.get("related_to");
		String candidateMeetingIDString = String.valueOf(candidateMeetingID);

		JsonPath jsonCompanyMeeting = function
				.createNewMeetingsWithCollaborators(baseURL, ThreadManager.getAccountApiKey(), "company", userid, teamid).jsonPath();
		int companyMeetingID = jsonCompanyMeeting.get("id");
		String companyMeetingIDString = String.valueOf(companyMeetingID);
		String companySlug = jsonCompanyMeeting.get("related_to");
		String companyMeetingDescription = jsonCompanyMeeting.get("description");

		JsonPath jsonContactMeeting = function
				.createNewMeetingsWithCollaborators(baseURL, ThreadManager.getAccountApiKey(), "contact", userid, teamid).jsonPath();
		int contactMeetingID = jsonContactMeeting.get("id");
		String contactMeetingIDString = String.valueOf(contactMeetingID);
		String contactSlug = jsonContactMeeting.get("related_to");
		String contactMeetingDescription = jsonContactMeeting.get("description");

		JsonPath jsonJobMeeting = function
				.createNewMeetingsWithCollaborators(baseURL, ThreadManager.getAccountApiKey(), "job", userid, teamid).jsonPath();
		int jobMeetingID = jsonJobMeeting.get("id");
		String jobMeetingIDString = String.valueOf(jobMeetingID);
		String jobSlug = jsonJobMeeting.get("related_to");
		String jobMeetingDescription = jsonJobMeeting.get("description");

		JsonPath jsonDealMeeting = function
				.createNewMeetingsWithCollaborators(baseURL, ThreadManager.getAccountApiKey(), "deal", userid, teamid).jsonPath();
		int dealMeetingID = jsonDealMeeting.get("id");
		String dealMeetingIDString = String.valueOf(dealMeetingID);
		String dealSlug = jsonDealMeeting.get("related_to");
		String dealMeetingDescription = jsonDealMeeting.get("description");

		Object data[][] = {
				{ "candidate", 200, candidateMeetingIDString, meetingDescription, candidateSlug, companySlug },
				{ "contact", 200, contactMeetingIDString, contactMeetingDescription, contactSlug, companySlug },
				{ "company", 200, companyMeetingIDString, companyMeetingDescription, companySlug, companySlug },
				{ "job", 200, jobMeetingIDString, jobMeetingDescription, jobSlug, companySlug },
				{ "deal", 200, dealMeetingIDString, dealMeetingDescription, dealSlug, companySlug },

				{ "candidate", 404, "9999" + candidateMeetingIDString, meetingDescription, candidateSlug, companySlug },
				{ "contact", 404, "9999" + contactMeetingIDString, contactMeetingDescription, contactSlug,
						companySlug },
				{ "company", 404, "9999" + companyMeetingIDString, companyMeetingDescription, companySlug,
						companySlug },
				{ "job", 404, "9999" + jobMeetingIDString, jobMeetingDescription, jobSlug, companySlug },
				{ "deal", 404, "9999" + dealMeetingIDString, dealMeetingDescription, dealSlug, companySlug } };
		return data;
	}

	@DataProvider
	public Object[][] getSingleEntityMeetingData() {

		JsonPath jsonCandidateMeeting = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int candidateMeetingID = jsonCandidateMeeting.get("id");
		String meetingDescription = jsonCandidateMeeting.get("description");
		String candidateSlug = jsonCandidateMeeting.get("related_to");
		String candidateMeetingIDString = String.valueOf(candidateMeetingID);

		Object data[][] = { { "candidate", candidateMeetingIDString, meetingDescription, candidateSlug } };
		return data;
	}

	public void createTeams() {
		Response response = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
		response.then().statusCode(200);
		JsonPath user = response.jsonPath();
		accountOwnerid = user.get("[0].id");
		adminId = user.get("[1].id");

		Response team = function.getTeams(baseURL, ThreadManager.getAccountApiKey());
		team.then().statusCode(200);
		JsonPath teamJsonPath = team.jsonPath();

		int arraySize = teamJsonPath.getInt("$.size()");
		for (int i = 0; i < arraySize; i++) {
			int teamId = teamJsonPath.get("[" + i + "].team_id");
			allCrudFunctions.deleteTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), teamId);

		}

		ArrayList<String> userId1 = new ArrayList<String>();
		userId1.add(String.valueOf(accountOwnerid));
		userId1.add(String.valueOf(adminId));

		Response response1 = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "meet edit team", userId1);
		response1.then().statusCode(200);

		Response teamPath = function.getTeams(baseURL, ThreadManager.getAccountApiKey());
		teamPath.then().statusCode(200);
		JsonPath teamCreated = teamPath.jsonPath();

		team1Id = teamCreated.get("[0].team_id");

	}

}