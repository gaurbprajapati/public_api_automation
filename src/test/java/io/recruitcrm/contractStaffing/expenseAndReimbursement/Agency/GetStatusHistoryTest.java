package io.recruitcrm.contractStaffing.expenseAndReimbursement.Agency;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.ContractStaffing.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class GetStatusHistoryTest extends ContractStaffingBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
    }

    private static final String WORKFLOW_REJECT_REMARK = "Rejected for status-history workflow";
    private static final String WORKFLOW_REOPEN_REMARK = "Reopened for contractor correction";

    @Test(dataProvider = "reimbursementSubmittedRejectReopenApproveData")
    public void getReimbursementStatusHistory_Test(int timesheetID, int reimbursementID) {
        Response response = getReimbursementStatusHistory(timesheetID, reimbursementID, albatrossAuthToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Status history fetched successfully"));
        assertThat(jp.getInt("meta.status"), is(200));
        Object responseType = jp.get("meta.responseType");
        if (responseType instanceof String) {
            assertThat((String) responseType, is("SUCCESS"));
        }

        assertThat(jp.getInt("data[3].status"), is(1));
        assertThat(jp.getString("data[3].statusLabel"), is("Submitted"));
        assertThat(jp.getInt("data[0].status"), is(3));
        assertThat(jp.getString("data[0].statusLabel"), is("Rejected"));
        assertThat(jp.getString("data[0].remark"), is(WORKFLOW_REJECT_REMARK));
        assertThat(jp.getInt("data[1].status"), is(1));
        assertThat(jp.getString("data[1].statusLabel"), is("Submitted"));
        assertThat(jp.getString("data[1].remark"), is(WORKFLOW_REOPEN_REMARK));
        assertThat(jp.getInt("data[2].status"), is(2));
        assertThat(jp.getString("data[2].statusLabel"), is("Approved"));
        assertThat(jp.getList("data").size(), is(4));
        for (int i = 0; i < 4; i++) {
            assertThat(jp.get("data[" + i + "].id"), notNullValue());
            assertThat(jp.get("data[" + i + "].createdBy.id"), notNullValue());
            assertThat(jp.get("data[" + i + "].createdBy.name"), notNullValue());
            assertThat(jp.get("data[" + i + "].createdBy.userTypeId"), notNullValue());
            assertThat(jp.get("data[" + i + "].createdOn"), notNullValue());
        }
    }

    @Test(dataProvider = "addTimeSheetData")
    public void getReimbursementStatusHistoryAfterApprove_Test(int timesheetID, int reimbursementID) {
        Response approve = updateReimbursementStatus(timesheetID, reimbursementID, "approve", null,
                albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));

        Response response = getReimbursementStatusHistory(timesheetID, reimbursementID, albatrossAuthToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Status history fetched successfully"));

        assertThat(jp.getList("data").size(), greaterThanOrEqualTo(2));
        assertThat(jp.getInt("data[1].status"), is(1));
        assertThat(jp.getString("data[1].statusLabel"), is("Submitted"));
        assertThat(jp.getInt("data[0].status"), is(2));
        assertThat(jp.getString("data[0].statusLabel"), is("Approved"));
        for (int i = 0; i < 2; i++) {
            assertThat(jp.get("data[" + i + "].id"), notNullValue());
            assertThat(jp.get("data[" + i + "].createdBy.id"), notNullValue());
            assertThat(jp.get("data[" + i + "].createdBy.name"), notNullValue());
            assertThat(jp.get("data[" + i + "].createdBy.userTypeId"), notNullValue());
            assertThat(jp.get("data[" + i + "].createdOn"), notNullValue());
        }
    }

    @Test(dataProvider = "addTimeSheetData")
    public void getStatusHistoryWithInvalidReimbursementId(int timesheetID, int reimbursementID) {
        int invalidReimbursementId = JavaFakerReimbursement.generateFakerId();
        Response response = getReimbursementStatusHistory(timesheetID, invalidReimbursementId, albatrossAuthToken);
        assertStatusHistoryError(response, 404, "TimesheetReimbursement id " + invalidReimbursementId + " not found.");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void getStatusHistoryWithInvalidTimesheetId(int timesheetID, int reimbursementID) {
        int invalidTimesheetId = JavaFakerReimbursement.generateFakerId();
        Response response = getReimbursementStatusHistory(invalidTimesheetId, reimbursementID, albatrossAuthToken);
        assertStatusHistoryError(response, 404, "Timesheet id " + invalidTimesheetId + " not found.");
    }

    private void assertStatusHistoryError(Response response, int expectedStatus, String expectedMessage) {
        assertThat(response.getStatusCode(), is(expectedStatus));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), nullValue());
        assertThat(jp.getString("errors[0].message"), is(expectedMessage));
        assertThat(jp.getInt("meta.status"), is(expectedStatus));
        assertThat(jp.get("data"), nullValue());
        Object responseType = jp.get("meta.responseType");
        if (responseType instanceof String) {
            assertThat((String) responseType, is("ERROR"));
        }
    }

    @DataProvider(parallel = true)
    public Object[][] addTimeSheetData() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 1);
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);
        int reimbursementID = createReimbursement("1 Lunch outside", 40.00, "test.pdf", timesheetIDs.get(0),
                albatrossAuthToken);
        return new Object[][] { { timesheetIDs.get(0), reimbursementID } };
    }

    
    @DataProvider(parallel = true)
    public Object[][] reimbursementSubmittedRejectReopenApproveData() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 1);
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);
        int timesheetId = timesheetIDs.get(0);
        int reimbursementID = createReimbursement("1 Lunch outside", 40.00, "test.pdf", timesheetId,
                albatrossAuthToken);

        Response approve = updateReimbursementStatus(timesheetId, reimbursementID, "approve", null,
                albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));

        Response reopen = reopenReimbursement(timesheetId, reimbursementID, WORKFLOW_REOPEN_REMARK,
                albatrossAuthToken);
        assertThat(reopen.getStatusCode(), is(200));

        Response reject = updateReimbursementStatus(timesheetId, reimbursementID, "reject", WORKFLOW_REJECT_REMARK,
                albatrossAuthToken);
        assertThat(reject.getStatusCode(), is(200));

        return new Object[][] { { timesheetId, reimbursementID } };
    }

    public Response getReimbursementStatusHistory(int timesheetId, int reimbursementId, String authToken) {
        return RestClient.doGet("JSON", timesheetBaseURL,
                        "timesheets/" + timesheetId + "/reimbursements/" + reimbursementId + "/status-history",
                        authToken, null, null, true);
    }
}
