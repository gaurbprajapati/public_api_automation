package io.recruitcrm.albatross.auditlog;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.Login;
import io.restassured.path.json.JsonPath;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.pojo.auditLog.AuditLogList;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AuditLog_ExportLogs_AllEntities extends TestBase{
	String basePath = "export-logs";
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogs_AllEntities() {
		String candidate_slugs[] = new String[1];
		candidate_slugs[0]="all";
		String contacts_slugs[] = new String[1];
		contacts_slugs[0]="all";
		String company_slugs[] = new String[1];
		company_slugs[0]="all";
		String deals_slugs[] = new String[1];
		deals_slugs[0]="all";
		String jobs_slugs[] = new String[1];
		jobs_slugs[0]="all";
		String others_slugs[] = new String[1];
		others_slugs[0]="all";
		
		AuditLogList auditLogList = new AuditLogList();
		auditLogList.setOrderBy("added-on");
		auditLogList.setOrder("desc");
		auditLogList.setActionType("All Actions");
		auditLogList.setCandidateSlugs(candidate_slugs);
		auditLogList.setContactSlugs(contacts_slugs);
		auditLogList.setCompanySlugs(company_slugs);
		auditLogList.setDealSlugs(deals_slugs);
		auditLogList.setJobSlugs(jobs_slugs);
		auditLogList.setOtherSlugs(others_slugs);
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogs_Candidates() {
		String candidate_slugs[] = new String[1];
		candidate_slugs[0]="all";
	
		AuditLogList auditLogList = new AuditLogList();
		auditLogList.setCandidateSlugs(candidate_slugs);
		auditLogList.setOrderBy("added-on");
		auditLogList.setOrder("desc");
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogs_Contacts() {
		String contacts_slugs[] = new String[1];
		contacts_slugs[0]="all";
		
		AuditLogList auditLogList = new AuditLogList();
		auditLogList.setContactSlugs(contacts_slugs);
		auditLogList.setOrderBy("added-on");
		auditLogList.setOrder("desc");
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogs_Company() {
		String company_slugs[] = new String[1];
		company_slugs[0]="all";
		
		AuditLogList auditLogList = new AuditLogList();
		auditLogList.setCompanySlugs(company_slugs);
		auditLogList.setOrderBy("added-on");
		auditLogList.setOrder("desc");
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogs_Deals() {
		String deals_slugs[] = new String[1];
		deals_slugs[0]="all";
		
		AuditLogList auditLogList = new AuditLogList();
		auditLogList.setDealSlugs(deals_slugs);
		auditLogList.setOrderBy("added-on");
		auditLogList.setOrder("desc");
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogs_Jobs() {
		String jobs_slugs[] = new String[1];
		jobs_slugs[0]="all";
		
		AuditLogList auditLogList = new AuditLogList();
		auditLogList.setJobSlugs(jobs_slugs);
		auditLogList.setOrderBy("added-on");
		auditLogList.setOrder("desc");
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogs_Others() {
		String others_slugs[] = new String[1];
		others_slugs[0]="all";
		
		AuditLogList auditLogList = new AuditLogList();
		auditLogList.setOtherSlugs(others_slugs);
		auditLogList.setOrderBy("added-on");
		auditLogList.setOrder("desc");
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsWithInvalidCredentails() {
		AuditLogList auditLogList = new AuditLogList();
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"1234", null, null, true, auditLogList);
		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}
}
