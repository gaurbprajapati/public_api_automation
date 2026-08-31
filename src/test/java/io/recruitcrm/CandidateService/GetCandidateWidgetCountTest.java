package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCandidateWidgetCountTest extends TestBase {

    String albatrossTkn;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "widgetCountTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateWidgetCount_Success(String candidateId, String candidateSlug) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", candidateId);
        queryParams.put("candidateSlug", candidateSlug);

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/widget-count",
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Response type code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Widget Count Fetched Successfully"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());

        // Verify all required fields in data
        assertThat("relatedDealsCount should not be null", jp.get("data.relatedDealsCount"), notNullValue());
        assertThat("hotlistCount should not be null", jp.get("data.hotlistCount"), notNullValue());
        assertThat("assignedJobsCount should not be null", jp.get("data.assignedJobsCount"), notNullValue());
        assertThat("candidateQuestionsCount should not be null", jp.get("data.candidateQuestionsCount"), notNullValue());
        assertThat("pitchCandidatesCount should not be null", jp.get("data.pitchCandidatesCount"), notNullValue());

        // Verify all values are non-negative integers
        assertThat("relatedDealsCount should be non-negative", (Integer) jp.get("data.relatedDealsCount"), greaterThanOrEqualTo(0));
        assertThat("hotlistCount should be non-negative", (Integer) jp.get("data.hotlistCount"), greaterThanOrEqualTo(0));
        assertThat("assignedJobsCount should be non-negative", (Integer) jp.get("data.assignedJobsCount"), greaterThanOrEqualTo(0));
        assertThat("candidateQuestionsCount should be non-negative", (Integer) jp.get("data.candidateQuestionsCount"), greaterThanOrEqualTo(0));
        assertThat("pitchCandidatesCount should be non-negative", (Integer) jp.get("data.pitchCandidatesCount"), greaterThanOrEqualTo(0));

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/candidate/candidateWidgetCount.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "widgetCountTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateWidgetCount_WithoutAuth(String candidateId, String candidateSlug) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", candidateId);
        queryParams.put("candidateSlug", candidateSlug);

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/widget-count", 
                null, queryParams, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "widgetCountTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateWidgetCount_InvalidAuth(String candidateId, String candidateSlug) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", candidateId);
        queryParams.put("candidateSlug", candidateSlug);

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/widget-count",
                albatrossTkn + "23123", queryParams, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "widgetCountTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateWidgetCount_InvalidCandidateId(String candidateId, String candidateSlug) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", candidateId + "1212"); // Invalid candidate ID
        queryParams.put("candidateSlug", candidateSlug);

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/widget-count",
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "widgetCountTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateWidgetCount_MissingCandidateId(String candidateId, String candidateSlug) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateSlug", candidateSlug);
        // Missing candidateId parameter

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/widget-count",
                albatrossTkn, queryParams, null, true);

        // This should return 400 Bad Request for missing required parameter
        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "widgetCountTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateWidgetCount_MissingCandidateSlug(String candidateId, String candidateSlug) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", candidateId);
        // Missing candidateSlug parameter

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/widget-count",
                albatrossTkn, queryParams, null, true);

        // This should return 400 Bad Request for missing required parameter
        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "widgetCountTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateWidgetCount_InvalidCandidateSlug(String candidateId, String candidateSlug) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", candidateId);
        queryParams.put("candidateSlug", candidateSlug + "3212321");

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/widget-count",
                albatrossTkn, queryParams, null, true);

        // This should return 404 Not Found for invalid candidate slug
        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));
    }

    @DataProvider(name = "widgetCountTestData")
    public Object[][] getWidgetCountTestData() {
        AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
        Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);

        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
        
        JsonPath jp = candidateResponse.jsonPath();
        String candidateSlug = jp.get("data.candidate.slug");
        int candidateId = jp.get("data.candidate.id");
        
        assertThat("Candidate slug should not be null", candidateSlug, notNullValue());
        assertThat("Candidate ID should not be null", candidateId, notNullValue());
        
        return new Object[][] { { String.valueOf(candidateId), candidateSlug } };
    }
}
