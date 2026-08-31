package io.rcrm.api.offlimit;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetOffLimitCandidatesTest extends TestBase {
	public GetOffLimitCandidatesTest() {
		super();
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getOffLimitCandidatesWithInvalidToken() {
		Response response = RestClient.doGet("JSON", baseURL, "candidates/off-limit", ThreadManager.getAccountApiKey()+"123", null, null, false);
		JsonPath jp = response.jsonPath();
		response.then().statusCode(401);
		Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");
	}
}
