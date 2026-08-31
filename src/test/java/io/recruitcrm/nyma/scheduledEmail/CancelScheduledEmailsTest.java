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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class CancelScheduledEmailsTest extends TestBase {
	commanFunction function = new commanFunction();
	JavaFakerMails fakerMails = new JavaFakerMails();
	long epoch = System.currentTimeMillis() / 1000;
	long l = epoch;
	int i = (int) l + 12000;
	String generatedString = RandomStringUtils.randomAlphabetic(4);


	@Owner("Harika")
	@Test(dataProvider = "getScheduledEmail", groups = "nightly-build")
	public void cancelScheduledEmailsPOST_Test(int emailID) {
		Map<String, String> queryParameters1 = new HashMap<String, String>();
		queryParameters1.put("id", Integer.toString(emailID));
		queryParameters1.put("link_email_type", "1");
		Response response = RestClient.doPost1("JSON", nymaURL, "scheduled-emails/cancel", ThreadManager.getOwnerAlbatrossToken(), queryParameters1, null,
				true, null);
		response.then().statusCode(200);

		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Cancel Scheduled Email Successful"));
		response.then().body("action_name", Matchers.containsString("Cancel Scheduled Email"));

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void unAuthorizedUserCannotCancelScheduledEmails_Test() {
		Map<String, String> queryParameters1 = new HashMap<String, String>();
		queryParameters1.put("id", "1234");
		queryParameters1.put("link_email_type", "1");
		Response response1 = RestClient.doPost1("JSON", nymaURL, "scheduled-emails/cancel", ThreadManager.getOwnerAlbatrossToken()+"x003", queryParameters1, null,
				true, null);
		response1.then().statusCode(401);

	}

	@DataProvider
	public Object[][] getScheduledEmail() {
		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");
		String candidateName = jsonCandidate.get("first_name") + " " + jsonCandidate.get("last_name");
		String candidateEmail = jsonCandidate.get("email");

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getCandResponse = albatrossFunctions.getCandidateResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), candidateEntitySlug);
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
		response.then().body("message", Matchers.containsString("1 Email(s) Scheduled Successfully, 0 Email(s) Skipped."));
		response.then().body("action_name", Matchers.containsString("Create Scheduled Email"));

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "scheduled_on");
		queryParameters.put("sort_order", "desc");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "50");
		String basePath = "scheduled-emails/";
		Response response1 = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response1.then().statusCode(200);
		JsonPath jp = response1.jsonPath();

		int emailID = jp.get("data.records.findAll { it.scheduled_on == "+i+" }[0].id");

		return new Object[][]{{emailID}};
	}
}
