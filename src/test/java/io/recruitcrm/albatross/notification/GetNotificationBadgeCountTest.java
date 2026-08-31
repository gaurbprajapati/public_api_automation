package io.recruitcrm.albatross.notification;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.albatross.notification.JavaFakerNotification;
import io.rcrm.api.pojo.albatross.notification.DeleteCandidateProfileFromExternalPages;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetNotificationBadgeCountTest extends TestBase {

	public GetNotificationBadgeCountTest() {
		super();
	}

	String basePath = "rcrm/get-notification-badge-count";
	JavaFakerNotification fakerNotification = new JavaFakerNotification();

	@Owner("Divya")
	@Test
	public void getNotificationCountTest_200() {

		AllCrudFunctions crudFunctions = new AllCrudFunctions();
		JsonPath jp = (crudFunctions.createCandidate(albatrossURL, ThreadManager.getAccountApiKey())).jsonPath();
		String candidateSlug = jp.get("slug");
		DeleteCandidateProfileFromExternalPages deleteCandidate = new DeleteCandidateProfileFromExternalPages(
				"rcrm_" + candidateSlug);
		Response response = RestClient.doPost("JSON", albatrossURL, "v1/external-pages/delete-candidate", null, null,
				false, deleteCandidate);
		Assert.assertEquals(response.getStatusCode(), 200, " Endpoint Failure");
		Response response1 = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, true, null);
		Assert.assertEquals(response1.getStatusCode(), 200, " Endpoint Failure");
		Response response2 = RestClient.doGet("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, null, true);

		Assert.assertEquals(response2.getStatusCode(), 200, " Endpoint Failure");
		response2.then().body("data.count", Matchers.notNullValue());
		response2.then().body("message_type", Matchers.containsString("success"));
		response2.then().body("message", Matchers.is("Notification badge count fetched successfully"));
		response2.then().assertThat()
				.body(matchesJsonSchemaInClasspath("schemaValidation//notifications//getNotificationCount.json"));

	}

	@Owner("Divya")
	@Test
	public void getNotificationCountWithInvalidURLTest_404() {

		Response response = RestClient.doGet("JSON", notificationServiceURL, basePath + "123",
				ThreadManager.getOwnerAlbatrossToken(), null, null, true);

		Assert.assertEquals(response.getStatusCode(), 404, " Endpoint Failure");
	}

	@Owner("Divya")
	@Test
	public void getNotificationCountForUnauthorizedAccessTest_401() {

		Response response = RestClient.doGet("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken() + "123", null, null, true);

		Assert.assertEquals(response.getStatusCode(), 401, " Endpoint Failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("message", Matchers.is("Unauthorized"));
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("schemaValidation//notifications//unauthorizedAccess.json"));
	}

}
