package io.recruitcrm.contractStaffing.hourBasedTimeSheets.exportTimeSheets;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class ExportTimeSheetsHourBaseTest extends ExportTimeSheetBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
        apiAuthToken = ThreadManager.getAccountApiKey();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "testTimesheetSettingsDataHourBased", groups = {"contract_staffing", "nightly-build"})
    public void exportTimesheetsHourBaseTest(String testcase, int jobId, int candidateId, int userId,
            int timesheetFrequency,
            String fileFormat, String dataType, String exportEachDayFlag, JsonPath candidateJson, JsonPath jobJson,
            JsonPath dealJson) throws IOException {

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);
        List<Integer> timesheetIds = createTimesheetsForValidation(jobId, candidateId, timesheetFrequency,
                albatrossAuthToken);

        boolean exportEachDay = parseExportEachDayFlag(exportEachDayFlag);
        List<Integer> exportTimesheetIds = prepareTimesheetData(timesheetIds, dataType, albatrossAuthToken);

        Map<Integer, double[]> expectedAmounts = null;
        if (DATA_TYPE_WITH_SUBMITTED.equalsIgnoreCase(dataType)) {
            expectedAmounts = evaluateAndCaptureAmounts(timesheetIds, albatrossAuthToken);
        }

        JSONObject requestBody = createExportRequestBody(exportTimesheetIds, fileFormat, dataType, exportEachDay);
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
    public Object[][] testTimesheetSettingsDataHourBased() {
        return new Object[][] {
                buildTestRow("test1", 2, FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE),
                buildTestRow("test2", 2, FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_FALSE),
                buildTestRow("test5", 2, FILE_FORMAT_CSV,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE),
                buildTestRow("test6", 2, FILE_FORMAT_CSV,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_FALSE),
                buildTestRow("test9", 3, FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE),
                buildTestRow("test10", 3, FILE_FORMAT_CSV,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE),
                buildTestRow("test3", 3, FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_FALSE),
                buildTestRow("test4", 3, FILE_FORMAT_CSV,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_FALSE),
                buildTestRow("test11", 4, FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE),
                buildTestRow("test12", 4, FILE_FORMAT_CSV,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_TRUE),
                buildTestRow("test7", 4, FILE_FORMAT_EXCEL,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_FALSE),
                buildTestRow("test8", 4, FILE_FORMAT_CSV,
                        DATA_TYPE_WITH_SUBMITTED, EXPORT_EACH_DAY_FALSE),

                buildTestRow("test13", 2, FILE_FORMAT_EXCEL,
                        DATA_TYPE_EMPTY_TIMESHEETS, EXPORT_EACH_DAY_FALSE),

                buildTestRow("test14", 2, FILE_FORMAT_CSV,
                        WITHOUT_TIMESHEET_PERIOD, EXPORT_EACH_DAY_TRUE),
                buildTestRow("test15", 2, FILE_FORMAT_CSV,
                        INVALID_TIMESHEETID_FIELD, EXPORT_EACH_DAY_TRUE),

                // Developer missing validation, uncomment test case when it is fixed
                // buildTestRow("test16", 2, FILE_FORMAT_CSV,
                //         WITHOUT_TIMESHEETID_FIELD, EXPORT_EACH_DAY_TRUE),
        };
    }

    private Object[] buildTestRow(String testCase, int timesheetFrequency, String fileFormat,
            String dataType, String exportEachDayFlag) {
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
                fileFormat,
                dataType,
                exportEachDayFlag,
                candidateJson,
                jobJson,
                dealJson
        };
    }
}
