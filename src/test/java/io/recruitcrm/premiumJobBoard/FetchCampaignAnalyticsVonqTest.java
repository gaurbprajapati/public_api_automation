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

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class FetchCampaignAnalyticsVonqTest extends TestBase {

	public FetchCampaignAnalyticsVonqTest() {
		super();
	}

	JavaFakerVonq faker = new JavaFakerVonq();
	int generatedRandomNumber = faker.getGeneratedRandomNumber();

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void fetchCampaignAnalyticsWithInvalidCampaignId() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(generatedRandomNumber));

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, "vonq/campaigns/analytics/{id}",
				ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

		response.then().statusCode(500); // invalid api responder in jobboard service
	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void fetchCampaignAnalyticsWithInvalidAuth() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(generatedRandomNumber));

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, "vonq/campaigns/analytics/{id}",
				ThreadManager.getOwnerAlbatrossToken() + 123, null, pathParamters, true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}
}
