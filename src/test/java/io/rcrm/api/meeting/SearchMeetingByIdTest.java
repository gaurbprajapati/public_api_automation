package io.rcrm.api.meeting;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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
public class SearchMeetingByIdTest extends TestBase {

	public SearchMeetingByIdTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	commanFunction function = new commanFunction();
	int meetingId;


	@Owner("Harika")
	@Test(dataProvider = "getMeetingValidData", groups = "nightly-build")
	public void searchMeetingByID(String realtedToType, int statusCode, String meetingID, String meetingDescription) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("meeting", meetingID);
		String basePath = "meetings/{meeting}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		String responseBody = response.getBody().asString();

		if (statusCode == 200) {
			int meetingIDint = Integer.parseInt(meetingID);

			response.then().statusCode(statusCode);
			response.then().body("related_to_type", Matchers.is(realtedToType));
			response.then().body("id", Matchers.is(meetingIDint));
			response.then().body("description", Matchers.containsString(meetingDescription));
			MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_users"));
			MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_teams"));
			MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("do_not_send_calendar_invites"));

		} else {
			verify422Endpoint(response, statusCode, "Meeting doesn't exist", true);
		}

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void unauthorizedUserShouldNotBeAbleToSearchMeetingByID_MeetingTest() {

		String meetingID = "x001";

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("meeting", meetingID);
		String basePath = "meetings/{meeting}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"x001", null, pathParamters, true);


		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	public void verify422Endpoint(Response response, int httpStatus, String errorMessage, boolean isTrue) {
		response.then().body("errorMessage", Matchers.is(errorMessage));
		response.then().body("errorCode", Matchers.is(httpStatus));
		response.then().body("error", Matchers.is(isTrue));
	}

	@DataProvider
	public Object[][] getMeetingValidData() {

		JsonPath jsonCandidateMeeting = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int candidateMeetingID = jsonCandidateMeeting.get("id");
		String meetingDescription = jsonCandidateMeeting.get("description");
		String candidateMeetingIDString = String.valueOf(candidateMeetingID);

		JsonPath jsonCompanyMeeting = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "company").jsonPath();
		int companyMeetingID = jsonCompanyMeeting.get("id");
		String companyMeetingIDString = String.valueOf(companyMeetingID);
		String companyMeetingDescription = jsonCompanyMeeting.get("description");

		JsonPath jsonContactMeeting = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "contact").jsonPath();
		int contactMeetingID = jsonContactMeeting.get("id");
		String contactMeetingIDString = String.valueOf(contactMeetingID);
		String contactMeetingDescription = jsonContactMeeting.get("description");

		JsonPath jsonJobMeeting = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "job").jsonPath();
		int jobMeetingID = jsonJobMeeting.get("id");
		String jobMeetingIDString = String.valueOf(jobMeetingID);
		String jobMeetingDescription = jsonJobMeeting.get("description");

		JsonPath jsonDealMeeting = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "deal").jsonPath();
		int dealMeetingID = jsonDealMeeting.get("id");
		String dealMeetingIDString = String.valueOf(dealMeetingID);
		String dealMeetingDescription = jsonDealMeeting.get("description");

		Object data[][] = { { "candidate", 200, candidateMeetingIDString, meetingDescription },
				{ "contact", 200, contactMeetingIDString, contactMeetingDescription },
				{ "company", 200, companyMeetingIDString, companyMeetingDescription },
				{ "job", 200, jobMeetingIDString, jobMeetingDescription },
				{ "deal", 200, dealMeetingIDString, dealMeetingDescription },

				{ "candidate", 404, "9999" + candidateMeetingIDString, meetingDescription },
				{ "contact", 404, "9999" + contactMeetingIDString, contactMeetingDescription },
				{ "company", 404, "9999" + companyMeetingIDString, companyMeetingDescription },
				{ "job", 404, "9999" + jobMeetingIDString, jobMeetingDescription },
				{ "deal", 404, "9999" + dealMeetingIDString, dealMeetingDescription } };
		return data;
	}

}
