package io.recruitcrm.albatross.sso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ResetMFAAccountTest extends TestBase  {
	
	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void resetMFAAccount_Test() {
		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/reset-mfa-for-account", ThreadManager.getOwnerAlbatrossToken(), null, true, null);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("status_message", Matchers.containsString("MFA Disabled For Account Successfully"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotResetMFAAccount_Test() {
		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/reset-mfa-for-account", ThreadManager.getOwnerAlbatrossToken()+"123", null, true, null);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));

	}
}
