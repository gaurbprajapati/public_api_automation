package io.rcrm.api.meeting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

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
import io.rcrm.api.javafaker.JavaFakerMeeting;
import io.rcrm.api.pojo.Meeting;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateNewMeetingTest extends TestBase {

	public CreateNewMeetingTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	String accountAPIKey ="";
	String albatrossToken="";
	@BeforeClass(alwaysRun = true)	public void getAccountAPI() {
		accountAPIKey = ThreadManager.getAccountApiKey();
		albatrossToken= ThreadManager.getOwnerAlbatrossToken();
	}
	
	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	int meetingId;
	
	JavaFakerMeeting fakerMeeting = new JavaFakerMeeting();
	String meetingName = fakerMeeting.getMeetingName();
	String meetingDescription = fakerMeeting.getDescription();
	String meetingDescriptionSize = fakerMeeting.getDescription(5001);
	String startDate = fakerMeeting.getFutureDate();
	String endDate = fakerMeeting.getEndDateWithReferenceDate(startDate);
	String address = fakerMeeting.getAddress();

	@Owner("Harika")
	@Test(dataProvider = "getMeetingValidData", groups = "nightly-build")
	public void createNewMeetings(String realtedToType, int statusCode, String entitySlug) {
		int teamId = 0;
		teamId = createTeams("accountOwner", "teamMember");
		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(startDate);
		meeting.setEnd_date(endDate);
		meeting.setDo_not_send_calendar_invites(1);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", accountAPIKey, null, true,meeting);

		JsonPath jp = response.jsonPath();

		meetingId = jp.get("id");

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("title", Matchers.containsString(meetingName));
		response.then().body("address", Matchers.containsString(address));
		response.then().body("description", Matchers.containsString(meetingDescription));
		response.then().body("do_not_send_calendar_invites", Matchers.is(1));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		response.then().body("collaborator_teams", Matchers.hasItem(teamId));
	}

	@Owner("Harika")
	@Test(dataProvider = "getSingleEntityData", groups = "nightly-build")
	public void createMeetingsWithDisableAutoTeamsPopulation(String realtedToType, int statusCode, String entitySlug) {

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(startDate);
		meeting.setEnd_date(endDate);
		meeting.setEnable_auto_populate_teams(0);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", accountAPIKey, null, true, meeting);

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		response.then().body("collaborator_users", Matchers.containsInAnyOrder(teamsAdded.toArray()));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));
	}

	@Owner("Harika")
	@Test(dataProvider = "getSingleEntityData", groups = "nightly-build")
	public void createMeetingsWithEnableAutoTeamsPopulation(String realtedToType, int statusCode, String entitySlug) {

		int teamId = createTeams("admin","resTeamMember");
		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		userResponse.then().statusCode(200);
		JsonPath user = userResponse.jsonPath();
		int accountOwnerid = user.get("[0].id");
		int adminId = user.get("[1].id");

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(startDate);
		meeting.setEnd_date(endDate);
		meeting.setEnable_auto_populate_teams(1);
		meeting.setOwner_id(adminId);
		meeting.setCreated_by(accountOwnerid);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", accountAPIKey, null, true, meeting);

//		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();
//		teamsAdded.add(team2Id);

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());

		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		response.then().body("collaborator_teams", Matchers.hasItem(teamId));
	}

	@Owner("Harika")
	@Test(dataProvider = "getSingleEntityData", groups = "nightly-build")
	public void createMeetingsWithCollaborators(String realtedToType, int statusCode, String entitySlug) {
		int team1Id = createTeams("admin", "resTeamMember");
		int team2Id = createTeams("teamMember", "resTeamMember");

		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		userResponse.then().statusCode(200);
		JsonPath user = userResponse.jsonPath();
		int accountOwnerid = user.get("[0].id");
		int adminId = user.get("[1].id");
		int resTeamMember = user.get("[2].id");

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(startDate);
		meeting.setEnd_date(endDate);
		meeting.setOwner_id(accountOwnerid);
		meeting.setCreated_by(accountOwnerid);
		meeting.setUpdated_by(accountOwnerid);
		meeting.setCollaborator_user_ids(String.valueOf(adminId) + "," + String.valueOf(resTeamMember));
		meeting.setCollaborator_team_ids(String.valueOf(team1Id) + "," + String.valueOf(team2Id));
		meeting.setEnable_auto_populate_teams(1);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", accountAPIKey, null, true, meeting);

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();
		teamsAdded.add(team1Id);
		teamsAdded.add(team2Id);

		ArrayList<Integer> collaboratorsAdded = new ArrayList<Integer>();
		collaboratorsAdded.add(adminId);
		collaboratorsAdded.add(resTeamMember);

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("collaborator_users", Matchers.containsInAnyOrder(collaboratorsAdded.toArray()));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));
	}

	@Owner("Harika")
	@Test(dataProvider = "getMeetingValidData", groups = "nightly-build")
	public void createMeetingsWithInvalidCollaborators(String realtedToType, int statusCode, String entitySlug) {
		int team1Id = createTeams("admin", "resTeamMember");
		int team2Id = createTeams("teamMember", "resTeamMember");

		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		userResponse.then().statusCode(200);
		JsonPath user = userResponse.jsonPath();
		int accountOwnerid = user.get("[0].id");
		int adminId = user.get("[1].id");
		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(startDate);
		meeting.setEnd_date(endDate);
		meeting.setOwner_id(accountOwnerid);
		meeting.setCreated_by(accountOwnerid);
		meeting.setUpdated_by(accountOwnerid);
		meeting.setCollaborator_user_ids("1234," + String.valueOf(adminId));
		meeting.setCollaborator_team_ids(String.valueOf(team1Id) + "," + String.valueOf(team2Id));
		meeting.setEnable_auto_populate_teams(1);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", accountAPIKey, null, true, meeting);

		response.then().statusCode(422);
		response.then().body("collaborator_user_ids[0]", Matchers.is("collaborators ids are not valid"));
	}

	@Owner("Harika")
	@Test(dataProvider = "getMeetingValidData", groups = "nightly-build")
	public void createMeetingsWithInvalidCollaboratorsTeams(String realtedToType, int statusCode, String entitySlug) {
		int teamId = createTeams("teamMember", "resTeamMember");

		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		userResponse.then().statusCode(200);
		JsonPath user = userResponse.jsonPath();
		int accountOwnerid = user.get("[0].id");
		int adminId = user.get("[1].id");

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(startDate);
		meeting.setEnd_date(endDate);
		meeting.setOwner_id(accountOwnerid);
		meeting.setCreated_by(accountOwnerid);
		meeting.setUpdated_by(accountOwnerid);
		meeting.setCollaborator_user_ids(String.valueOf(adminId));
		meeting.setCollaborator_team_ids("1234," + String.valueOf(teamId));
		meeting.setEnable_auto_populate_teams(1);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", accountAPIKey, null, true, meeting);

		response.then().statusCode(422);
		response.then().body("collaborator_team_ids[0]", Matchers.is("Team ids are not valid"));
	}

	@Owner("Harika")
	@Test(dataProvider = "getSingleEntityData", groups = "nightly-build")
	public void createMeetingsWithInvalidCalendarInviteValue(String realtedToType, int statusCode, String entitySlug) {

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(startDate);
		meeting.setEnd_date(endDate);
		meeting.setEnable_auto_populate_teams(0);
		meeting.setDo_not_send_calendar_invites(2);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", accountAPIKey, null, true, meeting);

		response.then().statusCode(422);
		response.then().body("do_not_send_calendar_invites[0]", Matchers.is("The selected do not send calendar invites is invalid."));
	}

	@Owner("Harika")
	@Test(dataProvider = "getMeetingInValidData", groups = "nightly-build")
	public void userShouldNotBeAbleToCreateNewMeetingWithEmptyFieldsData(String realtedToType, int statusCode,
			String entitySlug) {

		Meeting meeting = new Meeting();
		meeting.setTitle("");
		meeting.setDescription("");
		meeting.setAddress("");

		meeting.setReminder(1);
		meeting.setRelated_to("");
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date("");
		meeting.setEnd_date("");

		Response response = RestClient.doPost("JSON", baseURL, "meetings", accountAPIKey, null, true, meeting);

		response.then().statusCode(statusCode);
		response.then().body("title[0]", Matchers.containsString("The title field is required."));
		response.then().body("start_date[0]", Matchers.containsString("The start date field is required."));
		response.then().body("end_date[0]", Matchers.containsString("The end date field is required."));
		response.then().body("reminder[0]", Matchers.containsString("The selected reminder is invalid."));

	}

	@Owner("Harika")
	@Test(dataProvider = "getsDataForAssociates", groups = "nightly-build")
	public void createNewMeetingsWithAssociates(String realtedToType, String entitySlug, String statusCode,
			String associatedCandidatesSlugs, String associatedCompaniesSlugs, String associatedContactsSlugs,
			String associatedJobsSlugs, String associatedDealsSlugs) {

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(startDate);
		meeting.setEnd_date(endDate);

		meeting.setAssociated_candidates(associatedCandidatesSlugs);
		meeting.setAssociated_companies(associatedCompaniesSlugs);
		meeting.setAssociated_contacts(associatedContactsSlugs);
		meeting.setAssociated_jobs(associatedJobsSlugs);
		meeting.setAssociated_deals(associatedDealsSlugs);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", accountAPIKey, null, true, meeting);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		if (statusCode == "200") {

			response.then().statusCode(200);
			response.then().body("id", Matchers.notNullValue());
			response.then().body("title", Matchers.containsString(meetingName));
			response.then().body("address", Matchers.containsString(address));
			response.then().body("description", Matchers.containsString(meetingDescription));
			response.then().body("related_to", Matchers.is(entitySlug));
			response.then().body("related_to_type", Matchers.is(realtedToType));

		} else if (statusCode == "422") {

			response.then().body("associated_companies[0]", Matchers.containsString("Invalid associated companies"));
			response.then().body("associated_contacts[0]", Matchers.containsString("Invalid associated contacts"));
			response.then().body("associated_jobs[0]", Matchers.containsString("Invalid associated jobs"));
			response.then().body("associated_deals[0]", Matchers.containsString("nvalid associated deals"));
		}

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void verifyMeetingDescriptionWithMaxChars() {
		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");
		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescriptionSize);
		meeting.setAddress(address);
		meeting.setReminder(15);
		meeting.setRelated_to_type("candidate");
		meeting.setRelated_to(candidateEntitySlug);
		meeting.setStart_date(startDate);
		meeting.setEnd_date(endDate);
		meeting.setDo_not_send_calendar_invites(1);
		Response response = RestClient.doPost("JSON", baseURL, "meetings", accountAPIKey, null, true, meeting);
		Assert.assertEquals(response.getStatusCode(), 422);
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(jp.getString("description[0]"), "The description may not be greater than 5000 characters.");
	}

	@DataProvider (parallel = true)
	public Object[][] getMeetingValidData() {

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");

		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String companySlug = jsonCompany.get("slug");

		JsonPath jsonContact = function.createNewContact_POST(baseURL, accountAPIKey, companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");

		JsonPath jsonJob = function.createNewJob(baseURL, accountAPIKey, companySlug, contactSlug).jsonPath();
		String jobSlug = jsonJob.getString("slug");

		JsonPath jsonDeal = function
				.createNewDealWithMandatoryFields(baseURL, accountAPIKey, companySlug, contactSlug, jobSlug).jsonPath();
		String deaslEntitySlug = jsonDeal.get("slug");

		Object data[][] = { { "candidate", 200 ,candidateEntitySlug }, { "contact", 200,contactSlug }, { "company", 200,companySlug }, { "job", 200 ,jobSlug },
				{ "deal", 200 ,deaslEntitySlug } };
		return data;
	}

	@DataProvider
	public Object[][] getSingleEntityData() {

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");

		Object data[][] = { { "candidate", 200, candidateEntitySlug } };
		return data;
	}

	@DataProvider
	public Object[][] getMeetingInValidData() {

		Object data[][] = { { "candidate", 422, "" }, { "contact", 422, "" }, { "company", 422, "" },
				{ "job", 422, "" }, { "deal", 422, "" } };
		return data;
	}

	@DataProvider (parallel = true)
	public Object[][] getsDataForAssociates() {

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");

		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String companySlug = jsonCompany.get("slug");

		JsonPath jsonContact = function.createNewContact_POST(baseURL, accountAPIKey, companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");

		JsonPath jsonJob = function.createNewJob(baseURL, accountAPIKey, companySlug, contactSlug).jsonPath();
		String jobSlug = jsonJob.getString("slug");

		JsonPath jsonDeal = function
				.createNewDealWithMandatoryFields(baseURL, accountAPIKey, companySlug, contactSlug, jobSlug).jsonPath();

		String deaslEntitySlug = jsonDeal.get("slug");

		ExecutorService executorService = Executors.newFixedThreadPool(5);
		ConcurrentHashMap<String, String> results = new ConcurrentHashMap<>();

		executorService.submit(() -> {
			JsonPath jsonGetAllCompanies = function.getAllCompanies_GET(baseURL, accountAPIKey).jsonPath();
			List<String> companyslugs = jsonGetAllCompanies.get("data.slug");
			results.put("associatedCompaniesSlugs", StringUtils.join(companyslugs, ","));
		});

		executorService.submit(() -> {
			JsonPath jsonGetAllCandidates = function.getAllCandidates(baseURL, accountAPIKey).jsonPath();
			List<String> candidateslugs = jsonGetAllCandidates.get("data.slug");
			results.put("associatedCandidatesSlugs", StringUtils.join(candidateslugs, ","));
		});

		executorService.submit(() -> {
			JsonPath jsonGetAllContacts = function.getAllContacts_GET(baseURL, accountAPIKey).jsonPath();
			List<String> contactslugs = jsonGetAllContacts.get("data.slug");
			results.put("associatedContactsSlugs", StringUtils.join(contactslugs, ","));
		});

		executorService.submit(() -> {
			JsonPath jsonGetAllJobs = function.getAllJobs_GET(baseURL, accountAPIKey).jsonPath();
			List<String> jobsSlugs = jsonGetAllJobs.get("data.slug");
			results.put("associatedJobsSlugs", StringUtils.join(jobsSlugs, ","));
		});

		executorService.submit(() -> {
			JsonPath jsonGetAllDeals = function.getAllDealsGET(baseURL, accountAPIKey).jsonPath();
			List<String> DealSlugs = jsonGetAllDeals.get("data.slug");
			results.put("associatedDealsSlugs", StringUtils.join(DealSlugs, ","));
		});

		executorService.shutdown();
		try {
			executorService.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String associatedCompaniesSlugs = results.get("associatedCompaniesSlugs");
		String associatedCandidatesSlugs = results.get("associatedCandidatesSlugs");
		String associatedContactsSlugs = results.get("associatedContactsSlugs");
		String associatedJobsSlugs = results.get("associatedJobsSlugs");
		String associatedDealsSlugs = results.get("associatedDealsSlugs");

		Object data[][] = {

				{ "candidate", candidateEntitySlug, "200", "", associatedCompaniesSlugs, associatedContactsSlugs,
						associatedJobsSlugs, associatedDealsSlugs },
				{ "candidate", candidateEntitySlug, "200", "", associatedCompaniesSlugs, associatedContactsSlugs,
						associatedJobsSlugs, "" },
				{ "candidate", candidateEntitySlug, "200", "", associatedCompaniesSlugs, associatedContactsSlugs, "",
						"" },
				{ "candidate", candidateEntitySlug, "200", "", associatedCompaniesSlugs, "", "", "" },
				{ "candidate", candidateEntitySlug, "200", "", "", "", "", "" },
				{ "candidate", candidateEntitySlug, "200", "", "", associatedContactsSlugs, associatedJobsSlugs,
						associatedDealsSlugs },
				{ "candidate", candidateEntitySlug, "200", "", "", "", associatedJobsSlugs, associatedDealsSlugs },
				{ "candidate", candidateEntitySlug, "200", "", "", "", "", associatedDealsSlugs },

				{ "company", companySlug, "200", associatedCandidatesSlugs, "", associatedContactsSlugs,
						associatedJobsSlugs, associatedDealsSlugs },
				{ "company", companySlug, "200", associatedCandidatesSlugs, "", associatedContactsSlugs,
						associatedJobsSlugs, "" },
				{ "company", companySlug, "200", associatedCandidatesSlugs, "", associatedContactsSlugs, "", "" },
				{ "company", companySlug, "200", associatedCandidatesSlugs, "", "", "", "" },
				{ "company", companySlug, "200", "", "", "", "", "" },
				{ "company", companySlug, "200", "", "", associatedContactsSlugs, associatedJobsSlugs,
						associatedDealsSlugs },
				{ "company", companySlug, "200", "", "", "", associatedJobsSlugs, associatedDealsSlugs },
				{ "company", companySlug, "200", "", "", "", "", associatedDealsSlugs },

				{ "contact", contactSlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs, "",
						associatedJobsSlugs, associatedDealsSlugs },
				{ "contact", contactSlug, "200", "", associatedCompaniesSlugs, "", associatedJobsSlugs,
						associatedDealsSlugs },
				{ "contact", contactSlug, "200", "", "", "", associatedJobsSlugs, associatedDealsSlugs },
				{ "contact", contactSlug, "200", "", "", "", "", associatedDealsSlugs },
				{ "contact", contactSlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs, "",
						associatedJobsSlugs, "" },
				{ "contact", contactSlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs, "", "", "" },
				{ "contact", contactSlug, "200", associatedCandidatesSlugs, "", "", "", "" },

				{ "job", jobSlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs, associatedContactsSlugs,
						"", "" },
				{ "job", jobSlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs, "", "", "" },
				{ "job", jobSlug, "200", associatedCandidatesSlugs, "", "", "", "" },
				{ "job", jobSlug, "200", "", "", "", "", "" },
				{ "job", jobSlug, "200", "", associatedCompaniesSlugs, associatedContactsSlugs, "",
						associatedDealsSlugs },
				{ "job", jobSlug, "200", "", "", associatedContactsSlugs, "", associatedDealsSlugs },
				{ "job", jobSlug, "200", "", "", "", "", associatedDealsSlugs },
				{ "job", jobSlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs, associatedContactsSlugs,
						"", associatedDealsSlugs },

				{ "deal", deaslEntitySlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs,
						associatedContactsSlugs, associatedJobsSlugs, "" },
				{ "deal", deaslEntitySlug, "200", "", associatedCompaniesSlugs, associatedContactsSlugs,
						associatedJobsSlugs, "" },
				{ "deal", deaslEntitySlug, "200", "", "", associatedContactsSlugs, associatedJobsSlugs, "" },

				{ "deal", deaslEntitySlug, "200", "", "", "", associatedJobsSlugs, "" },
				{ "deal", deaslEntitySlug, "200", "", "", "", "", "" },
				{ "deal", deaslEntitySlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs,
						associatedContactsSlugs, "", "" },
				{ "deal", deaslEntitySlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs, "", "", "" },
				{ "deal", deaslEntitySlug, "200", associatedCandidatesSlugs, "", "", "", "" },

				{ "candidate", candidateEntitySlug, "422", "", associatedCompaniesSlugs + "1",
						associatedContactsSlugs + "1", associatedJobsSlugs + "1", associatedDealsSlugs + "1" }

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

		String team = "team"+ RandomStringUtils.randomAlphabetic(5);

		ArrayList<String> userIds = new ArrayList<String>();
		userIds.add(String.valueOf(userIdMap.get(userRole1)));
		userIds.add(String.valueOf(userIdMap.get(userRole2)));

		Response createTeamResponse = allCrudFunctions.createTeam(albatrossURL, albatrossToken, team, userIds);
		createTeamResponse.then().statusCode(200);

		Response teams = function.getTeams(baseURL,  accountAPIKey);
		teams.then().statusCode(200);
		JsonPath teamPath = teams.jsonPath();

		int noOfTeams = teamPath.getInt("$.size()");
		int teamId = 0;
		for (int i = 0; i < noOfTeams; i++) {
			String teamName = teamPath.get("[" + i + "].team_name");
			if (teamName.equals(team)) {
				teamId= teamPath.get("[" + i + "].team_id");
				break;
			}
		}
		return teamId;
	}
}
