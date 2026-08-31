package io.rcrm.api.sms;

import com.qa.api.util.Owner;
import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

@AccountType("Business|AlbatrossTkn")
public class SmsValidateTest extends TestBase {

    private final commanFunction function = new commanFunction();
    private final String basePath = "enrollments/sms-linkedin-status";

    String ownerAlbatrossToken;
    int accountId;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        ownerAlbatrossToken = ThreadManager.getOwnerAlbatrossToken();
        accountId = ThreadManager.getAccount().getAccountId();
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void smsValidation_FailType1() {
        ReaperIntegration.updateTwilioSubaccount(accountId);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("check_linkedin_status", "0");
        queryParams.put("check_sms_status", "1");

        Response response = RestClient.doGet("JSON", nymaURL, basePath, ownerAlbatrossToken, queryParams, null, false);

        response.then().statusCode(200);
        response.then().body("message", Matchers.containsString("Failed To Prospect SMS & Linkedin validation : Validation failed"));
        response.then().body("message_type", Matchers.is("is-danger"));
        response.then().body("status", Matchers.is("fail"));
        response.then().body("action_name", Matchers.is("Prospect SMS & Linkedin validation"));
        response.then().body("data.sms.message", Matchers.is("A2P10DLC is not registered."));
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void smsValidation_FailType2() {
        ReaperIntegration.enableA2PRegistration(accountId);

        // First endpoint hit for the case where db write is in progress
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("check_linkedin_status", "0");
        queryParams.put("check_sms_status", "1");
        Response response = RestClient.doGet("JSON", nymaURL, basePath, ownerAlbatrossToken, queryParams, null, false);
        response.then().statusCode(200);
        response.then().body("message", Matchers.containsString("Failed To Prospect SMS & Linkedin validation : Validation failed"));
        response.then().body("message_type", Matchers.is("is-danger"));
        response.then().body("status", Matchers.is("fail"));
        response.then().body("action_name", Matchers.is("Prospect SMS & Linkedin validation"));
        response.then().body("data.sms.message", Matchers.is("A2P10DLC is not registered."));

        // Second endpoint hit for the case where db write is completed
        Map<String, String> queryParams2 = new HashMap<>();
        queryParams2.put("check_linkedin_status", "0");
        queryParams2.put("check_sms_status", "1");
        Response response2 = RestClient.doGet("JSON", nymaURL, basePath, ownerAlbatrossToken, queryParams2, null, false);
        response2.then().statusCode(200);
        response2.then().body("message", Matchers.containsString("Failed To Prospect SMS & Linkedin validation : Validation failed"));
        response2.then().body("message_type", Matchers.is("is-danger"));
        response2.then().body("status", Matchers.is("fail"));
        response2.then().body("action_name", Matchers.is("Prospect SMS & Linkedin validation"));
        response2.then().body("data.sms.message", Matchers.is("Number is not purchased."));
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void smsValidationPass() {
        ReaperIntegration.enableA2PRegistration(accountId);
        ReaperIntegration.updateTwilioSubaccount(accountId);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("check_linkedin_status", "0");
        queryParams.put("check_sms_status", "1");

        Response response = RestClient.doGet("JSON", nymaURL, basePath, ownerAlbatrossToken, queryParams, null, false);

        response.then().statusCode(200);
        response.then().body("message", Matchers.containsString("Prospect SMS & Linkedin validation Successful"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("status", Matchers.is("success"));
        response.then().body("action_name", Matchers.is("Prospect SMS & Linkedin validation"));
        response.then().body("data", Matchers.is(Matchers.empty()));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void linkedInValidation_FailType1() {
        Response usersResponse = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
        usersResponse.then().statusCode(200);
        int ownerUserId = usersResponse.jsonPath().getInt("[0].id");
        ReaperIntegration.insertUnipileSubscription(accountId, ThreadManager.getAccount().getOwner().getEmail(), ownerUserId);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("check_linkedin_status", "1");
        queryParams.put("check_sms_status", "0");

        Response response = RestClient.doGet("JSON", nymaURL, basePath, ownerAlbatrossToken, queryParams, null, false);

        response.then().statusCode(200);
        response.then().body("message", Matchers.containsString("Failed To Prospect SMS & Linkedin validation : Validation failed"));
        response.then().body("message_type", Matchers.is("is-danger"));
        response.then().body("status", Matchers.is("fail"));
        response.then().body("action_name", Matchers.is("Prospect SMS & Linkedin validation"));
        response.then().body("data.linkedin.type", Matchers.is(1));
        response.then().body("data.linkedin.message", Matchers.is("To start sending messages via LinkedIn integration, you'll first need to connect your LinkedIn account."));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void linkedInValidation_FailType2() {
        Response usersResponse = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
        usersResponse.then().statusCode(200);
        int ownerUserId = usersResponse.jsonPath().getInt("[0].id");
        ReaperIntegration.insertUnipileSubscription(accountId, ThreadManager.getAccount().getOwner().getEmail(), ownerUserId);
        ReaperIntegration.createDummyUnipileUserInfo(accountId, ownerUserId);
        ReaperIntegration.updateUnipileSubscriptionStatus(accountId);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("check_linkedin_status", "1");
        queryParams.put("check_sms_status", "0");

        Response response = RestClient.doGet("JSON", nymaURL, basePath, ownerAlbatrossToken, queryParams, null, false);

        response.then().statusCode(200);
        response.then().body("message", Matchers.containsString("Prospect SMS & Linkedin validation Successful"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("status", Matchers.is("success"));
        response.then().body("action_name", Matchers.is("Prospect SMS & Linkedin validation"));
    }
}
