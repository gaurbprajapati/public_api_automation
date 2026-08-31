package io.rcrm.api.deals;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.github.javafaker.Faker;
import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerDeal;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Deal;
import org.hamcrest.Matchers;
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
public class SearchDealByFieldsTest extends TestBase {

	public SearchDealByFieldsTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	int dealStageId;
	String companySlug;
	String companyName;
	JavaFakerDeal dealFaker = new JavaFakerDeal();

	String dealName = dealFaker.getDealName();
	int dealValue = dealFaker.getDealValue();
	String dealType = dealFaker.getNumber();
	String dealDate = DateUtil.getTodayDateString("dd-MM-yyyy");
	String dealSlug;
	String accountAPIKey;
	commanFunction function = new commanFunction();
	
	@BeforeClass(alwaysRun = true)	public void setUp() {
		accountAPIKey = ThreadManager.getAccountApiKey();
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createNewDealWithMandatoryFieldsForSearch() {
		Deal deal = new Deal();
		deal.setName(dealName);
		deal.setDeal_value(dealValue);
		deal.setClose_date(dealDate);
		deal.setDeal_stage("1");
		deal.setDeal_type(dealType);

		Response response = RestClient.doPost("JSON", baseURL, "deals", ThreadManager.getAccountApiKey(), null, true, deal);
		// Get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response Code and body
		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("name", Matchers.containsString(dealName));
		JsonPath jp = response.jsonPath();
		dealSlug = jp.get("slug");

	}

	@Owner("Smit Patel")
	@Test(dependsOnMethods = "createNewDealWithMandatoryFieldsForSearch", groups = "nightly-build")
	public void searchDealByName_GET() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("deal_name", dealName);

		Response response = RestClient.doGet("JSON", baseURL, "deals/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		response.then().statusCode(200);

		response.then().body("data.name[0]", Matchers.is(dealName));
	}

	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = "createNewDealWithMandatoryFieldsForSearch", groups = "nightly-build")
	public void searchDealBySlug_GET() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("deal_slug", dealSlug);

		Response response = RestClient.doGet("JSON", baseURL, "deals/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		response.then().statusCode(200);

		response.then().body("data.slug[0]", Matchers.is(dealSlug));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getExactSearchData", groups = "nightly-build")
	public void searchDealByAllFields_GET(int exactSearch) throws ParseException {
		getEntityValidData();
		Map<String, String> queryParameters = new HashMap<String, String>();

		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		Date expectedDate2 = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		// cannot include slug here as if slug is added other parameters will be ignored.
		queryParameters.put("deal_name", dealName);
		queryParameters.put("deal_stage", String.valueOf(dealStageId));
		queryParameters.put("company_name", companyName);
		queryParameters.put("exact_search", String.valueOf(exactSearch));
		queryParameters.put("added_from", yesterdayDateString);
		queryParameters.put("added_to", tomorrowDateString);
		queryParameters.put("updated_from", yesterdayDateString);
		queryParameters.put("updated_to", tomorrowDateString);
		queryParameters.put("closing_from", yesterdayDateString);
		queryParameters.put("closing_to", tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "deals/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		response.then().statusCode(200);
		response.then().body("data.name[0]", Matchers.is(dealName));
		response.then().body("data.company_slug[0]", Matchers.is(companySlug));
		response.then().body("data.deal_stage.id[0]", Matchers.is(dealStageId));

		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data.created_on[0]");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
		Assert.assertTrue(actualDate.before(expectedDate2), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);

		String jpDate2 = jp.get("data.updated_on[0]");
		Date actualDate2 = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate2);
		Assert.assertTrue(actualDate2.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate2);
		Assert.assertTrue(actualDate2.before(expectedDate2), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);

		String jpDate3 = jp.get("data[0].close_date");
		Date actualDate3 = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate3);
		Assert.assertTrue(actualDate3.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
		Assert.assertTrue(actualDate3.before(expectedDate2), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void searchDealByInvalidName_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("deal_name", dealName + "Invalid deal Name");

		Response response = RestClient.doGet("JSON", baseURL, "deals/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		response.then().statusCode(200);
		response.then().body(Matchers.is("[]"));

	}
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotSearchDealByFields()  {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("deal_name", dealName);

		Response response = RestClient.doGet("JSON", baseURL, "deals/search", ThreadManager.getAccountApiKey()+"12345", queryParameters, null,
				true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));

	}
	public void getEntityValidData() {
		commanFunction function = new commanFunction();
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		companySlug = jsonCompany.get("slug");
		companyName = jsonCompany.get("company_name");

		JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");

		JsonPath jsonJob = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath();
		String jobSlug = jsonJob.getString("slug");
		HashMap<Integer, String> fieldsMap = new HashMap<Integer, String>();
		fieldsMap.put(2, DateUtil.getTodayDateString("dd-MM-yyyy"));
		fieldsMap.put(5, companySlug);
		fieldsMap.put(6, jobSlug);
		fieldsMap.put(7, contactSlug);
		JsonPath jsonDeal = function.createNewDealWithSpecifiedFields(baseURL, ThreadManager.getAccountApiKey(), fieldsMap).jsonPath();
		dealName = jsonDeal.get("name");
		dealStageId =jsonDeal.get("deal_stage.id");
		dealSlug = jsonDeal.get("slug");
	}

	@Owner("Smit Patel")
	@Test(dependsOnMethods = "createNewDealWithMandatoryFieldsForSearch", groups = "nightly-build")
	public void searchDealByAddedFrom() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("added_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "deals/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = "createNewDealWithMandatoryFieldsForSearch", groups = "nightly-build")
	public void searchDealByAddedTo() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("added_to", DateUtil.getTomorrowDateString("dd-MM-yyyy"));
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "deals/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Sai Teja SG")
	@Test(dependsOnMethods = "createNewDealWithMandatoryFieldsForSearch", groups = "nightly-build")
	public void searchDealByClosingFrom() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("closing_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "deals/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].close_date");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Smit Patel")
	@Test(dependsOnMethods = "createNewDealWithMandatoryFieldsForSearch", groups = "nightly-build")
	public void searchDealByClosingTo() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("closing_to", tomorrowDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "deals/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].close_date");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = "createNewDealWithMandatoryFieldsForSearch", groups = "nightly-build")
	public void searchDealByUpdatedFrom() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("updated_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "deals/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Sai Teja SG")
	@Test(dependsOnMethods = "createNewDealWithMandatoryFieldsForSearch", groups = "nightly-build")
	public void searchDealByUpdatedTo() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("updated_to", tomorrowDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "deals/search", ThreadManager.getAccountApiKey(), queryParameters, null,
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
	@Test(dataProvider = "getDealAssociatedData", groups = "nightly-build")
	public void searchDealByAssociatedEntities_GET(String entityName, String entitySlug, String entityNameValue,
			String entitySlugValue, String dealSlug) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String basePath = "deals/search";

		queryParameters.put(entitySlug, entitySlugValue);
		queryParameters.put(entityName, entityNameValue);

		Response response = RestClient.doGet("JSON", baseURL, basePath, accountAPIKey, queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("data[0].slug", Matchers.is(dealSlug));
		response.then().body("first_page_url",
				Matchers.containsString(entityName + "=" + entityNameValue.substring(0, 3)));
		response.then().body("first_page_url", Matchers.containsString(entitySlug + "=" + entitySlugValue));
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchDealByOwnerParameters_Test() {
		// create deal using public api
		JsonPath jsonDeal = function.createNewDealWithMandatoryFields(baseURL, accountAPIKey, "", "", "").jsonPath();
		String dealSlug = jsonDeal.getString("slug");
		String dealName = jsonDeal.getString("name");

		// get owner data from users end point
		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		Assert.assertEquals(userResponse.getStatusCode(), 200);
		JsonPath user = userResponse.jsonPath();
		int id = user.get("[0].id");
		String ownerId = String.valueOf(id);
		String ownerName = user.get("[0].first_name");
		String ownerEmail = user.get("[0].email");

		// search deal by owner parameters
		String[] ownerParams = { "owner_id", "owner_name", "owner_email" };
		String[] ownerValues = { ownerId, ownerName, ownerEmail };

		for (int i = 0; i < ownerParams.length; i++) {
			Map<String, String> queryParameters = new HashMap<String, String>();
			queryParameters.put(ownerParams[i], ownerValues[i]);

			Response response = RestClient.doGet("JSON", baseURL, "deals/search", accountAPIKey, queryParameters,
					null, true);

			Assert.assertEquals(response.getStatusCode(), 200);
			JsonPath jsonPath = response.jsonPath();

			Assert.assertEquals(jsonPath.getInt("data[0].owner"), Integer.parseInt(ownerId),
					"Failed at " + ownerParams[i]);
			Assert.assertEquals(jsonPath.get("data[0].name"), dealName, "Failed at " + ownerParams[i]);
			Assert.assertEquals(jsonPath.get("data[0].slug"), dealSlug, "Failed at " + ownerParams[i]);
		}
	}

	@DataProvider
	public Object[][] getExactSearchData() {
		Object data[][] = { { 0 }, { 1 } };
		return data;
	}
	
	@DataProvider(parallel = true)
	public Object[][] getDealAssociatedData() {

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String candidateSlug = jsonCandidate.getString("slug");
		String candidateName = jsonCandidate.getString("first_name");
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String companySlug = jsonCompany.getString("slug");
		String companyName = jsonCompany.getString("company_name");
		JsonPath jsonContact = function.createNewContact_POST(baseURL, accountAPIKey, companySlug).jsonPath();
		String contactSlug = jsonContact.getString("slug");
		String contactName = jsonContact.getString("first_name");
		JsonPath jsonJob = function.createNewJob(baseURL, accountAPIKey, companySlug, contactSlug).jsonPath();
		String jobSlug = jsonJob.getString("slug");
		String jobName = jsonJob.getString("name");
		HashMap<Integer, String> fieldsMap = new HashMap<>();
		fieldsMap.put(5, companySlug);
		fieldsMap.put(6, jobSlug);
		fieldsMap.put(7, contactSlug);
		fieldsMap.put(8, candidateSlug);
		JsonPath jsonDeal = function.createNewDealWithSpecifiedFields(baseURL, accountAPIKey, fieldsMap).jsonPath();
		String dealSlug = jsonDeal.getString("slug");

		Object data[][] = { { "candidate_name", "candidate_slug", candidateName, candidateSlug, dealSlug },
				{ "company_name", "company_slug", companyName, companySlug, dealSlug },
				{ "contact_name", "contact_slug", contactName, contactSlug, dealSlug },
				{ "job_name", "job_slug", jobName, jobSlug, dealSlug } };
		return data;
	}

}
