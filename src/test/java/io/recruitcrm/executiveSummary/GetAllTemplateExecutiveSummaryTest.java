package io.recruitcrm.executiveSummary;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.executiveSummaryService.ExecutiveSummaryFunctions;
import io.rcrm.api.javafaker.executive_summary.TemplateFaker;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetAllTemplateExecutiveSummaryTest extends TestBase {

	String templateID = "";

	TemplateFaker templateFaker = new TemplateFaker();

	@Owner("Sandeep")
	@Test(groups = "nightly-build")  //(dataProvider = "getTemplateData")
	public void getAllTemplate_executiveSummaryTest() { //String templateID, int statusCode

		String basePath = "/templates";

		Response response = RestClient.doGet("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);
	}

	@Owner("Sandeep")
	@Test(groups = "nightly-build")
	public void getAllTemplateByType_executiveSummaryTest() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("type", "1");

		String basePath = "/templates";

		Response response = RestClient.doGet("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters,
				null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
	}

	@Owner("Sandeep")
	@Test(groups = "nightly-build")
	public void getAllTemplateByInvalidType_executiveSummaryTest() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("type", "x0001hi");

		String basePath = "/templates";

		Response response = RestClient.doGet("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters,
				null, true);

		Assert.assertEquals(response.getStatusCode(), 422);
	}

	@Owner("Sandeep")
	@Test(groups = "nightly-build")
	public void unAuthorizedUserCannotGetAllTemplate_executiveSummaryTest() {

		String basePath = "/templates";

		Response response = RestClient.doGet("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"x001", null,
				null, true);

		Assert.assertEquals(response.getStatusCode(), 401);
	}

	@DataProvider
	public Object[][] getTemplateData() {

		ExecutiveSummaryFunctions executiveSummaryFunctions = new ExecutiveSummaryFunctions();
		Response response = executiveSummaryFunctions.createTemplateForExecutiveSummary(1, executiveSummaryServiceURL,
				ThreadManager.getOwnerAlbatrossToken());

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int ID = jp.get("id");
		String template_ID = Integer.toString(ID);

		Object data[][] = { { template_ID, 200 } };
		return data;
	}

}
