package io.recruitcrm.contractStaffing.shiftBasedTimesheets.exportTimesheets;

import com.qa.api.util.FileToJsonUtil;
import io.rcrm.api.pojo.albatross.contractStaffing.SubmitTimeLogsRequest;
import io.rcrm.api.pojo.albatross.contractStaffing.TimeLog;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class ExportTimesheetBaseTest extends ContractStaffingBaseTest {

    protected static final String DATA_TYPE_WITH_SUBMITTED = "withSubmittedTimesheets";
    protected static final String DATA_TYPE_WITHOUT_SUBMITTED = "withoutSubmittedTimesheets";
    protected static final String DATA_TYPE_EMPTY_TIMESHEETS = "withEmptyTimesheetsData";
    protected static final String WITHOUT_TIMESHEETID_FIELD = "withEmptyTimesheetsIDData";
    protected static final String INVALID_TIMESHEETID_FIELD = "withInvalidTimesheetID";
    protected static final String WITHOUT_TIMESHEET_PERIOD = "withEmptyTimesheetsPeriodData";
    protected static final String EXPORT_EACH_DAY_TRUE = "exportEachDay_True";
    protected static final String EXPORT_EACH_DAY_FALSE = "exportEachDay_False";
    protected static final String FILE_FORMAT_EXCEL = "Excel";
    protected static final String FILE_FORMAT_CSV = "csv";

    private static final double AMOUNT_TOLERANCE = 1.0;

    protected int getExpectedPayRate() {
        return 5000;
    }

    protected int getExpectedBillRate() {
        return 6000;
    }

    // ── Data preparation ──────────────────────────────────────────────────

    protected List<Integer> prepareTimesheetData(List<Integer> timesheetIds, String dataType, String authToken,
            boolean reimbursementAttachment, List<String> payable, List<String> billable, List<String> statuses) {
        List<Integer> result;
        if (DATA_TYPE_WITH_SUBMITTED.equalsIgnoreCase(dataType)) {
            submitTimeLogsForTimesheets(timesheetIds, authToken);
            result = timesheetIds;
        } else if (DATA_TYPE_EMPTY_TIMESHEETS.equalsIgnoreCase(dataType)) {
            result = new ArrayList<>();
        } else if (INVALID_TIMESHEETID_FIELD.equalsIgnoreCase(dataType)) {
            result = Arrays.asList(12312, 12312, 88998);
        } else if (WITHOUT_TIMESHEETID_FIELD.equalsIgnoreCase(dataType)) {
            result = timesheetIds;
        } else if (WITHOUT_TIMESHEET_PERIOD.equalsIgnoreCase(dataType)) {
            result = timesheetIds;
        } else {
            result = timesheetIds;
        }

        if (reimbursementAttachment && canAddReimbursementsForExport(dataType, result)) {
            addReimbursementsToTimesheets(result, authToken, payable, billable, statuses);
        }
        return result;
    }

    private boolean canAddReimbursementsForExport(String dataType, List<Integer> timesheetIds) {
        if (timesheetIds == null || timesheetIds.isEmpty()) {
            return false;
        }
        if (INVALID_TIMESHEETID_FIELD.equalsIgnoreCase(dataType)
                || DATA_TYPE_EMPTY_TIMESHEETS.equalsIgnoreCase(dataType)) {
            return false;
        }
        return true;
    }

    private void addReimbursementsToTimesheets(List<Integer> timesheetIds, String authToken, List<String> payables, List<String> billables, List<String> statuses) {
        int i=0;
        for (Integer timesheetId : timesheetIds) {
            int reimbursementId = createReimbursement("Expense for timesheet export", 40.00, "test.pdf", timesheetId, authToken);
            if(statuses.get(i).equalsIgnoreCase("approved")) {
                updateReimbursementStatus(timesheetId, reimbursementId, "approve", "Approved for timesheet export", authToken);
                updatePayableBillableForReimbursement(timesheetId, reimbursementId, authToken, payables.get(i).equalsIgnoreCase("yes") ? 1 : 0, billables.get(i).equalsIgnoreCase("yes") ? 1 : 0);
            }else if(statuses.get(i).equalsIgnoreCase("rejected")) {
                updateReimbursementStatus(timesheetId, reimbursementId, "reject", "Rejected for timesheet export", authToken);
            }
            i++;
        }
    }

    protected void submitTimeLogsForTimesheets(List<Integer> timesheetIds, String authToken) {
        if (timesheetIds == null || timesheetIds.isEmpty()) {
            return;
        }

        for (Integer timesheetId : timesheetIds) {
            Response timeLogsResponse = getTimeSheetTimeLogs(timesheetId, authToken);
            if (timeLogsResponse.statusCode() == 200) {
                JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
                List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");
                if (timeLogs != null && !timeLogs.isEmpty()) {
                    List<TimeLog> timeLogsList = generateTimelogIDLists(timeLogs, timesheetId);
                    SubmitTimeLogsRequest submitRequest = new SubmitTimeLogsRequest();
                    submitRequest.setTimeLogs(timeLogsList);
                    submitRequest.setTimeDetails(generateTimeDetailsFromTimeLogs(timeLogsList));
                    submitTimeLogsForTimesheet(submitRequest, authToken);
                }
            }
        }
    }

    // ── Export API ─────────────────────────────────────────────────────────

    protected Response callExportApi(JSONObject requestBody, String authToken) {
        return RestClient.doPost("JSON", timesheetBaseURL, "timesheets/export",
                authToken, null, true, requestBody);
    }

    protected JSONArray convertFileToJson(byte[] fileBytes, String fileFormat) throws IOException {
        if (FILE_FORMAT_EXCEL.equalsIgnoreCase(fileFormat) || "EXCEL".equalsIgnoreCase(fileFormat)) {
            return FileToJsonUtil.convertExcelToJson(fileBytes);
        } else {
            return FileToJsonUtil.convertCsvToJson(fileBytes);
        }
    }

    // ── Request body builders ─────────────────────────────────────────────

    protected JSONObject createExportRequestBody(List<Integer> timesheetIds, String fileFormat, String dataType,
            boolean exportEachDay, boolean reimbursementAttachment) {
        return createExportRequestBody(timesheetIds, fileFormat, dataType, exportEachDay, reimbursementAttachment,
                false, false);
    }

    protected JSONObject createExportRequestBody(List<Integer> timesheetIds, String fileFormat, String dataType,
            boolean exportEachDay, boolean reimbursementAttachment, boolean includePayCurrency,
            boolean includeBillCurrency) {
        JSONObject requestBody = new JSONObject();

        JSONArray timesheetIdsArray;
        if (timesheetIds != null && !timesheetIds.isEmpty()) {
            timesheetIdsArray = new JSONArray();
            for (Integer timesheetId : timesheetIds) {
                timesheetIdsArray.put(timesheetId);
            }
            requestBody.put("timesheetIds", timesheetIdsArray);
        } else if (DATA_TYPE_EMPTY_TIMESHEETS.equalsIgnoreCase(dataType)) {
            timesheetIdsArray = new JSONArray();
            requestBody.put("timesheetIds", timesheetIdsArray);
        }

        String format = fileFormat.toUpperCase();
        requestBody.put("fileFormat", "EXCEL".equals(format) ? "EXCEL" : "CSV");

        int maxRecords = (timesheetIds == null || timesheetIds.isEmpty()) ? 100
                : Math.min(timesheetIds.size(), 100);
        requestBody.put("maxRecords", maxRecords);

        requestBody.put("exportEachDay", exportEachDay);

        requestBody.put("includeReimbursements", reimbursementAttachment);

        requestBody.put("timesheetFields", createTimesheetFieldsArray(dataType, includePayCurrency, includeBillCurrency));

        requestBody.put("candidateFields", createCandidateFieldsArray());

        return requestBody;
    }

    protected JSONArray createTimesheetFieldsArray(String dataReq) {
        return createTimesheetFieldsArray(dataReq, false, false);
    }

    protected JSONArray createTimesheetFieldsArray(String dataReq, boolean includePayCurrency,
            boolean includeBillCurrency) {
        JSONArray timesheetFields = new JSONArray();
        String[] fields = {
                "timesheetId",
                "timesheetPeriod",
                "contractor",
                "timesheetStatusId",
                "jobName",
                "dealName",
                "jobDuration",
                "totalWorkTime",
                "totalOvertime",
                "totalTime",
                "payData",
                "billData",
                "payRate",
                "billRate",
                "approvedBy",
                "allApprovers",
                "addedOn",
                "addedBy",
                "updatedOn",
                "updatedBy",
                "workDays",
                "timesheetFrequency",
                "timesheetCompany",
                "workHours",
                "overtimeHours",
                "payStatus",
                "billStatus",
                "payoutPaidOn",
                "payoutNumber",
                "invoiceCreatedOn",
                "invoiceNumber",
                "breakIntervals",
                "timeLogRemarks"
        };
        for (String field : fields) {
            if (WITHOUT_TIMESHEETID_FIELD.equalsIgnoreCase(dataReq) && "timesheetId".equals(field)) {
                continue;
            }
            if (WITHOUT_TIMESHEET_PERIOD.equalsIgnoreCase(dataReq) && "timesheetPeriod".equals(field)) {
                continue;
            }
            timesheetFields.put(field);
            if ("totalTime".equals(field)) {
                if (includePayCurrency) {
                    timesheetFields.put("payCurrency");
                }
                if (includeBillCurrency) {
                    timesheetFields.put("billCurrency");
                }
            }
        }
        return timesheetFields;
    }

    protected List<String> extractExcelColumnHeaders(byte[] fileBytes) throws IOException {
        List<String> headers = new ArrayList<>();
        java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(fileBytes);
        try {
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(inputStream);
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(0);
            for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(cellIndex);
                headers.add(cell == null ? "" : cell.getStringCellValue().trim());
            }
        } finally {
            inputStream.close();
        }
        return headers;
    }

    protected JSONArray createCandidateFieldsArray() {
        JSONArray candidateFields = new JSONArray();
        String[] fields = {
                "srno",
                "candidatename",
                "firstname",
                "lastname",
                "languageskills",
                "ownerid",
                "genderid",
                "candidatedob",
                "age",
                "workexpyr",
                "relevantexperience",
                "currentsalary",
                "lastorganisation",
                "skill",
                "willingtorelocate",
                "salaryexpectation",
                "position",
                "address",
                "city",
                "locality",
                "country",
                "profilefacebook",
                "profiletwitter",
                "profilelinkedin",
                "profilegithub",
                "source",
                "currentstatus",
                "noticeperiod",
                "availablefrom",
                "emailid",
                "contactnumber",
                "email_opt_out",
                "slug",
                "resumeupdatedon",
                "resumeupdaterequestedon",
                "profilexing",
                "createdby",
                "createdon",
                "updatedon",
                "off_limit_status_id",
                "last_calllog_created_on",
                "last_email_sent_on",
                "last_communication_timestamp",
                "last_meeting_created_on",
                "last_message_sent_on",
                "state",
                "postal_code",
                "last_sms_sent_on"
        };
        for (String field : fields) {
            candidateFields.put(field);
        }
        return candidateFields;
    }

    // ── Error / file response validation ──────────────────────────────────

    protected void validateErrorResponse(Response response, String dataType) {
        if (DATA_TYPE_EMPTY_TIMESHEETS.equalsIgnoreCase(dataType)) {
            assertThat("API should return 400 for empty timesheets", response.statusCode(), is(400));
        } else {
            assertThat("API should return 404", response.statusCode(), is(404));
            assertThat("Response should not be null", response, notNullValue());

            JsonPath jsonPath = response.jsonPath();
            if (WITHOUT_TIMESHEET_PERIOD.equalsIgnoreCase(dataType)) {
                assertThat("errors[0].message should be 'timesheetPeriod is a mandatory field for timesheet export'",
                        jsonPath.get("errors[0].message"),
                        is("timesheetPeriod is a mandatory field for timesheet export"));
            } else {
                assertThat("errors[0].message should be 'No data found for the specified criteria'",
                        jsonPath.get("errors[0].message"), is("No data found for the specified criteria"));
            }
        }

    }

    protected void validateFileResponse(byte[] fileBytes) {
        assertThat("File should not be empty", fileBytes.length, greaterThan(0));
    }

    protected void printJsonResponse(String testcase, JSONArray jsonData, String fileFormat) {
    }

    protected boolean parseExportEachDayFlag(String exportEachDayFlag) {
        return EXPORT_EACH_DAY_TRUE.equals(exportEachDayFlag);
    }

    // ── Dynamic amount capture via evaluate API ───────────────────────────

    protected Map<Integer, double[]> evaluateAndCaptureAmounts(List<Integer> timesheetIds, String authToken) {
        Map<Integer, double[]> amounts = new LinkedHashMap<>();
        for (Integer id : timesheetIds) {
            JSONObject payload = new JSONObject();
            payload.put("timesheetId", id);
            Response resp = RestClient.doPost("JSON", timesheetBaseURL, "rule-engine/evaluate",
                    authToken, null, true, payload);
            if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                JsonPath json = resp.jsonPath();
                Double pay = json.getDouble("data.evaluationSummary.totalPayAmount");
                Double bill = json.getDouble("data.evaluationSummary.totalBillAmount");
                if (pay != null && bill != null) {
                    amounts.put(id, new double[]{pay, bill});
                }
            }
        }
        return amounts;
    }

    // ── Dynamic export validation (replaces JSON-file-based validation) ───

    protected void validateExportedDataDynamic(String testCase, JSONArray actualJson,
            JsonPath candidateJson, JsonPath jobJson, JsonPath dealJson,
            Map<Integer, double[]> expectedAmounts, String dataType, boolean exportEachDay) {

        assertThat("Export should have records for " + testCase, actualJson.length(), greaterThan(0));

        for (int i = 0; i < actualJson.length(); i++) {
            JSONObject record = actualJson.getJSONObject(i);

            if (record.has("Reimbursement Description")) {
                validateReimbursementRecordDynamic(record, testCase, i, candidateJson, jobJson);
            } else {
                int tsId = extractTimesheetIdFromExport(record);
                double[] amounts = (expectedAmounts != null) ? expectedAmounts.get(tsId) : null;
                validateTimesheetRecordDynamic(record, testCase, i, candidateJson, jobJson,
                        dealJson, amounts, dataType, exportEachDay);
            }
        }
    }

    private void validateTimesheetRecordDynamic(JSONObject record, String testCase, int recordIndex,
            JsonPath candidateJson, JsonPath jobJson, JsonPath dealJson,
            double[] expectedAmounts, String dataType, boolean exportEachDay) {

        assertThat("Timesheet ID should be present in " + testCase + " record " + recordIndex,
                record.has("Timesheet ID"), is(true));
        assertThat("Timesheet Period should be present in " + testCase + " record " + recordIndex,
                record.has("Timesheet Period"), is(true));

        if (DATA_TYPE_WITH_SUBMITTED.equalsIgnoreCase(dataType)) {
            assertThat("Timesheet Status should be Submitted in " + testCase + " record " + recordIndex,
                    record.optString("Timesheet Status"), is("Submitted"));
        }

        assertThat("Timesheet Frequency should not be empty in " + testCase + " record " + recordIndex,
                record.optString("Timesheet Frequency", ""), not(is("")));
        assertThat("Work Days should not be empty in " + testCase + " record " + recordIndex,
                record.optString("Work Days", ""), not(is("")));

        if (DATA_TYPE_WITH_SUBMITTED.equalsIgnoreCase(dataType)) {
            assertThat("Bill Rate should be " + getExpectedBillRate() + " in " + testCase + " record " + recordIndex,
                    normalizeValue(record.opt("Bill Rate")), is(String.valueOf(getExpectedBillRate())));
            assertThat("Pay Rate should be " + getExpectedPayRate() + " in " + testCase + " record " + recordIndex,
                    normalizeValue(record.opt("Pay Rate")), is(String.valueOf(getExpectedPayRate())));
        }

        if (expectedAmounts != null) {
            double expectedPay = expectedAmounts[0];
            double expectedBill = expectedAmounts[1];

            double actualPay = record.optDouble("Pay Amount", 0);
            double actualBill = record.optDouble("Bill Amount", 0);

            assertThat("Pay Amount should match evaluate result in " + testCase + " record " + recordIndex
                    + ". Expected: " + expectedPay + ", Actual: " + actualPay,
                    Math.abs(actualPay - expectedPay) <= AMOUNT_TOLERANCE, is(true));
            assertThat("Bill Amount should match evaluate result in " + testCase + " record " + recordIndex
                    + ". Expected: " + expectedBill + ", Actual: " + actualBill,
                    Math.abs(actualBill - expectedBill) <= AMOUNT_TOLERANCE, is(true));
        }

        if (DATA_TYPE_WITH_SUBMITTED.equalsIgnoreCase(dataType)) {
            Object totalWorkHours = record.opt("Total Work Hours");
            assertThat("Total Work Hours should not be null in " + testCase + " record " + recordIndex,
                    totalWorkHours, notNullValue());
            assertThat("Total Hours should not be null in " + testCase + " record " + recordIndex,
                    record.opt("Total Hours"), notNullValue());
        }

        validateEntityFields(record, testCase, recordIndex, candidateJson, jobJson, dealJson);

        assertTimesheetAddedOnIsToday(record, testCase, recordIndex);
        assertTimesheetUpdatedOnIsToday(record, testCase, recordIndex);

        if (exportEachDay) {
            validatePerDayColumnsPresent(record, testCase, recordIndex);
        }
    }

    private void validateEntityFields(JSONObject record, String testCase, int recordIndex,
            JsonPath candidateJson, JsonPath jobJson, JsonPath dealJson) {

        Set<String> actualKeys = record.keySet();

        // ── Timesheet-level fields — value assertions for known-present fields ──
        if (record.has("Contractor Name")) {
            assertThat("Contractor Name should not be null in " + testCase + " record " + recordIndex,
                    record.get("Contractor Name"), notNullValue());
            if (candidateJson != null) {
                String expectedContractorName = candidateJson.getString("first_name");
                if (expectedContractorName != null && !expectedContractorName.isEmpty()) {
                    assertThat("Contractor Name should contain candidate first_name in " + testCase + " record " + recordIndex,
                            record.optString("Contractor Name", ""), containsString(expectedContractorName));
                }
            }
        }

        if (record.has("Job Name")) {
            assertThat("Job Name should not be null in " + testCase + " record " + recordIndex,
                    record.get("Job Name"), notNullValue());
            if (jobJson != null) {
                String expectedJobName = jobJson.getString("name");
                if (expectedJobName != null && !expectedJobName.isEmpty()) {
                    assertThat("Job Name should match in " + testCase + " record " + recordIndex,
                            record.optString("Job Name", ""), is(expectedJobName));
                }
            }
        }

        if (dealJson != null && record.has("Deal Name")) {
            String expectedDealName = dealJson.getString("name");
            if (expectedDealName != null && !expectedDealName.isEmpty()) {
                assertThat("Deal Name should match in " + testCase + " record " + recordIndex,
                        record.optString("Deal Name", ""), is(expectedDealName));
            }
        }

        // ── Candidate field value matching (only when field exists) ─────
        if (candidateJson != null) {
            matchCandidateField(record, "Candidate First Name", candidateJson, "first_name", testCase, recordIndex);
            matchCandidateField(record, "Candidate Last Name", candidateJson, "last_name", testCase, recordIndex);
            matchCandidateField(record, "Email", candidateJson, "email", testCase, recordIndex);
            matchCandidateField(record, "Candidate Slug", candidateJson, "slug", testCase, recordIndex);
        }

        // ── Bulk column presence check — all expected columns in one assertion ──
        List<String> expectedColumns = getExpectedExportColumns();
        List<String> missingColumns = new ArrayList<>();
        for (String col : expectedColumns) {
            if (!actualKeys.contains(col)) {
                missingColumns.add(col);
            }
        }

        assertThat("Missing export columns in " + testCase + " record " + recordIndex
                + ". Actual columns: " + new ArrayList<>(actualKeys),
                missingColumns.isEmpty(), is(true));
    }

    private List<String> getExpectedExportColumns() {
        return Arrays.asList(
                "Timesheet ID", "Timesheet Period", "Timesheet Status",
                "Timesheet Frequency", "Work Days",
                "Bill Rate", "Pay Rate", "Pay Amount", "Bill Amount",
                "Total Work Hours", "Total Overtime Hours", "Total Hours",
                "Contractor Name", "Job Name", "Deal Name",
                "Job Duration", "Company Name",
                "Timesheet Approvers", "Timesheet Approved By",
                "Added By", "Updated By",
                "Timesheet Added On", "Timesheet Updated On",
                "Pay Status", "Bill Status",
                "Payout Paid On", "Payout Number",
                "Invoice Created On", "Invoice Number",
                "Candidate Name", "Candidate First Name", "Candidate Last Name",
                "Candidate Id", "Candidate Slug",
                "Email", "Phone",
                "Full Address", "City", "State", "Country", "Locality", "Postal Code",
                "Current Organisation", "Title / Position",
                "Skills", "Total Experience", "Relevant Experience",
                "Current Salary", "Salary Expectation",
                "Notice Period (Days)", "Willing to Relocate", "Available From",
                "Current Employment Status", "Language Skills",
                "Email Opt-out", "Source", "Off-Limit Status",
                "Gender", "Date of Birth", "Candidate Age",
                "Owner", "Created By", "Added On", "Updated On",
                "Facebook Profile", "Twitter Profile", "LinkedIn Profile", "GitHub Profile", "Xing Profile",
                "Profile Request Sent On", "Profile Updated By Candidate On",
                "Last Email Sent On", "Last Communication",
                "Last Call Log Added On", "Last Meeting Added",
                "Last LinkedIn Message Sent On", "Last SMS Sent On"
        );
    }

    private void matchCandidateField(JSONObject record, String exportField, JsonPath candidateJson,
            String jsonField, String testCase, int recordIndex) {
        if (!record.has(exportField)) return;
        String expected = candidateJson.getString(jsonField);
        if (expected != null && !expected.isEmpty()) {
            String actual = record.optString(exportField, "");
            assertThat(exportField + " should match candidate " + jsonField + " in " + testCase + " record " + recordIndex,
                    actual, is(expected));
        }
    }

    private void validatePerDayColumnsPresent(JSONObject record, String testCase, int recordIndex) {
        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        boolean hasAnyDayColumn = false;
        for (String dayName : dayNames) {
            for (String key : record.keySet()) {
                if (key.startsWith(dayName + ", ")) {
                    hasAnyDayColumn = true;
                    break;
                }
            }
            if (hasAnyDayColumn) break;
        }
        assertThat("Export should have per-day columns when exportEachDay=true in " + testCase + " record " + recordIndex,
                hasAnyDayColumn, is(true));
    }

    private void validateReimbursementRecordDynamic(JSONObject record, String testCase, int recordIndex,
            JsonPath candidateJson, JsonPath jobJson) {
        assertThat("Reimbursement Description should not be empty in " + testCase + " record " + recordIndex,
                record.optString("Reimbursement Description", ""), not(is("")));

        assertThat("Status should be present in reimbursement " + testCase + " record " + recordIndex,
                record.has("Status"), is(true));

        if (record.has("Amount")) {
            String rawAmount = record.optString("Amount", "").replaceAll("[^0-9.]", "");
            double parsedAmount = Double.parseDouble(rawAmount);
            assertThat("Reimbursement Amount should be 40 in " + testCase + " record " + recordIndex,
                    Math.abs(parsedAmount - 40.0) <= 0.01, is(true));
        }

        if (record.has("Payable")) {
            String payable = record.optString("Payable", "");
            assertThat("Payable should be Yes or No in " + testCase + " record " + recordIndex,
                    payable, anyOf(is("Yes"), is("No")));
        }
        if (record.has("Billable")) {
            String billable = record.optString("Billable", "");
            assertThat("Billable should be Yes or No in " + testCase + " record " + recordIndex,
                    billable, anyOf(is("Yes"), is("No")));
        }

        if (candidateJson != null && record.has("Contractor Name")) {
            String expectedName = candidateJson.getString("first_name");
            if (expectedName != null && !expectedName.isEmpty()) {
                assertThat("Contractor Name should match in reimbursement " + testCase + " record " + recordIndex,
                        record.optString("Contractor Name", ""), containsString(expectedName));
            }
        }
        if (jobJson != null && record.has("Job Name")) {
            String expectedJobName = jobJson.getString("name");
            if (expectedJobName != null && !expectedJobName.isEmpty()) {
                assertThat("Job Name should match in reimbursement " + testCase + " record " + recordIndex,
                        record.optString("Job Name", ""), is(expectedJobName));
            }
        }
    }

    private int extractTimesheetIdFromExport(JSONObject record) {
        String tsIdStr = record.optString("Timesheet ID", "");
        String[] parts = tsIdStr.split("-");
        if (parts.length >= 3) {
            try {
                return Integer.parseInt(parts[parts.length - 1]);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    // ── Date validation helpers ───────────────────────────────────────────

    private void assertTimesheetAddedOnIsToday(JSONObject actualRecord, String testCase, int recordIndex) {
        Object actualValue = actualRecord.opt("Timesheet Added On");

        assertThat("Field 'Timesheet Added On' should not be null in " + testCase + " record " + recordIndex,
                actualValue, notNullValue());

        assertExportDateIsCurrentOrPreviousDate("Timesheet Added On", normalizeValue(actualValue), testCase,
                recordIndex);
    }

    private void assertTimesheetUpdatedOnIsToday(JSONObject actualRecord, String testCase, int recordIndex) {
        Object actualValue = actualRecord.opt("Timesheet Updated On");

        assertThat("Field 'Timesheet Updated On' should not be null in " + testCase + " record " + recordIndex,
                actualValue, notNullValue());

        assertExportDateIsCurrentOrPreviousDate("Timesheet Updated On", normalizeValue(actualValue), testCase,
                recordIndex);
    }

    private void assertExportDateIsCurrentOrPreviousDate(String fieldName, String actualValue, String testCase,
            int recordIndex) {
        List<String> acceptedDates = Arrays.asList(getTodayDateInExportFormat(), getYesterdayDateInExportFormat());
        assertThat("Field '" + fieldName + "' should match current or previous export date in " + testCase
                + " record " + recordIndex + ". Expected one of: " + acceptedDates + ", Actual: " + actualValue,
                acceptedDates.contains(actualValue), is(true));
    }

    private String getTodayDateInExportFormat() {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy");
        return dateFormat.format(new java.util.Date());
    }

    private String getYesterdayDateInExportFormat() {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return dateFormat.format(calendar.getTime());
    }

    // ── Value normalization ───────────────────────────────────────────────

    private String normalizeValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Number) {
            Number num = (Number) value;
            if (num.doubleValue() == num.intValue()) {
                return String.valueOf(num.intValue());
            } else {
                return String.valueOf(num.doubleValue());
            }
        }
        return value.toString().trim();
    }
}
