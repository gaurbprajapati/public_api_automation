package io.recruitcrm.premiumJobBoard;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.jobboard.JavaFakerVonq;
import io.rcrm.api.pojo.premiumJobBoard.TakeCampaignOffline;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class TakeCampaignOfflineVonqTest extends TestBase {

	public TakeCampaignOfflineVonqTest() {
		super();
	}

	commanFunction function = new commanFunction();
	int jobId = -1;

	JavaFakerVonq faker = new JavaFakerVonq();
	int generatedRandomNumber = faker.getGeneratedRandomNumber();

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void takeCampaignOfflineWithInvalidAuth() {
		TakeCampaignOffline takeCampaignOffline = new TakeCampaignOffline();
		takeCampaignOffline.setType("CAMPAIGN");
		takeCampaignOffline.setCampaign_id(generatedRandomNumber);

		Response response = RestClient.doPost1("JSON", jobBoardServiceURL, "vonq/campaigns/take-off-line",
				ThreadManager.getOwnerAlbatrossToken() + 123, null, null, false, takeCampaignOffline);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void takeCampaignOfflineWithInvalidCampaignId() {
		TakeCampaignOffline takeCampaignOffline = new TakeCampaignOffline();
		takeCampaignOffline.setType("CAMPAIGN");
		takeCampaignOffline.setCampaign_id(generatedRandomNumber);

		Response response = RestClient.doPost1("JSON", jobBoardServiceURL, "vonq/campaigns/take-off-line",
				ThreadManager.getOwnerAlbatrossToken(), null, null, false, takeCampaignOffline);

		response.then().statusCode(500); // invalid api responder in jobboard service
	}
}
