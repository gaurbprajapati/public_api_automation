package io.recruitcrm.albatross.job;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class JobApplicationClosedLabelTest extends TestBase {

    private String albatrossToken;
    private int accountId;

    @BeforeClass(alwaysRun = true)    public void setupTestData() {
        albatrossToken = ThreadManager.getOwnerAlbatrossToken();
        accountId = ThreadManager.getAccount().getAccountId();
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void testUpdateJobApplicationClosedLabel_success() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("account_id", accountId);
        requestBody.put("key", "externalpageheadings");
        requestBody.put("value", new JSONObject());

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                albatrossToken, null, true, requestBody.toString());

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 status code");

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.get("message_type"), "is-success", "Expected message_type to be is-success");
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void testUpdateJobApplicationClosedLabel_InvalidAccountId() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("account_id", 999999999);
        requestBody.put("key", "externalpageheadings");
        requestBody.put("value", new JSONObject());

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                albatrossToken, null, true, requestBody.toString());

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 for invalid account ID, got: " + response.getStatusCode());

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.get("message"), "Invalid account ID or access denied", "Expected message to be Invalid account ID or access denied");
        Assert.assertEquals(jsonPath.get("message_type"), "is-danger", "Expected message_type to be is-danger");
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void testUpdateJobApplicationClosedLabel_InvalidKey() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("account_id", accountId);
        requestBody.put("key", "invalid_key");
        requestBody.put("value", new JSONObject());

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                albatrossToken, null, true, requestBody.toString());

        Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for invalid key");
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void testUpdateJobApplicationClosedLabel_MissingAccountId() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("key", "externalpageheadings");
        requestBody.put("value", new JSONObject());

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                albatrossToken, null, true, requestBody.toString());

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 for invalid account ID, got: " + response.getStatusCode());

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.get("message"), "Invalid account ID or access denied", "Expected message to be Invalid account ID or access denied");
        Assert.assertEquals(jsonPath.get("message_type"), "is-danger", "Expected message_type to be is-danger");
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void testUpdateJobApplicationClosedLabel_MissingKey() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("account_id", accountId);
        requestBody.put("value", new JSONObject());

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                albatrossToken, null, true, requestBody.toString());

        Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for missing key");
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void testUpdateJobApplicationClosedLabel_UnauthorizedUser() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("account_id", accountId);
        requestBody.put("key", "externalpageheadings");
        requestBody.put("value", new JSONObject());

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                albatrossToken + "invalid_token", null, true, requestBody.toString());

        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 for unauthorized access");
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void testUpdateJobApplicationClosedLabel_EmptyBody() {
        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                albatrossToken, null, true, "");

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 for invalid account ID, got: " + response.getStatusCode());

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.get("message"), "Invalid account ID or access denied", "Expected message to be Invalid account ID or access denied");
        Assert.assertEquals(jsonPath.get("message_type"), "is-danger", "Expected message_type to be is-danger");
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void testUpdateJobApplicationClosedLabel_MalformedJSON() {
        String malformedJson = "{\"account_id\": " + accountId + ", \"key\": \"externalpageheadings\", \"value\": {";

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                albatrossToken, null, true, malformedJson);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 for invalid account ID, got: " + response.getStatusCode());

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.get("message"), "Invalid account ID or access denied", "Expected message to be Invalid account ID or access denied");
        Assert.assertEquals(jsonPath.get("message_type"), "is-danger", "Expected message_type to be is-danger");
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void testUpdateJobApplicationClosedLabel_WithNullValues() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("account_id", accountId);
        requestBody.put("key", "externalpageheadings");
        requestBody.put("value", JSONObject.NULL);

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                albatrossToken, null, true, requestBody.toString());

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 status code");

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.get("message_type"), "is-success", "Expected message_type to be is-success");
    }
}
