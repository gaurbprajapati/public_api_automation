package io.recruitcrm.contractStaffing.hourBasedTimeSheets.BulkUpdateTimeSheets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.contractStaffing.ApproveTimesheetRequest;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class DeleteTimesheetsHourBasedTest extends ContractStaffingBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;
    private final Faker faker = new Faker();

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
    }

    /**
     * Hour-based submit: same flow as {@link BulkUpdateTimeSheetsHourBasedTest#submitTimeLogForTimesheetHourBasedTest}.
     */
    private void submitTimeLogsForTimesheetUsingHourBasedApi(int timesheetId) {
        Response timeLogsResponse = getTimeSheetTimeLogs(timesheetId, albatrossAuthToken);
        assertThat(timeLogsResponse.statusCode(), is(200));
        JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
        List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");
        List<Map<String, Object>> timeLogsList = generateTimeLogIdsforHourBased(timeLogs, timesheetId);
        TimeDetails timeDetails = generateTimeDetailsForHourBased(timeLogs, timesheetId);
        Map<String, Object> submitTimeLogsRequest = new HashMap<>();
        submitTimeLogsRequest.put("timeLogs", timeLogsList);
        submitTimeLogsRequest.put("timeDetails", Arrays.asList(timeDetails));
        Response submitResponse = submitTimeLogsForTimesheetHourBased(submitTimeLogsRequest, albatrossAuthToken);
        assertThat(submitResponse.statusCode(), is(200));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void deleteTimesheetsHourBasedTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

        Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

        Response addTimesheetResponse1 = addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates,
                albatrossAuthToken);

        assertThat(addTimesheetResponse1.statusCode(), is(200));

        Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId, 1, 100, albatrossAuthToken);
        assertThat(getAllTimesheetsResponse.statusCode(), is(200));
        JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse.jsonPath();

        List<Map<String, Object>> dataList = getAllTimesheetsJsonPath.getList("data");
        assertThat(!dataList.isEmpty(), is(true));

        int initialTimesheetCount = dataList.size();
        assertThat(initialTimesheetCount >= 4, is(true));

        int openTimesheetID = getAllTimesheetsJsonPath.getInt("data[0].id");
        int submittedTimesheetID = getAllTimesheetsJsonPath.getInt("data[1].id");
        int approvedTimesheetID = getAllTimesheetsJsonPath.getInt("data[2].id");
        int rejectedTimesheetID = getAllTimesheetsJsonPath.getInt("data[3].id");

        submitTimeLogsForTimesheetUsingHourBasedApi(submittedTimesheetID);

        submitTimeLogsForTimesheetUsingHourBasedApi(approvedTimesheetID);
        ApproveTimesheetRequest approveRequest = buildApproveTimesheetRequest(4);
        Response approveResponse = approveTimesheet(approvedTimesheetID, approveRequest, albatrossAuthToken);
        assertThat(approveResponse.statusCode(), is(201));

        submitTimeLogsForTimesheetUsingHourBasedApi(rejectedTimesheetID);
        ApproveTimesheetRequest rejectRequest = buildApproveTimesheetRequest(3,
                "Rejected for hour-based delete test");
        Response rejectResponse = approveTimesheet(rejectedTimesheetID, rejectRequest, albatrossAuthToken);
        assertThat(rejectResponse.statusCode(), is(201));

        Response deleteOpenResponse = deleteTimesheet(openTimesheetID, albatrossAuthToken);
        assertThat(deleteOpenResponse.statusCode(), is(200));
        JsonPath deleteOpenJsonPath = deleteOpenResponse.jsonPath();
        assertThat(deleteOpenJsonPath.getString("meta.message"), is("Timesheet deleted successfully"));
        assertThat(deleteOpenJsonPath.getInt("meta.status"), is(200));
        Response getAllTimesheetsAfterOpenDeletion = getAllTimesheets(jobId, candidateId, 1, 100, albatrossAuthToken);
        assertThat(getAllTimesheetsAfterOpenDeletion.statusCode(), is(200));
        JsonPath afterOpenDeletionJsonPath = getAllTimesheetsAfterOpenDeletion.jsonPath();
        List<Map<String, Object>> afterOpenDeletionDataList = afterOpenDeletionJsonPath.getList("data");
        int afterOpenDeletionCount = (afterOpenDeletionDataList != null) ? afterOpenDeletionDataList.size() : 0;
        assertThat(afterOpenDeletionCount, is(initialTimesheetCount - 1));
        List<Integer> afterOpenDeletionTimesheetList = new ArrayList<>();
        for (Map<String, Object> timesheet : afterOpenDeletionDataList) {
            int timesheetId = ((Number) timesheet.get("id")).intValue();
            afterOpenDeletionTimesheetList.add(timesheetId);
        }
        assertThat("Deleted OPEN timesheet should not be in the list", afterOpenDeletionTimesheetList,
                not(hasItem(openTimesheetID)));

        Response deleteSubmittedResponse = deleteTimesheet(submittedTimesheetID, albatrossAuthToken);
        assertThat(deleteSubmittedResponse.statusCode(), is(200));
        JsonPath deleteSubmittedJsonPath = deleteSubmittedResponse.jsonPath();
        assertThat(deleteSubmittedJsonPath.getString("meta.message"), is("Timesheet deleted successfully"));
        assertThat(deleteSubmittedJsonPath.getInt("meta.status"), is(200));
        Response getAllTimesheetsAfterSubmittedDeletion = getAllTimesheets(jobId, candidateId, 1, 100,
                albatrossAuthToken);
        assertThat(getAllTimesheetsAfterSubmittedDeletion.statusCode(), is(200));
        JsonPath afterSubmittedDeletionJsonPath = getAllTimesheetsAfterSubmittedDeletion.jsonPath();
        List<Map<String, Object>> afterSubmittedDeletionDataList = afterSubmittedDeletionJsonPath.getList("data");
        int afterSubmittedDeletionCount = (afterSubmittedDeletionDataList != null) ? afterSubmittedDeletionDataList.size() : 0;
        assertThat(afterSubmittedDeletionCount, is(initialTimesheetCount - 2));
        List<Integer> afterSubmittedDeletionTimesheetList = new ArrayList<>();
        for (Map<String, Object> timesheet : afterSubmittedDeletionDataList) {
            int timesheetId = ((Number) timesheet.get("id")).intValue();
            afterSubmittedDeletionTimesheetList.add(timesheetId);
        }
        assertThat("Deleted SUBMITTED timesheet should not be in the list",
                afterSubmittedDeletionTimesheetList, not(hasItem(submittedTimesheetID)));

        Response deleteApprovedResponse = deleteTimesheet(approvedTimesheetID, albatrossAuthToken);
        assertThat(deleteApprovedResponse.statusCode(), is(not(200)));
        Response getAllTimesheetsAfterApprovedDeletion = getAllTimesheets(jobId, candidateId, 1, 100,
                albatrossAuthToken);
        assertThat(getAllTimesheetsAfterApprovedDeletion.statusCode(), is(200));
        JsonPath afterApprovedDeletionJsonPath = getAllTimesheetsAfterApprovedDeletion.jsonPath();
        List<Map<String, Object>> afterApprovedDeletionDataList = afterApprovedDeletionJsonPath.getList("data");
        int afterApprovedDeletionCount = (afterApprovedDeletionDataList != null)
                ? afterApprovedDeletionDataList.size()
                : 0;
        assertThat(afterApprovedDeletionCount, is(initialTimesheetCount - 2));
        List<Integer> afterApprovedDeletionTimesheetList = new ArrayList<>();
        for (Map<String, Object> timesheet : afterApprovedDeletionDataList) {
            int timesheetId = ((Number) timesheet.get("id")).intValue();
            afterApprovedDeletionTimesheetList.add(timesheetId);
        }
        assertThat("Approved timesheet should still be in the list (not deleted)",
                afterApprovedDeletionTimesheetList, hasItem(approvedTimesheetID));

        Response deleteRejectedResponse = deleteTimesheet(rejectedTimesheetID, albatrossAuthToken);
        assertThat(deleteRejectedResponse.statusCode(), is(200));
        JsonPath deleteRejectedJsonPath = deleteRejectedResponse.jsonPath();
        assertThat(deleteRejectedJsonPath.getString("meta.message"), is("Timesheet deleted successfully"));
        assertThat(deleteRejectedJsonPath.getInt("meta.status"), is(200));

        Response getAllTimesheetsAfterRejectedDeletion = getAllTimesheets(jobId, candidateId, 1, 100,
                albatrossAuthToken);
        assertThat(getAllTimesheetsAfterRejectedDeletion.statusCode(), is(200));
        JsonPath afterRejectedDeletionJsonPath = getAllTimesheetsAfterRejectedDeletion.jsonPath();
        List<Map<String, Object>> afterRejectedDeletionDataList = afterRejectedDeletionJsonPath.getList("data");
        int afterRejectedDeletionCount = (afterRejectedDeletionDataList != null) ? afterRejectedDeletionDataList.size() : 0;
        assertThat(afterRejectedDeletionCount, is(initialTimesheetCount - 3));

        List<Integer> afterRejectedDeletionTimesheetList = new ArrayList<>();
        for (Map<String, Object> timesheet : afterRejectedDeletionDataList) {
            int timesheetId = ((Number) timesheet.get("id")).intValue();
            afterRejectedDeletionTimesheetList.add(timesheetId);
        }
        assertThat("Deleted REJECTED timesheet should not be in the list",
                afterRejectedDeletionTimesheetList, not(hasItem(rejectedTimesheetID)));
        assertThat("Approved timesheet should still be in the list",
                afterRejectedDeletionTimesheetList, hasItem(approvedTimesheetID));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void deleteTimesheetWithInvalidTimesheetIDHourBasedTest() {
        int invalidTimesheetId = faker.number().numberBetween(10000000, 99999999);
        Response deleteResponse = deleteTimesheet(invalidTimesheetId, albatrossAuthToken);
        assertThat(deleteResponse.statusCode(), is(404));
        JsonPath jsonPath = deleteResponse.jsonPath();
        String errorMessage = jsonPath.getString("errors[0].message");
        assertThat(errorMessage, notNullValue());
        Assert.assertTrue(errorMessage.contains("Timesheet id " + invalidTimesheetId + " not found."),
                "Error message should contain the invalid timesheet ID: " + invalidTimesheetId);
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void unauthorizedUserCannotDeleteTimesheetHourBasedTest() {
        int timesheetId = 1240;
        String invalidAuthToken = albatrossAuthToken + "invalid";
        Response deleteResponse = deleteTimesheet(timesheetId, invalidAuthToken);

        assertThat(deleteResponse.statusCode(), is(401));

        JsonPath jsonPath = deleteResponse.jsonPath();
        assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
        assertThat(jsonPath.getInt("meta.status"), is(401));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void bulkDeleteTimesheetsHourBasedTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

        Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");
        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

        Response addTimesheetResponse1 = addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates,
                albatrossAuthToken);
        assertThat(addTimesheetResponse1.statusCode(), is(200));

        Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId, 1, 100, albatrossAuthToken);
        assertThat(getAllTimesheetsResponse.statusCode(), is(200));
        JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse.jsonPath();

        List<Map<String, Object>> dataList = getAllTimesheetsJsonPath.getList("data");
        assertThat(!dataList.isEmpty(), is(true));

        int initialTimesheetCount = dataList.size();
        assertThat(initialTimesheetCount >= 4, is(true));

        int openTimesheetID = getAllTimesheetsJsonPath.getInt("data[0].id");
        int submittedTimesheetID = getAllTimesheetsJsonPath.getInt("data[1].id");
        int rejectedTimesheetID = getAllTimesheetsJsonPath.getInt("data[2].id");
        int approvedTimesheetID = getAllTimesheetsJsonPath.getInt("data[3].id");

        assertThat(openTimesheetID > 0, is(true));
        assertThat(submittedTimesheetID > 0, is(true));
        assertThat(rejectedTimesheetID > 0, is(true));
        assertThat(approvedTimesheetID > 0, is(true));

        submitTimeLogsForTimesheetUsingHourBasedApi(submittedTimesheetID);

        submitTimeLogsForTimesheetUsingHourBasedApi(rejectedTimesheetID);
        ApproveTimesheetRequest rejectRequest = buildApproveTimesheetRequest(3,
                "Rejected for hour-based bulk delete test");
        Response rejectResponse = approveTimesheet(rejectedTimesheetID, rejectRequest, albatrossAuthToken);
        assertThat(rejectResponse.statusCode(), is(201));

        submitTimeLogsForTimesheetUsingHourBasedApi(approvedTimesheetID);
        ApproveTimesheetRequest approveRequest = buildApproveTimesheetRequest(4);
        Response approveResponse = approveTimesheet(approvedTimesheetID, approveRequest, albatrossAuthToken);
        assertThat(approveResponse.statusCode(), is(201));

        List<Integer> timesheetIds = Arrays.asList(openTimesheetID, submittedTimesheetID, rejectedTimesheetID,
                approvedTimesheetID);
        Response bulkDeleteResponse = bulkDeleteTimesheets(timesheetIds, albatrossAuthToken);

        assertThat(bulkDeleteResponse.statusCode(), is(200));

        JsonPath bulkDeleteJsonPath = bulkDeleteResponse.jsonPath();
        assertThat(bulkDeleteJsonPath.getString("meta.message"), is("Timesheets deleted successfully"));
        assertThat(bulkDeleteJsonPath.getString("meta.responseType.context"), is("Request is successful"));

        Assert.assertNotNull(bulkDeleteJsonPath.getString("meta.requestUuid"), "Request UUID should not be null");
        Assert.assertNotNull(bulkDeleteJsonPath.getString("meta.timestamp"), "Timestamp should not be null");
        Assert.assertNull(bulkDeleteJsonPath.get("data"), "Data should be null for bulk delete response");

        Response getAllTimesheetsAfterDeletion = getAllTimesheets(jobId, candidateId, 1, 100, albatrossAuthToken);
        assertThat(getAllTimesheetsAfterDeletion.statusCode(), is(200));
        JsonPath afterDeletionJsonPath = getAllTimesheetsAfterDeletion.jsonPath();
        List<Map<String, Object>> afterDeletionDataList = afterDeletionJsonPath.getList("data");

        int afterDeletionCount = (afterDeletionDataList != null) ? afterDeletionDataList.size() : 0;
        assertThat(afterDeletionCount, is(initialTimesheetCount - 3));

        List<Integer> afterDeletionTimesheetList = new ArrayList<>();
        for (Map<String, Object> timesheet : afterDeletionDataList) {
            int timesheetId = ((Number) timesheet.get("id")).intValue();
            afterDeletionTimesheetList.add(timesheetId);
        }

        assertThat("Deleted OPEN timesheet should not be in the list", afterDeletionTimesheetList,
                not(hasItem(openTimesheetID)));
        assertThat("Deleted SUBMITTED timesheet should not be in the list", afterDeletionTimesheetList,
                not(hasItem(submittedTimesheetID)));
        assertThat("Deleted REJECTED timesheet should not be in the list", afterDeletionTimesheetList,
                not(hasItem(rejectedTimesheetID)));

        assertThat("Approved timesheet should still be in the list", afterDeletionTimesheetList,
                hasItem(approvedTimesheetID));
        assertThat("After deletion, timesheet count should be initial count minus 3",
                afterDeletionTimesheetList.size(), is(initialTimesheetCount - 3));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void bulkDeleteTimesheetsWithInvalidIDsHourBasedTest() {
        List<Integer> invalidTimesheetIds = Arrays.asList(999997, 999998, 999999);

        Response bulkDeleteResponse = bulkDeleteTimesheets(invalidTimesheetIds, albatrossAuthToken);

        assertThat(bulkDeleteResponse.statusCode(), is(404));

        JsonPath jsonPath = bulkDeleteResponse.jsonPath();

        assertThat(jsonPath.getInt("meta.status"), is(404));
        assertThat(jsonPath.getInt("meta.responseType.code"), is(101));
        assertThat(jsonPath.getString("meta.responseType.context"), is("Error while processing request"));

        String errorMessage = jsonPath.getString("errors[0].message");
        Assert.assertTrue(errorMessage.contains("not found"), "Error message should contain 'not found'");
        Assert.assertTrue(errorMessage.contains("Timesheet id"), "Error message should contain 'Timesheet id'");
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void unauthorizedUserCannotBulkDeleteTimesheetsHourBasedTest() {
        List<Integer> timesheetIds = Arrays.asList(1240, 1241);

        String invalidAuthToken = albatrossAuthToken + "invalid";
        Response bulkDeleteResponse = bulkDeleteTimesheets(timesheetIds, invalidAuthToken);

        assertThat(bulkDeleteResponse.statusCode(), is(401));

        JsonPath jsonPath = bulkDeleteResponse.jsonPath();

        assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
        assertThat(jsonPath.getInt("meta.status"), is(401));
    }

    @DataProvider(parallel = true)
    public Object[][] testTimesheetSettingsData() {
        Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int candidateId2 = ((Number) testData[2]).intValue();
        int userId = ((Number) testData[4]).intValue();

        return new Object[][]{
                {jobId, candidateId, userId, 2},
                {jobId, candidateId2, userId, 3},
        };
    }
}
