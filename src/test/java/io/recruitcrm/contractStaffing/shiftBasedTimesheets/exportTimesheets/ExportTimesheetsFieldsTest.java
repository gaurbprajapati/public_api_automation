package io.recruitcrm.contractStaffing.shiftBasedTimesheets.exportTimesheets;

import com.qa.api.util.Owner;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.PrivateApiCommonFunctions;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class ExportTimesheetsFieldsTest extends ExportTimesheetBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;
    private String email;
    private PrivateApiCommonFunctions privateApiCommonFunctions = new PrivateApiCommonFunctions();
    private int userId;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        createRuleEngineTemplate(albatrossAuthToken);
        apiAuthToken = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        userId = ThreadManager.getOwner().getUserId();
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void exportTimesheetsTest(String testcase, int jobId, int candidateId, int userId,
            int timesheetFrequency,
            String formatType,
            String fileFormat, String dataType, String exportEachDayFlag, JsonPath candidateJson, JsonPath jobJson,
            JsonPath dealJson, boolean reimbursementAttachment, List<String> payable, List<String> billable, List<String> statuses) throws IOException {

        if (formatType.contains("24")) {
                privateApiCommonFunctions.updateUserTimezone(albatrossURL, albatrossAuthToken, userId, 1);
        } else if (formatType.contains("12")) {
                privateApiCommonFunctions.updateUserTimezone(albatrossURL, albatrossAuthToken, userId, 0);
        }
        if(reimbursementAttachment) {
                enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 1);
        } else {
            enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);
        }
        List<Integer> timesheetIds = createTimesheetsForValidation(jobId, candidateId, timesheetFrequency,
                albatrossAuthToken);

        boolean exportEachDay = parseExportEachDayFlag(exportEachDayFlag);
        List<Integer> exportTimesheetIds = prepareTimesheetData(timesheetIds, dataType, albatrossAuthToken,
                reimbursementAttachment, payable, billable, statuses);

        Map<Integer, double[]> expectedAmounts = null;
        if (DATA_TYPE_WITH_SUBMITTED.equalsIgnoreCase(dataType)) {
            expectedAmounts = evaluateAndCaptureAmounts(timesheetIds, albatrossAuthToken);
        }

        JSONObject requestBody = createExportRequestBody(exportTimesheetIds, fileFormat, dataType, exportEachDay,
                reimbursementAttachment);
        Response response = callExportApi(requestBody, albatrossAuthToken);

        if (INVALID_TIMESHEETID_FIELD.equalsIgnoreCase(dataType) || DATA_TYPE_EMPTY_TIMESHEETS.equalsIgnoreCase(dataType)
                || WITHOUT_TIMESHEETID_FIELD.equalsIgnoreCase(dataType)
                || WITHOUT_TIMESHEET_PERIOD.equalsIgnoreCase(dataType)) {
            validateErrorResponse(response, dataType);
        } else {
            assertThat("Export API should return 200 for " + testcase,
                    response.statusCode(), is(200));
            byte[] fileBytes = response.asByteArray();
            validateFileResponse(fileBytes);
            JSONArray jsonData = convertFileToJson(fileBytes, fileFormat);
            validateExportedDataDynamic(testcase, jsonData, candidateJson, jobJson, dealJson,
                    expectedAmounts, dataType, exportEachDay);
        }
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "testTimesheetSettingsData12HourBased", groups = {"contract_staffing", "nightly-build"})
    public void exportTimesheets12HourBaseTest(String testcase, int jobId, int candidateId, int userId,
            int timesheetFrequency,
            String formatType,
            String fileFormat, String dataType, String exportEachDayFlag, JsonPath candidateJson, JsonPath jobJson,
            JsonPath dealJson, boolean reimbursementAttachment, List<String> payable, List<String> billable, List<String> statuses) throws IOException {

        if (formatType.contains("24")) {
                privateApiCommonFunctions.updateUserTimezone(albatrossURL, albatrossAuthToken, userId, 1);
        } else if (formatType.contains("12")) {
                privateApiCommonFunctions.updateUserTimezone(albatrossURL, albatrossAuthToken, userId, 0);
        }
        if(reimbursementAttachment) {
            enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 1);
        } else {
            enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);
        }
        List<Integer> timesheetIds = createTimesheetsForValidation(jobId, candidateId, timesheetFrequency,
                albatrossAuthToken);

        boolean exportEachDay = parseExportEachDayFlag(exportEachDayFlag);
        List<Integer> exportTimesheetIds = prepareTimesheetData(timesheetIds, dataType, albatrossAuthToken,
                reimbursementAttachment, payable, billable, statuses);

        Map<Integer, double[]> expectedAmounts = null;
        if (DATA_TYPE_WITH_SUBMITTED.equalsIgnoreCase(dataType)) {
            expectedAmounts = evaluateAndCaptureAmounts(timesheetIds, albatrossAuthToken);
        }

        JSONObject requestBody = createExportRequestBody(exportTimesheetIds, fileFormat, dataType, exportEachDay,
                reimbursementAttachment);
        Response response = callExportApi(requestBody, albatrossAuthToken);

        if (INVALID_TIMESHEETID_FIELD.equalsIgnoreCase(dataType) || DATA_TYPE_EMPTY_TIMESHEETS.equalsIgnoreCase(dataType)
                || WITHOUT_TIMESHEETID_FIELD.equalsIgnoreCase(dataType)
                || WITHOUT_TIMESHEET_PERIOD.equalsIgnoreCase(dataType)) {
            validateErrorResponse(response, dataType);
        } else {
            assertThat("Export API should return 200 for " + testcase,
                    response.statusCode(), is(200));
            byte[] fileBytes = response.asByteArray();
            validateFileResponse(fileBytes);
            JSONArray jsonData = convertFileToJson(fileBytes, fileFormat);
            validateExportedDataDynamic(testcase, jsonData, candidateJson, jobJson, dealJson,
                    expectedAmounts, dataType, exportEachDay);
        }
    }

    @DataProvider(parallel = true)
    public Object[][] testTimesheetSettingsData() {
        return new Object[][] {
                buildTestRow("test1", 2, "24hrs", FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),
                buildTestRow("test2", 2, "24hrs", FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_FALSE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),
                buildTestRow("test3", 4, "24hrs", FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITHOUT_SUBMITTED, EXPORT_EACH_DAY_TRUE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),
                buildTestRow("test4", 2, "24hrs", FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITHOUT_SUBMITTED, EXPORT_EACH_DAY_FALSE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),

                buildTestRow("test5", 2, "24hrs", FILE_FORMAT_CSV,
                         DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),
               buildTestRow("test6", 2, "24hrs", FILE_FORMAT_CSV,
                       DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_FALSE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),
                buildTestRow("test7", 2, "24hrs", FILE_FORMAT_CSV,
                        DATA_TYPE_WITHOUT_SUBMITTED, EXPORT_EACH_DAY_TRUE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),
                buildTestRow("test8", 2, "24hrs", FILE_FORMAT_CSV,
                        DATA_TYPE_WITHOUT_SUBMITTED, EXPORT_EACH_DAY_FALSE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),

                buildTestRow("test9", 3, "24hrs", FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),
                buildTestRow("test10", 3, "24hrs", FILE_FORMAT_CSV,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),

                buildTestRow("test11", 4, "24hrs", FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),
                buildTestRow("test12", 4, "24hrs", FILE_FORMAT_CSV,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),

                 buildTestRow("test16", 2, "24hrs", FILE_FORMAT_EXCEL,
                         DATA_TYPE_EMPTY_TIMESHEETS, EXPORT_EACH_DAY_FALSE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),

                 buildTestRow("test16", 2, "24hrs", FILE_FORMAT_CSV,
                         WITHOUT_TIMESHEET_PERIOD, EXPORT_EACH_DAY_TRUE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),
                 buildTestRow("test17", 2, "24hrs", FILE_FORMAT_CSV,
                         INVALID_TIMESHEETID_FIELD, EXPORT_EACH_DAY_TRUE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),

                buildTestRow("test14", 2, "24hrs", FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE, true, Arrays.asList("yes", "no", "yes"), Arrays.asList("yes", "yes", "no"), Arrays.asList("approved", "approved", "approved")),
                buildTestRow("test15", 2, "24hrs", FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE, true, Arrays.asList("yes", "no", "no"), Arrays.asList("yes", "no", "no"), Arrays.asList("approved", "pending", "rejected")),
//                Developer missing validation, uncomment test case when it is fixed
//                 buildTestRow("test16", 2, "24hrs", FILE_FORMAT_CSV,
//                         WITHOUT_TIMESHEETID_FIELD, EXPORT_EACH_DAY_TRUE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),
        };
    }

    @DataProvider(parallel = true)
    public Object[][] testTimesheetSettingsData12HourBased() {
        return new Object[][] {
                buildTestRow("test13", 2, "12hrs", FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE, false, Arrays.asList(), Arrays.asList(), Arrays.asList()),

        };
    }


    private Object[] buildTestRow(String testCase, int timesheetFrequency, String formatType, String fileFormat,
            String dataType, String exportEachDayFlag, boolean reimbursementAttachment, List<String> payable, List<String> billable, List<String> statuses) {
        Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL,
                albatrossAuthToken);
        int jobId = (Integer) testData[0];
        int candidateId = (Integer) testData[1];
        int userId = (Integer) testData[4];
        JsonPath candidateJson = (JsonPath) testData[11];
        JsonPath jobJson = (JsonPath) testData[14];
        JsonPath dealJson = (JsonPath) testData[15];

        return new Object[] {
                testCase,
                jobId,
                candidateId,
                userId,
                timesheetFrequency,
                formatType,
                fileFormat,
                dataType,
                exportEachDayFlag,
                candidateJson,
                jobJson,
                dealJson,
                reimbursementAttachment,
                payable,
                billable,
                statuses
        };
    }
}
