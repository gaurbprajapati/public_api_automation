package io.recruitcrm.albatross.neptune;

import com.qa.api.util.S3Uploader;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AccountType("Business|AlbatrossTkn|aiTestParser")
public class AiMatchingLlmScoringIntegrationTest extends TestBase {

    private static final String C2C_PATH = "candidates/{candidate_slug}/candidate-matching";
    private static final String J2C_PATH = "jobs/{job_slug}/job-candidate-matching";

    private static final String RICH_JD =
            "Senior Java Backend Developer with 5+ years of experience. Required: Java 17, Spring Boot, "
                    + "REST APIs, microservices, AWS, Kafka, PostgreSQL, Docker, Kubernetes. "
                    + "Location: San Francisco, CA. Bachelor's in Computer Science required.";

    private commanFunction function = new commanFunction();
    private String albatrossAuthToken;
    private String apiAuthToken;
    private String parsedCandidateSlug;
    private String richJobSlug;

    @BeforeClass
    public void setUp() throws Exception {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();

        parsedCandidateSlug = parseResumeAndGetCandidateSlug("ArjunReddyResume.pdf");
        parseResumeAndGetCandidateSlug("SampleResume.pdf");
        richJobSlug = createJobWithRichJd();

        Assert.assertNotNull(parsedCandidateSlug, "Setup: parsed candidate slug must not be null");
        Assert.assertNotNull(richJobSlug, "Setup: rich JD job slug must not be null");
    }

    private String parseResumeAndGetCandidateSlug(String resumeFileName) throws Exception {
        String resumeFilePath = System.getProperty("user.dir")
                + "/src/main/java/io/rcrm/api/testdata/" + resumeFileName;
        File resumeFile = new File(resumeFilePath);
        Assert.assertTrue(resumeFile.exists(), "Resume PDF must exist at: " + resumeFilePath);

        Map<String, String> presignedQuery = new HashMap<>();
        presignedQuery.put("fileName", resumeFile.getName());
        presignedQuery.put("requestType", "put");

        Response presignedResponse = RestClient.doGet("JSON", albatrossURL, "get-presigned-url",
                albatrossAuthToken, presignedQuery, null, false);
        Assert.assertEquals(presignedResponse.getStatusCode(), 200);

        JsonPath presignedJson = presignedResponse.jsonPath();
        String encryptedKey = presignedJson.getString("data.key");
        String preSignedUrl = presignedJson.getString("data.preSignedUrl");
        S3Uploader.uploadFileToS3(preSignedUrl, resumeFile.getAbsolutePath(), "application/pdf");

        JSONObject filesInfo = new JSONObject();
        filesInfo.put("key", encryptedKey);
        filesInfo.put("name", resumeFile.getName());
        filesInfo.put("type", "application/pdf");
        filesInfo.put("size", resumeFile.length());
        filesInfo.put("index", 0);

        JSONObject resumeParserData = new JSONObject();
        resumeParserData.put("resumesParsed", 0);
        resumeParserData.put("resumesFailed", 0);
        resumeParserData.put("resumesTotal", 1);
        resumeParserData.put("filesInfo", filesInfo);

        JSONObject parseRequest = new JSONObject();
        parseRequest.put("resumeParserData", resumeParserData);
        parseRequest.put("actionid", 0);

        Map<String, String> parseQuery = new HashMap<>();
        parseQuery.put("actionsteps", "1");

        Response parseResponse = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume",
                albatrossAuthToken, parseQuery, null, false, parseRequest);
        Assert.assertEquals(parseResponse.getStatusCode(), 200);
        Assert.assertEquals(parseResponse.jsonPath().getString("message_type"), "is-success");

        return parseResponse.jsonPath().getString("data.candidate.slug");
    }

    private String createJobWithRichJd() {
        JsonPath companyJson = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String companySlug = companyJson.getString("slug");
        JsonPath contactJson = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
        String contactSlug = contactJson.getString("slug");

        JSONObject job = new JSONObject();
        job.put("name", "LLM Scoring Test Job " + System.currentTimeMillis());
        job.put("company_slug", companySlug);
        job.put("contact_slug", contactSlug);
        job.put("number_of_openings", 1);
        job.put("job_type", 4);
        job.put("job_description_text", RICH_JD);
        job.put("enable_job_application_form", 1);

        Response createResponse = RestClient.doPost("JSON", baseURL, "jobs",
                apiAuthToken, null, false, job);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        return createResponse.jsonPath().getString("slug");
    }

    private Response postC2C(String slug) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidate_slug", slug);
        return RestClient.doPost1("JSON", albatrossURL, C2C_PATH, albatrossAuthToken,
                null, pathParams, false, new JSONObject());
    }

    private Response postJ2C(String slug) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("job_slug", slug);
        return RestClient.doPost1("JSON", albatrossURL, J2C_PATH, albatrossAuthToken,
                null, pathParams, false, new JSONObject());
    }

    private void assertNoEngineErrorMessage(String message, String body) {
        if (message != null && !message.trim().isEmpty()) {
            String lower = message.toLowerCase();
            Assert.assertFalse(
                    lower.contains("error") || lower.contains("exception")
                            || lower.contains("internal") || lower.contains("timeout")
                            || lower.contains("unavailable") || lower.contains("traceback"),
                    "Response contains engine-error keywords. message='" + message + "'. Body: " + body);
        }
    }

    private void assertLlmScoringFieldsOnSuccess(JsonPath jsonPath, String body) {
        String messageType = jsonPath.getString("message_type");
        if (!"is-success".equals(messageType) && !"success".equals(messageType)) {
            assertNoEngineErrorMessage(jsonPath.getString("message"), body);
            return;
        }

        Assert.assertNotNull(jsonPath.get("data.llm_status"),
                "On success, data.llm_status must be present. Body: " + body);
        String llmStatus = jsonPath.getString("data.llm_status");
        Assert.assertTrue(
                "pending".equals(llmStatus) || "success".equals(llmStatus) || "cooldown".equals(llmStatus),
                "data.llm_status must be pending, success, or cooldown. Got: " + llmStatus + ". Body: " + body);

        Assert.assertNotNull(jsonPath.get("data.cooldown"),
                "data.cooldown must be present. Body: " + body);
        Assert.assertNotNull(jsonPath.get("data.cooldown_remaining_seconds"),
                "data.cooldown_remaining_seconds must be present. Body: " + body);

        List<Map<String, Object>> records = jsonPath.getList("data.records");
        if (records != null && !records.isEmpty() && "success".equals(llmStatus)) {
            Map<String, Object> firstRecord = records.get(0);
            Assert.assertNotNull(firstRecord.get("llm_score"),
                    "When llm_status=success, record must contain llm_score. Body: " + body);
            Assert.assertNotNull(firstRecord.get("llm_status"),
                    "When llm_status=success, record must contain llm_status. Body: " + body);

            Object llmScoring = firstRecord.get("llm_scoring");
            if (llmScoring instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> scoring = (Map<String, Object>) llmScoring;
                Assert.assertNotNull(scoring.get("o"),
                        "llm_scoring.o (overall score) must be present when llm_scoring object exists");
                for (String dim : new String[]{"fn", "sk", "lv", "tr", "dm", "lc", "l"}) {
                    Assert.assertNotNull(scoring.get(dim),
                            "llm_scoring." + dim + " must be present. Body: " + body);
                }
            }
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScoringIntegration_C2C_LlmStatusFields() {
        Response response = postC2C(parsedCandidateSlug);
        String body = response.getBody().asString();

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected HTTP 200 from candidate-matching. Body: " + body);
        assertLlmScoringFieldsOnSuccess(response.jsonPath(), body);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScoringIntegration_J2C_LlmStatusFields() {
        Response response = postJ2C(richJobSlug);
        String body = response.getBody().asString();

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected HTTP 200 from job-candidate-matching. Body: " + body);
        assertLlmScoringFieldsOnSuccess(response.jsonPath(), body);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScoringIntegration_C2C_SecondCallChecksCooldownOrSuccess() {
        Response first = postC2C(parsedCandidateSlug);
        Assert.assertEquals(first.getStatusCode(), 200);

        Response second = postC2C(parsedCandidateSlug);
        String body = second.getBody().asString();
        Assert.assertEquals(second.getStatusCode(), 200, "Second call must return HTTP 200. Body: " + body);

        JsonPath jsonPath = second.jsonPath();
        String messageType = jsonPath.getString("message_type");

        if ("is-warning".equals(messageType)) {
            Assert.assertEquals(jsonPath.getString("status"), "cooldown",
                    "Cooldown response must have status=cooldown. Body: " + body);
            Assert.assertEquals(jsonPath.getString("data.llm_status"), "cooldown",
                    "Cooldown response must have data.llm_status=cooldown. Body: " + body);
            Assert.assertEquals(jsonPath.getBoolean("data.cooldown"), true,
                    "Cooldown response must have data.cooldown=true. Body: " + body);
            Assert.assertTrue(jsonPath.getInt("data.cooldown_remaining_seconds") >= 0,
                    "cooldown_remaining_seconds must be >= 0. Body: " + body);
        } else {
            assertLlmScoringFieldsOnSuccess(jsonPath, body);
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScoringIntegration_C2C_InvalidToken() {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidate_slug", parsedCandidateSlug);
        Response response = RestClient.doPost1("JSON", albatrossURL, C2C_PATH, "InvalidTokenValue",
                null, pathParams, false, new JSONObject());
        Assert.assertEquals(response.getStatusCode(), 401);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScoringIntegration_J2C_InvalidToken() {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("job_slug", richJobSlug);
        Response response = RestClient.doPost1("JSON", albatrossURL, J2C_PATH, "InvalidTokenValue",
                null, pathParams, false, new JSONObject());
        Assert.assertEquals(response.getStatusCode(), 401);
    }
}
