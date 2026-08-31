package io.recruitcrm.albatross.neptune;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

import java.util.List;
import java.util.Map;

@AccountType("Business|AlbatrossTkn")
public class CandidateSubmissionAgentSettingsTest extends TestBase {

    String albatrossAuthToken;

    @BeforeClass
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
    }

    private void assertSuccessMessageType(String actual) {
        Assert.assertTrue("is-success".equals(actual) || "success".equals(actual),
                "message_type should be 'is-success' or 'success', got: " + actual);
    }

    private void assertDangerMessageType(String actual) {
        Assert.assertTrue("is-danger".equals(actual) || "danger".equals(actual),
                "message_type should be 'is-danger' or 'danger', got: " + actual);
    }

    private void assertMetaFields(JsonPath jsonPath) {
        Assert.assertNotNull(jsonPath.get("meta.request_UUID"), "Meta should contain request_UUID");
        Assert.assertNotNull(jsonPath.get("meta.message"), "Meta should contain message");
        Assert.assertNotNull(jsonPath.get("meta.message_type"), "Meta should contain message_type");
        Assert.assertNotNull(jsonPath.get("meta.status"), "Meta should contain status");
        Assert.assertEquals(jsonPath.getInt("meta.status"), 200, "meta.status should be 200");
    }

    private void assertEmptyData(JsonPath jsonPath) {
        Object data = jsonPath.get("data");
        Assert.assertNotNull(data, "data field should be present");
        if (data instanceof List) {
            Assert.assertTrue(((List<?>) data).isEmpty(),
                    "data should be an empty list when save is rejected");
            return;
        }
        if (data instanceof Map) {
            Assert.assertTrue(((Map<?, ?>) data).isEmpty(),
                    "data should be an empty object when save is rejected");
            return;
        }
        if (data instanceof String) {
            Assert.assertTrue(((String) data).trim().isEmpty(),
                    "data should be an empty string when save is rejected");
            return;
        }
        Assert.fail("Unexpected data type for empty-data assertion: " + data.getClass().getName());
    }

    private void assertLogoFlag(Object value, String fieldName) {
        Assert.assertNotNull(value, fieldName + " should not be null");
        int flag = Integer.parseInt(value.toString());
        Assert.assertTrue(flag == 0 || flag == 1,
                fieldName + " should be 0 or 1, got: " + flag);
    }

    private Map<String, Object> getFirstSetting() {
        Response response = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "GET settings should return 200");
        Map<String, Object> setting = response.jsonPath().getMap("data[0]");
        Assert.assertNotNull(setting, "Should have at least one settings entry");
        return setting;
    }

    // ==================== GET Settings ====================

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGetSubmissionSettings_HappyPath() {
        Response response = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK for get settings");

        JsonPath jsonPath = response.jsonPath();
        Object data = jsonPath.get("data");
        Assert.assertTrue(data instanceof List, "data should be a list");
        Assert.assertFalse(((List<?>) data).isEmpty(), "data should have at least one settings entry");

        assertMetaFields(jsonPath);
        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGetSubmissionSettings_ResponseStructure() {
        Response response = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true);

        Assert.assertEquals(response.getStatusCode(), 200);

        Map<String, Object> setting = response.jsonPath().getMap("data[0]");
        Assert.assertNotNull(setting, "Should have at least one settings entry");

        Assert.assertTrue(setting.containsKey("account_id"), "Should contain account_id");
        Assert.assertTrue(setting.containsKey("instruction_prompt"), "Should contain instruction_prompt");
        Assert.assertTrue(setting.containsKey("include_company_logo"), "Should contain include_company_logo");
        Assert.assertTrue(setting.containsKey("include_client_logo"), "Should contain include_client_logo");

        Assert.assertNotNull(setting.get("account_id"), "account_id should not be null");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGetSubmissionSettings_DefaultResponseFields() {
        Map<String, Object> setting = getFirstSetting();

        assertLogoFlag(setting.get("include_company_logo"), "include_company_logo");
        assertLogoFlag(setting.get("include_client_logo"), "include_client_logo");

        Assert.assertTrue(setting.containsKey("instruction_prompt"),
                "instruction_prompt field should exist in response");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGetSubmissionSettings_InvalidToken() {
        Response response = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-candidate-submission-agent-settings",
                "InvalidToken", null, null, true);

        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 Unauthorized for invalid token");
        Assert.assertEquals(response.jsonPath().getString("error"), "Unauthorized",
                "Error message should be Unauthorized");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGetSubmissionSettings_WrongHttpMethod() {
        JSONObject emptyBody = new JSONObject();
        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/get-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true, emptyBody);

        Assert.assertEquals(response.getStatusCode(), 405,
                "Expected 405 Method Not Allowed for POST on a GET endpoint");
    }

    // ==================== SAVE Settings ====================

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySaveSubmissionSettings_HappyPath() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Generate a professional candidate submission email for testing.");
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_client_logo", 1);

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK for save settings");

        JsonPath jsonPath = response.jsonPath();
        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
        Assert.assertTrue(jsonPath.getString("meta.message").toLowerCase().contains("saved"),
                "Success message should confirm settings saved");
        assertMetaFields(jsonPath);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySaveSubmissionSettings_WithLogosDisabled() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Minimal design, no logos.");
        requestBody.put("include_company_logo", 0);
        requestBody.put("include_client_logo", 0);

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK with logos disabled");
        assertSuccessMessageType(response.jsonPath().getString("meta.message_type"));

        Response getResponse = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true);
        Assert.assertEquals(getResponse.getStatusCode(), 200);

        JsonPath jsonPath = getResponse.jsonPath();
        Assert.assertEquals(Integer.parseInt(jsonPath.get("data[0].include_company_logo").toString()), 0,
                "include_company_logo should persist as 0");
        Assert.assertEquals(Integer.parseInt(jsonPath.get("data[0].include_client_logo").toString()), 0,
                "include_client_logo should persist as 0");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySaveSubmissionSettings_WithCustomPrompt() {
        String customPrompt = "Name: Forest & Cream\n\nColor Palette\n\n"
                + "Background: #FAFAF7\nPrimary text: #1a1a1a\nAccent: #1e4d3b";

        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", customPrompt);
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_client_logo", 0);

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200);
        assertSuccessMessageType(response.jsonPath().getString("meta.message_type"));

        Response getResponse = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true);

        Assert.assertEquals(getResponse.getStatusCode(), 200);
        String savedPrompt = getResponse.jsonPath().getString("data[0].instruction_prompt");
        Assert.assertNotNull(savedPrompt, "Instruction prompt should be saved");
        Assert.assertTrue(savedPrompt.contains("Forest & Cream"),
                "Saved prompt should contain the custom text");
        Assert.assertEquals(Integer.parseInt(getResponse.jsonPath().get("data[0].include_client_logo").toString()), 0,
                "include_client_logo should persist as 0");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySaveSubmissionSettings_VerifySaveAndRetrieve() {
        String testPrompt = "Automated round-trip test prompt - candidate submission agent "
                + System.currentTimeMillis();

        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", testPrompt);
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_client_logo", 1);

        Response saveResponse = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(saveResponse.getStatusCode(), 200);
        assertSuccessMessageType(saveResponse.jsonPath().getString("meta.message_type"));

        Response getResponse = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true);

        Assert.assertEquals(getResponse.getStatusCode(), 200);

        JsonPath jsonPath = getResponse.jsonPath();
        String savedPrompt = jsonPath.getString("data[0].instruction_prompt");
        Assert.assertNotNull(savedPrompt, "Instruction prompt should be persisted");
        Assert.assertTrue(savedPrompt.contains("round-trip test prompt"),
                "Saved prompt should match what was sent");
        Assert.assertEquals(Integer.parseInt(jsonPath.get("data[0].include_company_logo").toString()), 1,
                "include_company_logo should persist as 1");
        Assert.assertEquals(Integer.parseInt(jsonPath.get("data[0].include_client_logo").toString()), 1,
                "include_client_logo should persist as 1");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySaveSubmissionSettings_InvalidToken() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Test prompt");
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_client_logo", 1);

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-candidate-submission-agent-settings",
                "InvalidToken", null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 Unauthorized for invalid token");
        Assert.assertEquals(response.jsonPath().getString("error"), "Unauthorized",
                "Error message should be Unauthorized");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySaveSubmissionSettings_EmptyBody() {
        JSONObject requestBody = new JSONObject();

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "API returns 200 for invalid save payload per Albatross convention");
        JsonPath jsonPath = response.jsonPath();
        assertEmptyData(jsonPath);
        assertDangerMessageType(jsonPath.getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySaveSubmissionSettings_NullPrompt() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", JSONObject.NULL);
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_client_logo", 1);

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Null prompt with valid logo flags should succeed (prompt is optional)");
        assertSuccessMessageType(response.jsonPath().getString("meta.message_type"));

        Response getResponse = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true);
        Assert.assertEquals(getResponse.getStatusCode(), 200);

        JsonPath jsonPath = getResponse.jsonPath();
        Assert.assertEquals(Integer.parseInt(jsonPath.get("data[0].include_company_logo").toString()), 1,
                "include_company_logo should persist when prompt is null");
        Assert.assertEquals(Integer.parseInt(jsonPath.get("data[0].include_client_logo").toString()), 1,
                "include_client_logo should persist when prompt is null");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySaveSubmissionSettings_InvalidLogoValues() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Test prompt with invalid logo values for boundary testing.");
        requestBody.put("include_company_logo", 99);
        requestBody.put("include_client_logo", -1);

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "API returns 200 for invalid logo values per Albatross convention");
        JsonPath jsonPath = response.jsonPath();
        assertEmptyData(jsonPath);
        assertDangerMessageType(jsonPath.getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySaveSubmissionSettings_GetMethodNotAllowed() {
        Response response = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/save-candidate-submission-agent-settings",
                albatrossAuthToken, null, null, true);

        Assert.assertEquals(response.getStatusCode(), 405,
                "Expected 405 Method Not Allowed for GET on a POST endpoint");
    }
}
