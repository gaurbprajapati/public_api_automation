package io.recruitcrm.hiringPipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerJob;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.hiringPipelineService.HiringPipelineFunctions;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.albatross.report.JavaFakerSavePerferences;
import io.rcrm.api.javafaker.hiringPipeline.HiringPipeline;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.pojo.albatross.hiringpipeline.CreateHiringPipeline;
import io.rcrm.api.pojo.albatross.hiringpipeline.HiringStages;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndpointsOfHiringPipelineServiceTest extends TestBase {

	String hiringPipelineID = "";
	commanFunction function = new commanFunction();
	ListFunctions listFunctions = new ListFunctions();

	JavaFakerSavePerferences reportFaker = new JavaFakerSavePerferences();
	String reportName = reportFaker.getReportName();
	JavaFakerJob jobFaker = new JavaFakerJob();

	@Owner("Sandeep")
	@Test(dataProvider = "getHiringStagesValidTestData", invocationCount = 1, groups = {"nightly-build", "hiring-pipeline-service"})
	public void createCustomHiringPipeline_Test(String customHiringPipelineName, String isPrimary,
			ArrayList<Object> json_obj) {

		HiringPipeline hiringFaker = new HiringPipeline();
		String pipelineName = hiringFaker.getHiringPipelineName();

		CreateHiringPipeline createHiringPipeline = new CreateHiringPipeline();
		createHiringPipeline.setName(pipelineName);
		createHiringPipeline.setIs_primary(isPrimary);
		createHiringPipeline.setHiring_stages(json_obj);

		Response response = RestClient.doPost("JSON", hiringPipelineServiceURL, "pipelines/add", ThreadManager.getOwnerAlbatrossToken(), null,
				true, createHiringPipeline);

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int ID = jp.get("id");
		hiringPipelineID = Integer.toString(ID);

	}

	@Owner("Sandeep")
	@Test(dependsOnMethods = "createCustomHiringPipeline_Test", dataProvider = "getHiringStagesValidTestData", groups = {"nightly-build", "hiring-pipeline-service"})
	public void editCustomHiringPipeline_Test(String customHiringPipelineName, String isPrimary,
			ArrayList<Object> json_ob) {
		
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineID);
		String basePath = "pipelines/update/{ID}";

		HiringPipeline hiringFaker = new HiringPipeline();
		String pipelineName = hiringFaker.getHiringPipelineName() + "_Edited";

		CreateHiringPipeline createHiringPipeline = new CreateHiringPipeline();
		createHiringPipeline.setName(pipelineName);
		createHiringPipeline.setIs_primary(isPrimary);
		createHiringPipeline.setHiring_stages(json_ob);

		Response response = RestClient.doPost1("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, true, createHiringPipeline);

		response.then().statusCode(200);
	}

	@Owner("Sandeep")
	@Test(dependsOnMethods = "createCustomHiringPipeline_Test", groups = {"nightly-build", "hiring-pipeline-service"})
	public void getCustomHiringPipelineByID_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineID);
		String basePath = "pipelines/{ID}";

		Response response = RestClient.doGet("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);
	}

	@Owner("Sandeep")
	@Test(groups = {"nightly-build", "hiring-pipeline-service"})
	public void getAllCustomHiringPipeline_Test() {
		String basePath = " pipelines/list";
		Response response = RestClient.doGet("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null,
				true);


		response.then().statusCode(200);
		response.then().body("default-pipeline.id", Matchers.is(0));

	}

	@Owner("Sandeep")
	@Test(groups = {"nightly-build", "hiring-pipeline-service"})
	public void getAllListOfHiringPipelineForDropdown_Test() {

		String basePath = " pipelines/list";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("getDropdownValues", "true");

		Response response = RestClient.doGet("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters,
				null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
	}

	@Owner("Sandeep")
	@Test(groups = "nightly-build")
	public void deleteStageFromCustomHiringPipeline_Test() {

	}

	@Owner("Sandeep")
	@Test(dependsOnMethods = "createCustomHiringPipeline_Test", groups = {"nightly-build", "hiring-pipeline-service"})
	public void deleteCustomHiringPipeline_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineID);
		String basePath = "/pipelines/delete/{ID}";

		Response response = RestClient.doDelete("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), 200);
	}
	
	
	@Owner("Sandeep")
	@Test(groups = "nightly-build")
	public void createCustomHiringPipeLineUsingFunctions() {

		HiringPipelineFunctions hiringPipelineFunctions = new HiringPipelineFunctions();
		Response response = hiringPipelineFunctions.createCustomHiringPipeline(baseURL, hiringPipelineServiceURL, ThreadManager.getAccountApiKey(), ThreadManager.getOwnerAlbatrossToken());
	
		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int ID = jp.get("id");
		hiringPipelineID = Integer.toString(ID);
	
	}

	@Owner("Sampurn Chouksey")
	@Test(dataProvider = "getJobWithHiringPipelineTestData", groups = "nightly-build")
	public void createJobWithCustomHiringPipelineAndMarkAsDefault_Test(String companySlug, String contactSlug, int customPipelineID) {
		Map<String, String> authTokenMap = new HashMap<String, String>();
		authTokenMap.put("Authorization", "Bearer " + ThreadManager.getAccountApiKey());
		Job job = new Job();
		job.setName(jobFaker.getJobName());
		job.setCompany_slug(companySlug);
		job.setContact_slug(contactSlug);
		job.setNumber_of_openings(jobFaker.getOpenings());
		job.setJob_description_text(jobFaker.getJobDescriptionText());
		job.setEnable_job_application_form(1);
		job.setHiring_pipeline_id(customPipelineID);
		Response jobResponse = RestClient.doPost("JSON", baseURL, "jobs", authTokenMap, null, true, job);
		Assert.assertEquals(jobResponse.getStatusCode(), 200, "Job creation Failed");
		JsonPath jobJp = jobResponse.jsonPath();
		Integer actualPipelineId = jobJp.get("hiring_pipeline_id");
		Assert.assertEquals(companySlug, jobJp.get("company_slug"), "Company slug should match");
		Assert.assertEquals(contactSlug, jobJp.get("contact_slug"), "Contact slug should match");
		Assert.assertEquals(actualPipelineId, Integer.valueOf(customPipelineID), "Job should use the specified custom hiring pipeline ID");
	}

	@DataProvider
	public Object[][] getHiringStagesValidTestData() {

		HiringPipeline hiringFaker = new HiringPipeline();
		hiringFaker.getHiringPipelineName();

		ArrayList<Object> hiringStagesList = new ArrayList<Object>();
		String generatedString = RandomStringUtils.randomAlphabetic(4);

		JsonPath jsonGetAllCandidateHiringStages = listFunctions
				.getAllCandidateHiringStages(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		ArrayList<Integer> hiringStagesID = jsonGetAllCandidateHiringStages.get("status_id");
		int sizeOfStages = hiringStagesID.size();

//		List<Integer> list = new ArrayList<Integer>(sizeOfStages);
//		for (int i = 1; i <= sizeOfStages; i++) {
//			list.add(i);
//		}
//
//		Random rand = new Random();
//		while (list.size() > 0) {
//			int index = rand.nextInt(list.size());
//		}

		for (int i = 1; i < sizeOfStages; i++) {
			HiringStages hiringStagesi = new HiringStages();

			if (i == 10) {
				hiringStagesi.setId(10);
				hiringStagesi.setSequenceno(0);
			} else if (i == 1) {
				hiringStagesi.setId(1);
				hiringStagesi.setSequenceno(1);
			} else if (i == 8) {
				hiringStagesi.setId(8);
				hiringStagesi.setSequenceno(55);
			} else { // if (i != 10 || i != 1 || i != 8)
				hiringStagesi.setId(hiringStagesID.get(i));
				hiringStagesi.setSequenceno(i);
			}
			hiringStagesList.add(hiringStagesi);
		}

		// Remove duplicates
//        ArrayList<Integer>
//            newList = removeDuplicates(hiringStagesList);
//		

		Object data[][] = {

				{ hiringFaker.getHiringPipelineName(), "0", hiringStagesList } };
		return data;
	}

	@DataProvider
	public Object[][] getJobWithHiringPipelineTestData() {
		HiringPipeline hiringFaker = new HiringPipeline();
		JsonPath companyJp = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String companySlug = companyJp.get("slug");
		JsonPath contactJp = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
		String contactSlug = contactJp.get("slug");
		ArrayList<Object> hiringStagesList = new ArrayList<Object>();
		int[] ids = {10, 1, 8};
		int[] sequenceno = {0, 1, 55};
		for(int i = 0; i < 3; i++){
			HiringStages hiringStage = new HiringStages(ids[i], sequenceno[i]);
			hiringStagesList.add(hiringStage);
		}
		CreateHiringPipeline createHiringPipeline = new CreateHiringPipeline();
		createHiringPipeline.setName(hiringFaker.getHiringPipelineName() + "_ForJob");
		createHiringPipeline.setIs_primary("0");
		createHiringPipeline.setHiring_stages(hiringStagesList);
		Response pipelineResponse = RestClient.doPost("JSON", hiringPipelineServiceURL, "pipelines/add", ThreadManager.getOwnerAlbatrossToken(), null, true, createHiringPipeline);
		Assert.assertEquals(pipelineResponse.getStatusCode(), 200, "Pipeline not Created");
		pipelineResponse.then().body("id", Matchers.notNullValue());
		JsonPath pipelineJp = pipelineResponse.jsonPath();
		int customPipelineID = pipelineJp.get("id");
		JSONObject markAsDefaultPayload = new JSONObject();
		markAsDefaultPayload.put("pipeline_id", customPipelineID);
		Response markDefaultResponse = RestClient.doPost("JSON", hiringPipelineServiceURL, "pipelines/mark-primary", ThreadManager.getOwnerAlbatrossToken(), null, true, markAsDefaultPayload);
		Assert.assertEquals(markDefaultResponse.getStatusCode(), 200);
		Object data[][] = {{ companySlug, contactSlug, customPipelineID }};
		return data;
	}

}
