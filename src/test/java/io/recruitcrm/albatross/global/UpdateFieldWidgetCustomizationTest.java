package io.recruitcrm.albatross.global;

import io.rcrm.api.pojo.albatross.global.UpdateFieldWidgetCustomizationRequest;
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

@TestBase.AccountType("CrossAccount")
public class UpdateFieldWidgetCustomizationTest extends TestBase {

    // Cross account test fields
    private String albatrossAuthTokenA;
    private String publicAPIKeyA;
    private String albatrossAuthTokenB;
    private String publicAPIKeyB;
    private int actualUserIdA;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        // Setup for AccountA
        albatrossAuthTokenA = getTokenForAccount("AccountA","valid");
        publicAPIKeyA = getAccountApiKey("AccountA");
        
        // Setup for AccountB  
        albatrossAuthTokenB = getTokenForAccount("AccountB","valid");
        publicAPIKeyB = getAccountApiKey("AccountB");
        getOwnerDetailsFromAPI();
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "UpdateFieldWidgetCustomizationTestData", groups = {"candidate_service", "company_service", "contact_service", "nightly-build"})
    public void testUpdateFieldsSuccess(String testDescription, String value, String expectedMessage, String expectedActionName) {
        UpdateFieldWidgetCustomizationRequest updateRequest = new UpdateFieldWidgetCustomizationRequest();
        updateRequest.setId(actualUserIdA);
        updateRequest.setSilentProcess(true);
        updateRequest.setKey("entity_view_lock_settings");
        updateRequest.setTableFlag("accountsettings");
        updateRequest.setValue(value);

        Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields", albatrossAuthTokenA, null, true, updateRequest);

        assert response != null;
        assertThat("Expected status code 200 for " + testDescription, response.getStatusCode(), equalTo(200));

        // Validate using JsonPath
        JsonPath jp = response.jsonPath();
        assertThat("Message type should be is-success for " + testDescription, jp.get("message_type"), equalTo("is-success"));
        assertThat("Status should be success for " + testDescription, jp.get("status"), equalTo("success"));
        assertThat("Action name should match expected for " + testDescription, jp.getString("action_name"), equalTo(expectedActionName));
        assertThat("Message should match expected for " + testDescription, jp.getString("message"), equalTo(expectedMessage));
        assertThat("Action ID should not be null for " + testDescription, jp.get("actionid"), notNullValue());
        assertThat("Data should be empty array", jp.getList("data").size(), equalTo(0));
        assertThat("Recaptcha site key should not be null", jp.get("recaptcha_site_key"), notNullValue());

        String responseBody = response.getBody().asString();
        JSONObject jsonResponse = new JSONObject(responseBody);
        assertThat("Response should have user object", jsonResponse.has("user"), is(true));
        assertThat("Response should have intercom object", jsonResponse.has("intercom"), is(true));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/global/updateFields.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"candidate_service", "company_service", "contact_service", "nightly-build"})
    public void testUpdateFieldsWithInvalidAuth() {
        UpdateFieldWidgetCustomizationRequest updateRequest = new UpdateFieldWidgetCustomizationRequest();
        updateRequest.setId(actualUserIdA);
        updateRequest.setSilentProcess(true);
        updateRequest.setKey("entity_view_lock_settings");
        updateRequest.setTableFlag("accountsettings");
        updateRequest.setValue("{\"1\":0, \"2\":0, \"3\":0, \"4\":0, \"5\":0, \"6\":0, \"7\":0, \"8\":0, \"9\":1, \"10\":0, \"15\":0, \"16\":0, \"17\":0, \"18\":0, \"19\":0, \"20\":0, \"21\":0, \"22\":0}");

        String invalidToken = albatrossAuthTokenA + "invalid";
        Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields", invalidToken, null, true, updateRequest);

        assert response != null;
        assertThat("Expected status code 401 for unauthorized access", response.getStatusCode(), equalTo(401));
        JsonPath jp = response.jsonPath();
        assertThat("Error should be 'Unauthorized'", jp.get("error"), equalTo("Unauthorized"));
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "invalidDataTestData", groups = {"candidate_service", "company_service", "contact_service", "nightly-build"})
    public void testUpdateFieldsWithInvalidData(String testDescription, String key, String tableFlag, 
                                               String value, int expectedStatusCode, String expectedMessage) {
        UpdateFieldWidgetCustomizationRequest updateRequest = new UpdateFieldWidgetCustomizationRequest();
        updateRequest.setId(actualUserIdA);
        updateRequest.setSilentProcess(true);
        updateRequest.setKey(key);
        updateRequest.setTableFlag(tableFlag);
        updateRequest.setValue(value);

        Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields",
                albatrossAuthTokenA, null, true, updateRequest);

        assert response != null;
        assertThat("Expected status code " + expectedStatusCode + " for " + testDescription, 
                response.getStatusCode(), equalTo(expectedStatusCode));

        // Validate error response structure
        JsonPath jp = response.jsonPath();
        assertThat("Message type should be is-danger for " + testDescription, jp.get("message_type"), equalTo("is-danger"));
        assertThat("Status should be fail for " + testDescription, jp.get("status"), equalTo("fail"));
        assertThat("Expected specific error message for " + testDescription, jp.getString("message"), equalTo(expectedMessage));
        assertThat("Data should be empty array", jp.getList("data").size(), equalTo(0));
        assertThat("Notifications should be empty array", jp.getList("notifications").size(), equalTo(0));
        assertThat("Recaptcha site key should not be null", jp.get("recaptcha_site_key"), notNullValue());
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"candidate_service", "company_service", "contact_service", "nightly-build"})
    public void testUpdateFieldsWithInvalidUserId() {
        UpdateFieldWidgetCustomizationRequest updateRequest = new UpdateFieldWidgetCustomizationRequest();
        updateRequest.setId(999999); // Non-existent user ID
        updateRequest.setSilentProcess(true);
        updateRequest.setKey("entity_view_lock_settings");
        updateRequest.setTableFlag("accountsettings");
        updateRequest.setValue("{\"1\":0, \"2\":0, \"3\":0, \"4\":0, \"5\":0, \"6\":0, \"7\":0, \"8\":0, \"9\":1, \"10\":0, \"15\":0, \"16\":0, \"17\":0, \"18\":0, \"19\":0, \"20\":0, \"21\":0, \"22\":0}");

        Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields", albatrossAuthTokenA, null, true, updateRequest);

        assert response != null;
        assertThat("Expected status code 401 for invalid user ID", response.getStatusCode(), equalTo(401));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = {"candidate_service", "company_service", "contact_service", "nightly-build"})
    public void testCrossAccountUpdateFields() {
        UpdateFieldWidgetCustomizationRequest updateRequest = new UpdateFieldWidgetCustomizationRequest();
        updateRequest.setId(actualUserIdA); // Using AccountA's user ID
        updateRequest.setSilentProcess(true);
        updateRequest.setKey("entity_view_lock_settings");
        updateRequest.setTableFlag("accountsettings");
        updateRequest.setValue("{\"1\":0, \"2\":0, \"3\":0, \"4\":0, \"5\":0, \"6\":0, \"7\":0, \"8\":0, \"9\":1, \"10\":0, \"15\":0, \"16\":0, \"17\":0, \"18\":0, \"19\":0, \"20\":0, \"21\":0, \"22\":0}");

        // Try to access with AccountB's token
        Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields", albatrossAuthTokenB, null, true, updateRequest);
        assert response != null;
        assertThat("Expected status code 401 for cross-account access", response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();
        assertThat("Error should be 'Unauthorized'", jp.get("error"), equalTo("Unauthorized"));
    }

    @DataProvider(name = "UpdateFieldWidgetCustomizationTestData", parallel = true)
    public Object[][] UpdateFieldWidgetCustomizationTestData() {
        return new Object[][]{
                {
                        "Candidate Activity Sidebar customize",
                        "{\"1\":0, \"2\":0, \"3\":0, \"4\":0, \"5\":0, \"6\":0, \"7\":0, \"8\":0, \"9\":1, \"10\":0, \"15\":0, \"16\":0, \"17\":0, \"18\":0, \"19\":0, \"20\":0, \"21\":0, \"22\":0}",
                        "Update Field Successful ",
                        "Update Field"
                },
                {
                        "Candidate Widget customize",
                        "{\"1\":0, \"2\":0, \"3\":0, \"4\":0, \"5\":0, \"6\":0, \"7\":0, \"8\":1, \"9\":0, \"10\":0, \"15\":0, \"16\":0, \"17\":0, \"18\":0, \"19\":0, \"20\":0, \"21\":0, \"22\":0}",
                        "Update Field Successful ",
                        "Update Field"
                },
                {
                        "Company Activity Sidebar customize",
                        "{\"1\":0, \"2\":0, \"3\":0, \"4\":0, \"5\":0, \"6\":0, \"7\":0, \"8\":0, \"9\":0, \"10\":0, \"15\":0, \"16\":0, \"17\":0, \"18\":0, \"19\":1, \"20\":0, \"21\":0, \"22\":0}",
                        "Update Field Successful ",
                        "Update Field"
                },
                {
                        "Company Widget customize",
                        "{\"1\":0, \"2\":0, \"3\":0, \"4\":0, \"5\":0, \"6\":0, \"7\":0, \"8\":0, \"9\":0, \"10\":0, \"15\":0, \"16\":0, \"17\":0, \"18\":1, \"19\":0, \"20\":0, \"21\":0, \"22\":0}",
                        "Update Field Successful ",
                        "Update Field"
                },
                {
                        "Contact Activity Sidebar customize",
                        "{\"1\":0, \"2\":0, \"3\":0, \"4\":0, \"5\":0, \"6\":0, \"7\":0, \"8\":0, \"9\":0, \"10\":0, \"15\":0, \"16\":0, \"17\":0, \"18\":0, \"19\":0, \"20\":0, \"21\":0, \"22\":1}",
                        "Update Field Successful ",
                        "Update Field"
                },
                {
                        "Contact Widget customize",
                        "{\"1\":0, \"2\":0, \"3\":0, \"4\":0, \"5\":0, \"6\":0, \"7\":0, \"8\":0, \"9\":0, \"10\":0, \"15\":0, \"16\":0, \"17\":0, \"18\":0, \"19\":0, \"20\":0, \"21\":1, \"22\":0}",
                        "Update Field Successful ",
                        "Update Field"
                }
        };
    }

    @DataProvider(name = "invalidDataTestData", parallel = true)
    public Object[][] invalidDataTestData() {
        return new Object[][]{
                {
                        "Empty key field",
                        "", // empty key
                        "accountsettings",
                        "{\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":0,\"6\":0,\"8\":1,\"9\":0,\"10\":0,\"16\":0,\"17\":0,\"18\":0,\"19\":0,\"20\":0,\"21\":0,\"22\":0}",
                        422,
                        "The key field is required."
                },
                {
                        "Empty tableFlag field",
                        "entity_view_lock_settings",
                        "", // empty tableFlag
                        "{\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":0,\"6\":0,\"8\":1,\"9\":0,\"10\":0,\"16\":0,\"17\":0,\"18\":0,\"19\":0,\"20\":0,\"21\":0,\"22\":0}",
                        422,
                        "The table flag field is required."
                },
                {
                        "Empty value field",
                        "entity_view_lock_settings",
                        "accountsettings",
                        "", // empty value
                        200,
                        "Failed To Update Field : Value is not valid"
                },
                {
                        "Invalid value field",
                        "entity_view_lock_settings", 
                        "accountsettings",
                        "{\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":0,\"6\":0,\"7\":0,\"8\":1,\"9\":1,\"10\":0}",
                        200,
                        "Failed To Update Field : Value is not valid"
                }
        };
    }

    // ---------------- Helper Methods ----------------

    private void getOwnerDetailsFromAPI() {
        Response response = RestClient.doGet("JSON", baseURL, "users", publicAPIKeyA, null, null, true);
        if (response != null && response.getStatusCode() == 200) {
            JsonPath jsonPath = response.jsonPath();
            actualUserIdA = jsonPath.getInt("[0].id");
            assertThat("Expected valid user ID", actualUserIdA, greaterThan(0));
        } else {
            throw new AssertionError("Failed to retrieve owner details from API. Response: " +
                    (response != null ? response.getBody().asString() : "null response"));
        }
    }
}
