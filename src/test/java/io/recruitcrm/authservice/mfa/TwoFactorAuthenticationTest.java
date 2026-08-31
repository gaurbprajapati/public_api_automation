package io.recruitcrm.authservice.mfa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.github.javafaker.Faker;
import io.restassured.path.json.JsonPath;
import io.rcrm.api.javafaker.JavaFakerMFA;
import io.rcrm.api.pojo.authservice.DisableTwoFactorAuthentication;
import io.rcrm.api.pojo.authservice.EnforceTwoFactorAuthentication;
import io.rcrm.api.pojo.authservice.TOTP;
import io.rcrm.api.pojo.authservice.TwoFactorAuthentication;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class TwoFactorAuthenticationTest extends TestBase {
	JavaFakerMFA javaFakerMFA = new JavaFakerMFA();

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void generateQRCode_Test() {
		Response response = RestClient.doGet("JSON", authServiceURL, "mfa/generate-qr-code", ThreadManager.getOwnerAlbatrossToken(), null, null,
				true);

		JsonPath jsonPath = response.jsonPath();
		String secretKey = jsonPath.getString("data.secret_key");

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(secretKey.length(), 16);
		Assert.assertEquals(jsonPath.getString("message"), "success");

	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGenerateQRCode_Test() {
		Response response = RestClient.doGet("JSON", authServiceURL, "mfa/generate-qr-code", ThreadManager.getOwnerAlbatrossToken()+"123", null,
				null, true);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(jsonPath.getString("error"), "Unauthorized");
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getMFASecurityTestData", groups = "nightly-build")
	public void verifyMFASecurityWithValidOTP_Test(boolean fromSignInpage) {
		TOTP totp = new TOTP();
		totp.setTotp("qacode");
		totp.setSecretKey(javaFakerMFA.getSecretKey());
		totp.setFromSignInpage(fromSignInpage);
		
		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/verify-from-security", ThreadManager.getOwnerAlbatrossToken(), null, true,totp);
		
		JsonPath jsonPath = response.jsonPath();
		
		Assert.assertEquals(response.getStatusCode(),200 );
		Assert.assertTrue(jsonPath.get("data.user_verified"));
		Assert.assertEquals(jsonPath.get("status_message"), "User Verified Successfully");
	}
	

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getMFASecurityTestData", groups = "nightly-build")
	public void verifyMFASecurityWithInvalidOTP_Test(boolean fromSignInpage) {
		TOTP totp = new TOTP();
		totp.setTotp(javaFakerMFA.getRandomOTP());
		totp.setSecretKey(javaFakerMFA.getSecretKey());
		totp.setFromSignInpage(fromSignInpage);
		
		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/verify-from-security", ThreadManager.getOwnerAlbatrossToken(), null, true,totp);
		 
		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("message", Matchers.containsString("The OTP Entered is Wrong"));
	}
	
	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "verifyMFASecurityWithValidOTP_Test", groups = "nightly-build")
	public void verifyTwoFactorAuthenticationWithValidOTP_Test() {
		TOTP totp = new TOTP();
		totp.setTotp("qacode");

		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/verify-2fa", ThreadManager.getOwnerAlbatrossToken(), null, true, totp);
		response.then().statusCode(200);
		response.then().body("status_message", Matchers.is("User Verified Successfully"));
		response.then().body("data.token", Matchers.notNullValue());
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "verifyMFASecurityWithValidOTP_Test", groups = "nightly-build")
	public void verifyTwoFactorAuthenticationWithInvalidOTP_Test() {
		TOTP totp = new TOTP();
		totp.setTotp(javaFakerMFA.getRandomOTP());

		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/verify-2fa", ThreadManager.getOwnerAlbatrossToken(), null, true, totp);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("message", Matchers.containsString("The OTP Entered is Wrong"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void verifyTwoFactorAuthenticationWithEmptyRequestBody_Test() {

		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/verify-2fa", ThreadManager.getOwnerAlbatrossToken(), null, true, null);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);

		Assert.assertEquals(jsonPath.getString("message"), "Please Provide Valid Code");
		Assert.assertEquals(jsonPath.getString("errors.TOTP[0]"), "Please Provide Valid Code");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotVerifyTwoFactorAuthentication_Test() {
		TOTP totp = new TOTP();
		totp.setTotp(javaFakerMFA.getRandomOTP());

		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/verify-2fa", ThreadManager.getOwnerAlbatrossToken()+"123", null, true,
				totp);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void enforceTwoFactorAuthenticationForAllUsers_Test() {
		EnforceTwoFactorAuthentication enforce = new EnforceTwoFactorAuthentication();
		enforce.setUsersEnforced("allUserEnforceflag");

		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/enforce-2fa", ThreadManager.getOwnerAlbatrossToken(), null, true,
				enforce);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertTrue(jsonPath.get("mfa_enforced"));
		Assert.assertEquals(jsonPath.get("status_message"), "MFA Enforced Successfully.");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void enforceTwoFactorAuthenticationForSpecificUsers_Test() {
		TwoFactorAuthenticationJson users = new TwoFactorAuthenticationJson();
		ArrayList<TwoFactorAuthentication> userEnforcedList = new ArrayList<>();
		TwoFactorAuthentication userEnforced = new TwoFactorAuthentication();
		userEnforced.setId(javaFakerMFA.getRandomDigit());
		userEnforced.setName(javaFakerMFA.getFullName());
		userEnforced.setSlug(javaFakerMFA.getSlug());
		userEnforced.setEmail(javaFakerMFA.getEmailAddress());
		userEnforced.setRoleid(javaFakerMFA.getRandomDigit());
		userEnforced.setRole(javaFakerMFA.getJobTitle());
		userEnforced.setTeamid(javaFakerMFA.getRandomDigit());
		userEnforcedList.add(userEnforced);
		users.setUsersEnforced(userEnforcedList);

		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/enforce-2fa", ThreadManager.getOwnerAlbatrossToken(), null, true,
				users);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertTrue(jsonPath.get("mfa_enforced"));
		Assert.assertEquals(jsonPath.get("status_message"), "MFA Enforced Successfully.");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void enforceTwoFactorAuthenticationWithEmptyRequestBody_Test() {
		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/enforce-2fa", ThreadManager.getOwnerAlbatrossToken(), null, true,
				null);

		Assert.assertEquals(response.getStatusCode(), 500);
		response.then().body("message", Matchers.containsString("Please Select Atleast one User."));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotEnforceTwoFactorAuthentication_Test() {
		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/enforce-2fa", ThreadManager.getOwnerAlbatrossToken()+"123", null,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void disableTwoFactorAuthenticationWithValidPassword_Test() {
		DisableTwoFactorAuthentication disable2FA = new DisableTwoFactorAuthentication();
		disable2FA.setPasswordFlag(true);
		disable2FA.setPassword("123456");
		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/disable-2fa", ThreadManager.getOwnerAlbatrossToken(), null, true,
				disable2FA);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("status_message", Matchers.containsString("MFA Is Successfully Disabled"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void disableTwoFactorAuthenticationWithInvalidPassword_Test() {
		DisableTwoFactorAuthentication disable2FA = new DisableTwoFactorAuthentication();
		disable2FA.setPasswordFlag(true);
		disable2FA.setPassword(javaFakerMFA.getPassword());

		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/disable-2fa", ThreadManager.getOwnerAlbatrossToken(), null, true,
				disable2FA);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("message", Matchers.containsString("The Password Entered Is Incorrect"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void disableTwoFactorAuthenticationWithEmptyRequestBody_Test() {
		DisableTwoFactorAuthentication disable2FA = new DisableTwoFactorAuthentication();
		disable2FA.setPasswordFlag(true);
		disable2FA.setPassword("");

		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/disable-2fa", ThreadManager.getOwnerAlbatrossToken(), null, true,
				disable2FA);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.getString("message"), "Please Provide Password");
		Assert.assertEquals(jsonPath.getString("errors.password[0]"), "Please Provide Password");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotDisableTwoFactorAuthentication_Test() {
		DisableTwoFactorAuthentication disable2FA = new DisableTwoFactorAuthentication();
		disable2FA.setPasswordFlag(true);
		disable2FA.setPassword(javaFakerMFA.getPassword());

		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/disable-2fa", ThreadManager.getOwnerAlbatrossToken()+"123", null,
				true, disable2FA);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void disableMFAAccount_Test() {
		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/disable-mfa-for-account", ThreadManager.getOwnerAlbatrossToken(), null,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("status_message", Matchers.containsString("Field is Updated Succssfully."));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotDisableMFAAccount_Test() {
		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/disable-mfa-for-account",
				ThreadManager.getOwnerAlbatrossToken()+"123", null, true, null);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}
	
	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void verifyMFASecurityWithEmptyRquestBody_Test() {
		TOTP totp = new TOTP();
		totp.setTotp("");
		totp.setSecretKey("");
		totp.setFromSignInpage(javaFakerMFA.getRandomBoolean());
		
		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/verify-from-security", ThreadManager.getOwnerAlbatrossToken(), null, true,totp);
		
		Assert.assertEquals(response.getStatusCode(), 422);
		response.then().body("message", Matchers.containsString("Please Provide Valid Code"));
		response.then().body("errors.TOTP[0]", Matchers.equalTo("Please Provide Valid Code"));
	}
	
	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotVerifyMFASecurity_Test() {
		TOTP totp = new TOTP();
		totp.setTotp(javaFakerMFA.getRandomOTP());
		totp.setSecretKey(javaFakerMFA.getSecretKey());
		totp.setFromSignInpage(javaFakerMFA.getRandomBoolean());
		
		Response response = RestClient.doPost("JSON", authServiceURL, "mfa/verify-from-security", ThreadManager.getOwnerAlbatrossToken()+"123", null, true,totp);
		
		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}
	
	@DataProvider
	public Object[] getMFASecurityTestData() {
		return new Object[] { true, false };
	}

}
