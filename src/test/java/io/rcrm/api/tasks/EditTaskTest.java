package io.rcrm.api.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerDeal;
import io.rcrm.api.javafaker.JavaFakerHotlist;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.javafaker.JavaFakerTask;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.pojo.Task;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditTaskTest extends TestBase {

	public EditTaskTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String taskID = "";
	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	Task task = new Task();
	JavaFakerTask fakerTask = new JavaFakerTask();
	JavaFakerJob jobFaker = new JavaFakerJob();
	String taskTitle = fakerTask.getTaskName();
	String taskDescription = fakerTask.getDescription();
	String startDate = fakerTask.getFutureDate();
	String pastDate = fakerTask.getPastDate();
	int invalidSReminder = jobFaker.qualification_id();
	String longText = jobFaker.getJobDescriptionText() + jobFaker.getNoteForCandidate();

	int accountOwnerid;
	int adminId;
	int team1Id;
	int team2Id;


	@Owner("Harika")
	@Test(dataProvider = "getTaskValidData", groups = "nightly-build")
	public void editTaskById_POST(String relatedToType, int statusCode) {
		createTeams();
		int taskTypeId = allCrudFunctions.getTaskTypeId(albatrossURL,ThreadManager.getOwnerAlbatrossToken()).jsonPath().get("data.customizeTaskType[0].id");

		String userid = String.valueOf(adminId);
		String teamid = String.valueOf(team1Id);
		JsonPath json = function.createNewTaskWithCollaborators(baseURL, ThreadManager.getAccountApiKey(), relatedToType, userid, teamid)
				.jsonPath();
		int taskId = json.get("id");
		taskID = String.valueOf(taskId);
		String relatedTo = json.get("related_to");
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", taskID);
		String basePath = "tasks/{task}";

		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(15);
		task.setRelated_to(relatedTo);
		task.setRelated_to_type(relatedToType);
		task.setStart_date(startDate);
		task.setCollaborators(String.valueOf(accountOwnerid));
		task.setTask_type_id(taskTypeId);
		if (relatedToType.equals("contact")) {
			ArrayList<String> associatedCompanies = json.get("associated_companies");
			task.setAssociated_companies(associatedCompanies.get(0));
		}
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				task);

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();
		teamsAdded.add(team1Id);

		ArrayList<Integer> collaboratorsAdded = new ArrayList<Integer>();
		collaboratorsAdded.add(adminId);
		collaboratorsAdded.add(accountOwnerid);

		// Verify Response Code and body
		response.then().statusCode(statusCode);
		response.then().body("title", Matchers.containsString(taskTitle));
		response.then().body("related_to", Matchers.is(relatedTo));
		response.then().body("related_to_type", Matchers.is(relatedToType));
		response.then().body("collaborators[0].attendee_id", Matchers.is(adminId));
		response.then().body("collaborator_users", Matchers.containsInAnyOrder(collaboratorsAdded.toArray()));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));
		response.then().body("task_type.id", Matchers.equalTo(taskTypeId));
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void editTaskByInvalidId404_POST() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", taskID + "123459");
		String basePath = "tasks/{task}";
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(30);
		task.setStart_date(startDate);
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				task);


		// Verify Response Code and body
		response.then().statusCode(404);
		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(404));
		response.then().body("errorMessage", Matchers.is("Task doesn't exist"));
	}

	@Owner("Harika")
	@Test(dataProvider = "getTaskValidData", groups = "nightly-build")
	public void editTaskByInvalidFieldsValues422_POST(String realtedToType, int statusCode) {
		JsonPath json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), realtedToType).jsonPath();
		int taskId = json.get("id");
		String task_Id = String.valueOf(taskId);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", task_Id);

		String basePath = "tasks/{task}";

		task.setTitle(longText);
		task.setDescription(taskDescription);// showing more than 3000 not 10000
		task.setReminder(7);
		task.setRelated_to("12345");
		task.setRelated_to_type("x" + realtedToType);
		task.setStart_date("2022-018-14");

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				task);
		// Get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response Code and body
		response.then().statusCode(422);
		response.then().body("title[0]", Matchers.containsString("The title may not be greater than 100 characters."));
		response.then().body("reminder[0]", Matchers.containsString("The selected reminder is invalid."));

		response.then().body("start_date[0]", Matchers.containsString("The start date is not a valid date."));
		response.then().body("related_to_type[0]", Matchers.containsString("The selected related to type is invalid."));

		response.then().body("related_to[0]", Matchers.containsString("related to is not valid."));

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotEditTask() {
		JsonPath json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int taskId = json.get("id");
		String entitySlug = json.get("related_to");
		String task_Id = String.valueOf(taskId);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", task_Id);
		String basePath = "tasks/{task}";
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(15);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type("candidate");
		task.setStart_date(startDate);
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", null, pathParamters,
				true, task);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider
	public Object[][] getTaskValidData() {
		Object data[][] = { { "candidate", 200 }, { "company", 200 }, { "contact", 200 }, { "job", 200 },
				{ "deal", 200 } };
		return data;
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

		Response response1 = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "task edit team", userId1);
		response1.then().statusCode(200);

		Response team1 = function.getTeams(baseURL, ThreadManager.getAccountApiKey());
		team1.then().statusCode(200);
		JsonPath teamPath = team1.jsonPath();

		team1Id = teamPath.get("[0].team_id");

	}

}
