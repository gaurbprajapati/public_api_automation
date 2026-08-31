package io.rcrm.api.tasks;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerTask;
import io.rcrm.api.pojo.Task;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndpointsOfTasksTest extends TestBase {

	public AllEndpointsOfTasksTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

	JavaFakerTask fakerTask = new JavaFakerTask();
	String taskTitle = fakerTask.getTaskName();
	String taskDescription = fakerTask.getDescription();
	String startDate = fakerTask.getFutureDate();
	String pastDate = fakerTask.getPastDate();
	int taskId;
	int accountOwnerid;
	int adminId;
	int team1Id;

	@Owner("Harika")
	@Test(priority = 0, groups = "nightly-build")
	public void createNewTask() {

		JsonPath json;
		String entitySlug = "";
		createTeams();

		json = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		entitySlug = json.get("slug");

		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(15);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type("candidate");
		task.setStart_date(startDate);

		Response response = RestClient.doPost("JSON", baseURL, "tasks", ThreadManager.getAccountApiKey(), null, true, task);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//task//createTask.json"));

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		taskId = jp.get("id");
		// 2295174

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();
		teamsAdded.add(team1Id);

		ArrayList<Integer> collaboratorsAdded = new ArrayList<Integer>();

		response.then().body("title", Matchers.containsString(taskTitle));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is("candidate"));
		response.then().body("collaborators", Matchers.containsInAnyOrder(collaboratorsAdded.toArray()));
		response.then().body("collaborator_users", Matchers.containsInAnyOrder(collaboratorsAdded.toArray()));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));
	}

	@Owner("Ajendra Singh")
	@Test(priority = 1, groups = "nightly-build")
	public void showAllTasks() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "tasks", ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//task//getAllTasks.json"));
		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchTaskByFields() {
		JsonPath json;
		String noteID = "";

		json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int noteID_int = json.get("id");
		String entitySlug = json.get("related_to");

		noteID = String.valueOf(noteID_int);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("related_to", entitySlug);
		queryParameters.put("related_to_type", "candidate");

		String basePath = "tasks/search";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.related_to_type[0]", Matchers.is("candidate"));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchTaskByID() {
		JsonPath json;
		String taskID = "";

		json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int noteID_int = json.get("id");

		taskID = String.valueOf(noteID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", taskID);
		String basePath = "tasks/{task}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("related_to_type", Matchers.is("candidate"));
		response.then().body("description", Matchers.containsString(json.get("description")));
	}

	@Owner("Ajendra Singh")
	@Test
	public void editTaskByID() {

		JsonPath json;
		String entitySlug = "";
		String taskID = "";

		json = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		entitySlug = json.get("slug");

		json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int noteID_int = json.get("id");

		taskID = String.valueOf(noteID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", taskID);
		String basePath = "tasks/{task}";

		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(15);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type("candidate");
		task.setStart_date(startDate);
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				task);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//task//editTask.json"));
		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		taskId = jp.get("id");
		// 2295174

		response.then().body("title", Matchers.containsString(taskTitle));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is("candidate"));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void deleteTaskByID() {
		JsonPath json;
		String taskID = "";

		json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int taskID_int = json.get("id");

		taskID = String.valueOf(taskID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", taskID);
		String basePath = "tasks/{task}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), 200);

		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains("Deleted Successfully!"), "Deleted Successfully!");
	}

	public void createTeams() {
		Response response = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
		response.then().statusCode(200);
		JsonPath user = response.jsonPath();
		accountOwnerid = user.get("[0].id");
		adminId = user.get("[1].id");

		Response team = function.getTeams(baseURL, ThreadManager.getAccountApiKey());
		team.then().statusCode(200);
		JsonPath teamJsonPath = team.jsonPath();

		int arraySize = teamJsonPath.getInt("$.size()");
		for (int i = 0; i < arraySize; i++) {
			int teamId = teamJsonPath.get("[" + i + "].team_id");
			allCrudFunctions.deleteTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), teamId);

		}

		ArrayList<String> userId1 = new ArrayList<String>();
		userId1.add(String.valueOf(accountOwnerid));
		userId1.add(String.valueOf(adminId));

		Response response1 = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "team1", userId1);
		response1.then().statusCode(200);

		Response teamCreated = function.getTeams(baseURL, ThreadManager.getAccountApiKey());
		teamCreated.then().statusCode(200);
		JsonPath teamPath = teamCreated.jsonPath();

		team1Id = teamPath.get("[0].team_id");
	}

}
