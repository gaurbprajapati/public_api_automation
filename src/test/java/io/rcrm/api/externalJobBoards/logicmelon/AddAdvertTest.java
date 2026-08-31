package io.rcrm.api.externalJobBoards.logicmelon;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.externalJobBoards.logicmelon.LogicmelonExternalJobBoard;
import io.rcrm.api.pojo.externalJobBoards.logicmelon.LogicmelonJobBoardSetting;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerExternalJobBoards;
import io.rcrm.api.pojo.externalJobBoards.logicmelon.LogicmelonJobBoard;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AddAdvertTest extends TestBase {
	JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();

	commanFunction commonfunction = new commanFunction();

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "createJobForPostingDataProvider", groups = "nightly-build")
	public void postingJobUsingAddAdvertLogicmelonTest(int job_id, String job_slug) {
		LogicmelonJobBoard logicmelonjobboard = new LogicmelonJobBoard();
		logicmelonjobboard.setJob_slug(job_slug);

		String basePath = "/logicmelon/add-advert";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				true, logicmelonjobboard);

		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//externalJobBoards//logicmelonExternalJobBoard//addAdvert.json"));
		response.then().body("RedirectUrl", notNullValue());
	}

	@Owner("Yash Rampal")
	@Test(dataProvider = "createJobForPostingDataProvider", groups = "nightly-build")
	public void nullResponseInAnyFieldAfterJobPostedUsingAddAdvertLogicmelonTest(int job_id, String job_slug) {
		LogicmelonJobBoard logicmelonjobboard = new LogicmelonJobBoard();
		logicmelonjobboard.setJob_slug(job_slug);

		String basePath = "/logicmelon/add-advert";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				true, logicmelonjobboard);

		response.then().statusCode(200);
		response.then().body("AdvertID", notNullValue());
		response.then().body("UserID", notNullValue());
		response.then().body("OrganisationID", notNullValue());
		response.then().body("RedirectUrl", notNullValue());
		// response.then().body("LocationLookupLogs", notNullValue());
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "createJobForPostingDataProvider", groups = "nightly-build")
	public void unAuthorizedUserCannotAccessLogicmelonTest(int job_id, String job_slug) {

		LogicmelonJobBoard logicmelonjobboard = new LogicmelonJobBoard();
		logicmelonjobboard.setJob_slug(job_slug);

		String basePath = "/logicmelon/add-advert";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123",
				null, true, logicmelonjobboard);

		response.then().statusCode(401);
	}
	
	@Owner("Sampurn Chouksey")
	@Test(dataProvider = "createJobForPostingDataProvider", groups = "nightly-build")
	public void postingJobUsingAddAdvertWithInvalidSlugLogicmelonTest(int job_id, String job_slug) {
		LogicmelonJobBoard logicmelonjobboard = new LogicmelonJobBoard();
		logicmelonjobboard.setJob_slug(job_slug + "abc");

		String basePath = "/logicmelon/add-advert";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				true, logicmelonjobboard);

		response.then().statusCode(404);
	}

	

	@DataProvider
	public Object[][] createJobForPostingDataProvider() {

		JsonPath json = commonfunction.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String company_slug = json.get("slug");

		JsonPath contactJp = commonfunction.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), company_slug).jsonPath();
		String contact_slug = contactJp.get("slug");

		JsonPath jobJp = commonfunction.createNewJob(baseURL, ThreadManager.getAccountApiKey(), company_slug, contact_slug).jsonPath();
		String job_slug = jobJp.get("slug");
		int job_id = jobJp.get("id");

		Object data[][] = { { job_id, job_slug } };
		saveLogicmelonCredentials();

		return data;
	}

	public void saveLogicmelonCredentials(){
		LogicmelonExternalJobBoard jobBoard = new LogicmelonExternalJobBoard();
		LogicmelonJobBoardSetting jobBoardSettings = new LogicmelonJobBoardSetting(logicmelon_username,
				logicmelon_password, logicmelon_apikey);
		jobBoard.setJob_board_id(2);
		jobBoard.setSettings(jobBoardSettings);
		jobBoard.setEnable_logicmelon_to_accounts_user(javaFakerJobBoards.getEnable_logicmelon());
		String basePath = "/jobboards/settings/save";
		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, true, jobBoard);

		response.then().statusCode(200);
	}

}
