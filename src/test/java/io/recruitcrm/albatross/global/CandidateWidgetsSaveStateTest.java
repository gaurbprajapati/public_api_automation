package io.recruitcrm.albatross.global;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.global.CandidateWidgetsSaveState;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class CandidateWidgetsSaveStateTest extends TestBase {

    private String albatrossAuthToken;
    private int ownerAccountID;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "candidateWidgetsTestData", groups = {"candidate_service", "nightly-build"})
    public void testGlobalSaveStateSuccess(String testDescription, String columnstate, String datatablekey,
                                           boolean expectedSilentProgress, String expectedMessage) {
        CandidateWidgetsSaveState saveStateRequest = new CandidateWidgetsSaveState();
        saveStateRequest.setColumnstate(columnstate);
        saveStateRequest.setDatatablekey(datatablekey);

        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", albatrossAuthToken, null, true, saveStateRequest);
        assertThat("Expected status code 200 for " + testDescription, response.getStatusCode(), equalTo(200));

        // Validate using JsonPath
        JsonPath jp = response.jsonPath();
        assertThat("Message type should be success for " + testDescription, jp.get("message_type"), equalTo("is-success"));
        assertThat("Status should be success for " + testDescription, jp.get("status"), equalTo("success"));
        assertThat("Action name should be 'State Saved' for " + testDescription, jp.getString("action_name"), equalTo("State Saved"));
        assertThat("Action ID should not be null for " + testDescription, jp.get("actionid"), notNullValue());
        assertThat("Account ID should match for " + testDescription, jp.getInt("user.accountid"), equalTo(ownerAccountID));

        // Validate response based on data provider values
        assertThat("Silent progress should match expected for " + testDescription, jp.getBoolean("silent_progress"), is(expectedSilentProgress));
        assertThat("Message should match expected for " + testDescription, jp.getString("message"), equalTo(expectedMessage));

        // Validate using JSONObject
        String responseBody = response.getBody().asString();
        JSONObject jsonResponse = new JSONObject(responseBody);
        assertThat("Response should have silent_progress field", jsonResponse.has("silent_progress"), is(true));
        assertThat("Response should have user object", jsonResponse.has("user"), is(true));

        // Validate schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/global/candidateWidgetsSaveState.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testGlobalSaveStateWithoutAuth() {
        CandidateWidgetsSaveState saveStateRequest = new CandidateWidgetsSaveState();
        saveStateRequest.setColumnstate("{\"visible\":[{\"id\":1},{\"id\":2},{\"id\":3},{\"id\":4},{\"id\":5}],\"hidden\":[{\"id\":6},{\"id\":7},{\"id\":8},{\"id\":9},{\"id\":10}]}");
        saveStateRequest.setDatatablekey("candidate_widgets");

        Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", albatrossAuthToken + "invalid", null, true, saveStateRequest);

        assertThat("Expected status code 401 for unauthorized access", response.getStatusCode(), equalTo(401));
        JsonPath jp = response.jsonPath();
        assertThat("Error message should be 'Unauthorized'", jp.get("error"), equalTo("Unauthorized"));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "emptyDataTestData", groups = {"candidate_service", "nightly-build"})
    public void testGlobalSaveStateEmptyData(String testDescription, String columnstate, String datatablekey) {
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
    public Object[][] candidateWidgetsTestData() {
        return new Object[][]{
                {
                        "Own View - Candidate Widgets Customization",
                        "{\"visible\":[{\"id\":1},{\"id\":2},{\"id\":6},{\"id\":5},{\"id\":7},{\"id\":4}],\"hidden\":[{\"id\":8},{\"id\":9},{\"id\":10},{\"id\":3}]}",
                        "candidate_widgets",
                        true,  // expectedSilentProgress
                        "0"    // expectedMessage
                },
                {
                        "Other View - Candidate Widgets Customization",
                        "{\"visible\":[{\"id\":1},{\"id\":2},{\"id\":3},{\"id\":4},{\"id\":5}],\"hidden\":[{\"id\":6},{\"id\":7},{\"id\":8},{\"id\":9},{\"id\":10}]}",
                        "candidate_widgets_others_view",
                        false, // expectedSilentProgress
                        "Field order and visibility has been set for other users successfully" // expectedMessage
                }
        };
    }

    // If we pass any invalid string in place of empty there it will pass - existing endpoint issue
    @DataProvider
    public Object[][] emptyDataTestData() {
        return new Object[][]{
                {
                        "Empty columnstate with candidate_widgets datatablekey",
                        "",  // empty columnstate
                        "candidate_widgets"
                },
                {
                        "Empty columnstate with candidate_widgets_others_view datatablekey",
                        "",  // empty columnstate
                        "candidate_widgets_others_view"
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
}
