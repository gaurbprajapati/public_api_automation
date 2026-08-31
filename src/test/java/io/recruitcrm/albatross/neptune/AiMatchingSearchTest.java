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

import java.util.HashMap;
import java.util.Map;

@AccountType("Business|AlbatrossTkn")
public class AiMatchingSearchTest extends TestBase {

    private static final String ENDPOINT = "ai-matching/search/get";

    private String albatrossAuthToken;

    private static final String SAMPLE_JD =
            "Senior Java Developer with 5+ years of experience building backend services. "
            + "Required skills: Spring Boot, REST APIs, microservices, AWS, Kafka, PostgreSQL. "
            + "Location: San Francisco, CA. Bachelor's degree in Computer Science required. "
            + "Industry: Software Development. Must have experience with CI/CD pipelines and Agile methodologies.";

    private static final String SAMPLE_RESUME =
            "John Doe - Senior Software Engineer based in Austin, TX. 8 years experience in Java, "
            + "Spring Boot, microservices, AWS, Docker, Kubernetes. Master's degree in Computer Science. "
            + "Worked at fintech and SaaS companies on payment systems and large-scale APIs.";

    @BeforeClass
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
    }

    private JSONObject buildRequestBody(String rawText) {
        JSONObject entity = new JSONObject();
        entity.put("raw_text", rawText);
        JSONObject body = new JSONObject();
        body.put("entity", entity);
        body.put("weights", new JSONObject());
        return body;
    }

    private Response postSearch(JSONObject body, Map<String, String> queryParams) {
        return RestClient.doPost("JSON", neptuneServiceURL, ENDPOINT,
                albatrossAuthToken, queryParams, false, body);
    }

    private Response postSearchWithToken(String token, JSONObject body) {
        return RestClient.doPost("JSON", neptuneServiceURL, ENDPOINT,
                token, null, false, body);
    }

    private void assertFiltersPayload(JsonPath jsonPath) {
        Assert.assertEquals(jsonPath.getString("meta.message_type"), "is-success",
                "Expected meta.message_type=is-success");
        Assert.assertEquals(jsonPath.getInt("meta.status"), 200,
                "Expected meta.status=200");
        Assert.assertNotNull(jsonPath.get("meta.request_UUID"), "meta.request_UUID should be present");

        Assert.assertNotNull(jsonPath.get("data.filters"), "data.filters should be present");
        Assert.assertNotNull(jsonPath.get("data.filters.job_titles"), "filters.job_titles should be present");
        Assert.assertNotNull(jsonPath.get("data.filters.location"), "filters.location should be present");
        Assert.assertNotNull(jsonPath.get("data.filters.experience"), "filters.experience should be present");
        Assert.assertNotNull(jsonPath.get("data.filters.skills"), "filters.skills should be present");
        Assert.assertNotNull(jsonPath.get("data.filters.tools_tech"), "filters.tools_tech should be present");
        Assert.assertNotNull(jsonPath.get("data.filters.industries"), "filters.industries should be present");
        Assert.assertNotNull(jsonPath.get("data.filters.education"), "filters.education should be present");
        Assert.assertNotNull(jsonPath.get("data.filters.languages"), "filters.languages should be present");
        Assert.assertNotNull(jsonPath.get("data.filters.additional_keywords"),
                "filters.additional_keywords should be present");
        Assert.assertNotNull(jsonPath.get("data.filters.excluded_keywords"),
                "filters.excluded_keywords should be present");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySearchGet_HappyPath_JdText() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("page_size", "10");

        Response response = postSearch(buildRequestBody(SAMPLE_JD), queryParams);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 OK for valid JD text. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        assertFiltersPayload(jsonPath);

        String primaryTitle = jsonPath.getString("data.filters.job_titles.primary");
        Assert.assertNotNull(primaryTitle, "filters.job_titles.primary must be present");
        Assert.assertFalse(primaryTitle.trim().isEmpty(),
                "filters.job_titles.primary must be non-empty for JD with 'Senior Java Developer'");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySearchGet_HappyPath_ResumeText() {
        Response response = postSearch(buildRequestBody(SAMPLE_RESUME), null);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 OK for valid resume text. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        assertFiltersPayload(jsonPath);

        String primaryTitle = jsonPath.getString("data.filters.job_titles.primary");
        Assert.assertNotNull(primaryTitle, "filters.job_titles.primary must be present");
        Assert.assertFalse(primaryTitle.trim().isEmpty(),
                "filters.job_titles.primary must be non-empty for resume text");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySearchGet_ResponseStructure() {
        Response response = postSearch(buildRequestBody(SAMPLE_JD), null);
        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jsonPath = response.jsonPath();
        assertFiltersPayload(jsonPath);

        Assert.assertNotNull(jsonPath.get("data.filters.job_titles.primary"),
                "job_titles.primary should be present");
        Assert.assertNotNull(jsonPath.getList("data.filters.job_titles.synonyms"),
                "job_titles.synonyms should be a list");

        Assert.assertNotNull(jsonPath.getList("data.filters.skills.must_have"),
                "skills.must_have should be a list");
        Assert.assertNotNull(jsonPath.getList("data.filters.skills.good_to_have"),
                "skills.good_to_have should be a list");

        Assert.assertNotNull(jsonPath.getList("data.filters.tools_tech.must_have"),
                "tools_tech.must_have should be a list");

        Assert.assertNotNull(jsonPath.get("data.filters.location.specified"),
                "location.specified should be present");
        Assert.assertNotNull(jsonPath.get("data.filters.location.coordinates"),
                "location.coordinates should be present");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySearchGet_PaginationParams() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("page_size", "5");

        Response response = postSearch(buildRequestBody(SAMPLE_JD), queryParams);
        Assert.assertEquals(response.getStatusCode(), 200,
                "Expected 200 with pagination params. Response: " + response.getBody().asString());
        assertFiltersPayload(response.jsonPath());
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySearchGet_ToolsTechExtracted() {
        Response response = postSearch(buildRequestBody(SAMPLE_JD), null);
        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jsonPath = response.jsonPath();
        java.util.List<String> tools = jsonPath.getList("data.filters.tools_tech.must_have");
        Assert.assertNotNull(tools, "tools_tech.must_have should be a list");
        Assert.assertFalse(tools.isEmpty(),
                "tools_tech.must_have should contain at least one entry for JD mentioning Spring Boot/AWS");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySearchGet_EmptyBody() {
        Response response = RestClient.doPost("JSON", neptuneServiceURL, ENDPOINT,
                albatrossAuthToken, null, false, new JSONObject());

        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 for empty body. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.getString("meta.message_type"), "is-fail",
                "Validation error should return is-fail");
        Assert.assertEquals(jsonPath.getInt("meta.status"), 400, "meta.status should be 400");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySearchGet_MissingEntity() {
        JSONObject body = new JSONObject();
        body.put("weights", new JSONObject());

        Response response = postSearch(body, null);
        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 when entity is missing. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.getString("meta.message_type"), "is-fail");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySearchGet_EmptyRawText() {
        Response response = postSearch(buildRequestBody(""), null);
        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 for empty raw_text. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.getString("meta.message_type"), "is-fail");
        Assert.assertNotNull(jsonPath.get("errors.errors.entity"),
                "errors.errors.entity should describe the validation failure");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySearchGet_MissingRawTextInsideEntity() {
        JSONObject entity = new JSONObject();
        JSONObject body = new JSONObject();
        body.put("entity", entity);
        body.put("weights", new JSONObject());

        Response response = postSearch(body, null);
        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 when entity.raw_text is missing. Response: " + response.getBody().asString());
        Assert.assertEquals(response.jsonPath().getString("meta.message_type"), "is-fail");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySearchGet_MissingWeights() {
        JSONObject entity = new JSONObject();
        entity.put("raw_text", SAMPLE_JD);
        JSONObject body = new JSONObject();
        body.put("entity", entity);

        Response response = postSearch(body, null);
        Assert.assertEquals(response.getStatusCode(), 400,
                "Expected 400 when weights field is missing. Response: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.getString("meta.message_type"), "is-fail");
        Assert.assertNotNull(jsonPath.get("errors.errors.weights"),
                "errors.errors.weights should describe missing weights field");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySearchGet_InvalidToken() {
        Response response = postSearchWithToken("InvalidTokenValue", buildRequestBody(SAMPLE_JD));
        Assert.assertEquals(response.getStatusCode(), 401,
                "Expected 401 Unauthorized for invalid token. Response: " + response.getBody().asString());
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void verifySearchGet_WrongHttpMethod() {
        Response response = RestClient.doGet("JSON", neptuneServiceURL, ENDPOINT,
                albatrossAuthToken, null, null, false);
        Assert.assertEquals(response.getStatusCode(), 405,
                "Expected 405 Method Not Allowed for GET. Response: " + response.getBody().asString());
    }
}
