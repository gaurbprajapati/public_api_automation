package io.recruitcrm.contractStaffing.expenseAndReimbursement.ContractorPortal;

import io.rcrm.api.javafaker.ContractStaffing.JavaFakerReimbursement;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class GetStatusHistoryInContractorPortalTest extends ContractorPortalExpenseReimbursementBaseTest {

    private static final String WORKFLOW_REJECT_REMARK = "Rejected for status-history workflow";
    private static final String WORKFLOW_REOPEN_REMARK = "Reopened for contractor correction";
    private static final String WORKFLOW_APPROVE_REMARK = "Approved after reopen";

    @Test(dataProvider = "reimbursementSubmittedRejectReopenApproveContractorPortalData")
    public void getStatusHistoryWithContractorTkn(int timesheetID, int reimbursementID, String portalToken) {
        Response response = getReimbursementStatusHistory(timesheetID, reimbursementID, portalToken);
        
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Status history fetched successfully"));
        assertThat(jp.getInt("meta.status"), is(200));
        Object responseType = jp.get("meta.responseType");
        if (responseType instanceof String) {
            assertThat((String) responseType, is("SUCCESS"));
        }

        assertThat(jp.getList("data").size(), is(4));
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
        assertThat(jp.getString("data[2].remark"), nullValue());

        for (int i = 0; i < 4; i++) {
            assertThat(jp.get("data[" + i + "].createdBy.id"), notNullValue());
            assertThat(jp.get("data[" + i + "].createdBy.name"), notNullValue());
            assertThat(jp.get("data[" + i + "].createdBy.userTypeId"), notNullValue());
            assertThat(jp.get("data[" + i + "].createdOn"), notNullValue());
        }
    }

    @Test(dataProvider = "addContractorPortalTimesheetIdReimbursementIdTokenData")
    public void getReimbursementStatusHistoryAfterApprove_Contractor(int timesheetID, int reimbursementID,
            String portalToken) {
        String approveRemark = "Approved by manager";
        Response approve = updateReimbursementStatus(timesheetID, reimbursementID, "approve", approveRemark,
                albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));

        Response response = getReimbursementStatusHistory(timesheetID, reimbursementID, portalToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Status history fetched successfully"));

        assertThat(jp.getList("data").size(), greaterThanOrEqualTo(2));
        assertThat(jp.getInt("data[0].status"), is(2));
        assertThat(jp.getString("data[0].statusLabel"), is("Approved"));
        assertThat(jp.getInt("data[1].status"), is(1));
        assertThat(jp.getString("data[1].statusLabel"), is("Submitted"));
        assertThat(jp.get("data[1].createdBy.id"), notNullValue());
        assertThat(jp.get("data[1].createdBy.name"), notNullValue());
        assertThat(jp.get("data[1].createdBy.userTypeId"), notNullValue());
        assertThat(jp.get("data[1].createdOn"), notNullValue());
    }

    @Test(dataProvider = "addContractorPortalTimesheetIdReimbursementIdTokenData")
    public void getStatusHistoryWithInvalidReimbursementId_Contractor(int timesheetID, int reimbursementID,
            String portalToken) {
        int invalidReimbursementId = JavaFakerReimbursement.generateFakerId();
        Response response = getReimbursementStatusHistory(timesheetID, invalidReimbursementId, portalToken);
        assertStatusHistoryError(response, 404, "TimesheetReimbursement id " + invalidReimbursementId + " not found.");
    }

    @Test(dataProvider = "addContractorPortalTimesheetIdReimbursementIdTokenData")
    public void getStatusHistoryWithInvalidTimesheetId_Contractor(int timesheetID, int reimbursementID,
            String portalToken) {
        int invalidTimesheetId = JavaFakerReimbursement.generateFakerId();
        Response response = getReimbursementStatusHistory(invalidTimesheetId, reimbursementID, portalToken);
        assertStatusHistoryError(response, 404, "Timesheet id " + invalidTimesheetId + " not found.");
    }

    @Test(dataProvider = "crossContractorPortalOtherUsersResourceData")
    public void getStatusHistoryWithAnotherContractorId(int timesheetID, int reimbursementID,
            String otherPortalToken) {
        Response response = getReimbursementStatusHistory(timesheetID, reimbursementID, otherPortalToken);
        assertStatusHistoryError(response, 404, "Timesheet id " + timesheetID + " not found.");
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
    public Object[][] reimbursementSubmittedRejectReopenApproveContractorPortalData() {
        Object[] row = buildContractorPortalData(1, true);
        int timesheetId = (Integer) row[3];
        int reimbursementID = (Integer) row[4];
        String portalToken = (String) row[5];

        Response approve = updateReimbursementStatus(timesheetId, reimbursementID, "approve", WORKFLOW_APPROVE_REMARK,
        albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));

        Response reopen = reopenReimbursement(timesheetId, reimbursementID, WORKFLOW_REOPEN_REMARK, albatrossAuthToken);
        assertThat(reopen.getStatusCode(), is(200));

       

        Response reject = updateReimbursementStatus(timesheetId, reimbursementID, "reject", WORKFLOW_REJECT_REMARK,
                albatrossAuthToken);
        assertThat(reject.getStatusCode(), is(200));

        return new Object[][] { { timesheetId, reimbursementID, portalToken } };
    }

    public Response getReimbursementStatusHistory(int timesheetId, int reimbursementId, String authToken) {
        return RestClient.doGet("JSON", timesheetBaseURL,
                "timesheets/" + timesheetId + "/reimbursements/" + reimbursementId + "/status-history", authToken, null,
                null, true);
    }
}
