package io.rcrm.api.tasks;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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
public class SearchTaskByFieldsTest extends TestBase {

	public SearchTaskByFieldsTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	commanFunction function = new commanFunction();
	String taskTitle = "";
	JsonPath json;
	
	String accountAPIKey;

	@BeforeClass(alwaysRun = true)		public void setUp() {
		accountAPIKey = ThreadManager.getAccountApiKey();
		}

	@Owner("Ajendra Singh")
	@Test(dataProvider = "getTaskValidData", groups = "nightly-build")
	public void searchTaskByTitle_GET(String relatedToType,int statusCode) {
		  JsonPath json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(),relatedToType).jsonPath();
		   taskTitle = json.get("title");
		   String realtedTo = json.get("related_to");
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("title", taskTitle);
		Response response = RestClient.doGet("JSON", baseURL, "tasks/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		response.then().statusCode(statusCode);

		String responseBody = response.getBody().asString();

		response.then().body("data.title[0]", Matchers.is(taskTitle));
		response.then().body("data.related_to_type[0]", Matchers.is(relatedToType));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("task_type"));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchTaskByInvalidTitle_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("title", taskTitle+ "Invalid task Name");
		Response response = RestClient.doGet("JSON", baseURL, "tasks/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		response.then().statusCode(200);
		response.then().body(Matchers.is("[]"));

	}
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchTaskByAllFields_GET() throws ParseException {
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		Date expectedYesterdayDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);
		Date expectedTomorrowDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		JsonPath json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(),"candidate").jsonPath();
		String taskTitle = json.get("title");
		String realtedTo = json.get("related_to");
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("title", taskTitle);
		queryParameters.put("related_to", realtedTo);
		queryParameters.put("related_to_type", "candidate");
		queryParameters.put("created_from", yesterdayDateString);
		queryParameters.put("created_to", tomorrowDateString);
		queryParameters.put("updated_from", yesterdayDateString);
		queryParameters.put("updated_to", tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "tasks/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		response.then().statusCode(200);

		String responseBody = response.getBody().asString();

		response.then().body("data.title[0]", Matchers.is(taskTitle));
		response.then().body("data.related_to[0]", Matchers.is(realtedTo));
		response.then().body("data.related_to_type[0]", Matchers.is("candidate"));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborators"));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_users"));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_teams"));

		JsonPath jp = response.jsonPath();
		String jpDate1 = jp.get("data[0].created_on");
		String jpDate2 = jp.get("data[0].updated_on");
		Date actualDate1 = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate1);
		Date actualDate2 = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate2);

		Assert.assertTrue(actualDate1.after(expectedYesterdayDate), "Actual date is not after the expected date, expected : " + expectedYesterdayDate
				+ " but found : " + actualDate1);
		Assert.assertTrue(actualDate1.before(expectedTomorrowDate), "Actual date is not before the expected date, expected : " + expectedTomorrowDate
				+ " but found : " + actualDate1);
		Assert.assertTrue(actualDate2.after(expectedYesterdayDate), "Actual date is not after the expected date, expected : " + expectedYesterdayDate
				+ " but found : " + actualDate2);
		Assert.assertTrue(actualDate2.before(expectedTomorrowDate), "Actual date is not before the expected date, expected : " + expectedTomorrowDate
				+ " but found : " + actualDate2);
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotSearchTaskByFields() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("title", taskTitle);

		Response response = RestClient.doGet("JSON", baseURL, "tasks/search", ThreadManager.getAccountApiKey()+"12345", queryParameters, null,true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void searchTaskByCreatedFrom() throws ParseException {
		if(json == null) json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(),"candidate").jsonPath();

		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("created_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "tasks/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchTaskByCreatedTo() throws ParseException {
		if(json == null) json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(),"candidate").jsonPath();
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("created_to", DateUtil.getTomorrowDateString("dd-MM-yyyy"));
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "tasks/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void searchTaskByUpdatedFrom() throws ParseException {
		if(json == null) json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(),"candidate").jsonPath();
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("updated_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "tasks/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchTaskByUpdatedTo() throws ParseException {
		if(json == null) json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(),"candidate").jsonPath();
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("updated_to", DateUtil.getTomorrowDateString("dd-MM-yyyy"));
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "tasks/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchTaskByOwnerParameters_Test() {
		// create task using public api
		JsonPath json = function.createNewTask(baseURL, accountAPIKey, "").jsonPath();
		int taskId = json.get("id");
		String taskTitle = json.get("title");

		// get owner data from users end point
		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		Assert.assertEquals(userResponse.getStatusCode(), 200);
		JsonPath user = userResponse.jsonPath();
		int id = user.get("[0].id");
		String ownerId = String.valueOf(id);
		String ownerName = user.get("[0].first_name");
		String ownerEmail = user.get("[0].email");

		// search task by owner parameters
		String[] ownerParams = { "owner_id", "owner_name", "owner_email" };
		String[] ownerValues = { ownerId, ownerName, ownerEmail };

		for (int i = 0; i < ownerParams.length; i++) {
			Map<String, String> queryParameters = new HashMap<String, String>();
			queryParameters.put(ownerParams[i], ownerValues[i]);

			Response response = RestClient.doGet("JSON", baseURL, "tasks/search", accountAPIKey, queryParameters, null,
					true);

			Assert.assertEquals(response.getStatusCode(), 200);
			JsonPath jsonPath = response.jsonPath();

			Assert.assertEquals(jsonPath.getInt("data[0].owner"), Integer.parseInt(ownerId),
					"Failed at " + ownerParams[i]);
			Assert.assertEquals(jsonPath.getInt("data[0].id"), taskId, "Failed at " + ownerParams[i]);
			Assert.assertEquals(jsonPath.get("data[0].title"), taskTitle, "Failed at " + ownerParams[i]);
		}
	}

	@DataProvider
	public Object[][] getTaskValidData() {

		Object data[][] = { { "candidate", 200 }, { "company",200 },{ "contact",200 },
				 { "job",200 },{ "deal",200 } };
		return data;
	}

}
