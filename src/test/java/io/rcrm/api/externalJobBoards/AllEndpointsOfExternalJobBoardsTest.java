package io.rcrm.api.externalJobBoards;

import java.util.*;
import static org.hamcrest.Matchers.*;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.externalJobBoards.CandidateFromJobBoard;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

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
public class AllEndpointsOfExternalJobBoardsTest extends TestBase {
	commanFunction function = new commanFunction();

	JavaFakerCandidate javaFakerCandidate = new JavaFakerCandidate();
	String candidateFacebookURL = javaFakerCandidate.getCandidateFacebookURL().replace(" ", "");
	String candidateTwitterURL = javaFakerCandidate.getCandidateTwitterURL().replace(" ", "");
	String candidateLinkedinURL = javaFakerCandidate.getCandidateLinkedinURL().replace(" ", "");
	String candidateGithubURL = javaFakerCandidate.getCandidateGithubURL().replace(" ", "");
	String candidateXingURL = javaFakerCandidate.getCandidateXingURL().replace(" ", "");

	JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();
	String email = javaFakerJobBoards.getEmailAddress();
	String password = javaFakerJobBoards.getPassword();
	int id;

	//specific for /candidates api
	String EXTERNAL_JOB_BOARDS_TOKEN = "7zPOWept9uSphD3bT93kDZEcHOZjJXQA9cRR27xgF8SszKQJJ1xlulgdPxDMtDf4";

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void saveJobBoardSettings_POST() {
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

		JsonPath jp = response.jsonPath();
		id = jp.get("id");
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "saveJobBoardSettings_POST", groups = "nightly-build")
	public void editJobBoardSettings_POST() {
		JobBoard jobBoard = new JobBoard();
		JobBoardSettings jobBoardSettings = new JobBoardSettings();

		jobBoardSettings.setUserEmail("new" + email);
		jobBoardSettings.setPassword("new" + password);

		jobBoard.setJob_board_id(javaFakerJobBoards.getJobBoardId());
		jobBoard.setSettings(jobBoardSettings);

		String basePath = "/jobboards/settings/save";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, jobBoard);


		response.then().statusCode(200);
		response.then().body("id", notNullValue());
		response.then().body("job_board_id", is(javaFakerJobBoards.getJobBoardId()));
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "saveJobBoardSettings_POST", groups = "nightly-build")
	public void getJobBoardDetailsById_GET() {
		Map<String, String> pathParamters = new HashMap<>();
		pathParamters.put("jobboard_id", String.valueOf(javaFakerJobBoards.getJobBoardId()));

		String basePath = "/jobboards/settings/{jobboard_id}";

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters,
				true);


		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("account_id", Matchers.notNullValue());
		response.then().body("job_board_id", Matchers.notNullValue());
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void getAllJobBoardsList_GET() {
		String basePath = "/jobboards/list";

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true);


		response.then().statusCode(200);
		response.then().body("job_board_id", notNullValue());
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = { "saveJobBoardSettings_POST", "getJobBoardDetailsById_GET" }, groups = "nightly-build")
	public void deleteJobBoard_DELETE() {
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("jobboard_id", String.valueOf(id));
		String basePath = "/jobboards/settings/{jobboard_id}";

		Response response = RestClient.doDelete("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParameters, true);

		response.then().statusCode(200);
		response.then().body("status_message", is("success"));
	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void addCandidateFromAJobBoard() {
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String companySlug = jsonCompany.get("slug");
		JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");
		JsonPath jsonJob = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug,contactSlug).jsonPath();
		String jobSlug = jsonJob.get("slug");

		CandidateFromJobBoard candidateFromJobBoard = new CandidateFromJobBoard();
		CandidateFromJobBoard.Candidate candidate = candidateFromJobBoard.new Candidate();
		candidate.setFirst_name(javaFakerCandidate.getFirstName());
		candidate.setLast_name(javaFakerCandidate.getLastName());
		candidate.setEmail_id(javaFakerCandidate.getEmailID());
		candidate.setContact_number(javaFakerCandidate.getContactNumber());
		candidate.setWork_experience_year(javaFakerCandidate.getWork_ex_year());
		candidate.setResume(javaFakerCandidate.getResume());
		candidate.setCurrent_organization_name(javaFakerCandidate.getCurrentOrganization());
		candidate.setAddress(javaFakerCandidate.getCandidateAddress());
		candidate.setCity(javaFakerCandidate.getCity());
		candidate.setState(javaFakerCandidate.getState());
		candidate.setCountry(javaFakerCandidate.getCountry());
		candidate.setProfile_facebook(candidateFacebookURL);
		candidate.setProfile_twitter(candidateTwitterURL);
		candidate.setProfile_linkedin(candidateLinkedinURL);
		candidate.setProfile_github(candidateGithubURL);
		candidate.setProfile_xing(candidateXingURL);
		candidate.setNotice_period_days(javaFakerCandidate.getNotice_period());
		candidate.setCandidate_dob(javaFakerCandidate.getDOB());

		candidateFromJobBoard.setJob_reference_id(jobSlug);
		candidateFromJobBoard.setCandidate(candidate);

		String basePath = "/candidate";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, EXTERNAL_JOB_BOARDS_TOKEN,
				null, true, candidateFromJobBoard);

		response.then().statusCode(200);
		response.then().body("success", is(true));
		response.then().body("message", is("Candidate Received for processing"));
		response.then().body("status_code", is(200));
	}
}
