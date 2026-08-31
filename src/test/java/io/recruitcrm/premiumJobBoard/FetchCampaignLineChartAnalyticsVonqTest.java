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
public class FetchCampaignLineChartAnalyticsVonqTest extends TestBase {

	public FetchCampaignLineChartAnalyticsVonqTest() {
		super();
	}

	JavaFakerVonq faker = new JavaFakerVonq();
	int generatedRandomNumber = faker.getGeneratedRandomNumber();

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void fetchCampaignLineChartAnalyticsWithInvalidCampaignId() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(generatedRandomNumber));

		Response response = RestClient.doPost1("JSON", jobBoardServiceURL, "vonq/campaigns/analytics/line-chart/{id}",
				ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true, null);

		response.then().statusCode(422);
		response.then().body("message", Matchers.is("The given data was invalid."));
		response.then().body("errors.campaign_id[0]", Matchers.is("The selected campaign id is invalid."));
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void fetchCampaignLineChartAnalyticsWithInvalidAuth() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(generatedRandomNumber));

		Response response = RestClient.doPost1("JSON", jobBoardServiceURL, "vonq/campaigns/analytics/line-chart/{id}",
				ThreadManager.getOwnerAlbatrossToken() + 123, null, pathParamters, true, null);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}
}
