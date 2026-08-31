package io.rcrm.api.externalJobBoards.logicmelon;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.externalJobBoards.LogicmelonJobBoardFunctions;
import io.rcrm.api.javafaker.JavaFakerExternalJobBoards;
import io.rcrm.api.pojo.externalJobBoards.logicmelon.LogicmelonExternalJobBoard;
import io.rcrm.api.pojo.externalJobBoards.logicmelon.LogicmelonJobBoardSetting;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditLogicmelonJobboardTest extends TestBase {
	JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();
	String logicmelon_username = javaFakerJobBoards.getEmailAddress();
	String logicmelon_password = javaFakerJobBoards.getPassword();
	String logicmelon_apikey = javaFakerJobBoards.getFakerApikey();
	int jobBoardId_global;
	String jobBoardSettingId_global;

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "saveLogicmelonJobBoardSettingsData", groups = "nightly-build")
	public void editLogicmelonJobBoardSetting(int jobBoardId, String jobBoardSettingId) {
		LogicmelonExternalJobBoard logicmelonJobBoard = new LogicmelonExternalJobBoard();
		LogicmelonJobBoardSetting logicmelonJobBoardSetting = new LogicmelonJobBoardSetting();

		jobBoardId_global = jobBoardId;
		jobBoardSettingId_global = jobBoardSettingId;

		logicmelonJobBoardSetting.setUsername("new." + logicmelon_username);
		logicmelonJobBoardSetting.setPassword("pass." + logicmelon_password);
		logicmelonJobBoardSetting.setApikey(logicmelon_apikey);

		logicmelonJobBoard.setJob_board_id(jobBoardId);
		logicmelonJobBoard.setSettings(logicmelonJobBoardSetting);
		logicmelonJobBoard.setEnable_logicmelon_to_accounts_user(javaFakerJobBoards.getEnable_logicmelon());

		String basePath = "/jobboards/settings/save";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				logicmelonJobBoard);


		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"publicApi//externalJobBoards//logicmelonExternalJobBoard//saveLogicmelonJobBoardSetting.json"));
		response.then().body("id", notNullValue());
		response.then().body("job_board_id", is(2));
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "editLogicmelonJobBoardSetting", groups = "nightly-build")
	public void unAuthorizedLogicmelonJobBoard() {
		LogicmelonExternalJobBoard logicmelonJobBoard = new LogicmelonExternalJobBoard();
		LogicmelonJobBoardSetting logicmelonJobBoardSetting = new LogicmelonJobBoardSetting();

		logicmelonJobBoardSetting.setUsername("new." + logicmelon_username);
		logicmelonJobBoardSetting.setPassword("pass." + logicmelon_password);
		logicmelonJobBoardSetting.setApikey(logicmelon_apikey);

		logicmelonJobBoard.setJob_board_id(jobBoardId_global);
		logicmelonJobBoard.setSettings(logicmelonJobBoardSetting);
		logicmelonJobBoard.setEnable_logicmelon_to_accounts_user(javaFakerJobBoards.getEnable_logicmelon());

		String basePath = "/jobboards/settings/save";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true,
				logicmelonJobBoard);


		response.then().statusCode(401);
		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "editLogicmelonJobBoardSetting", groups = "nightly-build")
	public void deleteLogicmelonJobBoard() {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("jobboard_id", String.valueOf(jobBoardSettingId_global));
		String basePath = "/jobboards/settings/{jobboard_id}";

		Response response = RestClient.doDelete("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParameters, true);

		response.then().statusCode(200);
		response.then().body("status_message", is("success"));
	}

	@DataProvider
	public Object[][] saveLogicmelonJobBoardSettingsData() {
		String basePath = "/jobboards/settings/save";

		LogicmelonJobBoardFunctions logicmelonJobBoardFunctions = new LogicmelonJobBoardFunctions();

		Response response = logicmelonJobBoardFunctions.createLogicmelonJobBoardFunctions(basePath, jobBoardServiceURL,
				ThreadManager.getOwnerAlbatrossToken(), logicmelon_apikey);
		JsonPath jp = response.jsonPath();
		int jobBoardId = jp.get("job_board_id");
		String jobBoardSettingId = Integer.toString(jp.get("id"));

		response.then().statusCode(200);
		response.then().body("id", notNullValue());

		Object data[][] = { { jobBoardId, jobBoardSettingId } };

		return data;
	}
}
