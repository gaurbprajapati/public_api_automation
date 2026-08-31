package io.recruitcrm.premiumJobBoard;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.jobboard.JavaFakerVonq;
import io.rcrm.api.pojo.premiumJobBoard.GetCampaignDraftList;
import io.rcrm.api.pojo.premiumJobBoard.SaveCampaignAsDraft;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditCampaignDraftVonqTest extends TestBase {

	public EditCampaignDraftVonqTest() {
		super();
	}

	commanFunction function = new commanFunction();
	int jobId = -1;
	int savedCampaignId = -1;
	int savedDraftId = -1;

	JavaFakerVonq faker = new JavaFakerVonq();
	String jobBoardCompanyName = faker.getJobBoardCompanyName();
	String companyEmail = faker.getCompanyEmail();
	String jobBoardUrl = faker.getJobBoardUrl();
	String jobDescription = faker.getJobDescription();
	int generatedRandomNumber = faker.getGeneratedRandomNumber();
	String campaignName = faker.getCampaignName();
	String campaignId = faker.getCampaignId();
	String jobName, jobSlug, companyName, companySlug;

	final String CAMPAIGNDATAJSON = "{\"campaign_form\":{\"labels\":null,\"recruiterInfo\":" +
			"{\"name\":\"" + jobBoardCompanyName + "\",\"emailAddress\":\"" + companyEmail + "\"}," +
			"\"postingDetails\":{\"title\":\"Construction Analyst Job\",\"description\":\"" + jobDescription + "\"," +
			"\"organization\":{\"name\":\"Ledner, Murray and KautzerLedner, Murray and KautzerLedner, Murray and KautzerLedner, Murray and Kautzer\","
			+
			"\"companyLogo\":\"\"},\"contactInfo\":{\"name\":\"\",\"emailAddress\":\"\",\"phoneNumber\":\"\"}," +
			"\"workingLocation\":{\"addressLine1\":\"\",\"addressLine2\":\"\",\"postcode\":\"\",\"city\":\"\"," +
			"\"country\":\"\",\"allowsRemoteWork\":0},\"yearsOfExperience\":0,\"employmentType\":\"permanent\"," +
			"\"weeklyWorkingHours\":{\"from\":0,\"to\":0},\"salaryIndication\":{\"period\":\"monthly\"," +
			"\"range\":{\"from\":0,\"to\":0,\"currency\":\"INR\"}},\"jobPageUrl\":\"https://"
			+ System.getProperty("envname") + "web.recruitcrm.net/apply/16872398956200000198NWA\"," +
			"\"applicationUrl\":\"https://" + System.getProperty("envname")
			+ "web.recruitcrm.net/apply/16872398956200000198NWA?source=%20Construction%20Analyst%20Job\"}," +
			"\"targetGroup\":{\"educationLevel\":[],\"seniority\":[],\"industry\":[],\"jobCategory\":[]}," +
			"\"orderedProducts\":[],\"orderedProductsSpecs\":{\"d6513524-bd5f-5b65-a242-1e5d24eefee6\":{}}," +
			"\"campaignName\":\" Construction Analyst Job\",\"companyId\":\"82033\",\"poNumber\":\"\",\"currency\":\"USD\","
			+
			"\"paymentMethod\":null},\"recruiter\":{},\"basket\":[]}";

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void editCampaignDraftWithInvalidId() {
		if (savedDraftId == -1) {
			saveCampaignAsDraftTest();
		}
		SaveCampaignAsDraft saveCampaignAsDraft = new SaveCampaignAsDraft();
		saveCampaignAsDraft.setCampaign_name(campaignName);
		saveCampaignAsDraft.setJob_id(jobId);
		saveCampaignAsDraft.setCampaign_data(CAMPAIGNDATAJSON);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(savedDraftId) + 123);

		Response response = RestClient.doPost1("JSON", jobBoardServiceURL, "vonq/campaigns/draft/{id}",
				ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true, saveCampaignAsDraft);

		response.then().statusCode(422);
		response.then().body("message", Matchers.is("The given data was invalid."));
		response.then().body("errors.campaign_id[0]", Matchers.is("The selected campaign id is invalid."));
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void editCampaignDraftByIdWithInvalidAuth() {
		SaveCampaignAsDraft saveCampaignAsDraft = new SaveCampaignAsDraft();
		saveCampaignAsDraft.setCampaign_name(campaignName);
		saveCampaignAsDraft.setJob_id(jobId);
		saveCampaignAsDraft.setCampaign_data(CAMPAIGNDATAJSON);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(savedDraftId));

		Response response = RestClient.doPost1("JSON", jobBoardServiceURL, "vonq/campaigns/draft/{id}",
				ThreadManager.getOwnerAlbatrossToken() + 123, null, pathParamters, true, saveCampaignAsDraft);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	public void saveCampaignAsDraftTest() {
		if (jobId == -1) {
			jobId = createJob();
		}
		SaveCampaignAsDraft saveCampaignAsDraft = new SaveCampaignAsDraft();
		saveCampaignAsDraft.setCampaign_name(campaignName);
		saveCampaignAsDraft.setJob_id(jobId);
		saveCampaignAsDraft.setCampaign_data(CAMPAIGNDATAJSON);

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns/draft",
				ThreadManager.getOwnerAlbatrossToken(), null, true, saveCampaignAsDraft);

		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//vonq//saveCampaignAsDraft.json"));
		response.then().body("message", Matchers.is(campaignName + " was successfully saved as a draft."));
		response.then().body("status", Matchers.is("success"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data.'draft campaign'.campaign_name", Matchers.is(campaignName));
		response.then().body("data.'draft campaign'.job_id", Matchers.is(jobId));
		response.then().body("data.'draft campaign'.campaign_data", Matchers.is(CAMPAIGNDATAJSON));

		savedDraftId = response.jsonPath().get("data.'draft campaign'.id");
	}

	public int createJob() {
		jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getJobResponse = albatrossFunctions.getJobResponse(albatrossURL,
				ThreadManager.getOwnerAlbatrossToken(), jobSlug);
		JsonPath jpJob = getJobResponse.jsonPath();
		jobName = jpJob.get("data.job.name");
		jobSlug = jpJob.get("data.job.slug");
		companyName = jpJob.get("data.job.companyname");
		companySlug = jpJob.get("data.job.companyslug");
		return jpJob.get("data.job.id");
	}
}
