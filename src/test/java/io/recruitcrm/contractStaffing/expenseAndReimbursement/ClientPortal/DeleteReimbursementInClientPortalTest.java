package io.recruitcrm.contractStaffing.expenseAndReimbursement.ClientPortal;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class DeleteReimbursementInClientPortalTest extends ClientPortalExpenseReimbursementBaseTest {

    @Test(dataProvider = "addPortalTimesheetData")
    public void deleteReimbursementWithClientTkn(int jobId, int candidateId, int userId, int timesheetID,
            int reimbursementID, String portalToken) {
        Response deleteReimbursementResponse = deleteReimbursement(timesheetID, reimbursementID, portalToken);
        assertThat(deleteReimbursementResponse.statusCode(), is(403));
        JsonPath deleteReimbursementJsonPath = deleteReimbursementResponse.jsonPath();
        assertThat(deleteReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(deleteReimbursementJsonPath.getString("errors[0].message"), is("Only agency and contractor users can create reimbursements"));
    }

    @Test(dataProvider = "addPortalTimesheetApprovedReimbursementData")
    public void deleteApprovedReimbursementFromClientPortal(int timesheetID, int reimbursementID, String portalToken) {
        Response deleteReimbursementResponse = deleteReimbursement(timesheetID, reimbursementID, portalToken);
        assertThat(deleteReimbursementResponse.statusCode(), is(403));
        JsonPath deleteReimbursementJsonPath = deleteReimbursementResponse.jsonPath();
        assertThat(deleteReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(deleteReimbursementJsonPath.getString("errors[0].message"), is("Only agency and contractor users can create reimbursements"));
    }

    @DataProvider(parallel = true)
    public Object[][] addPortalTimesheetApprovedReimbursementData() {
        Object[] row = buildPortalData(1, true);
        int timesheetId = (Integer) row[3];
        int reimbursementId = (Integer) row[4];
        String portalToken = (String) row[5];
        Response approve = updateReimbursementStatus(timesheetId, reimbursementId, "approve",
                "Approved before client delete attempt", albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));
        return new Object[][] { { timesheetId, reimbursementId, portalToken } };
    }

    public Response deleteReimbursement(int timesheetID, int reimbursementID, String authToken) {
        return RestClient.doDelete("JSON", timesheetBaseURL,
                "timesheets/" + timesheetID + "/reimbursements/" + reimbursementID, authToken, null, null, true);
    }
}
