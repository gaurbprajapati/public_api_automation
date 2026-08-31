package io.recruitcrm.albatross.auditlog;

import java.util.ArrayList;
import java.util.List;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerAuditLog;
import io.rcrm.api.pojo.auditLog.ScheduleAuditLog;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ScheduleAuditLogPageTest extends TestBase {

	JavaFakerAuditLog faker = new JavaFakerAuditLog();

	@Owner("Sai Teja SG")
	@Test(groups = {"audit_log", "nightly-build"})
	public void scheduleAuditLog_Test() {

		int intervalType = faker.getValidIntervalType();
		String recipient = ThreadManager.getAccount().getOwner().getEmail();
		List<String> recipients = new ArrayList<String>();
		recipients.add(recipient);
		ScheduleAuditLog scheduleAuditLog = new ScheduleAuditLog();
		scheduleAuditLog.setInterval_type(intervalType);
		scheduleAuditLog.setRecipients(recipients);

		Response response = RestClient.doPost1("JSON", auditLogURL, "schedule-email",
				ThreadManager.getOwnerAlbatrossToken(), null, null, true, scheduleAuditLog);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("message", Matchers.is("Audit log successfully scheduled as an email."));
		response.then().body("data.account_id", Matchers.is(ThreadManager.getAccount().getAccountId()));
		response.then().body("data.recipients", Matchers.is(recipient));
		response.then().body("data.interval_type", Matchers.is(intervalType));
	}

	@Owner("Sai Teja SG")
	@Test(groups = {"audit_log", "nightly-build"})
	public void scheduleAuditLogToMultipleRecipients_Test() {

		int intervalType = faker.getValidIntervalType();
		String recipient1 = ThreadManager.getAccount().getOwner().getEmail();
		String recipient2 = ThreadManager.getAccount().getAdmin().getEmail();
		String recipient3 = ThreadManager.getAccount().getTeamMember().getEmail();
		List<String> recipients = new ArrayList<String>();
		recipients.add(recipient1);
		recipients.add(recipient2);
		recipients.add(recipient3);
		ScheduleAuditLog scheduleAuditLog = new ScheduleAuditLog();
		scheduleAuditLog.setInterval_type(intervalType);
		scheduleAuditLog.setRecipients(recipients);

		Response response = RestClient.doPost1("JSON", auditLogURL, "schedule-email",
				ThreadManager.getOwnerAlbatrossToken(), null, null, true, scheduleAuditLog);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("message", Matchers.is("Audit log successfully scheduled as an email."));
		response.then().body("data.account_id", Matchers.is(ThreadManager.getAccount().getAccountId()));
		response.then().body("data.recipients", Matchers.is(recipient1 + "," + recipient2 + "," + recipient3));
		response.then().body("data.interval_type", Matchers.is(intervalType));
	}

	@Owner("Sai Teja SG")
	@Test(groups = {"audit_log", "nightly-build"})
	public void scheduleAuditLogWithInvalidIntervalType_Test() {

		int intervalType = faker.getInvalidIntervalType();

		List<String> recipients = new ArrayList<String>();
		recipients.add("");
		ScheduleAuditLog scheduleAuditLog = new ScheduleAuditLog();
		scheduleAuditLog.setInterval_type(intervalType);
		scheduleAuditLog.setRecipients(recipients);

		Response response = RestClient.doPost1("JSON", auditLogURL, "schedule-email",
				ThreadManager.getOwnerAlbatrossToken(), null, null, true, scheduleAuditLog);

		Assert.assertEquals(response.getStatusCode(), 422);
		response.then().body("message", Matchers.is("The given data was invalid."));
		response.then().body("errors.interval_type[0]", Matchers.is("The selected interval type is invalid."));
	}

	@Owner("Sai Teja SG")
	@Test(groups = {"audit_log", "nightly-build"})
	public void scheduleAuditLogWithEmptyData_Test() {

		ScheduleAuditLog scheduleAuditLog = new ScheduleAuditLog();

		Response response = RestClient.doPost1("JSON", auditLogURL, "schedule-email",
				ThreadManager.getOwnerAlbatrossToken(), null, null, true, scheduleAuditLog);

		Assert.assertEquals(response.getStatusCode(), 422);
		response.then().body("message", Matchers.is("The given data was invalid."));
		response.then().body("errors.recipients[0]", Matchers.is("The recipients field is required."));
		response.then().body("errors.interval_type[0]", Matchers.is("The selected interval type is invalid."));
	}

	@Owner("Sai Teja SG")
	@Test(groups = {"audit_log", "nightly-build"})
	public void unauthorizedUserCannotScheduleAuditLog_Test() {

		ScheduleAuditLog scheduleAuditLog = new ScheduleAuditLog();

		Response response = RestClient.doPost1("JSON", auditLogURL, "schedule-email",
				ThreadManager.getOwnerAlbatrossToken() + "123", null, null, true, scheduleAuditLog);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "scheduleAuditLogDataProvider", groups = {"audit_log", "nightly-build"})
	public void getScheduleAuditLogDetails_Test(int intervalType, String recipient) {

		Response response = RestClient.doGet("JSON", auditLogURL, "get-schedule-details",
				ThreadManager.getOwnerAlbatrossToken(), null, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data.account_id", Matchers.is(ThreadManager.getAccount().getAccountId()));
		response.then().body("data.recipients", Matchers.is(recipient));
		response.then().body("data.interval_type", Matchers.is(intervalType));
	}

	@Owner("Sai Teja SG")
	@Test(groups = {"audit_log", "nightly-build"})
	public void getScheduleAuditLogEmptyDetails_Test() {

		Response response = RestClient.doGet("JSON", auditLogURL, "get-schedule-details",
				ThreadManager.getOwnerAlbatrossToken(), null, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message", Matchers.is(""));
		response.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = {"audit_log", "nightly-build"})
	public void unauthorizedUserCannotGetScheduleAuditLogDetails_Test() {

		Response response = RestClient.doGet("JSON", auditLogURL, "get-schedule-details",
				ThreadManager.getOwnerAlbatrossToken() + 123, null, null, true);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = {"audit_log", "nightly-build"})
	public void cancelAuditLogSchedule_Test() {

		Response response = RestClient.doDelete("JSON", auditLogURL, "cancel-schedule",
				ThreadManager.getOwnerAlbatrossToken(), null, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("status", Matchers.is("success"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("message", Matchers.is("Audit log email schedule canceled successfully."));
	}

	@Owner("Sai Teja SG")
	@Test(groups = {"audit_log", "nightly-build"})
	public void unauthorizedUserCannotCancelAuditLogSchedule_Test() {

		Response response = RestClient.doDelete("JSON", auditLogURL, "cancel-schedule",
				ThreadManager.getOwnerAlbatrossToken() + 123, null, null, true);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider
	public Object[][] scheduleAuditLogDataProvider() {

		int intervalType = faker.getValidIntervalType();
		String recipient = ThreadManager.getAccount().getOwner().getEmail();
		List<String> recipients = new ArrayList<String>();
		recipients.add(recipient);
		ScheduleAuditLog scheduleAuditLog = new ScheduleAuditLog();
		scheduleAuditLog.setInterval_type(intervalType);
		scheduleAuditLog.setRecipients(recipients);

		Response response = RestClient.doPost1("JSON", auditLogURL, "schedule-email",
				ThreadManager.getOwnerAlbatrossToken(), null, null, true, scheduleAuditLog);

		Assert.assertEquals(response.getStatusCode(), 200);

		Object data[][] = { { intervalType, recipient } };

		return data;
	}

}
