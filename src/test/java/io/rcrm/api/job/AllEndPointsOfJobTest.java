package io.rcrm.api.job;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerTask;
import io.rcrm.api.pojo.*;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndPointsOfJobTest extends TestBase {

	public AllEndPointsOfJobTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	Faker faker = new Faker();
	JavaFakerJob jobFaker = new JavaFakerJob();

	String jobName = jobFaker.getJobName();

	String companyName = faker.company().name();
	String companyWebsite = "https://" + faker.company().url();

	String ContactFirstName = faker.name().firstName();
	String ContactLastName = faker.name().lastName();
	String ContactEmail = "rcrmtest0@gmail.com";
	String contactNumber = faker.phoneNumber().phoneNumber();
	String job_skill=faker.job().keySkills();

	String Company_slug = "";
	String Contact_slug = "";
	String job_slug = "";
	String candidate_slug = "";
	String[] feedNames;
	int[] feedIds;
	String[] feedTypes;

	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

	@Owner("Raj Pandey")
	@Test(priority=0, groups = "nightly-build")
	public void createNewCompany_POST() {

		Company company = new Company(companyName, companyWebsite, contactNumber, "");

		Response response = RestClient.doPost("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(), null, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Company_slug = jp.get("slug");
		// 2295174
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewCompany_POST",priority=1, groups = "nightly-build")
	public void createNewContact_POST() {

		Contact contact = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumber, Company_slug);

		Response response = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact);

		Assert.assertEquals(response.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Contact_slug = jp.get("slug");
		// 2295174
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = { "createNewCompany_POST", "createNewContact_POST" },priority=2, groups = "nightly-build")
	public void createNewJob_POST() {

		Job job = new Job();
		job.setName(jobName);
		job.setCompany_slug(Company_slug);
		job.setContact_slug(Contact_slug);
		job.setNumber_of_openings(jobFaker.getOpenings());
		job.setEnable_job_application_form(jobFaker.getEnable_job_application_form());
		job.setMaximum_experience(jobFaker.getMaximumExperience());
		job.setMinimum_experience(jobFaker.getMinimumExperience());
		job.setMin_annual_salary(jobFaker.getMin_annual_salary());
		job.setMax_annual_salary(jobFaker.getMax_annual_salary());
		job.setJob_description_text(jobFaker.getJobDescriptionText());
		job.setNote_for_candidates(jobFaker.getNoteForCandidate());
		// Address
		job.setCity(jobFaker.getJobCity());
		job.setCountry(jobFaker.getJobCountry());
		job.setState(jobFaker.getJobState());
		job.setLocality(jobFaker.getJobLocality());
		job.setAddress(jobFaker.getJobFullAddress());

		job.setCurrency_id(jobFaker.getCurrency_id());
		job.setQualification_id(jobFaker.qualification_id());
		job.setSalary_type(jobFaker.getSalary_type());
		job.setSpecialization(jobFaker.getSpecialization());
		job.setJob_status(jobFaker.getJob_status());
		job.setShow_company_logo(jobFaker.getShow_company_logo());
		job.setJob_skill(job_skill);
		job.setJob_type(jobFaker.getJobType());
		job.setJobCategory(jobFaker.getJobCategory());
		job.setPostal_code(jobFaker.getPostalCode());

		JsonPath jsonFeed1 = allCrudFunctions.createCustomXmlFeed(jobBoardServiceURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
		feedIds = new int[]{1, jsonFeed1.getInt("data.id")};
		feedNames = new String[]{"Free Job Boards", jsonFeed1.getString("data.title")};
		feedTypes = new String[]{"default", "custom"};
		XmlFeed xmlFeed = new XmlFeed();
		xmlFeed.setDefault(String.valueOf(feedIds[0]));
		xmlFeed.setCustom(String.valueOf(feedIds[1]));
		job.setXml_feeds(xmlFeed);

		/*
		 * These fields are not able to be entered via api
		 * Latitude
		 * Longitude
		 * job_posting_status
		 */

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//job//createJob.json"));

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		job_slug = jp.get("slug");
		for(int i = 0; i<jp.getList("xml_feeds").size(); i++) {
			response.then().assertThat().body("xml_feeds["+i+"].id", Matchers.is(feedIds[i]));
			response.then().assertThat().body("xml_feeds["+i+"].label", Matchers.is(feedNames[i]));
			response.then().assertThat().body("xml_feeds["+i+"].type", Matchers.is(feedTypes[i]));
		}
	}

	@Owner("Yash Rampal")
	@Test(priority=4, groups = "nightly-build")
	public void verifyValidationOfCreateNewContact() {

		Job job = new Job("", "", "", 0, 0);
		Response response = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, job);

		Assert.assertEquals(response.getStatusCode(), 422);

	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = "createNewJob_POST",priority=5, groups = "nightly-build")
	public void editJobBySlug_POST() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("job", job_slug);

		String basePath = "jobs/{job}";

		// Here we can also use data provider.

		Job job = new Job(jobFaker.getJobName() + " " + jobFaker.getJobSeniority(), Company_slug, Contact_slug,
				jobFaker.getOpenings(), 1);

		feedIds = new int[]{feedIds[1]};
		feedNames = new String[]{feedNames[1]};
		feedTypes = new String[]{feedTypes[1]};
		XmlFeed xmlFeed = new XmlFeed();
		xmlFeed.setCustom(String.valueOf(feedIds[0]));
		job.setXml_feeds(xmlFeed);

		Response response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null,
				pathParamters, true, job);

		Assert.assertEquals(response1.getStatusCode(), 200);
		for(int i = 0; i<response1.jsonPath().getList("xml_feeds").size(); i++) {
			response1.then().assertThat().body("xml_feeds["+i+"].id", Matchers.is(feedIds[i]));
			response1.then().assertThat().body("xml_feeds["+i+"].label", Matchers.is(feedNames[i]));
			response1.then().assertThat().body("xml_feeds["+i+"].type", Matchers.is(feedTypes[i]));
		}
		response1.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//job//editJob.json"));
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewJob_POST",priority=5, groups = "nightly-build")
	public void showAllJobs_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		for(int i = 0; i<response.jsonPath().getList("data[0].xml_feeds").size(); i++) {
			response.then().assertThat().body("data[0].xml_feeds["+i+"].id", Matchers.is(feedIds[i]));
			response.then().assertThat().body("data[0].xml_feeds["+i+"].label", Matchers.is(feedNames[i]));
			response.then().assertThat().body("data[0].xml_feeds["+i+"].type", Matchers.is(feedTypes[i]));
		}
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//job//getAllJobs.json"));

	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewJob_POST",priority=3, groups = "nightly-build")
	public void searchJobByName_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("name", jobName);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		for(int i = 0; i<response.jsonPath().getList("data[0].xml_feeds").size(); i++) {
			response.then().assertThat().body("data[0].xml_feeds["+i+"].id", Matchers.is(feedIds[i]));
			response.then().assertThat().body("data[0].xml_feeds["+i+"].label", Matchers.is(feedNames[i]));
			response.then().assertThat().body("data[0].xml_feeds["+i+"].type", Matchers.is(feedTypes[i]));
		}
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//job//searchForJobs.json"));

	}
	
	@Owner("Raj Pandey")
	@Test(dependsOnMethods = "createNewJob_POST", groups = "nightly-build")
	public void searchJobBySkill_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_skill", job_skill);

		Response response = RestClient.doGet("JSON", baseURL, "jobs/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//job//searchForJobs.json"));

	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "createNewJob_POST",priority=7, groups = "nightly-build")
	public void searchJobBySlug_GET() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("job", job_slug);

		String basePath = "jobs/{job}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null,
				pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		for(int i = 0; i<response.jsonPath().getList("xml_feeds").size(); i++) {
			response.then().assertThat().body("xml_feeds["+i+"].id", Matchers.is(feedIds[i]));
			response.then().assertThat().body("xml_feeds["+i+"].label", Matchers.is(feedNames[i]));
			response.then().assertThat().body("xml_feeds["+i+"].type", Matchers.is(feedTypes[i]));
		}
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//job//findJobBySlug.json"));

	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "createNewJob_POST",priority=8, groups = "nightly-build")
	public void getAssignedCandidatesForJob_GET() {

		candidate_slug = function.assignCandidateByJobSlug(baseURL, ThreadManager.getAccountApiKey(), job_slug).jsonPath()
				.get("candidate_slug");

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("job", job_slug);

		String basePath = "jobs/{job}/assigned-candidates";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null,
				pathParamters, true);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//job//assignedCandidatesForJob.json"));
	}

	@Owner("Rahul Shibu")
	@Test(priority=9, groups = "nightly-build")
	public void getStageHistoryOfCandidateForJob_GET() {
		Map<String, String> pathParamters = new HashMap<String, String>();

		pathParamters.put("job", job_slug);
		pathParamters.put("candidate", candidate_slug);

		String basePath = "jobs/{job}/stage-history/{candidate}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null,
				pathParamters, true);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.prettyPrint();
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//job//stageHistoryOfCandidateForJob.json"));
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "createNewJob_POST", groups = "nightly-build")
	public void deleteJobBySlug_GET() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("job", job_slug);

		String basePath = "jobs/{job}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null,
				pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		boolean jobSucessStatus = jp.get("success");
		String jobSuccessMessage = jp.getString("message");

		Assert.assertEquals(jobSucessStatus, true);
		Assert.assertEquals(jobSuccessMessage, "Job deleted successfully.");

	}
	
	@Owner("Smit Patel")
	@Test(dataProvider = "jobWithCandidatesProvider", groups = "nightly-build")
	public void getAssignedCandidatesForJobWithMultipleStatus_GET(String jobSlug, String candidateSlug2, int userId) {
	    JavaFakerTask fakerTask = new JavaFakerTask();
	    HiringStage hiringStage = new HiringStage();
	    hiringStage.setRemark(fakerTask.getTaskName());
	    hiringStage.setStage_date(fakerTask.getFutureDate());
	    hiringStage.setStatus_id(8);
	    hiringStage.setUpdated_by(userId);

	    Map<String, String> pathParam1 = Map.of("candidate", candidateSlug2, "job", jobSlug);

	    RestClient.doPost1("JSON", baseURL, "candidates/{candidate}/hiring-stages/{job}", ThreadManager.getAccountApiKey(), null, pathParam1, true, hiringStage);

	    Map<String, String> pathParams = Map.of("job", jobSlug);
	    Map<String, String> queryParams = Map.of("status_id", "1,8");

	    Response response = RestClient.doGet("JSON", baseURL, "jobs/{job}/assigned-candidates", ThreadManager.getAccountApiKey(), queryParams, pathParams, true);

	    Assert.assertEquals(response.getStatusCode(), 200);

	    List<Integer> expectedStatuses = Arrays.asList(1, 8);
	    List<Map<String, Object>> candidates = response.jsonPath().getList("data");

	    for (int i = 0; i < candidates.size(); i++) {
	        int statusId = response.jsonPath().getInt("data[" + i + "].status.status_id");
	        Assert.assertTrue(expectedStatuses.contains(statusId));
	    }
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getJobApplicationForm() throws Exception {
		

		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String companySlug = jsonCompany.get("slug");
		JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug)
				.jsonPath();
		String contactSlug = jsonContact.get("slug");
		JsonPath json = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug)
				.jsonPath();
		String entitySlug = json.get("slug");

		// Set up job application form
		URL url = new URL(albatrossURL+ "/global/update-fields");

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Authorization", "Bearer " + ThreadManager.getOwnerAlbatrossToken()); // Set the Bearer token here
		connection.setRequestProperty("Content-Type", "application/json; utf-8");
		connection.setRequestProperty("Accept", "application/json");
		connection.setDoOutput(true);

		// This is extracted manually in the job application customization page, yes this is huge, but I honestly found no other way to go around this
		String valueContents = "{\\\"id\\\":{\\\"allow_on_apply\\\":false},\\\"sourceid\\\":{\\\"allow_on_apply\\\":false},\\\"srno\\\":{\\\"allow_on_apply\\\":false},\\\"candidatename\\\":{\\\"allow_on_apply\\\":false},\\\"firstname\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":true},\\\"lastname\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"emailid\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":true},\\\"genderid\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"contactnumber\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"candidatedob\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"age\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"qualificationid\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"specialization\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"workexpyr\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"summary\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"relevantexperience\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"email_opt_out\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"resumetext\\\":{\\\"allow_on_apply\\\":false},\\\"currentsalary\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"resume\\\":{\\\"allow_on_apply\\\":false},\\\"resumefilename\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"lastorganisation\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"skill\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"willingtorelocate\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"salaryexpectation\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"position\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"address\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"city\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"locality\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"state\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"country\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"currencyid\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"currencycountry\\\":{\\\"allow_on_apply\\\":false},\\\"symbol\\\":{\\\"allow_on_apply\\\":false},\\\"accountid\\\":{\\\"allow_on_apply\\\":false},\\\"ownerid\\\":{\\\"allow_on_apply\\\":false},\\\"ownerslug\\\":{\\\"allow_on_apply\\\":false},\\\"ownername\\\":{\\\"allow_on_apply\\\":false},\\\"canaccess\\\":{\\\"allow_on_apply\\\":false},\\\"profilepic\\\":{\\\"allow_on_apply\\\":false},\\\"deleted\\\":{\\\"allow_on_apply\\\":false},\\\"authid\\\":{\\\"allow_on_apply\\\":false},\\\"resumeupdatedon\\\":{\\\"allow_on_apply\\\":false},\\\"resumeupdaterequestedon\\\":{\\\"allow_on_apply\\\":false},\\\"requestresumelinkstatus\\\":{\\\"allow_on_apply\\\":false},\\\"resumeaddedon\\\":{\\\"allow_on_apply\\\":false},\\\"profilefacebook\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"profiletwitter\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"profilelinkedin\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"profilegithub\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"profilexing\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"source\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"createdbyname\\\":{\\\"allow_on_apply\\\":false},\\\"qualification\\\":{\\\"allow_on_apply\\\":false},\\\"currentstatus\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"noticeperiod\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"createdby\\\":{\\\"allow_on_apply\\\":false},\\\"createdon\\\":{\\\"allow_on_apply\\\":false},\\\"updatedon\\\":{\\\"allow_on_apply\\\":false},\\\"availablefrom\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"salarytype\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"languageskills\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"proficiency_level\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"accountlogo\\\":{\\\"allow_on_apply\\\":false},\\\"accounttitle\\\":{\\\"allow_on_apply\\\":false},\\\"candidateterms\\\":{\\\"allow_on_apply\\\":false},\\\"jobapplicationsettings\\\":{\\\"allow_on_apply\\\":false},\\\"eeocompliance\\\":{\\\"allow_on_apply\\\":false},\\\"gdprcompliance\\\":{\\\"allow_on_apply\\\":false},\\\"unavailable\\\":{\\\"allow_on_apply\\\":true},\\\"custcolumn1\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn2\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn3\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn4\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn5\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn6\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn7\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn8\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn9\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn10\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn11\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn12\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn13\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn14\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn15\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn16\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn17\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn18\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn19\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn20\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn21\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn22\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn23\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn24\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn25\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn26\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn27\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn28\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn29\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn30\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn31\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn32\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn33\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn34\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn35\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn36\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn37\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn38\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn39\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn40\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn41\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn42\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn43\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn44\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn45\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn46\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn47\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn48\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn49\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn50\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn51\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn52\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn53\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn54\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn55\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn56\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn57\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn58\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn59\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn60\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn61\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn62\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn63\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn64\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn65\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn66\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn67\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn68\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn69\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn70\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn71\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn72\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn73\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn74\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn75\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn76\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn77\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn78\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn79\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn80\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn81\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn82\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn83\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn84\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn85\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn86\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn87\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn88\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn89\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn90\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn91\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn92\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn93\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn94\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn95\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn96\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn97\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn98\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn99\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"custcolumn100\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"slug\\\":{\\\"allow_on_apply\\\":false},\\\"note\\\":{\\\"allow_on_apply\\\":false},\\\"formattedcv\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"coverletter\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"portfolio\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"other_file_1\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"other_file_2\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"institute_name\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"educational_qualification\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":true},\\\"educational_specialization\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"grade\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"education_location\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"education_start_date\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"education_end_date\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"education_description\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"education_history\\\":{\\\"allow_on_apply\\\":false},\\\"work_history\\\":{\\\"allow_on_apply\\\":false},\\\"title\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"work_company_name\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"employment_type\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"industry_id\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"work_location\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"salary\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"is_currently_working\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"work_start_date\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"work_end_date\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"work_description\\\":{\\\"allow_on_apply\\\":true,\\\"required_on_apply\\\":false},\\\"hotlist\\\":{\\\"allow_on_apply\\\":false},\\\"candidate_company_slug\\\":{\\\"allow_on_apply\\\":false},\\\"candidate_company_city\\\":{\\\"allow_on_apply\\\":false},\\\"candidate_company_logo\\\":{\\\"allow_on_apply\\\":false},\\\"job_associated_cust_column_1\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"job_associated_cust_column_2\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"job_associated_cust_column_3\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"job_associated_cust_column_4\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"job_associated_cust_column_5\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false},\\\"off_limit_status\\\":{\\\"allow_on_apply\\\":false},\\\"last_calllog_created_on\\\":{\\\"allow_on_apply\\\":false},\\\"last_sms_sent_on\\\":{\\\"allow_on_apply\\\":false},\\\"last_email_sent_on\\\":{\\\"allow_on_apply\\\":false},\\\"last_communication_timestamp\\\":{\\\"allow_on_apply\\\":false},\\\"last_communication_method\\\":{\\\"allow_on_apply\\\":false},\\\"sms_opt_out\\\":{\\\"allow_on_apply\\\":false,\\\"required_on_apply\\\":false}}";

		String jsonInputString = "{\n" +
				"    \"key\": \"jobapplypagesettings\",\n" +
				"    \"value\": \" " +valueContents + "\",\n" +
				"    \"tableFlag\": \"account\",\n" +
				"    \"id\": " + ThreadManager.getAccount().getAccountId() +",\n" +
				"    \"isSilentProcess\": true\n" +
				"}";

		try (OutputStream os = connection.getOutputStream()) {
			byte[] input = jsonInputString.getBytes("utf-8");
			os.write(input, 0, input.length);
		}

		int responseCode = connection.getResponseCode();

		connection.disconnect();

		String basePath = "jobs/application-form";
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", entitySlug);

		Response response2 = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters,
				null, true);

		Assert.assertEquals(response2.getStatusCode(), 200);
		response2.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//job//getJobApplicationForm.json"));
	}
	
	@DataProvider
	public Object[][] jobWithCandidatesProvider() throws Exception {
	    ExecutorService executor = Executors.newFixedThreadPool(3);
	    String accountApiKey = ThreadManager.getAccountApiKey();

	    Future<String> companyFuture = executor.submit(() -> {
	        Company company = new Company(companyName, companyWebsite, contactNumber, "");
	        return RestClient.doPost("JSON", baseURL, "companies", accountApiKey, null, false, company).jsonPath().getString("slug");
	    });

	    Future<String> contactFuture = executor.submit(() -> {
	        String companySlug = companyFuture.get();
	        Contact contact = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumber, companySlug);
	        return RestClient.doPost("JSON", baseURL, "contacts", accountApiKey, null, false, contact).jsonPath().getString("slug");
	    });

	    Future<String> jobFuture = executor.submit(() -> {
	        String companySlug = companyFuture.get();
	        String contactSlug = contactFuture.get();
	        Job job = new Job();
	        job.setName(jobName);
	        job.setCompany_slug(companySlug);
	        job.setContact_slug(contactSlug);
	        job.setJob_description_text(jobFaker.getJobDescriptionText());
	        job.setPostal_code(jobFaker.getPostalCode());
	        return RestClient.doPost("JSON", baseURL, "jobs", accountApiKey, null, true, job).jsonPath().getString("slug");
	    });

	    String jobSlug = jobFuture.get();

	    // Assign both candidates
	    Future<String> candidate1Future = executor.submit(() -> function.assignCandidateByJobSlug(baseURL, accountApiKey, jobSlug).jsonPath().getString("candidate_slug"));
	    Future<JsonPath> candidate2Future = executor.submit(() -> function.assignCandidateByJobSlug(baseURL, accountApiKey, jobSlug).jsonPath());

	    JsonPath candidate2Json = candidate2Future.get();
	    String candidateSlug2 = candidate2Json.getString("candidate_slug");
	    int updatedByUserId = candidate2Json.getInt("updated_by");

	    executor.shutdown();

	    return new Object[][] { { jobSlug, candidateSlug2, updatedByUserId } };
	}

}
