package io.rcrm.api.offlimit;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetOffLimitContactsTest extends TestBase {
	public GetOffLimitContactsTest() {
		super();
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getOffLimitContactsWithInvalidToken() {
		Response response = RestClient.doGet("JSON", baseURL, "contacts/off-limit", ThreadManager.getAccountApiKey()+"123", null, null, false);
		response.then().statusCode(401);
		Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");
	}
}
