package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class PostSaveStateTest extends TestBase {

    String apiAuthToken;
    String albatrossTkn;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "saveStateTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateAssignedJobSaveState_Success(String datatablekey, String columnstate) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("datatablekey", datatablekey);
        requestBody.put("columnstate", columnstate);

        // Make API call
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/save-state", 
                albatrossTkn, null, null, true, requestBody.toString());

        // Validate response
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        // Validate response structure
        JsonPath jp = response.jsonPath();
        assertThat("Message should be 0", jp.get("message"), equalTo("0"));
        assertThat("Message type should be is-success", jp.get("message_type"), equalTo("is-success"));
        assertThat("Data should be empty array", (Integer) jp.get("data.size()"), equalTo(0));
        assertThat("Notifications should be empty array", (Integer) jp.get("notifications.size()"), equalTo(0));
        assertThat("User object should not be null", jp.get("user"), notNullValue());
        assertThat("Status should be success", jp.get("status"), equalTo("success"));
        assertThat("Action name should be State Saved", jp.get("action_name"), equalTo("State Saved"));
        assertThat("Action ID should not be null", jp.get("actionid"), notNullValue());
        assertThat("Intercom should be null", jp.get("intercom"), nullValue());
        assertThat("Recaptcha site key should not be null", jp.get("recaptcha_site_key"), notNullValue());

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/global/saveState.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "saveStateTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateAssignedJobSaveState_WithoutAuth(String datatablekey, String columnstate) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("datatablekey", datatablekey);
        requestBody.put("columnstate", columnstate);

        // Make API call without auth
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/save-state", 
                null, null, null, true, requestBody.toString());

        // Validate response
        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "saveStateTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateAssignedJobSaveState_InvalidAuth(String datatablekey, String columnstate) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("datatablekey", datatablekey);
        requestBody.put("columnstate", columnstate);

        // Make API call with invalid auth
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/save-state", 
                albatrossTkn + "invalid_token", null, null, true, requestBody.toString());

        // Validate response
        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "saveStateTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateAssignedJobSaveState_MissingDatatablekey(String datatablekey, String columnstate) {
        // Create request body without datatablekey
        JSONObject requestBody = new JSONObject();
        requestBody.put("columnstate", columnstate);

        // Make API call
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/save-state", 
                albatrossTkn, null, null, true, requestBody.toString());

        // Response is 200 but message is "Required data is missing in the request"
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        assertThat("Message should be Required data is missing in the request", response.jsonPath().get("message"), equalTo("Required data is missing in the request"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "saveStateTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateAssignedJobSaveState_MissingColumnstate(String datatablekey, String columnstate) {
        // Create request body without columnstate
        JSONObject requestBody = new JSONObject();
        requestBody.put("datatablekey", datatablekey);

        // Make API call
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/save-state", 
                albatrossTkn, null, null, true, requestBody.toString());

        // Response is 200 but message is "Required data is missing in the request"
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        assertThat("Message should be Required data is missing in the request", response.jsonPath().get("message"), equalTo("Required data is missing in the request"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testCandidateAssignedJobSaveState_EmptyRequestBody() {
        // Make API call with empty request body
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/save-state", 
                albatrossTkn, null, null, true, "{}");

        // Response is 200 but message is "Required data is missing in the request"
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        assertThat("Message should be Required data is missing in the request", response.jsonPath().get("message"), equalTo("Required data is missing in the request"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "saveStateOthersViewTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateAssignedJobSaveStateOthersView_Success(String datatablekey, String columnstate) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("datatablekey", datatablekey);
        requestBody.put("columnstate", columnstate);

        // Make API call
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/save-state", 
                albatrossTkn, null, null, true, requestBody.toString());

        // Validate response
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        // Validate response structure
        JsonPath jp = response.jsonPath();
        assertThat("Message should match expected", jp.get("message"), equalTo("Field order and visibility has been set for other users successfully"));
        assertThat("Message type should be is-success", jp.get("message_type"), equalTo("is-success"));
        assertThat("Data should be empty array", (Integer) jp.get("data.size()"), equalTo(0));
        assertThat("Notifications should be empty array", (Integer) jp.get("notifications.size()"), equalTo(0));
        assertThat("User object should not be null", jp.get("user"), notNullValue());
        assertThat("Status should be success", jp.get("status"), equalTo("success"));
        assertThat("Action name should be State Saved", jp.get("action_name"), equalTo("State Saved"));
        assertThat("Action ID should not be null", jp.get("actionid"), notNullValue());
        assertThat("Intercom should be null", jp.get("intercom"), nullValue());
        assertThat("Recaptcha site key should not be null", jp.get("recaptcha_site_key"), notNullValue());

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/global/saveState.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "saveStateOthersViewTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateAssignedJobSaveStateOthersView_WithoutAuth(String datatablekey, String columnstate) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("datatablekey", datatablekey);
        requestBody.put("columnstate", columnstate);

        // Make API call without auth
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/save-state", 
                null, null, null, true, requestBody.toString());

        // Validate response
        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "saveStateOthersViewTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateAssignedJobSaveStateOthersView_InvalidAuth(String datatablekey, String columnstate) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("datatablekey", datatablekey);
        requestBody.put("columnstate", columnstate);

        // Make API call with invalid auth
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/save-state", 
                albatrossTkn + "invalid_token", null, null, true, requestBody.toString());

        // Validate response
        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "saveStateOthersViewTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateAssignedJobSaveStateOthersView_MissingDatatablekey(String datatablekey, String columnstate) {
        // Create request body without datatablekey
        JSONObject requestBody = new JSONObject();
        requestBody.put("columnstate", columnstate);

        // Make API call
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/save-state", 
                albatrossTkn, null, null, true, requestBody.toString());

        // Response is 200 but message is "Required data is missing in the request"
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        assertThat("Message should be Required data is missing in the request", response.jsonPath().get("message"), equalTo("Required data is missing in the request"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "saveStateOthersViewTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateAssignedJobSaveStateOthersView_MissingColumnstate(String datatablekey, String columnstate) {
        // Create request body without columnstate
        JSONObject requestBody = new JSONObject();
        requestBody.put("datatablekey", datatablekey);

        // Make API call
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/save-state", 
                albatrossTkn, null, null, true, requestBody.toString());

        // Response is 200 but message is "Required data is missing in the request"
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        assertThat("Message should be Required data is missing in the request", response.jsonPath().get("message"), equalTo("Required data is missing in the request"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testCandidateAssignedJobSaveStateOthersView_EmptyRequestBody() {
        // Make API call with empty request body
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/save-state", 
                albatrossTkn, null, null, true, "{}");

        // Response is 200 but message is "Required data is missing in the request"
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        assertThat("Message should be Required data is missing in the request", response.jsonPath().get("message"), equalTo("Required data is missing in the request"));
    }

    @DataProvider(name = "saveStateTestData")
    public Object[][] getSaveStateTestData() {
        // Read column state from JSON file
        JSONObject columnStateJson = readJsonFileFromPath("src/test/resources/privateApi/global/candidateAssignedJobColumnState.json");
        String candidateAssignedJobColumnState = columnStateJson.getJSONObject("candidateAssignedJobColumnState").toString();
        
        return new Object[][] { { "candidate_assigned_job", candidateAssignedJobColumnState } };
    }

    @DataProvider(name = "saveStateOthersViewTestData")
    public Object[][] getSaveStateOthersViewTestData() {
        // Read column state from JSON file
        JSONObject columnStateJson = readJsonFileFromPath("src/test/resources/privateApi/global/candidateAssignedJobColumnState.json");
        String candidateAssignedJobOthersViewColumnState = columnStateJson.getJSONObject("candidateAssignedJobOthersViewColumnState").toString();
        
        return new Object[][] { { "candidate_assigned_job_others_view", candidateAssignedJobOthersViewColumnState } };
    }
}
