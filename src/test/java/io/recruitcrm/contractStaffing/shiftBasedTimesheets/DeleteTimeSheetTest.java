package io.recruitcrm.contractStaffing.shiftBasedTimesheets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.contractStaffing.ApproveTimesheetRequest;
import io.rcrm.api.pojo.albatross.contractStaffing.SubmitTimeLogsRequest;
import io.rcrm.api.pojo.albatross.contractStaffing.TimeLog;
import io.rcrm.api.pojo.albatross.contractStaffing.TimesheetDate;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class DeleteTimeSheetTest extends ContractStaffingBaseTest {

        String albatrossAuthToken;
        String apiAuthToken;
        int ownerAccountID;
        commanFunction function;
        private final Faker faker = new Faker();

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
        public void deleteTimesheetTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
                enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

                Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

                JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
                List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

                List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

                // Create 4 timesheets for different status scenarios
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

                // Create list of all timesheet IDs
                List<Integer> allTimesheetList = new ArrayList<>();
                for (Map<String, Object> timesheet : dataList) {
                        int timesheetId = ((Number) timesheet.get("id")).intValue();
                        allTimesheetList.add(timesheetId);
                }

                // Get timesheet IDs
                int openTimesheetID = getAllTimesheetsJsonPath.getInt("data[0].id");
                int submittedTimesheetID = getAllTimesheetsJsonPath.getInt("data[1].id");
                int approvedTimesheetID = getAllTimesheetsJsonPath.getInt("data[2].id");
                int rejectedTimesheetID = getAllTimesheetsJsonPath.getInt("data[3].id");


                // 1. Keep first timesheet as OPEN (no action needed)

                // 2. Submit second timesheet (status = SUBMITTED)
                Response submittedTimeLogsResponse = getTimeSheetTimeLogs(submittedTimesheetID, albatrossAuthToken);
                assertThat(submittedTimeLogsResponse.statusCode(), is(200));
                JsonPath submittedTimeLogsJsonPath = submittedTimeLogsResponse.jsonPath();
                List<Map<String, Object>> submittedTimeLogs = submittedTimeLogsJsonPath.getList("data.timeLogs");
                List<TimeLog> submittedTimeLogsList = generateTimelogIDLists(submittedTimeLogs, submittedTimesheetID);
                SubmitTimeLogsRequest submitRequest = new SubmitTimeLogsRequest();
                submitRequest.setTimeLogs(submittedTimeLogsList);
                Response submitResponse = submitTimeLogsForTimesheet(submitRequest, albatrossAuthToken);
                assertThat(submitResponse.statusCode(), is(200));


                // 3. Approve third timesheet (status = APPROVED)
                Response approvedTimeLogsResponse = getTimeSheetTimeLogs(approvedTimesheetID, albatrossAuthToken);
                assertThat(approvedTimeLogsResponse.statusCode(), is(200));
                JsonPath approvedTimeLogsJsonPath = approvedTimeLogsResponse.jsonPath();
                List<Map<String, Object>> approvedTimeLogs = approvedTimeLogsJsonPath.getList("data.timeLogs");
                List<TimeLog> approvedTimeLogsList = generateTimelogIDLists(approvedTimeLogs, approvedTimesheetID);
                SubmitTimeLogsRequest submitRequestForApproved = new SubmitTimeLogsRequest();
                submitRequestForApproved.setTimeLogs(approvedTimeLogsList);
                Response submitResponseForApproved = submitTimeLogsForTimesheet(submitRequestForApproved,
                                albatrossAuthToken);
                assertThat(submitResponseForApproved.statusCode(), is(200));
                ApproveTimesheetRequest approveRequest = buildApproveTimesheetRequest(4);
                Response approveResponse = approveTimesheet(approvedTimesheetID, approveRequest,
                                albatrossAuthToken);
                assertThat(approveResponse.statusCode(), is(201));


                // 4. Reject fourth timesheet (status = REJECTED)
                Response rejectedTimeLogsResponse = getTimeSheetTimeLogs(rejectedTimesheetID, albatrossAuthToken);
                assertThat(rejectedTimeLogsResponse.statusCode(), is(200));
                JsonPath rejectedTimeLogsJsonPath = rejectedTimeLogsResponse.jsonPath();
                List<Map<String, Object>> rejectedTimeLogs = rejectedTimeLogsJsonPath.getList("data.timeLogs");
                List<TimeLog> rejectedTimeLogsList = generateTimelogIDLists(rejectedTimeLogs, rejectedTimesheetID);
                SubmitTimeLogsRequest submitRequestForRejected = new SubmitTimeLogsRequest();
                submitRequestForRejected.setTimeLogs(rejectedTimeLogsList);
                Response submitResponseForRejected = submitTimeLogsForTimesheet(submitRequestForRejected, albatrossAuthToken);
                assertThat(submitResponseForRejected.statusCode(), is(200));
                ApproveTimesheetRequest rejectRequest = buildApproveTimesheetRequest(3, "Rejected for delete test");
                Response rejectResponse = approveTimesheet(rejectedTimesheetID, rejectRequest, albatrossAuthToken);
                assertThat(rejectResponse.statusCode(), is(201));


                // Test 1: Delete OPEN timesheet - should be deleted
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
                // Verify deleted timesheet is not in the list
                List<Integer> afterOpenDeletionTimesheetList = new ArrayList<>();
                for (Map<String, Object> timesheet : afterOpenDeletionDataList) {
                        int timesheetId = ((Number) timesheet.get("id")).intValue();
                        afterOpenDeletionTimesheetList.add(timesheetId);
                }
                assertThat("Deleted OPEN timesheet should not be in the list", afterOpenDeletionTimesheetList,
                                not(hasItem(openTimesheetID)));

                // Test 2: Delete SUBMITTED timesheet - should be deleted
                Response deleteSubmittedResponse = deleteTimesheet(submittedTimesheetID, albatrossAuthToken);
                assertThat(deleteSubmittedResponse.statusCode(), is(200));
                JsonPath deleteSubmittedJsonPath = deleteSubmittedResponse.jsonPath();
                assertThat(deleteSubmittedJsonPath.getString("meta.message"), is("Timesheet deleted successfully"));
                assertThat(deleteSubmittedJsonPath.getInt("meta.status"), is(200));
                Response getAllTimesheetsAfterSubmittedDeletion = getAllTimesheets(jobId, candidateId, 1, 100,
                                albatrossAuthToken);
                assertThat(getAllTimesheetsAfterSubmittedDeletion.statusCode(), is(200));
                JsonPath afterSubmittedDeletionJsonPath = getAllTimesheetsAfterSubmittedDeletion.jsonPath();
                List<Map<String, Object>> afterSubmittedDeletionDataList = afterSubmittedDeletionJsonPath
                                .getList("data");
                int afterSubmittedDeletionCount = (afterSubmittedDeletionDataList != null) ? afterSubmittedDeletionDataList.size() : 0;
                assertThat(afterSubmittedDeletionCount, is(initialTimesheetCount - 2));
                // Verify deleted timesheet is not in the list
                List<Integer> afterSubmittedDeletionTimesheetList = new ArrayList<>();
                for (Map<String, Object> timesheet : afterSubmittedDeletionDataList) {
                        int timesheetId = ((Number) timesheet.get("id")).intValue();
                        afterSubmittedDeletionTimesheetList.add(timesheetId);
                }
                assertThat("Deleted SUBMITTED timesheet should not be in the list",
                                afterSubmittedDeletionTimesheetList, not(hasItem(submittedTimesheetID)));

                // Test 3: Delete APPROVED timesheet - should NOT be deleted
                Response deleteApprovedResponse = deleteTimesheet(approvedTimesheetID, albatrossAuthToken);
                // Approved timesheets should not be deleted, expect error status
                assertThat(deleteApprovedResponse.statusCode(), is(not(200)));
                // Verify timesheet still exists
                Response getAllTimesheetsAfterApprovedDeletion = getAllTimesheets(jobId, candidateId, 1, 100,
                                albatrossAuthToken);
                assertThat(getAllTimesheetsAfterApprovedDeletion.statusCode(), is(200));
                JsonPath afterApprovedDeletionJsonPath = getAllTimesheetsAfterApprovedDeletion.jsonPath();
                List<Map<String, Object>> afterApprovedDeletionDataList = afterApprovedDeletionJsonPath.getList("data");
                int afterApprovedDeletionCount = (afterApprovedDeletionDataList != null)
                                ? afterApprovedDeletionDataList.size()
                                : 0;
                // Count should remain the same (timesheet not deleted)
                assertThat(afterApprovedDeletionCount, is(initialTimesheetCount - 2));
                // Verify approved timesheet is still in the list (not deleted)
                List<Integer> afterApprovedDeletionTimesheetList = new ArrayList<>();
                for (Map<String, Object> timesheet : afterApprovedDeletionDataList) {
                        int timesheetId = ((Number) timesheet.get("id")).intValue();
                        afterApprovedDeletionTimesheetList.add(timesheetId);
                }
                assertThat("Approved timesheet should still be in the list (not deleted)",
                                afterApprovedDeletionTimesheetList, hasItem(approvedTimesheetID));

                // Test 4: Delete REJECTED timesheet - should be deleted
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
                // Should have deleted rejected timesheet, but approved should still exist
                assertThat(afterRejectedDeletionCount, is(initialTimesheetCount - 3));

                // Verify deleted timesheet is not in the list
                List<Integer> afterRejectedDeletionTimesheetList = new ArrayList<>();
                for (Map<String, Object> timesheet : afterRejectedDeletionDataList) {
                        int timesheetId = ((Number) timesheet.get("id")).intValue();
                        afterRejectedDeletionTimesheetList.add(timesheetId);
                }
                assertThat("Deleted REJECTED timesheet should not be in the list",
                                afterRejectedDeletionTimesheetList, not(hasItem(rejectedTimesheetID)));
                // Verify approved timesheet is still in the list
                assertThat("Approved timesheet should still be in the list",
                                afterRejectedDeletionTimesheetList, hasItem(approvedTimesheetID));
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void deleteTimesheetWithInvalidTimesheetIDTest() {
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
        public void unauthorizedUserCannotDeleteTimesheetTest() {
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
        public void bulkDeleteTimesheetsTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
                enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

                Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

                JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
                List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");
                List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

                // Create 4 timesheets for different status scenarios
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

                // Create list of all timesheet IDs
                List<Integer> allTimesheetList = new ArrayList<>();
                for (Map<String, Object> timesheet : dataList) {
                        int timesheetId = ((Number) timesheet.get("id")).intValue();
                        allTimesheetList.add(timesheetId);
                }

                // Get timesheet IDs
                int openTimesheetID = getAllTimesheetsJsonPath.getInt("data[0].id");
                int submittedTimesheetID = getAllTimesheetsJsonPath.getInt("data[1].id");
                int rejectedTimesheetID = getAllTimesheetsJsonPath.getInt("data[2].id");
                int approvedTimesheetID = getAllTimesheetsJsonPath.getInt("data[3].id");

                assertThat(openTimesheetID > 0, is(true));
                assertThat(submittedTimesheetID > 0, is(true));
                assertThat(rejectedTimesheetID > 0, is(true));
                assertThat(approvedTimesheetID > 0, is(true));

                // 1. Keep first timesheet as OPEN (no action needed)

                // 2. Submit second timesheet (status = SUBMITTED)
                Response submittedTimeLogsResponse = getTimeSheetTimeLogs(submittedTimesheetID, albatrossAuthToken);
                assertThat(submittedTimeLogsResponse.statusCode(), is(200));
                JsonPath submittedTimeLogsJsonPath = submittedTimeLogsResponse.jsonPath();
                List<Map<String, Object>> submittedTimeLogs = submittedTimeLogsJsonPath.getList("data.timeLogs");
                List<TimeLog> submittedTimeLogsList = generateTimelogIDLists(submittedTimeLogs, submittedTimesheetID);
                SubmitTimeLogsRequest submitRequest = new SubmitTimeLogsRequest();
                submitRequest.setTimeLogs(submittedTimeLogsList);
                Response submitResponse = submitTimeLogsForTimesheet(submitRequest, albatrossAuthToken);
                assertThat(submitResponse.statusCode(), is(200));

                // 3. Reject third timesheet (status = REJECTED)
                Response rejectedTimeLogsResponse = getTimeSheetTimeLogs(rejectedTimesheetID, albatrossAuthToken);
                assertThat(rejectedTimeLogsResponse.statusCode(), is(200));
                JsonPath rejectedTimeLogsJsonPath = rejectedTimeLogsResponse.jsonPath();
                List<Map<String, Object>> rejectedTimeLogs = rejectedTimeLogsJsonPath.getList("data.timeLogs");
                List<TimeLog> rejectedTimeLogsList = generateTimelogIDLists(rejectedTimeLogs, rejectedTimesheetID);
                SubmitTimeLogsRequest submitRequestForRejected = new SubmitTimeLogsRequest();
                submitRequestForRejected.setTimeLogs(rejectedTimeLogsList);
                Response submitResponseForRejected = submitTimeLogsForTimesheet(submitRequestForRejected,
                                albatrossAuthToken);
                assertThat(submitResponseForRejected.statusCode(), is(200));
                ApproveTimesheetRequest rejectRequest = buildApproveTimesheetRequest(3, "Rejected for bulk delete test");
                Response rejectResponse = approveTimesheet(rejectedTimesheetID, rejectRequest, albatrossAuthToken);
                assertThat(rejectResponse.statusCode(), is(201));

                // 4. Approve fourth timesheet (status = APPROVED)
                Response approvedTimeLogsResponse = getTimeSheetTimeLogs(approvedTimesheetID, albatrossAuthToken);
                assertThat(approvedTimeLogsResponse.statusCode(), is(200));
                JsonPath approvedTimeLogsJsonPath = approvedTimeLogsResponse.jsonPath();
                List<Map<String, Object>> approvedTimeLogs = approvedTimeLogsJsonPath.getList("data.timeLogs");
                List<TimeLog> approvedTimeLogsList = generateTimelogIDLists(approvedTimeLogs, approvedTimesheetID);
                SubmitTimeLogsRequest submitRequestForApproved = new SubmitTimeLogsRequest();
                submitRequestForApproved.setTimeLogs(approvedTimeLogsList);
                Response submitResponseForApproved = submitTimeLogsForTimesheet(submitRequestForApproved,
                                albatrossAuthToken);
                assertThat(submitResponseForApproved.statusCode(), is(200));
                ApproveTimesheetRequest approveRequest = buildApproveTimesheetRequest(4);
                Response approveResponse = approveTimesheet(approvedTimesheetID, approveRequest, albatrossAuthToken);
                assertThat(approveResponse.statusCode(), is(201));

                // Bulk delete all 4 timesheets
                List<Integer> timesheetIds = Arrays.asList(openTimesheetID, submittedTimesheetID, rejectedTimesheetID,
                                approvedTimesheetID);
                Response bulkDeleteResponse = bulkDeleteTimesheets(timesheetIds, albatrossAuthToken);

                assertThat(bulkDeleteResponse.statusCode(), is(200));

                JsonPath bulkDeleteJsonPath = bulkDeleteResponse.jsonPath();
                assertThat(bulkDeleteJsonPath.getString("meta.message"), is("Timesheets deleted successfully"));
                assertThat(bulkDeleteJsonPath.getString("meta.responseType.context"), is("Request is successful"));

                Assert.assertNotNull(bulkDeleteJsonPath.getString("meta.requestUuid"),
                                "Request UUID should not be null");
                Assert.assertNotNull(bulkDeleteJsonPath.getString("meta.timestamp"),
                                "Timestamp should not be null");
                Assert.assertNull(bulkDeleteJsonPath.get("data"),
                                "Data should be null for bulk delete response");

                // Verify after bulk deletion
                Response getAllTimesheetsAfterDeletion = getAllTimesheets(jobId, candidateId, 1, 100,
                                albatrossAuthToken);
                assertThat(getAllTimesheetsAfterDeletion.statusCode(), is(200));
                JsonPath afterDeletionJsonPath = getAllTimesheetsAfterDeletion.jsonPath();
                List<Map<String, Object>> afterDeletionDataList = afterDeletionJsonPath.getList("data");

                int afterDeletionCount = (afterDeletionDataList != null) ? afterDeletionDataList.size() : 0;
                // Only approved timesheet should remain
                assertThat(afterDeletionCount, is(initialTimesheetCount - 3));

                // Create list of remaining timesheet IDs
                List<Integer> afterDeletionTimesheetList = new ArrayList<>();
                for (Map<String, Object> timesheet : afterDeletionDataList) {
                        int timesheetId = ((Number) timesheet.get("id")).intValue();
                        afterDeletionTimesheetList.add(timesheetId);
                }

                // Verify deleted timesheets are not in the list
                assertThat("Deleted OPEN timesheet should not be in the list", afterDeletionTimesheetList,
                                not(hasItem(openTimesheetID)));
                assertThat("Deleted SUBMITTED timesheet should not be in the list", afterDeletionTimesheetList,
                                not(hasItem(submittedTimesheetID)));
                assertThat("Deleted REJECTED timesheet should not be in the list", afterDeletionTimesheetList,
                                not(hasItem(rejectedTimesheetID)));

                // Verify only approved timesheet is present
                assertThat("Approved timesheet should still be in the list", afterDeletionTimesheetList,
                                hasItem(approvedTimesheetID));
                assertThat("After deletion, timesheet count should be initial count minus 3",
                                afterDeletionTimesheetList.size(), is(initialTimesheetCount - 3));
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void bulkDeleteTimesheetsWithInvalidIDsTest() {
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
        public void unauthorizedUserCannotBulkDeleteTimesheetsTest() {
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
                Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL,
                                albatrossAuthToken);
                int jobId = (Integer) testData[0];
                int candidateId = (Integer) testData[1];
                int candidateId2 = (Integer) testData[2];
                int candidateId3 = (Integer) testData[3];
                int userId = (Integer) testData[4];

                return new Object[][] {
                                { jobId, candidateId, userId, 2 },
                               { jobId, candidateId2, userId, 3 }
                };
        }
}