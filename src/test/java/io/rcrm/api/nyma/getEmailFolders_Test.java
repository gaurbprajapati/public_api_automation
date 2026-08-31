package io.rcrm.api.nyma;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email1|Email2")
public class getEmailFolders_Test extends TestBase {

	@Owner("Harika")
	@Test(priority = 1, groups = "nightly-build")
	public void getFolder_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("view", "false");
		queryParameters.put("linked_email_type", "2");

		Response response = RestClient.doGet("JSON", nymaURLv3, "folders", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}

	@Owner("Harika")
	@Test(priority = 2, groups = "nightly-build")
	public void getFolderWithFolderView_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("view", "folder-view");

		Response response = RestClient.doGet("JSON", nymaURLv3, "folders", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
	}

	@Owner("Harika")
	@Test(priority = 3, groups = "nightly-build")
	public void getFolderInvalidAuth_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("view", "false");

		Response response = RestClient.doGet("JSON", nymaURLv3, "folders", ThreadManager.getOwnerAlbatrossToken()+"123", queryParameters, null, true);
		response.then().statusCode(401);
	}

	@Owner("Harika")
	@Test(priority = 4, groups = "nightly-build")
	public void getFolderWithFolderViewInvalidAuth_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("view", "folder-view");

		Response response = RestClient.doGet("JSON", nymaURLv3, "folders", ThreadManager.getOwnerAlbatrossToken()+"123", queryParameters, null, true);
		response.then().statusCode(401);
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void getFolderInvalidParamsTest() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("view", "false");
		queryParameters.put("linked_email_type", "3");

		Response response = RestClient.doGet("JSON", nymaURLv3, "folders", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		JsonPath responseJson = response.jsonPath();
		response.then().statusCode(422);
		Assert.assertTrue(responseJson.getString("message").contains("The selected linked email type is invalid."));
	}

}
