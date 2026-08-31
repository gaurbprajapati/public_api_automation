package io.rcrm.api.externalJobBoards;

import static org.hamcrest.Matchers.is;
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
import io.rcrm.api.commanfunctions.hiringPipelineService.HiringPipelineFunctions;
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
public class EditJobBoardTest extends TestBase {
	public EditJobBoardTest() {
		super();
	}
	JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();
	SaveJobBoardSettingTest saveJobBoardSetting = new SaveJobBoardSettingTest();

	String email = javaFakerJobBoards.getEmailAddress();
	String password = javaFakerJobBoards.getPassword();
	String jobBoardSettingId_global;
	int jobBoardId_global;

	@Owner("Rahul Shibu")
	@Test(dataProvider = "saveJobBoardSettingsData", groups = "nightly-build")
	public void editCredentialsOfJobBoard_POST(int jobBoardId, String jobBoardSettingId) {
		JobBoard jobBoard = new JobBoard();
		JobBoardSettings jobBoardSettings = new JobBoardSettings();
		
		jobBoardId_global = jobBoardId;
		jobBoardSettingId_global = jobBoardSettingId;

		jobBoardSettings.setUserEmail("new." + email);
		jobBoardSettings.setPassword("new." + password);

		jobBoard.setJob_board_id(jobBoardId);
		jobBoard.setSettings(jobBoardSettings);

		String basePath = "/jobboards/settings/save";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, jobBoard);


		response.then().statusCode(200);
		response.then().body("id", notNullValue());
		response.then().body("job_board_id", is(javaFakerJobBoards.getJobBoardId()));
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "editCredentialsOfJobBoard_POST", groups = "nightly-build")
	public void unAuthorizedUserCannoteditCredentialsOfJobBoard_POST() {
		JobBoard jobBoard = new JobBoard();
		JobBoardSettings jobBoardSettings = new JobBoardSettings();

		jobBoardSettings.setUserEmail("new." + email);
		jobBoardSettings.setPassword("new." + password);

		jobBoard.setJob_board_id(jobBoardId_global);
		jobBoard.setSettings(jobBoardSettings);

		String basePath = "/jobboards/settings/save";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true,
				jobBoard);


		response.then().statusCode(401);
		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "editCredentialsOfJobBoard_POST", groups = "nightly-build")
	public void deleteCredentialsOfJobBoardById() {

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

	@DataProvider
	public Object[][] saveJobBoardSettingsData() {

		String basePath = "/jobboards/settings/save";

		ExternalJobBoardFunctions externalJobBoardFunctions = new ExternalJobBoardFunctions();

		Response response = externalJobBoardFunctions.createExternalJobBoardResponse(basePath, jobBoardServiceURL,
				ThreadManager.getOwnerAlbatrossToken());
		JsonPath jp = response.jsonPath();
		int jobBoardId = jp.get("job_board_id");
		String jobBoardSettingId = Integer.toString(jp.get("id"));

		response.then().statusCode(200);
		response.then().body("id", notNullValue());

		Object data[][] = { { jobBoardId, jobBoardSettingId } };

		return data;
	}

}