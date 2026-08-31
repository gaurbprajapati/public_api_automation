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
public class AuditLog_ExportLogs_AllActions extends TestBase{
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
	
	@BeforeClass
	public void setUp() throws IOException {
		basePath = "export-logs";

		auditLogList = new AuditLogList();
		auditLogList.setOrderBy(orderBy);
		auditLogList.setOrder(order);
		auditLogList.setCandidateSlugs(candidate_slugs);
		auditLogList.setContactSlugs(contacts_slugs);
		auditLogList.setCompanySlugs(company_slugs);
		auditLogList.setDealSlugs(deals_slugs);
		auditLogList.setJobSlugs(jobs_slugs);
		auditLogList.setOtherSlugs(others_slugs);

	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_Create() {
		auditLogList.setActionType("Create");
		
		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_Update() {
		auditLogList.setActionType("Update");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_Delete() {
		auditLogList.setActionType("Delete");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_Import() {
		auditLogList.setActionType("Import");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_Export() {
		auditLogList.setActionType("Export");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_BulkUpdate() {
		auditLogList.setActionType("Bulk Update");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_BulkDelete() {
		auditLogList.setActionType("Create");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_MergeDuplicate() {
		auditLogList.setActionType("Merge Duplicates");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_LinkRecord() {
		auditLogList.setActionType("Link Record");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_Send() {
		auditLogList.setActionType("Send");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_Archived() {
		auditLogList.setActionType("Archived");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_Parse() {
		auditLogList.setActionType("Parse");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_SubmitCandidates() {
		auditLogList.setActionType("Submit Candidates");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_Payment() {
		auditLogList.setActionType("Payment");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_ConnectEmail() {
		auditLogList.setActionType("Connect Email");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_ConnectCalendar() {
		auditLogList.setActionType("Connect Calendar");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_DisconnectEmail() {
		auditLogList.setActionType("Disconnect Email");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_DisconnectCalendar() {
		auditLogList.setActionType("Disconnect Calendar");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_ParseAssign() {
		auditLogList.setActionType("Parse & Assign");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_Assign() {
		auditLogList.setActionType("Connect Email");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test
	public void auditLogExportLogsActionType_AllActions() {
		auditLogList.setActionType("All Actions");

		Response response = RestClient.doPost1("JSON", auditLogURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, auditLogList);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Audit logs Exported successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//auditLog//exportAuditLog.json"));
	}
	
}
