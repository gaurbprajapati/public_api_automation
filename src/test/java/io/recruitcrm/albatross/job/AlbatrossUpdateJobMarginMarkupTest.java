package io.recruitcrm.albatross.job;

import com.qa.api.util.Owner;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.pojo.albatross.jobs.JobUpdateData;
import io.rcrm.api.pojo.albatross.jobs.UpdateJobRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Covers PAY-406 (Margin and Markup based Rates) for POST /v1/jobs/{job} (Albatross internal — "Update Job",
 * session-authenticated). Same clamp-not-reject behavior as AlbatrossCreateJobMarginMarkupTest — both
 * store()/update() go through the same JobController::clampMarginMarkupPercentage() per the LLD.
 */
@AccountType("Business|AlbatrossTkn")
public class AlbatrossUpdateJobMarginMarkupTest extends TestBase {

	private final commanFunction function = new commanFunction();
	private final JavaFakerJob jobFaker = new JavaFakerJob();

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "existingJob", groups = "nightly-build")
	public void updateJobSwitchesFromFixedRateToMarkupModeTest(String jobSlug, int companyId, int contactId,
			int ownerId, int jobId) {
		Response response = updateJob(jobSlug, companyId, contactId, ownerId, jobId, 3, 23.08, 30.0);

		Assert.assertEquals(response.getStatusCode(), 200,
				"Expected HTTP 200 for Albatross update job to Markup mode but got " + response.getStatusCode());
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(jp.getInt("data.job.calculate_charge_by"), 3, "calculate_charge_by must reflect the update");
		Assert.assertEquals(jp.getDouble("data.job.markup_percentage"), 30.0, 0.001, "markup_percentage must reflect the update");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "outOfRangeValues", groups = "nightly-build")
	public void updateJobClampsOutOfRangeMarginOrMarkupTest(String jobSlug, int companyId, int contactId,
			int ownerId, int jobId, Double marginPercentage, Double markupPercentage, double expectedClampedValue,
			String clampedField) {
		Response response = updateJob(jobSlug, companyId, contactId, ownerId, jobId,
				marginPercentage != null ? 2 : 3, marginPercentage, markupPercentage);

		Assert.assertEquals(response.getStatusCode(), 200,
				"Out-of-range margin/markup on update must be clamped and return 200, not rejected — got "
						+ response.getStatusCode());
		Assert.assertEquals(response.jsonPath().getDouble("data.job." + clampedField), expectedClampedValue, 0.001,
				clampedField + " must be clamped to " + expectedClampedValue + " on update, same as create");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "existingJob", groups = "nightly-build")
	public void unauthorizedUserCannotUpdateJobMarginMarkupTest(String jobSlug, int companyId, int contactId,
			int ownerId, int jobId) {
		JobUpdateData jobData = JobUpdateData.builder()
				.slug(jobSlug)
				.name(jobFaker.getJobName())
				.companyid(companyId)
				.contactid(contactId)
				.ownerid(ownerId)
				.id(jobId)
				.calculate_charge_by(2)
				.margin_percentage(30.0)
				.markup_percentage(42.85)
				.build();
		UpdateJobRequest updateJobRequest = UpdateJobRequest.builder().job(jobData).build();

		Response response = RestClient.doPost("JSON", albatrossURL, "jobs/" + jobSlug,
				ThreadManager.getOwnerAlbatrossToken() + "invalid", null, true, updateJobRequest);

		Assert.assertEquals(response.getStatusCode(), 401,
				"Expected HTTP 401 for invalid Albatross token on update but got " + response.getStatusCode());
	}

	private Response updateJob(String jobSlug, int companyId, int contactId, int ownerId, int jobId,
			Integer calculateChargeBy, Double marginPercentage, Double markupPercentage) {
		JobUpdateData jobData = JobUpdateData.builder()
				.slug(jobSlug)
				.name(jobFaker.getJobName())
				.description("")
				.noofopenings(5)
				.job_type("contract")
				.companyid(companyId)
				.contactid(contactId)
				.ownerid(ownerId)
				.id(jobId)
				.pay_rate(40)
				.bill_rate(57)
				.calculate_charge_by(calculateChargeBy)
				.margin_percentage(marginPercentage)
				.markup_percentage(markupPercentage)
				.build();

		UpdateJobRequest updateJobRequest = UpdateJobRequest.builder()
				.job(jobData)
				.address_changed(false)
				.filesInfo(new Object[]{})
				.deleteJobKey("")
				.secondaryContacts(new Object[]{})
				.xml_feeds(new Object[]{})
				.jobParserData(new Object[]{})
				.collaborator(null)
				.build();

		return RestClient.doPost("JSON", albatrossURL, "jobs/" + jobSlug, ThreadManager.getOwnerAlbatrossToken(),
				null, true, updateJobRequest);
	}

	/** Creates a contract job via the Public API, then fetches its Albatross-side numeric IDs — same pattern
	 *  as AllEndpointJobAlbatrossTest's editClosedJobTest. */
	private Object[] createJobAndGetAlbatrossIds() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath().getString("slug");
		String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug)
				.jsonPath().getString("slug");

		Job job = new Job();
		job.setName(jobFaker.getJobName());
		job.setCompany_slug(companySlug);
		job.setContact_slug(contactSlug);
		job.setNumber_of_openings(jobFaker.getOpenings());
		job.setJob_description_text(jobFaker.getJobDescriptionText());
		job.setEnable_job_application_form(1);
		job.setJob_type(3);

		Response creationResponse = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(),
				null, true, job);
		String jobSlug = creationResponse.jsonPath().getString("slug");

		Response getJobResponse = RestClient.doPost("JSON", albatrossURL, "jobs/" + jobSlug + "/get",
				ThreadManager.getOwnerAlbatrossToken(), null, true, null);
		JsonPath getJobJsonPath = getJobResponse.jsonPath();

		return new Object[] {
				jobSlug,
				getJobJsonPath.getInt("data.job.companyid"),
				getJobJsonPath.getInt("data.job.contactid"),
				getJobJsonPath.getInt("data.job.ownerid"),
				getJobJsonPath.getInt("data.job.id"),
		};
	}

	@DataProvider
	public Object[][] existingJob() {
		return new Object[][] { createJobAndGetAlbatrossIds() };
	}

	@DataProvider
	public Object[][] outOfRangeValues() {
		Object[] ids = createJobAndGetAlbatrossIds();
		return new Object[][] {
				{ ids[0], ids[1], ids[2], ids[3], ids[4], -150.0, null, -99.99, "margin_percentage" },
				{ ids[0], ids[1], ids[2], ids[3], ids[4], 150.0, null, 100.0, "margin_percentage" },
				{ ids[0], ids[1], ids[2], ids[3], ids[4], null, -500.0, -100.0, "markup_percentage" },
				{ ids[0], ids[1], ids[2], ids[3], ids[4], null, 15000.0, 10000.0, "markup_percentage" },
		};
	}
}
