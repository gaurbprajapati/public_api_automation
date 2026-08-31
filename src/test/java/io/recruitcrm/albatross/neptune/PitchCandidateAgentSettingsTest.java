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

import java.util.Map;

@AccountType("Business|AlbatrossTkn")
public class PitchCandidateAgentSettingsTest extends TestBase {

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

    // ==================== GET Settings ====================

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGetPitchSettings_HappyPath() {
        Response response = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK for get pitch settings");

        JsonPath jsonPath = response.jsonPath();
        Assert.assertNotNull(jsonPath.get("data"), "Response should contain data field");
        Object data = jsonPath.get("data");
        Assert.assertTrue(data instanceof java.util.List, "data should be a list");
        java.util.List<?> dataList = (java.util.List<?>) data;
        Assert.assertTrue(dataList.size() > 0, "data should have at least one settings entry");

        Assert.assertNotNull(jsonPath.get("meta"), "Response should contain meta field");
        Assert.assertEquals(jsonPath.getInt("meta.status"), 200);
        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGetPitchSettings_ResponseStructure() {
        Response response = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);

        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jsonPath = response.jsonPath();
        Map<String, Object> setting = jsonPath.getMap("data[0]");
        Assert.assertNotNull(setting, "Should have at least one settings entry");

        Assert.assertTrue(setting.containsKey("account_id"), "Should contain account_id");
        Assert.assertTrue(setting.containsKey("instruction_prompt"), "Should contain instruction_prompt");
        Assert.assertTrue(setting.containsKey("include_company_logo"), "Should contain include_company_logo");
        Assert.assertTrue(setting.containsKey("include_contact_logo"), "Should contain include_contact_logo");
        Assert.assertTrue(setting.containsKey("include_email_signature"), "Should contain include_email_signature");
        Assert.assertTrue(setting.containsKey("preview_template"), "Should contain preview_template");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGetPitchSettings_DefaultResponse() {
        Response response = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);

        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jsonPath = response.jsonPath();
        Map<String, Object> setting = jsonPath.getMap("data[0]");

        Object companyLogo = setting.get("include_company_logo");
        Assert.assertNotNull(companyLogo, "include_company_logo should not be null");
        int companyLogoVal = Integer.parseInt(companyLogo.toString());
        Assert.assertTrue(companyLogoVal == 0 || companyLogoVal == 1,
                "include_company_logo should be 0 or 1, got: " + companyLogoVal);

        Object contactLogo = setting.get("include_contact_logo");
        Assert.assertNotNull(contactLogo, "include_contact_logo should not be null");
        int contactLogoVal = Integer.parseInt(contactLogo.toString());
        Assert.assertTrue(contactLogoVal == 0 || contactLogoVal == 1,
                "include_contact_logo should be 0 or 1, got: " + contactLogoVal);

        Object emailSignature = setting.get("include_email_signature");
        Assert.assertNotNull(emailSignature, "include_email_signature should not be null");
        int emailSigVal = Integer.parseInt(emailSignature.toString());
        Assert.assertTrue(emailSigVal == 0 || emailSigVal == 1,
                "include_email_signature should be 0 or 1, got: " + emailSigVal);

        Assert.assertTrue(setting.containsKey("instruction_prompt"),
                "instruction_prompt should exist in default response");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGetPitchSettings_MetaFields() {
        Response response = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);

        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jsonPath = response.jsonPath();
        Assert.assertNotNull(jsonPath.get("meta.request_UUID"), "Meta should contain request_UUID");
        Assert.assertNotNull(jsonPath.get("meta.message"), "Meta should contain message");
        Assert.assertNotNull(jsonPath.get("meta.message_type"), "Meta should contain message_type");
        Assert.assertNotNull(jsonPath.get("meta.status"), "Meta should contain status");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGetPitchSettings_InvalidToken() {
        Response response = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                "InvalidToken", null, null, true);

        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 Unauthorized for invalid token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGetPitchSettings_WrongHttpMethod() {
        JSONObject emptyBody = new JSONObject();
        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, emptyBody);

        Assert.assertEquals(response.getStatusCode(), 405,
                "Expected 405 Method Not Allowed for POST on a GET endpoint");
    }

    // ==================== SAVE Settings ====================

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_HappyPath() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Generate a professional pitch email with candidate grid layout for testing.");
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_contact_logo", 1);
        requestBody.put("include_email_signature", 1);

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK for save pitch settings");

        JsonPath jsonPath = response.jsonPath();
        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
        Assert.assertTrue(jsonPath.getString("meta.message").contains("saved successfully"),
                "Success message should confirm settings saved");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_WithLogosDisabled() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Minimal pitch design, no logos. Automated test.");
        requestBody.put("include_company_logo", 0);
        requestBody.put("include_contact_logo", 0);
        requestBody.put("include_email_signature", 1);

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK with logos disabled");
        assertSuccessMessageType(response.jsonPath().getString("meta.message_type"));

        Response getResponse = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);
        Assert.assertEquals(getResponse.getStatusCode(), 200);

        JsonPath jsonPath = getResponse.jsonPath();
        Assert.assertEquals(Integer.parseInt(jsonPath.get("data[0].include_company_logo").toString()), 0,
                "include_company_logo should persist as 0");
        Assert.assertEquals(Integer.parseInt(jsonPath.get("data[0].include_contact_logo").toString()), 0,
                "include_contact_logo should persist as 0");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_TalentGridPreset() {
        String talentGridPrompt = "Name: Talent Grid\n\nLayout: Candidate grid with header. "
                + "Professional blue accent. Clean minimal typography.";

        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", talentGridPrompt);
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_contact_logo", 1);
        requestBody.put("include_email_signature", 1);

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200);
        assertSuccessMessageType(response.jsonPath().getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_SaveAndRetrieveRoundTrip() {
        String testPrompt = "Automated round-trip test prompt - pitch candidate agent " + System.currentTimeMillis();

        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", testPrompt);
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_contact_logo", 0);
        requestBody.put("include_email_signature", 1);

        Response saveResponse = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);
        Assert.assertEquals(saveResponse.getStatusCode(), 200);

        Response getResponse = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);
        Assert.assertEquals(getResponse.getStatusCode(), 200);

        JsonPath jsonPath = getResponse.jsonPath();
        String savedPrompt = jsonPath.getString("data[0].instruction_prompt");
        Assert.assertNotNull(savedPrompt, "Instruction prompt should be persisted");
        Assert.assertTrue(savedPrompt.contains("round-trip test prompt"),
                "Saved prompt should match what was sent");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_WithResetFlag() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Design Rules: Default instruction prompt for reset scenario.");
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_contact_logo", 1);
        requestBody.put("include_email_signature", 1);
        requestBody.put("preview_template", JSONObject.NULL);
        requestBody.put("is_reset", true);
        requestBody.put("source", "Settings Page");

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK for reset save");
        assertSuccessMessageType(response.jsonPath().getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_WithSourcePreview() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Save from preview source with full settings.");
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_contact_logo", 1);
        requestBody.put("include_email_signature", 1);
        requestBody.put("source", "Preview");

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200);
        assertSuccessMessageType(response.jsonPath().getString("meta.message_type"));

        Response getResponse = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);
        Assert.assertEquals(getResponse.getStatusCode(), 200);

        JsonPath jsonPath = getResponse.jsonPath();
        Assert.assertEquals(Integer.parseInt(jsonPath.get("data[0].include_company_logo").toString()), 1,
                "include_company_logo should persist as 1");
        Assert.assertEquals(Integer.parseInt(jsonPath.get("data[0].include_contact_logo").toString()), 1,
                "include_contact_logo should persist as 1");
        Assert.assertEquals(Integer.parseInt(jsonPath.get("data[0].include_email_signature").toString()), 1,
                "include_email_signature should persist as 1");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_WithEmailSignatureDisabled() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Test with email signature disabled.");
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_contact_logo", 1);
        requestBody.put("include_email_signature", 0);

        Response saveResponse = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(saveResponse.getStatusCode(), 200);
        assertSuccessMessageType(saveResponse.jsonPath().getString("meta.message_type"));

        Response getResponse = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);
        Assert.assertEquals(getResponse.getStatusCode(), 200);

        JsonPath jsonPath = getResponse.jsonPath();
        int emailSig = Integer.parseInt(jsonPath.get("data[0].include_email_signature").toString());
        Assert.assertEquals(emailSig, 0,
                "include_email_signature should persist as 0 after disabling");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_WithPreviewTemplate() {
        String htmlTemplate = "<html><body><h1>Preview Template</h1></body></html>";

        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Save with preview template HTML.");
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_contact_logo", 1);
        requestBody.put("include_email_signature", 1);
        requestBody.put("preview_template", htmlTemplate);

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200);
        assertSuccessMessageType(response.jsonPath().getString("meta.message_type"));

        Response getResponse = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);
        Assert.assertEquals(getResponse.getStatusCode(), 200);

        String savedTemplate = getResponse.jsonPath().getString("data[0].preview_template");
        Assert.assertNotNull(savedTemplate, "preview_template should be persisted");
        Assert.assertTrue(savedTemplate.contains("Preview Template"),
                "Saved preview_template should contain the HTML we sent");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_PromptExceeds5000Chars() {
        StringBuilder longPrompt = new StringBuilder();
        for (int i = 0; i < 510; i++) {
            longPrompt.append("0123456789");
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", longPrompt.toString());
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_contact_logo", 1);
        requestBody.put("include_email_signature", 1);

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Albatross returns 200 for validation errors");
        assertDangerMessageType(response.jsonPath().getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_InvalidToken() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Test");
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_contact_logo", 1);
        requestBody.put("include_email_signature", 1);

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                "InvalidToken", null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 Unauthorized");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_EmptyBody() {
        JSONObject requestBody = new JSONObject();

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Albatross returns 200 for validation errors per convention");
        assertDangerMessageType(response.jsonPath().getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_NullPrompt() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", JSONObject.NULL);
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_contact_logo", 1);
        requestBody.put("include_email_signature", 1);

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Null prompt with valid required fields should succeed (prompt is optional)");
        assertSuccessMessageType(response.jsonPath().getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_InvalidLogoValues() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Test prompt with invalid logo values for boundary testing.");
        requestBody.put("include_company_logo", 99);
        requestBody.put("include_contact_logo", -1);
        requestBody.put("include_email_signature", 1);

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Albatross returns 200 for validation errors per convention");
        assertDangerMessageType(response.jsonPath().getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_MissingRequiredBooleans() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Test with missing required boolean fields.");

        Response response = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Albatross returns 200 for validation errors");
        assertDangerMessageType(response.jsonPath().getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_GetMethodNotAllowed() {
        Response response = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);

        Assert.assertEquals(response.getStatusCode(), 405,
                "Expected 405 Method Not Allowed for GET on a POST endpoint");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_SpecialCharactersInPrompt() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Create pitch with <script>alert('xss')</script> & special chars: \"quotes\" 'apostrophe' <b>bold</b> \u00e9\u00e8\u00ea");
        requestBody.put("include_company_logo", 1);
        requestBody.put("include_contact_logo", 1);
        requestBody.put("include_email_signature", 1);

        Response saveResponse = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, requestBody);

        Assert.assertEquals(saveResponse.getStatusCode(), 200);
        assertSuccessMessageType(saveResponse.jsonPath().getString("meta.message_type"));

        Response getResponse = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);
        Assert.assertEquals(getResponse.getStatusCode(), 200);

        String savedPrompt = getResponse.jsonPath().getString("data[0].instruction_prompt");
        Assert.assertNotNull(savedPrompt, "Prompt with special chars should persist");
        Assert.assertFalse(savedPrompt.isEmpty(), "Saved prompt should not be empty");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_ResetClearsPreviewTemplate() {
        JSONObject saveBody = new JSONObject();
        saveBody.put("instruction_prompt", "Reset test: before reset with template.");
        saveBody.put("include_company_logo", 1);
        saveBody.put("include_contact_logo", 1);
        saveBody.put("include_email_signature", 1);
        saveBody.put("preview_template", "<html><body>Before reset</body></html>");

        Response saveResponse = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, saveBody);
        Assert.assertEquals(saveResponse.getStatusCode(), 200);

        JSONObject resetBody = new JSONObject();
        resetBody.put("instruction_prompt", "Default prompt after reset.");
        resetBody.put("include_company_logo", 1);
        resetBody.put("include_contact_logo", 1);
        resetBody.put("include_email_signature", 1);
        resetBody.put("preview_template", JSONObject.NULL);
        resetBody.put("is_reset", true);

        Response resetResponse = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, resetBody);
        Assert.assertEquals(resetResponse.getStatusCode(), 200);
        assertSuccessMessageType(resetResponse.jsonPath().getString("meta.message_type"));

        Response getResponse = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);
        Assert.assertEquals(getResponse.getStatusCode(), 200);

        String template = getResponse.jsonPath().getString("data[0].preview_template");
        Assert.assertTrue(template == null || !template.contains("Before reset"),
                "After reset, preview_template should be cleared or not contain old value");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGetPitchSettings_DeleteMethodNotAllowed() {
        Response response = RestClient.doDelete("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);

        Assert.assertTrue(response.getStatusCode() == 405 || response.getStatusCode() == 404,
                "Expected 405 or 404 for DELETE on settings endpoint, got: " + response.getStatusCode());
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySavePitchSettings_ToggleLogosPersistence() {
        JSONObject disableBody = new JSONObject();
        disableBody.put("instruction_prompt", "Toggle test: logos off.");
        disableBody.put("include_company_logo", 0);
        disableBody.put("include_contact_logo", 0);
        disableBody.put("include_email_signature", 0);

        Response disableResponse = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, disableBody);
        Assert.assertEquals(disableResponse.getStatusCode(), 200);

        Response getAfterDisable = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);
        JsonPath jp1 = getAfterDisable.jsonPath();
        Assert.assertEquals(Integer.parseInt(jp1.get("data[0].include_company_logo").toString()), 0);
        Assert.assertEquals(Integer.parseInt(jp1.get("data[0].include_contact_logo").toString()), 0);
        Assert.assertEquals(Integer.parseInt(jp1.get("data[0].include_email_signature").toString()), 0);

        JSONObject enableBody = new JSONObject();
        enableBody.put("instruction_prompt", "Toggle test: logos on.");
        enableBody.put("include_company_logo", 1);
        enableBody.put("include_contact_logo", 1);
        enableBody.put("include_email_signature", 1);

        Response enableResponse = RestClient.doPost1("JSON", albatrossURL,
                "admin-settings/save-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true, enableBody);
        Assert.assertEquals(enableResponse.getStatusCode(), 200);

        Response getAfterEnable = RestClient.doGet("JSON", albatrossURL,
                "admin-settings/get-pitch-candidate-agent-settings",
                albatrossAuthToken, null, null, true);
        JsonPath jp2 = getAfterEnable.jsonPath();
        Assert.assertEquals(Integer.parseInt(jp2.get("data[0].include_company_logo").toString()), 1);
        Assert.assertEquals(Integer.parseInt(jp2.get("data[0].include_contact_logo").toString()), 1);
        Assert.assertEquals(Integer.parseInt(jp2.get("data[0].include_email_signature").toString()), 1);
    }
}
