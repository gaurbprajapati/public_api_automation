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
import io.rcrm.api.pojo.albatross.notification.Context;
import io.rcrm.api.pojo.albatross.notification.Cta;
import io.rcrm.api.pojo.albatross.notification.DeleteCandidateProfileFromExternalPages;
import io.rcrm.api.pojo.albatross.notification.QueueNotification;
import io.rcrm.api.pojo.albatross.notification.Template;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class QueueNotificationsTest extends TestBase {

	public QueueNotificationsTest() {
		super();
	}

	String basePath = "rcrm/queue-notifications";
	JavaFakerNotification fakerNotification = new JavaFakerNotification();

	@Owner("Divya")
	@Test(dataProvider = "getNotificationData")
	public void postQueueNotificationsTest_200(String actionName, String sender, ArrayList<Integer> receiver,
			String source, String destination, String template_id, ArrayList<String> cta_id, String candidateName,
			String candidateSlug) {

		Template template = new Template(candidateName);
		Cta cta = new Cta(candidateSlug);
		Context context = new Context(template, cta);
		QueueNotification queueNotification = new QueueNotification(actionName, sender, receiver, source, destination,
				template_id, cta_id, context);

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, queueNotification);

		Assert.assertEquals(response.getStatusCode(), 200, "Queue Notification failure");
		response.then().body("message_type", Matchers.containsString("success"));
		response.then().body("message", Matchers.is("Notification queued successfully"));
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("schemaValidation//notifications//queueNotification.json"));

	}

	@Owner("Divya")
	@Test(dataProvider = "getNotificationData")
	public void postQueueNotificationsWithInvalidActionTest_400(String actionName, String sender,
			ArrayList<Integer> receiver, String source, String destination, String template_id,
			ArrayList<String> cta_id, String candidateName, String candidateSlug) {

		actionName = fakerNotification.getRandomString();
		Template template = new Template(candidateName);
		Cta cta = new Cta(candidateSlug);
		Context context = new Context(template, cta);
		QueueNotification queueNotification = new QueueNotification(actionName, sender, receiver, source, destination,
				template_id, cta_id, context);

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, queueNotification);

		Assert.assertEquals(response.getStatusCode(), 400, "Queue Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.action_name.errorMessage",
				Matchers.contains("Action name must be one of the following"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"schemaValidation//notifications//queueNotificationWithInvalidAction.json"));
	}

	@Owner("Divya")
	@Test(dataProvider = "getNotificationData")
	public void postQueueNotificationsWithInvalidSenderTest_400(String actionName, String sender,
			ArrayList<Integer> receiver, String source, String destination, String template_id,
			ArrayList<String> cta_id, String candidateName, String candidateSlug) {

		sender = fakerNotification.getRandomNumericValue();
		Template template = new Template(candidateName);
		Cta cta = new Cta(candidateSlug);
		Context context = new Context(template, cta);
		QueueNotification queueNotification = new QueueNotification(actionName, sender, receiver, source, destination,
				template_id, cta_id, context);

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, queueNotification);

		Assert.assertEquals(response.getStatusCode(), 400, "Queue Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.sender.errorMessage", Matchers.contains("Sender Id is invalid"));
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("schemaValidation//notifications//queueNotification.json"));
	}

	@Owner("Divya")
	@Test(dataProvider = "getNotificationData")
	public void postQueueNotificationsWithInvalidRecieverTest_400(String actionName, String sender,
			ArrayList<Integer> receiver, String source, String destination, String template_id,
			ArrayList<String> cta_id, String candidateName, String candidateSlug) {

		receiver = new ArrayList<>();
		receiver.add(fakerNotification.getRandomInvalidLimit());
		Template template = new Template(candidateName);
		Cta cta = new Cta(candidateSlug);
		Context context = new Context(template, cta);
		QueueNotification queueNotification = new QueueNotification(actionName, sender, receiver, source, destination,
				template_id, cta_id, context);

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, queueNotification);

		Assert.assertEquals(response.getStatusCode(), 400, "Queue Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.reciever.errorMessage", Matchers.contains("Reciever Id is invalid"));
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("schemaValidation//notifications//queueNotification.json"));
	}

	@Owner("Divya")
	@Test(dataProvider = "getNotificationData")
	public void postQueueNotificationsWithInvalidSourceTest_400(String actionName, String sender,
			ArrayList<Integer> receiver, String source, String destination, String template_id,
			ArrayList<String> cta_id, String candidateName, String candidateSlug) {

		source = fakerNotification.getRandomString();
		Template template = new Template(candidateName);
		Cta cta = new Cta(candidateSlug);
		Context context = new Context(template, cta);
		QueueNotification queueNotification = new QueueNotification(actionName, sender, receiver, source, destination,
				template_id, cta_id, context);

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, queueNotification);

		Assert.assertEquals(response.getStatusCode(), 400, "Queue Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.source.errorMessage",
				Matchers.contains("Source must be either RECRUITCRM or VMS"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"schemaValidation//notifications//queueNotificationWithInvalidSource.json"));
	}

	@Owner("Divya")
	@Test(dataProvider = "getNotificationData")
	public void postQueueNotificationsWithInvalidDestinationTest_400(String actionName, String sender,
			ArrayList<Integer> receiver, String source, String destination, String template_id,
			ArrayList<String> cta_id, String candidateName, String candidateSlug) {

		destination = fakerNotification.getRandomString();
		Template template = new Template(candidateName);
		Cta cta = new Cta(candidateSlug);
		Context context = new Context(template, cta);
		QueueNotification queueNotification = new QueueNotification(actionName, sender, receiver, source, destination,
				template_id, cta_id, context);

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, queueNotification);

		Assert.assertEquals(response.getStatusCode(), 400, "Queue Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.destination.errorMessage",
				Matchers.contains("Destination must be either rcrm or crm"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"schemaValidation//notifications//queueNotificationWithInvalidDestination.json"));
	}

	@Owner("Divya")
	@Test(dataProvider = "getNotificationData")
	public void postQueueNotificationsWithInvalidTemplateIdTest_400(String actionName, String sender,
			ArrayList<Integer> receiver, String source, String destination, String template_id,
			ArrayList<String> cta_id, String candidateName, String candidateSlug) {

		template_id = fakerNotification.getRandomString();
		Template template = new Template(candidateName);
		Cta cta = new Cta(candidateSlug);
		Context context = new Context(template, cta);
		QueueNotification queueNotification = new QueueNotification(actionName, sender, receiver, source, destination,
				template_id, cta_id, context);

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, queueNotification);

		Assert.assertEquals(response.getStatusCode(), 400, "Queue Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.template_id.errorMessage",
				Matchers.contains("Template id must be one of the following"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"schemaValidation//notifications//queueNotificationWithInvalidTemplate.json"));
	}

	@Owner("Divya")
	@Test(dataProvider = "getNotificationData")
	public void postQueueNotificationsWithInvalidCtaIdTest_400(String actionName, String sender,
			ArrayList<Integer> receiver, String source, String destination, String template_id,
			ArrayList<String> cta_id, String candidateName, String candidateSlug) {

		cta_id = new ArrayList<>();
		cta_id.add(fakerNotification.getRandomString());
		Template template = new Template(candidateName);
		Cta cta = new Cta(candidateSlug);
		Context context = new Context(template, cta);
		QueueNotification queueNotification = new QueueNotification(actionName, sender, receiver, source, destination,
				template_id, cta_id, context);

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, queueNotification);

		Assert.assertEquals(response.getStatusCode(), 400, "Queue Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.cta_id.errorMessage",
				Matchers.contains("Cta id must be one of the following"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"schemaValidation//notifications//queueNotificationWithInvalidTemplate.json"));
	}

	@Owner("Divya")
	@Test
	public void postQueueNotificationsWithEmptyValuesTest_400() {

		Template template = new Template(null);
		Cta cta = new Cta(null);
		Context context = new Context(template, cta);
		QueueNotification queueNotification = new QueueNotification(null, null, null, null, null, null, null, context);

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, false, queueNotification);

		Assert.assertEquals(response.getStatusCode(), 400, "Queue Notification failure");
		response.then().body("message_type", Matchers.containsString("failure"));
		response.then().body("errors.errors.action_name.errorMessage",
				Matchers.contains("Action name is required."));
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("schemaValidation//notifications//queueNotificationWithEmptyValues.json"));
	}

	@Owner("Divya")
	@Test
	public void postQueueNotificationsforUnauthorizedAccessTest_401() {

		QueueNotification queueNotification = new QueueNotification();

		Response response = RestClient.doPost("JSON", notificationServiceURL, basePath,
				ThreadManager.getOwnerAlbatrossToken() + "123", null, true, queueNotification);

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

		ArrayList<String> cta_id = new ArrayList<>();
		cta_id.add(jsonPath.get("data.records[0].payload.cta_id[0]"));

		Object[] data = { jsonPath.get("data.records[0].payload.action_name"),
				jsonPath.get("data.records[0].payload.sender").toString(),
				jsonPath.get("data.records[0].payload.receiver"), jsonPath.get("data.records[0].payload.source"),
				jsonPath.get("data.records[0].payload.destination"),
				jsonPath.get("data.records[0].payload.template_id"), cta_id,
				jsonPath.get("data.context.template.candidate_name"), jsonPath.get("data.context.cta.candidate_slug") };
		return data;

	}

}
