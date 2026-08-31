package io.recruitcrm.CandidateService.Filters;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.reaper.ReaperIntegration;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.DateUtil;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class EducationAndWorkHistoryFieldCandidateFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions function = new AllCrudFunctions();
    String albatrossAuthToken;
    String email;
    String apiKey;
    commanFunction commanFunction = new commanFunction();
    Map<String, Map<String, String>> timestampScenarios;

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
    };

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        apiKey = ThreadManager.getAccountApiKey();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "historyDateFieldFilterSearchTestData", description = "Filter Search Test for History Date Fields")
    public void historyDateFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logInfo("filterValue_TYPE: ", filterValue_TYPE);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "candidatename");
        

        validateHistoryDateFieldData(data, fieldName, filterType, filterValue, expectedResult);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "historyTextFieldFilterSearchTestData", description = "Filter Search Test for History Text Fields")
    public void historyTextFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String filterValue_TYPE) {
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logInfo("filterValue_TYPE: ", filterValue_TYPE);
        JSONObject payload = createTextFilterSearchPayload(fieldName, filterType, filterValue, dbField, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "candidatename");

        validateHistoryTextFieldData(data, fieldName, filterType, filterValue, expectedResult, dbField);
    }

    @DataProvider(name = "historyDateFieldFilterSearchTestData", parallel = true)
    public Object[][] historyDateDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateHistoryTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String fieldType = test.getString("fieldType");
                if ("date".equals(fieldType)) {
                    String filterType = test.getString("filterType");
                    String filterValue = test.getString("filterValue");
                    
                    testData.add(new Object[]{
                        key,
                        filterType,
                        filterValue,
                        test.getString("dbField"),
                        test.getString("expectedResult"),
                        test.getString("fieldType"),
                        test.getString("filterValue_TYPE")
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "historyTextFieldFilterSearchTestData", parallel = true)
    public Object[][] historyTextDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateHistoryTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String fieldType = test.getString("fieldType");
                if ("text".equals(fieldType)) {
                    String filterType = test.getString("filterType");
                    String filterValue = test.getString("filterValue");
                    
                    testData.add(new Object[]{
                        key,
                        filterType,
                        filterValue,
                        test.getString("dbField"),
                        test.getString("expectedResult"),
                        test.getString("filterValue_TYPE")
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public void createTestData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/candidate_data.json");

        ExecutorService executor = Executors.newFixedThreadPool(5);
        ConcurrentMap<String, Integer> candidateIdMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> candidateSlugMap = new ConcurrentHashMap<>();
        try {
            CompletableFuture.allOf(candidateJson.keySet().stream()
                    .filter(key -> key.startsWith("candidate"))
                    .map(candidateKey -> CompletableFuture.runAsync(() -> {
                        JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                        JSONObject payload = candidateEntry.getJSONObject("payload");
                        Response response = function.createCandidateWithJson(albatrossURL, albatrossAuthToken, payload);
                        candidateIdMap.put(candidateKey, response.jsonPath().getInt("data.candidate.id"));
                        candidateSlugMap.put(candidateKey, response.jsonPath().getString("data.candidate.slug"));
                    }, executor)).toArray(CompletableFuture[]::new)).join();

            List<Integer> candidateIds = new ArrayList<>(candidateIdMap.values());
            
            updateCandidatesWithHistoryTimestamps(candidateIds);
            
        } finally {
            executor.shutdown();
        }
    }
    
    private void updateCandidatesWithHistoryTimestamps(List<Integer> candidateIds) {
        timestampScenarios = createHistoryTimestampScenarios();
        
        int candidateIndex = 0;
        for (Map.Entry<String, Map<String, String>> scenario : timestampScenarios.entrySet()) {
            if (candidateIndex >= timestampScenarios.size()) {
                break;
            }
            
            Map<String, String> timestamps = scenario.getValue();
            Integer candidateId = candidateIds.get(candidateIndex);
            
            JSONObject fieldsAndTimestamps = new JSONObject();
            for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                fieldsAndTimestamps.put(timestamp.getKey(), timestamp.getValue());
            }
            
            try {
                Response updateResponse = ReaperIntegration.updateCandidateTimestamp(candidateId, fieldsAndTimestamps);
                if(updateResponse.getStatusCode()!=200) {
                    Assert.fail("Failed to update the candidate history timestamps");
                }
            } catch (Exception e) {
                Assert.fail("Failed to update the candidate history timestamps");
            }
            
            candidateIndex++;
        }
        
    }
    
    private Map<String, Map<String, String>> createHistoryTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();
        
        // Today scenario
        String todayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getTodayDateString("yyyy-MM-dd")));
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("EducationStartDate", todayEpoch);
        todayTimestamps.put("EducationEndDate", todayEpoch);
        todayTimestamps.put("WorkStartDate", todayEpoch);
        todayTimestamps.put("WorkEndDate", todayEpoch);
        scenarios.put("today_scenario", todayTimestamps);
        
        // Yesterday scenario
        String yesterdayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getYesterdayDateString("yyyy-MM-dd")));
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("EducationStartDate", yesterdayEpoch);
        yesterdayTimestamps.put("EducationEndDate", yesterdayEpoch);
        yesterdayTimestamps.put("WorkStartDate", yesterdayEpoch);
        yesterdayTimestamps.put("WorkEndDate", yesterdayEpoch);
        scenarios.put("yesterday_scenario", yesterdayTimestamps);
        
        // This week scenario
        String thisWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisWeekDateString()));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("EducationStartDate", thisWeekEpoch);
        thisWeekTimestamps.put("EducationEndDate", thisWeekEpoch);
        thisWeekTimestamps.put("WorkStartDate", thisWeekEpoch);
        thisWeekTimestamps.put("WorkEndDate", thisWeekEpoch);
        scenarios.put("this_week_scenario", thisWeekTimestamps);
        
        // Last week scenario
        String lastWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastWeekDateString()));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("EducationStartDate", lastWeekEpoch);
        lastWeekTimestamps.put("EducationEndDate", lastWeekEpoch);
        lastWeekTimestamps.put("WorkStartDate", lastWeekEpoch);
        lastWeekTimestamps.put("WorkEndDate", lastWeekEpoch);
        scenarios.put("last_week_scenario", lastWeekTimestamps);
        
        // This month scenario
        String thisMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisMonthDateString()));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("EducationStartDate", thisMonthEpoch);
        thisMonthTimestamps.put("EducationEndDate", thisMonthEpoch);
        thisMonthTimestamps.put("WorkStartDate", thisMonthEpoch);
        thisMonthTimestamps.put("WorkEndDate", thisMonthEpoch);
        scenarios.put("this_month_scenario", thisMonthTimestamps);
        
        // Last month scenario
        String lastMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastMonthDateString()));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("EducationStartDate", lastMonthEpoch);
        lastMonthTimestamps.put("EducationEndDate", lastMonthEpoch);
        lastMonthTimestamps.put("WorkStartDate", lastMonthEpoch);
        lastMonthTimestamps.put("WorkEndDate", lastMonthEpoch);
        scenarios.put("last_month_scenario", lastMonthTimestamps);
        
        // This quarter scenario
        String thisQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisQuarterDateString()));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("EducationStartDate", thisQuarterEpoch);
        thisQuarterTimestamps.put("EducationEndDate", thisQuarterEpoch);
        thisQuarterTimestamps.put("WorkStartDate", thisQuarterEpoch);
        thisQuarterTimestamps.put("WorkEndDate", thisQuarterEpoch);
        scenarios.put("this_quarter_scenario", thisQuarterTimestamps);
        
        // Last quarter scenario
        String lastQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastQuarterDateString()));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("EducationStartDate", lastQuarterEpoch);
        lastQuarterTimestamps.put("EducationEndDate", lastQuarterEpoch);
        lastQuarterTimestamps.put("WorkStartDate", lastQuarterEpoch);
        lastQuarterTimestamps.put("WorkEndDate", lastQuarterEpoch);
        scenarios.put("last_quarter_scenario", lastQuarterTimestamps);
        
        // This year scenario
        String thisYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisYearDateString()));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("EducationStartDate", thisYearEpoch);
        thisYearTimestamps.put("EducationEndDate", thisYearEpoch);
        thisYearTimestamps.put("WorkStartDate", thisYearEpoch);
        thisYearTimestamps.put("WorkEndDate", thisYearEpoch);
        scenarios.put("this_year_scenario", thisYearTimestamps);
        
        // Last year scenario
        String lastYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastYearDateString()));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("EducationStartDate", lastYearEpoch);
        lastYearTimestamps.put("EducationEndDate", lastYearEpoch);
        lastYearTimestamps.put("WorkStartDate", lastYearEpoch);
        lastYearTimestamps.put("WorkEndDate", lastYearEpoch);
        scenarios.put("last_year_scenario", lastYearTimestamps);
        
        return scenarios;
    }

    private String resolveHistoryDateGroupType(String fieldName) {
        if ("Education Start Date".equals(fieldName) || "Education End Date".equals(fieldName)) {
            return "education_history";
        }
        if ("Work Start Date".equals(fieldName) || "Work End Date".equals(fieldName)) {
            return "work_history";
        }
        Assert.fail("Unsupported field name for history date fields: " + fieldName);
        return null;
    }

    private String resolveHistoryTextGroupType(String fieldName) {
        if (fieldName.startsWith("Education") || "School/College Name".equals(fieldName) || "Grade".equals(fieldName)) {
            return "education_history";
        }
        if (fieldName.startsWith("Work") || "Company Name".equals(fieldName)) {
            return "work_history";
        }
        Assert.fail("Unsupported field name for history text fields: " + fieldName);
        return null;
    }

    /**
     * Advanced candidate search payload aligned with {@link DateFieldCandidateFilterSearchTest#createFilterSearchPayload}
     * and {@link TaskActivityFieldsCandidateFilterSearchTest#createDateFilterSearchPayload}, scoped to education/work history.
     */
    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        String groupType = resolveHistoryDateGroupType(fieldName);

        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("offLimitBehavior", "bypass");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        JSONObject filterValueObj = new JSONObject();

        if ("is_between".equals(filterType)) {
            filterValueObj.put("type", "LONG_START_END");
            JSONObject rangeValue = new JSONObject();
            String startValue = filterValue.split(",")[0].trim();
            String endValue = filterValue.split(",")[1].trim();
            long startEpoch = dateToEpochSeconds(startValue);
            long endEpoch = dateToEpochSeconds(endValue);
            rangeValue.put("start", startEpoch);
            rangeValue.put("end", endEpoch);
            filterValueObj.put("value", rangeValue);
        } else {
            filterValueObj.put("type", filterValue_TYPE);
            if ("is_mt".equals(filterType) || "is_lt".equals(filterType)) {
                filterValueObj.put("value", Integer.parseInt(filterValue));
            } else if ("has_any_value".equals(filterType) || "is_empty".equals(filterType)) {
                filterValueObj.put("value", filterValue.isEmpty() ? 0 : Integer.parseInt(filterValue));
            } else if ("is_equal_to".equals(filterType) || "is_before".equals(filterType) || "is_after".equals(filterType) || "is_not".equals(filterType)) {
                long epochValue = dateToEpochSeconds(filterValue);
                filterValueObj.put("value", epochValue);
            } else {
                filterValueObj.put("value", filterValue);
            }
        }

        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();
        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");

        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", groupType);
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


    public JSONArray getFilteredData(Response response) {
        String responseBody = response.getBody().asString();
        JSONObject jsonObject = new JSONObject(responseBody);
        return jsonObject.getJSONArray("data");
    }

 
    public void validateHistoryDateFieldData(JSONArray data, String fieldName, String filterType, String filterValue, String expectedResult) {
        
        boolean isEducationHistory = fieldName.equals("Education Start Date") || fieldName.equals("Education End Date");
        
        String historyDateField;
        if (fieldName.equals("Education Start Date")) {
            historyDateField = "education_start_date";
        } else if (fieldName.equals("Education End Date")) {
            historyDateField = "education_end_date";
        } else if (fieldName.equals("Work Start Date")) {
            historyDateField = "work_start_date";
        } else if (fieldName.equals("Work End Date")) {
            historyDateField = "work_end_date";
        } else {
            Assert.fail("Unsupported field name for work/education history validation: " + fieldName);
            return;
        }
        
        for (int i = 0; i < data.length(); i++) {
            JSONObject candidate = data.getJSONObject(i);
            int candidateId = candidate.getInt("id");
            
            try {
                Response historyResponse;
                if (isEducationHistory) {
                    historyResponse = function.getEducationHistory(albatrossURL, albatrossAuthToken, candidateId);
                } else {
                    historyResponse = function.getWorkHistory(albatrossURL, albatrossAuthToken, candidateId);
                }
                
                Assert.assertEquals(historyResponse.getStatusCode(), 200, 
                    "Failed to fetch " + (isEducationHistory ? "education" : "work") + " history for candidate ID: " + candidateId);
                
                JSONArray historyContent = getFilteredData(historyResponse);
                if(expectedResult.equals("Empty")) {
                    Assert.assertEquals(historyContent.length(), 0, "Wrong candidate data for field: " + fieldName +  " and filterType: " + filterType + " and filterValue: " + filterValue);
                    return;
                } else if (expectedResult.isEmpty()){
                    Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
                }

                
                boolean foundMatchingRecord = false;
                
                for (int j = 0; j < historyContent.length(); j++) {
                    JSONObject historyRecord = historyContent.getJSONObject(j);
                    
                    String historyDateStr = historyRecord.optString(historyDateField, "0");
                    
                    if (validateHistoryDateAgainstFilter(historyDateStr, filterType, filterValue, fieldName)) {
                        foundMatchingRecord = true;
                        break; 
                    }
                }
                
                Assert.assertTrue(foundMatchingRecord, 
                    "No matching " + (isEducationHistory ? "education" : "work") + " history record found for candidate ID: " + candidateId + 
                    " with field: " + fieldName + ", filterType: " + filterType + ", filterValue: " + filterValue);
                    
            } catch (Exception e) {
                Assert.fail("Error validating " + (isEducationHistory ? "education" : "work") + " history for candidate ID: " + candidateId + 
                    " - " + e.getMessage());
            }
        }
    }


    

    private boolean validateHistoryDateAgainstFilter(String historyDateStr, String filterType, String filterValue, String fieldName) {
        try {
            if (historyDateStr == null || historyDateStr.trim().isEmpty() || historyDateStr.equals("0")) {
                return filterType.equals("is_empty");
            }
            
            switch (filterType) {
                case "is":
                case "is_equal_to":
                    return validateHistoryExactDateMatch(historyDateStr, filterValue);
                case "is_not":
                    return !validateHistoryExactDateMatch(historyDateStr, filterValue);
                case "is_before":
                    return validateHistoryDateBefore(historyDateStr, filterValue);
                case "is_after":
                    return validateHistoryDateAfter(historyDateStr, filterValue);
                case "is_between":
                    return validateHistoryDateBetween(historyDateStr, filterValue);
                case "is_mt":
                    return validateHistoryDateMoreThanDaysAgo(historyDateStr, filterValue);
                case "is_lt":
                    return validateHistoryDateLessThanDaysAgo(historyDateStr, filterValue);
                case "has_any_value":
                    return !historyDateStr.equals("0");
                case "is_empty":
                    return historyDateStr.equals("0");
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean validateHistoryExactDateMatch(String historyDateStr, String filterValue) {
        try {
            LocalDate historyDate = parseDate(historyDateStr);
            if (isRelativeDatePeriod(filterValue)) {
                return isDateInPeriod(historyDate, filterValue);
            } else {
                LocalDate filterDate = parseDate(filterValue);
                return historyDate.equals(filterDate);
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean validateHistoryDateBefore(String historyDateStr, String filterValue) {
        try {
            LocalDate historyDate = parseDate(historyDateStr);
            LocalDate filterDate = parseDate(filterValue);
            return historyDate.isBefore(filterDate);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean validateHistoryDateAfter(String historyDateStr, String filterValue) {
        try {
            LocalDate historyDate = parseDate(historyDateStr);
            LocalDate filterDate = parseDate(filterValue);
            return historyDate.isAfter(filterDate) || historyDate.isEqual(filterDate);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean validateHistoryDateBetween(String historyDateStr, String filterValue) {
        try {
            String[] dates = filterValue.split(",");
            if (dates.length != 2) {
                return false;
            }
            
            LocalDate historyDate = parseDate(historyDateStr);
            LocalDate startDate = parseDate(dates[0].trim());
            LocalDate endDate = parseDate(dates[1].trim());
            
            return (historyDate.isEqual(startDate) || historyDate.isAfter(startDate)) &&
                   (historyDate.isEqual(endDate) || historyDate.isBefore(endDate));
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean validateHistoryDateMoreThanDaysAgo(String historyDateStr, String daysStr) {
        try {
            int days = Integer.parseInt(daysStr);
            LocalDate historyDate = parseDate(historyDateStr);
            LocalDate cutoffDate = LocalDate.now().minusDays(days);
            return historyDate.isBefore(cutoffDate) || historyDate.isEqual(cutoffDate);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean validateHistoryDateLessThanDaysAgo(String historyDateStr, String daysStr) {
        try {
            int days = Integer.parseInt(daysStr);
            LocalDate historyDate = parseDate(historyDateStr);
            LocalDate cutoffDate = LocalDate.now().minusDays(days);
            return historyDate.isAfter(cutoffDate) || historyDate.isEqual(cutoffDate);
        } catch (Exception e) {
            return false;
        }
    }



    public LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }
        
        try {
            long epochSeconds = Long.parseLong(dateStr.trim());
            return Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (NumberFormatException e) {
            // Not an epoch value, continue with date string parsing
        }
        

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                try {
                    return LocalDate.parse(dateStr.trim(), formatter);
                } catch (DateTimeParseException e) {
                    LocalDateTime dateTime = LocalDateTime.parse(dateStr.trim(), formatter);
                    return dateTime.toLocalDate();
                }
            } catch (DateTimeParseException e) {
            }
        }
        
        throw new IllegalArgumentException("Unable to parse date: " + dateStr + 
                                         ". Supported formats: yyyy-MM-dd, yy-MM-dd, yyyy-MM-dd HH:mm:ss, MM/dd/yyyy, dd/MM/yyyy, ISO formats, or epoch seconds");
    }


    /**
     * Advanced candidate search payload aligned with {@link TextFieldCandidateFilterSearchTest#createFilterSearchPayload},
     * scoped to education/work history text fields.
     */
    public JSONObject createTextFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String filterValue_TYPE) {
        String groupType = resolveHistoryTextGroupType(fieldName);

        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("offLimitBehavior", "bypass");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        JSONObject filterValueObj;
        if ("STRING_LIST".equals(filterValue_TYPE)) {
            filterValueObj = stringListFilterValue(filterValue);
        } else {
            filterValueObj = new JSONObject();
            filterValueObj.put("type", filterValue_TYPE);
            filterValueObj.put("value", filterValue);
        }

        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();
        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");

        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", groupType);
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "candidate");
        filter.put("fieldType", "text");
        filter.put("filterValue", filterValueObj);

        filtersArray.put(filter);
        groupFilterList.put("filters", filtersArray);
        groupFilterListArray.put(groupFilterList);
        filterSearchList.put("groupFilterList", groupFilterListArray);
        filterSearchList.put("groupJoinOperator", "AND");

        payload.put("filterSearchList", filterSearchList);
        return payload;
    }

    public void validateHistoryTextFieldData(JSONArray data, String fieldName, String filterType, String filterValue, String expectedResult, String dbField) {
        
        boolean isEducationHistory = fieldName.startsWith("Education") || fieldName.equals("School/College Name") || fieldName.equals("Grade");
        
        
        for (int i = 0; i < data.length(); i++) {
            JSONObject candidate = data.getJSONObject(i);
            int candidateId = candidate.getInt("id");
            
            try {
                Response historyResponse;
                if (isEducationHistory) {
                    historyResponse = function.getEducationHistory(albatrossURL, albatrossAuthToken, candidateId);
                } else {
                    historyResponse = function.getWorkHistory(albatrossURL, albatrossAuthToken, candidateId);
                }
                
                Assert.assertEquals(historyResponse.getStatusCode(), 200, 
                    "Failed to fetch " + (isEducationHistory ? "education" : "work") + " history for candidate ID: " + candidateId);
                
                JSONArray historyContent = getFilteredData(historyResponse);
                FilterSearchReporter.logResponse(historyResponse, historyContent);
                if(expectedResult.equals("Empty")) {
                    Assert.assertEquals(historyContent.length(), 0, "Wrong candidate data for field: " + fieldName +  " and filterType: " + filterType + " and filterValue: " + filterValue);
                    return;
                } else if (expectedResult.isEmpty()){
                    Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
                }

                boolean foundMatchingRecord = false;
                if (filterType.equals("is_not") || filterType.equals("is_empty") || filterType.equals("does_not_contain")) {
                    if (historyContent.length() == 0) {
                        foundMatchingRecord = true;
                        continue;
                    }
                }
                
                for (int j = 0; j < historyContent.length(); j++) {
                    JSONObject historyRecord = historyContent.getJSONObject(j);
                    
                    String historyFieldValue = historyRecord.optString(dbField, "");
                    
                    if (validateTextAgainstFilter(historyFieldValue, filterType, filterValue, fieldName)) {
                        foundMatchingRecord = true;
                        break; 
                    }
                }
                
                Assert.assertTrue(foundMatchingRecord, 
                    "No matching " + (isEducationHistory ? "education" : "work") + " history record found for candidate ID: " + candidateId + 
                    " with field: " + fieldName + ", filterType: " + filterType + ", filterValue: " + filterValue);
                    
            } catch (Exception e) {
                Assert.fail("Error validating " + (isEducationHistory ? "education" : "work") + " history for candidate ID: " + candidateId + 
                    " - " + e.getMessage());
            }
        }
    }
}
