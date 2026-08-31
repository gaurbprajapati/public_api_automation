package io.recruitcrm.albatross.neptune;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.javafaker.neptune.JavaFakerSummary;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.EducationHistory;
import io.rcrm.api.pojo.WorkHistory;
import io.rcrm.api.pojo.neptune.CandidateSummary;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GenerateCandidateSummaryTest extends TestBase {
	JavaFakerSummary javaFakerSummary = new JavaFakerSummary();
	JavaFakerCandidate javaFakerCandidate = new JavaFakerCandidate();
	JavaFakerJob javaFakerJob = new JavaFakerJob();

	@Owner("Divya")
	@Test(dataProvider = "getKeyData", groups = "nightly-build")
	public void generateCandidateSummaryWithMandatoryFields_Test(String key) {
		String basePath = "candidates/generate-summary";

		Candidate candidate = new Candidate(javaFakerCandidate.getFirstName(), javaFakerJob.getSpecialization(),
				javaFakerJob.getJobName());
		CandidateSummary candidateSummary = new CandidateSummary();

		candidateSummary.setCandidate(candidate);
		candidateSummary.setKey(key);
		if (key == "manual_prompt") {
			candidateSummary.setPrompt(javaFakerSummary.getPromptText());
		}

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, false,
				candidateSummary);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.generated_text", Matchers.notNullValue());
		response.then().body("meta.message", Matchers.is("Candidate Summary Generated Successfully"));

	}

	@Owner("Divya")
	@Test(dataProvider = "getKeyData", groups = "nightly-build")
	public void generateCandidateSummaryWithOptionalFields_Test(String key) {

		String basePath = "candidates/generate-summary";
		CandidateSummary candidateSummary = new CandidateSummary();
		EducationHistory educationHistory = new EducationHistory(javaFakerJob.getJobName(),
				javaFakerCandidate.getCurrentOrganization(), javaFakerJob.getJobCity());
		WorkHistory workHistory = new WorkHistory();

		workHistory.WorkHistory1(javaFakerCandidate.getInstituteName(),
				javaFakerCandidate.getEducationalQualification());
		ArrayList<WorkHistory> workHistoryList = new ArrayList<>();
		workHistoryList.add(workHistory);

		ArrayList<EducationHistory> educationHistoryList = new ArrayList<>();
		educationHistoryList.add(educationHistory);

		Candidate candidate = new Candidate(javaFakerCandidate.getFirstName(), javaFakerJob.getSpecialization(),
				javaFakerJob.getJobName(), javaFakerCandidate.getCurrentOrganization(),
				javaFakerCandidate.getWork_ex_year(), javaFakerJob.getJobCity(), javaFakerJob.getJobLocality(),
				javaFakerJob.getMin_annual_salary(), javaFakerJob.getMax_annual_salary(),
				javaFakerCandidate.getCurrentEmploymentStatus(), javaFakerCandidate.getEducationalQualification(),
				javaFakerJob.getSpecialization(), workHistoryList, educationHistoryList);
		candidateSummary.setCandidate(candidate);
		candidateSummary.setKey(key);
		if (key == "manual_prompt") {
			candidateSummary.setPrompt(javaFakerSummary.getPromptText());
		}

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, false,
				candidateSummary);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.generated_text", Matchers.notNullValue());
		response.then().body("meta.message", Matchers.is("Candidate Summary Generated Successfully"));

	}

	@Owner("Divya")
	@Test(dependsOnMethods = "generateCandidateSummaryWithMandatoryFields_Test", groups = "nightly-build")
	public void generateCandidateSummaryWithInvalidKey_Test() {
		String basePath = "candidates/generate-summary";

		Candidate candidate = new Candidate(javaFakerCandidate.getFirstName(), javaFakerJob.getSpecialization(),
				javaFakerJob.getJobName());
		CandidateSummary candidateSummary = new CandidateSummary();

		candidateSummary.setCandidate(candidate);
		candidateSummary.setKey(javaFakerSummary.getRandomKey());

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, false,
				candidateSummary);
		Assert.assertEquals(response.getStatusCode(), 400);

		response.then().body("data", Matchers.nullValue());
		response.then().body("errors.errors.key.errorMsg", Matchers.is("key is invalid"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//nullValuesValidation.json"));

	}

	@Owner("Divya")
	@Test(dependsOnMethods = "generateCandidateSummaryWithMandatoryFields_Test", groups = "nightly-build")
	public void generateCandidateSummaryWithPartialNullValues_Test() {
		String basePath = "candidates/generate-summary";

		Candidate candidate = new Candidate(javaFakerCandidate.getFirstName(), javaFakerJob.getSpecialization(),
				javaFakerJob.getJobName());
		CandidateSummary candidateSummary = new CandidateSummary();

		candidateSummary.setCandidate(candidate);
		candidateSummary.setKey(null);

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, false,
				candidateSummary);
		Assert.assertEquals(response.getStatusCode(), 400);

		response.then().body("data", Matchers.nullValue());
		response.then().body("errors.errors.key.errorMsg", Matchers.is("Input should be a valid string"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//nullValuesValidation.json"));
	}

	@Owner("Divya")
	@Test(dependsOnMethods = "generateCandidateSummaryWithMandatoryFields_Test", groups = "nightly-build")
	public void generateCandidateSummaryWithNullValues_Test() {
		String basePath = "candidates/generate-summary";

		CandidateSummary candidateSummary = new CandidateSummary();

		candidateSummary.setCandidate(null);
		candidateSummary.setKey(null);

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, false,
				candidateSummary);
		Assert.assertEquals(response.getStatusCode(), 400);

		response.then().body("data", Matchers.nullValue());
		response.then().body("errors.errors.key.errorMsg", Matchers.is("Input should be a valid string"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//nullValuesValidation.json"));
	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGenerateCandidateSummary_Test() {
		String basePath = "candidates/generate-summary";

		CandidateSummary candidateSummary = new CandidateSummary();

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, false,
				candidateSummary);
		Assert.assertEquals(response.getStatusCode(), 401);

		response.then().body("detail", Matchers.is("Unauthorized"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//unauthorizedGPTAccess.json"));

	}

	@DataProvider
	public Object[] getKeyData() {

		Object[] data = { "summarise", "manual_prompt" };
		return data;
	}

}
