package io.rcrm.api.subscriptions.receiveWebhook;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.WebhookHelper;
import com.qa.api.util.RetryOn500OrSkippedAnalyzer;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCallLog;
import io.rcrm.api.javafaker.JavaFakerMeeting;
import io.rcrm.api.javafaker.JavaFakerNote;
import io.rcrm.api.javafaker.JavaFakerTask;
import io.rcrm.api.pojo.Call_Log;
import io.rcrm.api.pojo.Meeting;
import io.rcrm.api.pojo.Note;
import io.rcrm.api.pojo.Subscription;
import io.rcrm.api.pojo.Task;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetEntityCreatedTest extends TestBase {

	commanFunction function = new commanFunction();
	WebhookHelper webhookHelper;
	Map<String, String> pathParameters;
	String responseFromWebhook;
	JsonPath jp;
	List<String> slugs = new ArrayList<String>();

	JavaFakerNote fakeNote = new JavaFakerNote();
	JavaFakerTask fakerTask = new JavaFakerTask();
	JavaFakerMeeting fakerMeeting = new JavaFakerMeeting();
	JavaFakerCallLog fakerakerCallLog = new JavaFakerCallLog();

	String entitySlug = "";

	@BeforeClass
	public void setUp(){
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
		webhookHelper = new WebhookHelper();
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "creationEvents", retryAnalyzer = RetryOn500OrSkippedAnalyzer.class)
	public void getEntityCreated(String event) {
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
		// Create new subscription
		Subscription subscription = new Subscription(event, webhookHelper.getTargetURL());
		RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);

		// Trigger subscription and extract data
		Response response = null;
		switch (event) {
		case "candidate.created": {
			response = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey());
			break;
		}
		case "contact.created": {
			String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug");
			slugs.add(companySlug);

			response = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug);
			break;
		}
		case "company.created": {
			response = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey());
			break;
		}
		case "job.created": {
			String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug");
			slugs.add(companySlug);
			String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath()
					.get("slug");
			slugs.add(contactSlug);

			response = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug);
			break;
		}
		case "deal.created": {
			response = function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey());
			break;
		}
		case "meeting.created": {
			String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug");
			slugs.add(candidateSlug);

			String startDate = fakerTask.getFutureDate();
			String address = fakerMeeting.getAddress();
			String meetingName = fakerMeeting.getMeetingName();
			String meetingDescription = fakerMeeting.getDescription();
			String endDate = fakerMeeting.getEndDateWithReferenceDate(startDate);
			String pastDate = fakerTask.getPastDate();

			Meeting meeting = new Meeting();
			meeting.setTitle(meetingName);
			meeting.setDescription(meetingDescription);
			meeting.setAddress(address);
			meeting.setReminder(15);
			meeting.setRelated_to(candidateSlug);
			meeting.setRelated_to_type("candidate");
			meeting.setStart_date(pastDate);
			meeting.setEnd_date(endDate);

			response = RestClient.doPost("JSON", baseURL, "meetings", ThreadManager.getAccountApiKey(), null, true, meeting);
			break;
		}
		case "task.created": {
			String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug");
			slugs.add(candidateSlug);

			String taskTitle = fakerTask.getTaskName();
			String taskDescription = fakerTask.getDescription();
			String startDate = fakerTask.getFutureDate();

			Task task = new Task();
			task.setTitle(taskTitle);
			task.setDescription(taskDescription);
			task.setReminder(30);
			task.setRelated_to(candidateSlug);
			task.setRelated_to_type("candidate");
			task.setStart_date(startDate);

			response = RestClient.doPost("JSON", baseURL, "tasks", ThreadManager.getAccountApiKey(), null, true, task);
			break;
		}
		case "note.created": {
			String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug");
			slugs.add(candidateSlug);

			String notesText = fakeNote.getNotes();

			Note note = new Note();
			note.setRelated_to(candidateSlug);
			note.setRelated_to_type("candidate");
			note.setDescription(notesText + "<br><br>" + notesText); // System.lineSeparator()

			response = RestClient.doPost("JSON", baseURL, "notes", ThreadManager.getAccountApiKey(), null, true, note);
			break;
		}
		case "calllog.created": {
			String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug");
			slugs.add(candidateSlug);

			String contactNo = fakerakerCallLog.getContactNumber();
			String callNotes = fakerakerCallLog.getCall_notes();
			String pastDate = fakerTask.getPastDate();

			Call_Log callLog = new Call_Log();
			callLog.setCall_notes(callNotes);
			callLog.setCall_type("CALL_INCOMING");
			callLog.setContact_number(contactNo);
			callLog.setRelated_to(candidateSlug);
			callLog.setRelated_to_type("candidate");
			callLog.setCall_started_on(pastDate);

			response = RestClient.doPost("JSON", baseURL, "call-logs", ThreadManager.getAccountApiKey(), null, true, callLog);
			break;
		}
		}

		jp = response.jsonPath();

		if (jp.get("slug") != null) {
			entitySlug = jp.get("slug");
		} else {
			entitySlug = jp.get("id").toString();
		}
		slugs.add(entitySlug);

		// Fetch data from webhook site
		try {
			responseFromWebhook = webhookHelper.getData(entitySlug);
		} catch (Exception e) {
			Assert.fail("Failed to fetch Webhook data for Event "+event+", "+e.getMessage());
		}

		/**
		 * Verify that 1) If webhook is sent in the first place 2) Correct contents are
		 * sent
		 */

		assertThat(responseFromWebhook, containsString(entitySlug));

		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
	}

	@AfterClass(alwaysRun = true)
	public void tearDown() {
		webhookHelper.clear();
	}

	@DataProvider(name = "creationEvents")
	public Object[][] dpMethod() {
		return new Object[][] { { "candidate.created" }, { "contact.created" }, { "company.created" },
				{ "job.created" }, { "deal.created" }, { "meeting.created" }, { "task.created" }, { "note.created" },
				{ "calllog.created" } };
	}
}
