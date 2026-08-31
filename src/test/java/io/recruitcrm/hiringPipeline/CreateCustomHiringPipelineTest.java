package io.recruitcrm.hiringPipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.albatross.report.JavaFakerSavePerferences;
import io.rcrm.api.javafaker.hiringPipeline.HiringPipeline;
import io.rcrm.api.pojo.albatross.hiringpipeline.CreateHiringPipeline;
import io.rcrm.api.pojo.albatross.hiringpipeline.HiringStages;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateCustomHiringPipelineTest extends TestBase {

	public CreateCustomHiringPipelineTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	// 401, 422, 404

	String hiringPipelineID = "";
	commanFunction function = new commanFunction();
	ListFunctions listFunctions = new ListFunctions();

	JavaFakerSavePerferences reportFaker = new JavaFakerSavePerferences();
	String reportName = reportFaker.getReportName();

	@Owner("Sandeep")
	@Test(dataProvider = "getHiringStagesValidTestData", invocationCount = 1, groups = {"nightly-build", "hiring-pipeline-service"})
	public void createCustomHiringPipelineWithRequiredFields_test(String customHiringPipelineName, String isPrimary,
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
	@Test(groups = {"nightly-build", "hiring-pipeline-service"})
	public void userShouldNotBeAbleToAddCustomHiringPipelineNameWithMoreThan150Chars_test() {
		HiringPipeline hiringFaker = new HiringPipeline();
		String pipelineName = hiringFaker.getHiringPipelineNameWithMoreThan150Chars();

		CreateHiringPipeline createHiringPipeline = new CreateHiringPipeline();
		createHiringPipeline.setName(pipelineName);
		createHiringPipeline.setIs_primary("65");
		createHiringPipeline.setHiring_stages(null);

		Response response = RestClient.doPost("JSON", hiringPipelineServiceURL, "pipelines/add", ThreadManager.getOwnerAlbatrossToken(), null,
				true, createHiringPipeline);

		response.then().statusCode(422);
//		response.then().body("id", Matchers.notNullValue());
	}

	@Owner("Sandeep")
	@Test(dataProvider = "getHiringStagesValidTestData", invocationCount = 1, groups = {"nightly-build", "hiring-pipeline-service"})
	public void unauthroizedUserCannotCreateCustomHiringPipeline_test(String customHiringPipelineName, String isPrimary,
			ArrayList<Object> json_obj) {
		
		HiringPipeline hiringFaker = new HiringPipeline();
		String pipelineName = hiringFaker.getHiringPipelineName();

		CreateHiringPipeline createHiringPipeline = new CreateHiringPipeline();
		createHiringPipeline.setName(pipelineName);
		createHiringPipeline.setIs_primary(isPrimary);
		createHiringPipeline.setHiring_stages(json_obj);

		Response response = RestClient.doPost("JSON", hiringPipelineServiceURL, "pipelines/add", ThreadManager.getOwnerAlbatrossToken()+"123", null,
				true, createHiringPipeline);

		response.then().statusCode(401);
//		response.then().body("id", Matchers.notNullValue());


	}

	@Owner("Sandeep")
	@Test(dependsOnMethods = "createCustomHiringPipelineWithRequiredFields_test", groups = {"nightly-build", "hiring-pipeline-service"})
	public void deleteCustomHiringPipeline_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineID);
		String basePath = "/pipelines/delete/{ID}";

		Response response = RestClient.doDelete("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), 200);
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

}
