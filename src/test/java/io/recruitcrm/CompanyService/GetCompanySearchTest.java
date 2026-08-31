package io.recruitcrm.CompanyService;

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
import io.rcrm.api.pojo.offlimit.MarkCompanyOffLimit;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCompanySearchTest extends TestBase {

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
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanySearch_Success() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();
        
        Response response = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Entities retrieved successfully"));
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("Context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Response code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data array
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Data should be an array", jp.get("data"), instanceOf(java.util.List.class));

        // Validate JSON schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/company/companySearchGet.json"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanySearch_WithoutAuth() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();
        
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                null, queryParameters, true, requestBody);

        assertThat("Expected status code 401 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanySearch_InvalidAuth() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();
        
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn + "invalid-token-123", queryParameters, true, requestBody);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanySearch_AfterCreatingCompany() {
        // Get initial count
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();
        
        Response initialResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn, queryParameters, true, requestBody);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        int initialSize = initialResponse.jsonPath().getInt("data.size()");

        // Create a company
        String companySlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "company");
        assertThat("Company slug should not be null", companySlug, notNullValue());

        // Get results after creating company
        Response afterCreateResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn, queryParameters, true, requestBody);
        
        assertThat("After create response should succeed", afterCreateResponse.getStatusCode(), equalTo(200));
        int afterCreateSize = afterCreateResponse.jsonPath().getInt("data.size()");

        // Verify size increased
        assertThat("Company search results should increase after creating company", 
                afterCreateSize, equalTo(initialSize + 1));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "companySearchData", groups = {"company_service", "nightly-build"})
    public void testCompanySearch_WithOwnerFilter(String companySlug, int companyId, String companyName) {
        // Search with owner filter
        JSONObject requestBody = createOwnerFilterRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();
        
        Response response = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn, queryParameters, null, true, requestBody.toString());
        
        assertThat("Response should succeed", response.getStatusCode(), equalTo(200));
        
        JsonPath jp = response.jsonPath();
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        
        // Verify all returned companies belong to the owner
        int dataSize = jp.getInt("data.size()");
        Response usersResponse = commanFunction.getUsers(baseURL, apiAuthToken);
        int ownerId = usersResponse.jsonPath().get("[0].id");
        
        for (int i = 0; i < dataSize; i++) {
            int companyOwnerId = jp.getInt("data[" + i + "].ownerid");
            assertThat("Company should belong to owner", companyOwnerId, equalTo(ownerId));
        }
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "companySearchData", groups = {"company_service", "nightly-build"})
    public void testCompanySearch_WithOffLimitFilter(String companySlug, int companyId, String companyName) {
        // Search with off-limit filter (inclusion)
        JSONObject requestBody = createOffLimitFilterRequestBody("inclusion");
        Map<String, String> queryParameters = createDefaultQueryParameters();
        
        Response initialResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn, queryParameters, true, requestBody);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        int initialSize = initialResponse.jsonPath().getInt("data.size()");

        // Get off-limit status ID
        int statusId = getOffLimitStatus();
        assertThat("Status ID should not be null", statusId, notNullValue());

        // Mark company as off-limit
        markCompanyAsOffLimit(companySlug, statusId);

        // Search after marking as off-limit
        Response afterMarkResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn, queryParameters, true, requestBody);
        
        assertThat("After mark response should succeed", afterMarkResponse.getStatusCode(), equalTo(200));
        int afterMarkSize = afterMarkResponse.jsonPath().getInt("data.size()");

        // Verify off-limit companies increased
        assertThat("Off-limit companies should increase after marking company as off-limit", 
                afterMarkSize, equalTo(initialSize + 1));
        
        // Verify the marked company is in the results
        JsonPath jp = afterMarkResponse.jsonPath();
        boolean companyFound = false;
        int dataSize = jp.getInt("data.size()");
        for (int i = 0; i < dataSize; i++) {
            String resultSlug = jp.getString("data[" + i + "].slug");
            if (companySlug.equals(resultSlug)) {
                companyFound = true;
                break;
            }
        }
        assertThat("Marked company should be in off-limit search results", companyFound, is(true));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "companySearchData", groups = {"company_service", "nightly-build"})
    public void testCompanySearch_WithHotlistFilter(String companySlug, int companyId, String companyName) {
        // Search with hotlist filter (not in any hotlist)
        JSONObject requestBody = createHotlistFilterRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();
        
        Response initialResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn, queryParameters, true, requestBody);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        int initialSize = initialResponse.jsonPath().getInt("data.size()");
        
        // Verify the company is in the initial results
        JsonPath initialJp = initialResponse.jsonPath();
        boolean companyFoundInitially = false;
        int initialDataSize = initialJp.getInt("data.size()");
        for (int i = 0; i < initialDataSize; i++) {
            String resultSlug = initialJp.getString("data[" + i + "].slug");
            if (companySlug.equals(resultSlug)) {
                companyFoundInitially = true;
                break;
            }
        }
        assertThat("Company should be in 'not in any hotlist' results initially", companyFoundInitially, is(true));

        // Create a hotlist and add company to it
        Response hotlistResponse = commanFunction.createNewHotlist(baseURL, apiAuthToken, "company");
        assertThat("Hotlist creation should succeed", hotlistResponse.getStatusCode(), equalTo(200));
        
        JsonPath hotlistJp = hotlistResponse.jsonPath();
        String hotlistId = hotlistJp.getString("id");
        assertThat("Hotlist ID should not be null", hotlistId, notNullValue());

        // Add company to hotlist
        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(companySlug);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", hotlistId);
        String basePath = "hotlists/{hotlist}/add-record";

        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParameters, true, hotlistRelated);
        assertThat("Add to hotlist should succeed", addResponse.getStatusCode(), equalTo(200));

        // Wait for the operation to complete and verify results change
        Response afterAddResponse = waitForSearchResultsChange(initialSize, requestBody, queryParameters, albatrossTkn, companySlug, 5, 1000);
        
        assertThat("After add response should succeed", afterAddResponse.getStatusCode(), equalTo(200));
        int afterAddSize = afterAddResponse.jsonPath().getInt("data.size()");

        // Verify size decreased (company is no longer "not in any hotlist")
        assertThat("Search results should decrease after adding company to hotlist", 
                afterAddSize, equalTo(initialSize - 1));
        
        // Verify the company is no longer in the results
        JsonPath afterAddJp = afterAddResponse.jsonPath();
        boolean companyFoundAfter = false;
        int afterAddDataSize = afterAddJp.getInt("data.size()");
        for (int i = 0; i < afterAddDataSize; i++) {
            String resultSlug = afterAddJp.getString("data[" + i + "].slug");
            if (companySlug.equals(resultSlug)) {
                companyFoundAfter = true;
                break;
            }
        }
        assertThat("Company should not be in 'not in any hotlist' results after adding to hotlist", 
                companyFoundAfter, is(false));
    }

    @DataProvider(name = "companySearchData")
    public Object[][] getCompanySearchData() {
        // Create test company using function
        String companySlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "company");
        assertThat("Company slug should not be null", companySlug, notNullValue());
        
        // Get company details to extract ID and name
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("company", companySlug);
        String basePath = "companies/{company}";
        
        Response companyResponse = RestClient.doGet("JSON", baseURL, basePath, apiAuthToken, null, pathParameters, true);
        assertThat("Company details should be retrieved", companyResponse.getStatusCode(), equalTo(200));
        
        JsonPath jp = companyResponse.jsonPath();
        int companyId = jp.get("id");
        String companyName = jp.get("company_name");
        
        assertThat("Company ID should not be null", companyId, notNullValue());
        assertThat("Company name should not be null", companyName, notNullValue());
        
        return new Object[][] { { companySlug, companyId, companyName } };
    }

    private JSONObject createDefaultSearchRequestBody() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "COMPANY");
        requestBody.put("offLimitBehavior", "bypass");
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

    private JSONObject createOwnerFilterRequestBody() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "COMPANY");
        requestBody.put("offLimitBehavior", "bypass");
        
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
        filter.put("groupType", "companies");
        filter.put("searchField", "ownerid");
        filter.put("filterType", "is");
        filter.put("entityType", "company");
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
        requestBody.put("advancedSearchContext", "COMPANY");
        requestBody.put("offLimitBehavior", behavior);
        
        // Create defaultFilterList with off-limit filter
        JSONObject defaultFilterList = new JSONObject();
        JSONObject defaultFilterListInner = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("groupType", "companies");
        filter.put("searchField", "off_limit_status_id");
        filter.put("filterType", "not_empty");
        filter.put("entityType", "company");
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
        requestBody.put("advancedSearchContext", "COMPANY");
        requestBody.put("offLimitBehavior", "bypass");
        
        // Create defaultFilterList with hotlist filter (is_empty)
        JSONObject defaultFilterList = new JSONObject();
        JSONObject defaultFilterListInner = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("groupType", "companies");
        filter.put("searchField", "hotlistid");
        filter.put("filterType", "is_empty");
        filter.put("entityType", "company");
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

    private void markCompanyAsOffLimit(String companySlug, int statusId) {
        MarkCompanyOffLimit markCompanyOffLimit = new MarkCompanyOffLimit();
        markCompanyOffLimit.setCompany_slugs(companySlug);
        markCompanyOffLimit.setStatus_id(String.valueOf(statusId));
        markCompanyOffLimit.setEnd_date(DateUtil.getTomorrowDateString());
        markCompanyOffLimit.setReason("Test off-limit reason for search test");
        markCompanyOffLimit.setMark_candidate_off_limit(false);
        markCompanyOffLimit.setMark_contact_off_limit(false);

        Response response = RestClient.doPost1("JSON", baseURL, "companies/mark-off-limit", apiAuthToken,
                null, null, false, markCompanyOffLimit);

        assertThat("Failed to mark company as off-limit", response.getStatusCode(), equalTo(200));
    }

    private Response waitForSearchResultsChange(int initialSize, JSONObject requestBody, Map<String, String> queryParameters, 
                                                String token, String companySlug, int maxRetries, long delayMs) {
        int retryCount = 0;
        
        // Initial delay to allow operation to complete
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Wait interrupted", e);
        }
        
        while (retryCount < maxRetries) {
            Response response = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                    token, queryParameters, null, true, requestBody.toString());
            
            assertThat("Response should succeed during retry", response.getStatusCode(), equalTo(200));
            JsonPath jp = response.jsonPath();
            int currentSize = jp.getInt("data.size()");
            
            // Check if size changed AND company is no longer in results
            boolean companyFound = false;
            for (int i = 0; i < currentSize; i++) {
                String resultSlug = jp.getString("data[" + i + "].slug");
                if (companySlug.equals(resultSlug)) {
                    companyFound = true;
                    break;
                }
            }
            
            // If size changed and company is not found, operation completed
            if (currentSize != initialSize && !companyFound) {
                return response;
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
        
        // Return the final response even if it didn't change (will fail assertion)
        Response finalResponse = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                token, queryParameters, null, true, requestBody.toString());
        assertThat("Final response should succeed", finalResponse.getStatusCode(), equalTo(200));
        return finalResponse;
    }
}

