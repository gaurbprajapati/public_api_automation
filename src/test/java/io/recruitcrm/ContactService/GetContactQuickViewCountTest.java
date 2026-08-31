package io.recruitcrm.ContactService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.pojo.offlimit.MarkContactOffLimit;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetContactQuickViewCountTest extends TestBase {

    String apiAuthToken;
    String albatrossTkn;
    commanFunction commanFunction;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        commanFunction = new commanFunction();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactQuickViewCount_Success() {
        Response response = RestClient.doGet("JSON", contactServiceURL, "contacts/quick-view-count",
                albatrossTkn, null, null, true);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Contact quick view data fetched successfully"));
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("Context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Response code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data array
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Data array should not be empty", (Integer) jp.get("data.size()"), greaterThan(0));

        // Verify all required fields in data
        assertThat("allContacts should not be null", jp.get("data[0].allContacts"), notNullValue());
        assertThat("myContacts should not be null", jp.get("data[0].myContacts"), notNullValue());
        assertThat("notInAnyHotlist should not be null", jp.get("data[0].notInAnyHotlist"), notNullValue());
        assertThat("offLimitContacts should not be null", jp.get("data[0].offLimitContacts"), notNullValue());

        // Verify all values are non-negative integers
        assertThat("allContacts should be non-negative", (Integer) jp.get("data[0].allContacts"), greaterThanOrEqualTo(0));
        assertThat("myContacts should be non-negative", (Integer) jp.get("data[0].myContacts"), greaterThanOrEqualTo(0));
        assertThat("notInAnyHotlist should be non-negative", (Integer) jp.get("data[0].notInAnyHotlist"), greaterThanOrEqualTo(0));
        assertThat("offLimitContacts should be non-negative", (Integer) jp.get("data[0].offLimitContacts"), greaterThanOrEqualTo(0));

        // Verify logical constraints
        assertThat("myContacts should not exceed allContacts", 
                (Integer) jp.get("data[0].myContacts"), lessThanOrEqualTo((Integer) jp.get("data[0].allContacts")));
        assertThat("notInAnyHotlist should not exceed allContacts", 
                (Integer) jp.get("data[0].notInAnyHotlist"), lessThanOrEqualTo((Integer) jp.get("data[0].allContacts")));
        assertThat("offLimitContacts should not exceed allContacts", 
                (Integer) jp.get("data[0].offLimitContacts"), lessThanOrEqualTo((Integer) jp.get("data[0].allContacts")));

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/contact/contactQuickViewCount.json"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactQuickViewCount_WithoutAuth() {
        Response response = RestClient.doGet("JSON", contactServiceURL, "contacts/quick-view-count",
                null, null, null, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactQuickViewCount_InvalidAuth() {
        Response response = RestClient.doGet("JSON", contactServiceURL, "contacts/quick-view-count",
                albatrossTkn + "invalid-token-123", null, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testAllContactsCount_AfterCreatingContact() {
        // Get initial count
        Response initialResponse = RestClient.doGet("JSON", contactServiceURL, "contacts/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        JsonPath initialJp = initialResponse.jsonPath();
        int initialAllContacts = (Integer) initialJp.get("data[0].allContacts");
        int initialMyContacts = (Integer) initialJp.get("data[0].myContacts");
        int initialNotInAnyHotlist = (Integer) initialJp.get("data[0].notInAnyHotlist");
        int initialOffLimitContacts = (Integer) initialJp.get("data[0].offLimitContacts");

        String contactSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "contact");
        assertThat("Contact slug should not be null", contactSlug, notNullValue());

        // Get count after creating contact
        Response afterCreateResponse = RestClient.doGet("JSON", contactServiceURL, "contacts/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("After create response should succeed", afterCreateResponse.getStatusCode(), equalTo(200));
        JsonPath afterCreateJp = afterCreateResponse.jsonPath();
        int afterCreateAllContacts = (Integer) afterCreateJp.get("data[0].allContacts");
        int afterCreateMyContacts = (Integer) afterCreateJp.get("data[0].myContacts");
        int afterCreateNotInAnyHotlist = (Integer) afterCreateJp.get("data[0].notInAnyHotlist");
        int afterCreateOffLimitContacts = (Integer) afterCreateJp.get("data[0].offLimitContacts");

        // Verify allContacts count increased
        assertThat("All contacts count should increase after creating contact", 
                afterCreateAllContacts, equalTo(initialAllContacts + 1));
        
        // Verify myContacts count remains same (as the new contact owner is currently set to null)
        assertThat("My contacts count should increase after creating contact", 
                afterCreateMyContacts, equalTo(initialMyContacts+1));
        
        // Verify notInAnyHotlist count increased (new contact is not in any hotlist)
        assertThat("Not in any hotlist count should increase after creating contact", 
                afterCreateNotInAnyHotlist, equalTo(initialNotInAnyHotlist + 1));
        
        // Verify offLimitContacts count remains the same (new contact is not off-limit)
        assertThat("Off-limit contacts count should remain the same after creating contact", 
                afterCreateOffLimitContacts, equalTo(initialOffLimitContacts));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "contactCountData", groups = {"contact_service", "nightly-build"})
    public void testMyContactsCount_AfterOwnershipChange(String contactSlug, int contactId, String contactFirstName, String contactLastName) {
        // Get initial myContacts count
        Response initialResponse = RestClient.doGet("JSON", contactServiceURL, "contacts/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        JsonPath initialJp = initialResponse.jsonPath();
        int initialAllContacts = (Integer) initialJp.get("data[0].allContacts");
        int initialMyContacts = (Integer) initialJp.get("data[0].myContacts");
        int initialNotInAnyHotlist = (Integer) initialJp.get("data[0].notInAnyHotlist");
        int initialOffLimitContacts = (Integer) initialJp.get("data[0].offLimitContacts");

        // Get a different user to transfer ownership to
        Response usersResponse = commanFunction.getUsers(baseURL, apiAuthToken);
        assertThat("Users response should succeed", usersResponse.getStatusCode(), equalTo(200));
        JsonPath usersJp = usersResponse.jsonPath();
        int newOwnerId = usersJp.get("[1].id");
        assertThat("New owner ID should not be null", newOwnerId, notNullValue());

        commanFunction.transferContactOwnership(albatrossURL, albatrossTkn, contactSlug, newOwnerId);

        // Get count after ownership change
        Response afterChangeResponse = RestClient.doGet("JSON", contactServiceURL, "contacts/quick-view-count",
                albatrossTkn, null, null, true);

        assertThat("After change response should succeed", afterChangeResponse.getStatusCode(), equalTo(200));
        JsonPath afterCreateJp = afterChangeResponse.jsonPath();
        int afterCreateAllContacts = (Integer) afterCreateJp.get("data[0].allContacts");
        int afterCreateMyContacts = (Integer) afterCreateJp.get("data[0].myContacts");
        int afterCreateNotInAnyHotlist = (Integer) afterCreateJp.get("data[0].notInAnyHotlist");
        int afterCreateOffLimitContacts = (Integer) afterCreateJp.get("data[0].offLimitContacts");

        // Verify allContacts count remains same
        assertThat("All contacts count should not increase after transferring ownership",
                afterCreateAllContacts, equalTo(initialAllContacts));

        // Verify myContacts count should decrease
        assertThat("My contacts count should decrease after transferring ownership",
                afterCreateMyContacts, equalTo(initialMyContacts-1));

        // Verify notInAnyHotlist count remains same
        assertThat("Not in any hotlist count should not increase after transferring ownership",
                afterCreateNotInAnyHotlist, equalTo(initialNotInAnyHotlist));

        // Verify offLimitContacts count remains the same
        assertThat("Off-limit contacts count should remain the same after transferring ownership",
                afterCreateOffLimitContacts, equalTo(initialOffLimitContacts));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "contactCountData", groups = {"contact_service", "nightly-build"})
    public void testNotInAnyHotlistCount_AfterAddingToHotlist(String contactSlug, int contactId, String contactFirstName, String contactLastName) {
        // Get initial notInAnyHotlist count
        Response initialResponse = RestClient.doGet("JSON", contactServiceURL, "contacts/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        JsonPath initialJp = initialResponse.jsonPath();
        int initialAllContacts = (Integer) initialJp.get("data[0].allContacts");
        int initialMyContacts = (Integer) initialJp.get("data[0].myContacts");
        int initialNotInAnyHotlist = (Integer) initialJp.get("data[0].notInAnyHotlist");
        int initialOffLimitContacts = (Integer) initialJp.get("data[0].offLimitContacts");

        // Create a hotlist and add contact to it
        Response hotlistResponse = commanFunction.createNewHotlist(baseURL, apiAuthToken, "contact");
        assertThat("Hotlist creation should succeed", hotlistResponse.getStatusCode(), equalTo(200));
        
        JsonPath hotlistJp = hotlistResponse.jsonPath();
        String hotlistId = hotlistJp.getString("id");
        assertThat("Hotlist ID should not be null", hotlistId, notNullValue());

        // Add contact to hotlist
        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(contactSlug);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", hotlistId);
        String basePath = "hotlists/{hotlist}/add-record";

        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParameters, true, hotlistRelated);
        assertThat("Add to hotlist should succeed", addResponse.getStatusCode(), equalTo(200));

        // Get count after adding to hotlist
        Response afterAddResponse = RestClient.doGet("JSON", contactServiceURL, "contacts/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("After add response should succeed", afterAddResponse.getStatusCode(), equalTo(200));
        JsonPath afterCreateJp = afterAddResponse.jsonPath();
        int afterCreateAllContacts = (Integer) afterCreateJp.get("data[0].allContacts");
        int afterCreateMyContacts = (Integer) afterCreateJp.get("data[0].myContacts");
        int afterCreateNotInAnyHotlist = (Integer) afterCreateJp.get("data[0].notInAnyHotlist");
        int afterCreateOffLimitContacts = (Integer) afterCreateJp.get("data[0].offLimitContacts");

        // Verify allContacts count remains same
        assertThat("All contacts count should increase after creating contact",
                afterCreateAllContacts, equalTo(initialAllContacts));

        // Verify myContacts count remains same
        assertThat("My contacts count should increase after creating contact",
                afterCreateMyContacts, equalTo(initialMyContacts));

        // Verify notInAnyHotlist count decreased (or stayed same if contact was already in a hotlist)
        assertThat("Not in any hotlist count should not increase after adding to hotlist", 
                afterCreateNotInAnyHotlist, equalTo(initialNotInAnyHotlist-1));

        // Verify offLimitContacts count remains the same
        assertThat("Off-limit contacts count should remain the same after creating contact",
                afterCreateOffLimitContacts, equalTo(initialOffLimitContacts));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "contactCountData", groups = {"contact_service", "nightly-build"})
    public void testOffLimitContactsCount_AfterSettingOffLimit(String contactSlug, int contactId, String contactFirstName, String contactLastName) {
        // Get initial offLimitContacts count
        Response initialResponse = RestClient.doGet("JSON", contactServiceURL, "contacts/quick-view-count",
                albatrossTkn, null, null, true);
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        int initialOffLimitContacts = (Integer) initialResponse.jsonPath().get("data[0].offLimitContacts");

        // Get off-limit status ID
        int statusId = getOffLimitStatus();
        assertThat("Status ID should not be null", statusId, notNullValue());

        // Mark contact as off-limit
        markContactAsOffLimit(contactSlug, statusId);

        // Get count after marking as off-limit
        Response afterMarkResponse = RestClient.doGet("JSON", contactServiceURL, "contacts/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("After mark response should succeed", afterMarkResponse.getStatusCode(), equalTo(200));
        JsonPath afterMarkJp = afterMarkResponse.jsonPath();
        int afterMarkOffLimitContacts = (Integer) afterMarkJp.get("data[0].offLimitContacts");
        int allContacts = (Integer) afterMarkJp.get("data[0].allContacts");

        // Verify off-limit contacts count increased
        assertThat("Off-limit contacts count should increase after marking contact as off-limit", 
                afterMarkOffLimitContacts, equalTo(initialOffLimitContacts + 1));
    }

    @DataProvider(name = "contactCountData")
    public Object[][] getContactCountData() {
        // Create test contact using function
        String contactSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "contact");
        assertThat("Contact slug should not be null", contactSlug, notNullValue());
        
        // Get contact details to extract ID and name
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("contact", contactSlug);
        String basePath = "contacts/{contact}";
        
        Response contactResponse = RestClient.doGet("JSON", baseURL, basePath, apiAuthToken, null, pathParameters, true);
        assertThat("Contact details should be retrieved", contactResponse.getStatusCode(), equalTo(200));
        
        JsonPath jp = contactResponse.jsonPath();
        int contactId = jp.get("id");
        String contactFirstName = jp.get("first_name");
        String contactLastName = jp.get("last_name");
        
        assertThat("Contact ID should not be null", contactId, notNullValue());
        assertThat("Contact first name should not be null", contactFirstName, notNullValue());
        assertThat("Contact last name should not be null", contactLastName, notNullValue());
        
        return new Object[][] { { contactSlug, contactId, contactFirstName, contactLastName } };
    }

    private int getOffLimitStatus() {
        Response response = RestClient.doGet("JSON", baseURL, "off-limit-status", apiAuthToken, null, null, false);
        assertThat("Failed to get off-limit status", response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();
        assertThat("Off-limit status should not be null", jp.get("[0].id"), notNullValue());
        return jp.getInt("[0].id");
    }

    private void markContactAsOffLimit(String contactSlug, int statusId) {
        MarkContactOffLimit markContactOffLimit = new MarkContactOffLimit();
        markContactOffLimit.setContact_slugs(contactSlug);
        markContactOffLimit.setStatus_id(String.valueOf(statusId));
        markContactOffLimit.setEnd_date(DateUtil.getTomorrowDateString());
        markContactOffLimit.setReason("Test off-limit reason for quick view count test");

        Response response = RestClient.doPost1("JSON", baseURL, "contacts/mark-off-limit", apiAuthToken,
                null, null, false, markContactOffLimit);

        assertThat("Failed to mark contact as off-limit", response.getStatusCode(), equalTo(200));
    }
}
