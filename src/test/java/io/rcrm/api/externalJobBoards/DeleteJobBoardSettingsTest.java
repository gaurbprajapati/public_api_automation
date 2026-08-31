package io.rcrm.api.externalJobBoards;

import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.externalJobBoards.ExternalJobBoardFunctions;
import io.rcrm.api.javafaker.JavaFakerExternalJobBoards;
import io.rcrm.api.pojo.externalJobBoards.JobBoard;
import io.rcrm.api.pojo.externalJobBoards.JobBoardSettings;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DeleteJobBoardSettingsTest extends TestBase {

	public DeleteJobBoardSettingsTest() {
		super();
	}
	JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();

	String email = javaFakerJobBoards.getEmailAddress();
	String password = javaFakerJobBoards.getPassword();
	String jobBoardSettingId_global;

	@Owner("Rahul Shibu")
	@Test(dataProvider = "saveJobBoardSettingsData", groups = "nightly-build")
	public void deleteCredentialsOfJobBoardById(String jobBoardSettingId) {

		jobBoardSettingId_global = jobBoardSettingId;

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(jobBoardSettingId_global));

		String basePath = "/jobboards/settings/{id}";

		Response response = RestClient.doDelete("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("id", Matchers.nullValue());
		response.then().body("status_code", Matchers.is(200));
		response.then().body("status_message", Matchers.is("success"));
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "deleteCredentialsOfJobBoardById", groups = "nightly-build")
	public void deleteCredentialsOfJobBoardByInvalidId() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", jobBoardSettingId_global + "123");

		String basePath = "/jobboards/settings/{id}";

		Response response = RestClient.doDelete("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters,
				true);

		Assert.assertEquals(response.getStatusCode(), 404);

		response.then().body("error", Matchers.is(true));
		response.then().body("error_code", Matchers.is(404));
		response.then().body("error_message", Matchers.is("Job board detail not found"));
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "deleteCredentialsOfJobBoardById", groups = "nightly-build")
	public void deleteCredentialsOfJobBoardByEmptyId() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", "");

		String basePath = "/jobboards/settings/{id}";

		Response response = RestClient.doDelete("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters,
				true);

		Assert.assertEquals(response.getStatusCode(), 301);
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "deleteCredentialsOfJobBoardById", groups = "nightly-build")
	public void unAuthorizedUserCannotdeleteCredentialsOfJobBoardById() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(jobBoardSettingId_global));

		String basePath = "/jobboards/settings/{id}";

		Response response = RestClient.doDelete("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"12345", null,
				pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@DataProvider
	public Object[][] saveJobBoardSettingsData() {

		String basePath = "/jobboards/settings/save";

		ExternalJobBoardFunctions externalJobBoardFunctions = new ExternalJobBoardFunctions();

		Response response = externalJobBoardFunctions.createExternalJobBoardResponse(basePath, jobBoardServiceURL,
				ThreadManager.getOwnerAlbatrossToken());
		JsonPath jp = response.jsonPath();
		String jobBoardSettingId = Integer.toString(jp.get("id"));

		response.then().statusCode(200);
		response.then().body("id", notNullValue());

		Object data[][] = { { jobBoardSettingId } };

		return data;
	}
}