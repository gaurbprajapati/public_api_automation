package io.recruitcrm.JobService;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.global.CandidateWidgetsSaveState;
import io.rcrm.api.pojo.albatross.global.UpdateFieldWidgetCustomizationRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class JobWidgetCustomizationSaveStateTest extends TestBase {

    private String albatrossAuthToken;
    private String adminUserAuthToken;
    private String restrictedUserAuthToken;
    private int ownerAccountID;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        restrictedUserAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        adminUserAuthToken = ThreadManager.getAlbatrossToken("Admin");
        ownerAccountID = ThreadManager.getAccount().getAccountId();
    }

    @Owner("Harika")
    @Test(dataProvider = "dataTableKeyTestData", groups = {"job_service", "nightly-build"})
    public void customizeJobWidgetsMyView(String datatablekey, String columnstate) {
        CandidateWidgetsSaveState saveStateRequest = new CandidateWidgetsSaveState();
        saveStateRequest.setColumnstate(columnstate);
        saveStateRequest.setDatatablekey(datatablekey);

        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", albatrossAuthToken, null, true, saveStateRequest);
        assertThat("Expected status code 200 for Own View - Job Widgets Customization", response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Message type should be success for Own View - Job Widgets Customization", jp.get("message_type"), equalTo("is-success"));
        assertThat("Status should be success for Own View - Job Widgets Customization", jp.get("status"), equalTo("success"));
        assertThat("Action name should be 'State Saved' for Own View - Job Widgets Customization", jp.getString("action_name"), equalTo("State Saved"));
        assertThat("Action ID should not be null for Own View - Job Widgets Customization", jp.get("actionid"), notNullValue());
        assertThat("Account ID should match for Own View - Job Widgets Customization", jp.getInt("user.accountid"), equalTo(ownerAccountID));

        assertThat("Silent progress should match expected for Own View - Job Widgets Customization", jp.getBoolean("silent_progress"), is(true));
        assertThat("Message should match expected for Own View - Job Widgets Customization", jp.getString("message"), equalTo("0"));

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/global/candidateWidgetsSaveState.json"));

        Response viewResponse = getJobWidgetsView("entity-widgets/customize-user-view", albatrossAuthToken, datatablekey);

        JsonPath viewJson = viewResponse.jsonPath();
        assertThat("Message should match expected", viewJson.get("meta.message"), equalTo("Customize User's View Fetched Successfully"));

        verifyViewMatchesColumnState(viewResponse, columnstate);
    }

    @Owner("Harika")
    @Test(dataProvider = "dataTableKeyTestDataOthersView", groups = {"job_service", "nightly-build"})
    public void customizeJobWidgetsOtherView(String datatablekey, String viewType, String columnstate) {
        CandidateWidgetsSaveState saveStateRequest = new CandidateWidgetsSaveState();
        saveStateRequest.setColumnstate(columnstate);
        saveStateRequest.setDatatablekey(datatablekey);

        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", albatrossAuthToken, null, true, saveStateRequest);
        assertThat("Expected status code 200 for Own View - Job Widgets Customization", response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Message type should be success for Own View - Job Widgets Customization", jp.get("message_type"), equalTo("is-success"));
        assertThat("Status should be success for Own View - Job Widgets Customization", jp.get("status"), equalTo("success"));
        assertThat("Action name should be 'State Saved' for Own View - Job Widgets Customization", jp.getString("action_name"), equalTo("State Saved"));
        assertThat("Action ID should not be null for Own View - Job Widgets Customization", jp.get("actionid"), notNullValue());
        assertThat("Account ID should match for Own View - Job Widgets Customization", jp.getInt("user.accountid"), equalTo(ownerAccountID));

        assertThat("Silent progress should match expected for Own View - Job Widgets Customization", jp.getBoolean("silent_progress"), is(false));
        assertThat("Message should match expected for Own View - Job Widgets Customization", jp.getString("message"), equalTo("Field order and visibility has been set for other users successfully"));

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/global/candidateWidgetsSaveState.json"));

        Response accountViewResponse = getJobWidgetsView("entity-widgets/customize-account-view", albatrossAuthToken, viewType);
        JsonPath viewJson = accountViewResponse.jsonPath();
        assertThat("Message should match expected", viewJson.get("meta.message"), equalTo("Customize Account's View Fetched Successfully"));
        verifyViewMatchesColumnState(accountViewResponse, columnstate);

        Response userViewResponse = getJobWidgetsView("entity-widgets/customize-user-view", restrictedUserAuthToken, viewType);
        JsonPath userViewJson = userViewResponse.jsonPath();
        assertThat("Message should match expected", userViewJson.get("meta.message"), equalTo("Customize User's View Fetched Successfully"));
        verifyViewMatchesColumnState(userViewResponse, columnstate);
    }

    @Owner("Harika")
    @Test(dataProvider = "dataTableKeyTestDataOthersView", groups = {"job_service", "nightly-build"})
    public void verifyOtherUserRestrictedToEditOtherView(String datatablekey, String viewType, String columnstate) {
        CandidateWidgetsSaveState saveStateRequest = new CandidateWidgetsSaveState();
        saveStateRequest.setColumnstate(columnstate);
        saveStateRequest.setDatatablekey(datatablekey);

        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", adminUserAuthToken, null, true, saveStateRequest);
        assertThat("Expected status code 200 for Own View - Job Widgets Customization", response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Message type should be danger for Restricted User - Job Widgets Customization", jp.get("message_type"), equalTo("is_danger"));
        assertThat("Message should be access denied for Restricted User - Job Widgets Customization", jp.get("message"), equalTo("Access Denied"));
        assertThat("Status should be fail for Restricted User - Job Widgets Customization", jp.get("status"), equalTo("fail"));
        assertThat("Action name should be empty for Restricted User - Job Widgets Customization", jp.getString("action_name"), equalTo(""));
    }

    @Owner("Harika")
    @Test(dataProvider = "dataTableKeyTestData", groups = {"job_service", "nightly-build"})
    public void verifyUserRestrictedToCustomizeJobWidgets(String datatablekey, String columnstate) {
        restrictEdit();
        CandidateWidgetsSaveState saveStateRequest = new CandidateWidgetsSaveState();
        saveStateRequest.setColumnstate(columnstate);
        saveStateRequest.setDatatablekey(datatablekey);

        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", adminUserAuthToken, null, true, saveStateRequest);
        assertThat("Expected status code 200 for Own View - Job Widgets Customization", response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Message type should be success for Own View - Job Widgets Customization", jp.get("message_type"), equalTo("is-success"));
        assertThat("Status should be success for Own View - Job Widgets Customization", jp.get("status"), equalTo("success"));
        assertThat("Action name should be 'State Saved' for Own View - Job Widgets Customization", jp.getString("action_name"), equalTo("State Saved"));

        Response response1 = RestClient.doPost("JSON", albatrossURL, "global/save-state", restrictedUserAuthToken, null, true, saveStateRequest);
        assertThat("Expected status code 200 for Restricted User - Job Widgets Customization", response1.getStatusCode(), equalTo(200));

        JsonPath jp1 = response1.jsonPath();
        assertThat("Message type should be danger for Restricted User - Job Widgets Customization", jp1.get("message_type"), equalTo("is_danger"));
        assertThat("Message should be access denied for Restricted User - Job Widgets Customization", jp1.get("message"), equalTo("Access Denied"));
        assertThat("Status should be fail for Restricted User - Job Widgets Customization", jp1.get("status"), equalTo("fail"));
        assertThat("Action name should be empty for Restricted User - Job Widgets Customization", jp1.getString("action_name"), equalTo(""));
    }

    @Owner("Harika")
    @Test(dataProvider = "dataTableKeyTestData", groups = {"job_service", "nightly-build"})
    public void customizeJobWidgetsStateInvalidAuth(String datatablekey, String columnstate) {
        CandidateWidgetsSaveState saveStateRequest = new CandidateWidgetsSaveState();
        saveStateRequest.setColumnstate(columnstate);
        saveStateRequest.setDatatablekey(datatablekey);

        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", albatrossAuthToken + "invalid", null, true, saveStateRequest);

        assertThat("Expected status code 401 for unauthorized access", response.getStatusCode(), equalTo(401));
        JsonPath jp = response.jsonPath();
        assertThat("Error message should be 'Unauthorized'", jp.get("error"), equalTo("Unauthorized"));
    }

    @Owner("Harika")
    @Test(dataProvider = "emptyDataTestData", groups = {"job_service", "nightly-build"})
    public void customizeJobWidgetsSaveStateEmptyData(String testDescription, String columnstate, String datatablekey) {
        CandidateWidgetsSaveState saveStateRequest = new CandidateWidgetsSaveState();
        saveStateRequest.setColumnstate(columnstate);
        saveStateRequest.setDatatablekey(datatablekey);

        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", albatrossAuthToken, null, true, saveStateRequest);

        assertThat("Expected status code 200 for " + testDescription, response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Message type should be is_danger for " + testDescription, jp.get("message_type"), equalTo("is_danger"));
        assertThat("Status should be fail for " + testDescription, jp.get("status"), equalTo("fail"));
        assertThat("Message should indicate missing data for " + testDescription, jp.getString("message"), equalTo("Required data is missing in the request"));
        assertThat("Silent progress should be true for " + testDescription, jp.getBoolean("silent_progress"), is(true));
        assertThat("Action name should be empty for failed request", jp.getString("action_name"), equalTo(""));
        assertThat("Action ID should be empty for failed request", jp.getString("actionid"), equalTo(""));
        assertThat("Data should be empty array", jp.getList("data").size(), equalTo(0));
        assertThat("Notifications should be empty array", jp.getList("notifications").size(), equalTo(0));

        String responseBody = response.getBody().asString();
        JSONObject jsonResponse = new JSONObject(responseBody);
        assertThat("Response should have silent_progress field", jsonResponse.has("silent_progress"), is(true));
        assertThat("Response should have recaptcha_site_key field", jsonResponse.has("recaptcha_site_key"), is(true));
        assertThat("Recaptcha site key should not be null", jsonResponse.get("recaptcha_site_key"), notNullValue());
    }

    @DataProvider
    public Object[][] emptyDataTestData() {
        return new Object[][]{
                {
                        "Empty columnstate with job_widgets datatablekey",
                        "",
                        "job_widgets"
                },
                {
                        "Empty columnstate with job_widgets_others_view datatablekey",
                        "",
                        "job_widgets_others_view"
                },
                {
                        "Empty columnstate and empty datatablekey",
                        "",
                        ""
                },
                {
                        "Valid columnstate with empty datatablekey",
                        "{\"visible\":[{\"id\":1},{\"id\":2},{\"id\":3},{\"id\":4},{\"id\":5}],\"hidden\":[{\"id\":6},{\"id\":7},{\"id\":8},{\"id\":9}]}",
                        ""
                }
        };
    }

    @DataProvider
    public Object[][] dataTableKeyTestData() {
        return new Object[][]{
                {
                        "job_widgets",
                        "{\"visible\":[{\"id\":1},{\"id\":2},{\"id\":4},{\"id\":3},{\"id\":5},{\"id\":6},{\"id\":7}],\"hidden\":[{\"id\":8},{\"id\":9}]}"
                },
                {
                        "job_activity_widgets",
                        "{\"visible\":[{\"id\":1},{\"id\":3},{\"id\":2}],\"hidden\":[{\"id\":4}]}"
                }
        };
    }

    @DataProvider
    public Object[][] dataTableKeyTestDataOthersView() {
        return new Object[][]{
                {
                        "job_widgets_others_view",
                        "job_widgets",
                        "{\"visible\":[{\"id\":1},{\"id\":2},{\"id\":4},{\"id\":3},{\"id\":5},{\"id\":6},{\"id\":7}],\"hidden\":[{\"id\":8},{\"id\":9}]}"
                },
                {
                        "job_activity_widgets_others_view",
                        "job_activity_widgets",
                        "{\"visible\":[{\"id\":1},{\"id\":3},{\"id\":2}],\"hidden\":[{\"id\":4}]}"
                }
        };
    }

    private Response getJobWidgetsView(String basePath, String authToken, String viewType) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doGet("JSON", jobServiceURL, basePath, authToken, queryParameters, null, true);
        assertThat("Response status should be 200", response.getStatusCode(), equalTo(200));
        return response;
    }


    private void compareArrays(org.json.JSONArray expectedArray, java.util.List<Map<String, Object>> actualList, String arrayType) {
        assertThat(arrayType + " array size should match", actualList.size(), equalTo(expectedArray.length()));
        for (int i = 0; i < expectedArray.length(); i++) {
            JSONObject expectedItem = expectedArray.getJSONObject(i);
            Map<String, Object> actualItem = actualList.get(i);
            assertThat(arrayType + " item " + i + " id should match", actualItem.get("id"), equalTo(expectedItem.getInt("id")));
        }
    }

    private void verifyViewMatchesColumnState(Response response, String expectedColumnStateJson) {

        JSONObject expectedColumnState = new JSONObject(expectedColumnStateJson);
        JsonPath responseJp = response.jsonPath();

        org.json.JSONArray expectedVisible = expectedColumnState.getJSONArray("visible");
        java.util.List<Map<String, Object>> actualVisible = responseJp.get("data.view.visible");
        compareArrays(expectedVisible, actualVisible, "Visible");

        org.json.JSONArray expectedHidden = expectedColumnState.getJSONArray("hidden");
        java.util.List<Map<String, Object>> actualHidden = responseJp.get("data.view.hidden");
        compareArrays(expectedHidden, actualHidden, "Hidden");
    }

    public void restrictEdit() {
        UpdateFieldWidgetCustomizationRequest updateRequest = new UpdateFieldWidgetCustomizationRequest();
        updateRequest.setId(ownerAccountID);
        updateRequest.setSilentProcess(true);
        updateRequest.setKey("entity_view_lock_settings");
        updateRequest.setTableFlag("accountsettings");
        updateRequest.setValue("{\"1\":1, \"2\":1, \"3\":1, \"4\":1, \"5\":1, \"6\":1, \"7\":1, \"8\":1, \"9\":1, \"10\":1, \"15\":1, \"16\":1, \"17\":1, \"18\":1, \"19\":1, \"20\":1, \"21\":1, \"22\":1, \"25\":1, \"26\":1, \"27\":1}");

        Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields", albatrossAuthToken, null, true, updateRequest);

        assert response != null;
        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Message type should be is-success", jp.get("message_type"), equalTo("is-success"));
    }
}