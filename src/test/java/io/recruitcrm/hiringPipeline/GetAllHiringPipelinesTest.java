package io.recruitcrm.hiringPipeline;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.albatross.report.JavaFakerSavePerferences;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetAllHiringPipelinesTest extends TestBase {

	public GetAllHiringPipelinesTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	String hiringPipelineIdSave = "";
	commanFunction function = new commanFunction();
	ListFunctions listFunctions = new ListFunctions();

	JavaFakerSavePerferences reportFaker = new JavaFakerSavePerferences();
	String reportName = reportFaker.getReportName();

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
	public void getAllCustomHiringPipelineWithNameAndID_Test() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("getDropdownValues", "true");

		String basePath = " pipelines/list";
		Response response = RestClient.doGet("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null,
				true);


		response.then().statusCode(200);
		response.then().body("default-pipeline.id", Matchers.is(0));

	}

	@Owner("Sandeep")
	@Test(groups = {"nightly-build", "hiring-pipeline-service"})
	public void unauthorizedUserCannotGetAllCustomHiringPipeline_Test() {

		String basePath = " pipelines/list";
		Response response = RestClient.doGet("JSON", hiringPipelineServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"x001", null,
				null, true);


		response.then().statusCode(401);

	}
	
	

}
