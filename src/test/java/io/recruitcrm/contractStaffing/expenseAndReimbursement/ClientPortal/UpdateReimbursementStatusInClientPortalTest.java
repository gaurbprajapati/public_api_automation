package io.recruitcrm.contractStaffing.expenseAndReimbursement.ClientPortal;

import io.rcrm.api.javafaker.ContractStaffing.JavaFakerReimbursement;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class UpdateReimbursementStatusInClientPortalTest extends InvoiceBaseTest {

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void updateStatusWithClientTkn(int timesheetID, int reimbursementID, String portalToken) {
        Response response = updateReimbursementStatus(timesheetID, reimbursementID, "approve",
                "Approved by manager - valid expense", portalToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Reimbursement status updated successfully"));
        assertThat(jp.getString("meta.responseType.context"), is("Request is successful"));
        assertThat(jp.getInt("meta.responseType.code"), is(103));
        assertThat(jp.getInt("data.id"), is(reimbursementID));
        assertThat(jp.getInt("data.timesheetId"), is(timesheetID));
        assertThat(jp.getInt("data.status"), is(2));
        assertThat(jp.getString("data.statusLabel"), is("Approved"));
        assertThat(jp.getInt("data.isPayable"), is(1));
        assertThat(jp.getInt("data.isBillable"), is(1));
    }

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void rejectReimbursement_Client(int timesheetID, int reimbursementID, String portalToken) {
        Response response = updateReimbursementStatus(timesheetID, reimbursementID, "reject",
                "Rejected - insufficient documentation provided", portalToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Reimbursement status updated successfully"));
        assertThat(jp.getInt("data.id"), is(reimbursementID));
        assertThat(jp.getInt("data.timesheetId"), is(timesheetID));
        assertThat(jp.getInt("data.status"), is(3));
        assertThat(jp.getString("data.statusLabel"), is("Rejected"));
        assertThat(jp.getInt("data.isPayable"), is(0));
        assertThat(jp.getInt("data.isBillable"), is(0));
    }

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void approveAlreadyApprovedReimbursement_Client(int timesheetID, int reimbursementID, String portalToken) {
        Response firstApprove = updateReimbursementStatus(timesheetID, reimbursementID, "approve",
                "Approved by manager - valid expense", portalToken);
        assertThat(firstApprove.getStatusCode(), is(200));
        Response secondApprove = updateReimbursementStatus(timesheetID, reimbursementID, "approve",
                "Second approve attempt", portalToken);
        assertUpdateReimbursementStatusError(secondApprove, 409, "Reimbursement is already approved");
    }

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void rejectAlreadyRejectedReimbursement_Client(int timesheetID, int reimbursementID, String portalToken) {
        Response firstReject = updateReimbursementStatus(timesheetID, reimbursementID, "reject",
                "Rejected - insufficient documentation provided", portalToken);
        assertThat(firstReject.getStatusCode(), is(200));
        Response secondReject = updateReimbursementStatus(timesheetID, reimbursementID, "reject",
                "Second reject attempt", portalToken);
        assertUpdateReimbursementStatusError(secondReject, 409, "Reimbursement is already rejected");
    }

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void rejectApprovedReimbursement_Client(int timesheetID, int reimbursementID, String portalToken) {
        Response approve = updateReimbursementStatus(timesheetID, reimbursementID, "approve",
                "Approved by manager - valid expense", portalToken);
        assertThat(approve.getStatusCode(), is(200));
        Response rejectAfterApprove = updateReimbursementStatus(timesheetID, reimbursementID, "reject",
                "Attempt reject after approve", portalToken);
        assertUpdateReimbursementStatusError(rejectAfterApprove, 409, "Cannot reject an approved reimbursement");
    }

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void approveRejectedReimbursement_Client(int timesheetID, int reimbursementID, String portalToken) {
        Response reject = updateReimbursementStatus(timesheetID, reimbursementID, "reject",
                "Rejected - insufficient documentation provided", portalToken);
        assertThat(reject.getStatusCode(), is(200));
        Response approveAfterReject = updateReimbursementStatus(timesheetID, reimbursementID, "approve",
                "Attempt approve after reject", portalToken);
        assertUpdateReimbursementStatusError(approveAfterReject, 409,
                "Reimbursement can only be approved if status is SUBMITTED");
    }

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void rejectReimbursementWithNoRemark_Client(int timesheetID, int reimbursementID, String portalToken) {
        Response response = updateReimbursementStatus(timesheetID, reimbursementID, "reject", "", portalToken);
        assertUpdateReimbursementStatusError(response, 400, "Remark is mandatory when rejecting a reimbursement");
    }

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void updateReimbursementWithInvalidStatus_Client(int timesheetID, int reimbursementID,
            String portalToken) {
        Response response = updateReimbursementStatus(timesheetID, reimbursementID, "hold", "invalid action",
                portalToken);
        assertUpdateReimbursementStatusError(response, 400, "Invalid reimbursement status ID. Allowed values: 2 (Approved), 3 (Rejected)");
    }

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void updateReimbursementStatusWithInvalidId_Client(int timesheetID, int reimbursementID, String portalToken) {
        int invalidId = JavaFakerReimbursement.generateFakerId();
        Response response = updateReimbursementStatus(timesheetID, invalidId, "approve",
                "ok", portalToken);
        assertUpdateReimbursementStatusError(response, 404, "Reimbursement id " + invalidId + " not found.");
    }

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void updateReimbursementStatusWithInvalidTimesheetId_Client(int timesheetID, int reimbursementID, String portalToken) {
        int invalidTimesheetId = JavaFakerReimbursement.generateFakerId();
        Response response = updateReimbursementStatus(invalidTimesheetId, reimbursementID,
                "approve", "ok", portalToken);
        assertUpdateReimbursementStatusError(response, 404, "Timesheet id " + invalidTimesheetId + " not found.");
    }

    @Test(dataProvider = "crossPortalOtherClientsResourceData")
    public void updateReimbursementWithCrossAccountToken_Client(int timesheetID, int reimbursementID, String otherPortalToken) {
        Response response = updateReimbursementStatus(timesheetID, reimbursementID, "approve", "ok", otherPortalToken);
        assertUpdateReimbursementStatusError(response, 403, "Timesheet not found");
    }

    @Test(dataProvider = "buildPortalData")
    public void updateReimbursementStatusWithInvoiceLinkedId_Client(int timesheetId, int reimbursementId, String portalToken) {
        Response approveResponse = updateReimbursementStatus(timesheetId, reimbursementId, "approve", "ok", portalToken);
        assertThat(approveResponse.getStatusCode(), is(409));
        JsonPath approveJsonPath = approveResponse.jsonPath();
        assertThat(approveJsonPath.getString("meta.message"), nullValue());
        assertThat(approveJsonPath.getString("errors[0].message"), is("Reimbursement  be modified while an invoice is linked to this timesheet"));
    }

    private void assertUpdateReimbursementStatusError(Response response, int expectedStatus, String expectedMessage) {
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
}
