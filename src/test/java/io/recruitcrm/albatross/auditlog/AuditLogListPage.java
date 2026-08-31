package io.recruitcrm.albatross.auditlog;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.albatross.New_sms_templatePage;
import io.rcrm.api.pojo.albatross.SmsTemplatePage;
import io.rcrm.api.pojo.auditLog.AuditLogList;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.albatross.company.Company;
import io.recruitcrm.albatross.company.CompanyJson;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AuditLogListPage extends TestBase {
	
	JavaFakerCompany faker = new JavaFakerCompany();

	String companyName = faker.getCompanyName();
	String companyWebsite = faker.getUrl();
	String contactNumber = "13456789087654";
	String companyCity = faker.getCity();
	String address = faker.getAddress();
	int industry_id = faker.getIndustry_id();
	String logo = faker.getLogoURL();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	
	static int id;
	
	@Owner("Rahul Shibu")
	@Test
	public void getAuditLogListTest() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");

		String basePath = "search";
		
		AuditLogList auditLogList = new AuditLogList();
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("current_page", Matchers.is(1));
		response.then().body("data", Matchers.notNullValue());
		response.then().body("data[0]._id", Matchers.notNullValue());
		response.then().body("data[0].entity_slug", Matchers.notNullValue());
		response.then().body("data[0].entity_slug_detail", Matchers.notNullValue());
		response.then().body("data[0].field_updated_detail", Matchers.notNullValue());
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getAuditLogList.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void getAuditLogListWithInvalidCredentialsTest() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");

		String basePath = "search";
		
		AuditLogList auditLogList = new AuditLogList();
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", queryParameters, null, true, auditLogList);
		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
		}
	
	@Owner("Rahul Shibu")
	@Test
	public void exportLogsInAuditLogTest() {
		String basePath = "export-logs";
		AuditLogList auditLogList = new AuditLogList();
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);

	}

}
