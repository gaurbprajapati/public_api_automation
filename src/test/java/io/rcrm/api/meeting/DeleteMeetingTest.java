package io.rcrm.api.meeting;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class DeleteMeetingTest extends TestBase {

	public DeleteMeetingTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	commanFunction function = new commanFunction();
	String authApiToken;
	int resTeamMemberId;
	int adminId;

	@BeforeClass(alwaysRun = true)	public void setup() {
		authApiToken = ThreadManager.getAccountApiKey();
	}


	@Owner("Ajendra Singh")
	@Test(dataProvider = "getMeetingValidData", groups = "nightly-build")
	public void deleteMeetingByID(String realtedToType, int statusCode) {
		JsonPath json;
		String meetingID = "";

		json = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), realtedToType).jsonPath();
		int meetingID_int = json.get("id");

		meetingID = String.valueOf(meetingID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("meeting", meetingID);
		String basePath = "meetings/{meeting}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), statusCode);

		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains("Deleted Successfully!"), "Deleted Successfully!");

	}

	@Owner("Harika")
	@Test(dataProvider = "getUserId", groups = "nightly-build")
	public void deleteMeetingCreatedByUsers(int userId) {
		JsonPath json;
		String meetingID = "";

		json = function.createMeetingWithCreatedByUserId(baseURL, authApiToken, "candidate",userId).jsonPath();
		int meetingID_int = json.get("id");

		meetingID = String.valueOf(meetingID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("meeting", meetingID);
		String basePath = "meetings/{meeting}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, authApiToken, null, pathParamters, false);


		Assert.assertEquals(response.getStatusCode(), 200);

		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains("Deleted Successfully!"), "Deleted Successfully!");
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void deleteMeetingByInvalidID() {

		String meetingID = "";

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("meeting", meetingID + "00221122");
		String basePath = "meetings/{meeting}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);


		// Assert Response Body
		verify422ResponseBody(response, 404, "Meeting doesn't exist", true);
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void unauthorizedUserShouldNotAbleToDeleteMeetingByID() {

		String meetingID = "x001";

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("meeting", meetingID);
		String basePath = "meetings/{meeting}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey() +"x001", null, pathParamters,
				false);


		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	public void verify422ResponseBody(Response response, int httpStatus, String errorMessage, boolean isTrue) {
		response.then().body("errorMessage", Matchers.is(errorMessage));
		response.then().body("errorCode", Matchers.is(httpStatus));
		response.then().body("error", Matchers.is(isTrue));
	}

	@DataProvider
	public Object[][] getMeetingValidData() {

		Object data[][] = { { "candidate", 200 }, { "contact", 200 }, { "company", 200 }, { "job", 200 },
				{ "deal", 200 } };
		return data;
	}

	@DataProvider(parallel = true)
	public Object[][] getUserId() {
		Response response = function.getUsers(baseURL, authApiToken);
		response.then().statusCode(200);

		JsonPath user = response.jsonPath();
		resTeamMemberId = user.get("[2].id");
		adminId = user.get("[1].id");

		Object data[][] = { { resTeamMemberId }, { adminId }};
		return data;
	}

}
