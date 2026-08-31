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
public class DeleteReimbursementInContractorPortalTest extends InvoiceBaseTest {

    @Test(dataProvider = "addContractorPortalTimesheetData")
    public void deleteReimbursementWithContractorTkn(int jobId, int candidateId, int userId, int timesheetID,
            int reimbursementID, String portalToken) {
        Response deleteReimbursementResponse = deleteReimbursement(timesheetID, reimbursementID, portalToken);
        assertThat(deleteReimbursementResponse.statusCode(), is(200));
        JsonPath deleteReimbursementJsonPath = deleteReimbursementResponse.jsonPath();
        assertThat(deleteReimbursementJsonPath.getString("meta.message"), is("Reimbursement deleted successfully"));
    }

    @Test(dataProvider = "addContractorPortalTimesheetApprovedReimbursementData")
    public void deleteApprovedReimbursementFromContractorPortal(int timesheetID, int reimbursementID, String portalToken) {
        Response deleteReimbursementResponse = deleteReimbursement(timesheetID, reimbursementID, portalToken);
        assertThat(deleteReimbursementResponse.getStatusCode(), is(200));
        JsonPath jp = deleteReimbursementResponse.jsonPath();
        assertThat(jp.getString("meta.message"), is("Reimbursement deleted successfully"));
    }

    @Test(dataProvider = "buildPortalData")
    public void deleteReimbursementWithInvoiceLinkedId_Contractor(int timesheetId, int reimbursementId, String portalToken) {
        Response deleteReimbursementResponse = deleteReimbursement(timesheetId, reimbursementId, portalToken);
        assertThat(deleteReimbursementResponse.getStatusCode(), is(409));
        JsonPath deleteReimbursementJsonPath = deleteReimbursementResponse.jsonPath();
        assertThat(deleteReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(deleteReimbursementJsonPath.getString("errors[0].message"), is("Reimbursement cannot be deleted while an invoice is linked to this timesheet"));
    }

    @Test(dataProvider = "crossContractorPortalDeleteOtherContractorReimbursementData")
    public void deleteReimbursementWithAnotherContractorId_Contractor(int timesheetID, int reimbursementID,
            String portalToken) {
        Response deleteReimbursementResponse = deleteReimbursement(timesheetID, reimbursementID, portalToken);
        assertThat(deleteReimbursementResponse.statusCode(), is(404));
        JsonPath deleteReimbursementJsonPath = deleteReimbursementResponse.jsonPath();
        assertThat(deleteReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(deleteReimbursementJsonPath.getString("errors[0].message"), is("Reimbursement id " + reimbursementID + " not found."));
    }

    @DataProvider(parallel = true)
    public Object[][] addContractorPortalTimesheetApprovedReimbursementData() {
        Object[] row = buildContractorPortalData(1, true);
        int timesheetId = (Integer) row[3];
        int reimbursementId = (Integer) row[4];
        String portalToken = (String) row[5];
        Response approve = updateReimbursementStatus(timesheetId, reimbursementId, "approve",
                "Approved before contractor delete attempt", albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));
        return new Object[][] { { timesheetId, reimbursementId, portalToken } };
    }

    @DataProvider(parallel = true)
    public Object[][] addContractorPortalTimesheetNoReimbursementLineData() {
        Object[] row = buildContractorPortalData(1, false);
        return new Object[][] { {
                row[0], row[1], row[2], row[3], row[5]
        } };
    }

    public Response deleteReimbursement(int timesheetID, int reimbursementID, String authToken) {
        return RestClient.doDelete("JSON", timesheetBaseURL,
                "timesheets/" + timesheetID + "/reimbursements/" + reimbursementID, authToken, null, null, true);
    }
}
