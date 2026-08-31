package io.recruitcrm.ostrich;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.ostrich.LinkedInChatPreference;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class LinkedInAccessControlTest extends TestBase {

    private int accountOwnerId;
    private String ownerTokenAlbatross;
    private int accountId;
    private String ownerEmail;
    commanFunction function = new commanFunction();

    @BeforeClass(alwaysRun = true)    public void initSetup() {
        // Get owner user details
        Response usersResponse = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
        usersResponse.then().statusCode(200);
        JsonPath user = usersResponse.jsonPath();
        accountOwnerId = user.get("[0].id");

        ownerTokenAlbatross = ThreadManager.getOwnerAlbatrossToken();
        accountId = ThreadManager.getAccount().getAccountId();
        ownerEmail = ThreadManager.getAccount().getOwner().getEmail();
    }

    @Owner("Suhel Bhadane")
    @Test(priority = 1, groups = "nightly-build")
    public void updateLinkedInChatPreference_NoLinkedInSetup() {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + ownerTokenAlbatross);

        LinkedInChatPreference chatPreference = new LinkedInChatPreference();
        chatPreference.setHideChat(1);
        String basePath = "/unipile/account/update-hide-chat-preference";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, headers, null, true, chatPreference);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat()
                .body("message", Matchers.is("Error while updating hide chat preference"))
                .body("message_type", Matchers.containsString("is-danger"))
                .body(matchesJsonSchemaInClasspath("privateApi/ostrich/linkedInChatPreference.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(priority = 2, groups = "nightly-build")
    public void updateLinkedInChatPreference_WithoutLinkedInAccount() {
        ReaperIntegration.insertUnipileSubscription(accountId, ownerEmail, accountOwnerId);

        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + ownerTokenAlbatross);

        LinkedInChatPreference chatPreference = new LinkedInChatPreference();
        chatPreference.setHideChat(1);
        String basePath = "/unipile/account/update-hide-chat-preference";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, headers, null, true, chatPreference);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat()
                .body("message", Matchers.is("Error while updating hide chat preference"))
                .body("message_type", Matchers.containsString("is-danger"))
                .body(matchesJsonSchemaInClasspath("privateApi/ostrich/linkedInChatPreference.json"));

    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "chatPreferenceData", priority = 3, groups = "nightly-build")
    public void updateLinkedInChatPreference(int hideChatValue) {
        setupLinkedInSubscription();

        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + ownerTokenAlbatross);

        LinkedInChatPreference chatPreference = new LinkedInChatPreference();
        chatPreference.setHideChat(hideChatValue);
        String basePath = "/unipile/account/update-hide-chat-preference";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, headers, null, true, chatPreference);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat()
                .body("message", Matchers.is("Hide chat preference updated successfully"))
                .body("message_type", Matchers.containsString("is-success"))
                .body(matchesJsonSchemaInClasspath("privateApi/ostrich/linkedInChatPreference.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(priority = 4, groups = "nightly-build")
    public void updateLinkedInChatPreference_401_InvalidToken() {
        setupLinkedInSubscription();

        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + ownerTokenAlbatross + "123");

        LinkedInChatPreference chatPreference = new LinkedInChatPreference();
        chatPreference.setHideChat(1);
        String basePath = "/unipile/account/update-hide-chat-preference";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, headers, null, true, chatPreference);

        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Suhel Bhadane")
    @Test(priority = 5, groups = "nightly-build")
    public void updateLinkedInChatPreference_InvalidValue() {
        setupLinkedInSubscription();

        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + ownerTokenAlbatross);

        LinkedInChatPreference chatPreference = new LinkedInChatPreference();
        chatPreference.setHideChat(3);  // Invalid value, only 0 or 1 expected
        String basePath = "/unipile/account/update-hide-chat-preference";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, headers, null, true, chatPreference);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.is(400));
        response.then().body("message", Matchers.is("The hide chat field must be true or false."));
    }

    private void setupLinkedInSubscription() {
        ReaperIntegration.insertUnipileSubscription(accountId, ownerEmail, accountOwnerId);
        ReaperIntegration.createDummyUnipileUserInfo(accountId, accountOwnerId);
    }

    @DataProvider(name = "chatPreferenceData", parallel = true)
    public Object[][] getChatPreferenceTestData() {
        return new Object[][]{
                {1},
                {0}
        };
    }
}
