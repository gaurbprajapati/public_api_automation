package io.rcrm.api.copilot;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
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
public class TogglePinPromptAICopilotTest extends TestBase {

    String albatrossAuthToken, apiAuthToken;
    int ownerAccountID, actualUserId;
    Faker faker = new Faker();
    AllCrudFunctions function = new AllCrudFunctions();

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        getOwnerDetailsFromAPI();
    }

    private void getOwnerDetailsFromAPI() {
        Response getUsers = function.getUsers(albatrossURL, albatrossAuthToken);
        Assert.assertNotNull(getUsers, "Get users response should not be null");
        Assert.assertEquals(getUsers.getStatusCode(), 200, "Expected status code 200 when retrieving users");
        JsonPath jp = getUsers.jsonPath();
        actualUserId = jp.get("data.records[0].id");
        Assert.assertTrue(actualUserId > 0, "User ID should be greater than 0");
    }

    private int createPrompt() {
        JSONObject payload = new JSONObject();
        payload.put("title", "Prompt_" + faker.lorem().word());
        payload.put("prompt", faker.lorem().sentence(5));
        payload.put("pinned", false);
        Response response = RestClient.doPost("JSON", neptuneServiceURL, "copilot/saved-prompts", albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to create prompt for pin test");
        return response.jsonPath().getInt("data.id");
    }

    @DataProvider(name = "promptProvider")
    public Object[][] promptProvider() {
        int promptId = createPrompt();
        return new Object[][]{{promptId}};
    }

    private Response togglePin(int promptId, boolean pin, String token) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("pin", String.valueOf(pin));
        return RestClient.doPost("JSON", neptuneServiceURL, "copilot/saved-prompts/toggle-pin/" + promptId, token, queryParams, true, null);
    }

    private void validatePinResponse(JsonPath jp, String expectedMessage) {
        Assert.assertNotNull(jp.getList("data"), "Pinned prompt data list should not be null");
        Assert.assertEquals(jp.getString("meta.message"), expectedMessage, "Meta message mismatch");
        Assert.assertEquals(jp.getString("meta.message_type"), "is-success", "Expected meta.message_type to be 'is-success'");
        Assert.assertEquals(jp.getInt("meta.status"), 200, "Expected meta.status to be 200");
        Assert.assertNotNull(jp.getString("meta.request_UUID"), "Meta.request_UUID should not be null");
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "promptProvider")
    public void pinPromptWithValidId_POST(int promptId) {
        Response response = togglePin(promptId, true, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 when pinning a prompt with valid ID");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/togglePinResponse.json"));
        validatePinResponse(response.jsonPath(), "Prompt pinned successfully");
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "promptProvider")
    public void unpinPromptWithValidId_POST(int promptId) {
        togglePin(promptId, true, albatrossAuthToken);
        Response response = togglePin(promptId, false, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 when unpinning a prompt with valid ID");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/togglePinResponse.json"));
        validatePinResponse(response.jsonPath(), "Prompt unpinned successfully");
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "promptProvider")
    public void togglePinMultipleTimes_POST(int promptId) {
        Response pinResponse = togglePin(promptId, true, albatrossAuthToken);
        Assert.assertEquals(pinResponse.getStatusCode(), 200, "Expected HTTP 200 when pinning the prompt the first time");
        pinResponse.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/togglePinResponse.json"));
        validatePinResponse(pinResponse.jsonPath(), "Prompt pinned successfully");

        Response unpinResponse = togglePin(promptId, false, albatrossAuthToken);
        Assert.assertEquals(unpinResponse.getStatusCode(), 200, "Expected HTTP 200 when unpinning the prompt");
        unpinResponse.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/togglePinResponse.json"));
        validatePinResponse(unpinResponse.jsonPath(), "Prompt unpinned successfully");

        Response repinResponse = togglePin(promptId, true, albatrossAuthToken);
        Assert.assertEquals(repinResponse.getStatusCode(), 200, "Expected HTTP 200 when pinning the prompt again");
        repinResponse.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/togglePinResponse.json"));
        validatePinResponse(repinResponse.jsonPath(), "Prompt pinned successfully");
    }

    @Owner("Akshaya Uppala")
    @Test
    public void pinMoreThanSixPrompts_POST() {
        List<Integer> promptIds = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            promptIds.add(createPrompt());
        }

        for (int i = 0; i < 6; i++) {
            Response response = togglePin(promptIds.get(i), true, albatrossAuthToken);
            Assert.assertEquals(response.getStatusCode(), 200, "Pin " + (i + 1) + " should succeed with HTTP 200");
            response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/togglePinResponse.json"));
            validatePinResponse(response.jsonPath(), "Prompt pinned successfully");
        }

        Response response7th = togglePin(promptIds.get(6), true, albatrossAuthToken);
        Assert.assertEquals(response7th.getStatusCode(), 201, "Pinning 7th prompt should fail with HTTP 400");
        JsonPath jp = response7th.jsonPath();
        Assert.assertEquals(jp.getString("meta.message"), "You've reached the pinned prompt limit", "Expected pinned limit error message");
        Assert.assertEquals(jp.getString("meta.message_type"), "is-fail", "Expected meta.message_type to be 'is-fail'");
    }

    @Owner("Sai Teja SG")
    @Test
    public void pinPromptWithInvalidId_POST() {
        int invalidId = faker.number().numberBetween(9999, 99999);
        Response response = togglePin(invalidId, true, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 404, "Expected HTTP 404 when pinning a non-existent prompt ID");
    }

    @Owner("Smit Patel")
    @Test
    public void pinPromptWithInvalidAuth_POST() {
        int promptId = createPrompt();
        Response response = togglePin(promptId, true, "invalid_token_12345");
        Assert.assertEquals(response.getStatusCode(), 401, "Expected HTTP 401 for invalid authentication token");
    }
}
