package io.rcrm.api.company;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class SearchCompanyByFieldsTest extends TestBase {

	public SearchCompanyByFieldsTest() {
		super();
	}

	JavaFakerCompany faker = new JavaFakerCompany();

	String companyName = faker.getCompanyName();
	String companyWebsite = faker.getUrl();
	String contactNumber = faker.getContactNumber();
	String companyCity = faker.getCity();
	String address = faker.getAddress();
	int industry_id = faker.getIndustry_id();
	int owner =0;
	String logo = faker.getLogoURL();
	String meetingCreatedOn;
	String lastCommunication;
	commanFunction function = new commanFunction();

	String apiAuthToken;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyData", groups = "nightly-build")
	public void searchCompanyByAllFields_GET(String companySlug) throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();

		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		Date expectedDate2 = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		// cannot include slug here as if slug is added other parameters will be
		// ignored.
		queryParameters.put("company_name", companyName);
		queryParameters.put("exact_search", String.valueOf(0));
		queryParameters.put("created_from", yesterdayDateString);
		queryParameters.put("created_to", tomorrowDateString);
		queryParameters.put("updated_from", yesterdayDateString);
		queryParameters.put("updated_to", tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "companies/search", apiAuthToken, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.company_name[0]", Matchers.is(companyName));

		JsonPath jp = response.jsonPath();

		String jpDate = jp.get("data.created_on[0]");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
		Assert.assertTrue(actualDate.before(expectedDate2), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);

		String jpDate2 = jp.get("data.updated_on[0]");
		Date actualDate2 = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate2);
		Assert.assertTrue(actualDate2.after(expectedDate), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate2);
		Assert.assertTrue(actualDate2.before(expectedDate2), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyData", groups = "nightly-build")
	public void searchCompanyByName_GET(String companySlug) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("company_name", companyName);
		queryParameters.put("exact_search", String.valueOf(1));

		Response response = RestClient.doGet("JSON", baseURL, "companies/search", apiAuthToken, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.company_name[0]", Matchers.is(companyName));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyData", groups = "nightly-build")
	public void searchCompanyBySlug_GET(String companySlug) {

		meetingCreatedOn = function.createNewMeetingWithEntitySlug(baseURL, apiAuthToken, "company", companySlug).jsonPath().get("created_on");
		lastCommunication = "Meeting on "+java.time.OffsetDateTime.parse(meetingCreatedOn).toLocalDateTime().toString().replace('T', ' ');

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("company_slug", companySlug);

		Response response = RestClient.doGet("JSON", baseURL, "companies/search", apiAuthToken, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.slug[0]", Matchers.is(companySlug));
		response.then().body("data.company_name[0]", Matchers.is(companyName));
		response.then().body("data[0].last_meeting_created_on",Matchers.is(meetingCreatedOn));
		response.then().body("data[0].last_meeting_created_by",Matchers.is(owner));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchCompanyByInvalidName_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("company_name", companyName + "Invalid Company Name");

		Response response = RestClient.doGet("JSON", baseURL, "companies/search", apiAuthToken, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body(Matchers.is("[]"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyData", groups = "nightly-build")
	public void searchCompanyByCreatedFrom(String companySlug) throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("created_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "companies/search", apiAuthToken, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data[0].company_name", Matchers.is(companyName));
		response.then().body("data[0].city", Matchers.is(companyCity));
		response.then().body("data[0].address", Matchers.is(address));
		response.then().body("data[0].website", Matchers.is(companyWebsite));
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyData", groups = "nightly-build")
	public void searchCompanyByCreatedTo(String companySlug) throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("created_to", DateUtil.getTomorrowDateString("dd-MM-yyyy"));
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "companies/search", apiAuthToken, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data[0].company_name", Matchers.is(companyName));
		response.then().body("data[0].city", Matchers.is(companyCity));
		response.then().body("data[0].address", Matchers.is(address));
		response.then().body("data[0].website", Matchers.is(companyWebsite));
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyData", groups = "nightly-build")
	public void searchCompanyByUpdatedFrom(String companySlug) throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("updated_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "companies/search", apiAuthToken, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data[0].company_name", Matchers.is(companyName));
		response.then().body("data[0].city", Matchers.is(companyCity));
		response.then().body("data[0].address", Matchers.is(address));
		response.then().body("data[0].website", Matchers.is(companyWebsite));
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyData", groups = "nightly-build")
	public void searchCompanyByUpdatedTo(String companySlug) throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("updated_to", DateUtil.getTomorrowDateString("dd-MM-yyyy"));
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "companies/search", apiAuthToken, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		response.then().body("data[0].company_name", Matchers.is(companyName));
		response.then().body("data[0].city", Matchers.is(companyCity));
		response.then().body("data[0].address", Matchers.is(address));
		response.then().body("data[0].website", Matchers.is(companyWebsite));
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchCompanyByOwnerParameters_Test() {
		// create company using public api
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
		String companySlug = jsonCompany.getString("slug");
		String companyName = jsonCompany.getString("company_name");

		// get owner data from users end point
		Response userResponse = function.getUsers(baseURL, apiAuthToken);
		Assert.assertEquals(userResponse.getStatusCode(), 200);
		JsonPath user = userResponse.jsonPath();
		int id = user.get("[0].id");
		String ownerId = String.valueOf(id);
		String ownerName = user.get("[0].first_name");
		String ownerEmail = user.get("[0].email");

		// search company by owner parameters
		String[] ownerParams = { "owner_id", "owner_name", "owner_email" };
		String[] ownerValues = { ownerId, ownerName, ownerEmail };

		for (int i = 0; i < ownerParams.length; i++) {
			Map<String, String> queryParameters = new HashMap<String, String>();
			queryParameters.put(ownerParams[i], ownerValues[i]);

			Response response = RestClient.doGet("JSON", baseURL, "companies/search", apiAuthToken, queryParameters,
					null, true);

			Assert.assertEquals(response.getStatusCode(), 200);
			JsonPath jsonPath = response.jsonPath();

			Assert.assertEquals(jsonPath.getInt("data[0].owner"), Integer.parseInt(ownerId),
					"Failed at " + ownerParams[i]);
			Assert.assertEquals(jsonPath.get("data[0].company_name"), companyName, "Failed at " + ownerParams[i]);
			Assert.assertEquals(jsonPath.get("data[0].slug"), companySlug, "Failed at " + ownerParams[i]);
		}
	}

	@DataProvider(parallel = true)
	public Object[][] getCompanyData() {

		Company company = new Company();
		company.setCompany_name(companyName);
		company.setCity(companyCity);
		company.setAddress(address);
		company.setIndustry_id(industry_id);
		company.setLogo(logo);
		company.setWebsite(companyWebsite);
		company.setLinkedin(companyWebsite);
		company.setTwitter(companyWebsite);
		company.setFacebook(companyWebsite);

		Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);

		JsonPath jsonPath = response.jsonPath();
		String companySlug = jsonPath.get("slug");
		owner =  jsonPath.get("owner");
		Object data[][] = { { companySlug } };

		return data;
	}

}
