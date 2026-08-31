package io.recruitcrm.ContactService;

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
public class ContactWidgetCustomizationSaveStateTest extends TestBase {

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
    @Test(dataProvider = "dataTableKeyTestData", groups = {"contact_service", "nightly-build"})
    public void customizeContactWidgetsMyView(String datatablekey, String columnstate) {
        CandidateWidgetsSaveState saveStateRequest = new CandidateWidgetsSaveState();
        saveStateRequest.setColumnstate(columnstate);
        saveStateRequest.setDatatablekey(datatablekey);

        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", albatrossAuthToken, null, true, saveStateRequest);
        assertThat("Expected status code 200 for Own View - Contact Widgets Customization", response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Message type should be success for Own View - Contact Widgets Customization", jp.get("message_type"), equalTo("is-success"));
        assertThat("Status should be success for Own View - Contact Widgets Customization", jp.get("status"), equalTo("success"));
        assertThat("Action name should be 'State Saved' for Own View - Contact Widgets Customization", jp.getString("action_name"), equalTo("State Saved"));
        assertThat("Action ID should not be null for Own View - Contact Widgets Customization", jp.get("actionid"), notNullValue());
        assertThat("Account ID should match for Own View - Contact Widgets Customization", jp.getInt("user.accountid"), equalTo(ownerAccountID));

        // Validate response based on data provider values
        assertThat("Silent progress should match expected for Own View - Contact Widgets Customization", jp.getBoolean("silent_progress"), is(true));
        assertThat("Message should match expected for Own View - Contact Widgets Customization", jp.getString("message"), equalTo("0"));

        // Validate schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/global/candidateWidgetsSaveState.json"));

        //Verify the Contact Widgets view after update
        Response viewResponse = getContactWidgetsView("entity-widgets/customize-user-view", albatrossAuthToken, datatablekey);

        JsonPath viewJson = viewResponse.jsonPath();
        assertThat("Message should match expected", viewJson.get("meta.message"), equalTo("Customize User's View Fetched Successfully"));

        verifyViewMatchesColumnState(viewResponse, columnstate);

    }

    @Owner("Harika")
    @Test(dataProvider = "dataTableKeyTestDataOthersView", groups = {"contact_service", "nightly-build"})
    public void customizeContactWidgetsOtherView(String datatablekey, String viewType, String columnstate) {
        CandidateWidgetsSaveState saveStateRequest = new CandidateWidgetsSaveState();
        saveStateRequest.setColumnstate(columnstate);
        saveStateRequest.setDatatablekey(datatablekey);

        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", albatrossAuthToken, null, true, saveStateRequest);
        assertThat("Expected status code 200 for Own View - Contact Widgets Customization", response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Message type should be success for Own View - Contact Widgets Customization", jp.get("message_type"), equalTo("is-success"));
        assertThat("Status should be success for Own View - Contact Widgets Customization", jp.get("status"), equalTo("success"));
        assertThat("Action name should be 'State Saved' for Own View - Contact Widgets Customization", jp.getString("action_name"), equalTo("State Saved"));
        assertThat("Action ID should not be null for Own View - Contact Widgets Customization", jp.get("actionid"), notNullValue());
        assertThat("Account ID should match for Own View - Contact Widgets Customization", jp.getInt("user.accountid"), equalTo(ownerAccountID));

        // Validate response based on data provider values
        assertThat("Silent progress should match expected for Own View - Contact Widgets Customization", jp.getBoolean("silent_progress"), is(false));
        assertThat("Message should match expected for Own View - Contact Widgets Customization", jp.getString("message"), equalTo("Field order and visibility has been set for other users successfully"));

        // Validate schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/global/candidateWidgetsSaveState.json"));

        //Verify the Contact Widgets account view after update
        Response accountViewResponse = getContactWidgetsView("entity-widgets/customize-account-view", albatrossAuthToken, viewType);
        JsonPath viewJson = accountViewResponse.jsonPath();
        assertThat("Message should match expected", viewJson.get("meta.message"), equalTo("Customize Account's View Fetched Successfully"));
        verifyViewMatchesColumnState(accountViewResponse, columnstate);

        //Verify the Contact Widgets user view after update
        Response userViewResponse = getContactWidgetsView("entity-widgets/customize-user-view", restrictedUserAuthToken, viewType);
        JsonPath userViewJson = userViewResponse.jsonPath();
        assertThat("Message should match expected", userViewJson.get("meta.message"), equalTo("Customize User's View Fetched Successfully"));
        verifyViewMatchesColumnState(userViewResponse, columnstate);

    }

    @Owner("Harika")
    @Test(dataProvider = "dataTableKeyTestData", groups = {"contact_service", "nightly-build"})
    public void verifyUserRestrictedToCustomizeContactWidgets(String datatablekey, String columnstate) {
        restrictEdit();
        CandidateWidgetsSaveState saveStateRequest = new CandidateWidgetsSaveState();
        saveStateRequest.setColumnstate(columnstate);
        saveStateRequest.setDatatablekey(datatablekey);

        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", adminUserAuthToken, null, true, saveStateRequest);
        assertThat("Expected status code 200 for Own View - Contact Widgets Customization", response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Message type should be success for Own View - Contact Widgets Customization", jp.get("message_type"), equalTo("is-success"));
        assertThat("Status should be success for Own View - Contact Widgets Customization", jp.get("status"), equalTo("success"));
        assertThat("Action name should be 'State Saved' for Own View - Contact Widgets Customization", jp.getString("action_name"), equalTo("State Saved"));

        Response response1 = RestClient.doPost("JSON", albatrossURL, "global/save-state", restrictedUserAuthToken, null, true, saveStateRequest);
        assertThat("Expected status code 200 for Restricted User - Contact Widgets Customization", response1.getStatusCode(), equalTo(200));

        JsonPath jp1 = response1.jsonPath();
        assertThat("Message type should be danger for Restricted User - Contact Widgets Customization", jp1.get("message_type"), equalTo("is_danger"));
        assertThat("Message should be access denied for Restricted User - Contact Widgets Customization", jp1.get("message"), equalTo("Access Denied"));
        assertThat("Status should be fail for Restricted User - Contact Widgets Customization", jp1.get("status"), equalTo("fail"));
        assertThat("Action name should be empty for Restricted User - Contact Widgets Customization", jp1.getString("action_name"), equalTo(""));
    }

    @Owner("Harika")
    @Test(dataProvider = "dataTableKeyTestData", groups = {"contact_service", "nightly-build"})
    public void customizeContactWidgetsStateInvalidAuth(String datatablekey, String columnstate) {
        CandidateWidgetsSaveState saveStateRequest = new CandidateWidgetsSaveState();
        saveStateRequest.setColumnstate(columnstate);
        saveStateRequest.setDatatablekey(datatablekey);

        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", albatrossAuthToken + "invalid", null, true, saveStateRequest);

        assertThat("Expected status code 401 for unauthorized access", response.getStatusCode(), equalTo(401));
        JsonPath jp = response.jsonPath();
        assertThat("Error message should be 'Unauthorized'", jp.get("error"), equalTo("Unauthorized"));
    }

    @Owner("Harika")
    @Test(dataProvider = "emptyDataTestData", groups = {"contact_service", "nightly-build"})
    public void customizeContactWidgetsSaveStateEmptyData(String testDescription, String columnstate, String datatablekey) {
        CandidateWidgetsSaveState saveStateRequest = new CandidateWidgetsSaveState();
        saveStateRequest.setColumnstate(columnstate);
        saveStateRequest.setDatatablekey(datatablekey);

        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", albatrossAuthToken, null, true, saveStateRequest);

        // API returns 200 but with error indicators in response body
        assertThat("Expected status code 200 for " + testDescription, response.getStatusCode(), equalTo(200));

        // Validate error response using JsonPath
        JsonPath jp = response.jsonPath();
        assertThat("Message type should be is_danger for " + testDescription, jp.get("message_type"), equalTo("is_danger"));
        assertThat("Status should be fail for " + testDescription, jp.get("status"), equalTo("fail"));
        assertThat("Message should indicate missing data for " + testDescription, jp.getString("message"), equalTo("Required data is missing in the request"));
        assertThat("Silent progress should be true for " + testDescription, jp.getBoolean("silent_progress"), is(true));
        assertThat("Action name should be empty for failed request", jp.getString("action_name"), equalTo(""));
        assertThat("Action ID should be empty for failed request", jp.getString("actionid"), equalTo(""));
        assertThat("Data should be empty array", jp.getList("data").size(), equalTo(0));
        assertThat("Notifications should be empty array", jp.getList("notifications").size(), equalTo(0));

        // Validate using JSONObject
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
                        "Empty columnstate with contact_widgets datatablekey",
                        "",  // empty columnstate
                        "contact_widgets"
                },
                {
                        "Empty columnstate with contact_widgets_others_view datatablekey",
                        "",  // empty columnstate
                        "contact_widgets_others_view"
                },
                {
                        "Empty columnstate and empty datatablekey",
                        "",  // empty columnstate
                        ""   // empty datatablekey
                },
                {
                        "Valid columnstate with empty datatablekey",
                        "{\"visible\":[{\"id\":1},{\"id\":2},{\"id\":3},{\"id\":4},{\"id\":5}],\"hidden\":[{\"id\":6},{\"id\":7},{\"id\":8},{\"id\":9},{\"id\":10}]}",
                        ""   // empty datatablekey
                }
        };
    }

    @DataProvider
    public Object[][] dataTableKeyTestData() {
        return new Object[][]{
                {
                        "contact_widgets",
                        "{\"visible\":[{\"id\":1},{\"id\":2},{\"id\":3},{\"id\":6},{\"id\":7}],\"hidden\":[{\"id\":8},{\"id\":5},{\"id\":4}]}"
                },
                {
                        "contact_activity_widgets",
                        "{\"visible\":[{\"id\":1},{\"id\":3},{\"id\":5}],\"hidden\":[{\"id\":4},{\"id\":2}]}"
                }
        };
    }

    @DataProvider
    public Object[][] dataTableKeyTestDataOthersView() {
        return new Object[][]{
                {
                        "contact_widgets_others_view",
                        "contact_widgets",
                        "{\"visible\":[{\"id\":1},{\"id\":2},{\"id\":3},{\"id\":6},{\"id\":7}],\"hidden\":[{\"id\":8},{\"id\":5},{\"id\":4}]}"
                },
                {
                        "contact_activity_widgets_others_view",
                        "contact_activity_widgets",
                        "{\"visible\":[{\"id\":1},{\"id\":3},{\"id\":5}],\"hidden\":[{\"id\":4},{\"id\":2}]}"
                }
        };
    }



    private Response getContactWidgetsView(String basePath, String authToken, String viewType) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doGet("JSON", contactServiceURL, basePath, authToken, queryParameters, null, true);
        assertThat("Response status should be 200", response.getStatusCode(), equalTo(200));
        return response;
    }

    
    private void verifyViewResponseStructure(Response response) {
        JsonPath jp = response.jsonPath();
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
        verifyViewResponseStructure(response);

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
        updateRequest.setValue("{\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":0,\"6\":0,\"8\":0,\"9\":0,\"10\":0,\"16\":0,\"17\":0,\"18\":0,\"19\":0,\"20\":0,\"21\":1,\"22\":1}");

        Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields", albatrossAuthToken, null, true, updateRequest);

        assert response != null;
        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Message type should be is-success", jp.get("message_type"), equalTo("is-success"));
        
    }
}
