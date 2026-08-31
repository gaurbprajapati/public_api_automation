package io.rcrm.api.externalJobBoards.logicmelon;

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

import io.rcrm.api.commanfunctions.externalJobBoards.LogicmelonJobBoardFunctions;
import io.rcrm.api.javafaker.JavaFakerExternalJobBoards;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DeleteLogicmelonJobBoardSettingsTest extends TestBase {
	JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();
	String logicmelon_username = javaFakerJobBoards.getEmailAddress();
	String logicmelon_password = javaFakerJobBoards.getPassword();
	String logicmelon_apikey = javaFakerJobBoards.getFakerApikey();
	String jobBoardSettingId_global;

	@Owner("Rahul Shibu")
	@Test(dataProvider = "saveLogicmelonJobBoardSettingsData", groups = "nightly-build")
	public void deleteLogicmelonCredentialsOfJobBoardById(String jobBoardSettingId) {
		jobBoardSettingId_global = jobBoardSettingId;

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("jobboard_id", jobBoardSettingId);
		String basePath = "/jobboards/settings/{jobboard_id}";

		Response response = RestClient.doDelete("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParameters, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("id", Matchers.nullValue());
		response.then().body("status_code", Matchers.is(200));
		response.then().body("status_message", Matchers.is("success"));
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "deleteLogicmelonCredentialsOfJobBoardById", groups = "nightly-build")
	public void unAuthorizedUserCannotdeleteCredentialsOfLogicmelonJobBoardById() {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("jobboard_id", jobBoardSettingId_global);
		String basePath = "/jobboards/settings/{jobboard_id}";

		Response response = RestClient.doDelete("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null,
				pathParameters, true);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@DataProvider
	public Object[][] saveLogicmelonJobBoardSettingsData() {
		String basePath = "/jobboards/settings/save";

		LogicmelonJobBoardFunctions logicmelonJobBoardFunctions = new LogicmelonJobBoardFunctions();

		Response response = logicmelonJobBoardFunctions.createLogicmelonJobBoardFunctions(basePath, jobBoardServiceURL,
				ThreadManager.getOwnerAlbatrossToken(), logicmelon_apikey);
		JsonPath jp = response.jsonPath();
		String jobBoardSettingId = Integer.toString(jp.get("id"));

		response.then().statusCode(200);
		response.then().body("id", notNullValue());

		Object data[][] = { { jobBoardSettingId } };

		return data;
	}
}
