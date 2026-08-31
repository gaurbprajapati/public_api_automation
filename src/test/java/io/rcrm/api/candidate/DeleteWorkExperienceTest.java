package io.rcrm.api.candidate;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.WorkHistory;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DeleteWorkExperienceTest extends TestBase {
	public DeleteWorkExperienceTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	String id = "";
	Map<String, String> authTokenMap = null;

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
	// JsonPath jp = null;

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void createNewCandidateWithMandatoryFields() {

		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);
		candidate.setLinkedin(linkedinLink);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 200);

		Assert.assertEquals(CandidateFirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("last_name"), "last_name");
		Assert.assertEquals(CandidateEmail, jp.get("email"), "email");
		Assert.assertEquals(CandidateNumber, jp.get("contact_number").toString(), "contact_number");

		slug = jp.get("slug");
	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")

	public void addWorkExpericenceWithValidFields() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		workHistory = new WorkHistory();
		String basePath = "candidates/work-history/create";
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
		Response response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				jsonArray);
		Assert.assertEquals(response1.getStatusCode(), 200);
		// Schema getting checked
		response1.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//addWorkExperience.json"));
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = { "createNewCandidateWithMandatoryFields", "addWorkExpericenceWithValidFields" }, groups = "nightly-build")

	public void getCandidateWorkExperience() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}/work-history";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);
		JsonPath jp = response.jsonPath();
		id = jp.getString("id");
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//getWorkExperience.json"));
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = { "createNewCandidateWithMandatoryFields", "addWorkExpericenceWithValidFields",
			"getCandidateWorkExperience" }, groups = "nightly-build")
	public void deleteWorkExperienceVerify200() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String regex = "[^a-zA-Z0-9\\s]";
		String replaced = id.replaceAll(regex, "");
		pathParamters.put("workId", replaced);

		String basePath = "candidates/work-history/{workId}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);
		Assert.assertEquals(response.getStatusCode(), 200);
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = { "createNewCandidateWithMandatoryFields", "addWorkExpericenceWithValidFields",
			"getCandidateWorkExperience" }, groups = "nightly-build")
	public void deleteWorkExperienceVerify401() {
		Map<String, String> authTokenMapInvalid = new HashMap<String, String>();
		authTokenMapInvalid.put("Authorization", "Bearer " + ThreadManager.getAccountApiKey()+"12345");
		Map<String, String> pathParamters = new HashMap<String, String>();
		String regex = "[^a-zA-Z0-9\\s]";
		String replaced = id.replaceAll(regex, "");
		pathParamters.put("workId", replaced);

		String basePath = "candidates/work-history/{workId}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, authTokenMapInvalid, null, pathParamters, false);
		// Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = { "createNewCandidateWithMandatoryFields", "addWorkExpericenceWithValidFields",
			"getCandidateWorkExperience" }, groups = "nightly-build")
	public void deleteWorkExperienceVerify404() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String regex = "[^a-zA-Z0-9\\s]";
		String replaced = id.replaceAll(regex, "");
		pathParamters.put("workId", replaced + "12345");

		String basePath = "candidates/work-history/{workId}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);
		response.then().body("errorMessage", Matchers.is("Candidate Work Experience doesn't exist"));
	}
}
