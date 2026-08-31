package io.recruitcrm.albatross.auditlog;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.pojo.auditLog.AuditLogActionData;
import io.rcrm.api.pojo.auditLog.AuditLogActionKey;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AuditLogActionTest extends TestBase{
	String basePath;
	Map<String, String> queryParameters = null;

	commanFunction function = new commanFunction();
	
	@BeforeTest(alwaysRun = true)
	public void setUp() throws IOException {
		basePath = "action-log";
		queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");
	}
	
	@Owner("Smit Patel")
	@Test(groups = {"audit_log", "nightly-build"})
	public void actionLogExportCandidateAuditLogTest() {
		function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey());
		Response candidateResponse = RestClient.doGet("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(),queryParameters, null, true);
		
		JsonPath jp = candidateResponse.jsonPath();
		String entitySlug[] = new String[1];
		entitySlug[0] = jp.get("data[0].slug");
		
		
		AuditLogActionData actionData = new AuditLogActionData("candidate",5,entitySlug);
		AuditLogActionKey actionKey = new AuditLogActionKey("export",actionData);
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, actionKey);
		response.then().statusCode(200);
		response.then().body("status_code", Matchers.is(200));
		response.then().body("status_message", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//actionLogAuditLog.json"));
	}
	
	@Owner("Akshaya Uppala")
	@Test(groups = {"audit_log", "nightly-build"})
	public void actionLogExportContactAuditLogTest() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
		function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().get("slug");
		Response contactResponse = RestClient.doGet("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(),queryParameters,null, true);
		
		JsonPath jp = contactResponse.jsonPath();
		String entitySlug[] = new String[1];
		entitySlug[0] = jp.get("data[0].slug");
		
		AuditLogActionData actionData = new AuditLogActionData("contact",2,entitySlug);
		AuditLogActionKey actionKey = new AuditLogActionKey("export",actionData);
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, actionKey);
		response.then().statusCode(200);
		response.then().body("status_code", Matchers.is(200));
		response.then().body("status_message", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//actionLogAuditLog.json"));
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = {"audit_log", "nightly-build"})
	public void actionLogExportCompanyAuditLogTest() {
		function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey());
		Response companyResponse = RestClient.doGet("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(),queryParameters,null, true);
		
		JsonPath jp = companyResponse.jsonPath();
		String entitySlug[] = new String[1];
		entitySlug[0] = jp.get("data[0].slug");
		
		AuditLogActionData actionData = new AuditLogActionData("company",3,entitySlug);
		AuditLogActionKey actionKey = new AuditLogActionKey("export",actionData);
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, actionKey);
		response.then().statusCode(200);
		response.then().body("status_code", Matchers.is(200));
		response.then().body("status_message", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//actionLogAuditLog.json"));
	}
	
	@Owner("Smit Patel")
	@Test(groups = {"audit_log", "nightly-build"})
	public void actionLogExportJobsAuditLogTest() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
		String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().get("slug");
		function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug);

		Response companyResponse = RestClient.doGet("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(),queryParameters,null, true);
		
		JsonPath jp = companyResponse.jsonPath();
		String entitySlug[] = new String[1];
		entitySlug[0] = jp.get("data[0].slug");
		
		AuditLogActionData actionData = new AuditLogActionData("job",4,entitySlug);
		AuditLogActionKey actionKey = new AuditLogActionKey("export",actionData);
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, actionKey);
		response.then().statusCode(200);
		response.then().body("status_code", Matchers.is(200));
		response.then().body("status_message", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//actionLogAuditLog.json"));
	}
	
	@Owner("Akshaya Uppala")
	@Test(groups = {"audit_log", "nightly-build"})
	public void actionLogExportUserAuditLogTest() {
		AuditLogActionData actionData = new AuditLogActionData("user",6,new String[0]);
		AuditLogActionKey actionKey = new AuditLogActionKey("export",actionData);
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, actionKey);
		response.then().statusCode(200);
		response.then().body("status_code", Matchers.is(200));
		response.then().body("status_message", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//actionLogAuditLog.json"));
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = {"audit_log", "nightly-build"})
	public void actionLogExportDealsAuditLogTest() {
		function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey());
		Response dealResponse = RestClient.doGet("JSON", baseURL, "deals", ThreadManager.getAccountApiKey(),queryParameters,null, true);
		JsonPath jp = dealResponse.jsonPath();
		String entitySlug[] = new String[1];
		entitySlug[0] = jp.get("data[0].slug");
		
		AuditLogActionData actionData = new AuditLogActionData("deal",11,entitySlug);
		AuditLogActionKey actionKey = new AuditLogActionKey("export",actionData);
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, actionKey);
		response.then().statusCode(200);
		response.then().body("status_code", Matchers.is(200));
		response.then().body("status_message", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//actionLogAuditLog.json"));
	}
	
	@Owner("Smit Patel")
	@Test(groups = {"audit_log", "nightly-build"})
	public void exportActionLogWithInvalidCredentialsTest() {
		AuditLogActionData actionData = new AuditLogActionData("candidate",5,new String[0]);
		AuditLogActionKey actionKey = new AuditLogActionKey("export",actionData);
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"12345", null, null, true, actionKey);
		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}
	
	@Owner("Akshaya Uppala")
	@Test(groups = {"audit_log", "nightly-build"})
	public void exportActionLogWithInvalidEntitySlugTest() {
		String entity_slugs[] = {"76765432gt"};
		AuditLogActionData actionData = new AuditLogActionData("candidate",5,entity_slugs);
		AuditLogActionKey actionKey = new AuditLogActionKey("export",actionData);
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, actionKey);
		response.then().statusCode(500);
		response.then().body("error", Matchers.is(true));
		response.then().body("error_code", Matchers.is(500));
		response.then().body("error_message", Matchers.is("Trying to get property 'name' of non-object"));
	}
}
