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
public class GetJobBoardByIdTest extends TestBase {
	public GetJobBoardByIdTest() {
		super();
	}
	JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();

	String email = javaFakerJobBoards.getEmailAddress();
	String password = javaFakerJobBoards.getPassword();
	int jobBoardId_global;


	@Owner("Yash Rampal")
	@Test(dataProvider = "saveJobBoardSettingsData", groups = "nightly-build")
	public void getJobBoardDetailsByValidId_GET(int jobBoardId) {
		jobBoardId_global = jobBoardId;
		
		Map<String, String> pathParamters = new HashMap<>();
		pathParamters.put("jobboard_id", String.valueOf(jobBoardId_global));

		String basePath = "/jobboards/settings/{jobboard_id}";

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters,
				true);


		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("account_id", Matchers.notNullValue());
		response.then().body("job_board_id", Matchers.notNullValue());
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "getJobBoardDetailsByValidId_GET", groups = "nightly-build")
	public void getJobBoardDetailsByInvalidId_GET() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("jobboard_id", jobBoardId_global + "123");

		String basePath = "/jobboards/settings/{jobboard_id}";

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters,
				true);


		response.then().body("error", Matchers.is(true));
		response.then().body("error_code", Matchers.is(404));
		response.then().body("error_message", Matchers.is("Job board detail not found"));
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "getJobBoardDetailsByValidId_GET", groups = "nightly-build")
	public void getJobBoardDetailsByEmptyId_GET() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("jobboard_id", "");

		String basePath = "jobboards/settings/{jobboard_id}";

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters,
				true);

		Assert.assertEquals(response.getStatusCode(), 404);
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "getJobBoardDetailsByValidId_GET", groups = "nightly-build")
	public void unAuthorizedUserCannotAccessGetJobBoardByID() {
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
	public Object[][] saveJobBoardSettingsData() {

		String basePath = "/jobboards/settings/save";

		ExternalJobBoardFunctions externalJobBoardFunctions = new ExternalJobBoardFunctions();

		Response response = externalJobBoardFunctions.createExternalJobBoardResponse(basePath, jobBoardServiceURL,
				ThreadManager.getOwnerAlbatrossToken());
		JsonPath jp = response.jsonPath();
		int jobBoardId = jp.get("job_board_id");

		response.then().statusCode(200);
		response.then().body("id", notNullValue());

		Object data[][] = { { jobBoardId } };

		return data;
	}
}
