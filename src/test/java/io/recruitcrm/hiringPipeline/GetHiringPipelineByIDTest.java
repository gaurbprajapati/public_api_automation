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
import io.rcrm.api.commanfunctions.errorResponseBody;
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
public class GetHiringPipelineByIDTest extends TestBase{

	public GetHiringPipelineByIDTest() {
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
	public void getHiringPipelineByID_test(String hiringPipelineID, int statusCode) {
		
		hiringPipelineIdSave = hiringPipelineID;
		
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineID);
		String basePath = "pipelines/{ID}";

		Response response = RestClient.doGet("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, true);

		response.then().statusCode(200);
	}
	
	
	@Owner("Sandeep")
	@Test(dataProvider = "getHiringPipelinewithJobSlug", groups = {"nightly-build", "hiring-pipeline-service"})
	public void getHiringPipelineStagesButNotIncludedInPipeline_test(String hiringPipelineID, int statusCode,String jobID ) {
		
	
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineID);
		String basePath = "pipelines/{ID}";
		
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("expand_with_job_id", jobID);


		Response response = RestClient.doGet("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters,
				pathParamters, true);

		response.then().statusCode(200);
		
		//Delete Pipeline for Test data cleaning. 
		deleteCustomHiringPipeline(hiringPipelineID);
	}
	
	
	@Owner("Sandeep")
	@Test(groups = {"nightly-build", "hiring-pipeline-service"})
	public void getHiringPipelineByInvalidID_test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", "x003hi");
		String basePath = "pipelines/{ID}";

		Response response = RestClient.doGet("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, true);

		response.then().statusCode(422);	}
	
	@Owner("Sandeep")
	@Test(groups = {"nightly-build", "hiring-pipeline-service"})
	public void unAuthorizedUserCannotGetHiringPipelineByID_test() {
		
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineIdSave);
		String basePath = "pipelines/{ID}";
		
		Response response = RestClient.doGet("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"x001", null,
				pathParamters, true);

		response.then().statusCode(401);
		//Assert.assertEquals(response.getStatusCode(), 401);
	}
	
	@Owner("Sandeep")
	@Test(dependsOnMethods = "getHiringPipelineByID_test", groups = {"nightly-build", "hiring-pipeline-service"})
	public void deleteCustomHiringPipeline_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineIdSave);
		String basePath = "/pipelines/delete/{ID}";

		Response response = RestClient.doDelete("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, false);

		response.then().statusCode(200);
		
	}
	
	
	public void deleteCustomHiringPipeline(String ID) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", ID);
		String basePath = "/pipelines/delete/{ID}";

		Response response = RestClient.doDelete("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, false);

		response.then().statusCode(200);
		
	}
	
	
	@DataProvider
	public Object[][] getHiringPipelineValidData() {

		HiringPipelineFunctions hiringPipelineFunctions = new HiringPipelineFunctions();
		Response response = hiringPipelineFunctions.createCustomHiringPipeline(baseURL, hiringPipelineServiceURL,
				ThreadManager.getAccountApiKey(), ThreadManager.getOwnerAlbatrossToken());

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int ID = jp.get("id");
		String hiringPipelineID = Integer.toString(ID);

		Object data[][] = { { hiringPipelineID, 200 }
				};
		return data;
	}
	
	@DataProvider
	public Object[][] getHiringPipelinewithJobSlug() {

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
		String hiringPipelineID_2 = Integer.toString(ID);
		
		Object data[][] = { { hiringPipelineID_2, 200,jobID_String }
				};
		return data;
	}
}
