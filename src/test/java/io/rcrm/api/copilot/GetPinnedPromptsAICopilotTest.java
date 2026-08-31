package io.rcrm.api.copilot;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class GetPinnedPromptsAICopilotTest extends TestBase {

    private String albatrossAuthToken, apiAuthToken;
    private int ownerAccountID, actualUserId;
    private final Faker faker = new Faker();
    private final AllCrudFunctions function = new AllCrudFunctions();
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
        Assert.assertTrue(actualUserId > 0, "Valid user ID should be retrieved from Get Users API");
    }

    private int createPinnedPrompt(String title, String prompt) {
        JSONObject payload = new JSONObject();
        payload.put("title", title);
        payload.put("prompt", prompt);
        payload.put("pinned", true);

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "copilot/saved-prompts", albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 when creating pinned prompt");
        return response.jsonPath().getInt("data.id");
    }

    @DataProvider(name = "pinnedPromptProvider")
    public Object[][] pinnedPromptProvider() {
        String title = "TestPrompt_" + faker.lorem().word();
        String prompt = faker.lorem().sentence(5);
        int promptId = createPinnedPrompt(title, prompt);
        return new Object[][]{{title, promptId}};
    }

    private Response getPinnedPrompts(String searchTerm, String token) {
        Map<String, String> queryParams = new HashMap<>();
        if (searchTerm != null && !searchTerm.isEmpty()) queryParams.put("search", searchTerm);
        return RestClient.doGet("JSON", neptuneServiceURL, "copilot/saved-prompts/pinned-prompts", token, queryParams, null, true);
    }

    private void validatePinnedPromptResponse(JsonPath jp) {
        Assert.assertEquals(jp.getString("meta.message"), "Pinned prompts data retrieved successfully", "Unexpected meta message");
        Assert.assertEquals(jp.getString("meta.message_type"), "is-success", "Meta message_type should be 'is-success'");
        Assert.assertEquals(jp.getInt("meta.status"), 200, "Meta status should be 200");
        Assert.assertNotNull(jp.getString("meta.request_UUID"), "Request UUID should not be null");
    }

    private void validatePromptData(Map<String, Object> prompt) {
        Assert.assertNotNull(prompt.get("id"), "Prompt ID should not be null");
        Assert.assertEquals(prompt.get("user_id"), actualUserId, "Prompt user_id should match current user");
        Assert.assertEquals(prompt.get("account_id"), ownerAccountID, "Prompt account_id should match current account");
        Assert.assertNotNull(prompt.get("title"), "Prompt title should not be null");
        Assert.assertNotNull(prompt.get("prompt"), "Prompt text should not be null");
        Assert.assertEquals(prompt.get("pinned"), 1, "Pinned flag should be 1");
        Assert.assertEquals(prompt.get("created_by"), actualUserId, "Created_by should match current user");
        Assert.assertEquals(prompt.get("updated_by"), actualUserId, "Updated_by should match current user");
        Assert.assertNotNull(prompt.get("created_on"), "Created_on should not be null");
        Assert.assertNotNull(prompt.get("updated_on"), "Updated_on should not be null");
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "pinnedPromptProvider")
    public void getPinnedPromptsWithoutSearch_GET(String title, int promptId) {
        Response response = getPinnedPrompts(null, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 when fetching pinned prompts without search");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/pinnedPromptsResponse.json"));
        JsonPath jp = response.jsonPath();
        validatePinnedPromptResponse(jp);
        List<Map<String, Object>> prompts = jp.getList("data");
        Assert.assertNotNull(prompts, "Response data list should not be null");
        Assert.assertTrue(prompts.size() > 0, "Response should contain at least one pinned prompt");
        boolean foundPrompt = prompts.stream().anyMatch(p -> p.get("id").equals(promptId));
        Assert.assertTrue(foundPrompt, "Recently created pinned prompt should be present in the response");
        prompts.forEach(this::validatePromptData);
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "pinnedPromptProvider")
    public void getPinnedPromptsWithSearch_GET(String title, int promptId) {
        Response response = getPinnedPrompts(title, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 when fetching pinned prompts with search");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/pinnedPromptsResponse.json"));
        JsonPath jp = response.jsonPath();
        validatePinnedPromptResponse(jp);
        List<Map<String, Object>> prompts = jp.getList("data");
        Assert.assertNotNull(prompts, "Response data should not be null");
        boolean foundPrompt = prompts.stream().anyMatch(p -> p.get("id").equals(promptId) && ((String) p.get("title")).contains(title));
        Assert.assertTrue(foundPrompt, "Search results should include the created pinned prompt");
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "pinnedPromptProvider")
    public void getPinnedPromptsWithPartialSearch_GET(String title, int promptId) {
        String partialSearch = title.substring(0, Math.min(5, title.length()));
        Response response = getPinnedPrompts(partialSearch, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 for partial search");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/pinnedPromptsResponse.json"));
        JsonPath jp = response.jsonPath();
        validatePinnedPromptResponse(jp);
        List<Map<String, Object>> prompts = jp.getList("data");
        Assert.assertNotNull(prompts, "Response data should not be null for partial search");
    }

    @Owner("Akshaya Uppala")
    @Test
    public void getPinnedPromptsWithMaxLengthSearch_GET() {
        String maxSearch = faker.lorem().characters(100);
        Response response = getPinnedPrompts(maxSearch, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 for max length search");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/pinnedPromptsResponse.json"));
        validatePinnedPromptResponse(response.jsonPath());
    }

    @Owner("Sai Teja SG")
    @Test
    public void getPinnedPromptsWithNoMatch_GET() {
        String noMatchSearch = "NonExistentPrompt_" + faker.random().hex(10);
        Response response = getPinnedPrompts(noMatchSearch, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 even when no prompts match");
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/copilot/pinnedPromptsResponse.json"));
        JsonPath jp = response.jsonPath();
        validatePinnedPromptResponse(jp);
        List<Map<String, Object>> prompts = jp.getList("data");
        Assert.assertNotNull(prompts, "Data list should not be null even for no match search");
    }

    @Owner("Smit Patel")
    @Test
    public void getPinnedPromptsWithInvalidAuth_GET() {
        Response response = getPinnedPrompts(null, "invalid_token_12345");
        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 for invalid auth token");
    }
}
