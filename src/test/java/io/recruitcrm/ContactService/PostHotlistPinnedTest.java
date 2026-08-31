package io.recruitcrm.ContactService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class PostHotlistPinnedTest extends TestBase {

    String apiAuthToken;
    String albatrossTkn;
    JavaFakerContact faker;
    commanFunction function;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        faker = new JavaFakerContact();
        function = new commanFunction();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistData", groups = {"contact_service", "nightly-build"})
    public void testHotlistPin_Success(String hotlistId, String hotlistName) {
        Response response = pinHotlist(hotlistId, albatrossTkn);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Hotlist pinned successfully."));
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("Context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Response code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data is null for pin operation
        assertThat("Data should be null for pin operation", jp.get("data"), nullValue());

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/contact/hotlistPinned.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistData", groups = {"contact_service", "nightly-build"})
    public void testHotlistUnpin_Success(String hotlistId, String hotlistName) {
        // First pin the hotlist
        Response pinResponse = pinHotlist(hotlistId, albatrossTkn);
        assertThat("Pin should succeed first", pinResponse.getStatusCode(), equalTo(200));

        // Now unpin the hotlist
        Response response = unpinHotlist(hotlistId, albatrossTkn);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Hotlist unpinned successfully."));
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("Context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Response code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data is null for unpin operation
        assertThat("Data should be null for unpin operation", jp.get("data"), nullValue());

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/contact/hotlistUnpinned.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistData", groups = {"contact_service", "nightly-build"})
    public void testHotlistPinUnpin_Workflow(String hotlistId, String hotlistName) {
        // Step 1: Pin the hotlist
        Response pinResponse = pinHotlist(hotlistId, albatrossTkn);
        assertThat("Pin should succeed", pinResponse.getStatusCode(), equalTo(200));
        assertThat("Pin message should match", pinResponse.jsonPath().get("meta.message"), equalTo("Hotlist pinned successfully."));

        // Step 2: Try to pin again (should still succeed or handle gracefully)
        Response pinAgainResponse = pinHotlist(hotlistId, albatrossTkn);
        assertThat("Pin again should be handled appropriately", pinAgainResponse.getStatusCode(), equalTo(409));
        assertThat("Pin again should contain message that hotlist is already pinned", pinAgainResponse.jsonPath().get("errors[0].message"), equalTo("Hotlist is already pinned."));

        // Step 3: Unpin the hotlist
        Response unpinResponse = unpinHotlist(hotlistId, albatrossTkn);
        assertThat("Unpin should succeed", unpinResponse.getStatusCode(), equalTo(200));
        assertThat("Unpin message should match", unpinResponse.jsonPath().get("meta.message"), equalTo("Hotlist unpinned successfully."));

        // Step 4: Try to unpin again (should still succeed or handle gracefully)
        Response unpinAgainResponse = unpinHotlist(hotlistId, albatrossTkn);
        assertThat("Unpin again should be handled appropriately", 
                unpinAgainResponse.getStatusCode(), equalTo(409));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testHotlistPin_WithoutAuth() {
        Response response = pinHotlist("123456", null);

        assertThat("Expected status code 401 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testHotlistUnpin_WithoutAuth() {
        Response response = unpinHotlist("123456", null);

        assertThat("Expected status code 401 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testHotlistPin_InvalidAuth() {
        Response response = pinHotlist("123456", albatrossTkn+"123");

        assertThat("Expected status code 401 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testHotlistUnpin_InvalidAuth() {
        Response response = unpinHotlist("123456", albatrossTkn+"123");

        assertThat("Expected status code 401 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testHotlistPin_InvalidHotlistId() {
        Response response = pinHotlist("invalid-hotlist-id-123", albatrossTkn);

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testHotlistUnpin_InvalidHotlistId() {
        Response response = unpinHotlist("invalid-hotlist-id-123", albatrossTkn);

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testHotlistPin_MissingHotlistId() {
        // Test with empty hotlist ID
        Response response = pinHotlist("", albatrossTkn);

        assertThat("Expected status code 404 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(404));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testHotlistUnpin_MissingHotlistId() {
        // Test with empty hotlist ID
        Response response = unpinHotlist("", albatrossTkn);

        assertThat("Expected status code 404 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(404));
    }

    @DataProvider(name = "hotlistData")
    public Object[][] getHotlistData() {
        // Create test hotlist using function
        Response hotlistResponse = function.createNewHotlist(baseURL, apiAuthToken, "contact");
        
        assertThat("Failed to create test hotlist", hotlistResponse.getStatusCode(), equalTo(200));
        
        JsonPath jp = hotlistResponse.jsonPath();
        String hotlistId = jp.getString("id");
        String hotlistName = jp.getString("name");
        
        assertThat("Hotlist ID should not be null", hotlistId, notNullValue());
        assertThat("Hotlist name should not be null", hotlistName, notNullValue());
        
        return new Object[][] { { hotlistId, hotlistName } };
    }

    private Response pinHotlist(String hotlistId, Object token) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", hotlistId);
        String basePath = "hotlists/{hotlist}/pinned-hotlist";
        return RestClient.doPost1("JSON", contactServiceURL, basePath, token, null, pathParameters, true, null);
    }

    private Response unpinHotlist(String hotlistId, Object token) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", hotlistId);
        String basePath = "hotlists/{hotlist}/pinned-hotlist";
        return RestClient.doDelete("JSON", contactServiceURL, basePath, token, null, pathParameters, true);
    }
}

