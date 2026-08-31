package io.recruitcrm.CandidateService.Filters;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.restclient.RestClient;
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

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DateFieldCrossEntityCandidateFilterSearchTest extends FilterSearchBaseTest {

    private static final Pattern COMPANY_SLUG_PLACEHOLDER = Pattern.compile("^\\{company_slug(\\d+)\\}$");
    private static final String ADVANCED_SEARCH_CANDIDATES_CROSS_ENTITY_SUCCESS = "Entities retrieved successfully";

    private final AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    private commanFunction function;
    private String albatrossAuthToken;
    private String accountOwnerAPIKey;
    private String email;

    private Map<String, Integer> customFieldIds = new HashMap<>();
    private final Map<String, String> entityCFValueMap = new HashMap<>();
    private final ConcurrentHashMap<String, String> candidateKeyToIdMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> candidateIdToKeyMap = new ConcurrentHashMap<>();
    private final Map<String, String> companyCfKeyToSlugMap = new HashMap<>();
    private final ConcurrentHashMap<String, String> fixtureCandidateKeyToCompanySlugMap = new ConcurrentHashMap<>();
    private final Map<String, List<JsonPath>> companyDataByCandidateId = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        function = new commanFunction();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        customFieldIds = createCustomFields();
        createEntityCFValueMap();
        createCompaniesFromCompanyCfData();
        createCandidatesFromCrossEntityData();
        loadCompanyJsonPathsForFixtureCandidates();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "candidateCompanyCrossEntityDateTypeFilterData")
    public void dateFieldCrossEntityCompanyFilterSearchOnCandidatesTest(
            String fieldName,
            String filterType,
            String filterValue,
            String dbField,
            String expectedResult,
            String fieldType,
            String filterValue_TYPE) {
        JSONObject payload = createDateCrossEntityCompanyFilterSearchPayloadForCandidates(
                fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200,
                "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        String metaMessage = response.jsonPath().getString("meta.message");
        Assert.assertEquals(metaMessage, ADVANCED_SEARCH_CANDIDATES_CROSS_ENTITY_SUCCESS,
                "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue
                        + " is not '" + ADVANCED_SEARCH_CANDIDATES_CROSS_ENTITY_SUCCESS + "' (actual: " + metaMessage + ")");
        JSONArray data = getFilteredData(response);
        JSONObject companyDataByCandidate = getCompaniesForCandidates(data);
        logCandidateAndCompanyDateField(response, data, companyDataByCandidate, fieldName, dbField);
        validateDateFieldCrossEntityCompanyFilteredDataForCandidates(
                companyDataByCandidate, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    public void validateDateFieldCrossEntityCompanyFilteredDataForCandidates(
            JSONObject companyDataByCandidate,
            String filterType,
            String filterValue,
            String fieldName,
            String dbField,
            String expectedResult) {
        if ("Empty".equals(expectedResult)) {
            Assert.assertEquals(companyDataByCandidate.length(), 0,
                    "Expected empty result but response has data for field: " + fieldName + " and filterType: " + filterType
                            + " and filterValue: " + filterValue);
            return;
        }
        if (companyDataByCandidate.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: "
                    + filterValue);
        }

        for (String candidateIdStr : companyDataByCandidate.keySet()) {
            JSONArray companies = companyDataByCandidate.getJSONArray(candidateIdStr);
            boolean atLeastOneMatch = false;
            if ("is_empty".equals(filterType) || "is_not".equals(filterType)) {
                if (companies.length() == 0) {
                    atLeastOneMatch = true;
                    continue;
                }
            }
            for (int i = 0; i < companies.length(); i++) {
                JSONObject company = companies.getJSONObject(i);
                String companyDateStr = String.valueOf(company.opt(dbField)).trim();
                if ("null".equals(companyDateStr)) {
                    companyDateStr = "";
                }
                boolean matches = validateDateFieldFilteredDataBoolean(companyDateStr, filterType, filterValue, fieldName);
                if (matches) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!atLeastOneMatch) {
                Assert.fail("No company matched the date filter for candidate still in the response: " + candidateIdStr
                        + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
        }
    }

    private boolean validateDateFieldFilteredDataBoolean(String companyDate, String filterType, String filterValue, String fieldName) {
        if (companyDate.isEmpty() || companyDate.equals("null") || companyDate.equals("0")) {
            return "is_empty".equals(filterType);
        }

        try {
            LocalDate companyParsedDate = parseDate(companyDate);

            switch (filterType) {
                case "is":
                case "is_equal_to":
                    if (isRelativeDatePeriod(filterValue)) {
                        return isDateInPeriod(companyParsedDate, filterValue);
                    } else {
                        LocalDate filterDate = parseDate(filterValue);
                        return companyParsedDate.equals(filterDate);
                    }
                case "is_not":
                    LocalDate filterDateNot = parseDate(filterValue);
                    return !companyParsedDate.equals(filterDateNot);
                case "is_before":
                    LocalDate filterDateBefore = parseDate(filterValue);
                    return companyParsedDate.isBefore(filterDateBefore);
                case "is_after":
                    LocalDate filterDateAfter = parseDate(filterValue);
                    return companyParsedDate.isAfter(filterDateAfter);
                case "is_between":
                    String[] dates = filterValue.split(",");
                    if (dates.length != 2) {
                        return false;
                    }
                    LocalDate startDate = parseDate(dates[0].trim());
                    LocalDate endDate = parseDate(dates[1].trim());
                    return (companyParsedDate.isEqual(startDate) || companyParsedDate.isAfter(startDate))
                            && (companyParsedDate.isEqual(endDate) || companyParsedDate.isBefore(endDate));
                case "is_mt":
                    int days = Integer.parseInt(filterValue);
                    LocalDate cutoffDate = LocalDate.now().minusDays(days);
                    return companyParsedDate.isBefore(cutoffDate) || companyParsedDate.isEqual(cutoffDate);
                case "is_lt":
                    int daysLt = Integer.parseInt(filterValue);
                    LocalDate cutoffDateLt = LocalDate.now().minusDays(daysLt);
                    return companyParsedDate.isAfter(cutoffDateLt) || companyParsedDate.isEqual(cutoffDateLt);
                case "has_any_value":
                    return !companyDate.isEmpty() && !companyDate.equals("null") && !companyDate.equals("0");
                case "is_empty":
                    return companyDate.isEmpty() || companyDate.equals("null") || companyDate.equals("0");
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void logCandidateAndCompanyDateField(
            Response response,
            JSONArray candidateData,
            JSONObject companyDataByCandidate,
            String fieldName,
            String dbField) {
        FilterSearchReporter.logInfo("<b>📋 Candidate — company date (cross-entity):</b>");
        if (candidateData == null || companyDataByCandidate == null) {
            return;
        }
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("<pre style='background-color: #f8f9fa; padding: 10px; border-radius: 5px;'>");
        logMessage.append("<code>");
        for (int i = 0; i < candidateData.length(); i++) {
            JSONObject candidate = candidateData.getJSONObject(i);
            String candidateIdStr = candidateIdToString(candidate);
            JSONArray companies = companyDataByCandidate.optJSONArray(candidateIdStr);
            String firstName = candidate.optString("firstname", "").trim();
            String lastName = candidate.optString("lastname", "").trim();
            String candidateName = (firstName + " " + lastName).trim();
            if (candidateName.isEmpty()) {
                candidateName = "Unknown (id=" + candidateIdStr + ")";
            }
            logMessage.append("Candidate: ").append(candidateName).append("\n");
            if (companies != null && companies.length() > 0) {
                for (int j = 0; j < companies.length(); j++) {
                    JSONObject company = companies.getJSONObject(j);
                    String companyName = company.optString("companyname", "N/A");
                    String fieldVal = String.valueOf(company.opt(dbField)).trim();
                    if (fieldVal.isEmpty() || "null".equals(fieldVal) || "0".equals(fieldVal)) {
                        fieldVal = "N/A";
                    }
                    logMessage.append("  Company ").append(j + 1).append(": ").append(companyName);
                    logMessage.append(" | ").append(fieldName).append(" (").append(dbField).append("): ").append(fieldVal).append("\n");
                }
            } else {
                logMessage.append("  No companies in fixture map for this candidate id\n");
            }
            logMessage.append("\n");
        }
        logMessage.append("</code></pre>");
        FilterSearchReporter.logInfo(logMessage.toString());
    }

    public JSONObject createDateCrossEntityCompanyFilterSearchPayloadForCandidates(
            String fieldName,
            String filterType,
            String filterValue,
            String dbField,
            String fieldType,
            String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
        JSONObject defaultFilterInner = new JSONObject();
        defaultFilterInner.put("filters", new JSONArray());
        defaultFilterInner.put("subGroupJoinOperator", "AND");
        JSONObject defaultFilterRoot = new JSONObject();
        defaultFilterRoot.put("defaultFilterList", defaultFilterInner);
        payload.put("defaultFilterList", defaultFilterRoot);
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
            } else if ("is_equal_to".equals(filterType) || "is_before".equals(filterType) || "is_after".equals(filterType)) {
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
        filter.put("isCrossEntity", true);
        filter.put("groupType", "companies");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        if ("Last Meeting Added".equals(fieldName)) {
            filter.put("entityType", "company_last_activities_t");
        } else {
            filter.put("entityType", "company");
        }
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

    @DataProvider(name = "candidateCompanyCrossEntityDateTypeFilterData", parallel = true)
    public Object[][] candidateCompanyCrossEntityDateTypeFilterData() {
        JSONObject filterData = readJsonFileFromPath(
                "src/test/resources/filtersDataProvider/candidateCompanyCrossEntityDateTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
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
                        test.getString("filterValue_TYPE")
                });
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public JSONObject getCompaniesForCandidates(JSONArray candidateData) {
        JSONObject companyResult = new JSONObject();
        for (int i = 0; i < candidateData.length(); i++) {
            JSONObject candidate = candidateData.getJSONObject(i);
            String candidateIdStr = candidateIdToString(candidate);
            JSONArray candidateAssociatedCompanies = new JSONArray();
            List<JsonPath> companyList = companyDataByCandidateId.get(candidateIdStr);
            if (companyList != null) {
                for (JsonPath companyJsonPath : companyList) {
                    Map<String, Object> companyMap = companyJsonPath.get("data.company");
                    candidateAssociatedCompanies.put(new JSONObject(companyMap));
                }
            }
            companyResult.put(candidateIdStr, candidateAssociatedCompanies);
        }
        return companyResult;
    }

    private static String candidateIdToString(JSONObject candidate) {
        if (candidate.has("id")) {
            Object id = candidate.get("id");
            if (id instanceof Number) {
                return String.valueOf(((Number) id).intValue());
            }
            return String.valueOf(id);
        }
        throw new IllegalArgumentException("Candidate row missing id: " + candidate);
    }

    public Response getCompany(String companySlug) {
        String basePath = "/companies/{companySlug}";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companySlug", companySlug);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParams, true);
        response.then().statusCode(200);
        return response;
    }

    private void createCompaniesFromCompanyCfData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/companyCF_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, String> companySlugMap = new HashMap<>();
        try {
            List<CompletableFuture<Map.Entry<String, String>>> createFutures = companyJson.keySet().stream()
                    .filter(key -> key.startsWith("company"))
                    .map(companyKey -> CompletableFuture.supplyAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
                        response.then().statusCode(200);
                        String slug = response.jsonPath().getString("data.company.slug");
                        return Map.entry(companyKey, slug);
                    }, executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<Map.Entry<String, String>> future : createFutures) {
                Map.Entry<String, String> entry = future.join();
                companySlugMap.put(entry.getKey(), entry.getValue());
                companyCfKeyToSlugMap.put(entry.getKey(), entry.getValue());
            }
            List<CompletableFuture<Void>> linkFutures = companyJson.keySet().stream()
                    .filter(key -> key.startsWith("company"))
                    .map(companyKey -> CompletableFuture.runAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        String parentCompanyKey = companyEntry.optString("parentCompany", null);
                        if (parentCompanyKey == null || parentCompanyKey.isEmpty() || "null".equals(parentCompanyKey)) {
                            return;
                        }
                        if (!parentCompanyKey.startsWith("company") || !companySlugMap.containsKey(parentCompanyKey)) {
                            return;
                        }
                        String parentCompanySlug = companySlugMap.get(parentCompanyKey);
                        String childCompanySlug = companySlugMap.get(companyKey);
                        if (parentCompanySlug != null && childCompanySlug != null) {
                            List<String> childSlugs = new ArrayList<>();
                            childSlugs.add(childCompanySlug);
                            Response linkResponse = allCrudFunctions.linkCompanyToParentCompany(
                                    albatrossURL, albatrossAuthToken, parentCompanySlug, childSlugs);
                            Assert.assertEquals(linkResponse.getStatusCode(), 200,
                                    "Failed to link " + companyKey + " to parent " + parentCompanyKey);
                        }
                    }, executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(linkFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        applyCompanyJobCountUpdates(companyJson, companySlugMap);

        List<String> orderedKeys = sortedCompanyKeys(companyJson);
        List<Integer> orderedIds = new ArrayList<>();
        for (String key : orderedKeys) {
            String slug = companySlugMap.get(key);
            orderedIds.add(function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, slug));
        }
        List<String> orderedSlugs = orderedKeys.stream().map(companySlugMap::get).collect(Collectors.toList());
        createMeetingsForCompanies(orderedSlugs);
        updateCompaniesWithTimestampScenarios(orderedIds);
        updateCompaniesWithLastActivityTimestamps(orderedIds);
    }

    private static List<String> sortedCompanyKeys(JSONObject companyJson) {
        return companyJson.keySet().stream()
                .filter(k -> k.startsWith("company"))
                .sorted(Comparator.comparingInt(k -> Integer.parseInt(k.substring(7))))
                .collect(Collectors.toList());
    }

    private void applyCompanyJobCountUpdates(JSONObject companyJson, Map<String, String> companySlugMap) {
        ExecutorService pool = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = sortedCompanyKeys(companyJson).stream()
                    .map(companyKey -> CompletableFuture.runAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        String slug = companySlugMap.get(companyKey);
                        if (slug == null) {
                            return;
                        }
                        int companyId = function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, slug);
                        JSONObject fieldsAndValues = new JSONObject();
                        if (companyEntry.has("totalonholdjob")) {
                            fieldsAndValues.put("totalonholdjob", companyEntry.getInt("totalonholdjob"));
                        }
                        if (companyEntry.has("totalcanceledjob")) {
                            fieldsAndValues.put("totalcanceledjob", companyEntry.getInt("totalcanceledjob"));
                        }
                        if (companyEntry.has("totalclosedjob")) {
                            fieldsAndValues.put("totalclosedjob", companyEntry.getInt("totalclosedjob"));
                        }
                        if (companyEntry.has("totalopenjob")) {
                            fieldsAndValues.put("totalopenjob", companyEntry.getInt("totalopenjob"));
                        }
                        if (fieldsAndValues.length() > 0) {
                            ReaperIntegration.updateCompanyFields(companyId, fieldsAndValues);
                        }
                    }, pool))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            pool.shutdown();
        }
    }

    private void createMeetingsForCompanies(List<String> companySlugs) {
        ExecutorService pool = Executors.newFixedThreadPool(5);
        try {
            CompletableFuture.allOf(companySlugs.stream()
                    .map(slug -> CompletableFuture.runAsync(() ->
                            function.createNewMeetingWithEntitySlug(baseURL, accountOwnerAPIKey, "company", slug), pool))
                    .toArray(CompletableFuture[]::new)).join();
        } finally {
            pool.shutdown();
        }
    }

    private void updateCompaniesWithTimestampScenarios(List<Integer> companyIds) {
        List<Map<String, String>> scenarios = buildOrderedTimestampScenarios();
        for (int i = 0; i < Math.min(companyIds.size(), scenarios.size()); i++) {
            JSONObject fieldsAndValues = new JSONObject();
            for (Map.Entry<String, String> e : scenarios.get(i).entrySet()) {
                fieldsAndValues.put(e.getKey(), e.getValue());
            }
            Response updateResponse = ReaperIntegration.updateCompanyFields(companyIds.get(i), fieldsAndValues);
            Assert.assertEquals(updateResponse.getStatusCode(), 200,
                    "Failed to update company createdon/updatedon for company id " + companyIds.get(i));
        }
    }

    private void updateCompaniesWithLastActivityTimestamps(List<Integer> companyIds) {
        List<Map<String, String>> scenarios = buildOrderedLastActivityScenarios();
        for (int i = 0; i < Math.min(companyIds.size(), scenarios.size()); i++) {
            JSONObject fieldsAndTimestamps = new JSONObject();
            for (Map.Entry<String, String> e : scenarios.get(i).entrySet()) {
                fieldsAndTimestamps.put(e.getKey(), e.getValue());
            }
            Response updateResponse = ReaperIntegration.updateLastActivityTimestamp("company", companyIds.get(i), fieldsAndTimestamps);
            Assert.assertEquals(updateResponse.getStatusCode(), 200,
                    "Failed to update last activity timestamps for company id " + companyIds.get(i));
        }
    }

    private List<Map<String, String>> buildOrderedTimestampScenarios() {
        List<Map<String, String>> list = new ArrayList<>();
        addTs(list, DateUtil.getTodayDateString("yyyy-MM-dd"));
        addTs(list, DateUtil.getYesterdayDateString("yyyy-MM-dd"));
        addTs(list, DateUtil.getThisWeekDateString());
        addTs(list, DateUtil.getLastWeekDateString());
        addTs(list, DateUtil.getThisMonthDateString());
        addTs(list, DateUtil.getLastMonthDateString());
        addTs(list, DateUtil.getThisQuarterDateString());
        addTs(list, DateUtil.getLastQuarterDateString());
        addTs(list, DateUtil.getThisYearDateString());
        addTs(list, DateUtil.getLastYearDateString());
        addStaticTs(list, "1655251200");
        addStaticTs(list, "1678406400");
        addStaticTs(list, "1717689600");
        addStaticTs(list, "1739491200");
        return list;
    }

    private void addTs(List<Map<String, String>> list, String yyyyMmDd) {
        String epoch = String.valueOf(dateToEpochSeconds(yyyyMmDd));
        Map<String, String> m = new HashMap<>();
        m.put("createdon", epoch);
        m.put("updatedon", epoch);
        list.add(m);
    }

    private void addStaticTs(List<Map<String, String>> list, String epoch) {
        Map<String, String> m = new HashMap<>();
        m.put("createdon", epoch);
        m.put("updatedon", epoch);
        list.add(m);
    }

    private List<Map<String, String>> buildOrderedLastActivityScenarios() {
        List<Map<String, String>> list = new ArrayList<>();
        addMeeting(list, DateUtil.getTodayDateString("yyyy-MM-dd"));
        addMeeting(list, DateUtil.getYesterdayDateString("yyyy-MM-dd"));
        addMeeting(list, DateUtil.getThisWeekDateString());
        addMeeting(list, DateUtil.getLastWeekDateString());
        addMeeting(list, DateUtil.getThisMonthDateString());
        addMeeting(list, DateUtil.getLastMonthDateString());
        addMeeting(list, DateUtil.getThisQuarterDateString());
        addMeeting(list, DateUtil.getLastQuarterDateString());
        addMeeting(list, DateUtil.getThisYearDateString());
        addMeeting(list, DateUtil.getLastYearDateString());
        addMeetingEpoch(list, "1655251200");
        addMeetingEpoch(list, "1678406400");
        addMeetingEpoch(list, "1717689600");
        addMeetingEpoch(list, "1739491200");
        return list;
    }

    private void addMeeting(List<Map<String, String>> list, String yyyyMmDd) {
        String epoch = String.valueOf(dateToEpochSeconds(yyyyMmDd));
        Map<String, String> m = new HashMap<>();
        m.put("meeting_created_on", epoch);
        list.add(m);
    }

    private void addMeetingEpoch(List<Map<String, String>> list, String epoch) {
        Map<String, String> m = new HashMap<>();
        m.put("meeting_created_on", epoch);
        list.add(m);
    }

    private void createCandidatesFromCrossEntityData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/candidateCrossEntity_Data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = candidateJson.keySet().stream()
                    .filter(key -> key.startsWith("candidate"))
                    .map(candidateKey -> CompletableFuture.runAsync(() -> {
                        JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                        JSONObject payload = new JSONObject(candidateEntry.getJSONObject("payload").toString());
                        JSONObject processedPayload = processPayloadPlaceholders(payload);
                        applyCandidateCompanySlugPlaceholder(processedPayload);
                        Response response = allCrudFunctions.createCandidateWithJson(
                                albatrossURL, albatrossAuthToken, processedPayload);
                        response.then().statusCode(200);
                        int candidateId = response.jsonPath().getInt("data.candidate.id");
                        candidateIdToKeyMap.put(String.valueOf(candidateId), candidateKey);
                        candidateKeyToIdMap.put(candidateKey, String.valueOf(candidateId));
                        String companySlug = processedPayload.optString("candidate_company_slug", "");
                        if (!companySlug.isEmpty()) {
                            fixtureCandidateKeyToCompanySlugMap.put(candidateKey, companySlug);
                        }
                    }, executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private void loadCompanyJsonPathsForFixtureCandidates() {
        for (Map.Entry<String, String> e : fixtureCandidateKeyToCompanySlugMap.entrySet()) {
            String candidateKey = e.getKey();
            String companySlug = e.getValue();
            String candidateId = candidateKeyToIdMap.get(candidateKey);
            if (candidateId == null) {
                continue;
            }
            Response companyResponse = getCompany(companySlug);
            JsonPath companyJsonPath = companyResponse.jsonPath();
            List<JsonPath> list = new ArrayList<>();
            list.add(companyJsonPath);
            synchronized (companyDataByCandidateId) {
                companyDataByCandidateId.put(candidateId, list);
            }
        }
    }

    private void applyCandidateCompanySlugPlaceholder(JSONObject payload) {
        if (!payload.has("candidate_company_slug")) {
            return;
        }
        String value = payload.getString("candidate_company_slug");
        Matcher m = COMPANY_SLUG_PLACEHOLDER.matcher(value);
        if (!m.matches()) {
            return;
        }
        String companyKey = "company" + m.group(1);
        String slug = companyCfKeyToSlugMap.get(companyKey);
        Assert.assertNotNull(slug, "No company slug for placeholder " + value + " (expected " + companyKey + " in companyCF_data)");
        payload.put("candidate_company_slug", slug);
    }

    private void createEntityCFValueMap() {
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
            CompletableFuture<JsonPath> contactJson1Future = companyJson1Future.thenApplyAsync(companyJson1 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> contactJson2Future = companyJson2Future.thenApplyAsync(companyJson2 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson2.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> jobJson1Future = companyJson1Future.thenCombineAsync(contactJson1Future, (c1, ct1) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, c1.getString("slug"), ct1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> jobJson2Future = companyJson2Future.thenCombineAsync(contactJson2Future, (c2, ct2) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, c2.getString("slug"), ct2.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> dealJson1Future = jobJson1Future.thenApplyAsync(jobJson1 -> {
                String cs = companyJson1Future.join().getString("slug");
                String cts = contactJson1Future.join().getString("slug");
                return function.createNewDealWithMandatoryFields(baseURL, accountOwnerAPIKey, cs, cts, jobJson1.getString("slug")).jsonPath();
            }, executor);
            CompletableFuture<JsonPath> dealJson2Future = jobJson2Future.thenApplyAsync(jobJson2 -> {
                String cs = companyJson2Future.join().getString("slug");
                String cts = contactJson2Future.join().getString("slug");
                return function.createNewDealWithMandatoryFields(baseURL, accountOwnerAPIKey, cs, cts, jobJson2.getString("slug")).jsonPath();
            }, executor);
            CompletableFuture<JsonPath> userJsonFuture = CompletableFuture.supplyAsync(() ->
                    function.getUsers(baseURL, accountOwnerAPIKey).jsonPath(), executor);
            CompletableFuture<JsonPath> teamJsonFuture = userJsonFuture.thenApplyAsync((userJson) -> {
                ArrayList<String> team1UserId = new ArrayList<>();
                ArrayList<String> team2UserId = new ArrayList<>();
                team1UserId.add(String.valueOf(userJson.getInt("[1].id")));
                team1UserId.add(String.valueOf(userJson.getInt("[3].id")));
                team2UserId.add(String.valueOf(userJson.getInt("[0].id")));
                team2UserId.add(String.valueOf(userJson.getInt("[2].id")));
                Response team1Response = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team1", team1UserId);
                Response team2Response = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team2", team2UserId);
                team1Response.then().statusCode(200);
                team2Response.then().statusCode(200);
                return function.getTeams(baseURL, accountOwnerAPIKey).jsonPath();
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

    private Map<String, Integer> createCustomFields() {
        Map<String, Integer> ids = new HashMap<>();
        List<String> entityTypes = new ArrayList<>(List.of(
                "candidate", "company", "deal", "job", "contact", "user", "team",
                "text", "email", "phonenumber", "longtext", "number", "date", "social_profile"));
        ExecutorService executor = Executors.newFixedThreadPool(7);
        try {
            List<CompletableFuture<AbstractMap.SimpleEntry<String, Response>>> futures = entityTypes.stream()
                    .map(entity -> CompletableFuture.supplyAsync(() -> {
                        String fieldName = entity + "CF";
                        Response response = function.createCustomFieldsResponse(
                                albatrossURL, albatrossAuthToken, "candidate", fieldName, entity, "");
                        return new AbstractMap.SimpleEntry<>(fieldName, response);
                    }, executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<AbstractMap.SimpleEntry<String, Response>> future : futures) {
                AbstractMap.SimpleEntry<String, Response> result = future.get();
                String fieldName = result.getKey();
                Response response = result.getValue();
                Assert.assertEquals(response.getStatusCode(), 200, "Failed to create custom field: " + fieldName);
                ids.put(fieldName, response.jsonPath().getInt("data.custumField.columnid"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating custom fields in parallel", e);
        } finally {
            executor.shutdown();
        }
        return ids;
    }

    private JSONObject processPayloadPlaceholders(JSONObject payload) {
        JSONObject processedPayload = new JSONObject();
        for (String key : payload.keySet()) {
            Object value = payload.get(key);
            if (key.startsWith("{") && key.endsWith("}")) {
                String trimmedKey = key.substring(1, key.length() - 1);
                if (customFieldIds.containsKey(trimmedKey)) {
                    String newKey = "custcolumn" + customFieldIds.get(trimmedKey);
                    if (value instanceof String) {
                        processedPayload.put(newKey, processEntityPlaceholders((String) value));
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

    private String processEntityPlaceholders(String value) {
        if (value == null) {
            return null;
        }
        if (value.startsWith("{") && value.endsWith("}")) {
            String innerValue = value.substring(1, value.length() - 1);
            String[] entityKeys = innerValue.split(",");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < entityKeys.length; i++) {
                String entityKey = entityKeys[i].trim();
                if (i > 0) {
                    result.append(",");
                }
                if (entityCFValueMap.containsKey(entityKey)) {
                    result.append(entityCFValueMap.get(entityKey));
                } else {
                    result.append(entityKey);
                }
            }
            return result.toString();
        }
        return value;
    }
}
