package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class PostUpdateFieldsTest extends TestBase {

    String albatrossTkn;
    int userId;
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "updateFieldsTestData", groups = {"candidate_service", "nightly-build"})
    public void testEntityViewLockSettingsUpdateFields_Success(String key, String value, String tableFlag, int id) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("key", key);
        requestBody.put("value", value);
        requestBody.put("tableFlag", tableFlag);
        requestBody.put("id", id);

        // Make API call
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/update-fields", 
                albatrossTkn, null, null, true, requestBody.toString());

        // Validate response
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        // Validate response structure
        JsonPath jp = response.jsonPath();
        assertThat("Message should match expected", jp.get("message"), equalTo("Update Field Successful "));
        assertThat("Message type should be is-success", jp.get("message_type"), equalTo("is-success"));
        assertThat("Data should be empty array", (Integer) jp.get("data.size()"), equalTo(0));
        assertThat("Notifications should be empty array", (Integer) jp.get("notifications.size()"), equalTo(0));
        assertThat("User object should not be null", jp.get("user"), notNullValue());
        assertThat("Status should be success", jp.get("status"), equalTo("success"));
        assertThat("Action name should be Update Field", jp.get("action_name"), equalTo("Update Field"));
        assertThat("Action ID should not be null", jp.get("actionid"), notNullValue());
        assertThat("Intercom should not be null", jp.get("intercom"), notNullValue());
        assertThat("Recaptcha site key should not be null", jp.get("recaptcha_site_key"), notNullValue());
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "updateFieldsTestData", groups = {"candidate_service", "nightly-build"})
    public void testEntityViewLockSettingsUpdateFields_WithoutAuth(String key, String value, String tableFlag, int id) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("key", key);
        requestBody.put("value", value);
        requestBody.put("tableFlag", tableFlag);
        requestBody.put("id", id);

        // Make API call without auth
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/update-fields", 
                null, null, null, true, requestBody.toString());

        // Validate response
        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "updateFieldsTestData", groups = {"candidate_service", "nightly-build"})
    public void testEntityViewLockSettingsUpdateFields_InvalidAuth(String key, String value, String tableFlag, int id) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("key", key);
        requestBody.put("value", value);
        requestBody.put("tableFlag", tableFlag);
        requestBody.put("id", id);

        // Make API call with invalid auth
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/update-fields", 
                albatrossTkn + "invalid_token", null, null, true, requestBody.toString());

        // Validate response
        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "updateFieldsTestData", groups = {"candidate_service", "nightly-build"})
    public void testEntityViewLockSettingsUpdateFields_MissingKey(String key, String value, String tableFlag, int id) {
        // Create request body without key
        JSONObject requestBody = new JSONObject();
        requestBody.put("value", value);
        requestBody.put("tableFlag", tableFlag);
        requestBody.put("id", id);

        // Make API call
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/update-fields", 
                albatrossTkn, null, null, true, requestBody.toString());

        JsonPath jp = response.jsonPath();        
        assertThat("Expected status code 422 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(422));
        assertThat("Expected message should be Key field is required", jp.get("message"), equalTo("The key field is required."));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "updateFieldsTestData", groups = {"candidate_service", "nightly-build"})
    public void testEntityViewLockSettingsUpdateFields_MissingTableFlag(String key, String value, String tableFlag, int id) {
        // Create request body without tableFlag
        JSONObject requestBody = new JSONObject();
        requestBody.put("key", key);
        requestBody.put("value", value);
        requestBody.put("id", id);

        // Make API call
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/update-fields", 
                albatrossTkn, null, null, true, requestBody.toString());

        JsonPath jp = response.jsonPath();
        assertThat("Expected status code 422 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(422));
        assertThat("Expected message should be Table flag field is required", jp.get("message"), equalTo("The table flag field is required."));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testEntityViewLockSettingsUpdateFields_EmptyRequestBody() {
        // Make API call with empty request body
        Response response = RestClient.doPost1("JSON", albatrossURL, "global/update-fields", 
                albatrossTkn, null, null, true, "{}");

        // Validate response - should return 422 for empty request body
        assertThat("Expected status code 422 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(422));
    }

    @DataProvider(name = "updateFieldsTestData")
    public Object[][] getUpdateFieldsTestData() {
        // Test data for update fields endpoint
        String entityViewLockSettingsValue = "{\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":0,\"6\":0,\"8\":0,\"9\":0,\"10\":1,\"16\":0}";

        Response getUsers = albatrossFunctions.getUsers(albatrossURL, albatrossTkn);
        assertThat("Failed to get users", getUsers.getStatusCode(), equalTo(200));
        JsonPath jp = getUsers.jsonPath();
        userId = jp.get("data.records[0].id");
        assertThat("User ID should not be null", userId, notNullValue());
        
        return new Object[][] { 
            { "entity_view_lock_settings", entityViewLockSettingsValue, "accountsettings", userId }
        };
    }
}
