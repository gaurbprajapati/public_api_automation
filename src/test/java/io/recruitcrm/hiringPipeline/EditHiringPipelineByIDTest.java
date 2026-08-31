package io.recruitcrm.hiringPipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.hiringPipelineService.HiringPipelineFunctions;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.albatross.report.JavaFakerSavePerferences;
import io.rcrm.api.javafaker.hiringPipeline.HiringPipeline;
import io.rcrm.api.pojo.albatross.hiringpipeline.CreateHiringPipeline;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditHiringPipelineByIDTest extends TestBase {

	public EditHiringPipelineByIDTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	String hiringPipelineID_global = "";
	commanFunction function = new commanFunction();
	ListFunctions listFunctions = new ListFunctions();

	JavaFakerSavePerferences reportFaker = new JavaFakerSavePerferences();
	String reportName = reportFaker.getReportName();

	@Owner("Sandeep")
	@Test(dataProvider = "getHiringPipelineValidData", groups = {"nightly-build", "hiring-pipeline-service"})
	public void editCustomHiringPipeline_Test(String hiringPipelineID, int statusCode) {

		hiringPipelineID_global = hiringPipelineID;

		HiringPipelineFunctions hiringPipelineStages = new HiringPipelineFunctions();
		ArrayList<Object> hiringstagesObject = hiringPipelineStages.getHiringPipelineStageArrayObject(baseURL,
				ThreadManager.getAccountApiKey());

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineID);
		String basePath = "pipelines/update/{ID}";

		HiringPipeline hiringFaker = new HiringPipeline();
		String pipelineName = hiringFaker.getHiringPipelineName() + "_Edited";

		CreateHiringPipeline createHiringPipeline = new CreateHiringPipeline();
		createHiringPipeline.setName(pipelineName);
		createHiringPipeline.setIs_primary("0");
		createHiringPipeline.setHiring_stages(hiringstagesObject);

		Response response = RestClient.doPost1("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, true, createHiringPipeline);

		response.then().statusCode(200);
	}

	@Owner("Sandeep")
	@Test(dependsOnMethods = "editCustomHiringPipeline_Test", groups = {"nightly-build", "hiring-pipeline-service"})
	public void editCustomHiringPipelineByInvalidID_Test() {

		HiringPipelineFunctions hiringPipelineStages = new HiringPipelineFunctions();
		ArrayList<Object> hiringstagesObject = hiringPipelineStages.getHiringPipelineStageArrayObject(baseURL,
				ThreadManager.getAccountApiKey());

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineID_global+"x000hi");
		String basePath = "pipelines/update/{ID}";

		HiringPipeline hiringFaker = new HiringPipeline();
		String pipelineName = hiringFaker.getHiringPipelineName() + "_Edited";

		CreateHiringPipeline createHiringPipeline = new CreateHiringPipeline();
		createHiringPipeline.setName(pipelineName);
		createHiringPipeline.setIs_primary("0");
		createHiringPipeline.setHiring_stages(hiringstagesObject);

		Response response = RestClient.doPost1("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, true, createHiringPipeline);

		response.then().statusCode(422);
	}
	
	
	@Owner("Sandeep")
	@Test(dependsOnMethods = "editCustomHiringPipeline_Test", groups = {"nightly-build", "hiring-pipeline-service"})
	public void unAuthorizedUserCannotEditCustomHiringPipelineByInvalidID_Test() {
		
		HiringPipelineFunctions hiringPipelineStages = new HiringPipelineFunctions();
		ArrayList<Object> hiringstagesObject = hiringPipelineStages.getHiringPipelineStageArrayObject(baseURL,
				ThreadManager.getAccountApiKey());

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineID_global);
		String basePath = "pipelines/update/{ID}";

		HiringPipeline hiringFaker = new HiringPipeline();
		String pipelineName = hiringFaker.getHiringPipelineName() + "_Edited";

		CreateHiringPipeline createHiringPipeline = new CreateHiringPipeline();
		createHiringPipeline.setName(pipelineName);
		createHiringPipeline.setIs_primary("0");
		createHiringPipeline.setHiring_stages(hiringstagesObject);

		Response response = RestClient.doPost1("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null,
				pathParamters, true, createHiringPipeline);

		response.then().statusCode(401);
	}
	
	@Owner("Sandeep")
	@Test(dependsOnMethods = "editCustomHiringPipeline_Test", groups = {"nightly-build", "hiring-pipeline-service"})
	public void deleteCustomHiringPipeline_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineID_global);
		String basePath = "/pipelines/delete/{ID}";

		Response response = RestClient.doDelete("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), 200);
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

		Object data[][] = { { hiringPipelineID, 200 } };
		return data;
	}

}
