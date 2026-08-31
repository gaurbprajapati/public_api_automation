package io.rcrm.api.copilot;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.UUID;
import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class SavedPromptsAICopilotTest extends TestBase {

    String albatrossAuthToken, apiAuthToken;
    int ownerAccountID, actualUserId;
    Faker faker = new Faker();
    AllCrudFunctions function = new AllCrudFunctions();
    commanFunction commonFuntion = new commanFunction();

    @BeforeClass
    public void setup() throws InterruptedException {
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
        Assert.assertTrue(actualUserId > 0);
    }

    private Response postSavedPrompt(JSONObject payload, String token) {
        return RestClient.doPost("JSON", neptuneServiceURL, "copilot/saved-prompts", token, null, true, payload);
    }

    private JSONObject buildPayload(String title, String prompt, Boolean pinned) {
        JSONObject payload = new JSONObject();
        if (title != null) payload.put("title", title);
        if (prompt != null) payload.put("prompt", prompt);
        if (pinned != null) payload.put("pinned", pinned);
        return payload;
    }

    private void validateMeta(JsonPath jp) {
        Assert.assertEquals(jp.getString("meta.message_type"), "is-success", "Expected meta.message_type to be 'is-success'");
        Assert.assertEquals(jp.getInt("meta.status"), 200, "Expected meta.status to be 200");
        Assert.assertNotNull(jp.getString("meta.request_UUID"), "Expected meta.request_UUID to be non-null");
    }

    private void validateSavedPrompt(JsonPath jp) {
        Assert.assertNotNull(jp.getInt("data.id"), "Prompt ID should not be null");
        Assert.assertEquals(jp.getInt("data.user_id"), actualUserId, "Prompt's user_id should match the actual user ID");
        Assert.assertEquals(jp.getInt("data.account_id"), ownerAccountID, "Prompt's account_id should match the owner's account ID");
        Assert.assertNotNull(jp.getString("data.title"), "Prompt title should not be null");
        Assert.assertNotNull(jp.getString("data.prompt"), "Prompt text should not be null");
        Assert.assertNotNull(jp.getBoolean("data.pinned"), "Prompt pinned field should not be null");
        Assert.assertEquals(jp.getInt("data.created_by"), actualUserId, "Prompt created_by should match actual user ID");
        Assert.assertEquals(jp.getInt("data.updated_by"), actualUserId, "Prompt updated_by should match actual user ID");
        Assert.assertNotNull(jp.getLong("data.created_on"), "Prompt created_on timestamp should not be null");
        Assert.assertNotNull(jp.getLong("data.updated_on"), "Prompt updated_on timestamp should not be null");
        Assert.assertEquals(jp.getString("meta.message"), "Prompt saved successfully", "Expected meta.message to be 'Prompt saved successfully'");
        validateMeta(jp);
    }

    private void assertValidResponse(Response response) {
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP status code 200 for valid prompt response");
        validateSavedPrompt(new JsonPath(response.getBody().asString()));
    }

    private Response sendPrompt(String title, String prompt, Boolean pinned) {
        return postSavedPrompt(buildPayload(title, prompt, pinned), albatrossAuthToken);
    }

    @Owner("Akshaya Uppala")
    @Test
    public void savePromptWithValidData_POST() {
        String title = "Prompt " + faker.lorem().word() + "_" + UUID.randomUUID().toString().substring(0, 6);
        String prompt = faker.lorem().sentence(6);
        Response response = sendPrompt(title, prompt, false);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for valid prompt save request");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/savedPromptResponse.json"));
        assertValidResponse(response);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("data.title"), title, "The saved prompt title does not match the requested title");
        Assert.assertEquals(jp.getString("data.prompt"), prompt, "The saved prompt text does not match the requested prompt");
        Assert.assertFalse(jp.getBoolean("data.pinned"), "Pinned field should be false for this request");
    }
    @Owner("Sai Teja SG")
    @Test
    public void savePromptWithPinnedTrue_POST() {
        Response response = sendPrompt(faker.lorem().word(), faker.lorem().sentence(), true);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 when saving prompt with pinned=true");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/savedPromptResponse.json"));
        assertValidResponse(response);
        Assert.assertTrue(response.jsonPath().getBoolean("data.pinned"), "Pinned field should be true when explicitly set");
    }
    @Owner("Smit Patel")
    @Test
    public void savePromptWithoutPinnedField_POST() {
        String title = faker.lorem().word();
        String prompt = faker.lorem().sentence();
        Response response = postSavedPrompt(buildPayload(title, prompt, null), albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 when pinned field is missing");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/savedPromptResponse.json"));
        assertValidResponse(response);
        Assert.assertFalse(response.jsonPath().getBoolean("data.pinned"), "Pinned field should default to false when missing");
    }
    @Owner("Akshaya Uppala")
    @Test
    public void savePromptWithMinLengthTitle_POST() {
        Response response = sendPrompt("A", faker.lorem().sentence(), false);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 when saving prompt with minimum length title");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/savedPromptResponse.json"));
        assertValidResponse(response);
        Assert.assertEquals(response.jsonPath().getString("data.title"), "A", "Title should be exactly 'A'");
    }
    @Owner("Sai Teja SG")
    @Test
    public void savePromptWithMaxLengthTitle_POST() {
        String maxTitle = faker.lorem().characters(100);
        Response response = sendPrompt(maxTitle, faker.lorem().sentence(), false);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for max length title");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/savedPromptResponse.json"));
        assertValidResponse(response);
        Assert.assertEquals(response.jsonPath().getString("data.title"), maxTitle, "Saved title should match the max length title");
    }
    @Owner("Smit Patel")
    @Test
    public void savePromptWithMinLengthPrompt_POST() {
        Response response = sendPrompt(faker.lorem().word(), "A", false);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for min length prompt");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/savedPromptResponse.json"));
        assertValidResponse(response);
        Assert.assertEquals(response.jsonPath().getString("data.prompt"), "A", "Prompt should be exactly 'A'");
    }
    @Owner("Akshaya Uppala")
    @Test
    public void savePromptWithEmptyTitle_POST() {
        Response response = sendPrompt("", faker.lorem().sentence(), false);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected status code 400 when saving prompt with empty title");
    }
    @Owner("Sai Teja SG")
    @Test
    public void savePromptWithExceedMaxTitle_POST() {
        Response response = sendPrompt(faker.lorem().characters(105), faker.lorem().sentence(), false);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected status code 400 when title exceeds max allowed length");
    }
    @Owner("Smit Patel")
    @Test
    public void savePromptWithEmptyPrompt_POST() {
        Response response = sendPrompt("Valid Title", "", false);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected status code 400 when saving prompt with empty prompt text");
    }
    @Owner("Akshaya Uppala")
    @Test
    public void savePromptWithMissingTitle_POST() {
        Response response = postSavedPrompt(buildPayload(null, faker.lorem().sentence(), false), albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected status code 400 when title field is missing");
    }
    @Owner("Sai Teja SG")
    @Test
    public void savePromptWithMissingPrompt_POST() {
        Response response = postSavedPrompt(buildPayload(faker.lorem().word(), null, false), albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected status code 400 when prompt field is missing");
    }
    @Owner("Smit Patel")
    @Test
    public void savePromptWithInvalidAuth_POST() {
        Response response = postSavedPrompt(buildPayload(faker.lorem().word(), faker.lorem().sentence(), false), "invalid_token_12345");
        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401 for request with invalid authentication token");
    }
    @Owner("Akshaya Uppala")
    @Test
    public void savePromptWithLongPrompt_POST() {
        String longPrompt = faker.lorem().characters(1000);
        Response response = sendPrompt(faker.lorem().word(), longPrompt, false);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 when saving prompt with long prompt text");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/savedPromptResponse.json"));
        assertValidResponse(response);
        String actualPrompt = response.jsonPath().getString("data.prompt");
        Assert.assertTrue(actualPrompt.contains(longPrompt), "Saved prompt text should contain the original long prompt text");
        Assert.assertTrue(actualPrompt.length() >= 1000, "Saved prompt length should be at least 1000 characters");
    }

    @Owner("Sai Teja SG")
    @Test
    public void savePromptWithExceedMaxPrompt_POST() {
        Response response = sendPrompt(faker.lorem().word(), faker.lorem().characters(1001), false);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected status code 400 when prompt exceeds max allowed length");
    }

    @Owner("Smit Patel")
    @Test
    public void saveMoreThanFiftyPrompts_POST() {
        for (int i = 0; i < 50; i++) {
            Response response = sendPrompt("Limit_" + i + "_" + faker.lorem().word(), faker.lorem().sentence(), false);
            Assert.assertEquals(response.getStatusCode(), 200, "Prompt " + (i + 1) + " should save successfully");
        }
        Response response51st = sendPrompt("Limit_51_" + faker.lorem().word(), faker.lorem().sentence(), false);
        Assert.assertEquals(response51st.getStatusCode(), 201, "Saving 51st prompt should fail with 400");
        JsonPath jp = response51st.jsonPath();
        Assert.assertEquals(jp.getString("meta.message"), "You've reached the saved prompt limit", "Expected pinned limit error message");
        Assert.assertEquals(jp.getString("meta.message_type"), "is-fail", "Message type should be is-fail for limit exceeded");
    }

}
