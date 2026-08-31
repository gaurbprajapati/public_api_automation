package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.reaper.ThreadManager;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCandidatesTest extends TestBase {

    String albatrossTkn;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidatesSuccess(String candidateSlug, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("slug", candidateSlug);

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates", 
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Candidates Details fetched successfully"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Candidates object should not be null", jp.get("data.candidates"), notNullValue());

        // Verify candidate details
        assertThat("Candidate ID should match", (Integer) jp.get("data.candidates.id"), equalTo(recordId));
        assertThat("Candidate slug should match", jp.get("data.candidates.slug"), equalTo(candidateSlug));
        assertThat("First name should not be null", jp.get("data.candidates.firstname"), notNullValue());
        assertThat("Last name should not be null", jp.get("data.candidates.lastname"), notNullValue());
        assertThat("Email should not be null", jp.get("data.candidates.emailid"), notNullValue());
        assertThat("Account ID should not be null", (Integer) jp.get("data.candidates.accountid"), greaterThan(0));
        assertThat("Owner ID should not be null", (Integer) jp.get("data.candidates.ownerid"), greaterThan(0));
        assertThat("Created on should not be null", (Integer) jp.get("data.candidates.createdon"), greaterThan(0));
        assertThat("Updated on should not be null", (Integer) jp.get("data.candidates.updatedon"), greaterThan(0));

        // Verify pagination fields
        assertThat("Next field should exist", jp.get("data.next"), notNullValue());
        assertThat("Previous field should exist", jp.get("data.previous"), notNullValue());

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/candidate/getCandidates.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidatesWithoutAuth(String candidateSlug, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("slug", candidateSlug);

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates", 
                null, queryParams, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", (Integer) jp.get("meta.responseType.code"), equalTo(104));

        // Verify data field
        assertThat("Data should contain error message", jp.get("data"), equalTo("Internal Server Error"));
        assertThat("Errors array should be empty", jp.get("errors"), notNullValue());
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidatesInvalidAuth(String candidateSlug, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("slug", candidateSlug);

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates", 
                albatrossTkn + "invalid_token", queryParams, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidatesInvalidSlug(String candidateSlug, int recordId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("slug", candidateSlug + "dsnf");

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates", 
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 404", (Integer) jp.get("meta.status"), equalTo(404));
        assertThat("Message should be null for error case", jp.get("meta.message"), nullValue());
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", (Integer) jp.get("meta.responseType.code"), equalTo(101));

        // Verify data and errors
        assertThat("Data should be null", jp.get("data"), nullValue());
        assertThat("Errors array should not be null", jp.get("errors"), notNullValue());
        assertThat("Errors array should not be empty", (Integer) jp.get("errors.size()"), greaterThan(0));
        assertThat("Error message should match", jp.get("errors[0].message"), equalTo("Candidate not found for slug: " + candidateSlug + "dsnf"));
        assertThat("Error type should not be null", jp.get("errors[0].errorType"), notNullValue());
        assertThat("Error type context should match", jp.get("errors[0].errorType.context"), equalTo("Generic Error"));
        assertThat("Error type code should be 202", (Integer) jp.get("errors[0].errorType.code"), equalTo(202));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testGetCandidatesMissingSlug() {
        Response response = RestClient.doGet("JSON", candidatesURL, "candidates", 
                albatrossTkn, null, null, true);

        assertThat("Status code should be either 400", response.getStatusCode(), equalTo(400));
    }

    @DataProvider(name = "candidateTestData")
    public Object[][] getCandidateTestData() {
        AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
        Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
        
        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
        
        JsonPath jp = candidateResponse.jsonPath();
        String candidateSlug = jp.get("data.candidate.slug");
        int recordId = jp.get("data.candidate.id");
        
        assertThat("Candidate slug should not be null", candidateSlug, notNullValue());
        assertThat("Record ID should not be null", recordId, notNullValue());
        
        return new Object[][] { { candidateSlug, recordId } };
    }
}
