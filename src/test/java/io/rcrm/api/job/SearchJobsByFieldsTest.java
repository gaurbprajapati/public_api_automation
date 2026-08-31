package io.rcrm.api.job;

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

import com.github.javafaker.Faker;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.Contact;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class SearchJobsByFieldsTest extends TestBase {

	public SearchJobsByFieldsTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	Faker faker = new Faker();
	JavaFakerJob jobFaker = new JavaFakerJob();

	String companyName = faker.company().name();
	String companyWebsite = "https://" + faker.company().url();

	String ContactFirstName = faker.name().firstName();
	String ContactLastName = faker.name().lastName();
	String ContactEmail = "rcrmtest0@gmail.com";
	String contactNumber = faker.phoneNumber().phoneNumber();

	String city = jobFaker.getJobCity();
	String locality = jobFaker.getJobLocality();
	String full_address = jobFaker.getJobFullAddress();
	String country = jobFaker.getJobCountry();

	String JobName = jobFaker.getJobName();
	int NoOfOpenings = jobFaker.getOpenings();
	int MaximumExperience = jobFaker.getMaximumExperience();
	int MinimumExperience = jobFaker.getMinimumExperience();
	int Min_annual_salary = jobFaker.getMin_annual_salary();
	int Max_annual_salary = jobFaker.getMax_annual_salary();
	String noteForCandidate = jobFaker.getNoteForCandidate();
	String job_skill = jobFaker.getSkills();

	String company_slug = "";
	String contact_slug = "";
	String secondary_contact_slug = "";
	String job_slug = "";
	String longText = jobFaker.getJobDescriptionText() + jobFaker.getNoteForCandidate();
	
	commanFunction function = new commanFunction();
	
	String apiAuthToken;

	@BeforeClass(alwaysRun = true)		public void setUp() {
			apiAuthToken = ThreadManager.getAccountApiKey();
		}
	
	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void createNewCompany_POST() {

		Company company = new Company(companyName, companyWebsite, contactNumber, "");

		Response response = RestClient.doPost("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(), null, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		company_slug = jp.get("slug");
		// 2295174
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewCompany_POST", groups = "nightly-build")
	public void createNewContact_POST() {

		Contact contact = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumber, company_slug);

		Response response = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact);

		Assert.assertEquals(response.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		contact_slug = jp.get("slug");

		// Create secondary contact
		Response response2 = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact);

		secondary_contact_slug = response2.jsonPath().getString("slug");
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = { "createNewCompany_POST", "createNewContact_POST" }, groups = "nightly-build")
	public void createNewJobAndVerifyStatusOk() {
		Job job = new Job();
		job.setName(JobName);
		job.setCompany_slug(company_slug);
		job.setContact_slug(contact_slug);
		job.setNumber_of_openings(NoOfOpenings);
		job.setNote_for_candidates(noteForCandidate);
		job.setCity(city);
		job.setLocality(locality);
		job.setAddress(full_address);
		job.setCountry(country);
		job.setJob_skill(job_skill);
		job.setSecondary_contact_slug(secondary_contact_slug);

		// Below fields can't be blank
		job.setJob_description_text(jobFaker.getJobDescriptionText());
		job.setEnable_job_application_form(1);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		// get the response body:
		// String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200

		Assert.assertEquals(response.getStatusCode(), 200);

		job_slug = jp.get("slug");
		// Verify Response body
		int number_of_openings = jp.get("number_of_openings");
		String res_numberOf_openings = String.valueOf(number_of_openings);

		Assert.assertEquals(JobName, jp.get("name"), "Job Name");
		Assert.assertEquals(company_slug, jp.get("company_slug"), "Company Slug");
		Assert.assertEquals(contact_slug, jp.get("contact_slug"), "Contact Slug");
		Assert.assertEquals(NoOfOpenings + "", res_numberOf_openings, "No Of Openings");

	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByJobName_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("name", JobName);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(JobName, jp.get("data.name[0]"), "Job Name");
	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByNoteForCandidate_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("note_for_candidates", noteForCandidate);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(noteForCandidate, jp.get("data.note_for_candidates[0]"), "note_for_candidates");
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByCity_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("city", city);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(city, jp.get("data.city[0]"), "city");
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByLocality_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("locality", locality);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(locality, jp.get("data.locality[0]"), "locality");
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByFullAddress_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("full_address", full_address);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(full_address, jp.get("data.address[0]"), "address");
	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByCountry_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("country", country);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(country, jp.get("data.country[0]"), "country");
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByCompanyName_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("company_name", companyName);
		queryParameters.put("expand", "company");

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(companyName, jp.get("data.company[0].company_name"), "companyName");
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByContactName_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("contact_name", ContactFirstName + " " + ContactLastName);
		queryParameters.put("expand", "contact");

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(ContactFirstName, jp.get("data.contact[0].first_name"), "first_name");
		Assert.assertEquals(ContactLastName, jp.get("data.contact[0].last_name"), "last_name");

	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByContactEmail_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("contact_email", ContactEmail);
		queryParameters.put("expand", "contact");

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(ContactEmail, jp.get("data.contact[0].email"), "email");
	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByContactNumber_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("contact_number", contactNumber);
		queryParameters.put("expand", "contact");

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(contactNumber, jp.get("data.contact[0].contact_number"), "contact_number");
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByJobStatus_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_status", "1");

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Assert.assertEquals("Open", jp.get("data.job_status[0].label"), "job_label");
		response.then().body("data.job_status.label", Matchers.hasItems("Open"));
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobBySlug_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", job_slug);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(job_slug, jp.get("data.slug[0]"), job_slug);
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByJobSkill() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_skill", job_skill);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(job_slug, jp.get("data.slug[0]"), job_slug);
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", dataProvider = "getExactSearchData", groups = "nightly-build")
	public void searchJobByAllFields_GET(int exactSearch) throws ParseException {

		Map<String, String> queryParameters = new HashMap<String, String>();

		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		Date expectedDate2 = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		queryParameters.put("job_status", "1");
		queryParameters.put("name", JobName);
		queryParameters.put("note_for_candidates", noteForCandidate);
		queryParameters.put("city", city);
		queryParameters.put("locality", locality);
		queryParameters.put("full_address", full_address);
		queryParameters.put("country", country);
		queryParameters.put("company_name", companyName);
		queryParameters.put("contact_name", ContactFirstName + " " + ContactLastName);
		queryParameters.put("contact_email", ContactEmail);
		queryParameters.put("contact_number", contactNumber);
		queryParameters.put("exact_search", String.valueOf(exactSearch));
		queryParameters.put("job_skill", job_skill);

		queryParameters.put("created_from", yesterdayDateString);
		queryParameters.put("created_to", tomorrowDateString);
		queryParameters.put("updated_from", yesterdayDateString);
		queryParameters.put("updated_to", tomorrowDateString);

		queryParameters.put("expand", "*");
		String basePath = "jobs/search";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		response.then().body("data.name", Matchers.hasItems(JobName));
		response.then().body("data.name[0]", Matchers.containsString(JobName));
		response.then().body("data.note_for_candidates[0]", Matchers.is(noteForCandidate));

		response.then().body("data.city[0]", Matchers.is(city));
		response.then().body("data.locality[0]", Matchers.is(locality));

		response.then().body("data.address[0]", Matchers.is(full_address));
		response.then().body("data.country[0]", Matchers.is(country));

		response.then().body("data.job_status.label[0]", Matchers.is("Open"));
		response.then().body("data.country[0]", Matchers.is(country));

		response.then().body("current_page", Matchers.greaterThan(0));
		response.then().body("from", Matchers.greaterThan(0));
		response.then().body("last_page", Matchers.greaterThan(0));
		response.then().body("per_page", Matchers.greaterThan(0));
		response.then().body("to", Matchers.greaterThan(0));
		response.then().body("total", Matchers.greaterThan(0));

		response.then().body("path", Matchers.containsString(basePath));
		response.then().body("first_page_url", Matchers.containsString(basePath));
		response.then().body("last_page_url", Matchers.containsString(basePath));

		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate2), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : " + expectedDate2
				+ " but found : " + actualDate);

		String jpDate2 = jp.get("data[0].updated_on");
		Date actualDate2 = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate2);
		Assert.assertTrue(actualDate2.after(expectedDate2), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate2);
		Assert.assertTrue(actualDate2.before(expectedDate), "Actual date is not before the expected date, expected : " + expectedDate2
				+ " but found : " + actualDate2);
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByInvalidJobName_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("name", "1" + JobName + "Test");

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		response.then().body("", Matchers.empty());
	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByCreatedFrom() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("created_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByCreatedTo() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("created_to", DateUtil.getTomorrowDateString("dd-MM-yyyy"));
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByUpdatedFrom() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("updated_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "createNewJobAndVerifyStatusOk", groups = "nightly-build")
	public void searchJobByUpdatedTo() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("updated_to", DateUtil.getTomorrowDateString("dd-MM-yyyy"));
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getRequiredTestData", groups = "nightly-build")
	public void searchJobByCompanySlug(String company_slug, String contact_slug, String secondary_contact_slug, String job_slug){
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("company_slug", company_slug);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);


		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(company_slug, jp.get("data.company_slug[0]"), company_slug);
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getRequiredTestData", groups = "nightly-build")
	public void searchJobByContactSlug(String company_slug, String contact_slug, String secondary_contact_slug, String job_slug){
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("contact_slug", contact_slug);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);


		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(contact_slug, jp.get("data.contact_slug[0]"), contact_slug);
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getRequiredTestData", groups = "nightly-build")
	public void searchJobBySecondaryContactSlug(String company_slug, String contact_slug, String secondary_contact_slug, String job_slug){
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("secondary_contact_slug", secondary_contact_slug);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);


		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jp = response.jsonPath();

		response.prettyPrint();

		Assert.assertEquals(secondary_contact_slug, jp.get("data[0].secondary_contact_slugs[0]"), secondary_contact_slug);
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchJobByOwnerParameters_Test() {
		// create job using public api
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
		String companySlug = jsonCompany.getString("slug");
		JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
		String contactSlug = jsonContact.getString("slug");
		JsonPath jsonJob = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
		String jobSlug = jsonJob.getString("slug");
		String jobName = jsonJob.getString("name");

		// get owner data from users end point
		Response userResponse = function.getUsers(baseURL, apiAuthToken);
		Assert.assertEquals(userResponse.getStatusCode(), 200);
		JsonPath user = userResponse.jsonPath();
		int id = user.get("[0].id");
		String ownerId = String.valueOf(id);
		String ownerName = user.get("[0].first_name");
		String ownerEmail = user.get("[0].email");

		// search job by owner parameters
		String[] ownerParams = { "owner_id", "owner_name", "owner_email" };
		String[] ownerValues = { ownerId, ownerName, ownerEmail };

		for (int i = 0; i < ownerParams.length; i++) {
			Map<String, String> queryParameters = new HashMap<String, String>();
			queryParameters.put(ownerParams[i], ownerValues[i]);

			Response response = RestClient.doGet("JSON", baseURL, "jobs/search", apiAuthToken, queryParameters,
					null, true);

			Assert.assertEquals(response.getStatusCode(), 200);
			JsonPath jsonPath = response.jsonPath();

			Assert.assertEquals(jsonPath.getInt("data[0].owner"), Integer.parseInt(ownerId),
					"Failed at " + ownerParams[i]);
			Assert.assertEquals(jsonPath.get("data[0].name"), jobName, "Failed at " + ownerParams[i]);
			Assert.assertEquals(jsonPath.get("data[0].slug"), jobSlug, "Failed at " + ownerParams[i]);
		}
	}

	@DataProvider
	public Object[][] getExactSearchData() {
		Object data[][] = { { 0 }, { 1 } };
		return data;
	}

	@DataProvider(name = "getRequiredTestData")
	public Object[][]  dpMethod() {

		String company_slug = null;
		String contact_slug = null;
		String job_slug = null;

		// Create company
		Company company = new Company(companyName, companyWebsite, contactNumber, "");
		company_slug = RestClient.doPost("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(), null, true, company).jsonPath().getString("slug");

		// Create contact
		Contact contact = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumber, company_slug);
		contact_slug = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact).jsonPath().getString("slug");

		// Create secondary contact
		String secondary_contact_slug = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact).jsonPath().get("slug");

		// Create job
		Job job = new Job();
		job.setName(JobName);
		job.setCompany_slug(company_slug);
		job.setContact_slug(contact_slug);
		job.setNumber_of_openings(NoOfOpenings);
		job.setNote_for_candidates(noteForCandidate);
		job.setCity(city);
		job.setLocality(locality);
		job.setAddress(full_address);
		job.setCountry(country);
		job.setJob_skill(job_skill);
		job.setJob_description_text(jobFaker.getJobDescriptionText());
		job.setEnable_job_application_form(1);
		job.setSecondary_contact_slug(secondary_contact_slug);

		job_slug = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job).jsonPath().getString("slug");
		return new Object[][] { { company_slug, contact_slug, secondary_contact_slug, job_slug } };
	}

}
