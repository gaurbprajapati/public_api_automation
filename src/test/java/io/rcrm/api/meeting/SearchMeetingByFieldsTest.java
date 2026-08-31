package io.rcrm.api.meeting;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.DateUtil;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
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
public class SearchMeetingByFieldsTest extends TestBase {

	public SearchMeetingByFieldsTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	commanFunction function = new commanFunction();
	int meetingId;
	JsonPath json;
	
	String apiAuthToken;

	@BeforeClass(alwaysRun = true)		public void setUp() {
			apiAuthToken = ThreadManager.getAccountApiKey();
		}

	@Owner("Harika")
	@Test(dataProvider = "getMeetingValidData", groups = "nightly-build")
	public void searchMeetingByFields_Test(String relatedToType, int statusCode) throws ParseException {
		String meetingID = "";

		String todayDateString = DateUtil.getTodayDateString("dd-MM-yyyy");
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");

		Date expectedYesterdayDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);
		Date expectedTomorrowDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		HashMap<Integer, String> fieldMap = new HashMap<Integer, String>();
		fieldMap.put(4, yesterdayDateString);
		fieldMap.put(5, todayDateString);
		JsonPath json = function.createNewMeetingsWithSpecifiedFields(baseURL, ThreadManager.getAccountApiKey(), relatedToType, fieldMap).jsonPath();

		int meetingID_int = json.get("id");
		String entitySlug = json.get("related_to");
		String title = json.get("title");

		meetingID = String.valueOf(meetingID_int);

		Map<String, String> queryParameters = new HashMap<String, String>();

		queryParameters.put("related_to", entitySlug);
		queryParameters.put("related_to_type", relatedToType);
		queryParameters.put("title", title);
		queryParameters.put("starting_from", yesterdayDateString);
		queryParameters.put("starting_to", tomorrowDateString);
		queryParameters.put("created_from", yesterdayDateString);
		queryParameters.put("created_to", tomorrowDateString);
		queryParameters.put("updated_from", yesterdayDateString);
		queryParameters.put("updated_to", tomorrowDateString);

		String basePath = "meetings/search";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.related_to_type[0]", Matchers.is(relatedToType));
		response.then().body("data.related_to[0]", Matchers.is(entitySlug));
		response.then().body("data.title[0]", Matchers.is(title));

		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		String jpDate2 = jp.get("data[0].updated_on");
		String jpDate3 = jp.get("data[0].start_date");
		Date actualCreatedDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Date actualUpdatedDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate2);
		Date actualStartedDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate3);

		Assert.assertTrue(actualCreatedDate.after(expectedYesterdayDate), "Actual date is not after the expected date, expected : " + expectedYesterdayDate
				+ " but found : " + actualCreatedDate);
		Assert.assertTrue(actualCreatedDate.before(expectedTomorrowDate), "Actual date is not before the expected date, expected : " + expectedTomorrowDate
				+ " but found : " + actualCreatedDate);

		Assert.assertTrue(actualUpdatedDate.after(expectedYesterdayDate), "Actual date is not after the expected date, expected : " + expectedYesterdayDate
				+ " but found : " + actualUpdatedDate);
		Assert.assertTrue(actualUpdatedDate.before(expectedTomorrowDate), "Actual date is not before the expected date, expected : " + expectedTomorrowDate
				+ " but found : " + actualUpdatedDate);

		Assert.assertTrue(actualStartedDate.before(expectedTomorrowDate), "Actual date is not before the expected date, expected : " + expectedTomorrowDate
				+ " but found : " + actualStartedDate);
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchMeetingByInvalidFields_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("title", "Invalid task title x001x");

		String basePath = "meetings/search";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, null, true);

		response.then().statusCode(200);
		response.then().body(Matchers.is("[]"));

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotSearchMeetingByFields_Test() {
		JsonPath json;
		String meetingID = "";

		json = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int meetingID_int = json.get("id");
		String entitySlug = json.get("related_to");

		meetingID = String.valueOf(meetingID_int);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("related_to", entitySlug);
		queryParameters.put("related_to_type", "candidate");

		String basePath = "meetings/search";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", queryParameters, null,
				true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void searchMeetingByCreatedFrom() throws ParseException {
		if(json == null) json = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();

		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("created_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "meetings/search", ThreadManager.getAccountApiKey(), queryParameters, null,
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
	public void searchMeetingByCreatedTo() throws ParseException {
		if(json == null) json = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("created_to", tomorrowDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "meetings/search", ThreadManager.getAccountApiKey(), queryParameters, null,
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
	@Test
	public void searchMeetingByUpdatedFrom() throws ParseException {
		if(json == null) json = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("updated_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "meetings/search", ThreadManager.getAccountApiKey(), queryParameters, null,
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
	@Test
	public void searchMeetingByUpdatedTo() throws ParseException {
		if(json == null) json = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("updated_to", tomorrowDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "meetings/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void searchMeetingByStartingFrom() throws ParseException {
		String todayDateString = DateUtil.getTodayDateString("dd-MM-yyyy");
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		HashMap<Integer, String> fieldMap = new HashMap<Integer, String>();
		fieldMap.put(4, todayDateString);
		fieldMap.put(5, tomorrowDateString);
		if(json == null) json = function.createNewMeetingsWithSpecifiedFields(baseURL, ThreadManager.getAccountApiKey(), "candidate", fieldMap).jsonPath();

		Map<String, String> queryParameters = new HashMap<String, String>();
		String nDateString = DateUtil.getNDateFromTodayDateString(-2, "dd-MM-yyyy");
		queryParameters.put("starting_from", nDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(nDateString);

		Response response = RestClient.doGet("JSON", baseURL, "meetings/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].start_date");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchMeetingByStartingTo() throws ParseException {
		String todayDateString = DateUtil.getTodayDateString("dd-MM-yyyy");
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		HashMap<Integer, String> fieldMap = new HashMap<Integer, String>();
		fieldMap.put(4, yesterdayDateString);
		fieldMap.put(5, todayDateString);
		if(json == null) json = function.createNewMeetingsWithSpecifiedFields(baseURL, ThreadManager.getAccountApiKey(), "candidate", fieldMap).jsonPath();

		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("starting_to", tomorrowDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "meetings/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].start_date");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchMeetingByOwnerParameters_Test() {
		// create meeting using public api
		JsonPath json = function.createNewMeetings(baseURL, apiAuthToken, "").jsonPath();
		int meetingId = json.get("id");
		String meetingTitle = json.get("title");

		// get owner data from users end point
		Response userResponse = function.getUsers(baseURL, apiAuthToken);
		Assert.assertEquals(userResponse.getStatusCode(), 200);
		JsonPath user = userResponse.jsonPath();
		int id = user.get("[0].id");
		String ownerId = String.valueOf(id);
		String ownerName = user.get("[0].first_name");
		String ownerEmail = user.get("[0].email");

		// search meeting by owner parameters
		String[] ownerParams = { "owner_id", "owner_name", "owner_email" };
		String[] ownerValues = { ownerId, ownerName, ownerEmail };

		for (int i = 0; i < ownerParams.length; i++) {
			Map<String, String> queryParameters = new HashMap<String, String>();
			queryParameters.put(ownerParams[i], ownerValues[i]);

			Response response = RestClient.doGet("JSON", baseURL, "meetings/search", apiAuthToken, queryParameters,
					null, true);

			Assert.assertEquals(response.getStatusCode(), 200);
			JsonPath jsonPath = response.jsonPath();

			Assert.assertEquals(jsonPath.getInt("data[0].owner"), Integer.parseInt(ownerId),
					"Failed at " + ownerParams[i]);
			Assert.assertEquals(jsonPath.getInt("data[0].id"), meetingId, "Failed at " + ownerParams[i]);
			Assert.assertEquals(jsonPath.get("data[0].title"), meetingTitle, "Failed at " + ownerParams[i]);
		}
	}

	@DataProvider
	public Object[][] getMeetingValidData() {

		Object data[][] = { { "candidate", 200 }, { "company", 200 }, { "contact", 200 }, { "job", 200 },
				{ "deal", 200 } };
		return data;
	}
}
