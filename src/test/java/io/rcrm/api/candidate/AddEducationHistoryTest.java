package io.rcrm.api.candidate;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.*;

import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.EducationHistory;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class AddEducationHistoryTest extends TestBase {
	public AddEducationHistoryTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	JavaFakerJob jobFaker = new JavaFakerJob();

	String CandidateFirstName = fakerCandidate.getFirstName();
	String CandidateLastName = fakerCandidate.getLastName();
	String CandidateEmail = fakerCandidate.getEmailID();
	String CandidateNumber = fakerCandidate.getContactNumber();
	String linkedinLink = fakerCandidate.getUrl();
	String candidate_slug = "";
	String institute_name = fakerCandidate.getInstituteName();
	String educational_qualification = fakerCandidate.getEducationalQualification();
	String educational_specialization = fakerCandidate.getSpecialization();
	String grade = fakerCandidate.getGrade();
	String education_location = fakerCandidate.getEducationLocation();
	int education_start_date = fakerCandidate.getStartDate();
	int education_end_date = fakerCandidate.getEndDateWithReferenceDate(education_start_date);
	String education_description = fakerCandidate.getDescription();
	EducationHistory educationHistory;

	@Owner("Raj Pandey")
	@Test(dataProvider = "getCandidateSlug", groups = "nightly-build")
	public void addEducationHistoryWithValidFields(String slug) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		educationHistory = new EducationHistory();
		String basePath = "candidates/education-history/create";
		educationHistory.setCandidate_slug(slug);
		educationHistory.setInstitute_name(institute_name);
		educationHistory.setEducational_qualification(educational_qualification);
		educationHistory.setEducational_specialization(educational_specialization);
		educationHistory.setGrade(grade);
		educationHistory.setEducation_location(education_location);
		educationHistory.setEducation_start_date(education_start_date);
		educationHistory.setEducation_end_date(education_end_date);
		educationHistory.setEducation_description(education_description);
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(educationHistory);
		Response response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				jsonArray);

		Assert.assertEquals(response1.getStatusCode(), 200, "Expected status code 200 but got: " + response1.getStatusCode());
		response1.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//addEducationHistory.json"));
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getCandidateSlug", groups = "nightly-build")
	public void addEducationHistoryWithInvalidTokenVerify401(String slug) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		educationHistory = new EducationHistory();
		String basePath = "candidates/education-history/create";
		educationHistory.setCandidate_slug(slug);
		educationHistory.setInstitute_name(institute_name);
		educationHistory.setEducational_qualification(educational_qualification);
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(educationHistory);
		Response response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", null, pathParamters,
				true, jsonArray);

		JsonPath jp = response1.jsonPath();

		Assert.assertTrue(jp.getString("error").contains("Unauthorized"), "Expected 'Unauthorized' in error but got: " + jp.getString("error"));
		Assert.assertEquals(response1.getStatusCode(), 401, "Expected status code 401 but got: " + response1.getStatusCode());
	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void addEducationHistoryWithInvalidSlugVerify404() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		EducationHistory educationHistory1 = new EducationHistory();
		String basePath1 = "candidates/education-history/create";
		educationHistory1.setCandidate_slug("DummyDataEducation");
		educationHistory1.setEducational_qualification("Dummy Qualification Education");
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(educationHistory1);
		Response response1 = RestClient.doPost1("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				jsonArray);

		JsonPath jp = response1.jsonPath();

		Assert.assertTrue(jp.getString("errorMessage").contains("Candidate doesn't exist"),
				"Expected error message not found in: " + jp.getString("errorMessage"));
		Assert.assertEquals(jp.getInt("errorCode"), 404, "Unexpected error code");
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getCandidateSlug", groups = "nightly-build")
	public void addEducationHistoryVerify429(String slug) {
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		List<Future<Response>> futures = new ArrayList<>();
		String mainThreadApiKey = ThreadManager.getAccountApiKey();
		Response response = null;

//		Adding 10 Education Histories using mutlithreading
		for (int i = 1; i <= 20; i++) {
			final int index = i;
			Callable<Response> task = () -> addDataForEducationHistory(slug, index, mainThreadApiKey);
			futures.add(executorService.submit(task));
		}
		executorService.shutdown();

		for (Future<Response> future : futures) {
			try {
				response = future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
//		Adding 11th education history to achieve error code 429
		response = addDataForEducationHistory(slug, 21, ThreadManager.getAccountApiKey());

		JsonPath jp = response.jsonPath();

		Assert.assertTrue(jp.getString("errorMessage").contains("Maximum 20 history can be added for a candidate"),
				"Expected error message not found in: " + jp.getString("errorMessage"));
		Assert.assertEquals(jp.getInt("errorCode"), 429, "Unexpected error code");
	}

	private Response addDataForEducationHistory(String slug, int index, String accountAPIKey) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String basePath = "candidates/education-history/create";

		EducationHistory educationHistory2 = new EducationHistory();
		educationHistory2.setCandidate_slug(slug);
		educationHistory2.setInstitute_name(institute_name + index);
		educationHistory2.setEducational_qualification(educational_qualification + index);
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(educationHistory2);
		return RestClient.doPost1("JSON", baseURL, basePath, accountAPIKey, null, pathParamters, true,
				jsonArray);
	}

	@DataProvider()
	public Object[][] getCandidateSlug() {
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);
		candidate.setLinkedin(linkedinLink);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);

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
