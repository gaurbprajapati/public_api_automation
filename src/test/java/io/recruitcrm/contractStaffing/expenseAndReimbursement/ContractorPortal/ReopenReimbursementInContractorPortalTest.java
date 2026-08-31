package io.recruitcrm.contractStaffing.expenseAndReimbursement.ContractorPortal;

import io.rcrm.api.javafaker.ContractStaffing.JavaFakerReimbursement;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.hamcrest.Matchers.nullValue;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class ReopenReimbursementInContractorPortalTest extends ContractorPortalExpenseReimbursementBaseTest {

    private static final String REOPEN_REMARK = "Reopened for contractor correction";

    @Test(dataProvider = "addContractorPortalTimesheetIdReimbursementIdTokenData")
    public void reopenReimbursementWithContractorTkn(int timesheetID, int reimbursementID, String portalToken) {
        Response reject = updateReimbursementStatus(timesheetID, reimbursementID, "reject",
                "Rejected - will reopen for correction", albatrossAuthToken);
        assertThat(reject.getStatusCode(), is(200));

        Response response = reopenReimbursement(timesheetID, reimbursementID, REOPEN_REMARK, portalToken);
        assertThat(response.getStatusCode(), is(403));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), nullValue());
        assertThat(jp.getString("errors[0].message"), is("You are not authorized to reopen reimbursements"));
        assertThat(jp.getInt("meta.status"), is(403));
        assertNull(jp.get("data"));
    }
}
