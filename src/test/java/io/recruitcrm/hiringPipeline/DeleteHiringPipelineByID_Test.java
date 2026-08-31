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
public class DeleteHiringPipelineByID_Test extends TestBase {

	public DeleteHiringPipelineByID_Test() {
		super();
		// TODO Auto-generated constructor stub
	}

	String hiringPipelineID = "";
	commanFunction function = new commanFunction();
	ListFunctions listFunctions = new ListFunctions();

	JavaFakerSavePerferences reportFaker = new JavaFakerSavePerferences();
	String reportName = reportFaker.getReportName();

	@Owner("Sandeep")
	@Test(dataProvider = "getHiringPipelineValidData", groups = {"nightly-build", "hiring-pipeline-service"})
	public void deleteCustomHiringPipelineByID_Test(String hiringPipelineID, int statusCode) {
		
		
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineID);
		String basePath = "/pipelines/delete/{ID}";

		Response response = RestClient.doDelete("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), statusCode);
	}
	
	
	@Owner("Sandeep")
	@Test(groups = {"nightly-build", "hiring-pipeline-service"})  //(dataProvider = "getHiringPipelineValidData")
	public void unAuthorizedUserCannotdeleteCustomHiringPipelineByID_Test() {
		
		
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", hiringPipelineID);
		String basePath = "/pipelines/delete/{ID}";

		Response response = RestClient.doDelete("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"12345", null,
				pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), 401);
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
		hiringPipelineID = Integer.toString(ID);

		Object data[][] = { { hiringPipelineID, 200 },
				{ "x003", 404 },
				};
		return data;
	}

}
