package io.recruitcrm.premiumJobBoard;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.jobboard.JavaFakerVonq;
import io.rcrm.api.pojo.premiumJobBoard.IncrementNameCampaign;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class IncrementCampaignNameVonqTest extends TestBase {

	public IncrementCampaignNameVonqTest() {
		super();
	}

	JavaFakerVonq faker = new JavaFakerVonq();
	int generatedRandomNumber = faker.getGeneratedRandomNumber();
	String campaignName = faker.getCampaignName();

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void incrementCampaignNameWithInvalidId() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(generatedRandomNumber));

		IncrementNameCampaign incrementNameCampaign = new IncrementNameCampaign();
		incrementNameCampaign.setCampaign_name(campaignName);

		Response response = RestClient.doPost1("JSON", jobBoardServiceURL, "vonq/campaigns/increment-name/{id}",
				ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true, incrementNameCampaign);

		response.then().statusCode(422);
		response.then().body("message", Matchers.is("The given data was invalid."));
		response.then().body("errors.job_id[0]", Matchers.is("The selected job id is invalid."));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void incrementCampaignNameWithInvalidAuth() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(generatedRandomNumber)); //job id //500 //401

		IncrementNameCampaign incrementNameCampaign = new IncrementNameCampaign();
		incrementNameCampaign.setCampaign_name(campaignName);

		Response response = RestClient.doPost1("JSON", jobBoardServiceURL, "vonq/campaigns/increment-name/{id}",
				ThreadManager.getOwnerAlbatrossToken() + 123, null, pathParamters, true, incrementNameCampaign);

		response.then().statusCode(401);
		response.then().body("error", Matchers.equalTo("Unauthorized"));
	}
}
