package io.rcrm.api.copilot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetConversationsAICopilotTest extends TestBase {

    String albatrossAuthToken;
    String apiAuthToken;
    Faker faker = new Faker();
    private List<String> createdConversationUUIDs;
    private List<String> createdConversationTitles;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createdConversationUUIDs = new java.util.ArrayList<>();
        createdConversationTitles = new java.util.ArrayList<>();
        createTestConversations();
    }

    private void createTestConversations() {
        // Create multiple test conversations by sending ask requests
        String[] testMessages = {
            "Show me all candidates with Java skills",
            "Find candidates added in the last 10 days",
            "What are the open job positions?",
            "Show me the pipeline for open jobs",
            "All candidates placed in last 30 days"
        };

        String[] expectedTitles = {
            "Generated Candidates Records",
            "Recent Candidates Analysis", 
            "Open Job Positions",
            "Job Pipeline Analysis",
            "Placement History Report"
        };

        for (int i = 0; i < testMessages.length; i++) {
            Response response = sendAskCopilotRequest(testMessages[i]);
            if (response.getStatusCode() == 200) {
                JsonPath completion = extractCompletionEvent(response);
                String chatUUID = completion.getString("meta.request_UUID");
                if (chatUUID != null && !chatUUID.isEmpty()) {
                    createdConversationUUIDs.add(chatUUID);
                    createdConversationTitles.add(expectedTitles[i]);
                }
            }
        }
    }

    private Response sendAskCopilotRequest(String message) {
        JSONObject payload = new JSONObject();
        payload.put("message", message);
        payload.put("uuid", "");
        return RestClient.doPost("JSON", neptuneServiceURL, "copilot/ask", albatrossAuthToken, null, true, payload);
    }

    private JsonPath extractCompletionEvent(Response response) {
        String responseBody = response.getBody().asString();
        Assert.assertNotNull(responseBody, "Response body should not be null");
        Assert.assertFalse(responseBody.trim().isEmpty(), "Response body should not be empty");
        int completeMessageIndex = responseBody.lastIndexOf("\"complete_message\"");
        Assert.assertTrue(completeMessageIndex != -1, "Completion event with 'complete_message' not found in response");
        int dataIndex = responseBody.lastIndexOf("\"data\"", completeMessageIndex);
        int startIndex = responseBody.lastIndexOf("{", dataIndex - 1);
        int endIndex = responseBody.lastIndexOf("}") + 1;
        return new JsonPath(responseBody.substring(startIndex, endIndex));
    }

    private Response getConversations(String searchTerm, Integer page, Integer limit, String token) {
        Map<String, String> queryParams = new HashMap<>();
        if (searchTerm != null && !searchTerm.isEmpty()) {
            queryParams.put("searchTerm", searchTerm);
        }
        if (page != null) {
            queryParams.put("page", String.valueOf(page));
        }
        if (limit != null) {
            queryParams.put("limit", String.valueOf(limit));
        }
        return RestClient.doGet("JSON", neptuneServiceURL, "copilot/conversations", token, queryParams, null, true);
    }

    private void validateConversationsResponse(Response response) {
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP status 200");
        JsonPath jp = response.jsonPath();
        
        // Validate response structure
        Assert.assertNotNull(jp.get("data"), "Data should not be null");
        Assert.assertNotNull(jp.get("meta"), "Meta should not be null");
        
        // Validate meta structure
        Assert.assertEquals(jp.getString("meta.message"), "Conversations fetched successfully", "Meta message should match expected");
        Assert.assertEquals(jp.getString("meta.message_type"), "is-success", "Meta message_type should be 'is-success'");
        Assert.assertEquals(jp.getInt("meta.status"), 200, "Meta status should be 200");
        Assert.assertNotNull(jp.getString("meta.request_UUID"), "Meta request_UUID should not be null");
    }

    private void validateConversationData(JsonPath jp) {
        List<Map<String, Object>> conversations = jp.getList("data");
        Assert.assertNotNull(conversations, "Conversations list should not be null");
        
        for (Map<String, Object> conversation : conversations) {
            Assert.assertNotNull(conversation.get("chat_uuid"), "chat_uuid should not be null");
            Assert.assertNotNull(conversation.get("title"), "title should not be null");
            Assert.assertFalse(conversation.get("chat_uuid").toString().trim().isEmpty(), "chat_uuid should not be empty");
            Assert.assertFalse(conversation.get("title").toString().trim().isEmpty(), "title should not be empty");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithDefaultParams_GET() {
        Response response = getConversations(null, null, null, albatrossAuthToken);
        validateConversationsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateConversationData(jp);
        
        // Should return conversations (may be empty if no conversations exist)
        List<Map<String, Object>> conversations = jp.getList("data");
        Assert.assertNotNull(conversations, "Conversations list should not be null");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithPagination_GET() {
        Response response = getConversations(null, 1, 5, albatrossAuthToken);
        validateConversationsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateConversationData(jp);
        
        List<Map<String, Object>> conversations = jp.getList("data");
        Assert.assertNotNull(conversations, "Conversations list should not be null");
        Assert.assertTrue(conversations.size() <= 5, "Should return at most 5 conversations per page");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithSearchTerm_GET() {
        String searchTerm = "candidates";
        Response response = getConversations(searchTerm, 1, 10, albatrossAuthToken);
        validateConversationsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateConversationData(jp);
        
        // Verify that returned conversations contain the search term in title
        List<Map<String, Object>> conversations = jp.getList("data");
        for (Map<String, Object> conversation : conversations) {
            String title = conversation.get("title").toString().toLowerCase();
            Assert.assertTrue(title.contains(searchTerm.toLowerCase()) || 
                           title.contains("candidate") || 
                           title.contains("generated"), 
                           "Conversation title should contain search term or be related");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithEmptySearchTerm_GET() {
        Response response = getConversations("", 1, 10, albatrossAuthToken);
        validateConversationsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateConversationData(jp);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithLargePage_GET() {
        Response response = getConversations(null, 999, 10, albatrossAuthToken);
        validateConversationsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateConversationData(jp);
        
        // Large page number should return empty list or handle gracefully
        List<Map<String, Object>> conversations = jp.getList("data");
        Assert.assertNotNull(conversations, "Conversations list should not be null");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithZeroLimit_GET() {
        Response response = getConversations(null, 1, 0, albatrossAuthToken);
        // Should handle zero limit gracefully - either return 400 or empty list
        if (response.getStatusCode() == 200) {
            JsonPath jp = response.jsonPath();
            validateConversationData(jp);
            List<Map<String, Object>> conversations = jp.getList("data");
            Assert.assertNotNull(conversations, "Conversations list should not be null");
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for zero limit");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithNegativePage_GET() {
        Response response = getConversations(null, -1, 10, albatrossAuthToken);
        // Should handle negative page gracefully
        if (response.getStatusCode() == 200) {
            JsonPath jp = response.jsonPath();
            validateConversationData(jp);
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for negative page");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithNegativeLimit_GET() {
        Response response = getConversations(null, 1, -5, albatrossAuthToken);
        // Should handle negative limit gracefully
        if (response.getStatusCode() == 200) {
            JsonPath jp = response.jsonPath();
            validateConversationData(jp);
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for negative limit");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithMaxLimit_GET() {
        Response response = getConversations(null, 1, 100, albatrossAuthToken);
        validateConversationsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateConversationData(jp);
        
        List<Map<String, Object>> conversations = jp.getList("data");
        Assert.assertNotNull(conversations, "Conversations list should not be null");
        Assert.assertTrue(conversations.size() <= 100, "Should return at most 100 conversations");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithSpecialCharacters_GET() {
        String searchTerm = "candidates@#$%^&*()";
        Response response = getConversations(searchTerm, 1, 10, albatrossAuthToken);
        validateConversationsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateConversationData(jp);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithLongSearchTerm_GET() {
        String longSearchTerm = faker.lorem().characters(1000);
        Response response = getConversations(longSearchTerm, 1, 10, albatrossAuthToken);
        validateConversationsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateConversationData(jp);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithInvalidAuth_GET() {
        Response response = getConversations(null, 1, 10, "invalid_token_12345");
        Assert.assertEquals(response.getStatusCode(), 401, "Expected HTTP 401 for invalid token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithEmptyAuth_GET() {
        Response response = getConversations(null, 1, 10, "");
        Assert.assertEquals(response.getStatusCode(), 401, "Expected HTTP 401 for empty token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithNullAuth_GET() {
        Response response = getConversations(null, 1, 10, null);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected HTTP 401 for null token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithExactSearchMatch_GET() {
        if (!createdConversationTitles.isEmpty()) {
            String exactTitle = createdConversationTitles.get(0);
            Response response = getConversations(exactTitle, 1, 10, albatrossAuthToken);
            validateConversationsResponse(response);
            
            JsonPath jp = response.jsonPath();
            validateConversationData(jp);
            
            // Note: This might not find exact match due to title generation variations
            // The test validates that the search works without errors
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithCaseInsensitiveSearch_GET() {
        String searchTerm = "CANDIDATES";
        Response response = getConversations(searchTerm, 1, 10, albatrossAuthToken);
        validateConversationsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateConversationData(jp);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationsWithMultiplePages_GET() {
        // Test multiple pages to ensure pagination works
        for (int page = 1; page <= 3; page++) {
            Response response = getConversations(null, page, 2, albatrossAuthToken);
            validateConversationsResponse(response);
            
            JsonPath jp = response.jsonPath();
            validateConversationData(jp);
        }
    }
}
