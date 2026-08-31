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

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DeleteCampaignVonqTest extends TestBase {

	public DeleteCampaignVonqTest() {
		super();
	}

	commanFunction function = new commanFunction();
	int jobId = -1;

	JavaFakerVonq faker = new JavaFakerVonq();
	int generatedRandomNumber = faker.getGeneratedRandomNumber();

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void deleteCampaignWithInvalidAuth() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(generatedRandomNumber));

		Response response = RestClient.doDelete("JSON", jobBoardServiceURL, "vonq/campaigns/{id}",
				ThreadManager.getOwnerAlbatrossToken() + 123, null, pathParamters, false);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void deleteCampaignWithInvalidCampaignId() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(generatedRandomNumber));

		Response response = RestClient.doDelete("JSON", jobBoardServiceURL, "vonq/campaigns/{id}",
				ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, false);

		response.then().statusCode(404);
		response.then().body("error", Matchers.is(true));
		response.then().body("error_code", Matchers.is(404));
		response.then().body("error_message", Matchers.is("Invalid campaign ID"));
		response.then().body("silent_progress", Matchers.is(false));
	}
}
