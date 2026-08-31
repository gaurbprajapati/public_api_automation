package io.recruitcrm.CandidateService;

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CandidateRelatedHotlistsTest extends TestBase {

    commanFunction function = new commanFunction();
    String apiAuthToken;
    String albatrossTkn;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistSearchData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateRelatedHotlistsSearch_Success(String candidateSlug, int hotlistId, int recordId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "candidates");
        requestBody.put("recordId", recordId);

        Response response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, null, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        // Validate response structure based on provided response
        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Related hotlists fetched successfully."));
        assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Validate responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", jp.get("meta.responseType.code"), equalTo(103));

        // Validate data array
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Data array should not be empty", (Integer) jp.get("data.size()"), greaterThan(0));

        // Validate first hotlist object structure
        assertThat("Hotlist ID should not be null", jp.get("data[0].id"), notNullValue());
        assertThat("Hotlist name should not be null", jp.get("data[0].name"), notNullValue());
        assertThat("CreatedOn should not be null", jp.get("data[0].createdOn"), notNullValue());
        assertThat("UpdatedOn should not be null", jp.get("data[0].updatedOn"), notNullValue());
        assertThat("OwnerId should not be null", jp.get("data[0].ownerId"), notNullValue());
        assertThat("OwnerName should not be null", jp.get("data[0].ownerName"), notNullValue());
        assertThat("EntityName should not be null", jp.get("data[0].entityName"), notNullValue());
        assertThat("Shared should not be null", jp.get("data[0].shared"), notNullValue());
        assertThat("AccountId should not be null", jp.get("data[0].accountId"), notNullValue());
        assertThat("UpdatedBy should not be null", jp.get("data[0].updatedBy"), notNullValue());

        // Verify the created hotlist is found in search results
        boolean hotlistFound = false;
        for (int i = 0; i < ((Integer) jp.get("data.size()")); i++) {
            if (jp.get("data[" + i + "].id").equals(hotlistId)) {
                hotlistFound = true;
                break;
            }
        }
        assertThat("Created hotlist should be found in search results", hotlistFound, is(true));

        // Validate response schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/candidate/getRelatedHotlists.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistSearchData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateRelatedHotlistsSearch_WithoutAuth(String candidateSlug, int hotlistId, int recordId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "candidates");
        requestBody.put("recordId", recordId);

        Response response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related-hotlists/search/get",
                null, null, null, true, requestBody.toString());

        assertThat("Expected status code 401 for unauthorized request", response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistSearchData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateRelatedHotlistsSearch_InvalidAuth(String candidateSlug, int hotlistId, int recordId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "candidates");
        requestBody.put("recordId", recordId);

        Response response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related-hotlists/search/get",
                "invalid_token_12345", null, null, true, requestBody.toString());

        assertThat("Expected status code 401 for invalid token", response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistSearchData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateRelatedHotlistsSearch_InvalidEntityName(String candidateSlug, int hotlistId, int recordId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "InvalidEntity");
        requestBody.put("recordId", recordId);

        Response response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, null, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistSearchData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateRelatedHotlistsSearch_InvalidRecordId(String candidateSlug, int hotlistId, int recordId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "candidates");
        requestBody.put("recordId", 99999999); // Invalid record ID

        Response response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, null, null, true, requestBody.toString());

        assertThat("Expected status code 404 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(404));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistSearchData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateRelatedHotlistsSearch_MissingEntityName(String candidateSlug, int hotlistId, int recordId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("recordId", recordId);
        // Missing entityName

        Response response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, null, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistSearchData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateRelatedHotlistsSearch_MissingRecordId(String candidateSlug, int hotlistId, int recordId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "candidates");
        // Missing recordId

        Response response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, null, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistSearchData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateRelatedHotlistsDelete_Success(String candidateSlug, int hotlistId, int recordId) {
        // Step 1: First search to confirm hotlist exists
        JSONObject searchRequestBody = new JSONObject();
        searchRequestBody.put("entityName", "candidates");
        searchRequestBody.put("recordId", recordId);

        Response searchResponse = RestClient.doPost1("JSON", candidatesURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, null, null, true, searchRequestBody.toString());

        assertThat("Search should succeed before delete", searchResponse.getStatusCode(), equalTo(200));

        JsonPath searchJp = searchResponse.jsonPath();
        assertThat("Should have hotlists before delete", (Integer) searchJp.get("data.size()"), greaterThan(0));

        // Step 2: Delete the hotlist
        JSONObject deleteRequestBody = new JSONObject();
        deleteRequestBody.put("recordId", recordId);
        deleteRequestBody.put("entityName", "candidates");
        deleteRequestBody.put("hotlistIds", new int[]{hotlistId});

        Response deleteResponse = RestClient.doPost1("JSON", candidatesURL, "hotlists/related/delete",
                albatrossTkn, null, null, true, deleteRequestBody.toString());

        assertThat("Delete should succeed", deleteResponse.getStatusCode(), equalTo(200));

        // Step 3: Validate delete response
        JsonPath deleteJp = deleteResponse.jsonPath();
        assertThat("Meta should not be null", deleteJp.get("meta"), notNullValue());
        assertThat("Message should match expected", deleteJp.get("meta.message"), equalTo("Related hotlists removed successfully"));
        assertThat("Status should be 200", deleteJp.get("meta.status"), equalTo(200));
        assertThat("Request UUID should not be null", deleteJp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", deleteJp.get("meta.timestamp"), notNullValue());
        assertThat("ResponseType should not be null", deleteJp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", deleteJp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", deleteJp.get("meta.responseType.code"), equalTo(103));
        assertThat("Data should be null", deleteJp.get("data"), nullValue());

        // Validate delete response schema
        deleteResponse.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/candidate/deleteRelatedHotlists.json"));

        // Step 4: Search again to confirm deletion
        Response searchAfterDeleteResponse = RestClient.doPost1("JSON", candidatesURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, null, null, true, searchRequestBody.toString());

        assertThat("Search after delete should succeed", searchAfterDeleteResponse.getStatusCode(), equalTo(200));

        JsonPath searchAfterDeleteJp = searchAfterDeleteResponse.jsonPath();
        // Should have empty array
        assertThat("Should have empty data array after deletion", (Integer) searchAfterDeleteJp.get("data.size()"), equalTo(0));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistSearchData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateRelatedHotlistsDelete_WithoutAuth(String candidateSlug, int hotlistId, int recordId) {
        JSONObject deleteRequestBody = new JSONObject();
        deleteRequestBody.put("recordId", recordId);
        deleteRequestBody.put("entityName", "candidates");
        deleteRequestBody.put("hotlistIds", new int[]{hotlistId});

        Response response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related/delete",
                null, null, null, true, deleteRequestBody.toString());

        assertThat("Expected status code 401 for unauthorized delete request", response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistSearchData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateRelatedHotlistsDelete_InvalidAuth(String candidateSlug, int hotlistId, int recordId) {
        JSONObject deleteRequestBody = new JSONObject();
        deleteRequestBody.put("recordId", recordId);
        deleteRequestBody.put("entityName", "candidates");
        deleteRequestBody.put("hotlistIds", new int[]{hotlistId});

        Response response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related/delete",
                "invalid_token_12345", null, null, true, deleteRequestBody.toString());

        assertThat("Expected status code 401 for invalid token delete request", response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistSearchData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateRelatedHotlistsDelete_InvalidRecordId(String candidateSlug, int hotlistId, int recordId) {
        JSONObject deleteRequestBody = new JSONObject();
        deleteRequestBody.put("recordId", 99999999); // Invalid record ID
        deleteRequestBody.put("entityName", "candidates");
        deleteRequestBody.put("hotlistIds", new int[]{hotlistId});

        Response response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related/delete",
                albatrossTkn, null, null, true, deleteRequestBody.toString());

        assertThat("Expected status code 404 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(404));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistSearchData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateRelatedHotlistsDelete_MissingFields(String candidateSlug, int hotlistId, int recordId) {
        // Test with missing recordId
        JSONObject deleteRequestBodyMissingRecordId = new JSONObject();
        deleteRequestBodyMissingRecordId.put("entityName", "candidates");
        deleteRequestBodyMissingRecordId.put("hotlistIds", new int[]{hotlistId});

        Response responseMissingRecordId = RestClient.doPost1("JSON", candidatesURL, "hotlists/related/delete",
                albatrossTkn, null, null, true, deleteRequestBodyMissingRecordId.toString());

        assertThat("Expected status code 400 for missing recordId but got " + responseMissingRecordId.getStatusCode(),
                responseMissingRecordId.getStatusCode(), equalTo(400));

        // Test with missing entityName
        JSONObject deleteRequestBodyMissingEntityName = new JSONObject();
        deleteRequestBodyMissingEntityName.put("recordId", recordId);
        deleteRequestBodyMissingEntityName.put("hotlistIds", new int[]{hotlistId});

        Response responseMissingEntityName = RestClient.doPost1("JSON", candidatesURL, "hotlists/related/delete",
                albatrossTkn, null, null, true, deleteRequestBodyMissingEntityName.toString());

        assertThat("Expected status code 400 for missing entityName but got " + responseMissingEntityName.getStatusCode(),
                responseMissingEntityName.getStatusCode(), equalTo(400));

        // Test with missing hotlistIds
        JSONObject deleteRequestBodyMissingHotlistIds = new JSONObject();
        deleteRequestBodyMissingHotlistIds.put("recordId", recordId);
        deleteRequestBodyMissingHotlistIds.put("entityName", "candidates");

        Response responseMissingHotlistIds = RestClient.doPost1("JSON", candidatesURL, "hotlists/related/delete",
                albatrossTkn, null, null, true, deleteRequestBodyMissingHotlistIds.toString());

        assertThat("Expected status code 400 for missing hotlistIds but got " + responseMissingHotlistIds.getStatusCode(),
                responseMissingHotlistIds.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistSortOrderTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateRelatedHotlistsWithSortOrder(int recordId, String sortOrderName, JSONArray sortOrder) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "candidates");
        requestBody.put("recordId", recordId);
        requestBody.put("sortOrder", sortOrder);

        Response response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, null, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode() + " for sortOrder: " + sortOrderName, 
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify deals array exists and has at least 2 hotlists for sorting verification
        assertThat("Hotlists array should not be null", jp.get("data"), notNullValue());
        int hotlistsSize = jp.get("data.size()");
        assertThat("Should have at least 2 hotlists to verify sorting for " + sortOrderName, hotlistsSize, greaterThanOrEqualTo(2));
        
        // Verify sorting is applied correctly
        verifyHotlistSortOrder(jp, sortOrder, hotlistsSize, sortOrderName);
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "hotlistSearchTestData", groups = {"candidate_service", "nightly-build"})
    public void testCandidateRelatedHotlistsSearch(int recordId, String searchTerm, String searchType) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "candidates");
        requestBody.put("recordId", recordId);
        requestBody.put("searchTerm", searchTerm);

        Response response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, null, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode() + " for searchType: " + searchType, 
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Related hotlists fetched successfully."));

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Hotlists array should not be null", jp.get("data"), instanceOf(java.util.List.class));

        int hotlistsSize = jp.get("data.size()");
        
        // Verify search results based on search type
        switch (searchType) {
            case "exact_match":
                assertThat("Exact match search should return at least one hotlist", hotlistsSize, greaterThanOrEqualTo(1));
                boolean foundMatch = false;
                for (int i = 0; i < hotlistsSize; i++) {
                    String hotlistName = jp.get("data[" + i + "].name");
                    if (hotlistName != null && hotlistName.equalsIgnoreCase(searchTerm)) {
                        foundMatch = true;
                        break;
                    }
                }
                assertThat("Search results should contain the exact search term", foundMatch, equalTo(true));
                break;
                
            case "partial_match":
                assertThat("Partial match search should return at least one hotlist", hotlistsSize, greaterThanOrEqualTo(1));
                boolean foundPartialMatch = false;
                for (int i = 0; i < hotlistsSize; i++) {
                    String hotlistName = jp.get("data[" + i + "].name");
                    if (hotlistName != null && hotlistName.toLowerCase().contains(searchTerm.toLowerCase())) {
                        foundPartialMatch = true;
                        break;
                    }
                }
                assertThat("Search results should contain the partial search term", foundPartialMatch, equalTo(true));
                break;
                
            case "case_insensitive":
                assertThat("Case insensitive search should return at least one hotlist", hotlistsSize, greaterThanOrEqualTo(1));
                boolean foundCaseInsensitive = false;
                String lowerSearchTerm = searchTerm.toLowerCase();
                for (int i = 0; i < hotlistsSize; i++) {
                    String hotlistName = jp.get("data[" + i + "].name");
                    if (hotlistName != null && hotlistName.toLowerCase().contains(lowerSearchTerm)) {
                        foundCaseInsensitive = true;
                        break;
                    }
                }
                assertThat("Case insensitive search should return matching results", foundCaseInsensitive, equalTo(true));
                break;
                
            case "no_results":
                assertThat("No results search should return empty hotlists array", hotlistsSize, equalTo(0));
                assertThat("Hotlists array should be empty for non-existent search term", 
                        jp.get("data"), instanceOf(java.util.List.class));
                break;
                
            default:
                assertThat("Hotlists array should not be null for search type: " + searchType, 
                        jp.get("data"), notNullValue());
                assertThat("Hotlists array should be a list for search type: " + searchType, 
                        jp.get("data"), instanceOf(java.util.List.class));
                assertThat("Hotlists size should be non-negative for search type: " + searchType, 
                        hotlistsSize, greaterThanOrEqualTo(0));
                break;
        }

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/candidate/getRelatedHotlists.json"));
    }

    private void verifyHotlistSortOrder(JsonPath jp, JSONArray sortOrder, int hotlistsSize, String sortOrderName) {
        if (sortOrder.length() == 0) {
            return;
        }
        
        JSONObject firstSort = sortOrder.getJSONObject(0);
        String field = firstSort.getString("field");
        String order = firstSort.getString("order");
        
        // Verify sorting based on the first sort field
        for (int i = 0; i < hotlistsSize - 1; i++) {
            Object currentValueObj = jp.get("data[" + i + "]." + field);
            Object nextValueObj = jp.get("data[" + (i + 1) + "]." + field);
            
            if (currentValueObj != null && nextValueObj != null) {
                // Handle different data types
                if (currentValueObj instanceof Number && nextValueObj instanceof Number) {
                    // Numeric comparison (for ownerId, createdOn, updatedOn)
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
                    // String comparison (for name)
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

    @DataProvider(name = "hotlistSearchData")
    public Object[][] getHotlistSearchData() {
        AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
        Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);

        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));

        JsonPath jp = candidateResponse.jsonPath();
        String candidateSlug = jp.get("data.candidate.slug");
        int recordId = jp.get("data.candidate.id");

        assertThat("Candidate slug should not be null", candidateSlug, notNullValue());
        assertThat("Record ID should not be null", recordId, notNullValue());

        Response hotlistResponse = function.createNewHotlist(baseURL, apiAuthToken, "candidate");

        assertThat("Failed to create test hotlist", hotlistResponse.getStatusCode(), equalTo(200));

        JsonPath hotlistJp = hotlistResponse.jsonPath();
        int hotlistId = hotlistJp.getInt("id");

        assertThat("Hotlist ID should not be null", hotlistId, notNullValue());

        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(candidateSlug);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistId));
        String basePath = "hotlists/{hotlist}/add-record";

        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParameters, true, hotlistRelated);

        assertThat("Failed to add candidate to hotlist", addResponse.getStatusCode(), equalTo(200));

        return new Object[][]{{candidateSlug, hotlistId, recordId}};
    }

    @DataProvider(name = "hotlistSortOrderTestData")
    public Object[][] getHotlistSortOrderTestData() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        try {
            // Step 1: Create candidate
            AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
            Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
            assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
            JsonPath candidateJp = candidateResponse.jsonPath();
            String candidateSlug = candidateJp.get("data.candidate.slug");
            int recordId = candidateJp.get("data.candidate.id");
            assertThat("Candidate slug should not be null", candidateSlug, notNullValue());
            assertThat("Record ID should not be null", recordId, notNullValue());
            
            // Step 2: Create at least 2 hotlists in parallel (independent operations)
            CompletableFuture<Response> hotlist1Future = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewHotlist(baseURL, apiAuthToken, "candidate");
                assertThat("Failed to create first test hotlist", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> hotlist2Future = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewHotlist(baseURL, apiAuthToken, "candidate");
                assertThat("Failed to create second test hotlist", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both hotlists to be created
            Response hotlist1Response = hotlist1Future.join();
            Response hotlist2Response = hotlist2Future.join();
            
            JsonPath hotlist1Jp = hotlist1Response.jsonPath();
            int hotlist1Id = hotlist1Jp.getInt("id");
            
            JsonPath hotlist2Jp = hotlist2Response.jsonPath();
            int hotlist2Id = hotlist2Jp.getInt("id");
            
            // Step 3: Add candidate to both hotlists in parallel
            CompletableFuture<Response> add1Future = CompletableFuture.supplyAsync(() -> {
                HotlistRelated hotlistRelated1 = new HotlistRelated();
                hotlistRelated1.setRelated(candidateSlug);
                Map<String, String> pathParameters1 = new HashMap<>();
                pathParameters1.put("hotlist", String.valueOf(hotlist1Id));
                String basePath1 = "hotlists/{hotlist}/add-record";
                Response response = RestClient.doPost1("JSON", baseURL, basePath1, apiAuthToken, null, pathParameters1, true, hotlistRelated1);
                assertThat("Failed to add candidate to first hotlist", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> add2Future = CompletableFuture.supplyAsync(() -> {
                HotlistRelated hotlistRelated2 = new HotlistRelated();
                hotlistRelated2.setRelated(candidateSlug);
                Map<String, String> pathParameters2 = new HashMap<>();
                pathParameters2.put("hotlist", String.valueOf(hotlist2Id));
                String basePath2 = "hotlists/{hotlist}/add-record";
                Response response = RestClient.doPost1("JSON", baseURL, basePath2, apiAuthToken, null, pathParameters2, true, hotlistRelated2);
                assertThat("Failed to add candidate to second hotlist", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both additions to complete
            add1Future.join();
            add2Future.join();
            
            // Read sortOrder configurations from JSON file
            JSONObject sortOrderJson = readJsonFileFromPath("src/test/resources/privateApi/candidate/candidateRelatedHotlistsSortOrder.json");
            JSONArray configurations = sortOrderJson.getJSONArray("sortOrderConfigurations");
            
            // Create test data for each sortOrder configuration
            Object[][] testData = new Object[configurations.length()][3];
            for (int i = 0; i < configurations.length(); i++) {
                JSONObject config = configurations.getJSONObject(i);
                String sortOrderName = config.getString("name");
                JSONArray sortOrder = config.getJSONArray("sortOrder");
                
                testData[i][0] = recordId;
                testData[i][1] = sortOrderName;
                testData[i][2] = sortOrder;
            }
            
            return testData;
        } finally {
            executor.shutdown();
        }
    }

    @DataProvider(name = "hotlistSearchTestData")
    public Object[][] getHotlistSearchTestData() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        try {
            // Step 1: Create candidate
            AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
            Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
            assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
            JsonPath candidateJp = candidateResponse.jsonPath();
            String candidateSlug = candidateJp.get("data.candidate.slug");
            int recordId = candidateJp.get("data.candidate.id");
            assertThat("Candidate slug should not be null", candidateSlug, notNullValue());
            assertThat("Record ID should not be null", recordId, notNullValue());
            
            // Step 2: Create hotlists with different names in parallel
            CompletableFuture<Response> hotlist1Future = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewHotlist(baseURL, apiAuthToken, "candidate");
                assertThat("Failed to create first test hotlist", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> hotlist2Future = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewHotlist(baseURL, apiAuthToken, "candidate");
                assertThat("Failed to create second test hotlist", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both hotlists to be created
            Response hotlist1Response = hotlist1Future.join();
            Response hotlist2Response = hotlist2Future.join();
            
            JsonPath hotlist1Jp = hotlist1Response.jsonPath();
            int hotlist1Id = hotlist1Jp.getInt("id");
            String hotlist1Name = hotlist1Jp.getString("name");
            
            JsonPath hotlist2Jp = hotlist2Response.jsonPath();
            int hotlist2Id = hotlist2Jp.getInt("id");
            
            // Step 3: Add candidate to both hotlists in parallel
            CompletableFuture<Response> add1Future = CompletableFuture.supplyAsync(() -> {
                HotlistRelated hotlistRelated1 = new HotlistRelated();
                hotlistRelated1.setRelated(candidateSlug);
                Map<String, String> pathParameters1 = new HashMap<>();
                pathParameters1.put("hotlist", String.valueOf(hotlist1Id));
                String basePath1 = "hotlists/{hotlist}/add-record";
                Response response = RestClient.doPost1("JSON", baseURL, basePath1, apiAuthToken, null, pathParameters1, true, hotlistRelated1);
                assertThat("Failed to add candidate to first hotlist", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> add2Future = CompletableFuture.supplyAsync(() -> {
                HotlistRelated hotlistRelated2 = new HotlistRelated();
                hotlistRelated2.setRelated(candidateSlug);
                Map<String, String> pathParameters2 = new HashMap<>();
                pathParameters2.put("hotlist", String.valueOf(hotlist2Id));
                String basePath2 = "hotlists/{hotlist}/add-record";
                Response response = RestClient.doPost1("JSON", baseURL, basePath2, apiAuthToken, null, pathParameters2, true, hotlistRelated2);
                assertThat("Failed to add candidate to second hotlist", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both additions to complete
            add1Future.join();
            add2Future.join();
            
            // Create test data for different search scenarios
            return new Object[][] {
                // {recordId, searchTerm, searchType}
                {recordId, hotlist1Name, "exact_match"},
                {recordId, hotlist1Name.substring(0, Math.min(5, hotlist1Name.length())), "partial_match"},
                {recordId, hotlist1Name.toUpperCase(), "case_insensitive"},
                {recordId, hotlist1Name.toLowerCase(), "case_insensitive"},
                {recordId, "NonExistentHotlistName12345", "no_results"}
            };
        } finally {
            executor.shutdown();
        }
    }
}
