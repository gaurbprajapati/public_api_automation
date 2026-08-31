package io.rcrm.api.externalJobBoards.logicmelon;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
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
public class GetLogicmelonJobBoardByIdTest extends TestBase {
	JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();
	String logicmelon_username = javaFakerJobBoards.getEmailAddress();
	String logicmelon_password = javaFakerJobBoards.getPassword();
	String logicmelon_apikey = javaFakerJobBoards.getFakerApikey();
	int jobBoardId_global;

	@Owner("Sampurn Chouksey")
	@Test(dataProvider = "saveLogicmelonJobBoardSettingsData", groups = "nightly-build")
	public void getLogicmelonJobBoardDetailsByValidId(int jobBoardId) {
		jobBoardId_global = jobBoardId;

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("jobboard_id", String.valueOf(jobBoardId));
		String basePath = "/jobboards/settings/{jobboard_id}";

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters,
				true);


		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"publicApi//externalJobBoards//logicmelonExternalJobBoard//saveLogicmelonJobBoardSetting.json"));
		response.then().body("id", Matchers.notNullValue());
		response.then().body("account_id", Matchers.notNullValue());
		response.then().body("job_board_id", Matchers.notNullValue());
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "getLogicmelonJobBoardDetailsByValidId", groups = "nightly-build")
	public void getLogicmelonJobBoardDetailsByInvalidId() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("jobboard_id", String.valueOf(jobBoardId_global) + "123");

		String basePath = "/jobboards/settings/{jobboard_id}";

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters,
				true);


		response.then().body("error", Matchers.is(true));
		response.then().body("error_code", Matchers.is(404));
		response.then().body("error_message", Matchers.is("Job board detail not found"));
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "getLogicmelonJobBoardDetailsByValidId", groups = "nightly-build")
	public void unAuthorizedUserCannotAccessGetLogicmelonJobBoardByID() {
		Map<String, String> pathParamters = new HashMap<>();
		pathParamters.put("jobboard_id", String.valueOf(jobBoardId_global));
		String basePath = "/jobboards/settings/{jobboard_id}";

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null,
				pathParamters, true);


		response.then().statusCode(401);
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
		int jobBoardId = jp.get("job_board_id");
		;

		response.then().statusCode(200);
		response.then().body("id", notNullValue());

		Object data[][] = { { jobBoardId } };

		return data;
	}
}
