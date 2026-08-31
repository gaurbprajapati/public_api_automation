package io.rcrm.api.subscriptions.receiveWebhook;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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
import io.rcrm.api.pojo.Subscription;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetMiscTest extends TestBase {

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
	public void setUp() throws IOException {
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
		webhookHelper = new WebhookHelper();
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "miscEvents", retryAnalyzer = RetryOn500OrSkippedAnalyzer.class)
	public void miscEvents(String event) {
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
		// Create new subscription
		Subscription subscription = new Subscription(event, webhookHelper.getTargetURL());
		RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);

		// Trigger subscription and extract data
		Response response = null;
		switch (event) {
			case "candidate.assigned": {
				response = function.assignCandidateToJob(baseURL, ThreadManager.getAccountApiKey());
				String jobSlug = response.jsonPath().get("job_slug");
				entitySlug = jobSlug;
				break;
			}
			case "meeting.deleted": {
				JsonPath json = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(),"candidate").jsonPath();
				int meetingID_int = json.get("id");
				entitySlug = String.valueOf(meetingID_int);

				Map<String, String> pathParamters = new HashMap<String, String>();
				pathParamters.put("meeting", entitySlug);
				String basePath = "meetings/{meeting}";

				response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);
				break;
			}
			case "task.deleted": {
					JsonPath json = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(),"candidate").jsonPath();
					int taskId = json.get("id");
					entitySlug = String.valueOf(taskId);

					Map<String, String> pathParamters = new HashMap<String, String>();
					pathParamters.put("task", entitySlug);
					String basePath = "tasks/{task}";

					response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);
					break;
			}
			case "calllog.deleted": {
				JsonPath json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(),"candidate").jsonPath();
				int calllogId = json.get("id");
				entitySlug = String.valueOf(calllogId);

				Map<String, String> pathParamters = new HashMap<String, String>();
				pathParamters.put("calllog", entitySlug);
				String basePath = "call-logs/{calllog}";

				response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);
				break;
			}
			case "note.deleted": {
					JsonPath json = function.createNewNoteAndGetResponse(baseURL, ThreadManager.getAccountApiKey(),"candidate").jsonPath();
					int noteId = json.get("id");
					entitySlug = String.valueOf(noteId);

					Map<String, String> pathParamters = new HashMap<String, String>();
					pathParamters.put("note", entitySlug);
					String basePath = "notes/{note}";

					response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);
					break;
			}
			case "candidate.profile.update.requested": {
					JsonPath json = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
					entitySlug = json.get("slug");
					Map<String, String> pathParamters = new HashMap<String, String>();
					pathParamters.put("candidate", entitySlug);
					String basePath = "candidates/{candidate}/request-update";

					response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);
					break;
			}
		}

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

		assertThat(responseFromWebhook, is(notNullValue()));

		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
	}

	@AfterClass(alwaysRun = true)
	public void tearDown() {
		webhookHelper.clear();
	}

	@DataProvider(name = "miscEvents")
	public Object[][] dpMethod() {
		return new Object[][] { { "candidate.assigned" },{ "meeting.deleted" },{ "calllog.deleted" },{ "note.deleted" },{ "task.deleted" },{" candidate.profile.update.requested"} };
	}
}
