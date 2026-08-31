package io.rcrm.api.hotlists;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerHotlist;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class SeachHotlistByIDTest extends TestBase {

	public SeachHotlistByIDTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	commanFunction function = new commanFunction();

	@Owner("Raj Pandey")
	@Test(dataProvider = "getHotlistIdAndValidData", groups = "nightly-build")
	public void searchHotlistByID_HotlistTest(String realtedToType, int hotlistID, int shared, String hotlistName,
			String hotlistMessage, int statusCode) {

		String hotlistID_String = "";
		hotlistID_String = String.valueOf(hotlistID);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("hotlist", hotlistID_String);
		String basePath = "hotlists/{hotlist}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);


		if (statusCode == 200) {
			response.then().statusCode(statusCode);
			response.then().body("id", Matchers.is(hotlistID));
			response.then().body("name", Matchers.containsString(hotlistName));
			response.then().body("shared", Matchers.is(shared));
			response.then().body("related_to_type", Matchers.containsString(realtedToType));

		} else {
			verify422ForHotlistEndpoint(response, statusCode, "Hotlist doesn't exist", true);

		}
	}

	@Owner("Sampurn Chouksey")
	@Test
	public void unauthorizedUserShouldNotBeAbleToSearchHotlistByID_HotlistTest() {

		String hotlistID = "x001";

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("hotlist", hotlistID);
		String basePath = "hotlists/{hotlist}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"123", null, pathParamters, true);


		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	public void verify422ForHotlistEndpoint(Response response, int httpStatus, String errorMessage, boolean isTrue) {
		response.then().body("errorMessage", Matchers.is(errorMessage));
		response.then().body("errorCode", Matchers.is(httpStatus));
		response.then().body("error", Matchers.is(isTrue));
	}

	@DataProvider
	public Object[][] getHotlistIdAndValidData() {

		JsonPath jsonCandidateHotlist = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int candidateHotlistID = jsonCandidateHotlist.get("id");
		String candidateHotlistName = jsonCandidateHotlist.get("name");

		JsonPath jsonCompanyHotlist = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "company").jsonPath();
		int companyHotlistID = jsonCompanyHotlist.get("id");
		String companyHotlistName = jsonCompanyHotlist.get("name");

		JsonPath jsonContactHotlist = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "contact").jsonPath();
		int contactHotlistID = jsonContactHotlist.get("id");
		String contactHotlistName = jsonContactHotlist.get("name");

		JsonPath jsonJobHotlist = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "job").jsonPath();
		int jobHotlistID = jsonJobHotlist.get("id");
		String jobHotlistName = jsonJobHotlist.get("name");

		Object data[][] = {

				{ "candidate", candidateHotlistID, 1, candidateHotlistName, "", 200 },
				{ "company", companyHotlistID, 1, companyHotlistName, "", 200 },
				{ "contact", contactHotlistID, 1, contactHotlistName, "", 200 },
				{ "job", jobHotlistID, 1, jobHotlistName, "", 200 },

				{ "candidate", 1000 + candidateHotlistID, 1, candidateHotlistName, "", 404 },
				{ "company", 1000 + companyHotlistID, 1, companyHotlistName, "", 404 },
				{ "contact", 1000 + contactHotlistID, 1, contactHotlistName, "", 404 },
				{ "job", 1000 + jobHotlistID, 1, jobHotlistName, "", 404 }

		};

		return data;
	}

}
