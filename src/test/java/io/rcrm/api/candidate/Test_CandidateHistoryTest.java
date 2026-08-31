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
public class Test_CandidateHistoryTest extends TestBase{
	String slug = "";
	JavaFakerTask fakerTask = new JavaFakerTask();
	String taskTitle = fakerTask.getTaskName();
	String startDate = fakerTask.getFutureDate();

	commanFunction function = new commanFunction();
	
	@Owner("Sampurn Chouksey")
	@Test(dataProvider = "getAllHiringStagesIds", groups = "nightly-build")
	public void getAllHiringStagesIds_Test(String Candidateslug, String JobSlug,
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
		
		
		
		Map<String, String> pathParamter = new HashMap<String, String>();
		pathParamter.put("candidate", Candidateslug);
		String basePath1 = "candidates/{candidate}/history";


		Response response1 = RestClient.doGet("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, pathParamter, true);
		Assert.assertEquals(response1.getStatusCode(), 200);
		
		//response1.then().body("data[0].job_status_id", Matchers.is(getStatusID));
	}
	
	@Owner("Gaurav Prajapati")
	@Test
	public void getAllHiringStagesIDsForInvalidCandidateSlug_Test() {
		
//		Write scripts for this endpoint once This issue fixed
//		 https://rcrm.atlassian.net/browse/SS-1228

	}
	
	@Owner("Yash Rampal")
	@Test(dataProvider = "getInValidAPIKey")
	public void unAuthorizedUserCannotGetAllHiringStages_Test(int statusCode,
			String authTokenMapInValid) {
		

		Map<String, String> pathParamter = new HashMap<String, String>();
		pathParamter.put("candidate", "98765432567");
		String basePath1 = "candidates/{candidate}/history";


		Response response = RestClient.doGet("JSON", baseURL, basePath1, authTokenMapInValid, null, pathParamter, true);
		
		response.then().statusCode(statusCode);
		response.then().body("error", Matchers.containsString("Unauthorized"));
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
			if(i==3) {
				break;
			}
		}

		Object tests[][] = new Object[3][3]; // .read(json,"$..status_id");

		for (int i = 0; i < 3; i++) {
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
	
	@DataProvider
	public Object[][] getInValidAPIKey() {
		Object data[][] = { {401, ThreadManager.getAccountApiKey()+"x001" } };
		return data;
	}

}
