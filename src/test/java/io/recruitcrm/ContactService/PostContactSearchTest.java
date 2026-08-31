package io.recruitcrm.ContactService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.ContactSearchData;
import io.rcrm.api.pojo.albatross.UpdateFields;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class PostContactSearchTest extends TestBase {

    String apiAuthToken;
    String albatrossTkn;
    JavaFakerContact faker;
    JavaFakerCustomField customFieldFaker;
    commanFunction commanFunction;
    AllCrudFunctions allCrudFunctions;
    
    // Store custom field column IDs for sorting tests
    private static int textCustomFieldColumnId;
    private static int numberCustomFieldColumnId;
    private static int dateCustomFieldColumnId;
    private static int dateTimeCustomFieldColumnId;
    private static int longTextCustomFieldColumnId;
    private static int phoneNumberCustomFieldColumnId;
    private static int dropdownCustomFieldColumnId;
    private static int multiselectCustomFieldColumnId;
    private static int checkboxCustomFieldColumnId;
    private static int fileCustomFieldColumnId;
    private static int socialProfileCustomFieldColumnId;
    
    // Map column IDs to field names for better error messages
    private static Map<Integer, String> customFieldColumnIdToNameMap = new HashMap<>();
    
    // Store company mappings for sorting tests
    private Map<String, String> companyKeyToSlugMap = new HashMap<>();
    private Map<String, String> companyKeyToIdMap = new HashMap<>();

    // Synchronization object to ensure thread-safe initialization
    private static final Object INIT_LOCK = new Object();
    private static boolean customFieldsInitialized = false;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        faker = new JavaFakerContact();
        customFieldFaker = new JavaFakerCustomField();
        commanFunction = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "contactSearchData", groups = {"contact_service", "nightly-build"})
    public void testContactSearch_Success(String contactSlug, String contactId, String contactFirstName, String contactLastName) {
        // Step 1: Search for the created contact
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response searchResponse = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/contacts/search/get",
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + searchResponse.getStatusCode(), 
                searchResponse.getStatusCode(), equalTo(200));

        JsonPath searchJp = searchResponse.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", searchJp.get("meta"), notNullValue());
        assertThat("Message should match expected", searchJp.get("meta.message"), equalTo("Entities retrieved successfully"));
        assertThat("Meta status should be 200", (Integer) searchJp.get("meta.status"), equalTo(200));
        assertThat("Request UUID should not be null", searchJp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", searchJp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", searchJp.get("meta.responseType"), notNullValue());
        assertThat("Context should match", searchJp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Response code should be 103", (Integer) searchJp.get("meta.responseType.code"), equalTo(103));

        // Verify data array
        assertThat("Data should not be null", searchJp.get("data"), notNullValue());
        assertThat("Data array should not be empty", (Integer) searchJp.get("data.size()"), greaterThan(0));

        // Verify our created contact exists in the search results
        boolean contactFound = false;
        int dataSize = (Integer) searchJp.get("data.size()");
        for (int i = 0; i < dataSize; i++) {
            String dataPath = "data[" + i + "]";
            if (searchJp.get(dataPath + ".slug").equals(contactSlug)) {
                contactFound = true;
                
                // Validate contact data structure
                assertThat("Contact ID should match", (Integer) searchJp.get(dataPath + ".id"), equalTo(Integer.parseInt(contactId)));
                assertThat("First name should match", searchJp.get(dataPath + ".firstname"), equalTo(contactFirstName));
                assertThat("Last name should match", searchJp.get(dataPath + ".lastname"), equalTo(contactLastName));
                assertThat("Slug should match", searchJp.get(dataPath + ".slug"), equalTo(contactSlug));
                assertThat("Owner ID should not be null", searchJp.get(dataPath + ".ownerid"), notNullValue());
                assertThat("Account ID should not be null", searchJp.get(dataPath + ".accountid"), notNullValue());
                assertThat("Created by should not be null", searchJp.get(dataPath + ".createdby"), notNullValue());
                assertThat("Created on should not be null", searchJp.get(dataPath + ".createdon"), notNullValue());
                assertThat("Updated by should not be null", searchJp.get(dataPath + ".updatedby"), notNullValue());
                assertThat("Updated on should not be null", searchJp.get(dataPath + ".updatedon"), notNullValue());
                break;
            }
        }

        assertThat("Created contact should be found in search results", contactFound, is(true));

        // Validate JSON schema using existing schema
        searchResponse.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/contact/contactSearchGet.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "contactSearchDataForResponseDataValidation", groups = {"contact_service", "nightly-build"})
    public void testContactSearch_ComprehensiveDataValidation(ContactSearchData data) {

        // Step 1: Search for the contact with comprehensive data
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response searchResponse = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/contacts/search/get",
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + searchResponse.getStatusCode(),
                searchResponse.getStatusCode(), equalTo(200));

        JsonPath searchJp = searchResponse.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", searchJp.get("meta"), notNullValue());
        assertThat("Message should match expected", searchJp.get("meta.message"), equalTo("Entities retrieved successfully"));
        assertThat("Meta status should be 200", (Integer) searchJp.get("meta.status"), equalTo(200));

        // Find our contact in the search results
        boolean contactFound = false;
        int dataSize = (Integer) searchJp.get("data.size()");
        for (int i = 0; i < dataSize; i++) {
            String dataPath = "data[" + i + "]";
            if (searchJp.get(dataPath + ".slug").equals(data.contactSlug)) {
                contactFound = true;

                // Validate basic contact data
                assertThat("Contact ID should match", (Integer) searchJp.get(dataPath + ".id"), equalTo(Integer.parseInt(data.contactId)));
                assertThat("First name should match", searchJp.get(dataPath + ".firstname"), equalTo(data.contactFirstName));
                assertThat("Last name should match", searchJp.get(dataPath + ".lastname"), equalTo(data.contactLastName));
                if (data.name != null) {
                    assertThat("Name should match", searchJp.get(dataPath + ".name"), equalTo(data.name));
                }
                assertThat("Slug should match", searchJp.get(dataPath + ".slug"), equalTo(data.contactSlug));

                // Validate contact details
                if (data.contactEmail != null) {
                    assertThat("Email should match", searchJp.get(dataPath + ".email"), equalTo(data.contactEmail));
                }
                if (data.contactNumber != null) {
                    assertThat("Contact number should match", searchJp.get(dataPath + ".contactnumber"), equalTo(data.contactNumber));
                }
                if (data.city != null) {
                    assertThat("City should match", searchJp.get(dataPath + ".city"), equalTo(data.city));
                }
                if (data.address != null) {
                    assertThat("Address should match", searchJp.get(dataPath + ".address"), equalTo(data.address));
                }
                if (data.locality != null) {
                    assertThat("Locality should match", searchJp.get(dataPath + ".locality"), equalTo(data.locality));
                }
                if (data.state != null) {
                    assertThat("State should match", searchJp.get(dataPath + ".state"), equalTo(data.state));
                }
                if (data.country != null) {
                    assertThat("Country should match", searchJp.get(dataPath + ".country"), equalTo(data.country));
                }
                if (data.postalCode != null) {
                    assertThat("Postal code should match", searchJp.get(dataPath + ".postal_code"), equalTo(data.postalCode));
                }
                if (data.designation != null) {
                    assertThat("Designation should match", searchJp.get(dataPath + ".designation"), equalTo(data.designation));
                }
                if (data.linkedin != null) {
                    assertThat("LinkedIn should match", searchJp.get(dataPath + ".profilelinkedin"), equalTo(data.linkedin));
                }
                if (data.facebook != null) {
                    assertThat("Facebook should match", searchJp.get(dataPath + ".profilefacebook"), equalTo(data.facebook));
                }
                if (data.twitter != null) {
                    assertThat("Twitter should match", searchJp.get(dataPath + ".profiletwitter"), equalTo(data.twitter));
                }
                if (data.xing != null) {
                    assertThat("Xing should match", searchJp.get(dataPath + ".profilexing"), equalTo(data.xing));
                }
                if (data.avatar != null) {
                    assertThat("Photo should match", searchJp.get(dataPath + ".photo"), equalTo(data.avatar));
                }
                assertThat("Company slug should match", searchJp.get(dataPath + ".companyslug"), equalTo(data.companySlug));
                if (data.companyId > 0) {
                    Integer companyIdFromResponse = searchJp.get(dataPath + ".companyid");
                    if (companyIdFromResponse != null) {
                        assertThat("Company ID should match", companyIdFromResponse, equalTo(data.companyId));
                    }
                }
                if (data.companyName != null) {
                    assertThat("Company name should match", searchJp.get(dataPath + ".companyname"), equalTo(data.companyName));
                }
                if (data.stageId > 0) {
                    Integer stageIdFromResponse = searchJp.get(dataPath + ".stageid");
                    if (stageIdFromResponse != null) {
                        assertThat("Stage ID should match", stageIdFromResponse, equalTo(data.stageId));
                    }
                }

                // Validate system fields with equality checks
                Integer accountIdFromResponse = searchJp.get(dataPath + ".accountid");
                if (accountIdFromResponse != null) {
                    assertThat("Account ID should match", accountIdFromResponse, equalTo(data.accountId));
                }
                Integer createdByFromResponse = searchJp.get(dataPath + ".createdby");
                if (createdByFromResponse != null) {
                    assertThat("Created by should match", createdByFromResponse, equalTo(data.createdBy));
                }
                Integer createdOnFromResponse = searchJp.get(dataPath + ".createdon");
                if (createdOnFromResponse != null && data.createdOn > 0) {
                    assertThat("Created on should match", createdOnFromResponse, equalTo(data.createdOn));
                }
                Integer updatedByFromResponse = searchJp.get(dataPath + ".updatedby");
                if (updatedByFromResponse != null) {
                    assertThat("Updated by should match", updatedByFromResponse, equalTo(data.updatedBy));
                }
                Integer updatedOnFromResponse = searchJp.get(dataPath + ".updatedon");
                if (updatedOnFromResponse != null && data.updatedOn > 0) {
                    assertThat("Updated on should match", updatedOnFromResponse, equalTo(data.updatedOn));
                }
                Boolean deletedFromResponse = searchJp.get(dataPath + ".deleted");
                if (deletedFromResponse != null) {
                    assertThat("Deleted flag should match", deletedFromResponse, equalTo(data.deleted));
                }
                
                // Validate company-related fields
                if (data.companyId > 0) {
                    assertThat("Company ID should match", (Integer) searchJp.get(dataPath + ".companyid"), equalTo(data.companyId));
                }
                if (data.companyName != null) {
                    assertThat("Company name should match", searchJp.get(dataPath + ".companyname"), equalTo(data.companyName));
                }
                if (searchJp.get(dataPath + ".companydeleted") != null) {
                    assertThat("Company deleted flag should match", (Boolean) searchJp.get(dataPath + ".companydeleted"), equalTo(data.companyDeleted));
                }

                break;
            }
        }

        assertThat("Created contact should be found in search results", contactFound, is(true));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "contactSearchData", groups = {"contact_service", "nightly-build"})
    public void testContactSearch_Workflow(String contactSlug, String contactId, String contactFirstName, String contactLastName) {
        // Step 1: Search to confirm contact exists
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response searchResponse = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/contacts/search/get",
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        assertThat("Search should succeed", searchResponse.getStatusCode(), equalTo(200));
        
        JsonPath searchJp = searchResponse.jsonPath();
        int initialCount = (Integer) searchJp.get("data.size()");
        assertThat("Should have contacts before delete", initialCount, greaterThan(0));

        // Step 2: Delete the contact
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("contact", contactSlug);
        String basePath = "contacts/{contact}";

        Response deleteResponse = RestClient.doDelete("JSON", albatrossURL, basePath, albatrossTkn, null, pathParameters, true);
        assertThat("Delete should succeed", deleteResponse.getStatusCode(), equalTo(200));

        // Step 3: Search again to verify deletion
        Response searchAfterDeleteResponse = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/contacts/search/get",
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        assertThat("Search after delete should succeed", searchAfterDeleteResponse.getStatusCode(), equalTo(200));

        JsonPath searchAfterDeleteJp = searchAfterDeleteResponse.jsonPath();

        // Should have fewer contacts or the deleted contact should not be found
        boolean contactStillExists = false;
        int dataSize = (Integer) searchAfterDeleteJp.get("data.size()");
        for (int i = 0; i < dataSize; i++) {
            String dataPath = "data[" + i + "]";
            if (searchAfterDeleteJp.get(dataPath + ".slug").equals(contactSlug)) {
                contactStillExists = true;
                break;
            }
        }

        assertThat("Deleted contact should not exist in search results", contactStillExists, is(false));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactSearch_WithoutAuth() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response response = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/contacts/search/get",
                null, queryParameters, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactSearch_InvalidAuth() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response response = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/contacts/search/get",
                albatrossTkn + "invalid-token-123", queryParameters, null, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactSearch_EmptyRequestBody() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "CONTACT");
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response response = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/contacts/search/get",
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(200));

        // Should still return companies even with empty request body
        JsonPath jp = response.jsonPath();
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactSearch_InvalidQueryParameters() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "invalid-page");
        queryParameters.put("size", "invalid-size");

        Response response = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/contacts/search/get",
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        // Should handle invalid parameters gracefully or return error
        assertThat("Response should be handled appropriately", 
                response.getStatusCode(), anyOf(equalTo(200), equalTo(400), equalTo(422)));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void testContactSearch_MissingQueryParameters() {
        JSONObject requestBody = createDefaultSearchRequestBody();

        Response response = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/contacts/search/get",
                albatrossTkn, null, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(200));

        JsonPath searchJp = response.jsonPath();

        assertThat("Meta object should not be null", searchJp.get("meta"), notNullValue());
        assertThat("Message should match expected", searchJp.get("meta.message"), equalTo("Entities retrieved successfully"));

    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getTextSortData", groups = {"sorting", "contact_service", "nightly-build"})
    public void searchContact_SortByTextField(String sortField, String jsonPath, String sortOrder, int statusCode) {
        Map<String, String> queryParameters = createDefaultQueryParameters();

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = new JSONObject();
        searchPayload.put("advancedSearchContext", "CONTACT");
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("defaultFilterList", JSONObject.NULL);
        searchPayload.put("filterSearchList", JSONObject.NULL);
        searchPayload.put("booleanSearchList", JSONObject.NULL);

        Response response = RestClient.doPost("JSON", ariesServiceURL,
                "advanced-search/contacts/search/get", albatrossTkn, queryParameters, true, searchPayload);

        String testCase = "searchContact_SortByTextField - Field: " + sortField + ", Order: " + sortOrder;
        assertThat(testCase + " - Expected status code " + statusCode, 
                response.getStatusCode(), equalTo(statusCode));

        List<String> values = response.jsonPath().getList(jsonPath);

        if (sortOrder.equals("asc")) {
            assertThat(testCase + " - " + sortField + " not sorted ascending",
                    commanFunction.isSortedAscendingText(values), is(true));
        } else {
            assertThat(testCase + " - " + sortField + " not sorted descending",
                    commanFunction.isSortedDescendingText(values), is(true));
        }
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getNumericSortData", groups = {"sorting", "contact_service", "nightly-build"})
    public void searchContact_SortByNumericField(String sortField, String jsonPath, String sortOrder, int statusCode) {
        Map<String, String> queryParameters = createDefaultQueryParameters();

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = new JSONObject();
        searchPayload.put("advancedSearchContext", "CONTACT");
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("defaultFilterList", JSONObject.NULL);
        searchPayload.put("filterSearchList", JSONObject.NULL);
        searchPayload.put("booleanSearchList", JSONObject.NULL);

        Response response = RestClient.doPost("JSON", ariesServiceURL,
                "advanced-search/contacts/search/get", albatrossTkn, queryParameters, true, searchPayload);

        String testCase = "searchContact_SortByNumericField - Field: " + sortField + ", Order: " + sortOrder;
        assertThat(testCase + " - Expected status code " + statusCode,
                response.getStatusCode(), equalTo(statusCode));

        List<Number> values = response.jsonPath().getList(jsonPath);

        if (sortOrder.equals("asc")) {
            assertThat(testCase + " - " + sortField + " not sorted ascending",
                    commanFunction.isSortedAscendingNumeric(values), is(true));
        } else {
            assertThat(testCase + " - " + sortField + " not sorted descending",
                    commanFunction.isSortedDescendingNumeric(values), is(true));
        }
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getDateSortData", groups = {"sorting", "contact_service", "nightly-build"})
    public void searchContact_SortByDateField(String sortField, String jsonPath, String sortOrder, int statusCode) {
        Map<String, String> queryParameters = createDefaultQueryParameters();

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = new JSONObject();
        searchPayload.put("advancedSearchContext", "CONTACT");
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("defaultFilterList", JSONObject.NULL);
        searchPayload.put("filterSearchList", JSONObject.NULL);
        searchPayload.put("booleanSearchList", JSONObject.NULL);

        Response response = RestClient.doPost("JSON", ariesServiceURL,
                "advanced-search/contacts/search/get", albatrossTkn, queryParameters, true, searchPayload);

        String testCase = "searchContact_SortByDateField - Field: " + sortField + ", Order: " + sortOrder;
        assertThat(testCase + " - Expected status code " + statusCode,
                response.getStatusCode(), equalTo(statusCode));

        List<Object> values = response.jsonPath().getList(jsonPath);

        if (sortOrder.equals("asc")) {
            assertThat(testCase + " - " + sortField + " not sorted ascending",
                    commanFunction.isSortedAscendingDate(values), is(true));
        } else {
            assertThat(testCase + " - " + sortField + " not sorted descending",
                    commanFunction.isSortedDescendingDate(values), is(true));
        }
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getCustomFieldSortData", groups = {"sorting", "contact_service", "nightly-build"})
    public void searchContact_SortByCustomField(String sortField, String sortOrder, int statusCode, String fieldType) {
        Map<String, String> queryParameters = createDefaultQueryParameters();

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = new JSONObject();
        searchPayload.put("advancedSearchContext", "CONTACT");
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("defaultFilterList", JSONObject.NULL);
        searchPayload.put("filterSearchList", JSONObject.NULL);
        searchPayload.put("booleanSearchList", JSONObject.NULL);

        Response response = RestClient.doPost("JSON", ariesServiceURL,
                "advanced-search/contacts/search/get", albatrossTkn, queryParameters, true, searchPayload);

        // Extract column ID from sortField (e.g., "custcolumn123" -> 123)
        int columnId = Integer.parseInt(sortField.replace("custcolumn", ""));
        String fieldName = customFieldColumnIdToNameMap.getOrDefault(columnId, sortField);
        
        String testCase = "searchContact_SortByCustomField - Field: " + fieldName + " (" + sortField + "), Type: " + fieldType + ", Order: " + sortOrder;
        assertThat(testCase + " - Expected status code " + statusCode,
                response.getStatusCode(), equalTo(statusCode));

        // Validate sorting based on field type
        if ("text".equals(fieldType)) {
            // Text custom fields
            List<String> values = commanFunction.extractCustomFieldValues(response, sortField);
            if (sortOrder.equals("asc")) {
                assertThat(testCase + " - " + fieldName + " (" + sortField + ") not sorted ascending",
                        commanFunction.isSortedAscendingText(values), is(true));
            } else {
                assertThat(testCase + " - " + fieldName + " (" + sortField + ") not sorted descending",
                        commanFunction.isSortedDescendingText(values), is(true));
            }
        } else if ("numeric".equals(fieldType)) {
            // Numeric custom fields
            List<Number> values = commanFunction.extractCustomFieldNumericValues(response, sortField);
            if (sortOrder.equals("asc")) {
                assertThat(testCase + " - " + fieldName + " (" + sortField + ") not sorted ascending",
                        commanFunction.isSortedAscendingNumeric(values), is(true));
            } else {
                assertThat(testCase + " - " + fieldName + " (" + sortField + ") not sorted descending",
                        commanFunction.isSortedDescendingNumeric(values), is(true));
            }
        } else if ("date".equals(fieldType)) {
            // Date custom fields
            List<Object> values = commanFunction.extractCustomFieldDateValues(response, sortField);
            if (sortOrder.equals("asc")) {
                assertThat(testCase + " - " + fieldName + " (" + sortField + ") not sorted ascending",
                        commanFunction.isSortedAscendingDate(values), is(true));
            } else {
                assertThat(testCase + " - " + fieldName + " (" + sortField + ") not sorted descending",
                        commanFunction.isSortedDescendingDate(values), is(true));
            }
        }
    }
    
    private void ensureCustomFieldsInitialized() {
        if (!customFieldsInitialized) {
            synchronized (INIT_LOCK) {
                if (!customFieldsInitialized) {
                    try {
                        // Step 1: Create companies first
                        createCompanies();
                        
                        // Step 2: Create custom fields for contacts of each type
                        createCustomFields();
                        
                        // Step 3: Create contacts from JSON and update custom field values
                        createContactsWithCustomFields();
                        
                        customFieldsInitialized = true;
                    } catch (Exception e) {
                        throw new AssertionError("Failed to initialize test data for sorting: " + e.getMessage(), e);
                    }
                }
            }
        }
    }
    
    @BeforeGroups(groups = "sorting", alwaysRun = true)
    public void createTestDataForSorting() {
        // Ensure initialization happens (this will be a no-op if already initialized by data provider)
        ensureCustomFieldsInitialized();
    }
    
    private void createCompanies() {
        // Create 4 companies
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<CompletableFuture<Void>> createFutures = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                final int companyIndex = i;
                createFutures.add(CompletableFuture.runAsync(() -> {
                    String companyKey = "company" + companyIndex;
                    Response response = commanFunction.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
                    assertThat("Company creation failed for " + companyKey,
                            response.getStatusCode(), equalTo(200));
                    JsonPath jsonPath = response.jsonPath();
                    String slug = jsonPath.getString("slug");

                    Response companyResponse = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTkn, slug);
                    JsonPath jp = companyResponse.jsonPath();
                    String companyIdStr = jp.getString("data.company.id");
                    
                    if (companyIdStr == null) {
                        throw new AssertionError("Company ID is null for " + companyKey);
                    }
                    
                    synchronized (companyKeyToSlugMap) {
                        companyKeyToSlugMap.put(companyKey, slug);
                    }
                    synchronized (companyKeyToIdMap) {
                        companyKeyToIdMap.put(companyKey, companyIdStr);
                    }
                }, executor));
            }
            
            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }
    
    private void createCustomFields() {
        // Create all custom field types for contacts
        ExecutorService executor = Executors.newFixedThreadPool(11);
        try {
            // Define custom field configurations
            List<Map<String, String>> fieldConfigs = Arrays.asList(
                Map.of("type", "text", "name", "CF_Text_Sort", "varName", "text"),
                Map.of("type", "number", "name", "CF_Number_Sort", "varName", "number"),
                Map.of("type", "date", "name", "CF_Date_Sort", "varName", "date"),
                Map.of("type", "date_time", "name", "CF_DateTime_Sort", "varName", "dateTime"),
                Map.of("type", "longtext", "name", "CF_LongText_Sort", "varName", "longText"),
                Map.of("type", "phonenumber", "name", "CF_Phone_Sort", "varName", "phoneNumber"),
                Map.of("type", "dropdown", "name", "CF_Dropdown_Sort", "varName", "dropdown", "options", "Option A, Option B, Option C"),
                Map.of("type", "multiselect", "name", "CF_Multiselect_Sort", "varName", "multiselect", "options", "Option A, Option B, Option C"),
                Map.of("type", "checkbox", "name", "CF_Checkbox_Sort", "varName", "checkbox"),
                Map.of("type", "file", "name", "CF_File_Sort", "varName", "file"),
                Map.of("type", "social_profile", "name", "CF_SocialProfile_Sort", "varName", "socialProfile")
            );
            
            // Create custom fields in parallel
            Map<String, CompletableFuture<Integer>> futures = new HashMap<>();
            for (Map<String, String> config : fieldConfigs) {
                String fieldType = config.get("type");
                String fieldName = config.get("name");
                String varName = config.get("varName");
                
                CompletableFuture<Integer> future;
                if (config.containsKey("options")) {
                    String options = config.get("options");
                    future = CompletableFuture.supplyAsync(() -> 
                        commanFunction.createCustomFieldWithOptionsAndGetColumnId(
                            albatrossURL, albatrossTkn, fieldType, fieldName, options, "contact"), executor);
                } else {
                    future = CompletableFuture.supplyAsync(() -> 
                        commanFunction.createCustomFieldAndGetColumnId(
                            albatrossURL, albatrossTkn, fieldType, fieldName, "contact"), executor);
                }
                futures.put(varName, future);
            }
            
            // Wait for all custom fields to be created and assign to static variables
            textCustomFieldColumnId = futures.get("text").join();
            numberCustomFieldColumnId = futures.get("number").join();
            dateCustomFieldColumnId = futures.get("date").join();
            dateTimeCustomFieldColumnId = futures.get("dateTime").join();
            longTextCustomFieldColumnId = futures.get("longText").join();
            phoneNumberCustomFieldColumnId = futures.get("phoneNumber").join();
            dropdownCustomFieldColumnId = futures.get("dropdown").join();
            multiselectCustomFieldColumnId = futures.get("multiselect").join();
            checkboxCustomFieldColumnId = futures.get("checkbox").join();
            fileCustomFieldColumnId = futures.get("file").join();
            socialProfileCustomFieldColumnId = futures.get("socialProfile").join();
            
            // Create mapping from column ID to field name for better error messages
            customFieldColumnIdToNameMap.put(textCustomFieldColumnId, "CF_Text_Sort");
            customFieldColumnIdToNameMap.put(numberCustomFieldColumnId, "CF_Number_Sort");
            customFieldColumnIdToNameMap.put(dateCustomFieldColumnId, "CF_Date_Sort");
            customFieldColumnIdToNameMap.put(dateTimeCustomFieldColumnId, "CF_DateTime_Sort");
            customFieldColumnIdToNameMap.put(longTextCustomFieldColumnId, "CF_LongText_Sort");
            customFieldColumnIdToNameMap.put(phoneNumberCustomFieldColumnId, "CF_Phone_Sort");
            customFieldColumnIdToNameMap.put(dropdownCustomFieldColumnId, "CF_Dropdown_Sort");
            customFieldColumnIdToNameMap.put(multiselectCustomFieldColumnId, "CF_Multiselect_Sort");
            customFieldColumnIdToNameMap.put(checkboxCustomFieldColumnId, "CF_Checkbox_Sort");
            customFieldColumnIdToNameMap.put(fileCustomFieldColumnId, "CF_File_Sort");
            customFieldColumnIdToNameMap.put(socialProfileCustomFieldColumnId, "CF_SocialProfile_Sort");
        } finally {
            executor.shutdown();
        }
    }
    
    private void createContactsWithCustomFields() {
        // Read JSON file
        String filePath = "src/test/resources/contactCreationDataProvider.json";
        JSONObject contactJson = readJsonFileFromPath(filePath);
        
        // Map custom field types to column IDs
        Map<String, Integer> customFieldMap = new HashMap<>();
        customFieldMap.put("text", textCustomFieldColumnId);
        customFieldMap.put("number", numberCustomFieldColumnId);
        customFieldMap.put("date", dateCustomFieldColumnId);
        customFieldMap.put("date_time", dateTimeCustomFieldColumnId);
        customFieldMap.put("longtext", longTextCustomFieldColumnId);
        customFieldMap.put("phonenumber", phoneNumberCustomFieldColumnId);
        customFieldMap.put("dropdown", dropdownCustomFieldColumnId);
        customFieldMap.put("multiselect", multiselectCustomFieldColumnId);
        customFieldMap.put("checkbox", checkboxCustomFieldColumnId);
        customFieldMap.put("file", fileCustomFieldColumnId);
        customFieldMap.put("social_profile", socialProfileCustomFieldColumnId);
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            // Create contacts and update custom fields in parallel
            CompletableFuture.allOf(contactJson.keySet().stream()
                    .filter(key -> key.startsWith("contact"))
                    .map(contactKey -> CompletableFuture.runAsync(() -> {
                        try {
                            JSONObject contactEntry = contactJson.getJSONObject(contactKey);
                            JSONObject originalPayload = contactEntry.getJSONObject("payload");
                            
                            // Extract customFields from payload (to be updated separately)
                            JSONObject customFields = originalPayload.optJSONObject("customFields");
                            
                            // Process company placeholders in selectedcompanies
                            JSONArray selectedCompanies = originalPayload.optJSONArray("selectedcompanies");
                            if (selectedCompanies != null) {
                                for (int i = 0; i < selectedCompanies.length(); i++) {
                                    JSONObject companyInfo = selectedCompanies.getJSONObject(i);
                                    String slugPlaceholder = companyInfo.optString("slug", "");
                                    String idPlaceholder = companyInfo.optString("id", "");
                                    
                                    String companyKey = null;
                                    if (slugPlaceholder.startsWith("{") && slugPlaceholder.endsWith("_slug}")) {
                                        companyKey = slugPlaceholder.substring(1, slugPlaceholder.length() - 6);
                                    } else if (idPlaceholder.startsWith("{") && idPlaceholder.endsWith("_id}")) {
                                        companyKey = idPlaceholder.substring(1, idPlaceholder.length() - 4);
                                    }
                                    
                                    if (companyKey != null) {
                                        String actualSlug = companyKeyToSlugMap.get(companyKey);
                                        String actualId = companyKeyToIdMap.get(companyKey);
                                        
                                        if (actualSlug != null && actualId != null) {
                                            companyInfo.put("slug", actualSlug);
                                            companyInfo.put("id", actualId);
                                        }
                                    }
                                }
                            }
                            
                            // Rebuild payload in correct order: address_changed, contact, selectedcompanies, filesInfo
                            JSONObject payload = new JSONObject();
                            payload.put("address_changed", originalPayload.optBoolean("address_changed", false));
                            payload.put("contact", originalPayload.getJSONObject("contact"));
                            if (selectedCompanies != null) {
                                payload.put("selectedcompanies", selectedCompanies);
                            } else {
                                payload.put("selectedcompanies", new JSONArray());
                            }
                            payload.put("filesInfo", originalPayload.optJSONObject("filesInfo", new JSONObject()));
                            
                            // Create contact
                            Response contactResponse = RestClient.doPost("JSON", albatrossURL, "contacts",
                                    albatrossTkn, null, true, payload);
                            assertThat("Contact creation failed for " + contactKey + ". Status: " + contactResponse.getStatusCode() + 
                                    ", Response: " + contactResponse.getBody().asString(),
                                    contactResponse.getStatusCode(), equalTo(200));
                            
                            String contactSlug = contactResponse.jsonPath().getString("data.contact.slug");
                            assertThat("Contact slug should not be null for " + contactKey, contactSlug, notNullValue());
                            
                            // Get contact ID
                            Response getContactResponse = allCrudFunctions.getContactResponse(albatrossURL, albatrossTkn, contactSlug);
                            assertThat("Failed to get contact for " + contactKey, getContactResponse.getStatusCode(), equalTo(200));
                            
                            JsonPath contactJp = getContactResponse.jsonPath();
                            String contactIdObj = contactJp.get("data.contact.id");
                            assertThat("Contact ID should not be null for " + contactKey, contactIdObj, notNullValue());
                            int contactId = Integer.parseInt(contactIdObj);
                            
                            // Update custom field values
                            if (customFields != null && !customFields.isEmpty()) {
                                updateContactCustomFields(contactId, customFields, customFieldMap, albatrossTkn);
                            }
                        } catch (Exception e) {
                            throw new AssertionError("Failed to create contact " + contactKey + ": " + e.getMessage(), e);
                        }
                    }, executor))
                    .toArray(CompletableFuture[]::new)).join();
        } finally {
            executor.shutdown();
        }
    }
    
    private void updateContactCustomFields(int contactId, JSONObject customFields, Map<String, Integer> customFieldMap, String authToken) {
        ExecutorService executor = Executors.newFixedThreadPool(11);
        try {
            List<CompletableFuture<Void>> updateFutures = new ArrayList<>();
            
            // Update all custom fields found in customFields object
            for (String fieldType : customFields.keySet()) {
                if (customFieldMap.containsKey(fieldType)) {
                    Integer columnId = customFieldMap.get(fieldType);
                    String fieldKey = "custcolumn" + columnId;
                    String fieldValue = customFields.optString(fieldType, "");
                    
                    if (!fieldValue.isEmpty()) {
                        updateFutures.add(CompletableFuture.runAsync(() -> 
                            updateContactCustomField(contactId, fieldKey, fieldValue, authToken), executor));
                    }
                }
            }
            
            CompletableFuture.allOf(updateFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }
    
    private void updateContactCustomField(int contactId, String fieldKey, String fieldValue, String authToken) {
        List<Integer> entityIds = Arrays.asList(contactId);
        UpdateFields updateFields = new UpdateFields();
        updateFields.setKey(fieldKey);
        updateFields.setValue(fieldValue);
        updateFields.setTableFlag("contact");
        updateFields.setId(entityIds);
        
        Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", authToken, null, true, updateFields);
        assertThat("Failed to update custom field " + fieldKey + " for contact " + contactId,
                response.getStatusCode(), equalTo(200));
    }

    private JSONObject createDefaultSearchRequestBody() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "CONTACT");
        requestBody.put("defaultFilterList", JSONObject.NULL);
        requestBody.put("filterSearchList", JSONObject.NULL);
        requestBody.put("booleanSearchList", JSONObject.NULL);
        requestBody.put("sortPriorityList", new JSONArray());
        return requestBody;
    }

    private Map<String, String> createDefaultQueryParameters() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");
        return queryParameters;
    }

    // ==================== DATA PROVIDERS ====================

    @DataProvider(name = "getTextSortData", parallel = true)
    public static Object[][] getTextSortData() {

        return new Object[][]{
                // Text fields - ascending
                {"name", "data.name", "asc", 200},
                {"firstname", "data.firstname", "asc", 200},
                {"lastname", "data.lastname", "asc", 200},
                {"email", "data.email", "asc", 200},
                {"contactnumber", "data.contactnumber", "asc", 200},
                {"address", "data.address", "asc", 200},
                {"city", "data.city", "asc", 200},
                {"locality", "data.locality", "asc", 200},
                {"state", "data.state", "asc", 200},
                {"country", "data.country", "asc", 200},
                {"postal_code", "data.postal_code", "asc", 200},
                {"designation", "data.designation", "asc", 200},
                {"companyname", "data.companyname", "asc", 200},
                {"ownername", "data.ownername", "asc", 200},
                {"profilefacebook", "data.profilefacebook", "asc", 200},
                {"profiletwitter", "data.profiletwitter", "asc", 200},
                {"profilelinkedin", "data.profilelinkedin", "asc", 200},
                {"profilexing", "data.profilexing", "asc", 200},
                {"slug", "data.slug", "asc", 200},
                {"stage", "data.stage", "asc", 200},
                {"last_communication_method", "data.last_communication_method", "asc", 200},

                // Text fields - descending
                {"name", "data.name", "desc", 200},
                {"firstname", "data.firstname", "desc", 200},
                {"lastname", "data.lastname", "desc", 200},
                {"email", "data.email", "desc", 200},
                {"contactnumber", "data.contactnumber", "desc", 200},
                {"address", "data.address", "desc", 200},
                {"city", "data.city", "desc", 200},
                {"locality", "data.locality", "desc", 200},
                {"state", "data.state", "desc", 200},
                {"country", "data.country", "desc", 200},
                {"postal_code", "data.postal_code", "desc", 200},
                {"designation", "data.designation", "desc", 200},
                {"companyname", "data.companyname", "desc", 200},
                {"ownername", "data.ownername", "desc", 200},
                {"profilefacebook", "data.profilefacebook", "desc", 200},
                {"profiletwitter", "data.profiletwitter", "desc", 200},
                {"profilelinkedin", "data.profilelinkedin", "desc", 200},
                {"profilexing", "data.profilexing", "desc", 200},
                {"slug", "data.slug", "desc", 200},
                {"stage", "data.stage", "desc", 200},
                {"last_communication_method", "data.last_communication_method", "desc", 200},
        };
    }

    @DataProvider(name = "getNumericSortData", parallel = true)
    public static Object[][] getNumericSortData() {
        return new Object[][]{
                // Numeric fields - ascending
                {"id", "data.id", "asc", 200},
                {"srno", "data.srno", "asc", 200},
                {"stageid", "data.stageid", "asc", 200},
                {"companyid", "data.companyid", "asc", 200},
                {"ownerid", "data.ownerid", "asc", 200},
                {"email_opt_out", "data.email_opt_out", "asc", 200},
                {"sms_opt_out", "data.sms_opt_out", "asc", 200},
                {"createdby", "data.createdby", "asc", 200},

                // Numeric fields - descending
                {"id", "data.id", "desc", 200},
                {"srno", "data.srno", "desc", 200},
                {"stageid", "data.stageid", "desc", 200},
                {"companyid", "data.companyid", "desc", 200},
                {"ownerid", "data.ownerid", "desc", 200},
                {"email_opt_out", "data.email_opt_out", "desc", 200},
                {"sms_opt_out", "data.sms_opt_out", "desc", 200},
                {"createdby", "data.createdby", "desc", 200},
        };
    }

    @DataProvider(name = "getDateSortData", parallel = true)
    public static Object[][] getDateSortData() {
        return new Object[][]{
                // Date fields - ascending
                {"createdon", "data.createdon", "asc", 200},
                {"updatedon", "data.updatedon", "asc", 200},
                {"last_calllog_created_on", "data.last_calllog_created_on", "asc", 200},
                {"last_sms_sent_on", "data.last_sms_sent_on", "asc", 200},
                {"last_email_sent_on", "data.last_email_sent_on", "asc", 200},
                {"last_communication_timestamp", "data.last_communication_timestamp", "asc", 200},
                {"last_meeting_created_on", "data.last_meeting_created_on", "asc", 200},

                // Date fields - descending
                {"createdon", "data.createdon", "desc", 200},
                {"updatedon", "data.updatedon", "desc", 200},
                {"last_calllog_created_on", "data.last_calllog_created_on", "desc", 200},
                {"last_sms_sent_on", "data.last_sms_sent_on", "desc", 200},
                {"last_email_sent_on", "data.last_email_sent_on", "desc", 200},
                {"last_communication_timestamp", "data.last_communication_timestamp", "desc", 200},
                {"last_meeting_created_on", "data.last_meeting_created_on", "desc", 200}
        };
    }

    @DataProvider(name = "getCustomFieldSortData")
    public Object[][] getCustomFieldSortData() {
        // Ensure custom fields are initialized (lazy initialization for data provider)
        ensureCustomFieldsInitialized();
        
        return new Object[][]{
                // Text custom fields - ascending
                {"custcolumn" + textCustomFieldColumnId, "asc", 200, "text"},
                {"custcolumn" + longTextCustomFieldColumnId, "asc", 200, "text"},
                {"custcolumn" + phoneNumberCustomFieldColumnId, "asc", 200, "text"},
                {"custcolumn" + dropdownCustomFieldColumnId, "asc", 200, "text"},
                {"custcolumn" + multiselectCustomFieldColumnId, "asc", 200, "text"},
                {"custcolumn" + fileCustomFieldColumnId, "asc", 200, "text"},
                {"custcolumn" + socialProfileCustomFieldColumnId, "asc", 200, "text"},

                // Text custom fields - descending
                {"custcolumn" + textCustomFieldColumnId, "desc", 200, "text"},
                {"custcolumn" + longTextCustomFieldColumnId, "desc", 200, "text"},
                {"custcolumn" + phoneNumberCustomFieldColumnId, "desc", 200, "text"},
                {"custcolumn" + dropdownCustomFieldColumnId, "desc", 200, "text"},
                {"custcolumn" + multiselectCustomFieldColumnId, "desc", 200, "text"},
                {"custcolumn" + fileCustomFieldColumnId, "desc", 200, "text"},
                {"custcolumn" + socialProfileCustomFieldColumnId, "desc", 200, "text"},

                // Numeric custom fields - ascending
                {"custcolumn" + numberCustomFieldColumnId, "asc", 200, "numeric"},
                {"custcolumn" + checkboxCustomFieldColumnId, "asc", 200, "numeric"},

                // Numeric custom fields - descending
                {"custcolumn" + numberCustomFieldColumnId, "desc", 200, "numeric"},
                {"custcolumn" + checkboxCustomFieldColumnId, "desc", 200, "numeric"},

                // Date custom fields - ascending
                {"custcolumn" + dateCustomFieldColumnId, "asc", 200, "date"},
                {"custcolumn" + dateTimeCustomFieldColumnId, "asc", 200, "date"},

                // Date custom fields - descending
                {"custcolumn" + dateCustomFieldColumnId, "desc", 200, "date"},
                {"custcolumn" + dateTimeCustomFieldColumnId, "desc", 200, "date"}
        };
    }

    @DataProvider(name = "contactSearchData")
    public Object[][] getContactSearchData() {
        // Create test contact using Albatross
        String contactSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "contact");

        JsonPath jp = allCrudFunctions.getContactResponse(albatrossURL, albatrossTkn, contactSlug).jsonPath();
        String contactId = jp.get("data.contact.id");
        String contactFirstName = jp.get("data.contact.firstname");
        String contactLastName = jp.get("data.contact.lastname");
        
        assertThat("Contact slug should not be null", contactSlug, notNullValue());
        assertThat("Contact ID should not be null", contactId, notNullValue());
        assertThat("Contact first name should not be null", contactFirstName, notNullValue());
        assertThat("Contact last name should not be null", contactLastName, notNullValue());
        
        return new Object[][] { { contactSlug, contactId, contactFirstName, contactLastName } };
    }

    @DataProvider(name = "contactSearchDataForResponseDataValidation", parallel = true)
    public Object[][] getContactSearchDataForResponseDataValidation() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // Create a company first (contacts need to be associated with a company)
            CompletableFuture<Response> companyFuture = CompletableFuture.supplyAsync(() ->
                    commanFunction.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken), executor);

            String companySlug = companyFuture.thenApply(r -> r.jsonPath().getString("slug")).join();
            assertThat("Company slug should not be null", companySlug, notNullValue());

            // Create contact with comprehensive data
            CompletableFuture<Response> contactFuture = CompletableFuture.supplyAsync(() ->
                    commanFunction.createNewContact_POST(baseURL, apiAuthToken, companySlug), executor);

            String contactSlug = contactFuture.thenApply(r -> r.jsonPath().getString("slug")).join();

            // Get contact details from GET endpoint (uses snake_case)
            JsonPath contactJp = allCrudFunctions.getContactResponse(albatrossURL, albatrossTkn, contactSlug).jsonPath();
            String contactId = contactJp.get("data.contact.id");
            String contactFirstName = contactJp.get("data.contact.firstname");
            String contactLastName = contactJp.get("data.contact.lastname");
            // Construct full name from first and last name
            String name = (contactFirstName != null && contactLastName != null) ? 
                contactFirstName + " " + contactLastName : null;
            String contactEmail = contactJp.get("data.contact.email");
            String contactNumber = contactJp.get("data.contact.contactnumber");
            String city = contactJp.get("data.contact.city");
            String address = contactJp.get("data.contact.address");
            String locality = contactJp.get("data.contact.locality");
            String state = contactJp.get("data.contact.state");
            String country = contactJp.get("data.contact.country");
            String postalCode = contactJp.get("data.contact.postal_code");
            String designation = contactJp.get("data.contact.designation");
            String linkedin = contactJp.get("data.contact.linkedin");
            String facebook = contactJp.get("data.contact.facebook");
            String twitter = contactJp.get("data.contact.twitter");
            String xing = contactJp.get("data.contact.xing");
            String avatar = contactJp.get("data.contact.avatar");
            Integer stageIdObj = contactJp.get("data.contact.stageid");
            int stageId = (stageIdObj != null) ? stageIdObj : 0;
            // Note: GET endpoint uses 'owner' not 'ownerid', and might not have accountid directly
            Integer ownerIdObj = contactJp.get("data.contact.owner");
            int ownerId = (ownerIdObj != null) ? ownerIdObj : 0;
            String createdByObj = contactJp.get("data.contact.createdby");
            int createdBy = (createdByObj != null) ? Integer.parseInt(createdByObj) : 0;
            Integer createdOnStr = contactJp.get("data.contact.createdon");
            int createdOn = (createdOnStr != null) ? createdOnStr : 0;
            Integer updatedByObj = contactJp.get("data.contact.updatedby");
            int updatedBy = (updatedByObj != null) ? updatedByObj : 0;
            Integer updatedOnStr = contactJp.get("data.contact.updatedon");
            int updatedOn = (updatedOnStr != null) ? updatedOnStr : 0;
            // GET endpoint might return deleted as integer (0/1) or boolean
            Object deletedObj = contactJp.get("data.contact.deleted");
            boolean deleted = (deletedObj instanceof Boolean) ? (Boolean) deletedObj : 
                             (deletedObj instanceof Integer) ? ((Integer) deletedObj != 0) : false;
            
            // Get account ID from ThreadManager
            int accountId = ThreadManager.getAccount().getAccountId();
            
            // Get company details
            JsonPath companyJp = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTkn, companySlug).jsonPath();
            Integer companyIdObj = companyJp.get("data.company.id");
            int companyId = (companyIdObj != null) ? companyIdObj : 0;
            String companyName = companyJp.get("data.company.companyname");
            Object companyDeletedObj = companyJp.get("data.company.deleted");
            boolean companyDeleted = (companyDeletedObj instanceof Boolean) ? (Boolean) companyDeletedObj : 
                                   (companyDeletedObj instanceof Integer) ? ((Integer) companyDeletedObj != 0) : false;

            assertThat("Contact slug should not be null", contactSlug, notNullValue());
            assertThat("Contact ID should not be null", contactId, notNullValue());
            assertThat("Contact first name should not be null", contactFirstName, notNullValue());
            assertThat("Contact last name should not be null", contactLastName, notNullValue());

            ContactSearchData data = new ContactSearchData();
            data.contactSlug = contactSlug;
            data.contactId = contactId;
            data.contactFirstName = contactFirstName;
            data.contactLastName = contactLastName;
            data.name = name;
            data.contactEmail = contactEmail;
            data.contactNumber = contactNumber;
            data.city = city;
            data.address = address;
            data.locality = locality;
            data.state = state;
            data.country = country;
            data.postalCode = postalCode;
            data.designation = designation;
            data.linkedin = linkedin;
            data.facebook = facebook;
            data.twitter = twitter;
            data.xing = xing;
            data.avatar = avatar;
            data.companySlug = companySlug;
            data.companyId = companyId;
            data.companyName = companyName;
            data.stageId = stageId;
            data.accountId = accountId;
            data.ownerId = ownerId;
            data.createdBy = createdBy;
            data.createdOn = createdOn;
            data.updatedBy = updatedBy;
            data.updatedOn = updatedOn;
            data.deleted = deleted;
            data.companyDeleted = companyDeleted;

            return new Object[][] { { data } };
        } catch (Exception e) {
            throw new AssertionError("Failed to create data for response data validation: " + e.getMessage(), e);
        } finally {
            executor.shutdown();
        }
    }
}
