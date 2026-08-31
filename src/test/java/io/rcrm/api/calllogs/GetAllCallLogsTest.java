package io.rcrm.api.calllogs;

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
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetAllCallLogsTest extends TestBase {

	public GetAllCallLogsTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	commanFunction function = new commanFunction();

	@Owner("Harika")
	@Test(dataProvider = "getEntityValidData", groups = "nightly-build")
	public void showAllCallLogs_GET(String relatedToType, int statusCode) {
		function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), relatedToType).jsonPath();
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "call-logs", ThreadManager.getAccountApiKey(), queryParameters, null, true);


		String responseBody = response.getBody().asString();

		response.then().statusCode(statusCode);
		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
		response.then().body("data[0].duration", Matchers.comparesEqualTo(3600));

		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_users"));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_teams"));
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAllCallLogs() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "10");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "call-logs", ThreadManager.getAccountApiKey()+"12345", queryParameters, null,
				true);


		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider
	public Object[][] getEntityValidData() {

		Object data[][] = { { "candidate", 200 }, { "contact", 200 }, { "company", 200 } };
		return data;
	}
}