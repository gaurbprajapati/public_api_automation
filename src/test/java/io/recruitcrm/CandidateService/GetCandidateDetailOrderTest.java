package io.recruitcrm.CandidateService;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@TestBase.AccountType("CrossAccount")
public class GetCandidateDetailOrderTest extends TestBase {

    // Cross account test fields
    private String tokenA;
    private String publicAPIKeyA;
    private String tokenB;
    private String publicAPIKeyB;
    private int actualUserId;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        tokenA = getTokenForAccount("AccountA", "valid");
        publicAPIKeyA = getAccountApiKey("AccountA");
        tokenB = getTokenForAccount("AccountB", "valid");
        publicAPIKeyB = getAccountApiKey("AccountB");
        getOwnerDetailsFromAPI();
    }


    @Owner("Suhel Bhadane")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testCandidateDetailOrderGet_Success() {
        String basePath = "candidates/detail-order";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("userId", String.valueOf(actualUserId));

        Response response = RestClient.doGet(
                "JSON", candidatesURL, basePath, tokenA, queryParameters, null, true);

        assert response != null;
        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
        assertMetaSuccess(response);

        // Schema validation
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/GetCandidateDetailOrder.json"));

        String candidateDetailOrder = response.jsonPath().getString("data.candidateAllDetailOrder");
        assertThat("Expected candidateAllDetailOrder not null", candidateDetailOrder, notNullValue());

        JSONObject detailOrderJson = new JSONObject(candidateDetailOrder);
        assertThat(detailOrderJson.has("0"), is(true));
        assertThat(detailOrderJson.getString("0"), equalTo("work_history"));
        assertThat(detailOrderJson.has("1"), is(true));
        assertThat(detailOrderJson.getString("1"), equalTo("education_history"));
        assertThat(detailOrderJson.has("2"), is(true));
        assertThat(detailOrderJson.getString("2"), equalTo("basic_details"));
        assertThat("Expected 3 items in candidateAllDetailOrder", detailOrderJson.length(), equalTo(3));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "invalidUserIdData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateDetailOrderGet_InvalidUserId(String userId, String description, String expectedMessage) {
        String basePath = "candidates/detail-order";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("userId", userId);

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, tokenA, queryParameters, null, true);

        assert response != null;
        assertThat("Expected status code 400 for " + description, response.getStatusCode(), equalTo(400));
        assertThat("Unexpected error message for " + description, response.jsonPath().getString("userId"), equalTo(expectedMessage));

    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testCandidateDetailOrderGet_MissingUserId() {
        String basePath = "candidates/detail-order";
        Response response = RestClient.doGet("JSON", candidatesURL, basePath, tokenA, new HashMap<>(), null, true);

        assert response != null;
        assertThat("Expected status code 400 for missing userId", response.getStatusCode(), equalTo(400));
        assertThat("Expected error message about null userId", response.jsonPath().getString("userId"), equalTo("userId cannot be null"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testCandidateDetailOrderGet_NonExistentUserId() {
        String basePath = "candidates/detail-order";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("userId", "999999");

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, tokenA, queryParameters, null, true);
		assert response != null;
        assertThat("Expected status code 401 for unauthorized access", response.getStatusCode(), equalTo(401));
        assertMetaError(response, 401, 101, "Error while processing request");

        assertThat("Expected data section to be null", response.jsonPath().get("data"), nullValue());
        assertThat("Expected errors section not null", response.jsonPath().get("errors"), notNullValue());
        assertThat("Expected error message 'Unauthorized'",
                response.jsonPath().getString("errors[0].message"), equalTo("Unauthorized"));
        assertThat("Expected errorType code 202",
                response.jsonPath().getInt("errors[0].errorType.code"), equalTo(202));
        assertThat("Expected generic error context",
                response.jsonPath().getString("errors[0].errorType.context"), equalTo("Generic Error"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testCandidateDetailOrderGet_UnauthorizedAccess() {
        String basePath = "candidates/detail-order";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("userId", String.valueOf(actualUserId));

        String invalidToken = tokenA + "invalid";
        Response response = RestClient.doGet("JSON", candidatesURL, basePath, invalidToken, queryParameters, null, true);
		assert response != null;
        assertThat("Expected status code 401 for unauthorized access", response.getStatusCode(), equalTo(401));
        assertThat("Expected 'Unauthorised access' message",
                response.jsonPath().getString("meta.message"), equalTo("Unauthorised access"));
        assertThat("Expected status 401 in meta",
                response.jsonPath().getInt("meta.status"), equalTo(401));
        assertThat("Expected responseType code 104",
                response.jsonPath().getInt("meta.responseType.code"), equalTo(104));
        assertThat("Expected warning context",
                response.jsonPath().getString("meta.responseType.context"), equalTo("Warning"));
        assertThat("Expected requestUuid not null",
                response.jsonPath().get("meta.requestUuid"), notNullValue());
        assertThat("Expected timestamp not null",
                response.jsonPath().get("meta.timestamp"), notNullValue());
        assertThat("Expected 'Invalid token' in data",
                response.jsonPath().getString("data"), equalTo("Invalid or expired token"));
        assertThat("Expected empty errors list",
                response.jsonPath().getList("errors").size(), equalTo(0));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testCandidateDetailOrderGet_MethodNotAllowed() {
        String basePath = "candidates/detail-order";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("userId", String.valueOf(actualUserId));

        Response response = RestClient.doPost(
                "JSON", candidatesURL, basePath, tokenA, queryParameters, true, null);

        assertThat("Expected status code 405 for method not allowed", response.getStatusCode(), equalTo(405));
        assertThat("Expected error to be 'Method Not Allowed'",
                response.jsonPath().getString("error"), equalTo("Method Not Allowed"));
        assertThat("Expected status field in body to be 405",
                response.jsonPath().getInt("status"), equalTo(405));
        assertThat("Expected path to match detail-order endpoint",
                response.jsonPath().getString("path"), containsString("/v2/candidates/detail-order"));
        assertThat("Expected timestamp not null", response.jsonPath().get("timestamp"), notNullValue());
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testCandidateDetailOrderUpdate_ThenGet_Success() {
        String[] desiredOrder = {"basic_details", "work_history", "education_history"};
        updateCandidateDetailOrder(true, true, true, desiredOrder);

        String getBasePath = "candidates/detail-order";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("userId", String.valueOf(actualUserId));

        Response getResponse = RestClient.doGet(
                "JSON", candidatesURL, getBasePath, tokenA, queryParameters, null, true);
		assert getResponse != null;
        assertThat("Expected status code 200", getResponse.getStatusCode(), equalTo(200));
        assertMetaSuccess(getResponse);

        // Schema validation
        getResponse.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/GetCandidateDetailOrder.json"));

        String candidateDetailOrder = getResponse.jsonPath().getString("data.candidateAllDetailOrder");
        assertThat("Expected candidateAllDetailOrder not null", candidateDetailOrder, notNullValue());

        JSONObject detailOrderJson = new JSONObject(candidateDetailOrder);


        JSONObject responseCollapseState = detailOrderJson.getJSONObject("collapseState");
        assertThat("Expected work_history in collapseState", responseCollapseState.has("work_history"), is(true));
        assertThat("Expected education_history in collapseState", responseCollapseState.has("education_history"), is(true));
        assertThat("Expected basic_details in collapseState", responseCollapseState.has("basic_details"), is(true));

        JSONArray responseDraggableSequal = detailOrderJson.getJSONArray("draggableSequal");
        assertThat("Expected draggableSequal array length 3", responseDraggableSequal.length(), equalTo(3));
        assertThat("Expected basic_details in draggableSequal", responseDraggableSequal.get(0), equalTo("basic_details"));
        assertThat("Expected work_history in draggableSequal", responseDraggableSequal.get(1), equalTo("work_history"));
        assertThat("Expected education_history in draggableSequal", responseDraggableSequal.get(2), equalTo("education_history"));

    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testCrossAccountGetCandidateDetailOrder() {
        String basePath = "candidates/detail-order";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("userId", String.valueOf(actualUserId));
        Response response = RestClient.doGet(
                "JSON", candidatesURL, basePath, tokenB, queryParameters, null, true);
        assert response != null;
        assertThat("Expected status code 401 for cross-account access", response.getStatusCode(), equalTo(401));

        // Validate meta block
        assertThat("Expected status 401 in meta", response.jsonPath().getInt("meta.status"), equalTo(401));
        assertThat("Expected requestUuid not null", response.jsonPath().get("meta.requestUuid"), notNullValue());
        assertThat("Expected timestamp not null", response.jsonPath().get("meta.timestamp"), notNullValue());
    }


    // ---------------- Helper Methods ----------------

    private void getOwnerDetailsFromAPI() {
        Response response = RestClient.doGet("JSON", baseURL, "users", publicAPIKeyA, null, null, true);
        if (response != null && response.getStatusCode() == 200) {
            JsonPath jsonPath = response.jsonPath();
            actualUserId = jsonPath.getInt("[0].id");
            assertThat("Expected valid user ID", actualUserId, greaterThan(0));
        } else {
            throw new AssertionError("Failed to retrieve owner details from API. Response: " +
                    (response != null ? response.getBody().asString() : "null response"));
        }
    }


    private void updateCandidateDetailOrder(boolean workHistoryCollapsed, boolean educationHistoryCollapsed, boolean basicDetailsCollapsed, String[] draggableOrder) {
        String updateBasePath = "get-candidate-details-order";

        JSONObject updatePayload = new JSONObject();
        updatePayload.put("id", actualUserId);
        updatePayload.put("type", "update");

        JSONObject collapseState = new JSONObject();
        collapseState.put("work_history", workHistoryCollapsed);
        collapseState.put("education_history", educationHistoryCollapsed);
        collapseState.put("basic_details", basicDetailsCollapsed);

        org.json.JSONArray draggableSequal = new org.json.JSONArray();
        for (String section : draggableOrder) {
            draggableSequal.put(section);
        }

        JSONObject orderObject = new JSONObject();
        orderObject.put("collapseState", collapseState);
        orderObject.put("draggableSequal", draggableSequal);

        updatePayload.put("order", orderObject);

        Response response = RestClient.doPost("JSON", albatrossURL, updateBasePath, tokenA, null, true, updatePayload);
        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
    }


    private void assertMetaSuccess(Response response) {
        assertThat("Expected success message", response.jsonPath().getString("meta.message"), equalTo("Candidate detail order fetched successfully"));
        assertThat("Expected status 200 in meta", response.jsonPath().getInt("meta.status"), equalTo(200));
        assertThat("Expected response type code 103", response.jsonPath().getInt("meta.responseType.code"), equalTo(103));
        assertThat("Expected success context", response.jsonPath().getString("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Expected requestUuid not null", response.jsonPath().get("meta.requestUuid"), notNullValue());
        assertThat("Expected timestamp not null", response.jsonPath().get("meta.timestamp"), notNullValue());
    }

    private void assertMetaError(Response response, int expectedStatus, int expectedCode, String expectedContext) {
        assertThat("Expected null message for error", response.jsonPath().get("meta.message"), nullValue());
        assertThat("Expected status in meta", response.jsonPath().get("meta.status"), equalTo(expectedStatus));
        assertThat("Expected response type code", response.jsonPath().get("meta.responseType.code"), equalTo(expectedCode));
        assertThat("Expected response context", response.jsonPath().getString("meta.responseType.context"), equalTo(expectedContext));
        assertThat("Expected requestUuid not null", response.jsonPath().get("meta.requestUuid"), notNullValue());
        assertThat("Expected timestamp not null", response.jsonPath().get("meta.timestamp"), notNullValue());
    }

    @DataProvider(name = "invalidUserIdData", parallel = true)
    public Object[][] invalidUserIdData() {
        return new Object[][]{
                {"-1", "negative userId", "userId must be positive"},
                {"0", "zero userId", "userId must be positive"},
                {"abc", "string userId",
                        "Failed to convert property value of type 'java.lang.String' to required type 'java.lang.Integer' for property 'userId'; For input string: \"abc\""}
        };
    }
}
