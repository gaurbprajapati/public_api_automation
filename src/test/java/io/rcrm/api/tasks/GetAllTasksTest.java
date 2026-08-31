package io.rcrm.api.tasks;

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
public class GetAllTasksTest extends TestBase {

	public GetAllTasksTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	commanFunction function = new commanFunction();

	@Owner("Harika")
	@Test(dataProvider = "getTaskValidData", groups = "nightly-build")
	public void showAllTasks_GET(String relatedToType, int statusCode) {
		function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), relatedToType).jsonPath();
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "tasks", ThreadManager.getAccountApiKey(), queryParameters, null, true);


		String responseBody = response.getBody().asString();

		response.then().statusCode(statusCode);
		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborators"));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_users"));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_teams"));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("task_type"));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAllTasks() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "10");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "tasks", ThreadManager.getAccountApiKey()+"12345", queryParameters, null,
				true);


		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider
	public Object[][] getTaskValidData() {

		Object data[][] = { { "candidate", 200 }, { "company", 200 }, { "contact", 200 }, { "job", 200 },
				{ "deal", 200 } };
		return data;
	}
}