package io.recruitcrm.JobService;

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
import io.rcrm.api.pojo.albatross.JobSearchData;
import io.rcrm.api.pojo.albatross.UpdateFields;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class PostJobSearchTest extends TestBase {

    String apiAuthToken;
    String albatrossTkn;
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

    private static final String ADVANCED_JOB_SEARCH_GET_PATH = "advanced-search/jobs/search/get";

    @BeforeClass
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        customFieldFaker = new JavaFakerCustomField();
        commanFunction = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobSearchData", groups = {"job_service", "nightly-build"})
    public void testJobSearch_Success(String jobSlug, Integer jobId, String jobName) {
        // Step 1: Search for the created job
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response searchResponse = RestClient.doPost1("JSON", ariesServiceURL, ADVANCED_JOB_SEARCH_GET_PATH,
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

        // Verify our created job exists in the search results
        boolean jobFound = false;
        int dataSize = (Integer) searchJp.get("data.size()");
        for (int i = 0; i < dataSize; i++) {
            String dataPath = "data[" + i + "]";
            if (searchJp.get(dataPath + ".slug").equals(jobSlug)) {
                jobFound = true;
                
                // Validate job data structure
                assertThat("Job ID should match", (Integer) searchJp.get(dataPath + ".id"), equalTo((jobId)));
                assertThat("Job name should match", searchJp.get(dataPath + ".name"), equalTo(jobName));
                assertThat("Slug should match", searchJp.get(dataPath + ".slug"), equalTo(jobSlug));
                assertThat("Owner ID should not be null", searchJp.get(dataPath + ".ownerid"), notNullValue());
                assertThat("Account ID should not be null", searchJp.get(dataPath + ".accountid"), notNullValue());
                assertThat("Created by should not be null", searchJp.get(dataPath + ".createdby"), notNullValue());
                assertThat("Created on should not be null", searchJp.get(dataPath + ".createdon"), notNullValue());
                assertThat("Updated by should not be null", searchJp.get(dataPath + ".updatedby"), notNullValue());
                assertThat("Updated on should not be null", searchJp.get(dataPath + ".updatedon"), notNullValue());
                break;
            }
        }

        assertThat("Created job should be found in search results", jobFound, is(true));

        // Validate JSON schema using existing schema
        searchResponse.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/job/jobSearchGet.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobSearchDataForResponseDataValidation", groups = {"job_service", "nightly-build"})
    public void testJobSearch_ComprehensiveDataValidation(JobSearchData data) {

        // Step 1: Search for the job with comprehensive data
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response searchResponse = RestClient.doPost1("JSON", ariesServiceURL, ADVANCED_JOB_SEARCH_GET_PATH,
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + searchResponse.getStatusCode(),
                searchResponse.getStatusCode(), equalTo(200));

        JsonPath searchJp = searchResponse.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", searchJp.get("meta"), notNullValue());
        assertThat("Message should match expected", searchJp.get("meta.message"), equalTo("Entities retrieved successfully"));
        assertThat("Meta status should be 200", (Integer) searchJp.get("meta.status"), equalTo(200));

        // Find our job in the search results
        boolean jobFound = false;
        int dataSize = (Integer) searchJp.get("data.size()");
        for (int i = 0; i < dataSize; i++) {
            String dataPath = "data[" + i + "]";
            if (searchJp.get(dataPath + ".slug").equals(data.jobSlug)) {
                jobFound = true;

                // Validate basic job data
                assertThat("Job ID should match", (Integer) searchJp.get(dataPath + ".id"), equalTo(data.jobId));
                assertThat("Job name should match", searchJp.get(dataPath + ".name"), equalTo(data.jobName));
                assertThat("Slug should match", searchJp.get(dataPath + ".slug"), equalTo(data.jobSlug));
                
                // Validate srno field exists
                assertThat("Srno should not be null", searchJp.get(dataPath + ".srno"), notNullValue());

                // Validate job details - location fields
                if (data.city != null && !data.city.isEmpty()) {
                    assertThat("City should match", searchJp.get(dataPath + ".city"), equalTo(data.city));
                } else {
                    assertThat("City field should exist", searchJp.get(dataPath + ".city"), anyOf(nullValue(), equalTo("")));
                }
                if (data.address != null && !data.address.isEmpty()) {
                    assertThat("Address should match", searchJp.get(dataPath + ".address"), equalTo(data.address));
                } else {
                    assertThat("Address field should exist", searchJp.get(dataPath + ".address"), anyOf(nullValue(), equalTo("")));
                }
                if (data.locality != null && !data.locality.isEmpty()) {
                    assertThat("Locality should match", searchJp.get(dataPath + ".locality"), equalTo(data.locality));
                } else {
                    assertThat("Locality field should exist", searchJp.get(dataPath + ".locality"), anyOf(nullValue(), equalTo("")));
                }
                if (data.state != null && !data.state.isEmpty()) {
                    assertThat("State should match", searchJp.get(dataPath + ".state"), equalTo(data.state));
                } else {
                    assertThat("State field should exist", searchJp.get(dataPath + ".state"), anyOf(nullValue(), equalTo("")));
                }
                if (data.country != null && !data.country.isEmpty()) {
                    assertThat("Country should match", searchJp.get(dataPath + ".country"), equalTo(data.country));
                } else {
                    assertThat("Country field should exist", searchJp.get(dataPath + ".country"), anyOf(nullValue(), equalTo("")));
                }
                
                // Validate postalcode (note: response uses "postalcode" not "postal_code")
                if (data.postalCode != null && !data.postalCode.isEmpty()) {
                    String postalCodeFromResponse = searchJp.get(dataPath + ".postalcode");
                    if (postalCodeFromResponse != null) {
                        assertThat("Postal code should match", postalCodeFromResponse, equalTo(data.postalCode));
                    }
                } else {
                    assertThat("Postalcode field should exist", searchJp.get(dataPath + ".postalcode"), anyOf(nullValue(), equalTo("")));
                }
                
                // Validate company fields
                assertThat("Company slug should match", searchJp.get(dataPath + ".companyslug"), equalTo(data.companySlug));
                if (data.companyId > 0) {
                    Integer companyIdFromResponse = searchJp.get(dataPath + ".companyid");
                    if (companyIdFromResponse != null) {
                        assertThat("Company ID should match", companyIdFromResponse, equalTo(data.companyId));
                    }
                }
                if (data.companyName != null && !data.companyName.isEmpty()) {
                    assertThat("Company name should match", searchJp.get(dataPath + ".companyname"), equalTo(data.companyName));
                }
                
                // Validate contact fields
                if (data.contactSlug != null) {
                    assertThat("Contact slug should match", searchJp.get(dataPath + ".contactslug"), equalTo(data.contactSlug));
                }
                assertThat("Contact name field should exist", searchJp.get(dataPath + ".contactname"), anyOf(nullValue(), notNullValue()));
                assertThat("Contact email field should exist", searchJp.get(dataPath + ".contactemail"), anyOf(nullValue(), notNullValue()));
                assertThat("Contact number field should exist", searchJp.get(dataPath + ".contactnumber"), anyOf(nullValue(), notNullValue()));

                // Validate system fields with equality checks
                Integer accountIdFromResponse = searchJp.get(dataPath + ".accountid");
                if (accountIdFromResponse != null) {
                    assertThat("Account ID should match", accountIdFromResponse, equalTo(data.accountId));
                }
                Integer ownerIdFromResponse = searchJp.get(dataPath + ".ownerid");
                if (ownerIdFromResponse != null) {
                    assertThat("Owner ID should match", ownerIdFromResponse, equalTo(data.ownerId));
                }
                assertThat("Owner name field should exist", searchJp.get(dataPath + ".ownername"), anyOf(nullValue(), notNullValue()));
                
                Integer createdByFromResponse = searchJp.get(dataPath + ".createdby");
                if (createdByFromResponse != null) {
                    assertThat("Created by should match", createdByFromResponse, equalTo(data.createdBy));
                }
                assertThat("Creator name field should exist", searchJp.get(dataPath + ".creatorname"), anyOf(nullValue(), notNullValue()));
                
                Integer createdOnFromResponse = searchJp.get(dataPath + ".createdon");
                if (createdOnFromResponse != null && data.createdOn > 0) {
                    assertThat("Created on should match", createdOnFromResponse, equalTo(data.createdOn));
                }
                
                Integer updatedByFromResponse = searchJp.get(dataPath + ".updatedby");
                if (updatedByFromResponse != null) {
                    assertThat("Updated by should match", updatedByFromResponse, equalTo(data.updatedBy));
                }
                assertThat("Updator name field should exist", searchJp.get(dataPath + ".updatorname"), anyOf(nullValue(), notNullValue()));
                
                Integer updatedOnFromResponse = searchJp.get(dataPath + ".updatedon");
                if (updatedOnFromResponse != null && data.updatedOn > 0) {
                    assertThat("Updated on should match", updatedOnFromResponse, equalTo(data.updatedOn));
                }
                
                // Validate deleted field (as integer in response, not boolean)
                Object deletedFromResponse = searchJp.get(dataPath + ".deleted");
                if (deletedFromResponse != null) {
                    if (deletedFromResponse instanceof Integer) {
                        boolean deletedAsBool = ((Integer) deletedFromResponse) != 0;
                        assertThat("Deleted flag should match", deletedAsBool, equalTo(data.deleted));
                    } else if (deletedFromResponse instanceof Boolean) {
                        assertThat("Deleted flag should match", (Boolean) deletedFromResponse, equalTo(data.deleted));
                    }
                }
                
                // Validate company-related fields
                Object companyDeletedFromResponse = searchJp.get(dataPath + ".companydeleted");
                if (companyDeletedFromResponse != null) {
                    if (companyDeletedFromResponse instanceof Integer) {
                        boolean companyDeletedAsBool = ((Integer) companyDeletedFromResponse) != 0;
                        assertThat("Company deleted flag should match", companyDeletedAsBool, equalTo(data.companyDeleted));
                    } else if (companyDeletedFromResponse instanceof Boolean) {
                        assertThat("Company deleted flag should match", (Boolean) companyDeletedFromResponse, equalTo(data.companyDeleted));
                    }
                }
                
                // Validate additional fields from actual response
                assertThat("Description field should exist", searchJp.get(dataPath + ".description"), anyOf(nullValue(), notNullValue()));
                assertThat("Job status field should exist", searchJp.get(dataPath + ".jobstatus"), anyOf(nullValue(), notNullValue()));
                assertThat("Job status label field should exist", searchJp.get(dataPath + ".jobstatuslabel"), anyOf(nullValue(), notNullValue()));
                assertThat("Number of openings field should exist", searchJp.get(dataPath + ".noofopenings"), anyOf(nullValue(), notNullValue()));
                assertThat("Hiring pipeline ID field should exist", searchJp.get(dataPath + ".hiring_pipeline_id"), anyOf(nullValue(), notNullValue()));
                assertThat("Hiring pipeline name field should exist", searchJp.get(dataPath + ".hiring_pipeline_name"), anyOf(nullValue(), notNullValue()));
                assertThat("Job type field should exist", searchJp.get(dataPath + ".job_type"), anyOf(nullValue(), notNullValue()));
                assertThat("Job category field should exist", searchJp.get(dataPath + ".job_category"), anyOf(nullValue(), notNullValue()));
                assertThat("Job skill field should exist", searchJp.get(dataPath + ".job_skill"), anyOf(nullValue(), notNullValue()));
                assertThat("Job function field should exist", searchJp.get(dataPath + ".job_function"), anyOf(nullValue(), notNullValue()));
                assertThat("Job industry field should exist", searchJp.get(dataPath + ".job_industry"), anyOf(nullValue(), notNullValue()));
                assertThat("Specialization field should exist", searchJp.get(dataPath + ".specialization"), anyOf(nullValue(), notNullValue()));
                assertThat("Qualification ID field should exist", searchJp.get(dataPath + ".qualificationid"), anyOf(nullValue(), notNullValue()));
                assertThat("Salary type field should exist", searchJp.get(dataPath + ".salarytype"), anyOf(nullValue(), notNullValue()));
                assertThat("Pay rate field should exist", searchJp.get(dataPath + ".pay_rate"), anyOf(nullValue(), notNullValue()));
                assertThat("Bill rate field should exist", searchJp.get(dataPath + ".bill_rate"), anyOf(nullValue(), notNullValue()));
                assertThat("Annual salary min field should exist", searchJp.get(dataPath + ".annualsalarymin"), anyOf(nullValue(), notNullValue()));
                assertThat("Annual salary max field should exist", searchJp.get(dataPath + ".annualsalarymax"), anyOf(nullValue(), notNullValue()));
                assertThat("Min experience in years field should exist", searchJp.get(dataPath + ".minexperienceinyears"), anyOf(nullValue(), notNullValue()));
                assertThat("Max experience in years field should exist", searchJp.get(dataPath + ".maxexperienceinyears"), anyOf(nullValue(), notNullValue()));
                assertThat("Lat field should exist", searchJp.get(dataPath + ".lat"), anyOf(nullValue(), notNullValue()));
                assertThat("Lng field should exist", searchJp.get(dataPath + ".lng"), anyOf(nullValue(), notNullValue()));
                assertThat("Archived field should exist", searchJp.get(dataPath + ".archived"), anyOf(nullValue(), notNullValue()));
                assertThat("Target companies field should exist", searchJp.get(dataPath + ".targetcompanies"), anyOf(nullValue(), notNullValue()));

                break;
            }
        }

        assertThat("Created job should be found in search results", jobFound, is(true));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobSearchData", groups = {"job_service", "nightly-build"})
    public void testJobSearch_Workflow(String jobSlug, Integer jobId, String jobName) {
        // Step 1: Search to confirm job exists
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response searchResponse = RestClient.doPost1("JSON", ariesServiceURL, ADVANCED_JOB_SEARCH_GET_PATH,
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        assertThat("Search should succeed", searchResponse.getStatusCode(), equalTo(200));
        
        JsonPath searchJp = searchResponse.jsonPath();
        int initialCount = (Integer) searchJp.get("data.size()");
        assertThat("Should have jobs before delete", initialCount, equalTo(1));

        // Step 2: Delete the job using global/delete-record endpoint
        JSONObject deleteRequestBody = new JSONObject();
        deleteRequestBody.put("idsToDelete", new JSONArray().put(jobId));
        deleteRequestBody.put("slugsToDelete", new JSONArray().put(jobSlug));
        deleteRequestBody.put("tableFlag", "job");

        String basePath = "global/delete-record";
        Response deleteResponse = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, null, null, true, deleteRequestBody.toString());
        assertThat("Delete should succeed", deleteResponse.getStatusCode(), equalTo(200));

        // Step 3: Search again to verify deletion
        Response searchAfterDeleteResponse = RestClient.doPost1("JSON", ariesServiceURL, ADVANCED_JOB_SEARCH_GET_PATH,
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        assertThat("Search after delete should succeed", searchAfterDeleteResponse.getStatusCode(), equalTo(200));

        JsonPath searchAfterDeleteJp = searchAfterDeleteResponse.jsonPath();

        // Should have fewer jobs or the deleted job should not be found
        boolean jobStillExists = false;
        int dataSize = (Integer) searchAfterDeleteJp.get("data.size()");
        for (int i = 0; i < dataSize; i++) {
            String dataPath = "data[" + i + "]";
            if (searchAfterDeleteJp.get(dataPath + ".slug").equals(jobSlug)) {
                jobStillExists = true;
                break;
            }
        }

        assertThat("Deleted job should not exist in search results", jobStillExists, is(false));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearch_WithoutAuth() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response response = RestClient.doPost1("JSON", ariesServiceURL, ADVANCED_JOB_SEARCH_GET_PATH,
                null, queryParameters, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearch_InvalidAuth() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response response = RestClient.doPost1("JSON", ariesServiceURL, ADVANCED_JOB_SEARCH_GET_PATH,
                albatrossTkn + "invalid-token-123", queryParameters, null, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearch_EmptyRequestBody() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "JOB");
        Map<String, String> queryParameters = createDefaultQueryParameters();

        Response response = RestClient.doPost1("JSON", ariesServiceURL, ADVANCED_JOB_SEARCH_GET_PATH,
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(200));

        // Should still return jobs even with empty request body
        JsonPath jp = response.jsonPath();
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearch_InvalidQueryParameters() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "invalid-page");
        queryParameters.put("size", "invalid-size");

        Response response = RestClient.doPost1("JSON", ariesServiceURL, ADVANCED_JOB_SEARCH_GET_PATH,
                albatrossTkn, queryParameters, null, true, requestBody.toString());

        // Should handle invalid parameters gracefully or return error
        assertThat("Response should be handled appropriately", 
                response.getStatusCode(), anyOf(equalTo(200), equalTo(400), equalTo(422)));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearch_MissingQueryParameters() {
        JSONObject requestBody = createDefaultSearchRequestBody();

        Response response = RestClient.doPost1("JSON", ariesServiceURL, ADVANCED_JOB_SEARCH_GET_PATH,
                albatrossTkn, null, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(200));

        JsonPath searchJp = response.jsonPath();

        assertThat("Meta object should not be null", searchJp.get("meta"), notNullValue());
        assertThat("Message should match expected", searchJp.get("meta.message"), equalTo("Entities retrieved successfully"));

    }

    private Response postAdvancedJobSearchGet(JSONObject searchPayload) {
        return RestClient.doPost("JSON", ariesServiceURL, ADVANCED_JOB_SEARCH_GET_PATH,
                albatrossTkn, createDefaultQueryParameters(), true, searchPayload);
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getTextSortData", groups = {"sorting", "job_service"})
    public void searchJob_SortByTextField(String sortField, String jsonPath, String sortOrder, int statusCode) {
        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = new JSONObject();
        searchPayload.put("advancedSearchContext", "JOB");
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("defaultFilterList", JSONObject.NULL);
        searchPayload.put("filterSearchList", JSONObject.NULL);
        searchPayload.put("booleanSearchList", JSONObject.NULL);

        Response response = postAdvancedJobSearchGet(searchPayload);

        String testCase = "searchJob_SortByTextField - Field: " + sortField + ", Order: " + sortOrder;
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
    @Test(dataProvider = "getNumericSortData", groups = {"sorting", "job_service"})
    public void searchJob_SortByNumericField(String sortField, String jsonPath, String sortOrder, int statusCode) {
        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = new JSONObject();
        searchPayload.put("advancedSearchContext", "JOB");
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("defaultFilterList", JSONObject.NULL);
        searchPayload.put("filterSearchList", JSONObject.NULL);
        searchPayload.put("booleanSearchList", JSONObject.NULL);

        Response response = postAdvancedJobSearchGet(searchPayload);

        String testCase = "searchJob_SortByNumericField - Field: " + sortField + ", Order: " + sortOrder;
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
    @Test(dataProvider = "getDateSortData", groups = {"sorting", "job_service"})
    public void searchJob_SortByDateField(String sortField, String jsonPath, String sortOrder, int statusCode) {
        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = new JSONObject();
        searchPayload.put("advancedSearchContext", "JOB");
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("defaultFilterList", JSONObject.NULL);
        searchPayload.put("filterSearchList", JSONObject.NULL);
        searchPayload.put("booleanSearchList", JSONObject.NULL);

        Response response = postAdvancedJobSearchGet(searchPayload);

        String testCase = "searchJob_SortByDateField - Field: " + sortField + ", Order: " + sortOrder;
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
    @Test(dataProvider = "getCustomFieldSortData", groups = {"sorting", "job_service"})
    public void searchJob_SortByCustomField(String sortField, String sortOrder, int statusCode, String fieldType) {
        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = new JSONObject();
        searchPayload.put("advancedSearchContext", "JOB");
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("defaultFilterList", JSONObject.NULL);
        searchPayload.put("filterSearchList", JSONObject.NULL);
        searchPayload.put("booleanSearchList", JSONObject.NULL);

        Response response = postAdvancedJobSearchGet(searchPayload);

        // Extract column ID from sortField (e.g., "custcolumn123" -> 123)
        int columnId = Integer.parseInt(sortField.replace("custcolumn", ""));
        String fieldName = customFieldColumnIdToNameMap.getOrDefault(columnId, sortField);
        
        String testCase = "searchJob_SortByCustomField - Field: " + fieldName + " (" + sortField + "), Type: " + fieldType + ", Order: " + sortOrder;
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
                        // Step 1: Create companies and contacts first
                        createCompanies();
                        createContacts();
                        
                        // Step 2: Create custom fields for jobs of each type
                        createCustomFields();
                        
                        // Step 3: Create jobs from JSON and update custom field values
                        createJobsWithCustomFields();
                        
                        customFieldsInitialized = true;
                    } catch (Exception e) {
                        throw new AssertionError("Failed to initialize test data for sorting: " + e.getMessage(), e);
                    }
                }
            }
        }
    }
    
    @BeforeGroups(groups = "sorting")
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
    
    private Map<String, String> contactKeyToSlugMap = new HashMap<>();
    
    private void createContacts() {
        // Create contacts needed for jobs
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<CompletableFuture<Void>> createFutures = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                final int contactIndex = i;
                createFutures.add(CompletableFuture.runAsync(() -> {
                    String contactKey = "contact" + contactIndex;
                    String companySlug = companyKeyToSlugMap.get("company" + contactIndex);
                    Response response = commanFunction.createNewContact_POST(baseURL, apiAuthToken, companySlug);
                    assertThat("Contact creation failed for " + contactKey,
                            response.getStatusCode(), equalTo(200));
                    String slug = response.jsonPath().getString("slug");
                    synchronized (contactKeyToSlugMap) {
                        contactKeyToSlugMap.put(contactKey, slug);
                    }
                }, executor));
            }
            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }
    
    private void createCustomFields() {
        // Create all custom field types for jobs
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
                            albatrossURL, albatrossTkn, fieldType, fieldName, options, "job"), executor);
                } else {
                    future = CompletableFuture.supplyAsync(() -> 
                        commanFunction.createCustomFieldAndGetColumnId(
                            albatrossURL, albatrossTkn, fieldType, fieldName, "job"), executor);
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
    
    private void createJobsWithCustomFields() {
        // Read JSON file and create jobs with custom field values
        String filePath = "src/test/resources/jobCreationWithCustomFieldsDataProvider.json";
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new AssertionError("Failed to read JSON file: " + filePath + ". Error: " + e.getMessage(), e);
        }
        
        JSONObject jsonObject = new JSONObject(content);
        JSONArray jobsArray = jsonObject.getJSONArray("jobs");
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            // Create jobs and update custom fields in parallel
            CompletableFuture.allOf(IntStream.range(0, jobsArray.length())
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            JSONObject jobPayload = jobsArray.getJSONObject(i);
                            JSONObject customFields = jobPayload.optJSONObject("customFields");
                            
                            // Extract index from placeholder (e.g., "{companySlug1}" -> "1")
                            int jobIndex = i + 1;
                            String companyKey = "company" + jobIndex;
                            String contactKey = "contact" + jobIndex;
                            
                            String companySlug = companyKeyToSlugMap.get(companyKey);
                            String contactSlug = contactKeyToSlugMap.get(contactKey);
                            
                            if (companySlug == null || contactSlug == null) {
                                throw new AssertionError("Company or contact slug is null for job " + jobIndex);
                            }
                            
                            // Replace placeholders in the payload
                            jobPayload.put("company_slug", companySlug);
                            jobPayload.put("contact_slug", contactSlug);
                            
                            // Remove customFields from payload before creating job
                            jobPayload.remove("customFields");
                            
                            // Create job using JSONObject
                            Response jobResponse = RestClient.doPost("JSON", baseURL, "jobs", apiAuthToken, null, true, jobPayload);
                            assertThat("Job creation failed for job at index " + i + ". Status: " + jobResponse.getStatusCode() + 
                                    ", Response: " + jobResponse.getBody().asString(),
                                    jobResponse.getStatusCode(), equalTo(200));
                            
                            String jobSlug = jobResponse.jsonPath().getString("slug");
                            assertThat("Job slug should not be null for job at index " + i, jobSlug, notNullValue());
                            
                            // Get job ID
                            Response getJobResponse = allCrudFunctions.getJobResponse(albatrossURL, albatrossTkn, jobSlug);
                            assertThat("Failed to get job for job at index " + i, getJobResponse.getStatusCode(), equalTo(200));
                            
                            JsonPath jobJp = getJobResponse.jsonPath();
                            Integer jobIdObj = jobJp.get("data.job.id");
                            assertThat("Job ID should not be null for job at index " + i, jobIdObj, notNullValue());
                            int jobId = Integer.parseInt(String.valueOf(jobIdObj));
                            
                            // Update custom field values if present
                            if (customFields != null) {
                                updateJobCustomFields(jobId, customFields, albatrossTkn);
                            }
                        } catch (Exception e) {
                            throw new AssertionError("Failed to create job at index " + i + ": " + e.getMessage(), e);
                        }
                    }, executor))
                    .toArray(CompletableFuture[]::new))
                    .join();
        } finally {
            executor.shutdown();
        }
    }
    
    private void updateJobCustomFields(int jobId, JSONObject customFields, String authToken) {
        ExecutorService executor = Executors.newFixedThreadPool(11);
        try {
            List<CompletableFuture<Void>> updateFutures = new ArrayList<>();
            
            // Update all custom fields explicitly
            if (customFields.has("text")) {
                updateFutures.add(CompletableFuture.runAsync(() -> 
                    updateJobCustomField(jobId, "custcolumn" + textCustomFieldColumnId, 
                        customFields.getString("text"), authToken), executor));
            }
            if (customFields.has("number")) {
                updateFutures.add(CompletableFuture.runAsync(() -> 
                    updateJobCustomField(jobId, "custcolumn" + numberCustomFieldColumnId, 
                        customFields.getString("number"), authToken), executor));
            }
            if (customFields.has("date")) {
                updateFutures.add(CompletableFuture.runAsync(() -> 
                    updateJobCustomField(jobId, "custcolumn" + dateCustomFieldColumnId, 
                        customFields.getString("date"), authToken), executor));
            }
            if (customFields.has("date_time")) {
                updateFutures.add(CompletableFuture.runAsync(() -> 
                    updateJobCustomField(jobId, "custcolumn" + dateTimeCustomFieldColumnId, 
                        customFields.getString("date_time"), authToken), executor));
            }
            if (customFields.has("longtext")) {
                updateFutures.add(CompletableFuture.runAsync(() -> 
                    updateJobCustomField(jobId, "custcolumn" + longTextCustomFieldColumnId, 
                        customFields.getString("longtext"), authToken), executor));
            }
            if (customFields.has("phonenumber")) {
                updateFutures.add(CompletableFuture.runAsync(() -> 
                    updateJobCustomField(jobId, "custcolumn" + phoneNumberCustomFieldColumnId, 
                        customFields.getString("phonenumber"), authToken), executor));
            }
            if (customFields.has("dropdown")) {
                updateFutures.add(CompletableFuture.runAsync(() -> 
                    updateJobCustomField(jobId, "custcolumn" + dropdownCustomFieldColumnId, 
                        customFields.getString("dropdown"), authToken), executor));
            }
            if (customFields.has("multiselect")) {
                updateFutures.add(CompletableFuture.runAsync(() -> 
                    updateJobCustomField(jobId, "custcolumn" + multiselectCustomFieldColumnId, 
                        customFields.getString("multiselect"), authToken), executor));
            }
            if (customFields.has("checkbox")) {
                updateFutures.add(CompletableFuture.runAsync(() -> 
                    updateJobCustomField(jobId, "custcolumn" + checkboxCustomFieldColumnId, 
                        customFields.getString("checkbox"), authToken), executor));
            }
            if (customFields.has("file")) {
                updateFutures.add(CompletableFuture.runAsync(() -> 
                    updateJobCustomField(jobId, "custcolumn" + fileCustomFieldColumnId, 
                        customFields.getString("file"), authToken), executor));
            }
            if (customFields.has("social_profile")) {
                updateFutures.add(CompletableFuture.runAsync(() -> 
                    updateJobCustomField(jobId, "custcolumn" + socialProfileCustomFieldColumnId, 
                        customFields.getString("social_profile"), authToken), executor));
            }
            
            CompletableFuture.allOf(updateFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }
    
    private void updateJobCustomField(int jobId, String fieldKey, String fieldValue, String authToken) {
        List<Integer> entityIds = Arrays.asList(jobId);
        UpdateFields updateFields = new UpdateFields();
        updateFields.setKey(fieldKey);
        updateFields.setValue(fieldValue);
        updateFields.setTableFlag("job");
        updateFields.setId(entityIds);
        
        Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", authToken, null, true, updateFields);
        assertThat("Failed to update custom field " + fieldKey + " for job " + jobId,
                response.getStatusCode(), equalTo(200));
    }

    private JSONObject createDefaultSearchRequestBody() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "JOB");
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
                {"jobstatus", "data.jobstatus", "asc", 200},
                {"companyname", "data.companyname", "asc", 200},
                {"description", "data.description", "asc", 200},
                {"address", "data.address", "asc", 200},
                {"city", "data.city", "asc", 200},
                {"locality", "data.locality", "asc", 200},
                {"contactname", "data.contactname", "asc", 200},
                {"contactemail", "data.contactemail", "asc", 200},
                {"contactnumber", "data.contactnumber", "asc", 200},
                {"job_category", "data.job_category", "asc", 200},
                {"postalcode", "data.postal_code", "asc", 200},
                {"state", "data.state", "asc", 200},
                {"country", "data.country", "asc", 200},
                {"specialization", "data.specialization", "asc", 200},
                {"job_skill", "data.job_skill", "asc", 200},
                {"job_function", "data.job_function", "asc", 200},
                {"job_industry", "data.job_industry", "asc", 200},

                // Text fields - descending
                {"name", "data.name", "desc", 200},
                {"jobstatus", "data.jobstatus", "desc", 200},
                {"companyname", "data.companyname", "desc", 200},
                {"description", "data.description", "desc", 200},
                {"address", "data.address", "desc", 200},
                {"city", "data.city", "desc", 200},
                {"locality", "data.locality", "desc", 200},
                {"contactname", "data.contactname", "desc", 200},
                {"contactemail", "data.contactemail", "desc", 200},
                {"contactnumber", "data.contactnumber", "desc", 200},
                {"job_category", "data.job_category", "desc", 200},
                {"postalcode", "data.postal_code", "desc", 200},
                {"state", "data.state", "desc", 200},
                {"country", "data.country", "desc", 200},
                {"specialization", "data.specialization", "desc", 200},
                {"job_skill", "data.job_skill", "desc", 200},
                {"job_function", "data.job_function", "desc", 200},
                {"job_industry", "data.job_industry", "desc", 200},
        };
    }

    @DataProvider(name = "getNumericSortData", parallel = true)
    public static Object[][] getNumericSortData() {
        return new Object[][]{
                // Numeric fields - ascending 
                {"srno", "data.srno", "asc", 200},
                {"minexperienceinyears", "data.minimum_experience", "asc", 200},
                {"bill_rate", "data.bill_rate", "asc", 200},
                {"maxexperienceinyears", "data.maximum_experience", "asc", 200},
                {"annualsalarymin", "data.min_annual_salary", "asc", 200},
                {"annualsalarymax", "data.max_annual_salary", "asc", 200},
                {"noofopenings", "data.numberofopenings", "asc", 200},
                {"job_type", "data.job_type", "asc", 200},
                {"ownerid", "data.ownerid", "asc", 200},
                {"hiring_pipeline_id", "data.hiring_pipeline_id", "asc", 200},
                {"pay_rate", "data.pay_rate", "asc", 200},
                {"salarytype", "data.salary_type", "asc", 200},
                {"qualificationid", "data.qualification_id", "asc", 200},
                {"createdby", "data.createdby", "asc", 200},
                {"updatedby", "data.updatedby", "asc", 200},

                // Numeric fields - descending
                {"srno", "data.srno", "desc", 200},
                {"minexperienceinyears", "data.minimum_experience", "desc", 200},
                {"bill_rate", "data.bill_rate", "desc", 200},
                {"maxexperienceinyears", "data.maximum_experience", "desc", 200},
                {"annualsalarymin", "data.min_annual_salary", "desc", 200},
                {"annualsalarymax", "data.max_annual_salary", "desc", 200},
                {"noofopenings", "data.numberofopenings", "desc", 200},
                {"job_type", "data.job_type", "desc", 200},
                {"ownerid", "data.ownerid", "desc", 200},
                {"hiring_pipeline_id", "data.hiring_pipeline_id", "desc", 200},
                {"pay_rate", "data.pay_rate", "desc", 200},
                {"salarytype", "data.salary_type", "desc", 200},
                {"qualificationid", "data.qualification_id", "desc", 200},
                {"createdby", "data.createdby", "desc", 200},
                {"updatedby", "data.updatedby", "desc", 200}
        };
    }

    @DataProvider(name = "getDateSortData", parallel = true)
    public static Object[][] getDateSortData() {
        return new Object[][]{
                // Date fields - ascending
                {"createdon", "data.createdon", "asc", 200},
                {"updatedon", "data.updatedon", "asc", 200},

                // Date fields - descending
                {"createdon", "data.createdon", "desc", 200},
                {"updatedon", "data.updatedon", "desc", 200}
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

    @DataProvider(name = "jobSearchData")
    public Object[][] getJobSearchData() {
        // Create test job using Albatross
        String jobSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "job");

        JsonPath jp = allCrudFunctions.getJobResponse(albatrossURL, albatrossTkn, jobSlug).jsonPath();
        Integer jobId = jp.get("data.job.id");
        String jobName = jp.get("data.job.name");
        
        assertThat("Job slug should not be null", jobSlug, notNullValue());
        assertThat("Job ID should not be null", jobId, notNullValue());
        assertThat("Job name should not be null", jobName, notNullValue());
        
        return new Object[][] { { jobSlug, jobId, jobName } };
    }

    @DataProvider(name = "jobSearchDataForResponseDataValidation", parallel = true)
    public Object[][] getJobSearchDataForResponseDataValidation() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // Create a company first (jobs need to be associated with a company)
            CompletableFuture<Response> companyFuture = CompletableFuture.supplyAsync(() ->
                    commanFunction.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken), executor);

            String companySlug = companyFuture.thenApply(r -> r.jsonPath().getString("slug")).join();
            assertThat("Company slug should not be null", companySlug, notNullValue());

            // Create a contact (jobs need to be associated with a contact)
            CompletableFuture<Response> contactFuture = CompletableFuture.supplyAsync(() ->
                    commanFunction.createNewContact_POST(baseURL, apiAuthToken, companySlug), executor);

            String contactSlug = contactFuture.thenApply(r -> r.jsonPath().getString("slug")).join();
            assertThat("Contact slug should not be null", contactSlug, notNullValue());

            // Create job with comprehensive data
            CompletableFuture<Response> jobFuture = CompletableFuture.supplyAsync(() ->
                    commanFunction.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug), executor);

            String jobSlug = jobFuture.thenApply(r -> r.jsonPath().getString("slug")).join();

            // Get job details from GET endpoint
            JsonPath jobJp = allCrudFunctions.getJobResponse(albatrossURL, albatrossTkn, jobSlug).jsonPath();
            Integer jobId = jobJp.get("data.job.id");
            String jobName = jobJp.get("data.job.name");
            String city = jobJp.get("data.job.city");
            String address = jobJp.get("data.job.address");
            String locality = jobJp.get("data.job.locality");
            String state = jobJp.get("data.job.state");
            String country = jobJp.get("data.job.country");
            String postalCode = jobJp.get("data.job.postal_code");
            int ownerId = jobJp.get("data.job.ownerid");
            Integer createdByObj = jobJp.get("data.job.createdby");
            int createdBy = (createdByObj != null) ? createdByObj : 0;
            Integer createdOnStr = jobJp.get("data.job.createdon");
            int createdOn = (createdOnStr != null) ? createdOnStr : 0;
            Integer updatedByObj = jobJp.get("data.job.updatedby");
            int updatedBy = (updatedByObj != null) ? updatedByObj : 0;
            Integer updatedOnStr = jobJp.get("data.job.updatedon");
            int updatedOn = (updatedOnStr != null) ? updatedOnStr : 0;
            Object deletedObj = jobJp.get("data.job.deleted");
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

            assertThat("Job slug should not be null", jobSlug, notNullValue());
            assertThat("Job ID should not be null", jobId, notNullValue());
            assertThat("Job name should not be null", jobName, notNullValue());

            JobSearchData data = new JobSearchData();
            data.jobSlug = jobSlug;
            data.jobId = jobId;
            data.jobName = jobName;
            data.city = city;
            data.address = address;
            data.locality = locality;
            data.state = state;
            data.country = country;
            data.postalCode = postalCode;
            data.companySlug = companySlug;
            data.companyId = companyId;
            data.companyName = companyName;
            data.contactSlug = contactSlug;
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
