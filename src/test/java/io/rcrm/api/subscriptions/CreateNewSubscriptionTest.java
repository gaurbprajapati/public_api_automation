package io.rcrm.api.subscriptions;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.Subscription;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class CreateNewSubscriptionTest extends TestBase {

	Response response;
	commanFunction function = new commanFunction();

	@BeforeClass(alwaysRun = true)	public void setUp() {
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "allEvents", groups = "nightly-build")
	public void createSubscription_POST(String validEvent) {
		Faker faker = new Faker();
		String targetURL = "http://" + faker.internet().url();

		Subscription subscription = new Subscription(validEvent, targetURL);
		response = RestClient.doPost1("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, null, false, subscription);

		response.then().statusCode(equalTo(200));
		response.then().body(matchesJsonSchemaInClasspath("publicApi//subscriptions//subscriptions.json"));
		response.then().body("event", Matchers.equalTo(validEvent));
		response.then().body("target_url", Matchers.equalTo(targetURL));
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "invalidData", groups = "nightly-build")
	public void createSubscriptionWithInvalidData_POST(String invalidEvent, String targetURL, String jsonSelector,
			String errorMessage1, String errorMessage2) {

		Subscription subscription = new Subscription(invalidEvent, targetURL);
		response = RestClient.doPost1("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, null, false, subscription);

		response.then().statusCode(equalTo(422));
		if (errorMessage2 == "") {
			response.then().body(jsonSelector, Matchers.equalTo(errorMessage1));
		} else {
			response.then().body("event[0]", Matchers.equalTo(errorMessage1));
			response.then().body("target_url[0]", Matchers.equalTo(errorMessage2));
		}
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createSubscriptionWithoutAuthorization_POST() {
		Faker faker = new Faker();
		String targetURL = "http://" + faker.internet().url();
		Map<String, String> authTokenMap = new HashMap<String, String>();

		Subscription subscription = new Subscription("candidate.created", targetURL);
		response = RestClient.doPost1("JSON", baseURL, "subscriptions", authTokenMap, null, null, false, subscription);

		response.then().statusCode(equalTo(401));
		response.then().body("error", Matchers.equalTo("Unauthorized"));
	}

	@AfterClass(alwaysRun = true)
	public void tearDown() {
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
	}

	@DataProvider(name = "allEvents")
	public Object[][] dpMethod() {
		return new Object[][] { { "candidate.assigned" }, { "candidate.created" }, { "candidate.created.talentpool" },
				{ "candidate.updated" }, { "candidate.external.profile.updated" },
				{ "candidate.external.applied.jobs" }, { "candidate.hiringstage.updated" },
				{ "candidate.clientfeedback.received" }, { "contact.created" }, { "contact.updated" },
				{ "company.created" }, { "company.updated" }, { "job.created" }, { "job.updated" },
				{ "job.status.updated" }, { "deal.created" }, { "deal.updated" }, { "deal.stage.updated" },
				{ "meeting.created" }, { "meeting.updated" },{ "meeting.deleted" }, { "task.created" }, { "task.updated" },
				{ "note.created" }, { "note.updated" }, { "calllog.created" }, { "calllog.updated" } };
	}

	@DataProvider
	public synchronized Object[][] invalidData() {
		Object data[][] = { { "", "http://www.google.com", "event[0]", "The event field is required.", "" },
				{ "non.existent.event", "http://www.google.com", "event[0]", "The selected event is invalid.", "" },
				{ "candidate.created", "", "target_url[0]", "The target url field is required.", "" },
				{ "candidate.created", "www.urlwithouthttp.com", "target_url[0]", "The target url format is invalid.",
						"" },
				{ "", "", "", "The event field is required.", "The target url field is required." },
				{ "non.existent.event", "", "", "The selected event is invalid.", "The target url field is required." },
				{ "", "www.urlwithouthttp.com", "", "The event field is required.",
						"The target url format is invalid." },
				{ "non.existent.event", "www.urlwithouthttp.com", "", "The selected event is invalid.",
						"The target url format is invalid." }, };
		return data;
	}
}
