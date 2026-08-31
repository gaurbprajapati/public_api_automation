package io.recruitcrm.albatross.sso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.rcrm.api.javafaker.JavaFakerSSO;
import io.rcrm.api.pojo.albatross.SSOConfiguration;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class SSOConfigurationTest extends TestBase {
	JavaFakerSSO javaFakerSSO = new JavaFakerSSO();

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void updateSSOConfiguration_Test() {
		SSOConfiguration ssoConfig = new SSOConfiguration();
		ssoConfig.setClient_id(javaFakerSSO.getClientId());
		ssoConfig.setClient_secret(javaFakerSSO.getClientSecret());
		ssoConfig.setIs_google_idp(javaFakerSSO.getIsGoogleIdp());
		Response response = RestClient.doPost("JSON", albatrossURL, "sso/update-sso-configuration", ThreadManager.getOwnerAlbatrossToken(), null, true, ssoConfig);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("status"), "fail");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-danger");
		
		switch (ssoConfig.getIs_google_idp()) {
		case 1:
			Assert.assertEquals(jsonPath.getString("message"), "SSO Configuration is not valid");
			break;
		case 0:
			Assert.assertEquals(jsonPath.getString("message"), "Undefined array key \"authorization_url\"");
			break;
		default:
			break;
		}
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void updateSSOConfigurationWithEmptyRequestBody_Test() {
		SSOConfiguration ssoConfig = new SSOConfiguration();
		ssoConfig.setClient_id("");
		ssoConfig.setClient_secret("");
		ssoConfig.setIs_google_idp(javaFakerSSO.getIsGoogleIdp());
		Response response = RestClient.doPost("JSON", albatrossURL, "sso/update-sso-configuration", ThreadManager.getOwnerAlbatrossToken(), null, true, ssoConfig);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.getString("status"), "fail");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-danger");
		Assert.assertEquals(jsonPath.getString("message"), "Client ID is mandatory,Client Secret is mandatory");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotUpdateSSOConfiguration_Test() {
		SSOConfiguration ssoConfig = new SSOConfiguration();
		ssoConfig.setClient_id(javaFakerSSO.getClientId());
		ssoConfig.setClient_secret(javaFakerSSO.getClientSecret());
		ssoConfig.setIs_google_idp(javaFakerSSO.getIsGoogleIdp());
		Response response = RestClient.doPost("JSON", albatrossURL, "sso/update-sso-configuration", ThreadManager.getOwnerAlbatrossToken()+"123", null, true, ssoConfig);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getSSOConfiguration_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, "sso/get-sso-configuration", ThreadManager.getOwnerAlbatrossToken(), null, null, true);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("status"), "success");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetSSOConfiguration_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, "sso/get-sso-configuration", ThreadManager.getOwnerAlbatrossToken()+"123", null, null, true);
		
		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void disableSSOConfiguration_Test() {
		Response response = RestClient.doPost("JSON", albatrossURL, "sso/disable-sso-configuration", ThreadManager.getOwnerAlbatrossToken(), null, true, null);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("status"), "fail");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-danger");
		Assert.assertTrue(jsonPath.getString("message").contains("SSO is not enabled for this account"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotDisableSSOConfiguration_Test() {
		Response response = RestClient.doPost("JSON", albatrossURL, "sso/disable-sso-configuration", ThreadManager.getOwnerAlbatrossToken()+"123", null, true, null);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

}
