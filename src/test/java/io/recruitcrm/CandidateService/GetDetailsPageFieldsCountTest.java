package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
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
public class GetDetailsPageFieldsCountTest extends TestBase {

    String albatrossTkn;
    String restrictedUserAlbatrossTkn;
    int accountId;
    int userId;
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        restrictedUserAlbatrossTkn = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        // Get dynamic account ID
        accountId = ThreadManager.getAccount().getAccountId();
        
        // Get dynamic user ID
        Response getUsers = albatrossFunctions.getUsers(albatrossURL, albatrossTkn);
        assertThat("Failed to get users", getUsers.getStatusCode(), equalTo(200));
        JsonPath jp = getUsers.jsonPath();
        userId = jp.get("data.records[0].id");
        assertThat("User ID should not be null", userId, notNullValue());
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testGetDetailsPageFieldsCountSuccess() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", String.valueOf(userId));

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/details-page/fields-count", 
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Candidates details page field count fetched successfully"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("MyViewCount should not be null", jp.get("data.myViewCount"), notNullValue());
        assertThat("OthersViewCount should not be null", jp.get("data.othersViewCount"), notNullValue());

        // Verify field count values are non-negative integers
        assertThat("Default MyViewCount should be 16", (Integer) jp.get("data.myViewCount"), equalTo(16));
        assertThat("Default OthersViewCount should be 16", (Integer) jp.get("data.othersViewCount"), equalTo(16));

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/candidate/detailsPageFieldsCount.json"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testGetDetailsPageFieldsCountWithoutAuth() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", String.valueOf(userId));

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/details-page/fields-count", 
                null, queryParams, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure for error
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", (Integer) jp.get("meta.responseType.code"), equalTo(104));

        // Verify error data
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
        assertThat("Errors array should be empty", jp.get("errors"), notNullValue());
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testGetDetailsPageFieldsCountInvalidAuth() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", String.valueOf(userId));

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/details-page/fields-count", 
                albatrossTkn+"invalid_token", queryParams, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure for error
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", (Integer) jp.get("meta.responseType.code"), equalTo(104));

        // Verify error data
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
        assertThat("Errors array should be empty", jp.get("errors"), notNullValue());
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testGetDetailsPageFieldsCountMissingUserId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/details-page/fields-count", 
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 400", (Integer) jp.get("meta.status"), equalTo(400));
        assertThat("Message should be null for validation errors", jp.get("meta.message"), nullValue());
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure for error
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", (Integer) jp.get("meta.responseType.code"), equalTo(101));

        // Verify error data and validation errors
        assertThat("Data should be null for validation errors", jp.get("data"), nullValue());
        assertThat("Errors array should not be null", jp.get("errors"), notNullValue());
        assertThat("Errors array should not be empty", (Integer) jp.get("errors.size()"), greaterThan(0));
        assertThat("Error message should match", jp.get("errors[0].message"), equalTo("userId cannot be null"));
        assertThat("Error type should not be null", jp.get("errors[0].errorType"), notNullValue());
        assertThat("Error type context should match", jp.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("Error type code should be 201", (Integer) jp.get("errors[0].errorType.code"), equalTo(201));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testGetDetailsPageFieldsCountMissingAccountId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("userId", String.valueOf(userId));

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/details-page/fields-count", 
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 400", (Integer) jp.get("meta.status"), equalTo(400));
        assertThat("Message should be null for validation errors", jp.get("meta.message"), nullValue());
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure for error
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", (Integer) jp.get("meta.responseType.code"), equalTo(101));

        // Verify error data and validation errors
        assertThat("Data should be null for validation errors", jp.get("data"), nullValue());
        assertThat("Errors array should not be null", jp.get("errors"), notNullValue());
        assertThat("Errors array should not be empty", (Integer) jp.get("errors.size()"), greaterThan(0));
        assertThat("Error message should match", jp.get("errors[0].message"), equalTo("accountId cannot be null"));
        assertThat("Error type should not be null", jp.get("errors[0].errorType"), notNullValue());
        assertThat("Error type context should match", jp.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("Error type code should be 201", (Integer) jp.get("errors[0].errorType.code"), equalTo(201));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testGetDetailsPageFieldsCountInvalidUserId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", "invalid_user_id");

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/details-page/fields-count", 
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 400", (Integer) jp.get("meta.status"), equalTo(400));
        assertThat("Message should be null for validation errors", jp.get("meta.message"), nullValue());
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure for error
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", (Integer) jp.get("meta.responseType.code"), equalTo(101));

        // Verify error data and validation errors
        assertThat("Data should be null for validation errors", jp.get("data"), nullValue());
        assertThat("Errors array should not be null", jp.get("errors"), notNullValue());
        assertThat("Errors array should not be empty", (Integer) jp.get("errors.size()"), greaterThan(0));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testGetDetailsPageFieldsCountInvalidAccountId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", "invalid_account_id");
        queryParams.put("userId", String.valueOf(userId));

        Response response = RestClient.doGet("JSON", candidatesURL, "candidates/details-page/fields-count", 
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 400", (Integer) jp.get("meta.status"), equalTo(400));
        assertThat("Message should be null for validation errors", jp.get("meta.message"), nullValue());
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure for error
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", (Integer) jp.get("meta.responseType.code"), equalTo(101));

        // Verify error data and validation errors
        assertThat("Data should be null for validation errors", jp.get("data"), nullValue());
        assertThat("Errors array should not be null", jp.get("errors"), notNullValue());
        assertThat("Errors array should not be empty", (Integer) jp.get("errors.size()"), greaterThan(0));
    }

    // PUT Endpoint Tests - Update Details Page Fields Count
    @Owner("Raj Pandey")
    @Test(dataProvider = "updateFieldsCountTestData", groups = {"candidate_service", "nightly-build"})
    public void testUpdateDetailsPageFieldsCountSuccess(int myViewFieldsCount, int otherViewFieldsCount) {
        // Step 1: Get current field counts
        Response getResponse = getCurrentFieldCounts();
        assertThat("Get should succeed before update", getResponse.getStatusCode(), equalTo(200));
        
        // Step 2: Update field counts
        JSONObject requestBody = new JSONObject();
        requestBody.put("myViewFieldsCount", myViewFieldsCount);
        requestBody.put("otherViewFieldsCount", otherViewFieldsCount);

        Response response = RestClient.doPut("JSON", candidatesURL, "candidates/details-page/fields-count", 
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Details Page Field Count Updated successfully."));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data is null for update response
        assertThat("Data should be null for update response", jp.get("data"), nullValue());

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/candidate/updateDetailsPageFieldsCount.json"));

        // Step 3: Verify the update by getting the data again
        Response verifyResponse = getCurrentFieldCounts();
        assertThat("Get should succeed after update", verifyResponse.getStatusCode(), equalTo(200));
        
        JsonPath verifyJp = verifyResponse.jsonPath();
        assertThat("MyViewCount should be updated", (Integer) verifyJp.get("data.myViewCount"), equalTo(myViewFieldsCount));
        assertThat("OthersViewCount should be updated", (Integer) verifyJp.get("data.othersViewCount"), equalTo(otherViewFieldsCount));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "updateFieldsCountTestData", groups = {"candidate_service", "nightly-build"})
    public void testUpdateDetailsPageFieldsCountWithoutAuth(int myViewFieldsCount, int otherViewFieldsCount) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("myViewFieldsCount", myViewFieldsCount);
        requestBody.put("otherViewFieldsCount", otherViewFieldsCount);

        Response response = RestClient.doPut("JSON", candidatesURL, "candidates/details-page/fields-count", 
                null, null, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure for error
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", (Integer) jp.get("meta.responseType.code"), equalTo(104));

        // Verify error data
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
        assertThat("Errors array should be empty", jp.get("errors"), notNullValue());
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "updateFieldsCountTestData", groups = {"candidate_service", "nightly-build"})
    public void testUpdateDetailsPageFieldsCountInvalidAuth(int myViewFieldsCount, int otherViewFieldsCount) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("myViewFieldsCount", myViewFieldsCount);
        requestBody.put("otherViewFieldsCount", otherViewFieldsCount);

        Response response = RestClient.doPut("JSON", candidatesURL, "candidates/details-page/fields-count", 
                albatrossTkn + "invalid_token", null, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure for error
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", (Integer) jp.get("meta.responseType.code"), equalTo(104));

        // Verify error data
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
        assertThat("Errors array should be empty", jp.get("errors"), notNullValue());
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testUpdateDetailsPageFieldsCountEmptyRequestBody() {
        JSONObject requestBody = new JSONObject();
        // Empty request body

        Response response = RestClient.doPut("JSON", candidatesURL, "candidates/details-page/fields-count", 
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Details Page Field Count Updated successfully."));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data is null for update response
        assertThat("Data should be null for update response", jp.get("data"), nullValue());
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testUpdateDetailsPageFieldsCountInvalidMyViewFieldsCount() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("myViewFieldsCount", "invalid_number");
        requestBody.put("otherViewFieldsCount", 5);

        Response response = RestClient.doPut("JSON", candidatesURL, "candidates/details-page/fields-count", 
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testUpdateDetailsPageFieldsCountInvalidOtherViewFieldsCount() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("myViewFieldsCount", 10);
        requestBody.put("otherViewFieldsCount", "invalid_number");

        Response response = RestClient.doPut("JSON", candidatesURL, "candidates/details-page/fields-count", 
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testUpdateDetailsPageFieldsCountNegativeValues() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("myViewFieldsCount", -5);
        requestBody.put("otherViewFieldsCount", -2);

        Response response = RestClient.doPut("JSON", candidatesURL, "candidates/details-page/fields-count", 
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    // Helper method to get current field counts
    private Response getCurrentFieldCounts() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", String.valueOf(userId));

        return RestClient.doGet("JSON", candidatesURL, "candidates/details-page/fields-count", 
                albatrossTkn, queryParams, null, true);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testUpdateDetailsPageFieldsCountForOtherUserAndVerifyRestrictedUserAccess() {
        // Step 1: Get restricted user ID (different from owner)
        Response getUsersResponse = albatrossFunctions.getUsers(albatrossURL, albatrossTkn);
        assertThat("Failed to get users", getUsersResponse.getStatusCode(), equalTo(200));
        JsonPath usersJp = getUsersResponse.jsonPath();
        
        // Find a restricted user or use a different user from the list
        int restrictedUserId = 0;
        int usersSize = usersJp.get("data.records.size()");
        
        // Try to find a user that's not the owner (usually users[0] is owner)
        if (usersSize > 1) {
            restrictedUserId = usersJp.get("data.records[1].id");
            assertThat("Restricted user ID should be different from owner user ID", restrictedUserId, not(equalTo(userId)));
        } else {
            // If only one user, we'll use the same user ID but test with restricted token
            restrictedUserId = usersJp.get("data.records[0].id");
        }
        
        assertThat("Restricted user ID should not be null", restrictedUserId, notNullValue());
        
        // Step 2: As account owner, update field count for the restricted user
        int updatedMyViewCount = 20;
        int updatedOtherViewCount = 5;
        
        JSONObject updateRequestBody = new JSONObject();
        updateRequestBody.put("myViewFieldsCount", updatedMyViewCount);
        updateRequestBody.put("otherViewFieldsCount", updatedOtherViewCount);
        
        // Try with userId as query parameter (account owner updating for other user)
        Map<String, String> updateQueryParams = new HashMap<>();
        updateQueryParams.put("userId", String.valueOf(restrictedUserId));
        
        Response ownerUpdateResponse = RestClient.doPut("JSON", candidatesURL, "candidates/details-page/fields-count", 
                albatrossTkn, updateQueryParams, true, updateRequestBody.toString());
        
        assertThat("Account owner should be able to update count for other user", 
                ownerUpdateResponse.getStatusCode(), equalTo(200));
        
        JsonPath ownerUpdateJp = ownerUpdateResponse.jsonPath();
        assertThat("Update message should match", ownerUpdateJp.get("meta.message"), 
                equalTo("Candidate details page field count updated successfully."));
        
        assertThat("Restricted user token should not be null", restrictedUserAlbatrossTkn, notNullValue());
        
        // Step 4: As restricted user, try to update the count (should fail - no permission)
        JSONObject restrictedUpdateRequestBody = new JSONObject();
        restrictedUpdateRequestBody.put("myViewFieldsCount", 25);
        
        Response restrictedUpdateResponse = RestClient.doPut("JSON", candidatesURL, "candidates/details-page/fields-count", 
                restrictedUserAlbatrossTkn, null, true, restrictedUpdateRequestBody.toString());
        
        // Restricted user should not be able to update (403 Forbidden or 401 Unauthorized)
        assertThat("Restricted user should not be able to update field count. Status: " + restrictedUpdateResponse.getStatusCode(),
                restrictedUpdateResponse.getStatusCode(), anyOf(equalTo(403), equalTo(401)));
        
        // Step 5: As restricted user, verify the count using GET endpoint (should succeed)
        Map<String, String> restrictedQueryParams = new HashMap<>();
        restrictedQueryParams.put("accountId", String.valueOf(accountId));
        restrictedQueryParams.put("userId", String.valueOf(restrictedUserId));
        
        Response restrictedGetResponse = RestClient.doGet("JSON", candidatesURL, "candidates/details-page/fields-count", 
                restrictedUserAlbatrossTkn, restrictedQueryParams, null, true);
        
        assertThat("Restricted user should be able to get field count", 
                restrictedGetResponse.getStatusCode(), equalTo(200));
        
        JsonPath restrictedGetJp = restrictedGetResponse.jsonPath();
        
        // Verify the count values match what owner updated
        assertThat("MyViewCount should match owner's update", 
                (Integer) restrictedGetJp.get("data.myViewCount"), equalTo(updatedMyViewCount));
        assertThat("OthersViewCount should match owner's update", 
                (Integer) restrictedGetJp.get("data.othersViewCount"), equalTo(updatedOtherViewCount));
    }

    @DataProvider(name = "updateFieldsCountTestData")
    public Object[][] getUpdateFieldsCountTestData() {
        return new Object[][] { {16, 2} };
    }
}
