package io.recruitcrm.CandidateService;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.global.SaveStateRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ResetEntityColumnsTest extends TestBase {
    
    private static final String BASE_PATH = "entity-columns/reset";
    private static final String BASE_PATH_SAVE_STATE = "global/save-state";
    private static final String DEFAULT_COLUMNSTATE = "{\"locality\":{\"visible\":false,\"listPageOrder\":21,\"detailPageOrder\":0,\"width\":200,\"detailPageVisible\":true,\"detailPageOrderV2\":1,\"primaryOrder\":3},\"city\":{\"visible\":false,\"listPageOrder\":20,\"detailPageOrder\":0,\"width\":200,\"detailPageVisible\":true,\"detailPageOrderV2\":2,\"primaryOrder\":4},\"state\":{\"visible\":true,\"listPageOrder\":62,\"detailPageOrder\":32,\"width\":200,\"detailPageVisible\":true,\"detailPageOrderV2\":3,\"primaryOrder\":0}}";
    
    private String albatrossAuthToken;
    private int ownerAccountID;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
    }

    @DataProvider(name = "resetEntityColumnsDataProvider")
    public Object[][] resetEntityColumnsDataProvider() {
        return new Object[][] {
            { "myView", "true" },
            { "myView", "false" },
            { "othersView", "true" },
            { "othersView", "false" }
        };
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "resetEntityColumnsDataProvider", groups = {"candidate_service", "nightly-build"})
    public void resetEntityColumns_Success(String viewType, String isDetailPageReset) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "candidates");
        queryParameters.put("viewType", viewType);
        queryParameters.put("isDetailPageReset", isDetailPageReset);

        Response response = RestClient.doPatch("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true, null);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
        
        JsonPath jsonPath = response.jsonPath();
        
        boolean isAccountView = "othersView".equals(viewType);
        String expectedMessage = isAccountView ? "Account View Columns Fetched Successfully" : "Entity Column Fetched Successfully";
        
        if (isAccountView) {
            response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/ResetEntityColumnsAccountView.json"));
            assertThat("Account view columns should exist in data", jsonPath.get("data[0].accountViewColumns"), notNullValue());
        } else {
            response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/ResetEntityColumns.json"));
            assertThat("Columns should exist in data", jsonPath.get("data[0].columns"), notNullValue());
        }
        
        assertThat("Expected success message", jsonPath.get("meta.message"), equalTo(expectedMessage));
        assertThat("Expected status 200 in meta", jsonPath.getInt("meta.status"), equalTo(200));
        assertThat("Expected success context", jsonPath.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Expected response code 103", jsonPath.getInt("meta.responseType.code"), equalTo(103));
        assertThat("Request UUID should not be null", jsonPath.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.get("meta.timestamp"), notNullValue());
        assertThat("Data array should exist", jsonPath.get("data"), notNullValue());
        assertThat("Data should be an array", jsonPath.get("data"), instanceOf(java.util.List.class));
        assertThat("Data array should not be empty", jsonPath.getList("data").size(), greaterThan(0));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void resetEntityColumns_WithoutAuth() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "candidates");
        queryParameters.put("viewType", "myView");
        queryParameters.put("isDetailPageReset", "true");

        Response response = RestClient.doPatch("JSON", candidatesURL, BASE_PATH, "", queryParameters, null, true, null);

        assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/ResetEntityColumnsUnauthorized.json"));
        
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
    public void resetEntityColumns_InvalidAuth() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "candidates");
        queryParameters.put("viewType", "myView");
        queryParameters.put("isDetailPageReset", "true");

        Response response = RestClient.doPatch("JSON", candidatesURL, BASE_PATH, albatrossAuthToken + "invalid", queryParameters, null, true, null);

        assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/ResetEntityColumnsUnauthorized.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected unauthorized access message", jsonPath.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Expected status 401 in meta", jsonPath.getInt("meta.status"), equalTo(401));
        assertThat("Expected warning context", jsonPath.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("Expected response code 104", jsonPath.getInt("meta.responseType.code"), equalTo(104));
        assertThat("Request UUID should not be null", jsonPath.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.get("meta.timestamp"), notNullValue());
        assertThat("Expected data to contain missing bearer token message", jsonPath.get("data"), equalTo("Invalid or expired token"));
        assertThat("Errors array should exist", jsonPath.get("errors"), notNullValue());
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void resetEntityColumns_MissingEntity() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", "myView");
        queryParameters.put("isDetailPageReset", "true");

        Response response = RestClient.doPatch("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true, null);

        assertThat("Expected status code 400", response.getStatusCode(), equalTo(400));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/ResetEntityColumnsValidationError.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected message to be null", jsonPath.get("meta.message"), nullValue());
        assertThat("Expected status 400 in meta", jsonPath.getInt("meta.status"), equalTo(400));
        assertThat("Expected error context", jsonPath.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("Expected response code 101", jsonPath.getInt("meta.responseType.code"), equalTo(101));
        assertThat("Request UUID should not be null", jsonPath.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.get("meta.timestamp"), notNullValue());
        assertThat("Expected data to be null", jsonPath.get("data"), nullValue());
        assertThat("Errors array should exist", jsonPath.get("errors"), notNullValue());
        assertThat("Errors array should not be empty", jsonPath.getList("errors").size(), greaterThan(0));
        assertThat("Error message should contain entity validation", jsonPath.get("errors[0].message"), equalTo("Entity cannot be null or blank"));
        assertThat("Error context should be Validation Error", jsonPath.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("Error code should be 201", jsonPath.getInt("errors[0].errorType.code"), equalTo(201));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void resetEntityColumns_MissingViewType() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "candidates");
        queryParameters.put("isDetailPageReset", "true");

        Response response = RestClient.doPatch("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true, null);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/ResetEntityColumnsAccountView.json"));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected success message", jsonPath.get("meta.message"), equalTo("Account View Columns Fetched Successfully"));
        assertThat("Expected status 200 in meta", jsonPath.getInt("meta.status"), equalTo(200));
        assertThat("Expected success context", jsonPath.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Expected response code 103", jsonPath.getInt("meta.responseType.code"), equalTo(103));
        assertThat("Request UUID should not be null", jsonPath.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.get("meta.timestamp"), notNullValue());
        assertThat("Data array should exist", jsonPath.get("data"), notNullValue());
        assertThat("Data should be an array", jsonPath.get("data"), instanceOf(java.util.List.class));
        assertThat("Data array should not be empty", jsonPath.getList("data").size(), greaterThan(0));
        assertThat("Account view columns should exist in data", jsonPath.get("data[0].accountViewColumns"), notNullValue());
    }

    @DataProvider(name = "saveStateDataProvider")
    public Object[][] saveStateDataProvider() {
        return new Object[][] {
            { false, true, true, false },
            { false, true, true, true },
            { false, true, false, false },
            { false, true, false, true },
            { false, false, true, false },
            { false, false, true, true },
            { false, false, false, false },
            { false, false, false, true },
            { true, true, true, false },
            { true, true, true, true },
            { true, true, false, false },
            { true, true, false, true },
            { true, false, true, false },
            { true, false, true, true },
            { true, false, false, false },
            { true, false, false, true }
        };
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "saveStateDataProvider", groups = {"candidate_service", "nightly-build"})
    public void saveState_Success(boolean fromDetailPage, boolean updateUserObj, boolean isListPageV2, boolean isDetailPageV2) {
        SaveStateRequest saveStateRequest = new SaveStateRequest();
        saveStateRequest.setDatatablekey("candidates");
        saveStateRequest.setColumnstate(DEFAULT_COLUMNSTATE);
        saveStateRequest.setFromDetailPage(fromDetailPage);
        saveStateRequest.setUpdateUserObj(updateUserObj);
        saveStateRequest.setIsListPageV2(isListPageV2);
        saveStateRequest.setIsDetailPageV2(isDetailPageV2);

        Response response = RestClient.doPost("JSON", albatrossURL, BASE_PATH_SAVE_STATE, albatrossAuthToken, null, true, saveStateRequest);
        
        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/global/candidateWidgetsSaveState.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Message type should be success", jsonPath.get("message_type"), equalTo("is-success"));
        assertThat("Status should be success", jsonPath.get("status"), equalTo("success"));
        assertThat("Action name should be 'State Saved'", jsonPath.getString("action_name"), equalTo("State Saved"));
        assertThat("Action ID should not be null", jsonPath.get("actionid"), notNullValue());
        assertThat("Account ID should match", jsonPath.getInt("user.accountid"), equalTo(ownerAccountID));
        assertThat("Silent progress should be boolean", jsonPath.get("silent_progress"), instanceOf(Boolean.class));
        assertThat("Message should not be null", jsonPath.get("message"), notNullValue());
        assertThat("Data should be array", jsonPath.get("data"), instanceOf(java.util.List.class));
        assertThat("Notifications should be array", jsonPath.get("notifications"), instanceOf(java.util.List.class));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void saveState_WithoutAuth() {
        SaveStateRequest saveStateRequest = new SaveStateRequest();
        saveStateRequest.setDatatablekey("candidates");
        saveStateRequest.setColumnstate(DEFAULT_COLUMNSTATE);
        saveStateRequest.setFromDetailPage(true);
        saveStateRequest.setUpdateUserObj(true);
        saveStateRequest.setIsListPageV2(true);
        saveStateRequest.setIsDetailPageV2(true);

        Response response = RestClient.doPost("JSON", albatrossURL, BASE_PATH_SAVE_STATE, "", null, true, saveStateRequest);
        
        assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void saveState_InvalidAuth() {
        SaveStateRequest saveStateRequest = new SaveStateRequest();
        saveStateRequest.setDatatablekey("candidates");
        saveStateRequest.setColumnstate(DEFAULT_COLUMNSTATE);
        saveStateRequest.setFromDetailPage(true);
        saveStateRequest.setUpdateUserObj(true);
        saveStateRequest.setIsListPageV2(true);
        saveStateRequest.setIsDetailPageV2(true);

        Response response = RestClient.doPost("JSON", albatrossURL, BASE_PATH_SAVE_STATE, albatrossAuthToken + "invalid", null, true, saveStateRequest);
        
        assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Error message should be 'Unauthorized'", jsonPath.get("error"), equalTo("Unauthorized"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void saveState_MissingDatatablekey() {
        SaveStateRequest saveStateRequest = new SaveStateRequest();
        saveStateRequest.setDatatablekey("");
        saveStateRequest.setColumnstate(DEFAULT_COLUMNSTATE);
        saveStateRequest.setFromDetailPage(true);
        saveStateRequest.setUpdateUserObj(true);
        saveStateRequest.setIsListPageV2(true);
        saveStateRequest.setIsDetailPageV2(true);

        Response response = RestClient.doPost("JSON", albatrossURL, BASE_PATH_SAVE_STATE, albatrossAuthToken, null, true, saveStateRequest);
        
        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Message type should be is_danger", jsonPath.get("message_type"), equalTo("is_danger"));
        assertThat("Status should be fail", jsonPath.get("status"), equalTo("fail"));
        assertThat("Message should indicate missing data", jsonPath.getString("message"), equalTo("Required data is missing in the request"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void saveState_MissingColumnstate() {
        SaveStateRequest saveStateRequest = new SaveStateRequest();
        saveStateRequest.setDatatablekey("candidates");
        saveStateRequest.setColumnstate("");
        saveStateRequest.setFromDetailPage(true);
        saveStateRequest.setUpdateUserObj(true);
        saveStateRequest.setIsListPageV2(true);
        saveStateRequest.setIsDetailPageV2(true);

        Response response = RestClient.doPost("JSON", albatrossURL, BASE_PATH_SAVE_STATE, albatrossAuthToken, null, true, saveStateRequest);
        
        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Message type should be is_danger", jsonPath.get("message_type"), equalTo("is_danger"));
        assertThat("Status should be fail", jsonPath.get("status"), equalTo("fail"));
        assertThat("Message should indicate missing data", jsonPath.getString("message"), equalTo("Required data is missing in the request"));
    }
}

