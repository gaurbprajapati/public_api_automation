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
import io.rcrm.api.pojo.EducationHistory;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class EditEducationHistoryTest extends TestBase {
	public EditEducationHistoryTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	String id = "";

	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	JavaFakerJob jobFaker = new JavaFakerJob();

	String CandidateFirstName = fakerCandidate.getFirstName();
	String CandidateLastName = fakerCandidate.getLastName();
	// String CandidateEmail = "rcrmtest0@gmail.com";
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
	// JsonPath jp = null;

	@Owner("Sampurn Chouksey")
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

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")

	public void addEducationHistoryWithValidFields() {
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
		Assert.assertEquals(response1.getStatusCode(), 200);
		// Schema getting checked
		// response1.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//addWorkExperience.json"));
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = { "createNewCandidateWithMandatoryFields", "addEducationHistoryWithValidFields" }, groups = "nightly-build")

	public void getCandidateEducationHistory() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}/education-history";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);
		JsonPath jp = response.jsonPath();
		id = jp.getString("id");
		Assert.assertEquals(response.getStatusCode(), 200);
	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = { "createNewCandidateWithMandatoryFields", "addEducationHistoryWithValidFields",
			"getCandidateEducationHistory" }, groups = "nightly-build")
	public void editEducationHistoryVerify200() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String regex = "[^a-zA-Z0-9\\s]";
		String replaced = id.replaceAll(regex, "");
		pathParamters.put("educationId", replaced);

		String basePath = "candidates/education-history/{educationId}}";
		String newQualification = "MS in CS";
		EducationHistory educationHistoryObj = new EducationHistory(slug, newQualification);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				educationHistoryObj);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//editEducationHistory.json"));
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(jp.get("educational_qualification"), newQualification, newQualification);

	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = { "createNewCandidateWithMandatoryFields", "addEducationHistoryWithValidFields",
			"getCandidateEducationHistory" }, groups = "nightly-build")
	public void editEducationHistoryVerify401() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String regex = "[^a-zA-Z0-9\\s]";
		String replaced = id.replaceAll(regex, "");
		pathParamters.put("educationId", replaced);

		String basePath = "candidates/education-history/{educationId}";
		String newQualification = "MS in CS";
		EducationHistory educationHistoryObj = new EducationHistory(slug, newQualification);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", null, pathParamters,
				true, educationHistoryObj);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	/*
	 * @Test(dependsOnMethods = { "createNewCandidateWithMandatoryFields",
	 * "addEducationHistoryWithValidFields", "getCandidateEducationHistory" })
	 * public void editEducationHistoryVerify422() { Map<String, String>
	 * pathParamters = new HashMap<String, String>(); String regex =
	 * "[^a-zA-Z0-9\\s]"; String replaced = id.replaceAll(regex, "");
	 * pathParamters.put("educationId", replaced);
	 * 
	 * String basePath = "candidates/work-history/{educationId}";
	 * 
	 * String newQualification = "MS in CS"; EducationHistory educationHistoryObj =
	 * new EducationHistory(slug, newQualification);
	 * 
	 * Response response = RestClient.doPost1("JSON", baseURL, basePath,
	 * authTokenMap, null, pathParamters, true, educationHistoryObj);
	 * response.then().body("error",
	 * Matchers.containsString("Unprocessable Entity")); }
	 */
}
