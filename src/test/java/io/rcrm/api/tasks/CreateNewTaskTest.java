package io.rcrm.api.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.javafaker.JavaFakerTask;
import io.rcrm.api.pojo.Task;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateNewTaskTest extends TestBase {

	public CreateNewTaskTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	commanFunction commanFunction = new commanFunction();

	JavaFakerTask fakerTask = new JavaFakerTask();
	JavaFakerJob jobFaker = new JavaFakerJob();
	Object accountAPIKey;
	Object albatrossTkn;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		accountAPIKey = ThreadManager.getAccountApiKey();
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getTasksWithEntitiesAndTeams", groups = "nightly-build")
	public void createNewTask(String relatedToType, String entitySlug, int teamId, int team2Id, int team3Id) {
		int taskTypeId = allCrudFunctions.getTaskTypeId(albatrossURL,albatrossTkn).jsonPath().get("data.customizeTaskType[0].id");

		String taskTitle = fakerTask.getTaskName();
		String taskDescription = fakerTask.getDescription();
		String startDate = fakerTask.getFutureDate();

		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(15);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type(relatedToType);
		task.setStart_date(startDate);
		task.setTask_type_id(taskTypeId);

		Response response = RestClient.doPost("JSON", baseURL, "tasks", accountAPIKey, null, true, task);

		JsonPath jp = response.jsonPath();

		response.then().statusCode(200);
		Assert.assertTrue(jp.getString("title").contains(taskTitle), "Title does not contain expected taskTitle");
		Assert.assertEquals(jp.getString("related_to"), entitySlug, "related_to mismatch");
		Assert.assertEquals(jp.getString("related_to_type"), relatedToType, "related_to_type mismatch");
		Assert.assertTrue(jp.getList("collaborator_teams").contains(teamId), "collaborator_teams does not contain expected teamId");
		Assert.assertEquals(jp.getInt("task_type.id"), taskTypeId, "task_type.id mismatch");

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createTaskWithDisableAutoTeamsPopulation() {
		String entitySlug = createEntityTestData("candidate");
		String taskTitle = fakerTask.getTaskName();
		String taskDescription = fakerTask.getDescription();
		String startDate = fakerTask.getFutureDate();

		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(15);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type("candidate");
		task.setStart_date(startDate);
		task.setEnable_auto_populate_teams(0);

		Response response = RestClient.doPost("JSON", baseURL, "tasks", accountAPIKey, null, true, task);

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();

		response.then().statusCode(200);
		response.then().body("title", Matchers.containsString(taskTitle));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is("candidate"));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createTaskWithEnableAutoTeamsPopulation() {
		String entitySlug = createEntityTestData("candidate");
		String taskTitle = fakerTask.getTaskName();
		String taskDescription = fakerTask.getDescription();
		String startDate = fakerTask.getFutureDate();

		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		userResponse.then().statusCode(200);
		JsonPath user = userResponse.jsonPath();
		int adminId = user.get("[1].id");
		int accountOwnerid = user.get("[0].id");

		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(15);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type("candidate");
		task.setStart_date(startDate);
		task.setOwner_id(adminId);
		task.setCreated_by(accountOwnerid);
		task.setEnable_auto_populate_teams(1);
		int teamId = createTeams("admin", "resTeamMember");

		Response response = RestClient.doPost("JSON", baseURL, "tasks", accountAPIKey, null, true, task);

		response.then().statusCode(200);
		response.then().body("title", Matchers.containsString(taskTitle));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is("candidate"));
		response.then().body("collaborator_teams", Matchers.hasItem(teamId));
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getTasksWithEntitiesAndTeams", groups = "nightly-build")
	public void createNewTaskWithCollaborators(String relatedToType, String entitySlug, int teamId, int team2Id, int team3Id) {
		String taskTitle = fakerTask.getTaskName();
		String taskDescription = fakerTask.getDescription();
		String startDate = fakerTask.getFutureDate();

		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		userResponse.then().statusCode(200);
		JsonPath user = userResponse.jsonPath();
		int adminId = user.get("[1].id");
		int accountOwnerid = user.get("[0].id");
		int resTeamMember = user.get("[2].id");

		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(15);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type(relatedToType);
		task.setStart_date(startDate);
		task.setOwner_id(accountOwnerid);
		task.setCreated_by(accountOwnerid);
		task.setUpdated_by(accountOwnerid);
		task.setCollaborators(String.valueOf(adminId) + "," + String.valueOf(resTeamMember));
		task.setCollaborator_team_ids(String.valueOf(team2Id) + "," + String.valueOf(team3Id));
		task.setEnable_auto_populate_teams(1);

		ArrayList<Integer> teamsAdded = new ArrayList<Integer>();
		teamsAdded.add(team2Id);
		teamsAdded.add(team3Id);

		ArrayList<Integer> collaboratorsAdded = new ArrayList<Integer>();
		collaboratorsAdded.add(adminId);
		collaboratorsAdded.add(resTeamMember);

		Response response = RestClient.doPost("JSON", baseURL, "tasks", accountAPIKey, null, true, task);

		response.then().statusCode(200);
		response.then().body("title", Matchers.containsString(taskTitle));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(relatedToType));
		response.then().body("collaborators[0].attendee_id", Matchers.is(adminId));
		response.then().body("collaborators[1].attendee_id", Matchers.is(resTeamMember));
		response.then().body("collaborator_users", Matchers.containsInAnyOrder(collaboratorsAdded.toArray()));
		response.then().body("collaborator_teams", Matchers.containsInAnyOrder(teamsAdded.toArray()));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createNewTaskWithInvalidCollaborators() {
		String entitySlug = createEntityTestData("candidate");
		String taskTitle = fakerTask.getTaskName();
		String taskDescription = fakerTask.getDescription();
		String startDate = fakerTask.getFutureDate();

		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		userResponse.then().statusCode(200);
		JsonPath user = userResponse.jsonPath();
		int adminId = user.get("[1].id");
		int accountOwnerid = user.get("[0].id");

		int team2Id = createTeams("admin", "resTeamMember");

		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(15);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type("candidate");
		task.setStart_date(startDate);
		task.setOwner_id(accountOwnerid);
		task.setCreated_by(accountOwnerid);
		task.setUpdated_by(accountOwnerid);
		task.setCollaborators("1234," + String.valueOf(adminId));
		task.setCollaborator_team_ids("1234," + String.valueOf(team2Id));
		task.setEnable_auto_populate_teams(1);

		Response response = RestClient.doPost("JSON", baseURL, "tasks", accountAPIKey, null, true, task);

		response.then().statusCode(422);
		response.then().body("collaborators[0]", Matchers.is("collaborators ids are not valid"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createNewTaskWithInvalidCollaboratorTeams() {
		String entitySlug = createEntityTestData("candidate");
		String taskTitle = fakerTask.getTaskName();
		String taskDescription = fakerTask.getDescription();
		String startDate = fakerTask.getFutureDate();

		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		userResponse.then().statusCode(200);
		JsonPath user = userResponse.jsonPath();
		int adminId = user.get("[1].id");
		int accountOwnerid = user.get("[0].id");

		int team2Id = createTeams("admin", "resTeamMember");

		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(15);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type("candidate");
		task.setStart_date(startDate);
		task.setOwner_id(accountOwnerid);
		task.setCreated_by(accountOwnerid);
		task.setUpdated_by(accountOwnerid);
		task.setCollaborators(String.valueOf(adminId));
		task.setCollaborator_team_ids("1234," + String.valueOf(team2Id));
		task.setEnable_auto_populate_teams(1);

		Response response = RestClient.doPost("JSON", baseURL, "tasks", accountAPIKey, null, true, task);

		response.then().statusCode(422);
		response.then().body("collaborator_team_ids[0]", Matchers.is("Team ids are not valid"));

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build") // (invocationCount=1000, threadPoolSize = 2)
	public void createNewTaskPastDate() {

		JsonPath json;
		String entitySlug = "";
		String taskTitle = fakerTask.getTaskName();
		String taskDescription = fakerTask.getDescription();
		String pastDate = fakerTask.getPastDate();

		json = function.createNewCandidateWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		entitySlug = json.get("slug");
		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(15);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type("candidate");
		task.setStart_date(pastDate);

		Response response = RestClient.doPost("JSON", baseURL, "tasks", accountAPIKey, null, true, task);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int taskId = jp.get("id");
		// 2295174

		response.then().body("title", Matchers.containsString(taskTitle));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is("candidate"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getTasksReminderValues", groups = "nightly-build")
	public void createNewTaskWithAllReminder(int reminder, String statusCode) {
		Task task = new Task();
		String taskDescription = fakerTask.getDescription();
		String startDate = fakerTask.getFutureDate();

		task.setTitle(fakerTask.getTaskName());
		task.setDescription(taskDescription);
		task.setReminder(reminder);
		task.setStart_date(startDate);
		// task.setRelated_to("326057");
		// task.setRelated_to_type("job");

		Response response = RestClient.doPost("JSON", baseURL, "tasks", accountAPIKey, null, true, task);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int taskId = jp.get("id");
		// 2295174

		// response.then().body("title", Matchers.containsString(taskTitle));
		// response.then().body("reminder", Matchers.is(reminder));

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void createNewTaskWithEmptyBody() {
		Task task = new Task();
		Response response = RestClient.doPost("JSON", baseURL, "tasks", accountAPIKey, null, true, task);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		response.then().statusCode(422);
		response.then().body("title[0]", Matchers.is("The title field is required."));
		response.then().body("start_date[0]", Matchers.is("The start date field is required."));
	}

	@Owner("Harika")
	@Test(dataProvider = "getTasksInValidData", groups = "nightly-build")
	public void createNewTaskWithInvalidMandatoryValue(String taskTitle_d, int reminder_d, String taskDate_d) {
		Task task = new Task();

		task.setTitle(taskTitle_d);
		task.setReminder(reminder_d);
		task.setStart_date(taskDate_d);

		Response response = RestClient.doPost("JSON", baseURL, "tasks", accountAPIKey, null, true, task);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		response.then().statusCode(422);
		response.then().body("title[0]", Matchers.is("The title must be at least 2 characters."));
		response.then().body("reminder[0]", Matchers.is("The selected reminder is invalid."));
		response.then().body("start_date[0]", Matchers.is("The start date is not a valid date."));

	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getTaskIdAndRelatedType", groups = "nightly-build")
	public void createNewTaskWithLimitExceeds(String task_Id, String relatedToType) {
		String longText = jobFaker.getJobDescriptionText() + jobFaker.getNoteForCandidate();
		String taskDescription = fakerTask.getDescription();

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("task", task_Id);

		String basePath = "tasks/{task}";

		Task task = new Task();
		task.setTitle(longText);
		task.setDescription(taskDescription);
		task.setRelated_to("12345");
		task.setRelated_to_type("x" + relatedToType);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, accountAPIKey, null, pathParamters, true,
				task);
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422, "Response Code must be 422");
		Assert.assertTrue(jp.getString("title[0]").contains("The title may not be greater than 100 characters."),
				"Expected error message for title not found. Actual: " + jp.getString("title[0]"));
		Assert.assertTrue(jp.getString("related_to_type[0]").contains("The selected related to type is invalid."),
				"Expected error message for related_to_type not found. Actual: " + jp.getString("related_to_type[0]"));
		Assert.assertTrue(jp.getString("related_to[0]").contains("related to is not valid."),
				"Expected error message for related_to not found. Actual: " + jp.getString("related_to[0]"));

	}

	@Owner("Ajendra Singh")
	@Test(dataProvider = "getTasksValidDataWithAssociates", groups = "nightly-build")
	public void createNewTaskWithAssociates(String realtedToType, String entitySlug, String statusCode,
			String associatedCandidatesSlugs, String associatedCompaniesSlugs, String associatedContactsSlugs,
			String associatedJobsSlugs, String associatedDealsSlugs) {
		String taskTitle = fakerTask.getTaskName();
		String taskDescription = fakerTask.getDescription();
		String startDate = fakerTask.getFutureDate();

		Task task = new Task();

		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(15);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type(realtedToType);
		task.setStart_date(startDate);

		task.setAssociated_candidates(associatedCandidatesSlugs);
		task.setAssociated_companies(associatedCompaniesSlugs);
		task.setAssociated_contacts(associatedContactsSlugs);
		task.setAssociated_deals(associatedDealsSlugs);
		task.setAssociated_jobs(associatedJobsSlugs);

		Response response = RestClient.doPost("JSON", baseURL, "tasks", accountAPIKey, null, true, task);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		if (statusCode == "200") {
			response.then().body("title", Matchers.containsString(taskTitle));
			response.then().body("related_to", Matchers.is(entitySlug));
			response.then().body("related_to_type", Matchers.is(realtedToType));

		} else if (statusCode == "422") {

			response.then().body("associated_companies[0]", Matchers.containsString("Invalid associated companies"));
			response.then().body("associated_contacts[0]", Matchers.containsString("Invalid associated contacts"));
			response.then().body("associated_jobs[0]", Matchers.containsString("Invalid associated jobs"));
			response.then().body("associated_deals[0]", Matchers.containsString("nvalid associated deals"));
		}

	}

	@Owner("Harika")
	@Test(dataProvider = "getTasksValidData", groups = "nightly-build")
	public void unauthorizedUserCannotCreateNewTask(String realtedToType) {
		String taskTitle = fakerTask.getTaskName();
		String taskDescription = fakerTask.getDescription();
		String startDate = fakerTask.getFutureDate();

		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(15);
		task.setRelated_to_type(realtedToType);
		task.setStart_date(startDate);

		Response response = RestClient.doPost("JSON", baseURL, "tasks", accountAPIKey+"12345", null, true, task);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider(parallel = true)
	public Object[][] getTasksWithEntitiesAndTeams() {
		int teamId = createTeams("accountOwner", "teamMember");
		int team2Id = createTeams("admin", "resTeamMember");
		int team3Id = createTeams("teamMember", "resTeamMember");

		return new Object[][] {
				{ "candidate", createEntityTestData("candidate"), teamId, team2Id, team3Id },
				{ "contact", createEntityTestData("contact"), teamId, team2Id, team3Id },
				{ "company", createEntityTestData("company"), teamId, team2Id, team3Id },
				{ "job", createEntityTestData("job"), teamId, team2Id, team3Id },
				{ "deal", createEntityTestData("deal"), teamId, team2Id, team3Id },
		};
	}

	@DataProvider(parallel = true)
	public Object[][] getTaskIdAndRelatedType() {
		String[] relatedTypes = { "candidate", "contact", "company", "job", "deal" };
		ExecutorService executor = Executors.newFixedThreadPool(5);
		List<Future<Object[]>> futures = new ArrayList<>();

		for (String relatedType : relatedTypes) {
			futures.add(executor.submit(() -> {
				Response response = function.createNewTask(baseURL, accountAPIKey, relatedType);
				Assert.assertEquals(response.getStatusCode(), 200, "Status Code must be 200!");
				return new Object[]{ String.valueOf(response.jsonPath().getInt("id")), relatedType };
			}));
		}

		List<Object[]> dataList = new ArrayList<>();
		for (Future<Object[]> future : futures) {
			try {
				dataList.add(future.get());
			} catch (Exception e) {
				Assert.fail("Failed to create task with error message: "+e.getMessage());
			}
		}

		executor.shutdown();
		return dataList.toArray(new Object[0][]);
	}

	@DataProvider(parallel = true)
	public Object[][] getTasksValidData() {
		Object data[][] = { { "candidate" }, { "contact" }, { "company" }, { "job" }, { "deal" } };
		return data;
	}

	@DataProvider(parallel = true)
	public Object[][] getTasksReminderValues() {
		Object data[][] = { { -1, "200" }, { 0, "200" }, { 15, "200" }, { 30, "200" }, { 60, "200" }, { 1440, "200" } };
		return data;
	}

	@DataProvider(parallel = true)
	public Object[][] getTasksInValidData() {
		Object data[][] = { { "a", -12, "032322099934" }, { "1", -2, "2x2323123" } };
		return data;
	}

	@DataProvider(parallel = true)
	public Object[][] getTasksValidDataWithAssociates() {

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");

		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String companySlug = jsonCompany.get("slug");

		JsonPath jsonContact = function.createNewContact_POST(baseURL, accountAPIKey, companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");

		JsonPath jsonJob = function.createNewJob(baseURL, accountAPIKey, companySlug, contactSlug).jsonPath();
		String jobSlug = jsonJob.getString("slug");

		JsonPath jsonDeal = function
				.createNewDealWithMandatoryFields(baseURL, accountAPIKey, companySlug, contactSlug, jobSlug).jsonPath();

		String deaslEntitySlug = jsonDeal.get("slug");

		ExecutorService executor = Executors.newFixedThreadPool(5);
		ConcurrentHashMap<String, String> results = new ConcurrentHashMap<>();

		executor.submit(() -> {
			JsonPath jsonGetAllCompanies = function.getAllCompanies_GET(baseURL, accountAPIKey).jsonPath();
			List<String> companyslugs = jsonGetAllCompanies.get("data.slug");
			results.put("associatedCompaniesSlugs", StringUtils.join(companyslugs, ","));
		});

		executor.submit(() -> {
			JsonPath jsonGetAllCandidates = function.getAllCandidates(baseURL, accountAPIKey).jsonPath();
			List<String> candidateslugs = jsonGetAllCandidates.get("data.slug");
			results.put("associatedCandidatesSlugs", StringUtils.join(candidateslugs, ","));
		});

		executor.submit(() -> {
			JsonPath jsonGetAllContacts = function.getAllContacts_GET(baseURL, accountAPIKey).jsonPath();
			List<String> contactslugs = jsonGetAllContacts.get("data.slug");
			results.put("associatedContactsSlugs", StringUtils.join(contactslugs, ","));
		});

		executor.submit(() -> {
			JsonPath jsonGetAllJobs = function.getAllJobs_GET(baseURL, accountAPIKey).jsonPath();
			List<String> jobsSlugs = jsonGetAllJobs.get("data.slug");
			results.put("associatedJobsSlugs", StringUtils.join(jobsSlugs, ","));
		});

		executor.submit(() -> {
			JsonPath jsonGetAllDeals = function.getAllDealsGET(baseURL, accountAPIKey).jsonPath();
			List<String> DealSlugs = jsonGetAllDeals.get("data.slug");
			results.put("associatedDealsSlugs", StringUtils.join(DealSlugs, ","));
		});

		executor.shutdown();
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String associatedCompaniesSlugs = results.get("associatedCompaniesSlugs");
		String associatedCandidatesSlugs = results.get("associatedCandidatesSlugs");
		String associatedContactsSlugs = results.get("associatedContactsSlugs");
		String associatedJobsSlugs = results.get("associatedJobsSlugs");
		String associatedDealsSlugs = results.get("associatedDealsSlugs");

		Object data[][] = {
				{ "candidate", candidateEntitySlug, "200", "", associatedCompaniesSlugs, associatedContactsSlugs,
						associatedJobsSlugs, associatedDealsSlugs },
				{ "candidate", candidateEntitySlug, "200", "", associatedCompaniesSlugs, associatedContactsSlugs,
						associatedJobsSlugs, "" },
				{ "candidate", candidateEntitySlug, "200", "", associatedCompaniesSlugs, associatedContactsSlugs, "",
						"" },
				{ "candidate", candidateEntitySlug, "200", "", associatedCompaniesSlugs, "", "", "" },
				{ "candidate", candidateEntitySlug, "200", "", "", "", "", "" },
				{ "candidate", candidateEntitySlug, "200", "", "", associatedContactsSlugs, associatedJobsSlugs,
						associatedDealsSlugs },
				{ "candidate", candidateEntitySlug, "200", "", "", "", associatedJobsSlugs, associatedDealsSlugs },
				{ "candidate", candidateEntitySlug, "200", "", "", "", "", associatedDealsSlugs },

				{ "company", companySlug, "200", associatedCandidatesSlugs, "", associatedContactsSlugs,
						associatedJobsSlugs, associatedDealsSlugs },
				{ "company", companySlug, "200", associatedCandidatesSlugs, "", associatedContactsSlugs,
						associatedJobsSlugs, "" },
				{ "company", companySlug, "200", associatedCandidatesSlugs, "", associatedContactsSlugs, "", "" },
				{ "company", companySlug, "200", associatedCandidatesSlugs, "", "", "", "" },
				{ "company", companySlug, "200", "", "", "", "", "" },
				{ "company", companySlug, "200", "", "", associatedContactsSlugs, associatedJobsSlugs,
						associatedDealsSlugs },
				{ "company", companySlug, "200", "", "", "", associatedJobsSlugs, associatedDealsSlugs },
				{ "company", companySlug, "200", "", "", "", "", associatedDealsSlugs },

				{ "contact", contactSlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs, "",
						associatedJobsSlugs, associatedDealsSlugs },
				{ "contact", contactSlug, "200", "", associatedCompaniesSlugs, "", associatedJobsSlugs,
						associatedDealsSlugs },
				{ "contact", contactSlug, "200", "", "", "", associatedJobsSlugs, associatedDealsSlugs },
				{ "contact", contactSlug, "200", "", "", "", "", associatedDealsSlugs },
				{ "contact", contactSlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs, "",
						associatedJobsSlugs, "" },
				{ "contact", contactSlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs, "", "", "" },
				{ "contact", contactSlug, "200", associatedCandidatesSlugs, "", "", "", "" },

				{ "job", jobSlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs, associatedContactsSlugs,
						"", "" },
				{ "job", jobSlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs, "", "", "" },
				{ "job", jobSlug, "200", associatedCandidatesSlugs, "", "", "", "" },
				{ "job", jobSlug, "200", "", "", "", "", "" },
				{ "job", jobSlug, "200", "", associatedCompaniesSlugs, associatedContactsSlugs, "",
						associatedDealsSlugs },
				{ "job", jobSlug, "200", "", "", associatedContactsSlugs, "", associatedDealsSlugs },
				{ "job", jobSlug, "200", "", "", "", "", associatedDealsSlugs },
				{ "job", jobSlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs, associatedContactsSlugs,
						"", associatedDealsSlugs },

				{ "deal", deaslEntitySlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs,
						associatedContactsSlugs, associatedJobsSlugs, "" },
				{ "deal", deaslEntitySlug, "200", "", associatedCompaniesSlugs, associatedContactsSlugs,
						associatedJobsSlugs, "" },
				{ "deal", deaslEntitySlug, "200", "", "", associatedContactsSlugs, associatedJobsSlugs, "" },

				{ "deal", deaslEntitySlug, "200", "", "", "", associatedJobsSlugs, "" },
				{ "deal", deaslEntitySlug, "200", "", "", "", "", "" },
				{ "deal", deaslEntitySlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs,
						associatedContactsSlugs, "", "" },
				{ "deal", deaslEntitySlug, "200", associatedCandidatesSlugs, associatedCompaniesSlugs, "", "", "" },
				{ "deal", deaslEntitySlug, "200", associatedCandidatesSlugs, "", "", "", "" },

				{ "candidate", candidateEntitySlug, "422", "", associatedCompaniesSlugs + "1",
						associatedContactsSlugs + "1", associatedJobsSlugs + "1", associatedDealsSlugs + "1" }

		};
		return data;
	}

	public int createTeams(String userRole1, String userRole2) {
		Response response = function.getUsers(baseURL, accountAPIKey);
		response.then().statusCode(200);
		JsonPath user = response.jsonPath();
		HashMap<String, Integer> userIdMap = new HashMap<String, Integer>();
		userIdMap.put("accountOwner", user.get("[0].id"));
		userIdMap.put("admin", user.get("[1].id"));
		userIdMap.put("resTeamMember", user.get("[2].id"));
		userIdMap.put("teamMember", user.get("[3].id"));

		String team1 = "team"+ RandomStringUtils.randomAlphabetic(5);

		ArrayList<String> userId1 = new ArrayList<String>();
		userId1.add(String.valueOf(userIdMap.get(userRole1)));
		userId1.add(String.valueOf(userIdMap.get(userRole2)));

		Response response1 = allCrudFunctions.createTeam(albatrossURL, albatrossTkn, team1, userId1);
		response1.then().statusCode(200);

		Response teamCreated = function.getTeams(baseURL, accountAPIKey);
		teamCreated.then().statusCode(200);
		JsonPath teamPath = teamCreated.jsonPath();

		int noOfTeams = teamPath.getInt("$.size()");
		int teamId = 0;
		for (int i = 0; i < noOfTeams; i++) {
			String teamName = teamPath.get("[" + i + "].team_name");
			if (teamName.equals(team1)) {
				teamId= teamPath.get("[" + i + "].team_id");
				break;
			}
		}
		return teamId;
	}

	public String createEntityTestData(String relatedToType) {
		JsonPath json;
		String entitySlug="";
		if (relatedToType == "candidate") {
			json = function.createNewCandidateWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
			entitySlug = json.get("slug");
		}

		if (relatedToType == "company") {
			json = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
			entitySlug = json.get("slug");
		}
		if (relatedToType == "contact") {

			JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
			;
			String companySlug = jsonCompany.get("slug");
			json = function.createNewContact_POST(baseURL, accountAPIKey, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (relatedToType == "job") {
			JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = function.createNewContact_POST(baseURL, accountAPIKey, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			json = function.createNewJob(baseURL, accountAPIKey, companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (relatedToType == "deal") {
			JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
			String companySlug = jsonCompany.get("slug");

			JsonPath jsonContact = function.createNewContact_POST(baseURL, accountAPIKey, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");

			JsonPath jsonJob = function.createNewJob(baseURL, accountAPIKey, companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.getString("slug");

			JsonPath jsonDeal = function
					.createNewDealWithMandatoryFields(baseURL, accountAPIKey, companySlug, contactSlug, jobSlug)
					.jsonPath();

			entitySlug = jsonDeal.get("slug");
		}
		return entitySlug;
	}

}
