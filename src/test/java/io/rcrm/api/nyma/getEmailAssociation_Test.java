package io.rcrm.api.nyma;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerMails;
import io.rcrm.api.pojo.nyma.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email1")
public class getEmailAssociation_Test extends TestBase {

    JavaFakerMails fakerMails = new JavaFakerMails();
    static String threadId;
    String candidateSlug;

    @Owner("Ajendra Singh")
    @Test(dataProvider = "testData", priority = 0, groups = "nightly-build")
    public void getEmailTestWithAssociation(ArrayList<Object> receiverList, ArrayList<Object> ccList, ArrayList<Object> bccList, Map<String, List<String>> associations) {
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
        sendEmailsPage.setAssociated_data(associations);
        Response response = RestClient.doPost("JSON", nymaURLv3, "emails", ThreadManager.getOwnerAlbatrossToken(), null, false, sendEmailsPage);
        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        // System.out.println("Response: " + response.prettyPrint());

        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("page", "1");
        queryParameters.put("page_size", "1");
        queryParameters.put("linked_email_type", "1");

        Response response1 = RestClient.doGet("JSON", nymaURLv3, "threads", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
        response1.then().statusCode(200);
        response1.then().body("status", Matchers.containsString("success"));
        response1.then().body("message_type", Matchers.containsString("is-success"));
        JsonPath jp = response1.jsonPath();
        // System.out.println("Response1: " + response1.prettyPrint());
        response1.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//email//threadList.json"));
        threadId = jp.get("data.records[0].latest_draft_or_message.thread_id");
        // System.out.println("Thread ID: " + threadId);
    }

    @Owner("Ajendra Singh")
    @Test(priority = 1, groups = "nightly-build")
    public void addAssociation() {
        commanFunction function = new commanFunction();
        JsonPath candJson = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        candidateSlug = candJson.get("slug");

        UpdateAssociations updateAssociations = new UpdateAssociations();
        updateAssociations.setThread_id(threadId);
        updateAssociations.setAssociated_entity(candidateSlug);
        updateAssociations.setAssociated_entity_type_id("5");
        updateAssociations.setEvent_type("add");

        String basePath = "update-association";

        Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, false, updateAssociations);
        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//email//updateAssociation.json"));
    }

    @Owner("Ajendra Singh")
    @Test(priority = 2, groups = "nightly-build")
    public void updateAssociation() {
        UpdateAssociations updateAssociations = new UpdateAssociations();
        updateAssociations.setThread_id(threadId);
        updateAssociations.setAssociated_entity(candidateSlug);
        updateAssociations.setAssociated_entity_type_id("5");
        updateAssociations.setEvent_type("add");

        String basePath = "update-association";
        Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, false, updateAssociations);
        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("fail"));
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("message", Matchers.containsString("Failed To update association : Association already exists"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//email//updateAssociation.json"));
    }

    @Owner("Ajendra Singh")
    @Test(priority = 3, groups = "nightly-build")
    public void removeAssociation() {
        UpdateAssociations updateAssociations = new UpdateAssociations();
        updateAssociations.setThread_id(threadId);
        updateAssociations.setAssociated_entity(candidateSlug);
        updateAssociations.setAssociated_entity_type_id("5");
        updateAssociations.setEvent_type("remove");

        String basePath = "update-association";
        Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, false, updateAssociations);
        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//email//updateAssociation.json"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void addAssociationInvalidAuth() {
        UpdateAssociations updateAssociations = new UpdateAssociations();
        updateAssociations.setThread_id("");
        updateAssociations.setAssociated_entity("");
        updateAssociations.setAssociated_entity_type_id("");
        updateAssociations.setEvent_type("");

        String basePath = "update-association";

        Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken() + "1234", null, null, false, updateAssociations);
        response.then().statusCode(401);
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void addAssociationMissingThreadId() {
        commanFunction function = new commanFunction();
        JsonPath candJson = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        candidateSlug = candJson.get("slug");

        UpdateAssociations updateAssociations = new UpdateAssociations();
        updateAssociations.setThread_id("");
        updateAssociations.setAssociated_entity(candidateSlug);
        updateAssociations.setAssociated_entity_type_id("5");
        updateAssociations.setEvent_type("add");

        String basePath = "update-association";

        Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, false, updateAssociations);
        response.then().statusCode(422);
        response.then().body("status", Matchers.containsString("fail"));
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("message", Matchers.containsString("The thread id field is required."));
    }

    @Owner("Ajendra Singh")
    @Test(dependsOnMethods = "getEmailTestWithAssociation", priority = 1, groups = "nightly-build")
    public void addAssociationInvalidEntity() {
        UpdateAssociations updateAssociations = new UpdateAssociations();
        updateAssociations.setThread_id(threadId);
        updateAssociations.setAssociated_entity("invalid_slug");
        updateAssociations.setAssociated_entity_type_id("5");
        updateAssociations.setEvent_type("add");

        String basePath = "update-association";

        Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, false, updateAssociations);
        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("fail"));
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("message", Matchers.containsString("Failed To update association : Invalid entity"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void addAssociationInvalidRequest() {
        UpdateAssociations updateAssociations = new UpdateAssociations();

        String basePath = "update-association";

        Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, false, updateAssociations);
        response.then().statusCode(422);
        response.then().body("status", Matchers.containsString("fail"));
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("message", Matchers.containsString("The thread id field is required.,The associated " +
                "entity field is required.,The associated entity type id field is required.,The event type field is required."));
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void syncV3() {
        String basePath = "syncv3";
        Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true);
        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void syncV3InvalidAuth() {
        String basePath = "syncv3";
        Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken() + "1234", null, null, true);
        response.then().statusCode(401);
    }

    @DataProvider
    public Object[][] testData() {
        commanFunction function = new commanFunction();
        Map<String, List<String>> associations = new HashMap<>();
        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        String candidateEntitySlug = jsonCandidate.get("slug");
        String candidateName = jsonCandidate.get("first_name") + " " + jsonCandidate.get("last_name");
        String candidateEmail = jsonCandidate.get("email");
        associations.put("5", Arrays.asList(candidateEntitySlug));
        JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        String companySlug = jsonCompany.get("slug");
        associations.put("3", Arrays.asList(companySlug));
        JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
        String contactSlug = jsonContact.get("slug");
        associations.put("2", Arrays.asList(contactSlug));
        JsonPath jsonJob = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath();
        String jobSlug = jsonJob.get("slug");
        associations.put("4", Arrays.asList(jobSlug));
        JsonPath dealJson = function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug, jobSlug).jsonPath();
        String dealSlug = dealJson.get("slug");
        associations.put("11", Arrays.asList(dealSlug));

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

        Object data[][] = {{receiverList, ccList, bccList, associations}};
        return data;
    }

}
