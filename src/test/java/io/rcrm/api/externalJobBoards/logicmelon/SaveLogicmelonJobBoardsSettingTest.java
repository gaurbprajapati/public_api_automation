package io.rcrm.api.externalJobBoards.logicmelon;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.hamcrest.Matchers;
import org.testng.Assert;

import io.rcrm.api.javafaker.JavaFakerExternalJobBoards;
import io.rcrm.api.pojo.externalJobBoards.logicmelon.LogicmelonExternalJobBoard;
import io.rcrm.api.pojo.externalJobBoards.logicmelon.LogicmelonJobBoardSetting;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class SaveLogicmelonJobBoardsSettingTest extends TestBase {
	JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();

	String logicmelon_username = javaFakerJobBoards.getEmailAddress();
	String logicmelon_password = javaFakerJobBoards.getPassword();
	String logicmelon_apikey = javaFakerJobBoards.getFakerApikey();

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void saveLogicmelonJobBoardSettings() {
		LogicmelonExternalJobBoard jobBoard = new LogicmelonExternalJobBoard();
		LogicmelonJobBoardSetting jobBoardSettings = new LogicmelonJobBoardSetting(logicmelon_username,
				logicmelon_password, logicmelon_apikey);

		jobBoard.setJob_board_id(2);
		jobBoard.setSettings(jobBoardSettings);
		jobBoard.setEnable_logicmelon_to_accounts_user(javaFakerJobBoards.getEnable_logicmelon());
		;

		String basePath = "/jobboards/settings/save";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, jobBoard);


		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"publicApi//externalJobBoards//logicmelonExternalJobBoard//saveLogicmelonJobBoardSetting.json"));
		response.then().body("id", notNullValue());
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void nullResponseInAnyFieldAfterSavingLogicmelonJobBoardSettings() {
		LogicmelonExternalJobBoard jobBoard = new LogicmelonExternalJobBoard();
		LogicmelonJobBoardSetting jobBoardSettings = new LogicmelonJobBoardSetting(logicmelon_username,
				logicmelon_password, logicmelon_apikey);

		jobBoard.setJob_board_id(2);
		jobBoard.setSettings(jobBoardSettings);
		jobBoard.setEnable_logicmelon_to_accounts_user(javaFakerJobBoards.getEnable_logicmelon());

		String basePath = "/jobboards/settings/save";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, jobBoard);


		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"publicApi//externalJobBoards//logicmelonExternalJobBoard//saveLogicmelonJobBoardSetting.json"));
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void unAuthorizedUserCannotAccessSaveLogicmelonJobBoardSettings() {
		LogicmelonExternalJobBoard jobBoard = new LogicmelonExternalJobBoard();
		LogicmelonJobBoardSetting jobBoardSettings = new LogicmelonJobBoardSetting(logicmelon_username,
				logicmelon_password, logicmelon_apikey);

		jobBoard.setJob_board_id(2);
		jobBoard.setSettings(jobBoardSettings);

		String basePath = "/jobboards/settings/save";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true,
				jobBoard);


		response.then().statusCode(401);
		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}
}
