package io.recruitcrm.albatross.neptune;

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

import java.util.HashMap;
import java.util.Map;

@AccountType("Business|AlbatrossTkn|aiTestParser")
public class AiMatchingJobTest extends TestBase {

    private static final String BASE_PATH = "jobs/{job_slug}/job-candidate-matching";

    private static final String RICH_JD_TEXT =
            "Senior Java Backend Developer with 5+ years of experience building large-scale distributed systems. "
                    + "Required skills: Java 17, Spring Boot, REST APIs, microservices architecture, AWS (EC2, Lambda, S3, "
                    + "RDS), Kafka, PostgreSQL, Docker, Kubernetes. Strong experience with CI/CD pipelines (Jenkins, "
                    + "GitHub Actions), Agile methodologies and code review processes. Bachelor's degree in Computer "
                    + "Science or related field required, Master's preferred. Located in San Francisco, CA. The role "
                    + "involves designing and implementing backend services, mentoring junior engineers, and "
                    + "collaborating with product and design teams. Industry: Software Development / SaaS.";

    private commanFunction function = new commanFunction();
    private String albatrossAuthToken;
    private String apiAuthToken;
    private String richJobSlug;

    @BeforeClass
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        richJobSlug = createJobWithRichJd();
        Assert.assertNotNull(richJobSlug, "Setup: rich JD job slug must not be null");
    }

    private String createJobWithRichJd() {
        JsonPath companyJson = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String companySlug = companyJson.getString("slug");

        JsonPath contactJson = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
        String contactSlug = contactJson.getString("slug");

        JSONObject job = new JSONObject();
        job.put("name", "AI Matching Auto Test Job " + System.currentTimeMillis());
        job.put("company_slug", companySlug);
        job.put("contact_slug", contactSlug);
        job.put("number_of_openings", 1);
        job.put("job_type", 4);
        job.put("job_description_text", RICH_JD_TEXT);
        job.put("enable_job_application_form", 1);

        Response createResponse = RestClient.doPost("JSON", baseURL, "jobs",
                apiAuthToken, null, false, job);
        Assert.assertEquals(createResponse.getStatusCode(), 200,
                "Setup: job creation must return 200. Body: " + createResponse.getBody().asString());

        String slug = createResponse.jsonPath().getString("slug");
        Assert.assertNotNull(slug, "Setup: created job must have a slug");
        return slug;
    }

    private Response postMatching(String slug, String token, JSONObject body) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("job_slug", slug);
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
    public void verifyJobCandidateMatching_HappyPath() {
        Response response = postMatching(richJobSlug, albatrossAuthToken, null);
        assertMatchingResponseValid(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyJobCandidateMatching_ResponseStructure() {
        Response response = postMatching(richJobSlug, albatrossAuthToken, null);
        assertMatchingResponseValid(response);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyJobCandidateMatching_DataFieldShape() {
        Response response = postMatching(richJobSlug, albatrossAuthToken, null);
        Assert.assertEquals(response.getStatusCode(), 200);

        Object data = response.jsonPath().get("data");
        Assert.assertNotNull(data, "data must be present in response");
        Assert.assertTrue(data instanceof java.util.List || data instanceof java.util.Map,
                "data should be a list (when no matches) or a map with records (when matches exist). Got: "
                        + data.getClass().getName());
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyJobCandidateMatching_InvalidSlug() {
        Response response = postMatching("non-existent-job-slug-aim", albatrossAuthToken, null);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Albatross convention: invalid slug returns HTTP 200 with is-danger");

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.getString("message_type"), "is-danger",
                "Invalid slug should return message_type=is-danger");
        Assert.assertEquals(jsonPath.getString("status"), "fail",
                "Invalid slug should return status=fail");
        Assert.assertEquals(jsonPath.getString("message"), "Job not found",
                "Should report 'Job not found' for invalid job slug");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifyJobCandidateMatching_InvalidToken() {
        Response response = postMatching(richJobSlug, "InvalidTokenValue", null);
        Assert.assertEquals(response.getStatusCode(), 401,
                "Expected 401 for invalid token. Response: " + response.getBody().asString());
    }
}
