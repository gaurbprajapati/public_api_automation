package io.recruitcrm.albatross.neptune;

import com.qa.api.util.S3Uploader;
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@AccountType("Business|AlbatrossTkn|aiTestParser")
public class AiMatchingCandidateTest extends TestBase {

    private static final String BASE_PATH = "candidates/{candidate_slug}/candidate-matching";

    private String albatrossAuthToken;
    private String parsedCandidateSlug;

    @BeforeClass
    public void setUp() throws Exception {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        // Parse two resumes so the source candidate has at least one peer to match against.
        parsedCandidateSlug = parseResumeAndGetCandidateSlug("ArjunReddyResume.pdf");
        parseResumeAndGetCandidateSlug("SampleResume.pdf");
        Assert.assertNotNull(parsedCandidateSlug, "Setup: parsed candidate slug must not be null");
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
        Assert.assertEquals(presignedResponse.getStatusCode(), 200,
                "Setup: get-presigned-url must return 200");

        JsonPath presignedJson = presignedResponse.jsonPath();
        String encryptedKey = presignedJson.getString("data.key");
        String preSignedUrl = presignedJson.getString("data.preSignedUrl");
        Assert.assertNotNull(preSignedUrl, "Setup: presigned URL is required");

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
        Assert.assertEquals(parseResponse.getStatusCode(), 200,
                "Setup: parse-resume must return 200. Body: " + parseResponse.getBody().asString());
        Assert.assertEquals(parseResponse.jsonPath().getString("message_type"), "is-success",
                "Setup: parse-resume must return is-success");

        String slug = parseResponse.jsonPath().getString("data.candidate.slug");
        Assert.assertNotNull(slug, "Setup: parsed candidate must have a slug");
        return slug;
    }

    private Response postMatching(String slug, String token, JSONObject body) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidate_slug", slug);
        return RestClient.doPost1("JSON", albatrossURL, BASE_PATH, token, null, pathParams, false,
                body == null ? new JSONObject() : body);
    }

    /**
     * Strict response contract: must be 200, must contain the standard Albatross envelope,
     * and the response must be either a clean success (is-success/success with data.records list)
     * OR the documented empty-result case (is-danger with empty message and status=fail).
     * Any other shape (5xx, missing fields, is-danger with non-empty unexpected message) is a regression.
     */
    private void assertMatchingResponseValid(Response response) {
        String body = response.getBody().asString();
        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected HTTP 200 (Albatross envelope, errors are in body). Body: " + body);

        JsonPath jsonPath = response.jsonPath();
        Assert.assertNotNull(jsonPath.get("message_type"),
                "message_type must be present. Body: " + body);
        Assert.assertNotNull(jsonPath.get("status"),
                "status must be present. Body: " + body);
        Assert.assertNotNull(jsonPath.get("silent_progress"),
                "silent_progress must be present. Body: " + body);
        Assert.assertNotNull(jsonPath.get("application_version"),
                "application_version must be present. Body: " + body);
        Assert.assertNotNull(jsonPath.get("user"),
                "user must be present. Body: " + body);

        String messageType = jsonPath.getString("message_type");
        String message = jsonPath.getString("message");
        String status = jsonPath.getString("status");

        if ("is-success".equals(messageType) || "success".equals(messageType)) {
            // Success path: data must be a map with records list and total_count.
            Object data = jsonPath.get("data");
            Assert.assertTrue(data instanceof java.util.Map,
                    "On success, data must be a map with records/total_count. Got: "
                            + (data == null ? "null" : data.getClass().getName()) + ". Body: " + body);
            Assert.assertNotNull(jsonPath.get("data.records"),
                    "On success, data.records must be present. Body: " + body);
            Assert.assertTrue(jsonPath.get("data.records") instanceof java.util.List,
                    "On success, data.records must be a list. Body: " + body);
            Assert.assertNotNull(jsonPath.get("data.total_count"),
                    "On success, data.total_count must be present. Body: " + body);
        } else if ("is-danger".equals(messageType)) {
            // Documented "no matches" case: empty message + status=fail.
            // Any non-empty message containing engine-error keywords is a regression.
            Assert.assertEquals(status, "fail",
                    "When message_type=is-danger, status must be 'fail'. Body: " + body);
            if (message != null && !message.trim().isEmpty()) {
                String lower = message.toLowerCase();
                Assert.assertFalse(
                        lower.contains("error") || lower.contains("exception")
                                || lower.contains("internal") || lower.contains("timeout")
                                || lower.contains("unavailable") || lower.contains("traceback"),
                        "is-danger response contains engine-error keywords. message='" + message
                                + "'. Body: " + body);
            }
        } else {
            Assert.fail("Unexpected message_type: " + messageType + ". Body: " + body);
        }
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyCandidateMatching_HappyPath() {
        Response response = postMatching(parsedCandidateSlug, albatrossAuthToken, null);
        assertMatchingResponseValid(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyCandidateMatching_ResponseStructure() {
        Response response = postMatching(parsedCandidateSlug, albatrossAuthToken, null);
        assertMatchingResponseValid(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyCandidateMatching_DataFieldShape() {
        Response response = postMatching(parsedCandidateSlug, albatrossAuthToken, null);
        Assert.assertEquals(response.getStatusCode(), 200);

        Object data = response.jsonPath().get("data");
        Assert.assertNotNull(data, "data must be present in response");
        Assert.assertTrue(data instanceof java.util.List || data instanceof java.util.Map,
                "data should be a list (when no matches) or a map with records (when matches exist). Got: "
                        + data.getClass().getName());
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyCandidateMatching_InvalidSlug() {
        Response response = postMatching("non-existent-candidate-slug-aim", albatrossAuthToken, null);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Albatross convention: invalid slug returns HTTP 200 with is-danger");

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.getString("message_type"), "is-danger",
                "Invalid slug should return message_type=is-danger");
        Assert.assertEquals(jsonPath.getString("status"), "fail",
                "Invalid slug should return status=fail");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyCandidateMatching_InvalidToken() {
        Response response = postMatching(parsedCandidateSlug, "InvalidTokenValue", null);
        Assert.assertEquals(response.getStatusCode(), 401,
                "Expected 401 for invalid token. Response: " + response.getBody().asString());
    }
}
