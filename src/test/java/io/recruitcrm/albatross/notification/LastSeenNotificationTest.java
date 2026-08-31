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
import io.rcrm.api.pojo.albatross.notification.LastSeenNotification;
import io.rcrm.api.pojo.albatross.notification.Notifications;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class LastSeenNotificationTest extends TestBase {

	public LastSeenNotificationTest() {
		super();
	}

	String basePath = "rcrm/queue-notifications";
	JavaFakerNotification fakerNotification = new JavaFakerNotification();

	@Owner("Divya")
	@Test(dataProvider = "getNotificationData")
	public void postLastSeenNotificationsTest_200(String id) {

		LastSeenNotification lastSeenNotification = new LastSeenNotification();
		lastSeenNotification.setLastSeenNotificationId(id);

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, lastSeenNotification);

		Assert.assertEquals(response.getStatusCode(), 200, "Last seen Notification failure");
		response.then().body("message_type", Matchers.containsString("success"));
		response.then().body("message", Matchers.is("Last seen notification saved successfully"));
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("schemaValidation//notifications//queueNotification.json"));
	}

	@Owner("Divya")
	@Test
	public void postLastSeenNotificationsWithInvalidIdTest_400() {

		LastSeenNotification lastSeenNotification = new LastSeenNotification();
		lastSeenNotification.setLastSeenNotificationId(fakerNotification.getRandomString());

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, lastSeenNotification);

		Assert.assertEquals(response.getStatusCode(), 400, "Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.lastSeenNotificationId.errorMessage",
				Matchers.is("lastSeenNotificationId must be a valid notification id"));
		response.then().assertThat().body(
				matchesJsonSchemaInClasspath("schemaValidation//notifications//lastSeenNotificationInvalidId.json"));
	}

	@Owner("Divya")
	@Test
	public void postLastSeenNotificationsWithEmptyIdTest_400() {

		LastSeenNotification lastSeenNotification = new LastSeenNotification();
		lastSeenNotification.setLastSeenNotificationId(null);

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, lastSeenNotification);

		Assert.assertEquals(response.getStatusCode(), 200, "Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.lastSeenNotificationId.errorMessage",
				Matchers.is("lastSeenNotificationId must be a valid notification id"));
		response.then().assertThat().body(
				matchesJsonSchemaInClasspath("schemaValidation//notifications//lastSeenNotificationInvalidId.json"));
	}

	@Owner("Divya")
	@Test
	public void postLastSeenNotificationsForUnauthorizedUserTest_401() {
		int limit = fakerNotification.getRandomInvalidLimit();
		String continuationToken = fakerNotification.getRandomString();
		Notifications notifications = new Notifications();
		notifications.setLimit(limit);
		notifications.setContinuationToken(continuationToken);
		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken() + "123", null, true, notifications);

		Assert.assertEquals(response.getStatusCode(), 401, " Endpoint Failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("message", Matchers.is("Unauthorized"));
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("schemaValidation//notifications//unauthorizedAccess.json"));
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
		notification_id.add(jsonPath.get("data.records[0]._id"));

		Object[] data = { notification_id };
		return data;

	}

}
