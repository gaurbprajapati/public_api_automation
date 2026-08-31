package io.recruitcrm.contractStaffing.expenseAndReimbursement.ClientPortal;

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
public class GetReimbursementInClientPortalTest extends ClientPortalExpenseReimbursementBaseTest {

    private static final String REOPEN_REMARK = "Reopened for contractor correction";

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void getReimbursementWithClientkn(int timesheetID, int reimbursementID, String portalToken) {
        Response response = listReimbursements(timesheetID, portalToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getInt("data.timesheetId"), is(timesheetID));
        assertThat(jp.getString("data.description"), is("1 Lunch outside"));
        assertThat(jp.getDouble("data.amount"), is(40.00));
        assertThat(jp.getInt("data.status"), is(1));
        assertThat(jp.get("data.isPayable"), is(0));
        assertThat(jp.get("data.isBillable"), is(0));
    }

    @Test(dataProvider = "addPortalTimesheetIdTokenNoReimbursementLineData")
    public void getReimbursementListWithoutReimbursement_Client(int timesheetId, String portalToken) {
        Response response = listReimbursements(timesheetId, portalToken);
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

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void getReimbursementsWithInvalidTimesheetId_Client(int timesheetID, int reimbursementID, String portalToken) {
        int invalidTimesheetId = JavaFakerReimbursement.generateFakerId();
        Response response = listReimbursements(invalidTimesheetId, portalToken);
        assertThat(response.getStatusCode(), is(404));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), nullValue());
        assertThat(jp.getString("errors[0].message"), is("Timesheet id " + invalidTimesheetId + " not found."));
        assertThat(jp.getInt("meta.status"), is(404));
        assertThat(jp.get("data"), nullValue());
        Object responseType = jp.get("meta.responseType");
        if (responseType instanceof String) {
            assertThat((String) responseType, is("ERROR"));
        }
    }

    @Test(dataProvider = "addPortalTimesheetWithReopenedReimbursementListData")
    public void getReimbursementsWithReopenedReimbursement_Client(int timesheetId, int reimbursementId,
            String portalToken) {
        Response response = listReimbursements(timesheetId, portalToken);
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

    @Test(dataProvider = "crossPortalOtherClientsResourceData")
    public void getReimbursementWithAnotherClientId_Client(int otherClientTimesheetId, int otherClientReimbursementId,
            String differentClientPortalToken) {
        Response response = getReimbursement(otherClientTimesheetId, otherClientReimbursementId,
                differentClientPortalToken);
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

    public Response getReimbursement(int timesheetId, int reimbursementId, String authToken) {
        return RestClient.doGet("JSON", timesheetBaseURL,
                "timesheets/" + timesheetId + "/reimbursements/" + reimbursementId, authToken, null, null, true);
    }

    public Response listReimbursements(int timesheetId, String authToken) {
        return RestClient.doGet("JSON", timesheetBaseURL, "timesheets/" + timesheetId + "/reimbursements", authToken,
                null, null, true);
    }

    @DataProvider(parallel = true)
    public Object[][] addPortalTimesheetIdTokenNoReimbursementLineData() {
        Object[] row = buildPortalData(1, false);
        return new Object[][] { { row[3], row[5] } };
    }

    @DataProvider(parallel = true)
    public Object[][] addPortalTimesheetWithReopenedReimbursementListData() {
        Object[] row = buildPortalData(1, true);
        int timesheetId = (Integer) row[3];
        int reimbursementId = (Integer) row[4];
        String portalToken = (String) row[5];

        Response reject = updateReimbursementStatus(timesheetId, reimbursementId, "approve",
                "Rejected before agency reopen for list test", albatrossAuthToken);
        assertThat(reject.getStatusCode(), is(200));

        Response reopen = reopenReimbursement(timesheetId, reimbursementId, REOPEN_REMARK, albatrossAuthToken);
        assertThat(reopen.getStatusCode(), is(200));

        return new Object[][] { { timesheetId, reimbursementId, portalToken } };
    }
}
