package io.recruitcrm.premiumJobBoard;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class FetchTokenFromVonqTest extends TestBase {

	public FetchTokenFromVonqTest() {
		super();
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void fetchTokenFromVonqWithInvalidAuth() {
		Response response = RestClient.doGet("JSON", jobBoardServiceURL, "vonq/token",
				ThreadManager.getOwnerAlbatrossToken() + 123, null, null, true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}
}
