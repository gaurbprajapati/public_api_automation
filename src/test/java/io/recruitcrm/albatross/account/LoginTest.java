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
import io.rcrm.api.javafaker.JavaFakerUser;
import io.rcrm.api.pojo.albatross.*;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class LoginTest extends TestBase {

	Map<String, String> emptyTokenMap = new HashMap<String, String>();
	JavaFakerUser faker = new JavaFakerUser();

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void loginWithValidCredentials_Test() {

		String emailId = ThreadManager.getAccount().getOwner().getEmail();
		String password = ThreadManager.getOwner().getPassword();

		Login login = new Login();
		login.setEmail(emailId);
		login.setPassword(password);

		Response response = RestClient.doPost("JSON", albatrossURL, "login", emptyTokenMap, null, true, login);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("status"), "success");
		Assert.assertEquals(jsonPath.getString("message"), "Login Successful");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void loginWithInvalidCredentials_Test() {

		Login login = new Login();
		login.setEmail(faker.getUserEmail());
		login.setPassword(faker.getPassword());

		Response response = RestClient.doPost("JSON", albatrossURL, "login", emptyTokenMap, null, true, login);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("status"), "fail");
		Assert.assertEquals(jsonPath.getString("message"),
				"Failed to Login : Please check your Email ID & Password, if you still can’t login, email us at support@recruitcrm.io");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void loginWithEmptyCredentials_Test() {

		String emailId = ThreadManager.getAccount().getOwner().getEmail();

		Login login = new Login();
		login.setEmail(emailId);

		Response response = RestClient.doPost("JSON", albatrossURL, "login", emptyTokenMap, null, true, login);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.getString("status"), "fail");
		Assert.assertEquals(jsonPath.getString("message"), "The password field is required.");
	}

}
