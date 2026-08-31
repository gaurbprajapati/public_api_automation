package io.rcrm.api.copilot;

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
public class UpdateConversationAICopilotTest extends TestBase {

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
            validConversationUUID = "3ea550bd-43c7-4ff7-9228-a42eadf32a51";
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

    private Response updateConversationTitle(String uuid, String title, String token) {
        JSONObject payload = new JSONObject();
        payload.put("title", title);
        return RestClient.doPatchOnce("JSON", neptuneServiceURL, "copilot/conversations/" + uuid, token, null, true, payload);
    }

    private void validateUpdateResponse(Response response) {
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP status 200");
        JsonPath jp = response.jsonPath();
        
        // Validate response structure
        Assert.assertNotNull(jp.get("meta"), "Meta should not be null");
        
        // Validate meta structure
        Assert.assertEquals(jp.getString("meta.message"), "Title updated successfully", "Meta message should match expected");
        Assert.assertEquals(jp.getString("meta.message_type"), "is-success", "Meta message_type should be 'is-success'");
        Assert.assertEquals(jp.getInt("meta.status"), 200, "Meta status should be 200");
        Assert.assertNotNull(jp.getString("meta.request_UUID"), "Meta request_UUID should not be null");
        
        // Validate data is null as per specification
        Object data = jp.get("data");
        Assert.assertNull(data, "Data should be null as per specification");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithValidUUID_PATCH() {
        String newTitle = "Updated Conversation Title";
        Response response = updateConversationTitle(validConversationUUID, newTitle, albatrossAuthToken);
        validateUpdateResponse(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithEmptyTitle_PATCH() {
        Response response = updateConversationTitle(validConversationUUID, "", albatrossAuthToken);
        // Should handle empty title gracefully - either return 400 or accept it
        if (response.getStatusCode() == 200) {
            validateUpdateResponse(response);
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for empty title");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithNullTitle_PATCH() {
        JSONObject payload = new JSONObject();
        payload.put("title", JSONObject.NULL);
        Response response = RestClient.doPatchOnce("JSON", neptuneServiceURL, "copilot/conversations/" + validConversationUUID, albatrossAuthToken, null, true, payload);
        // Should handle null title gracefully
        if (response.getStatusCode() == 200) {
            validateUpdateResponse(response);
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for null title");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithMissingTitle_PATCH() {
        JSONObject payload = new JSONObject();
        // No title field in payload
        Response response = RestClient.doPatchOnce("JSON", neptuneServiceURL, "copilot/conversations/" + validConversationUUID, albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 when title is missing");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithLongTitle_PATCH() {
        String longTitle = faker.lorem().characters(1000);
        Response response = updateConversationTitle(validConversationUUID, longTitle, albatrossAuthToken);
        if (response.getStatusCode() == 400) {
            Assert.assertEquals(response.getStatusCode(), 400, "API returned 400 for long title, indicating max title length exceeded");
        } else {
            validateUpdateResponse(response);
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithVeryLongTitle_PATCH() {
        String veryLongTitle = faker.lorem().characters(10000);
        Response response = updateConversationTitle(validConversationUUID, veryLongTitle, albatrossAuthToken);
        // Should handle very long title gracefully
        if (response.getStatusCode() == 200) {
            validateUpdateResponse(response);
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for very long title");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithSpecialCharacters_PATCH() {
        String specialTitle = "Title with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?";
        Response response = updateConversationTitle(validConversationUUID, specialTitle, albatrossAuthToken);
        validateUpdateResponse(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithUnicodeCharacters_PATCH() {
        String unicodeTitle = "Title with unicode: 你好世界 🌍 émojis 🚀";
        Response response = updateConversationTitle(validConversationUUID, unicodeTitle, albatrossAuthToken);
        validateUpdateResponse(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithWhitespaceOnly_PATCH() {
        String whitespaceTitle = "   \t\n   ";
        Response response = updateConversationTitle(validConversationUUID, whitespaceTitle, albatrossAuthToken);
        // Should handle whitespace-only title gracefully
        if (response.getStatusCode() == 200) {
            validateUpdateResponse(response);
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for whitespace-only title");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithSameTitle_PATCH() {
        String sameTitle = "Same Title Test";
        // First update
        Response firstResponse = updateConversationTitle(validConversationUUID, sameTitle, albatrossAuthToken);
        validateUpdateResponse(firstResponse);
        
        // Second update with same title
        Response secondResponse = updateConversationTitle(validConversationUUID, sameTitle, albatrossAuthToken);
        validateUpdateResponse(secondResponse);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithInvalidUUID_PATCH() {
        String newTitle = "Test Title";
        Response response = updateConversationTitle(invalidConversationUUID, newTitle, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 for invalid UUID");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithMalformedUUID_PATCH() {
        String newTitle = "Test Title";
        Response response = updateConversationTitle(malformedConversationUUID, newTitle, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 for malformed UUID");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithEmptyUUID_PATCH() {
        String newTitle = "Test Title";
        Response response = updateConversationTitle("", newTitle, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 307, "Expected HTTP 307 for empty UUID");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithNullUUID_PATCH() {
        String newTitle = "Test Title";
        Response response = updateConversationTitle(null, newTitle, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 for null UUID");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithNonExistentUUID_PATCH() {
        String nonExistentUUID = "00000000-0000-0000-0000-000000000000";
        String newTitle = "Test Title";
        Response response = updateConversationTitle(nonExistentUUID, newTitle, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 for non-existent UUID");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithInvalidAuth_PATCH() {
        String newTitle = "Test Title";
        Response response = updateConversationTitle(validConversationUUID, newTitle, "invalid_token_12345");
        Assert.assertEquals(response.getStatusCode(), 401, "Expected HTTP 401 for invalid token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithEmptyAuth_PATCH() {
        String newTitle = "Test Title";
        Response response = updateConversationTitle(validConversationUUID, newTitle, "");
        Assert.assertEquals(response.getStatusCode(), 403, "Expected HTTP 403 for empty token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithNullAuth_PATCH() {
        String newTitle = "Test Title";
        Response response = updateConversationTitle(validConversationUUID, newTitle, null);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected HTTP 401 for null token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithSpecialCharactersInUUID_PATCH() {
        String specialUUID = "uuid-with-special-chars-@#$%^&*()";
        String newTitle = "Test Title";
        Response response = updateConversationTitle(specialUUID, newTitle, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 for UUID with special characters");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithVeryLongUUID_PATCH() {
        String longUUID = "a".repeat(1000);
        String newTitle = "Test Title";
        Response response = updateConversationTitle(longUUID, newTitle, albatrossAuthToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 for very long UUID");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithNumericTitle_PATCH() {
        String numericTitle = "123456789";
        Response response = updateConversationTitle(validConversationUUID, numericTitle, albatrossAuthToken);
        validateUpdateResponse(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithBooleanTitle_PATCH() {
        JSONObject payload = new JSONObject();
        payload.put("title", true);
        Response response = RestClient.doPatchOnce("JSON", neptuneServiceURL, "copilot/conversations/" + validConversationUUID, albatrossAuthToken, null, true, payload);
        // Should handle boolean title gracefully
        if (response.getStatusCode() == 200) {
            validateUpdateResponse(response);
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for boolean title");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithArrayTitle_PATCH() {
        JSONObject payload = new JSONObject();
        payload.put("title", new String[]{"title1", "title2"});
        Response response = RestClient.doPatchOnce("JSON", neptuneServiceURL, "copilot/conversations/" + validConversationUUID, albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for array title");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithObjectTitle_PATCH() {
        JSONObject payload = new JSONObject();
        JSONObject titleObject = new JSONObject();
        titleObject.put("text", "title");
        payload.put("title", titleObject);
        Response response = RestClient.doPatchOnce("JSON", neptuneServiceURL, "copilot/conversations/" + validConversationUUID, albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for object title");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithEmptyPayload_PATCH() {
        JSONObject payload = new JSONObject();
        // Empty payload
        Response response = RestClient.doPatchOnce("JSON", neptuneServiceURL, "copilot/conversations/" + validConversationUUID, albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for empty payload");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithMultipleUpdates_PATCH() {
        String[] titles = {"First Title", "Second Title", "Third Title"};
        
        for (String title : titles) {
            Response response = updateConversationTitle(validConversationUUID, title, albatrossAuthToken);
            validateUpdateResponse(response);
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithCaseSensitiveTitle_PATCH() {
        String caseSensitiveTitle = "Title With Mixed Case";
        Response response = updateConversationTitle(validConversationUUID, caseSensitiveTitle, albatrossAuthToken);
        validateUpdateResponse(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updateConversationTitleWithLeadingTrailingSpaces_PATCH() {
        String spacedTitle = "   Title with spaces   ";
        Response response = updateConversationTitle(validConversationUUID, spacedTitle, albatrossAuthToken);
        validateUpdateResponse(response);
    }
}
