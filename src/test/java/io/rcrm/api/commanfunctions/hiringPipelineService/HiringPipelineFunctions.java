package io.rcrm.api.commanfunctions.hiringPipelineService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.albatross.report.JavaFakerSavePerferences;
import io.rcrm.api.javafaker.hiringPipeline.HiringPipeline;
import io.rcrm.api.pojo.albatross.hiringpipeline.CreateHiringPipeline;
import io.rcrm.api.pojo.albatross.hiringpipeline.HiringStages;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class HiringPipelineFunctions {

	String hiringPipelineID = "";
	commanFunction function = new commanFunction();
	ListFunctions listFunctions = new ListFunctions();

	JavaFakerSavePerferences reportFaker = new JavaFakerSavePerferences();
	String reportName = reportFaker.getReportName();

	public HiringPipelineFunctions() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Response createCustomHiringPipeline(String publicApiURL, String hiringPipelineServiceURL,
			Object authTokenMapPublicAPI, Object authTokenMapHiringPipeline) {

		/*
		 * Test Data creation Start here
		 */

		ArrayList<Object> hiringStagesList = new ArrayList<Object>();
		String generatedString = RandomStringUtils.randomAlphabetic(4);

		JsonPath jsonGetAllCandidateHiringStages = listFunctions
				.getAllCandidateHiringStages(publicApiURL, authTokenMapPublicAPI).jsonPath();
		ArrayList<Integer> hiringStagesID = jsonGetAllCandidateHiringStages.get("status_id");
		int sizeOfStages = hiringStagesID.size();

		for (int i = 1; i < 4; i++) {
			HiringStages hiringStagesi = new HiringStages();

			if (i == 1) {
				hiringStagesi.setId(10);
				hiringStagesi.setSequenceno(0);
			} else if (i == 2) {
				hiringStagesi.setId(1);
				hiringStagesi.setSequenceno(1);
			} else if (i == 3) {
				hiringStagesi.setId(8);
				hiringStagesi.setSequenceno(55);
			}
			hiringStagesList.add(hiringStagesi);
		}

		/*
		 * Test Data creation End here
		 */

		HiringPipeline hiringFaker = new HiringPipeline();
		String pipelineName = hiringFaker.getHiringPipelineName();

		CreateHiringPipeline createHiringPipeline = new CreateHiringPipeline();
		createHiringPipeline.setName(pipelineName);
		createHiringPipeline.setIs_primary("0");
		createHiringPipeline.setHiring_stages(hiringStagesList);

		Response response = RestClient.doPost("JSON", hiringPipelineServiceURL, "pipelines/add",
				authTokenMapHiringPipeline, null, true, createHiringPipeline);

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int ID = jp.get("id");
		hiringPipelineID = Integer.toString(ID);

		return response;
	}

	public ArrayList<Object> getHiringPipelineStageArrayObject(String publicApiURL,
			Object authTokenMapPublicAPI) {

		/*
		 * Test Data creation Start here
		 */

		ArrayList<Object> hiringStagesList = new ArrayList<Object>();
		String generatedString = RandomStringUtils.randomAlphabetic(4);

		JsonPath jsonGetAllCandidateHiringStages = listFunctions
				.getAllCandidateHiringStages(publicApiURL, authTokenMapPublicAPI).jsonPath();
		ArrayList<Integer> hiringStagesID = jsonGetAllCandidateHiringStages.get("status_id");
		int sizeOfStages = hiringStagesID.size();

		for (int i = 1; i < sizeOfStages; i++) {
			HiringStages hiringStagesi = new HiringStages();

			if (i == 1) {
				hiringStagesi.setId(10);
				hiringStagesi.setSequenceno(0);
			} else if (i == 2) {
				hiringStagesi.setId(1);
				hiringStagesi.setSequenceno(1);
			} else if (i == 3) {
				hiringStagesi.setId(8);
				hiringStagesi.setSequenceno(55);
			} else {
				hiringStagesi.setId(hiringStagesID.get(i));
				hiringStagesi.setSequenceno(i);

			}
			hiringStagesList.add(hiringStagesi);
		}
		return hiringStagesList;
	}

	public Response deleteCustomHiringPipelineByID(String hiringPipelineID, String hiringPipelineServiceURL,
			Map<String, String> authTokenMapHiringPipeline) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineID);
		String basePath = "/pipelines/delete/{ID}";

		Response response = RestClient.doDelete("JSON", hiringPipelineServiceURL, basePath, authTokenMapHiringPipeline,
				null, pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), 200);

		return response;
	}
	
	
	

}
