package io.recruitcrm.albatross.neptune;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
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
public class CandidateSubmissionGenerateEmailTest extends TestBase {

    private static final String ENDPOINT = "ai-candidate-submission";

    commanFunction function = new commanFunction();
    String albatrossAuthToken;
    String apiAuthToken;
    int jobId;
    int singleCandidateJobId;
    int emptyJobId;
    int candidateId1;
    int candidateId2;
    String jobSlug;
    String singleCandidateJobSlug;
    String emptyJobSlug;
    String candidateOneName = "SubmissionOne Candidate";
    String candidateTwoName = "SubmissionTwo Candidate";

    private void assertSuccessMessageType(String actual) {
        Assert.assertTrue("is-success".equals(actual) || "success".equals(actual),
                "message_type should be 'is-success' or 'success', got: " + actual);
    }

    private int createAndAssignCandidate(String firstName, String lastName, String jobSlugToAssign) {
        JSONObject candidate = new JSONObject();
        candidate.put("id", false);
        candidate.put("slug", "");
        candidate.put("firstname", firstName);
        candidate.put("lastname", lastName);
        candidate.put("emailid", firstName.toLowerCase() + "_" + System.currentTimeMillis() + "@yopmail.com");
        candidate.put("genderid", 0);
        candidate.put("contactnumber", "");
        candidate.put("address", "");
        candidate.put("city", "Austin");
        candidate.put("summary", "Automation candidate for submission agent tests.");
        candidate.put("locality", "");
        candidate.put("skill", "Java, API Testing, Selenium");

        JSONObject payload = new JSONObject();
        payload.put("candidate", candidate);
        payload.put("address_changed", false);
        payload.put("filesInfo", new JSONObject());
        payload.put("deleteResumeKey", "");
        payload.put("deleteEducation", new JSONArray());
        payload.put("deleteWork", new JSONArray());
        payload.put("sovrenData", new JSONArray());

        Response createResponse = RestAssured.given()
                .baseUri(albatrossURL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + albatrossAuthToken)
                .body(payload.toString())
                .post("/candidates");

        Assert.assertEquals(createResponse.getStatusCode(), 200,
                "Candidate creation should return 200. Response: " + createResponse.getBody().asString());

        JsonPath jsonPath = createResponse.jsonPath();
        String candidateSlug = jsonPath.getString("data.candidate.slug");
        if (candidateSlug == null) {
            candidateSlug = jsonPath.getString("data.slug");
        }
        Assert.assertNotNull(candidateSlug, "Candidate slug should be returned");

        function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug, jobSlugToAssign);

        Object idObj = jsonPath.get("data.candidate.id");
        if (idObj == null) {
            idObj = jsonPath.get("data.id");
        }
        Assert.assertNotNull(idObj, "Candidate ID should be returned");
        return Integer.parseInt(idObj.toString());
    }

    private JSONObject buildPreviewRequest(int targetJobId, String instructionPrompt) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("job_id", targetJobId);
        requestBody.put("mode", "preview");
        requestBody.put("instruction_prompt", instructionPrompt);
        requestBody.put("include_company_logo", true);
        requestBody.put("include_client_logo", true);
        requestBody.put("include_email_signature", true);
        return requestBody;
    }

    private JSONObject buildColumnsParam(boolean includeName, boolean includeSkill, boolean includeEmail) {
        JSONObject columns = new JSONObject();

        if (includeName) {
            JSONObject nameCol = new JSONObject();
            nameCol.put("visible", "1");
            nameCol.put("order", 1);
            nameCol.put("external_label", "Name");
            nameCol.put("type", "text");
            columns.put("candidatename", nameCol);
        }

        if (includeSkill) {
            JSONObject skillCol = new JSONObject();
            skillCol.put("visible", "1");
            skillCol.put("order", 2);
            skillCol.put("external_label", "Skills");
            skillCol.put("type", "text");
            columns.put("skill", skillCol);
        }

        if (includeEmail) {
            JSONObject emailCol = new JSONObject();
            emailCol.put("visible", "1");
            emailCol.put("order", 3);
            emailCol.put("external_label", "Email");
            emailCol.put("type", "text");
            columns.put("emailid", emailCol);
        }

        return columns;
    }

    private Response postSubmissionRequest(JSONObject requestBody) {
        return RestClient.doPost("JSON", neptuneServiceURL, ENDPOINT,
                albatrossAuthToken, null, false, requestBody);
    }

    private Response postSubmissionRequestWithToken(String token, JSONObject requestBody) {
        return RestClient.doPost("JSON", neptuneServiceURL, ENDPOINT,
                token, null, false, requestBody);
    }

    private void assertPreviewEmailPayload(JsonPath jsonPath) {
        String html = jsonPath.getString("data.html");
        Assert.assertNotNull(html, "Preview response should contain data.html");
        Assert.assertFalse(html.trim().isEmpty(), "data.html should not be empty");
        Assert.assertTrue(html.toLowerCase().contains("<html"),
                "Preview data.html should contain HTML markup");

        String template = jsonPath.getString("data.template");
        Assert.assertNotNull(template, "Preview response should contain data.template");
        Assert.assertFalse(template.trim().isEmpty(), "data.template should not be empty");
        Assert.assertTrue(template.contains("{{"),
                "Preview data.template should contain placeholder tokens");

        Assert.assertNotNull(jsonPath.get("data.token_usage.total_tokens"), "Preview token_usage.total_tokens should exist");
        Assert.assertTrue(jsonPath.getInt("data.token_usage.total_tokens") > 0,
                "Preview token_usage.total_tokens should be greater than 0");

        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    private void assertGeneratedEmailPayload(JsonPath jsonPath) {
        String template = jsonPath.getString("data.template");
        Assert.assertNotNull(template, "Response should contain data.template");
        Assert.assertFalse(template.trim().isEmpty(), "data.template should not be empty");
        Assert.assertTrue(template.toLowerCase().contains("<html"),
                "data.template should contain HTML markup");

        String subject = jsonPath.getString("data.subject");
        Assert.assertNotNull(subject, "Response should contain data.subject");
        Assert.assertFalse(subject.trim().isEmpty(), "data.subject should not be empty");

        Assert.assertNotNull(jsonPath.get("data.job.id"), "Response should contain data.job.id");
        Assert.assertNotNull(jsonPath.get("data.job.name"), "Response should contain data.job.name");
        Assert.assertNotNull(jsonPath.get("data.records"), "Response should contain data.records");
        Assert.assertTrue(jsonPath.getInt("data.records") >= 1,
                "data.records should be at least 1");

        Assert.assertNotNull(jsonPath.get("data.attachments"), "Response should contain data.attachments");
        Assert.assertNotNull(jsonPath.get("data.analytics"), "Response should contain data.analytics");
        Assert.assertNotNull(jsonPath.get("data.token_usage.total_tokens"), "token_usage.total_tokens should exist");
        Assert.assertTrue(jsonPath.getInt("data.token_usage.total_tokens") > 0,
                "token_usage.total_tokens should be greater than 0");

        assertSuccessMessageType(jsonPath.getString("meta.message_type"));
    }

    @BeforeClass
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();

        JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String companySlug = jsonCompany.getString("slug");

        JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
        String contactSlug = jsonContact.getString("slug");

        JsonPath jsonJob = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
        jobSlug = jsonJob.getString("slug");
        jobId = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job")
                .jsonPath().getInt("data.job.id");

        JsonPath jsonEmptyJob = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
        emptyJobSlug = jsonEmptyJob.getString("slug");
        emptyJobId = function.getEntityDetail(albatrossURL, albatrossAuthToken, emptyJobSlug, "job")
                .jsonPath().getInt("data.job.id");

        candidateId1 = createAndAssignCandidate("SubmissionOne", "Candidate", jobSlug);
        candidateId2 = createAndAssignCandidate("SubmissionTwo", "Candidate", jobSlug);

        JsonPath jsonSingleJob = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
        singleCandidateJobSlug = jsonSingleJob.getString("slug");
        singleCandidateJobId = function.getEntityDetail(albatrossURL, albatrossAuthToken, singleCandidateJobSlug, "job")
                .jsonPath().getInt("data.job.id");
        createAndAssignCandidate("SubmissionSingle", "Candidate", singleCandidateJobSlug);
    }

    // ==================== Generate Mode ====================

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_GenerateMode_HappyPath() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("job_id", jobId);
        requestBody.put("mode", "generate");

        Response response = postSubmissionRequest(requestBody);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 OK for generate mode. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        assertGeneratedEmailPayload(jsonPath);
        Assert.assertEquals(jsonPath.getInt("data.job.id"), jobId, "Returned job id should match request");
        Assert.assertTrue(jsonPath.getString("data.template").contains(candidateOneName)
                        || jsonPath.getString("data.template").contains(candidateTwoName),
                "Generated template should include assigned candidate names");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_GenerateMode_ResponseStructure() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("job_id", jobId);
        requestBody.put("mode", "generate");

        Response response = postSubmissionRequest(requestBody);
        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jsonPath = response.jsonPath();
        assertGeneratedEmailPayload(jsonPath);
        Assert.assertNotNull(jsonPath.get("data.job.contactname"), "data.job.contactname should exist");
        Assert.assertNotNull(jsonPath.get("data.job.slug"), "data.job.slug should exist");
        Assert.assertNotNull(jsonPath.get("data.analytics.include_company_logo"), "analytics.include_company_logo should exist");
        Assert.assertNotNull(jsonPath.get("data.analytics.include_client_company_logo"),
                "analytics.include_client_company_logo should exist");
        Assert.assertNotNull(jsonPath.getInt("data.token_usage.input_tokens"));
        Assert.assertNotNull(jsonPath.getInt("data.token_usage.output_tokens"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_GenerateMode_MultipleCandidates() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("job_id", jobId);
        requestBody.put("mode", "generate");

        Response response = postSubmissionRequest(requestBody);
        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jsonPath = response.jsonPath();
        assertGeneratedEmailPayload(jsonPath);

        int records = jsonPath.getInt("data.records");
        Assert.assertTrue(records >= 2,
                "Multiple assigned candidates should produce records >= 2, got: " + records);

        String template = jsonPath.getString("data.template");
        Assert.assertTrue(template.contains(candidateOneName),
                "Template should include first assigned candidate");
        Assert.assertTrue(template.contains(candidateTwoName),
                "Template should include second assigned candidate");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_GenerateMode_SingleCandidate() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("job_id", singleCandidateJobId);
        requestBody.put("mode", "generate");

        Response response = postSubmissionRequest(requestBody);
        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 when generating for a job with one assigned candidate. Response: "
                        + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        assertGeneratedEmailPayload(jsonPath);
        Assert.assertEquals(jsonPath.getInt("data.records"), 1,
                "Job with one assigned candidate should produce records=1");

        String template = jsonPath.getString("data.template");
        Assert.assertTrue(template.contains("SubmissionSingle Candidate"),
                "Template should include the only assigned candidate");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_GenerateMode_WithCustomColumns() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("job_id", jobId);
        requestBody.put("mode", "generate");
        requestBody.put("columns", buildColumnsParam(true, true, false));

        Response response = postSubmissionRequest(requestBody);
        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 with custom columns. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        assertGeneratedEmailPayload(jsonPath);

        String template = jsonPath.getString("data.template");
        Assert.assertTrue(template.contains("Name"), "Custom columns should include Name header");
        Assert.assertTrue(template.contains("Skills") || template.contains("Java, API Testing, Selenium"),
                "Custom columns should include skills content");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_GenerateMode_WithLogoFlagsFromSettings() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("job_id", jobId);
        requestBody.put("mode", "generate");

        Response response = postSubmissionRequest(requestBody);
        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jsonPath = response.jsonPath();
        assertGeneratedEmailPayload(jsonPath);
        Assert.assertNotNull(jsonPath.get("data.analytics.include_company_logo"),
                "analytics.include_company_logo should reflect saved agent settings");
        Assert.assertNotNull(jsonPath.get("data.analytics.include_client_company_logo"),
                "analytics.include_client_company_logo should reflect saved agent settings");
    }

    // ==================== Preview Mode ====================

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_PreviewMode_HappyPath() {
        Response response = postSubmissionRequest(buildPreviewRequest(jobId,
                "Create a professional candidate submission email with a clean table layout."));

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 OK for preview mode. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        assertPreviewEmailPayload(jsonPath);
        Assert.assertTrue(jsonPath.getString("data.html").toLowerCase().contains("candidate"),
                "Preview html should contain candidate-related content");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_PreviewMode_WithCustomPrompt() {
        String customPrompt = "Create a polished candidate submission email with navy accents and a concise summary table.";
        Response response = postSubmissionRequest(buildPreviewRequest(jobId, customPrompt));
        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jsonPath = response.jsonPath();
        assertPreviewEmailPayload(jsonPath);
        Assert.assertTrue(jsonPath.getString("data.html").toLowerCase().contains("html"),
                "Custom preview prompt should still return rendered html");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_PreviewMode_MissingInstructionPrompt() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("job_id", jobId);
        requestBody.put("mode", "preview");

        Response response = postSubmissionRequest(requestBody);
        Assert.assertEquals(response.getStatusCode(), 400,
                "Preview mode requires instruction_prompt. Response: " + response.getBody().asString());
        Assert.assertEquals(response.jsonPath().getString("meta.message_type"), "is-fail",
                "Missing preview prompt should return is-fail");
    }

    // ==================== Error Scenarios ====================

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_InvalidToken() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("job_id", jobId);
        requestBody.put("mode", "generate");

        Response response = postSubmissionRequestWithToken("InvalidToken", requestBody);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 Unauthorized for invalid token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_InvalidMode() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("job_id", jobId);
        requestBody.put("mode", "invalid_mode");

        Response response = postSubmissionRequest(requestBody);
        int statusCode = response.getStatusCode();
        Assert.assertTrue(statusCode == 400 || statusCode == 422,
                "Expected 400 or 422 for invalid mode, got: " + statusCode);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_MissingJobId() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "generate");

        Response response = postSubmissionRequest(requestBody);
        int statusCode = response.getStatusCode();
        Assert.assertTrue(statusCode == 400 || statusCode == 422,
                "Expected 400 or 422 when job_id is missing, got: " + statusCode);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_InvalidJobId() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("job_id", 999999999);
        requestBody.put("mode", "generate");

        Response response = postSubmissionRequest(requestBody);
        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 for non-existent job id. Response: " + response.getBody().asString());
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_JobWithNoAssignedCandidates() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("job_id", emptyJobId);
        requestBody.put("mode", "generate");

        Response response = postSubmissionRequest(requestBody);
        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 when job has no assigned candidates. Response: " + response.getBody().asString());
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_EmptyCandidateIdsFallsBackToAllAssigned() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("job_id", jobId);
        requestBody.put("mode", "generate");
        requestBody.put("candidate_ids", new JSONArray());

        Response response = postSubmissionRequest(requestBody);
        Assert.assertEquals(response.getStatusCode(), 200,
                "Empty candidate_ids falls back to all assigned candidates on the job. Response: "
                        + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        assertGeneratedEmailPayload(jsonPath);
        Assert.assertTrue(jsonPath.getInt("data.records") >= 2,
                "Fallback generation should include all assigned candidates on the job");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyGenerateSubmissionEmail_InvalidCandidateIdsFallsBackToAllAssigned() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("job_id", jobId);
        requestBody.put("mode", "generate");
        requestBody.put("candidate_ids", new JSONArray().put(999999999));

        Response response = postSubmissionRequest(requestBody);
        Assert.assertEquals(response.getStatusCode(), 200,
                "Invalid candidate_ids falls back to all assigned candidates on the job. Response: "
                        + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        assertGeneratedEmailPayload(jsonPath);
        Assert.assertTrue(jsonPath.getInt("data.records") >= 2,
                "Fallback generation should include assigned candidates when candidate_ids are invalid");
    }
}
