package io.rcrm.api.meeting;

import java.io.IOException;
import java.util.HashMap;
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
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetAllMeetingTest extends TestBase {

	public GetAllMeetingTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	commanFunction function = new commanFunction();
	int meetingId;

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getMeetingValidData", groups = "nightly-build")
	public void getAllMeeting_Test(String realtedToType, int statusCode) {
		JsonPath json;
		String meetingID = "";

		json = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), realtedToType).jsonPath();
		int meetingID_int = json.get("id");

		meetingID = String.valueOf(meetingID_int);
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "meetings", ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		String responseBody = response.getBody().asString();

		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_users"));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_teams"));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("do_not_send_calendar_invites"));
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAllMeeting_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "meetings", ThreadManager.getAccountApiKey()+"x001", queryParameters, null,
				true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@DataProvider
	public Object[][] getMeetingValidData() {

		Object data[][] = { { "candidate", 200 }, { "contact", 200 }, { "company", 200 }, { "job", 200 },
				{ "deal", 200 } };
		return data;
	}

}
