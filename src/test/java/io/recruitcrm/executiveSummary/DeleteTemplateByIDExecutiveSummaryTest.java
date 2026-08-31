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
public class DeleteTemplateByIDExecutiveSummaryTest extends TestBase {

	// String templateID = "";

	TemplateFaker templateFaker = new TemplateFaker();

	@Owner("Sandeep")
	@Test(dataProvider = "getTemplateData", groups = "nightly-build")
	public void userShouldAbleToDeleteNewTemplate_executiveSummaryTest(String templateID, int statusCode) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("template_id", templateID);
		String basePath = "/templates/{template_id}";

		Response response = RestClient.doDelete("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), 200);
	}

	@Owner("Sandeep")
	@Test(groups = "nightly-build") // (dataProvider = "getHiringPipelineValidData")
	public void unauthorizedUserCannotDeleteNewTemplate_executiveSummaryTest() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("template_id", "1");
		String basePath = "/templates/{template_id}";

		Response response = RestClient.doDelete("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null,
				pathParamters, false);

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
