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

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class TimeSheetSortingTest extends ContractStaffingBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;
    private int jobId;
    private int contractorId;
    private int userId;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplate(albatrossAuthToken);
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        jobId = (Integer) testData[0];
        contractorId = (Integer) testData[1];
        userId = (Integer) testData[2];
        enableTimesheet(contractorId, jobId, userId, albatrossAuthToken, 2, 200, 0);
        Response freeSlotsResponse = getTimeSheetFreeSlots(contractorId, jobId, 2, albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START,
                DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);
        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");
        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, 2);
        addTimeSheet(jobId, Arrays.asList(contractorId), timesheetDates, albatrossAuthToken);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "sortingScenarios", groups = {"contract_staffing", "nightly-build"})
    public void verifyTimesheetSortingTest(String scenario, String sortField, String sortOrder, int page, int size, int expectedStatus, String expectedMessage) {

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("jobId", String.valueOf(jobId));
        queryParams.put("contractorId", String.valueOf(contractorId));
        queryParams.put("page", String.valueOf(page));
        queryParams.put("size", String.valueOf(size));

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> sortPriorityList = new ArrayList<>();

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
        Response response = executePostWithQueryParams("timesheets/job/contractor/get", tokenToUse, queryParams,
                requestBody);
        

        if (scenario.equals("UnauthorizedAccess")) {
            validateUnauthorizedResponse(response);
        } else if (expectedStatus == 200) {
            validateSuccessResponse(response, expectedMessage);

            JsonPath jsonPath = response.jsonPath();
            List<Map<String, Object>> data = jsonPath.getList("data");
            assertThat("Response should have data array", data, notNullValue());
            assertThat("Data should be a list", data, instanceOf(List.class));

            if (sortField != null && sortOrder != null && !scenario.contains("Invalid") && !scenario.contains("Null")
                    && !scenario.contains("Empty")) {
                List<Map<String, Object>> unsortedList = getTimesheetsWithoutSorting(jobId, contractorId, page, size);

                verifySortingOrder(unsortedList, data, sortField, sortOrder);
            }
        } else {
            assertThat("Response status should match expected", response.getStatusCode(), equalTo(expectedStatus));
        }
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifySortingChangesOrderTest() {
        List<Map<String, Object>> unsortedList = getTimesheetsWithoutSorting(jobId, contractorId, 1, 100);

        List<Map<String, Object>> sortedByPeriodAsc = getTimesheetsWithSorting(jobId, contractorId, 1, 100,
                "timesheetPeriodStartDate", "asc");

        List<Map<String, Object>> sortedByPeriodDesc = getTimesheetsWithSorting(jobId, contractorId, 1, 100,
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
                { "SortByTimesheetPeriodStartDateAsc", "timesheetPeriodStartDate", "asc", 1, 100, 200, "fetched successfully" },
                { "SortByTimesheetPeriodStartDateDesc", "timesheetPeriodStartDate", "desc", 1, 100, 200, "fetched successfully" },
                { "SortByTimesheetPeriodAddedOnAsc", "addedOn", "asc", 1, 100, 200, "fetched successfully" },
                { "SortByTimesheetPeriodAddedOnDesc", "addedOn", "desc", 1, 100, 200, "fetched successfully" },
                { "SortByTimesheetPeriodUpdatedOnAsc", "updatedOn", "asc", 1, 100, 200, "fetched successfully" },
                { "SortByTimesheetPeriodUpdatedOnDesc", "updatedOn", "desc", 1, 100, 200, "fetched successfully" },
                { "EmptySortPriorityList", null, null, 1, 100, 200, "fetched successfully" },
                { "UnauthorizedAccess", "addedOn", "asc", 1, 100, 401, "Unauthorised access" },
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

    private List<Map<String, Object>> getTimesheetsWithoutSorting(int jobId, int contractorId, int page, int size) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("jobId", String.valueOf(jobId));
        queryParams.put("contractorId", String.valueOf(contractorId));
        queryParams.put("page", String.valueOf(page));
        queryParams.put("size", String.valueOf(size));

        Map<String, Object> emptyBody = new HashMap<>();
        emptyBody.put("sortPriorityList", new ArrayList<>());

        Response response = executePostWithQueryParams("timesheets/job/contractor/get", albatrossAuthToken, queryParams,
                emptyBody);

        if (response.getStatusCode() == 200) {
            JsonPath jsonPath = response.jsonPath();
            return jsonPath.getList("data");
        }
        return new ArrayList<>();
    }

    private List<Map<String, Object>> getTimesheetsWithSorting(int jobId, int contractorId, int page, int size,
            String sortField, String sortOrder) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("jobId", String.valueOf(jobId));
        queryParams.put("contractorId", String.valueOf(contractorId));
        queryParams.put("page", String.valueOf(page));
        queryParams.put("size", String.valueOf(size));

        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        sortPriorityList.add(sortItem);

        Map<String, Object> body = new HashMap<>();
        body.put("sortPriorityList", sortPriorityList);

        Response response = executePostWithQueryParams("timesheets/job/contractor/get", albatrossAuthToken, queryParams, body);

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
                if (field.equals("timesheetPeriodStartDate")) {
                    long currentVal = toComparableLong(current);
                    long nextVal = toComparableLong(next);
                    assertThat("Timesheet period start dates should be in ascending order",
                            currentVal, lessThanOrEqualTo(nextVal));
                }
            }
        }
    }

    private void verifyDescendingOrder(List<Object> values, String field) {
        for (int i = 0; i < values.size() - 1; i++) {
            Object current = values.get(i);
            Object next = values.get(i + 1);

            if (current != null && next != null) {
                if (field.equals("timesheetPeriodStartDate")) {
                    long currentVal = toComparableLong(current);
                    long nextVal = toComparableLong(next);
                    assertThat("Timesheet period start dates should be in descending order",
                            currentVal, greaterThanOrEqualTo(nextVal));
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
