package io.rcrm.api.emails;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerMails;
import io.rcrm.api.pojo.emails.SendEmail;
import io.rcrm.api.pojo.emails.ReceiverEmailsPage;
import io.rcrm.api.pojo.reaper.UpdateEntityRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email1")
public class EmailSettingsTest extends TestBase {
    commanFunction function = new commanFunction();
    JavaFakerMails fakerMails = new JavaFakerMails();
    ReceiverEmailsPage to = new ReceiverEmailsPage();
    List<ReceiverEmailsPage> ccList = new ArrayList<>();
    List<ReceiverEmailsPage> bccList = new ArrayList<>();
    int email_status_id = 0;
    String candidateEntitySlug, candidateEntitySlug2, contactSlug;

    @Owner("Ajendra Singh")
    @Test(priority = 0, dataProvider = "getValidReceiversTestData", groups = "nightly-build")
    public void sendEmail(ReceiverEmailsPage to, ArrayList ccList, ArrayList bccList) {
        String basePath = "emails";
        SendEmail sendEmail = new SendEmail();
        sendEmail.setSubject(fakerMails.getFakeEmailSubject());
        sendEmail.setBody(fakerMails.getFakeEmailBody(5));
        sendEmail.setTo(to);
        sendEmail.setCc(ccList);
        sendEmail.setBcc(bccList);
        sendEmail.setInclude_signature(true);
        sendEmail.setInclude_opt_out_link(true);

        Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, false, sendEmail);
        response.then().statusCode(200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//email//sendEmail.json"));
        response.then().body("status", Matchers.containsString("success"));
        email_status_id = response.jsonPath().getInt("email_status_id");
    }

    @Owner("Ajendra Singh")
    @Test(priority = 1, dependsOnMethods = "sendEmail", groups = "nightly-build")
    public void getSentEmailStatus() {
        String basePath = "emails/status/" + email_status_id;
        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, null, false);
        response.then().statusCode(200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//email//sentEmailStatus.json"));
        response.then().body("status", Matchers.containsString("Processing"));
    }

    @Owner("Ajendra Singh")
    @Test(priority = 2, groups = "nightly-build")
    public void sendEmailInvalidAuth() {
        String basePath = "emails";
        SendEmail sendEmail = new SendEmail();
        sendEmail.setSubject(fakerMails.getFakeEmailSubject());
        sendEmail.setBody(fakerMails.getFakeEmailBody(5));
        sendEmail.setTo(to);
        sendEmail.setCc(ccList);
        sendEmail.setBcc(bccList);
        sendEmail.setInclude_signature(true);
        sendEmail.setInclude_opt_out_link(true);

        Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey() + "1234", null, false, sendEmail);
        response.then().statusCode(401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Ajendra Singh")
    @Test(priority = 3, groups = "nightly-build")
    public void sendEmailInvalidRequestBody() {
        String basePath = "emails";
        SendEmail sendEmail = new SendEmail();
        sendEmail.setBody(fakerMails.getFakeEmailBody(5));
        sendEmail.setCc(ccList);
        sendEmail.setBcc(bccList);
        sendEmail.setInclude_signature(true);
        sendEmail.setInclude_opt_out_link(true);

        Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, false, sendEmail);
        response.then().statusCode(422);
        response.then().body("to[0]", Matchers.containsString("To is required"));
        response.then().body("'to.identifier'[0]", Matchers.containsString("To identifier is required"));
        response.then().body("'to.type'[0]", Matchers.containsString("To type is required"));
        response.then().body("subject[0]", Matchers.containsString("Subject is required"));
    }

    @Owner("Ajendra Singh")
    @Test(priority = 4, dependsOnMethods = "sendEmail", groups = "nightly-build")
    public void getSentEmailStatusInvalidAuth() {
        String basePath = "emails/status/" + email_status_id;
        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey() + "1234", null, null, false);
        response.then().statusCode(401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Ajendra Singh")
    @Test(priority = 0, dataProvider = "getInvalidReceiversTestData", groups = "nightly-build")
    public void sendEmailInvalidReceivers(ReceiverEmailsPage to, ArrayList ccList, ArrayList bccList) {
        String basePath = "emails";
        SendEmail sendEmail = new SendEmail();
        sendEmail.setSubject(fakerMails.getFakeEmailSubject());
        sendEmail.setBody(fakerMails.getFakeEmailBody(5));
        sendEmail.setTo(to);
        sendEmail.setCc(ccList);
        sendEmail.setBcc(bccList);
        sendEmail.setInclude_signature(true);
        sendEmail.setInclude_opt_out_link(true);

        Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, false, sendEmail);
        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        email_status_id = response.jsonPath().getInt("email_status_id");
    }

    @Owner("Ajendra Singh")
    @Test(priority = 1, dependsOnMethods = "sendEmailInvalidReceivers", groups = "nightly-build")
    public void getSentEmailStatusInvalidId() {
        int invalidEmailStatusId = email_status_id + 1000; // Assuming this ID does not exist
        String basePath = "emails/status/" + invalidEmailStatusId;
        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, null, false);
        response.then().statusCode(404);
        response.then().body("message", Matchers.containsString("Email Status ID is invalid"));
    }

    @Owner("Ajendra Singh")
    @Test(priority = 2, dependsOnMethods = "sendEmailInvalidReceivers", groups = "nightly-build")
    public void getSentEmailStatusInvalid() throws InterruptedException {
        Thread.sleep(2000); // Wait for the email status to be processed
        String basePath = "emails/status/" + email_status_id;
        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, null, false);
        response.then().statusCode(200);

        String status = response.jsonPath().getString("status");
        if ("Processing".equals(status)) {
            response.then().body("status", Matchers.equalTo("Processing"));
            response.then().body("message", Matchers.equalTo("Sending Email is currently under process"));
            response.then().body("email_message_id", Matchers.nullValue());
            response.then().body("created_at", Matchers.notNullValue());
        } else {
            response.then().body("status", Matchers.containsString("Failed"));
            response.then().body("message", Matchers.containsString("There was an error while sending email"));
            response.then().body("error_message.to[0]", Matchers.containsString("candidate with slug:" + candidateEntitySlug + " is opted out of email communication"));
            response.then().body("error_message.cc[0]", Matchers.containsString("candidate with slug:" + candidateEntitySlug2 + " is opted out of email communication"));
            response.then().body("error_message.bcc[0]", Matchers.containsString("contact with slug:" + contactSlug + " is missing an email address"));
        }
    }

    @DataProvider
    public Object[][] getValidReceiversTestData() {
        String apiKey = ThreadManager.getAccountApiKey();
        String candidateEntitySlug = createCandidateSlug(apiKey);
        String candidateEntitySlug2 = createCandidateSlug(apiKey);
        String companySlug = createCompanySlug(apiKey);
        String contactSlug = createContactSlug(apiKey, companySlug);

        to.setIdentifier(candidateEntitySlug);
        to.setType("candidate");
        ccList.add(buildReceiver(candidateEntitySlug2, "candidate"));
        bccList.add(buildReceiver(contactSlug, "contact"));

        Object data[][] = {{to, ccList, bccList}};
        return data;
    }

    @DataProvider
    public Object[][] getInvalidReceiversTestData() {
        String apiKey = ThreadManager.getAccountApiKey();
        candidateEntitySlug = createCandidateSlug(apiKey);
        Map<String, String> updateColumnsCandidate = new HashMap<>();
        updateColumnsCandidate.put("email_opt_out", "1");
        ReaperIntegration.updateEntityColumns(candidateEntitySlug, new UpdateEntityRequest("candidate", updateColumnsCandidate));

        candidateEntitySlug2 = createCandidateSlug(apiKey);
        ReaperIntegration.updateEntityColumns(candidateEntitySlug2, new UpdateEntityRequest("candidate", updateColumnsCandidate));

        contactSlug = createContactSlug(apiKey, createCompanySlug(apiKey));
        Map<String, String> updateColumnsContact = new HashMap<>();
        updateColumnsContact.put("email", "");
        ReaperIntegration.updateEntityColumns(contactSlug, new UpdateEntityRequest("contact", updateColumnsContact));

        to.setIdentifier(candidateEntitySlug);
        to.setType("candidate");
        ccList.add(buildReceiver(candidateEntitySlug2, "candidate"));
        bccList.add(buildReceiver(contactSlug, "contact"));

        Object data[][] = {{to, ccList, bccList}};
        return data;
    }

    private String createCandidateSlug(String apiKey) {
        return function.createNewCandidateWithMandatoryFields(baseURL, apiKey).jsonPath().get("slug");
    }

    private String createCompanySlug(String apiKey) {
        return function.createNewCompanyWithMandatoryFields(baseURL, apiKey).jsonPath().get("slug");
    }

    private String createContactSlug(String apiKey, String companySlug) {
        return function.createNewContact_POST(baseURL, apiKey, companySlug).jsonPath().get("slug");
    }

    private ReceiverEmailsPage buildReceiver(String identifier, String type) {
        ReceiverEmailsPage receiver = new ReceiverEmailsPage();
        receiver.setIdentifier(identifier);
        receiver.setType(type);
        return receiver;
    }
}