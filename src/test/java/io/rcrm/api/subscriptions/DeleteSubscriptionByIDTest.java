package io.rcrm.api.subscriptions;

import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class DeleteSubscriptionByIDTest extends TestBase {
	Map<String, String> pathParameters;

	Response response;
	commanFunction function = new commanFunction();

	String subscriptionID = "";
	String targetURL;

	List<String> allURLs = new ArrayList<String>();
	List<String> allEvents = new ArrayList<String>();
	List<String> allSubscriptionIDs = new ArrayList<String>();

	@BeforeClass(alwaysRun = true)	public void setUp() {
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "allEvents", priority = 1, groups = "nightly-build")
	public void createSubscription_POST(String event) {
		// Generate fake urls
		Faker faker = new Faker();
		targetURL = "http://" + faker.internet().url();

		Subscription subscription = new Subscription(event, targetURL);
		response = RestClient.doPost1("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, null, false, subscription);
		JsonPath jp = response.jsonPath();
		subscriptionID = jp.get("id").toString();

		// List of URLs, Events and IDs
		allURLs.add(targetURL);
		allEvents.add(event);
		allSubscriptionIDs.add(subscriptionID);
	}

	@Owner("Rahul Shibu")
	@Test(priority = 2, groups = "nightly-build")
	public void deleteSubscriptionWithValidID_DELETE() {
		for (int i = 0; i < allSubscriptionIDs.size(); i++) {
			pathParameters = new HashMap<String, String>();
			subscriptionID = allSubscriptionIDs.get(i);

			pathParameters.put("subscription", subscriptionID);
			response = RestClient.doDelete("JSON", baseURL, "subscriptions/{subscription}", ThreadManager.getAccountApiKey(), null,
					pathParameters, false);

			response.then().statusCode(equalTo(200));
			response.then().body("success", Matchers.equalTo(true));
		}
	}

	@Owner("Rahul Shibu")
	@Test(priority = 3, groups = "nightly-build")
	public void deleteSubscriptionWithInvalidID_DELETE() {
		pathParameters = new HashMap<String, String>();
		pathParameters.put("subscription", "abc");

		response = RestClient.doDelete("JSON", baseURL, "subscriptions/{subscription}", ThreadManager.getAccountApiKey(), null,
				pathParameters, false);

		response.then().statusCode(equalTo(404));
		response.then().body("success", Matchers.equalTo(false));
	}

	@Owner("Rahul Shibu")
	@Test(priority = 4, groups = "nightly-build")
	public void deleteSubscriptionWithoutAuthorization_DELETE() {
		Map<String, String> authTokenMap = new HashMap<String, String>();
		pathParameters = new HashMap<String, String>();
		pathParameters.put("subscription", "abc");

		response = RestClient.doDelete("JSON", baseURL, "subscriptions/{subscription}", authTokenMap, null,
				pathParameters, false);

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

}
