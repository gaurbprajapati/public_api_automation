package io.recruitcrm.nyma.scheduledEmail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerMails;
import io.rcrm.api.pojo.nyma.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email1|Email2")
public class AllEndpointsOfScheduledEmailsTest extends TestBase {

	commanFunction function = new commanFunction();
	JavaFakerMails fakerMails = new JavaFakerMails();
	ListFunctions listFunctions = new ListFunctions();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	ArrayList<Object> emailList = new ArrayList<Object>();

	static String id;
	static String scheduledEmailID =null;
	static String candidateEntitySlug ;
	long epoch = System.currentTimeMillis()/1000;	
	long l=epoch;
	int i=(int)l+1800;


	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getReceiversTestData",priority = 0, groups = "nightly-build")
	public void createScheduledEmailsPOST_Test(ArrayList<Object> receiverList,ArrayList<Object> ccList,ArrayList<Object> bccList) {
		ScheduledEmailPage scheduledEmailPage = new ScheduledEmailPage();
		scheduledEmailPage.setVersion(1);
		scheduledEmailPage.setReceivers(receiverList);
		scheduledEmailPage.setCC(ccList);
		scheduledEmailPage.setBCC(bccList);
		scheduledEmailPage.setScheduled_on(i);
		scheduledEmailPage.setTimezone_id(1);
		scheduledEmailPage.setInclude_opt_out_link(0);
		scheduledEmailPage.setSubject(fakerMails.getFakeEmailSubject());
		scheduledEmailPage.setBody(fakerMails.getFakeEmailBody(5));
		Scheduled_emailPage scheduled_emailPage = new Scheduled_emailPage();
		scheduled_emailPage.setScheduled_email(scheduledEmailPage);
		scheduled_emailPage.setLinked_email_type(2);
		Response response = RestClient.doPost("JSON", nymaURL, "scheduled-emails", ThreadManager.getOwnerAlbatrossToken(), null, true, scheduled_emailPage);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message",Matchers.containsString("1 Email(s) Scheduled Successfully, 0 Email(s) Skipped."));
		response.then().body("action_name", Matchers.containsString("Create Scheduled Email"));

	}
	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getReceiversTestData",priority = 1, groups = "nightly-build")
	public void createScheduledEmailsPOSTWithDraftId_Test(ArrayList<Object> receiverList,ArrayList<Object> ccList,ArrayList<Object> bccList) {
		EmailsPage emailsPage = new EmailsPage();
		emailsPage.setRecivers(receiverList);
		emailsPage.setCC(emailList);
		emailsPage.setBCC(ccList);
		emailsPage.setSubject(fakerMails.getFakeEmailSubject());
		emailsPage.setBody(fakerMails.getFakeEmailBody(5));
		emailsPage.setVersion(0);
		SendEmailsPage sendEmailsPage = new SendEmailsPage();
		sendEmailsPage.setEmail(emailsPage);
		sendEmailsPage.setis_send(false);
		Response response = RestClient.doPost("JSON", nymaURL, "emails", ThreadManager.getOwnerAlbatrossToken(), null, true, sendEmailsPage);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		 id = jp.get("data.id");
				
		ScheduledEmailPage scheduledEmailPage = new ScheduledEmailPage();
		scheduledEmailPage.setVersion(1);
		scheduledEmailPage.setDraft_id(id);
		scheduledEmailPage.setInclude_opt_out_link(1);
		scheduledEmailPage.setReceivers(receiverList);
		scheduledEmailPage.setCC(ccList);
		scheduledEmailPage.setBCC(bccList);
		scheduledEmailPage.setScheduled_on(i);
		scheduledEmailPage.setTimezone_id(1); 
		scheduledEmailPage.setInclude_opt_out_link(0);
		scheduledEmailPage.setSubject(fakerMails.getFakeEmailSubject());
		scheduledEmailPage.setBody(fakerMails.getFakeEmailBody(5));
		Scheduled_emailPage scheduled_emailPage = new Scheduled_emailPage();
		scheduled_emailPage.setScheduled_email(scheduledEmailPage);
		Response response1= RestClient.doPost("JSON", nymaURL, "scheduled-emails", ThreadManager.getOwnerAlbatrossToken(), null, true, scheduled_emailPage);
		response1.then().statusCode(200);
		response1.then().body("status", Matchers.containsString("success"));
		response1.then().body("message_type", Matchers.containsString("is-success"));
		response1.then().body("message",Matchers.containsString("1 Email(s) Scheduled Successfully, 0 Email(s) Skipped."));
		response1.then().body("action_name", Matchers.containsString("Create Scheduled Email"));


	}
	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getReceiversInvalidData",priority = 2, groups = "nightly-build")
	public void createScheduledEmailsPOSTWithInvalidEmail_Test(ArrayList<Object> receiverList,int statusCode,String statusMessage) {
		ScheduledEmailPage scheduledEmailPage = new ScheduledEmailPage();
		scheduledEmailPage.setVersion(1);
		scheduledEmailPage.setReceivers(receiverList);
        scheduledEmailPage.setCC(emailList);
        scheduledEmailPage.setBCC(emailList);
		scheduledEmailPage.setScheduled_on(i);
		scheduledEmailPage.setTimezone_id(1);
		scheduledEmailPage.setSubject(fakerMails.getFakeEmailSubject());
		scheduledEmailPage.setBody(fakerMails.getFakeEmailBody(5));
		Scheduled_emailPage scheduled_emailPage = new Scheduled_emailPage();
		scheduled_emailPage.setScheduled_email(scheduledEmailPage);
		Response response1 = RestClient.doPost("JSON", nymaURL, "scheduled-emails", ThreadManager.getOwnerAlbatrossToken(), null, true, scheduled_emailPage);
		response1.then().statusCode(statusCode);
		response1.then().body("status", Matchers.containsString("fail"));
		response1.then().body("message_type", Matchers.containsString("is-danger"));
		response1.then().body("message",Matchers.containsString(statusMessage));


	}
	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getReceiversTestData",priority = 3, groups = "nightly-build")
	public void createScheduledEmailsPOSTWithDeleteEntity_Test(ArrayList<Object> receiverList,ArrayList<Object> ccList,ArrayList<Object> bccList) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateEntitySlug);

		String basePath = "candidates/{candidate}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);
		response.then().statusCode(200);
		ScheduledEmailPage scheduledEmailPage = new ScheduledEmailPage();
		scheduledEmailPage.setVersion(1);
		scheduledEmailPage.setReceivers(receiverList);
        scheduledEmailPage.setCC(emailList);
        scheduledEmailPage.setBCC(emailList);
		scheduledEmailPage.setScheduled_on(i);
		scheduledEmailPage.setTimezone_id(1);
		scheduledEmailPage.setSubject(fakerMails.getFakeEmailSubject());
		scheduledEmailPage.setBody(fakerMails.getFakeEmailBody(5));
		Scheduled_emailPage scheduled_emailPage = new Scheduled_emailPage();
		scheduled_emailPage.setScheduled_email(scheduledEmailPage);
		Response response1 = RestClient.doPost("JSON", nymaURL, "scheduled-emails", ThreadManager.getOwnerAlbatrossToken(), null, true, scheduled_emailPage);
		response1.then().statusCode(422);
		response1.then().body("status", Matchers.containsString("fail"));
		response1.then().body("message_type", Matchers.containsString("is-danger"));
		response1.then().body("message",Matchers.containsString("Failed To Create Scheduled Email : Candidate Recipients not available (rcrmtest0@gmail.com)"));

	}
	@Owner("Priyanka Shinde")
	@Test(priority = 4, groups = "nightly-build")
	public void getScheduledEmailsList_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "scheduled_on");
		queryParameters.put("sort_order", "asc");
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");
		queryParameters.put("linked_email_type", "2");
		String basePath = "scheduled-emails/";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int emailID = jp.get("data.records.id[0]");
		 scheduledEmailID = Integer.toString(emailID);

	}
	@Owner("Priyanka Shinde")
	@Test(priority = 5, groups = "nightly-build")
	public void getScheduledEmailsListWithScheduledID_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", scheduledEmailID);

		String basePath = " scheduled-emails/email-body/{ID}";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);



		response.then().statusCode(200);
	}
	@Owner("Priyanka Shinde")
	@Test(priority = 6, groups = "nightly-build")
	public void getScheduledEmailsListWithAttachment_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("ID", scheduledEmailID);

		String basePath = " scheduled-emails/email-attachments/{ID}";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);



		response.then().statusCode(200);
	}
	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getReceiversTestData",priority = 7, groups = "nightly-build")
	public void cancelScheduledEmailsPOST_Test(ArrayList<Object> receiverList,ArrayList<Object> ccList,ArrayList<Object> bccList) {
		ScheduledEmailPage scheduledEmailPage = new ScheduledEmailPage();
		scheduledEmailPage.setVersion(1);
		scheduledEmailPage.setReceivers(receiverList);
		scheduledEmailPage.setCC(ccList);
		scheduledEmailPage.setBCC(bccList);
		scheduledEmailPage.setScheduled_on(i);
		scheduledEmailPage.setTimezone_id(1);
		scheduledEmailPage.setInclude_opt_out_link(0);
		scheduledEmailPage.setSubject(fakerMails.getFakeEmailSubject());
		scheduledEmailPage.setBody(fakerMails.getFakeEmailBody(5));
		Scheduled_emailPage scheduled_emailPage = new Scheduled_emailPage();
		scheduled_emailPage.setScheduled_email(scheduledEmailPage);
		Response response = RestClient.doPost("JSON", nymaURL, "scheduled-emails/cancel", ThreadManager.getOwnerAlbatrossToken(), null, true, scheduled_emailPage);
		response.then().statusCode(422);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message",Matchers.containsString("Failed To Cancel Scheduled Email : The id field is required."));

	}
	@DataProvider
	public Object[][] getReceiversTestData() {

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		 candidateEntitySlug = jsonCandidate.get("slug");
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

		Object data[][] = { { receiverList,ccList,bccList } };
		return data;
	}
	@DataProvider
	public Object[][] getReceiversInvalidData() {
      
		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");
		String candidateName = jsonCandidate.get("first_name") + " " + jsonCandidate.get("last_name");
		
		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getCandResponse =  albatrossFunctions.getCandidateResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), candidateEntitySlug);
		
		// Verify Response using Assertion and Jsonpath
		JsonPath jpCand = getCandResponse.jsonPath();

		int candID = jpCand.get("data.candidate.id");
		
		ArrayList<Object> receiverList = new ArrayList<Object>();
		ArrayList<Object> ccList = new ArrayList<Object>();
		ReceiverEmailsPage candEmailsPage = new ReceiverEmailsPage();
		candEmailsPage.setEmail("#@%^%#$@#$@#.com");
		candEmailsPage.setName(candidateName);
		candEmailsPage.setEntity_slug(candidateEntitySlug);
		candEmailsPage.setEntity_type(5);
		candEmailsPage.setEntity_id(candID);
		receiverList.add(candEmailsPage);
		ReceiverEmailsPage ccEmailsPage = new ReceiverEmailsPage();
	    ccEmailsPage.setEmail("rcrmtest3@gmail.com");
		ccList.add(ccEmailsPage);
		Object data[][] = { { emailList,422,"Failed To Create Scheduled Email : Receiver field required"},{receiverList,422,"Failed To Create Scheduled Email : Recipient email address (#@%^%#$@#$@#.com) is invalid."}};
		return data;
	}

}

