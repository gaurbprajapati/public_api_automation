package io.recruitcrm.contractStaffing.expenseAndReimbursement.ClientPortal;

import io.rcrm.api.javafaker.ContractStaffing.JavaFakerReimbursement;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class GetReimbursementCountInClientPortalTest extends ClientPortalExpenseReimbursementBaseTest {

    @Test(dataProvider = "portalTimesheetWithoutReimbursementsData")
    public void getReimbursementCountWithoutReimbursements_Client(int timesheetId, String portalToken) {
        Response response = getReimbursementCount(timesheetId, portalToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Reimbursement count fetched successfully"));
        assertThat(jp.getInt("meta.status"), is(200));
        assertThat(jp.getInt("data"), is(0));
        Object responseType = jp.get("meta.responseType");
        if (responseType instanceof String) {
            assertThat((String) responseType, is("SUCCESS"));
        }
    }

    @Test(dataProvider = "portalTimesheetWithTwoReimbursementsData")
    public void getReimbursementCountWithClientTkn(int timesheetId, String portalToken) {
        Response response = getReimbursementCount(timesheetId, portalToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Reimbursement count fetched successfully"));
        assertThat(jp.getInt("data"), is(2));
    }

    @Test(dataProvider = "crossPortalOtherClientsResourceData")
    public void getReimbursementCountWithAnotherClientId_Client(int timesheetID, int reimbursementID, String otherPortalToken) {
        Response response = getReimbursementCount(timesheetID, otherPortalToken);
        assertThat(response.getStatusCode(), is(404));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), nullValue());
        assertThat(jp.getString("errors[0].message"), is("Timesheet id " + timesheetID + " not found."));
        assertThat(jp.getInt("meta.status"), is(404));
        assertThat(jp.get("data"), nullValue());
        Object responseType = jp.get("meta.responseType");
        if (responseType instanceof String) {
            assertThat((String) responseType, is("ERROR"));
        }
    }

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void getReimbursementCountWithInvalidTimesheetId_Client(int timesheetID, int reimbursementID, String portalToken) {
        int invalidTimesheetId = JavaFakerReimbursement.generateFakerId();
        Response response = getReimbursementCount(invalidTimesheetId, portalToken);
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

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void getReimbursementCountWithNegativeTimesheetId_Client(int timesheetID, int reimbursementID, String portalToken) {
        Response response = getReimbursementCount(-1, portalToken);
        assertThat(response.getStatusCode(), is(400));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), nullValue());
        assertThat(jp.getString("errors[0].message"), is("timesheetId must be a positive integer"));
        assertThat(jp.getInt("meta.status"), is(400));
        assertThat(jp.get("data"), nullValue());
        Object responseType = jp.get("meta.responseType");
        if (responseType instanceof String) {
            assertThat((String) responseType, is("ERROR"));
        }
    }

    @DataProvider(parallel = true)
    public Object[][] portalTimesheetWithoutReimbursementsData() {
        Object[] row = buildPortalData(1, false);
        return new Object[][] { { row[3], row[5] } };
    }

    @DataProvider(parallel = true)
    public Object[][] portalTimesheetWithTwoReimbursementsData() {
        Object[] row = buildPortalData(1, false);
        int timesheetId = (Integer) row[3];
        String portalToken = (String) row[5];
        createReimbursement("Expense A", 10.00, "test.pdf", timesheetId, albatrossAuthToken);
        createReimbursement("Expense B", 20.50, "test.pdf", timesheetId, albatrossAuthToken);
        return new Object[][] { { timesheetId, portalToken } };
    }

    public Response getReimbursementCount(int timesheetId, String authToken) {
        return RestClient.doGet("JSON", timesheetBaseURL, "timesheets/" + timesheetId + "/reimbursements/count",
                authToken, null, null, true);
    }
}
