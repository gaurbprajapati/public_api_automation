package io.rcrm.api.externalJobBoards.broadbean;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerExternalJobBoards;
import io.rcrm.api.pojo.externalJobBoards.broadbean.BroadbeanAdcUserName;
import io.rcrm.api.pojo.externalJobBoards.broadbean.BroadbeanJobBoard;
import io.rcrm.api.pojo.externalJobBoards.broadbean.BroadbeanJobBoardSetting;
import io.rcrm.api.pojo.externalJobBoards.broadbean.BroadbeanPermission;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndpointsOfBroadbeanExternalJobBoardsTest extends TestBase {
	JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();
	String broadbeanAccountAlias = javaFakerJobBoards.getAccountAlias();
	String broadbeanClientId = javaFakerJobBoards.getClientId();
	String broadbeanSecret = javaFakerJobBoards.getSecret();
	String broadbeanAdcUsername = javaFakerJobBoards.getAdcUsername();
	String createdOn = javaFakerJobBoards.getCreatedOnTimestampInSecs();

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void saveBroadbeanJobBoardSetting_POST() {
		BroadbeanJobBoard broadbeanJobBoard = new BroadbeanJobBoard();
		BroadbeanJobBoardSetting broadbeanJobBoardSetting = new BroadbeanJobBoardSetting(broadbeanAccountAlias,
				broadbeanClientId, broadbeanSecret, broadbeanAdcUsername, "emailapply", "0", "1", "2", "3");

		broadbeanJobBoard.setJob_board_id(4);
		broadbeanJobBoard.setSettings(broadbeanJobBoardSetting);
		broadbeanJobBoard.setCreatedOn(new String[] { createdOn });

		String basePath = "/jobboards/settings/save";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				broadbeanJobBoard);


		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"publicApi//externalJobBoards//broadbeanExternalJobBoard//saveBroadbeanJobBoardSetting.json"));
		response.then().body("id", notNullValue());
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void editBroadbeanJobBoardSetting_POST() {
		saveBroadbeanJobBoardSetting();
		BroadbeanJobBoard broadbeanJobBoard = new BroadbeanJobBoard();
		BroadbeanJobBoardSetting broadbeanJobBoardSetting = new BroadbeanJobBoardSetting(broadbeanAccountAlias + "edited",
				broadbeanClientId, broadbeanSecret, broadbeanAdcUsername, "emailapply", "0", "1", "2", "3");

		broadbeanJobBoard.setJob_board_id(4);
		broadbeanJobBoard.setSettings(broadbeanJobBoardSetting);
		broadbeanJobBoard.setCreatedOn(new String[] { createdOn });
		String basePath = "/jobboards/settings/save";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				broadbeanJobBoard);

		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"publicApi//externalJobBoards//broadbeanExternalJobBoard//saveBroadbeanJobBoardSetting.json"));
		response.then().body("id", notNullValue());
		response.then().body("job_board_id", is(4));
	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void getBroadbeanJobDetailsById_GET() {
		saveBroadbeanJobBoardSetting();
		Map<String, String> pathParamters = new HashMap<>();
		pathParamters.put("jobboard_id", String.valueOf(4));

		String basePath = "/jobboards/settings/{jobboard_id}";
		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters,
				true);

		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"publicApi//externalJobBoards//broadbeanExternalJobBoard//saveBroadbeanJobBoardSetting.json"));
		response.then().body("id", Matchers.notNullValue());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void addBroadbeanJobBoardPermission_POST() {
		saveBroadbeanJobBoardSetting();
		BroadbeanPermission broadbeanJobBoard = new BroadbeanPermission("1");

		String basePath = "/broadbean/permission";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				broadbeanJobBoard);

		response.then().statusCode(200);
		response.then().body("message", is("Broadbean User Permission Updated Successfully"));
		response.then().body("status", is("success"));
		response.then().body("message_type", is("is-success"));
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void getBroadbeanJobBoardPermission_GET() {
		saveBroadbeanJobBoardSetting();
		enableBroadbeanJobBoardPermission();
		String basePath = "/broadbean/get-permission";
		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null,
				true);

		response.then().statusCode(200);
		response.then().body("data", is(1));
		response.then().body("status", is("success"));
		response.then().body("message_type", is("is-success"));
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void getBroadbeanConnectedId_GET() {
		saveBroadbeanJobBoardSetting();
		String basePath = "/broadbean/get-connected-account/adc_popup";
		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null,
				true);

		response.then().statusCode(200);
		response.then().body("data[0].id", notNullValue());
	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void mapAdcUsernameFromBroadbeanAccount_POST() {
		int connectionId = getBroadbeanConnectedId();
		BroadbeanAdcUserName broadbeanAdcUserName = new BroadbeanAdcUserName(connectionId, broadbeanAdcUsername + " mapped");

		String basePath = "/broadbean/map-adcusername/map";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				broadbeanAdcUserName);

		response.then().statusCode(200);
		response.then().body("message", is("ADC Username mapped successfully"));
		response.then().body("status", is("success"));
		response.then().body("message_type", is("is-success"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void removeAdcUsernameFromBroadbeanAccount_POST() {
		int connectionId = getBroadbeanConnectedId();
		mapAdcUsernameFromBroadbeanAccount(connectionId);
		BroadbeanAdcUserName broadbeanAdcUserName = new BroadbeanAdcUserName(connectionId, broadbeanAdcUsername + " mapped");

		String basePath = "/broadbean/map-adcusername/remove";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				broadbeanAdcUserName);

		response.then().statusCode(200);
		response.then().body("message", is(broadbeanAdcUsername + " mapped" + " ADC Username mapping removed successfully"));
		response.then().body("status", is("success"));
		response.then().body("message_type", is("is-success"));
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void getBroadbeanJobBoardDetails_GET() {
		saveBroadbeanJobBoardSetting();
		String basePath = "/broadbean/get-connected-account/job_detail";
		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null,
				true);

		response.then().statusCode(200);
		response.then().body("data[0].id", notNullValue());
		response.then().body("data[0].job_board_id", notNullValue());
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void deleteBroadbeanJobBoard_DELETE() {
		int id = saveBroadbeanJobBoardSetting();
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("jobboard_id", String.valueOf(id));
		String basePath = "/jobboards/settings/{jobboard_id}";

		Response response = RestClient.doDelete("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParameters, true);
		response.then().statusCode(200);
		response.then().body("status_message", is("success"));
	}


	public int saveBroadbeanJobBoardSetting() {
		BroadbeanJobBoard broadbeanJobBoard = new BroadbeanJobBoard();
		BroadbeanJobBoardSetting broadbeanJobBoardSetting = new BroadbeanJobBoardSetting(broadbeanAccountAlias,
				broadbeanClientId, broadbeanSecret, broadbeanAdcUsername, "emailapply", "0", "1", "2", "3");
		broadbeanJobBoard.setJob_board_id(4);
		broadbeanJobBoard.setSettings(broadbeanJobBoardSetting);
		broadbeanJobBoard.setCreatedOn(new String[] { createdOn });

		String basePath = "/jobboards/settings/save";
		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				broadbeanJobBoard);

		response.then().statusCode(200);
		JsonPath jp = response.jsonPath();
		return jp.get("id");
	}

	public void enableBroadbeanJobBoardPermission() {
		saveBroadbeanJobBoardSetting();
		BroadbeanPermission broadbeanJobBoard = new BroadbeanPermission("1");

		String basePath = "/broadbean/permission";
		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				broadbeanJobBoard);
		response.then().statusCode(200);
	}

	public int getBroadbeanConnectedId() {
		saveBroadbeanJobBoardSetting();
		String basePath = "/broadbean/get-connected-account/adc_popup";
		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null,
				true);

		response.then().statusCode(200);
		return response.jsonPath().get("data[0].id");
	}

	public void mapAdcUsernameFromBroadbeanAccount(int connectionId) {
		BroadbeanAdcUserName broadbeanAdcUserName = new BroadbeanAdcUserName(connectionId, broadbeanAdcUsername + " mapped");

		String basePath = "/broadbean/map-adcusername/map";
		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				broadbeanAdcUserName);
		response.then().statusCode(200);
	}

}
