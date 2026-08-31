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
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class SearchTaskByIdTest extends TestBase {

	public SearchTaskByIdTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String task_Id = "";
	commanFunction function = new commanFunction();

	@Owner("Harika")
	@Test(dataProvider = "getTaskValidData", groups = "nightly-build")
	public void searchTaskById_GET(String realtedToType, int statusCode) {
		JsonPath json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), realtedToType).jsonPath();
		int taskID = json.get("id");
		task_Id = String.valueOf(taskID);
		String taskTitle = json.get("title");
		String description = json.get("description");
		String related_to = json.get("related_to");

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", task_Id);

		String basePath = "tasks/{task}";
		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);


		String responseBody = response.getBody().asString();

		// Verify Response Code and body
		response.then().statusCode(statusCode);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("title", Matchers.containsString(taskTitle));
		response.then().body("description", Matchers.containsString(description));
		response.then().body("related_to", Matchers.is(related_to));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_users"));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborators"));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_teams"));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("task_type"));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchTaskByInvalidId_GET() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", task_Id + "12345");

		String basePath = "tasks/{task}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);


		// Verify Response Code and body
		response.then().statusCode(404);
		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(404));
		response.then().body("errorMessage", Matchers.is("Task doesn't exist"));

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotSearchTaskById() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", task_Id);

		String basePath = "tasks/{task}";
		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", null, pathParamters, true);


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
