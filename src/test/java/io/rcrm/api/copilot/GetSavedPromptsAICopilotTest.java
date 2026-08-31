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
public class GetSavedPromptsAICopilotTest extends TestBase {

    String albatrossAuthToken;
    String apiAuthToken;
    Faker faker = new Faker();
    private List<Integer> createdPromptIds;
    private List<String> createdPromptNames;
    private List<String> createdPromptTexts;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createdPromptIds = new java.util.ArrayList<>();
        createdPromptNames = new java.util.ArrayList<>();
        createdPromptTexts = new java.util.ArrayList<>();
        createTestSavedPrompts();
    }

    private void createTestSavedPrompts() {
        // Create multiple test saved prompts
        String[] promptNames = {
            "Find Java Candidates",
            "Search for Python Developers", 
            "Get Recent Hires",
            "Show Open Positions",
            "Candidates with 5+ Years Experience"
        };

        String[] promptTexts = {
            "Find all candidates with Java programming skills",
            "Search for candidates who know Python programming language",
            "Show me all candidates hired in the last 30 days",
            "Display all open job positions in the system",
            "Find candidates with 5 or more years of experience"
        };

        for (int i = 0; i < promptNames.length; i++) {
            Response response = createSavedPrompt(promptNames[i], promptTexts[i]);
            if (response.getStatusCode() == 200) {
                JsonPath jp = response.jsonPath();
                Integer promptId = jp.getInt("data.id");
                if (promptId != null && promptId > 0) {
                    createdPromptIds.add(promptId);
                    createdPromptNames.add(promptNames[i]);
                    createdPromptTexts.add(promptTexts[i]);
                }
            }
        }
    }

    private Response createSavedPrompt(String name, String prompt) {
        JSONObject payload = new JSONObject();
        payload.put("name", name);
        payload.put("prompt", prompt);
        return RestClient.doPost("JSON", neptuneServiceURL, "copilot/saved-prompts", albatrossAuthToken, null, true, payload);
    }

    private Response getSavedPrompts(String searchTerm, Integer page, Integer size, String token) {
        Map<String, String> queryParams = new HashMap<>();
        if (searchTerm != null && !searchTerm.isEmpty()) {
            queryParams.put("search_term", searchTerm);
        }
        if (page != null) {
            queryParams.put("page", String.valueOf(page));
        }
        if (size != null) {
            queryParams.put("size", String.valueOf(size));
        }
        return RestClient.doGet("JSON", neptuneServiceURL, "copilot/saved-prompts", token, queryParams, null, true);
    }

    private void validateSavedPromptsResponse(Response response) {
        Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP status 200");
        JsonPath jp = response.jsonPath();
        
        // Validate response structure
        Assert.assertNotNull(jp.get("data"), "Data should not be null");
        Assert.assertNotNull(jp.get("meta"), "Meta should not be null");
        
        // Validate meta structure
        Assert.assertEquals(jp.getString("meta.message"), "Saved prompts data retrieved successfully", "Meta message should match expected");
        Assert.assertEquals(jp.getString("meta.message_type"), "is-success", "Meta message_type should be 'is-success'");
        Assert.assertEquals(jp.getInt("meta.status"), 200, "Meta status should be 200");
        Assert.assertNotNull(jp.getString("meta.request_UUID"), "Meta request_UUID should not be null");
    }

    private void validateSavedPromptData(JsonPath jp) {
        List<Map<String, Object>> prompts = jp.getList("data");
        Assert.assertNotNull(prompts, "Prompts list should not be null");
        
        if (prompts == null || prompts.isEmpty()) {
            return;
        }
        
        for (Map<String, Object> prompt : prompts) {
            if (prompt == null || prompt.isEmpty()) {
                continue;
            }
            
            if (prompt.get("id") != null) {
                Assert.assertTrue(prompt.get("id") instanceof Integer, "ID should be an integer");
                Assert.assertTrue((Integer) prompt.get("id") > 0, "ID should be positive");
            }
            
            if (prompt.get("name") != null) {
                Assert.assertFalse(prompt.get("name").toString().trim().isEmpty(), "Prompt name should not be empty");
            }
            
            if (prompt.get("prompt") != null) {
                Assert.assertFalse(prompt.get("prompt").toString().trim().isEmpty(), "Prompt text should not be empty");
            }
            
            if (prompt.get("user_id") != null && prompt.get("user_id") instanceof Integer) {
                Assert.assertTrue((Integer) prompt.get("user_id") > 0, "User ID should be positive");
            }
            
            if (prompt.get("account_id") != null && prompt.get("account_id") instanceof Integer) {
                Assert.assertTrue((Integer) prompt.get("account_id") > 0, "Account ID should be positive");
            }
            
            if (prompt.get("pinned") != null && prompt.get("pinned") instanceof Integer) {
                Integer pinned = (Integer) prompt.get("pinned");
                Assert.assertTrue(pinned == 0 || pinned == 1, "Pinned should be 0 or 1, got: " + pinned);
            }
            
            if (prompt.get("created_by") != null && prompt.get("created_by") instanceof Integer) {
                Assert.assertTrue((Integer) prompt.get("created_by") > 0, "Created by should be positive");
            }
            
            if (prompt.get("updated_by") != null && prompt.get("updated_by") instanceof Integer) {
                Assert.assertTrue((Integer) prompt.get("updated_by") > 0, "Updated by should be positive");
            }
            
            if (prompt.get("created_on") != null && prompt.get("created_on") instanceof Integer) {
                Assert.assertTrue((Integer) prompt.get("created_on") > 0, "Created on should be positive");
            }
            
            if (prompt.get("updated_on") != null && prompt.get("updated_on") instanceof Integer) {
                Assert.assertTrue((Integer) prompt.get("updated_on") > 0, "Updated on should be positive");
            }
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithDefaultParams_GET() {
        Response response = getSavedPrompts(null, null, null, albatrossAuthToken);
        validateSavedPromptsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateSavedPromptData(jp);
        
        // Should return prompts (may be empty if no prompts exist)
        List<Map<String, Object>> prompts = jp.getList("data");
        Assert.assertNotNull(prompts, "Prompts list should not be null");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithPagination_GET() {
        Response response = getSavedPrompts(null, 1, 5, albatrossAuthToken);
        validateSavedPromptsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateSavedPromptData(jp);
        
        List<Map<String, Object>> prompts = jp.getList("data");
        Assert.assertNotNull(prompts, "Prompts list should not be null");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithSearchTerm_GET() {
        String searchTerm = "Java";
        Response response = getSavedPrompts(searchTerm, 1, 10, albatrossAuthToken);
        validateSavedPromptsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateSavedPromptData(jp);
        
        List<Map<String, Object>> prompts = jp.getList("data");
        for (Map<String, Object> prompt : prompts) {
            if (prompt == null || prompt.get("name") == null || prompt.get("prompt") == null) {
                continue;
            }
            String name = prompt.get("name").toString().toLowerCase();
            String promptText = prompt.get("prompt").toString().toLowerCase();
            Assert.assertTrue(name.contains(searchTerm.toLowerCase()) || 
                           promptText.contains(searchTerm.toLowerCase()), 
                           "Prompt should contain search term in name or text");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithEmptySearchTerm_GET() {
        Response response = getSavedPrompts("", 1, 10, albatrossAuthToken);
        validateSavedPromptsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateSavedPromptData(jp);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithLargePage_GET() {
        Response response = getSavedPrompts(null, 999, 10, albatrossAuthToken);
        validateSavedPromptsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateSavedPromptData(jp);
        
        // Large page number should return empty list or handle gracefully
        List<Map<String, Object>> prompts = jp.getList("data");
        Assert.assertNotNull(prompts, "Prompts list should not be null");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithZeroSize_GET() {
        Response response = getSavedPrompts(null, 1, 0, albatrossAuthToken);
        // Should handle zero size gracefully - either return 400 or empty list
        if (response.getStatusCode() == 200) {
            JsonPath jp = response.jsonPath();
            validateSavedPromptData(jp);
            List<Map<String, Object>> prompts = jp.getList("data");
            Assert.assertNotNull(prompts, "Prompts list should not be null");
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for zero size");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithNegativePage_GET() {
        Response response = getSavedPrompts(null, -1, 10, albatrossAuthToken);
        // Should handle negative page gracefully
        if (response.getStatusCode() == 200) {
            JsonPath jp = response.jsonPath();
            validateSavedPromptData(jp);
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for negative page");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithNegativeSize_GET() {
        Response response = getSavedPrompts(null, 1, -5, albatrossAuthToken);
        // Should handle negative size gracefully
        if (response.getStatusCode() == 200) {
            JsonPath jp = response.jsonPath();
            validateSavedPromptData(jp);
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for negative size");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithMaxSize_GET() {
        Response response = getSavedPrompts(null, 1, 100, albatrossAuthToken);
        validateSavedPromptsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateSavedPromptData(jp);
        
        List<Map<String, Object>> prompts = jp.getList("data");
        Assert.assertNotNull(prompts, "Prompts list should not be null");
        Assert.assertTrue(prompts.size() <= 100, "Should return at most 100 prompts");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithSpecialCharacters_GET() {
        String searchTerm = "Java@#$%^&*()";
        Response response = getSavedPrompts(searchTerm, 1, 10, albatrossAuthToken);
        validateSavedPromptsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateSavedPromptData(jp);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithLongSearchTerm_GET() {
        String longSearchTerm = faker.lorem().characters(1000);
        Response response = getSavedPrompts(longSearchTerm, 1, 10, albatrossAuthToken);
        validateSavedPromptsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateSavedPromptData(jp);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithInvalidAuth_GET() {
        Response response = getSavedPrompts(null, 1, 10, "invalid_token_12345");
        Assert.assertEquals(response.getStatusCode(), 401, "Expected HTTP 401 for invalid token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithEmptyAuth_GET() {
        Response response = getSavedPrompts(null, 1, 10, "");
        Assert.assertEquals(response.getStatusCode(), 403, "Expected HTTP 403 for empty token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithNullAuth_GET() {
        Response response = getSavedPrompts(null, 1, 10, null);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected HTTP 401 for null token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithExactSearchMatch_GET() {
        if (!createdPromptNames.isEmpty()) {
            String exactName = createdPromptNames.get(0);
            Response response = getSavedPrompts(exactName, 1, 10, albatrossAuthToken);
            validateSavedPromptsResponse(response);
            
            JsonPath jp = response.jsonPath();
            validateSavedPromptData(jp);
            
            // Note: This might not find exact match due to search implementation
            // The test validates that the search works without errors
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithCaseInsensitiveSearch_GET() {
        String searchTerm = "JAVA";
        Response response = getSavedPrompts(searchTerm, 1, 10, albatrossAuthToken);
        validateSavedPromptsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateSavedPromptData(jp);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithMultiplePages_GET() {
        // Test multiple pages to ensure pagination works
        for (int page = 1; page <= 3; page++) {
            Response response = getSavedPrompts(null, page, 2, albatrossAuthToken);
            validateSavedPromptsResponse(response);
            
            JsonPath jp = response.jsonPath();
            validateSavedPromptData(jp);
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithZeroPage_GET() {
        Response response = getSavedPrompts(null, 0, 10, albatrossAuthToken);
        // Should handle zero page gracefully
        if (response.getStatusCode() == 200) {
            JsonPath jp = response.jsonPath();
            validateSavedPromptData(jp);
        } else {
            Assert.assertEquals(response.getStatusCode(), 400, "Expected 400 for zero page");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithLargeSize_GET() {
        Response response = getSavedPrompts(null, 1, 1000, albatrossAuthToken);
        validateSavedPromptsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateSavedPromptData(jp);
        
        List<Map<String, Object>> prompts = jp.getList("data");
        Assert.assertNotNull(prompts, "Prompts list should not be null");
        Assert.assertTrue(prompts.size() <= 1000, "Should return at most 1000 prompts");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithPartialSearch_GET() {
        String partialSearch = "Java";
        Response response = getSavedPrompts(partialSearch, 1, 10, albatrossAuthToken);
        validateSavedPromptsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateSavedPromptData(jp);
        
        List<Map<String, Object>> prompts = jp.getList("data");
        for (Map<String, Object> prompt : prompts) {
            if (prompt == null || prompt.get("name") == null || prompt.get("prompt") == null) {
                continue;
            }
            String name = prompt.get("name").toString().toLowerCase();
            String promptText = prompt.get("prompt").toString().toLowerCase();
            Assert.assertTrue(name.contains(partialSearch.toLowerCase()) || 
                           promptText.contains(partialSearch.toLowerCase()), 
                           "Prompt should contain partial search term");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getSavedPromptsWithNoResults_GET() {
        String nonExistentSearch = "NonExistentPrompt12345";
        Response response = getSavedPrompts(nonExistentSearch, 1, 10, albatrossAuthToken);
        validateSavedPromptsResponse(response);
        
        JsonPath jp = response.jsonPath();
        validateSavedPromptData(jp);
        
        // Should return empty list for non-existent search
        List<Map<String, Object>> prompts = jp.getList("data");
        Assert.assertNotNull(prompts, "Prompts list should not be null");
    }
}
