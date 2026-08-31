package io.recruitcrm.contractStaffing.hourBasedTimeSheets.BulkUpdateTimeSheets;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.contractStaffing.ApproveTimesheetRequest;
import io.rcrm.api.pojo.albatross.contractStaffing.SubmitTimeLogsRequest;
import io.rcrm.api.pojo.albatross.contractStaffing.TimeDetails;
import io.rcrm.api.pojo.albatross.contractStaffing.TimesheetDate;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class BulkUpdateTimeSheetsHourBasedTest extends ContractStaffingBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void getTimeLogOfTimeSheetHourBasedTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

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

        Response timeLogsResponse = getTimeSheetTimeLogs(timesheetID, albatrossAuthToken);

        assertThat(timeLogsResponse.statusCode(), is(200));

        JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
        assertThat(timeLogsJsonPath.getInt("meta.status"), is(200));
        assertThat(timeLogsJsonPath.getString("meta.message"), is("Time logs fetched successfully"));
        assertThat(timeLogsJsonPath.getString("meta.responseType.context"), is("Request is successful"));
        assertThat(timeLogsJsonPath.getInt("meta.responseType.code"), is(103));

        assertThat(timeLogsJsonPath.getInt("data.timesheetId"), is(timesheetID));
        assertThat(timeLogsJsonPath.getInt("data.workLogType"), is(1));
        assertThat(timeLogsJsonPath.getInt("data.timesheetFrequency"), is(timesheetFrequency));
        assertThat(timeLogsJsonPath.getBoolean("data.calculateBreakTime"), is(false));
        assertThat(timeLogsJsonPath.getInt("data.approvalStatusId"), is(1));

        assertThat(timeLogsJsonPath.get("data.timeLogs"), notNullValue());
        List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");
        assertThat(timeLogs.size(), greaterThan(0));

        for (Map<String, Object> timeLog : timeLogs) {
            assertThat(timeLog.get("id"), notNullValue());
            assertThat(timeLog.get("timesheetId"), notNullValue());
            assertThat(timeLog.get("timesheetPeriod"), notNullValue());
            int actualTimesheetId = ((Number) timeLog.get("timesheetId")).intValue();
            assertThat(actualTimesheetId, is(timesheetID));
        }

        assertThat(timeLogsJsonPath.get("data.templateWorkDays"), notNullValue());
        assertThat(timeLogsJsonPath.get("data.approvers"), notNullValue());

        timeLogsResponse.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/contractStaffing/GetTimeSheetTimeLogs.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void getTimeLogOfTimeSheetWithInvalidTimesheetIdHourBasedTest() {
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

    @Owner("Akshaya Uppala")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void unauthorizedUserCannotGetTimeLogOfTimeSheetHourBasedTest() {
        int validTimesheetId = 825;
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

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void submitTimeLogForTimesheetHourBasedTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

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
        int timesheetIDGlobal = ((Number) getAllTimesheetsJsonPath.get("data[0].id")).intValue();
        Response timeLogsResponse = getTimeSheetTimeLogs(timesheetIDGlobal, albatrossAuthToken);
        JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
        List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");

        List<Map<String, Object>> timeLogsList = generateTimeLogIdsforHourBased(timeLogs, timesheetIDGlobal);
        TimeDetails timeDetails = generateTimeDetailsForHourBased(timeLogs, timesheetIDGlobal);

        Map<String, Object> submitTimeLogsRequest = new HashMap<>();
        submitTimeLogsRequest.put("timeLogs", timeLogsList);
        submitTimeLogsRequest.put("timeDetails", Arrays.asList(timeDetails));
        Response submitResponse = submitTimeLogsForTimesheetHourBased(submitTimeLogsRequest, albatrossAuthToken);

        assertThat(submitResponse.statusCode(), is(200));

        JsonPath submitJsonPath = submitResponse.jsonPath();
        assertThat(submitJsonPath.getInt("meta.status"), is(200));
        assertThat(submitJsonPath.getString("meta.message"), is("Time logs bulk updated successfully"));
        assertThat(submitJsonPath.getString("meta.responseType.context"), is("Request is successful"));
        submitResponse.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/contractStaffing/SubmitTimeLogsForTimesheet.json"));
    }

    @Owner("Akshaya Uppala")
    @Test
    public void submitTimeLogForTimesheetWithInvalidTimesheetIdHourBasedTest() {
        Map<String, Object> timeLogMap = new HashMap<>();
        timeLogMap.put("id", 1);
        timeLogMap.put("timesheetId", 999999);
        timeLogMap.put("timesheetPeriod", "Jul 14, 2025 - Jul 20, 2025");
        timeLogMap.put("workTime", 32400);
        timeLogMap.put("breakTime", 3600);
        timeLogMap.put("overTime", 1800);
        timeLogMap.put("totalTime", 32400);
        timeLogMap.put("remark", "Invalid Test");
        timeLogMap.put("workTimeDetails", null);

        TimeDetails timeDetails = TimeDetails.builder()
                .timesheetId(999999)
                .totalWorkTime(32400)
                .totalOvertime(1800)
                .totalTime(32400)
                .build();

        Map<String, Object> submitTimeLogsRequest = new HashMap<>();
        submitTimeLogsRequest.put("isApproved", 0);
        submitTimeLogsRequest.put("timeLogs", Arrays.asList(timeLogMap));
        submitTimeLogsRequest.put("timeDetails", Arrays.asList(timeDetails));
        Response submitResponse = submitTimeLogsForTimesheetHourBased(submitTimeLogsRequest, albatrossAuthToken);

        assertThat(submitResponse.statusCode(), is(404));

        JsonPath jsonPath = submitResponse.jsonPath();
        assertThat(jsonPath.getInt("meta.status"), is(404));
        assertThat(jsonPath.get("meta.message"), nullValue());
        assertThat(jsonPath.getString("meta.responseType.context"), is("Error while processing request"));
        assertThat(jsonPath.getInt("meta.responseType.code"), is(101));
        assertThat(jsonPath.get("data"), nullValue());

        List<Map<String, Object>> errors = jsonPath.getList("errors");
        assertThat(errors.size(), greaterThan(0));
        String errorMessage = String.valueOf(errors.get(0).get("message"));
        Assert.assertTrue(errorMessage.contains("Timesheet id") && errorMessage.contains("not found"));

        @SuppressWarnings("unchecked")
        Map<String, Object> errorType = (Map<String, Object>) errors.get(0).get("errorType");
        assertThat("Generic Error", is(errorType.get("context")));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void unauthorizedUserCannotSubmitTimeLogHourBasedTest() {
        Map<String, Object> timeLogMap = new HashMap<>();
        timeLogMap.put("id", 123);
        timeLogMap.put("timesheetId", 456);
        timeLogMap.put("timesheetPeriod", "Jul 14, 2025 - Jul 20, 2025");
        timeLogMap.put("workTime", 32400);
        timeLogMap.put("breakTime", 3600);
        timeLogMap.put("overTime", 1800);
        timeLogMap.put("totalTime", 32400);
        timeLogMap.put("remark", "Unauthorized Test");
        timeLogMap.put("workTimeDetails", null);

        TimeDetails timeDetails = TimeDetails.builder()
                .timesheetId(456)
                .totalWorkTime(32400)
                .totalOvertime(1800)
                .totalTime(32400)
                .build();

        String invalidAuthToken = albatrossAuthToken + "invalid";
        Map<String, Object> submitTimeLogsRequest = new HashMap<>();
        submitTimeLogsRequest.put("isApproved", 0);
        submitTimeLogsRequest.put("timeLogs", Arrays.asList(timeLogMap));
        submitTimeLogsRequest.put("timeDetails", Arrays.asList(timeDetails));
        Response submitResponse = submitTimeLogsForTimesheetHourBased(submitTimeLogsRequest, invalidAuthToken);

        assertThat(submitResponse.statusCode(), is(401));

        JsonPath jsonPath = submitResponse.jsonPath();
        assertThat(jsonPath.getInt("meta.status"), is(401));
        assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
        assertThat(jsonPath.getString("meta.responseType.context"), is("Warning"));
        assertThat(jsonPath.getInt("meta.responseType.code"), is(104));
        assertThat(jsonPath.getString("data"), is("Invalid token"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void approveTimeSheetHourBasedTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

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

        Response timeLogsResponse = getTimeSheetTimeLogs(timesheetID, albatrossAuthToken);
        assertThat(timeLogsResponse.statusCode(), is(200));

        JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
        List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");

        List<Map<String, Object>> timeLogsList = generateTimeLogIdsforHourBased(timeLogs, timesheetID);
        TimeDetails timeDetails = generateTimeDetailsForHourBased(timeLogs, timesheetID);

        Map<String, Object> submitTimeLogsRequest = new HashMap<>();
        submitTimeLogsRequest.put("isApproved", 0);
        submitTimeLogsRequest.put("timeLogs", timeLogsList);
        submitTimeLogsRequest.put("timeDetails", Arrays.asList(timeDetails));
        Response submitResponse = submitTimeLogsForTimesheetHourBased(submitTimeLogsRequest, albatrossAuthToken);
        assertThat(submitResponse.statusCode(), is(200));

        int approvalStatus = new Random().nextInt(2) + 3;
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

    @Owner("Akshaya Uppala")
    @Test
    public void approveTimeSheetWithInvalidApprovalStatusHourBasedTest() {
        int timesheetId = 1146;

        ApproveTimesheetRequest approveRequest = new ApproveTimesheetRequest();
        approveRequest.setApprovalStatus(999);

        Response approveResponse = approveTimesheet(timesheetId, approveRequest, albatrossAuthToken);

        assertThat(approveResponse.statusCode(), is(401));

        JsonPath jsonPath = approveResponse.jsonPath();
        assertThat(jsonPath.getInt("meta.status"), is(401));
        assertThat(jsonPath.getString("meta.responseType.context"), is("Error while processing request"));
        assertThat(jsonPath.getInt("meta.responseType.code"), is(101));
        assertThat(jsonPath.get("data"), nullValue());

        List<Map<String, Object>> errors = jsonPath.getList("errors");
        assertThat(errors.size(), greaterThan(0));
        String errorMessage = String.valueOf(errors.get(0).get("message"));
        Assert.assertTrue(errorMessage.contains("is not an approver for timesheet ID"));

        @SuppressWarnings("unchecked")
        Map<String, Object> errorType = (Map<String, Object>) errors.get(0).get("errorType");
        assertThat("Generic Error", is(errorType.get("context")));
        assertThat(202, is(((Number) errorType.get("code")).intValue()));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void unauthorizedUserCannotApproveTimeSheetHourBasedTest() {
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

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void getTimeSheetStatusHistoryHourBasedTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

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

        Response timeLogsResponse = getTimeSheetTimeLogs(timesheetID, albatrossAuthToken);
        assertThat(timeLogsResponse.statusCode(), is(200));
        JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
        List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");

        List<Map<String, Object>> timeLogsList = generateTimeLogIdsforHourBased(timeLogs, timesheetID);
        TimeDetails timeDetails = generateTimeDetailsForHourBased(timeLogs, timesheetID);

        Map<String, Object> submitTimeLogsRequest = new HashMap<>();
        submitTimeLogsRequest.put("isApproved", 0);
        submitTimeLogsRequest.put("timeLogs", timeLogsList);
        submitTimeLogsRequest.put("timeDetails", Arrays.asList(timeDetails));

        Response submitResponse = submitTimeLogsForTimesheetHourBased(submitTimeLogsRequest, albatrossAuthToken);
        assertThat(submitResponse.statusCode(), is(200));

        int randomApprovalStatus = new Random().nextInt(2) + 3;
        ApproveTimesheetRequest approveRequest = buildApproveTimesheetRequest(randomApprovalStatus,
                "Rejected by automated test");

        Response approveResponse = approveTimesheet(timesheetID, approveRequest, albatrossAuthToken);
        assertThat(approveResponse.statusCode(), is(201));
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
        }
        assertThat(statusHistory.size(), greaterThanOrEqualTo(3));
        Map<String, Object> latestEntry = statusHistory.get(0);
        assertThat(((Number) latestEntry.get("status")).intValue(), is(randomApprovalStatus));

        statusHistoryResponse.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/contractStaffing/GetTimeSheetStatusHistory.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void getTimeSheetStatusHistoryWithInvalidTimesheetIdHourBasedTest() {
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

    @Owner("Akshaya Uppala")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void unauthorizedUserCannotGetTimeSheetStatusHistoryHourBasedTest() {
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

    @DataProvider(parallel = true)
    public Object[][] testTimesheetSettingsData() {
        Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int candidateId2 = ((Number) testData[2]).intValue();
        int candidateId3 = ((Number) testData[3]).intValue();
        int userId = ((Number) testData[4]).intValue();

        return new Object[][]{
                {jobId, candidateId, userId, 2},
                {jobId, candidateId2, userId, 3},
                {jobId, candidateId3, userId, 4},
        };
    }
}
