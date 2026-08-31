package io.rcrm.api.job;

import com.qa.api.util.Owner;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
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
 * Covers PAY-406 (Margin and Markup based Rates) for POST /v1/jobs/{job} (Public API — RecruitCRM-API repo,
 * "Edit Job"). Same 422 range-rejection behavior as CreateJobWithMarginMarkupTest — see that class's header
 * comment for why this differs from the Albatross-internal endpoint's clamp behavior.
 */
@AccountType("Business")
public class EditJobWithMarginMarkupTest extends TestBase {

	private final commanFunction function = new commanFunction();

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "existingJobSlug", groups = "nightly-build")
	public void editJobSwitchesFromFixedRateToMarginModeTest(String jobSlug) {
		Job update = new Job();
		update.setPay_rate(40.0);
		update.setBill_rate(57.14);
		update.setCalculate_charge_by(2);
		update.setMargin_percentage(30.0);
		update.setMarkup_percentage(42.85);

		Response response = RestClient.doPost("JSON", baseURL, "jobs/" + jobSlug, ThreadManager.getAccountApiKey(),
				null, true, update);

		Assert.assertEquals(response.getStatusCode(), 200,
				"Expected HTTP 200 for edit job to Margin mode but got " + response.getStatusCode());
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/job/editJob.json"));
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(jp.getInt("calculate_charge_by"), 2, "calculate_charge_by must reflect the edit (margin_percentage)");
		Assert.assertEquals(jp.getDouble("margin_percentage"), 30.0, 0.001, "margin_percentage must reflect the edit");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "existingJobSlug", groups = "nightly-build")
	public void editJobSwitchesFromMarginToMarkupModeTest(String jobSlug) {
		Job toMargin = new Job();
		toMargin.setCalculate_charge_by(2);
		toMargin.setMargin_percentage(30.0);
		toMargin.setMarkup_percentage(42.85);
		RestClient.doPost("JSON", baseURL, "jobs/" + jobSlug, ThreadManager.getAccountApiKey(), null, true, toMargin);

		Job toMarkup = new Job();
		toMarkup.setCalculate_charge_by(3);
		toMarkup.setMargin_percentage(23.08);
		toMarkup.setMarkup_percentage(30.0);

		Response response = RestClient.doPost("JSON", baseURL, "jobs/" + jobSlug, ThreadManager.getAccountApiKey(),
				null, true, toMarkup);

		Assert.assertEquals(response.getStatusCode(), 200,
				"Expected HTTP 200 for edit job mode switch Margin -> Markup but got " + response.getStatusCode());
		Assert.assertEquals(response.jsonPath().getInt("calculate_charge_by"), 3,
				"calculate_charge_by must reflect the second edit (markup_percentage)");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "outOfRangeMarginMarkup", groups = "nightly-build")
	public void editJobRejectsOutOfRangeMarginOrMarkupTest(String jobSlug, Double marginPercentage,
			Double markupPercentage) {
		Job update = new Job();
		update.setCalculate_charge_by(marginPercentage != null ? 2 : 3);
		update.setMargin_percentage(marginPercentage);
		update.setMarkup_percentage(markupPercentage);

		Response response = RestClient.doPost("JSON", baseURL, "jobs/" + jobSlug, ThreadManager.getAccountApiKey(),
				null, true, update);

		Assert.assertEquals(response.getStatusCode(), 422,
				"Expected HTTP 422 for out-of-range margin/markup on edit but got " + response.getStatusCode());
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void editNonExistentJobReturns404Test() {
		Job update = new Job();
		update.setCalculate_charge_by(2);
		update.setMargin_percentage(30.0);
		update.setMarkup_percentage(42.85);

		Response response = RestClient.doPost("JSON", baseURL, "jobs/non-existent-slug-xyz",
				ThreadManager.getAccountApiKey(), null, true, update);

		Assert.assertEquals(response.getStatusCode(), 404,
				"Expected HTTP 404 for editing a non-existent job slug but got " + response.getStatusCode());
		Assert.assertEquals(response.jsonPath().getString("errorMessage"), "Job doesn't exist",
				"404 response must contain the 'Job doesn't exist' error message");
	}

	private String createContractJobSlug() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath().getString("slug");
		String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug)
				.jsonPath().getString("slug");
		return function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug)
				.jsonPath().getString("slug");
	}

	@DataProvider
	public Object[][] existingJobSlug() {
		return new Object[][] { { createContractJobSlug() } };
	}

	@DataProvider
	public Object[][] outOfRangeMarginMarkup() {
		String jobSlug = createContractJobSlug();
		return new Object[][] {
				{ jobSlug, -5.0, null },
				{ jobSlug, 150.0, null },
				{ jobSlug, null, -5.0 },
				{ jobSlug, null, 15000.0 },
		};
	}
}
