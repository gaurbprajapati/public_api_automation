package io.rcrm.api.calllogs;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.DateUtil;
import org.hamcrest.Matchers;
import org.testng.Assert;
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
public class SearchCallLogByFieldsTest extends TestBase {

	public SearchCallLogByFieldsTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	commanFunction function = new commanFunction();
	String callType = "";

	JsonPath json;

	@Owner("Ajendra Singh")
	@Test(dataProvider = "getEntityValidData", groups = "nightly-build")
	public void searchCallLogByAllFields_GET(String relatedToType,int statusCode) throws ParseException {
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		Date expectedYesterdayDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);
		Date expectedTomorrowDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		HashMap<Integer, String> fieldsMap = new HashMap<Integer, String>();
		fieldsMap.put(0, relatedToType);
		fieldsMap.put(1, DateUtil.getTodayDateString());
		JsonPath json = function.createNewCallLogWithSpecificFields(baseURL, ThreadManager.getAccountApiKey(), fieldsMap).jsonPath();
		callType = json.get("call_type");
		String realtedTo = json.get("related_to");
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("call_type", callType);
		queryParameters.put("related_to", realtedTo);
		queryParameters.put("related_to_type", relatedToType);
		queryParameters.put("updated_to", tomorrowDateString);
		queryParameters.put("updated_from", yesterdayDateString);
		queryParameters.put("starting_to", tomorrowDateString);
		queryParameters.put("starting_from", yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "call-logs/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		response.then().statusCode(statusCode);

		response.then().body("data.call_type[0]", Matchers.is(callType));
		response.then().body("data.related_to[0]", Matchers.is(realtedTo));
		response.then().body("data.related_to_type[0]", Matchers.is(relatedToType));

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

	@Owner("Harika")
	@Test(dataProvider = "getEntityValidData", groups = "nightly-build")
	public void searchCallLogByCallType_GET(String relatedToType,int statusCode) {
		JsonPath json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(),relatedToType).jsonPath();
		callType = json.get("call_type");
		String realtedTo = json.get("related_to");
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("call_type", callType);
		queryParameters.put("related_to", realtedTo);
		queryParameters.put("related_to_type", relatedToType);

		Response response = RestClient.doGet("JSON", baseURL, "call-logs/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		response.then().statusCode(statusCode);

		response.then().body("data.call_type[0]", Matchers.is(callType));
		response.then().body("data.related_to[0]", Matchers.is(realtedTo));
		response.then().body("data.related_to_type[0]", Matchers.is(relatedToType));
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void searchTaskByInvalidCallType_GET() {
		  JsonPath json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(),"candidate").jsonPath();
		 String  callType = json.get("call_type");
		  String realtedTo = json.get("related_to");
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("call_type", callType + " Invalid call Type");
		queryParameters.put("related_to",realtedTo);
		queryParameters.put("related_to_type", "candidate");
		Response response = RestClient.doGet("JSON", baseURL, "call-logs/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		response.then().statusCode(200);
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotSearchCallLogByFields() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("call_type", callType);

		Response response = RestClient.doGet("JSON", baseURL, "call-logs/search", ThreadManager.getAccountApiKey()+"12345", queryParameters, null,true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchCallLogByStartingFrom() throws ParseException {
		HashMap<Integer, String> fieldsMap = new HashMap<Integer, String>();
		fieldsMap.put(0, "candidate");
		fieldsMap.put(1, DateUtil.getTodayDateString());
		if(json == null) json = function.createNewCallLogWithSpecificFields(baseURL, ThreadManager.getAccountApiKey(),fieldsMap).jsonPath();

		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getNDateFromTodayDateString(-2, "dd-MM-yyyy");
		queryParameters.put("starting_from", yesterdayDateString);
		queryParameters.put("related_to",json.get("related_to"));
		queryParameters.put("related_to_type", "candidate");
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "call-logs/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void searchCallLogByStartingTo() throws ParseException {
		if(json == null) json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(),"candidate").jsonPath();
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getNDateFromTodayDateString(2, "dd-MM-yyyy");
		queryParameters.put("starting_to", tomorrowDateString);
		queryParameters.put("related_to",json.get("related_to"));
		queryParameters.put("related_to_type", "candidate");
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "call-logs/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchCallLogByUpdatedFrom() throws ParseException {
		HashMap<Integer, String> fieldsMap = new HashMap<Integer, String>();
		fieldsMap.put(0, "candidate");
		fieldsMap.put(1, DateUtil.getTodayDateString());
		if(json == null) json = function.createNewCallLogWithSpecificFields(baseURL, ThreadManager.getAccountApiKey(),fieldsMap).jsonPath();

		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getNDateFromTodayDateString(-2, "dd-MM-yyyy");
		queryParameters.put("updated_from", yesterdayDateString);
		queryParameters.put("related_to",json.get("related_to"));
		queryParameters.put("related_to_type", "candidate");
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "call-logs/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void searchCallLogByUpdatedTo() throws ParseException {
		if(json == null) json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(),"candidate").jsonPath();
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getNDateFromTodayDateString(2, "dd-MM-yyyy");
		queryParameters.put("updated_to", tomorrowDateString);
		queryParameters.put("related_to",json.get("related_to"));
		queryParameters.put("related_to_type", "candidate");
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "call-logs/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@DataProvider
	public Object[][] getEntityValidData() {
		Object data[][] = { { "candidate", 200 },{ "contact",200 },{ "company", 200 } };
		return data;
	}

}