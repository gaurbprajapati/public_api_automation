package io.recruitcrm.contractStaffing.expenseAndReimbursement.ContractorPortal;

import io.rcrm.api.javafaker.ContractStaffing.JavaFakerReimbursement;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class GetReimbursementInContractorPortalTest extends ContractorPortalExpenseReimbursementBaseTest {

    private static final String REOPEN_REMARK = "Reopened for contractor correction";

    @Test(dataProvider = "addContractorPortalTimesheetIdReimbursementIdTokenData")
    public void getReimbursementWithContractorTkn(int timesheetID, int reimbursementID, String portalToken) {
        Response response = getReimbursementsList(timesheetID, portalToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getInt("data[0].id"), is(reimbursementID));
        assertThat(jp.getInt("data[0].timesheetId"), is(timesheetID));
        assertThat(jp.getString("data[0].description"), is("1 Lunch outside"));
        assertThat(jp.getDouble("data[0].amount"), is(40.00));
        assertThat(jp.getInt("data[0].status"), is(1));
        assertThat(jp.get("data[0].addedOn"), notNullValue());
        assertThat(jp.get("data[0].updatedOn"), notNullValue());
        assertThat(jp.get("data[0].addedBy.id"), notNullValue());
        assertThat(jp.get("data[0].addedBy.name"), notNullValue());
        assertThat(jp.get("data[0].addedBy.userTypeId"), notNullValue());
        assertThat(jp.get("data[0].updatedBy.id"), notNullValue());
        assertThat(jp.get("data[0].updatedBy.name"), notNullValue());
        assertThat(jp.get("data[0].updatedBy.userTypeId"), notNullValue());
        assertThat(jp.get("data[0].isPayable"), is(0));
        assertThat(jp.get("data[0].isBillable"), is(0));
    }

    @Test(dataProvider = "addContractorPortalTimesheetIdTokenNoReimbursementLineData")
    public void getReimbursementListWithoutReimbursement_Contractor(int timesheetId, String portalToken) {
        Response response = getReimbursementsList(timesheetId, portalToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Reimbursements fetched successfully"));
        assertThat(jp.getInt("meta.status"), is(200));
        assertThat(jp.getList("data").size(), is(0));
        Object responseType = jp.get("meta.responseType");
        if (responseType instanceof String) {
            assertThat((String) responseType, is("SUCCESS"));
        }
    }

    @Test(dataProvider = "addContractorPortalTimesheetWithReopenedReimbursementListData")
    public void getReimbursementsWithReopenedReimbursement_Contractor(int timesheetId, int reimbursementId,
            String portalToken) {
        Response response = getReimbursementsList(timesheetId, portalToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Reimbursements fetched successfully"));
        List<Object> data = jp.getList("data");
        assertThat(data.size(), is(1));
        assertThat(jp.getInt("data[0].id"), is(reimbursementId));
        assertThat(jp.getInt("data[0].timesheetId"), is(timesheetId));
        assertThat(jp.getInt("data[0].status"), is(1));
        assertThat(jp.getString("data[0].description"), is("1 Lunch outside"));
        assertThat(jp.get("data[0].addedOn"), notNullValue());
    }

    @Test(dataProvider = "crossContractorPortalOtherUsersResourceData")
    public void getReimbursementWithAnotherContractorId_Contractor(int otherTimesheetId, int otherReimbursementId,
            String differentPortalToken) {
        Response response = getReimbursementsList(otherTimesheetId, differentPortalToken);
        assertThat(response.getStatusCode(), anyOf(is(403), is(404)));
        JsonPath jp = response.jsonPath();
        assertThat(jp.get("data"), nullValue());
        if (response.getStatusCode() == 403) {
            assertThat(jp.getString("meta.message"), startsWith("You are not authorized"));
        }
        Object responseType = jp.get("meta.responseType");
        if (responseType instanceof String) {
            assertThat((String) responseType, is("ERROR"));
        }
    }


    public Response getReimbursementsList(int timesheetId, String authToken) {
        return RestClient.doGet("JSON", timesheetBaseURL, "timesheets/" + timesheetId + "/reimbursements", authToken,
                null, null, true);
    }

    @DataProvider(parallel = true)
    public Object[][] addContractorPortalTimesheetIdTokenNoReimbursementLineData() {
        Object[] row = buildContractorPortalData(1, false);
        return new Object[][] { { row[3], row[5] } };
    }

    @DataProvider(parallel = true)
    public Object[][] addContractorPortalTimesheetWithReopenedReimbursementListData() {
        Object[] row = buildContractorPortalData(1, true);
        int timesheetId = (Integer) row[3];
        int reimbursementId = (Integer) row[4];
        String portalToken = (String) row[5];

        Response reject = updateReimbursementStatus(timesheetId, reimbursementId, "approve",
                "Rejected before reopen for list test", albatrossAuthToken);
        assertThat(reject.getStatusCode(), is(200));

        Response reopen = reopenReimbursement(timesheetId, reimbursementId, REOPEN_REMARK, albatrossAuthToken);
        assertThat(reopen.getStatusCode(), is(200));

        return new Object[][] { { timesheetId, reimbursementId, portalToken } };
    }
}
