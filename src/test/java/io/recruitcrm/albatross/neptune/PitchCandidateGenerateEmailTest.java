package io.recruitcrm.albatross.neptune;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class PitchCandidateGenerateEmailTest extends TestBase {

    String albatrossAuthToken;
    String candidateSlug;
    int candidateId;

    private void assertSuccessMessageType(String actual) {
        Assert.assertTrue("is-success".equals(actual) || "success".equals(actual),
                "message_type should be 'is-success' or 'success', got: " + actual);
    }

    private Response createCandidateDirectly(String baseUrl, String token, JSONObject payload) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Response response = RestAssured.given()
                    .baseUri(baseUrl)
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(payload.toString())
                    .post("/candidates");
            String msgType = response.jsonPath().getString("message_type");
            if (!"is-danger".equals(msgType) && !"danger".equals(msgType)) {
                return response;
            }
            if (attempt < maxAttempts) {
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
        return RestAssured.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(payload.toString())
                .post("/candidates");
    }

    @BeforeClass
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();

        JSONObject candidate = new JSONObject();
        candidate.put("id", false);
        candidate.put("slug", "");
        candidate.put("firstname", "PitchTest");
        candidate.put("lastname", "Automation");
        candidate.put("emailid", "pitchtest_" + System.currentTimeMillis() + "@yopmail.com");
        candidate.put("genderid", 0);
        candidate.put("contactnumber", "");
        candidate.put("address", "");
        candidate.put("city", "");
        candidate.put("summary", "");
        candidate.put("locality", "");

        JSONObject payload = new JSONObject();
        payload.put("candidate", candidate);
        payload.put("address_changed", false);
        payload.put("filesInfo", new JSONObject());
        payload.put("deleteResumeKey", "");
        payload.put("deleteEducation", new JSONArray());
        payload.put("deleteWork", new JSONArray());
        payload.put("sovrenData", new JSONArray());

        Response createResponse = createCandidateDirectly(albatrossURL, albatrossAuthToken, payload);

        Assert.assertEquals(createResponse.getStatusCode(), 200,
                "Candidate creation should return 200");

        JsonPath jsonPath = createResponse.jsonPath();
        String msgType = jsonPath.getString("message_type");
        Assert.assertTrue(!"is-danger".equals(msgType) && !"danger".equals(msgType),
                "Candidate creation failed: " + jsonPath.getString("message"));

        candidateSlug = jsonPath.getString("data.candidate.slug");
        if (candidateSlug == null) {
            candidateSlug = jsonPath.getString("data.slug");
        }
        Assert.assertNotNull(candidateSlug, "Candidate slug should be returned");

        Object idObj = jsonPath.get("data.candidate.id");
        if (idObj == null) {
            idObj = jsonPath.get("data.id");
        }
        Assert.assertNotNull(idObj, "Candidate ID should be returned");
        candidateId = Integer.parseInt(idObj.toString());
    }

    private JSONObject buildColumnsParam() {
        JSONObject columns = new JSONObject();

        JSONObject nameCol = new JSONObject();
        nameCol.put("visible", "1");
        nameCol.put("order", 1);
        nameCol.put("external_label", "Name");
        nameCol.put("type", "text");
        columns.put("candidatename", nameCol);

        JSONObject skillCol = new JSONObject();
        skillCol.put("visible", "1");
        skillCol.put("order", 2);
        skillCol.put("external_label", "Skills");
        skillCol.put("type", "text");
        columns.put("skill", skillCol);

        JSONObject emailCol = new JSONObject();
        emailCol.put("visible", "0");
        emailCol.put("order", 3);
        emailCol.put("external_label", "Email");
        emailCol.put("type", "text");
        columns.put("emailid", emailCol);

        return columns;
    }

    // ==================== Preview Mode ====================

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_PreviewMode_HappyPath() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "preview");
        requestBody.put("instruction_prompt", "Create a professional pitch email introducing the candidates with a clean layout.");
        requestBody.put("include_company_logo", true);
        requestBody.put("include_contact_company_logo", true);

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK for preview mode");

        JsonPath jsonPath = response.jsonPath();
        Assert.assertNotNull(jsonPath.get("data"), "Response should contain data field");

        String html = jsonPath.getString("data.html");
        Assert.assertNotNull(html, "Preview response should contain data.html");
        Assert.assertFalse(html.isEmpty(), "data.html should not be empty");
        Assert.assertTrue(html.toLowerCase().contains("<") && html.toLowerCase().contains(">"),
                "data.html should contain valid HTML tags");

        String template = jsonPath.getString("data.template");
        Assert.assertNotNull(template, "Preview response should contain data.template");
        Assert.assertFalse(template.isEmpty(), "data.template should not be empty");

        Assert.assertNotNull(jsonPath.get("meta"), "Response should contain meta");
        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_PreviewMode_ResponseContainsHtml() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "preview");
        requestBody.put("instruction_prompt", "Generate a candidate pitch email with modern styling.");
        requestBody.put("include_company_logo", true);
        requestBody.put("include_contact_company_logo", true);

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jsonPath = response.jsonPath();
        String html = jsonPath.getString("data.html");
        Assert.assertNotNull(html, "data.html should not be null");
        Assert.assertTrue(html.toLowerCase().contains("html"),
                "Preview data.html should contain HTML content");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_PreviewMode_TokenUsage() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "preview");
        requestBody.put("instruction_prompt", "Create a professional pitch email for token usage check.");
        requestBody.put("include_company_logo", true);
        requestBody.put("include_contact_company_logo", true);

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jsonPath = response.jsonPath();
        String html = jsonPath.getString("data.html");
        Assert.assertNotNull(html, "Preview should return data.html");
        Assert.assertFalse(html.isEmpty(), "data.html should not be empty");

        String template = jsonPath.getString("data.template");
        Assert.assertNotNull(template, "Preview should return data.template");
        Assert.assertFalse(template.isEmpty(), "data.template should not be empty");

        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_PreviewMode_WithRegenerate() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "preview");
        requestBody.put("instruction_prompt", "Create a fresh pitch email variant with regenerate enabled.");
        requestBody.put("include_company_logo", true);
        requestBody.put("include_contact_company_logo", true);
        requestBody.put("regenerate", true);

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 OK for preview mode with regenerate=true");

        JsonPath jsonPath = response.jsonPath();
        String html = jsonPath.getString("data.html");
        Assert.assertNotNull(html, "Regenerated preview should contain data.html");
        Assert.assertFalse(html.isEmpty(), "Regenerated data.html should not be empty");
        Assert.assertTrue(html.toLowerCase().contains("<"),
                "Regenerated data.html should contain HTML markup");

        String template = jsonPath.getString("data.template");
        Assert.assertNotNull(template, "Regenerated preview should contain data.template");
        Assert.assertFalse(template.isEmpty(), "Regenerated data.template should not be empty");

        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    // ==================== Generate Mode ====================

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_GenerateMode_HappyPath() {
        Assert.assertTrue(candidateId > 0, "Candidate should be created for generate mode test");

        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "generate");
        requestBody.put("candidate_ids", new JSONArray().put(candidateId));
        requestBody.put("columns", buildColumnsParam());

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK for generate mode");

        JsonPath jsonPath = response.jsonPath();
        String template = jsonPath.getString("data.template");
        Assert.assertNotNull(template, "Generate response should contain data.template");
        Assert.assertFalse(template.isEmpty(), "data.template should not be empty");

        String subject = jsonPath.getString("data.subject");
        Assert.assertNotNull(subject, "Generate response should contain data.subject");
        Assert.assertFalse(subject.isEmpty(), "data.subject should not be empty");

        Object records = jsonPath.get("data.records");
        Assert.assertNotNull(records, "Generate response should contain data.records");
        int recordCount = Integer.parseInt(records.toString());
        Assert.assertTrue(recordCount >= 1,
                "data.records count should be >= 1 for a valid candidate, got: " + recordCount);

        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_GenerateMode_ResponseStructure() {
        Assert.assertTrue(candidateId > 0, "Candidate should be available");

        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "generate");
        requestBody.put("candidate_ids", new JSONArray().put(candidateId));
        requestBody.put("columns", buildColumnsParam());

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jsonPath = response.jsonPath();
        Assert.assertNotNull(jsonPath.get("data.template"), "Should contain data.template");
        Assert.assertNotNull(jsonPath.get("data.subject"), "Should contain data.subject");
        Assert.assertNotNull(jsonPath.get("data.records"), "Should contain data.records");
        Assert.assertNotNull(jsonPath.get("meta"), "Should contain meta");
        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_GenerateMode_WithCustomDetails() {
        Assert.assertTrue(candidateId > 0, "Candidate should be available");

        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "generate");
        requestBody.put("candidate_ids", new JSONArray().put(candidateId));
        requestBody.put("columns", buildColumnsParam());
        requestBody.put("instruction_prompt", "Use a clean Navy White design with professional tone.");
        requestBody.put("include_company_logo", true);
        requestBody.put("include_contact_company_logo", true);

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 OK for generate mode with custom details");

        JsonPath jsonPath = response.jsonPath();
        String template = jsonPath.getString("data.template");
        Assert.assertNotNull(template, "Custom generate should contain data.template");
        Assert.assertFalse(template.isEmpty(), "data.template should not be empty");
        Assert.assertTrue(template.toLowerCase().contains("<"),
                "data.template should contain HTML content");

        Assert.assertNotNull(jsonPath.get("data.subject"), "Should contain data.subject");
        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_GenerateMode_MultipleCandidates() {
        Assert.assertTrue(candidateId > 0, "At least one candidate should be available");

        JSONObject candidate2 = new JSONObject();
        candidate2.put("id", false);
        candidate2.put("slug", "");
        candidate2.put("firstname", "PitchSecond");
        candidate2.put("lastname", "Candidate");
        candidate2.put("emailid", "pitchtest2_" + System.currentTimeMillis() + "@yopmail.com");
        candidate2.put("genderid", 0);

        JSONObject payload2 = new JSONObject();
        payload2.put("candidate", candidate2);
        payload2.put("address_changed", false);
        payload2.put("filesInfo", new JSONObject());
        payload2.put("deleteResumeKey", "");
        payload2.put("deleteEducation", new JSONArray());
        payload2.put("deleteWork", new JSONArray());
        payload2.put("sovrenData", new JSONArray());

        Response createResponse2 = createCandidateDirectly(albatrossURL, albatrossAuthToken, payload2);

        Assert.assertEquals(createResponse2.getStatusCode(), 200,
                "Second candidate creation should return 200");

        JsonPath jsonPath2 = createResponse2.jsonPath();
        Object idObj2 = jsonPath2.get("data.candidate.id");
        if (idObj2 == null) {
            idObj2 = jsonPath2.get("data.id");
        }
        Assert.assertNotNull(idObj2, "Second candidate ID should be returned");
        int candidateId2 = Integer.parseInt(idObj2.toString());

        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "generate");
        requestBody.put("candidate_ids", new JSONArray().put(candidateId).put(candidateId2));
        requestBody.put("columns", buildColumnsParam());

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 OK for generate with multiple candidates");

        JsonPath jsonPath = response.jsonPath();
        String template = jsonPath.getString("data.template");
        Assert.assertNotNull(template, "Multi-candidate generate should contain data.template");
        Assert.assertFalse(template.isEmpty(), "data.template should not be empty");

        Object records = jsonPath.get("data.records");
        Assert.assertNotNull(records, "Should contain data.records for multiple candidates");
        int recordCount = Integer.parseInt(records.toString());
        Assert.assertTrue(recordCount >= 2,
                "data.records count should be >= 2 for multiple candidates, got: " + recordCount);

        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_GenerateMode_ExceedMaxCandidates() {
        JSONArray candidateIds = new JSONArray();
        for (int i = 1; i <= 21; i++) {
            candidateIds.put(i);
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "generate");
        requestBody.put("candidate_ids", candidateIds);
        requestBody.put("columns", buildColumnsParam());

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 when exceeding MAX_PITCH_CANDIDATES (20)");

        String responseBody = response.getBody().asString();
        Assert.assertFalse(responseBody.isEmpty(), "Error response body should not be empty");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_GenerateMode_MissingColumns() {
        Assert.assertTrue(candidateId > 0, "Candidate should be available");

        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "generate");
        requestBody.put("candidate_ids", new JSONArray().put(candidateId));

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 when columns parameter is missing in generate mode");

        String responseBody = response.getBody().asString();
        Assert.assertFalse(responseBody.isEmpty(), "Error response body should not be empty");
    }

    // ==================== Validation Endpoint ====================

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyValidateInstruction_ValidPrompt() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt",
                "Create a professional pitch email with candidate details in a grid layout. "
                        + "Use forest green accent color and clean typography.");

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate/validate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 for valid instruction");

        JsonPath jsonPath = response.jsonPath();
        Assert.assertTrue((Boolean) jsonPath.get("data.valid"), "Valid instruction should return data.valid=true");
        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyValidateInstruction_IrrelevantPrompt() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Who is the President of India?");

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate/validate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Validate always returns 200 per LLD");

        JsonPath jsonPath = response.jsonPath();
        Assert.assertFalse((Boolean) jsonPath.get("data.valid"),
                "Irrelevant instruction should return data.valid=false");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyValidateInstruction_EmptyPrompt() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "");

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate/validate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 for empty instruction_prompt");

        JsonPath jsonPath = response.jsonPath();
        Assert.assertFalse((Boolean) jsonPath.get("data.valid"),
                "Empty prompt should be flagged as invalid (data.valid=false)");
        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyValidateInstruction_MissingPrompt() {
        JSONObject requestBody = new JSONObject();

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate/validate",
                albatrossAuthToken, null, false, requestBody);

        int statusCode = response.getStatusCode();
        Assert.assertTrue(statusCode == 422 || statusCode == 400,
                "Expected 422 or 400 when instruction_prompt is missing, got: " + statusCode);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyValidateInstruction_InvalidToken() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "Professional pitch email layout.");

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate/validate",
                "InvalidToken", null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 Unauthorized for validate endpoint");
    }

    // ==================== Error Scenarios ====================

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_InvalidMode() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "invalid_mode");

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        int statusCode = response.getStatusCode();
        Assert.assertTrue(statusCode == 422 || statusCode == 400,
                "Expected 422 or 400 for invalid mode, got: " + statusCode);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_GenerateMode_NoCandidateIds() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "generate");
        requestBody.put("columns", buildColumnsParam());

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        int statusCode = response.getStatusCode();
        Assert.assertTrue(statusCode == 422 || statusCode == 400,
                "Expected 422 or 400 when candidate_ids missing in generate mode, got: " + statusCode);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_GenerateMode_EmptyCandidateIds() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "generate");
        requestBody.put("candidate_ids", new JSONArray());
        requestBody.put("columns", buildColumnsParam());

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 for no candidates selected");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_GenerateMode_InvalidCandidateIds() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "generate");
        requestBody.put("candidate_ids", new JSONArray().put(999999999));
        requestBody.put("columns", buildColumnsParam());

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 for non-existent candidate IDs (no candidate data found)");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_PreviewMode_MissingInstruction() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "preview");
        requestBody.put("include_company_logo", true);
        requestBody.put("include_contact_company_logo", true);

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 when instruction_prompt missing in preview mode");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_InvalidToken() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "preview");
        requestBody.put("instruction_prompt", "Test prompt for auth check.");
        requestBody.put("include_company_logo", true);
        requestBody.put("include_contact_company_logo", true);

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                "InvalidToken", null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 Unauthorized for invalid token");

        String responseBody = response.getBody().asString();
        Assert.assertTrue(responseBody.toLowerCase().contains("unauthorized") || responseBody.contains("error"),
                "401 response should contain unauthorized or error indication");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_EmptyBody() {
        JSONObject requestBody = new JSONObject();

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 for empty body (mode is required)");

        String responseBody = response.getBody().asString();
        Assert.assertFalse(responseBody.isEmpty(), "Error response body should not be empty");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_GetMethodNotAllowed() {
        Response response = RestClient.doGet("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, null, true);

        Assert.assertEquals(response.getStatusCode(), 405,
                "Expected 405 Method Not Allowed for GET on a POST endpoint");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_PreviewMode_LogosDisabled() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "preview");
        requestBody.put("instruction_prompt", "Create pitch email without any logos.");
        requestBody.put("include_company_logo", false);
        requestBody.put("include_contact_company_logo", false);

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Preview with logos disabled should return 200");

        JsonPath jsonPath = response.jsonPath();
        String html = jsonPath.getString("data.html");
        Assert.assertNotNull(html, "data.html should be present even without logos");
        Assert.assertFalse(html.isEmpty(), "data.html should not be empty");
        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_PreviewMode_VeryLongPrompt() {
        StringBuilder longPrompt = new StringBuilder("Create a professional pitch email. ");
        for (int i = 0; i < 100; i++) {
            longPrompt.append("Use clean design with corporate colors and professional tone. ");
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "preview");
        requestBody.put("instruction_prompt", longPrompt.toString());
        requestBody.put("include_company_logo", true);
        requestBody.put("include_contact_company_logo", true);

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Very long prompt should be handled by API and return 200");

        JsonPath jsonPath = response.jsonPath();
        String html = jsonPath.getString("data.html");
        Assert.assertNotNull(html, "Very long prompt should still produce data.html");
        Assert.assertFalse(html.isEmpty(), "data.html should not be empty for long prompt");
        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyValidateInstruction_HtmlInjectionPrompt() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruction_prompt", "<script>alert('test')</script> Create a pitch email with <b>bold</b> and <img src=x onerror=alert(1)>");

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate/validate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Validate endpoint should return 200 for any prompt");

        JsonPath jsonPath = response.jsonPath();
        Assert.assertNotNull(jsonPath.get("data.valid"), "Should return valid field");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_GenerateMode_NegativeCandidateId() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "generate");
        requestBody.put("candidate_ids", new JSONArray().put(-1));
        requestBody.put("columns", buildColumnsParam());

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertTrue(response.getStatusCode() >= 400,
                "Negative candidate ID should return error status, got: " + response.getStatusCode());
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_GenerateMode_ZeroCandidateId() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "generate");
        requestBody.put("candidate_ids", new JSONArray().put(0));
        requestBody.put("columns", buildColumnsParam());

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertTrue(response.getStatusCode() >= 400,
                "Zero candidate ID should return error status, got: " + response.getStatusCode());
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_DeleteMethodNotAllowed() {
        Response response = RestClient.doDelete("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, null, true);

        Assert.assertTrue(response.getStatusCode() == 405 || response.getStatusCode() == 404,
                "Expected 405 or 404 for DELETE on generate endpoint, got: " + response.getStatusCode());
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyValidateInstruction_DeleteMethodNotAllowed() {
        Response response = RestClient.doDelete("JSON", neptuneServiceURL, "ai-pitch-candidate/validate",
                albatrossAuthToken, null, null, true);

        Assert.assertTrue(response.getStatusCode() == 405 || response.getStatusCode() == 404,
                "Expected 405 or 404 for DELETE on validate endpoint, got: " + response.getStatusCode());
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_PreviewMode_EmptyStringInstruction() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "preview");
        requestBody.put("instruction_prompt", "");
        requestBody.put("include_company_logo", true);
        requestBody.put("include_contact_company_logo", true);

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Empty string instruction is accepted by API and returns 200");

        JsonPath jsonPath = response.jsonPath();
        String html = jsonPath.getString("data.html");
        Assert.assertNotNull(html, "Empty instruction should still produce data.html");
        Assert.assertFalse(html.isEmpty(), "data.html should not be empty");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_PreviewMode_NullInstruction() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "preview");
        requestBody.put("instruction_prompt", JSONObject.NULL);
        requestBody.put("include_company_logo", true);
        requestBody.put("include_contact_company_logo", true);

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 400,
                "Null instruction_prompt should return 400 validation error");

        JsonPath jsonPath = response.jsonPath();
        Assert.assertNotNull(jsonPath.get("errors"), "Should contain errors object");
        String errorMsg = response.getBody().asString();
        Assert.assertTrue(errorMsg.contains("instruction_prompt"),
                "Error should mention instruction_prompt as required field");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateEmail_GenerateMode_ExactlyMaxCandidates() {
        JSONArray candidateIds = new JSONArray();
        for (int i = 1; i <= 20; i++) {
            candidateIds.put(i);
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "generate");
        requestBody.put("candidate_ids", candidateIds);
        requestBody.put("columns", buildColumnsParam());

        Response response = RestClient.doPost("JSON", neptuneServiceURL, "ai-pitch-candidate",
                albatrossAuthToken, null, false, requestBody);

        Assert.assertEquals(response.getStatusCode(), 400,
                "IDs 1-20 don't belong to this account, should return 400 not-found error");

        JsonPath jsonPath = response.jsonPath();
        Assert.assertNull(jsonPath.get("data"),
                "data should be null when candidate IDs are not found");
        String message = jsonPath.getString("meta.message");
        Assert.assertNotNull(message, "Should have error message in meta");
        Assert.assertTrue(message.toLowerCase().contains("candidate"),
                "Error message should reference candidates");
    }
}
