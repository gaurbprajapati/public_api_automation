
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
public class AllEndpointsOfSubscriptionsTest extends TestBase {
	Map<String, String> pathParameters;

	Response response;
	commanFunction function = new commanFunction();

	String subscriptionID = "";
	String event = "candidate.created";
	Faker faker = new Faker();
	String targetURL = "http://" + faker.internet().url();

	@BeforeClass
	public void startUp() throws IOException {
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
	}

	@Owner("Rahul Shibu")
	@Test(priority = 1, groups = "nightly-build")
	public void createSubscription_POST() {
		Subscription subscription = new Subscription(event, targetURL);
		response = RestClient.doPost1("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, null, true, subscription);
		subscriptionID = response.jsonPath().get("id").toString();

		response.then().statusCode(equalTo(200));
		response.then().body(matchesJsonSchemaInClasspath("publicApi//subscriptions//subscriptions.json"));
		response.then().body("event", Matchers.equalTo(event));
		response.then().body("target_url", Matchers.equalTo(targetURL));
	}

	@Owner("Rahul Shibu")
	@Test(priority = 2, groups = "nightly-build")
	public void getSubscriptions_GET() {
		response = RestClient.doGet("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, null, false);

		response.then().statusCode(equalTo(200));
		response.then().body(matchesJsonSchemaInClasspath("publicApi//subscriptions//showAllSubscriptions.json"));
		response.then().body("data.event[0]", Matchers.equalTo(event));
		response.then().body("data.target_url[0]", Matchers.equalTo(targetURL));
	}

	@Owner("Rahul Shibu")
	@Test(priority = 3, groups = "nightly-build")
	public void searchSubscription_GET() {
		pathParameters = new HashMap<String, String>();
		pathParameters.put("subscription", subscriptionID);
		response = RestClient.doGet("JSON", baseURL, "subscriptions/{subscription}", ThreadManager.getAccountApiKey(), null, pathParameters,
				true);

		response.then().statusCode(equalTo(200));
		response.then().body(matchesJsonSchemaInClasspath("publicApi//subscriptions//subscriptions.json"));
		response.then().body("event", Matchers.equalTo(event));
		response.then().body("target_url", Matchers.equalTo(targetURL));
	}

	@Owner("Rahul Shibu")
	@Test(priority = 4, groups = "nightly-build")
	public void editSubscription_POST() {
		Subscription subscription = new Subscription("candidate.assigned", "https://www.checkupdates.com");
		pathParameters = new HashMap<String, String>();
		pathParameters.put("subscription", subscriptionID);
		response = RestClient.doPost1("JSON", baseURL, "subscriptions/{subscription}", ThreadManager.getAccountApiKey(), null,
				pathParameters, false, subscription);

		response.then().statusCode(equalTo(200));
		response.then().body(matchesJsonSchemaInClasspath("publicApi//subscriptions//subscriptions.json"));
		response.then().body("event", Matchers.equalTo("candidate.assigned"));
		response.then().body("target_url", Matchers.equalTo("https://www.checkupdates.com"));

	}

	@Owner("Rahul Shibu")
	@Test(priority = 5, groups = "nightly-build")
	public void deleteSubscription_DELETE() {
		pathParameters = new HashMap<String, String>();
		pathParameters.put("subscription", subscriptionID);
		response = RestClient.doDelete("JSON", baseURL, "subscriptions/{subscription}", ThreadManager.getAccountApiKey(), null,
				pathParameters, false);

		response.then().statusCode(equalTo(200));
		response.then().body("success", Matchers.equalTo(true));
	}

	@AfterClass(alwaysRun = true)
	public void tearDown() {
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
	}

}
