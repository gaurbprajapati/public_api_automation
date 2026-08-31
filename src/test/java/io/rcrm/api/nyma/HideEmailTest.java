package io.rcrm.api.nyma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.nyma.HideEmails;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class HideEmailTest extends TestBase {
    String threadId;

    @Owner("Ajendra Singh")
    @Test(priority = 1, groups = "nightly-build")
    public void getThread_Test(){
        connectSpecificEmail(ThreadManager.getAccount().getAccountId(), getEmailAccountDetails()[0],getEmailAccountDetails()[1],"imap",1, 1,4);

        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("page", "1");
        queryParameters.put("page_size", "1");
        queryParameters.put("linked_email_type", "1");
        Response getThreadsResponse = RestClient.doGet("JSON", nymaURLv3, "/threads", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
        assert getThreadsResponse != null;
        getThreadsResponse.then().statusCode(200);
        getThreadsResponse.then().body("status", Matchers.containsString("success"));
        getThreadsResponse.then().body("message_type", Matchers.containsString("is-success"));
        JsonPath jp = getThreadsResponse.jsonPath();
        threadId = jp.get("data.records[0].latest_draft_or_message.thread_id");
    }

    @Owner("Ajendra Singh")
    @Test(priority = 2, groups = "nightly-build")
    public void getThreadById_Test() {
        String basePath = "/threads/" + threadId;
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("linked_email_type", "1");

        Response response = RestClient.doGet("JSON", nymaURLv3, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
        assert response != null;
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
    }

    @Owner("Ajendra Singh")
    @Test(priority = 3, groups = "nightly-build")
    public void markHideEmailByThreadId() {
        HideEmails hideEmails = new HideEmails();
        ArrayList<String> threadIds = new ArrayList<String>();
        threadIds.add(threadId);
        hideEmails.setThread_ids(threadIds);
        hideEmails.setHide_email(1);
        hideEmails.setLinked_email_type(1);
        hideEmails.setBulk_action(1);

        Response response = RestClient.doPost("JSON", nymaURLv3, "/private-emails", ThreadManager.getOwnerAlbatrossToken(), null, true, hideEmails);
        assert response != null;
        response.then().statusCode(200);
//        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//emails//hideUnhideEmails.json"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.containsString("Email Marked as Private Successful"));
    }

    @Owner("Ajendra Singh")
    @Test(priority = 4, groups = "nightly-build")
    public void getHiddenEmails() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("linked_email_type", "1");
        queryParameters.put("page", "1");
        queryParameters.put("page_size", "25");
        Response response = RestClient.doGet("JSON", nymaURLv3, "/private-emails", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
        assert response != null;
        response.then().statusCode(200);
//        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//emails//getPrivateEmails.json"));
        response.then().body("message_type", Matchers.containsString("is-success"));
    }
}
