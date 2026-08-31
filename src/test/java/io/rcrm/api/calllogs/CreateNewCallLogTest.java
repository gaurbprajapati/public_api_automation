package io.rcrm.api.calllogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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
public class CreateNewCallLogTest extends TestBase {

	public CreateNewCallLogTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	JavaFakerCallLog fakerakerCallLog = new JavaFakerCallLog();
	JavaFakerJob jobFaker = new JavaFakerJob();
	String callNotes = fakerakerCallLog.getCall_notes();
	String startDate = fakerakerCallLog.getPastDate();
	String futureDate = DateUtil.getTomorrowDateString("EEE MMM dd HH:mm:ss zzz yyyy");
	String contactNo = fakerakerCallLog.getContactNumber();
	String longText = jobFaker.getJobDescriptionText() + fakerakerCallLog.notesText();
	Object accountAPIKey;
	Object albatrossTkn;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		accountAPIKey = ThreadManager.getAccountApiKey();
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Harika")
	@Test(dataProvider = "getCallLogValidData", groups = "nightly-build")
	public void createNewCallLogWithFields(String realtedToType, String callNotes, String callType,
										   String CallStartDate, String contactNumber, String callDuration, int responseDuration) {

		String entitySlug = createEntityTestData(realtedToType);
		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		userResponse.then().statusCode(200);
		JsonPath user = userResponse.jsonPath();
		int accountOwnerid = user.get("[0].id");
		int adminId = user.get("[1].id");
		int resTeamMember = user.get("[2].id");

		int team2Id = createTeams("admin", "resTeamMember");
		int team3Id = createTeams("teamMember", "resTeamMember");

		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type(callType);
		callLog.setCall_started_on(CallStartDate);
		callLog.setContact_number(contactNumber);
		callLog.setEnable_auto_populate_teams(1);
		callLog.setCreated_by(accountOwnerid);
		callLog.setUpdated_by(accountOwnerid);
		callLog.setCollaborator_user_ids(String.valueOf(adminId) + "," + String.valueOf(resTeamMember));
		callLog.setCollaborator_team_ids(String.valueOf(team2Id) + "," + String.valueOf(team3Id));

		callLog.setRelated_to(entitySlug);
		callLog.setRelated_to_type(realtedToType);
		callLog.setDuration(callDuration);

		Response response = RestClient.doPost("JSON", baseURL, "call-logs", accountAPIKey, null, true, callLog);
		response.then().statusCode(200);

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();
		teamsAdded.add(team2Id);
		teamsAdded.add(team3Id);

		ArrayList<Integer> collaboratorsAdded = new ArrayList<Integer>();
		collaboratorsAdded.add(adminId);
		collaboratorsAdded.add(resTeamMember);

		response.then().statusCode(200);
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		response.then().body("collaborator_users", Matchers.containsInAnyOrder(collaboratorsAdded.toArray()));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));
		response.then().body("duration", Matchers.is(responseDuration));
	}

	@Owner("Ajendra Singh")
	@Test(dataProvider = "getCallDurationInvalidData", groups = "nightly-build")
	public void verifyValidationOnDurationFieldPost_422(String callDuration, String errorMessage) {

		String entitySlug = createEntityTestData("candidate");
		createTeams("accountOwner", "teamMember");
		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type("CALL_INCOMING");
		callLog.setCall_started_on(startDate);
		callLog.setContact_number(contactNo);
		callLog.setRelated_to(entitySlug);
		callLog.setRelated_to_type("candidate");
		callLog.setDuration(callDuration);

		Response response = RestClient.doPost("JSON", baseURL, "call-logs", accountAPIKey, null,
				true, callLog);

		response.then().statusCode(422).body("errorMessage", Matchers.is(errorMessage));
	}

	@Owner("Harika")
	@Test(dataProvider = "getAutoPopulateTeamsValue", groups = "nightly-build")
	public void createCallLogWithEnableDisableAutoTeamsPopulation(int value) {
		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		userResponse.then().statusCode(200);
		JsonPath user = userResponse.jsonPath();
		int adminId = user.get("[1].id");

		String entitySlug = createEntityTestData("candidate");

		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type("CALL_OUTGOING");
		callLog.setCall_started_on(startDate);
		callLog.setContact_number("9090909090");
		callLog.setEnable_auto_populate_teams(value);
		callLog.setCreated_by(adminId);
		callLog.setRelated_to(entitySlug);
		callLog.setRelated_to_type("candidate");

		int teamId = createTeams("admin", "resTeamMember");

		Response response = RestClient.doPost("JSON", baseURL, "call-logs", accountAPIKey, null, true, callLog);

		response.then().statusCode(200);

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();
		ArrayList<Integer> usersAdded = new ArrayList<Integer>();

		response.then().statusCode(200);
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is("candidate"));
		if(value == 1) {
			response.then().body("collaborator_users", Matchers.containsInAnyOrder(usersAdded.toArray()));
			response.then().body("collaborator_teams", Matchers.hasItem(teamId));
		}else{
			response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));
		}
	}

	@Owner("Harika")
	@Test(dataProvider = "getInvalidCollaboratorsData", groups = "nightly-build")
	public void createCallLogWithInvalidCollaborators(String userIds , String teamIds, int accountOwnerid, String type) {
		String entitySlug = createEntityTestData("candidate");

		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type("CALL_OUTGOING");
		callLog.setCall_started_on(startDate);
		callLog.setContact_number("9090909090");
		callLog.setEnable_auto_populate_teams(1);
		callLog.setCreated_by(accountOwnerid);
		callLog.setUpdated_by(accountOwnerid);
		callLog.setCollaborator_user_ids(userIds);
		callLog.setCollaborator_team_ids(teamIds);
		callLog.setRelated_to(entitySlug);
		callLog.setRelated_to_type("candidate");

		Response response = RestClient.doPost("JSON", baseURL, "call-logs", accountAPIKey, null, true, callLog);
		response.then().statusCode(422);
		if(type.equals("user"))
			response.then().body("collaborator_user_ids[0]", Matchers.is("collaborators ids are not valid"));
		else
			response.then().body("collaborator_team_ids[0]", Matchers.is("Team ids are not valid"));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void userShouldNotBeAbleToCreateCallLogWithInvalidData() {
		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type("CALL_INCOMING");
		callLog.setCall_started_on(futureDate);
		callLog.setContact_number(contactNo);
		callLog.setRelated_to("12345");
		callLog.setRelated_to_type("x" + "candidate");
		Response response = RestClient.doPost("JSON", baseURL, "call-logs", accountAPIKey, null, true, callLog);

		Assert.assertEquals(response.getStatusCode(), 422);
		if (response != null) {
			response.then().body("call_started_on[0]", Matchers.is("The call started on must be a date before tomorrow."));
			response.then().body("related_to[0]", Matchers.is("related to is not valid."));
			response.then().body("related_to_type[0]", Matchers.is("The selected related to type is invalid."));
		}
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createNewCallLogWithMandatoryValue() {

		Call_Log callLog = new Call_Log();
		Response response = RestClient.doPost("JSON", baseURL, "call-logs", accountAPIKey, null, true, callLog);

		response.then().statusCode(422);
		response.then().body("call_notes[0]", Matchers.containsString("The call notes must be at least 2 characters."));
		response.then().body("call_type[0]", Matchers.containsString("The call type field is required."));
		response.then().body("call_started_on[0]", Matchers.is("The call started on field is required."));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createNewCallLogWithLimitExceeds() {
		JsonPath json = function.createNewCallLog(baseURL, accountAPIKey, "candidate").jsonPath();
		int callLogId = json.get("id");
		String relatedTo = json.get("related_to");
		String callLog_Id = String.valueOf(callLogId);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("callLog", callLog_Id);

		String basePath = "call-logs/{callLog}";
		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(longText);
		callLog.setContact_number(longText);
		callLog.setCall_type("CALL_INCOMING");
		callLog.setCall_started_on(startDate);
		callLog.setRelated_to(relatedTo);
		callLog.setRelated_to_type("candidate");
		Response response = RestClient.doPost1("JSON", baseURL, basePath, accountAPIKey, null, pathParamters, true,
				callLog);

        // Verify Response Code and body
		response.then().statusCode(422);
		response.then().body("call_notes[0]",
				Matchers.containsString("The call notes may not be greater than 10000 characters."));
		response.then().body("contact_number[0]",
				Matchers.containsString("The contact number may not be greater than 50 characters."));

	}

	@Owner("Harika")
	@Test(dataProvider = "getDataForAssociates", groups = "nightly-build")
	public void createnewCallLogWithAssociates(String realtedToType, String callNotes, String callType,
											   String CallStartDate, String contactNumber, String entitySlug, String statusCode,
											   String associatedCandidatesSlugs, String associatedCompaniesSlugs, String associatedContactsSlugs,
											   String associatedJobsSlugs, String associatedDealsSlugs) {

		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type(callType);
		callLog.setCall_started_on(CallStartDate);
		callLog.setContact_number(contactNumber);
		callLog.setRelated_to(entitySlug);
		callLog.setRelated_to_type(realtedToType);
		callLog.setAssociated_candidates(associatedCandidatesSlugs);
		callLog.setAssociated_contacts(associatedContactsSlugs);
		callLog.setAssociated_companies(associatedCompaniesSlugs);
		callLog.setAssociated_deals(associatedDealsSlugs);
		callLog.setAssociated_jobs(associatedJobsSlugs);

		Response response = RestClient.doPost("JSON", baseURL, "call-logs", ThreadManager.getAccountApiKey(), null, true, callLog);
		if (statusCode == "200") {
			response.then().statusCode(200);
			response.then().body("related_to", Matchers.is(entitySlug));
		} else if (statusCode == "422") {
			if (associatedCandidatesSlugs != "")
				response.then().body("associated_candidates[0]",
						Matchers.containsString("Invalid associated candidates"));
			else
				response.then().body("associated_contacts[0]", Matchers.containsString("Invalid associated contacts"));
		}

	}

	@Owner("Harika")
	@Test(dataProvider = "getCallLogsValidData", groups = "nightly-build")
	public void unauthorizedUserCannotCreateNewCallLog(String realtedToType, int statusCode) {

		JsonPath json = function.createNewCallLog(baseURL, accountAPIKey, realtedToType).jsonPath();
		String relatedTo = json.get("related_to");
		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type("CALL_INCOMING");
		callLog.setCall_started_on(futureDate);
		callLog.setContact_number(contactNo);
		callLog.setRelated_to(relatedTo);
		callLog.setRelated_to_type(realtedToType);

		Response response = RestClient.doPost("JSON", baseURL, "call-logs", accountAPIKey+ "12345", null, true, callLog);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider
	public Object[][] getCallLogValidData() {
		Object data[][] = { { "candidate", callNotes, "CALL_INCOMING", startDate, contactNo, "23:59:59", 86399 },
				{ "contact", callNotes, "CALL_OUTGOING", startDate, contactNo, "1hr 2min 5sec", 3725 },
				{ "contact", callNotes, "CALL_OUTGOING", startDate, contactNo, "7:30:31", 27031 },
				{ "contact", callNotes, "CALL_OUTGOING", startDate, contactNo, "4293", 4293 },
				{ "company", callNotes, "CALL_OUTGOING", startDate, contactNo, "1hour 2minute 5second", 3725 },
				{ "company", callNotes, "CALL_OUTGOING", startDate, contactNo, "0:0:0", 0 },
				{ "candidate", callNotes, "CALL_OUTGOING", startDate, contactNo, "1h5min", 3900 },
				{ "candidate", callNotes, "CALL_OUTGOING", startDate, contactNo, "2hours5min9s", 7509 },
				{ "candidate", callNotes, "CALL_OUTGOING", startDate, contactNo, "120min", 7200 } };
		return data;
	}

	@DataProvider(parallel = true)
	public Object[][] getCallDurationInvalidData() {
		Object data[][] = { { "ABCD", "Please provide a valid duration" },
				{ "-r6756", "Please provide a valid duration" }, { "-123", "Duration components cannot be negative." },
				{ "86500", "Duration cannot be more than 24 hours." },
				{ "2h 60m 30s", "Please provide a valid duration" },
				{ "2h 50m 80s", "Please provide a valid duration" },
				{ "24:00:00", "Duration cannot be more than 24 hours." },
				{ "1h75min", "Please provide a valid duration" } };
		return data;
	}

	@DataProvider(parallel = true)
	public Object[][] getCallLogsValidData() {
		Object data[][] = { { "candidate", 200 }, { "contact", 200 } ,{ "company", 200 }};
		return data;
	}

	@DataProvider(parallel = true)
	public Object[][] getInvalidCollaboratorsData() {

		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		userResponse.then().statusCode(200);

		JsonPath user = userResponse.jsonPath();
		int accountOwnerId = user.getInt("[0].id");
		int adminId = user.getInt("[1].id");
		int resTeamMemberId = user.getInt("[2].id");

		int team2Id = createTeams("admin", "resTeamMember");
		int team3Id = createTeams("teamMember", "resTeamMember");

		String userIdsInvalid = "1234," + adminId;
		String teamIds = team2Id + "," + team3Id;

		String userIds = adminId + "," + resTeamMemberId;
		String teamIdsInvalid = "1234," + team3Id;

		Object[][] data = {
				{ userIdsInvalid, teamIds, accountOwnerId, "user" },
				{ userIds, teamIdsInvalid, accountOwnerId, "team" }
		};
		return data;
	}

	@DataProvider
	public Object[][] getDataForAssociates() {

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");

		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String companySlug = jsonCompany.get("slug");

		JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");

		String associatedCandidatesSlugs = "";
		String associatedCompaniesSlugs = "";
		String associatedContactsSlugs = "";
		String associatedJobsSlugs = "";
		String associatedDealsSlugs = "";

		JsonPath jsonGetAllCompanies = function.getAllCompanies_GET(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		List<String> companyslugs = jsonGetAllCompanies.get("data.slug");
		associatedCompaniesSlugs = StringUtils.join(companyslugs, ",");

		JsonPath jsonGetAllCandidates = function.getAllCandidates(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		List<String> candidateslugs = jsonGetAllCandidates.get("data.slug");
		associatedCandidatesSlugs = StringUtils.join(candidateslugs, ",");

		JsonPath jsonGetAllContacts = function.getAllContacts_GET(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		List<String> contactslugs = jsonGetAllContacts.get("data.slug");
		associatedContactsSlugs = StringUtils.join(contactslugs, ",");

		JsonPath jsonGetAllJobs = function.getAllJobs_GET(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		List<String> jobsSlugs = jsonGetAllJobs.get("data.slug");
		associatedJobsSlugs = StringUtils.join(jobsSlugs, ",");

		JsonPath jsonGetAllDeals = function.getAllDealsGET(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		List<String> DealSlugs = jsonGetAllDeals.get("data.slug");
		associatedDealsSlugs = (DealSlugs != null && !DealSlugs.isEmpty()) ? StringUtils.join(DealSlugs, ",") : "";

		Object data[][] = {

				{ "candidate", callNotes, "CALL_INCOMING", startDate, contactNo, candidateEntitySlug, "200", "",
						associatedCompaniesSlugs, associatedContactsSlugs, associatedJobsSlugs, associatedDealsSlugs },
				{ "candidate", callNotes, "CALL_INCOMING", startDate, contactNo, candidateEntitySlug, "200", "",
						associatedCompaniesSlugs, associatedContactsSlugs, associatedJobsSlugs, "" },
				{ "candidate", callNotes, "CALL_INCOMING", startDate, contactNo, candidateEntitySlug, "200", "",
						associatedCompaniesSlugs, associatedContactsSlugs, "", "" },
				{ "candidate", callNotes, "CALL_INCOMING", startDate, contactNo, candidateEntitySlug, "200", "",
						associatedCompaniesSlugs, "", "", "" },
				{ "candidate", callNotes, "CALL_INCOMING", startDate, contactNo, candidateEntitySlug, "200", "", "", "",
						"", "" },

				{ "candidate", callNotes, "CALL_INCOMING", startDate, contactNo, candidateEntitySlug, "200", "", "",
						associatedContactsSlugs, associatedJobsSlugs, associatedDealsSlugs },
				{ "candidate", callNotes, "CALL_INCOMING", startDate, contactNo, candidateEntitySlug, "200", "", "", "",
						associatedJobsSlugs, associatedDealsSlugs },
				{ "candidate", callNotes, "CALL_INCOMING", startDate, contactNo, candidateEntitySlug, "200", "", "", "",
						"", associatedDealsSlugs },
				{ "candidate", callNotes, "CALL_INCOMING", startDate, contactNo, candidateEntitySlug, "200", "",
						associatedCompaniesSlugs, "", associatedJobsSlugs, associatedDealsSlugs },
				{ "candidate", callNotes, "CALL_INCOMING", startDate, contactNo, candidateEntitySlug, "200", "",
						associatedCompaniesSlugs, "", "", associatedDealsSlugs },

				{ "contact", callNotes, "CALL_INCOMING", startDate, contactNo, contactSlug, "200",
						associatedCandidatesSlugs, associatedCompaniesSlugs, "", associatedJobsSlugs,
						associatedDealsSlugs },
				{ "contact", callNotes, "CALL_INCOMING", startDate, contactNo, contactSlug, "200", "",
						associatedCompaniesSlugs, "", associatedJobsSlugs, associatedDealsSlugs },
				{ "contact", callNotes, "CALL_INCOMING", startDate, contactNo, contactSlug, "200", "", "", "",
						associatedJobsSlugs, associatedDealsSlugs },
				{ "contact", callNotes, "CALL_INCOMING", startDate, contactNo, contactSlug, "200", "", "", "", "",
						associatedDealsSlugs },
				{ "contact", callNotes, "CALL_INCOMING", startDate, contactNo, contactSlug, "200", "", "", "", "", "" },
				{ "contact", callNotes, "CALL_INCOMING", startDate, contactNo, contactSlug, "200",
						associatedCandidatesSlugs, associatedCompaniesSlugs, "", "", associatedDealsSlugs },
				{ "contact", callNotes, "CALL_INCOMING", startDate, contactNo, contactSlug, "200",
						associatedCandidatesSlugs, associatedCompaniesSlugs, "", associatedJobsSlugs, "" },

				{ "candidate", callNotes, "CALL_INCOMING", startDate, contactNo, candidateEntitySlug, "422",
						associatedContactsSlugs + "1", "", "", "", "" },
				{ "candidate", callNotes, "CALL_INCOMING", startDate, contactNo, candidateEntitySlug, "422", "", "",
						associatedContactsSlugs + "x1", "", "" },
				{ "company", callNotes, "CALL_INCOMING", startDate, contactNo, companySlug, "200",
						associatedCandidatesSlugs,"",associatedContactsSlugs , associatedJobsSlugs,
						associatedDealsSlugs }
		};
		return data;
	}

	public int createTeams(String userRole1, String userRole2) {
		Response response = function.getUsers(baseURL, accountAPIKey);
		response.then().statusCode(200);
		JsonPath user = response.jsonPath();
		HashMap<String, Integer> userIdMap = new HashMap<String, Integer>();
		userIdMap.put("accountOwner", user.get("[0].id"));
		userIdMap.put("admin", user.get("[1].id"));
		userIdMap.put("resTeamMember", user.get("[2].id"));
		userIdMap.put("teamMember", user.get("[3].id"));

		String team1 = "team"+ RandomStringUtils.randomAlphabetic(5);

		ArrayList<String> userId1 = new ArrayList<String>();
		userId1.add(String.valueOf(userIdMap.get(userRole1)));
		userId1.add(String.valueOf(userIdMap.get(userRole2)));

		Response response1 = allCrudFunctions.createTeam(albatrossURL, albatrossTkn, team1, userId1);
		response1.then().statusCode(200);

		Response teams = function.getTeams(baseURL, accountAPIKey);
		teams.then().statusCode(200);
		JsonPath teamPath = teams.jsonPath();

		int noOfTeams = teamPath.getInt("$.size()");
		int teamId = 0;
		for (int i = 0; i < noOfTeams; i++) {
			String teamName = teamPath.get("[" + i + "].team_name");
			if (teamName.equals(team1)) {
				teamId= teamPath.get("[" + i + "].team_id");
				break;
			}
		}
		return teamId;
	}

	public String createEntityTestData(String relatedToType) {
		JsonPath json;
		String entitySlug="";
		if (relatedToType == "candidate") {
			json = function.createNewCandidateWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
			entitySlug = json.get("slug");
		}

		if (relatedToType == "contact") {
			JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
			String companySlug = jsonCompany.get("slug");
			json = function.createNewContact_POST(baseURL, accountAPIKey, companySlug).jsonPath();

			entitySlug = json.get("slug");
		}

		if (relatedToType == "company") {
			JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			entitySlug = jsonCompany.get("slug");
		}
		return entitySlug;
	}

	@DataProvider(parallel = true)
	public Object[][] getAutoPopulateTeamsValue() {
		Object data[][] = { { 1 }, { 0 }};
		return data;
	}

}

