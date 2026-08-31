package io.recruitcrm.hiringPipeline;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.hiringPipelineService.HiringPipelineFunctions;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.albatross.report.JavaFakerSavePerferences;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCandidateStagesAndCandidateCountsTest extends TestBase {

	public GetCandidateStagesAndCandidateCountsTest() {
		super();
		// TODO Auto-generated constructor stub
	}


	String hiringPipelineIdSave = "";
	commanFunction function = new commanFunction();
	ListFunctions listFunctions = new ListFunctions();

	JavaFakerSavePerferences reportFaker = new JavaFakerSavePerferences();
	String reportName = reportFaker.getReportName();

	@Owner("Sandeep")
	@Test(dataProvider = "getHiringPipelineValidData", groups = {"nightly-build", "hiring-pipeline-service"})
	public void getCandidateStagesStagesWiseCounts(String hiringPipelineID, int statusCode, String jobSlug) {
		
		hiringPipelineIdSave = hiringPipelineID;
		
		
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("jobid", jobSlug);
		String basePath = "job/pipeline-stages-candidate-count/{jobid}";
		
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("hiring_pipeline_id", "0");
		queryParameters.put("new_pipeline_id", hiringPipelineID);

		Response response = RestClient.doGet("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters,
				pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);
	}
	
	

	@Owner("Sandeep")
	@Test(groups = {"nightly-build", "hiring-pipeline-service"})  //(dataProvider = "getHiringPipelineValidData")
	public void unAuthorizedUserCannotAccessPipelineStagesCandidateCount() {
		
		
		
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("jobid", "12345");
		String basePath = "job/pipeline-stages-candidate-count/{jobid}";
		
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("hiring_pipeline_id", "0");
		queryParameters.put("new_pipeline_id", "12");

		Response response = RestClient.doGet("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"x001", queryParameters,
				pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 401);
	}
	
	@Owner("Sandeep")
	@Test(groups = {"nightly-build", "hiring-pipeline-service"})  //(dependsOnMethods = "getCandidateStagesStagesWiseCounts")
	public void deleteCustomHiringPipeline_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineIdSave);
		String basePath = "/pipelines/delete/{ID}";
	

		Response response = RestClient.doDelete("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), 200);
	}
	
	
	@DataProvider
	public Object[][] getHiringPipelineValidData() {

		
		
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String companySlug = jsonCompany.get("slug");

		JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
		JsonPath jsonContact1 = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug")+ "," + jsonContact1.get("slug");

		JsonPath jsonJob = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, jsonContact.get("slug")).jsonPath();
//		JsonPath jsonJob1 = function.createNewJob(baseURL, authTokenMap, companySlug, jsonContact1.get("slug")).jsonPath();
		String jobSlug =  jsonJob.getString("slug");
		
		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getJobResponse =  albatrossFunctions.getJobResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), jobSlug);
		
		// Verify Response using Assertion and Jsonpath
		JsonPath jpJob = getJobResponse.jsonPath();

		int jobID = jpJob.get("data.job.id");
		String jobID_String = Integer.toString(jobID);
		
		
		HiringPipelineFunctions hiringPipelineFunctions = new HiringPipelineFunctions();
		Response response = hiringPipelineFunctions.createCustomHiringPipeline(baseURL, hiringPipelineServiceURL,
				ThreadManager.getAccountApiKey(), ThreadManager.getOwnerAlbatrossToken());

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int ID = jp.get("id");
		String hiringPipelineID = Integer.toString(ID);

		
		
		
		
		Object data[][] = { { hiringPipelineID, 200,jobID_String }
				};
		return data;
	}
	
	
}
