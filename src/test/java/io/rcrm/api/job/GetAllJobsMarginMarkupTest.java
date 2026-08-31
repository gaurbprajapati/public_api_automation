package io.rcrm.api.job;

import com.qa.api.util.Owner;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Covers PAY-406 (Margin and Markup based Rates) for GET /v1/jobs (Public API — RecruitCRM-API repo,
 * "Show All Jobs"). This is a read-only list endpoint: the 3 new fields are only ever output here, never
 * accepted as input, so there is no validation/negative coverage for this class beyond auth.
 */
@AccountType("Business")
public class GetAllJobsMarginMarkupTest extends TestBase {

	private final commanFunction function = new commanFunction();
	private final JavaFakerJob jobFaker = new JavaFakerJob();

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void listedJobIncludesMarginMarkupFieldsAsIntegerEnumTest() {
		String jobName = jobFaker.getJobName() + "_" + System.currentTimeMillis();
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath().getString("slug");
		String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug)
				.jsonPath().getString("slug");

		Job job = new Job();
		job.setName(jobName);
		job.setCompany_slug(companySlug);
		job.setContact_slug(contactSlug);
		job.setNumber_of_openings(jobFaker.getOpenings());
		job.setJob_description_text(jobFaker.getJobDescriptionText());
		job.setEnable_job_application_form(1);
		job.setJob_type(3);
		job.setPay_rate(40.0);
		job.setBill_rate(57.14);
		job.setCalculate_charge_by(2);
		job.setMargin_percentage(30.0);
		job.setMarkup_percentage(42.85);

		RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		Map<String, Object> listedJob = findJobByName(jobName);

		Assert.assertNotNull(listedJob, "Newly created job '" + jobName + "' must appear in GET /v1/jobs");
		Assert.assertEquals(listedJob.get("calculate_charge_by"), 2,
				"calculate_charge_by must be returned as an integer code (2), not a string label");
		Assert.assertEquals(((Number) listedJob.get("margin_percentage")).doubleValue(), 30.0, 0.001,
				"margin_percentage in the list response must match what was saved");
		Assert.assertEquals(((Number) listedJob.get("markup_percentage")).doubleValue(), 42.85, 0.001,
				"markup_percentage in the list response must match what was saved");
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void getAllJobsSchemaIncludesMarginMarkupFieldsTest() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200,
				"Expected HTTP 200 for GET /v1/jobs but got " + response.getStatusCode());
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/job/getAllJobs.json"));
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void getAllJobsWithInvalidTokenTest() {
		Response response = RestClient.doGet("JSON", baseURL, "jobs", "invalid_token", null, null, true);

		Assert.assertEquals(response.getStatusCode(), 401,
				"Expected HTTP 401 for invalid auth token but got " + response.getStatusCode());
	}

	private Map<String, Object> findJobByName(String jobName) {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("limit", "100");
		queryParameters.put("page", "1");
		queryParameters.put("sort_by", "updatedon");
		queryParameters.put("sort_order", "desc");

		JsonPath jsonPath = RestClient.doGet("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(),
				queryParameters, null, true).jsonPath();

		List<Map<String, Object>> jobs = jsonPath.getList("data");
		for (Map<String, Object> job : jobs) {
			if (jobName.equals(job.get("name"))) {
				return job;
			}
		}
		return null;
	}
}
