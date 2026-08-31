package io.rcrm.api.nyma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerMails;
import io.rcrm.api.pojo.nyma.EmailsPage;
import io.rcrm.api.pojo.nyma.ReceiverEmailsPage;
import io.rcrm.api.pojo.nyma.SendEmailsPage;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email1|Email2")
public class getEmailThread_Test extends TestBase {
	String threadId;
	commanFunction function = new commanFunction();
	JavaFakerMails fakerMails = new JavaFakerMails();
	String emailSubject;

	@Owner("Harika")
	@Test(dataProvider = "testData",priority = 1, groups = "nightly-build")
	public void getThread_Test(ArrayList<Object> receiverList, ArrayList<Object> ccList,
							   ArrayList<Object> bccList) {
		emailSubject = fakerMails.getFakeEmailSubject();

		EmailsPage emailsPage = new EmailsPage();
		emailsPage.setRecivers(receiverList);
		emailsPage.setCC(ccList);
		emailsPage.setBCC(bccList);
		emailsPage.setSubject(emailSubject);
		emailsPage.setBody(fakerMails.getFakeEmailBody(5));
		emailsPage.setVersion(0);
		SendEmailsPage sendEmailsPage = new SendEmailsPage();
		sendEmailsPage.setEmail(emailsPage);
		sendEmailsPage.setis_send(true);
		Response response1 = RestClient.doPost("JSON", nymaURLv3, "emails", ThreadManager.getOwnerAlbatrossToken(), null, true, sendEmailsPage);

		response1.then().statusCode(200);
		response1.then().body("status", Matchers.containsString("success"));
		response1.then().body("message_type", Matchers.containsString("is-success"));

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");

		queryParameters.put("linked_email_type", "2");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		JsonPath jp = response.jsonPath();
		threadId = jp.get("data.records[0].latest_draft_or_message[0].thread_id");
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void getSearchResultInvalid(){
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("search", emailSubject);	//query parameter page and page_size missing
		queryParameters.put("linked_email_type", "2");
		queryParameters.put("subject", "true");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(422);
		response.then().body("message", Matchers.containsString("The page field is required.,The page size field is required."));
		response.then().body("message_type", Matchers.containsString("is-danger"));
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void getSearchResultInvalidLinkedEmailType(){
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");
		queryParameters.put("search", emailSubject);
		queryParameters.put("linked_email_type", "3");			//invalid linked_email_type
		queryParameters.put("subject", "true");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(422);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void getSearchResult(){
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");
		queryParameters.put("search", emailSubject);
		queryParameters.put("linked_email_type", "1");
		queryParameters.put("subject", "true");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
	}

	@Owner("Harika")
	@Test(priority = 3, groups = "nightly-build")
	public void getThreadInvalidAuth_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads", ThreadManager.getOwnerAlbatrossToken()+"1234", queryParameters, null,
				true);
		response.then().statusCode(401);

	}

	@Owner("Harika")
	@Test(priority = 4, groups = "nightly-build")
	public void getThreadInvalidParams_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("linked_email_type", "3");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		JsonPath responseJson = response.jsonPath();
		response.then().statusCode(422);
		Assert.assertTrue(responseJson.getString("message").contains("The page size field is required."));

	}

	@Owner("Harika")
	@Test(priority = 2, groups = "nightly-build")
	public void getThreadById_Test() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("thread_id", threadId);
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("linked_email_type", "2");
		String basePath = "threads/{thread_id}";
		Response response = RestClient.doGet("JSON", nymaURLv3, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, pathParameters, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}

	@Owner("Ajendra Singh")
	@Test(priority = 2, groups = "nightly-build")
	public void getThreadByIdInvalid_Test() {
		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("thread_id", threadId);
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("linked_email_type", "3");
		String basePath = "threads/{thread_id}";
		Response response = RestClient.doGet("JSON", nymaURLv3, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, pathParameters, true);
		JsonPath responseJson = response.jsonPath();
		response.then().statusCode(422);
		Assert.assertTrue(responseJson.getString("message").contains("The selected linked email type is invalid."));

	}

	@Owner("Harika")
	@Test(priority = 5, groups = "nightly-build")
	public void getThreadByIdInvalidAuth_Test() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("thread_id", threadId);
		String basePath = "threads/{thread_id}";
		Response response = RestClient.doGet("JSON", nymaURLv3, basePath, ThreadManager.getOwnerAlbatrossToken()+"1234", null, pathParameters, true);
		response.then().statusCode(401);

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getThreadFailed_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");
		queryParameters.put("linked_email_type", "2");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads/failed-emails", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getThreadOpened_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");
		queryParameters.put("linked_email_type", "2");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads/opened", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void getThreadReplied_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");
		queryParameters.put("linked_email_type", "2");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads/replied", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getThreadFailedInvalid_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("linked_email_type", "3");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads/failed-emails", ThreadManager.getOwnerAlbatrossToken(), queryParameters,
				null, true);
		JsonPath responseJson = response.jsonPath();
		response.then().statusCode(422);
		Assert.assertTrue(responseJson.getString("message").contains("The selected linked email type is invalid."));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getThreadOpenedInvalid_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("linked_email_type", "3");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads/opened", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		JsonPath responseJson = response.jsonPath();
		response.then().statusCode(422);
		Assert.assertTrue(responseJson.getString("message").contains("The selected linked email type is invalid."));

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void getThreadRepliedInvalid_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("linked_email_type", "3");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads/replied", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		JsonPath responseJson = response.jsonPath();
		response.then().statusCode(422);
		Assert.assertTrue(responseJson.getString("message").contains("The selected linked email type is invalid."));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getThreadFailedInvalidAuth_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads/failed-emails", ThreadManager.getOwnerAlbatrossToken()+"1234", queryParameters, null, true);
		response.then().statusCode(401);

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getThreadOpenedInvalidAuth_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads/opened", ThreadManager.getOwnerAlbatrossToken()+"1234", queryParameters, null, true);
		response.then().statusCode(401);

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void getThreadRepliedInvalidAuth_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("page_size", "1");

		Response response = RestClient.doGet("JSON", nymaURLv3, "threads/replied", ThreadManager.getOwnerAlbatrossToken()+"1234", queryParameters, null, true);
		response.then().statusCode(401);

	}

	@DataProvider
	public Object[][] testData() {
		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");
		String candidateName = jsonCandidate.get("first_name") + " " + jsonCandidate.get("last_name");
		String candidateEmail = jsonCandidate.get("email");

		ArrayList<Object> receiverList = new ArrayList<Object>();
		ArrayList<Object> ccList = new ArrayList<Object>();
		ArrayList<Object> bccList = new ArrayList<Object>();
		ReceiverEmailsPage candEmailsPage = new ReceiverEmailsPage();

		candEmailsPage.setEmail(candidateEmail);
		candEmailsPage.setName(candidateName);
		candEmailsPage.setEntity_slug(candidateEntitySlug);
		candEmailsPage.setEntity_type(5);
		receiverList.add(candEmailsPage);

		ReceiverEmailsPage ccEmailsPage = new ReceiverEmailsPage();
		ccEmailsPage.setEmail("rcrmtest3@gmail.com");
		ccList.add(ccEmailsPage);

		ReceiverEmailsPage bccEmailsPage = new ReceiverEmailsPage();
		bccEmailsPage.setEmail("rcrmtest82@gmail.com");
		bccList.add(bccEmailsPage);

		Object data[][] = { { receiverList, ccList, bccList } };
		return data;
	}
}