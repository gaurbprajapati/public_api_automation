package io.recruitcrm.JobService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import io.rcrm.api.commanfunctions.commanFunction;
import org.testng.annotations.BeforeClass;
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
public class GetJobDetailsPageFieldCountTest extends TestBase {

    String albatrossTkn;
    int accountId;
    int userId;
    int adminId;
    commanFunction function = new commanFunction();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        accountId = ThreadManager.getAccount().getAccountId();

        Response getUsers = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
        assertThat("Failed to get users", getUsers.getStatusCode(), equalTo(200));
        JsonPath jp = getUsers.jsonPath();
        userId = jp.get("[0].id");
        adminId = jp.get("[1].id");
        assertThat("User ID should not be null", userId, notNullValue());
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testGetJobDetailsPageFieldsCountSuccess() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", String.valueOf(userId));
        queryParams.put("entityType", "jobs");

        Response response = RestClient.doGet("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Details page field count fetched successfully"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", jp.get("meta.responseType.code"), equalTo(103));

        assertThat("MyViewCount should not be null", jp.get("data.myViewCount"), equalTo(16));
        assertThat("OthersViewCount should not be null", jp.get("data.othersViewCount"), equalTo(16));
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testGetJobDetailsPageFieldsCountWithoutAuth() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", String.valueOf(userId));
        queryParams.put("entityType", "jobs");

        Response response = RestClient.doGet("JSON", jobServiceURL, "details-page/fields-count",
                "", queryParams, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", jp.get("meta.responseType.code"), equalTo(104));
        assertThat("Data should contain error message", jp.get("data"), equalTo("Missing bearer token in header"));
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testGetJobDetailsPageFieldsCountInvalidAuth() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", String.valueOf(userId));
        queryParams.put("entityType", "jobs");

        Response response = RestClient.doGet("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn + "invalid_token", queryParams, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", jp.get("meta.responseType.code"), equalTo(104));
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testGetJobDetailsPageFieldsCountMissingUserId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("entityType", "jobs");

        Response response = RestClient.doGet("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(400));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should match", jp.get("errors[0].message"), equalTo("userId cannot be null"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("ErrorType code should be 201", jp.get("errors[0].errorType.code"), equalTo(201));
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testGetJobDetailsPageFieldsCountMissingAccountId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("userId", String.valueOf(userId));
        queryParams.put("entityType", "jobs");

        Response response = RestClient.doGet("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(400));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should match", jp.get("errors[0].message"), equalTo("accountId cannot be null"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("ErrorType code should be 201", jp.get("errors[0].errorType.code"), equalTo(201));
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testGetJobDetailsPageFieldsCountMissingEntityType() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", String.valueOf(userId));

        Response response = RestClient.doGet("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(400));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should match", jp.get("errors[0].message"), equalTo("entityType cannot be null"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("ErrorType code should be 201", jp.get("errors[0].errorType.code"), equalTo(201));
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testGetJobDetailsPageFieldsCountInvalidAccountId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", "-999999");
        queryParams.put("userId", String.valueOf(userId));
        queryParams.put("entityType", "jobs");

        Response response = RestClient.doGet("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("Data should be null", jp.get("data"), nullValue());
        assertThat("Error message should match", jp.get("errors[0].message"), equalTo("Unauthorized"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Generic Error"));
        assertThat("ErrorType code should be 202", jp.get("errors[0].errorType.code"), equalTo(202));
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testGetJobDetailsPageFieldsCountInvalidUserId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", "-999999");
        queryParams.put("entityType", "jobs");

        Response response = RestClient.doGet("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("Data should be null", jp.get("data"), nullValue());
        assertThat("Error message should match", jp.get("errors[0].message"), equalTo("Unauthorized"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Generic Error"));
        assertThat("ErrorType code should be 202", jp.get("errors[0].errorType.code"), equalTo(202));
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testGetJobDetailsPageFieldsCountWithNonOwnerUserId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", String.valueOf(adminId));
        queryParams.put("entityType", "jobs");

        Response response = RestClient.doGet("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, queryParams, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("Data should be null", jp.get("data"), nullValue());
        assertThat("Error message should match", jp.get("errors[0].message"), equalTo("Unauthorized"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Generic Error"));
        assertThat("ErrorType code should be 202", jp.get("errors[0].errorType.code"), equalTo(202));
    }
}
