package io.recruitcrm.contractStaffing.shiftBasedTimesheets;

import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.contractStaffing.TimesheetDate;
import io.rcrm.api.pojo.albatross.contractStaffing.TimeLog;
import io.rcrm.api.pojo.albatross.contractStaffing.SubmitTimeLogsRequest;
import io.rcrm.api.pojo.albatross.contractStaffing.ApproveTimesheetRequest;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import org.hamcrest.Matcher;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class SubmitTimeSheetTest extends ContractStaffingBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;
    private int ownerAccountID;
    private commanFunction function;
    private int timesheetIDGlobal;
    private int timesheetIDGlobal2;
    private int timesheetIDGlobal1;

    @BeforeClass(alwaysRun = true)
    public void Setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        apiAuthToken = ThreadManager.getAccountApiKey();
        function = new commanFunction();
        createRuleEngineTemplate(albatrossAuthToken);
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void getTimeLogOfTimeSheetTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
        enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

        Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

        Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates,
                albatrossAuthToken);

        Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId, 1, 100, albatrossAuthToken);

        assertThat(getAllTimesheetsResponse.statusCode(), is(200));
        JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse.jsonPath();
        int timesheetID = ((Number) getAllTimesheetsJsonPath.get("data[0].id")).intValue();

        Response timeLogsResponse = getTimeSheetTimeLogs(timesheetID, albatrossAuthToken);

        assertThat(timeLogsResponse.statusCode(), is(200));

        JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
        assertThat(timeLogsJsonPath.getInt("meta.status"), is(200));
        assertThat(timeLogsJsonPath.getString("meta.message"), is("Time logs fetched successfully"));
        assertThat(timeLogsJsonPath.getString("meta.responseType.context"), is("Request is successful"));
        assertThat(timeLogsJsonPath.getInt("meta.responseType.code"), is(103));

        assertThat(timeLogsJsonPath.getInt("data.timesheetId"), is(timesheetID));
        assertThat(timeLogsJsonPath.getInt("data.workLogType"), is(2));
        assertThat(timeLogsJsonPath.getInt("data.timesheetFrequency"), is(timesheetFrequency));
        assertThat(timeLogsJsonPath.getBoolean("data.calculateBreakTime"), is(false));
        assertThat(timeLogsJsonPath.getInt("data.approvalStatusId"), is(1));

        assertThat(timeLogsJsonPath.get("data.timeLogs"), notNullValue());
        List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");
        assertThat(timeLogs.size(), greaterThan(0));

        validateTimeLogsBasedOnFrequency(timesheetFrequency, timeLogs, timesheetID);

        String expectedTimesheetPeriod = String.valueOf(timeLogs.get(0).get("timesheetPeriod"));
        assertThat(expectedTimesheetPeriod, notNullValue());
        assertThat(expectedTimesheetPeriod.contains(" - "), is(true));

        assertThat(timeLogsJsonPath.get("data.templateWorkDays"), notNullValue());
        List<Map<String, Object>> templateWorkDays = timeLogsJsonPath.getList("data.templateWorkDays");
        assertThat(templateWorkDays.size(), greaterThan(0));

        int expectedTemplateWorkDays = 6;
        assertThat(templateWorkDays.size(), is(expectedTemplateWorkDays));

        for (Map<String, Object> workDay : templateWorkDays) {
            assertThat(workDay.get("workDayId"), notNullValue());
            assertThat(((Number) workDay.get("workTime")).intValue(), is(0));
            assertThat(((Number) workDay.get("workStartTime")).intValue(), is(32400));
            assertThat(((Number) workDay.get("workEndTime")).intValue(), is(61200));
        }

        assertThat(timeLogsJsonPath.get("data.approvers"), notNullValue());
        assertThat(timeLogsJsonPath.get("data.approvers.agencyIds"), notNullValue());
        assertThat(timeLogsJsonPath.get("data.approvers.clientIds"), notNullValue());

        List<Integer> agencyIds = timeLogsJsonPath.getList("data.approvers.agencyIds");
        List<Integer> clientIds = timeLogsJsonPath.getList("data.approvers.clientIds");

        assertThat(agencyIds.size(), greaterThan(0));
        assertThat(agencyIds.size(), is(1));
        assertThat(agencyIds.get(0).intValue(), is(userId));

        assertThat(clientIds, notNullValue());

        timeLogsResponse.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/contractStaffing/GetTimeSheetTimeLogs.json"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void getTimeLogOfTimeSheetWithInvalidTimesheetIdTest() {
        int invalidTimesheetId = 826482895;

        Response timeLogsResponse = getTimeSheetTimeLogs(invalidTimesheetId, albatrossAuthToken);

        assertThat(timeLogsResponse.statusCode(), is(404));

        JsonPath jsonPath = timeLogsResponse.jsonPath();
        assertThat(jsonPath.getInt("meta.status"), is(404));
        assertThat(jsonPath.getString("meta.responseType.context"), is("Error while processing request"));
        assertThat(jsonPath.getInt("meta.responseType.code"), is(101));
        assertThat(jsonPath.get("data"), nullValue());

        List<Map<String, Object>> errors = jsonPath.getList("errors");
        assertThat(errors.size(), greaterThan(0));
        String message = String.valueOf(errors.get(0).get("message"));
        Assert.assertTrue(message.contains("Timesheet id " + invalidTimesheetId + " not found."));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void unauthorizedUserCannotGetTimeLogOfTimeSheetTest() {
        // sample data (these are placeholders)
        int validTimesheetId = 825; // Should exist in system for meaningful test

        String invalidAuthToken = albatrossAuthToken + "invalid";

        Response timeLogsResponse = getTimeSheetTimeLogs(validTimesheetId, invalidAuthToken);

        assertThat(timeLogsResponse.statusCode(), is(401));

        JsonPath jsonPath = timeLogsResponse.jsonPath();
        assertThat(jsonPath.getInt("meta.status"), is(401));
        assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
        assertThat(jsonPath.getString("meta.responseType.context"), is("Warning"));
        assertThat(jsonPath.get("data"), notNullValue());
        assertThat(jsonPath.get("errors"), notNullValue());
    }

    private boolean responseHasTimesheetDataWithId(Response resp) {
        if (resp.statusCode() != 200) {
            return false;
        }
        JsonPath jp = resp.jsonPath();
        List<Map<String, Object>> data = jp.getList("data");
        if (data == null || data.isEmpty()) {
            return false;
        }
        return jp.get("data[0].id") != null;
    }


    private Object[] buildSubmitTimeLogMultiContractorRow(int timesheetFrequency, int numberOfWorkEntriesPerLog,
            int numberOfBreaksPerSegment) {
        Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId1 = ((Number) testData[1]).intValue();
        int candidateId2 = ((Number) testData[2]).intValue();
        int userId = ((Number) testData[4]).intValue();
        return new Object[] {
                jobId,
                candidateId1,
                candidateId2,
                userId,
                timesheetFrequency,
                numberOfWorkEntriesPerLog,
                numberOfBreaksPerSegment,
        };
    }

    private void validateTimeLogsBasedOnFrequency(int timesheetFrequency, List<Map<String, Object>> timeLogs,
            int timesheetID) {

        switch (timesheetFrequency) {
            case 2: // Weekly
                assertThat(timeLogs.size(), is(7));
                validateWeeklyTimeLogs(timeLogs, timesheetID);
                break;
            case 3: // BiWeekly
                // Bi-weekly timeLogs representation assumed to be 14 entries or 7 depending on
                // API.
                // Keeping original intent (7) but you may adjust if API returns 14.
                assertThat(timeLogs.size(), is(7));
                validateBiWeeklyTimeLogs(timeLogs, timesheetID);
                break;
            case 4: // Monthly
                assertThat(timeLogs.size(), greaterThanOrEqualTo(7));
                assertThat(timeLogs.size(), lessThanOrEqualTo(31));
                validateMonthlyTimeLogs(timeLogs, timesheetID);
                break;
            default:
                throw new AssertionError("Unknown timesheet frequency: " + timesheetFrequency);
        }
    }

    private void validateWeeklyTimeLogs(List<Map<String, Object>> timeLogs, int timesheetID) {
        String expectedPeriod = String.valueOf(timeLogs.get(0).get("timesheetPeriod"));
        for (Map<String, Object> timeLog : timeLogs) {
            validateTimeLogStructure(timeLog, timesheetID);
            assertThat(String.valueOf(timeLog.get("timesheetPeriod")), is(expectedPeriod));
        }
    }

    private void validateBiWeeklyTimeLogs(List<Map<String, Object>> timeLogs, int timesheetID) {
        String expectedPeriod = String.valueOf(timeLogs.get(0).get("timesheetPeriod"));
        for (Map<String, Object> timeLog : timeLogs) {
            validateTimeLogStructure(timeLog, timesheetID);
            assertThat(String.valueOf(timeLog.get("timesheetPeriod")), is(expectedPeriod));
        }
    }

    private void validateMonthlyTimeLogs(List<Map<String, Object>> timeLogs, int timesheetID) {
        String expectedPeriod = String.valueOf(timeLogs.get(0).get("timesheetPeriod"));
        for (Map<String, Object> timeLog : timeLogs) {
            validateTimeLogStructure(timeLog, timesheetID);
            assertThat(String.valueOf(timeLog.get("timesheetPeriod")), is(expectedPeriod));
        }
    }

    private void validateTimeLogStructure(Map<String, Object> timeLog, int timesheetID) {
        assertThat(timeLog.get("id"), notNullValue());
        assertThat(timeLog.get("timesheetId"), notNullValue());
        Assert.assertNotNull(timeLog.get("timesheetPeriod"), "Time log timesheetPeriod should not be null");
        assertThat(timeLog.get("date"), notNullValue());
        // breakIntervals can be null if no break intervals exist for the time log
        Object breakIntervalsObj = timeLog.get("breakIntervals");
        assertThat(breakIntervalsObj, instanceOf(List.class));

        int actualTimesheetId = ((Number) timeLog.get("timesheetId")).intValue();
        assertThat(actualTimesheetId, is(timesheetID));

        Object payDataObj = timeLog.get("payData");
        Object billDataObj = timeLog.get("billData");

        // Handle null values by treating them as 0.0 (initial state for unsaved time
        // logs)
        double payDataValue = (payDataObj == null) ? 0.0 : convertToDouble(payDataObj, "payData");
        double billDataValue = (billDataObj == null) ? 0.0 : convertToDouble(billDataObj, "billData");

        // Expecting near-zero values; use TestNG Assert with delta
        Assert.assertEquals(payDataValue, 0.0, 0.001, "payData should be approximately 0.0");
        Assert.assertEquals(billDataValue, 0.0, 0.001, "billData should be approximately 0.0");

        Object dayTypeIdObj = timeLog.get("dayTypeId");
        if (dayTypeIdObj != null) {
            int dayTypeId = ((Number) dayTypeIdObj).intValue();
            Assert.assertTrue(dayTypeId == 1 || dayTypeId == 2,
                    "DayTypeId should be 1 (working day) or 2 (weekend/holiday), but was: " + dayTypeId);
        }

        assertThat(timeLog.get("workTime"), nullValue());
        assertThat(timeLog.get("workStartTime"), nullValue());
        assertThat(timeLog.get("workEndTime"), nullValue());
        assertThat(timeLog.get("breakTime"), nullValue());
        assertThat(timeLog.get("overTime"), nullValue());
        assertThat(timeLog.get("remark"), nullValue());
        assertThat(timeLog.get("totalTime"), nullValue());
    }

    private double convertToDouble(Object obj, String fieldName) {
        if (obj == null) {
            throw new AssertionError(fieldName + " is null but was expected to be a numeric value");
        }

        if (obj instanceof Float) {
            return ((Float) obj).doubleValue();
        } else if (obj instanceof Double) {
            return (Double) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).doubleValue();
        } else if (obj instanceof Long) {
            return ((Long) obj).doubleValue();
        } else if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else {
            throw new AssertionError(fieldName + " has unexpected type: " + obj.getClass().getSimpleName()
                    + ", value: " + obj);
        }
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void submitTimeLogForTimesheetTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
        enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0 );

        Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

        Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates,
                albatrossAuthToken);

        Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId, 1, 100, albatrossAuthToken);

        assertThat(getAllTimesheetsResponse.statusCode(), is(200));
        JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse.jsonPath();
        timesheetIDGlobal = ((Number) getAllTimesheetsJsonPath.get("data[0].id")).intValue();
        Response timeLogsResponse = getTimeSheetTimeLogs(timesheetIDGlobal, albatrossAuthToken);
        JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
        List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");

        List<TimeLog> timeLogsList = generateTimelogIDLists(timeLogs, timesheetIDGlobal);

        SubmitTimeLogsRequest submitRequest = new SubmitTimeLogsRequest();
        submitRequest.setTimeLogs(timeLogsList);

        Response submitResponse = submitTimeLogsForTimesheet(submitRequest, albatrossAuthToken);

        assertThat(submitResponse.statusCode(), is(200));

        JsonPath submitJsonPath = submitResponse.jsonPath();
        assertThat(submitJsonPath.getInt("meta.status"), is(200));
        assertThat(submitJsonPath.getString("meta.message"), is("Time logs bulk updated successfully"));
        assertThat(submitJsonPath.getString("meta.responseType.context"), is("Request is successful"));
        submitResponse.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/contractStaffing/SubmitTimeLogsForTimesheet.json"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void submitTimeLogForTimesheetWithInvalidTimesheetIdTest() {
        Faker faker = new Faker();
        int randNumber = faker.number().numberBetween(100000, 999999);

        TimeLog timeLog = new TimeLog();
        timeLog.setId(randNumber);
        timeLog.setWorkTimeDetails(buildDefaultBulkWorkTimeDetails());
        timeLog.setTimesheetId(999999); // Invalid timesheet ID
        timeLog.setTimesheetPeriod("Jul 14, 2025 - Jul 20, 2025");
        timeLog.setOverTime(3600);
        timeLog.setTotalTime(30600); // Invalid time log ID

        List<TimeLog> timeLogsList = Arrays.asList(timeLog);

        SubmitTimeLogsRequest submitRequest = new SubmitTimeLogsRequest();
        submitRequest.setTimeDetails(generateTimeDetailsFromTimeLogs(timeLogsList));
        submitRequest.setTimeLogs(timeLogsList);

        Response submitResponse = submitTimeLogsForTimesheet(submitRequest, albatrossAuthToken);

        assertThat(submitResponse.statusCode(), is(404));

        System.out.println(submitResponse.jsonPath().prettyPrint());
        JsonPath jsonPath = submitResponse.jsonPath();
        List<Map<String, Object>> errors = jsonPath.getList("errors");
        assertThat(errors.size(), greaterThan(0));
        String errorMessage = String.valueOf(errors.get(0).get("message"));
        Assert.assertTrue(errorMessage.contains("TimeLog id "+  randNumber +" not found."));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void unauthorizedUserCannotSubmitTimeLogTest() {
        TimeLog timeLog = new TimeLog();
        timeLog.setId(123);
        timeLog.setWorkTimeDetails(buildDefaultBulkWorkTimeDetails());
        timeLog.setTimesheetPeriod("Jul 14, 2025 - Jul 20, 2025");
        timeLog.setOverTime(3600);
        timeLog.setTotalTime(30600); // Invalid time log ID
        timeLog.setTimesheetId(456);

        List<TimeLog> timeLogsList = Arrays.asList(timeLog);

        SubmitTimeLogsRequest submitRequest = new SubmitTimeLogsRequest();
        submitRequest.setTimeDetails(generateTimeDetailsFromTimeLogs(timeLogsList));
        submitRequest.setTimeLogs(timeLogsList);

        String invalidAuthToken = albatrossAuthToken + "invalid";
        Response submitResponse = submitTimeLogsForTimesheet(submitRequest, invalidAuthToken);

        assertThat(submitResponse.statusCode(), is(401));

        JsonPath jsonPath = submitResponse.jsonPath();
        assertThat(jsonPath.getInt("meta.status"), is(401));
        assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
        assertThat(jsonPath.getString("meta.responseType.context"), is("Warning"));
        assertThat(jsonPath.getInt("meta.responseType.code"), is(104));
        assertThat(jsonPath.getString("data"), is("Invalid token"));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void approveTimeSheetTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
        enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

        Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

        Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates,
                albatrossAuthToken);

        Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId, 1, 100, albatrossAuthToken);

        assertThat(getAllTimesheetsResponse.statusCode(), is(200));
        JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse.jsonPath();
        int timesheetID = ((Number) getAllTimesheetsJsonPath.get("data[0].id")).intValue();

        Response timeLogsResponse = getTimeSheetTimeLogs(timesheetID, albatrossAuthToken);
        assertThat(timeLogsResponse.statusCode(), is(200));

        JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
        List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");

        List<TimeLog> timeLogsList = generateTimelogIDLists(timeLogs, timesheetID);

        SubmitTimeLogsRequest submitRequest = new SubmitTimeLogsRequest();
        submitRequest.setTimeLogs(timeLogsList);
        submitRequest.setTimeDetails(generateTimeDetailsFromTimeLogs(timeLogsList));

        Response submitResponse = submitTimeLogsForTimesheet(submitRequest, albatrossAuthToken);
        assertThat(submitResponse.statusCode(), is(200));

        int approvalStatus = ThreadLocalRandom.current().nextInt(3, 5);
        ApproveTimesheetRequest approveRequest = buildApproveTimesheetRequest(approvalStatus,
                "Rejected by automated test");

        Response approveResponse = approveTimesheet(timesheetID, approveRequest, albatrossAuthToken);

        assertThat(approveResponse.statusCode(), is(201));

        JsonPath approveJsonPath = approveResponse.jsonPath();
        assertThat(approveJsonPath.getInt("meta.status"), is(200));
        assertThat(approveJsonPath.getString("meta.message"), is("Timesheet status updated successfully"));
        assertThat(approveJsonPath.getString("meta.responseType.context"), is("Request is successful"));
        Assert.assertNull(approveJsonPath.get("data"), "Response data should be null for successful approval");
        approveResponse.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/contractStaffing/ApproveTimesheet.json"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void approveTimeSheetWithInvalidApprovalStatusTest() {
        int timesheetId = 1146;

        ApproveTimesheetRequest approveRequest = new ApproveTimesheetRequest();
        approveRequest.setApprovalStatus(999);

        Response approveResponse = approveTimesheet(timesheetId, approveRequest, albatrossAuthToken);
        assertThat(approveResponse.statusCode(), is(400));
        JsonPath jsonPath = approveResponse.jsonPath();

        List<Map<String, Object>> errors = jsonPath.getList("errors");
        assertThat(errors.size(), greaterThan(0));
        String errorMessage = String.valueOf(errors.get(0).get("message"));
        Assert.assertTrue(
                errorMessage.contains("approvalStatus can be only 3 for rejected and 4 for approve timesheet"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void unauthorizedUserCannotApproveTimeSheetTest() {
        int timesheetId = 1146;

        ApproveTimesheetRequest approveRequest = buildApproveTimesheetRequest(3, "Unauthorized test");

        String invalidAuthToken = albatrossAuthToken + "invalid";
        Response approveResponse = approveTimesheet(timesheetId, approveRequest, invalidAuthToken);

        assertThat(approveResponse.statusCode(), is(401));

        JsonPath jsonPath = approveResponse.jsonPath();
        assertThat(jsonPath.getInt("meta.status"), is(401));
        assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
        assertThat(jsonPath.getString("meta.responseType.context"), is("Warning"));
    }

    /**
     * Shift timesheet through create + submit so status history includes draft, submitted, and a reject/approve step.
     */
    private int createSubmittedShiftTimesheetForStatusHistory(int jobId, int candidateId, int userId,
            int timesheetFrequency) {
        enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

        Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

        Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates,
                albatrossAuthToken);
        assertThat(addTimesheetResponse.statusCode(), is(200));

        Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId, 1, 100, albatrossAuthToken);

        assertThat(getAllTimesheetsResponse.statusCode(), is(200));
        JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse.jsonPath();
        int timesheetID = ((Number) getAllTimesheetsJsonPath.get("data[0].id")).intValue();

        Response submittedTimeLogsResponse = getTimeSheetTimeLogs(timesheetID, albatrossAuthToken);
        assertThat(submittedTimeLogsResponse.statusCode(), is(200));
        JsonPath submittedTimeLogsJsonPath = submittedTimeLogsResponse.jsonPath();
        List<Map<String, Object>> submittedTimeLogs = submittedTimeLogsJsonPath.getList("data.timeLogs");
        List<TimeLog> submittedTimeLogsList = generateTimelogIDLists(submittedTimeLogs, timesheetID);
        SubmitTimeLogsRequest submitRequest = new SubmitTimeLogsRequest();
        submitRequest.setTimeLogs(submittedTimeLogsList);
        Response submitResponse = submitTimeLogsForTimesheet(submitRequest, albatrossAuthToken);
        assertThat(submitResponse.statusCode(), is(200));

        return timesheetID;
    }

    private void assertShiftStatusHistoryAfterDecision(int timesheetID, int expectedLatestStatus,
            Matcher<Object> latestRemarkMatcher) {
        Response statusHistoryResponse = getTimeSheetStatusHistory(timesheetID, albatrossAuthToken);
        assertThat(statusHistoryResponse.statusCode(), is(200));

        JsonPath statusHistoryJsonPath = statusHistoryResponse.jsonPath();
        assertThat(statusHistoryJsonPath.getInt("meta.status"), is(200));
        Assert.assertEquals(statusHistoryJsonPath.getString("meta.message"),
                "Timesheet status history fetched successfully");
        Assert.assertEquals(statusHistoryJsonPath.getString("meta.responseType.context"),
                "Request is successful");
        assertThat(statusHistoryJsonPath.getInt("meta.responseType.code"), is(103));

        assertThat(statusHistoryJsonPath.getInt("data.timesheetId"), is(timesheetID));
        Assert.assertNotNull(statusHistoryJsonPath.get("data.statusHistory"), "Status history should not be null");

        List<Map<String, Object>> statusHistory = statusHistoryJsonPath.getList("data.statusHistory");
        assertThat(statusHistory.size(), greaterThan(0));

        for (Map<String, Object> historyEntry : statusHistory) {
            assertThat(historyEntry.get("id"), notNullValue());
            assertThat(historyEntry.get("status"), notNullValue());
            Assert.assertNotNull(historyEntry.get("updatedOn"), "History entry updatedOn should not be null");
            Assert.assertNotNull(historyEntry.get("updatedBy"), "History entry updatedBy should not be null");

            Map<String, Object> updatedBy = (Map<String, Object>) historyEntry.get("updatedBy");
            assertThat(updatedBy.get("id"), notNullValue());
            assertThat(updatedBy.get("name"), notNullValue());
            assertThat(updatedBy.get("userTypeId"), notNullValue());
        }

        assertThat(statusHistory.size(), greaterThanOrEqualTo(3));

        Map<String, Object> latestEntry = statusHistory.get(0);
        assertThat(((Number) latestEntry.get("status")).intValue(), is(expectedLatestStatus));
        assertThat(latestEntry.get("remark"), latestRemarkMatcher);
        Map<String, Object> latestUpdatedBy = (Map<String, Object>) latestEntry.get("updatedBy");
        assertThat(latestUpdatedBy.size(), greaterThan(0));
        Assert.assertNotNull(latestUpdatedBy.get("id"), "Latest entry updatedBy id should not be null");
        Assert.assertNotNull(latestUpdatedBy.get("name"), "Latest entry updatedBy name should not be null");
        assertThat(((Number) latestUpdatedBy.get("userTypeId")).intValue(), is(2));

        Map<String, Object> submittedEntry = statusHistory.get(1);
        assertThat(((Number) submittedEntry.get("status")).intValue(), is(2));
        assertThat(submittedEntry.get("remark"), nullValue());
        Map<String, Object> submittedUpdatedBy = (Map<String, Object>) submittedEntry.get("updatedBy");
        assertThat(((Number) submittedUpdatedBy.get("userTypeId")).intValue(), is(2));

        Map<String, Object> createdEntry = statusHistory.get(2);
        assertThat(((Number) createdEntry.get("status")).intValue(), is(1));
        assertThat(createdEntry.get("remark"), nullValue());
        Map<String, Object> createdUpdatedBy = (Map<String, Object>) createdEntry.get("updatedBy");
        assertThat(((Number) createdUpdatedBy.get("userTypeId")).intValue(), is(2));

        for (Map<String, Object> historyEntry : statusHistory) {
            Map<String, Object> updatedBy = (Map<String, Object>) historyEntry.get("updatedBy");
            assertThat(((Number) updatedBy.get("userTypeId")).intValue(), is(2));
            assertThat(updatedBy.get("name"), notNullValue());
            Assert.assertTrue(updatedBy.get("name").toString().contains("Owner"));
            assertThat(updatedBy.get("photo"), nullValue());
        }

        long latestTimestamp = ((Number) statusHistory.get(0).get("updatedOn")).longValue();
        long submittedTimestamp = ((Number) statusHistory.get(1).get("updatedOn")).longValue();
        long createdTimestamp = ((Number) statusHistory.get(2).get("updatedOn")).longValue();

        Assert.assertTrue(latestTimestamp > submittedTimestamp,
                "Latest timestamp should be greater than submitted timestamp");
        Assert.assertTrue(submittedTimestamp > createdTimestamp,
                "Submitted timestamp should be greater than created timestamp");

        statusHistoryResponse.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/contractStaffing/GetTimeSheetStatusHistory.json"));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void getTimeSheetStatusHistoryRejectedTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
        int timesheetID = createSubmittedShiftTimesheetForStatusHistory(jobId, candidateId, userId, timesheetFrequency);

        ApproveTimesheetRequest approveRequest = buildApproveTimesheetRequest(3, "Rejected by automated test");
        Response approveResponse = approveTimesheet(timesheetID, approveRequest, albatrossAuthToken);
        assertThat(approveResponse.statusCode(), is(201));

        assertShiftStatusHistoryAfterDecision(timesheetID, 3,
                hasToString(containsString("Rejected by automated test")));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void getTimeSheetStatusHistoryApprovedTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
        int timesheetID = createSubmittedShiftTimesheetForStatusHistory(jobId, candidateId, userId, timesheetFrequency);

        ApproveTimesheetRequest approveRequest = buildApproveTimesheetRequest(4);
        Response approveResponse = approveTimesheet(timesheetID, approveRequest, albatrossAuthToken);
        assertThat(approveResponse.statusCode(), is(201));

        // Approve payload has no remark; API may return null or a system approval message on the latest entry.
        assertShiftStatusHistoryAfterDecision(timesheetID, 4,
                anyOf(nullValue(), hasToString(containsString("Approved"))));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void getTimeSheetStatusHistoryWithInvalidTimesheetIdTest() {
        int invalidTimesheetId = 14340;

        Response statusHistoryResponse = getTimeSheetStatusHistory(invalidTimesheetId, albatrossAuthToken);

        assertThat(statusHistoryResponse.statusCode(), is(404));

        JsonPath jsonPath = statusHistoryResponse.jsonPath();
        assertThat(jsonPath.getInt("meta.status"), is(404));
        assertThat(jsonPath.get("meta.message"), nullValue());
        assertThat(jsonPath.getString("meta.responseType.context"), is("Error while processing request"));
        assertThat(jsonPath.get("data"), nullValue());
        List<Map<String, Object>> errors = jsonPath.getList("errors");
        assertThat(errors.size(), greaterThan(0));
        String errorMessage = String.valueOf(errors.get(0).get("message"));
        Assert.assertTrue(errorMessage.contains("Timesheet id " + invalidTimesheetId + " not found"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void unauthorizedUserCannotGetTimeSheetStatusHistoryTest() {
        int timesheetId = 1240;

        String invalidAuthToken = albatrossAuthToken + "invalid";
        Response statusHistoryResponse = getTimeSheetStatusHistory(timesheetId, invalidAuthToken);

        assertThat(statusHistoryResponse.statusCode(), is(401));

        JsonPath jsonPath = statusHistoryResponse.jsonPath();
        assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
        assertThat(jsonPath.getString("meta.responseType.context"), is("Warning"));
        assertThat(jsonPath.getString("data"), is("Invalid token"));

        List<Object> errors = jsonPath.getList("errors");
        assertThat(errors.isEmpty(), is(true));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "testTimeSheetSettingsDataForMultipleCandidates", groups = {"contract_staffing", "nightly-build"})
    public void submitTimesheetForMultipleContractorsTest(int jobId, int candidateId1, int candidateId2, int userId,
            int timesheetFrequency) {
        enableTimesheet(candidateId1, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);
        enableTimesheet(candidateId2, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

        Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId1, jobId, timesheetFrequency,
                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

        Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId1, candidateId2), timesheetDates,
                albatrossAuthToken);

        Response getAllTimesheetsResponse1 = getAllTimesheets(jobId, candidateId1, 1, 100, albatrossAuthToken);
        Response getAllTimesheetsResponse2 = getAllTimesheets(jobId, candidateId2, 1, 100, albatrossAuthToken);

        assertThat(getAllTimesheetsResponse1.statusCode(), is(200));
        JsonPath getAllTimesheetsJsonPath1 = getAllTimesheetsResponse1.jsonPath();
        assertThat(getAllTimesheetsResponse2.statusCode(), is(200));
        JsonPath getAllTimesheetsJsonPath2 = getAllTimesheetsResponse2.jsonPath();
        timesheetIDGlobal1 = ((Number) getAllTimesheetsJsonPath1.get("data[0].id")).intValue();
        timesheetIDGlobal2 = ((Number) getAllTimesheetsJsonPath2.get("data[0].id")).intValue();
        Response timeLogsResponse1 = getTimeSheetTimeLogs(timesheetIDGlobal1, albatrossAuthToken);
        Response timeLogsResponse2 = getTimeSheetTimeLogs(timesheetIDGlobal2, albatrossAuthToken);
        JsonPath timeLogsJsonPath1 = timeLogsResponse1.jsonPath();
        JsonPath timeLogsJsonPath2 = timeLogsResponse2.jsonPath();
        List<Map<String, Object>> timeLogs1 = timeLogsJsonPath1.getList("data.timeLogs");
        List<Map<String, Object>> timeLogs2 = timeLogsJsonPath2.getList("data.timeLogs");
        List<TimeLog> timeLogsList1 = generateTimelogIDLists(timeLogs1, timesheetIDGlobal1);
        List<TimeLog> timeLogsList2 = generateTimelogIDLists(timeLogs2, timesheetIDGlobal2);
        List<TimeLog> timeLogsList = new ArrayList<>();
        timeLogsList.addAll(timeLogsList1);
        timeLogsList.addAll(timeLogsList2);
        SubmitTimeLogsRequest submitRequest = new SubmitTimeLogsRequest();
        submitRequest.setTimeLogs(timeLogsList);
        Response submitResponse = submitTimeLogsForTimesheet(submitRequest, albatrossAuthToken);

        assertThat(submitResponse.statusCode(), is(200));

        JsonPath submitJsonPath = submitResponse.jsonPath();
        assertThat(submitJsonPath.getInt("meta.status"), is(200));
        assertThat(submitJsonPath.getString("meta.message"), is("Time logs bulk updated successfully"));
        assertThat(submitJsonPath.getString("meta.responseType.context"), is("Request is successful"));
    }

    /**
     * Candidate 1 on job 1 and candidate 2 on job 2 (separate placements); same
     * timesheet settings for both,
     * then bulk submit time logs for both timesheets in one request.
     */
    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "testTwoJobsTwoCandidatesTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void submitTimesheetsForTwoCandidatesOnTwoJobsSameSettingsTest(int jobId1, int jobId2,
            int candidateId1, int candidateId2, int userId, int timesheetFrequency) {
        enableTimesheet(candidateId1, jobId1, userId, albatrossAuthToken, timesheetFrequency, 200, 0);
        enableTimesheet(candidateId2, jobId2, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

        Response freeSlotsResponse1 = getTimeSheetFreeSlots(candidateId1, jobId1, timesheetFrequency,
                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);
        Response freeSlotsResponse2 = getTimeSheetFreeSlots(candidateId2, jobId2, timesheetFrequency,
                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

        List<TimesheetDate> timesheetDates1 = convertFreeSlotsToTimesheetDates(
                freeSlotsResponse1.jsonPath().getList("data"), timesheetFrequency);
        List<TimesheetDate> timesheetDates2 = convertFreeSlotsToTimesheetDates(
                freeSlotsResponse2.jsonPath().getList("data"), timesheetFrequency);

        addTimeSheet(jobId1, Arrays.asList(candidateId1), timesheetDates1, albatrossAuthToken);
        addTimeSheet(jobId2, Arrays.asList(candidateId2), timesheetDates2, albatrossAuthToken);

        Response getAllTimesheetsResponse1 = getAllTimesheets(jobId1, candidateId1, 1, 100, albatrossAuthToken);
        Response getAllTimesheetsResponse2 = getAllTimesheets(jobId2, candidateId2, 1, 100, albatrossAuthToken);

        assertThat(getAllTimesheetsResponse1.statusCode(), is(200));
        assertThat(getAllTimesheetsResponse2.statusCode(), is(200));

        JsonPath getAllTimesheetsJsonPath1 = getAllTimesheetsResponse1.jsonPath();
        JsonPath getAllTimesheetsJsonPath2 = getAllTimesheetsResponse2.jsonPath();
        int timesheetId1 = ((Number) getAllTimesheetsJsonPath1.get("data[0].id")).intValue();
        int timesheetId2 = ((Number) getAllTimesheetsJsonPath2.get("data[0].id")).intValue();

        Response timeLogsResponse1 = getTimeSheetTimeLogs(timesheetId1, albatrossAuthToken);
        Response timeLogsResponse2 = getTimeSheetTimeLogs(timesheetId2, albatrossAuthToken);
        JsonPath timeLogsJsonPath1 = timeLogsResponse1.jsonPath();
        JsonPath timeLogsJsonPath2 = timeLogsResponse2.jsonPath();
        List<Map<String, Object>> timeLogs1 = timeLogsJsonPath1.getList("data.timeLogs");
        List<Map<String, Object>> timeLogs2 = timeLogsJsonPath2.getList("data.timeLogs");
        List<TimeLog> timeLogsList1 = generateTimelogIDLists(timeLogs1, timesheetId1);
        List<TimeLog> timeLogsList2 = generateTimelogIDLists(timeLogs2, timesheetId2);

        List<TimeLog> combinedTimeLogs = new ArrayList<>();
        combinedTimeLogs.addAll(timeLogsList1);
        combinedTimeLogs.addAll(timeLogsList2);

        SubmitTimeLogsRequest submitRequest = new SubmitTimeLogsRequest();
        submitRequest.setTimeLogs(combinedTimeLogs);
        Response submitResponse = submitTimeLogsForTimesheet(submitRequest, albatrossAuthToken);

        assertThat(submitResponse.statusCode(), is(200));
        JsonPath submitJsonPath = submitResponse.jsonPath();
        assertThat(submitJsonPath.getInt("meta.status"), is(200));
        assertThat(submitJsonPath.getString("meta.message"), is("Time logs bulk updated successfully"));
        assertThat(submitJsonPath.getString("meta.responseType.context"), is("Request is successful"));
    }

    @DataProvider
    public Object[][] testTwoJobsTwoCandidatesTimesheetSettingsData() {
        Object[] testData = createTwoJobsTwoCandidatesTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId1 = ((Number) testData[0]).intValue();
        int jobId2 = ((Number) testData[1]).intValue();
        int candidateId1 = ((Number) testData[2]).intValue();
        int candidateId2 = ((Number) testData[3]).intValue();
        int userId = ((Number) testData[4]).intValue();

        return new Object[][] {
                { jobId1, jobId2, candidateId1, candidateId2, userId, 2 },
        };
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "testTimesheetSettingsData")
    public void bulkUpdateSubmittedTimeSheet(int jobId, int candidateId, int userId, int timesheetFrequency) {
        enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

        Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

        Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates,
                albatrossAuthToken);

        Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId, 1, 100, albatrossAuthToken);
        assertThat(getAllTimesheetsResponse.statusCode(), is(200));
        JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse.jsonPath();
        timesheetIDGlobal = ((Number) getAllTimesheetsJsonPath.get("data[0].id")).intValue();
        Response timeLogsResponse = getTimeSheetTimeLogs(timesheetIDGlobal, albatrossAuthToken);
        JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
        List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");
        List<TimeLog> timeLogsList = generateTimelogIDLists(timeLogs, timesheetIDGlobal);
        SubmitTimeLogsRequest submitRequest = new SubmitTimeLogsRequest();
        submitRequest.setTimeLogs(timeLogsList);
        submitRequest.setTimeDetails(Arrays.asList(generateTimeDetails(timeLogs, timesheetIDGlobal)));
        Response submitResponse = submitTimeLogsForTimesheet(submitRequest, albatrossAuthToken);

        assertThat(submitResponse.statusCode(), is(200));

        JsonPath submitJsonPath = submitResponse.jsonPath();
        assertThat(submitJsonPath.getInt("meta.status"), is(200));
        assertThat(submitJsonPath.getString("meta.message"), is("Time logs bulk updated successfully"));
        assertThat(submitJsonPath.getString("meta.responseType.context"), is("Request is successful"));
    }

    @DataProvider(parallel = true)
    public Object[][] testTimesheetSettingsData() {
        Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int candidateId2 = ((Number) testData[2]).intValue();
        int candidateId3 = ((Number) testData[3]).intValue();
        int userId = ((Number) testData[4]).intValue();

        return new Object[][] {
                { jobId, candidateId, userId, 2 },
                { jobId, candidateId2, userId, 3 },
                { jobId, candidateId3, userId, 4 },
        };
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "submitTimeLogForTimesheetWithNEntryData", groups = {"contract_staffing", "nightly-build"})
    public void submitTimeLogForTimesheetWithNEntryTest(int jobId, int candidateId, int userId, int timesheetFrequency,
            int numberOfWorkEntriesPerLog, int numberOfBreaksPerSegment) {
        enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

        Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

        addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates, albatrossAuthToken);

        Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId, 1, 100, albatrossAuthToken);

        assertThat(getAllTimesheetsResponse.statusCode(), is(200));
        JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse.jsonPath();
        int timesheetId = ((Number) getAllTimesheetsJsonPath.get("data[0].id")).intValue();
        Response timeLogsResponse = getTimeSheetTimeLogs(timesheetId, albatrossAuthToken);
        JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
        List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");

        List<TimeLog> timeLogsList = generateTimelogIDListsWithNEntry(timeLogs, timesheetId, numberOfWorkEntriesPerLog,
                numberOfBreaksPerSegment);

        SubmitTimeLogsRequest submitRequest = new SubmitTimeLogsRequest();
        submitRequest.setTimeLogs(timeLogsList);

        Response submitResponse = submitTimeLogsForTimesheet(submitRequest, albatrossAuthToken);
        JsonPath submitJsonPath = submitResponse.jsonPath();

        boolean expectIntervalLimitError = numberOfWorkEntriesPerLog == 11 && numberOfBreaksPerSegment == 1;
        boolean expectBreakLimitError = numberOfWorkEntriesPerLog == 2 && numberOfBreaksPerSegment == 6;

        if (expectIntervalLimitError || expectBreakLimitError) {
            assertThat(submitResponse.statusCode(), is(400));
            assertThat(submitJsonPath.getInt("meta.status"), is(400));
            assertThat(submitJsonPath.getString("meta.responseType.context"), is("Error while processing request"));
            assertThat(submitJsonPath.getInt("meta.responseType.code"), is(101));
            assertThat(submitJsonPath.get("data"), nullValue());
            String errorMessage = submitJsonPath.getString("errors[0].message");
            assertThat(errorMessage, notNullValue());
            assertThat(submitJsonPath.getString("errors[0].errorType.context"), is("Generic Error"));
            assertThat(submitJsonPath.getInt("errors[0].errorType.code"), is(202));
            if (expectIntervalLimitError) {
                assertThat(errorMessage, containsString("Time log ID"));
                assertThat(errorMessage, containsString("11 intervals after this operation"));
                assertThat(errorMessage, containsString("exceeds the maximum of 10"));
            } else {
                assertThat(errorMessage, containsString("Time log ID"));
                assertThat(errorMessage, containsString("6 break intervals"));
                assertThat(errorMessage, containsString("exceeds the maximum of 5"));
            }
        } else {
            assertThat(submitResponse.statusCode(), is(200));
            assertThat(submitJsonPath.getInt("meta.status"), is(200));
            assertThat(submitJsonPath.getString("meta.message"), is("Time logs bulk updated successfully"));
            assertThat(submitJsonPath.getString("meta.responseType.context"), is("Request is successful"));
            submitResponse.then().assertThat()
                    .body(matchesJsonSchemaInClasspath("privateApi/contractStaffing/SubmitTimeLogsForTimesheet.json"));
        }
    }

    @DataProvider(parallel = true)
    public Object[][] submitTimeLogForTimesheetWithNEntryData() {
        Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int candidateId2 = ((Number) testData[2]).intValue();
        int candidateId3 = ((Number) testData[3]).intValue();
        int userId = ((Number) testData[4]).intValue();

        return new Object[][] {
                { jobId, candidateId, userId, 2, 10, 5 },
                { jobId, candidateId2, userId, 3, 11, 1 },
                { jobId, candidateId3, userId, 4, 2, 6 },
        };
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "submitTimeLogForTimesheetWithNEntryMultipleContractorsData", groups = {"contract_staffing", "nightly-build"})
    public void submitTimeLogForTimesheetWithNEntryMultipleContractorsTest(int jobId, int candidateId1,
            int candidateId2, int userId, int timesheetFrequency, int numberOfWorkEntriesPerLog,
            int numberOfBreaksPerSegment) {
        enableTimesheet(candidateId1, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);
        enableTimesheet(candidateId2, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

        Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId1, jobId, timesheetFrequency,
                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

        Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId1, candidateId2), timesheetDates,
                albatrossAuthToken);
        assertThat(addTimesheetResponse.statusCode(), is(200));

        Response getAllTimesheetsResponse1 = getAllTimesheets(jobId, candidateId1, 1, 100, albatrossAuthToken);
        Response getAllTimesheetsResponse2 = getAllTimesheets(jobId, candidateId2, 1, 100, albatrossAuthToken);

        assertThat(getAllTimesheetsResponse1.statusCode(), is(200));
        assertThat(getAllTimesheetsResponse2.statusCode(), is(200));
        JsonPath getAllTimesheetsJsonPath1 = getAllTimesheetsResponse1.jsonPath();
        JsonPath getAllTimesheetsJsonPath2 = getAllTimesheetsResponse2.jsonPath();
        int timesheetId1 = ((Number) getAllTimesheetsJsonPath1.get("data[0].id")).intValue();
        int timesheetId2 = ((Number) getAllTimesheetsJsonPath2.get("data[0].id")).intValue();

        Response timeLogsResponse1 = getTimeSheetTimeLogs(timesheetId1, albatrossAuthToken);
        Response timeLogsResponse2 = getTimeSheetTimeLogs(timesheetId2, albatrossAuthToken);
        JsonPath timeLogsJsonPath1 = timeLogsResponse1.jsonPath();
        JsonPath timeLogsJsonPath2 = timeLogsResponse2.jsonPath();
        List<Map<String, Object>> timeLogs1 = timeLogsJsonPath1.getList("data.timeLogs");
        List<Map<String, Object>> timeLogs2 = timeLogsJsonPath2.getList("data.timeLogs");

        List<TimeLog> timeLogsList1 = generateTimelogIDListsWithNEntry(timeLogs1, timesheetId1,
                numberOfWorkEntriesPerLog, numberOfBreaksPerSegment);
        List<TimeLog> timeLogsList2 = generateTimelogIDListsWithNEntry(timeLogs2, timesheetId2,
                numberOfWorkEntriesPerLog, numberOfBreaksPerSegment);

        List<TimeLog> combinedTimeLogs = new ArrayList<>();
        combinedTimeLogs.addAll(timeLogsList1);
        combinedTimeLogs.addAll(timeLogsList2);

        SubmitTimeLogsRequest submitRequest = new SubmitTimeLogsRequest();
        submitRequest.setTimeLogs(combinedTimeLogs);

        Response submitResponse = submitTimeLogsForTimesheet(submitRequest, albatrossAuthToken);
        JsonPath submitJsonPath = submitResponse.jsonPath();

        boolean expectIntervalLimitError = numberOfWorkEntriesPerLog == 11 && numberOfBreaksPerSegment == 1;
        boolean expectBreakLimitError = numberOfWorkEntriesPerLog == 2 && numberOfBreaksPerSegment == 6;

        if (expectIntervalLimitError || expectBreakLimitError) {
            assertThat(submitResponse.statusCode(), is(400));
            assertThat(submitJsonPath.getInt("meta.status"), is(400));
            assertThat(submitJsonPath.getString("meta.responseType.context"), is("Error while processing request"));
            assertThat(submitJsonPath.getInt("meta.responseType.code"), is(101));
            assertThat(submitJsonPath.get("data"), nullValue());
            String errorMessage = submitJsonPath.getString("errors[0].message");
            assertThat(errorMessage, notNullValue());
            assertThat(submitJsonPath.getString("errors[0].errorType.context"), is("Generic Error"));
            assertThat(submitJsonPath.getInt("errors[0].errorType.code"), is(202));
            if (expectIntervalLimitError) {
                assertThat(errorMessage, containsString("Time log ID"));
                assertThat(errorMessage, containsString("11 intervals after this operation"));
                assertThat(errorMessage, containsString("exceeds the maximum of 10"));
            } else {
                assertThat(errorMessage, containsString("Time log ID"));
                assertThat(errorMessage, containsString("6 break intervals"));
                assertThat(errorMessage, containsString("exceeds the maximum of 5"));
            }
        } else {
            assertThat(submitResponse.statusCode(), is(200));
            assertThat(submitJsonPath.getInt("meta.status"), is(200));
            assertThat(submitJsonPath.getString("meta.message"), is("Time logs bulk updated successfully"));
            assertThat(submitJsonPath.getString("meta.responseType.context"), is("Request is successful"));
            submitResponse.then().assertThat()
                    .body(matchesJsonSchemaInClasspath("privateApi/contractStaffing/SubmitTimeLogsForTimesheet.json"));
        }
    }

    @DataProvider(parallel = true)
    public Object[][] submitTimeLogForTimesheetWithNEntryMultipleContractorsData() {
        Object[] scenario1 = buildSubmitTimeLogMultiContractorRow(2, 10, 5);
        int jobId1 = ((Number) scenario1[0]).intValue();
        int candidateId1 = ((Number) scenario1[1]).intValue();
        int candidateId2 = ((Number) scenario1[2]).intValue();
        int userId1 = ((Number) scenario1[3]).intValue();

        Object[] scenario2 = buildSubmitTimeLogMultiContractorRow(3, 11, 1);
        int jobId2 = ((Number) scenario2[0]).intValue();
        int candidateId3 = ((Number) scenario2[1]).intValue();
        int candidateId4 = ((Number) scenario2[2]).intValue();
        int userId2 = ((Number) scenario2[3]).intValue();

        Object[] scenario3 = buildSubmitTimeLogMultiContractorRow(4, 2, 6);
        int jobId3 = ((Number) scenario3[0]).intValue();
        int candidateId5 = ((Number) scenario3[1]).intValue();
        int candidateId6 = ((Number) scenario3[2]).intValue();
        int userId3 = ((Number) scenario3[3]).intValue();

        return new Object[][] {
                { jobId1, candidateId1, candidateId2, userId1, 2, 10, 5 },
                { jobId2, candidateId3, candidateId4, userId2, 3, 11, 1 },
                { jobId3, candidateId5, candidateId6, userId3, 4, 2, 6 },
        };
    }

    @DataProvider(parallel = true)
    public Object[][] testTimeSheetSettingsDataForMultipleCandidates() {
        Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int candidateId2 = ((Number) testData[2]).intValue();
        int userId = ((Number) testData[4]).intValue();

        return new Object[][] {
                { jobId, candidateId, candidateId2, userId, 2 },
        };
    }
}