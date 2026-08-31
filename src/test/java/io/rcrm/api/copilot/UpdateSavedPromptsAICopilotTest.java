package io.rcrm.api.copilot;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.UUID;
import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class UpdateSavedPromptsAICopilotTest extends TestBase {

    String albatrossAuthToken, apiAuthToken;
    int ownerAccountID, actualUserId;
    Faker faker = new Faker();
    AllCrudFunctions function = new AllCrudFunctions();
    commanFunction commonFuntion = new commanFunction();
    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        getOwnerDetailsFromAPI();
    }

    private void getOwnerDetailsFromAPI() {
        Response getUsers = commonFuntion.getUsers(baseURL, apiAuthToken);
        Assert.assertNotNull(getUsers);
        Assert.assertEquals(getUsers.getStatusCode(), 200);
        JsonPath jp = getUsers.jsonPath();
        actualUserId = jp.get("[0].id");
        Assert.assertTrue(actualUserId > 0, "Failed: actualUserId is invalid or missing.");
    }

    private Response createPrompt(String title, String prompt, Boolean pinned) {
        JSONObject payload = new JSONObject();
        payload.put("title", title);
        payload.put("prompt", prompt);
        if (pinned != null) payload.put("pinned", pinned);
        return RestClient.doPost("JSON", neptuneServiceURL, "copilot/saved-prompts", albatrossAuthToken, null, true, payload);
    }

    private Response updatePrompt(int promptId, JSONObject payload, String token) {
        return RestClient.doPatchOnce("JSON", neptuneServiceURL, "copilot/saved-prompts/" + promptId, token, null, true, payload);
    }

    private JSONObject buildUpdatePayload(String title, String prompt, Boolean pinned) {
        JSONObject payload = new JSONObject();
        if (title != null) payload.put("title", title);
        if (prompt != null) payload.put("prompt", prompt);
        if (pinned != null) payload.put("pinned", pinned);
        return payload;
    }

    private void validateMeta(JsonPath jp, String expectedMessage) {
        Assert.assertEquals(jp.getString("meta.message_type"), "is-success", "Meta validation failed: message_type mismatch.");
        Assert.assertEquals(jp.getInt("meta.status"), 200, "Meta validation failed: status mismatch.");
        Assert.assertNotNull(jp.getString("meta.request_UUID"), "Meta validation failed: request_UUID is null.");
        Assert.assertEquals(jp.getString("meta.message"), expectedMessage, "Meta validation failed: message mismatch.");
    }

    private void validateUpdatedPrompt(JsonPath jp) {
        Assert.assertNotNull(jp.getInt("data.id"), "Data validation failed: id is null.");
        Assert.assertEquals(jp.getInt("data.user_id"), actualUserId, "Data validation failed: user_id mismatch.");
        Assert.assertEquals(jp.getInt("data.account_id"), ownerAccountID, "Data validation failed: account_id mismatch.");
        Assert.assertNotNull(jp.getString("data.title"), "Data validation failed: title is null.");
        Assert.assertNotNull(jp.getString("data.prompt"), "Data validation failed: prompt is null.");
        Assert.assertNotNull(jp.get("data.pinned"), "Data validation failed: pinned flag is null.");
        Assert.assertEquals(jp.getInt("data.created_by"), actualUserId, "Data validation failed: created_by mismatch.");
        Assert.assertEquals(jp.getInt("data.updated_by"), actualUserId, "Data validation failed: updated_by mismatch.");
        Assert.assertNotNull(jp.getLong("data.created_on"), "Data validation failed: created_on timestamp is null.");
        Assert.assertNotNull(jp.getLong("data.updated_on"), "Data validation failed: updated_on timestamp is null.");
        validateMeta(jp, "Prompt updated successfully");
    }

    private int generatePromptId() {
        String title = "Test_" + faker.lorem().word() + "_" + UUID.randomUUID().toString().substring(0, 6);
        String prompt = faker.lorem().sentence(5);
        Response response = createPrompt(title, prompt, false);
        Assert.assertEquals(response.getStatusCode(), 200, "Prompt creation failed: expected 200.");
        return response.jsonPath().getInt("data.id");
    }

    @DataProvider(name = "promptProvider")
    public Object[][] promptProvider() {
        int promptId = generatePromptId();
        return new Object[][]{{promptId}};
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "promptProvider")
    public void updatePromptWithValidData_PATCH(int promptId) {
        String newTitle = "Updated_" + faker.lorem().word();
        String newPrompt = faker.lorem().sentence(10);
        JSONObject payload = buildUpdatePayload(newTitle, newPrompt, false);
        Response response = updatePrompt(promptId, payload, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 for valid prompt update.");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/savedPromptResponse.json"));
        JsonPath jp = response.jsonPath();
        validateUpdatedPrompt(jp);
        Assert.assertEquals(jp.getString("data.title"), newTitle, "Expected updated title to match.");
        Assert.assertEquals(jp.getString("data.prompt"), newPrompt, "Expected updated prompt to match.");
        Assert.assertFalse(jp.getBoolean("data.pinned"), "Expected pinned to be false after update.");
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "promptProvider")
    public void updatePromptSetPinnedTrue_PATCH(int promptId) {
        String newTitle = "Pinned_" + faker.lorem().word();
        String newPrompt = faker.lorem().sentence(10);
        JSONObject payload = buildUpdatePayload(newTitle, newPrompt, true);
        Response response = updatePrompt(promptId, payload, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 for pinned prompt update.");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/savedPromptResponse.json"));
        JsonPath jp = response.jsonPath();
        validateUpdatedPrompt(jp);
        Assert.assertEquals(jp.getString("data.title"), newTitle, "Expected title to be updated correctly.");
        Assert.assertEquals(jp.getString("data.prompt"), newPrompt, "Expected prompt text to be updated correctly.");
        Assert.assertTrue(jp.getBoolean("data.pinned"), "Expected pinned to be true after update.");
    }


    @Owner("Smit Patel")
    @Test(dataProvider = "promptProvider")
    public void updatePromptWithMaxLengthTitle_PATCH(int promptId) {
        String maxTitle = faker.lorem().characters(100);
        JSONObject payload = buildUpdatePayload(maxTitle, faker.lorem().sentence(), false);
        Response response = updatePrompt(promptId, payload, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 for max-length title update.");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/savedPromptResponse.json"));
        JsonPath jp = response.jsonPath();
        validateUpdatedPrompt(jp);
        Assert.assertEquals(jp.getString("data.title"), maxTitle, "Title does not match max-length value.");
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "promptProvider")
    public void updatePromptWithMinLengthTitle_PATCH(int promptId) {
        JSONObject payload = buildUpdatePayload("A", faker.lorem().sentence(), false);
        Response response = updatePrompt(promptId, payload, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 for min-length title update.");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/savedPromptResponse.json"));
        Assert.assertEquals(response.jsonPath().getString("data.title"), "A", "Min-length title not updated correctly.");
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "promptProvider")
    public void updatePromptWithMinLengthPrompt_PATCH(int promptId) {
        JSONObject payload = buildUpdatePayload(faker.lorem().word(), "A", false);
        Response response = updatePrompt(promptId, payload, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 for min-length prompt update.");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/savedPromptResponse.json"));
        Assert.assertEquals(response.jsonPath().getString("data.prompt"), "A", "Min-length prompt not updated correctly.");
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "promptProvider")
    public void updatePromptWithLongPrompt_PATCH(int promptId) {
        String longPrompt = faker.lorem().paragraph(1000);
        JSONObject payload = buildUpdatePayload(faker.lorem().word(), longPrompt, false);
        Response response = updatePrompt(promptId, payload, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 for long prompt update.");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/savedPromptResponse.json"));
        String actualPrompt = response.jsonPath().getString("data.prompt");
        Assert.assertTrue(actualPrompt.contains(longPrompt.substring(0, 100)), "Long prompt not partially retained.");
        Assert.assertTrue(actualPrompt.length() >= 1000, "Prompt length less than expected after update.");
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "promptProvider")
    public void updatePromptWithEmptyTitle_PATCH(int promptId) {
        JSONObject payload = buildUpdatePayload("", faker.lorem().sentence(), false);
        Response response = updatePrompt(promptId, payload, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for empty title.");
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "promptProvider")
    public void updatePromptWithExceedMaxTitle_PATCH(int promptId) {
        JSONObject payload = buildUpdatePayload(faker.lorem().characters(105), faker.lorem().sentence(), false);
        Response response = updatePrompt(promptId, payload, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for exceeding max title length.");
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "promptProvider")
    public void updatePromptWithEmptyPrompt_PATCH(int promptId) {
        JSONObject payload = buildUpdatePayload(faker.lorem().word(), "", false);
        Response response = updatePrompt(promptId, payload, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for empty prompt.");
    }

    @Owner("Akshaya Uppala")
    @Test
    public void updatePromptWithInvalidId_PATCH() {
        int invalidId = faker.number().numberBetween(9999, 99999);
        JSONObject payload = buildUpdatePayload(faker.lorem().word(), faker.lorem().sentence(), false);
        Response response = updatePrompt(invalidId, payload, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 404, "Expected 404 for invalid prompt ID.");
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "promptProvider")
    public void updatePromptWithInvalidAuth_PATCH(int promptId) {
        JSONObject payload = buildUpdatePayload(faker.lorem().word(), faker.lorem().sentence(), false);
        Response response = updatePrompt(promptId, payload, "invalid_token_12345");
        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 for invalid authentication token.");
    }
}
