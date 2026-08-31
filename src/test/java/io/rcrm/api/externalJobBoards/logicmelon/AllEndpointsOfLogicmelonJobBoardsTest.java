package io.rcrm.api.externalJobBoards.logicmelon;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

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
public class AllEndpointsOfLogicmelonJobBoardsTest extends TestBase {
	JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();
	String logicmelon_username = javaFakerJobBoards.getEmailAddress();
	String logicmelon_password = javaFakerJobBoards.getPassword();
	String logicmelon_apikey = javaFakerJobBoards.getFakerApikey();
	int id;

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void saveLogicmelonJobBoardSetting_POST() {
		LogicmelonExternalJobBoard logicmelonJobBoard = new LogicmelonExternalJobBoard();
		LogicmelonJobBoardSetting logicmelonJobBoardSetting = new LogicmelonJobBoardSetting(logicmelon_username,
				logicmelon_password, logicmelon_apikey);

		logicmelonJobBoard.setJob_board_id(2);
		logicmelonJobBoard.setSettings(logicmelonJobBoardSetting);
		logicmelonJobBoard.setEnable_logicmelon_to_accounts_user(javaFakerJobBoards.getEnable_logicmelon());

		String basePath = "/jobboards/settings/save";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				logicmelonJobBoard);


		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"publicApi//externalJobBoards//logicmelonExternalJobBoard//saveLogicmelonJobBoardSetting.json"));
		response.then().body("id", notNullValue());

		JsonPath jp = response.jsonPath();
		id = jp.get("id");

	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "saveLogicmelonJobBoardSetting_POST", groups = "nightly-build")
	public void editLogicmelonJobBoardSetting_POST() {
		LogicmelonExternalJobBoard logicmelonJobBoard = new LogicmelonExternalJobBoard();
		LogicmelonJobBoardSetting logicmelonJobBoardSetting = new LogicmelonJobBoardSetting();

		logicmelonJobBoardSetting.setUsername("new" + logicmelon_username);
		logicmelonJobBoardSetting.setPassword("pass" + logicmelon_password);
		logicmelonJobBoardSetting.setApikey(logicmelon_apikey);

		logicmelonJobBoard.setJob_board_id(2);
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

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "saveLogicmelonJobBoardSetting_POST", groups = "nightly-build")
	public void getLogicmelonJobDetailsById_GET() {
		Map<String, String> pathParamters = new HashMap<>();
		pathParamters.put("jobboard_id", String.valueOf(2));

		String basePath = "/jobboards/settings/{jobboard_id}";

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters,
				true);


		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"publicApi//externalJobBoards//logicmelonExternalJobBoard//saveLogicmelonJobBoardSetting.json"));
		response.then().body("id", Matchers.notNullValue());
		response.then().body("account_id", Matchers.notNullValue());
		response.then().body("job_board_id", Matchers.notNullValue());
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = { "saveLogicmelonJobBoardSetting_POST", "editLogicmelonJobBoardSetting_POST" }, groups = "nightly-build")
	public void deleteLogicmelonJobBoard_DELETE() {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("jobboard_id", String.valueOf(id));
		String basePath = "/jobboards/settings/{jobboard_id}";

		Response response = RestClient.doDelete("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParameters, true);

		response.then().statusCode(200);
		response.then().body("status_message", is("success"));
	}
}
