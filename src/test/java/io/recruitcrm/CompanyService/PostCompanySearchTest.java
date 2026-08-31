package io.recruitcrm.CompanyService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.CompanyInheritance;
import io.rcrm.api.pojo.albatross.CompanySearchData;
import io.rcrm.api.pojo.albatross.UpdateFields;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class PostCompanySearchTest extends TestBase {

    String apiAuthToken;
    String albatrossTkn;
    JavaFakerCompany faker;
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
    
    // Synchronization object to ensure thread-safe initialization
    private static final Object INIT_LOCK = new Object();
    private static boolean customFieldsInitialized = false;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        faker = new JavaFakerCompany();
        customFieldFaker = new JavaFakerCustomField();
        commanFunction = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "companySearchData", groups = {"company_service", "nightly-build"})
    public void testCompanySearch_Success(String companySlug, int companyId, String companyName) {
        // Step 1: Search for the created company
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response searchResponse = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/companies/search/get",
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

        // Verify our created company exists in the search results
        boolean companyFound = false;
        int dataSize = (Integer) searchJp.get("data.size()");
        for (int i = 0; i < dataSize; i++) {
            String dataPath = "data[" + i + "]";
            if (searchJp.get(dataPath + ".slug").equals(companySlug)) {
                companyFound = true;
                
                // Validate company data structure
                assertThat("Company ID should match", (Integer) searchJp.get(dataPath + ".id"), equalTo(companyId));
                assertThat("Company name should match", searchJp.get(dataPath + ".companyname"), equalTo(companyName));
                assertThat("Slug should match", searchJp.get(dataPath + ".slug"), equalTo(companySlug));
                assertThat("Owner ID should not be null", searchJp.get(dataPath + ".ownerid"), notNullValue());
                assertThat("Account ID should not be null", searchJp.get(dataPath + ".accountid"), notNullValue());
                assertThat("Created by should not be null", searchJp.get(dataPath + ".createdby"), notNullValue());
                assertThat("Created on should not be null", searchJp.get(dataPath + ".createdon"), notNullValue());
                assertThat("Updated by should not be null", searchJp.get(dataPath + ".updatedby"), notNullValue());
                assertThat("Updated on should not be null", searchJp.get(dataPath + ".updatedon"), notNullValue());
                assertThat("Owner name should not be null", searchJp.get(dataPath + ".ownername"), notNullValue());
                assertThat("Creator name should not be null", searchJp.get(dataPath + ".creatorname"), notNullValue());
                assertThat("Deleted flag should be 0", (Integer) searchJp.get(dataPath + ".deleted"), equalTo(0));
                break;
            }
        }

        assertThat("Created company should be found in search results", companyFound, is(true));

        // Validate JSON schema using existing schema
        searchResponse.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/company/companySearchGet.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "companySearchDataForResponseDataValidation", groups = {"company_service", "nightly-build"})
    public void testCompanySearch_ComprehensiveDataValidation(CompanySearchData data) {

        // Step 1: Search for the company with comprehensive data
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response searchResponse = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + searchResponse.getStatusCode(),
                searchResponse.getStatusCode(), equalTo(200));

        JsonPath searchJp = searchResponse.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", searchJp.get("meta"), notNullValue());
        assertThat("Message should match expected", searchJp.get("meta.message"), equalTo("Entities retrieved successfully"));
        assertThat("Meta status should be 200", (Integer) searchJp.get("meta.status"), equalTo(200));

        // Find our company in the search results
        boolean companyFound = false;
        int dataSize = (Integer) searchJp.get("data.size()");
        for (int i = 0; i < dataSize; i++) {
            String dataPath = "data[" + i + "]";
            if (searchJp.get(dataPath + ".slug").equals(data.companySlug)) {
                companyFound = true;

                // Validate basic company data
                assertThat("Company ID should match", (Integer) searchJp.get(dataPath + ".id"), equalTo(data.companyId));
                assertThat("Company name should match", searchJp.get(dataPath + ".companyname"), equalTo(data.companyName));
                assertThat("Slug should match", searchJp.get(dataPath + ".slug"), equalTo(data.companySlug));

                // Validate company details
                assertThat("Website should match", searchJp.get(dataPath + ".website"), equalTo(data.companyWebsite));
                assertThat("City should match", searchJp.get(dataPath + ".city"), equalTo(data.companyCity));
                assertThat("Address should match", searchJp.get(dataPath + ".address"), equalTo(data.companyAddress));
                assertThat("About company should match", searchJp.get(dataPath + ".aboutcompany"), equalTo(data.companyAbout));
                assertThat("LinkedIn should match", searchJp.get(dataPath + ".profilelinkedin"), equalTo(data.companyLinkedin));
                assertThat("Facebook should match", searchJp.get(dataPath + ".profilefacebook"), equalTo(data.companyFacebook));
                assertThat("Twitter should match", searchJp.get(dataPath + ".profiletwitter"), equalTo(data.companyTwitter));
                assertThat("Logo should match", searchJp.get(dataPath + ".logoUrl"), equalTo(data.companyLogo));
                assertThat("Industry ID should match", (Integer) searchJp.get(dataPath + ".industryid"), equalTo(data.industryId));
                assertThat("Industry should match", searchJp.get(dataPath + ".industry"), equalTo(data.industry));

                // Validate parent company information
                assertThat("Parent company ID should match", (Integer) searchJp.get(dataPath + ".parentcompanyid"), notNullValue());
                assertThat("Parent company slug should match", searchJp.get(dataPath + ".parentcompanyslug"), equalTo(data.parentCompanySlug));
                assertThat("Parent company name should match", searchJp.get(dataPath + ".parentcompanyname"), equalTo(data.parentCompanyName));
                assertThat("Parent city should match", searchJp.get(dataPath + ".parentCity"), equalTo(data.parentCompanyCity));
                assertThat("Parent website should match", searchJp.get(dataPath + ".parentWebSite"), equalTo(data.parentCompanyWebsite));
                assertThat("Parent logo should match", searchJp.get(dataPath + ".parentLogo"), equalTo(data.parentCompanyLogo));
                assertThat("Parent industry should match", searchJp.get(dataPath + ".parentIndustryName"), equalTo(data.parentIndustryName));

                // Validate parent-child relationship flags
                assertThat("Has parent should be 1", (Integer) searchJp.get(dataPath + ".hasparent"), equalTo(1));
                assertThat("Has children should be 0", (Integer) searchJp.get(dataPath + ".haschildren"), equalTo(0));

                // Validate system fields
                assertThat("Owner ID should not be null", searchJp.get(dataPath + ".ownerid"), notNullValue());
                assertThat("Account ID should not be null", searchJp.get(dataPath + ".accountid"), notNullValue());
                assertThat("Created by should not be null", searchJp.get(dataPath + ".createdby"), notNullValue());
                assertThat("Created on should not be null", searchJp.get(dataPath + ".createdon"), notNullValue());
                assertThat("Updated by should not be null", searchJp.get(dataPath + ".updatedby"), notNullValue());
                assertThat("Updated on should not be null", searchJp.get(dataPath + ".updatedon"), notNullValue());
                assertThat("Owner name should not be null", searchJp.get(dataPath + ".ownername"), notNullValue());
                assertThat("Creator name should not be null", searchJp.get(dataPath + ".creatorname"), notNullValue());
                assertThat("Deleted flag should be 0", (Integer) searchJp.get(dataPath + ".deleted"), equalTo(0));

                // Validate contact linked status
                assertThat("Contact linked should be Yes", searchJp.get(dataPath + ".contact_linked"), equalTo("Yes"));

                // Validate job counts
                assertThat("Total open jobs should match", (Integer) searchJp.get(dataPath + ".totalopenjob"), equalTo(data.totalOpenJobs));
                assertThat("Total closed jobs should match", (Integer) searchJp.get(dataPath + ".totalclosedjob"), equalTo(data.totalClosedJobs));
                assertThat("Total on hold jobs should match", (Integer) searchJp.get(dataPath + ".totalonholdjob"), equalTo(data.totalOnHoldJobs));
                assertThat("Total canceled jobs should match", (Integer) searchJp.get(dataPath + ".totalcanceledjob"), equalTo(data.totalCanceledJobs));

                break;
            }
        }

        assertThat("Created company should be found in search results", companyFound, is(true));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "companySearchData", groups = {"company_service", "nightly-build"})
    public void testCompanySearch_Workflow(String companySlug, int companyId, String companyName) {
        // Step 1: Search to confirm company exists
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response searchResponse = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        assertThat("Search should succeed", searchResponse.getStatusCode(), equalTo(200));
        
        JsonPath searchJp = searchResponse.jsonPath();
        int initialCount = (Integer) searchJp.get("data.size()");
        assertThat("Should have companies before delete", initialCount, greaterThan(0));

        // Step 2: Delete the company
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("company", companySlug);
        String basePath = "companies/{company}";

        Response deleteResponse = RestClient.doDelete("JSON", albatrossURL, basePath, albatrossTkn, null, pathParameters, true);
        assertThat("Delete should succeed", deleteResponse.getStatusCode(), equalTo(200));

        // Step 3: Search again to verify deletion
        Response searchAfterDeleteResponse = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        assertThat("Search after delete should succeed", searchAfterDeleteResponse.getStatusCode(), equalTo(200));

        JsonPath searchAfterDeleteJp = searchAfterDeleteResponse.jsonPath();

        // Should have fewer companies or the deleted company should not be found
        boolean companyStillExists = false;
        int dataSize = (Integer) searchAfterDeleteJp.get("data.size()");
        for (int i = 0; i < dataSize; i++) {
            String dataPath = "data[" + i + "]";
            if (searchAfterDeleteJp.get(dataPath + ".slug").equals(companySlug)) {
                companyStillExists = true;
                break;
            }
        }

        assertThat("Deleted company should not exist in search results", companyStillExists, is(false));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanySearch_WithoutAuth() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response response = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                null, queryParameters, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanySearch_InvalidAuth() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response response = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn + "invalid-token-123", queryParameters, null, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanySearch_EmptyRequestBody() {
        JSONObject requestBody = new JSONObject();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response response = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanySearch_InvalidQueryParameters() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "invalid-page");
        queryParameters.put("size", "invalid-size");

        Response response = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        // Should handle invalid parameters gracefully or return error
        assertThat("Response should be handled appropriately", 
                response.getStatusCode(), anyOf(equalTo(200), equalTo(400), equalTo(422)));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testCompanySearch_MissingQueryParameters() {
        JSONObject requestBody = createDefaultSearchRequestBody();

        Response response = RestClient.doPost1("JSON", ariesServiceURL, "advanced-search/companies/search/get",
                albatrossTkn, null, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(200));

        JsonPath searchJp = response.jsonPath();

        assertThat("Meta object should not be null", searchJp.get("meta"), notNullValue());
        assertThat("Message should match expected", searchJp.get("meta.message"), equalTo("Entities retrieved successfully"));

    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getTextSortData", groups = {"sorting", "company_service", "nightly-build"})
    public void searchCompany_SortByTextField(String sortField, String jsonPath, String sortOrder, int statusCode) {
        Map<String, String> queryParameters = createDefaultQueryParameters();

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = new JSONObject();
        searchPayload.put("advancedSearchContext", "COMPANY");
        searchPayload.put("offLimitBehavior", "bypass");
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("defaultFilterList", JSONObject.NULL);
        searchPayload.put("filterSearchList", JSONObject.NULL);
        searchPayload.put("booleanSearchList", JSONObject.NULL);

        Response response = RestClient.doPost("JSON", ariesServiceURL,
                "advanced-search/companies/search/get", albatrossTkn, queryParameters, true, searchPayload);

        String testCase = "searchCompany_SortByTextField - Field: " + sortField + ", Order: " + sortOrder;
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
    @Test(dataProvider = "getNumericSortData", groups = {"sorting", "company_service", "nightly-build"})
    public void searchCompany_SortByNumericField(String sortField, String jsonPath, String sortOrder, int statusCode) {
        Map<String, String> queryParameters = createDefaultQueryParameters();

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = new JSONObject();
        searchPayload.put("advancedSearchContext", "COMPANY");
        searchPayload.put("offLimitBehavior", "bypass");
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("defaultFilterList", JSONObject.NULL);
        searchPayload.put("filterSearchList", JSONObject.NULL);
        searchPayload.put("booleanSearchList", JSONObject.NULL);

        Response response = RestClient.doPost("JSON", ariesServiceURL,
                "advanced-search/companies/search/get", albatrossTkn, queryParameters, true, searchPayload);

        String testCase = "searchCompany_SortByNumericField - Field: " + sortField + ", Order: " + sortOrder;
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
    @Test(dataProvider = "getDateSortData", groups = {"sorting", "company_service", "nightly-build"})
    public void searchCompany_SortByDateField(String sortField, String jsonPath, String sortOrder, int statusCode) {
        Map<String, String> queryParameters = createDefaultQueryParameters();

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = new JSONObject();
        searchPayload.put("advancedSearchContext", "COMPANY");
        searchPayload.put("offLimitBehavior", "bypass");
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("defaultFilterList", JSONObject.NULL);
        searchPayload.put("filterSearchList", JSONObject.NULL);
        searchPayload.put("booleanSearchList", JSONObject.NULL);

        Response response = RestClient.doPost("JSON", ariesServiceURL,
                "advanced-search/companies/search/get", albatrossTkn, queryParameters, true, searchPayload);

        String testCase = "searchCompany_SortByDateField - Field: " + sortField + ", Order: " + sortOrder;
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
    @Test(dataProvider = "getCustomFieldTextSortData", groups = {"customFieldSorting", "company_service", "nightly-build"})
    public void searchCompany_SortByCustomTextField(String sortField, String sortOrder, int statusCode) {
        Map<String, String> queryParameters = createDefaultQueryParameters();

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = new JSONObject();
        searchPayload.put("advancedSearchContext", "COMPANY");
        searchPayload.put("offLimitBehavior", "bypass");
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("defaultFilterList", JSONObject.NULL);
        searchPayload.put("filterSearchList", JSONObject.NULL);
        searchPayload.put("booleanSearchList", JSONObject.NULL);

        Response response = RestClient.doPost("JSON", ariesServiceURL,
                "advanced-search/companies/search/get", albatrossTkn, queryParameters, true, searchPayload);

        String testCase = "searchCompany_SortByCustomTextField - Field: " + sortField + ", Order: " + sortOrder;
        assertThat(testCase + " - Expected status code " + statusCode,
                response.getStatusCode(), equalTo(statusCode));

        // Extract custom field values from response
        List<String> values = commanFunction.extractCustomFieldValues(response, sortField);

        if (sortOrder.equals("asc")) {
            assertThat(testCase + " - " + sortField + " not sorted ascending",
                    commanFunction.isSortedAscendingText(values), is(true));
        } else {
            assertThat(testCase + " - " + sortField + " not sorted descending",
                    commanFunction.isSortedDescendingText(values), is(true));
        }
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getCustomFieldNumericSortData", groups = {"customFieldSorting", "company_service", "nightly-build"})
    public void searchCompany_SortByCustomNumericField(String sortField, String sortOrder, int statusCode) {
        Map<String, String> queryParameters = createDefaultQueryParameters();

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = new JSONObject();
        searchPayload.put("advancedSearchContext", "COMPANY");
        searchPayload.put("offLimitBehavior", "bypass");
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("defaultFilterList", JSONObject.NULL);
        searchPayload.put("filterSearchList", JSONObject.NULL);
        searchPayload.put("booleanSearchList", JSONObject.NULL);

        Response response = RestClient.doPost("JSON", ariesServiceURL,
                "advanced-search/companies/search/get", albatrossTkn, queryParameters, true, searchPayload);

        String testCase = "searchCompany_SortByCustomNumericField - Field: " + sortField + ", Order: " + sortOrder;
        assertThat(testCase + " - Expected status code " + statusCode,
                response.getStatusCode(), equalTo(statusCode));

        // Extract custom field values from response
        List<Number> values = commanFunction.extractCustomFieldNumericValues(response, sortField);

        if (sortOrder.equals("asc")) {
            assertThat(testCase + " - " + sortField + " not sorted ascending",
                    commanFunction.isSortedAscendingNumeric(values), is(true));
        } else {
            assertThat(testCase + " - " + sortField + " not sorted descending",
                    commanFunction.isSortedDescendingNumeric(values), is(true));
        }
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getCustomFieldDateSortData", groups = {"customFieldSorting", "company_service", "nightly-build"})
    public void searchCompany_SortByCustomDateField(String sortField, String sortOrder, int statusCode) {
        Map<String, String> queryParameters = createDefaultQueryParameters();

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = new JSONObject();
        searchPayload.put("advancedSearchContext", "COMPANY");
        searchPayload.put("offLimitBehavior", "bypass");
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("defaultFilterList", JSONObject.NULL);
        searchPayload.put("filterSearchList", JSONObject.NULL);
        searchPayload.put("booleanSearchList", JSONObject.NULL);

        Response response = RestClient.doPost("JSON", ariesServiceURL,
                "advanced-search/companies/search/get", albatrossTkn, queryParameters, true, searchPayload);

        String testCase = "searchCompany_SortByCustomDateField - Field: " + sortField + ", Order: " + sortOrder;
        assertThat(testCase + " - Expected status code " + statusCode,
                response.getStatusCode(), equalTo(statusCode));

        // Extract custom field values from response
        List<Object> values = commanFunction.extractCustomFieldDateValues(response, sortField);

        if (sortOrder.equals("asc")) {
            assertThat(testCase + " - " + sortField + " not sorted ascending",
                    commanFunction.isSortedAscendingDate(values), is(true));
        } else {
            assertThat(testCase + " - " + sortField + " not sorted descending",
                    commanFunction.isSortedDescendingDate(values), is(true));
        }
    }

    private void ensureCustomFieldsInitialized() {
        if (!customFieldsInitialized) {
            synchronized (INIT_LOCK) {
                if (!customFieldsInitialized) {
                    try {
                        // Read JSON file first
                        String filePath = "src/test/resources/companyCreationDataProvider.json";
                        String content = new String(Files.readAllBytes(Paths.get(filePath)));
                        JSONObject jsonObject = new JSONObject(content);
                        JSONArray companiesArray = jsonObject.getJSONArray("companies");
                        
                        // Get custom field types from the first company's customFields
                        JSONObject firstCompany = companiesArray.getJSONObject(0);
                        JSONObject companyObj = firstCompany.getJSONObject("company");
                        JSONObject customFields = companyObj.optJSONObject("customFields");
                        
                        if (customFields == null) {
                            throw new AssertionError("No customFields found in the first company");
                        }
                        
                        // Create custom fields and get their column IDs
                        ExecutorService customFieldExecutor = Executors.newFixedThreadPool(11);
                        try {
                            // Create all custom field futures first without blocking
                            Map<String, CompletableFuture<Integer>> futures = new HashMap<>();
                            
                            if (customFields.has("text")) {
                                futures.put("text", CompletableFuture.supplyAsync(() -> {
                                    Response response = commanFunction.createCustomFieldsResponse(albatrossURL, albatrossTkn, "company", "text", "text", "");
                                    return response.jsonPath().getInt("data.custumField.columnid");
                                }, customFieldExecutor));
                            }
                            
                            if (customFields.has("number")) {
                                futures.put("number", CompletableFuture.supplyAsync(() -> {
                                    Response response = commanFunction.createCustomFieldsResponse(albatrossURL, albatrossTkn, "company", "number", "number", "");
                                    return response.jsonPath().getInt("data.custumField.columnid");
                                }, customFieldExecutor));
                            }
                            
                            if (customFields.has("date")) {
                                futures.put("date", CompletableFuture.supplyAsync(() -> {
                                    Response response = commanFunction.createCustomFieldsResponse(albatrossURL, albatrossTkn, "company", "date", "date", "");
                                    return response.jsonPath().getInt("data.custumField.columnid");
                                }, customFieldExecutor));
                            }
                            
                            if (customFields.has("date_time")) {
                                futures.put("date_time", CompletableFuture.supplyAsync(() -> {
                                    Response response = commanFunction.createCustomFieldsResponse(albatrossURL, albatrossTkn, "company", "date_time", "date_time", "");
                                    return response.jsonPath().getInt("data.custumField.columnid");
                                }, customFieldExecutor));
                            }
                            
                            if (customFields.has("longtext")) {
                                futures.put("longtext", CompletableFuture.supplyAsync(() -> {
                                    Response response = commanFunction.createCustomFieldsResponse(albatrossURL, albatrossTkn, "company", "longtext", "longtext", "");
                                    return response.jsonPath().getInt("data.custumField.columnid");
                                }, customFieldExecutor));
                            }
                            
                            if (customFields.has("phonenumber")) {
                                futures.put("phonenumber", CompletableFuture.supplyAsync(() -> {
                                    Response response = commanFunction.createCustomFieldsResponse(albatrossURL, albatrossTkn, "company", "phonenumber", "phonenumber", "");
                                    return response.jsonPath().getInt("data.custumField.columnid");
                                }, customFieldExecutor));
                            }
                            
                            if (customFields.has("dropdown")) {
                                futures.put("dropdown", CompletableFuture.supplyAsync(() -> {
                                    Response response = commanFunction.createCustomFieldsResponse(albatrossURL, albatrossTkn, "company", "dropdown", "dropdown", "Alpha, Beta, Gamma, Delta, Epsilon");
                                    return response.jsonPath().getInt("data.custumField.columnid");
                                }, customFieldExecutor));
                            }
                            
                            if (customFields.has("multiselect")) {
                                futures.put("multiselect", CompletableFuture.supplyAsync(() -> {
                                    Response response = commanFunction.createCustomFieldsResponse(albatrossURL, albatrossTkn, "company", "multiselect", "multiselect", "Option1, Option2, Option3, Option4, Option5");
                                    return response.jsonPath().getInt("data.custumField.columnid");
                                }, customFieldExecutor));
                            }
                            
                            if (customFields.has("checkbox")) {
                                futures.put("checkbox", CompletableFuture.supplyAsync(() -> {
                                    Response response = commanFunction.createCustomFieldsResponse(albatrossURL, albatrossTkn, "company", "checkbox", "checkbox", "");
                                    return response.jsonPath().getInt("data.custumField.columnid");
                                }, customFieldExecutor));
                            }
                            
                            if (customFields.has("file")) {
                                futures.put("file", CompletableFuture.supplyAsync(() -> {
                                    Response response = commanFunction.createCustomFieldsResponse(albatrossURL, albatrossTkn, "company", "file", "file", "");
                                    return response.jsonPath().getInt("data.custumField.columnid");
                                }, customFieldExecutor));
                            }
                            
                            if (customFields.has("social_profile")) {
                                futures.put("social_profile", CompletableFuture.supplyAsync(() -> {
                                    Response response = commanFunction.createCustomFieldsResponse(albatrossURL, albatrossTkn, "company", "social_profile", "social_profile", "");
                                    return response.jsonPath().getInt("data.custumField.columnid");
                                }, customFieldExecutor));
                            }
                            
                            // Wait for all futures to complete and assign values
                            if (futures.containsKey("text")) {
                                textCustomFieldColumnId = futures.get("text").join();
                            }
                            if (futures.containsKey("number")) {
                                numberCustomFieldColumnId = futures.get("number").join();
                            }
                            if (futures.containsKey("date")) {
                                dateCustomFieldColumnId = futures.get("date").join();
                            }
                            if (futures.containsKey("date_time")) {
                                dateTimeCustomFieldColumnId = futures.get("date_time").join();
                            }
                            if (futures.containsKey("longtext")) {
                                longTextCustomFieldColumnId = futures.get("longtext").join();
                            }
                            if (futures.containsKey("phonenumber")) {
                                phoneNumberCustomFieldColumnId = futures.get("phonenumber").join();
                            }
                            if (futures.containsKey("dropdown")) {
                                dropdownCustomFieldColumnId = futures.get("dropdown").join();
                            }
                            if (futures.containsKey("multiselect")) {
                                multiselectCustomFieldColumnId = futures.get("multiselect").join();
                            }
                            if (futures.containsKey("checkbox")) {
                                checkboxCustomFieldColumnId = futures.get("checkbox").join();
                            }
                            if (futures.containsKey("file")) {
                                fileCustomFieldColumnId = futures.get("file").join();
                            }
                            if (futures.containsKey("social_profile")) {
                                socialProfileCustomFieldColumnId = futures.get("social_profile").join();
                            }
                        } finally {
                            customFieldExecutor.shutdown();
                        }
                        
                        customFieldsInitialized = true;
                    } catch (Exception e) {
                        throw new AssertionError("Failed to initialize custom field column IDs: " + e.getMessage(), e);
                    }
                }
            }
        }
    }
    
    @BeforeGroups(groups = "customFieldSorting")
    public void createTestDataForCustomFieldSorting() {
        try {
            // Ensure custom fields are initialized first
            ensureCustomFieldsInitialized();
            
            // Read JSON file
            String filePath = "src/test/resources/companyCreationDataProvider.json";
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject jsonObject = new JSONObject(content);
            JSONArray companiesArray = jsonObject.getJSONArray("companies");
            
            // Now create companies similar to normal sorting script
            ExecutorService executor = Executors.newFixedThreadPool(5);
            try {
                CompletableFuture.allOf(IntStream.range(0, companiesArray.length())
                        .mapToObj(i -> CompletableFuture.runAsync(() -> {
                            try {
                                JSONObject companyData = companiesArray.getJSONObject(i);
                                JSONObject companyObjInner = companyData.getJSONObject("company");
                                JSONObject companyCustomFields = companyObjInner.optJSONObject("customFields");
                                
                                // Remove customFields from company object as they will be updated separately
                                companyObjInner.remove("customFields");
                                
                                // Build payload with nested structure
                                JSONObject companyPayload = new JSONObject();
                                companyPayload.put("company", companyObjInner);
                                companyPayload.put("address_changed", companyData.optBoolean("address_changed", true));
                                companyPayload.put("contact", companyData.optJSONObject("contact", new JSONObject()));
                                
                                // Create company
                                Response response = RestClient.doPost("JSON", albatrossURL, "companies",
                                        albatrossTkn, null, true, companyPayload);
                                
                                assertThat("createTestDataForCustomFieldSorting - Company creation failed for company at index " + i + 
                                        " - Expected status code 200, but got " + response.getStatusCode(),
                                        response.getStatusCode(), equalTo(200));
                                
                                String companySlug = response.jsonPath().get("data.company.slug");
                                assertThat("Company slug should not be null", companySlug, notNullValue());
                                
                                // Update custom fields if they exist
                                if (companyCustomFields != null) {
                                    JsonPath companyJp = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTkn, companySlug).jsonPath();
                                    int companyId = companyJp.getInt("data.company.id");
                                    
                                    if (companyCustomFields.has("text")) {
                                        updateCompanyCustomField(companyId, "custcolumn" + textCustomFieldColumnId, 
                                            companyCustomFields.getString("text"));
                                    }
                                    if (companyCustomFields.has("number")) {
                                        updateCompanyCustomField(companyId, "custcolumn" + numberCustomFieldColumnId, 
                                            companyCustomFields.getString("number"));
                                    }
                                    if (companyCustomFields.has("date")) {
                                        updateCompanyCustomField(companyId, "custcolumn" + dateCustomFieldColumnId, 
                                            companyCustomFields.getString("date"));
                                    }
                                    if (companyCustomFields.has("date_time")) {
                                        updateCompanyCustomField(companyId, "custcolumn" + dateTimeCustomFieldColumnId, 
                                            companyCustomFields.getString("date_time"));
                                    }
                                    if (companyCustomFields.has("longtext")) {
                                        updateCompanyCustomField(companyId, "custcolumn" + longTextCustomFieldColumnId, 
                                            companyCustomFields.getString("longtext"));
                                    }
                                    if (companyCustomFields.has("phonenumber")) {
                                        updateCompanyCustomField(companyId, "custcolumn" + phoneNumberCustomFieldColumnId, 
                                            companyCustomFields.getString("phonenumber"));
                                    }
                                    if (companyCustomFields.has("dropdown")) {
                                        updateCompanyCustomField(companyId, "custcolumn" + dropdownCustomFieldColumnId, 
                                            companyCustomFields.getString("dropdown"));
                                    }
                                    if (companyCustomFields.has("multiselect")) {
                                        updateCompanyCustomField(companyId, "custcolumn" + multiselectCustomFieldColumnId, 
                                            companyCustomFields.getString("multiselect"));
                                    }
                                    if (companyCustomFields.has("checkbox")) {
                                        updateCompanyCustomField(companyId, "custcolumn" + checkboxCustomFieldColumnId, 
                                            companyCustomFields.getString("checkbox"));
                                    }
                                    if (companyCustomFields.has("file")) {
                                        updateCompanyCustomField(companyId, "custcolumn" + fileCustomFieldColumnId, 
                                            companyCustomFields.getString("file"));
                                    }
                                    if (companyCustomFields.has("social_profile")) {
                                        updateCompanyCustomField(companyId, "custcolumn" + socialProfileCustomFieldColumnId, 
                                            companyCustomFields.getString("social_profile"));
                                    }
                                }
                            } catch (Exception e) {
                                throw new AssertionError("Failed to create company at index " + i + ": " + e.getMessage(), e);
                            }
                        }, executor))
                        .toArray(CompletableFuture[]::new)).join();
            } finally {
                executor.shutdown();
            }
        } catch (IOException e) {
            throw new AssertionError("Failed to read companyCreationDataProvider.json file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new AssertionError("Failed to create test data for custom field sorting: " + e.getMessage(), e);
        }
    }
    
    private void updateCompanyCustomField(int companyId, String fieldKey, String fieldValue) {
        List<Integer> entityIds = Arrays.asList(companyId);
        UpdateFields updateFields = new UpdateFields();
        updateFields.setKey(fieldKey);
        updateFields.setValue(fieldValue);
        updateFields.setTableFlag("company");
        updateFields.setId(entityIds);
        
        Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossTkn, null, true, updateFields);
        assertThat("Failed to update custom field " + fieldKey + " for company " + companyId,
                response.getStatusCode(), equalTo(200));
    }

    @BeforeGroups(groups = "sorting")
    public void createTestDataForSorting() {
        try {
            // Read JSON file
            String filePath = "src/test/resources/companyCreationDataProvider.json";
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject jsonObject = new JSONObject(content);
            
            // Get companies array from JSON
            JSONArray companiesArray = jsonObject.getJSONArray("companies");
            
            ExecutorService executor = Executors.newFixedThreadPool(5);
            try {
                // Create companies in parallel
                CompletableFuture.allOf(IntStream.range(0, companiesArray.length())
                        .mapToObj(i -> CompletableFuture.runAsync(() -> {
                            try {
                                JSONObject companyData = companiesArray.getJSONObject(i);
                                JSONObject companyObj = companyData.getJSONObject("company");
                                
                                // Build payload with nested structure
                                JSONObject companyPayload = new JSONObject();
                                companyPayload.put("company", companyObj);
                                companyPayload.put("address_changed", companyData.optBoolean("address_changed", true));
                                companyPayload.put("contact", companyData.optJSONObject("contact", new JSONObject()));
                                
                                // Create company using RestClient directly
                                Response response = RestClient.doPost("JSON", albatrossURL, "companies",
                                        albatrossTkn, null, true, companyPayload);
                                
                                assertThat("createTestDataForSorting - Company creation failed for company at index " + i + 
                                        " - Expected status code 200, but got " + response.getStatusCode(),
                                        response.getStatusCode(), equalTo(200));
                                
                                String companySlug = response.jsonPath().get("data.company.slug");
                                assertThat("Company slug should not be null", companySlug, notNullValue());
                            } catch (Exception e) {
                                throw new AssertionError("Failed to create company at index " + i + ": " + e.getMessage(), e);
                            }
                        }, executor))
                        .toArray(CompletableFuture[]::new)).join();
            } finally {
                executor.shutdown();
            }
        } catch (IOException e) {
            throw new AssertionError("Failed to read companyCreationDataProvider.json file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new AssertionError("Failed to create test data for sorting: " + e.getMessage(), e);
        }
    }

    private JSONObject createDefaultSearchRequestBody() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "COMPANY");
        requestBody.put("offLimitBehavior", "bypass");
        requestBody.put("defaultFilterList", JSONObject.NULL);
        requestBody.put("filterSearchList", JSONObject.NULL);
        requestBody.put("booleanSearchList", JSONObject.NULL);
        requestBody.put("sortPriorityList", new Object[]{});
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
                {"companyname", "data.companyname", "asc", 200},
                {"aboutcompany", "data.aboutcompany", "asc", 200},
                {"parentcompanyname", "data.parentcompanyname", "asc", 200},
                {"address", "data.address", "asc", 200},
                {"city", "data.city", "asc", 200},
                {"industry", "data.industry", "asc", 200},
                {"website", "data.website", "asc", 200},
                {"locality", "data.locality", "asc", 200},
                {"state", "data.state", "asc", 200},
                {"country", "data.country", "asc", 200},
                {"postal_code", "data.postal_code", "asc", 200},
                {"creatorname", "data.creatorname", "asc", 200},
                {"ownername", "data.ownername", "asc", 200},
                {"profilefacebook", "data.profilefacebook", "asc", 200},
                {"profiletwitter", "data.profiletwitter", "asc", 200},
                {"profilelinkedin", "data.profilelinkedin", "asc", 200},

                // Text fields - descending
                {"companyname", "data.companyname", "desc", 200},
                {"aboutcompany", "data.aboutcompany", "desc", 200},
                {"parentcompanyname", "data.parentcompanyname", "desc", 200},
                {"address", "data.address", "desc", 200},
                {"city", "data.city", "desc", 200},
                {"industry", "data.industry", "desc", 200},
                {"website", "data.website", "desc", 200},
                {"locality", "data.locality", "desc", 200},
                {"state", "data.state", "desc", 200},
                {"country", "data.country", "desc", 200},
                {"postal_code", "data.postal_code", "desc", 200},
                {"creatorname", "data.creatorname", "desc", 200},
                {"ownername", "data.ownername", "desc", 200},
                {"profilefacebook", "data.profilefacebook", "desc", 200},
                {"profiletwitter", "data.profiletwitter", "desc", 200},
                {"profilelinkedin", "data.profilelinkedin", "desc", 200},
        };
    }

    @DataProvider(name = "getNumericSortData", parallel = true)
    public static Object[][] getNumericSortData() {
        return new Object[][]{
                // Numeric fields - ascending
                {"id", "data.id", "asc", 200},
                {"srno", "data.srno", "asc", 200},
                {"totalopenjob", "data.totalopenjob", "asc", 200},
                {"totalclosedjob", "data.totalclosedjob", "asc", 200},
                {"totalonholdjob", "data.totalonholdjob", "asc", 200},
                {"totalcanceledjob", "data.totalcanceledjob", "asc", 200},
                {"ownerid", "data.ownerid", "asc", 200},
                {"accountid", "data.accountid", "asc", 200},
                {"createdby", "data.createdby", "asc", 200},
                {"updatedby", "data.updatedby", "asc", 200},
                {"industryid", "data.industryid", "asc", 200},
                {"parentcompanyid", "data.parentcompanyid", "asc", 200},

                // Numeric fields - descending
                {"id", "data.id", "desc", 200},
                {"srno", "data.srno", "desc", 200},
                {"totalopenjob", "data.totalopenjob", "desc", 200},
                {"totalclosedjob", "data.totalclosedjob", "desc", 200},
                {"totalonholdjob", "data.totalonholdjob", "desc", 200},
                {"totalcanceledjob", "data.totalcanceledjob", "desc", 200},
                {"ownerid", "data.ownerid", "desc", 200},
                {"accountid", "data.accountid", "desc", 200},
                {"createdby", "data.createdby", "desc", 200},
                {"updatedby", "data.updatedby", "desc", 200},
                {"industryid", "data.industryid", "desc", 200},
                {"parentcompanyid", "data.parentcompanyid", "desc", 200}
        };
    }

    @DataProvider(name = "getDateSortData", parallel = true)
    public static Object[][] getDateSortData() {
        return new Object[][]{
                // Date fields - ascending
                {"createdon", "data.createdon", "asc", 200},
                {"updatedon", "data.updatedon", "asc", 200},
                {"last_meeting_created_on", "data.last_meeting_created_on", "asc", 200},

                // Date fields - descending
                {"createdon", "data.createdon", "desc", 200},
                {"updatedon", "data.updatedon", "desc", 200},
                {"last_meeting_created_on", "data.last_meeting_created_on", "desc", 200}
        };
    }

    @DataProvider(name = "getCustomFieldTextSortData", parallel = true)
    public Object[][] getCustomFieldTextSortData() {
        // Ensure custom fields are initialized (lazy initialization for data provider)
        ensureCustomFieldsInitialized();
        return new Object[][]{
                // Text custom fields - ascending
                {"custcolumn" + textCustomFieldColumnId, "asc", 200},
                {"custcolumn" + longTextCustomFieldColumnId, "asc", 200},
                {"custcolumn" + phoneNumberCustomFieldColumnId, "asc", 200},
                {"custcolumn" + dropdownCustomFieldColumnId, "asc", 200},
                {"custcolumn" + multiselectCustomFieldColumnId, "asc", 200},
                {"custcolumn" + fileCustomFieldColumnId, "asc", 200},
                {"custcolumn" + socialProfileCustomFieldColumnId, "asc", 200},

                // Text custom fields - descending
                {"custcolumn" + textCustomFieldColumnId, "desc", 200},
                {"custcolumn" + longTextCustomFieldColumnId, "desc", 200},
                {"custcolumn" + phoneNumberCustomFieldColumnId, "desc", 200},
                {"custcolumn" + dropdownCustomFieldColumnId, "desc", 200},
                {"custcolumn" + multiselectCustomFieldColumnId, "desc", 200},
                {"custcolumn" + fileCustomFieldColumnId, "desc", 200},
                {"custcolumn" + socialProfileCustomFieldColumnId, "desc", 200}
        };
    }

    @DataProvider(name = "getCustomFieldNumericSortData", parallel = true)
    public Object[][] getCustomFieldNumericSortData() {
        // Ensure custom fields are initialized (lazy initialization for data provider)
        ensureCustomFieldsInitialized();
        return new Object[][]{
                // Numeric custom fields - ascending
                {"custcolumn" + numberCustomFieldColumnId, "asc", 200},
                {"custcolumn" + checkboxCustomFieldColumnId, "asc", 200},

                // Numeric custom fields - descending
                {"custcolumn" + numberCustomFieldColumnId, "desc", 200},
                {"custcolumn" + checkboxCustomFieldColumnId, "desc", 200}
        };
    }

    @DataProvider(name = "getCustomFieldDateSortData", parallel = true)
    public Object[][] getCustomFieldDateSortData() {
        // Ensure custom fields are initialized (lazy initialization for data provider)
        ensureCustomFieldsInitialized();
        return new Object[][]{
                // Date custom fields - ascending
                {"custcolumn" + dateCustomFieldColumnId, "asc", 200},
                {"custcolumn" + dateTimeCustomFieldColumnId, "asc", 200},

                // Date custom fields - descending
                {"custcolumn" + dateCustomFieldColumnId, "desc", 200},
                {"custcolumn" + dateTimeCustomFieldColumnId, "desc", 200}
        };
    }

    @DataProvider(name = "companySearchData")
    public Object[][] getCompanySearchData() {
        // Create test company using Albatross
        String companySlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "company");

        JsonPath jp = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTkn, companySlug).jsonPath();
        int companyId = jp.get("data.company.id");
        String companyName = jp.get("data.company.companyname");
        
        assertThat("Company slug should not be null", companySlug, notNullValue());
        assertThat("Company ID should not be null", companyId, notNullValue());
        assertThat("Company name should not be null", companyName, notNullValue());
        
        return new Object[][] { { companySlug, companyId, companyName } };
    }

    @DataProvider(name = "companySearchDataForResponseDataValidation", parallel = true)
    public Object[][] getCompanySearchDataForResponseDataValidation() {
        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            CompletableFuture<Response> parentCompanyFuture = CompletableFuture.supplyAsync(() ->
                    commanFunction.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken), executor);

            CompletableFuture<Response> childCompanyFuture = CompletableFuture.supplyAsync(() ->
                    commanFunction.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken), executor);

            String parentCompanySlug = parentCompanyFuture.thenApply(r -> r.jsonPath().getString("slug")).join();
            String childCompanySlug = childCompanyFuture.thenApply(r -> r.jsonPath().getString("slug")).join();

            List<String> childCompanies = new ArrayList<>();
            childCompanies.add(childCompanySlug);

            CompanyInheritance companyInheritance = new CompanyInheritance();
            companyInheritance.setChild_company_slugs(childCompanies);
            companyInheritance.setParent_company_slug(parentCompanySlug);

            String basePath = "companies/link-to-parent-company";
            Response linkResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, companyInheritance);
            assertThat("Parent-child linking should succeed", linkResponse.getStatusCode(), equalTo(200));

            CompletableFuture<Response> contactFuture = CompletableFuture.supplyAsync(() ->
                    commanFunction.createNewContact_POST(baseURL, apiAuthToken, childCompanySlug), executor);

            CompletableFuture<JsonPath> childCompanyJpFuture = CompletableFuture.supplyAsync(() ->
                    allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTkn, childCompanySlug).jsonPath(), executor);

            CompletableFuture<JsonPath> parentCompanyJpFuture = CompletableFuture.supplyAsync(() ->
                    allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTkn, parentCompanySlug).jsonPath(), executor);

            CompletableFuture<Map<String, Integer>> jobStatusMapFuture = CompletableFuture.supplyAsync(() ->
                    commanFunction.getJobStatusValues(albatrossURL, albatrossTkn), executor);

            String contactSlug = contactFuture.thenApply(r -> r.jsonPath().getString("slug")).join();

            JsonPath jp = childCompanyJpFuture.join();
            int companyId = jp.get("data.company.id");
            String companyName = jp.get("data.company.companyname");
            String companyWebsite = jp.get("data.company.website");
            String companyCity = jp.get("data.company.city");
            String companyAddress = jp.get("data.company.address");
            String companyAbout = jp.get("data.company.aboutcompany");
            String companyLinkedin = jp.get("data.company.profilelinkedin");
            String companyFacebook = jp.get("data.company.profilefacebook");
            String companyTwitter = jp.get("data.company.profiletwitter");
            String companyLogo = jp.get("data.company.logoUrl");
            int industryId = jp.get("data.company.industryid");
            String industry = jp.get("data.company.industry");

            JsonPath parentJp = parentCompanyJpFuture.join();
            String parentCompanyName = parentJp.get("data.company.companyname");
            String parentCompanyCity = parentJp.get("data.company.city");
            String parentCompanyWebsite = parentJp.get("data.company.website");
            String parentCompanyLogo = parentJp.get("data.company.logoUrl");
            String parentIndustryName = parentJp.get("data.company.industry");

            JsonPath contactJp = allCrudFunctions.getContactResponse(albatrossURL, albatrossTkn, contactSlug).jsonPath();
            String contactFirstName = contactJp.get("data.contact.first_name");
            String contactLastName = contactJp.get("data.contact.last_name");
            String contactEmail = contactJp.get("data.contact.email");

            Map<String, Integer> jobStatusMap = jobStatusMapFuture.join();

            CompletableFuture<Integer> openJobsFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    Response jobResp = commanFunction.createNewJob(baseURL, apiAuthToken, childCompanySlug, contactSlug);
                    String jobSlug = jobResp.jsonPath().get("slug");
                    Integer statusId = jobStatusMap.get("Open");
                    if (statusId != null) {
                        commanFunction.updateJobStatus(albatrossURL, albatrossTkn, jobSlug, statusId);
                        return 1;
                    }
                } catch (Exception ignored) {}
                return 0;
            }, executor);

            CompletableFuture<Integer> closedJobsFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    Response jobResp = commanFunction.createNewJob(baseURL, apiAuthToken, childCompanySlug, contactSlug);
                    String jobSlug = jobResp.jsonPath().get("slug");
                    Integer statusId = jobStatusMap.get("Closed");
                    if (statusId != null) {
                        commanFunction.updateJobStatus(albatrossURL, albatrossTkn, jobSlug, statusId);
                        return 1;
                    }
                } catch (Exception ignored) {}
                return 0;
            }, executor);

            CompletableFuture<Integer> onHoldJobsFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    Response jobResp = commanFunction.createNewJob(baseURL, apiAuthToken, childCompanySlug, contactSlug);
                    String jobSlug = jobResp.jsonPath().get("slug");
                    Integer statusId = jobStatusMap.get("On Hold");
                    if (statusId != null) {
                        commanFunction.updateJobStatus(albatrossURL, albatrossTkn, jobSlug, statusId);
                        return 1;
                    }
                } catch (Exception ignored) {}
                return 0;
            }, executor);

            CompletableFuture<Integer> canceledJobsFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    Response jobResp = commanFunction.createNewJob(baseURL, apiAuthToken, childCompanySlug, contactSlug);
                    String jobSlug = jobResp.jsonPath().get("slug");
                    Integer statusId = jobStatusMap.get("Canceled");
                    if (statusId != null) {
                        commanFunction.updateJobStatus(albatrossURL, albatrossTkn, jobSlug, statusId);
                        return 1;
                    }
                } catch (Exception ignored) {}
                return 0;
            }, executor);

            int totalOpenJobs = openJobsFuture.join();
            int totalClosedJobs = closedJobsFuture.join();
            int totalOnHoldJobs = onHoldJobsFuture.join();
            int totalCanceledJobs = canceledJobsFuture.join();

            assertThat("Company slug should not be null", childCompanySlug, notNullValue());
            assertThat("Company ID should not be null", companyId, notNullValue());
            assertThat("Company name should not be null", companyName, notNullValue());
            assertThat("Parent company slug should not be null", parentCompanySlug, notNullValue());
            assertThat("Contact slug should not be null", contactSlug, notNullValue());

            CompanySearchData data = new CompanySearchData();
            data.companySlug = childCompanySlug;
            data.companyId = companyId;
            data.companyName = companyName;
            data.companyWebsite = companyWebsite;
            data.companyCity = companyCity;
            data.companyAddress = companyAddress;
            data.companyAbout = companyAbout;
            data.companyLinkedin = companyLinkedin;
            data.companyFacebook = companyFacebook;
            data.companyTwitter = companyTwitter;
            data.companyLogo = companyLogo;
            data.industryId = industryId;
            data.industry = industry;
            data.parentCompanySlug = parentCompanySlug;
            data.parentCompanyName = parentCompanyName;
            data.parentCompanyCity = parentCompanyCity;
            data.parentCompanyWebsite = parentCompanyWebsite;
            data.parentCompanyLogo = parentCompanyLogo;
            data.parentIndustryName = parentIndustryName;
            data.contactSlug = contactSlug;
            data.contactFirstName = contactFirstName;
            data.contactLastName = contactLastName;
            data.contactEmail = contactEmail;
            data.totalOpenJobs = totalOpenJobs;
            data.totalClosedJobs = totalClosedJobs;
            data.totalOnHoldJobs = totalOnHoldJobs;
            data.totalCanceledJobs = totalCanceledJobs;

            return new Object[][] { { data } };
        } catch (Exception e) {
            throw new AssertionError("Failed to create data for response data validation: " + e.getMessage(), e);
        } finally {
            executor.shutdown();
        }
    }
}
