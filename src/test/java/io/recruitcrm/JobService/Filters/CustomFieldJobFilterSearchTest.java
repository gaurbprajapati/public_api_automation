package io.recruitcrm.JobService.Filters;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.DateUtil;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import io.rcrm.api.pojo.albatross.UpdateFields;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.hiringPipeline.HiringPipeline;
import io.rcrm.api.pojo.albatross.hiringpipeline.CreateHiringPipeline;
import io.rcrm.api.pojo.albatross.hiringpipeline.HiringStages;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CustomFieldJobFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String albatrossAuthToken;
    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    String accountOwnerAPIKey;
    Map<String, Integer> customFieldIds = new HashMap<>();
    Map<String, String> entityCFValueMap = new HashMap<>();
    ConcurrentHashMap<String, String> jobKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> jobIdToKeyMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companyKeyToSlugMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companyKeyToIdMap = new ConcurrentHashMap<>();
    Map<String, Map<String, String>> timestampScenarios;
    String email;
    Map<String, String> contactKeyToSlugMap = new HashMap<>();
    Map<String, String> contactKeyToIdMap = new HashMap<>();
    Map<String, String> userMap = new HashMap<>();
    Map<String, String> teamMap = new HashMap<>();
    Map<String, Integer> jobStatusIdMap = new HashMap<>();
    Map<String, Integer> qualificationIdMap = new HashMap<>();
    Map<String, Integer> hiringPipelineIdMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        albatrossAuthToken = ownerAlbatrossAuthToken;
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        userMap = createUserMap();
        teamMap = createTeamMap();
        jobStatusIdMap = createJobStatusMap();
        qualificationIdMap = createQualificationMap();
        hiringPipelineIdMap = createHiringPipelineMap();
        createCompanies();
        createContacts();
        customFieldIds = createCustomFields();
        createEntityCFValueMap();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "entityCustomFieldFilterSearchTestData", description = "Filter Search Test for Custom Fields")
    public void entityCustomFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Account: ",email);
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        FilterSearchReporter.logInfo("Test Case ID", testCaseId);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "jobs");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "name", jobIdToKeyMap);

        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, jobKeyToIdMap, jobIdToKeyMap, 15, "Job");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "textCustomFieldFilterSearchTestData", description = "Filter Search Test for Text Custom Fields")
    public void textCustomFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Account: ",email);
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        FilterSearchReporter.logInfo("Test Case ID", testCaseId);
        JSONObject payload = createTextFieldFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "jobs");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);

        validateTextFieldFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "numberCustomFieldFilterSearchTestData", description = "Filter Search Test for Number Custom Fields")
    public void numberCustomFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Account: ",email);
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        FilterSearchReporter.logInfo("Test Case ID", testCaseId);
        JSONObject payload = createNumberFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "jobs");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);

        validateNumberFieldFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, "Job");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "dateCustomFieldFilterSearchTestData", description = "Filter Search Test for Date Custom Fields")
    public void dateCustomFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Account: ",email);
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        FilterSearchReporter.logInfo("Test Case ID", testCaseId);
        JSONObject payload = createDateFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "jobs");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);

        validateEntityDateField(data, filterType, filterValue, fieldName, dbField, expectedResult, "Job");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "dropdownAndMultiselectFilterSearchTestData", description = "Filter Search Test for Dropdown and Multiselect Custom Fields")
    public void dropdownAndMultiselectFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Account: ",email);
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        FilterSearchReporter.logInfo("Test Case ID", testCaseId);
        JSONObject payload = createDropdownAndMultiselectFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "jobs");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, jobKeyToIdMap, jobIdToKeyMap, 15, "Job");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "checkboxCustomFieldFilterSearchTestData", description = "Filter Search Test for Checkbox and File Custom Fields")
    public void checkboxCustomFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Account: ",email);
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        FilterSearchReporter.logInfo("Test Case ID", testCaseId);
        JSONObject payload = createCheckboxFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "jobs");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, jobKeyToIdMap, jobIdToKeyMap, 15, "Job");
    }

    @DataProvider(name = "entityCustomFieldFilterSearchTestData")
    public Object[][] entityCustomFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        List<String> entityTypes = Arrays.asList("candidate", "company", "contact", "job", "deal", "user", "team");
        
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String fieldType = test.getString("fieldType");
                String filterType = test.getString("filterType");
                String filterValue = test.getString("filterValue");
                String dbField = test.getString("dbField");
                String filterValue_TYPE = test.optString("filterValue_TYPE", "STRING_LIST");
                
                if (entityTypes.contains(fieldType)) {
                    testData.add(new Object[]{
                            key, filterType, filterValue, dbField, test.getString("expectedResult"), fieldType, filterValue_TYPE, test.optString("testCaseId", "")
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "dateCustomFieldFilterSearchTestData", parallel = true)
    public Object[][] dateCustomFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                if (tests.getJSONObject(i).getString("fieldType").equals("date")) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), test.optString("testCaseId", "")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "numberCustomFieldFilterSearchTestData", parallel = true)
    public Object[][] numberCustomFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                if (tests.getJSONObject(i).getString("fieldType").equals("number")) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), test.optString("testCaseId", "")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "textCustomFieldFilterSearchTestData", parallel = true)
    public Object[][] textCustomFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                if (tests.getJSONObject(i).getString("fieldType").equals("text") || tests.getJSONObject(i).getString("fieldType").equals("phonenumber") || tests.getJSONObject(i).getString("fieldType").equals("longtext") || tests.getJSONObject(i).getString("fieldType").equals("email") || tests.getJSONObject(i).getString("fieldType").equals("social_profile")) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), test.optString("testCaseId", "")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "dropdownAndMultiselectFilterSearchTestData")
    public Object[][] dropdownAndMultiselectFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                if (tests.getJSONObject(i).getString("fieldType").equals("multiselect") || tests.getJSONObject(i).getString("fieldType").equals("dropdown")) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), test.optString("testCaseId", "")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "checkboxCustomFieldFilterSearchTestData", parallel = true)
    public Object[][] checkboxCustomFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                String fieldType = tests.getJSONObject(i).getString("fieldType");
                if (fieldType.equals("checkbox") || fieldType.equals("file")) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), test.optString("testCaseId", "")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public void validateTextFieldFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Wrong job data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && data.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }
        
        for (int i = 0; i < data.length(); i++) {
            JSONObject job = data.getJSONObject(i);
            String jobData = job.optString(dbField, "").trim();
            filterValue = filterValue.trim();
            switch (filterType) {
                case "is":
                    Assert.assertEquals(jobData.toLowerCase(), filterValue.toLowerCase(), "Wrong job data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue + ",");
                    break;
                case "contains":
                    Assert.assertTrue(jobData.toLowerCase().contains(filterValue.toLowerCase()), "Wrong job data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
                    break;
                case "starts_with":
                    Assert.assertTrue(jobData.toLowerCase().startsWith(filterValue.toLowerCase()), "Wrong job data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
                    break;
                case "ends_with":
                    Assert.assertTrue(jobData.toLowerCase().endsWith(filterValue.toLowerCase()), "Wrong job data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
                    break;
                case "does_not_contain":
                    Assert.assertFalse(jobData.toLowerCase().contains(filterValue.toLowerCase()), "Wrong job data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
                    break;
                case "contains_exact_word":
                    Assert.assertTrue(jobData.toLowerCase().contains(filterValue.toLowerCase()), "Wrong job data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
                    break;
                case "contains_at_least_one":
                    String[] searchValues = filterValue.split(",");
                    boolean foundMatch = false;

                    for (String searchValue : searchValues) {
                        searchValue = searchValue.trim();
                        if (!searchValue.isEmpty() && jobData.toLowerCase().contains(searchValue.toLowerCase())) {
                            foundMatch = true;
                            break;
                        }
                    }

                    Assert.assertTrue(foundMatch, "Wrong job data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue + ". Field value '" + jobData + "' should contain at least one of: " + filterValue);
                    break;
                case "has_any_value":
                    Assert.assertFalse(jobData.isEmpty() || jobData.equals("") || jobData.equals("null"), "Wrong job data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
                    break;
                case "is_empty":
                    Assert.assertTrue(jobData.isEmpty() || jobData.equals("") || jobData.equals("null"), "Wrong job data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
                    break;
            }
        }
    }

    public void createCompanies() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            List<CompletableFuture<Void>> createFutures = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                String companyKey = "company" + i;
                if (companyJson.has(companyKey)) {
                    createFutures.add(CompletableFuture.runAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");
                        String createdBy = companyEntry.has("createdBy") ? companyEntry.getString("createdBy") : "admin";
                        String authToken = getAlbatrossAuthToken(createdBy);
                        
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, authToken, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String slug = jsonPath.getString("data.company.slug");
                        String companyIdStr = jsonPath.getString("data.company.id");
                        
                        if (companyIdStr == null) {
                            System.out.println("Skipping " + companyKey + " - company ID is null");
                            return;
                        }
                        Integer companyId = Integer.parseInt(companyIdStr);
                        
                        synchronized (companyKeyToSlugMap) {
                            companyKeyToSlugMap.put(companyKey, slug);
                        }
                        synchronized (companyKeyToIdMap) {
                            companyKeyToIdMap.put(companyKey, String.valueOf(companyId));
                        }
                    }, executor));
                }
            }

            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    public void createTestData() {
        JSONObject jobJson = readJsonFileFromPath("src/test/resources/testData/jobCF_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = jobJson.keySet().stream()
                .filter(key -> key.startsWith("job"))
                .map(jobKey -> CompletableFuture.runAsync(() -> {
                    JSONObject jobEntry = jobJson.getJSONObject(jobKey);
                    JSONObject payload = jobEntry.getJSONObject("payload");
                    String createdBy = jobEntry.has("createdBy") ? jobEntry.getString("createdBy") : "admin";
                    String authToken = getAlbatrossAuthToken(createdBy);
                    
                    // Replace company placeholders in selectedcompanies
                    if (payload.has("selectedcompanies")) {
                        JSONArray selectedCompanies = payload.getJSONArray("selectedcompanies");
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
                    
                    JSONObject job = payload.getJSONObject("job");
                    replaceJobPlaceholders(job, payload);
                    JSONObject processedPayload = processPayloadPlaceholders(payload);
                    Response response = RestClient.doPost("JSON", albatrossURL, "/jobs", authToken, null, true, processedPayload);
                    response.then().statusCode(200);
                    JsonPath jsonPath = response.jsonPath();
                    String jobIdStr = jsonPath.getString("data.job.id");
                    
                    if (jobIdStr == null) {
                        System.out.println("Skipping " + jobKey + " - job ID is null");
                        return;
                    }
                    
                    Integer jobId = Integer.parseInt(jobIdStr);
                    jobIdToKeyMap.put(String.valueOf(jobId), jobKey.toLowerCase());
                    jobKeyToIdMap.put(jobKey.toLowerCase(), String.valueOf(jobId));
                }, executor))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        updateJobCustomDateFieldsTimestamps();
    }

    private void updateJobCustomDateFieldsTimestamps() {
        timestampScenarios = createTimestampScenarios();
        String dateCFFieldName = "dateCF";
        
        if (!customFieldIds.containsKey(dateCFFieldName)) {
            return; // No date custom field created
        }
        
        String dateCFDbField = "custcolumn" + customFieldIds.get(dateCFFieldName);
        List<Map.Entry<String, Map<String, String>>> scenarios = new ArrayList<>(timestampScenarios.entrySet());
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            int jobIndex = 0;
            
            for (Map.Entry<String, Map<String, String>> scenario : scenarios) {
                if (jobIndex >= jobKeyToIdMap.size()) {
                    break;
                }
                
                final int idx = jobIndex;
                final Map<String, String> timestamps = scenario.getValue();
                
                futures.add(CompletableFuture.runAsync(() -> {
                    String jobKey = "job" + (idx + 1);
                    String jobIdStr = jobKeyToIdMap.get(jobKey);
                    
                    if (jobIdStr == null) {
                        return;
                    }
                    
                    int jobId = Integer.parseInt(jobIdStr);
                    String dateValue = timestamps.get(dateCFFieldName);
                    
                    if (dateValue != null) {
                        JSONObject fieldsAndTimestamps = new JSONObject();
                        fieldsAndTimestamps.put(dateCFDbField, dateValue);
                        
                        updateJobCustomFields(jobId, fieldsAndTimestamps);
                    }
                }, executor));
                
                jobIndex++;
            }
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private Map<String, Map<String, String>> createTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        // Today scenario - Custom date fields store dates as "yyyy-MM-dd" format
        String todayDate = DateUtil.getTodayDateString("yyyy-MM-dd");
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("dateCF", todayDate);
        scenarios.put("today_scenario", todayTimestamps);

        // Yesterday scenario
        String yesterdayDate = DateUtil.getYesterdayDateString("yyyy-MM-dd");
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("dateCF", yesterdayDate);
        scenarios.put("yesterday_scenario", yesterdayTimestamps);

        // This week scenario
        String thisWeekDate = DateUtil.getThisWeekDateString();
        LocalDate thisWeekLocalDate = LocalDate.parse(thisWeekDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("dateCF", thisWeekLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("this_week_scenario", thisWeekTimestamps);

        // Last week scenario
        String lastWeekDate = DateUtil.getLastWeekDateString();
        LocalDate lastWeekLocalDate = LocalDate.parse(lastWeekDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("dateCF", lastWeekLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("last_week_scenario", lastWeekTimestamps);

        // This month scenario
        String thisMonthDate = DateUtil.getThisMonthDateString();
        LocalDate thisMonthLocalDate = LocalDate.parse(thisMonthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("dateCF", thisMonthLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("this_month_scenario", thisMonthTimestamps);

        // Last month scenario
        String lastMonthDate = DateUtil.getLastMonthDateString();
        LocalDate lastMonthLocalDate = LocalDate.parse(lastMonthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("dateCF", lastMonthLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("last_month_scenario", lastMonthTimestamps);

        // This quarter scenario
        String thisQuarterDate = DateUtil.getThisQuarterDateString();
        LocalDate thisQuarterLocalDate = LocalDate.parse(thisQuarterDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("dateCF", thisQuarterLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("this_quarter_scenario", thisQuarterTimestamps);

        // Last quarter scenario
        String lastQuarterDate = DateUtil.getLastQuarterDateString();
        LocalDate lastQuarterLocalDate = LocalDate.parse(lastQuarterDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("dateCF", lastQuarterLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("last_quarter_scenario", lastQuarterTimestamps);

        // This year scenario
        String thisYearDate = DateUtil.getThisYearDateString();
        LocalDate thisYearLocalDate = LocalDate.parse(thisYearDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("dateCF", thisYearLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("this_year_scenario", thisYearTimestamps);

        // Last year scenario
        String lastYearDate = DateUtil.getLastYearDateString();
        LocalDate lastYearLocalDate = LocalDate.parse(lastYearDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("dateCF", lastYearLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("last_year_scenario", lastYearTimestamps);

        // Static date scenario 1 - Historical data (June 15, 2022)
        Map<String, String> staticTimestamps1 = new HashMap<>();
        staticTimestamps1.put("dateCF", "2022-06-15");
        scenarios.put("static_date_scenario1", staticTimestamps1);

        // Static date scenario 2 - Previous year data (March 10, 2023)
        Map<String, String> staticTimestamps2 = new HashMap<>();
        staticTimestamps2.put("dateCF", "2023-03-10");
        scenarios.put("static_date_scenario2", staticTimestamps2);

        // Static date scenario 3 - Current year data (June 6, 2024)
        Map<String, String> staticTimestamps3 = new HashMap<>();
        staticTimestamps3.put("dateCF", "2024-06-06");
        scenarios.put("static_date_scenario3", staticTimestamps3);

        // Static date scenario 4 - Future planning data (February 14, 2025)
        Map<String, String> staticTimestamps4 = new HashMap<>();
        staticTimestamps4.put("dateCF", "2025-02-14");
        scenarios.put("static_date_scenario4", staticTimestamps4);

        return scenarios;
    }

    public void createEntityCFValueMap() {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            CompletableFuture<JsonPath> candidateJson1Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCandidateWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);
            CompletableFuture<JsonPath> candidateJson2Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCandidateWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);

            CompletableFuture<JsonPath> companyJson1Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);
            CompletableFuture<JsonPath> companyJson2Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);

            // Contacts depend on companies
            CompletableFuture<JsonPath> contactJson1Future = companyJson1Future.thenApplyAsync(companyJson1 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> contactJson2Future = companyJson2Future.thenApplyAsync(companyJson2 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson2.getString("slug")).jsonPath(), executor);

            // Jobs depend on company + contact
            CompletableFuture<JsonPath> jobJson1Future = companyJson1Future.thenCombineAsync(contactJson1Future, (companyJson1, contactJson1) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, companyJson1.getString("slug"), contactJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> jobJson2Future = companyJson2Future.thenCombineAsync(contactJson2Future, (companyJson2, contactJson2) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, companyJson2.getString("slug"), contactJson2.getString("slug")).jsonPath(), executor);

            // Deals depend on company + contact + job
            CompletableFuture<JsonPath> dealJson1Future = jobJson1Future.thenApplyAsync(jobJson1 -> {
                String companySlug1 = companyJson1Future.join().getString("slug");
                String contactSlug1 = contactJson1Future.join().getString("slug");
                return function.createNewDealWithMandatoryFields(baseURL, accountOwnerAPIKey, companySlug1, contactSlug1, jobJson1.getString("slug")).jsonPath();
            }, executor);
            CompletableFuture<JsonPath> dealJson2Future = jobJson2Future.thenApplyAsync(jobJson2 -> {
                String companySlug2 = companyJson2Future.join().getString("slug");
                String contactSlug2 = contactJson2Future.join().getString("slug");
                return function.createNewDealWithMandatoryFields(baseURL, accountOwnerAPIKey, companySlug2, contactSlug2, jobJson2.getString("slug")).jsonPath();
            }, executor);

            //Get user
            CompletableFuture<JsonPath> userJsonFuture = CompletableFuture.supplyAsync(() -> {
                return function.getUsers(baseURL, accountOwnerAPIKey).jsonPath();
            }, executor);

            //Create team depends on user
            CompletableFuture<JsonPath> teamJsonFuture = userJsonFuture.thenApplyAsync((userJson) -> {
                ArrayList<String> team1UserId = new ArrayList<String>();
                ArrayList<String> team2UserId = new ArrayList<String>();
                team1UserId.add(String.valueOf(userJson.getInt("[1].id")));
                team1UserId.add(String.valueOf(userJson.getInt("[3].id")));
                team2UserId.add(String.valueOf(userJson.getInt("[0].id")));
                team2UserId.add(String.valueOf(userJson.getInt("[2].id")));

                Response team1Response = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team1", team1UserId);
                Response team2Response = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team2", team2UserId);
                team1Response.then().statusCode(200);
                team2Response.then().statusCode(200);
                Response team = function.getTeams(baseURL, accountOwnerAPIKey);
                return team.jsonPath();
            }, executor);

            CompletableFuture.allOf(
                    candidateJson1Future, candidateJson2Future,
                    companyJson1Future, companyJson2Future,
                    contactJson1Future, contactJson2Future,
                    jobJson1Future, jobJson2Future,
                    dealJson1Future, dealJson2Future,
                    userJsonFuture, teamJsonFuture
            ).join();

            JsonPath candidateJson1 = candidateJson1Future.join();
            JsonPath candidateJson2 = candidateJson2Future.join();
            JsonPath companyJson1 = companyJson1Future.join();
            JsonPath companyJson2 = companyJson2Future.join();
            JsonPath contactJson1 = contactJson1Future.join();
            JsonPath contactJson2 = contactJson2Future.join();
            JsonPath jobJson1 = jobJson1Future.join();
            JsonPath jobJson2 = jobJson2Future.join();
            JsonPath dealJson1 = dealJson1Future.join();
            JsonPath dealJson2 = dealJson2Future.join();
            JsonPath userJson = userJsonFuture.join();
            JsonPath teamJson = teamJsonFuture.join();
            entityCFValueMap.put("candidate1", candidateJson1.getString("slug"));
            entityCFValueMap.put("candidate2", candidateJson2.getString("slug"));
            entityCFValueMap.put("company1", companyJson1.getString("slug"));
            entityCFValueMap.put("company2", companyJson2.getString("slug"));
            entityCFValueMap.put("contact1", contactJson1.getString("slug"));
            entityCFValueMap.put("contact2", contactJson2.getString("slug"));
            entityCFValueMap.put("job1", jobJson1.getString("slug"));
            entityCFValueMap.put("job2", jobJson2.getString("slug"));
            entityCFValueMap.put("deal1", dealJson1.getString("slug"));
            entityCFValueMap.put("deal2", dealJson2.getString("slug"));
            entityCFValueMap.put("owner", String.valueOf(userJson.getInt("[0].id")));
            entityCFValueMap.put("admin", String.valueOf(userJson.getInt("[1].id")));
            entityCFValueMap.put("restricted", String.valueOf(userJson.getInt("[2].id")));
            entityCFValueMap.put("teamMember", String.valueOf(userJson.getInt("[3].id")));
            entityCFValueMap.put("team1", teamJson.getString("[0].team_id"));
            entityCFValueMap.put("team2", teamJson.getString("[1].team_id"));
            int entityCompany1Id = function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson1.getString("slug"));
            int entityCompany2Id = function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson2.getString("slug"));
            // Store entity companies in companyKeyToIdMap for company CF fields
            companyKeyToIdMap.put("entityCompany1", String.valueOf(entityCompany1Id));
            companyKeyToIdMap.put("entityCompany2", String.valueOf(entityCompany2Id));
            companyKeyToIdMap.put("entitycompany1", String.valueOf(entityCompany1Id));
            companyKeyToIdMap.put("entitycompany2", String.valueOf(entityCompany2Id));
            // Get job IDs from jobJson1 and jobJson2 for entityJob1/2 mapping
            String jobSlug1 = jobJson1.getString("slug");
            String jobSlug2 = jobJson2.getString("slug");
            int entityJob1Id = function.getJobIdBySlug(albatrossURL, albatrossAuthToken, jobSlug1);
            int entityJob2Id = function.getJobIdBySlug(albatrossURL, albatrossAuthToken, jobSlug2);
            // Store entity jobs in jobKeyToIdMap as entityJob1/2 for job filter validation
            jobKeyToIdMap.put("entityJob1", String.valueOf(entityJob1Id));
            jobKeyToIdMap.put("entityJob2", String.valueOf(entityJob2Id));
            jobKeyToIdMap.put("entityjob1", String.valueOf(entityJob1Id));
            jobKeyToIdMap.put("entityjob2", String.valueOf(entityJob2Id));
            jobIdToKeyMap.put(String.valueOf(entityJob1Id), "entityJob1");
            jobIdToKeyMap.put(String.valueOf(entityJob2Id), "entityJob2");
        } finally {
            executor.shutdown();
        }
    }

    public Map<String, Integer> createCustomFields() {
        Map<String, Integer> customFieldIds = new HashMap<>();
        commanFunction function = new commanFunction();
        int colId = 1;

        List<String> entityTypes = new ArrayList<>(List.of("candidate", "company", "deals", "job", "contact", "user", "team", "text", "email", "phonenumber", "longtext", "number", "date", "social_profile", "dropdown", "multiselect","checkbox","file"));
        
        for (String entity : entityTypes) {
            String fieldName = entity + "CF";
            Response response;

            if (entity.equals("dropdown") || entity.equals("multiselect")) {
                response = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, "job", fieldName, entity, "Option A, Option B, OptionC");
            } else {
                response = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, "job", fieldName, entity, "", colId);
            }
            colId++;
            
            Assert.assertEquals(response.getStatusCode(), 200, "Failed to create custom field: " + fieldName);
            
            int columnId = response.jsonPath().getInt("data.custumField.columnid");
            customFieldIds.put(fieldName, columnId);
        }
        
        return customFieldIds;
    }

    public JSONObject processPayloadPlaceholders(JSONObject payload) {
        JSONObject processedPayload = new JSONObject();
        
        for (String key : payload.keySet()) {
            Object value = payload.get(key);
            
            // Handle nested JSONObject
            if (value instanceof JSONObject) {
                JSONObject nestedObject = (JSONObject) value;
                JSONObject processedNestedObject = processPayloadPlaceholders(nestedObject);
                processedPayload.put(key, processedNestedObject);
            }
            // Handle JSONArray
            else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                JSONArray processedArray = new JSONArray();
                for (int i = 0; i < array.length(); i++) {
                    Object arrayItem = array.get(i);
                    if (arrayItem instanceof JSONObject) {
                        processedArray.put(processPayloadPlaceholders((JSONObject) arrayItem));
                    } else {
                        processedArray.put(arrayItem);
                    }
                }
                processedPayload.put(key, processedArray);
            }
            // Handle placeholder keys (custom fields)
            else if (key.startsWith("{") && key.endsWith("}")) {
                String trimmedKey = key.substring(1, key.length() - 1);
                
                if (customFieldIds.containsKey(trimmedKey)) {
                    String newKey = "custcolumn" + customFieldIds.get(trimmedKey);
                    if (value instanceof String) {
                        String stringValue = (String) value;
                        String processedValue = processEntityPlaceholders(stringValue);
                        processedPayload.put(newKey, processedValue);
                    } else {
                        processedPayload.put(newKey, value);
                    }
                } else {
                    processedPayload.put(key, value);
                }
            }
            // Handle regular keys
            else {
                processedPayload.put(key, value);
            }
        }
        
        return processedPayload;
    }

    public String processEntityPlaceholders(String value) {
        if (value == null) {
            return null;
        }
        
        if (value.startsWith("{") && value.endsWith("}")) {
            String innerValue = value.substring(1, value.length() - 1);
            String[] entityKeys = innerValue.split(",");
            
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < entityKeys.length; i++) {
                String entityKey = entityKeys[i].trim();
                if (entityCFValueMap.containsKey(entityKey)) {
                    if (i > 0) {
                        result.append(",");
                    }
                    result.append(entityCFValueMap.get(entityKey));
                } else {
                    if (i > 0) {
                        result.append(",");
                    }
                    result.append(entityKey);
                }
            }

            return result.toString();
        }
        return value;
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        String processedFilterValue = processEntityPlaceholders(filterValue);

        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        String[] values = processedFilterValue.split(",");
        JSONArray valueArray = new JSONArray();
        if (filterType.equals("is_empty") || filterType.equals("has_any_value")) {
            filterValueObj.put("value", valueArray);
        } else {
            for (String val : values) {
                if(filterValue_TYPE.equals("INTEGER_LIST")) {
                    valueArray.put(Integer.parseInt(val.trim()));
                } else {
                    valueArray.put(val.trim());
                }
            }
            filterValueObj.put("value", valueArray);
        }

        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "jobs");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "job");
        filter.put("fieldType", fieldType);
        filter.put("filterValue", filterValueObj);

        JSONArray filters = new JSONArray();
        filters.put(filter);
        
        JSONObject groupFilter = new JSONObject();
        groupFilter.put("groupFilterJoinOperator", "AND");
        groupFilter.put("filters", filters);
        
        JSONArray groupFilterList = new JSONArray();
        groupFilterList.put(groupFilter);

        JSONObject filterSearchList = new JSONObject();
        filterSearchList.put("groupFilterList", groupFilterList);
        filterSearchList.put("groupJoinOperator", "AND");

        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "JOB");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        return payload;
    }

    public JSONObject createDropdownAndMultiselectFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        String[] values = filterValue.split(",");
        JSONArray valueArray = new JSONArray();
        if (filterType.equals("is_empty") || filterType.equals("has_any_value")) {
            filterValueObj.put("value", valueArray);
        } else {    
            for (String val : values) {
                valueArray.put(val.trim());
            }
            filterValueObj.put("value", valueArray);
        }

        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "jobs");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "job");
        filter.put("fieldType", fieldType);
        filter.put("filterValue", filterValueObj);

        JSONArray filters = new JSONArray();
        filters.put(filter);
        
        JSONObject groupFilter = new JSONObject();
        groupFilter.put("groupFilterJoinOperator", "AND");
        groupFilter.put("filters", filters);
        
        JSONArray groupFilterList = new JSONArray();
        groupFilterList.put(groupFilter);

        JSONObject filterSearchList = new JSONObject();
        filterSearchList.put("groupFilterList", groupFilterList);
        filterSearchList.put("groupJoinOperator", "AND");

        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "JOB");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        return payload;
    }

    public JSONObject createCheckboxFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        
        // For checkbox, filterValue is "1" for "yes" and "0" for "no", and it's INTEGER type
        if (filterValue_TYPE.equals("INTEGER")) {
            filterValueObj.put("value", Integer.parseInt(filterValue.trim()));
        } else {
            // Fallback to INTEGER_LIST if needed
            JSONArray valueArray = new JSONArray();
            valueArray.put(Integer.parseInt(filterValue.trim()));
            filterValueObj.put("value", valueArray);
        }

        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "jobs");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "job");
        filter.put("fieldType", fieldType);
        filter.put("filterValue", filterValueObj);

        JSONArray filters = new JSONArray();
        filters.put(filter);
        
        JSONObject groupFilter = new JSONObject();
        groupFilter.put("groupFilterJoinOperator", "AND");
        groupFilter.put("filters", filters);
        
        JSONArray groupFilterList = new JSONArray();
        groupFilterList.put(groupFilter);

        JSONObject filterSearchList = new JSONObject();
        filterSearchList.put("groupFilterList", groupFilterList);
        filterSearchList.put("groupJoinOperator", "AND");

        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "JOB");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        return payload;
    }

    public JSONObject createTextFieldFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "JOB");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        // Create filterValue object with type and value
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        
        if ("STRING_LIST".equals(filterValue_TYPE)) {
            String[] values = filterValue.split(",");
            JSONArray valueArray = new JSONArray();
            for (String val : values) {
                valueArray.put(val.trim());
            }
            filterValueObj.put("value", valueArray);
        } else {
            filterValueObj.put("value", filterValue);
        }

        // Create filterSearchList structure
        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();

        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");

        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "jobs");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "job");
        filter.put("fieldType", fieldType);
        filter.put("filterValue", filterValueObj);

        filtersArray.put(filter);
        groupFilterList.put("filters", filtersArray);

        groupFilterListArray.put(groupFilterList);
        filterSearchList.put("groupFilterList", groupFilterListArray);
        filterSearchList.put("groupJoinOperator", "AND");

        payload.put("filterSearchList", filterSearchList);

        return payload;
    }

    public JSONObject createNumberFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject filterValueObj;
        if ("NUMERIC_STRING_START_END".equals(filterValue_TYPE) && "is_between".equals(filterType)) {
            filterValueObj = numericStringStartEndFilterValue(filterValue);
        } else if ("STRING_START_END".equals(filterValue_TYPE) && "is_between".equals(filterType)) {
            filterValueObj = stringStartEndFilterValue(filterValue);
        } else if ("STRING".equals(filterValue_TYPE)) {
            filterValueObj = stringFilterValue(filterValue);
        } else {
            filterValueObj = numericStringFilterValue(filterValue);
        }

        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "jobs");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "job");
        filter.put("fieldType", fieldType);
        filter.put("filterValue", filterValueObj);

        JSONArray filters = new JSONArray();
        filters.put(filter);
        
        JSONObject groupFilter = new JSONObject();
        groupFilter.put("groupFilterJoinOperator", "AND");
        groupFilter.put("filters", filters);
        
        JSONArray groupFilterList = new JSONArray();
        groupFilterList.put(groupFilter);

        JSONObject filterSearchList = new JSONObject();
        filterSearchList.put("groupFilterList", groupFilterList);
        filterSearchList.put("groupJoinOperator", "AND");

        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "JOB");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        return payload;
    }

    public JSONObject createDateFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject filterValueObj;
        
        if ("LONG_START_END".equals(filterValue_TYPE) && "is_between".equals(filterType)) {
            filterValueObj = dateStartEndFilterValue(filterValue);
        } else if ("DATE_IS".equals(filterValue_TYPE)) {
            filterValueObj = dateIsFilterValue(filterValue);
        } else if ("LONG".equals(filterValue_TYPE)) {
            filterValueObj = longFilterValue(filterValue);
        } else if ("INTEGER".equals(filterValue_TYPE)) {
            filterValueObj = integerFilterValue(filterValue);
        } else if (filterValue == null || filterValue.trim().isEmpty()) {
            filterValueObj = emptyFilterValue(filterValue_TYPE);
        } else {
            filterValueObj = dateIsFilterValue(filterValue);
        }

        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "jobs");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "job");
        filter.put("fieldType", fieldType);
        filter.put("filterValue", filterValueObj);

        JSONArray filters = new JSONArray();
        filters.put(filter);
        
        JSONObject groupFilter = new JSONObject();
        groupFilter.put("groupFilterJoinOperator", "AND");
        groupFilter.put("filters", filters);
        
        JSONArray groupFilterList = new JSONArray();
        groupFilterList.put(groupFilter);

        JSONObject filterSearchList = new JSONObject();
        filterSearchList.put("groupFilterList", groupFilterList);
        filterSearchList.put("groupJoinOperator", "AND");

        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "JOB");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        return payload;
    }


    public void createContacts() {
        JSONObject contactJson = readJsonFileFromPath("src/test/resources/testData/contact_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            List<CompletableFuture<Void>> createFutures = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                String contactKey = "contact" + i;
                if (contactJson.has(contactKey)) {
                    createFutures.add(CompletableFuture.runAsync(() -> {
                        JSONObject contactEntry = contactJson.getJSONObject(contactKey);
                        JSONObject payload = contactEntry.getJSONObject("payload");
                        String createdBy = contactEntry.has("createdBy") ? contactEntry.getString("createdBy") : "admin";
                        String authToken = getAlbatrossAuthToken(createdBy);
                        
                        if (payload.has("selectedcompanies")) {
                            JSONArray selectedCompanies = payload.getJSONArray("selectedcompanies");
                            for (int j = 0; j < selectedCompanies.length(); j++) {
                                JSONObject companyInfo = selectedCompanies.getJSONObject(j);
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
                        
                        Response response = RestClient.doPost("JSON", albatrossURL, "/contacts", authToken, null, true, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String slug = jsonPath.getString("data.contact.slug");
                        String contactIdStr = jsonPath.getString("data.contact.id");
                        
                        if (contactIdStr == null) {
                            return;
                        }
                        
                        synchronized (contactKeyToSlugMap) {
                            contactKeyToSlugMap.put(contactKey, slug);
                        }
                        synchronized (contactKeyToIdMap) {
                            contactKeyToIdMap.put(contactKey, contactIdStr);
                        }
                    }, executor));
                }
            }

            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    public Map<String, String> createUserMap() {
        Map<String, String> userMap = new HashMap<>();
        Response response = function.getUsers(baseURL, accountOwnerAPIKey);
        response.then().statusCode(200);
        JsonPath user = response.jsonPath();
        userMap.put("owner", user.get("[0].id").toString());
        userMap.put("admin", user.get("[1].id").toString());
        userMap.put("restrictedTeamMember", user.get("[2].id").toString());
        userMap.put("teamMember", user.get("[3].id").toString());
        return userMap;
    }

    public Map<String, String> createTeamMap() {
        Map<String, String> teamMap = new HashMap<>();
        ArrayList<String> userId = new ArrayList<String>();
        userId.add(String.valueOf(userMap.get("owner")));
        userId.add(String.valueOf(userMap.get("teamMember")));

        Response response = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team1", userId);
        response.then().statusCode(200);
        Response team = function.getTeams(baseURL, accountOwnerAPIKey);
        String teamId = team.jsonPath().getString("[0].team_id");
        teamMap.put("team", teamId);
        return teamMap;
    }

    public Map<String, Integer> createJobStatusMap() {
        Map<String, Integer> statusMap = new HashMap<>();
        try {
            statusMap = function.getJobStatusValues(albatrossURL, albatrossAuthToken);
        } catch (Exception e) {
        }
        return statusMap;
    }

    public Map<String, Integer> createQualificationMap() {
        Map<String, Integer> qualificationMap = new HashMap<>();
        try {
            Map<String, String> authTokenMap = new HashMap<>();
            authTokenMap.put("Authorization", "Bearer " + albatrossAuthToken);
            Response response = RestClient.doPost("JSON", albatrossURL, "qualifications", authTokenMap, null, true, null);
            if (response.getStatusCode() == 200) {
                JSONObject responseJson = new JSONObject(response.getBody().asString());
                JSONArray dataArray = responseJson.getJSONArray("data");
                
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject qualificationObj = dataArray.getJSONObject(i);
                    String qualificationLabel = qualificationObj.getString("label");
                    Integer qualificationId = qualificationObj.getInt("id");
                    qualificationMap.put(qualificationLabel, qualificationId);
                }
            }
        } catch (Exception e) {
        }
        return qualificationMap;
    }

    public Map<String, Integer> createHiringPipelineMap() {
        Map<String, Integer> pipelineMap = new HashMap<>();
        ListFunctions listFunctions = new ListFunctions();
        HiringPipeline hiringFaker = new HiringPipeline();
        
        try {
            JsonPath jsonGetAllCandidateHiringStages = listFunctions
                    .getAllCandidateHiringStages(baseURL, accountOwnerAPIKey).jsonPath();
            ArrayList<Integer> hiringStagesID = jsonGetAllCandidateHiringStages.get("status_id");
            
            if (hiringStagesID == null || hiringStagesID.isEmpty()) {
                return pipelineMap;
            }
            
            for (int pipelineNum = 1; pipelineNum <= 3; pipelineNum++) {
                ArrayList<Object> hiringStagesList = new ArrayList<Object>();
                
                HiringStages stage1 = new HiringStages();
                stage1.setId(10);
                stage1.setSequenceno(0);
                hiringStagesList.add(stage1);
                
                HiringStages stage2 = new HiringStages();
                stage2.setId(1);
                stage2.setSequenceno(1);
                hiringStagesList.add(stage2);
                
                HiringStages stage3 = new HiringStages();
                stage3.setId(8);
                stage3.setSequenceno(55);
                hiringStagesList.add(stage3);
                
                CreateHiringPipeline createHiringPipeline = new CreateHiringPipeline();
                createHiringPipeline.setName(hiringFaker.getHiringPipelineName() + "_Pipeline" + pipelineNum);
                createHiringPipeline.setIs_primary("0");
                createHiringPipeline.setHiring_stages(hiringStagesList);

                Response response = RestClient.doPost("JSON", hiringPipelineServiceURL, "pipelines/add",
                        albatrossAuthToken, null, true, createHiringPipeline);

                if (response.getStatusCode() == 200) {
                    JsonPath jsonPath = response.jsonPath();
                    Integer pipelineId = jsonPath.getInt("id");
                    String pipelineKey = "hiring_pipeline_" + pipelineNum;
                    pipelineMap.put(pipelineKey, pipelineId);
                }
            }
        } catch (Exception e) {
        }
        
        return pipelineMap;
    }

    private void replaceJobPlaceholders(JSONObject job, JSONObject payload) {
        if (job.has("companyid")) {
            String companyIdPlaceholder = job.getString("companyid");
            if (companyIdPlaceholder != null && companyIdPlaceholder.startsWith("{") && companyIdPlaceholder.endsWith("_id}")) {
                String companyKey = companyIdPlaceholder.substring(1, companyIdPlaceholder.length() - 4);
                String actualCompanyId = companyKeyToIdMap.get(companyKey);
                if (actualCompanyId != null) {
                    job.put("companyid", actualCompanyId);
                }
            }
        }
        
        if (job.has("contactid")) {
            String contactIdPlaceholder = job.getString("contactid");
            if (contactIdPlaceholder != null && contactIdPlaceholder.startsWith("{") && contactIdPlaceholder.endsWith("_id}")) {
                String contactKey = contactIdPlaceholder.substring(1, contactIdPlaceholder.length() - 4);
                String actualContactId = contactKeyToIdMap.get(contactKey);
                if (actualContactId != null) {
                    job.put("contactid", actualContactId);
                }
            }
        }
        
        if (job.has("ownerid")) {
            String ownerPlaceholder = job.getString("ownerid");
            if (ownerPlaceholder != null && ownerPlaceholder.startsWith("{") && ownerPlaceholder.endsWith("}")) {
                String ownerKey = ownerPlaceholder.substring(1, ownerPlaceholder.length() - 1);
                String actualOwnerId = userMap.get(ownerKey);
                if (actualOwnerId != null) {
                    job.put("ownerid", Integer.parseInt(actualOwnerId));
                }
            }
        }
        
        if (job.has("qualificationid")) {
            String qualificationPlaceholder = job.getString("qualificationid");
            if (qualificationPlaceholder != null && qualificationPlaceholder.startsWith("{") && qualificationPlaceholder.endsWith("}")) {
                String qualificationLabel = qualificationPlaceholder.substring(1, qualificationPlaceholder.length() - 1);
                Integer qualificationId = qualificationIdMap.get(qualificationLabel);
                if (qualificationId == null) {
                    for (Map.Entry<String, Integer> entry : qualificationIdMap.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(qualificationLabel)) {
                            qualificationId = entry.getValue();
                            break;
                        }
                    }
                }
                if (qualificationId != null) {
                    job.put("qualificationid", qualificationId);
                }
            }
        }
        
        if (job.has("jobstatus")) {
            String jobStatusPlaceholder = job.getString("jobstatus");
            if (jobStatusPlaceholder != null && jobStatusPlaceholder.startsWith("{") && jobStatusPlaceholder.endsWith("}")) {
                String jobStatusLabel = jobStatusPlaceholder.substring(1, jobStatusPlaceholder.length() - 1);
                Integer jobStatusId = jobStatusIdMap.get(jobStatusLabel);
                if (jobStatusId == null) {
                    for (Map.Entry<String, Integer> entry : jobStatusIdMap.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(jobStatusLabel)) {
                            jobStatusId = entry.getValue();
                            break;
                        }
                    }
                }
                if (jobStatusId != null) {
                    job.put("jobstatus", jobStatusId);
                }
            }
        }
        
        if (job.has("hiring_pipeline_id")) {
            Object hiringPipelineValue = job.get("hiring_pipeline_id");
            String hiringPipelinePlaceholder = null;
            
            if (hiringPipelineValue instanceof String) {
                hiringPipelinePlaceholder = (String) hiringPipelineValue;
            } else if (hiringPipelineValue != null) {
                String valueAsString = String.valueOf(hiringPipelineValue);
                if (valueAsString.startsWith("{")) {
                    hiringPipelinePlaceholder = valueAsString;
                }
            }
            
            if (hiringPipelinePlaceholder != null && hiringPipelinePlaceholder.startsWith("{") && hiringPipelinePlaceholder.endsWith("}")) {
                if (hiringPipelinePlaceholder.equals("{default_hiring_pipeline_id}")) {
                    job.put("hiring_pipeline_id", 0);
                } else {
                    String pipelineKey = hiringPipelinePlaceholder.substring(1, hiringPipelinePlaceholder.length() - 1);
                    if (pipelineKey.endsWith("_id")) {
                        pipelineKey = pipelineKey.substring(0, pipelineKey.length() - 3);
                    }
                    
                    Integer actualPipelineId = hiringPipelineIdMap.get(pipelineKey);
                    if (actualPipelineId != null) {
                        job.put("hiring_pipeline_id", actualPipelineId);
                    }
                }
            }
        }
        
        if (payload.has("collaborator")) {
            JSONObject collaborator = payload.getJSONObject("collaborator");
            
            if (collaborator.has("user_ids")) {
                JSONArray userIds = collaborator.getJSONArray("user_ids");
                JSONArray actualUserIds = new JSONArray();
                for (int i = 0; i < userIds.length(); i++) {
                    String userIdPlaceholder = userIds.getString(i);
                    if (userIdPlaceholder != null && userIdPlaceholder.startsWith("{") && userIdPlaceholder.endsWith("}")) {
                        String userIdKey = userIdPlaceholder.substring(1, userIdPlaceholder.length() - 1);
                        String actualUserId = userMap.get(userIdKey);
                        if (actualUserId != null) {
                            actualUserIds.put(Integer.parseInt(actualUserId));
                        }
                    } else {
                        actualUserIds.put(userIdPlaceholder);
                    }
                }
                collaborator.put("user_ids", actualUserIds);
            }
            
            if (collaborator.has("team_ids")) {
                JSONArray teamIds = collaborator.getJSONArray("team_ids");
                JSONArray actualTeamIds = new JSONArray();
                for (int i = 0; i < teamIds.length(); i++) {
                    String teamIdPlaceholder = teamIds.getString(i);
                    if (teamIdPlaceholder != null && teamIdPlaceholder.startsWith("{") && teamIdPlaceholder.endsWith("}")) {
                        String teamKey = teamIdPlaceholder.substring(1, teamIdPlaceholder.length() - 1);
                        String actualTeamId = teamMap.get(teamKey);
                        if (actualTeamId != null) {
                            actualTeamIds.put(Integer.parseInt(actualTeamId));
                        }
                    } else {
                        actualTeamIds.put(teamIdPlaceholder);
                    }
                }
                collaborator.put("team_ids", actualTeamIds);
            }
        }
        
        if (payload.has("targetcompanies")) {
            JSONArray targetCompanies = payload.getJSONArray("targetcompanies");
            for (int i = 0; i < targetCompanies.length(); i++) {
                JSONObject targetCompany = targetCompanies.getJSONObject(i);
                
                if (targetCompany.has("slug")) {
                    String slugPlaceholder = targetCompany.getString("slug");
                    if (slugPlaceholder != null && slugPlaceholder.startsWith("{") && slugPlaceholder.endsWith("_slug}")) {
                        String companyKey = slugPlaceholder.substring(1, slugPlaceholder.length() - 6);
                        String actualSlug = companyKeyToSlugMap.get(companyKey);
                        if (actualSlug != null) {
                            targetCompany.put("slug", actualSlug);
                        }
                    }
                }
                
                if (targetCompany.has("id")) {
                    String idPlaceholder = targetCompany.getString("id");
                    if (idPlaceholder != null && idPlaceholder.startsWith("{") && idPlaceholder.endsWith("_id}")) {
                        String companyKey = idPlaceholder.substring(1, idPlaceholder.length() - 4);
                        String actualId = companyKeyToIdMap.get(companyKey);
                        if (actualId != null) {
                            targetCompany.put("id", actualId);
                        }
                    }
                }
            }
        }
    }

    private void updateJobCustomFields(int jobId, JSONObject fieldsAndTimestamps) {
        // Use global/update-fields endpoint for jobs
        UpdateFields updateFields = new UpdateFields();
        updateFields.setTableFlag("job");
        updateFields.setId(Arrays.asList(jobId));
        
        for (String key : fieldsAndTimestamps.keySet()) {
            updateFields.setKey(key);
            updateFields.setValue(fieldsAndTimestamps.get(key).toString());
            String basePath = "global/update-fields";
            Response updateResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, true, updateFields);
            if (updateResponse.getStatusCode() != 200) {
                Assert.fail("Failed to update custom field " + key + " for job: " + jobId);
            }
        }
    }

    public String getAlbatrossAuthToken(String createdBy) {
        switch (createdBy) {
            case "owner":
                return ownerAlbatrossAuthToken;
            case "admin":
                return adminAlbatrossAuthToken;
            case "teamMember":
                return teamMemberAlbatrossAuthToken;
            case "restrictedTeamMember":
                return restrictedTeamMemberAlbatrossAuthToken;
            default:
                return ownerAlbatrossAuthToken; // Default to owner token
        }
    }
}
