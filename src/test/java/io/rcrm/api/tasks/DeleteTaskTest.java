package io.rcrm.api.tasks;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
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
public class DeleteTaskTest extends TestBase {

	public DeleteTaskTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String taskId = "";
	commanFunction function = new commanFunction();
	String authApiToken;
	int resTeamMemberId;
	int adminId;

	@BeforeClass(alwaysRun = true)	public void setup() {
		authApiToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Harika")
	@Test(dataProvider = "getTaskValidData", groups = "nightly-build")
	public void deleteTaskById_GET(String relatedToType, int statusCode) {
		JsonPath json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), relatedToType).jsonPath();
		int taskID = json.get("id");
		taskId = String.valueOf(taskID);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", taskId);

		String basePath = "tasks/{task}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		// Verify Response Code and body
		response.then().statusCode(statusCode);
		response.then().body(Matchers.is("\"Deleted Successfully!\""));

	}

	@Owner("Harika")
	@Test(dataProvider = "getUserId", groups = "nightly-build")
	public void deleteTaskCreatedByUsers(int userId) {
		JsonPath json = function.createTaskWithCreatedByUserId(baseURL, authApiToken, "candidate",userId).jsonPath();
		int taskID = json.get("id");

		taskId = String.valueOf(taskID);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", taskId);

		String basePath = "tasks/{task}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, authApiToken, null, pathParamters, true);

		// Verify Response Code and body
		response.then().statusCode(200);
		response.then().body(Matchers.is("\"Deleted Successfully!\""));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void deleteTaskByInvalidID_GET() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", taskId + "123");

		String basePath = "tasks/{task}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		// Verify Response Code and body
		response.then().statusCode(404);
		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(404));
		response.then().body("errorMessage", Matchers.is("Task doesn't exist"));

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotDeleteTask() {
		JsonPath json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int taskID = json.get("id");
		String taskId = String.valueOf(taskID);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", taskId);
		String basePath = "tasks/{task}";
		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", null, pathParamters,
				true);
		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider
	public Object[][] getTaskValidData() {
		Object data[][] = { { "candidate", 200 }, { "contact", 200 }, { "company", 200 }, { "job", 200 },
				{ "deal", 200 } };
		return data;
	}

	@DataProvider(parallel = true)
	public Object[][] getUserId() {
		Response response = function.getUsers(baseURL, authApiToken);
		response.then().statusCode(200);

		JsonPath user = response.jsonPath();
		adminId = user.get("[1].id");
		resTeamMemberId = user.get("[2].id");

		Object data[][] = { { resTeamMemberId }, { adminId }};
		return data;
	}
}
