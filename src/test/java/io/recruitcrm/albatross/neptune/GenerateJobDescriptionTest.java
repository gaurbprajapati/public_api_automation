package io.recruitcrm.albatross.neptune;

import java.io.IOException;
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
import io.rcrm.api.pojo.Job;
import io.rcrm.api.pojo.neptune.JobDescription;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GenerateJobDescriptionTest extends TestBase {
	JavaFakerSummary javaFakerSummary = new JavaFakerSummary();
	JavaFakerJob javaFakerJob = new JavaFakerJob();
	JavaFakerCandidate javaFakerCandidate = new JavaFakerCandidate();

	@Owner("Divya")
	@Test(dataProvider = "getKeyData", groups = "nightly-build")
	public void generateJobDescriptionWithMandatoryFields_Test(String key) {

		String basePath = "jobs/generate-job-description";

		Job job = new Job(javaFakerJob.getJobName());
		JobDescription jobDescription = new JobDescription();
		jobDescription.setJob(job);
		jobDescription.setKey(key);
		if (key == "manual_prompt") {
			jobDescription.setPrompt(javaFakerSummary.getPromptText());
		}

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, false,
				jobDescription);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.generated_text", Matchers.notNullValue());
		response.then().body("meta.message", Matchers.is("Job description generated successfully"));

	}

	@Owner("Divya")
	@Test(dataProvider = "getKeyData", groups = "nightly-build")
	public void generateJobDescriptionWithOptionalFields_Test(String key) {

		String basePath = "jobs/generate-job-description";

		Job job = new Job(javaFakerJob.getJobName(), javaFakerJob.getOpenings(), javaFakerJob.getMinimumExperience(),
				javaFakerJob.getMaximumExperience(), javaFakerJob.getSalary_type(), javaFakerJob.getMax_annual_salary(),
				javaFakerJob.getMin_annual_salary(), javaFakerJob.getSpecialization(), javaFakerJob.getJobCity(),
				javaFakerJob.getJobLocality(), javaFakerJob.getJobState(), javaFakerJob.getJobCountry(),
				javaFakerJob.getJobFullAddress(), javaFakerJob.getSkills(),javaFakerJob.getJobType(),javaFakerJob.getJobCategory());
		JobDescription jobDescription = new JobDescription();

		jobDescription.setJob(job);
		jobDescription.setKey(key);
		if (key == "manual_prompt") {
			jobDescription.setPrompt(javaFakerSummary.getPromptText());
		}

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, false,
				jobDescription);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.generated_text", Matchers.notNullValue());
		response.then().body("meta.message", Matchers.is("Job description generated successfully"));

	}

	@Owner("Divya")
	@Test(dependsOnMethods = "generateJobDescriptionWithMandatoryFields_Test", groups = "nightly-build")
	public void generateJobDescriptionWithInvalidKey_Test() {

		String basePath = "jobs/generate-job-description";

		Job job = new Job(javaFakerJob.getJobName());
		JobDescription jobDescription = new JobDescription();
		jobDescription.setJob(job);
		jobDescription.setKey(javaFakerSummary.getRandomKey());

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, false,
				jobDescription);
		Assert.assertEquals(response.getStatusCode(), 400);

		response.then().body("data", Matchers.nullValue());
		response.then().body("errors.errors.key.errorMsg", Matchers.is("key is invalid"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//nullValuesValidation.json"));

	}

	@Owner("Divya")
	@Test(dependsOnMethods = "generateJobDescriptionWithMandatoryFields_Test", groups = "nightly-build")
	public void generateJobDescriptionWithNullValues_Test() {

		String basePath = "jobs/generate-job-description";

		JobDescription jobDescription = new JobDescription();
		jobDescription.setJob(null);
		jobDescription.setKey(null);

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, false,
				jobDescription);
		Assert.assertEquals(response.getStatusCode(), 400);

		response.then().body("data", Matchers.nullValue());
		response.then().body("errors.errors.key.errorMsg", Matchers.is("Input should be a valid string"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//nullValuesValidation.json"));
	}

	@Owner("Divya")
	@Test(dependsOnMethods = "generateJobDescriptionWithMandatoryFields_Test", groups = "nightly-build")
	public void generateJobDescriptionWithPartialNullValues_Test() {

		String basePath = "jobs/generate-job-description";

		Job job = new Job(javaFakerJob.getJobName());
		JobDescription jobDescription = new JobDescription();

		jobDescription.setJob(job);
		jobDescription.setKey(null);

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, false,
				jobDescription);
		Assert.assertEquals(response.getStatusCode(), 400);

		response.then().body("data", Matchers.nullValue());
		response.then().body("errors.errors.key.errorMsg", Matchers.is("Input should be a valid string"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//nullValuesValidation.json"));
	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGenerateJobDescription_Test() {
		String basePath = "jobs/generate-job-description";

		JobDescription jobDescription = new JobDescription();

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, false,
				jobDescription);
		Assert.assertEquals(response.getStatusCode(), 401);

		response.then().body("detail", Matchers.is("Unauthorized"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//unauthorizedGPTAccess.json"));

	}

	@DataProvider
	public Object[] getKeyData() {

		Object[] data = { "generate", "manual_prompt" };
		return data;
	}

}
