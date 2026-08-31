package io.recruitcrm.premiumJobBoard;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.jobboard.JavaFakerVonq;
import io.rcrm.api.pojo.premiumJobBoard.SaveCampaign;
import io.rcrm.api.pojo.premiumJobBoard.SaveCampaign.channels;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class SaveCampaignVonqTest extends TestBase {

	public SaveCampaignVonqTest() {
		super();
	}

	commanFunction function = new commanFunction();
	int jobId = -1;

	JavaFakerVonq faker = new JavaFakerVonq();
	String jobBoardCompanyName = faker.getJobBoardCompanyName();
	String jobBoardUrl = faker.getJobBoardUrl();
	int generatedRandomNumber = faker.getGeneratedRandomNumber();
	String campaignName = faker.getCampaignName();
	String campaignId = faker.getCampaignId();
	String jobName, jobSlug, companyName, companySlug;

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void saveCampaignWithInvalidAuth() {
		jobId = generatedRandomNumber;
		SaveCampaign saveCampaign = new SaveCampaign();
		saveCampaign.setCampaign_name(campaignName);
		saveCampaign.setCampaign_id(campaignId);
		saveCampaign.setJob_id(jobId);
		saveCampaign.setTotal_channels("0");
		saveCampaign.setStatus("in progress");
		saveCampaign.setCurrency("USD");
		saveCampaign.setTotal_price(0);
		saveCampaign.setDraft_id("1");
		channels[] channels = new channels[1];
		channels[0] = new channels();
		channels[0].setChannel_id(String.valueOf(generatedRandomNumber));
		channels[0].setChannel_name(jobBoardCompanyName);
		channels[0].setIs_product(true);
		channels[0].setStatus("in progress");
		channels[0].setCurrency("USD");
		channels[0].setTotal_price(0);
		channels[0].setJob_board_link(jobBoardUrl);
		channels[0].setStart_date(faker.getStartDate()); 
		channels[0].setEnd_date(faker.getEndDate()); 
		saveCampaign.setChannels(channels);

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns",
				ThreadManager.getOwnerAlbatrossToken() + 123, null, false, saveCampaign);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void saveCampaignWithInvalidJobId() {
		// invalid job_id
		jobId = -generatedRandomNumber;
		SaveCampaign saveCampaign = new SaveCampaign();
		saveCampaign.setCampaign_name(campaignName);
		saveCampaign.setCampaign_id(campaignId);
		saveCampaign.setJob_id(jobId);
		saveCampaign.setTotal_channels("0");
		saveCampaign.setStatus("in progress");
		saveCampaign.setCurrency("USD");
		saveCampaign.setTotal_price(0);
		saveCampaign.setDraft_id("1");
		channels[] channels = new channels[1];
		channels[0] = new channels();
		channels[0].setChannel_id(String.valueOf(generatedRandomNumber));
		channels[0].setChannel_name(jobBoardCompanyName);
		channels[0].setIs_product(true);
		channels[0].setStatus("in progress");
		channels[0].setCurrency("USD");
		channels[0].setTotal_price(0);
		channels[0].setJob_board_link(jobBoardUrl);
		channels[0].setStart_date(faker.getStartDate()); 
		channels[0].setEnd_date(faker.getEndDate()); 
		saveCampaign.setChannels(channels);

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns",
				ThreadManager.getOwnerAlbatrossToken(), null, false, saveCampaign);

		response.then().statusCode(404);
		response.then().body("error", Matchers.is(true));
		response.then().body("error_code", Matchers.is(404));
		response.then().body("error_message", Matchers.is("Invalid Job ID"));
		response.then().body("silent_progress", Matchers.is(false));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void saveCampaignWithInvalidFields() {
		if (jobId == -1) jobId = createJob();

		SaveCampaign saveCampaign = new SaveCampaign();
		saveCampaign.setCampaign_name("");
		saveCampaign.setCampaign_id("");
		saveCampaign.setJob_id(jobId);
		saveCampaign.setTotal_channels("");
		saveCampaign.setStatus("");
		saveCampaign.setCurrency("");
		saveCampaign.setTotal_price(0);
		saveCampaign.setDraft_id("");
		channels[] channels = new channels[1];
		channels[0] = new channels();
		channels[0].setChannel_id("");
		channels[0].setChannel_name("");
		channels[0].setIs_product(true);
		channels[0].setStatus("");
		channels[0].setCurrency("");
		channels[0].setTotal_price(0);
		channels[0].setJob_board_link("");
		channels[0].setStart_date(faker.getStartDate());
		channels[0].setEnd_date(faker.getEndDate());
		saveCampaign.setChannels(channels);

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns",
				ThreadManager.getOwnerAlbatrossToken(), null, false, saveCampaign);

		response.then().statusCode(422);
		response.then().body("message", Matchers.is("The given data was invalid."));
		response.then().body("errors.campaign_name[0]", Matchers.is("The campaign name field is required."));
		response.then().body("errors.campaign_id[0]", Matchers.is("The campaign id field is required."));
		response.then().body("errors.status[0]", Matchers.is("The status field is required."));
		response.then().body("errors.currency[0]", Matchers.is("The currency field is required."));
	}

	public int createJob(){
		jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getJobResponse = albatrossFunctions.getJobResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), jobSlug);
		JsonPath jpJob = getJobResponse.jsonPath();
		jobName = jpJob.get("data.job.name");
		jobSlug = jpJob.get("data.job.slug");
		companyName = jpJob.get("data.job.companyname");
		companySlug = jpJob.get("data.job.companyslug");
		return jpJob.get("data.job.id");
	}
}
