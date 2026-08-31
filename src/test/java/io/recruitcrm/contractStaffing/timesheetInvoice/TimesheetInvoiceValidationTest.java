package io.recruitcrm.contractStaffing.timesheetInvoice;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.contractStaffing.*;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class TimesheetInvoiceValidationTest extends ContractStaffingBaseTest {

        /** Intentionally non-existent timesheet id for validate-invoice negative path (single-id payload). */
        private static final int NON_EXISTENT_TIMESHEET_ID = 999999999;

        String albatrossAuthToken;
        String apiAuthToken;
        int ownerAccountID;
        commanFunction function;

        @BeforeClass(alwaysRun = true)
        public void Setup() {
                albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
                ownerAccountID = ThreadManager.getAccount().getAccountId();
                apiAuthToken = ThreadManager.getAccountApiKey();
                function = new commanFunction();
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
        public void validateInvoiceTimesheetsIdTest(String testCaseName, int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);
                Response response;

                if (testCaseName.equalsIgnoreCase("unAuthUser")) {
                        List<Integer> timesheetIds = createTimesheetsForValidation(jobId, candidateId,
                                        timesheetFrequency, albatrossAuthToken);
                        response = validateInvoiceTimesheetID(timesheetIds, albatrossAuthToken + "abc");
                } else if (testCaseName.equalsIgnoreCase("invalidTimesheetId")) {
                        // Single unknown id only — do not mix real timesheet ids from createTimesheetsForValidation.
                        List<Integer> invalidOnly = Collections.singletonList(NON_EXISTENT_TIMESHEET_ID);
                        response = validateInvoiceTimesheetID(invalidOnly, albatrossAuthToken);
                } else {
                        List<Integer> timesheetIds = createTimesheetsForValidation(jobId, candidateId,
                                        timesheetFrequency, albatrossAuthToken);
                        response = validateInvoiceTimesheetID(timesheetIds, albatrossAuthToken);
                }
                JsonPath jsonPath = response.jsonPath();
                if (testCaseName.equalsIgnoreCase("unAuthUser")) {
                        assertThat("Status code should be 401 for unauthorized access",
                                        response.statusCode(),
                                        is(401));
                        assertThat("Meta message should indicate unauthorized access",
                                        jsonPath.getString("meta.message"),
                                        is("Unauthorised access"));
                        assertThat("Response type context should be Warning",
                                        jsonPath.getString("meta.responseType.context"),
                                        is("Warning"));
                        assertThat("Data should contain invalid token message",
                                        jsonPath.getString("data"),
                                        is("Invalid token"));

                } else if (testCaseName.equalsIgnoreCase("weekly")) {
                        assertThat("Status code should be 200 for weekly timesheets",
                                        response.statusCode(),
                                        is(200));
                        assertThat("Meta message should match",
                                        jsonPath.getString("meta.message"),
                                        is("Timesheet validation completed successfully"));
                        assertThat("Response type context should be successful",
                                        jsonPath.getString("meta.responseType.context"),
                                        is("Request is successful"));
                        assertThat("timesheetInvoicePreviewData should not be empty",
                                        jsonPath.getList("data.timesheetInvoicePreviewData"),
                                        is(not(empty())));

                        assertThat("errorCount should be greater than 0",
                                        jsonPath.getInt("data.errorCount"),
                                        greaterThan(0));

                        assertThat("First timesheet should have error key 'not_approved'",
                                        jsonPath.getString("data.timesheetInvoicePreviewData[0].errorKey"),
                                        is("not_approved"));
                        assertThat("First timesheet approval status should be 1",
                                        jsonPath.getInt("data.timesheetInvoicePreviewData[0].timesheetApprovalStatusTypeId"),
                                        is(1));
                        assertThat("Currency ID should be 53 (INR)",
                                        jsonPath.getInt("data.timesheetInvoicePreviewData[0].currencyId"),
                                        is(53));
                        assertThat("Bill currency symbol should be ₹",
                                        jsonPath.getString("data.timesheetInvoicePreviewData[0].billCurrencySymbol"),
                                        is("₹"));
                        assertThat("Bill currency code should be INR",
                                        jsonPath.getString("data.timesheetInvoicePreviewData[0].billCurrencyCode"),
                                        is("INR"));
                        assertThat("Bill amount should be 0.0 (not approved)",
                                        jsonPath.getDouble("data.timesheetInvoicePreviewData[0].billAmount"),
                                        is(0.0));
                        assertThat("Contractor name should not be empty",
                                        jsonPath.getString("data.timesheetInvoicePreviewData[0].contractorName"),
                                        is(not(emptyOrNullString())));
                        assertThat("Job ID should be valid",
                                        jsonPath.getInt("data.timesheetInvoicePreviewData[0].jobId"),
                                        is(greaterThan(0)));

                } else if (testCaseName.equalsIgnoreCase("biweekly")) {
                        assertThat("Status code should be 200 for biweekly timesheets",
                                        response.statusCode(),
                                        is(200));
                        assertThat("Meta message should match",
                                        jsonPath.getString("meta.message"),
                                        is("Timesheet validation completed successfully"));
                        assertThat("timesheetInvoicePreviewData should not be empty",
                                        jsonPath.getList("data.timesheetInvoicePreviewData"),
                                        is(not(empty())));

                        assertThat("errorCount should be greater than 0",
                                        jsonPath.getInt("data.errorCount"),
                                        greaterThan(0));

                        for (int i = 0; i < 3; i++) {
                                assertThat("Timesheet " + i + " should have error key 'not_approved'",
                                                jsonPath.getString(
                                                                "data.timesheetInvoicePreviewData[" + i + "].errorKey"),
                                                is("not_approved"));
                                assertThat("Timesheet " + i + " bill amount should be 0.0",
                                                jsonPath.getDouble("data.timesheetInvoicePreviewData[" + i
                                                                + "].billAmount"),
                                                is(0.0));
                                assertThat("Timesheet " + i + " should have timesheet period",
                                                jsonPath.getMap("data.timesheetInvoicePreviewData[" + i
                                                                + "].timesheetPeriod"),
                                                is(notNullValue()));
                        }

                } else if (testCaseName.equalsIgnoreCase("monthly")) {
                        assertThat("Status code should be 200 for monthly timesheets",
                                        response.statusCode(),
                                        is(200));
                        assertThat("Meta message should match",
                                        jsonPath.getString("meta.message"),
                                        is("Timesheet validation completed successfully"));
                        assertThat("timesheetInvoicePreviewData should not be empty",
                                        jsonPath.getList("data.timesheetInvoicePreviewData"),
                                        is(not(empty())));

                        assertThat("errorCount should be greater than 0",
                                        jsonPath.getInt("data.errorCount"),
                                        greaterThan(0));

                        assertThat("First timesheet start date should not be null",
                                        jsonPath.getLong(
                                                        "data.timesheetInvoicePreviewData[0].timesheetPeriod.timesheetStartDate"),
                                        is(greaterThan(0L)));
                        assertThat("First timesheet end date should not be null",
                                        jsonPath.getLong(
                                                        "data.timesheetInvoicePreviewData[0].timesheetPeriod.timesheetEndDate"),
                                        is(greaterThan(0L)));
                        assertThat("Should have contractor associations",
                                        jsonPath.getList("data.timesheetInvoicePreviewData[0].associations.5"),
                                        is(notNullValue()));
                        assertThat("Should have job associations",
                                        jsonPath.getList("data.timesheetInvoicePreviewData[0].associations.4"),
                                        is(notNullValue()));

                } else if (testCaseName.equalsIgnoreCase("invalidTimesheetId")) {
                        assertThat("Status code should be 200 even with invalid ID",
                                        response.statusCode(),
                                        is(200));
                        assertThat("Meta message should match",
                                        jsonPath.getString("meta.message"),
                                        is("Timesheet validation completed successfully"));
                        assertThat("Unknown ids should not produce invoice preview rows",
                                        jsonPath.getList("data.timesheetInvoicePreviewData"),
                                        empty());
                        assertThat("errorCount should be 0 when no timesheets resolve for invoice preview",
                                        jsonPath.getInt("data.errorCount"),
                                        is(0));
                }

                if (!testCaseName.equalsIgnoreCase("unAuthUser")) {
                        assertThat("Request UUID should not be null",
                                        jsonPath.getString("meta.requestUuid"),
                                        is(notNullValue()));
                        assertThat("Timestamp should not be null",
                                        jsonPath.getString("meta.timestamp"),
                                        is(notNullValue()));
                }
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "validateInvoiceApprovedTimesheetsIdData", groups = {"contract_staffing", "nightly-build"})
        public void validateInvoiceApprovedTimesheetsIdTest(String testCaseName, int jobId, int candidateId, int userId,
                        int timesheetFrequency) {

                enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);
                int timesheetID = createTimesheetWithTimeLogs(jobId, candidateId, timesheetFrequency,
                                albatrossAuthToken);

                // Get second timesheet ID
                Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId, 1, 100,
                                albatrossAuthToken);
                assertThat(getAllTimesheetsResponse.statusCode(), is(200));
                JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse.jsonPath();
                int timesheetID2 = ((Number) getAllTimesheetsJsonPath.get("data[1].id")).intValue();

                // First timesheet must be approved for validateApprovedTimesheet assertions (4 = status only)
                ApproveTimesheetRequest approveRequest = buildApproveTimesheetRequest(4);
                Response approveResponse = approveTimesheet(timesheetID, approveRequest, albatrossAuthToken);
                assertThat(approveResponse.statusCode(), is(201));

                List<Integer> timesheetIDs = new ArrayList<>();
                timesheetIDs.add(timesheetID2);
                timesheetIDs.add(timesheetID);

                Response response = validateInvoiceTimesheetID(timesheetIDs, albatrossAuthToken);
                JsonPath jsonPath = response.jsonPath();

                // Validate response status and meta information
                validateInvoiceResponseMeta(response, jsonPath, true);

                // Validate data structure
                assertThat("timesheetInvoicePreviewData should not be empty",
                                jsonPath.getList("data.timesheetInvoicePreviewData"),
                                is(not(empty())));

                assertThat("errorCount should be greater than 0",
                                jsonPath.getInt("data.errorCount"),
                                greaterThan(0));

                // Find timesheets by their IDs
                int[] indices = findTimesheetIndicesByIDs(jsonPath, timesheetID, timesheetID2);
                int timesheet1Index = indices[0];
                int timesheet2Index = indices[1];

                assertThat("Timesheet ID " + timesheetID + " should be found", timesheet1Index, is(not(-1)));
                assertThat("Timesheet ID " + timesheetID2 + " should be found", timesheet2Index, is(not(-1)));

                // timesheetID was approved, timesheetID2 was not approved
                int approvedIndex = timesheet1Index; // timesheetID
                int notApprovedIndex = timesheet2Index; // timesheetID2

                // Validate NOT APPROVED Timesheet (timesheetID2)
                assertThat("Not approved timesheet should have valid timesheet ID",
                                jsonPath.getInt("data.timesheetInvoicePreviewData[" + notApprovedIndex
                                                + "].timesheetId"),
                                is(timesheetID2));
                validateNotApprovedTimesheet(jsonPath, notApprovedIndex, true);
                validateTimesheetCurrencyFields(jsonPath, notApprovedIndex);

                // Validate APPROVED Timesheet (timesheetID)
                assertThat("Approved timesheet should have valid timesheet ID",
                                jsonPath.getInt("data.timesheetInvoicePreviewData[" + approvedIndex + "].timesheetId"),
                                is(timesheetID));
                validateApprovedTimesheet(jsonPath, approvedIndex);
                validateTimesheetCurrencyFields(jsonPath, approvedIndex);

                // Common validations for both timesheets
                for (int i = 0; i < 2; i++) {
                        validateTimesheetCommonFields(jsonPath, i);
                        validateTimesheetAssociations(jsonPath, i);
                }

                // Validate that both timesheets belong to the same contractor
                assertThat("Both timesheets should belong to same contractor",
                                jsonPath.getInt("data.timesheetInvoicePreviewData[0].contractorId"),
                                is(jsonPath.getInt("data.timesheetInvoicePreviewData[1].contractorId")));
                assertThat("Both timesheets should belong to same job",
                                jsonPath.getInt("data.timesheetInvoicePreviewData[0].jobId"),
                                is(jsonPath.getInt("data.timesheetInvoicePreviewData[1].jobId")));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "validateInvoiceTimesheetsIdWithDifferentCompanyData", groups = {"contract_staffing", "nightly-build"})
        public void validateInvoiceTimesheetsIdWithDifferentCompanyTest(String testCaseName, int jobId, int candidateId,
                        int candidateId2, int jobId2, int userId, int timesheetFrequency) {

                // Enable timesheet and create timesheets for Job 1 with Candidate 1
                enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);
                int timesheetID = createTimesheetWithTimeLogs(jobId, candidateId, timesheetFrequency,
                                albatrossAuthToken);

                // Enable timesheet and create timesheets for Job 2 with Candidate 2
                enableTimesheet(candidateId2, jobId2, userId, albatrossAuthToken, timesheetFrequency, 200, 0);
                int timesheetID2 = createTimesheetWithTimeLogs(jobId2, candidateId2, timesheetFrequency,
                                albatrossAuthToken);

                if (testCaseName.equalsIgnoreCase("both_timesheet_approved")) {
                        approveTimesheet(timesheetID, albatrossAuthToken);
                        approveTimesheet(timesheetID2, albatrossAuthToken);
                } else {
                        approveTimesheet(timesheetID, albatrossAuthToken);
                }

                List<Integer> timesheetIDs = new ArrayList<>();
                timesheetIDs.add(timesheetID2);
                timesheetIDs.add(timesheetID);

                Response response = validateInvoiceTimesheetID(timesheetIDs, albatrossAuthToken);

                JsonPath jsonPath = response.jsonPath();

                System.out.println("Response of : "+ testCaseName+ jsonPath.prettyPrint());

                // Common validations for successful responses
                validateInvoiceResponseMeta(response, jsonPath, true);

                // Validate data structure
                assertThat("timesheetInvoicePreviewData should not be empty",
                                jsonPath.getList("data.timesheetInvoicePreviewData"),
                                is(not(empty())));

                // Find timesheets by their IDs
                int[] indices = findTimesheetIndicesByIDs(jsonPath, timesheetID, timesheetID2);
                int timesheet1Index = indices[0];
                int timesheet2Index = indices[1];

                assertThat("Timesheet ID " + timesheetID + " should be found", timesheet1Index, is(not(-1)));
                assertThat("Timesheet ID " + timesheetID2 + " should be found", timesheet2Index, is(not(-1)));

                // Test case specific validations
                if (testCaseName.equalsIgnoreCase("both_timesheet_approved")) {

                        assertThat("errorCount should be greater than 0",
                                        jsonPath.getInt("data.errorCount"),
                                        greaterThan(0));

                        // Find which timesheet has which error
                        int[] errorIndices = getErrorIndices(jsonPath, timesheet1Index, timesheet2Index);
                        int differentCompanyIndex = errorIndices[0];
                        int notApprovedIndex = errorIndices[1];

                        // Validate different_company error timesheet
                        if (differentCompanyIndex != -1) {
                                validateDifferentCompanyTimesheet(jsonPath, differentCompanyIndex);
                        }

                        // Validate not_approved error timesheet (if exists)
                        if (notApprovedIndex != -1) {
                                validateNotApprovedTimesheet(jsonPath, notApprovedIndex, false);
                        }

                } else if (testCaseName.equalsIgnoreCase("timesheetPartialApproved")) {
                        // One approved, one not approved - both from different companies

                        assertThat("errorCount should be greater than 0",
                                        jsonPath.getInt("data.errorCount"),
                                        greaterThan(0));

                        // Find which timesheet is approved and which is not
                        int[] errorIndices = getErrorIndices(jsonPath, timesheet1Index, timesheet2Index);
                        int differentCompanyIndex = errorIndices[0];
                        int notApprovedIndex = errorIndices[1];
                        int approvedIndex = errorIndices[2];

                        // Validate approved timesheet (if exists)
                        if (approvedIndex != -1) {
                                validateApprovedTimesheet(jsonPath, approvedIndex);
                        }

                        // Validate not approved timesheet
                        if (notApprovedIndex != -1) {
                                validateNotApprovedTimesheet(jsonPath, notApprovedIndex, false);
                        }

                        // Validate different company timesheet
                        if (differentCompanyIndex != -1) {
                                validateDifferentCompanyTimesheet(jsonPath, differentCompanyIndex);
                        }

                } else if (testCaseName.equalsIgnoreCase("both_timesheet_NotApproved")) {
                        // Both not approved and from different companies - both have errors

                        assertThat("errorCount should be greater than 0",
                                        jsonPath.getInt("data.errorCount"),
                                        greaterThan(0));

                        // Find which timesheet has which error
                        int[] errorIndices = getErrorIndices(jsonPath, timesheet1Index, timesheet2Index);
                        int differentCompanyIndex = errorIndices[0];
                        int notApprovedIndex = errorIndices[1];

                        // Validate different_company error timesheet
                        if (differentCompanyIndex != -1) {
                                validateDifferentCompanyTimesheet(jsonPath, differentCompanyIndex);
                        }

                        // Validate not approved timesheet
                        if (notApprovedIndex != -1) {
                                validateNotApprovedTimesheet(jsonPath, notApprovedIndex, false);
                        }
                }

                // Common validations for both timesheets
                for (int i = 0; i < 2; i++) {
                        validateTimesheetCommonFields(jsonPath, i);
                        assertThat("Timesheet " + i + " approval status type ID should be valid",
                                        jsonPath.getInt("data.timesheetInvoicePreviewData[" + i
                                                        + "].timesheetApprovalStatusTypeId"),
                                        is(greaterThan(0)));
                        validateTimesheetAssociations(jsonPath, i);
                }

                // Validate that timesheets belong to different companies
                validateDifferentCompanies(jsonPath, 0, 1);

        }

        /**
         * One row of invoice validation data: primary job/candidate from {@link #createContractStaffingTestData}
         * plus a second company with its own job and candidate. Each call returns a fresh pair so data provider rows
         * do not share candidates.
         *
         * @return {@code [jobId, candidateIdFirst, candidateIdSecond, jobId2, userId]}
         */
        private Object[] buildValidateInvoiceDifferentCompanyRow() {
                Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL,
                                albatrossAuthToken);
                int jobId = ((Number) testData[0]).intValue();
                int candidateIdFirst = ((Number) testData[1]).intValue();
                int userId = ((Number) testData[4]).intValue();

                JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken)
                                .jsonPath();
                String candidateSlug = jsonCandidate.getString("slug");

                JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
                String companySlug = jsonCompany.getString("slug");

                JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
                String contactSlug = jsonContact.getString("slug");

                JsonPath jsonJob = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
                String jobSlug = jsonJob.getString("slug");

                function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug, jobSlug);
                int candidateIdSecond = function
                                .getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug, "candidate")
                                .jsonPath().getInt("data.candidate.id");

                int jobId2 = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job").jsonPath()
                                .getInt("data.job.id");

                return new Object[] { jobId, candidateIdFirst, candidateIdSecond, jobId2, userId };
        }

        @DataProvider(parallel = true)
        public Object[][] validateInvoiceTimesheetsIdWithDifferentCompanyData() {
                Object[] scenario1 = buildValidateInvoiceDifferentCompanyRow();
                int jobId1 = ((Number) scenario1[0]).intValue();
                int candidateId1 = ((Number) scenario1[1]).intValue();
                int candidateId2 = ((Number) scenario1[2]).intValue();
                int jobId1OtherCompany = ((Number) scenario1[3]).intValue();
                int userId1 = ((Number) scenario1[4]).intValue();

                Object[] scenario2 = buildValidateInvoiceDifferentCompanyRow();
                int jobId2 = ((Number) scenario2[0]).intValue();
                int candidateId3 = ((Number) scenario2[1]).intValue();
                int candidateId4 = ((Number) scenario2[2]).intValue();
                int jobId2OtherCompany = ((Number) scenario2[3]).intValue();
                int userId2 = ((Number) scenario2[4]).intValue();

                Object[] scenario3 = buildValidateInvoiceDifferentCompanyRow();
                int jobId3 = ((Number) scenario3[0]).intValue();
                int candidateId5 = ((Number) scenario3[1]).intValue();
                int candidateId6 = ((Number) scenario3[2]).intValue();
                int jobId3OtherCompany = ((Number) scenario3[3]).intValue();
                int userId3 = ((Number) scenario3[4]).intValue();

                return new Object[][] {
                                { "both_timesheet_approved", jobId1, candidateId1, candidateId2, jobId1OtherCompany,
                                                userId1, 2 },
                                { "both_timesheet_NotApproved", jobId2, candidateId3, candidateId4,
                                                jobId2OtherCompany, userId2, 3 },
                                { "timesheetPartialApproved", jobId3, candidateId5, candidateId6, jobId3OtherCompany,
                                                userId3, 2 }
                };
        }

        @DataProvider(parallel = true)
        public Object[][] validateInvoiceApprovedTimesheetsIdData() {
                Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL,
                                albatrossAuthToken);
                int jobId = ((Number) testData[0]).intValue();
                int candidateId = ((Number) testData[1]).intValue();
                int candidateId2 = ((Number) testData[2]).intValue();
                int candidateId3 = ((Number) testData[3]).intValue();
                int userId = ((Number) testData[4]).intValue();

                return new Object[][] {
                                { "weekly", jobId, candidateId, userId, 2 }
                };
        }

        @DataProvider(parallel = true)
        public Object[][] testTimesheetSettingsData() {
                Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL,
                                albatrossAuthToken);
                int jobId = ((Number) testData[0]).intValue();
                int candidateId = ((Number) testData[1]).intValue();
                int candidateId2 = ((Number) testData[2]).intValue();
                int candidateId3 = ((Number) testData[3]).intValue();
                int userId = ((Number) testData[4]).intValue();

                return new Object[][] {
                                { "weekly", jobId, candidateId, userId, 2 },
                                { "biweekly", jobId, candidateId2, userId, 3 },
                              { "monthly", jobId, candidateId3, userId, 4 },
                             { "unAuthUser", jobId, candidateId3, userId, 4 },
                                { "invalidTimesheetId", jobId, candidateId3, userId, 4 }
                };
        }
}
