package io.recruitcrm.nyma.scheduledEmail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerMails;
import io.rcrm.api.pojo.nyma.ReceiverEmailsPage;
import io.rcrm.api.pojo.nyma.ScheduledEmailPage;
import io.rcrm.api.pojo.nyma.Scheduled_emailPage;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class GetAllScheduledEmailsTest extends TestBase {

	String scheduledEmailID = "";
	commanFunction function = new commanFunction();
	JavaFakerMails fakerMails = new JavaFakerMails();
	long epoch = System.currentTimeMillis()/1000;
	long l=epoch;int i=(int)l+1800;
	String generatedString = RandomStringUtils.randomAlphabetic(4);

	@BeforeClass(alwaysRun = true)	public void setUp() throws IOException {
		createScheduledEmail();
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void getScheduledEmailsList_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "scheduled_on");
		queryParameters.put("sort_order", "asc");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");
		queryParameters.put("linked_email_type", "1");
		String basePath = "scheduled-emails/";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int emailID = jp.get("data.records.id[0]");
		scheduledEmailID = Integer.toString(emailID);

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void getScheduledEmailsListInvalidParamsTest() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "scheduled_on");
		queryParameters.put("sort_order", "asc");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");
		queryParameters.put("linked_email_type", "3");

		String basePath = "scheduled-emails/";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(422);
		JsonPath jp = response.jsonPath();

		Assert.assertTrue(jp.getString("message").contains("The selected linked email type is invalid."));

	}

	@Owner("Priyanka Shinde")
	@Test(dependsOnMethods = "getScheduledEmailsList_Test", groups = "nightly-build")
	public void getScheduledEmailsListWithScheduledID_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", scheduledEmailID);

		String basePath = " scheduled-emails/email-body/{ID}";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);


		response.then().statusCode(200);
	}

	@Owner("Priyanka Shinde")
	@Test(dependsOnMethods = "getScheduledEmailsList_Test", groups = "nightly-build")
	public void getScheduledEmailsListWithAttachment_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", scheduledEmailID);

		String basePath = " scheduled-emails/email-attachments/{ID}";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);


		response.then().statusCode(200);
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void unAuthorizedUserCannotGetScheduledEmailsList_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "scheduled_on");
		queryParameters.put("sort_order", "asc");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");
		String basePath = "scheduled-emails/";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int emailID = jp.get("data.records.id[0]");
		scheduledEmailID = Integer.toString(emailID);

	}

	private void createScheduledEmail(){
		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");
		String candidateName = jsonCandidate.get("first_name") + " " + jsonCandidate.get("last_name");
		String candidateEmail = jsonCandidate.get("email");

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getCandResponse =  albatrossFunctions.getCandidateResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), candidateEntitySlug);
		JsonPath jpCand = getCandResponse.jsonPath();
		int candID = jpCand.get("data.candidate.id");

		ArrayList<Object> receiverList = new ArrayList<Object>();
		ReceiverEmailsPage candEmailsPage = new ReceiverEmailsPage(candidateEmail, candidateName, candidateEntitySlug, 5, candID);
		receiverList.add(candEmailsPage);

		ScheduledEmailPage scheduledEmailPage = new ScheduledEmailPage();
		scheduledEmailPage.setVersion(1);
		scheduledEmailPage.setReceivers(receiverList);
		scheduledEmailPage.setScheduled_on(i);
		scheduledEmailPage.setTimezone_id(1);
		scheduledEmailPage.setInclude_opt_out_link(0);
		scheduledEmailPage.setSubject(fakerMails.getFakeEmailSubject());
		scheduledEmailPage.setBody(fakerMails.getFakeEmailBody(5));
		Scheduled_emailPage scheduled_emailPage = new Scheduled_emailPage();
		scheduled_emailPage.setScheduled_email(scheduledEmailPage);
		Response response = RestClient.doPost("JSON", nymaURL, "scheduled-emails", ThreadManager.getOwnerAlbatrossToken(), null, true, scheduled_emailPage);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message",Matchers.containsString("1 Email(s) Scheduled Successfully, 0 Email(s) Skipped."));
		response.then().body("action_name", Matchers.containsString("Create Scheduled Email"));
	}
}
