package io.recruitcrm.albatross.account;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;

import org.testng.annotations.Test;
import org.testng.Assert;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerUser;
import io.rcrm.api.pojo.albatross.*;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class ResendVerificationEmailTest extends TestBase {

	Map<String, String> authTokenMap = new HashMap<String, String>();
	commanFunction function = new commanFunction();
	JavaFakerUser faker = new JavaFakerUser();

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void sendVerificationEmailWithValidCredentials_Test() {

		String emailId = faker.getUserEmail();
		String password = ThreadManager.getOwner().getPassword();

		function.albatrossSignupResponse(albatrossURL, emailId);
		JsonPath jp = function.albatrossLoginResponse(albatrossURL, emailId, password).jsonPath();
		String privateTkn = jp.get("data.token");
		authTokenMap.put("Authorization", "Bearer " + privateTkn);

		ResendVerificationEmail resendVerificationEmail = new ResendVerificationEmail();
		resendVerificationEmail.setEmail(emailId);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "users/resend-verification-email", authTokenMap,
				null, true, resendVerificationEmail);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("status"), "success");
		Assert.assertEquals(jsonPath.getString("message"), "Verification Email Sent. Please Check Your Mailbox.");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void sendVerificationEmailForVerifiedAccount_Test() {

		String emailId = ThreadManager.getAccount().getOwner().getEmail();
		String password = ThreadManager.getOwner().getPassword();

		JsonPath jp = function.albatrossLoginResponse(albatrossURL, emailId, password).jsonPath();
		String privateTkn = jp.get("data.token");
		authTokenMap.put("Authorization", "Bearer " + privateTkn);

		ResendVerificationEmail resendVerificationEmail = new ResendVerificationEmail();
		resendVerificationEmail.setEmail(emailId);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "users/resend-verification-email", authTokenMap,
				null, true, resendVerificationEmail);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("status"), "success");
		Assert.assertEquals(jsonPath.getString("message"), "Email Already Verified");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void sendVerificationEmailWithEmptyCredentials_Test() {

		String emailId = ThreadManager.getAccount().getOwner().getEmail();
		String password = ThreadManager.getOwner().getPassword();

		JsonPath jp = function.albatrossLoginResponse(albatrossURL, emailId, password).jsonPath();
		String privateTkn = jp.get("data.token");
		authTokenMap.put("Authorization", "Bearer " + privateTkn);

		ResendVerificationEmail resendVerificationEmail = new ResendVerificationEmail();
		
		Response response = RestClient.doPost("JSON", albatrossURL, "users/resend-verification-email", authTokenMap,
				null, true, resendVerificationEmail);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.getString("status"), "fail");
		Assert.assertEquals(jsonPath.getString("message"),
				"Failed To Send Verification Email : The email field is required.");
	}

}
