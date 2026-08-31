package io.recruitcrm.premiumJobBoard;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.jobboard.JavaFakerVonq;
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
public class RefreshCampaignAnalyticsVonqTest extends TestBase {

	public RefreshCampaignAnalyticsVonqTest() {
		super();
	}

	JavaFakerVonq faker = new JavaFakerVonq();
	int generatedRandomNumber = faker.getGeneratedRandomNumber();

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void refreshCampaignAnalyticsWithInvalidId() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(generatedRandomNumber));

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, "vonq/campaigns/analytics/refresh/{id}",
				ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

		response.then().statusCode(404);
		response.then().body("error", Matchers.equalTo(true));
		response.then().body("error_code", Matchers.equalTo(404));
		response.then().body("error_message", Matchers.equalTo("Campaign Not Found"));
		response.then().body("silent_progress", Matchers.equalTo(false));

	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void refreshCampaignAnalyticsWithInvalidAuth() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(generatedRandomNumber));

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, "vonq/campaigns/analytics/refresh/{id}",
				ThreadManager.getOwnerAlbatrossToken() + 123, null, pathParamters, true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.equalTo("Unauthorized"));
	}
}
