package io.recruitcrm.contractStaffing.publicApi.hoursBased;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.contractStaffing.TimeDetails;
import io.rcrm.api.pojo.albatross.contractStaffing.TimesheetDate;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class GetTimesheetsListDataTest extends ContractStaffingBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;

    private String candidateSlug;
    private String companySlug;
    private String jobSlug;
    private String dealSlug;
    private int timesheetId;
    private String timesheetStartDate;
    private String timesheetEndDate;

    private int expectedTotalTimesheets;
    private String expectedSubmittedRegularHours;
    private int expectedSubmittedPayAmount;
    private int expectedSubmittedBillAmount;

    private static final String INCORRECT_SLUG = "00000000000000000000XXXXX";
    private static final String ENDPOINT = "timesheets";

  private static final int HOURS_PAY_RATE = 1;
    private static final int HOURS_BILL_RATE = 2;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        generateTimesheetData();
    }

    private void generateTimesheetData() {
        Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[4]).intValue();
        candidateSlug = (String) testData[5];
        companySlug = (String) testData[8];
        jobSlug = (String) testData[10];

        int timesheetFrequency = 2;

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200,0);

        Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

        addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates, albatrossAuthToken);

        Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId, 1, 100, albatrossAuthToken);
        assertThat(getAllTimesheetsResponse.statusCode(), is(200));
        JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse.jsonPath();
        List<Map<String, Object>> allTimesheets = getAllTimesheetsJsonPath.getList("data");
        assertThat("Should have created timesheets", allTimesheets.size(), greaterThan(0));
        timesheetId = ((Number) getAllTimesheetsJsonPath.get("data[0].id")).intValue();

        Response timeLogsResponse = getTimeSheetTimeLogs(timesheetId, albatrossAuthToken);
        JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
        List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");

        List<Map<String, Object>> timeLogsList = generateTimeLogIdsforHourBased(timeLogs, timesheetId);
        TimeDetails timeDetails = generateTimeDetailsForHourBased(timeLogs, timesheetId);

        Map<String, Object> submitTimeLogsRequest = new HashMap<>();
        submitTimeLogsRequest.put("timeLogs", timeLogsList);
        submitTimeLogsRequest.put("timeDetails", Arrays.asList(timeDetails));

        Response submitResponse = submitTimeLogsForTimesheetHourBased(submitTimeLogsRequest, albatrossAuthToken);
        assertThat(submitResponse.statusCode(), is(200));

        String timesheetPeriod = timeLogsJsonPath.getString("data.timeLogs[0].timesheetPeriod");
        if (timesheetPeriod != null && timesheetPeriod.contains(" - ")) {
            timesheetStartDate = extractDateFromPeriod(timesheetPeriod, true);
            timesheetEndDate = extractDateFromPeriod(timesheetPeriod, false);
        } else {
            timesheetStartDate = "2024-03-01";
            timesheetEndDate = "2025-12-31";
        }

        captureListExpectationsFromApi();
    }

    private void captureListExpectationsFromApi() {
        Response slugResponse = fetchTimesheetsList(singleParam("candidate_slug", candidateSlug));
        assertThat("Setup list call status", slugResponse.getStatusCode(), is(200));

        List<Map<String, Object>> dataItems = slugResponse.jsonPath().getList("data");
        assertThat("Setup list should return timesheets", dataItems, notNullValue());
        assertThat("Setup list should not be empty", dataItems.size(), greaterThan(0));

        expectedTotalTimesheets = dataItems.size();
        dealSlug = slugResponse.jsonPath().getString("data[0].related_entities_slug.deals");

        for (int i = 0; i < dataItems.size(); i++) {
            int statusId = slugResponse.jsonPath().getInt("data[" + i + "].timesheet_status.id");
            if (statusId == 2) {
                expectedSubmittedRegularHours = slugResponse.jsonPath()
                        .getString("data[" + i + "].hours.total_regular");
                expectedSubmittedPayAmount = slugResponse.jsonPath().getInt("data[" + i + "].pay.amount");
                expectedSubmittedBillAmount = slugResponse.jsonPath().getInt("data[" + i + "].bill.amount");
                return;
            }
        }
    }

    private String extractDateFromPeriod(String period, boolean start) {
        String[] parts = period.split(" - ");
        String dateStr = start ? parts[0].trim() : parts[1].trim();
        try {
            java.time.format.DateTimeFormatter inputFormatter =
                    java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy");
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr, inputFormatter);
            return date.toString();
        } catch (Exception e) {
            return start ? "2024-03-01" : "2025-12-31";
        }
    }

    // ========================== Helpers ==========================

    private Response fetchTimesheetsList(Map<String, String> queryParams) {
        return RestClient.doGet("JSON", baseURL, ENDPOINT,
                apiAuthToken, queryParams, null, true);
    }

    private Map<String, String> singleParam(String key, String value) {
        Map<String, String> params = new HashMap<>();
        params.put(key, value);
        return params;
    }

    private Map<String, String> buildAllQueryParams() {
        Map<String, String> params = new HashMap<>();
        params.put("time_logs", "1");
        params.put("page", "1");
        params.put("limit", "100");
        params.put("sort_by", "updatedon");
        params.put("sort_order", "asc");
        params.put("candidate_slug", candidateSlug);
        params.put("company_slug", companySlug);
        params.put("job_slug", jobSlug);
        return params;
    }

    private int countTimesheetsByStatusId(Response response, List<Map<String, Object>> data, int statusId) {
        if (data == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < data.size(); i++) {
            Number id = response.jsonPath().get("data[" + i + "].timesheet_status.id");
            if (id != null && id.intValue() == statusId) {
                count++;
            }
        }
        return count;
    }

    private void assertFullListSubmittedOpenPattern(Response response, List<Map<String, Object>> data,
            String tidPrefix) {
        assertThat(tidPrefix + " - submitted count (id=2)", countTimesheetsByStatusId(response, data, 2), is(1));
        assertThat(tidPrefix + " - open count (id=1)", countTimesheetsByStatusId(response, data, 1),
                is(expectedTotalTimesheets - 1));

        for (int i = 0; i < data.size(); i++) {
            String prefix = "data[" + i + "]";
            assertTimesheetItemStructure(response, prefix, tidPrefix + "[" + i + "]");
            int statusId = response.jsonPath().getInt(prefix + ".timesheet_status.id");
            if (statusId == 2) {
                assertSubmittedTimesheetValues(response, prefix, tidPrefix + "[" + i + "]-submitted");
            } else {
                assertOpenTimesheetValues(response, prefix, tidPrefix + "[" + i + "]-open");
            }
        }
    }

    private void assertTimesheetItemStructure(Response response, String prefix, String tid) {
        assertThat(prefix + ".id (" + tid + ")", response.jsonPath().get(prefix + ".id"), notNullValue());
        assertThat(prefix + ".timesheet_id (" + tid + ")", response.jsonPath().getString(prefix + ".timesheet_id"), notNullValue());
        assertThat(prefix + ".timesheet_id starts with TS- (" + tid + ")", response.jsonPath().getString(prefix + ".timesheet_id"), startsWith("TS-"));

        assertThat(prefix + ".timesheet_period.start_date (" + tid + ")", response.jsonPath().get(prefix + ".timesheet_period.start_date"), notNullValue());
        assertThat(prefix + ".timesheet_period.end_date (" + tid + ")", response.jsonPath().get(prefix + ".timesheet_period.end_date"), notNullValue());

        assertThat(prefix + ".related_entities_slug.candidate (" + tid + ")", response.jsonPath().getString(prefix + ".related_entities_slug.candidate"), is(candidateSlug));
        assertThat(prefix + ".related_entities_slug.job (" + tid + ")", response.jsonPath().getString(prefix + ".related_entities_slug.job"), is(jobSlug));
        assertThat(prefix + ".related_entities_slug.company (" + tid + ")", response.jsonPath().getString(prefix + ".related_entities_slug.company"), is(companySlug));
        assertThat(prefix + ".related_entities_slug.deals (" + tid + ")", response.jsonPath().getString(prefix + ".related_entities_slug.deals"), is(dealSlug));

        assertThat(prefix + ".timesheet_status.id (" + tid + ")", response.jsonPath().get(prefix + ".timesheet_status.id"), notNullValue());
        assertThat(prefix + ".timesheet_status.label (" + tid + ")", response.jsonPath().getString(prefix + ".timesheet_status.label"), notNullValue());
        assertThat(prefix + ".timesheet_status.performed_by (" + tid + ")", response.jsonPath().get(prefix + ".timesheet_status.performed_by"), notNullValue());
        assertThat(prefix + ".timesheet_status.performed_on (" + tid + ")", response.jsonPath().get(prefix + ".timesheet_status.performed_on"), notNullValue());

        assertThat(prefix + ".hours.total_overtime (" + tid + ")", response.jsonPath().getString(prefix + ".hours.total_overtime"), notNullValue());
        assertThat(prefix + ".hours.total_regular (" + tid + ")", response.jsonPath().getString(prefix + ".hours.total_regular"), notNullValue());
        assertThat(prefix + ".hours.total (" + tid + ")", response.jsonPath().getString(prefix + ".hours.total"), notNullValue());

        assertThat(prefix + ".pay.rate (" + tid + ")", response.jsonPath().getInt(prefix + ".pay.rate"), is(HOURS_PAY_RATE));
        assertThat(prefix + ".pay.currency (" + tid + ")", response.jsonPath().getString(prefix + ".pay.currency"), is("INR"));
        assertThat(prefix + ".pay.currency_id (" + tid + ")", response.jsonPath().getInt(prefix + ".pay.currency_id"), is(53));
        assertThat(prefix + ".pay.amount (" + tid + ")", response.jsonPath().get(prefix + ".pay.amount"), notNullValue());
        assertThat(prefix + ".pay.details (" + tid + ")", response.jsonPath().get(prefix + ".pay.details"), nullValue());

        assertThat(prefix + ".bill.rate (" + tid + ")", response.jsonPath().getInt(prefix + ".bill.rate"), is(HOURS_BILL_RATE));
        assertThat(prefix + ".bill.currency (" + tid + ")", response.jsonPath().getString(prefix + ".bill.currency"), is("INR"));
        assertThat(prefix + ".bill.currency_id (" + tid + ")", response.jsonPath().getInt(prefix + ".bill.currency_id"), is(53));
        assertThat(prefix + ".bill.amount (" + tid + ")", response.jsonPath().get(prefix + ".bill.amount"), notNullValue());
        assertThat(prefix + ".bill.details (" + tid + ")", response.jsonPath().get(prefix + ".bill.details"), nullValue());

        assertAmountSplitAndWeeklyOvertime(response, prefix, tid);
    }

    /**
     * PAY-748: pay/bill regular+overtime split and hours.weekly_overtime (replaces
     * hours.total_weekly_overtime) on every list-endpoint row.
     */
    private void assertAmountSplitAndWeeklyOvertime(Response response, String prefix, String tid) {
        Double payAmount = response.jsonPath().getDouble(prefix + ".pay.amount");
        Double payRegular = response.jsonPath().getDouble(prefix + ".pay.regular_amount");
        Double payOvertime = response.jsonPath().getDouble(prefix + ".pay.overtime_amount");
        assertThat(prefix + ".pay.regular_amount present (" + tid + ")", payRegular, notNullValue());
        assertThat(prefix + ".pay.overtime_amount present (" + tid + ")", payOvertime, notNullValue());
        assertThat(prefix + ".pay.regular_amount + pay.overtime_amount == pay.amount (" + tid + ")",
                Math.abs((payRegular + payOvertime) - payAmount) <= 0.01, is(true));

        Double billAmount = response.jsonPath().getDouble(prefix + ".bill.amount");
        Double billRegular = response.jsonPath().getDouble(prefix + ".bill.regular_amount");
        Double billOvertime = response.jsonPath().getDouble(prefix + ".bill.overtime_amount");
        assertThat(prefix + ".bill.regular_amount present (" + tid + ")", billRegular, notNullValue());
        assertThat(prefix + ".bill.overtime_amount present (" + tid + ")", billOvertime, notNullValue());
        assertThat(prefix + ".bill.regular_amount + bill.overtime_amount == bill.amount (" + tid + ")",
                Math.abs((billRegular + billOvertime) - billAmount) <= 0.01, is(true));

        assertThat(prefix + ".hours.total_weekly_overtime should no longer be returned (" + tid + ")",
                response.jsonPath().get(prefix + ".hours.total_weekly_overtime"), nullValue());
        assertThat(prefix + ".hours.weekly_overtime present (" + tid + ")",
                response.jsonPath().get(prefix + ".hours.weekly_overtime"), notNullValue());
        assertThat(prefix + ".hours.weekly_overtime.hours (" + tid + ")",
                response.jsonPath().get(prefix + ".hours.weekly_overtime.hours"), notNullValue());
        assertThat(prefix + ".hours.weekly_overtime.pay_amount (" + tid + ")",
                response.jsonPath().get(prefix + ".hours.weekly_overtime.pay_amount"), notNullValue());
        assertThat(prefix + ".hours.weekly_overtime.bill_amount (" + tid + ")",
                response.jsonPath().get(prefix + ".hours.weekly_overtime.bill_amount"), notNullValue());
    }

    private void assertPaginationStructure(Response response, String tid) {
        assertThat("current_page (" + tid + ")", response.jsonPath().getInt("current_page"), is(1));
        assertThat("first_page_url (" + tid + ")", response.jsonPath().getString("first_page_url"), notNullValue());
        assertThat("path (" + tid + ")", response.jsonPath().getString("path"), notNullValue());
        assertThat("per_page (" + tid + ")", response.jsonPath().get("per_page"), notNullValue());
        assertThat("from (" + tid + ")", response.jsonPath().getInt("from"), is(1));
        assertThat("to (" + tid + ")", response.jsonPath().get("to"), notNullValue());
    }

    private void assertSubmittedTimesheetValues(Response response, String prefix, String tid) {
        assertThat(prefix + ".timesheet_status.id (" + tid + ")", response.jsonPath().getInt(prefix + ".timesheet_status.id"), is(2));
        assertThat(prefix + ".timesheet_status.label (" + tid + ")", response.jsonPath().getString(prefix + ".timesheet_status.label"), is("Submitted"));
        assertThat(prefix + ".hours.total_regular (" + tid + ")", response.jsonPath().getString(prefix + ".hours.total_regular"),
                is(expectedSubmittedRegularHours));
        assertThat(prefix + ".pay.amount (" + tid + ")", response.jsonPath().getInt(prefix + ".pay.amount"), is(expectedSubmittedPayAmount));
        assertThat(prefix + ".bill.amount (" + tid + ")", response.jsonPath().getInt(prefix + ".bill.amount"), is(expectedSubmittedBillAmount));
    }

    private void assertOpenTimesheetValues(Response response, String prefix, String tid) {
        assertThat(prefix + ".timesheet_status.id (" + tid + ")", response.jsonPath().getInt(prefix + ".timesheet_status.id"), is(1));
        assertThat(prefix + ".timesheet_status.label (" + tid + ")", response.jsonPath().getString(prefix + ".timesheet_status.label"), is("Open"));
        assertThat(prefix + ".hours.total_regular (" + tid + ")", response.jsonPath().getString(prefix + ".hours.total_regular"), is("0h 0min"));
        assertThat(prefix + ".hours.total_overtime (" + tid + ")", response.jsonPath().getString(prefix + ".hours.total_overtime"), is("0h 0min"));
        assertThat(prefix + ".hours.total (" + tid + ")", response.jsonPath().getString(prefix + ".hours.total"), is("0h 0min"));
        assertThat(prefix + ".pay.amount (" + tid + ")", response.jsonPath().getInt(prefix + ".pay.amount"), is(0));
        assertThat(prefix + ".bill.amount (" + tid + ")", response.jsonPath().getInt(prefix + ".bill.amount"), is(0));
    }

    private void assertEmptyListResponse(Response response, String tid) {
        assertThat("Status 200 (" + tid + ")", response.getStatusCode(), is(200));
        assertThat("current_page (" + tid + ")", response.jsonPath().getInt("current_page"), is(1));
        List<Map<String, Object>> data = response.jsonPath().getList("data");
        assertThat("data should be empty (" + tid + ")", data.size(), is(0));
        assertThat("from is null (" + tid + ")", response.jsonPath().get("from"), nullValue());
        assertThat("to is null (" + tid + ")", response.jsonPath().get("to"), nullValue());
        assertThat("next_page_url is null (" + tid + ")", response.jsonPath().get("next_page_url"), nullValue());
    }

    // ========================== Tests ==========================

    @Test(priority = 1)
    public void getTimesheetsListWithAllCorrectParamsTest() {
        Map<String, String> params = buildAllQueryParams();
        Response response = fetchTimesheetsList(params);

        assertThat("Status should be 200", response.getStatusCode(), is(200));
        List<Map<String, Object>> data = response.jsonPath().getList("data");
        assertThat("data should not be null", data, notNullValue());
        assertThat("data length", data.size(), is(expectedTotalTimesheets));

        assertPaginationStructure(response, "AllParams");
        assertThat("per_page", response.jsonPath().getInt("per_page"), is(100));
        assertThat("next_page_url should be null", response.jsonPath().get("next_page_url"), nullValue());
        assertThat("prev_page_url should be null", response.jsonPath().get("prev_page_url"), nullValue());

        assertFullListSubmittedOpenPattern(response, data, "AllParams");
    }

    @Test(dataProvider = "candidateSlugDataProvider", priority = 2)
    public void getTimesheetsListByCandidateSlugTest(String testCase, String candidateSlugParam,
            int expectedStatus, boolean expectData) {
        Response response = fetchTimesheetsList(singleParam("candidate_slug", candidateSlugParam));
        assertThat(testCase + " - status", response.getStatusCode(), is(expectedStatus));

        if (expectData) {
            List<Map<String, Object>> data = response.jsonPath().getList("data");
            assertThat(testCase + " - data length", data.size(), is(expectedTotalTimesheets));
            assertPaginationStructure(response, testCase);
            assertFullListSubmittedOpenPattern(response, data, testCase);
        } else if (expectedStatus == 200 && INCORRECT_SLUG.equals(candidateSlugParam)) {
            assertEmptyListResponse(response, testCase);
        }
    }

    @Test(dataProvider = "companySlugDataProvider", priority = 3)
    public void getTimesheetsListByCompanySlugTest(String testCase, String companySlugParam,
            int expectedStatus, boolean expectData) {
        Response response = fetchTimesheetsList(singleParam("company_slug", companySlugParam));
        assertThat(testCase + " - status", response.getStatusCode(), is(expectedStatus));

        if (expectData) {
            List<Map<String, Object>> data = response.jsonPath().getList("data");
            assertThat(testCase + " - data length", data.size(), is(expectedTotalTimesheets));
            assertPaginationStructure(response, testCase);
            assertTimesheetItemStructure(response, "data[0]", testCase);
        } else if (expectedStatus == 200 && INCORRECT_SLUG.equals(companySlugParam)) {
            assertEmptyListResponse(response, testCase);
        }
    }

    @Test(dataProvider = "jobSlugDataProvider", priority = 4)
    public void getTimesheetsListByJobSlugTest(String testCase, String jobSlugParam,
            int expectedStatus, boolean expectData) {
        Response response = fetchTimesheetsList(singleParam("job_slug", jobSlugParam));
        assertThat(testCase + " - status", response.getStatusCode(), is(expectedStatus));

        if (expectData) {
            List<Map<String, Object>> data = response.jsonPath().getList("data");
            assertThat(testCase + " - data length", data.size(), is(expectedTotalTimesheets));
            assertPaginationStructure(response, testCase);
            assertTimesheetItemStructure(response, "data[0]", testCase);
        } else if (expectedStatus == 200 && INCORRECT_SLUG.equals(jobSlugParam)) {
            assertEmptyListResponse(response, testCase);
        }
    }

    @Test(dataProvider = "timesheetStatusDataProvider", priority = 5)
    public void getTimesheetsListByTimesheetStatusTest(String testCase, String statusParam,
            int expectedStatus, int expectedDataSize) {
        Response response = fetchTimesheetsList(singleParam("timesheet_status", statusParam));
        assertThat(testCase + " - status", response.getStatusCode(), is(expectedStatus));

        if (expectedStatus == 200) {
            List<Map<String, Object>> data = response.jsonPath().getList("data");
            assertThat(testCase + " - data should not be null", data, notNullValue());
            if (expectedDataSize > 0) {
                assertThat(testCase + " - data length", data.size(), is(expectedDataSize));
            }
            if ("submitted".equals(statusParam)) {
                assertThat(testCase + " - submitted count", data.size(), is(1));
                int submittedIdx = findFirstIndexByStatusId(response, data, 2);
                assertSubmittedTimesheetValues(response, "data[" + submittedIdx + "]", testCase);
            }
            if ("open".equals(statusParam)) {
                assertThat(testCase + " - open count", data.size(), is(expectedTotalTimesheets - 1));
                for (int i = 0; i < data.size(); i++) {
                    assertOpenTimesheetValues(response, "data[" + i + "]", testCase + "[" + i + "]");
                }
            }
        }
    }

    private int findFirstIndexByStatusId(Response response, List<Map<String, Object>> data, int statusId) {
        for (int i = 0; i < data.size(); i++) {
            if (response.jsonPath().getInt("data[" + i + "].timesheet_status.id") == statusId) {
                return i;
            }
        }
        return 0;
    }

    @Test(dataProvider = "paginationDataProvider", priority = 6)
    public void getTimesheetsListByPaginationTest(String testCase, String page, String limit,
            int expectedStatus) {
        Map<String, String> params = new HashMap<>();
        params.put("page", page);
        params.put("limit", limit);

        Response response = fetchTimesheetsList(params);
        assertThat(testCase + " - status", response.getStatusCode(), is(expectedStatus));

        if (expectedStatus == 200) {
            assertThat(testCase + " - current_page", response.jsonPath().getInt("current_page"), is(Integer.parseInt(page)));
            List<Map<String, Object>> data = response.jsonPath().getList("data");
            assertThat(testCase + " - data not null", data, notNullValue());

            if ("1".equals(page) && "1".equals(limit)) {
                assertThat(testCase + " - per_page", response.jsonPath().getInt("per_page"), is(1));
                assertThat(testCase + " - data size", data.size(), is(1));
                assertThat(testCase + " - next_page_url", response.jsonPath().getString("next_page_url"), notNullValue());
            }
            if ("999".equals(page)) {
                assertThat(testCase + " - empty data", data.size(), is(0));
                assertThat(testCase + " - from is null", response.jsonPath().get("from"), nullValue());
                assertThat(testCase + " - prev_page_url", response.jsonPath().getString("prev_page_url"), notNullValue());
            }
        }
        if (expectedStatus == 422) {
            String body = response.getBody().asString();
            assertThat(testCase + " - error body not empty", body, notNullValue());
            assertThat(testCase + " - error body has content", body.length(), greaterThan(0));
        }
    }

    @Test(dataProvider = "sortDataProvider", priority = 7)
    public void getTimesheetsListBySortParamsTest(String testCase, String sortBy, String sortOrder,
            int expectedStatus) {
        Map<String, String> params = new HashMap<>();
        params.put("sort_by", sortBy);
        params.put("sort_order", sortOrder);

        Response response = fetchTimesheetsList(params);
        assertThat(testCase + " - status", response.getStatusCode(), is(expectedStatus));

        if (expectedStatus == 200) {
            List<Map<String, Object>> data = response.jsonPath().getList("data");
            assertThat(testCase + " - data not null", data, notNullValue());
            assertThat(testCase + " - data length", data.size(), is(expectedTotalTimesheets));
        }
        if (expectedStatus == 422) {
            String body = response.getBody().asString();
            assertThat(testCase + " - validation error present", body, notNullValue());
        }
    }

    @Test(dataProvider = "timeLogsDataProvider", priority = 8)
    public void getTimesheetsListByTimeLogsParamTest(String testCase, String timeLogsParam,
            int expectedStatus) {
        Response response = fetchTimesheetsList(singleParam("time_logs", timeLogsParam));
        assertThat(testCase + " - status", response.getStatusCode(), is(expectedStatus));

        if (expectedStatus == 200) {
            List<Map<String, Object>> data = response.jsonPath().getList("data");
            assertThat(testCase + " - data not null", data, notNullValue());
            assertThat(testCase + " - data length", data.size(), is(expectedTotalTimesheets));

            if ("1".equals(timeLogsParam)) {
                assertThat(testCase + " - time_logs present on data[0]",
                        response.jsonPath().getList("data[0].time_logs"), notNullValue());
                assertThat(testCase + " - time_logs size",
                        response.jsonPath().getList("data[0].time_logs").size(), greaterThanOrEqualTo(6));

                Map<String, Object> firstTimeLog = response.jsonPath().getMap("data[0].time_logs[0]");
                assertThat(testCase + " - time_log.id", firstTimeLog.get("id"), notNullValue());
                assertThat(testCase + " - time_log.date", firstTimeLog.get("date"), notNullValue());
                assertThat(testCase + " - time_log.day", firstTimeLog.get("day"), notNullValue());
                assertThat(testCase + " - time_log.daily_hours", firstTimeLog.get("daily_hours"), notNullValue());

                // PAY-748: overtime_details is deliberately NOT included on the list endpoint's
                // time_logs=true variant (product-confirmed, Analysis Q7) — Details endpoint only.
                assertThat(testCase + " - daily_hours.overtime_details absent on list endpoint",
                        response.jsonPath().get("data[0].time_logs[0].daily_hours.overtime_details"), nullValue());
            }
            if ("0".equals(timeLogsParam)) {
                assertThat(testCase + " - time_logs absent on data[0]",
                        response.jsonPath().get("data[0].time_logs"), nullValue());
            }
        }
        if (expectedStatus == 422) {
            String errorMsg = response.jsonPath().getString("time_logs[0]");
            assertThat(testCase + " - validation message", errorMsg, is("The time logs must be one of: true, false, 1, 0."));
        }
    }

    @Test(dataProvider = "dateRangeDataProvider", priority = 9)
    public void getTimesheetsListByDateRangeTest(String testCase, String startDate, String endDate,
            int expectedStatus, boolean expectData) {
        Map<String, String> params = new HashMap<>();
        params.put("timesheet_start_date", startDate);
        params.put("timesheet_end_date", endDate);

        Response response = fetchTimesheetsList(params);
        assertThat(testCase + " - status", response.getStatusCode(), is(expectedStatus));

        if (expectedStatus == 200 && !expectData) {
            List<Map<String, Object>> data = response.jsonPath().getList("data");
            if (data != null) {
                assertThat(testCase + " - should return empty data for out-of-range dates",
                        data.size(), is(0));
            }
        }
        if (expectedStatus == 200 && expectData) {
            List<Map<String, Object>> data = response.jsonPath().getList("data");
            assertThat(testCase + " - data not null", data, notNullValue());
            assertThat(testCase + " - data has items", data.size(), greaterThan(0));
        }
    }

    // ========================== Data Providers ==========================

    @DataProvider(name = "candidateSlugDataProvider")
    public Object[][] candidateSlugDataProvider() {
        return new Object[][] {
                { "Correct candidate_slug", candidateSlug, 200, true },
                { "Incorrect candidate_slug", INCORRECT_SLUG, 200, false },
                { "Empty candidate_slug", "", 200, false },
        };
    }

    @DataProvider(name = "companySlugDataProvider")
    public Object[][] companySlugDataProvider() {
        return new Object[][] {
                { "Correct company_slug", companySlug, 200, true },
                { "Incorrect company_slug", INCORRECT_SLUG, 200, false },
                { "Empty company_slug", "", 200, false },
        };
    }

    @DataProvider(name = "jobSlugDataProvider")
    public Object[][] jobSlugDataProvider() {
        return new Object[][] {
                { "Correct job_slug", jobSlug, 200, true },
                { "Incorrect job_slug", INCORRECT_SLUG, 200, false },
                { "Empty job_slug", "", 200, false },
        };
    }

    @DataProvider(name = "timesheetStatusDataProvider")
    public Object[][] timesheetStatusDataProvider() {
        return new Object[][] {
                { "Status open", "open", 200, expectedTotalTimesheets - 1 },
                { "Status submitted", "submitted", 200, 1 },
                { "Status approved", "approved", 200, 0 },
                { "Status rejected", "rejected", 200, 0 },
                { "Invalid status", "invalidstatus", 422, 0 },
                { "Empty status", "", 200, expectedTotalTimesheets },
        };
    }

    @DataProvider(name = "paginationDataProvider")
    public Object[][] paginationDataProvider() {
        return new Object[][] {
                { "Page 1, Limit 100", "1", "100", 200 },
                { "Page 1, Limit 1", "1", "1", 200 },
                { "Page 999, Limit 100 (out of range)", "999", "100", 200 },
                { "Page 0, Limit 100", "0", "100", 422 },
                { "Page 1, Limit 0", "1", "0", 422 },
                { "Negative page", "-1", "100", 422 },
                { "Negative limit", "1", "-1", 422 },
        };
    }

    @DataProvider(name = "sortDataProvider")
    public Object[][] sortDataProvider() {
        return new Object[][] {
                { "Sort by updatedon asc", "updatedon", "asc", 200 },
                { "Sort by updatedon desc", "updatedon", "desc", 200 },
                { "Sort by createdon asc", "createdon", "asc", 200 },
                { "Sort by createdon desc", "createdon", "desc", 200 },
                { "Invalid sort_by", "invalidfield", "asc", 422 },
                { "Invalid sort_order", "updatedon", "invalidorder", 422 },
        };
    }

    @DataProvider(name = "timeLogsDataProvider")
    public Object[][] timeLogsDataProvider() {
        return new Object[][] {
                { "time_logs=1 (include)", "1", 200 },
                { "time_logs=0 (exclude)", "0", 200 },
                { "time_logs=invalid", "abc", 422 },
        };
    }

    @DataProvider(name = "dateRangeDataProvider",parallel = true)
    public Object[][] dateRangeDataProvider() {
        return new Object[][] {
                { "Valid date range", "2025-09-22", "2025-09-28", 200, true },
                { "Start date after end date", timesheetEndDate, timesheetStartDate, 200, false },
                { "Past date range (no data)", "2020-01-01", "2020-01-31", 200, false },
                { "Future date range (no data)", "2030-01-01", "2030-12-31", 200, false },
                { "Empty start date", "", timesheetEndDate, 200, true },
                { "Empty end date", timesheetStartDate, "", 200, true },
        };
    }
}
