package io.recruitcrm.contractStaffing.expenseAndReimbursement.ContractorPortal;

import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNull;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class UpdateReimbursementStatusInContractorPortalTest extends ContractorPortalExpenseReimbursementBaseTest {

    @Test(dataProvider = "addContractorPortalTimesheetIdReimbursementIdTokenData")
    public void updateStatusWithContractorTkn(int timesheetID, int reimbursementID, String portalToken) {
        Response response = updateReimbursementStatus(timesheetID, reimbursementID, "approve",
                "Approved by manager - valid expense", portalToken);
        assertThat(response.getStatusCode(), is(403));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), nullValue());
        assertThat(jp.getString("errors[0].message"), is("Only agency and client users can approve or reject reimbursements"));
        assertThat(jp.getInt("meta.status"), is(403));
        assertNull(jp.get("data"));
    }
}
