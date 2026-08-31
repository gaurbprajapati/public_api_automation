package io.recruitcrm.albatross.account;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.hamcrest.Matchers;
import org.testng.Assert;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.javafaker.JavaFakerUser;
import io.rcrm.api.pojo.albatross.*;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class SignUpAccountTest extends TestBase {

	Map<String, String> emptyTokenMap = null;
	JavaFakerUser faker = new JavaFakerUser();

	@BeforeTest
	public void setUp() throws IOException {
		emptyTokenMap = new HashMap<String, String>();
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void signUpAccount_Test() {
		SignUp user = new SignUp();
		user.setFirstname(faker.getUserFirstName());
		user.setEmail(faker.getRandomEmailId());
		user.setPassword(faker.getPassword());
		user.setLocale(faker.getLocale());
		SignUpJson signupJson = new SignUpJson();
		signupJson.setUser(user);

		Response response = RestClient.doPost("JSON", albatrossURL, "sign-up", emptyTokenMap, null, true, signupJson);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("status"), "success");
		Assert.assertEquals(jsonPath.getString("message"), "Signup Successful ");

		String[] jsonKeys = { "firstname", "email", "locale" };
		String[] expectedValues = { user.getFirstname(), user.getEmail(), user.getLocale() };
		for (int i = 0; i < jsonKeys.length; i++) {
			Assert.assertEquals(jsonPath.getString("user." + jsonKeys[i]), expectedValues[i],
					"Failed at asserting " + jsonKeys[i]);
		}

		int accountId = jsonPath.get("user.accountid");
		ReaperIntegration.logAccountEntry(accountId);
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void signUpAccountWithSameEmailId_Test() {
		String emailId = faker.getRandomEmailId();
		SignUp user = new SignUp();
		user.setFirstname(faker.getUserFirstName());
		user.setEmail(emailId);
		user.setPassword(faker.getPassword());
		user.setLocale(faker.getLocale());
		SignUpJson signupJson = new SignUpJson();
		signupJson.setUser(user);

		Response response = RestClient.doPost("JSON", albatrossURL, "sign-up", emptyTokenMap, null, true, signupJson);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("message"), "Signup Successful ");

		SignUp user1 = new SignUp();
		user1.setFirstname(faker.getUserFirstName());
		user1.setEmail(emailId);
		user1.setPassword(faker.getPassword());
		user1.setLocale(faker.getLocale());
		SignUpJson signupJson1 = new SignUpJson();
		signupJson1.setUser(user1);

		Response response1 = RestClient.doPost("JSON", albatrossURL, "sign-up", emptyTokenMap, null, true, signupJson);

		JsonPath jsonPath1 = response1.jsonPath();

		Assert.assertEquals(response1.getStatusCode(), 200);
		Assert.assertEquals(jsonPath1.getString("message"),
				"Failed To Signup : User with this Email ID already exists.");

		int accountId = jsonPath.get("user.accountid");
		ReaperIntegration.logAccountEntry(accountId);
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void signUpAccountWithEmptyRequestBody_Test() {
		SignUp user = new SignUp();
		user.setFirstname("");
		user.setEmail("");
		user.setPassword("");
		user.setLocale("");
		SignUpJson signupJson = new SignUpJson();
		signupJson.setUser(user);

		Response response = RestClient.doPost("JSON", albatrossURL, "sign-up", emptyTokenMap, null, true, signupJson);

		JsonPath jsonPath = response.jsonPath();
		String responseMessage = jsonPath.get("message");

		Assert.assertEquals(response.getStatusCode(), 422);
		String requiredFields[] = new String[] { "user.firstname", "user.email", "user.password", "user.locale" };
		for (String field : requiredFields) {
			Assert.assertTrue(responseMessage.contains("The " + field + " field is required"));
		}
		Assert.assertEquals(jsonPath.getString("action_name"), "Signup");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void signUpAccountWithInvalidToken_Test() {
		SignUp user = new SignUp();
		user.setFirstname(faker.getUserFirstName());
		user.setEmail(faker.getUserEmail());
		user.setPassword(faker.getPassword());
		user.setLocale(faker.getLocale());
		SignUpJson signupJson = new SignUpJson();
		signupJson.setUser(user);
		signupJson.setInviteuser(faker.getPassword());

		Response response = RestClient.doPost("JSON", albatrossURL, "sign-up", emptyTokenMap, null, true, signupJson);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("message"),
				"Failed To Signup : Invalid link. Ask your Recruit CRM Admin to resend invitation");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getIntercomSettingsWithoutAuthorization_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, "get-intercom-settings", emptyTokenMap, null, null,
				true);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("status"), "success");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getIntercomSettingsWithAuthorization_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, "get-intercom-settings",
				ThreadManager.getOwnerAlbatrossToken(), null, null, true);

		String responseBody = response.getBody().asString();
		JsonPath jsonPath = new JsonPath(responseBody);
		Map<String, Object> user = jsonPath.getMap("user");

		Assert.assertEquals(response.getStatusCode(), 200);
		String[] requiredFields = { "id", "firstname", "email", "role", "country", "accountid" };
		for (String field : requiredFields) {
			Assert.assertTrue(user.containsKey(field));
		}
		Assert.assertEquals(jsonPath.getString("status"), "success");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createAccount_Test() {
		Account account = new Account();
		account.setTitle(faker.getUserAccountName());
		account.setTimezone(faker.getTimezone());
		AccountJson accountJson = new AccountJson();
		accountJson.setAccount(account);

		Response response = RestClient.doPost("JSON", albatrossURL, "accounts", ThreadManager.getOwnerAlbatrossToken(),
				null, true, accountJson);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("status"), "success");
		Assert.assertEquals(jsonPath.getString("message"), "Account Created");
		Assert.assertEquals(jsonPath.getString("action_name"), "Account Create");

	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createAccountWithEmptyRequestBody_Test() {
		Account account = new Account();
		account.setTitle("");
		account.setTimezone(0);
		AccountJson accountJson = new AccountJson();
		accountJson.setAccount(account);

		Response response = RestClient.doPost("JSON", albatrossURL, "accounts", ThreadManager.getOwnerAlbatrossToken(),
				null, true, accountJson);

		JsonPath jsonPath = response.jsonPath();
		String responseMessage = jsonPath.get("message");

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertTrue(responseMessage.contains("account.title field is required"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotCreateAccount_Test() {
		Account account = new Account();
		account.setTitle(faker.getUserAccountName());
		account.setTimezone(faker.getTimezone());
		AccountJson accountJson = new AccountJson();
		accountJson.setAccount(account);

		Response response = RestClient.doPost("JSON", albatrossURL, "accounts", emptyTokenMap, null, true, accountJson);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}
}
