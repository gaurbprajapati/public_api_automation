package io.recruitcrm.albatross.neptune;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

import java.util.Map;

@AccountType("Business|AlbatrossTkn")
public class AiMatchingLlmScoreTest extends TestBase {

    private static final String ENDPOINT = "ai-matching/llm-score";

    private static final String SOURCE_JD =
            "Senior Java Developer with 5+ years of experience building backend services. "
                    + "Required skills: Spring Boot, REST APIs, microservices, AWS, Kafka, PostgreSQL. "
                    + "Location: San Francisco, CA. Bachelor's degree in Computer Science required.";

    private static final String CANDIDATE_RESUME =
            "John Doe - Senior Software Engineer based in Austin, TX. 8 years experience in Java, "
                    + "Spring Boot, microservices, AWS, Docker, Kubernetes. Master's degree in Computer Science.";

    private static final String[] SCORE_DIMENSIONS = {"fn", "sk", "lv", "tr", "dm", "lc"};

    private String albatrossAuthToken;

    @BeforeClass
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
    }

    private JSONObject buildScoreRequest(String sourceText, int filterTypeId, int candidateId, String candidateText) {
        JSONObject candidate = new JSONObject();
        candidate.put("id", candidateId);
        candidate.put("candidate_text", candidateText);

        JSONArray candidates = new JSONArray();
        candidates.put(candidate);

        JSONObject body = new JSONObject();
        body.put("source_text", sourceText);
        body.put("filter_type_id", filterTypeId);
        body.put("candidates", candidates);
        return body;
    }

    private Response postLlmScore(JSONObject body) {
        return RestClient.doPost("JSON", neptuneServiceURL, ENDPOINT,
                albatrossAuthToken, null, false, body);
    }

    private Response postLlmScoreWithToken(String token, JSONObject body) {
        return RestClient.doPost("JSON", neptuneServiceURL, ENDPOINT,
                token, null, false, body);
    }

    private void assertSuccessMeta(JsonPath jsonPath) {
        Assert.assertEquals(jsonPath.getString("meta.message_type"), "is-success",
                "meta.message_type must be is-success");
        Assert.assertEquals(jsonPath.getInt("meta.status"), 200, "meta.status must be 200");
        Assert.assertNotNull(jsonPath.get("meta.request_UUID"), "meta.request_UUID must be present");
        Assert.assertNotNull(jsonPath.getString("meta.message"), "meta.message must be present");
    }

    private void assertCandidateScoreResult(JsonPath jsonPath, String candidateKey) {
        String prefix = "data.results." + candidateKey;
        Assert.assertEquals(jsonPath.getBoolean(prefix + ".ok"), true,
                "results[" + candidateKey + "].ok must be true");
        Assert.assertNull(jsonPath.get(prefix + ".err"),
                "results[" + candidateKey + "].err must be null on success");

        for (String dimension : SCORE_DIMENSIONS) {
            String dimPath = prefix + ".data." + dimension;
            Assert.assertNotNull(jsonPath.get(dimPath + ".s"),
                    dimension + ".s (score) must be present");
            Assert.assertNotNull(jsonPath.get(dimPath + ".r"),
                    dimension + ".r (reasoning) must be present");

            int score = jsonPath.getInt(dimPath + ".s");
            Assert.assertTrue(score >= 0 && score <= 100,
                    dimension + ".s must be between 0 and 100, got: " + score);

            String reasoning = jsonPath.getString(dimPath + ".r");
            Assert.assertFalse(reasoning.trim().isEmpty(),
                    dimension + ".r (reasoning) must be non-empty");
        }

        int overallScore = jsonPath.getInt(prefix + ".data.o");
        Assert.assertTrue(overallScore >= 0 && overallScore <= 100,
                "data.o (overall score) must be between 0 and 100, got: " + overallScore);

        String overallReasoning = jsonPath.getString(prefix + ".data.or");
        String label = jsonPath.getString(prefix + ".data.l");
        Assert.assertNotNull(label, "data.l (match label) must be present");
        Assert.assertFalse(label.trim().isEmpty(), "data.l (match label) must be non-empty");

        boolean hasOverallNarrative = (overallReasoning != null && !overallReasoning.trim().isEmpty())
                || !label.trim().isEmpty();
        Assert.assertTrue(hasOverallNarrative,
                "Either data.or (overall reasoning) or data.l (match label) must provide overall match narrative");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScore_C2C_HappyPath() {
        Response response = postLlmScore(buildScoreRequest(SOURCE_JD, 1, 1, CANDIDATE_RESUME));

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 OK for C2C scoring. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        assertSuccessMeta(jsonPath);
        assertCandidateScoreResult(jsonPath, "1");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScore_J2C_HappyPath() {
        Response response = postLlmScore(buildScoreRequest(SOURCE_JD, 2, 1, CANDIDATE_RESUME));

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 OK for J2C scoring. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        assertSuccessMeta(jsonPath);
        assertCandidateScoreResult(jsonPath, "1");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScore_ResponseStructure() {
        Response response = postLlmScore(buildScoreRequest(SOURCE_JD, 1, 1, CANDIDATE_RESUME));
        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jsonPath = response.jsonPath();
        assertSuccessMeta(jsonPath);

        Map<String, Object> results = jsonPath.getMap("data.results");
        Assert.assertNotNull(results, "data.results must be a map");
        Assert.assertFalse(results.isEmpty(), "data.results must contain at least one candidate entry");
        Assert.assertTrue(results.containsKey("1"), "data.results must contain key for candidate id 1");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScore_MultipleCandidates() {
        JSONObject candidate1 = new JSONObject();
        candidate1.put("id", 101);
        candidate1.put("candidate_text", CANDIDATE_RESUME);

        JSONObject candidate2 = new JSONObject();
        candidate2.put("id", 102);
        candidate2.put("candidate_text",
                "Jane Smith - Backend Developer with 6 years Java, Spring, AWS experience in Dallas, TX.");

        JSONArray candidates = new JSONArray();
        candidates.put(candidate1);
        candidates.put(candidate2);

        JSONObject body = new JSONObject();
        body.put("source_text", SOURCE_JD);
        body.put("filter_type_id", 1);
        body.put("candidates", candidates);

        Response response = postLlmScore(body);
        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 for batch scoring multiple candidates. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        assertSuccessMeta(jsonPath);
        assertCandidateScoreResult(jsonPath, "101");
        assertCandidateScoreResult(jsonPath, "102");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScore_EmptyCandidatesList() {
        JSONObject body = new JSONObject();
        body.put("source_text", SOURCE_JD);
        body.put("filter_type_id", 1);
        body.put("candidates", new JSONArray());

        Response response = postLlmScore(body);
        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 for empty candidates list. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        assertSuccessMeta(jsonPath);
        Assert.assertEquals(jsonPath.getString("meta.message"), "Batch scoring complete",
                "Empty batch should return Batch scoring complete message");

        Map<String, Object> results = jsonPath.getMap("data.results");
        Assert.assertNotNull(results, "data.results must be present");
        Assert.assertTrue(results.isEmpty(), "data.results should be empty for empty candidates list");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScore_MissingSourceText() {
        JSONObject body = buildScoreRequest(SOURCE_JD, 1, 1, CANDIDATE_RESUME);
        body.remove("source_text");

        Response response = postLlmScore(body);
        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 when source_text is missing. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.getString("meta.message_type"), "is-fail");
        Assert.assertNotNull(jsonPath.get("errors.errors.source_text"),
                "errors.errors.source_text should describe missing field");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScore_MissingCandidates() {
        JSONObject body = new JSONObject();
        body.put("source_text", SOURCE_JD);
        body.put("filter_type_id", 1);

        Response response = postLlmScore(body);
        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 when candidates is missing. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.getString("meta.message_type"), "is-fail");
        Assert.assertNotNull(jsonPath.get("errors.errors.candidates"),
                "errors.errors.candidates should describe missing field");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScore_MissingCandidateId() {
        JSONObject candidate = new JSONObject();
        candidate.put("candidate_text", CANDIDATE_RESUME);

        JSONArray candidates = new JSONArray();
        candidates.put(candidate);

        JSONObject body = new JSONObject();
        body.put("source_text", SOURCE_JD);
        body.put("filter_type_id", 1);
        body.put("candidates", candidates);

        Response response = postLlmScore(body);
        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 when candidate id is missing. Response: " + response.getBody().asString());
        Assert.assertEquals(response.jsonPath().getString("meta.message_type"), "is-fail");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScore_EmptyBody() {
        Response response = RestClient.doPost("JSON", neptuneServiceURL, ENDPOINT,
                albatrossAuthToken, null, false, new JSONObject());
        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 for empty body. Response: " + response.getBody().asString());
        Assert.assertEquals(response.jsonPath().getString("meta.message_type"), "is-fail");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScore_InvalidToken() {
        Response response = postLlmScoreWithToken("InvalidTokenValue",
                buildScoreRequest(SOURCE_JD, 1, 1, CANDIDATE_RESUME));
        Assert.assertEquals(response.getStatusCode(), 401,
                "Expected 401 Unauthorized for invalid token");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScore_WrongHttpMethod() {
        Response response = RestClient.doGet("JSON", neptuneServiceURL, ENDPOINT,
                albatrossAuthToken, null, null, false);
        Assert.assertEquals(response.getStatusCode(), 405,
                "Expected 405 Method Not Allowed for GET. Response: " + response.getBody().asString());
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyLlmScore_C2CAndJ2C_ProduceDifferentReasoning() {
        Response c2cResponse = postLlmScore(buildScoreRequest(SOURCE_JD, 1, 1, CANDIDATE_RESUME));
        Response j2cResponse = postLlmScore(buildScoreRequest(SOURCE_JD, 2, 1, CANDIDATE_RESUME));

        Assert.assertEquals(c2cResponse.getStatusCode(), 200);
        Assert.assertEquals(j2cResponse.getStatusCode(), 200);

        String c2cReasoning = c2cResponse.jsonPath().getString("data.results.1.data.fn.r");
        String j2cReasoning = j2cResponse.jsonPath().getString("data.results.1.data.fn.r");

        Assert.assertNotNull(c2cReasoning, "C2C fn reasoning must be present");
        Assert.assertNotNull(j2cReasoning, "J2C fn reasoning must be present");
        Assert.assertFalse(c2cReasoning.trim().isEmpty(), "C2C fn reasoning must be non-empty");
        Assert.assertFalse(j2cReasoning.trim().isEmpty(), "J2C fn reasoning must be non-empty");
    }
}
