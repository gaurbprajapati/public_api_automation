package io.recruitcrm.nyma.scheduledEmail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerMails;
import org.hamcrest.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.nyma.EmailsPage;
import io.rcrm.api.pojo.nyma.ReceiverEmailsPage;
import io.rcrm.api.pojo.nyma.ScheduledEmailPage;
import io.rcrm.api.pojo.nyma.Scheduled_emailPage;
import io.rcrm.api.pojo.nyma.SendEmailsPage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email1|Email2")
public class UpdateScheduleEmailsTest extends TestBase {
 
	    commanFunction function = new commanFunction();
		JavaFakerMails fakerMails = new JavaFakerMails();

		static String id;
		int emailID;
		long epoch = System.currentTimeMillis()/1000;	
		long l=epoch;int i=(int)l+1800;

	@Owner("Harika")
	@Test(dataProvider = "getReceiversTestData",priority = 0, groups = "nightly-build")
	public void reScheduledEmailsPOST_Test(ArrayList<Object> receiverList,ArrayList<Object> ccList,ArrayList<Object> bccList, int linked_email_type) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "scheduled_on");
		queryParameters.put("sort_order", "desc");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "50");
		queryParameters.put("linked_email_type", Integer.toString(linked_email_type));
		String basePath = "scheduled-emails/";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

	    emailID = jp.get("data.records.id[0]");
		
		ScheduledEmailPage scheduledEmailPage = new ScheduledEmailPage();
		scheduledEmailPage.setScheduled_id(emailID);
		scheduledEmailPage.setReceivers(receiverList);
		scheduledEmailPage.setCC(ccList);
		scheduledEmailPage.setBCC(bccList);
		scheduledEmailPage.setScheduled_on(i);
		scheduledEmailPage.setTimezone_id(2);
		scheduledEmailPage.setInclude_opt_out_link(0);
		scheduledEmailPage.setSubject(fakerMails.getFakeEmailSubject());
		scheduledEmailPage.setBody(fakerMails.getFakeEmailBody(5));
		Scheduled_emailPage scheduled_emailPage = new Scheduled_emailPage();
		scheduled_emailPage.setScheduled_email(scheduledEmailPage);
		scheduled_emailPage.setLinked_email_type(linked_email_type);
		Response response1 = RestClient.doPost("JSON", nymaURL, "scheduled-emails/reschedule", ThreadManager.getOwnerAlbatrossToken(), null, true, scheduled_emailPage);
		response1.then().statusCode(200);
		response1.then().body("status", Matchers.containsString("success"));
		response1.then().body("message_type", Matchers.containsString("is-success"));
		response1.then().body("message",Matchers.containsString("Reschedule Email Successful "));
		response1.then().body("action_name", Matchers.containsString("Reschedule Email"));

	}
	@Owner("Ajendra Singh")
	@Test(dataProvider = "getReceiversTestData",priority = 1, groups = "nightly-build")
	public void sendScheduledEmailsPOST_Test(ArrayList<Object> receiverList,ArrayList<Object> ccList,ArrayList<Object> bccList, int linked_email_type) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "scheduled_on");
		queryParameters.put("sort_order", "desc");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "50");
		queryParameters.put("linked_email_type", Integer.toString(linked_email_type));
		String basePath = "scheduled-emails/";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

	    emailID = jp.get("data.records.id[0]");
		
		EmailsPage emailsPage = new EmailsPage();
		emailsPage.setId(id);
		emailsPage.setScheduled_email_id(emailID);
		emailsPage.setRecivers(receiverList);
		emailsPage.setCC(ccList);
		emailsPage.setBCC(bccList);
		emailsPage.setSubject(fakerMails.getFakeEmailSubject());
		emailsPage.setBody(fakerMails.getFakeEmailBody(5));
		emailsPage.setVersion(0);
		SendEmailsPage sendEmailsPage = new SendEmailsPage();
		sendEmailsPage.setEmail(emailsPage);
		sendEmailsPage.setis_send(true);
		sendEmailsPage.setLinked_email_type(linked_email_type);
		Response response1 = RestClient.doPost("JSON", nymaURLv3, "emails", ThreadManager.getOwnerAlbatrossToken(), null, true, sendEmailsPage);

		response1.then().statusCode(200);
		response1.then().body("status", Matchers.containsString("success"));
		response1.then().body("message_type", Matchers.containsString("is-success"));
		response1.then().body("message",Matchers.containsString("1 Email(s) Sent Successfully, 0 Email(s) Skipped And 0 Email(s) Failed."));
		response1.then().body("action_name", Matchers.containsString("Email Sent"));

	}
	@Owner("Harika")
	@Test(dataProvider = "getReceiversTestData",priority = 2, groups = "nightly-build")
	public void unAuthorizedUserCannotReScheduledEmails_Test(ArrayList<Object> receiverList,ArrayList<Object> ccList,ArrayList<Object> bccList, int linked_email_type) {
		ScheduledEmailPage scheduledEmailPage = new ScheduledEmailPage();
		scheduledEmailPage.setScheduled_id(emailID);
		scheduledEmailPage.setReceivers(receiverList);
		scheduledEmailPage.setCC(ccList);
		scheduledEmailPage.setBCC(bccList);
		scheduledEmailPage.setScheduled_on(i);
		scheduledEmailPage.setTimezone_id(2);
		scheduledEmailPage.setInclude_opt_out_link(0);
		scheduledEmailPage.setSubject(fakerMails.getFakeEmailSubject());
		scheduledEmailPage.setBody(fakerMails.getFakeEmailBody(5));
		Scheduled_emailPage scheduled_emailPage = new Scheduled_emailPage();
		scheduled_emailPage.setScheduled_email(scheduledEmailPage);
		Response response1 = RestClient.doPost("JSON", nymaURL, "scheduled-emails/reschedule", ThreadManager.getOwnerAlbatrossToken()+"x001", null, true, scheduled_emailPage);
		response1.then().statusCode(401);

	}
	@DataProvider
	public Object[][] getReceiversTestData() {
		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");
		String candidateName = jsonCandidate.get("first_name") + " " + jsonCandidate.get("last_name");
		String candidateEmail = jsonCandidate.get("email");
		
		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getCandResponse =  albatrossFunctions.getCandidateResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), candidateEntitySlug);
		// Verify Response using Assertion and Jsonpath
		JsonPath jpCand = getCandResponse.jsonPath();

		int candID = jpCand.get("data.candidate.id");
		ArrayList<Object> receiverList = new ArrayList<Object>();
		ArrayList<Object> ccList = new ArrayList<Object>();
		ArrayList<Object> bccList = new ArrayList<Object>();
		ReceiverEmailsPage candEmailsPage = new ReceiverEmailsPage();

		candEmailsPage.setEmail(candidateEmail);
		candEmailsPage.setName(candidateName);
		candEmailsPage.setEntity_slug(candidateEntitySlug);
		candEmailsPage.setEntity_type(5);
		candEmailsPage.setEntity_id(candID);
		receiverList.add(candEmailsPage);
		
		ReceiverEmailsPage ccEmailsPage = new ReceiverEmailsPage();
	    ccEmailsPage.setEmail("rcrmtest3@gmail.com");
		ccList.add(ccEmailsPage);
		
		ReceiverEmailsPage bccEmailsPage = new ReceiverEmailsPage();
		bccEmailsPage.setEmail("rcrmtest82@gmail.com");
		bccList.add(bccEmailsPage);

		ScheduledEmailPage scheduledEmailPage = new ScheduledEmailPage();
		scheduledEmailPage.setVersion(1);
		scheduledEmailPage.setReceivers(receiverList);
		scheduledEmailPage.setScheduled_on((int)(System.currentTimeMillis()/1000)+12000);
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

		scheduled_emailPage.setLinked_email_type(2);
		Response response1 = RestClient.doPost("JSON", nymaURL, "scheduled-emails", ThreadManager.getOwnerAlbatrossToken(), null, true, scheduled_emailPage);

		response1.then().statusCode(200);
		response1.then().body("status", Matchers.containsString("success"));
		response1.then().body("message_type", Matchers.containsString("is-success"));
		response1.then().body("message", Matchers.containsString("1 Email(s) Scheduled Successfully, 0 Email(s) Skipped."));
		response1.then().body("action_name", Matchers.containsString("Create Scheduled Email"));

		Object data[][] = { { receiverList,ccList,bccList,1 }, { receiverList,ccList,bccList,2 } };
		return data;
	}
}
