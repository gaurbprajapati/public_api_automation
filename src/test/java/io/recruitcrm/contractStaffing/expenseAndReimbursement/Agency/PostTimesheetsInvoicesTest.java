package io.recruitcrm.contractStaffing.expenseAndReimbursement.Agency;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.pojo.albatross.contractStaffing.*;
import io.rcrm.api.pojo.invoiceService.TimesheetsInvoiceDataRequest;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class PostTimesheetsInvoicesTest extends ExpenseAndReimbursementBaseTest {

    private static final int INVALID_TEMPLATE_ID = 9_999_999;

    private String albatrossAuthToken;
    private String apiAuthToken;
    private int jobId;
    private int contractorId;
    private int userId;
    private int timesheetId;
    private int invoiceTemplateId;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplate(albatrossAuthToken);

        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        jobId = (Integer) testData[0];
        contractorId = (Integer) testData[1];
        userId = (Integer) testData[2];

        enableTimesheetHourBased(contractorId, jobId, userId, albatrossAuthToken, 2, 200, 1);

        List<Integer> timesheetIds = createSingleTimesheetForValidation(jobId, contractorId, 2, albatrossAuthToken);
        assertThat("timesheet id required", timesheetIds.isEmpty(), is(false));
        timesheetId = timesheetIds.get(0);
        invoiceTemplateId = function.getInvoiceTemplateId(invoiceServiceURL, albatrossAuthToken, "Contract Job");
    }

    @Test(dataProvider = "postTimesheetsInvoicesData")
    public void postTimesheetsInvoices_success(String description1, String description2, String description3) {
        TimesheetsInvoiceDataRequest request = TimesheetsInvoiceDataRequest.builder()
                .timesheetIds(Collections.singletonList(timesheetId))
                .templateId(invoiceTemplateId)
                .build();

        Response response = postTimesheetsInvoices(request, albatrossAuthToken);
        assertThat(response.getStatusCode(), is(200));

        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Timesheet Data for Default Template Fetched Successfully"));
        assertThat(jp.getInt("meta.status"), is(200));

        String invoiceItems = jp.getString("data.invoiceItems");
        assertThat(invoiceItems, containsString(description1));
        assertThat(invoiceItems, containsString(description2));
        assertThat(invoiceItems, containsString(description3));
    }

    @Test
    public void postTimesheetsInvoices_emptyTimesheetIds_returns400() {
        TimesheetsInvoiceDataRequest request = TimesheetsInvoiceDataRequest.builder()
                .timesheetIds(Collections.emptyList())
                .templateId(invoiceTemplateId)
                .build();

        Response response = postTimesheetsInvoices(request, albatrossAuthToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Timesheet Data for Default Template Fetched Successfully"));
        assertThat(jp.getInt("meta.status"), is(200));
        assertThat(jp.get("data.currencyId"), nullValue());
        assertThat(jp.get("data.invoiceItems"), is(""));
    }

    @Test
    public void postTimesheetsInvoices_unauthorized_returns401() {
        TimesheetsInvoiceDataRequest request = TimesheetsInvoiceDataRequest.builder()
                .timesheetIds(Collections.singletonList(timesheetId))
                .templateId(invoiceTemplateId)
                .build();

        Response response = postTimesheetsInvoices(request, "invalid_token_for_401");
        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().getString("meta.message"), is("Unauthorised access"));
    }

    @Test
    public void postTimesheetsInvoices_templateNotFound_returns404() {
        TimesheetsInvoiceDataRequest request = TimesheetsInvoiceDataRequest.builder()
                .timesheetIds(Collections.singletonList(timesheetId))
                .templateId(INVALID_TEMPLATE_ID)
                .build();

        Response response = postTimesheetsInvoices(request, albatrossAuthToken);
        assertInvoiceTimesheetsError(response, 404, "Invoice Template Not Found");
    }

    private void assertInvoiceTimesheetsError(Response response, int expectedStatus, String expectedMessage) {
        assertThat(response.getStatusCode(), is(expectedStatus));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("errors[0].message"), is(expectedMessage));
        assertThat(jp.getInt("meta.status"), is(expectedStatus));
        assertThat(jp.get("data"), nullValue());
    }

    @DataProvider(parallel = true)
    public Object[][] postTimesheetsInvoicesData() {
        int reimbursementId = createReimbursement("1 Lunch outside", 40.00, "test.pdf", timesheetId, albatrossAuthToken);
        int reimbursementId2 = createReimbursement("Travel expenses", 60.00, "test.pdf", timesheetId, albatrossAuthToken);
        int reimbursementId3 = createReimbursement("Hotel expenses", 80.00, "test.pdf", timesheetId, albatrossAuthToken);
        Response approve = updateReimbursementStatus(timesheetId, reimbursementId, "approve", null, albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));
        Response approve2 = updateReimbursementStatus(timesheetId, reimbursementId2, "approve", null, albatrossAuthToken);
        assertThat(approve2.getStatusCode(), is(200));
        Response approve3 = updateReimbursementStatus(timesheetId, reimbursementId3, "approve", null, albatrossAuthToken);
        assertThat(approve3.getStatusCode(), is(200));
        Response timeLogsResponse = getTimeSheetTimeLogs(timesheetId, albatrossAuthToken);
        assertThat(timeLogsResponse.statusCode(), is(200));
        JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
        List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");

        List<Map<String, Object>> timeLogsList = generateTimeLogIdsforHourBased(timeLogs, timesheetId);
        TimeDetails timeDetails = generateTimeDetailsForHourBased(timeLogs, timesheetId);

        Map<String, Object> submitTimeLogsRequest = new HashMap<>();
        submitTimeLogsRequest.put("isApproved", 0);
        submitTimeLogsRequest.put("timeLogs", timeLogsList);
        submitTimeLogsRequest.put("timeDetails", Arrays.asList(timeDetails));

        Response submitResponse = submitTimeLogsForTimesheetHourBased(submitTimeLogsRequest, albatrossAuthToken);
        assertThat(submitResponse.statusCode(), is(200));

        int randomApprovalStatus = 4;
        ApproveTimesheetRequest approveRequest = buildApproveTimesheetRequest(randomApprovalStatus,
                "Rejected by automated test");

        Response approveResponse = approveTimesheet(timesheetId, approveRequest, albatrossAuthToken);
        assertThat(approveResponse.statusCode(), is(201));
        return new Object[][] {
                { "1 Lunch outside", "Travel expenses", "Hotel expenses" },
        };
    }
}
