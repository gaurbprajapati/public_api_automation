package io.rcrm.api.notes;

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
import io.rcrm.api.javafaker.JavaFakerNote;
import io.rcrm.api.pojo.Note;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class AllEndpointsOfNotesTest extends TestBase {

	String slug = "";

	public AllEndpointsOfNotesTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	int noteId;
	JavaFakerNote fakeNote = new JavaFakerNote();
	commanFunction function = new commanFunction();
	String notesText = fakeNote.getNotes();

	@Owner("Harika")
	@Test(priority = 0, groups = "nightly-build")
	public void createNewNote() {
		JsonPath json;
		String entitySlug = "";

		json = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		entitySlug = json.get("slug");

		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type("candidate");
		note.setDescription(notesText + "<br><br>" + notesText); // System.lineSeparator()
		note.setEnable_auto_populate_teams(0);

		Response response = RestClient.doPost("JSON", baseURL, "notes", ThreadManager.getAccountApiKey(), null, true, note);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//note//createNote.json"));

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		noteId = jp.get("id");
		// 2295174

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();

		response.then().body("description", Matchers.containsString(notesText));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is("candidate"));
		response.then().body("collaborator_users", Matchers.containsInAnyOrder(teamsAdded.toArray()));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));
	}

	@Owner("Ajendra Singh")
	@Test(priority = 1, groups = "nightly-build")
	public void showAllNotes_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "notes", ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//note//getAllNotes.json"));

		// Asserting response time in between some values
		// response.then().time(Matchers.both(Matchers.greaterThanOrEqualTo(500L)).and(Matchers.lessThanOrEqualTo(2000L)));

		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchNoteByID() {
		JsonPath json;
		String noteID = "";

		json = function.createNewNoteAndGetResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int noteID_int = json.get("id");

		noteID = String.valueOf(noteID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("note", noteID);
		String basePath = "notes/{note}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("related_to_type", Matchers.is("candidate"));
		response.then().body("description", Matchers.containsString(json.get("description")));
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void searchNoteByFields() {

		JsonPath json;
		String noteID = "";

		json = function.createNewNoteAndGetResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int noteID_int = json.get("id");
		String entitySlug = json.get("related_to");

		noteID = String.valueOf(noteID_int);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("related_to", entitySlug);
		queryParameters.put("related_to_type", "candidate");
		String basePath = "notes/search";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.related_to_type[0]", Matchers.is("candidate"));
		// response.then().body("description",
		// Matchers.containsString(json.get("description")));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void editNoteById() {
		JsonPath json;
		String noteID = "";

		json = function.createNewNoteAndGetResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int noteID_int = json.get("id");
		String entitySlug = json.get("related.slug");

		noteID = String.valueOf(noteID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("note", noteID);
		String basePath = "notes/{note}";

		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type("candidate");
		note.setDescription(notesText + "<br><br>" + notesText + " Edited"); // System.lineSeparator()

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				note);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//note//editNote.json"));
		response.then().body("description", Matchers.containsString("Edited"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void DeleteNoteByIDTest() {
		JsonPath json;
		String noteID = "";

		json = function.createNewNoteAndGetResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int noteID_int = json.get("id");

		noteID = String.valueOf(noteID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("note", noteID);
		String basePath = "notes/{note}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), 200);

		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains("Deleted Successfully!"), "Deleted Successfully!");
	}

}
