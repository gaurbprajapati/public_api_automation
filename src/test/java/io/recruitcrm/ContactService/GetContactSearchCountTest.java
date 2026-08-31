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
public class GetContactSearchCountTest extends TestBase {

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
    public void testContactSearchCount_Success() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/contacts/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Entity count retrieved successfully"));
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("Context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Response code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data is an integer
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Data should be an integer", jp.get("data"), instanceOf(Integer.class));
        assertThat("Data should be non-negative", (Integer) jp.get("data"), greaterThanOrEqualTo(0));

        // Validate JSON schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/contact/contactSearchCount.json"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactSearchCount_WithoutAuth() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/contacts/search/count/get",
                null, null, true, requestBody);

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactSearchCount_InvalidAuth() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/contacts/search/count/get",
                albatrossTkn + "invalid-token-123", null, true, requestBody);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactSearchCount_AfterCreatingContact() {
        // Get initial count
        JSONObject requestBody = createDefaultSearchRequestBody();
        Response initialResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/contacts/search/count/get",
                albatrossTkn, null, true, requestBody);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        int initialCount = initialResponse.jsonPath().getInt("data");

        // Create a contact
        String contactSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "contact");
        assertThat("Contact slug should not be null", contactSlug, notNullValue());

        // Get count after creating contact
        Response afterCreateResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/contacts/search/count/get",
                albatrossTkn, null, true, requestBody);
        
        assertThat("After create response should succeed", afterCreateResponse.getStatusCode(), equalTo(200));
        int afterCreateCount = afterCreateResponse.jsonPath().getInt("data");

        // Verify count increased
        assertThat("Contact count should increase after creating contact", 
                afterCreateCount, equalTo(initialCount + 1));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "contactCountData", groups = {"contact_service", "nightly-build"})
    public void testContactSearchCount_WithOwnerFilter(String contactSlug, int contactId, String contactFirstName, String contactLastName) {
        // Get count with owner filter
        JSONObject requestBody = createOwnerFilterRequestBody();
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/contacts/search/count/get",
                albatrossTkn, null, true, requestBody);
        
        assertThat("Response should succeed", response.getStatusCode(), equalTo(200));
        int count = response.jsonPath().getInt("data");
        
        assertThat("Count should be non-negative", count, equalTo(1));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "contactCountData", groups = {"contact_service", "nightly-build"})
    public void testContactSearchCount_WithOffLimitFilter(String contactSlug, int contactId, String contactFirstName, String contactLastName) {
        // Get initial count with off-limit filter (inclusion)
        JSONObject requestBody = createOffLimitFilterRequestBody("inclusion");
        Response initialResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/contacts/search/count/get",
                albatrossTkn, null, true, requestBody);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        int initialCount = initialResponse.jsonPath().getInt("data");

        // Get off-limit status ID
        int statusId = getOffLimitStatus();
        assertThat("Status ID should not be null", statusId, notNullValue());

        // Mark contact as off-limit
        markContactAsOffLimit(contactSlug, statusId);

        // Get count after marking as off-limit
        Response afterMarkResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/contacts/search/count/get",
                albatrossTkn, null, true, requestBody);
        
        assertThat("After mark response should succeed", afterMarkResponse.getStatusCode(), equalTo(200));
        int afterMarkCount = afterMarkResponse.jsonPath().getInt("data");

        // Verify off-limit contacts count increased
        assertThat("Off-limit contacts count should increase after marking contact as off-limit", 
                afterMarkCount, equalTo(initialCount + 1));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "contactCountData", groups = {"contact_service", "nightly-build"})
    public void testContactSearchCount_WithHotlistFilter(String contactSlug, int contactId, String contactFirstName, String contactLastName) {
        // Get initial count with hotlist filter (not in any hotlist)
        JSONObject requestBody = createHotlistFilterRequestBody();
        Response initialResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/contacts/search/count/get",
                albatrossTkn, null, true, requestBody);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        int initialCount = initialResponse.jsonPath().getInt("data");

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

        // Wait for the operation to complete and verify count changes
        int afterAddCount = waitForCountChange(initialCount, requestBody, albatrossTkn, 5, 1000);

        // Verify count decreased (contact is no longer "not in any hotlist")
        assertThat("Count should decrease after adding contact to hotlist", 
                afterAddCount, equalTo(initialCount - 1));
    }

    @DataProvider(name = "contactCountData")
    public Object[][] getContactCountData() {
        // Create test contact using function
        String contactSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "contact");
        assertThat("Contact slug should not be null", contactSlug, notNullValue());

        Response usersResponse = commanFunction.getUsers(baseURL, apiAuthToken);
        assertThat("Users response should succeed", usersResponse.getStatusCode(), equalTo(200));
        JsonPath usersJp = usersResponse.jsonPath();
        int newOwnerId = usersJp.get("[0].id");
        assertThat("New owner ID should not be null", newOwnerId, notNullValue());

        commanFunction.transferContactOwnership(albatrossURL, albatrossTkn, contactSlug, newOwnerId);

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

    private JSONObject createDefaultSearchRequestBody() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "CONTACT");
        requestBody.put("offLimitBehavior", "bypass");
        requestBody.put("defaultFilterList", JSONObject.NULL);
        requestBody.put("filterSearchList", JSONObject.NULL);
        requestBody.put("booleanSearchList", JSONObject.NULL);
        requestBody.put("sortPriorityList", new JSONArray());
        return requestBody;
    }

    private JSONObject createOwnerFilterRequestBody() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "CONTACT");
        
        // Get current user ID
        Response usersResponse = commanFunction.getUsers(baseURL, apiAuthToken);
        assertThat("Users response should succeed", usersResponse.getStatusCode(), equalTo(200));
        JsonPath usersJp = usersResponse.jsonPath();
        int ownerId = usersJp.get("[0].id");
        
        // Create defaultFilterList with owner filter
        JSONObject defaultFilterList = new JSONObject();
        JSONObject defaultFilterListInner = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("groupType", "contacts");
        filter.put("searchField", "ownerid");
        filter.put("filterType", "is");
        filter.put("entityType", "contact");
        filter.put("fieldType", "dropdown");
        
        JSONObject filterValue = new JSONObject();
        JSONArray value = new JSONArray();
        JSONObject entityObj = new JSONObject();
        entityObj.put("entityTypeId", 6); // User entity type ID
        JSONArray entityIds = new JSONArray();
        entityIds.put(ownerId);
        entityObj.put("entityIds", entityIds);
        value.put(entityObj);
        filterValue.put("value", value);
        filterValue.put("type", "ENTITY_ASSOCIATION");
        filter.put("filterValue", filterValue);
        
        filters.put(filter);
        defaultFilterListInner.put("filters", filters);
        defaultFilterListInner.put("subGroupJoinOperator", "AND");
        defaultFilterList.put("defaultFilterList", defaultFilterListInner);
        
        requestBody.put("defaultFilterList", defaultFilterList);
        requestBody.put("filterSearchList", JSONObject.NULL);
        requestBody.put("booleanSearchList", JSONObject.NULL);
        requestBody.put("sortPriorityList", new JSONArray());
        return requestBody;
    }

    private JSONObject createOffLimitFilterRequestBody(String behavior) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "CONTACT");
        requestBody.put("offLimitBehavior", behavior);
        
        // Create defaultFilterList with off-limit filter
        JSONObject defaultFilterList = new JSONObject();
        JSONObject defaultFilterListInner = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("groupType", "contacts");
        filter.put("searchField", "off_limit_status_id");
        filter.put("filterType", "not_empty");
        filter.put("entityType", "contact");
        filter.put("fieldType", "multiselect");
        
        JSONObject filterValue = new JSONObject();
        filterValue.put("value", true);
        filterValue.put("type", "BOOLEAN");
        filter.put("filterValue", filterValue);
        
        filters.put(filter);
        defaultFilterListInner.put("filters", filters);
        defaultFilterListInner.put("subGroupJoinOperator", "AND");
        defaultFilterList.put("defaultFilterList", defaultFilterListInner);
        
        requestBody.put("defaultFilterList", defaultFilterList);
        requestBody.put("filterSearchList", JSONObject.NULL);
        requestBody.put("booleanSearchList", JSONObject.NULL);
        requestBody.put("sortPriorityList", new JSONArray());
        return requestBody;
    }

    private JSONObject createHotlistFilterRequestBody() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "CONTACT");
        requestBody.put("offLimitBehavior", "bypass");
        
        // Create defaultFilterList with hotlist filter (is_empty)
        JSONObject defaultFilterList = new JSONObject();
        JSONObject defaultFilterListInner = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("groupType", "contacts");
        filter.put("searchField", "hotlistid");
        filter.put("filterType", "is_empty");
        filter.put("entityType", "contact");
        filter.put("fieldType", "multiselect");
        
        JSONObject filterValue = new JSONObject();
        filterValue.put("value", -1);
        filterValue.put("type", "INTEGER");
        filter.put("filterValue", filterValue);
        
        filters.put(filter);
        defaultFilterListInner.put("filters", filters);
        defaultFilterListInner.put("subGroupJoinOperator", "AND");
        defaultFilterList.put("defaultFilterList", defaultFilterListInner);
        
        requestBody.put("defaultFilterList", defaultFilterList);
        requestBody.put("filterSearchList", JSONObject.NULL);
        requestBody.put("booleanSearchList", JSONObject.NULL);
        requestBody.put("sortPriorityList", new JSONArray());
        return requestBody;
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
        markContactOffLimit.setReason("Test off-limit reason for search count test");

        Response response = RestClient.doPost1("JSON", baseURL, "contacts/mark-off-limit", apiAuthToken,
                null, null, false, markContactOffLimit);

        assertThat("Failed to mark contact as off-limit", response.getStatusCode(), equalTo(200));
    }

    private int waitForCountChange(int initialCount, JSONObject requestBody, String token, int maxRetries, long delayMs) {
        int currentCount = initialCount;
        int retryCount = 0;
        
        // Initial delay to allow operation to complete
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Wait interrupted", e);
        }
        
        while (retryCount < maxRetries) {
            Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/contacts/search/count/get",
                    token, null, true, requestBody);
            
            assertThat("Response should succeed during retry", response.getStatusCode(), equalTo(200));
            currentCount = response.jsonPath().getInt("data");
            
            // If count changed, return immediately
            if (currentCount != initialCount) {
                return currentCount;
            }
            
            // Wait before next retry
            if (retryCount < maxRetries - 1) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Wait interrupted", e);
                }
            }
            
            retryCount++;
        }
        
        // Return the final count even if it didn't change (will fail assertion)
        return currentCount;
    }
}

