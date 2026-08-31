package io.recruitcrm.contractStaffing.shiftBasedTimesheets;

import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.TestUtil;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.rcrm.api.pojo.albatross.contractStaffing.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class TimeSheetSortingOnDealsTest extends ContractStaffingBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;
    private int dealId;
    private int jobId;
    private int contractorId;
    private int userId;

    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplate(albatrossAuthToken);

        String candidateSlug = function.getEntityResponse(baseURL, apiAuthToken, "candidate");
        contractorId = function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug, "candidate").jsonPath().getInt("data.candidate.id");
        String dealSlug = function.getEntityResponse(baseURL, apiAuthToken, "deal");
        JsonPath dealJsonPath = allCrudFunctions.getDealResponse(albatrossURL, albatrossAuthToken, dealSlug).jsonPath();
        
        dealId = dealJsonPath.getInt("data.deal.id");
        jobId = dealJsonPath.getInt("data.deal.relatedjob");
        String jobSlug = dealJsonPath.getString("data.deal.jobslug");
        Response usersResponse = function.getUsers(baseURL, apiAuthToken);
        usersResponse.then().statusCode(200);
        JsonPath usersJsonPath = usersResponse.jsonPath();
        userId = usersJsonPath.getInt("[0].id");
        function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug, jobSlug);
        enableTimesheet(contractorId, jobId, userId, albatrossAuthToken, 2, 200, 1);
        Response freeSlotsResponse = getTimeSheetFreeSlots(contractorId, jobId, 2, albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START,
                DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);
        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");
        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, 2);
        addTimeSheet(jobId, Arrays.asList(contractorId), timesheetDates, albatrossAuthToken);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "sortingScenarios", groups = {"contract_staffing", "nightly-build"})
    public void verifyTimesheetSortingOnDealsTest(String scenario, String sortField, String sortOrder,
            int page, int size, int expectedStatus, String expectedMessage, int reimbursementCount) {

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", String.valueOf(page));
        queryParams.put("size", String.valueOf(size));

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> sortPriorityList = new ArrayList<>();

        if (reimbursementCount > 0) {
            Response getAllTimesheetsResponse = getAllTimesheets(jobId, contractorId, 1, 100, albatrossAuthToken);
            if (getAllTimesheetsResponse.statusCode() == 200) {
                JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse.jsonPath();
                List<Map<String, Object>> timesheets = getAllTimesheetsJsonPath.getList("data");
                if (!timesheets.isEmpty()) {
                    int timesheetId = ((Number) timesheets.get(0).get("id")).intValue();
                    createReimbursement("Reimbursement line " + reimbursementCount, 10.0 + reimbursementCount,
                            "test.pdf", timesheetId, albatrossAuthToken);
                }
            }
        }
        switch (scenario) {
            case "EmptyPayload":
                requestBody = new HashMap<>();
                break;

            case "NullSortPriorityList":
                requestBody.put("sortPriorityList", null);
                break;

            case "EmptySortPriorityList":
                requestBody.put("sortPriorityList", new ArrayList<>());
                break;

            case "InvalidField":
            case "InvalidOrder":
            case "NullField":
            case "EmptyField":
                Map<String, Object> sortItem = new HashMap<>();
                if (scenario.equals("NullField")) {
                    sortItem.put("field", null);
                } else if (scenario.equals("EmptyField")) {
                    sortItem.put("field", "");
                } else {
                    sortItem.put("field", sortField != null ? sortField : "timesheetPeriodStartDate");
                }
                if (scenario.equals("InvalidOrder")) {
                    sortItem.put("order", sortOrder);
                } else {
                    sortItem.put("order", sortOrder != null ? sortOrder : "asc");
                }
                sortPriorityList.add(sortItem);
                requestBody.put("sortPriorityList", sortPriorityList);
                break;

            case "UnauthorizedAccess":
                if (sortField != null && sortOrder != null) {
                    Map<String, Object> validSortItem = new HashMap<>();
                    validSortItem.put("field", sortField);
                    validSortItem.put("order", sortOrder);
                    sortPriorityList.add(validSortItem);
                }
                requestBody.put("sortPriorityList", sortPriorityList);
                break;

            default:
                if (sortField != null && sortOrder != null) {
                    Map<String, Object> validSortItem = new HashMap<>();
                    validSortItem.put("field", sortField);
                    validSortItem.put("order", sortOrder);
                    sortPriorityList.add(validSortItem);
                    requestBody.put("sortPriorityList", sortPriorityList);
                } else {
                    requestBody.put("sortPriorityList", new ArrayList<>());
                }
                break;
        }

        String tokenToUse = scenario.equals("UnauthorizedAccess") ? "invalid_token_123" : albatrossAuthToken;
        String endpoint = "timesheets/deal/" + dealId + "/get";
        Response response = executePostWithQueryParams(endpoint, tokenToUse, queryParams, requestBody);

        if (scenario.equals("UnauthorizedAccess")) {
            validateUnauthorizedResponse(response);
        } else if (expectedStatus == 200) {
            validateSuccessResponse(response, expectedMessage);

            JsonPath jsonPath = response.jsonPath();
            List<Map<String, Object>> data = jsonPath.getList("data");
            assertThat("Response should have data array", data, notNullValue());
            assertThat("Data should be a list", data, instanceOf(List.class));

            for (int i = 0; i < data.size(); i++) {
                assertThat("Reimbursement count should be " + reimbursementCount, data.get(i).get("reimbursementCount"), equalTo(reimbursementCount));
            }

            if (sortField != null && sortOrder != null && !scenario.contains("Invalid") && !scenario.contains("Null")
                    && !scenario.contains("Empty")) {
                List<Map<String, Object>> unsortedList = getTimesheetsByDealWithoutSorting(dealId, page, size);

                if (!data.isEmpty() && !unsortedList.isEmpty()) {
                    verifySortingOrder(unsortedList, data, sortField, sortOrder);
                }
            }
        } else {
            assertThat("Response status should match expected", response.getStatusCode(), equalTo(expectedStatus));
        }
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifySortingChangesOrderOnDealsTest() {
        List<Map<String, Object>> unsortedList = getTimesheetsByDealWithoutSorting(dealId, 1, 50);

        List<Map<String, Object>> sortedByPeriodAsc = getTimesheetsByDealWithSorting(dealId, 1, 50,
                "timesheetPeriodStartDate", "asc");

        List<Map<String, Object>> sortedByPeriodDesc = getTimesheetsByDealWithSorting(dealId, 1, 50,
                "timesheetPeriodStartDate", "desc");

        assertThat("Unsorted list should not be null", unsortedList, notNullValue());
        assertThat("Sorted by period asc should not be null", sortedByPeriodAsc, notNullValue());
        assertThat("Sorted by period desc should not be null", sortedByPeriodDesc, notNullValue());

        if (!unsortedList.isEmpty() && !sortedByPeriodAsc.isEmpty()) {
            verifySortingOrder(unsortedList, sortedByPeriodAsc, "timesheetPeriodStartDate", "asc");
        }
        if (!unsortedList.isEmpty() && !sortedByPeriodDesc.isEmpty()) {
            verifySortingOrder(unsortedList, sortedByPeriodDesc, "timesheetPeriodStartDate", "desc");
        }

        List<Object> ascValues = extractFieldValues(sortedByPeriodAsc, "timesheetPeriodStartDate");
        List<Object> descValues = extractFieldValues(sortedByPeriodDesc, "timesheetPeriodStartDate");

        if (!ascValues.isEmpty() && !descValues.isEmpty() && !areAllValuesEqual(ascValues) && !areAllValuesEqual(descValues)) {
            Collections.reverse(descValues);
            assertThat("Ascending and descending orders should be reverse of each other",
                    ascValues, equalTo(descValues));
        }
    }

    @DataProvider(name = "sortingScenarios", parallel = true)
    public Object[][] getSortingScenarios() {
        return new Object[][] {
                { "SortByTimesheetPeriodStartDateAsc", "timesheetPeriodStartDate", "asc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByTimesheetPeriodStartDateDesc", "timesheetPeriodStartDate", "desc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByTimesheetPeriodAddedOnAsc", "addedOn", "asc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByTimesheetPeriodAddedOnDesc", "addedOn", "desc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByTimesheetPeriodUpdatedOnAsc", "updatedOn", "asc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByTimesheetPeriodUpdatedOnDesc", "updatedOn", "desc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByContractorNameAsc", "contractorName", "asc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByContractorNameDesc", "contractorName", "desc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByJobNameAsc", "jobName", "asc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByJobNameDesc", "jobName", "desc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByJobDurationStartDateAsc", "jobDurationStartDate", "asc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByJobDurationStartDateDesc", "jobDurationStartDate", "desc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByPayRateAsc", "payRate", "asc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByPayRateDesc", "payRate", "desc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByBillRateAsc", "billRate", "asc", 1, 50, 200, "fetched successfully", 0 },
                { "SortByBillRateDesc", "billRate", "desc", 1, 50, 200, "fetched successfully", 0 },
                { "EmptySortPriorityList", null, null, 1, 50, 200, "fetched successfully", 0 },
                { "UnauthorizedAccess", "timesheetPeriodStartDate", "asc", 1, 50, 401, "Unauthorised access", 0 },
                { "SortByContractorNameAsc", "contractorName", "asc", 1, 50, 200, "fetched successfully", 1 },
                { "SortByTimesheetPeriodAddedOnDesc", "addedOn", "desc", 1, 50, 200, "fetched successfully", 1 },
        };
    }

    private Response executePostWithQueryParams(String endpoint, String authToken,
            Map<String, String> queryParams, Object payload) {
        Object requestPayload = payload;
        if (payload instanceof Map) {
            requestPayload = TestUtil.getSerializedJSON(payload);
        }
        return RestClient.doPost("JSON", timesheetBaseURL, endpoint, authToken, queryParams, true, requestPayload);
    }

    private List<Map<String, Object>> getTimesheetsByDealWithoutSorting(int dealId, int page, int size) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", String.valueOf(page));
        queryParams.put("size", String.valueOf(size));

        Map<String, Object> emptyBody = new HashMap<>();
        emptyBody.put("sortPriorityList", new ArrayList<>());

        String endpoint = "timesheets/deal/" + dealId + "/get";
        Response response = executePostWithQueryParams(endpoint, albatrossAuthToken, queryParams, emptyBody);

        if (response.getStatusCode() == 200) {
            JsonPath jsonPath = response.jsonPath();
            return jsonPath.getList("data");
        }
        return new ArrayList<>();
    }

    private List<Map<String, Object>> getTimesheetsByDealWithSorting(int dealId, int page, int size,
            String sortField, String sortOrder) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", String.valueOf(page));
        queryParams.put("size", String.valueOf(size));

        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        sortPriorityList.add(sortItem);

        Map<String, Object> body = new HashMap<>();
        body.put("sortPriorityList", sortPriorityList);

        String endpoint = "timesheets/deal/" + dealId + "/get";
        Response response = executePostWithQueryParams(endpoint, albatrossAuthToken, queryParams, body);

        if (response.getStatusCode() == 200) {
            JsonPath jsonPath = response.jsonPath();
            return jsonPath.getList("data");
        }
        return new ArrayList<>();
    }

    private void verifySortingOrder(List<Map<String, Object>> unsortedList, List<Map<String, Object>> sortedList,
            String sortField, String sortOrder) {
        assertThat("Sorted list should not be empty", sortedList, not(empty()));
        assertThat("Unsorted list should not be empty", unsortedList, not(empty()));

        List<Object> sortedValues = extractFieldValues(sortedList, sortField);

        if (!areAllValuesEqual(sortedValues)) {
            if (sortOrder.equalsIgnoreCase("asc")) {
                verifyAscendingOrder(sortedValues, sortField);
            } else if (sortOrder.equalsIgnoreCase("desc")) {
                verifyDescendingOrder(sortedValues, sortField);
            }
        }
    }

    private List<Object> extractFieldValues(List<Map<String, Object>> timesheets, String field) {
        return timesheets.stream()
                .map(ts -> {
                    if ("timesheetPeriodStartDate".equals(field)) {
                        return ts.get("timesheetPeriodStartDate");
                    }
                    if ("addedOn".equals(field)) {
                        return ts.get("addedOn");
                    }
                    if ("updatedOn".equals(field)) {
                        return ts.get("updatedOn");
                    }
                    if ("contractorName".equals(field)) {
                        return ts.get("contractorName");
                    }
                    if ("jobName".equals(field)) {
                        return ts.get("jobName");
                    }
                    if ("jobDurationStartDate".equals(field)) {
                        return ts.get("jobDurationStartDate");
                    }
                    if ("payRate".equals(field)) {
                        return ts.get("payRate");
                    }
                    if ("billRate".equals(field)) {
                        return ts.get("billRate");
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean areAllValuesEqual(List<Object> values) {
        if (values.isEmpty() || values.size() == 1) {
            return true;
        }
        Object first = values.get(0);
        return values.stream().allMatch(v -> Objects.equals(v, first));
    }

    private void verifyAscendingOrder(List<Object> values, String field) {
        for (int i = 0; i < values.size() - 1; i++) {
            Object current = values.get(i);
            Object next = values.get(i + 1);

            if (current != null && next != null) {
                if (field.equals("timesheetPeriodStartDate") || field.equals("jobDurationStartDate")) {
                    long currentVal = toComparableLong(current);
                    long nextVal = toComparableLong(next);
                    assertThat(field + " should be in ascending order", currentVal, lessThanOrEqualTo(nextVal));
                } else if (field.equals("addedOn") || field.equals("updatedOn")) {
                    long currentVal = toComparableLong(current);
                    long nextVal = toComparableLong(next);
                    assertThat("Timestamps should be in ascending order", currentVal, lessThanOrEqualTo(nextVal));
                } else if (field.equals("contractorName") || field.equals("jobName")) {
                    String currentStr = String.valueOf(current);
                    String nextStr = String.valueOf(next);
                    assertThat(field + " should be in ascending order",
                            currentStr.compareToIgnoreCase(nextStr), lessThanOrEqualTo(0));
                } else if (field.equals("payRate") || field.equals("billRate")) {
                    double currentVal = toComparableDouble(current);
                    double nextVal = toComparableDouble(next);
                    assertThat(field + " should be in ascending order", currentVal, lessThanOrEqualTo(nextVal));
                }
            }
        }
    }

    private void verifyDescendingOrder(List<Object> values, String field) {
        for (int i = 0; i < values.size() - 1; i++) {
            Object current = values.get(i);
            Object next = values.get(i + 1);

            if (current != null && next != null) {
                if (field.equals("timesheetPeriodStartDate") || field.equals("jobDurationStartDate")) {
                    long currentVal = toComparableLong(current);
                    long nextVal = toComparableLong(next);
                    assertThat(field + " should be in descending order", currentVal, greaterThanOrEqualTo(nextVal));
                } else if (field.equals("addedOn") || field.equals("updatedOn")) {
                    long currentVal = toComparableLong(current);
                    long nextVal = toComparableLong(next);
                    assertThat("Timestamps should be in descending order", currentVal, greaterThanOrEqualTo(nextVal));
                } else if (field.equals("contractorName") || field.equals("jobName")) {
                    String currentStr = String.valueOf(current);
                    String nextStr = String.valueOf(next);
                    assertThat(field + " should be in descending order",
                            currentStr.compareToIgnoreCase(nextStr), greaterThanOrEqualTo(0));
                } else if (field.equals("payRate") || field.equals("billRate")) {
                    double currentVal = toComparableDouble(current);
                    double nextVal = toComparableDouble(next);
                    assertThat(field + " should be in descending order", currentVal, greaterThanOrEqualTo(nextVal));
                }
            }
        }
    }

    private long toComparableLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        throw new IllegalArgumentException("Cannot compare value: " + value);
    }

    private double toComparableDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        throw new IllegalArgumentException("Cannot compare value: " + value);
    }

    private void validateSuccessResponse(Response response, String expectedMessagePart) {
        JsonPath jsonPath = response.jsonPath();
        assertThat("Meta should not be null", jsonPath.get("meta"), notNullValue());
        assertThat("Meta should be a map", jsonPath.get("meta"), instanceOf(Map.class));
        if (expectedMessagePart != null && jsonPath.get("meta.message") != null) {
            assertThat("Message should contain expected text",
                    jsonPath.getString("meta.message"),
                    containsString(expectedMessagePart));
        }
    }

    private void validateUnauthorizedResponse(Response response) {
        assertThat("Response status should be 401", response.getStatusCode(), equalTo(401));
        validateErrorResponse(response, "Unauthorised access");
    }

    private void validateErrorResponse(Response response, String expectedMessagePart) {
        JsonPath jsonPath = response.jsonPath();
        assertThat("Error response should have meta", jsonPath.get("meta"), notNullValue());
        if (expectedMessagePart != null && jsonPath.get("meta.message") != null) {
            assertThat("Error message should contain expected text",
                    jsonPath.getString("meta.message"),
                    containsString(expectedMessagePart));
        }
    }
}
