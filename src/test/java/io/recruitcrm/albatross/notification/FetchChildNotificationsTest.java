package io.recruitcrm.albatross.notification;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.ArrayList;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.albatross.notification.JavaFakerNotification;
import io.rcrm.api.pojo.albatross.notification.DeleteCandidateProfileFromExternalPages;
import io.rcrm.api.pojo.albatross.notification.FetchChildNotification;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class FetchChildNotificationsTest extends TestBase {

	public FetchChildNotificationsTest() {
		super();
	}

	String basePath = "rcrm/fetch-child-notificationss";
	JavaFakerNotification fakerNotification = new JavaFakerNotification();

	@Owner("Divya")
	@Test(dataProvider = "getNotificationData")
	public void postFetchChildNotificationsTest_200(String token) {

		int limit = fakerNotification.getRandomValidLimit();
		FetchChildNotification fetchChildNotification = new FetchChildNotification(token, limit);
		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, fetchChildNotification);

		Assert.assertEquals(response.getStatusCode(), 200, "Fetch Notification failure");
		response.then().body("message_type", Matchers.containsString("success"));
		response.then().body("message", Matchers.is("Notification fetched successfully"));
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("schemaValidation//notifications//fetchNotifications.json"));

	}

	@Owner("Divya")
	@Test
	public void postFetchChildNotificationsInvalidTokenTest_400() {

		int limit = fakerNotification.getRandomValidLimit();
		FetchChildNotification fetchChildNotification = new FetchChildNotification(fakerNotification.getRandomString(),
				limit);
		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, fetchChildNotification);

		Assert.assertEquals(response.getStatusCode(), 400, "Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.continuationToken.errorMessage",
				Matchers.is("continuationToken must be valid Child Notification Id"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"schemaValidation//notifications//fetchChildNotificationWithInvalidId.json"));
	}

	@Owner("Divya")
	@Test(dataProvider = "getNotificationData")
	public void postFetchChildNotificationsInvalidLimitTest_400(String token) {

		int limit = fakerNotification.getRandomInvalidLimit();
		FetchChildNotification fetchChildNotification = new FetchChildNotification(token, limit);
		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, fetchChildNotification);

		Assert.assertEquals(response.getStatusCode(), 400, "Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.limit.errorMessage", Matchers.is("Limit must be less than 10"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"schemaValidation//notifications//fetchChildNotificationWithInvalidLimit.json"));
	}

	@Owner("Divya")
	@Test
	public void postFetchChildNotificationsWithEmptyValuesTest_400() {

		FetchChildNotification fetchChildNotification = new FetchChildNotification(null, 0);
		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, fetchChildNotification);

		Assert.assertEquals(response.getStatusCode(), 400, "Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.continuationToken.errorMessage",
				Matchers.is("continuationToken cannot be empty"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"schemaValidation//notifications//fetchChildNotificationWithInvalidId.json"));
	}

	@Owner("Divya")
	@Test
	public void postFetchChildNotificationsforUnauthorizedAccessTest_401() {

		FetchChildNotification fetchChildNotification = new FetchChildNotification();

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken() + "123", null, true, fetchChildNotification);

		Assert.assertEquals(response.getStatusCode(), 401, " Endpoint Failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("message", Matchers.is("Unauthorized"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"schemaValidation//notifications//unauthorizedAccess.json"));

	}

	@DataProvider
	public Object[] getNotificationData() {
		AllCrudFunctions crudFunctions = new AllCrudFunctions();
		JsonPath jp = (crudFunctions.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken())).jsonPath();
		String candidateSlug = jp.get("slug");
		DeleteCandidateProfileFromExternalPages deleteCandidate = new DeleteCandidateProfileFromExternalPages(
				"rcrm_" + candidateSlug);
		Response response = RestClient.doPost("JSON", albatrossURL, "v1/external-pages/delete-candidate", null, null,
				false, deleteCandidate);
		Assert.assertEquals(response.getStatusCode(), 200, " Endpoint Failure");
		Response response1 = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, true, null);
		Assert.assertEquals(response1.getStatusCode(), 200, " Endpoint Failure");
		JsonPath jsonPath = response1.jsonPath();

		ArrayList<String> notification_id = new ArrayList<>();
		notification_id.add(jsonPath.get("child_notification[0].child_id"));

		Object[] data = { notification_id };
		return data;

	}

}
