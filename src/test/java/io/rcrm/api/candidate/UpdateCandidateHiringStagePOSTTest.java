package io.rcrm.api.candidate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerTask;
import io.rcrm.api.pojo.HiringStage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class UpdateCandidateHiringStagePOSTTest extends TestBase {

	public UpdateCandidateHiringStagePOSTTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";

	JavaFakerTask fakerTask = new JavaFakerTask();
	String taskTitle = fakerTask.getTaskName();
	String startDate = fakerTask.getFutureDate();

	commanFunction function = new commanFunction();

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void authorizedUserCanUpdateCandidateHiringStage() {

		String candidateSlug;
		String jobSlug;
		JsonPath json;
		JsonPath jsonAssigneCandidate;

		try {

			jsonAssigneCandidate = function.assignCandidateToJob(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			candidateSlug = jsonAssigneCandidate.get("candidate_slug");
			jobSlug = jsonAssigneCandidate.get("job_slug");
			json = function.getAllHiringStages(baseURL, ThreadManager.getAccountApiKey()).jsonPath();

		} catch (Exception e) {
			jsonAssigneCandidate = function.assignCandidateToJob(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			candidateSlug = jsonAssigneCandidate.get("candidate_slug");
			jobSlug = jsonAssigneCandidate.get("job_slug");
			json = function.getAllHiringStages(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		}

		int getStatusIdString = json.get("status_id[0]");
		// int getStatusId=Integer.parseInt(getStatusIdString);
		int userId = jsonAssigneCandidate.get("updated_by");
		HiringStage hiringStage = new HiringStage();
		hiringStage.setRemark(taskTitle);
		hiringStage.setStage_date(startDate);
		hiringStage.setStatus_id(getStatusIdString);
		hiringStage.setUpdated_by(userId);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);
		pathParamters.put("job", jobSlug);

		String basePath = "candidates/{candidate}/hiring-stages/{job}";

//		Map<String, String> queryParameters = new HashMap<String, String>();
//		queryParameters.put("job_slug", jobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				hiringStage);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("candidate_slug", Matchers.is(candidateSlug));
		response.then().body("job_slug", Matchers.is(jobSlug));

		// response.then().body("status.status_id", Matchers.is(1));
		// response.then().body("status.label", Matchers.is("Assigned"));
		// response.then().body("remark", Matchers.is("Updated"));
		response.then().body("visibility", Matchers.is(1));
		response.then().body("stage_date", Matchers.notNullValue());

		response.then().body("updated_on", Matchers.notNullValue());
		response.then().body("updated_by", Matchers.notNullValue());

	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void usershouldNotBeAbleToUpdateHiringStageOfCandidateIfCandididateIsNotAssigned() {

		String candidateSlug;
		String jobSlug;
		JsonPath json;

		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
			json = function.getAllHiringStages(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		} catch (Exception e) {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
			json = function.getAllHiringStages(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		}

		int getStatusIdString = json.get("status_id[0]");

		HiringStage hiringStage = new HiringStage();
		hiringStage.setRemark(taskTitle);
		hiringStage.setStage_date(startDate);
		hiringStage.setStatus_id(getStatusIdString);
		

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);
		pathParamters.put("job", jobSlug);

		String basePath = "candidates/{candidate}/hiring-stages/{job}";
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				hiringStage);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("errorMessage", Matchers.is("No such assignment exist"));
		response.then().body("errorCode", Matchers.is(404));
		// response.then().body("candidate_slug", Matchers.is(candidateSlug));

	}

	@Owner("Yash Rampal")
	@Test(dataProvider = "getInvalidSlugs", groups = "nightly-build")
	public void usershouldNotBeAbleToUpdateHiringStageOfWithInvalidCandidateJobSlug(String Candidateslug,
			String JobSlug, String errorMessage, int getStatusID) {

		HiringStage hiringStage = new HiringStage();
		hiringStage.setRemark(taskTitle);
		hiringStage.setStage_date(startDate);
		hiringStage.setStatus_id(getStatusID);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", Candidateslug);
		pathParamters.put("job", JobSlug);

		String basePath = "candidates/{candidate}/hiring-stages/{job}";
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				hiringStage);


		if (getStatusID == 1000123499) {
			Assert.assertEquals(response.getStatusCode(), 422);
		} else {
			Assert.assertEquals(response.getStatusCode(), 200);
		}

		if (errorMessage.contains("doesn't exist")) {
			if (getStatusID == 1000123499) {
				response.then().body("status_id[0]", Matchers.is("Invalid status id"));
			} else {
				response.then().body("errorMessage", Matchers.is(errorMessage));
				response.then().body("errorCode", Matchers.is(404));
			}
		}
		if (errorMessage.contains("200")) {
			response.then().body("errorMessage", Matchers.is("No such assignment exist"));
			response.then().body("errorCode", Matchers.is(404));
		}

	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getAllHiringStagesIds", groups = "nightly-build")
	public void userShouldUpdateToEachHiringStageOfCandidateToJob(String Candidateslug, String JobSlug,
			int getStatusID) {

		HiringStage hiringStage = new HiringStage();
		hiringStage.setRemark(taskTitle);
		hiringStage.setStage_date(startDate);
		hiringStage.setStatus_id(getStatusID);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", Candidateslug);
		pathParamters.put("job", JobSlug);

		String basePath = "candidates/{candidate}/hiring-stages/{job}";
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				hiringStage);


		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("candidate_slug", Matchers.is(Candidateslug));
		response.then().body("job_slug", Matchers.is(JobSlug));

		response.then().body("status.status_id", Matchers.is(getStatusID));
		// response.then().body("status.label", Matchers.is("Assigned"));
		// response.then().body("remark", Matchers.is("Updated"));
		response.then().body("visibility", Matchers.is(1));
		response.then().body("stage_date", Matchers.notNullValue());

		response.then().body("updated_on", Matchers.notNullValue());
		response.then().body("updated_by", Matchers.notNullValue());
	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void authorizedUserCanUpdateCandidateHiringStageWithInvalidUpdatedBy() {

		String candidateSlug;
		String jobSlug;
		JsonPath json;
		JsonPath jsonAssigneCandidate;

		try {

			jsonAssigneCandidate = function.assignCandidateToJob(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			candidateSlug = jsonAssigneCandidate.get("candidate_slug");
			jobSlug = jsonAssigneCandidate.get("job_slug");
			json = function.getAllHiringStages(baseURL, ThreadManager.getAccountApiKey()).jsonPath();

		} catch (Exception e) {
			jsonAssigneCandidate = function.assignCandidateToJob(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			candidateSlug = jsonAssigneCandidate.get("candidate_slug");
			jobSlug = jsonAssigneCandidate.get("job_slug");
			json = function.getAllHiringStages(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		}

		int getStatusIdString = json.get("status_id[0]");
		int userId = jsonAssigneCandidate.get("updated_by");
		HiringStage hiringStage = new HiringStage();
		hiringStage.setRemark(taskTitle);
		hiringStage.setStage_date(startDate);
		hiringStage.setStatus_id(getStatusIdString);
		hiringStage.setUpdated_by(userId+12);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);
		pathParamters.put("job", jobSlug);

		String basePath = "candidates/{candidate}/hiring-stages/{job}";

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				hiringStage);
		response.then().body("errorCode", Matchers.is(404));
		response.then().body("errorMessage", Matchers.is("Updated By Id is not valid"));

	}


	@DataProvider
	public Object[][] getInvalidSlugs() {

		String candidateSlug;
		String jobSlug;
		JsonPath json;

		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
			json = function.getAllHiringStages(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		} catch (Exception e) {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
			json = function.getAllHiringStages(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		}

		int getStatusIdString = json.get("status_id[0]");

		Object data[][] = { { candidateSlug, jobSlug, "200", getStatusIdString },
				{ "10xxx10", jobSlug, "Candidate doesn't exist", getStatusIdString },
				{ candidateSlug, "10xxx10", "Job doesn't exist", getStatusIdString },
				{ "10xxx10", "10xxx10", "Candidate doesn't exist", getStatusIdString },
				{ candidateSlug, "10xxx10", "Candidate doesn't exist", 1000123499 }, };
		return data;
	}

	@DataProvider
	public Object[][] getAllHiringStagesIds() {

		String candidateSlug;
		String jobSlug;
		JsonPath json;
		JsonPath jsonAssigneCandidate;

		try {

			jsonAssigneCandidate = function.assignCandidateToJob(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			candidateSlug = jsonAssigneCandidate.get("candidate_slug");
			jobSlug = jsonAssigneCandidate.get("job_slug");
			json = function.getAllHiringStages(baseURL, ThreadManager.getAccountApiKey()).jsonPath();

		} catch (Exception e) {
			jsonAssigneCandidate = function.assignCandidateToJob(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			candidateSlug = jsonAssigneCandidate.get("candidate_slug");
			jobSlug = jsonAssigneCandidate.get("job_slug");
			json = function.getAllHiringStages(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		}

		List<Integer> allHiringStageIds = json.get("status_id");
		for (int i = 0; i < allHiringStageIds.size(); i++) {
		}

		Object tests[][] = new Object[allHiringStageIds.size()][3]; // .read(json,"$..status_id");

		for (int i = 0; i < allHiringStageIds.size(); i++) {
			for (int j = 0; j < 3; j++) {
				if (j == 0) {
					tests[i][j] = candidateSlug;
				}
				if (j == 1) {
					tests[i][j] = jobSlug;
				}
				if (j == 2) {
					tests[i][j] = allHiringStageIds.get(i);
				}
			}
		}
		return tests;
	}

}
