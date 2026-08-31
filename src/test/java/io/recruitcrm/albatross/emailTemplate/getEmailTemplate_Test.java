package io.recruitcrm.albatross.emailTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.qa.api.util.GenerateToken;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerMails;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.albatross.DeleteTemplatePage;
import io.rcrm.api.pojo.albatross.EmailTemplatePage;
import io.rcrm.api.pojo.albatross.New_email_templatePage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class getEmailTemplate_Test  extends TestBase {

	JavaFakerCompany faker = new JavaFakerCompany();
	JavaFakerMails fakerMails = new JavaFakerMails();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	ArrayList<Object> emailList = new ArrayList<Object>();
	commanFunction function = new commanFunction();

	static int id;
	String albatrossTkn;
	String adminToken;

	@BeforeClass(alwaysRun = true)	public void setup() {
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
		adminToken = ThreadManager.getAlbatrossToken("Admin");
	}

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getRelateToTypeId", groups = "nightly-build")
	public void createEmailTemplates_Test(String templateForEntity,String relatedToTypeId) {
		New_email_templatePage new_email_templatePage = new New_email_templatePage();
		new_email_templatePage.setEmailcontext(templateForEntity+" Email Template "+generatedString);
		new_email_templatePage.setRelatedtotypeid(relatedToTypeId);
		new_email_templatePage.setEmailsubject(fakerMails.getFakeEmailSubject());
		new_email_templatePage.setTemplate(fakerMails.getFakeEmailBody(5));
		new_email_templatePage.setShare(false);
		
		EmailTemplatePage emailTemplatePage = new EmailTemplatePage();
		emailTemplatePage.setNew_email_template(new_email_templatePage);
		Response response = RestClient.doPost("JSON", albatrossURL, "email-templates", ThreadManager.getOwnerAlbatrossToken(), null, true, emailTemplatePage);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void getEmailTemplates_Test() {
		
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "emailcontext");
		queryParameters.put("sortOrder", "ASC");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");

		String basePath = "email-templates";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getStandardEmailTemplates_Test() {
		
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "emailcontext");
		queryParameters.put("sortOrder", "ASC");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");

		String basePath = "email-templates/standard-email-templates";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("data.records[0].emailtemplateid",Matchers.notNullValue());

	}
	
	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchEmailTemplate_Test() {
		
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "emailcontext");
		queryParameters.put("sortOrder", "ASC");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");
		queryParameters.put("search", generatedString);
		String basePath = "email-templates";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		 id = jp.get("data.records[0].id");
	}
	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void deleteEmailTemplate_Test() {
		
		DeleteTemplatePage deleteTemplatePage = new DeleteTemplatePage();
		deleteTemplatePage.setIdsToDelete(id);
		deleteTemplatePage.setTableFlag("email_template");
		Response response = RestClient.doPost("JSON", albatrossURL, "global/delete-record", ThreadManager.getOwnerAlbatrossToken(), null, true, deleteTemplatePage);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Delete Email Templete"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getValidTestData", groups = "nightly-build")
	public void getEmailTemplateByTemplatesOf_Test(String templatesOf, String search){
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "emailcontext");
		queryParameters.put("sortOrder", "ASC");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");
		queryParameters.put("templates_of", templatesOf);

		String basePath = "email-templates";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null, true);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertNotNull(jp.get("data.records[0].id"));
		Assert.assertTrue(jp.getString("data.records[0].emailcontext").contains(search));
	}

	@Owner("Ajendra Singh")
	@Test(dataProvider = "getValidTestData", groups = "nightly-build")
	public void getEmailTemplateLazyLoad_Test(String templatesOf, String search) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "emailcontext");
		queryParameters.put("sortOrder", "ASC");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "2");
		queryParameters.put("templates_of", templatesOf);
		queryParameters.put("offset", "0");
		queryParameters.put("search", search);

		String basePath = "email-templates/lazy-load";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null, true);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertNotNull(jp.get("data.records[0].id"));
		Assert.assertTrue(jp.getString("data.records[0].emailcontext").contains(search));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getEmailTemplateLazyLoadMandatoryFields_Test() {
		function.createEmailTemplate("5", albatrossURL, albatrossTkn, true, generatedString);
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page_size", "1");
		String basePath = "email-templates/lazy-load";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null, true);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertNotNull(jp.get("data.records[0].id"));
		Assert.assertTrue(jp.getString("data.records[0].emailcontext").contains(generatedString));
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void getEmailTemplateLazyLoadUnauthorized_Test() {
		String basePath = "email-templates/lazy-load";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTkn+"abc", null, null, true);
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(response.jsonPath().getString("error"), "Unauthorized");
	}

	@Owner("Harika")
	@Test(dataProvider = "getInvalidLazyLoadTestData", groups = "nightly-build")
	public void getEmailTemplateLazyLoadWithInvalidFields_Test(String templateOf, String offset, String message) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "emailcontext");
		queryParameters.put("sortOrder", "ASC");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "2");
		queryParameters.put("templates_of", templateOf);
		queryParameters.put("offset", offset);
		queryParameters.put("search", "123");
		String basePath = "email-templates/lazy-load";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null, true);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jp.getString("message"), message);
	}

	@DataProvider(parallel = true)
	public Object[][] getValidTestData() {
		String generatedString2 = RandomStringUtils.randomAlphabetic(4);

		ExecutorService executorService = Executors.newFixedThreadPool(2);

		executorService.submit(() -> {
			function.createEmailTemplate("2", albatrossURL, albatrossTkn, true, generatedString);
		});
		executorService.submit(() -> {
			function.createEmailTemplate("5", albatrossURL ,adminToken, true, generatedString2);
		});
		executorService.shutdown();
		try {
			executorService.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Object data[][] = { { "1", generatedString }, {"2", generatedString2} };
		return data;
	}

	@DataProvider(parallel = true)
	public Object[][] getInvalidLazyLoadTestData() {
		Object data[][] = { { "abc", "1", "The templates of must be an integer.,The selected templates of is invalid." },
				{"0", "0", "The selected templates of is invalid."},
				{"1", "abc", "The offset must be an integer."} };
		return data;
	}

	@DataProvider
	public Object[][] getRelateToTypeId() {
		Object data[][] = { { "Candidate","5"},{"Contacts","2"},{"Automated Workflow Candidate Email","9" } };
		return data;
	}

}