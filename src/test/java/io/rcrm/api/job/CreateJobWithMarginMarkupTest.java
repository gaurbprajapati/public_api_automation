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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Covers PAY-406 (Margin and Markup based Rates) for POST /v1/jobs (Public API — RecruitCRM-API repo).
 * Per the finalized LLD, calculate_charge_by/margin_percentage/markup_percentage are validated via Laravel
 * "between" rules here — out-of-range values are REJECTED with 422. This is the opposite of the Albatross
 * internal endpoint (see AlbatrossCreateJobMarginMarkupTest), which clamps instead of rejecting — both
 * behaviors are correct per their respective LLD pages, not a mistake.
 */
@AccountType("Business")
public class CreateJobWithMarginMarkupTest extends TestBase {

	private final commanFunction function = new commanFunction();
	private final JavaFakerJob jobFaker = new JavaFakerJob();

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "companyAndContact", groups = "nightly-build")
	public void createJobInFixedRateModeTest(String companySlug, String contactSlug) {
		Job job = buildContractJob(companySlug, contactSlug);
		job.setPay_rate(40.0);
		job.setBill_rate(57.14);
		job.setCalculate_charge_by(1);
		job.setMargin_percentage(0.0);
		job.setMarkup_percentage(0.0);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		Assert.assertEquals(response.getStatusCode(), 200,
				"Expected HTTP 200 for create job in Fixed Rate mode but got " + response.getStatusCode());
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/job/createJob.json"));
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(jp.getInt("calculate_charge_by"), 1, "calculate_charge_by must echo back 1 (fixed_rate)");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "companyAndContact", groups = "nightly-build")
	public void createJobInMarginModeTest(String companySlug, String contactSlug) {
		Job job = buildContractJob(companySlug, contactSlug);
		job.setPay_rate(40.0);
		job.setBill_rate(57.14);
		job.setCalculate_charge_by(2);
		job.setMargin_percentage(30.0);
		job.setMarkup_percentage(42.85);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		Assert.assertEquals(response.getStatusCode(), 200,
				"Expected HTTP 200 for create job in Margin mode but got " + response.getStatusCode());
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(jp.getInt("calculate_charge_by"), 2, "calculate_charge_by must echo back 2 (margin_percentage)");
		Assert.assertEquals(jp.getDouble("margin_percentage"), 30.0, 0.001, "margin_percentage must echo back the submitted value");
		Assert.assertEquals(jp.getDouble("markup_percentage"), 42.85, 0.001, "markup_percentage must echo back the submitted value");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "companyAndContact", groups = "nightly-build")
	public void createJobInMarkupModeTest(String companySlug, String contactSlug) {
		Job job = buildContractJob(companySlug, contactSlug);
		job.setPay_rate(40.0);
		job.setBill_rate(52.0);
		job.setCalculate_charge_by(3);
		job.setMargin_percentage(23.08);
		job.setMarkup_percentage(30.0);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		Assert.assertEquals(response.getStatusCode(), 200,
				"Expected HTTP 200 for create job in Markup mode but got " + response.getStatusCode());
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(jp.getInt("calculate_charge_by"), 3, "calculate_charge_by must echo back 3 (markup_percentage)");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "companyAndContact", groups = "nightly-build")
	public void createJobWithFieldsOmittedDefaultsToFixedRateTest(String companySlug, String contactSlug) {
		// calculate_charge_by/margin_percentage/markup_percentage are all optional on this endpoint —
		// omitting them entirely must default to fixed_rate with null percentages, per the LLD.
		Job job = buildContractJob(companySlug, contactSlug);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		Assert.assertEquals(response.getStatusCode(), 200,
				"Expected HTTP 200 for create job with margin/markup fields omitted but got " + response.getStatusCode());
		Assert.assertEquals(response.jsonPath().getInt("calculate_charge_by"), 1,
				"calculate_charge_by must default to 1 (fixed_rate) when omitted from the request");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "outOfRangeMarginMarkup", groups = "nightly-build")
	public void createJobRejectsOutOfRangeMarginOrMarkupTest(String companySlug, String contactSlug,
			Double marginPercentage, Double markupPercentage) {
		Job job = buildContractJob(companySlug, contactSlug);
		job.setCalculate_charge_by(marginPercentage != null ? 2 : 3);
		job.setMargin_percentage(marginPercentage);
		job.setMarkup_percentage(markupPercentage);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		Assert.assertEquals(response.getStatusCode(), 422,
				"Expected HTTP 422 for out-of-range margin/markup on the Public API (rejected, not clamped) but got "
						+ response.getStatusCode());
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "companyAndContact", groups = "nightly-build")
	public void createJobRejectsInvalidCalculateChargeByEnumTest(String companySlug, String contactSlug) {
		Job job = buildContractJob(companySlug, contactSlug);
		job.setCalculate_charge_by(5);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		Assert.assertEquals(response.getStatusCode(), 422,
				"Expected HTTP 422 for invalid calculate_charge_by enum value but got " + response.getStatusCode());
		Assert.assertTrue(response.getBody().asString().contains("calculate_charge_by is invalid"),
				"422 response body must contain the calculate_charge_by validation message");
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void createJobWithInvalidTokenTest() {
		Job job = new Job();
		job.setName(jobFaker.getJobName());

		Response response = RestClient.doPost("JSON", baseURL, "jobs", "invalid_token", null, true, job);

		Assert.assertEquals(response.getStatusCode(), 401,
				"Expected HTTP 401 for invalid auth token but got " + response.getStatusCode());
	}

	private Job buildContractJob(String companySlug, String contactSlug) {
		Job job = new Job();
		job.setName(jobFaker.getJobName());
		job.setCompany_slug(companySlug);
		job.setContact_slug(contactSlug);
		job.setNumber_of_openings(jobFaker.getOpenings());
		job.setJob_description_text(jobFaker.getJobDescriptionText());
		job.setEnable_job_application_form(1);
		job.setJob_type(3);
		return job;
	}

	@DataProvider
	public Object[][] companyAndContact() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath().getString("slug");
		String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug)
				.jsonPath().getString("slug");
		return new Object[][] { { companySlug, contactSlug } };
	}

	@DataProvider
	public Object[][] outOfRangeMarginMarkup() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath().getString("slug");
		String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug)
				.jsonPath().getString("slug");
		return new Object[][] {
				{ companySlug, contactSlug, -5.0, null },
				{ companySlug, contactSlug, 150.0, null },
				{ companySlug, contactSlug, null, -5.0 },
				{ companySlug, contactSlug, null, 15000.0 },
		};
	}
}
