package io.rcrm.api.notes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerNote;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class SearchNoteByIDTest extends TestBase {

	String slug = "";

	public SearchNoteByIDTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	int noteId;
	JavaFakerNote fakeNote = new JavaFakerNote();
	commanFunction function = new commanFunction();
	String notesText = fakeNote.getNotes();

	@Owner("Harika")
	@Test(dataProvider = "getNoteByIdWithValidData", groups = "nightly-build")
	public void searchNoteByID(String entityType, int noteID, int responseCode) {

		String noteID_String = "";
		noteID_String = String.valueOf(noteID);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("note", noteID_String);
		String basePath = "notes/{note}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);


		String responseBody = response.getBody().asString();

		if (responseCode == 200) {
			response.then().statusCode(responseCode);
			response.then().body("related_to_type", Matchers.is(entityType));
			MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_users"));
			MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_teams"));
		} else {
			verify422ForHotlistEndpoint(response, responseCode, "Note doesn't exist", true);

		}
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void unauthorizedUserShouldNotBeAbleToSearchNoteByID_noteTest() {

		String noteID = "x001";

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("note", noteID);
		String basePath = "notes/{note}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"x001", null, pathParamters, true);


		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	public void verify422ForHotlistEndpoint(Response response, int httpStatus, String errorMessage, boolean isTrue) {
		response.then().body("errorMessage", Matchers.is(errorMessage));
		response.then().body("errorCode", Matchers.is(httpStatus));
		response.then().body("error", Matchers.is(isTrue));
	}

	@DataProvider
	public Object[][] getNoteByIdWithValidData() {

		JsonPath jsonCandidateNote = function.createNewNoteAndGetResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate")
				.jsonPath();
		int candidateNoteID = jsonCandidateNote.get("id");

		JsonPath jsonCompanyNote = function.createNewNoteAndGetResponse(baseURL, ThreadManager.getAccountApiKey(), "company").jsonPath();
		int companyNoteID = jsonCompanyNote.get("id");

		JsonPath jsonContactNote = function.createNewNoteAndGetResponse(baseURL, ThreadManager.getAccountApiKey(), "contact").jsonPath();
		int contactNoteID = jsonContactNote.get("id");

		JsonPath jsonJobNote = function.createNewNoteAndGetResponse(baseURL, ThreadManager.getAccountApiKey(), "job").jsonPath();
		int jobNoteID = jsonJobNote.get("id");

		JsonPath jsonDealNote = function.createNewNoteAndGetResponse(baseURL, ThreadManager.getAccountApiKey(), "deal").jsonPath();
		int dealNoteID = jsonDealNote.get("id");

		Object data[][] = {

				{ "candidate", candidateNoteID, 200 }, { "company", companyNoteID, 200 },
				{ "contact", contactNoteID, 200 }, { "job", jobNoteID, 200 }, { "deal", dealNoteID, 200 },

				{ "candidate", 1000 + candidateNoteID, 404 }, { "company", 1000 + companyNoteID, 404 },
				{ "contact", 1000 + contactNoteID, 404 }, { "job", 1000 + jobNoteID, 404 }

		};

		return data;
	}

}
