package io.recruitcrm.albatross.job;

import com.qa.api.util.Owner;
import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.albatross.jobs.CreateJob;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Covers PAY-406 (Margin and Markup based Rates) for POST /v1/jobs (Albatross internal — the session-
 * authenticated endpoint behind the Add Job modal, distinct from the Public API path covered by
 * CreateJobWithMarginMarkupTest). Per the finalized LLD, out-of-range margin/markup values here are
 * CLAMPED (200, stored value adjusted) via JobController::clampMarginMarkupPercentage() — never rejected.
 * This is the opposite of the Public API's 422-rejecting behavior; both are correct per their own LLD pages.
 */
@AccountType("Business|AlbatrossTkn")
public class AlbatrossCreateJobMarginMarkupTest extends TestBase {

	private final commanFunction function = new commanFunction();
	private final JavaFakerJob jobFaker = new JavaFakerJob();

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "companyAndContactIds", groups = "nightly-build")
	public void createJobInMarginModeTest(String companyId, String contactId) {
		Response response = createJob(companyId, contactId, 2, 30.0, 42.85);

		Assert.assertEquals(response.getStatusCode(), 200,
				"Expected HTTP 200 for Albatross create job in Margin mode but got " + response.getStatusCode());
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(jp.getInt("data.job.calculate_charge_by"), 2, "calculate_charge_by must echo back 2");
		Assert.assertEquals(jp.getDouble("data.job.margin_percentage"), 30.0, 0.001, "margin_percentage must echo back the submitted value");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "companyAndContactIds", groups = "nightly-build")
	public void createJobInMarkupModeTest(String companyId, String contactId) {
		Response response = createJob(companyId, contactId, 3, 23.08, 30.0);

		Assert.assertEquals(response.getStatusCode(), 200,
				"Expected HTTP 200 for Albatross create job in Markup mode but got " + response.getStatusCode());
		Assert.assertEquals(response.jsonPath().getInt("data.job.calculate_charge_by"), 3,
				"calculate_charge_by must echo back 3");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "companyAndContactIds", groups = "nightly-build")
	public void createJobDefaultsToFixedRateWhenOmittedTest(String companyId, String contactId) {
		Response response = createJob(companyId, contactId, null, null, null);

		Assert.assertEquals(response.getStatusCode(), 200,
				"Expected HTTP 200 for Albatross create job with charge-by fields omitted but got "
						+ response.getStatusCode());
		Assert.assertEquals(response.jsonPath().getInt("data.job.calculate_charge_by"), 1,
				"calculate_charge_by must default to 1 (fixed_rate) at the DB level when omitted");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "outOfRangeValues", groups = "nightly-build")
	public void marginOrMarkupOutOfRangeIsClampedNotRejectedTest(String companyId, String contactId,
			Double marginPercentage, Double markupPercentage, double expectedClampedValue, String clampedField) {
		Response response = createJob(companyId, contactId, marginPercentage != null ? 2 : 3, marginPercentage,
				markupPercentage);

		Assert.assertEquals(response.getStatusCode(), 200,
				"Out-of-range margin/markup must be clamped and return 200 on the Albatross endpoint, not rejected — got "
						+ response.getStatusCode());
		Assert.assertEquals(response.jsonPath().getDouble("data.job." + clampedField), expectedClampedValue, 0.001,
				clampedField + " must be clamped to " + expectedClampedValue + " rather than rejected");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "companyAndContactIds", groups = "nightly-build")
	public void unauthorizedUserCannotCreateJobTest(String companyId, String contactId) {
		CreateJob.Job jobPayload = new CreateJob.Job(jobFaker.getJobName(), companyId,
				ThreadManager.getOwner().getUserId());
		jobPayload.setContactid(contactId);
		jobPayload.setJob_type("contract");
		CreateJob createJobRequest = new CreateJob(jobPayload);

		Response response = RestClient.doPost("JSON", albatrossURL, "jobs",
				ThreadManager.getOwnerAlbatrossToken() + "invalid", null, true, createJobRequest);

		Assert.assertEquals(response.getStatusCode(), 401,
				"Expected HTTP 401 for invalid Albatross token but got " + response.getStatusCode());
	}

	private Response createJob(String companyId, String contactId, Integer calculateChargeBy,
			Double marginPercentage, Double markupPercentage) {
		CreateJob.Job jobPayload = new CreateJob.Job(jobFaker.getJobName(), companyId,
				ThreadManager.getOwner().getUserId());
		jobPayload.setContactid(contactId);
		jobPayload.setJob_type("contract");
		// Bill rate calculation from margin/markup happens on the frontend, not the backend (per LLD note) —
		// send an explicit value regardless of mode, same as the frontend would.
		jobPayload.setPay_rate(40.0);
		jobPayload.setBill_rate(57.14);
		jobPayload.setCalculate_charge_by(calculateChargeBy);
		jobPayload.setMargin_percentage(marginPercentage);
		jobPayload.setMarkup_percentage(markupPercentage);

		CreateJob createJobRequest = new CreateJob(jobPayload);
		return RestClient.doPost("JSON", albatrossURL, "jobs", ThreadManager.getOwnerAlbatrossToken(), null, true,
				createJobRequest);
	}

	@DataProvider
	public Object[][] companyAndContactIds() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath().getString("slug");
		String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug)
				.jsonPath().getString("slug");
		String companyId = ReaperIntegration.getEntityIdFromSlug("company", companySlug).getBody().asString()
				.replace("Corresponding entity for the slug is : ", "").trim();
		String contactId = ReaperIntegration.getEntityIdFromSlug("contact", contactSlug).getBody().asString()
				.replace("Corresponding entity for the slug is : ", "").trim();
		return new Object[][] { { companyId, contactId } };
	}

	@DataProvider
	public Object[][] outOfRangeValues() {
		Object[][] ids = companyAndContactIds();
		String companyId = (String) ids[0][0];
		String contactId = (String) ids[0][1];
		return new Object[][] {
				{ companyId, contactId, -150.0, null, -99.99, "margin_percentage" },
				{ companyId, contactId, 150.0, null, 100.0, "margin_percentage" },
				{ companyId, contactId, null, -500.0, -100.0, "markup_percentage" },
				{ companyId, contactId, null, 15000.0, 10000.0, "markup_percentage" },
		};
	}
}
