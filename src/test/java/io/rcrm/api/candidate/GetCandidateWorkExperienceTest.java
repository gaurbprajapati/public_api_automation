package io.rcrm.api.candidate;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.WorkHistory;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetCandidateWorkExperienceTest extends TestBase {

	public GetCandidateWorkExperienceTest() {
		super();
	}

	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();

	// Global candidate fields
	String CandidateFirstName = fakerCandidate.getFirstName();
	String CandidateLastName = fakerCandidate.getLastName();
	String CandidateEmail = fakerCandidate.getEmailID();
	String CandidateNumber = fakerCandidate.getContactNumber();
	String linkedinLink = fakerCandidate.getUrl();
	String slug = "";

	String apiAuthToken;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@DataProvider(parallel = true)
	public Object[][] createCandidateData() {
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);
		candidate.setLinkedin(linkedinLink);
		Response response = RestClient.doPost("JSON", baseURL, "candidates", apiAuthToken, null, true, candidate);
		Assert.assertEquals(response.getStatusCode(), 200, "Candidate creation failed");
		JsonPath jp = response.jsonPath();
		slug = jp.get("slug");
		return new Object[][] { { slug } };
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "createCandidateData", groups = "nightly-build")
	public void addWorkExperienceWithValidFields(String candidateSlug) {
		Map<String, String> pathParams = new HashMap<>();
		WorkHistory workHistory = new WorkHistory();
		String basePath = "candidates/work-history/create";
		workHistory.setCandidate_slug(candidateSlug);
		workHistory.setTitle(fakerCandidate.getJobTitle());

		JSONArray jsonArray = new JSONArray();
		jsonArray.put(workHistory);
		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParams, true, jsonArray);
		Assert.assertEquals(response.getStatusCode(), 200, "Adding work experience failed");
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//addWorkExperience.json"));
	}

	@Owner("Sampurn Chouksey")
	@Test(dataProvider = "createCandidateData", groups = "nightly-build")
	public void getCandidateWorkExperienceVerify200(String candidateSlug) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("candidate", candidateSlug);
		String basePath = "candidates/{candidate}/work-history";
		Response response = RestClient.doGet("JSON", baseURL, basePath, apiAuthToken, null, pathParams, true);
		Assert.assertEquals(response.getStatusCode(), 200, "Status code is not 200");
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//getWorkExperience.json"));
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void getCandidateWorkExperienceVerify404() {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("candidate", fakerCandidate.getInvalidCandidateSlug());
		String basePath = "candidates/{candidate}/work-history";
		Response response = RestClient.doGet("JSON", baseURL, basePath, apiAuthToken, null, pathParams, true);
		response.then().body("errorCode", Matchers.is(404));
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "createCandidateData", groups = "nightly-build")
	public void getCandidateWorkExperienceVerify401(String candidateSlug) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("candidate", candidateSlug);
		String basePath = "candidates/{candidate}/work-history";
		Response response = RestClient.doGet("JSON", baseURL, basePath, apiAuthToken + "12345", null, pathParams, true);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}
}
