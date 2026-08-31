package io.recruitcrm.ContactService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.Map;

import io.rcrm.api.commanfunctions.commanFunction;
import org.json.JSONObject;
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
public class UpdateContactDetailsPageFieldCountTest extends TestBase {

    String albatrossTkn;
    String authToken;
    int accountId;
    int userId;
    commanFunction commanFunction = new commanFunction();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        authToken = ThreadManager.getAccountApiKey();
        accountId = ThreadManager.getAccount().getAccountId();

        Response usersResp = commanFunction.getUsers(baseURL, authToken);
        assertThat("Failed to get users", usersResp.getStatusCode(), equalTo(200));
        JsonPath jpUser = usersResp.jsonPath();

        userId = jpUser.get("[0].id");
        assertThat("User ID should not be null", userId, notNullValue());
    }

    @Owner("Harika")
    @Test(dataProvider = "updateFieldsCountTestData", groups = {"contact_service", "nightly-build"})
    public void testUpdateContactDetailsPageFieldsCountSuccess(int myViewFieldsCount, int otherViewFieldsCount) {

        // Step 1: Update field counts
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "contacts");
        requestBody.put("myViewFieldsCount", myViewFieldsCount);
        requestBody.put("otherViewFieldsCount", otherViewFieldsCount);

        Response response = RestClient.doPut("JSON", contactServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());


        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Details Page Field Count Updated successfully."));

        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/contact/updateDetailsPageFieldsCount.json"));

        // Step 2: Verify the update by getting the data again
        Response verifyResponse = getCurrentFieldCounts();
        System.out.println(verifyResponse.prettyPrint());
        assertThat("Get should succeed after update", verifyResponse.getStatusCode(), equalTo(200));

        JsonPath verifyJp = verifyResponse.jsonPath();
        assertThat("MyViewCount should be updated", (Integer) verifyJp.get("data.myViewCount"), equalTo(myViewFieldsCount));
        assertThat("OthersViewCount should be updated", (Integer) verifyJp.get("data.othersViewCount"), equalTo(otherViewFieldsCount));
    }

    @Owner("Harika")
    @Test(dataProvider = "updateFieldsCountTestData", groups = {"contact_service", "nightly-build"})
    public void testUpdateContactDetailsPageFieldsCountWithoutAuth(int myViewFieldsCount, int otherViewFieldsCount) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "contacts");
        requestBody.put("myViewFieldsCount", myViewFieldsCount);
        requestBody.put("otherViewFieldsCount", otherViewFieldsCount);

        Response response = RestClient.doPut("JSON", contactServiceURL, "details-page/fields-count",
                "", null, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Unauthorised access"));

        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", (Integer) jp.get("meta.responseType.code"), equalTo(104));

        assertThat("Data should contain error message", jp.get("data"), equalTo("Missing bearer token in header"));
        assertThat("Errors array should be empty", jp.get("errors"), notNullValue());
    }

    @Owner("Harika")
    @Test(dataProvider = "updateFieldsCountTestData", groups = {"contact_service", "nightly-build"})
    public void testUpdateContactDetailsPageFieldsCountInvalidAuth(int myViewFieldsCount, int otherViewFieldsCount) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "contacts");
        requestBody.put("myViewFieldsCount", myViewFieldsCount);
        requestBody.put("otherViewFieldsCount", otherViewFieldsCount);

        Response response = RestClient.doPut("JSON", contactServiceURL, "details-page/fields-count",
                albatrossTkn + "invalid_token", null, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Unauthorised access"));

        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", (Integer) jp.get("meta.responseType.code"), equalTo(104));

        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
        assertThat("Errors array should be empty", jp.get("errors"), notNullValue());
    }

    @Owner("Harika")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testUpdateContactDetailsPageFieldsCountEmptyRequestBody() {
        JSONObject requestBody = new JSONObject();
        // Empty request body

        Response response = RestClient.doPut("JSON", contactServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

    }

    @Owner("Harika")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testUpdateContactDetailsPageFieldsCountMissingEntityType() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("myViewFieldsCount", 5);
        requestBody.put("otherViewFieldsCount", 5);

        Response response = RestClient.doPut("JSON", contactServiceURL, "details-page/fields-count",
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
    @Test(groups = {"contact_service", "nightly-build"})
    public void testUpdateContactDetailsPageFieldsCountInvalidEntityType() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "invalid_entity");
        requestBody.put("myViewFieldsCount", 5);
        requestBody.put("otherViewFieldsCount", 5);

        Response response = RestClient.doPut("JSON", contactServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));

        JsonPath jp = response.jsonPath();
        assertThat("Meta status should be 400", jp.get("meta.status"), equalTo(400));
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
    }

    @Owner("Harika")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testUpdateContactDetailsPageFieldsCountInvalidMyViewFieldsCount() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "contacts");
        requestBody.put("myViewFieldsCount", "invalid_number");
        requestBody.put("otherViewFieldsCount", 5);

        Response response = RestClient.doPut("JSON", contactServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Harika")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testUpdateContactDetailsPageFieldsCountInvalidOtherViewFieldsCount() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "contacts");
        requestBody.put("myViewFieldsCount", 10);
        requestBody.put("otherViewFieldsCount", "invalid_number");

        Response response = RestClient.doPut("JSON", contactServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Harika")
    @Test(dataProvider = "negativeValuesTestData", groups = {"contact_service", "nightly-build"})
    public void testUpdateContactDetailsPageFieldsCountNegativeValues(int myViewFieldsCount, int otherViewFieldsCount) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "contacts");
        requestBody.put("myViewFieldsCount", myViewFieldsCount);
        requestBody.put("otherViewFieldsCount", otherViewFieldsCount);

        Response response = RestClient.doPut("JSON", contactServiceURL, "details-page/fields-count",
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
                    equalTo("otherViewFieldsCount cannot be negative"));
        } else if (myViewFieldsCount < 0 && otherViewFieldsCount < 0) {
            assertThat("Error message for myViewFieldsCount should match", jp.get("errors[0].message"),
                    equalTo("myViewFieldsCount cannot be negative"));
            assertThat("Error message for otherViewFieldsCount should match", jp.get("errors[1].message"),
                    equalTo("otherViewFieldsCount cannot be negative"));
            assertThat("ErrorType should not be null", jp.get("errors[1].errorType"), notNullValue());
            assertThat("ErrorType context should match", jp.get("errors[1].errorType.context"), equalTo("Validation Error"));
            assertThat("ErrorType code should be 201", jp.get("errors[1].errorType.code"), equalTo(201));
        }

    }

    @Owner("Harika")
    @Test(dataProvider = "zeroValuesTestData", groups = {"contact_service", "nightly-build"})
    public void testUpdateContactDetailsPageFieldsCountZeroValues(int myViewFieldsCount, int otherViewFieldsCount) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", "contacts");
        requestBody.put("myViewFieldsCount", myViewFieldsCount);
        requestBody.put("otherViewFieldsCount", otherViewFieldsCount);

        Response response = RestClient.doPut("JSON", contactServiceURL, "details-page/fields-count",
                albatrossTkn, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Details Page Field Count Updated successfully."));
    }

    // Helper method to get current field counts
    private Response getCurrentFieldCounts() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", String.valueOf(userId));
        queryParams.put("entityType", "contacts");

        return RestClient.doGet("JSON", contactServiceURL, "details-page/fields-count",
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
                {5, 0}
        };
    }
}
