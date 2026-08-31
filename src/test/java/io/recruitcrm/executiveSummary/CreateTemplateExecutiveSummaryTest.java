package io.recruitcrm.executiveSummary;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.executiveSummaryService.ExecutiveSummaryFunctions;
import io.rcrm.api.javafaker.executive_summary.TemplateFaker;
import io.rcrm.api.javafaker.hiringPipeline.HiringPipeline;
import io.rcrm.api.pojo.executiveSummary.Template;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateTemplateExecutiveSummaryTest extends TestBase {

	String templateID = "";

	TemplateFaker templateFaker = new TemplateFaker();
	ExecutiveSummaryFunctions executiveSummaryFunctions = new ExecutiveSummaryFunctions();

	@Owner("Sandeep")
	@Test(dataProvider = "getTemplateData", groups = "nightly-build")
	public void userShouldAbleToCreateNewTemplate_executiveSummaryTest(int type, String statusCode) {

		Template template = new Template();
		template.setType(type);
		template.setTemplate_name(templateFaker.getTemplateName());
		template.setTemplate_content(templateFaker.getTemplateContent());

		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "templates", ThreadManager.getOwnerAlbatrossToken(), null, true,
				template);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int ID = jp.get("id");
		templateID = Integer.toString(ID);

		response.then().statusCode(200);

		// response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/templateSchema.json"));

		// Data Deletion
		executiveSummaryFunctions.deleteTemplateByID(templateID, executiveSummaryServiceURL, ThreadManager.getOwnerAlbatrossToken());

	}

	@Owner("Sandeep")
	@Test(groups = "nightly-build")
	public void userCannotCreateNewTemplateWithInvalidData_executiveSummaryTest() {

		Template template = new Template();
		template.setType(10);
		template.setTemplate_name(templateFaker.getTemplateName());
		template.setTemplate_content("TEST");

		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "templates", ThreadManager.getOwnerAlbatrossToken(), null, true,
				template);

		response.then().statusCode(422);

	}

	@Owner("Sandeep")
	@Test(groups = "nightly-build")
	public void userCannotCreateNewTemplateWithInvalidTemplateName_executiveSummaryTest() {

		HiringPipeline hiringFaker = new HiringPipeline();
		String templateNameWithMoreThan150Chars = hiringFaker.getHiringPipelineNameWithMoreThan150Chars();

		Template template = new Template();
		template.setType(1);
		template.setTemplate_name(templateNameWithMoreThan150Chars);
		template.setTemplate_content(templateFaker.getTemplateContent());

		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "templates", ThreadManager.getOwnerAlbatrossToken(), null, true,
				template);

		response.then().statusCode(422);

	}

	@Owner("Sandeep")
	@Test(groups = "nightly-build")
	public void unAuthorizedCannotCreateNewTemplateValidData_executiveSummaryTest() {

		Template template = new Template();
		template.setType(1);
		template.setTemplate_name(templateFaker.getTemplateName());
		template.setTemplate_content(templateFaker.getTemplateContent());

		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "templates", ThreadManager.getOwnerAlbatrossToken()+"x001",
				null, true, template);

		response.then().statusCode(401);

	}

	@AfterMethod
	public void waitFor2Sec() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@DataProvider
	public Object[][] getTemplateData() {

		Object data[][] = { { 1, "200" }, { 2, "200" }, { 3, "200" } };
		return data;
	}

}
