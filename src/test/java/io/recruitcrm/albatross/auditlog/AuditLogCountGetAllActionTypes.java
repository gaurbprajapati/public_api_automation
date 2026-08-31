package io.recruitcrm.albatross.auditlog;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;

import io.rcrm.api.pojo.auditLog.AuditLogList;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AuditLogCountGetAllActionTypes extends TestBase{
	String orderBy = "added-on";
	String order = "desc";
	int dateFrom;
	int dateTo;
	String candidate_slugs[] = {"all"};
	String contacts_slugs[] = {"all"};
	String company_slugs[] = {"all" };
	String deals_slugs[] = {"all"};
	String jobs_slugs[] = {"all"};
	String others_slugs[] = {"all"};
	String basePath;
	
	AuditLogList auditLogList;
	
	@BeforeTest
	public void setUp() throws IOException {
		basePath = "count/get";
		
		auditLogList = new AuditLogList();
		auditLogList.setOrderBy(orderBy);
		auditLogList.setOrder(order);
		auditLogList.setActionType("Create");
		auditLogList.setDateFrom(dateFrom);
		auditLogList.setDateTo(dateTo);
		auditLogList.setCandidateSlugs(candidate_slugs);
		auditLogList.setContactSlugs(contacts_slugs);
		auditLogList.setCompanySlugs(company_slugs);
		auditLogList.setDealSlugs(deals_slugs);
		auditLogList.setJobSlugs(jobs_slugs);
		auditLogList.setOtherSlugs(others_slugs);
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_Create() {
		auditLogList.setActionType("Create");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_Update() {
		auditLogList.setActionType("Update");
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_Delete() {
		auditLogList.setActionType("Delete");
		
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_Import() {
		auditLogList.setActionType("Import");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_Export() {
		auditLogList.setActionType("Export");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_BulkUpdate() {
		auditLogList.setActionType("BulkUpdate");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_BulkDElete() {
		auditLogList.setActionType("Bulk Delete");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_MergeDuplicates() {
		auditLogList.setActionType("Merge Duplicates");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_LinkRecord() {
		auditLogList.setActionType("Link Record");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_Send() {
		auditLogList.setActionType("Send");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_Archived() {
		auditLogList.setActionType("Archived");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_Parse() {
		auditLogList.setActionType("Parse");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_SubmitCandidates() {
		auditLogList.setActionType("Submit Candidates");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_Payments() {
		auditLogList.setActionType("Payments");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_ConnectEmail() {
		auditLogList.setActionType("Connect Email");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_DisconnectEmail() {
		auditLogList.setActionType("Disconnect Email");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_ConnectCalender() {
		auditLogList.setActionType("Connect Calender");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_DisconnectCalender() {
		auditLogList.setActionType("Disconnect Calender");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_ParseAssign() {
		auditLogList.setActionType("Parse & Assign");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_Assign() {
		auditLogList.setActionType("Assign");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogCountGetActionType_AllActions() {
		auditLogList.setActionType("All Actions");
		

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//getCountAuditLog.json"));
	}
}
