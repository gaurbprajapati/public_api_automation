package io.rcrm.api.notes;

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
import io.rcrm.api.javafaker.JavaFakerNote;
import io.rcrm.api.pojo.Note;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditNoteByIDTest extends TestBase {

	String slug = "";
	ArrayList<String> companySlug1;
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	int meetingId;
	int accountOwnerid;
	int adminId;
	int team1Id;

	public EditNoteByIDTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	int noteId;
	JavaFakerNote fakeNote = new JavaFakerNote();
	commanFunction function = new commanFunction();
	String notesText = fakeNote.getNotes();

	@Owner("Ajendra Singh")
	@Test(dataProvider = "getNoteByIdWithValidData", groups = "nightly-build")
	public void editNoteById_notesTest(String entityType, String entitySlug, int noteID, int responseCode) {

		String noteID_String = "";

		noteID_String = String.valueOf(noteID);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("note", noteID_String);
		String basePath = "notes/{note}";

		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type(entityType);
		note.setAssociated_companies(companySlug1.get(0));
		note.setDescription(notesText + "<br><br>" + notesText + " Edited"); // System.lineSeparator()
		note.setCollaborator_user_ids(String.valueOf(accountOwnerid));

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				note);

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();
		teamsAdded.add(team1Id);

		ArrayList<Integer> collaboratorsAdded = new ArrayList<Integer>();
		collaboratorsAdded.add(adminId);
		collaboratorsAdded.add(accountOwnerid);

		if (responseCode == 200) {
			response.then().statusCode(responseCode);
			response.then().body("description", Matchers.containsString("Edited"));
			response.then().body("collaborator_users", Matchers.containsInAnyOrder(collaboratorsAdded.toArray()));
			response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));
		} else {
			verify422ForHotlistEndpoint(response, responseCode, "Note doesn't exist", true);

		}
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void unauthorizedCannoteEditNoteById_notesTest() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("note", "x341111");
		String basePath = "notes/{note}";

		Note note = new Note();
		note.setRelated_to("2x03845");
		note.setRelated_to_type("candidate");
		note.setDescription(notesText + "<br><br>" + notesText + " Edited"); // System.lineSeparator()

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", null, pathParamters,
				true, note);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	public void verify422ForHotlistEndpoint(Response response, int httpStatus, String errorMessage, boolean isTrue) {
		response.then().body("errorMessage", Matchers.is(errorMessage));
		response.then().body("errorCode", Matchers.is(httpStatus));
		response.then().body("error", Matchers.is(isTrue));
	}

	@DataProvider
	public Object[][] getNoteByIdWithValidData() {

		createTeams();

		String userid = String.valueOf(adminId);
		String teamid = String.valueOf(team1Id);

		JsonPath jsonCandidateNote = function
				.createNewNoteWithCollaborators(baseURL, ThreadManager.getAccountApiKey(), "candidate", userid, teamid).jsonPath();
		int candidateNoteID = jsonCandidateNote.get("id");
		String candidateSlug = jsonCandidateNote.get("related_to");

		JsonPath jsonCompanyNote = function
				.createNewNoteWithCollaborators(baseURL, ThreadManager.getAccountApiKey(), "company", userid, teamid).jsonPath();
		int companyNoteID = jsonCompanyNote.get("id");
		String companySlug = jsonCompanyNote.get("related_to");

		JsonPath jsonContactNote = function
				.createNewNoteWithCollaborators(baseURL, ThreadManager.getAccountApiKey(), "contact", userid, teamid).jsonPath();
		int contactNoteID = jsonContactNote.get("id");
		String contactSlug = jsonContactNote.get("related_to");
		companySlug1 = jsonContactNote.get("associated_companies");

		JsonPath jsonJobNote = function.createNewNoteWithCollaborators(baseURL, ThreadManager.getAccountApiKey(), "job", userid, teamid)
				.jsonPath();
		int jobNoteID = jsonJobNote.get("id");
		String jobSlug = jsonJobNote.get("related_to");

		JsonPath jsonDealNote = function.createNewNoteWithCollaborators(baseURL, ThreadManager.getAccountApiKey(), "deal", userid, teamid)
				.jsonPath();
		int dealNoteID = jsonDealNote.get("id");
		String dealSlug = jsonDealNote.get("related_to");

		Object data[][] = {

				{ "candidate", candidateSlug, candidateNoteID, 200 }, { "company", companySlug, companyNoteID, 200 },
				{ "contact", contactSlug, contactNoteID, 200 }, { "job", jobSlug, jobNoteID, 200 },
				{ "deal", dealSlug, dealNoteID, 200 }, { "candidate", candidateSlug, 1000 + candidateNoteID, 404 },
				{ "company", companySlug, 1000 + companyNoteID, 404 },
				{ "contact", contactSlug, 1000 + contactNoteID, 404 }, { "job", jobSlug, 1000 + jobNoteID, 404 },
				{ "deal", dealSlug, 1000 + dealNoteID, 404 }

		};

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
