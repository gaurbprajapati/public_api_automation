package io.recruitcrm.CandidateService;

import java.util.HashMap;
import java.util.Map;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetSystemInformationTest extends TestBase {
    
    private static final String BASE_PATH = "candidates/system-information";
    
    private commanFunction function = new commanFunction();
    private String albatrossAuthToken;
    private String accountBToken;
    private String candidateSlug;
    private String accountA_apiKey;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        setupCrossAccountTokens();
        albatrossAuthToken = getTokenForAccount("AccountA", "valid");
        accountBToken = getTokenForAccount("AccountB", "valid");
        accountA_apiKey = getAccountApiKey("AccountA");
        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, accountA_apiKey).jsonPath();
        candidateSlug = jsonCandidate.get("slug");
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getSystemInformation_Success() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("candidateSlug", candidateSlug);

        Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/GetSystemInformation.json"));
        
        JsonPath jsonPath = response.jsonPath();
        
        assertThat("Expected success message", jsonPath.get("meta.message"), equalTo("Candidate System Information fetched successfully"));
        assertThat("Expected status 200 in meta", jsonPath.getInt("meta.status"), equalTo(200));
        assertThat("Expected success context", jsonPath.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Expected response code 103", jsonPath.getInt("meta.responseType.code"), equalTo(103));
        assertThat("Request UUID should not be null", jsonPath.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.get("meta.timestamp"), notNullValue());
        
        assertThat("Data should not be null", jsonPath.get("data"), notNullValue());
        assertThat("lastUpdatedOn should not be null", jsonPath.get("data.lastUpdatedOn"), notNullValue());
        assertThat("lastUpdatedBy should not be null", jsonPath.get("data.lastUpdatedBy"), notNullValue());
        assertThat("lastUpdatedByName should not be null", jsonPath.get("data.lastUpdatedByName"), notNullValue());
        assertThat("createdOn should not be null", jsonPath.get("data.createdOn"), notNullValue());
        assertThat("createdBy should not be null", jsonPath.get("data.createdBy"), notNullValue());
        assertThat("createdByName should not be null", jsonPath.get("data.createdByName"), notNullValue());
        assertThat("lastEmailSentByName field should exist", jsonPath.get("data.lastEmailSentByName"), notNullValue());
        assertThat("lastCallLogAddedByName field should exist", jsonPath.get("data.lastCallLogAddedByName"), notNullValue());
        assertThat("lastSmsSentByName field should exist", jsonPath.get("data.lastSmsSentByName"), notNullValue());
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getSystemInformation_WithoutAuth() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("candidateSlug", candidateSlug);

        Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, "", queryParameters, null, true);

        assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/GetSystemInformationUnauthorized.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected unauthorized access message", jsonPath.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Expected status 401 in meta", jsonPath.getInt("meta.status"), equalTo(401));
        assertThat("Expected warning context", jsonPath.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("Expected response code 104", jsonPath.getInt("meta.responseType.code"), equalTo(104));
        assertThat("Request UUID should not be null", jsonPath.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.get("meta.timestamp"), notNullValue());
        assertThat("Expected data to contain missing bearer token message", jsonPath.get("data"), equalTo("Missing bearer token in header"));
        assertThat("Errors array should exist", jsonPath.get("errors"), notNullValue());
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getSystemInformation_InvalidAuth() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("candidateSlug", candidateSlug);

        Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, albatrossAuthToken + "invalid", queryParameters, null, true);

        assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/GetSystemInformationUnauthorized.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected unauthorized access message", jsonPath.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Expected status 401 in meta", jsonPath.getInt("meta.status"), equalTo(401));
        assertThat("Expected warning context", jsonPath.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("Expected response code 104", jsonPath.getInt("meta.responseType.code"), equalTo(104));
        assertThat("Request UUID should not be null", jsonPath.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.get("meta.timestamp"), notNullValue());
        assertThat("Expected data to contain unauthorized error message", jsonPath.get("data"), equalTo("Invalid or expired token"));
        assertThat("Errors array should exist", jsonPath.get("errors"), notNullValue());
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getSystemInformation_InvalidCandidateSlug() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("candidateSlug", "invalid-slug-12345");

        Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 404", response.getStatusCode(), equalTo(404));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/GetSystemInformationNotFound.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected message to be null", jsonPath.get("meta.message"), nullValue());
        assertThat("Expected status 404 in meta", jsonPath.getInt("meta.status"), equalTo(404));
        assertThat("Expected error context", jsonPath.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("Expected response code 101", jsonPath.getInt("meta.responseType.code"), equalTo(101));
        assertThat("Request UUID should not be null", jsonPath.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.get("meta.timestamp"), notNullValue());
        assertThat("Expected data to be null", jsonPath.get("data"), nullValue());
        assertThat("Errors array should exist", jsonPath.get("errors"), notNullValue());
        assertThat("Errors array should not be empty", jsonPath.getList("errors").size(), greaterThan(0));
        assertThat("Error message should contain 'not found'", jsonPath.get("errors[0].message"), containsString("not found"));
        assertThat("Error context should be Generic Error", jsonPath.get("errors[0].errorType.context"), equalTo("Generic Error"));
        assertThat("Error code should be 202", jsonPath.getInt("errors[0].errorType.code"), equalTo(202));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getSystemInformation_MissingCandidateSlug() {
        Map<String, String> queryParameters = new HashMap<>();

        Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 400", response.getStatusCode(), equalTo(400));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/GetSystemInformationBadRequest.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected status 400", jsonPath.getInt("status"), equalTo(400));
        assertThat("Expected error message", jsonPath.get("error"), equalTo("Bad Request"));
        assertThat("Expected path to match system-information endpoint", jsonPath.get("path"), equalTo("/v2/candidates/system-information"));
        assertThat("Timestamp should not be null", jsonPath.get("timestamp"), notNullValue());
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getSystemInformation_EmptyCandidateSlug() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("candidateSlug", null);

        Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 500", response.getStatusCode(), equalTo(500));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/GetSystemInformationInternalServerError.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected status 500", jsonPath.getInt("status"), equalTo(500));
        assertThat("Expected error message", jsonPath.get("error"), equalTo("Internal Server Error"));
        assertThat("Expected path to match system-information endpoint", jsonPath.get("path"), equalTo("/v2/candidates/system-information"));
        assertThat("Timestamp should not be null", jsonPath.get("timestamp"), notNullValue());
    }

    @Owner("Ajendra Singh")
    @Test
    public void getSystemInformation_CrossAccountAccess() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("candidateSlug", candidateSlug);

        Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, accountBToken, queryParameters, null, true);

        assertThat("Expected status code 401 for cross-account access", response.getStatusCode(), equalTo(401));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/GetSystemInformationUnauthorized.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected unauthorized access message", jsonPath.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Expected status 401 in meta", jsonPath.getInt("meta.status"), equalTo(401));
        assertThat("Expected warning context", jsonPath.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("Expected response code 104", jsonPath.getInt("meta.responseType.code"), equalTo(104));
        assertThat("Request UUID should not be null", jsonPath.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.get("meta.timestamp"), notNullValue());
        assertThat("Errors array should exist", jsonPath.get("errors"), notNullValue());
    }
}

