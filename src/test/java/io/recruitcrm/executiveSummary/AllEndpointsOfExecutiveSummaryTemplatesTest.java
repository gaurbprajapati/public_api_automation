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

import io.rcrm.api.javafaker.executive_summary.TemplateFaker;
import io.rcrm.api.pojo.executiveSummary.Template;
import io.rcrm.api.pojo.executiveSummary.TemplateShareWithTeam;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndpointsOfExecutiveSummaryTemplatesTest extends TestBase {

	TemplateFaker templateFaker = new TemplateFaker();

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void userShouldAbleToCreateNewTemplateExecutiveSummaryTest() {

		Template template = new Template();
		template.setType(1);
		template.setTemplate_name(templateFaker.getTemplateName());
		template.setTemplate_content(templateFaker.getTemplateContent());

		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "templates", ThreadManager.getOwnerAlbatrossToken(), null, true,
				template);

		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertNotNull(jp.getString("id"));
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getTemplateData", groups = "nightly-build")
	public void editTemplateExecutiveSummaryTest(String templateID) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("template_id", templateID);
		String basePath = "/templates/{template_id}";

		TemplateShareWithTeam templateShareWithTeam = new TemplateShareWithTeam();
		templateShareWithTeam.setIs_shared_with_teammates(1);
		templateShareWithTeam.setTemplate_content(templateFaker.getTemplateContent());
		templateShareWithTeam.setTemplate_name(templateFaker.getTemplateName() + "Edited");

		Response response = RestClient.doPost1("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, true, templateShareWithTeam);

		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertNotNull(jp.getString("id"));

	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getTemplateData", groups = "nightly-build")
	public void getAllTemplateExecutiveSummaryTest(String templateId) {

		String basePath = "/templates?type=1";

		Response response = RestClient.doGet("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null,
				true);

		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertNotNull(jp.getString("id"));
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getTemplateData", groups = "nightly-build")
	public void getTemplateByTemplateIDExecutiveSummaryTest(String templateID) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("template_id", templateID);
		String basePath = "/templates/{template_id}";

		Response response = RestClient.doGet("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, true);

		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertNotNull(jp.getString("id"));
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getTemplateData", groups = "nightly-build")
	public void userShouldAbleToDeleteNewTemplateExecutiveSummaryTest(String templateID) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("template_id", templateID);
		String basePath = "/templates/{template_id}";

		Response response = RestClient.doDelete("JSON", executiveSummaryServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParamters, false);

		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jp.getString("status_message"), "success");
		Assert.assertEquals(jp.getString("message"), "Template deleted successfully");
	}

	@DataProvider
	public Object[][] getTemplateData() {
		Template template = new Template();
		template.setType(1);
		template.setTemplate_name(templateFaker.getTemplateName());
		template.setTemplate_content(templateFaker.getTemplateContent());
		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "templates", ThreadManager.getOwnerAlbatrossToken(), null, true,
				template);
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertNotNull(jp.getString("id"));

		String templateId = jp.get("id").toString();

		return new Object[][]{{templateId}};
	}

}
