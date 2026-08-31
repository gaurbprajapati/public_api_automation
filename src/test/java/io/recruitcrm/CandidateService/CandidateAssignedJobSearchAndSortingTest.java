package io.recruitcrm.CandidateService;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.hiringPipelineService.HiringPipelineFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.Assert;

import io.rcrm.api.commanfunctions.commanFunction;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.text.SimpleDateFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.rcrm.api.testbase.TestBase.AccountType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CandidateAssignedJobSearchAndSortingTest extends TestBase {

    public CandidateAssignedJobSearchAndSortingTest() {
        super();
    }

    commanFunction function = new commanFunction();
    AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();
    HiringPipelineFunctions hiringPipelineFunctions = new HiringPipelineFunctions();

    private int testCandidateId2;
    Map<String, Integer> userIdMap = new HashMap<>();
    Map<String, String> userNameMap = new HashMap<>();
    Map<String, Integer> customHiringPipelineIds = new HashMap<>();
    Map<String, String> companySlugs = new HashMap<>();
    Map<String, String> contactSlugs = new HashMap<>();
    Map<String, String> companyNames = new HashMap<>();
    String albatrossAuthToken;
    String apiAuthToken;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createTestDataForSorting();
    }

    // ==================== SEARCH TESTS ====================

    @Owner("Harika")
    @Test(dataProvider = "getSearchTestData", groups = {"candidate_service", "nightly-build"})
    public void searchCandidateAssignedJobs_ComprehensiveSearch(String testDescription, String searchTerm,
                                                                int expectedStatus, String validationType, String expectedMessage) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");

        JSONObject searchPayload = createSearchPayload(testCandidateId2, searchTerm, new ArrayList<>());

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                albatrossAuthToken,
                queryParameters, true, searchPayload);

        assertThat("searchCandidateAssignedJobs_ComprehensiveSearch - " + testDescription + " - Expected status code " + expectedStatus, 
                response.getStatusCode(), is(equalTo(expectedStatus)));

        switch (validationType) {
            case "success_with_message":
                response.then().body("meta.message", containsString(expectedMessage));
                break;
            case "success_with_results":
                List<Map<String, Object>> data = response.jsonPath().getList("data");
                assertThat(testDescription + " - Search results should not be null", data, is(notNullValue()));
                assertThat(testDescription + " - Search results should not be empty when jobs match the search term", data.isEmpty(), is(false));

                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    for (Map<String, Object> result : data) {
                        String jobName = (String) result.get("name");
                        assertThat(testDescription + " - Job name '" + jobName + "' should contain search term '" + searchTerm + "'",
                                jobName.toLowerCase().contains(searchTerm.toLowerCase()), is(true));
                    }
                }
                break;
            case "success_empty_results":
                response.then().body("meta.message", containsString(expectedMessage));
                List<Map<String, Object>> emptyData = response.jsonPath().getList("data");

                assertThat(testDescription + " - Data array should not be null even for empty results", emptyData, is(notNullValue()));
                assertThat(testDescription + " - Data array should be empty when no jobs match the search term", emptyData.isEmpty(), is(true));
                break;
            default:
                break;
        }
    }

    @Owner("Harika")
    @Test(dataProvider = "getCompanyNameSearchTestData", dependsOnMethods = "searchCandidateAssignedJobs_ComprehensiveSearch", groups = {"candidate_service", "nightly-build"})
    public void searchCandidateAssignedJobs_CompanyNameSearch(String testDescription, String searchTerm,
                                                              int expectedStatus, String validationType, String expectedMessage) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");

        JSONObject searchPayload = createSearchPayload(testCandidateId2, searchTerm, new ArrayList<>());

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                albatrossAuthToken,
                queryParameters, true, searchPayload);

        assertThat("searchCandidateAssignedJobs_CompanyNameSearch - " + testDescription + " - Expected status code " + expectedStatus,
                response.getStatusCode(), is(equalTo(expectedStatus)));

        switch (validationType) {
            case "success_with_results":
                List<Map<String, Object>> data = response.jsonPath().getList("data");
                assertThat(testDescription + " - Search results should not be null", data, is(notNullValue()));
                assertThat(testDescription + " - Search results should not be empty when jobs match the search term", data.isEmpty(), is(false));

                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    for (Map<String, Object> result : data) {
                        String companyName = (String) result.get("companyname");
                        if (companyName != null) {
                            assertThat(testDescription + " - Company name '" + companyName + "' should contain search term '" + searchTerm + "'",
                                    companyName.toLowerCase().contains(searchTerm.toLowerCase()), is(true));
                        }
                    }
                }
                break;
            case "success_empty_results":
                response.then().body("meta.message", containsString(expectedMessage));
                List<Map<String, Object>> emptyData = response.jsonPath().getList("data");

                assertThat(testDescription + " - Data array should not be null even for empty results", emptyData, is(notNullValue()));
                assertThat(testDescription + " - Data array should be empty when no jobs match the search term", emptyData.isEmpty(), is(true));
                break;
            default:
                break;
        }
    }

    @Owner("Harika")
    @Test(dataProvider = "getUserNameSearchTestData", dependsOnMethods = "searchCandidateAssignedJobs_ComprehensiveSearch", groups = {"candidate_service", "nightly-build"})
    public void searchCandidateAssignedJobs_UserNameSearch(String testDescription, String searchTerm,
                                                           int expectedStatus, String validationType, String expectedMessage) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");

        JSONObject searchPayload = createSearchPayload(testCandidateId2, searchTerm, new ArrayList<>());

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                albatrossAuthToken,
                queryParameters, true, searchPayload);

        assertThat("searchCandidateAssignedJobs_UserNameSearch - " + testDescription + " - Expected status code " + expectedStatus,
                response.getStatusCode(), is(equalTo(expectedStatus)));

        switch (validationType) {
            case "success_with_results":
                List<Map<String, Object>> data = response.jsonPath().getList("data");
                assertThat(testDescription + " - Search results should not be null", data, is(notNullValue()));
                assertThat(testDescription + " - Search results should not be empty when jobs match the search term", data.isEmpty(), is(false));

                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    for (Map<String, Object> result : data) {
                        String ownerName = (String) result.get("owner");
                        if (ownerName != null) {
                            assertThat(testDescription + " - Owner name '" + ownerName + "' should contain search term '" + searchTerm + "'",
                                    ownerName.toLowerCase().contains(searchTerm.toLowerCase()), is(true));
                        }
                    }
                }
                break;
            case "success_empty_results":
                response.then().body("meta.message", containsString(expectedMessage));
                List<Map<String, Object>> emptyData = response.jsonPath().getList("data");

                assertThat(testDescription + " - Data array should not be null even for empty results", emptyData, is(notNullValue()));
                assertThat(testDescription + " - Data array should be empty when no jobs match the search term", emptyData.isEmpty(), is(true));
                break;
            default:
                break;
        }
    }

    // ==================== SORTING TESTS ====================

    @Owner("Harika")
    @Test(dataProvider = "getTextSortData", groups = {"candidate_service", "nightly-build"})
    public void searchCandidateAssignedJobs_SortByTextField(String sortField, String jsonPath, String sortOrder, int statusCode) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = createSearchPayload(testCandidateId2, "", sortPriorityList);

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                albatrossAuthToken,
                queryParameters, true, searchPayload);

        String testCase = "searchCandidateAssignedJobs_SortByTextField - Field: " + sortField + ", Order: " + sortOrder;
        assertThat(testCase + " - Expected status code " + statusCode, response.getStatusCode(), is(equalTo(statusCode)));

        List<String> values = response.jsonPath().getList(jsonPath);

        if (sortOrder.equals("asc")) {
            assertThat(testCase + " - " + sortField + " not sorted ascending", isSortedAscendingText(values), is(true));
        } else {
            assertThat(testCase + " - " + sortField + " not sorted descending", isSortedDescendingText(values), is(true));
        }
    }

    @Owner("Harika")
    @Test(dataProvider = "getNumericSortData", groups = {"candidate_service", "nightly-build"})
    public void searchCandidateAssignedJobs_SortByNumericField(String sortField, String jsonPath, String sortOrder, int statusCode) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = createSearchPayload(testCandidateId2, "", sortPriorityList);

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                albatrossAuthToken,
                queryParameters, true, searchPayload);

        String testCase = "searchCandidateAssignedJobs_SortByNumericField - Field: " + sortField + ", Order: " + sortOrder;
        assertThat(testCase + " - Expected status code " + statusCode, response.getStatusCode(), is(equalTo(statusCode)));

        List<Number> values = response.jsonPath().getList(jsonPath);

        if (sortOrder.equals("asc")) {
            assertThat(testCase + " - " + sortField + " not sorted ascending", isSortedAscendingNumeric(values), is(true));
        } else {
            assertThat(testCase + " - " + sortField + " not sorted descending", isSortedDescendingNumeric(values), is(true));
        }
    }

    @Owner("Harika")
    @Test(dataProvider = "getDateSortData", groups = {"candidate_service", "nightly-build"})
    public void searchCandidateAssignedJobs_SortByDateField(String sortField, String jsonPath, String sortOrder, int statusCode) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.add(sortItem);

        JSONObject searchPayload = createSearchPayload(testCandidateId2, "", sortPriorityList);

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                albatrossAuthToken,
                queryParameters, true, searchPayload);

        String testCase = "searchCandidateAssignedJobs_SortByDateField - Field: " + sortField + ", Order: " + sortOrder;
        assertThat(testCase + " - Expected status code " + statusCode, response.getStatusCode(), is(equalTo(statusCode)));

        List<Object> values = response.jsonPath().getList(jsonPath);

        if (sortOrder.equals("asc")) {
            assertThat(testCase + " - " + sortField + " not sorted ascending", isSortedAscendingDate(values), is(true));
        } else {
            assertThat(testCase + " - " + sortField + " not sorted descending", isSortedDescendingDate(values), is(true));
        }
    }

    // ==================== SORTING HELPER METHODS ====================

    private boolean isSortedAscendingText(List<String> list) {
        if (list == null || list.size() <= 1) return true;

        List<String> sortedList = new ArrayList<>(list);
        Collections.sort(sortedList, String.CASE_INSENSITIVE_ORDER);
        return list.equals(sortedList);
    }

    private boolean isSortedDescendingText(List<String> list) {
        if (list == null || list.size() <= 1) return true;

        List<String> sortedList = new ArrayList<>(list);
        Collections.sort(sortedList, String.CASE_INSENSITIVE_ORDER.reversed());
        return list.equals(sortedList);
    }

    private boolean isSortedAscendingNumeric(List<Number> list) {
        if (list == null || list.size() <= 1) return true;

        for (int i = 1; i < list.size(); i++) {
            if (list.get(i - 1) != null && list.get(i) != null) {
                if (list.get(i - 1).doubleValue() > list.get(i).doubleValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSortedDescendingNumeric(List<Number> list) {
        if (list == null || list.size() <= 1) return true;

        for (int i = 1; i < list.size(); i++) {
            if (list.get(i - 1) != null && list.get(i) != null) {
                if (list.get(i - 1).doubleValue() < list.get(i).doubleValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSortedAscendingDate(List<Object> list) {
        if (list == null || list.size() <= 1) return true;

        try {
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i - 1) != null && list.get(i) != null) {
                    long date1 = getTimestamp(list.get(i - 1));
                    long date2 = getTimestamp(list.get(i));
                    if (date1 > date2) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean isSortedDescendingDate(List<Object> list) {
        if (list == null || list.size() <= 1) return true;

        try {
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i - 1) != null && list.get(i) != null) {
                    long date1 = getTimestamp(list.get(i - 1));
                    long date2 = getTimestamp(list.get(i));
                    if (date1 < date2) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private long getTimestamp(Object value) throws ParseException {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                // Try parsing as Unix timestamp
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                // If not a number, try parsing as date string
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                return sdf.parse((String) value).getTime() / 1000; // Convert to seconds
            }
        }
        return 0;
    }

    private JSONObject createSearchPayload(Integer candidateId, String searchTerm, List<Map<String, Object>> sortPriorityList) {
        JSONObject searchPayload = new JSONObject();
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("searchTerm", searchTerm);
        searchPayload.put("candidateId", candidateId);
        return searchPayload;
    }

    private void assignCandidateToJob(String candidateSlug, String jobSlug) {
        try {
            // Assign candidate to job
            Map<String, String> pathParameters = new HashMap<>();
            pathParameters.put("candidate", candidateSlug);

            String basePath = "candidates/{candidate}/assign";

            Map<String, String> queryParameters = new HashMap<>();
            queryParameters.put("job_slug", jobSlug);

            Response response = RestClient.doPost1("JSON", baseURL, basePath,
                    apiAuthToken, queryParameters, pathParameters, true, null);

            assertThat("Response status code should be 200", response.getStatusCode(), is(equalTo(200)));
        } catch (Exception e) {
            Assert.fail("Failed to create test data: " + e.getMessage());
        }
    }

    private JSONObject processJobPayload(JSONObject jobPayload) {
        Map<String, PlaceholderMapping> mappings = Map.of(
                "company_slug", new PlaceholderMapping("{companySlug", companySlugs),
                "contact_slug", new PlaceholderMapping("{contactSlug", contactSlugs),
                "hiring_pipeline_id", new PlaceholderMapping("{hiringPipelineId", customHiringPipelineIds),
                "created_by", new PlaceholderMapping("{userId", userIdMap),
                "owner_id", new PlaceholderMapping("{userId", userIdMap),
                "updated_by", new PlaceholderMapping("{userId", userIdMap)
        );

        for (Map.Entry<String, PlaceholderMapping> entry : mappings.entrySet()) {
            String key = entry.getKey();
            if (jobPayload.has(key)) {
                String value = jobPayload.getString(key);
                PlaceholderMapping mapping = entry.getValue();

                if (value.startsWith(mapping.prefix) && value.endsWith("}")) {
                    String mapKey = value.substring(1, value.length() - 1); // remove { and }
                    Object resolvedValue = mapping.lookupMap.get(mapKey);
                    if (resolvedValue != null) {
                        jobPayload.put(key, resolvedValue);
                    }
                }
            }
        }

        return jobPayload;
    }

    private static class PlaceholderMapping {
        String prefix;
        Map<String, ?> lookupMap;

        PlaceholderMapping(String prefix, Map<String, ?> lookupMap) {
            this.prefix = prefix;
            this.lookupMap = lookupMap;
        }
    }

    public void createTestDataForSorting() {
        createCustomHiringPipelines();
        createCompanies();
        createContacts();
        createUserIdMap();
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, albatrossAuthToken).jsonPath();

        testCandidateId2 = candidateJsonPath.get("data.candidate.id");
        String candidateSlug = candidateJsonPath.get("data.candidate.slug");

        JSONObject jobJson = readJsonFileFromPath("src/test/resources/jobCreationDataProvider.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            CompletableFuture.allOf(jobJson.keySet().stream()
                    .filter(key -> key.startsWith("job"))
                    .map(jobKey -> CompletableFuture.runAsync(() -> {
                        JSONObject payload = processJobPayload(jobJson.getJSONObject(jobKey));
                        Response response = function.createJobWithJson(baseURL, apiAuthToken, payload);
                        assertThat("createTestDataForSorting - Job creation failed for " + jobKey + " - Expected status code 200",
                                response.getStatusCode(), is(equalTo(200)));
                        String jobSlug = response.jsonPath().get("slug");

                        assignCandidateToJob(candidateSlug, jobSlug);

                    }, executor)).toArray(CompletableFuture[]::new)).join();
        } finally {
            executor.shutdown();
        }
    }

    public void createUserIdMap() {
        commanFunction function = new commanFunction();
        Response response = function.getUsers(baseURL, apiAuthToken);
        assertThat("createUserIdMap - Expected status code 200", response.getStatusCode(), is(equalTo(200)));
        JsonPath user = response.jsonPath();
        userIdMap.put("userId1", user.get("[0].id"));
        userIdMap.put("userId2", user.get("[1].id"));
        userIdMap.put("userId3", user.get("[2].id"));
        userIdMap.put("userId4", user.get("[3].id"));
        userNameMap.put("userName1", user.get("[0].first_name")+" "+user.get("[0].last_name"));
        userNameMap.put("userName2", user.get("[1].first_name")+" "+user.get("[1].last_name"));
        userNameMap.put("userName3", user.get("[2].first_name")+" "+user.get("[2].last_name"));
        userNameMap.put("userName4", user.get("[3].first_name")+" "+user.get("[3].last_name"));

    }

    private void createCompanies() {
        try {
            for (int i = 0; i < 5; i++) {
                commanFunction function = new commanFunction();
                Response response = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);

                assertThat("Response status code should be 200", response.getStatusCode(), is(equalTo(200)));

                JsonPath jsonPath = response.jsonPath();
                String companySlug = jsonPath.get("slug");
                String companyName = jsonPath.get("company_name");
                companySlugs.put("companySlug" + (i + 1), companySlug);
                companyNames.put("companyName" + (i + 1), companyName);
            }
        } catch (Exception e) {
            Assert.fail("Failed to create companies: " + e.getMessage());
        }
    }

    private void createContacts() {
        try {
            for (int i = 0; i < 5; i++) {
                commanFunction function = new commanFunction();
                Response response = function.createNewContact_POST(baseURL, apiAuthToken, companySlugs.get("companySlug" + (i + 1)));

                assertThat("Response status code should be 200", response.getStatusCode(), is(equalTo(200)));

                JsonPath jsonPath = response.jsonPath();
                String contactSlug = jsonPath.get("slug");
                contactSlugs.put("contactSlug" + (i + 1), contactSlug);
            }
        } catch (Exception e) {
            Assert.fail("Failed to create contacts: " + e.getMessage());
        }
    }

    private void createCustomHiringPipelines() {
        try {
            for (int i = 0; i < 4; i++) {
                Response response = hiringPipelineFunctions.createCustomHiringPipeline(
                        baseURL,
                        hiringPipelineServiceURL,
                        apiAuthToken,
                        albatrossAuthToken
                );

                assertThat("Response status code should be 200", response.getStatusCode(), is(equalTo(200)));

                JsonPath jsonPath = response.jsonPath();
                int pipelineId = jsonPath.get("id");
                customHiringPipelineIds.put("hiringPipelineId" + (i + 1), pipelineId);
            }
            System.out.println(customHiringPipelineIds);
        } catch (Exception e) {
            Assert.fail("Failed to create custom hiring pipelines: " + e.getMessage());
        }
    }

    // ==================== DATA PROVIDERS FOR SORTING & SEARCH ====================

    @DataProvider(name = "getTextSortData", parallel = true)
    public static Object[][] getTextSortData() {

        return new Object[][]{
                // Text fields - ascending
                {"candidatestatus", "data.candidatestatus", "asc", 200},
                {"companyname", "data.companyname", "asc", 200},
                {"specialization", "data.specialization", "asc", 200},
                {"job_skill", "data.job_skill", "asc", 200},
                {"job_category", "data.job_category", "asc", 200},
                {"postalcode", "data.postalcode", "asc", 200},
                {"country", "data.country", "asc", 200},
                {"locality", "data.locality", "asc", 200},
                {"description", "data.description", "asc", 200},
                {"address", "data.address", "asc", 200},
                {"city", "data.city", "asc", 200},
                {"contactname", "data.contactname", "asc", 200},
                {"contactemail", "data.contactemail", "asc", 200},
                {"contactnumber", "data.contactnumber", "asc", 200},
                {"state", "data.state", "asc", 200},
                {"hiring_pipeline_name", "data.hiring_pipeline_name", "asc", 200},
                {"job_type", "data.job_type", "asc", 200},
                {"createdby", "data.createdbyname", "asc", 200},
                {"updatedby", "data.updatedbyname", "asc", 200},
                {"ownername", "data.ownername", "asc", 200},
                {"remote", "data.remote", "asc", 200},

                // Text fields - descending
                {"candidatestatus", "data.candidatestatus", "desc", 200},
                {"companyname", "data.companyname", "desc", 200},
                {"specialization", "data.specialization", "desc", 200},
                {"job_skill", "data.job_skill", "desc", 200},
                {"job_category", "data.job_category", "desc", 200},
                {"postalcode", "data.postalcode", "desc", 200},
                {"country", "data.country", "desc", 200},
                {"locality", "data.locality", "desc", 200},
                {"description", "data.description", "desc", 200},
                {"address", "data.address", "desc", 200},
                {"city", "data.city", "desc", 200},
                {"contactname", "data.contactname", "desc", 200},
                {"contactemail", "data.contactemail", "desc", 200},
                {"contactnumber", "data.contactnumber", "desc", 200},
                {"state", "data.state", "desc", 200},
                {"hiring_pipeline_name", "data.hiring_pipeline_name", "desc", 200},
                {"job_type", "data.job_type", "desc", 200},
                {"createdby", "data.createdbyname", "desc", 200},
                {"updatedby", "data.createdbyname", "desc", 200},
                {"ownername", "data.ownername", "desc", 200},
                {"remote", "data.remote", "desc", 200}
        };
    }

    @DataProvider(name = "getNumericSortData", parallel = true)
    public static Object[][] getNumericSortData() {
        return new Object[][]{
                // Numeric fields - ascending
                {"bill_rate", "data.bill_rate", "asc", 200},
                {"srno", "data.srno", "asc", 200},
                {"qualificationid", "data.qualificationid", "asc", 200},
                {"pay_rate", "data.pay_rate", "asc", 200},
                {"minexperienceinyears", "data.minexperienceinyears", "asc", 200},
                {"maxexperienceinyears", "data.maxexperienceinyears", "asc", 200},
                {"annualsalarymin", "data.annualsalarymin", "asc", 200},
                {"annualsalarymax", "data.annualsalarymax", "asc", 200},
                {"noofopenings", "data.noofopenings", "asc", 200},


                // Numeric fields - descending
                {"bill_rate", "data.bill_rate", "desc", 200},
                {"srno", "data.srno", "desc", 200},
                {"qualificationid", "data.qualificationid", "desc", 200},
                {"pay_rate", "data.pay_rate", "desc", 200},
                {"minexperienceinyears", "data.minexperienceinyears", "desc", 200},
                {"maxexperienceinyears", "data.maxexperienceinyears", "desc", 200},
                {"annualsalarymin", "data.annualsalarymin", "desc", 200},
                {"annualsalarymax", "data.annualsalarymax", "desc", 200},
                {"noofopenings", "data.noofopenings", "desc", 200}
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

    @DataProvider(name = "getSearchTestData", parallel = true)
    public static Object[][] getSearchTestData() {
        return new Object[][]{
                // Exact matches on job names
                {"Exact match - Engineer", "Engineer", 200, "success_with_results", null},
                {"Exact match - Software", "Software", 200, "success_with_results", null},
                {"Exact match - Data Scientist", "Data Scientist", 200, "success_with_results", null},
                {"Exact match - Product Manager", "Product Manager", 200, "success_with_results", null},
                {"Exact match - DevOps", "DevOps", 200, "success_with_results", null},


                // Partial matches on job names
                {"Partial match - Soft", "Soft", 200, "success_with_results", null},
                {"Partial match - Eng", "Eng", 200, "success_with_results", null},
                {"Partial match - Data", "Data", 200, "success_with_results", null},
                {"Partial match - Product", "Product", 200, "success_with_results", null},
                {"Partial match - Dev", "Dev", 200, "success_with_results", null},


                // Case insensitive searches on job names
                {"Case insensitive - lowercase engineer", "engineer", 200, "success_with_results", null},
                {"Case insensitive - UPPERCASE ENGINEER", "ENGINEER", 200, "success_with_results", null},
                {"Case insensitive - MiXeD cAsE", "EnGiNeEr", 200, "success_with_results", null},


                // Search by job title keywords
                {"Search - Senior", "Senior", 200, "success_with_results", null},
                {"Search - Lead", "Lead", 200, "success_with_results", null},
                {"Search - Full Stack", "Full Stack", 200, "success_with_results", null},
                {"Search - QA", "QA", 200, "success_with_results", null},
                {"Search tech - Node.js", "Node.js", 200, "success_with_results", null},
                {"Search platform - Applications", "Applications", 200, "success_with_results", null},
                {"Search platform - Prototyping", "Prototyping", 200, "success_with_results", null},
                {"Search domain - CRM", "CRM", 200, "success_with_results", null},

                // Empty and whitespace searches
                {"Empty search term", "", 200, "success_with_message", "Assigned jobs fetched successfully"},
                {"Whitespace only", "   ", 200, "success_with_message", "Assigned jobs fetched successfully"},

                // No match scenarios
                {"No match - gibberish", "XyZNoMatch123", 200, "success_empty_results", "Assigned jobs fetched successfully"},
                {"No match - random string", "qwertyuiop12345", 200, "success_empty_results", "Assigned jobs fetched successfully"},
                {"No match - non-existent role", "Quantum Physicist", 200, "success_empty_results", "Assigned jobs fetched successfully"},

                // Special characters
                {"Special characters - symbols", "test!@#$%^&*()", 200, "success_with_results", null},

                // Long search term
                {"Long search term", "This is a very long search term that should be handled gracefully by the search API endpoint without causing any issues or errors", 200, "success_empty_results", "Assigned jobs fetched successfully"},

                // Numeric search
                {"Numeric search", "2024", 200, "success_empty_results", "Assigned jobs fetched successfully"},

                // Multiple keywords
                {"Multiple keywords - QA Automation Test", "QA Automation Test", 200, "success_with_results", null},
                {"Multiple keywords - Technical Content Writer", "Technical Content Writer", 200, "success_with_results", null},

                // Edge cases
                {"Edge case - single character", "A", 200, "success_with_results", null},
                {"Edge case - numbers only", "12345", 200, "success_with_results", null},
                {"Edge case - mixed alphanumeric", "Engineer123", 200, "success_with_results", null}
        };
    }

    @DataProvider(name = "getCompanyNameSearchTestData", parallel = true)
    public Object[][] getCompanyNameSearchTestData() {
        List<String> companyNamesList = new ArrayList<>(companyNames.values());

        if (companyNamesList.isEmpty()) {
            return new Object[][]{
                    {"Company name search - Test", "Test", 200, "success_with_results", null},
                    {"No match - non-existent company", "NonExistentCompanyXYZ", 200, "success_empty_results", "Assigned jobs fetched successfully"}
            };
        }

        String firstCompanyName = companyNamesList.get(0);

        return new Object[][]{
                {"Company name exact match", firstCompanyName, 200, "success_with_results", null},
                {"Company name partial match", firstCompanyName.split("\\s+")[0], 200, "success_with_results", null},
                {"No match - non-existent company", "NonExistentCompanyXYZ", 200, "success_empty_results", "Assigned jobs fetched successfully"}
        };
    }

    @DataProvider(name = "getUserNameSearchTestData", parallel = true)
    public Object[][] getUserNameSearchTestData() {
        List<String> userNamesList = new ArrayList<>(userNameMap.values());

        if (userNamesList.isEmpty()) {
            return new Object[][]{
                    {"User name search - Test", "Test", 200, "success_with_results", null},
                    {"No match - non-existent user", "NonExistentUserXYZ", 200, "success_empty_results", "Assigned jobs fetched successfully"}
            };
        }

        String firstUserName = userNamesList.get(0);

        return new Object[][]{
                {"User name exact match", firstUserName, 200, "success_with_results", null},
                {"User name partial match", firstUserName.split("\\s+")[0], 200, "success_with_results", null},
                {"No match - non-existent user", "NonExistentUserXYZ", 200, "success_empty_results", "Assigned jobs fetched successfully"}
        };
    }

}


