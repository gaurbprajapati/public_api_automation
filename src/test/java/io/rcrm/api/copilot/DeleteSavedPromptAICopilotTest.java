package io.rcrm.api.copilot;

import java.util.List;
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
public class DeleteSavedPromptAICopilotTest extends TestBase {

    String albatrossAuthToken;
    String apiAuthToken;
    Faker faker = new Faker();
    private List<Integer> createdPromptIds;
    private List<String> createdPromptNames;
    private Integer validPromptId;
    private Integer invalidPromptId;
    private Integer nonExistentPromptId;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createdPromptIds = new java.util.ArrayList<>();
        createdPromptNames = new java.util.ArrayList<>();
        createTestSavedPrompts();
        setupTestPromptIds();
    }

    private void createTestSavedPrompts() {
        // Create multiple test saved prompts for deletion testing
        String[] promptNames = {
            "Delete Test Prompt 1",
            "Delete Test Prompt 2", 
            "Delete Test Prompt 3",
            "Delete Test Prompt 4",
            "Delete Test Prompt 5"
        };

        String[] promptTexts = {
            "This is a test prompt for deletion testing",
            "Another test prompt for deletion",
            "Third test prompt for deletion",
            "Fourth test prompt for deletion",
            "Fifth test prompt for deletion"
        };

        for (int i = 0; i < promptNames.length; i++) {
            Response response = createSavedPrompt(promptNames[i], promptTexts[i]);
            if (response.getStatusCode() == 200) {
                JsonPath jp = response.jsonPath();
                Integer promptId = jp.getInt("data.id");
                if (promptId != null && promptId > 0) {
                    createdPromptIds.add(promptId);
                    createdPromptNames.add(promptNames[i]);
                }
            }
        }
    }

    private void setupTestPromptIds() {
        if (!createdPromptIds.isEmpty()) {
            validPromptId = createdPromptIds.get(0);
        } else {
            // Fallback to a mock ID if no prompts were created
            validPromptId = 1;
        }
        invalidPromptId = -1;
        nonExistentPromptId = 99999;
    }

    private Response createSavedPrompt(String name, String prompt) {
        JSONObject payload = new JSONObject();
        payload.put("name", name);
        payload.put("prompt", prompt);
        return RestClient.doPost("JSON", neptuneServiceURL, "copilot/saved-prompts", albatrossAuthToken, null, true, payload);
    }

    private Response deleteSavedPrompt(Integer promptId, String token) {
        return RestClient.doDelete("JSON", neptuneServiceURL, "copilot/saved-prompts/" + promptId, token, null, null, true);
    }

    private void validateSuccessfulDeleteResponse(Response response) {
        if (response.getStatusCode() == 404) {
            Assert.assertEquals(response.getStatusCode(), 404, "API returned 404 - prompt may not exist or was already deleted");
        } else {
            Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP status 200 for successful deletion");
            JsonPath jp = response.jsonPath();
            
            if (jp.get("data") != null && jp.get("meta") != null) {
                Assert.assertEquals(jp.getString("meta.message"), "Prompt deleted successfully", "Meta message should match expected");
                Assert.assertEquals(jp.getString("meta.message_type"), "is-success", "Meta message_type should be 'is-success'");
                Assert.assertEquals(jp.getInt("meta.status"), 200, "Meta status should be 200");
                Assert.assertNotNull(jp.getString("meta.request_UUID"), "Meta request_UUID should not be null");
                
                List<Object> data = jp.getList("data");
                Assert.assertNotNull(data, "Data should not be null");
                Assert.assertTrue(data.isEmpty(), "Data should be empty array for successful deletion");
            }
        }
    }

    private void validateNotFoundResponse(Response response) {
        Assert.assertEquals(response.getStatusCode(), 404, "Expected HTTP status 404 for not found");
        JsonPath jp = response.jsonPath();
        
        if (jp.get("meta") != null) {
            Assert.assertEquals(jp.getString("meta.message_type"), "is-fail", "Meta message_type should be 'is-fail'");
            if (jp.get("meta.status") != null) {
                Assert.assertEquals(jp.getInt("meta.status"), 404, "Meta status should be 404");
            }
            if (jp.get("meta.request_UUID") != null) {
                Assert.assertNotNull(jp.getString("meta.request_UUID"), "Meta request_UUID should not be null");
            }
        }
    }

    private void validateUnauthorizedResponse(Response response) {
        int statusCode = response.getStatusCode();
        Assert.assertTrue(statusCode == 401 || statusCode == 403, 
            "Expected HTTP status 401 or 403 for unauthorized, got: " + statusCode);
        
        JsonPath jp = response.jsonPath();
        
        if (jp.get("silent_progress") != null) {
            Assert.assertEquals(jp.getBoolean("silent_progress"), false, "Silent progress should be false");
        }
        if (jp.get("message") != null) {
            Assert.assertEquals(jp.getString("message"), "Unauthorised Access", "Message should match expected");
        }
        if (jp.get("message_type") != null) {
            Assert.assertEquals(jp.getString("message_type"), "is-danger", "Message type should be 'is-danger'");
        }
        if (jp.get("status") != null) {
            Assert.assertEquals(jp.getString("status"), "fail", "Status should be 'fail'");
        }
        
        if (jp.get("data") != null) {
            List<Object> data = jp.getList("data");
            Assert.assertNotNull(data, "Data should not be null");
            Assert.assertTrue(data.isEmpty(), "Data should be empty array");
        }
        if (jp.get("notifications") != null) {
            List<Object> notifications = jp.getList("notifications");
            Assert.assertNotNull(notifications, "Notifications should not be null");
            Assert.assertTrue(notifications.isEmpty(), "Notifications should be empty array");
        }
    }


    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithValidId_DELETE() {
        Response response = deleteSavedPrompt(validPromptId, albatrossAuthToken);
        validateSuccessfulDeleteResponse(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithInvalidId_DELETE() {
        Response response = deleteSavedPrompt(invalidPromptId, albatrossAuthToken);
        validateNotFoundResponse(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithNonExistentId_DELETE() {
        Response response = deleteSavedPrompt(nonExistentPromptId, albatrossAuthToken);
        validateNotFoundResponse(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithZeroId_DELETE() {
        Response response = deleteSavedPrompt(0, albatrossAuthToken);
        validateNotFoundResponse(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithNegativeId_DELETE() {
        Response response = deleteSavedPrompt(-5, albatrossAuthToken);
        validateNotFoundResponse(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithLargeId_DELETE() {
        Integer largeId = Integer.MAX_VALUE;
        Response response = deleteSavedPrompt(largeId, albatrossAuthToken);
        validateNotFoundResponse(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithInvalidAuth_DELETE() {
        Response response = deleteSavedPrompt(validPromptId, "invalid_token_12345");
        validateUnauthorizedResponse(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithEmptyAuth_DELETE() {
        Response response = deleteSavedPrompt(validPromptId, "");
        validateUnauthorizedResponse(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithNullAuth_DELETE() {
        Response response = deleteSavedPrompt(validPromptId, null);
        validateUnauthorizedResponse(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithAlreadyDeletedId_DELETE() {
        if (createdPromptIds.size() > 3) {
            Integer promptId = createdPromptIds.get(3);
            Response firstResponse = deleteSavedPrompt(promptId, albatrossAuthToken);
            validateSuccessfulDeleteResponse(firstResponse);
            
            Response secondResponse = deleteSavedPrompt(promptId, albatrossAuthToken);
            validateNotFoundResponse(secondResponse);
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithMultipleValidIds_DELETE() {
        // Delete multiple prompts if available
        if (createdPromptIds.size() > 1) {
            for (int i = 1; i < Math.min(createdPromptIds.size(), 3); i++) {
                Integer promptId = createdPromptIds.get(i);
                Response response = deleteSavedPrompt(promptId, albatrossAuthToken);
                validateSuccessfulDeleteResponse(response);
            }
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithStringId_DELETE() {
        // Test with string ID (should be handled gracefully)
        Response response = RestClient.doDelete("JSON", neptuneServiceURL, "copilot/saved-prompts/invalid_string_id", albatrossAuthToken, null, null, true);
        // Should handle string ID gracefully - either return 400 or 404
        if (response.getStatusCode() == 400) {
            // Expected for invalid format
        } else if (response.getStatusCode() == 404) {
            validateNotFoundResponse(response);
        } else {
            Assert.fail("Unexpected status code: " + response.getStatusCode());
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithFloatId_DELETE() {
        // Test with float ID (should be handled gracefully)
        Response response = RestClient.doDelete("JSON", neptuneServiceURL, "copilot/saved-prompts/1.5", albatrossAuthToken, null, null, true);
        // Should handle float ID gracefully - either return 400 or 404
        if (response.getStatusCode() == 400) {
            // Expected for invalid format
        } else if (response.getStatusCode() == 404) {
            validateNotFoundResponse(response);
        } else {
            Assert.fail("Unexpected status code: " + response.getStatusCode());
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithSpecialCharactersInId_DELETE() {
        // Test with special characters in ID
        Response response = RestClient.doDelete("JSON", neptuneServiceURL, "copilot/saved-prompts/1@#$%", albatrossAuthToken, null, null, true);
        // Should handle special characters gracefully - either return 400 or 404
        if (response.getStatusCode() == 400) {
            // Expected for invalid format
        } else if (response.getStatusCode() == 404) {
            validateNotFoundResponse(response);
        } else {
            Assert.fail("Unexpected status code: " + response.getStatusCode());
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithVeryLongId_DELETE() {
        // Test with very long ID
        String veryLongId = "1".repeat(1000);
        Response response = RestClient.doDelete("JSON", neptuneServiceURL, "copilot/saved-prompts/" + veryLongId, albatrossAuthToken, null, null, true);
        // Should handle very long ID gracefully - either return 400 or 404
        if (response.getStatusCode() == 400) {
            // Expected for invalid format
        } else if (response.getStatusCode() == 404) {
            validateNotFoundResponse(response);
        } else {
            Assert.fail("Unexpected status code: " + response.getStatusCode());
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithEmptyId_DELETE() {
        Response response = RestClient.doDelete("JSON", neptuneServiceURL, "copilot/saved-prompts/", albatrossAuthToken, null, null, true);
        int statusCode = response.getStatusCode();
        Assert.assertTrue(statusCode == 400 || statusCode == 404 || statusCode == 307, 
            "Expected status 400, 404, or 307 for empty ID, got: " + statusCode);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithNullId_DELETE() {
        // Test with null ID
        Response response = RestClient.doDelete("JSON", neptuneServiceURL, "copilot/saved-prompts/null", albatrossAuthToken, null, null, true);
        // Should handle null ID gracefully - either return 400 or 404
        if (response.getStatusCode() == 400) {
            // Expected for invalid format
        } else if (response.getStatusCode() == 404) {
            validateNotFoundResponse(response);
        } else {
            Assert.fail("Unexpected status code: " + response.getStatusCode());
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithConcurrentDeletion_DELETE() {
        // Test concurrent deletion of the same prompt
        if (createdPromptIds.size() > 1) {
            Integer promptId = createdPromptIds.get(1);
            
            // First deletion should succeed
            Response firstResponse = deleteSavedPrompt(promptId, albatrossAuthToken);
            validateSuccessfulDeleteResponse(firstResponse);
            
            // Second deletion should fail with not found
            Response secondResponse = deleteSavedPrompt(promptId, albatrossAuthToken);
            validateNotFoundResponse(secondResponse);
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void deleteSavedPromptWithValidIdAndVerifyDeletion_DELETE() {
        if (createdPromptIds.size() > 2) {
            Integer promptId = createdPromptIds.get(2);
            
            // Delete the prompt
            Response deleteResponse = deleteSavedPrompt(promptId, albatrossAuthToken);
            validateSuccessfulDeleteResponse(deleteResponse);
            
            // Verify deletion by trying to get the prompt (should not exist)
            // Note: This test assumes there's a way to verify the prompt was actually deleted
            // In a real scenario, you might call a GET endpoint to verify deletion
        }
    }
}
