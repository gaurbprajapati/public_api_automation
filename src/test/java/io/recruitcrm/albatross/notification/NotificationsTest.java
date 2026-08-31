package io.recruitcrm.albatross.notification;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.albatross.notification.JavaFakerNotification;
import io.rcrm.api.pojo.albatross.notification.DeleteCandidateProfileFromExternalPages;
import io.rcrm.api.pojo.albatross.notification.Notifications;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class NotificationsTest extends TestBase {

	String continuationToken = null;

	public NotificationsTest() {
		super();
	}

	String basePath = "rcrm/notifications";
	JavaFakerNotification fakerNotification = new JavaFakerNotification();

	@Owner("Divya")
	@Test
	public void postNotificationsForNewUserTest_200() {
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
		response1.then().body("message_type", Matchers.containsString("success"));
		response1.then().body("message", Matchers.is("Notification fetched successfully"));
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("schemaValidation//notifications//notification.json"));
	}

	@Owner("Divya")
	@Test
	public void postNotificationsTestWithMandatoryFields_200() {
		int limit = fakerNotification.getRandomValidLimit();

		Notifications notifications = new Notifications();
		notifications.setLimit(limit);
		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, true, notifications);

		Assert.assertEquals(response.getStatusCode(), 200, " Endpoint Failure");
		JsonPath jsonPath = response.jsonPath();
		continuationToken = jsonPath.get("child_notification[0].child_id");
		response.then().body("message_type", Matchers.containsString("success"));
		response.then().body("message", Matchers.is("Notification fetched successfully"));
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("schemaValidation//notifications//notification.json"));
	}

	@Owner("Divya")
	@Test
	public void postNotificationsTestWithOptionalFields_200() {
		int limit = fakerNotification.getRandomValidLimit();
		boolean onlyUnreadNotification = fakerNotification.getRandomBooleanValue();
		boolean isPolling = fakerNotification.getRandomBooleanValue();

		Notifications notifications = new Notifications(continuationToken, limit, onlyUnreadNotification, isPolling);
		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, true, notifications);

		Assert.assertEquals(response.getStatusCode(), 200, " Endpoint Failure");
		response.then().body("message_type", Matchers.containsString("success"));
		response.then().body("message", Matchers.is("Notification fetched successfully"));
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi//allUsersAndTeams//getAllUsersAndTeams.json"));
	}

	@Owner("Divya")
	@Test
	public void postNotificationsTestWithInvalidLimit_400() {
		int limit = fakerNotification.getRandomInvalidLimit();

		Notifications notifications = new Notifications();
		notifications.setLimit(limit);
		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, true, notifications);

		Assert.assertEquals(response.getStatusCode(), 400, "Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.limit.errorMessage", Matchers.is("Limit must be less than 10"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"schemaValidation//notifications//fetchChildNotificationWithInvalidLimit.json"));
	}

	@Owner("Divya")
	@Test
	public void postNotificationsTestWithInvalidToken_400() {
		int limit = fakerNotification.getRandomValidLimit();
		continuationToken = fakerNotification.getRandomString();
		Notifications notifications = new Notifications();
		notifications.setLimit(limit);
		notifications.setContinuationToken(continuationToken);
		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, true, notifications);

		Assert.assertEquals(response.getStatusCode(), 400, "Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.continuationToken.errorMessage",
				Matchers.is("continuationToken must be valid Child Notification Id"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"schemaValidation//notifications//fetchChildNotificationWithInvalidId.json"));
	}

	@Owner("Divya")
	@Test
	public void postNotificationsForUnauthorizedUserTest_401() {
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

}
