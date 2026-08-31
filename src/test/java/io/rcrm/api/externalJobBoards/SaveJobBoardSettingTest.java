package io.rcrm.api.externalJobBoards;

import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerExternalJobBoards;
import io.rcrm.api.pojo.externalJobBoards.JobBoard;
import io.rcrm.api.pojo.externalJobBoards.JobBoardSettings;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class SaveJobBoardSettingTest extends TestBase {
	JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();
	
	String email = javaFakerJobBoards.getEmailAddress();
	String password = javaFakerJobBoards.getPassword();

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void saveJobBoardSettings() {
		JobBoard jobBoard = new JobBoard();
		JobBoardSettings jobBoardSettings = new JobBoardSettings();

		jobBoardSettings.setUserEmail(email);
		jobBoardSettings.setPassword(password);

		jobBoard.setJob_board_id(javaFakerJobBoards.getJobBoardId());
		jobBoard.setSettings(jobBoardSettings);

		String basePath = "/jobboards/settings/save";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, jobBoard);


		response.then().statusCode(200);
		response.then().body("id", notNullValue());
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void nullResponseInAnyFieldAfterSavingJobBoardSettings() {
		JobBoard jobBoard = new JobBoard();
		JobBoardSettings jobBoardSettings = new JobBoardSettings();

		jobBoardSettings.setUserEmail(email);
		jobBoardSettings.setPassword(password);

		jobBoard.setJob_board_id(1);
		jobBoard.setSettings(jobBoardSettings);

		String basePath = "/jobboards/settings/save";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, jobBoard);


		response.then().statusCode(200);
		response.then().body("id", notNullValue());
		response.then().body("account_id", notNullValue());
		response.then().body("job_board_id", notNullValue());
		response.then().body("settings", notNullValue());
		response.then().body("created_on", notNullValue());
		response.then().body("created_by", notNullValue());
		response.then().body("updated_on", notNullValue());
		response.then().body("updated_by", notNullValue());
	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void unAuthorizedUserCannotAccessSaveJobBoardSettings() {
		JobBoard jobBoard = new JobBoard();
		JobBoardSettings jobBoardSettings = new JobBoardSettings();

		jobBoardSettings.setUserEmail(email);
		jobBoardSettings.setPassword(password);

		jobBoard.setJob_board_id(javaFakerJobBoards.getJobBoardId());
		jobBoard.setSettings(jobBoardSettings);

		String basePath = "/jobboards/settings/save";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true,
				jobBoard);


		response.then().statusCode(401);
		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

}