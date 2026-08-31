package io.rcrm.api.nyma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerMails;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.nyma.DeleteDraftPage;
import io.rcrm.api.pojo.nyma.EmailsPage;
import io.rcrm.api.pojo.nyma.ReceiverEmailsPage;
import io.rcrm.api.pojo.nyma.SendEmailsPage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email1|Email2")
public class getEmails_Test extends TestBase {

    JavaFakerMails fakerMails = new JavaFakerMails();
    ArrayList<Object> emailList = new ArrayList<Object>();

    static String id;
    static String draftId;
    static String grantId;


    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void getEmailTokenStatus() {
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("ID", "email");
        String basePath = "token-status/{ID}";

        Response response = RestClient.doGet("JSON", nymaURLv3, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.Email_1.state", Matchers.is("valid"));
        response.then().body("data.Email_2.state", Matchers.is("valid"));
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "getReceiversTestData", groups = "nightly-build")
    public void sendBulkEmailsWithCCBCC_Test(ArrayList<Object> receiverList, ArrayList<Object> ccList, ArrayList<Object> bccList) {
        EmailsPage emailsPage = new EmailsPage();
        emailsPage.setRecivers(receiverList);
        emailsPage.setCC(ccList);
        emailsPage.setBCC(bccList);
        emailsPage.setSubject(fakerMails.getFakeEmailSubject());
        emailsPage.setBody(fakerMails.getFakeEmailBody(5));
        emailsPage.setVersion(0);
        SendEmailsPage sendEmailsPage = new SendEmailsPage();
        sendEmailsPage.setEmail(emailsPage);
        sendEmailsPage.setis_send(true);
        sendEmailsPage.setLinked_email_type(2);
        Response response = RestClient.doPost("JSON", nymaURLv3, "emails", ThreadManager.getOwnerAlbatrossToken(), null, true, sendEmailsPage);

        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.containsString("2 Email(s) Sent Successfully, 0 Email(s) Skipped And 0 Email(s) Failed."));
        response.then().body("action_name", Matchers.containsString("Email Sent"));
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "getReceiversTestData", groups = "nightly-build")
    public void sendBulkEmailsWithoutCCBCC_Test(ArrayList<Object> receiverList, ArrayList<Object> ccList, ArrayList<Object> bccList) {
        EmailsPage emailsPage = new EmailsPage();
        emailsPage.setRecivers(receiverList);
        emailsPage.setCC(emailList);
        emailsPage.setBCC(emailList);
        emailsPage.setSubject(fakerMails.getFakeEmailSubject());
        emailsPage.setBody(fakerMails.getFakeEmailBody(5));
        emailsPage.setVersion(0);
        SendEmailsPage sendEmailsPage = new SendEmailsPage();
        sendEmailsPage.setEmail(emailsPage);
        sendEmailsPage.setis_send(true);
        sendEmailsPage.setLinked_email_type(2);
        Response response = RestClient.doPost("JSON", nymaURLv3, "emails", ThreadManager.getOwnerAlbatrossToken(), null, true, sendEmailsPage);

        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.containsString("2 Email(s) Sent Successfully, 0 Email(s) Skipped And 0 Email(s) Failed."));
        response.then().body("action_name", Matchers.containsString("Email Sent"));
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "getReceiversTestData", groups = "nightly-build")
    public void saveDrafts_Test(ArrayList<Object> receiverList, ArrayList<Object> ccList, ArrayList<Object> bccList) {
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
        sendEmailsPage.setLinked_email_type(2);
        Response response = RestClient.doPost("JSON", nymaURLv3, "emails", ThreadManager.getOwnerAlbatrossToken(), null, true, sendEmailsPage);
        JsonPath jp = response.jsonPath();

        id = jp.get("data.id");
        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.containsString("Draft Save Successful"));
        response.then().body("action_name", Matchers.containsString("Draft Save"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void getDraftById() {
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("draft_id", id);
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("linked_email_type", "2");
        String basePath = "threads/draft/{draft_id}";

        Response response = RestClient.doGet("JSON", nymaURLv3, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, pathParamters, true);
        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "getReceiversTestData", groups = "nightly-build")
    public void updateDrafts_Test(ArrayList<Object> receiverList, ArrayList<Object> ccList, ArrayList<Object> bccList) {
        EmailsPage emailsPage = new EmailsPage();
        emailsPage.setId(id);
        emailsPage.setRecivers(receiverList);
        emailsPage.setCC(emailList);
        emailsPage.setBCC(emailList);
        emailsPage.setSubject(fakerMails.getFakeEmailSubject());
        emailsPage.setBody(fakerMails.getFakeEmailBody(5));
        emailsPage.setVersion(0);
        SendEmailsPage sendEmailsPage = new SendEmailsPage();
        sendEmailsPage.setEmail(emailsPage);
        sendEmailsPage.setis_send(false);
        sendEmailsPage.setLinked_email_type(2);
        Response response = RestClient.doPost("JSON", nymaURLv3, "emails", ThreadManager.getOwnerAlbatrossToken(), null, true, sendEmailsPage);

        JsonPath jp = response.jsonPath();
        draftId = jp.get("data.id");
        grantId = jp.get("data.grant_id");
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.containsString("Draft Save Successful"));
        response.then().body("action_name", Matchers.containsString("Draft Save"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void deleteDrafts_Test() {
        DeleteDraftPage deleteDraftPage = new DeleteDraftPage();
        deleteDraftPage.setId(id);
        deleteDraftPage.setVersion("0");
        deleteDraftPage.setGrant_id(grantId);
        JSONObject json = new JSONObject(deleteDraftPage);

        Map<String, String> queryParamters = new HashMap<String, String>();
        queryParamters.put("drafts[]", json.toString());
        queryParamters.put("linked_email_type", "2");
        Response response = RestClient.doDelete("JSON", nymaURLv3, "drafts", ThreadManager.getOwnerAlbatrossToken(), queryParamters, null, true);

        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.containsString("Delete Email Successful "));
        response.then().body("action_name", Matchers.containsString("Delete Email"));
    }
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void deleteDraftInvalidParams_Test() {

		Map<String, String> queryParamters = new HashMap<String, String>();
		queryParamters.put("drafts[]","" );
		queryParamters.put("linked_email_type", "3");
		Response response = RestClient.doDelete("JSON", nymaURLv3, "drafts", ThreadManager.getOwnerAlbatrossToken(), queryParamters,null, true);
		JsonPath jp = response.jsonPath();

		response.then().statusCode(422);
		Assert.assertTrue(jp.getString("message").contains("The selected linked email type is invalid."));
	}

	@Owner("Harika")
	@Test
	public void disconnectEmail() {

		Response response = RestClient.doDelete("JSON", nymaURLv3, "email", ThreadManager.getOwnerAlbatrossToken(), null, null, true);

		// Verify Response Code and body
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Email Settings Deleted"));

	}

	@DataProvider
	public Object[][] getReceiversTestData() {
		commanFunction function = new commanFunction();
		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");
		String candidateName = jsonCandidate.get("first_name") + " " + jsonCandidate.get("last_name");
		String candidateEmail = jsonCandidate.get("email");

		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String companySlug = jsonCompany.get("slug");

		JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");
		String contactName = jsonContact.get("first_name") + " " + jsonCandidate.get("last_name");
		String contactEmail = jsonContact.get("email");

		ArrayList<Object> receiverList = new ArrayList<Object>();
		ArrayList<Object> ccList = new ArrayList<Object>();
		ArrayList<Object> bccList = new ArrayList<Object>();
		ReceiverEmailsPage candEmailsPage = new ReceiverEmailsPage();
		ReceiverEmailsPage contEmailsPage = new ReceiverEmailsPage();
		contEmailsPage.setEmail(contactEmail);
		contEmailsPage.setName(contactName);
		contEmailsPage.setEntity_slug(contactSlug);
		contEmailsPage.setEntity_type(2);
		receiverList.add(contEmailsPage);

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