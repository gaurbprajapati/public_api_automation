package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.reaper.ThreadManager;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetPitchCandidateContactsTest extends TestBase {

    String accountApiKey;
    String albatrossTkn;
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        accountApiKey = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "pitchCandidateContactsTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetPitchCandidateContactsSuccess(int candidateId, String companyName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candidateId));

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/pitch-candidate/{candidateId}/contacts/get", 
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Pitch contacts fetched successfully."));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Data should be an array", jp.get("data"), instanceOf(java.util.List.class));

        // Verify contacts array structure if data exists
        int dataSize = jp.get("data.size()");
        if (dataSize > 0) {
            assertThat("Contact ID should not be null", jp.get("data[0].id"), notNullValue());
            assertThat("Contact ID should be positive", (Integer) jp.get("data[0].id"), greaterThan(0));
            assertThat("Contact name should not be null", jp.get("data[0].name"), notNullValue());
            assertThat("Contact ID field should not be null", jp.get("data[0].contactId"), notNullValue());
            assertThat("Company name should not be null", jp.get("data[0].companyName"), notNullValue());
        }

        // Validate JSON schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/candidate/pitchCandidateContacts.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "pitchCandidateContactsTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetPitchCandidateContactsWithoutAuth(int candidateId, String companyName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candidateId));

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/pitch-candidate/{candidateId}/contacts/get", 
                null, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Unauthorised access"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "pitchCandidateContactsTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetPitchCandidateContactsInvalidAuth(int candidateId, String companyName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candidateId));

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/pitch-candidate/{candidateId}/contacts/get", 
                albatrossTkn + "invalid_token", queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Unauthorised access"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testGetPitchCandidateContactsInvalidCandidateId() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", "99999999");

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/pitch-candidate/{candidateId}/contacts/get", 
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 404", (Integer) jp.get("meta.status"), equalTo(404));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "pitchCandidateContactsSearchTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetPitchCandidateContactsSearch(int candidateId, String companyName, String searchTerm, String searchType) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", searchTerm);
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candidateId));

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/pitch-candidate/{candidateId}/contacts/get", 
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode() + " for searchType: " + searchType, 
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Pitch contacts fetched successfully."));

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Data should be an array", jp.get("data"), instanceOf(java.util.List.class));

        int contactsSize = jp.get("data.size()");
        
        // Verify search results based on search type
        switch (searchType) {
            case "exact_match":
                // Exact match should return at least one contact
                assertThat("Exact match search should return at least one contact", contactsSize, greaterThanOrEqualTo(1));
                // Verify the returned contact contains the search term in name or email
                boolean foundMatch = false;
                for (int i = 0; i < contactsSize; i++) {
                    String contactName = jp.get("data[" + i + "].name");
                    String contactEmail = jp.get("data[" + i + "].email");
                    if ((contactName != null && contactName.equalsIgnoreCase(searchTerm)) ||
                        (contactEmail != null && contactEmail.equalsIgnoreCase(searchTerm))) {
                        foundMatch = true;
                        break;
                    }
                }
                assertThat("Search results should contain the exact search term", foundMatch, equalTo(true));
                break;
                
            case "partial_match":
                // Partial match should return at least one contact
                assertThat("Partial match search should return at least one contact", contactsSize, greaterThanOrEqualTo(1));
                // Verify the returned contacts contain the search term
                boolean foundPartialMatch = false;
                for (int i = 0; i < contactsSize; i++) {
                    String contactName = jp.get("data[" + i + "].name");
                    String contactEmail = jp.get("data[" + i + "].email");
                    if ((contactName != null && contactName.toLowerCase().contains(searchTerm.toLowerCase())) ||
                        (contactEmail != null && contactEmail.toLowerCase().contains(searchTerm.toLowerCase()))) {
                        foundPartialMatch = true;
                        break;
                    }
                }
                assertThat("Search results should contain the partial search term", foundPartialMatch, equalTo(true));
                break;
                
            case "case_insensitive":
                // Case insensitive search should return results
                assertThat("Case insensitive search should return at least one contact", contactsSize, greaterThanOrEqualTo(1));
                // Verify search works regardless of case
                boolean foundCaseInsensitive = false;
                String lowerSearchTerm = searchTerm.toLowerCase();
                for (int i = 0; i < contactsSize; i++) {
                    String contactName = jp.get("data[" + i + "].name");
                    String contactEmail = jp.get("data[" + i + "].email");
                    if ((contactName != null && contactName.toLowerCase().contains(lowerSearchTerm)) ||
                        (contactEmail != null && contactEmail.toLowerCase().contains(lowerSearchTerm))) {
                        foundCaseInsensitive = true;
                        break;
                    }
                }
                assertThat("Case insensitive search should return matching results", foundCaseInsensitive, equalTo(true));
                break;
                
            case "no_results":
                // Search with non-existent term should return empty contacts array
                assertThat("No results search should return empty contacts array", contactsSize, equalTo(0));
                assertThat("Contacts array should be empty for non-existent search term", 
                        jp.get("data"), instanceOf(java.util.List.class));
                break;
                
            default:
                // For any other search type, verify basic response structure
                assertThat("Contacts array should not be null for search type: " + searchType, 
                        jp.get("data"), notNullValue());
                assertThat("Contacts array should be a list for search type: " + searchType, 
                        jp.get("data"), instanceOf(java.util.List.class));
                assertThat("Contacts size should be non-negative for search type: " + searchType, 
                        contactsSize, greaterThanOrEqualTo(0));
                break;
        }
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "pitchCandidateContactsSortOrderTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetPitchCandidateContactsWithSortOrder(int candidateId, String companyName, String sortOrderName, JSONArray sortPriorityList) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", sortPriorityList);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candidateId));

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/pitch-candidate/{candidateId}/contacts/get", 
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode() + " for sortOrder: " + sortOrderName, 
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify contacts array exists and has at least 2 contacts for sorting verification
        assertThat("Contacts array should not be null", jp.get("data"), notNullValue());
        assertThat("Contacts array should be a list", jp.get("data"), instanceOf(java.util.List.class));
        int contactsSize = jp.get("data.size()");
        assertThat("Should have at least 2 contacts to verify sorting for " + sortOrderName, contactsSize, greaterThanOrEqualTo(2));
        
        // Verify sorting is applied correctly
        verifyContactSortOrder(jp, sortPriorityList, contactsSize, sortOrderName);
    }

    private void verifyContactSortOrder(JsonPath jp, JSONArray sortPriorityList, int contactsSize, String sortOrderName) {
        if (sortPriorityList.length() == 0) {
            return;
        }
        
        JSONObject firstSort = sortPriorityList.getJSONObject(0);
        String field = firstSort.getString("field");
        String order = firstSort.getString("order");
        
        // Verify sorting based on the first sort field
        for (int i = 0; i < contactsSize - 1; i++) {
            Object currentValueObj = jp.get("data[" + i + "]." + field);
            Object nextValueObj = jp.get("data[" + (i + 1) + "]." + field);
            
            if (currentValueObj != null && nextValueObj != null) {
                // Handle different data types
                if (currentValueObj instanceof Number && nextValueObj instanceof Number) {
                    // Numeric comparison (for createdOn, etc.)
                    double currentNum = ((Number) currentValueObj).doubleValue();
                    double nextNum = ((Number) nextValueObj).doubleValue();
                    
                    if (order.equalsIgnoreCase("asc")) {
                        assertThat("Sort order should be ascending for numeric field: " + field + " in " + sortOrderName,
                                currentNum, lessThanOrEqualTo(nextNum));
                    } else if (order.equalsIgnoreCase("desc")) {
                        assertThat("Sort order should be descending for numeric field: " + field + " in " + sortOrderName,
                                currentNum, greaterThanOrEqualTo(nextNum));
                    }
                } else {
                    // String comparison (for name, email, phone, pitchStage, companyName, createdByName, etc.)
                    String currentValue = String.valueOf(currentValueObj);
                    String nextValue = String.valueOf(nextValueObj);
                    
                    if (order.equalsIgnoreCase("asc")) {
                        assertThat("Sort order should be ascending for field: " + field + " in " + sortOrderName,
                                currentValue.compareToIgnoreCase(nextValue), lessThanOrEqualTo(0));
                    } else if (order.equalsIgnoreCase("desc")) {
                        assertThat("Sort order should be descending for field: " + field + " in " + sortOrderName,
                                currentValue.compareToIgnoreCase(nextValue), greaterThanOrEqualTo(0));
                    }
                }
            }
        }
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "pitchCandidateContactsComprehensiveValidationTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetPitchCandidateContactsComprehensiveValidation(int candidateId, String companyName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candidateId));

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/pitch-candidate/{candidateId}/contacts/get", 
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Pitch contacts fetched successfully."));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Data should be an array", jp.get("data"), instanceOf(java.util.List.class));

        int dataSize = jp.get("data.size()");
        assertThat("Data size should be non-negative", dataSize, greaterThanOrEqualTo(0));

        // Comprehensive validation for each contact in the response
        if (dataSize > 0) {
            for (int i = 0; i < dataSize; i++) {
                String prefix = "data[" + i + "].";
                
                // Validate ID fields
                Integer id = jp.get(prefix + "id");
                assertThat("Contact ID should not be null at index " + i, id, notNullValue());
                assertThat("Contact ID should be positive at index " + i, id, greaterThan(0));
                
                Integer contactId = jp.get(prefix + "contactId");
                assertThat("Contact ID field should not be null at index " + i, contactId, notNullValue());
                assertThat("Contact ID field should be positive at index " + i, contactId, greaterThan(0));
                
                Integer candidateIdFromResponse = jp.get(prefix + "candidateId");
                assertThat("Candidate ID should not be null at index " + i, candidateIdFromResponse, notNullValue());
                assertThat("Candidate ID should match input candidateId at index " + i, candidateIdFromResponse, equalTo(candidateId));
                
                // Validate name fields
                String name = jp.get(prefix + "name");
                assertThat("Name should not be null at index " + i, name, notNullValue());
                assertThat("Name should not be empty at index " + i, name.trim().length(), greaterThan(0));
                
                // Validate contact object
                assertThat("Contact object should not be null at index " + i, jp.get(prefix + "contact"), notNullValue());
                String contactName = jp.get(prefix + "contact.name");
                assertThat("Contact name should not be null at index " + i, contactName, notNullValue());
                Integer contactIdFromContact = jp.get(prefix + "contact.contactId");
                assertThat("Contact ID in contact object should match contactId at index " + i, contactIdFromContact, equalTo(contactId));
                
                // Validate company information
                String companyNameFromResponse = jp.get(prefix + "companyName");
                assertThat("Company name should not be null at index " + i, companyNameFromResponse, notNullValue());
                assertThat("Company object should not be null at index " + i, jp.get(prefix + "company"), notNullValue());
                String companyNameFromCompany = jp.get(prefix + "company.name");
                assertThat("Company name in company object should match companyName at index " + i, 
                        companyNameFromCompany, equalTo(companyNameFromResponse));
                
                // Validate pitch stage
                String pitchStage = jp.get(prefix + "pitchStage");
                assertThat("Pitch stage should not be null at index " + i, pitchStage, notNullValue());
                assertThat("Pitch stage should be a valid stage at index " + i, pitchStage, 
                        anyOf(containsString("Pitched"), containsString("Shortlisted"), containsString("Interviewed"), 
                              containsString("Offered"), containsString("Hired"), containsString("Rejected"), containsString("On Hold")));
                
                Integer statusId = jp.get(prefix + "statusId");
                assertThat("Status ID should not be null at index " + i, statusId, notNullValue());
                assertThat("Status ID should be within reasonable range at index " + i, statusId, greaterThanOrEqualTo(1));
                assertThat("Status ID should be within reasonable range at index " + i, statusId, lessThanOrEqualTo(100));
                
                // Validate timestamps
                Integer createdOn = jp.get(prefix + "createdOn");
                assertThat("CreatedOn should not be null at index " + i, createdOn, notNullValue());
                assertThat("CreatedOn should be positive at index " + i, createdOn, greaterThan(0));
                
                Integer updatedOn = jp.get(prefix + "updatedOn");
                assertThat("UpdatedOn should not be null at index " + i, updatedOn, notNullValue());
                assertThat("UpdatedOn should be positive at index " + i, updatedOn, greaterThan(0));
                
                Integer stageDate = jp.get(prefix + "stageDate");
                assertThat("StageDate should not be null at index " + i, stageDate, notNullValue());
                assertThat("StageDate should be positive at index " + i, stageDate, greaterThan(0));
                
                // Validate createdBy fields
                Integer createdBy = jp.get(prefix + "createdBy");
                assertThat("CreatedBy should not be null at index " + i, createdBy, notNullValue());
                assertThat("CreatedBy should be positive at index " + i, createdBy, greaterThan(0));
                
                String createdByName = jp.get(prefix + "createdByName");
                assertThat("CreatedByName should not be null at index " + i, createdByName, notNullValue());
                assertThat("CreatedByName should not be empty at index " + i, createdByName.trim().length(), greaterThan(0));
                
                // Validate email and phone (may be null or empty)
                String email = jp.get(prefix + "email");
                if (email != null && !email.isEmpty()) {
                    assertThat("Email should be a valid format at index " + i, email, containsString("@"));
                }
                
                String phone = jp.get(prefix + "phone");
                // Phone can be null or empty, but if present should be a string
                if (phone != null) {
                    assertThat("Phone should be a string at index " + i, phone, instanceOf(String.class));
                }
                
                // Validate contactEmailOptOut
                String contactEmailOptOut = jp.get(prefix + "contactEmailOptOut");
                assertThat("ContactEmailOptOut should not be null at index " + i, contactEmailOptOut, notNullValue());
                assertThat("ContactEmailOptOut should be '0' or '1' at index " + i, contactEmailOptOut, anyOf(equalTo("0"), equalTo("1")));
            }
        }
    }

    @DataProvider(name = "pitchCandidateContactsSortOrderTestData")
    public Object[][] getPitchCandidateContactsSortOrderTestData() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        try {
            // Step 1: Create candidate
            Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
            assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
            JsonPath candidateJp = candidateResponse.jsonPath();
            int candidateId = candidateJp.get("data.candidate.id");
            assertThat("Candidate ID should not be null", candidateId, notNullValue());
            
            // Step 2: Create company in parallel
            CompletableFuture<Response> companyFuture = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewCompanyWithMandatoryFields(baseURL, accountApiKey);
                assertThat("Failed to create test company", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            Response companyResponse = companyFuture.join();
            JsonPath companyJp = companyResponse.jsonPath();
            String companySlug = companyJp.get("slug");
            String companyName = companyJp.get("company_name");
            assertThat("Company name should not be null", companyName, notNullValue());
            
            // Step 3: Create at least 2 contacts in parallel (independent operations)
            CompletableFuture<Response> contact1Future = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewContact_POST(baseURL, accountApiKey, companySlug);
                assertThat("Failed to create first test contact", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> contact2Future = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewContact_POST(baseURL, accountApiKey, companySlug);
                assertThat("Failed to create second test contact", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both contacts to be created
            Response contact1Response = contact1Future.join();
            Response contact2Response = contact2Future.join();
            
            JsonPath contact1Jp = contact1Response.jsonPath();
            String contact1Slug = contact1Jp.get("slug");
            
            JsonPath contact2Jp = contact2Response.jsonPath();
            String contact2Slug = contact2Jp.get("slug");
            
            // Step 4: Pitch candidate to both contacts in parallel
            CompletableFuture<Response> pitch1Future = CompletableFuture.supplyAsync(() -> {
                String pitchPath = "pitch/{candidate}/contact/{contact}";
                Map<String, String> pathParameters = new HashMap<>();
                pathParameters.put("candidate", candidateJp.get("data.candidate.slug"));
                pathParameters.put("contact", contact1Slug);
                
                Response response = RestClient.doPost1("JSON", baseURL, pitchPath, accountApiKey, null, pathParameters, true, null);
                assertThat("Failed to pitch candidate to first contact", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> pitch2Future = CompletableFuture.supplyAsync(() -> {
                String pitchPath = "pitch/{candidate}/contact/{contact}";
                Map<String, String> pathParameters = new HashMap<>();
                pathParameters.put("candidate", candidateJp.get("data.candidate.slug"));
                pathParameters.put("contact", contact2Slug);
                
                Response response = RestClient.doPost1("JSON", baseURL, pitchPath, accountApiKey, null, pathParameters, true, null);
                assertThat("Failed to pitch candidate to second contact", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both pitches to be created
            pitch1Future.join();
            pitch2Future.join();
            
            // Read sortPriorityList configurations from JSON file
            JSONObject sortOrderJson = readJsonFileFromPath("src/test/resources/privateApi/candidate/pitchCandidateContactsSortOrder.json");
            JSONArray configurations = sortOrderJson.getJSONArray("sortOrderConfigurations");
            
            // Create test data for each sortPriorityList configuration
            Object[][] testData = new Object[configurations.length()][4];
            for (int i = 0; i < configurations.length(); i++) {
                JSONObject config = configurations.getJSONObject(i);
                String sortOrderName = config.getString("name");
                JSONArray sortPriorityList = config.getJSONArray("sortPriorityList");
                
                testData[i][0] = candidateId;
                testData[i][1] = companyName;
                testData[i][2] = sortOrderName;
                testData[i][3] = sortPriorityList;
            }
            
            return testData;
        } finally {
            executor.shutdown();
        }
    }

    @DataProvider(name = "pitchCandidateContactsSearchTestData")
    public Object[][] getPitchCandidateContactsSearchTestData() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        try {
            // Step 1: Create candidate
            Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
            assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
            JsonPath candidateJp = candidateResponse.jsonPath();
            int candidateId = candidateJp.get("data.candidate.id");
            String candidateSlug = candidateJp.get("data.candidate.slug");
            assertThat("Candidate ID should not be null", candidateId, notNullValue());
            
            // Step 2: Create companies in parallel (independent operations)
            CompletableFuture<Response> company1Future = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewCompanyWithMandatoryFields(baseURL, accountApiKey);
                assertThat("Failed to create first test company", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> company2Future = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewCompanyWithMandatoryFields(baseURL, accountApiKey);
                assertThat("Failed to create second test company", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both companies to be created
            Response company1Response = company1Future.join();
            Response company2Response = company2Future.join();
            
            JsonPath company1Jp = company1Response.jsonPath();
            String company1Slug = company1Jp.get("slug");
            String company1Name = company1Jp.get("company_name");
            
            JsonPath company2Jp = company2Response.jsonPath();
            String company2Slug = company2Jp.get("slug");
            
            // Step 3: Create contacts in parallel (independent operations)
            CompletableFuture<Response> contact1Future = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewContact_POST(baseURL, accountApiKey, company1Slug);
                assertThat("Failed to create first test contact", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> contact2Future = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewContact_POST(baseURL, accountApiKey, company2Slug);
                assertThat("Failed to create second test contact", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both contacts to be created
            Response contact1Response = contact1Future.join();
            Response contact2Response = contact2Future.join();
            
            JsonPath contact1Jp = contact1Response.jsonPath();
            String contact1Slug = contact1Jp.get("slug");
            String contact1Name = contact1Jp.get("first_name") + " " + contact1Jp.get("last_name");
            
            JsonPath contact2Jp = contact2Response.jsonPath();
            String contact2Slug = contact2Jp.get("slug");
            
            // Step 4: Pitch candidate to both contacts in parallel
            CompletableFuture<Response> pitch1Future = CompletableFuture.supplyAsync(() -> {
                String pitchPath = "pitch/{candidate}/contact/{contact}";
                Map<String, String> pathParameters = new HashMap<>();
                pathParameters.put("candidate", candidateSlug);
                pathParameters.put("contact", contact1Slug);
                
                Response response = RestClient.doPost1("JSON", baseURL, pitchPath, accountApiKey, null, pathParameters, true, null);
                assertThat("Failed to pitch candidate to first contact", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> pitch2Future = CompletableFuture.supplyAsync(() -> {
                String pitchPath = "pitch/{candidate}/contact/{contact}";
                Map<String, String> pathParameters = new HashMap<>();
                pathParameters.put("candidate", candidateSlug);
                pathParameters.put("contact", contact2Slug);
                
                Response response = RestClient.doPost1("JSON", baseURL, pitchPath, accountApiKey, null, pathParameters, true, null);
                assertThat("Failed to pitch candidate to second contact", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both pitches to be created
            pitch1Future.join();
            pitch2Future.join();
            
            // Create test data for different search scenarios
            return new Object[][] {
                {candidateId, company1Name, contact1Name, "exact_match"},
                {candidateId, company1Name, contact1Name.substring(0, Math.min(5, contact1Name.length())), "partial_match"},
                {candidateId, company1Name, contact1Name.toUpperCase(), "case_insensitive"},
                {candidateId, company1Name, contact1Name.toLowerCase(), "case_insensitive"},
                {candidateId, company1Name, "NonExistentContactName12345", "no_results"}
            };
        } finally {
            executor.shutdown();
        }
    }

    @DataProvider(name = "pitchCandidateContactsTestData")
    public Object[][] getPitchCandidateContactsTestData() {
        // Step 1: Create a candidate
        Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
        JsonPath candidateJp = candidateResponse.jsonPath();
        int candidateId = candidateJp.get("data.candidate.id");
        String candidateSlug = candidateJp.get("data.candidate.slug");
        assertThat("Candidate ID should not be null", candidateId, notNullValue());
        
        // Step 2: Create a company and contact
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountApiKey);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        JsonPath companyJp = companyResponse.jsonPath();
        String companySlug = companyJp.get("slug");
        String companyName = companyJp.get("company_name");
        assertThat("Company name should not be null", companyName, notNullValue());
        
        Response contactResponse = function.createNewContact_POST(baseURL, accountApiKey, companySlug);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
        JsonPath contactJp = contactResponse.jsonPath();
        String contactSlug = contactJp.get("slug");
        
        // Step 3: Pitch candidate to contact
        String pitchPath = "pitch/{candidate}/contact/{contact}";
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("candidate", candidateSlug);
        pathParameters.put("contact", contactSlug);
        
        Response pitchResponse = RestClient.doPost1("JSON", baseURL, pitchPath, accountApiKey, null, pathParameters, true, null);
        assertThat("Failed to pitch candidate to contact", pitchResponse.getStatusCode(), equalTo(200));
        
        return new Object[][] { { candidateId, companyName } };
    }

    @DataProvider(name = "pitchCandidateContactsComprehensiveValidationTestData")
    public Object[][] getPitchCandidateContactsComprehensiveValidationTestData() {
        // Step 1: Create a candidate
        Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
        JsonPath candidateJp = candidateResponse.jsonPath();
        int candidateId = candidateJp.get("data.candidate.id");
        String candidateSlug = candidateJp.get("data.candidate.slug");
        assertThat("Candidate ID should not be null", candidateId, notNullValue());
        
        // Step 2: Create a company and contact
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountApiKey);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        JsonPath companyJp = companyResponse.jsonPath();
        String companySlug = companyJp.get("slug");
        String companyName = companyJp.get("company_name");
        assertThat("Company name should not be null", companyName, notNullValue());
        
        Response contactResponse = function.createNewContact_POST(baseURL, accountApiKey, companySlug);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
        JsonPath contactJp = contactResponse.jsonPath();
        String contactSlug = contactJp.get("slug");
        
        // Step 3: Pitch candidate to contact
        String pitchPath = "pitch/{candidate}/contact/{contact}";
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("candidate", candidateSlug);
        pathParameters.put("contact", contactSlug);
        
        Response pitchResponse = RestClient.doPost1("JSON", baseURL, pitchPath, accountApiKey, null, pathParameters, true, null);
        assertThat("Failed to pitch candidate to contact", pitchResponse.getStatusCode(), equalTo(200));
        
        return new Object[][] { { candidateId, companyName } };
    }
}

