package io.recruitcrm.JobService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.Map;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.global.UpdateFieldWidgetCustomizationRequest;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class UpdateJobDetailsPageFieldCountTest extends TestBase {

    String albatrossTkn;
    String restrictedAuthToken;
    String authToken;
    int accountId;
    int userId;
    String adminTkn;
    commanFunction commanFunction = new commanFunction();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        restrictedAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        adminTkn = ThreadManager.getAlbatrossToken("Admin");
        authToken = ThreadManager.getAccountApiKey();
        accountId = ThreadManager.getAccount().getAccountId();

        Response usersResp = commanFunction.getUsers(baseURL, authToken);
        assertThat("Failed to get users", usersResp.getStatusCode(), equalTo(200));
        JsonPath jpUser = usersResp.jsonPath();

        userId = jpUser.get("[0].id");
        assertThat("User ID should not be null", userId, notNullValue());
    }

    @Owner("Harika")
    @Test(dataProvider = "updateFieldsCountTestData", groups = {"job_service", "nightly-build"})
    public void testUpdateJobDetailsPageFieldsCountSuccess(int myViewFieldsCount, int otherViewFieldsCount) {

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "jobs");
        requestBody.put("myViewFieldsCount", myViewFieldsCount);
        requestBody.put("otherViewFieldsCount", otherViewFieldsCount);

        Response response = RestClient.doPut("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Details Page Field Count Updated successfully."));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", jp.get("meta.responseType.code"), equalTo(103));

        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/job/updateDetailsPageFieldsCount.json"));

        Response verifyResponse = getCurrentFieldCounts();
        assertThat("Get should succeed after update", verifyResponse.getStatusCode(), equalTo(200));

        JsonPath verifyJp = verifyResponse.jsonPath();
        assertThat("MyViewCount should be updated", verifyJp.get("data.myViewCount"), equalTo(myViewFieldsCount));
        assertThat("OthersViewCount should be updated", verifyJp.get("data.othersViewCount"), equalTo(otherViewFieldsCount));
    }

    @Owner("Harika")
    @Test(dataProvider = "updateFieldsCountTestData", groups = {"job_service", "nightly-build"})
    public void testUpdateJobDetailsPageFieldsCountWithoutAuth(int myViewFieldsCount, int otherViewFieldsCount) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "jobs");
        requestBody.put("myViewFieldsCount", myViewFieldsCount);
        requestBody.put("otherViewFieldsCount", otherViewFieldsCount);

        Response response = RestClient.doPut("JSON", jobServiceURL, "details-page/fields-count",
                "", null, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Unauthorised access"));

        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", jp.get("meta.responseType.code"), equalTo(104));

        assertThat("Data should contain error message", jp.get("data"), equalTo("Missing bearer token in header"));
        assertThat("Errors array should be empty", jp.get("errors"), notNullValue());
    }

    @Owner("Harika")
    @Test(dataProvider = "updateFieldsCountTestData", groups = {"job_service", "nightly-build"})
    public void testUpdateJobDetailsPageFieldsCountInvalidAuth(int myViewFieldsCount, int otherViewFieldsCount) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "jobs");
        requestBody.put("myViewFieldsCount", myViewFieldsCount);
        requestBody.put("otherViewFieldsCount", otherViewFieldsCount);

        Response response = RestClient.doPut("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn + "invalid_token", null, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Unauthorised access"));

        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", jp.get("meta.responseType.code"), equalTo(104));

        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
        assertThat("Errors array should be empty", jp.get("errors"), notNullValue());
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testUpdateJobDetailsPageFieldsCountEmptyRequestBody() {
        JSONObject requestBody = new JSONObject();

        Response response = RestClient.doPut("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testUpdateJobDetailsPageFieldsCountMissingEntityType() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("myViewFieldsCount", 5);
        requestBody.put("otherViewFieldsCount", 5);

        Response response = RestClient.doPut("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(400));

        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));

        assertThat("Error message should match", jp.get("errors[0].message"), equalTo("entityType cannot be null"));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("ErrorType code should be 201", jp.get("errors[0].errorType.code"), equalTo(201));
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testUpdateJobDetailsPageFieldsCountInvalidEntityType() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "invalid_entity");
        requestBody.put("myViewFieldsCount", 5);
        requestBody.put("otherViewFieldsCount", 5);

        Response response = RestClient.doPut("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();
        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(400));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testUpdateJobDetailsPageFieldsCountInvalidMyViewFieldsCount() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "jobs");
        requestBody.put("myViewFieldsCount", "invalid_number");
        requestBody.put("otherViewFieldsCount", 5);

        Response response = RestClient.doPut("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testUpdateJobDetailsPageFieldsCountInvalidOtherViewFieldsCount() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "jobs");
        requestBody.put("myViewFieldsCount", 10);
        requestBody.put("otherViewFieldsCount", "invalid_number");

        Response response = RestClient.doPut("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Harika")
    @Test(dataProvider = "negativeValuesTestData", groups = {"job_service", "nightly-build"})
    public void testUpdateJobDetailsPageFieldsCountNegativeValues(int myViewFieldsCount, int otherViewFieldsCount) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "jobs");
        requestBody.put("myViewFieldsCount", myViewFieldsCount);
        requestBody.put("otherViewFieldsCount", otherViewFieldsCount);

        Response response = RestClient.doPut("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(400));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("ErrorType code should be 201", jp.get("errors[0].errorType.code"), equalTo(201));

        if (myViewFieldsCount < 0 && otherViewFieldsCount >= 0) {
            assertThat("Error message for myViewFieldsCount should match", jp.get("errors[0].message"),
                    equalTo("myViewFieldsCount cannot be negative"));
        } else if (otherViewFieldsCount < 0 && myViewFieldsCount >= 0) {
            assertThat("Error message for otherViewFieldsCount should match", jp.get("errors[0].message"),
                    equalTo("otherViewFieldsCount must be greater than or equal to 0"));
        } else if (myViewFieldsCount < 0 && otherViewFieldsCount < 0) {
            assertThat("Error message for myViewFieldsCount should match", jp.get("errors[0].message"),
                    equalTo("myViewFieldsCount cannot be negative"));
            assertThat("Error message for otherViewFieldsCount should match", jp.get("errors[1].message"),
                    equalTo("otherViewFieldsCount must be greater than or equal to 0"));
            assertThat("ErrorType should not be null", jp.get("errors[1].errorType"), notNullValue());
            assertThat("ErrorType context should match", jp.get("errors[1].errorType.context"), equalTo("Validation Error"));
            assertThat("ErrorType code should be 201", jp.get("errors[1].errorType.code"), equalTo(201));
        }
    }

    @Owner("Harika")
    @Test(dataProvider = "zeroValuesTestData", groups = {"job_service", "nightly-build"})
    public void testUpdateJobDetailsPageFieldsCountZeroValues(int myViewFieldsCount, int otherViewFieldsCount) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "jobs");
        requestBody.put("myViewFieldsCount", myViewFieldsCount);
        requestBody.put("otherViewFieldsCount", otherViewFieldsCount);

        Response response = RestClient.doPut("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Details Page Field Count Updated successfully."));
    }

    @Owner("Harika")
    @Test(dataProvider = "emptyValuesTestData", groups = {"job_service", "nightly-build"})
    public void testUpdateJobDetailsPageFieldsCountEmptyValues(Object myViewFieldsCount, Object otherViewFieldsCount) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "jobs");
        requestBody.put("myViewFieldsCount", myViewFieldsCount);
        requestBody.put("otherViewFieldsCount", otherViewFieldsCount);

        Response response = RestClient.doPut("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 400 for null/empty values", response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();
        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(400));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));

        assertThat("ErrorType context should be Validation Error", jp.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("ErrorType code should be 201", jp.get("errors[0].errorType.code"), equalTo(201));

        if (myViewFieldsCount == null || myViewFieldsCount.equals("")) {
            assertThat("Error message should indicate null not allowed",
                    jp.get("errors[0].message"), equalTo("myViewFieldsCount cannot be null"));
        } else {
            assertThat("Error message should indicate null not allowed",
                    jp.get("errors[0].message"), equalTo("otherViewFieldsCount cannot be null"));
        }

    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testUpdateJobDetailsPageFieldsCountForAdmin() {
        restrictEdit();
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "jobs");
        requestBody.put("myViewFieldsCount", 5);

        Response response = RestClient.doPut("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Details Page Field Count Updated successfully."));

        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", jp.get("meta.responseType.code"), equalTo(103));
    }

    @Owner("Harika")
    @Test(groups = {"job_service", "nightly-build"})
    public void testUpdateJobDetailsPageFieldsCountForRestrictedUser() {
        restrictEdit();
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "jobs");
        requestBody.put("myViewFieldsCount", 5);


        Response response = RestClient.doPut("JSON", jobServiceURL, "details-page/fields-count",
                restrictedAuthToken, null, true, requestBody.toString());

        assertThat("Restricted user should get 403", response.getStatusCode(), equalTo(403));

        JsonPath jp = response.jsonPath();
        assertThat("Meta status should be 403", jp.get("meta.status"), equalTo(403));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
        assertThat("Error message should indicate view editing disabled", jp.get("errors[0].message"), equalTo("View editing for jobs is disabled by account owner."));
        assertThat("ErrorType context should be Generic Error", jp.get("errors[0].errorType.context"), equalTo("Generic Error"));
        assertThat("ErrorType code should be 202", jp.get("errors[0].errorType.code"), equalTo(202));
    }

    public void restrictEdit() {
        UpdateFieldWidgetCustomizationRequest updateRequest = new UpdateFieldWidgetCustomizationRequest();
        updateRequest.setId(userId);
        updateRequest.setSilentProcess(true);
        updateRequest.setKey("entity_view_lock_settings");
        updateRequest.setTableFlag("accountsettings");
        updateRequest.setValue("{\"1\":1, \"2\":1, \"3\":1, \"4\":1, \"5\":1, \"6\":1, \"7\":1, \"8\":1, \"9\":1, \"10\":1, \"15\":1, \"16\":1, \"17\":1, \"18\":1, \"19\":1, \"20\":1, \"21\":1, \"22\":1, \"25\":1, \"26\":1, \"27\":1}");

        Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields", albatrossTkn, null, true, updateRequest);

        assert response != null;
        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Message type should be is-success", jp.get("message_type"), equalTo("is-success"));
    }



    private Response getCurrentFieldCounts() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", String.valueOf(userId));
        queryParams.put("entityType", "jobs");

        return RestClient.doGet("JSON", jobServiceURL, "details-page/fields-count",
                albatrossTkn, queryParams, null, true);
    }

    @DataProvider(name = "updateFieldsCountTestData")
    public Object[][] getUpdateFieldsCountTestData() {
        return new Object[][]{
                {5, 5},
                {10, 10},
                {15, 5},
                {5, 15}
        };
    }

    @DataProvider(name = "negativeValuesTestData", parallel = true)
    public Object[][] getNegativeValuesTestData() {
        return new Object[][]{
                {-5, -2},
                {-1, 5},
                {5, -1}
        };
    }

    @DataProvider(name = "zeroValuesTestData", parallel = true)
    public Object[][] getZeroValuesTestData() {
        return new Object[][]{
                {0, 0},
                {0, 5},
                {5, 0},
                {" ", " "},
                {null, null}
        };
    }

    @DataProvider(name = "emptyValuesTestData", parallel = true)
    public Object[][] getEmptyValuesTestData() {
        return new Object[][]{
                {" ", 5},
                {5, " "},
                {null, 5},
                {5, null},
                {" ", " "},
                {null, null}
        };
    }
}
