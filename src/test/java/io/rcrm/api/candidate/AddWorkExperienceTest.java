package io.rcrm.api.candidate;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.EducationHistory;
import io.rcrm.api.pojo.WorkHistory;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class AddWorkExperienceTest extends TestBase {
	public AddWorkExperienceTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	JavaFakerJob jobFaker = new JavaFakerJob();

	String CandidateFirstName = fakerCandidate.getFirstName();
	String CandidateLastName = fakerCandidate.getLastName();
	// String CandidateEmail = "rcrmtest0@gmail.com";
	String CandidateEmail = fakerCandidate.getEmailID();
	String CandidateNumber = fakerCandidate.getContactNumber();
	String linkedinLink = fakerCandidate.getUrl();
	String candidate_slug = "";
	String work_company_name = fakerCandidate.getWorkCompanyName();
	String title = fakerCandidate.getJobTitle();
	int employment_type = fakerCandidate.getEmploymentType();
	int industry_id = fakerCandidate.getIndustryId();
	String work_location = fakerCandidate.getWorkLocation();
	int is_currently_working = fakerCandidate.currentlyWorking();
	int work_start_date = fakerCandidate.getStartDate();
	int work_end_date = fakerCandidate.getEndDateWithReferenceDate(work_start_date);
	String work_description = fakerCandidate.getDescription();
	int salary = fakerCandidate.getSalary();
	WorkHistory workHistory;
	Object accountAPIKey;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		accountAPIKey = ThreadManager.getAccountApiKey();
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getCandidateSlug", groups = "nightly-build")
	public void addWorkExpericencVerify200(String slug) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String basePath = "candidates/work-history/create";
		workHistory = new WorkHistory();
		workHistory.setCandidate_slug(slug);
		workHistory.setTitle(title);
		workHistory.setWork_company_name(work_company_name);
		workHistory.setEmployment_type(employment_type);
		workHistory.setIndustry_id(industry_id);
		workHistory.setWork_location(work_location);
		workHistory.setSalary(salary);
		workHistory.setWork_start_date(work_start_date);
		workHistory.setWork_end_date(work_end_date);
		workHistory.setWork_description(work_description);

		JSONArray jsonArray = new JSONArray();
		jsonArray.put(workHistory);
		Response response = RestClient.doPost1("JSON", baseURL, basePath, accountAPIKey, null, pathParamters, true,
				jsonArray);

		Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 but got: " + response.getStatusCode());
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//addWorkExperience.json"));
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getCandidateSlug", groups = "nightly-build")
	public void addWorkExpericenceWithInvalidTokenVerify401(String slug) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		workHistory = new WorkHistory();
		String basePath = "candidates/work-history/create";
		workHistory.setCandidate_slug(slug);
		workHistory.setTitle(title);

		JSONArray jsonArray = new JSONArray();
		jsonArray.put(workHistory);
		Response response = RestClient.doPost1("JSON", baseURL, basePath, accountAPIKey+"12345", null, pathParamters,
				true, jsonArray);

		JsonPath jp = response.jsonPath();

		Assert.assertTrue(jp.getString("error").contains("Unauthorized"), "Expected 'Unauthorized' in error but got: " + jp.getString("error"));
		Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401 but got: " + response.getStatusCode());
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getCandidateSlug", groups = "nightly-build")
	public void addWorkExpericenceWithInvalidSlugVerify404(String slug) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		WorkHistory workHistory1 = new WorkHistory();
		String basePath1 = "candidates/work-history/create";
		workHistory1.setCandidate_slug("dummyData");
		workHistory1.setTitle(title);

		JSONArray jsonArray = new JSONArray();
		jsonArray.put(workHistory1);
		Response response = RestClient.doPost1("JSON", baseURL, basePath1, accountAPIKey, null, pathParamters, true,
				jsonArray);

		JsonPath jp = response.jsonPath();

		Assert.assertTrue(jp.getString("errorMessage").contains("Candidate doesn't exist"),
				"Expected error message not found in: " + jp.getString("errorMessage"));
		Assert.assertEquals(jp.getInt("errorCode"), 404, "Unexpected error code");
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getCandidateSlug", groups = "nightly-build")
	public void addWorkExpericenceVerify429(String slug) {
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		List<Future<Response>> futures = new ArrayList<>();
		Response response = null;

		for (int i = 1; i <= 20; i++) {
			final int index = i;
			Callable<Response> task = () -> addDataForWorkExperience(slug, index, accountAPIKey);
			futures.add(executorService.submit(task));
		}
		executorService.shutdown();

		for (Future<Response> future: futures) {
			try {
				response = future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
//		Adding 11th work history to achieve error code 429
		response = addDataForWorkExperience(slug, 21, accountAPIKey);

		JsonPath jp = response.jsonPath();

		Assert.assertTrue(jp.getString("errorMessage").contains("Maximum 20 history can be added for a candidate"),
				"Expected error message not found in: " + jp.getString("errorMessage"));
		Assert.assertEquals(jp.getInt("errorCode"), 429, "Unexpected error code");
	}

	private Response addDataForWorkExperience(String slug, int index, Object accountAPIKey) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String basePath = "candidates/work-history/create";
		WorkHistory workHistory2 = new WorkHistory();
		workHistory2.setCandidate_slug(slug);
		workHistory2.setTitle(title + index);
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(workHistory2);
		return RestClient.doPost1("JSON", baseURL, basePath, accountAPIKey, null, pathParamters, true,
				jsonArray);
	}

	@DataProvider()
	public Object[][] getCandidateSlug() {
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);
		candidate.setLinkedin(linkedinLink);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", accountAPIKey, null, true, candidate);

		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200, "response code is not 200!");
		Assert.assertEquals(CandidateFirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("last_name"), "last_name");
		Assert.assertEquals(CandidateEmail, jp.get("email"), "email");
		Assert.assertEquals(CandidateNumber, jp.get("contact_number").toString(), "contact_number");

		String slug = jp.get("slug");
		return new Object[][] {{ slug }};
	}
}
