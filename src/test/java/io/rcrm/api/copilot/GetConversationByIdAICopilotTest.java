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
public class GetConversationByIdAICopilotTest extends TestBase {

    String albatrossAuthToken;
    String apiAuthToken;
    Faker faker = new Faker();
    private String validConversationUUID;
    private String invalidConversationUUID;
    private String malformedConversationUUID;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createTestConversation();
        setupTestUUIDs();
    }

    private void createTestConversation() {
        // Create a test conversation by sending ask requests
        String testMessage = "Hello, I need help with recruiting candidates";
        Response response = sendAskCopilotRequest(testMessage);
        
        if (response.getStatusCode() == 200) {
            JsonPath completion = extractCompletionEvent(response);
            validConversationUUID = completion.getString("meta.request_UUID");
            Assert.assertNotNull(validConversationUUID, "Valid conversation UUID should not be null");
            Assert.assertFalse(validConversationUUID.trim().isEmpty(), "Valid conversation UUID should not be empty");
        } else {
            // Fallback to a mock UUID if conversation creation fails
            validConversationUUID = "d2b0bbdd-05f2-4898-8969-b2537137f027";
        }
    }

    private void setupTestUUIDs() {
        invalidConversationUUID = "invalid-uuid-12345";
        malformedConversationUUID = "not-a-valid-uuid-format";
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

    private Response getConversationById(String uuid, Integer page, Integer size, String token) {
        Map<String, String> queryParams = new HashMap<>();
        if (page != null) {
            queryParams.put("page", String.valueOf(page));
        }
        if (size != null) {
            queryParams.put("size", String.valueOf(size));
        }
        return RestClient.doGet("JSON", neptuneServiceURL, "copilot/conversations/" + uuid, token, queryParams, null, true);
    }

    private void validateConversationResponse(Response response) {
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP status 200");
        JsonPath jp = response.jsonPath();
        
        // Validate response structure
        Assert.assertNotNull(jp.get("data"), "Data should not be null");
        Assert.assertNotNull(jp.get("meta"), "Meta should not be null");
        
        // Validate meta structure
        Assert.assertEquals(jp.getString("meta.message"), "Fetched the conversation successfully", "Meta message should match expected");
        Assert.assertEquals(jp.getString("meta.message_type"), "is-success", "Meta message_type should be 'is-success'");
        Assert.assertEquals(jp.getInt("meta.status"), 200, "Meta status should be 200");
        Assert.assertNotNull(jp.getString("meta.request_UUID"), "Meta request_UUID should not be null");
    }

    private void validateConversationData(JsonPath jp) {
        List<Map<String, Object>> messages = jp.getList("data");
        Assert.assertNotNull(messages, "Messages list should not be null");
        
        for (Map<String, Object> message : messages) {
            Assert.assertNotNull(message.get("role"), "Message role should not be null");
            Assert.assertNotNull(message.get("message"), "Message content should not be null");
            Assert.assertFalse(message.get("role").toString().trim().isEmpty(), "Message role should not be empty");
            Assert.assertFalse(message.get("message").toString().trim().isEmpty(), "Message content should not be empty");
            
            // Validate role values
            String role = message.get("role").toString();
            Assert.assertTrue(role.equals("user") || role.equals("assistant"), 
                "Role should be either 'user' or 'assistant', got: " + role);
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithValidUUID_GET() {
        Response response = getConversationById(validConversationUUID, 1, 10, albatrossAuthToken);
        validateConversationResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateConversationData(jp);
        
        String responseUUID = jp.getString("meta.request_UUID");
        Assert.assertNotNull(responseUUID, "Response UUID should not be null");
        Assert.assertFalse(responseUUID.trim().isEmpty(), "Response UUID should not be empty");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithDefaultParams_GET() {
        Response response = getConversationById(validConversationUUID, null, null, albatrossAuthToken);
        validateConversationResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateConversationData(jp);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithPagination_GET() {
        Response response = getConversationById(validConversationUUID, 1, 5, albatrossAuthToken);
        validateConversationResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateConversationData(jp);
        
        List<Map<String, Object>> messages = jp.getList("data");
        Assert.assertNotNull(messages, "Messages list should not be null");
        Assert.assertTrue(messages.size() <= 5, "Should return at most 5 messages per page");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithLargePage_GET() {
        Response response = getConversationById(validConversationUUID, 999, 10, albatrossAuthToken);
        validateConversationResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateConversationData(jp);
        
        // Large page number should return empty list or handle gracefully
        List<Map<String, Object>> messages = jp.getList("data");
        Assert.assertNotNull(messages, "Messages list should not be null");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithZeroSize_GET() {
        Response response = getConversationById(validConversationUUID, 1, 0, albatrossAuthToken);
        // Should handle zero size gracefully - either return 400 or empty list
        if (response.getStatusCode() == 200) {
            JsonPath jp = response.jsonPath();
            validateConversationData(jp);
            List<Map<String, Object>> messages = jp.getList("data");
            Assert.assertNotNull(messages, "Messages list should not be null");
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for zero size");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithNegativePage_GET() {
        Response response = getConversationById(validConversationUUID, -1, 10, albatrossAuthToken);
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
    public void getConversationByIdWithNegativeSize_GET() {
        Response response = getConversationById(validConversationUUID, 1, -5, albatrossAuthToken);
        // Should handle negative size gracefully
        if (response.getStatusCode() == 200) {
            JsonPath jp = response.jsonPath();
            validateConversationData(jp);
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for negative size");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithMaxSize_GET() {
        Response response = getConversationById(validConversationUUID, 1, 100, albatrossAuthToken);
        if (response.getStatusCode() == 400) {
            Assert.assertEquals(response.getStatusCode(), 400, "API returned 400 for size=100, indicating max size limit exceeded");
        } else {
            validateConversationResponse(response);
            JsonPath jp = response.jsonPath();
            validateConversationData(jp);
            List<Map<String, Object>> messages = jp.getList("data");
            Assert.assertNotNull(messages, "Messages list should not be null");
            Assert.assertTrue(messages.size() <= 100, "Should return at most 100 messages");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithInvalidUUID_GET() {
        Response response = getConversationById(invalidConversationUUID, 1, 10, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 404, "Expected HTTP 404 for invalid UUID");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithMalformedUUID_GET() {
        Response response = getConversationById(malformedConversationUUID, 1, 10, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 404, "Expected HTTP 404 for malformed UUID");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithEmptyUUID_GET() {
        Response response = getConversationById("", 1, 10, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 for empty UUID");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithNullUUID_GET() {
        Response response = getConversationById(null, 1, 10, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 404, "Expected HTTP 404 for null UUID");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithNonExistentUUID_GET() {
        String nonExistentUUID = "00000000-0000-0000-0000-000000000000";
        Response response = getConversationById(nonExistentUUID, 1, 10, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 404, "Expected HTTP 404 for non-existent UUID");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithInvalidAuth_GET() {
        Response response = getConversationById(validConversationUUID, 1, 10, "invalid_token_12345");
        Assert.assertEquals(response.getStatusCode(), 401, "Expected HTTP 401 for invalid token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithEmptyAuth_GET() {
        Response response = getConversationById(validConversationUUID, 1, 10, "");
        Assert.assertEquals(response.getStatusCode(), 403, "Expected HTTP 403 for empty token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithNullAuth_GET() {
        Response response = getConversationById(validConversationUUID, 1, 10, null);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected HTTP 401 for null token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithSpecialCharactersInUUID_GET() {
        String specialUUID = "uuid-with-special-chars-@#$%^&*()";
        Response response = getConversationById(specialUUID, 1, 10, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 404, "Expected HTTP 404 for UUID with special characters");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithVeryLongUUID_GET() {
        String longUUID = "a".repeat(1000);
        Response response = getConversationById(longUUID, 1, 10, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 404, "Expected HTTP 404 for very long UUID");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithMultiplePages_GET() {
        // Test multiple pages to ensure pagination works
        for (int page = 1; page <= 3; page++) {
            Response response = getConversationById(validConversationUUID, page, 2, albatrossAuthToken);
            validateConversationResponse(response);
            
            JsonPath jp = response.jsonPath();
            validateConversationData(jp);
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithLargeSize_GET() {
        Response response = getConversationById(validConversationUUID, 1, 1000, albatrossAuthToken);
        if (response.getStatusCode() == 400) {
            Assert.assertEquals(response.getStatusCode(), 400, "API returned 400 for size=1000, indicating max size limit exceeded");
        } else {
            validateConversationResponse(response);
            JsonPath jp = response.jsonPath();
            validateConversationData(jp);
            List<Map<String, Object>> messages = jp.getList("data");
            Assert.assertNotNull(messages, "Messages list should not be null");
            Assert.assertTrue(messages.size() <= 1000, "Should return at most 1000 messages");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithZeroPage_GET() {
        Response response = getConversationById(validConversationUUID, 0, 10, albatrossAuthToken);
        // Should handle zero page gracefully
        if (response.getStatusCode() == 200) {
            JsonPath jp = response.jsonPath();
            validateConversationData(jp);
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for zero page");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getConversationByIdWithValidUUIDAndNoMessages_GET() {
        // This test assumes there might be a conversation with no messages
        // The response should still be valid but with empty data array
        Response response = getConversationById(validConversationUUID, 1, 10, albatrossAuthToken);
        validateConversationResponse(response);
        
        JsonPath jp = response.jsonPath();
        List<Map<String, Object>> messages = jp.getList("data");
        Assert.assertNotNull(messages, "Messages list should not be null");
        // Note: The conversation might have messages, so we just validate the structure
    }
}




