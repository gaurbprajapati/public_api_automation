package io.recruitcrm.contractStaffing.shiftBasedTimesheets.exportTimesheets;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.qa.api.util.Owner;

/**
 * Covers the "Removal of Yes Option For Breaks And Currency Enhancements in Timesheet Export" LLD:
 * payCurrency/billCurrency are new timesheetFields values on POST /timesheets/export.
 * Field names (payCurrency, billCurrency) and the request shape are confirmed from the LLD.
 * Exported column header text ("Pay Currency" / "Bill Currency") is inferred from this codebase's
 * existing Title-Case column-naming convention (e.g. "Pay Rate", "Total Hours") and is not yet
 * confirmed against the LLD - adjust if the real export renders different header text.
 */
@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class ExportTimesheetCurrencyFieldsTest extends ExportTimesheetBaseTest {

    private static final String PAY_CURRENCY_COLUMN = "Pay Currency";
    private static final String BILL_CURRENCY_COLUMN = "Bill Currency";
    private static final String TOTAL_HOURS_COLUMN = "Total Hours";
    private static final String EXPECTED_CURRENCY_CODE = "₹ INR";

    private String albatrossAuthToken;
    private String apiAuthToken;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        createRuleEngineTemplate(albatrossAuthToken);
        apiAuthToken = ThreadManager.getAccountApiKey();
    }

    private List<Integer> createExportableTimesheets(int jobId, int candidateId, int userId,
            int timesheetFrequency) {
        enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);
        return createTimesheetsForValidation(jobId, candidateId, timesheetFrequency, albatrossAuthToken);
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "currencyExportData", groups = {"contract_staffing", "nightly-build"})
    public void verifyExportIncludesPayAndBillCurrencyColumnsExcelTest(int jobId, int candidateId, int userId,
            int timesheetFrequency) throws IOException {
        List<Integer> timesheetIds = createExportableTimesheets(jobId, candidateId, userId, timesheetFrequency);

        JSONObject requestBody = createExportRequestBody(timesheetIds, FILE_FORMAT_EXCEL,
                DATA_TYPE_WITHOUT_SUBMITTED, false, false, true, true);
        Response response = callExportApi(requestBody, albatrossAuthToken);

        assertThat("Export with currency fields should return 200", response.statusCode(), is(200));
        byte[] fileBytes = response.asByteArray();
        validateFileResponse(fileBytes);

        List<String> headers = extractExcelColumnHeaders(fileBytes);
        int totalHoursIndex = headers.indexOf(TOTAL_HOURS_COLUMN);
        assertThat("Total Hours column should be present", totalHoursIndex, greaterThanOrEqualTo(0));
        assertThat("Pay Currency should immediately follow Total Hours",
                headers.indexOf(PAY_CURRENCY_COLUMN), is(totalHoursIndex + 1));
        assertThat("Bill Currency should immediately follow Pay Currency",
                headers.indexOf(BILL_CURRENCY_COLUMN), is(totalHoursIndex + 2));

        JSONArray jsonData = convertFileToJson(fileBytes, FILE_FORMAT_EXCEL);
        assertThat("Export should contain at least one record", jsonData.length(), greaterThan(0));
        for (int i = 0; i < jsonData.length(); i++) {
            JSONObject record = jsonData.getJSONObject(i);
            assertThat("Pay Currency value for record " + i, record.optString(PAY_CURRENCY_COLUMN, ""),
                    is(EXPECTED_CURRENCY_CODE));
            assertThat("Bill Currency value for record " + i, record.optString(BILL_CURRENCY_COLUMN, ""),
                    is(EXPECTED_CURRENCY_CODE));
        }
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "currencyExportData", groups = {"contract_staffing", "nightly-build"})
    public void verifyExportIncludesPayAndBillCurrencyColumnsCsvTest(int jobId, int candidateId, int userId,
            int timesheetFrequency) throws IOException {
        List<Integer> timesheetIds = createExportableTimesheets(jobId, candidateId, userId, timesheetFrequency);

        JSONObject requestBody = createExportRequestBody(timesheetIds, FILE_FORMAT_CSV,
                DATA_TYPE_WITHOUT_SUBMITTED, false, false, true, true);
        Response response = callExportApi(requestBody, albatrossAuthToken);

        assertThat("Export with currency fields should return 200", response.statusCode(), is(200));
        byte[] fileBytes = response.asByteArray();
        validateFileResponse(fileBytes);

        JSONArray jsonData = convertFileToJson(fileBytes, FILE_FORMAT_CSV);
        assertThat("Export should contain at least one record", jsonData.length(), greaterThan(0));
        for (int i = 0; i < jsonData.length(); i++) {
            JSONObject record = jsonData.getJSONObject(i);
            assertThat("Pay Currency column should be present for record " + i,
                    record.has(PAY_CURRENCY_COLUMN), is(true));
            assertThat("Bill Currency column should be present for record " + i,
                    record.has(BILL_CURRENCY_COLUMN), is(true));
            assertThat("Pay Currency value for record " + i, record.optString(PAY_CURRENCY_COLUMN, ""),
                    is(EXPECTED_CURRENCY_CODE));
            assertThat("Bill Currency value for record " + i, record.optString(BILL_CURRENCY_COLUMN, ""),
                    is(EXPECTED_CURRENCY_CODE));
        }
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "currencyExportData", groups = {"contract_staffing", "nightly-build"})
    public void verifyExportWithOnlyPayCurrencyFieldTest(int jobId, int candidateId, int userId,
            int timesheetFrequency) throws IOException {
        List<Integer> timesheetIds = createExportableTimesheets(jobId, candidateId, userId, timesheetFrequency);

        JSONObject requestBody = createExportRequestBody(timesheetIds, FILE_FORMAT_EXCEL,
                DATA_TYPE_WITHOUT_SUBMITTED, false, false, true, false);
        Response response = callExportApi(requestBody, albatrossAuthToken);

        assertThat("Export with only payCurrency should return 200", response.statusCode(), is(200));
        byte[] fileBytes = response.asByteArray();
        List<String> headers = extractExcelColumnHeaders(fileBytes);
        assertThat("Pay Currency column should be present", headers, hasItem(PAY_CURRENCY_COLUMN));
        assertThat("Bill Currency column should not be present when not requested",
                headers, not(hasItem(BILL_CURRENCY_COLUMN)));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "currencyExportData", groups = {"contract_staffing", "nightly-build"})
    public void verifyExportWithOnlyBillCurrencyFieldTest(int jobId, int candidateId, int userId,
            int timesheetFrequency) throws IOException {
        List<Integer> timesheetIds = createExportableTimesheets(jobId, candidateId, userId, timesheetFrequency);

        JSONObject requestBody = createExportRequestBody(timesheetIds, FILE_FORMAT_EXCEL,
                DATA_TYPE_WITHOUT_SUBMITTED, false, false, false, true);
        Response response = callExportApi(requestBody, albatrossAuthToken);

        assertThat("Export with only billCurrency should return 200", response.statusCode(), is(200));
        byte[] fileBytes = response.asByteArray();
        List<String> headers = extractExcelColumnHeaders(fileBytes);
        assertThat("Bill Currency column should be present", headers, hasItem(BILL_CURRENCY_COLUMN));
        assertThat("Pay Currency column should not be present when not requested",
                headers, not(hasItem(PAY_CURRENCY_COLUMN)));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "currencyExportData", groups = {"contract_staffing", "nightly-build"})
    public void verifyExportWithoutCurrencyFieldsExcludesColumnsTest(int jobId, int candidateId, int userId,
            int timesheetFrequency) throws IOException {
        List<Integer> timesheetIds = createExportableTimesheets(jobId, candidateId, userId, timesheetFrequency);

        // Baseline request - currency fields not opted into, matches every pre-existing export test's shape
        JSONObject requestBody = createExportRequestBody(timesheetIds, FILE_FORMAT_EXCEL,
                DATA_TYPE_WITHOUT_SUBMITTED, false, false);
        Response response = callExportApi(requestBody, albatrossAuthToken);

        assertThat("Baseline export should return 200", response.statusCode(), is(200));
        byte[] fileBytes = response.asByteArray();
        List<String> headers = extractExcelColumnHeaders(fileBytes);
        assertThat("Pay Currency column should not appear unless requested",
                headers, not(hasItem(PAY_CURRENCY_COLUMN)));
        assertThat("Bill Currency column should not appear unless requested",
                headers, not(hasItem(BILL_CURRENCY_COLUMN)));
    }

    @DataProvider(name = "currencyExportData", parallel = true)
    public Object[][] currencyExportData() {
        Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = (Integer) testData[0];
        int candidateId = (Integer) testData[1];
        int userId = (Integer) testData[4];

        return new Object[][] {
                { jobId, candidateId, userId, 2 }
        };
    }
}
