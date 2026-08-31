package io.rcrm.api.notes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerNote;
import io.rcrm.api.pojo.Note;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateNewNoteTest extends TestBase {

	String slug = "";

	public CreateNewNoteTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	int noteId;
	JavaFakerNote fakeNote = new JavaFakerNote();
	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	String notesText = fakeNote.getNotes();
	int accountOwnerid;
	int adminId;
	int resTeamMember;
	int teamMember;
	int team1Id;
	int team2Id;
	int team3Id;
	String entitySlug = "";

	@Owner("Ajendra Singh")
	@Test(dataProvider = "getNotesValidData", groups = "nightly-build")
	public void createNewNote(String realtedToType) {
		createEntityTestData(realtedToType);
		if (realtedToType == "candidate") {
			createTeams();
		}
		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type(realtedToType);
		note.setDescription(notesText + "<br><br>" + notesText); // System.lineSeparator()

		Response response = RestClient.doPost("JSON", baseURL, "notes", ThreadManager.getAccountApiKey(), null, true, note);

		JsonPath jp = response.jsonPath();

		noteId = jp.get("id");

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();
		teamsAdded.add(team1Id);

		response.then().statusCode(200);
		response.then().body("description", Matchers.containsString(notesText));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createNoteWithDisableAutoTeamsPopulation() {
		createEntityTestData("candidate");

		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type("candidate");
		note.setDescription(notesText + "<br><br>" + notesText); // System.lineSeparator()
		note.setEnable_auto_populate_teams(0);

		Response response = RestClient.doPost("JSON", baseURL, "notes", ThreadManager.getAccountApiKey(), null, true, note);

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();

		response.then().statusCode(200);
		response.then().body("description", Matchers.containsString(notesText));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is("candidate"));
		response.then().body("collaborator_users", Matchers.containsInAnyOrder(teamsAdded.toArray()));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createNoteWithEnableAutoTeamsPopulation() {
		createEntityTestData("candidate");

		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type("candidate");
		note.setDescription(notesText + "<br><br>" + notesText); // System.lineSeparator()
		note.setCreated_by(adminId);
		note.setEnable_auto_populate_teams(1);

		Response response = RestClient.doPost("JSON", baseURL, "notes", ThreadManager.getAccountApiKey(), null, true, note);

		ArrayList<Integer> usersAdded = new ArrayList<Integer>();

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();
		teamsAdded.add(team2Id);

		response.then().statusCode(200);
		response.then().body("description", Matchers.containsString(notesText));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is("candidate"));
		response.then().body("collaborator_users", Matchers.containsInAnyOrder(usersAdded.toArray()));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));

	}

	@Owner("Harika")
	@Test(dataProvider = "getNotesValidData", groups = "nightly-build")
	public void createNoteWithCollaborators(String realtedToType) {
		createEntityTestData(realtedToType);

		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type(realtedToType);
		note.setDescription(notesText + "<br><br>" + notesText); // System.lineSeparator()
		note.setCreated_by(accountOwnerid);
		note.setUpdated_by(accountOwnerid);
		note.setCollaborator_user_ids(String.valueOf(adminId) + "," + String.valueOf(resTeamMember));
		note.setCollaborator_team_ids(String.valueOf(team2Id) + "," + String.valueOf(team3Id));
		note.setEnable_auto_populate_teams(1);

		Response response = RestClient.doPost("JSON", baseURL, "notes", ThreadManager.getAccountApiKey(), null, true, note);

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();
		teamsAdded.add(team2Id);
		teamsAdded.add(team3Id);

		ArrayList<Integer> collaboratorsAdded = new ArrayList<Integer>();
		collaboratorsAdded.add(adminId);
		collaboratorsAdded.add(resTeamMember);

		response.then().statusCode(200);
		response.then().body("description", Matchers.containsString(notesText));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		response.then().body("collaborator_users", Matchers.containsInAnyOrder(collaboratorsAdded.toArray()));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createNoteWithInvalidCollaborators() {
		createEntityTestData("candidate");

		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type("candidate");
		note.setDescription(notesText + "<br><br>" + notesText); // System.lineSeparator()
		note.setEnable_auto_populate_teams(0);
		note.setCreated_by(accountOwnerid);
		note.setUpdated_by(accountOwnerid);
		note.setCollaborator_user_ids("1234," + String.valueOf(adminId));
		note.setCollaborator_team_ids(String.valueOf(team2Id) + "," + String.valueOf(team3Id));
		note.setEnable_auto_populate_teams(1);

		Response response = RestClient.doPost("JSON", baseURL, "notes", ThreadManager.getAccountApiKey(), null, true, note);

		response.then().statusCode(422);
		response.then().body("collaborator_user_ids[0]", Matchers.is("collaborators ids are not valid"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createNoteWithInvalidCollaboratorsTeams() {
		createEntityTestData("candidate");

		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type("candidate");
		note.setDescription(notesText + "<br><br>" + notesText); // System.lineSeparator()
		note.setEnable_auto_populate_teams(0);
		note.setCreated_by(accountOwnerid);
		note.setUpdated_by(accountOwnerid);
		note.setCollaborator_user_ids(String.valueOf(accountOwnerid) + "," + String.valueOf(adminId));
		note.setCollaborator_team_ids("1234," + String.valueOf(team3Id));
		note.setEnable_auto_populate_teams(1);

		Response response = RestClient.doPost("JSON", baseURL, "notes", ThreadManager.getAccountApiKey(), null, true, note);

		response.then().statusCode(422);
		response.then().body("collaborator_team_ids[0]", Matchers.is("Team ids are not valid"));

	}

	@Owner("Ajendra Singh")
	@Test(dataProvider = "getNotesValidDataForAssociates")
	public void createNewNoteWithAssociates(String realtedToType, String statusCode) {
		JsonPath json;
		String entitySlug = "";

		if (realtedToType == "candidate") {
			json = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "company") {
			json = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType == "contact") {

			JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			;
			String companySlug = jsonCompany.get("slug");
			json = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "job") {
			JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			json = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "deal") {
			JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			String companySlug = jsonCompany.get("slug");

			JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");

			JsonPath jsonJob = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.getString("slug");

			JsonPath jsonDeal = function
					.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug, jobSlug)
					.jsonPath();

			entitySlug = jsonDeal.get("slug");
		}

		String associatedCandidatesSlugs = "";
		String associatedCompaniesSlugs = "";
		String associatedContactsSlugs = "";
		String associatedJobsSlugs = "";
		String associatedDealsSlugs = "";

		if (realtedToType != "company") {
			JsonPath jsonGetAllCompanies = function.getAllCompanies_GET(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			List<String> companyslugs = jsonGetAllCompanies.get("data.slug");
			associatedCompaniesSlugs = StringUtils.join(companyslugs, ",");
		}

		if (realtedToType != "candidate") {
			JsonPath jsonGetAllCandidates = function.getAllCandidates(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			List<String> candidateslugs = jsonGetAllCandidates.get("data.slug");
			associatedCandidatesSlugs = StringUtils.join(candidateslugs, ",");
		}

		if (realtedToType != "contact") {
			JsonPath jsonGetAllContacts = function.getAllContacts_GET(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			List<String> contactslugs = jsonGetAllContacts.get("data.slug");
			associatedContactsSlugs = StringUtils.join(contactslugs, ",");
		}

		if (realtedToType != "job") {
			JsonPath jsonGetAllJobs = function.getAllJobs_GET(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			List<String> jobsSlugs = jsonGetAllJobs.get("data.slug");
			associatedJobsSlugs = StringUtils.join(jobsSlugs, ",");
		}

		if (realtedToType != "deal") {
			JsonPath jsonGetAllDeals = function.getAllDealsGET(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			List<String> DealSlugs = jsonGetAllDeals.get("data.slug");
			associatedDealsSlugs = StringUtils.join(DealSlugs, ",");
		}

		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type(realtedToType);
		note.setDescription(notesText + "<br><br>" + notesText); // System.lineSeparator()

		note.setAssociated_candidates(associatedCandidatesSlugs);
		note.setAssociated_contacts(associatedContactsSlugs);
		note.setAssociated_companies(associatedCompaniesSlugs);
		note.setAssociated_deals(associatedDealsSlugs);
		note.setAssociated_jobs(associatedJobsSlugs);

		Response response = RestClient.doPost("JSON", baseURL, "notes", ThreadManager.getAccountApiKey(), null, true, note);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		noteId = jp.get("id");
		// 2295174

		response.then().body("description", Matchers.containsString(notesText));
		response.then().body("related_to", Matchers.is(entitySlug));
		// response.then().body("related_to_type", Matchers.is(realtedToType));

	}

	@Owner("Harika")
	@Test(dataProvider = "getNotesDataForAssociates", groups = "nightly-build")
	public void createNewNoteWithAllValidAssociates(String realtedToType, String entitySlug, String statusCode,
			String associatedCandidatesSlugs, String associatedCompaniesSlugs, String associatedContactsSlugs,
			String associatedJobsSlugs, String associatedDealsSlugs) {

		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type(realtedToType);
		note.setDescription(notesText + "<br><br>" + notesText); // System.lineSeparator()

		note.setAssociated_candidates(associatedCandidatesSlugs);
		note.setAssociated_contacts(associatedContactsSlugs);
		note.setAssociated_companies(associatedCompaniesSlugs);
		note.setAssociated_deals(associatedDealsSlugs);
		note.setAssociated_jobs(associatedJobsSlugs);

		Response response = RestClient.doPost("JSON", baseURL, "notes", ThreadManager.getAccountApiKey(), null, true, note);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		if (statusCode == "200") {
			response.then().statusCode(200);
			response.then().body("description", Matchers.containsString(notesText));
			response.then().body("related_to", Matchers.is(entitySlug));
		} else if (statusCode == "422") {

			response.then().body("associated_companies[0]", Matchers.containsString("Invalid associated companies"));
			response.then().body("associated_contacts[0]", Matchers.containsString("Invalid associated contacts"));
			response.then().body("associated_jobs[0]", Matchers.containsString("Invalid associated jobs"));
			response.then().body("associated_deals[0]", Matchers.containsString("nvalid associated deals"));
		}
	}

	@DataProvider
	public Object[][] getNotesValidData() {
		Object data[][] = { { "candidate" } };
		return data;
	}

	@DataProvider
	public Object[][] getNotesValidDataForAssociates() {
		Object data[][] = { { "candidate", "200" }, { "company", "200" }, { "contact", "200" }, { "job", "200" },
				{ "deal", "200" } };
		return data;
	}

	@DataProvider
	public Object[][] getNotesInValidDataForAssociates() {
		Object data[][] = { { "candidate", "200" }, { "company", "200" }, { "contact", "200" }, { "job", "200" },
				{ "deal", "200" } };
		return data;
	}

	@DataProvider
	public Object[][] getNotesDataForAssociates() {

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");

		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String companySlug = jsonCompany.get("slug");

		JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");

		JsonPath jsonJob = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath();
		String jobSlug = jsonJob.getString("slug");

		JsonPath jsonDeal = function
				.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug, jobSlug).jsonPath();

		String deaslEntitySlug = jsonDeal.get("slug");

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
		associatedDealsSlugs = StringUtils.join(DealSlugs, ",");

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

	public void createTeams() {
		Response response = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
		response.then().statusCode(200);
		JsonPath user = response.jsonPath();
		accountOwnerid = user.get("[0].id");
		adminId = user.get("[1].id");
		resTeamMember = user.get("[2].id");
		teamMember = user.get("[3].id");

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
		userId1.add(String.valueOf(teamMember));

		Response response1 = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "team1", userId1);
		response1.then().statusCode(200);

		ArrayList<String> userId2 = new ArrayList<String>();
		userId2.add(String.valueOf(adminId));
		userId2.add(String.valueOf(resTeamMember));

		Response response2 = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "team2", userId2);
		response2.then().statusCode(200);

		ArrayList<String> userId3 = new ArrayList<String>();
		userId3.add(String.valueOf(teamMember));
		userId3.add(String.valueOf(resTeamMember));

		Response response3 = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "team3", userId3);
		response3.then().statusCode(200);

		Response team1 = function.getTeams(baseURL, ThreadManager.getAccountApiKey());
		team1.then().statusCode(200);
		JsonPath teamPath = team1.jsonPath();

		int noOfTeams = teamPath.getInt("$.size()");

		for (int i = 0; i < noOfTeams; i++) {
			String teamName = teamPath.get("[" + i + "].team_name");
			if (teamName.equals("team1")) {
				team1Id = teamPath.get("[" + i + "].team_id");
			}
			if (teamName.equals("team2")) {
				team2Id = teamPath.get("[" + i + "].team_id");
			}
			if (teamName.equals("team3")) {
				team3Id = teamPath.get("[" + i + "].team_id");
			}
		}

	}

	public void createEntityTestData(String relatedToType) {
		JsonPath json;
		if (relatedToType == "candidate") {
			json = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			entitySlug = json.get("slug");
		}

		if (relatedToType == "company") {
			json = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			entitySlug = json.get("slug");
		}
		if (relatedToType == "contact") {

			JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			;
			String companySlug = jsonCompany.get("slug");
			json = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (relatedToType == "job") {
			JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			json = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (relatedToType == "deal") {
			JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			String companySlug = jsonCompany.get("slug");

			JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");

			JsonPath jsonJob = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.getString("slug");

			JsonPath jsonDeal = function
					.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug, jobSlug)
					.jsonPath();

			entitySlug = jsonDeal.get("slug");
		}
	}

}
