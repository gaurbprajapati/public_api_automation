package io.recruitcrm.albatross.sso;

import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;

import io.rcrm.api.pojo.albatross.Login;
import io.rcrm.api.pojo.albatross.UpdateFieldStatus;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class SSOLoginTest extends TestBase {

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void ssoLoginWithValidEmailId_Test() {
		Login login = new Login();
		login.setEmail(ThreadManager.getOwner().getEmail());

		Response response = RestClient.doPost("JSON", albatrossURL, "sso/login", ThreadManager.getOwnerAlbatrossToken()+"123", null, true, login);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("status"), "fail");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-danger");
		Assert.assertEquals(jsonPath.getString("action_name"), "SSO Login");
		Assert.assertTrue(jsonPath.getString("message").contains(
				"Failed To SSO Login : SSO is not configured for your account; log in via email and password."));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void ssoLoginWithInvalidEmailId_Test() {
		Faker faker = new Faker();
		Login login = new Login();
		login.setEmail(faker.internet().emailAddress());

		Response response = RestClient.doPost("JSON", albatrossURL, "sso/login", ThreadManager.getOwnerAlbatrossToken()+"123", null, true, login);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("status"), "fail");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-danger");
		Assert.assertEquals(jsonPath.getString("action_name"), "SSO Login");
		Assert.assertTrue(jsonPath.getString("message").contains("Failed To SSO Login : Please check your Email ID"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void ssoLoginWithEmptyRequestBody_Test() {
		Login login = new Login();
		login.setEmail("");

		Response response = RestClient.doPost("JSON", albatrossURL, "sso/login", ThreadManager.getOwnerAlbatrossToken()+"123", null, true, login);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.getString("status"), "fail");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-danger");
		Assert.assertTrue(jsonPath.getString("message").contains("The email field is required"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void enableSSOLoginWithEmailIdPassword_Test() {
		Faker faker = new Faker();
		UpdateFieldStatus updateFieldStatus = new UpdateFieldStatus();
		updateFieldStatus.setKey("allow_credentials_login");
		updateFieldStatus.setValue("1");
		updateFieldStatus.setTableFlag("account_settings");
		updateFieldStatus.setId(faker.number().randomNumber(5, true));

		Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", ThreadManager.getOwnerAlbatrossToken(), null, true, updateFieldStatus);

		JsonPath jsonPath = response.jsonPath();
		Map<String, Object> user = jsonPath.getMap("user");

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("status"), "success");
		Assert.assertTrue(jsonPath.getString("message").contains("Field Updated Successfully"));
		String[] requiredFields = { "id", "firstname", "email", "role", "country", "accountid" };
		for (String field : requiredFields) {
			Assert.assertTrue(user.containsKey(field));
		}
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void enableSSOLoginWithEmailIdPasswordWithEmptyRequestBody_Test() {
		UpdateFieldStatus updateFieldStatus = new UpdateFieldStatus();
		updateFieldStatus.setKey("");
		updateFieldStatus.setValue("0");
		updateFieldStatus.setTableFlag("");
		updateFieldStatus.setId(0);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", ThreadManager.getOwnerAlbatrossToken(), null, true, null);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.getString("status"), "fail");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-danger");
		Assert.assertTrue(jsonPath.getString("message").contains("The key field is required.,The table flag field is required."));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotEnableSSOLoginWithEmailIdPassword_Test() {
		Faker faker = new Faker();
		UpdateFieldStatus updateFieldStatus = new UpdateFieldStatus();
		updateFieldStatus.setKey("allow_credentials_login");
		updateFieldStatus.setValue("1");
		updateFieldStatus.setTableFlag("account_settings");
		updateFieldStatus.setId(faker.number().randomNumber(5, true));
		
		Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", ThreadManager.getOwnerAlbatrossToken()+"123", null, true, updateFieldStatus);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}
}
