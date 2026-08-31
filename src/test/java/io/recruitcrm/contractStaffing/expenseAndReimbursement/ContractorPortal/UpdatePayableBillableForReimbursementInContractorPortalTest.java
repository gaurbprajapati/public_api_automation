package io.recruitcrm.contractStaffing.expenseAndReimbursement.ContractorPortal;

import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNull;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class UpdatePayableBillableForReimbursementInContractorPortalTest
        extends ContractorPortalExpenseReimbursementBaseTest {

    @Test(dataProvider = "addContractorPortalTimesheetIdReimbursementIdTokenData")
    public void UpdatePayableBillableFlagsWithContractorTkn(int timesheetID, int reimbursementID, String portalToken) {
        Response approve = updateReimbursementStatus(timesheetID, reimbursementID, "approve",
                "Approved for payable-billable update", albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));

        Response response = updatePayableBillableForReimbursement(timesheetID, reimbursementID, portalToken, 1, 0);
        assertThat(response.getStatusCode(), is(403));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), nullValue());
        assertThat(jp.getString("errors[0].message"), is("Only agency approvers can update payable and billable flags"));
        assertNull(jp.get("data"));
    }
}
