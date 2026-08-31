package io.rcrm.api.calllogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCallLog;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Call_Log;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditCallLogTest extends TestBase {

	public EditCallLogTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String callLogID = "";
	commanFunction function = new commanFunction();
	JavaFakerJob jobFaker = new JavaFakerJob();
	JavaFakerCallLog fakerakerCallLog = new JavaFakerCallLog();
	String contactNumber = fakerakerCallLog.getContactNumber();
	String callNotes = fakerakerCallLog.getCall_notes();
	String pastDate = fakerakerCallLog.getPastDate();
	int invalidSReminder = jobFaker.qualification_id();
	String longText = jobFaker.getJobDescriptionText() + fakerakerCallLog.notesText();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	int meetingId;
	int accountOwnerid;
	int adminId;
	int team1Id;

	@Owner("Ajendra Singh")
	@Test(dataProvider = "getEntityValidData", groups = "nightly-build")
	public void editCallLogById_POST(String relatedToType, int statusCode) {

		createTeams();

		String userid = String.valueOf(adminId);
		String teamid = String.valueOf(team1Id);

		JsonPath json = function.createNewCallLogWithCollaborators(baseURL, ThreadManager.getAccountApiKey(), relatedToType, userid, teamid)
				.jsonPath();
		int callLogId = json.get("id");
		callLogID = String.valueOf(callLogId);
		String relatedTo = json.get("related_to");
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("callLog", callLogID);
		String basePath = "call-logs/{callLog}";

		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type("CALL_INCOMING");
		callLog.setContact_number(contactNumber);
		callLog.setRelated_to(relatedTo);
		callLog.setRelated_to_type(relatedToType);
		callLog.setCall_started_on(pastDate);
		callLog.setCollaborator_user_ids(String.valueOf(accountOwnerid));
		callLog.setDuration("5h 2m 5s");
		if (relatedToType.equals("contact")) {
			ArrayList<String> associatedCompanies = json.get("associated_companies");
			callLog.setAssociated_companies(associatedCompanies.get(0));
		}
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				callLog);

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();
		teamsAdded.add(team1Id);

		ArrayList<Integer> collaboratorsAdded = new ArrayList<Integer>();
		collaboratorsAdded.add(adminId);
		collaboratorsAdded.add(accountOwnerid);

		// Verify Response Code and body
		response.then().statusCode(statusCode);
		response.then().body("call_notes", Matchers.is(callNotes));
		response.then().body("call_type", Matchers.is("CALL_INCOMING"));
		response.then().body("related_to", Matchers.is(relatedTo));
		response.then().body("related_to_type", Matchers.is(relatedToType));
		response.then().body("collaborator_users", Matchers.containsInAnyOrder(collaboratorsAdded.toArray()));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));
		response.then().body("duration", Matchers.comparesEqualTo(18125));
		
	}

	@Owner("Harika")
	@Test(dataProvider = "getEntityValidData", groups = "nightly-build")
	public void editCallLogByInvalidId404_POST(String relatedToType, int statusCode) {
		JsonPath json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), relatedToType).jsonPath();
		int callLogId = json.get("id");
		callLogID = String.valueOf(callLogId);
		String relatedTo = json.get("related_to");
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("callLog", callLogID + "123459");
		String basePath = "call-logs/{callLog}";
		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type("CALL_INCOMING");
		callLog.setContact_number(contactNumber);
		callLog.setRelated_to(relatedTo);
		callLog.setRelated_to_type(relatedToType);
		callLog.setCall_started_on(pastDate);
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				callLog);


		// Verify Response Code and body
		response.then().statusCode(404);
		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(404));
		response.then().body("errorMessage", Matchers.is("Call Log doesn't exist"));
	}

	@Owner("Ajendra Singh")
	@Test(dataProvider = "getEntityValidData", groups = "nightly-build")
	public void editCallLogByInvalidFieldsValues422_POST(String realtedToType, int statusCode) {
		JsonPath json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), realtedToType).jsonPath();
		int callLogId = json.get("id");
		String callLog_Id = String.valueOf(callLogId);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("callLog", callLog_Id);

		String basePath = "call-logs/{callLog}";

		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(longText);
		callLog.setCall_type("CALL INCOMING");
		callLog.setContact_number(contactNumber);
		callLog.setRelated_to("12345");
		callLog.setCall_started_on("2022-018-14");
		callLog.setRelated_to_type("x" + realtedToType);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				callLog);
		// Get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response Code and body
		response.then().statusCode(422);
		response.then().body("call_type[0]", Matchers.containsString("The selected call type is invalid."));
		response.then().body("call_notes[0]",
				Matchers.containsString("The call notes may not be greater than 10000 characters."));

		response.then().body("call_started_on[0]", Matchers.containsString("The call started on is not a valid date."));
		response.then().body("related_to_type[0]", Matchers.containsString("The selected related to type is invalid."));

		response.then().body("related_to[0]", Matchers.containsString("related to is not valid."));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotEditCallLog() {
		JsonPath json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int CallLogId = json.get("id");
		String entitySlug = json.get("related_to");
		String CallLog_Id = String.valueOf(CallLogId);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("CallLog", CallLog_Id);
		String basePath = "call-logs/{CallLog}";
		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type("CALL_INCOMING");
		callLog.setContact_number(contactNumber);
		callLog.setRelated_to(entitySlug);
		callLog.setRelated_to_type("candidate");
		callLog.setCall_started_on(pastDate);
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"12345", null, pathParamters,
				true, callLog);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider
	public Object[][] getEntityValidData() {

		Object data[][] = { { "candidate", 200 }, { "contact", 200 }, { "company", 200 } };
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

		Response team1 = function.getTeams(baseURL, ThreadManager.getAccountApiKey());
		team1.then().statusCode(200);
		JsonPath teamPath = team1.jsonPath();

		team1Id = teamPath.get("[0].team_id");

	}

}
