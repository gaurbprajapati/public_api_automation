package io.recruitcrm.CandidateService.Filters;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.rcrm.api.testbase.TestBase.AccountType;
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
import com.qa.api.util.reaper.ReaperIntegration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CustomFieldCandidateFilterSearchTest extends FilterSearchBaseTest {
    private static final List<String> ENTITY_ASSOCIATION_CF_KEYS = Arrays.asList(
            "candidateCF", "companyCF", "dealCF", "jobCF", "contactCF", "userCF", "teamCF");

    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String albatrossAuthToken;
    String email;
    String accountOwnerAPIKey;
    Map<String, Integer> customFieldIds = new HashMap<>();
    Map<String, String> entityCFValueMap = new HashMap<>();
    ConcurrentHashMap<String, String> candidateKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> candidateIdToKeyMap = new ConcurrentHashMap<>();
    Map<String, Map<String, String>> timestampScenarios;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        customFieldIds = createCustomFields();
        createEntityCFValueMap();
        createTestData();
        waitForDataSync();
    }
    
    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "customFieldFilterSearchTestData", description = "Filter Search Test for Custom Fields")
    public void entityCustomFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("TestCaseId: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ", email);
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "candidatename");
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);

        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, candidateKeyToIdMap, candidateIdToKeyMap, 17, "Candidate");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "dropdownAndMultiselectFilterSearchTestData", description = "Filter Search Test for Dropdown and Multiselect Custom Fields")
    public void dropdownAndMultiselectFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("TestCaseId: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ", email);
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        JSONObject payload = createDropdownAndMultiselectFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "candidatename");
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);

        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, candidateKeyToIdMap, candidateIdToKeyMap, 17, "Candidate");
    }

    @DataProvider(name = "customFieldFilterSearchTestData", parallel = true)
    public Object[][] customFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            if (!ENTITY_ASSOCIATION_CF_KEYS.contains(key)) {
                continue;
            }
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String fieldTypeJson = test.getString("fieldType");
                String filterType = test.getString("filterType");
                String filterValue = test.getString("filterValue");
                String dbField = test.getString("dbField");
                if (fieldTypeJson.equals("multiselect") || fieldTypeJson.equals("dropdown")) {
                    String apiFieldType = resolveSearchFieldFieldType(key, fieldTypeJson);
                    testData.add(new Object[]{
                            key,
                            filterType,
                            filterValue,
                            dbField,
                            test.getString("expectedResult"),
                            apiFieldType,
                            test.optString("filterValue_TYPE", "STRING_LIST"),
                            test.optString("testCaseId", "")
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "dropdownAndMultiselectFilterSearchTestData", parallel = true)
    public Object[][] dropdownAndMultiselectFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : Arrays.asList("dropdownCF", "multiselectCF")) {
            if (!filterData.has(key)) {
                continue;
            }
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{
                        key,
                        test.getString("filterType"),
                        test.getString("filterValue"),
                        test.getString("dbField"),
                        test.getString("expectedResult"),
                        test.getString("fieldType"),
                        test.optString("filterValue_TYPE", "STRING_LIST"),
                        test.optString("testCaseId", "")
                });
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "textCustomFieldFilterSearchTestData", description = "Filter Search Test for Text Custom Fields")
    public void textCustomFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("TestCaseId: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ", email);
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        JSONObject payload = createTextFieldFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);

        validateTextFieldFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "numberCustomFieldFilterSearchTestData", description = "Filter Search Test for Number Custom Fields")
    public void numberCustomFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("TestCaseId: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ", email);
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        JSONObject payload = createNumberFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);

        validateNumberFieldFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    }

   @Owner("Raj Pandey")
   @Test(groups = {"aries_service"}, dataProvider = "dateCustomFieldFilterSearchTestData", description = "Filter Search Test for Date Custom Fields")
   public void dateCustomFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
       FilterSearchReporter.logInfo("TestCaseId: ", testCaseId);
       FilterSearchReporter.logInfo("Account: ", email);
       dbField = "custcolumn" + customFieldIds.get(fieldName);
       JSONObject payload = createDateFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
       FilterSearchReporter.logPayload(payload);
       Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
       Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
       Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

       JSONArray data = getFilteredData(response);
       FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);

       validateDateFieldFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
   }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "checkboxCustomFieldFilterSearchTestData", description = "Filter Search Test for Checkbox Custom Fields")
    public void checkboxCustomFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("TestCaseId: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ", email);
        dbField = "custcolumn" + customFieldIds.get(fieldName);
        JSONObject payload = createCheckboxFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "candidatename");
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);

        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, candidateKeyToIdMap, candidateIdToKeyMap, 17, "Candidate");
    }

    @DataProvider(name = "checkboxCustomFieldFilterSearchTestData", parallel = true)
    public Object[][] checkboxCustomFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        if (!filterData.has("checkboxCF")) {
            return testData.toArray(new Object[0][0]);
        }
        JSONArray tests = filterData.getJSONArray("checkboxCF");
        for (int i = 0; i < tests.length(); i++) {
            JSONObject test = tests.getJSONObject(i);
            testData.add(new Object[]{
                    "checkboxCF",
                    test.getString("filterType"),
                    test.getString("filterValue"),
                    test.getString("dbField"),
                    test.getString("expectedResult"),
                    test.getString("fieldType"),
                    test.getString("filterValue_TYPE"),
                    test.optString("testCaseId", "")
            });
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "dateCustomFieldFilterSearchTestData", parallel = true)
    public Object[][] dateCustomFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                if (tests.getJSONObject(i).getString("fieldType").equals("date")) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{
                            key,
                            test.getString("filterType"),
                            test.getString("filterValue"),
                            test.getString("dbField"),
                            test.getString("expectedResult"),
                            test.getString("fieldType"),
                            test.optString("filterValue_TYPE", "LONG"),
                            test.optString("testCaseId", "")
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }


    

    @DataProvider(name = "numberCustomFieldFilterSearchTestData", parallel = true)
    public Object[][] numberCustomFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                if (tests.getJSONObject(i).getString("fieldType").equals("number")) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{
                            key,
                            test.getString("filterType"),
                            test.getString("filterValue"),
                            test.getString("dbField"),
                            test.getString("expectedResult"),
                            test.getString("fieldType"),
                            test.optString("filterValue_TYPE", "NUMERIC_STRING"),
                            test.optString("testCaseId", "")
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }
    

    @DataProvider(name = "textCustomFieldFilterSearchTestData", parallel = true)
    public Object[][] textCustomFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateCustomFieldFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                String ft = tests.getJSONObject(i).getString("fieldType");
                if (ft.equals("text") || ft.equals("phonenumber") || ft.equals("longtext") || ft.equals("email") || ft.equals("social_profile")) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{
                            key,
                            test.getString("filterType"),
                            test.getString("filterValue"),
                            test.getString("dbField"),
                            test.getString("expectedResult"),
                            test.getString("fieldType"),
                            test.optString("filterValue_TYPE", "STRING"),
                            test.optString("testCaseId", "")
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public void validateTextFieldFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField,String expectedResult) {
        if(expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Wrong candidate data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && data.length() == 0){
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }
        
        for (int i = 0; i < data.length(); i++) {
            JSONObject candidate = data.getJSONObject(i);
            String candidateData = candidate.optString(dbField, "").trim();
            String trimmedFilterValue = filterValue.trim();
            Assert.assertTrue(
                    validateTextAgainstFilter(candidateData, filterType, trimmedFilterValue, fieldName),
                    "Wrong candidate data for field: " + fieldName + " and filterType: " + filterType
                            + " and filterValue: " + trimmedFilterValue + ". Field value '" + candidateData + "'");
        }
    }

    public void validateNumberFieldFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty") || expectedResult.equals("Not available records")) {
            Assert.assertEquals(data.length(), 0, "Wrong candidate data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue + ". Expected empty results but got " + data.length() + " results");
            return;
        } else if (!expectedResult.equals("Empty") && data.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue + ". Expected results but got empty array");
        }
        
        for (int i = 0; i < data.length(); i++) {
            JSONObject candidate = data.getJSONObject(i);
            String candidateNumber = candidate.optString(dbField, "").trim();
            
            // Handle null or empty values for is_mt and is_lt filters
            if (candidateNumber.isEmpty() || candidateNumber.equals("null")) {
                if (filterType.equals("is_mt") || filterType.equals("is_lt") || filterType.equals("is_not") || filterType.equals("is_empty")) {
                    continue;
                }
            }
            
            if (candidateNumber.endsWith(".0")) {
                candidateNumber = candidateNumber.substring(0, candidateNumber.length() - 2);
            }
            
            double candidateNumberInt = Double.parseDouble(candidateNumber);
            if (filterType.equals("is_between")) {
                String[] rangeParts = filterValue.split(",");
                double startValue = Double.parseDouble(rangeParts[0].trim());
                double endValue = Double.parseDouble(rangeParts[1].trim());
                Assert.assertTrue(candidateNumberInt >= startValue && candidateNumberInt <= endValue, 
                    "Candidate number " + candidateNumberInt + " for field: " + fieldName + " is not between " + startValue + " and " + endValue);
                return;
            }

            double filterValueInt = 0;
            if (!filterValue.isEmpty() && !filterValue.equals(" ")) {
                filterValueInt = Double.parseDouble(filterValue);
            }
            if (filterType.equals("is")) {
                Assert.assertEquals(filterValue, candidateNumber, "Candidate number for field: " + fieldName + " is not equal to " + filterValue);
            }
            else if (filterType.equals("is_not")) {
                Assert.assertNotEquals(filterValue, candidateNumber, "Candidate number for field: " + fieldName + " is equal to " + filterValue);
            }
            else if (filterType.equals("is_mt")) {
                Assert.assertTrue(candidateNumberInt > filterValueInt, "Candidate number for field: " + fieldName + " is not greater than " + filterValue);
            }
            else if (filterType.equals("is_lt")) {  
                Assert.assertTrue(candidateNumberInt < filterValueInt, "Candidate number for field: " + fieldName + " is not less than " + filterValue);
            }
            else if (filterType.equals("begins_with")) {
                Assert.assertTrue(candidateNumber.startsWith(filterValue), "Candidate number for field: " + fieldName + " does not start with " + filterValue);
            }
            else if (filterType.equals("ends_with")) {
                Assert.assertTrue(candidateNumber.endsWith(filterValue), "Candidate number for field: " + fieldName + " does not end with " + filterValue);
            }
            else if (filterType.equals("has_any_value")) {
                Assert.assertNotNull(candidateNumber, "Candidate number for field: " + fieldName + " should not be null");
                Assert.assertFalse(candidateNumber.trim().isEmpty(), "Candidate number for field: " + fieldName + " should not be empty");
            }
            else if (filterType.equals("is_empty")) {
                Assert.assertTrue(candidateNumber.trim().isEmpty(), "Candidate number for field: " + fieldName + " is not equal to 0");
            }
        }
    }

    public void createTestData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/candidateCF_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = candidateJson.keySet().stream()
                .filter(key -> key.startsWith("candidate"))
                .map(candidateKey -> CompletableFuture.runAsync(() -> {
                    JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                    JSONObject payload = candidateEntry.getJSONObject("payload");
                    JSONObject processedPayload = processPayloadPlaceholders(payload);
                    Response response = allCrudFunctions.createCandidateWithJson(albatrossURL, albatrossAuthToken, processedPayload);
                    int candidateId = response.jsonPath().getInt("data.candidate.id");
                    candidateIdToKeyMap.put(String.valueOf(candidateId), candidateKey);
                    candidateKeyToIdMap.put(candidateKey, String.valueOf(candidateId));
                }, executor))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        updateCandidateCustomDateFieldsTimestamps();
    }

    private void updateCandidateCustomDateFieldsTimestamps() {
        timestampScenarios = createTimestampScenarios();
        System.out.println("timestampScenarios: " + timestampScenarios);
        String dateCFFieldName = "dateCF";
        
        if (!customFieldIds.containsKey(dateCFFieldName)) {
            return; // No date custom field created
        }
        
        String dateCFDbField = "custcolumn" + customFieldIds.get(dateCFFieldName);
        List<Map.Entry<String, Map<String, String>>> scenarios = new ArrayList<>(timestampScenarios.entrySet());
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            int candidateIndex = 0;
            
            for (Map.Entry<String, Map<String, String>> scenario : scenarios) {
                if (candidateIndex >= candidateKeyToIdMap.size()) {
                    break;
                }
                
                final int idx = candidateIndex;
                final Map<String, String> timestamps = scenario.getValue();
                
                futures.add(CompletableFuture.runAsync(() -> {
                    String candidateKey = "candidate" + (idx + 1);
                    String candidateIdStr = candidateKeyToIdMap.get(candidateKey);
                    
                    if (candidateIdStr == null) {
                        return;
                    }
                    
                    int candidateId = Integer.parseInt(candidateIdStr);
                    String dateValue = timestamps.get(dateCFFieldName);
                    
                    if (dateValue != null) {
                        JSONObject fieldsAndTimestamps = new JSONObject();
                        fieldsAndTimestamps.put(dateCFDbField, dateValue);
                        
                        Response updateResponse = ReaperIntegration.updateCandidateTimestamp(candidateId, fieldsAndTimestamps);
                        if (updateResponse.getStatusCode() != 200) {
                            Assert.fail("Failed to update custom date field for candidate: " + candidateId);
                        }
                    }
                }, executor));
                
                candidateIndex++;
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
            int entityCandidate1Id = function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson1.getString("slug"));
            int entityCandidate2Id = function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson2.getString("slug"));
            candidateKeyToIdMap.put("entityCandidate1", String.valueOf(entityCandidate1Id));
            candidateKeyToIdMap.put("entityCandidate2", String.valueOf(entityCandidate2Id));
            candidateKeyToIdMap.put("entitycandidate1", String.valueOf(entityCandidate1Id));
            candidateKeyToIdMap.put("entitycandidate2", String.valueOf(entityCandidate2Id));
            candidateIdToKeyMap.put(String.valueOf(entityCandidate1Id), "entityCandidate1");
            candidateIdToKeyMap.put(String.valueOf(entityCandidate2Id), "entityCandidate2");
        } finally {
            executor.shutdown();
        }
    }

    public Map<String, Integer> createCustomFields() {
        Map<String, Integer> customFieldIds = new HashMap<>();
        commanFunction function = new commanFunction();
        int colId = 1;

        List<String> entityTypes = new ArrayList<>(List.of(
                "candidate", "company", "deal", "job", "contact", "user", "team",
                "text", "email", "phonenumber", "longtext", "number", "date", "social_profile",
                "dropdown", "multiselect", "checkbox"));

        for (String entity : entityTypes) {
            String fieldName = entity + "CF";
            Response response;

            if (entity.equals("dropdown") || entity.equals("multiselect")) {
                response = function.createCustomFieldsResponse(
                        albatrossURL, albatrossAuthToken, "candidate", fieldName, entity, "Option A, Option AB, Option ABC",colId);
            } else {
                response = function.createCustomFieldsResponse(
                        albatrossURL, albatrossAuthToken, "candidate", fieldName, entity, "", colId);
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
            if (key.startsWith("{") && key.endsWith("}")) {
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
            } else {
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

    private String resolveSearchFieldFieldType(String sectionKey, String jsonFieldType) {
        if ("multiselect".equals(jsonFieldType) || "dropdown".equals(jsonFieldType)) {
            if ("dealCF".equals(sectionKey)) {
                return "deals";
            }
            if (sectionKey.endsWith("CF")) {
                return sectionKey.substring(0, sectionKey.length() - 2);
            }
        }
        return jsonFieldType;
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        String processedFilterValue = processEntityPlaceholders(filterValue);

        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        JSONArray valueArray = new JSONArray();
        if (filterType.equals("is_empty") || filterType.equals("has_any_value")) {
            filterValueObj.put("value", valueArray);
        } else {
            String[] values = processedFilterValue.split(",");
            for (String val : values) {
                if ("INTEGER_LIST".equals(filterValue_TYPE)) {
                    valueArray.put(Integer.parseInt(val.trim()));
                } else {
                    valueArray.put(val.trim());
                }
            }
            filterValueObj.put("value", valueArray);
        }

        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "candidates");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "candidate");
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
        payload.put("advancedSearchContext", "CANDIDATE");
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
        filter.put("groupType", "candidates");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "candidate");
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
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        return payload;
    }

    public JSONObject createCheckboxFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        if ("INTEGER".equals(filterValue_TYPE)) {
            filterValueObj.put("value", Integer.parseInt(filterValue.trim()));
        } else {
            JSONArray valueArray = new JSONArray();
            valueArray.put(Integer.parseInt(filterValue.trim()));
            filterValueObj.put("value", valueArray);
        }

        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "candidates");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "candidate");
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
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        return payload;
    }

    public JSONObject createTextFieldFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

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

        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();

        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");

        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "candidates");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "candidate");
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
        filter.put("groupType", "candidates");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "candidate");
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
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        return payload;
    }

    public JSONObject createDateFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject filterValueObj;

        if (filterValue == null || filterValue.trim().isEmpty()) {
            filterValueObj = emptyFilterValue(filterValue_TYPE);
        } else if ("LONG_START_END".equals(filterValue_TYPE) && "is_between".equals(filterType)) {
            filterValueObj = dateStartEndFilterValue(filterValue);
        } else if ("DATE_IS".equals(filterValue_TYPE)) {
            filterValueObj = dateIsFilterValue(filterValue);
        } else if ("LONG".equals(filterValue_TYPE)) {
            filterValueObj = longFilterValue(filterValue);
        } else if ("INTEGER".equals(filterValue_TYPE)) {
            filterValueObj = integerFilterValue(filterValue);
        } else {
            filterValueObj = dateIsFilterValue(filterValue);
        }

        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "candidates");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "candidate");
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
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        return payload;
    }

    public void validateDateFieldFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Wrong candidate data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (data.isEmpty()) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (int i = 0; i < data.length(); i++) {
            JSONObject candidate = data.getJSONObject(i);
            String candidateDateStr = candidate.optString(dbField, "").trim();

            switch (filterType) {
                case "is":
                case "is_equal_to":
                    validateExactDateMatch(candidateDateStr, filterValue, fieldName, filterType);
                    break;
                case "is_not":
                    validateDateNotEqual(candidateDateStr, filterValue, fieldName, filterType);
                    break;
                case "is_before":
                    validateDateBefore(candidateDateStr, filterValue, fieldName, filterType);
                    break;
                case "is_after":
                    validateDateAfter(candidateDateStr, filterValue, fieldName, filterType);
                    break;
                case "is_between":
                    validateDateBetween(candidateDateStr, filterValue, fieldName, filterType);
                    break;
                case "is_mt":
                    validateDateMoreThanDaysAgo(candidateDateStr, filterValue, fieldName, filterType);
                    break;
                case "is_lt":
                    validateDateLessThanDaysAgo(candidateDateStr, filterValue, fieldName, filterType);
                    break;
                case "has_any_value":
                    Assert.assertNotEquals(candidateDateStr, "", "Wrong candidate data for field: " + fieldName + " - expected non-empty date value");
                    Assert.assertNotEquals(candidateDateStr, "null", "Wrong candidate data for field: " + fieldName + " - expected non-empty date value");
                    break;
                case "is_empty":
                    Assert.assertTrue(candidateDateStr.isEmpty() || candidateDateStr.equals("null"), "Wrong candidate data for field: " + fieldName + " - expected empty date value");
                    break;
                default:
                    Assert.fail("Unsupported filter type: " + filterType + " for field: " + fieldName);
            }
        }
    }

    private void validateExactDateMatch(String candidateDate, String filterValue, String fieldName, String filterType) {
        if (candidateDate.isEmpty() || candidateDate.equals("null")) {
            Assert.fail("Candidate date is empty for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        LocalDate candidateParsedDate = parseDate(candidateDate);
        if (isRelativeDatePeriod(filterValue)) {
            validateDateInPeriod(candidateParsedDate, filterValue, fieldName, filterType);
        } else {
            LocalDate filterParsedDate = parseDate(filterValue);
            Assert.assertEquals(candidateParsedDate, filterParsedDate, "Wrong candidate data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }
    }

    private void validateDateNotEqual(String candidateDate, String filterValue, String fieldName, String filterType) {
        if (candidateDate.isEmpty() || candidateDate.equals("null")) {
            return;
        }

        LocalDate candidateParsedDate = parseDate(candidateDate);
        LocalDate filterParsedDate = parseDate(filterValue);

        Assert.assertNotEquals(candidateParsedDate, filterParsedDate, "Wrong candidate data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
    }

    private void validateDateBefore(String candidateDate, String filterValue, String fieldName, String filterType) {
        if (candidateDate.isEmpty() || candidateDate.equals("null")) {
            Assert.fail("Candidate date is empty for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        LocalDate candidateParsedDate = parseDate(candidateDate);
        LocalDate filterParsedDate = parseDate(filterValue);

        Assert.assertTrue(candidateParsedDate.isBefore(filterParsedDate), "Wrong candidate data for field: " + fieldName + " - candidate date " + candidateParsedDate + " should be before " + filterValue);
    }

    private void validateDateAfter(String candidateDate, String filterValue, String fieldName, String filterType) {
        if (candidateDate.isEmpty() || candidateDate.equals("null")) {
            Assert.fail("Candidate date is empty for field: " + fieldName +
                    " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        LocalDate candidateParsedDate = parseDate(candidateDate);
        LocalDate filterParsedDate = parseDate(filterValue);

        Assert.assertTrue(candidateParsedDate.isAfter(filterParsedDate) || candidateParsedDate.isEqual(filterParsedDate), "Wrong candidate data for field: " + fieldName + " - candidate date " + candidateParsedDate + " should be after or equal to " + filterValue);
    }

    private void validateDateBetween(String candidateDate, String filterValue, String fieldName, String filterType) {
        if (candidateDate.isEmpty() || candidateDate.equals("null")) {
            Assert.fail("Candidate date is empty for field: " + fieldName +
                    " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        String[] dates = filterValue.split(",");
        if (dates.length != 2) {
            Assert.fail("Invalid filter value for is_between: " + filterValue + ". Expected format: 'startDate,endDate'");
        }

        LocalDate candidateParsedDate = parseDate(candidateDate);
        LocalDate startDate = parseDate(dates[0].trim());
        LocalDate endDate = parseDate(dates[1].trim());

        Assert.assertTrue(
                (candidateParsedDate.isEqual(startDate) || candidateParsedDate.isAfter(startDate)) &&
                        (candidateParsedDate.isEqual(endDate) || candidateParsedDate.isBefore(endDate)), "Wrong candidate data for field: " + fieldName + " - candidate date " + candidateParsedDate + " should be between " + dates[0] + " and " + dates[1]);
    }

    private void validateDateInPeriod(LocalDate candidateDate, String period, String fieldName, String filterType) {
        LocalDate startDate;
        LocalDate endDate;
        LocalDate now = LocalDate.now();

        switch (period) {
            case "all_time":
                Assert.assertNotNull(candidateDate, "Candidate date should not be null for field: " + fieldName + " with all_time filter");
                return;
            case "today":
                startDate = endDate = now;
                break;
            case "yesterday":
                startDate = endDate = now.minusDays(1);
                break;
            case "this_week":
                startDate = now.minusDays(now.getDayOfWeek().getValue() - 1);
                endDate = startDate.plusDays(6);
                break;
            case "last_week":
                LocalDate lastWeekStart = now.minusDays(now.getDayOfWeek().getValue() + 6);
                startDate = lastWeekStart;
                endDate = lastWeekStart.plusDays(6);
                break;
            case "this_month":
                startDate = now.withDayOfMonth(1);
                endDate = now.withDayOfMonth(now.lengthOfMonth());
                break;
            case "last_month":
                LocalDate lastMonth = now.minusMonths(1);
                startDate = lastMonth.withDayOfMonth(1);
                endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
                break;
            case "this_quarter":
                int currentQuarter = (now.getMonthValue() - 1) / 3 + 1;
                int quarterStartMonth = (currentQuarter - 1) * 3 + 1;
                startDate = now.withMonth(quarterStartMonth).withDayOfMonth(1);
                endDate = now.withMonth(quarterStartMonth + 2).withDayOfMonth(now.withMonth(quarterStartMonth + 2).lengthOfMonth());
                break;
            case "last_quarter":
                int lastQuarter = (now.getMonthValue() - 1) / 3;
                if (lastQuarter == 0) {
                    lastQuarter = 4;
                    now = now.minusYears(1);
                }
                int lastQuarterStartMonth = (lastQuarter - 1) * 3 + 1;
                startDate = now.withMonth(lastQuarterStartMonth).withDayOfMonth(1);
                endDate = now.withMonth(lastQuarterStartMonth + 2).withDayOfMonth(now.withMonth(lastQuarterStartMonth + 2).lengthOfMonth());
                break;
            case "this_year":
                startDate = now.withDayOfYear(1);
                endDate = now.withDayOfYear(now.lengthOfYear());
                break;
            case "last_year":
                LocalDate lastYear = now.minusYears(1);
                startDate = lastYear.withDayOfYear(1);
                endDate = lastYear.withDayOfYear(lastYear.lengthOfYear());
                break;
            case "last_30":
                endDate = now;
                startDate = endDate.minusDays(30);
                break;
            case "last_60":
                endDate = now;
                startDate = endDate.minusDays(60);
                break;
            case "last_90":
                endDate = now;
                startDate = endDate.minusDays(90);
                break;
            case "last_365":
                endDate = now;
                startDate = endDate.minusDays(365);
                break;
            default:
                Assert.fail("Unsupported relative date period: " + period);
                return;
        }

        boolean isInPeriod = !candidateDate.isBefore(startDate) && !candidateDate.isAfter(endDate);
        Assert.assertTrue(isInPeriod,
                "Wrong candidate data for field: " + fieldName + " - candidate date " + candidateDate +
                        " should be within period '" + period + "' (between " + startDate + " and " + endDate + ")");
    }

    private void validateDateMoreThanDaysAgo(String candidateDate, String daysStr, String fieldName, String filterType) {
        if (candidateDate.isEmpty() || candidateDate.equals("null")) {
            Assert.fail("Candidate date is empty for field: " + fieldName +
                    " and filterType: " + filterType + " and filterValue: " + daysStr);
        }
        try {
            int days = Integer.parseInt(daysStr);
            LocalDate candidateParsedDate = parseDate(candidateDate);
            LocalDate cutoffDate = LocalDate.now().minusDays(days);

            Assert.assertTrue(candidateParsedDate.isBefore(cutoffDate) || candidateParsedDate.isEqual(cutoffDate),
                    "Wrong candidate data for field: " + fieldName + " - candidate date " + candidateParsedDate +
                            " should be more than " + days + " days ago (on or before " + cutoffDate + ")");
        } catch (NumberFormatException e) {
            Assert.fail("Invalid days value for is_mt filter: " + daysStr);
        }
    }

    private void validateDateLessThanDaysAgo(String candidateDate, String daysStr, String fieldName, String filterType) {
        if (candidateDate.isEmpty() || candidateDate.equals("null")) {
            Assert.fail("Candidate date is empty for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + daysStr);
        }
        try {
            int days = Integer.parseInt(daysStr);
            LocalDate candidateParsedDate = parseDate(candidateDate);
            LocalDate cutoffDate = LocalDate.now().minusDays(days);

            Assert.assertTrue(candidateParsedDate.isAfter(cutoffDate) || candidateParsedDate.isEqual(cutoffDate),
                    "Wrong candidate data for field: " + fieldName + " - candidate date " + candidateParsedDate +
                            " should be less than " + days + " days ago (on or after " + cutoffDate + ")");
        } catch (NumberFormatException e) {
            Assert.fail("Invalid days value for is_lt filter: " + daysStr);
        }
    }
}