package io.rcrm.api.notes;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.testng.Assert;
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
public class GetAllNotesTest extends TestBase {

	String slug = "";

	public GetAllNotesTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	int noteId;
	JavaFakerNote fakeNote = new JavaFakerNote();
	commanFunction function = new commanFunction();
	String notesText = fakeNote.getNotes();

	@Owner("Ajendra Singh")
	@Test(dataProvider = "getNotesValidData", groups = "nightly-build")
	public void getAllNotes_noteTest(String realtedToType, int statusCode) {

		JsonPath noteJson = function.createNewNoteAndGetResponse(baseURL, ThreadManager.getAccountApiKey(), realtedToType).jsonPath();
		int noteID = noteJson.get("id");

		String noteID_String = "";
		noteID_String = String.valueOf(noteID);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "notes", ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		String responseBody = response.getBody().asString();

		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_users"));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_teams"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void unauthorizedCannotAccessgGetAllNotes_noteTest() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "notes", ThreadManager.getAccountApiKey()+"12345", queryParameters, null,
				true);


		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	public void verify422ForHotlistEndpoint(Response response, int httpStatus, String errorMessage, boolean isTrue) {
		response.then().body("errorMessage", Matchers.is(errorMessage));
		response.then().body("errorCode", Matchers.is(httpStatus));
		response.then().body("error", Matchers.is(isTrue));
	}

	@DataProvider
	public Object[][] getNotesValidData() {

		Object data[][] = { { "candidate", 200 }, { "company", 200 }, { "contact", 200 }, { "job", 200 },
				{ "deal", 200 } };
		return data;
	}

}
