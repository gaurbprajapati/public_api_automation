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
import io.rcrm.api.pojo.albatross.notification.ToggleNotifications;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ToggleNotificationStatusTest extends TestBase {

	public ToggleNotificationStatusTest() {
		super();
	}

	String basePath = "rcrm/toggle-notification-status";
	JavaFakerNotification fakerNotification = new JavaFakerNotification();

	@Owner("Divya")
	@Test(dataProvider = "getNotificationData")
	public void postToggleNotificationsTest_200(ArrayList<String> id) {
		ToggleNotifications toggleNotifications = new ToggleNotifications();
		toggleNotifications.setNotificationIds(id);

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, toggleNotifications);

		Assert.assertEquals(response.statusCode(), 200, "Toggle Notification Failed");
		response.then().body("message_type", Matchers.containsString("success"));
		response.then().body("message", Matchers.is("Notification read status toggled successfully!"));
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("schemaValidation//notifications//queueNotification.json"));

	}

	@Owner("Divya")
	@Test(dataProvider = "getNotificationData")
	public void postToggleNotificationsWithOptionalValuesTest_200(ArrayList<String> id) {

		String attribute = "read_status/click_status";
		ToggleNotifications toggleNotifications = new ToggleNotifications(attribute, id,
				fakerNotification.getRandomBooleanValue(), fakerNotification.getRandomBooleanValue());

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, toggleNotifications);

		Assert.assertEquals(response.statusCode(), 200, "Toggle Notification Failed");
		response.then().body("message_type", Matchers.containsString("success"));
		response.then().body("message", Matchers.is("Notification read status toggled successfully!"));
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("schemaValidation//notifications//queueNotification.json"));

	}

	@Owner("Divya")
	@Test
	public void postToggleNotificationsWithInvalidNotificationIdTest_400() {

		ArrayList<String> ids = new ArrayList<>();
		ids.add(fakerNotification.getRandomString());
		ToggleNotifications toggleNotifications = new ToggleNotifications();
		toggleNotifications.setNotificationIds(ids);

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, toggleNotifications);

		Assert.assertEquals(response.getStatusCode(), 400, "Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.notificationIds[0].errorMessage",
				Matchers.is("notificationIds must be valid Notification Id"));
		response.then().assertThat().body(
				matchesJsonSchemaInClasspath("schemaValidation//notifications//toggleNotificationWithInvalidId.json"));
	}

	@Owner("Divya")
	@Test
	public void postToggleNotificationsWithEmptyValuesTest_400() {
		ToggleNotifications toggleNotifications = new ToggleNotifications();
		toggleNotifications.setNotificationIds(null);

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, toggleNotifications);

		Assert.assertEquals(response.getStatusCode(), 400, "Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.notificationIds[0].errorMessage",
				Matchers.is("notificationIds must be valid Notification Id"));
		response.then().assertThat().body(
				matchesJsonSchemaInClasspath("schemaValidation//notifications//toggleNotificationWithInvalidId.json"));
	}

	@Owner("Divya")
	@Test
	public void postToggleNotificationsForUnauthorizedAccessTest_401() {
		ToggleNotifications toggleNotifications = new ToggleNotifications();
		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken() + "123", null, false, toggleNotifications);

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
