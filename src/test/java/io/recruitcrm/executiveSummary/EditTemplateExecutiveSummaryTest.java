package io.recruitcrm.executiveSummary;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.executiveSummaryService.ExecutiveSummaryFunctions;
import io.rcrm.api.javafaker.executive_summary.TemplateFaker;
import io.rcrm.api.pojo.executiveSummary.TemplateShareWithTeam;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditTemplateExecutiveSummaryTest extends TestBase {

	String templateID = "";

	TemplateFaker templateFaker = new TemplateFaker();

	@Owner("Sandeep")
	@Test(dataProvider = "getTemplateData", groups = "nightly-build")
	public void editTemplate_executiveSummaryTest(String templateID, int statusCode) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("template_id", templateID);
		String basePath = "/templates/{template_id}";

		TemplateShareWithTeam templateShareWithTeam = new TemplateShareWithTeam();
		templateShareWithTeam.setIs_shared_with_teammates(1);
		templateShareWithTeam.setTemplate_content(templateFaker.getTemplateContent());
		templateShareWithTeam.setTemplate_name(templateFaker.getTemplateName() + "Edited");

		Response response = RestClient.doPost1("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, true, templateShareWithTeam);

		response.then().statusCode(200);

	}

	@Owner("Sandeep")
	@Test(groups = "nightly-build")
	public void editTemplateWithInvalidTemplateID_executiveSummaryTest() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("template_id", templateID + "x000hi");
		String basePath = "/templates/{template_id}";

		TemplateShareWithTeam templateShareWithTeam = new TemplateShareWithTeam();
		templateShareWithTeam.setIs_shared_with_teammates(1);
		templateShareWithTeam.setTemplate_content(templateFaker.getTemplateContent());
		templateShareWithTeam.setTemplate_name(templateFaker.getTemplateName() + "Edited");

		Response response = RestClient.doPost1("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, true, templateShareWithTeam);

		response.then().statusCode(404);

	}

	@Owner("Sandeep")
	@Test(groups = "nightly-build")
	public void unAuthorizedUserCannotEditTemplate_executiveSummaryTest() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("template_id", templateID + "x000hi");
		String basePath = "/templates/{template_id}";

		TemplateShareWithTeam templateShareWithTeam = new TemplateShareWithTeam();
		templateShareWithTeam.setIs_shared_with_teammates(1);
		templateShareWithTeam.setTemplate_content(templateFaker.getTemplateContent());
		templateShareWithTeam.setTemplate_name(templateFaker.getTemplateName() + "Edited");

		Response response = RestClient.doPost1("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"x001", null,
				pathParamters, true, templateShareWithTeam);

		response.then().statusCode(401);

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
