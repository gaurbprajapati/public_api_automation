package io.recruitcrm.albatross.smsTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.pojo.albatross.New_sms_templatePage;
import io.rcrm.api.pojo.albatross.SmsTemplatePage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndpointsOfSmsTemplateTest extends TestBase {
	
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	
	static int id;

	@Owner("Harika")
	@Test(dataProvider = "getRelateToTypeId", groups = "nightly-build")
	public void createSmsTemplates_Test(String templateForEntity,String relatedToTypeId) {
	    New_sms_templatePage new_sms_template = new New_sms_templatePage();
	    new_sms_template.setTemplate_name(templateForEntity+" sms Template "+generatedString);
	    new_sms_template.setRelatedtotypeid(relatedToTypeId);
	    new_sms_template.setTemplate(templateForEntity+" Template body "+generatedString);
	    new_sms_template.setShare(false);
		
		SmsTemplatePage smsTemplatePage = new SmsTemplatePage();
		smsTemplatePage.setNew_sms_template(new_sms_template);
		Response response = RestClient.doPost("JSON", albatrossURL, "sms-templates", ThreadManager.getOwnerAlbatrossToken(), null, true, smsTemplatePage);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}
	
	@Owner("Harika")
	@Test(dataProvider = "getRelateToTypeId", groups = "nightly-build")
	public void createSmsTemplatesInvalidParams_Test(String templateForEntity,String relatedToTypeId) {
	    New_sms_templatePage new_sms_template = new New_sms_templatePage();
	    new_sms_template.setRelatedtotypeid(relatedToTypeId);
	    new_sms_template.setTemplate(templateForEntity+" Template body "+generatedString);
	    new_sms_template.setShare(false);
		
		SmsTemplatePage smsTemplatePage = new SmsTemplatePage();
		smsTemplatePage.setNew_sms_template(new_sms_template);
		Response response = RestClient.doPost("JSON", albatrossURL, "sms-templates", ThreadManager.getOwnerAlbatrossToken(), null, true, smsTemplatePage);


		response.then().statusCode(422);
		
	}
	
	@Owner("Harika")
	@Test(dataProvider = "getRelateToTypeId", groups = "nightly-build")
	public void createSmsTemplatesInvalidAuth_Test(String templateForEntity,String relatedToTypeId) {
	    New_sms_templatePage new_sms_template = new New_sms_templatePage();
	    new_sms_template.setTemplate_name(templateForEntity+" sms Template "+generatedString);
	    new_sms_template.setRelatedtotypeid(relatedToTypeId);
	    new_sms_template.setTemplate(templateForEntity+" Template body "+generatedString);
	    new_sms_template.setShare(false);
		
		SmsTemplatePage smsTemplatePage = new SmsTemplatePage();
		smsTemplatePage.setNew_sms_template(new_sms_template);
		Response response = RestClient.doPost("JSON", albatrossURL, "sms-templates", ThreadManager.getOwnerAlbatrossToken()+"123", null, true, smsTemplatePage);


		response.then().statusCode(401);
		
	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getSmsTemplates_Test() {
		
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "updatedon");
		queryParameters.put("sortOrder", "ASC");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");

		String basePath = "sms-templates";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getSmsTemplatesInvalidAuth_Test() {
		
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "updatedon");
		queryParameters.put("sortOrder", "ASC");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");

		String basePath = "sms-templates";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", queryParameters, null, true);
		response.then().statusCode(401);

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchSmsTemplate_Test() {
		
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page_size", "1");
		queryParameters.put("search", generatedString);
		String basePath = "sms-templates";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		 id = jp.get("data.records[0].id");
	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void searchSmsTemplateInvalidAuth_Test() {
		
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "updatedon");
		queryParameters.put("sortOrder", "ASC");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");
		queryParameters.put("search", generatedString);
		String basePath = "sms-templates";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", queryParameters, null, true);
		response.then().statusCode(401);
	
	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void deleteSmsTemplate_Test() {
		
		Map<String, String> pathParameters = new HashMap<String, String>();
		String ids = String.valueOf(id);
		pathParameters.put("id", ids);
	
		String basePath = "sms-templates/{id}";
		Response response = RestClient.doDelete("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void deleteSmsTemplateInvalidAuth_Test() {
		
		Map<String, String> pathParameters = new HashMap<String, String>();
		String ids = String.valueOf(id);
		pathParameters.put("id", ids);
	
		String basePath = "sms-templates/{id}";
		Response response = RestClient.doDelete("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, pathParameters, true);
		response.then().statusCode(401);

	}
	
	@DataProvider
	public Object[][] getRelateToTypeId() {
		Object data[][] = { { "Candidate","5"},{"Contacts","2"} };
		return data;
	}

}
