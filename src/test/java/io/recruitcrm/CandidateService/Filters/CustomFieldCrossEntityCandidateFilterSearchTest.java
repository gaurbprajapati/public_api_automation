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
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class CustomFieldCrossEntityCandidateFilterSearchTest extends FilterSearchBaseTest {

    private static final Pattern COMPANY_SLUG_PLACEHOLDER = Pattern.compile("^\\{company_slug(\\d+)\\}$");
    private static final String ADVANCED_SEARCH_CANDIDATES_CROSS_ENTITY_SUCCESS = "Entities retrieved successfully";

    private final AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    private commanFunction function;

    private String ownerAlbatrossAuthToken;
    private String accountOwnerAPIKey;
    private String email;

    private Map<String, Integer> companyCfColumnIds = new HashMap<>();
    private Map<String, Integer> candidateCfColumnIds = new HashMap<>();
    private final Map<String, String> entityCFValueMap = new HashMap<>();

    private final ConcurrentHashMap<String, String> companyKeyToIdMap = new ConcurrentHashMap<>();
    private final Map<String, String> companyCfKeyToSlugMap = new HashMap<>();
    private final ConcurrentHashMap<String, String> candidateKeyToIdMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> candidateIdToKeyMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> fixtureCandidateKeyToCompanySlugMap = new ConcurrentHashMap<>();
    private final Map<String, List<JsonPath>> companyDataByCandidateId = new HashMap<>();
    private Map<String, Map<String, String>> timestampScenarios;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        function = new commanFunction();
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        companyCfColumnIds = createCompanyCustomFields();
        candidateCfColumnIds = createCandidateCustomFields();
        createEntityCFValueMap();
        createCompaniesFromCompanyCfData();
        updateCompanyCustomDateFieldsTimestamps();
        createCandidatesFromCrossEntityData();
        loadCompanyJsonPathsForFixtureCandidates();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "entityCustomFieldCrossEntityCandidateFilterData")
    public void entityCustomFieldCrossEntityCandidateFilterSearchTest(
            String fieldName, String filterType, String filterValue, String dbField,
            String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + companyCfColumnIds.get(fieldName);
        JSONObject payload = createEntityCustomFieldCrossEntityCandidatePayload(
                filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        runAndAssertMatchingExpectedFixtureCompanies(fieldName, filterType, filterValue, payload, expectedResult, dbField);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "textCustomFieldCrossEntityCandidateFilterData")
    public void textCustomFieldCrossEntityCandidateFilterSearchTest(
            String fieldName, String filterType, String filterValue, String dbField,
            String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + companyCfColumnIds.get(fieldName);
        JSONObject payload = asCrossEntityCandidatePayload(
                createTextFieldFilterSearchPayload(filterType, filterValue, dbField, fieldType, filterValue_TYPE));
        runAndAssertValidatingCompanyTextField(fieldName, filterType, filterValue, payload, expectedResult, dbField);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "numberCustomFieldCrossEntityCandidateFilterData")
    public void numberCustomFieldCrossEntityCandidateFilterSearchTest(
            String fieldName, String filterType, String filterValue, String dbField,
            String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + companyCfColumnIds.get(fieldName);
        JSONObject payload = asCrossEntityCandidatePayload(
                createNumberFilterSearchPayload(filterType, filterValue, dbField, fieldType, filterValue_TYPE));
        runAndAssertValidatingCompanyNumberField(fieldName, filterType, filterValue, payload, expectedResult, dbField);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "dateCustomFieldCrossEntityCandidateFilterData")
    public void dateCustomFieldCrossEntityCandidateFilterSearchTest(
            String fieldName, String filterType, String filterValue, String dbField,
            String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + companyCfColumnIds.get(fieldName);
        JSONObject payload = asCrossEntityCandidatePayload(
                createDateFilterSearchPayload(filterType, filterValue, dbField, fieldType, filterValue_TYPE));
        runAndAssertValidatingCompanyDateField(fieldName, filterType, filterValue, payload, expectedResult, dbField);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "dropdownMultiselectCustomFieldCrossEntityCandidateFilterData")
    public void dropdownMultiselectCustomFieldCrossEntityCandidateFilterSearchTest(
            String fieldName, String filterType, String filterValue, String dbField,
            String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + companyCfColumnIds.get(fieldName);
        JSONObject payload = asCrossEntityCandidatePayload(
                createDropdownAndMultiselectFilterSearchPayload(filterType, filterValue, dbField, fieldType, filterValue_TYPE));
        runAndAssertMatchingExpectedFixtureCompanies(fieldName, filterType, filterValue, payload, expectedResult, dbField);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "checkboxCustomFieldCrossEntityCandidateFilterData")
    public void checkboxCustomFieldCrossEntityCandidateFilterSearchTest(
            String fieldName, String filterType, String filterValue, String dbField,
            String expectedResult, String fieldType, String filterValue_TYPE) {
        dbField = "custcolumn" + companyCfColumnIds.get(fieldName);
        JSONObject payload = asCrossEntityCandidatePayload(
                createCheckboxFilterSearchPayload(filterType, filterValue, dbField, fieldType, filterValue_TYPE));
        runAndAssertMatchingExpectedFixtureCompanies(fieldName, filterType, filterValue, payload, expectedResult, dbField);
    }

    private void runAndAssertMatchingExpectedFixtureCompanies(String fieldName, String filterType, String filterValue,
                                                              JSONObject payload, String expectedResult, String logDbField) {
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200,
                "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().getString("meta.message"), ADVANCED_SEARCH_CANDIDATES_CROSS_ENTITY_SUCCESS,
                "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, logDbField);
        validateCrossEntityCandidateResultsByExpectedCompanies(data, expectedResult);
    }

    private void runAndAssertValidatingCompanyTextField(String fieldName, String filterType, String filterValue,
                                                        JSONObject payload, String expectedResult, String dbField) {
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200,
                "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().getString("meta.message"), ADVANCED_SEARCH_CANDIDATES_CROSS_ENTITY_SUCCESS,
                "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        JSONObject companyDataByCandidate = getCompaniesForCandidates(data);
        validateTextFieldCrossEntityCompanyFilteredDataForCandidates(
                companyDataByCandidate, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    private void runAndAssertValidatingCompanyNumberField(String fieldName, String filterType, String filterValue,
                                                          JSONObject payload, String expectedResult, String dbField) {
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200,
                "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().getString("meta.message"), ADVANCED_SEARCH_CANDIDATES_CROSS_ENTITY_SUCCESS,
                "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        JSONObject companyDataByCandidate = getCompaniesForCandidates(data);
        validateNumberFieldCrossEntityCompanyFilteredDataForCandidates(
                companyDataByCandidate, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    private void runAndAssertValidatingCompanyDateField(String fieldName, String filterType, String filterValue,
                                                        JSONObject payload, String expectedResult, String dbField) {
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200,
                "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().getString("meta.message"), ADVANCED_SEARCH_CANDIDATES_CROSS_ENTITY_SUCCESS,
                "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        JSONObject companyDataByCandidate = getCompaniesForCandidates(data);
        validateDateFieldCrossEntityCompanyFilteredDataForCandidates(
                companyDataByCandidate, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    private JSONObject getCompaniesForCandidates(JSONArray candidateData) {
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

    private void validateTextFieldCrossEntityCompanyFilteredDataForCandidates(
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

        String[] htmlStrip = "aboutcompany".equals(dbField) ? new String[]{"aboutcompany"} : new String[]{};

        for (String candidateIdStr : companyDataByCandidate.keySet()) {
            JSONArray companies = companyDataByCandidate.getJSONArray(candidateIdStr);
            boolean atLeastOneMatch = false;
            if ("is_not".equals(filterType) || "does_not_contain".equals(filterType) || "is_empty".equals(filterType)) {
                if (companies.length() == 0) {
                    atLeastOneMatch = true;
                    continue;
                }
            }
            for (int i = 0; i < companies.length(); i++) {
                JSONObject company = companies.getJSONObject(i);
                boolean matches = validateTextFieldFilteredDataBoolean(
                        company, filterType, filterValue, fieldName, dbField, expectedResult, "Company", htmlStrip);
                if (matches) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!atLeastOneMatch) {
                Assert.fail("No company matched the filter for candidate still coming in the response: " + candidateIdStr
                        + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
        }
    }

    private void validateNumberFieldCrossEntityCompanyFilteredDataForCandidates(
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
            if ("is_not".equals(filterType) || "is_empty".equals(filterType)) {
                if (companies.length() == 0) {
                    atLeastOneMatch = true;
                    continue;
                }
            }
            for (int i = 0; i < companies.length(); i++) {
                JSONObject company = companies.getJSONObject(i);
                String companyNumber = String.valueOf(company.opt(dbField)).trim();
                if (companyNumber.equals("null") || companyNumber.isEmpty()) {
                    if ("is_empty".equals(filterType)) {
                        atLeastOneMatch = true;
                        break;
                    }
                    continue;
                }
                if (companyNumber.endsWith(".0")) {
                    companyNumber = companyNumber.substring(0, companyNumber.length() - 2);
                }
                boolean matches = validateNumberFieldFilteredDataBooleanForCompany(companyNumber, filterType, filterValue, fieldName);
                if (matches) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!atLeastOneMatch) {
                Assert.fail("No company matched the number filter for candidate still in the response: " + candidateIdStr
                        + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
        }
    }

    private boolean validateNumberFieldFilteredDataBooleanForCompany(String entityNumber, String filterType, String filterValue, String fieldName) {
        try {
            if ("is_between".equals(filterType)) {
                String[] rangeParts = filterValue.split(",");
                double startValue = Double.parseDouble(rangeParts[0].trim());
                double endValue = Double.parseDouble(rangeParts[1].trim());
                double contactNumberValue = Double.parseDouble(entityNumber);
                return contactNumberValue >= startValue && contactNumberValue <= endValue;
            }
            if ("has_any_value".equals(filterType)) {
                return !entityNumber.isEmpty() && !"null".equals(entityNumber);
            }
            if ("is_empty".equals(filterType)) {
                if (entityNumber.isEmpty() || "null".equals(entityNumber) || "0".equals(entityNumber)) {
                    return true;
                }
                try {
                    return Double.parseDouble(entityNumber) == 0.0;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            double contactNumberValue = Double.parseDouble(entityNumber);
            double filterValueDouble = Double.parseDouble(filterValue.trim());

            switch (filterType) {
                case "is":
                    return contactNumberValue == filterValueDouble;
                case "is_not":
                    return contactNumberValue != filterValueDouble;
                case "is_mt":
                    return contactNumberValue > filterValueDouble;
                case "is_lt":
                    return contactNumberValue < filterValueDouble;
                case "begins_with":
                    return entityNumber.startsWith(filterValue.trim());
                case "ends_with":
                    return entityNumber.endsWith(filterValue.trim());
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void validateDateFieldCrossEntityCompanyFilteredDataForCandidates(
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
                boolean matches = validateDateFieldFilteredDataBooleanForCompany(companyDateStr, filterType, filterValue, fieldName);
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

    private boolean validateDateFieldFilteredDataBooleanForCompany(String companyDate, String filterType, String filterValue, String fieldName) {
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

    private void validateCrossEntityCandidateResultsByExpectedCompanies(JSONArray candidateData, String expectedResult) {
        if ("Empty".equals(expectedResult)) {
            Assert.assertEquals(candidateData.length(), 0, "Expected empty candidate result but response has data");
            return;
        }
        Set<Integer> expectedCompanyIds = new HashSet<>();
        for (String token : expectedResult.split(",")) {
            String normalizedKey = token.trim().toLowerCase().replace(" ", "");
            String idStr = companyKeyToIdMap.get(normalizedKey);
            if (idStr == null) {
                Assert.fail("Expected company key '" + token + "' (normalized: '" + normalizedKey + "') not found in companyKeyToIdMap. Keys: " + companyKeyToIdMap.keySet());
            }
            expectedCompanyIds.add(Integer.parseInt(idStr));
        }
        Set<Integer> expectedCandidateIds = new HashSet<>();
        for (Map.Entry<String, List<JsonPath>> e : companyDataByCandidateId.entrySet()) {
            List<JsonPath> paths = e.getValue();
            if (paths == null || paths.isEmpty()) {
                continue;
            }
            int companyId = paths.get(0).getInt("data.company.id");
            if (expectedCompanyIds.contains(companyId)) {
                expectedCandidateIds.add(Integer.parseInt(e.getKey()));
            }
        }
        Set<Integer> actualCandidateIds = new HashSet<>();
        for (int i = 0; i < candidateData.length(); i++) {
            actualCandidateIds.add(candidateData.getJSONObject(i).getInt("id"));
        }
        Assert.assertEquals(actualCandidateIds, expectedCandidateIds,
                "Candidate id set does not match fixture candidates for expected companies. expectedCompanies=" + expectedResult);
    }

    private JSONObject asCrossEntityCandidatePayload(JSONObject payload) {
        payload.put("advancedSearchContext", "CANDIDATE");
        JSONObject defaultFilterInner = new JSONObject();
        defaultFilterInner.put("filters", new JSONArray());
        defaultFilterInner.put("subGroupJoinOperator", "AND");
        JSONObject defaultFilterRoot = new JSONObject();
        defaultFilterRoot.put("defaultFilterList", defaultFilterInner);
        payload.put("defaultFilterList", defaultFilterRoot);
        payload.getJSONObject("filterSearchList").getJSONArray("groupFilterList")
                .getJSONObject(0).getJSONArray("filters").getJSONObject(0).put("isCrossEntity", true);
        return payload;
    }

    private JSONObject createEntityCustomFieldCrossEntityCandidatePayload(
            String filterType, String filterValue, String dbField,
            String fieldType, String filterValue_TYPE) {
        String processedFilterValue = processEntityPlaceholders(filterValue);
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        JSONArray valueArray = new JSONArray();
        if ("is_empty".equals(filterType) || "has_any_value".equals(filterType)) {
            filterValueObj.put("value", valueArray);
        } else {
            for (String val : processedFilterValue.split(",")) {
                if ("INTEGER_LIST".equals(filterValue_TYPE)) {
                    valueArray.put(Integer.parseInt(val.trim()));
                } else {
                    valueArray.put(val.trim());
                }
            }
            filterValueObj.put("value", valueArray);
        }

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
        filter.put("entityType", "company");
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

    private JSONObject createDropdownAndMultiselectFilterSearchPayload(String filterType, String filterValue, String dbField,
                                                                       String fieldType, String filterValue_TYPE) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        JSONArray valueArray = new JSONArray();
        if ("is_empty".equals(filterType) || "has_any_value".equals(filterType)) {
            filterValueObj.put("value", valueArray);
        } else {
            for (String val : filterValue.split(",")) {
                valueArray.put(val.trim());
            }
            filterValueObj.put("value", valueArray);
        }
        return buildCompanyStylePayload(dbField, filterType, fieldType, filterValueObj);
    }

    private JSONObject createTextFieldFilterSearchPayload(String filterType, String filterValue, String dbField,
                                                          String fieldType, String filterValue_TYPE) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        if ("STRING_LIST".equals(filterValue_TYPE)) {
            JSONArray valueArray = new JSONArray();
            for (String val : filterValue.split(",")) {
                valueArray.put(val.trim());
            }
            filterValueObj.put("value", valueArray);
        } else {
            filterValueObj.put("value", filterValue);
        }
        return buildCompanyStylePayload(dbField, filterType, fieldType, filterValueObj);
    }

    private JSONObject createNumberFilterSearchPayload(String filterType, String filterValue, String dbField,
                                                       String fieldType, String filterValue_TYPE) {
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
        return buildCompanyStylePayload(dbField, filterType, fieldType, filterValueObj);
    }

    private JSONObject createDateFilterSearchPayload(String filterType, String filterValue, String dbField,
                                                     String fieldType, String filterValue_TYPE) {
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
        return buildCompanyStylePayload(dbField, filterType, fieldType, filterValueObj);
    }

    private JSONObject createCheckboxFilterSearchPayload(String filterType, String filterValue, String dbField,
                                                         String fieldType, String filterValue_TYPE) {
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        if ("INTEGER".equals(filterValue_TYPE)) {
            filterValueObj.put("value", Integer.parseInt(filterValue.trim()));
        } else {
            JSONArray valueArray = new JSONArray();
            valueArray.put(Integer.parseInt(filterValue.trim()));
            filterValueObj.put("value", valueArray);
        }
        return buildCompanyStylePayload(dbField, filterType, fieldType, filterValueObj);
    }

    private JSONObject buildCompanyStylePayload(String dbField, String filterType, String fieldType, JSONObject filterValueObj) {
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "companies");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "company");
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
        payload.put("advancedSearchContext", "COMPANY");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        return payload;
    }

    private Map<String, Integer> createCompanyCustomFields() {
        Map<String, Integer> ids = new HashMap<>();
        List<String> entityTypes = new ArrayList<>(List.of(
                "candidate", "company", "deals", "job", "contact", "user", "team",
                "text", "email", "phonenumber", "longtext", "number", "date", "social_profile",
                "dropdown", "multiselect", "checkbox"));
        ExecutorService executor = Executors.newFixedThreadPool(7);
        try {
            List<CompletableFuture<AbstractMap.SimpleEntry<String, Response>>> futures = entityTypes.stream()
                    .map(entity -> CompletableFuture.supplyAsync(() -> {
                        String fieldName = entity + "CF";
                        Response response;
                        if ("dropdown".equals(entity) || "multiselect".equals(entity)) {
                            response = function.createCustomFieldsResponse(albatrossURL, ownerAlbatrossAuthToken, "company",
                                    fieldName, entity, "Option A, Option B, OptionC");
                        } else {
                            response = function.createCustomFieldsResponse(albatrossURL, ownerAlbatrossAuthToken, "company",
                                    fieldName, entity, "");
                        }
                        return new AbstractMap.SimpleEntry<>(fieldName, response);
                    }, executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<AbstractMap.SimpleEntry<String, Response>> future : futures) {
                AbstractMap.SimpleEntry<String, Response> result = future.get();
                Assert.assertEquals(result.getValue().getStatusCode(), 200, "Failed to create company custom field: " + result.getKey());
                ids.put(result.getKey(), result.getValue().jsonPath().getInt("data.custumField.columnid"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating company custom fields", e);
        } finally {
            executor.shutdown();
        }
        return ids;
    }

    private Map<String, Integer> createCandidateCustomFields() {
        Map<String, Integer> ids = new HashMap<>();
        List<String> entityTypes = new ArrayList<>(List.of(
                "candidate", "company", "deal", "job", "contact", "user", "team",
                "text", "email", "phonenumber", "longtext", "number", "date", "social_profile"));
        ExecutorService executor = Executors.newFixedThreadPool(7);
        try {
            List<CompletableFuture<AbstractMap.SimpleEntry<String, Response>>> futures = entityTypes.stream()
                    .map(entity -> CompletableFuture.supplyAsync(() -> {
                        String name = entity + "CF";
                        Response response = function.createCustomFieldsResponse(
                                albatrossURL, ownerAlbatrossAuthToken, "candidate", name, entity, "");
                        return new AbstractMap.SimpleEntry<>(name, response);
                    }, executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<AbstractMap.SimpleEntry<String, Response>> future : futures) {
                AbstractMap.SimpleEntry<String, Response> result = future.get();
                Assert.assertEquals(result.getValue().getStatusCode(), 200, "Failed to create candidate custom field: " + result.getKey());
                ids.put(result.getKey(), result.getValue().jsonPath().getInt("data.custumField.columnid"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating candidate custom fields", e);
        } finally {
            executor.shutdown();
        }
        return ids;
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
            CompletableFuture<JsonPath> contactJson1Future = companyJson1Future.thenApplyAsync(c1 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, c1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> contactJson2Future = companyJson2Future.thenApplyAsync(c2 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, c2.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> jobJson1Future = companyJson1Future.thenCombineAsync(contactJson1Future, (c1, ct1) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, c1.getString("slug"), ct1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> jobJson2Future = companyJson2Future.thenCombineAsync(contactJson2Future, (c2, ct2) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, c2.getString("slug"), ct2.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> dealJson1Future = jobJson1Future.thenApplyAsync(j1 -> {
                String cs = companyJson1Future.join().getString("slug");
                String cts = contactJson1Future.join().getString("slug");
                return function.createNewDealWithMandatoryFields(baseURL, accountOwnerAPIKey, cs, cts, j1.getString("slug")).jsonPath();
            }, executor);
            CompletableFuture<JsonPath> dealJson2Future = jobJson2Future.thenApplyAsync(j2 -> {
                String cs = companyJson2Future.join().getString("slug");
                String cts = contactJson2Future.join().getString("slug");
                return function.createNewDealWithMandatoryFields(baseURL, accountOwnerAPIKey, cs, cts, j2.getString("slug")).jsonPath();
            }, executor);
            CompletableFuture<JsonPath> userJsonFuture = CompletableFuture.supplyAsync(
                    () -> function.getUsers(baseURL, accountOwnerAPIKey).jsonPath(), executor);
            CompletableFuture<JsonPath> teamJsonFuture = userJsonFuture.thenApplyAsync((userJson) -> {
                ArrayList<String> team1UserId = new ArrayList<>();
                ArrayList<String> team2UserId = new ArrayList<>();
                team1UserId.add(String.valueOf(userJson.getInt("[1].id")));
                team1UserId.add(String.valueOf(userJson.getInt("[3].id")));
                team2UserId.add(String.valueOf(userJson.getInt("[0].id")));
                team2UserId.add(String.valueOf(userJson.getInt("[2].id")));
                Response t1 = allCrudFunctions.createTeam(albatrossURL, ownerAlbatrossAuthToken, "team1", team1UserId);
                Response t2 = allCrudFunctions.createTeam(albatrossURL, ownerAlbatrossAuthToken, "team2", team2UserId);
                t1.then().statusCode(200);
                t2.then().statusCode(200);
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
            JsonPath userJson = userJsonFuture.join();
            JsonPath teamJson = teamJsonFuture.join();
            entityCFValueMap.put("candidate1", candidateJson1Future.join().getString("slug"));
            entityCFValueMap.put("candidate2", candidateJson2Future.join().getString("slug"));
            entityCFValueMap.put("company1", companyJson1Future.join().getString("slug"));
            entityCFValueMap.put("company2", companyJson2Future.join().getString("slug"));
            entityCFValueMap.put("contact1", contactJson1Future.join().getString("slug"));
            entityCFValueMap.put("contact2", contactJson2Future.join().getString("slug"));
            entityCFValueMap.put("job1", jobJson1Future.join().getString("slug"));
            entityCFValueMap.put("job2", jobJson2Future.join().getString("slug"));
            entityCFValueMap.put("deal1", dealJson1Future.join().getString("slug"));
            entityCFValueMap.put("deal2", dealJson2Future.join().getString("slug"));
            entityCFValueMap.put("owner", String.valueOf(userJson.getInt("[0].id")));
            entityCFValueMap.put("admin", String.valueOf(userJson.getInt("[1].id")));
            entityCFValueMap.put("restricted", String.valueOf(userJson.getInt("[2].id")));
            entityCFValueMap.put("teamMember", String.valueOf(userJson.getInt("[3].id")));
            entityCFValueMap.put("team1", findTeamIdByName(teamJson, "team1"));
            entityCFValueMap.put("team2", findTeamIdByName(teamJson, "team2"));
            int entityCompany1Id = function.getCompanyIdBySlug(albatrossURL, ownerAlbatrossAuthToken, companyJson1Future.join().getString("slug"));
            int entityCompany2Id = function.getCompanyIdBySlug(albatrossURL, ownerAlbatrossAuthToken, companyJson2Future.join().getString("slug"));
            String e1 = String.valueOf(entityCompany1Id);
            String e2 = String.valueOf(entityCompany2Id);
            companyKeyToIdMap.put("entitycompany1", e1);
            companyKeyToIdMap.put("entitycompany2", e2);
        } finally {
            executor.shutdown();
        }
    }

    private static String findTeamIdByName(JsonPath teamPath, String teamName) {
        int n = teamPath.getInt("$.size()");
        for (int i = 0; i < n; i++) {
            if (teamName.equals(teamPath.getString("[" + i + "].team_name"))) {
                return teamPath.getString("[" + i + "].team_id");
            }
        }
        throw new IllegalStateException("Team not found: " + teamName);
    }

    private void createCompaniesFromCompanyCfData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/companyCF_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = companyJson.keySet().stream()
                    .filter(key -> key.startsWith("company"))
                    .map(companyKey -> CompletableFuture.runAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");
                        JSONObject processedPayload = processCompanyCfPayload(payload);
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, ownerAlbatrossAuthToken, processedPayload);
                        response.then().statusCode(200);
                        int companyId = response.jsonPath().getInt("data.company.id");
                        String slug = response.jsonPath().getString("data.company.slug");
                        String lower = companyKey.toLowerCase();
                        companyKeyToIdMap.put(lower, String.valueOf(companyId));
                        companyCfKeyToSlugMap.put(lower, slug);
                    }, executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private void updateCompanyCustomDateFieldsTimestamps() {
        timestampScenarios = createTimestampScenarios();
        String dateCFFieldName = "dateCF";
        if (!companyCfColumnIds.containsKey(dateCFFieldName)) {
            return;
        }
        String dateCFDbField = "custcolumn" + companyCfColumnIds.get(dateCFFieldName);
        List<Map.Entry<String, Map<String, String>>> scenarios = new ArrayList<>(timestampScenarios.entrySet());
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            int companyIndex = 0;
            for (Map.Entry<String, Map<String, String>> scenario : scenarios) {
                if (companyIndex >= countFixtureCompanies()) {
                    break;
                }
                final int idx = companyIndex;
                final Map<String, String> timestamps = scenario.getValue();
                futures.add(CompletableFuture.runAsync(() -> {
                    String companyKey = "company" + (idx + 1);
                    String companyIdStr = companyKeyToIdMap.get(companyKey);
                    if (companyIdStr == null) {
                        return;
                    }
                    int companyId = Integer.parseInt(companyIdStr);
                    String dateValue = timestamps.get(dateCFFieldName);
                    if (dateValue != null) {
                        JSONObject fieldsAndTimestamps = new JSONObject();
                        fieldsAndTimestamps.put(dateCFDbField, dateValue);
                        Response updateResponse = ReaperIntegration.updateCompanyFields(companyId, fieldsAndTimestamps);
                        if (updateResponse.getStatusCode() != 200) {
                            Assert.fail("Failed to update custom date field for company: " + companyId);
                        }
                    }
                }, executor));
                companyIndex++;
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private int countFixtureCompanies() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/companyCF_data.json");
        return (int) companyJson.keySet().stream().filter(k -> k.startsWith("company")).count();
    }

    private Map<String, Map<String, String>> createTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();
        String todayDate = DateUtil.getTodayDateString("yyyy-MM-dd");
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("dateCF", todayDate);
        scenarios.put("today_scenario", todayTimestamps);
        String yesterdayDate = DateUtil.getYesterdayDateString("yyyy-MM-dd");
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("dateCF", yesterdayDate);
        scenarios.put("yesterday_scenario", yesterdayTimestamps);
        String thisWeekDate = DateUtil.getThisWeekDateString();
        LocalDate thisWeekLocalDate = LocalDate.parse(thisWeekDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("dateCF", thisWeekLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("this_week_scenario", thisWeekTimestamps);
        String lastWeekDate = DateUtil.getLastWeekDateString();
        LocalDate lastWeekLocalDate = LocalDate.parse(lastWeekDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("dateCF", lastWeekLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("last_week_scenario", lastWeekTimestamps);
        String thisMonthDate = DateUtil.getThisMonthDateString();
        LocalDate thisMonthLocalDate = LocalDate.parse(thisMonthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("dateCF", thisMonthLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("this_month_scenario", thisMonthTimestamps);
        String lastMonthDate = DateUtil.getLastMonthDateString();
        LocalDate lastMonthLocalDate = LocalDate.parse(lastMonthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("dateCF", lastMonthLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("last_month_scenario", lastMonthTimestamps);
        String thisQuarterDate = DateUtil.getThisQuarterDateString();
        LocalDate thisQuarterLocalDate = LocalDate.parse(thisQuarterDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("dateCF", thisQuarterLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("this_quarter_scenario", thisQuarterTimestamps);
        String lastQuarterDate = DateUtil.getLastQuarterDateString();
        LocalDate lastQuarterLocalDate = LocalDate.parse(lastQuarterDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("dateCF", lastQuarterLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("last_quarter_scenario", lastQuarterTimestamps);
        String thisYearDate = DateUtil.getThisYearDateString();
        LocalDate thisYearLocalDate = LocalDate.parse(thisYearDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("dateCF", thisYearLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("this_year_scenario", thisYearTimestamps);
        String lastYearDate = DateUtil.getLastYearDateString();
        LocalDate lastYearLocalDate = LocalDate.parse(lastYearDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("dateCF", lastYearLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        scenarios.put("last_year_scenario", lastYearTimestamps);
        Map<String, String> staticTimestamps1 = new HashMap<>();
        staticTimestamps1.put("dateCF", "2022-06-15");
        scenarios.put("static_date_scenario1", staticTimestamps1);
        Map<String, String> staticTimestamps2 = new HashMap<>();
        staticTimestamps2.put("dateCF", "2023-03-10");
        scenarios.put("static_date_scenario2", staticTimestamps2);
        Map<String, String> staticTimestamps3 = new HashMap<>();
        staticTimestamps3.put("dateCF", "2024-06-06");
        scenarios.put("static_date_scenario3", staticTimestamps3);
        Map<String, String> staticTimestamps4 = new HashMap<>();
        staticTimestamps4.put("dateCF", "2025-02-14");
        scenarios.put("static_date_scenario4", staticTimestamps4);
        return scenarios;
    }

    private JSONObject processCompanyCfPayload(JSONObject payload) {
        JSONObject processedPayload = new JSONObject();
        for (String key : payload.keySet()) {
            Object value = payload.get(key);
            if (value instanceof JSONObject) {
                processedPayload.put(key, processCompanyCfPayload((JSONObject) value));
            } else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                JSONArray processedArray = new JSONArray();
                for (int i = 0; i < array.length(); i++) {
                    Object item = array.get(i);
                    if (item instanceof JSONObject) {
                        processedArray.put(processCompanyCfPayload((JSONObject) item));
                    } else {
                        processedArray.put(item);
                    }
                }
                processedPayload.put(key, processedArray);
            } else if (key.startsWith("{") && key.endsWith("}")) {
                String trimmedKey = key.substring(1, key.length() - 1);
                if (companyCfColumnIds.containsKey(trimmedKey)) {
                    String newKey = "custcolumn" + companyCfColumnIds.get(trimmedKey);
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

    private JSONObject processCandidateCfPayload(JSONObject payload) {
        JSONObject processedPayload = new JSONObject();
        for (String key : payload.keySet()) {
            Object value = payload.get(key);
            if (key.startsWith("{") && key.endsWith("}")) {
                String trimmedKey = key.substring(1, key.length() - 1);
                if (candidateCfColumnIds.containsKey(trimmedKey)) {
                    String newKey = "custcolumn" + candidateCfColumnIds.get(trimmedKey);
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

    private void createCandidatesFromCrossEntityData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/candidateCrossEntity_Data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = candidateJson.keySet().stream()
                    .filter(key -> key.startsWith("candidate"))
                    .map(candidateKey -> CompletableFuture.runAsync(() -> {
                        JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                        JSONObject payload = new JSONObject(candidateEntry.getJSONObject("payload").toString());
                        JSONObject processedPayload = processCandidateCfPayload(payload);
                        applyCandidateCompanySlugPlaceholder(processedPayload);
                        Response response = allCrudFunctions.createCandidateWithJson(
                                albatrossURL, ownerAlbatrossAuthToken, processedPayload);
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
            JsonPath jp = companyResponse.jsonPath();
            List<JsonPath> list = new ArrayList<>();
            list.add(jp);
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
        String slug = companyCfKeyToSlugMap.get(companyKey.toLowerCase());
        Assert.assertNotNull(slug, "No company slug for " + value + " (expected " + companyKey + " from companyCF_data)");
        payload.put("candidate_company_slug", slug);
    }

    public Response getCompany(String companySlug) {
        String basePath = "/companies/{companySlug}";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companySlug", companySlug);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, ownerAlbatrossAuthToken, null, pathParams, true);
        response.then().statusCode(200);
        return response;
    }

    @DataProvider(name = "entityCustomFieldCrossEntityCandidateFilterData")
    public Object[][] entityCustomFieldCrossEntityCandidateFilterData() {
        JSONObject filterData = readJsonFileFromPath(
                "src/test/resources/filtersDataProvider/candidateCompanyCustomFieldCrossEntityFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        List<String> entityTypes = Arrays.asList(
                "candidate", "company", "contact", "job", "deal", "deals", "user", "team");

        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String rowFieldType = test.getString("fieldType");
                if (!entityTypes.contains(rowFieldType)) {
                    continue;
                }
                testData.add(new Object[]{
                        key,
                        test.getString("filterType"),
                        test.getString("filterValue"),
                        test.getString("dbField"),
                        test.getString("expectedResult"),
                        rowFieldType,
                        test.optString("filterValue_TYPE", "STRING_LIST")
                });
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "textCustomFieldCrossEntityCandidateFilterData", parallel = true)
    public Object[][] textCustomFieldCrossEntityCandidateFilterData() {
        return extractRowsWhere((test) -> {
            String ft = test.getString("fieldType");
            return "text".equals(ft) || "phonenumber".equals(ft) || "longtext".equals(ft);
        });
    }

    @DataProvider(name = "numberCustomFieldCrossEntityCandidateFilterData", parallel = true)
    public Object[][] numberCustomFieldCrossEntityCandidateFilterData() {
        return extractRowsWhere((test) -> "number".equals(test.getString("fieldType")));
    }

    @DataProvider(name = "dateCustomFieldCrossEntityCandidateFilterData", parallel = true)
    public Object[][] dateCustomFieldCrossEntityCandidateFilterData() {
        return extractRowsWhere((test) -> "date".equals(test.getString("fieldType")));
    }

    @DataProvider(name = "dropdownMultiselectCustomFieldCrossEntityCandidateFilterData", parallel = true)
    public Object[][] dropdownMultiselectCustomFieldCrossEntityCandidateFilterData() {
        return extractRowsWhere((test) -> {
            String ft = test.getString("fieldType");
            return "multiselect".equals(ft) || "dropdown".equals(ft);
        });
    }

    @DataProvider(name = "checkboxCustomFieldCrossEntityCandidateFilterData", parallel = true)
    public Object[][] checkboxCustomFieldCrossEntityCandidateFilterData() {
        return extractRowsWhere((test) -> "checkbox".equals(test.getString("fieldType")));
    }

    private Object[][] extractRowsWhere(java.util.function.Predicate<JSONObject> includeTest) {
        JSONObject filterData = readJsonFileFromPath(
                "src/test/resources/filtersDataProvider/candidateCompanyCustomFieldCrossEntityFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                if (!includeTest.test(test)) {
                    continue;
                }
                testData.add(new Object[]{
                        key,
                        test.getString("filterType"),
                        test.getString("filterValue"),
                        test.getString("dbField"),
                        test.getString("expectedResult"),
                        test.getString("fieldType"),
                        test.optString("filterValue_TYPE", "STRING_LIST")
                });
            }
        }
        return testData.toArray(new Object[0][0]);
    }
}
