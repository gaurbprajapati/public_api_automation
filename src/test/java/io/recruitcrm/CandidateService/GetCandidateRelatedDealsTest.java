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
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.reaper.ThreadManager;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCandidateRelatedDealsTest extends TestBase {

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
    @Test(dataProvider = "candidateRelatedDealsTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateRelatedDealsSuccess(String candidateSlug, int dealId, String companyName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", companyName);
        
        JSONArray sortOrder = new JSONArray();
        JSONObject sort1 = new JSONObject();
        sort1.put("field", "updatedon");
        sort1.put("order", "desc");
        JSONObject sort2 = new JSONObject();
        sort2.put("field", "dealvalue");
        sort2.put("order", "asc");
        sortOrder.put(sort1);
        sortOrder.put(sort2);
        requestBody.put("sortOrder", sortOrder);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateSlug", candidateSlug);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/{candidateSlug}/related-deals", 
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Related deals fetched successfully"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Deals array should not be null", jp.get("data.deals"), notNullValue());

        // Verify deals array structure
        if (jp.get("data.deals.size()") != null && (Integer) jp.get("data.deals.size()") > 0) {
            assertThat("Deal ID should not be null", jp.get("data.deals[0].id"), notNullValue());
            assertThat("Deal name should not be null", jp.get("data.deals[0].name"), notNullValue());
            assertThat("Deal value should not be null", jp.get("data.deals[0].dealvalue"), notNullValue());
            assertThat("Company name should not be null", jp.get("data.deals[0].companyname"), notNullValue());
            assertThat("Candidate name should not be null", jp.get("data.deals[0].candidatename"), notNullValue());
            assertThat("Candidate slug should match", jp.get("data.deals[0].candidateslug"), equalTo(candidateSlug));
            
            // Verify numeric values are positive when present
            if (jp.get("data.deals[0].id") != null) {
                assertThat("Deal ID should be positive", (Integer) jp.get("data.deals[0].id"), greaterThan(0));
            }
            if (jp.get("data.deals[0].dealvalue") != null) {
                assertThat("Deal value should be non-negative", (Float) jp.get("data.deals[0].dealvalue"), greaterThanOrEqualTo(0.0f));
            }
        }

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/candidate/candidateRelatedDeals.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateRelatedDealsTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateRelatedDealsWithoutAuth(String candidateSlug, int dealId, String companyName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", companyName);
        
        JSONArray sortOrder = new JSONArray();
        JSONObject sort1 = new JSONObject();
        sort1.put("field", "updatedon");
        sort1.put("order", "desc");
        requestBody.put("sortOrder", sortOrder);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateSlug", candidateSlug);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/{candidateSlug}/related-deals", 
                null, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateRelatedDealsTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateRelatedDealsInvalidAuth(String candidateSlug, int dealId, String companyName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", companyName);
        
        JSONArray sortOrder = new JSONArray();
        JSONObject sort1 = new JSONObject();
        sort1.put("field", "updatedon");
        sort1.put("order", "desc");
        requestBody.put("sortOrder", sortOrder);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateSlug", candidateSlug);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/{candidateSlug}/related-deals", 
                albatrossTkn + "invalid_token", queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateRelatedDealsInvalidCandidateSlug() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "BMW");
        
        JSONArray sortOrder = new JSONArray();
        JSONObject sort1 = new JSONObject();
        sort1.put("field", "updatedon");
        sort1.put("order", "desc");
        requestBody.put("sortOrder", sortOrder);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateSlug", "invalid-candidate-slug-12345");

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/{candidateSlug}/related-deals", 
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 404", (Integer) jp.get("meta.status"), equalTo(404));
        assertThat("Data should be null", jp.get("data"), nullValue());
        assertThat("Errors should not be null", jp.get("errors"), notNullValue());
        assertThat("Error message should contain candidate not found", jp.get("errors[0].message"), containsString("Candidate not found for slug"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateRelatedDealsTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateRelatedDealsEmptyRequestBody(String candidateSlug, int dealId, String companyName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        // Empty request body

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateSlug", candidateSlug);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/{candidateSlug}/related-deals", 
                albatrossTkn, queryParams, pathParams, true, null);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateRelatedDealsSearchTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateRelatedDealsSearch(String candidateSlug, String companyName, String searchTerm, String searchType) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", searchTerm);
        
        JSONArray sortOrder = new JSONArray();
        JSONObject sort1 = new JSONObject();
        sort1.put("field", "updatedon");
        sort1.put("order", "desc");
        requestBody.put("sortOrder", sortOrder);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateSlug", candidateSlug);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/{candidateSlug}/related-deals", 
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode() + " for searchType: " + searchType, 
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Related deals fetched successfully"));

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Deals array should not be null", jp.get("data.deals"), notNullValue());

        int dealsSize = jp.get("data.deals.size()");
        
        // Verify search results based on search type
        switch (searchType) {
            case "exact_match":
                // Exact match should return at least one deal
                assertThat("Exact match search should return at least one deal", dealsSize, greaterThanOrEqualTo(1));
                // Verify the returned deal contains the search term in company name or deal name
                boolean foundMatch = false;
                for (int i = 0; i < dealsSize; i++) {
                    String dealCompanyName = jp.get("data.deals[" + i + "].companyname");
                    String dealName = jp.get("data.deals[" + i + "].name");
                    if ((dealCompanyName != null && dealCompanyName.equalsIgnoreCase(searchTerm)) ||
                        (dealName != null && dealName.contains(searchTerm))) {
                        foundMatch = true;
                        break;
                    }
                }
                assertThat("Search results should contain the exact search term", foundMatch, equalTo(true));
                break;
                
            case "partial_match":
                // Partial match should return at least one deal
                assertThat("Partial match search should return at least one deal", dealsSize, greaterThanOrEqualTo(1));
                // Verify the returned deals contain the search term
                boolean foundPartialMatch = false;
                for (int i = 0; i < dealsSize; i++) {
                    String dealCompanyName = jp.get("data.deals[" + i + "].companyname");
                    String dealName = jp.get("data.deals[" + i + "].name");
                    if ((dealCompanyName != null && dealCompanyName.toLowerCase().contains(searchTerm.toLowerCase())) ||
                        (dealName != null && dealName.toLowerCase().contains(searchTerm.toLowerCase()))) {
                        foundPartialMatch = true;
                        break;
                    }
                }
                assertThat("Search results should contain the partial search term", foundPartialMatch, equalTo(true));
                break;
                
            case "case_insensitive":
                // Case insensitive search should return results
                assertThat("Case insensitive search should return at least one deal", dealsSize, greaterThanOrEqualTo(1));
                // Verify search works regardless of case
                boolean foundCaseInsensitive = false;
                String lowerSearchTerm = searchTerm.toLowerCase();
                for (int i = 0; i < dealsSize; i++) {
                    String dealCompanyName = jp.get("data.deals[" + i + "].companyname");
                    String dealName = jp.get("data.deals[" + i + "].name");
                    if ((dealCompanyName != null && dealCompanyName.toLowerCase().contains(lowerSearchTerm)) ||
                        (dealName != null && dealName.toLowerCase().contains(lowerSearchTerm))) {
                        foundCaseInsensitive = true;
                        break;
                    }
                }
                assertThat("Case insensitive search should return matching results", foundCaseInsensitive, equalTo(true));
                break;
                
            case "no_results":
                // Search with non-existent term should return empty deals array
                assertThat("No results search should return empty deals array", dealsSize, equalTo(0));
                assertThat("Deals array should be empty for non-existent search term", 
                        jp.get("data.deals"), instanceOf(java.util.List.class));
                break;
                
            default:
                // For any other search type, verify basic response structure
                assertThat("Deals array should not be null for search type: " + searchType, 
                        jp.get("data.deals"), notNullValue());
                assertThat("Deals array should be a list for search type: " + searchType, 
                        jp.get("data.deals"), instanceOf(java.util.List.class));
                assertThat("Deals size should be non-negative for search type: " + searchType, 
                        dealsSize, greaterThanOrEqualTo(0));
                break;
        }

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/candidate/candidateRelatedDeals.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateRelatedDealsSortOrderTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateRelatedDealsWithSortOrder(String candidateSlug, String companyName, String sortOrderName, JSONArray sortOrder) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", companyName);
        requestBody.put("sortOrder", sortOrder);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateSlug", candidateSlug);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidates/{candidateSlug}/related-deals", 
                albatrossTkn, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode() + " for sortOrder: " + sortOrderName, 
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify deals array exists and has at least 2 deals for sorting verification
        assertThat("Deals array should not be null", jp.get("data.deals"), notNullValue());
        int dealsSize = jp.get("data.deals.size()");
        assertThat("Should have at least 2 deals to verify sorting for " + sortOrderName, dealsSize, greaterThanOrEqualTo(2));
        
        // Verify sorting is applied correctly
        verifySortOrder(jp, sortOrder, dealsSize, sortOrderName);
    }

    private void verifySortOrder(JsonPath jp, JSONArray sortOrder, int dealsSize, String sortOrderName) {
        if (sortOrder.length() == 0) {
            return;
        }
        
        JSONObject firstSort = sortOrder.getJSONObject(0);
        String field = firstSort.getString("field");
        String order = firstSort.getString("order");
        
        // Verify sorting based on the first sort field
        for (int i = 0; i < dealsSize - 1; i++) {
            Object currentValueObj = jp.get("data.deals[" + i + "]." + field);
            Object nextValueObj = jp.get("data.deals[" + (i + 1) + "]." + field);
            
            if (currentValueObj != null && nextValueObj != null) {
                // Handle different data types
                if (currentValueObj instanceof Number && nextValueObj instanceof Number) {
                    // Numeric comparison (for dealvalue, dealpercentagevalue, etc.)
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
                    // String comparison (for name, dealstagelabel, ownername, etc.)
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

    @DataProvider(name = "candidateRelatedDealsSortOrderTestData")
    public Object[][] getCandidateRelatedDealsSortOrderTestData() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        try {
            // Step 1 & 2: Create candidate and company in parallel (independent operations)
            CompletableFuture<Response> candidateFuture = CompletableFuture.supplyAsync(() -> {
                Response response = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
                assertThat("Failed to create test candidate", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> companyFuture = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewCompanyWithMandatoryFields(baseURL, accountApiKey);
                assertThat("Failed to create test company", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both candidate and company to be created
            Response candidateResponse = candidateFuture.join();
            Response companyResponse = companyFuture.join();
            
            JsonPath candidateJp = candidateResponse.jsonPath();
            String candidateSlug = candidateJp.get("data.candidate.slug");
            assertThat("Candidate slug should not be null", candidateSlug, notNullValue());
            
            JsonPath companyJp = companyResponse.jsonPath();
            String companySlug = companyJp.get("slug");
            String companyName = companyJp.get("company_name");
            assertThat("Company name should not be null", companyName, notNullValue());
            
            // Step 3: Create contact (required before job creation)
            Response contactResponse = function.createNewContact_POST(baseURL, accountApiKey, companySlug);
            assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
            JsonPath contactJp = contactResponse.jsonPath();
            String contactSlug = contactJp.get("slug");
            
            // Step 4: Create job
            Response jobResponse = function.createNewJob(baseURL, accountApiKey, companySlug, contactSlug);
            assertThat("Failed to create test job", jobResponse.getStatusCode(), equalTo(200));
            JsonPath jobJp = jobResponse.jsonPath();
            String jobSlug = jobJp.get("slug");
            
            // Step 5: Create at least 2 deals in parallel (independent operations)
            CompletableFuture<Response> deal1Future = CompletableFuture.supplyAsync(() -> {
                Deal deal1 = new Deal();
                deal1.setName("Deal A - " + companyName);
                deal1.setDeal_value(5000);
                deal1.setClose_date("2025-06-30");
                deal1.setDeal_stage("1");
                deal1.setDeal_type("1");
                deal1.setCompany_slug(companySlug);
                deal1.setJob_slug(jobSlug);
                deal1.setContact_slugs(contactSlug);
                deal1.setCandidate_slug(candidateSlug);
                
                Response response = RestClient.doPost("JSON", baseURL, "deals", 
                        accountApiKey, null, true, deal1);
                assertThat("Failed to create first test deal", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> deal2Future = CompletableFuture.supplyAsync(() -> {
                Deal deal2 = new Deal();
                deal2.setName("Deal B - " + companyName);
                deal2.setDeal_value(15000);
                deal2.setClose_date("2025-12-31");
                deal2.setDeal_stage("2");
                deal2.setDeal_type("1");
                deal2.setCompany_slug(companySlug);
                deal2.setJob_slug(jobSlug);
                deal2.setContact_slugs(contactSlug);
                deal2.setCandidate_slug(candidateSlug);
                
                Response response = RestClient.doPost("JSON", baseURL, "deals", 
                        accountApiKey, null, true, deal2);
                assertThat("Failed to create second test deal", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both deals to be created
            deal1Future.join();
            deal2Future.join();
            
            // Read sortOrder configurations from JSON file
            JSONObject sortOrderJson = readJsonFileFromPath("src/test/resources/privateApi/candidate/candidateRelatedDealsSortOrder.json");
            JSONArray configurations = sortOrderJson.getJSONArray("sortOrderConfigurations");
            
            // Create test data for each sortOrder configuration
            Object[][] testData = new Object[configurations.length()][4];
            for (int i = 0; i < configurations.length(); i++) {
                JSONObject config = configurations.getJSONObject(i);
                String sortOrderName = config.getString("name");
                JSONArray sortOrder = config.getJSONArray("sortOrder");
                
                testData[i][0] = candidateSlug;
                testData[i][1] = companyName;
                testData[i][2] = sortOrderName;
                testData[i][3] = sortOrder;
            }
            
            return testData;
        } finally {
            executor.shutdown();
        }
    }

    @DataProvider(name = "candidateRelatedDealsSearchTestData")
    public Object[][] getCandidateRelatedDealsSearchTestData() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        try {
            // Step 1: Create candidate
            Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
            assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
            JsonPath candidateJp = candidateResponse.jsonPath();
            String candidateSlug = candidateJp.get("data.candidate.slug");
            assertThat("Candidate slug should not be null", candidateSlug, notNullValue());
            
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
            String company2Name = company2Jp.get("company_name");
            
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
            
            JsonPath contact2Jp = contact2Response.jsonPath();
            String contact2Slug = contact2Jp.get("slug");
            
            // Step 4: Create jobs in parallel (independent operations)
            CompletableFuture<Response> job1Future = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewJob(baseURL, accountApiKey, company1Slug, contact1Slug);
                assertThat("Failed to create first test job", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> job2Future = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewJob(baseURL, accountApiKey, company2Slug, contact2Slug);
                assertThat("Failed to create second test job", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both jobs to be created
            Response job1Response = job1Future.join();
            Response job2Response = job2Future.join();
            
            JsonPath job1Jp = job1Response.jsonPath();
            String job1Slug = job1Jp.get("slug");
            
            JsonPath job2Jp = job2Response.jsonPath();
            String job2Slug = job2Jp.get("slug");
            
            // Step 5: Create deals in parallel (independent operations)
            CompletableFuture<Response> deal1Future = CompletableFuture.supplyAsync(() -> {
                Deal deal1 = new Deal();
                deal1.setName("SearchTest Deal Alpha - " + company1Name);
                deal1.setDeal_value(10000);
                deal1.setClose_date("2025-12-31");
                deal1.setDeal_stage("1");
                deal1.setDeal_type("1");
                deal1.setCompany_slug(company1Slug);
                deal1.setJob_slug(job1Slug);
                deal1.setContact_slugs(contact1Slug);
                deal1.setCandidate_slug(candidateSlug);
                
                Response response = RestClient.doPost("JSON", baseURL, "deals", 
                        accountApiKey, null, true, deal1);
                assertThat("Failed to create first test deal", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> deal2Future = CompletableFuture.supplyAsync(() -> {
                Deal deal2 = new Deal();
                deal2.setName("SearchTest Deal Beta - " + company2Name);
                deal2.setDeal_value(20000);
                deal2.setClose_date("2025-12-31");
                deal2.setDeal_stage("2");
                deal2.setDeal_type("1");
                deal2.setCompany_slug(company2Slug);
                deal2.setJob_slug(job2Slug);
                deal2.setContact_slugs(contact2Slug);
                deal2.setCandidate_slug(candidateSlug);
                
                Response response = RestClient.doPost("JSON", baseURL, "deals", 
                        accountApiKey, null, true, deal2);
                assertThat("Failed to create second test deal", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both deals to be created
            deal1Future.join();
            deal2Future.join();
            
            // Create test data for different search scenarios
            return new Object[][] {
                // {candidateSlug, companyName, searchTerm, searchType}
                {candidateSlug, company1Name, company1Name, "exact_match"},
                {candidateSlug, company1Name, company1Name.substring(0, Math.min(5, company1Name.length())), "partial_match"},
                {candidateSlug, company1Name, company1Name.toUpperCase(), "case_insensitive"},
                {candidateSlug, company1Name, company1Name.toLowerCase(), "case_insensitive"},
                {candidateSlug, company1Name, "SearchTest", "partial_match"},
                {candidateSlug, company1Name, "NonExistentCompanyName12345", "no_results"}
            };
        } finally {
            executor.shutdown();
        }
    }

    @DataProvider(name = "candidateRelatedDealsTestData")
    public Object[][] getCandidateRelatedDealsTestData() {
        // Step 1: Create a candidate
        Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
        JsonPath candidateJp = candidateResponse.jsonPath();
        String candidateSlug = candidateJp.get("data.candidate.slug");
        assertThat("Candidate slug should not be null", candidateSlug, notNullValue());
        
        // Step 2: Create a company and contact first (required for deal creation)
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
        
        // Step 3: Create a job
        Response jobResponse = function.createNewJob(baseURL, accountApiKey, companySlug, contactSlug);
        assertThat("Failed to create test job", jobResponse.getStatusCode(), equalTo(200));
        JsonPath jobJp = jobResponse.jsonPath();
        String jobSlug = jobJp.get("slug");
        
        // Step 4: Create a deal linked to the candidate
        Deal deal = new Deal();
        deal.setName("Test Deal for " + companyName);
        deal.setDeal_value(10000);
        deal.setClose_date("2025-12-31");
        deal.setDeal_stage("1");
        deal.setDeal_type("1");
        deal.setCompany_slug(companySlug);
        deal.setJob_slug(jobSlug);
        deal.setContact_slugs(contactSlug);
        deal.setCandidate_slug(candidateSlug);
        
        Response dealResponse = RestClient.doPost("JSON", baseURL, "deals", 
                accountApiKey, null, true, deal);
        assertThat("Failed to create test deal", dealResponse.getStatusCode(), equalTo(200));
        JsonPath dealJp = dealResponse.jsonPath();
        int dealId = dealJp.get("id");
        assertThat("Deal ID should not be null", dealId, notNullValue());
        
        return new Object[][] { { candidateSlug, dealId, companyName } };
    }
}
