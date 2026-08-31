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
import java.util.Map;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetOffLimitStatusTest extends TestBase {
	public GetOffLimitStatusTest() {
		super();
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getOffLimitStatusWithInvalidToken() {
		Response response = RestClient.doGet("JSON", baseURL, "off-limit-status", ThreadManager.getAccountApiKey()+"123", null, null, false);
		JsonPath jp = response.jsonPath();
		response.then().statusCode(401);
		Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");
	}


}
