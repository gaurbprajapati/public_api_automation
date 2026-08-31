package io.recruitcrm.contractStaffing.expenseAndReimbursement.Agency;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.ContractStaffing.JavaFakerReimbursement;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class UpdatePayableBillableForReimbursementTest extends ExpenseAndReimbursementBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
    }

    @Test(dataProvider = "addTimeSheetData")
    public void UpdatePayableBillableFlags_Test(int timesheetID, int reimbursementID) {
        Response approve = updateReimbursementStatus(timesheetID, reimbursementID, "approve",
                null, albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));

        Response response = updatePayableBillableForReimbursement(timesheetID, reimbursementID, albatrossAuthToken, 1,
                0);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Reimbursement updated successfully"));
        assertThat(jp.getInt("meta.status"), is(200));
        assertThat(jp.getInt("data.id"), is(reimbursementID));
        assertThat(jp.getInt("data.timesheetId"), is(timesheetID));
        assertThat(jp.getInt("data.status"), is(2));
        assertThat(jp.getString("data.statusLabel"), is("Approved"));
        assertThat(jp.getInt("data.isPayable"), is(1));
        assertThat(jp.getInt("data.isBillable"), is(0));
        Object responseType = jp.get("meta.responseType");
        if (responseType instanceof String) {
            assertThat((String) responseType, is("SUCCESS"));
        }
    }

    @Test(dataProvider = "addTimeSheetData")
    public void UpdatePayableBillableWithNoPayload_Test(int timesheetID, int reimbursementID) {
        Response approve = updateReimbursementStatus(timesheetID, reimbursementID, "approve",
                null, albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));

        Response response = updatePayableBillableForReimbursement(timesheetID, reimbursementID, albatrossAuthToken,
                null, null);
        assertPayableBillableError(response, 400, "At least one of isPayable or isBillable must be provided");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void UpdatePayableBillableForInvalidReimbursementId_Test(int timesheetID, int reimbursementID) {
        Response approve = updateReimbursementStatus(timesheetID, reimbursementID, "approve",
                null, albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));

        int invalidId = JavaFakerReimbursement.generateFakerId();
        Response response = updatePayableBillableForReimbursement(timesheetID, invalidId, albatrossAuthToken, 1, 0);
        assertPayableBillableError(response, 404, "TimesheetReimbursement id " + invalidId + " not found.");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void UpdatePayableBillableFlagsForInvalidTimesheetId_Test(int timesheetID, int reimbursementID) {
        Response approve = updateReimbursementStatus(timesheetID, reimbursementID, "approve",
                null, albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));

        int invalidTimesheetId = JavaFakerReimbursement.generateFakerId();
        Response response = updatePayableBillableForReimbursement(invalidTimesheetId, reimbursementID,
                albatrossAuthToken, 1, 0);
        assertThat(response.getStatusCode(), is(404));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), nullValue());
        assertThat(jp.getString("errors[0].message"),
                is("Timesheet id " + invalidTimesheetId + " not found."));
        assertThat(jp.getInt("meta.status"), is(404));
        assertThat(jp.get("data"), nullValue());
    }

    @Test(dataProvider = "addTimeSheetData")
    public void UpdatePayableBillableForNotApprovedReimbursement_Test(int timesheetID, int reimbursementID) {
        Response response = updatePayableBillableForReimbursement(timesheetID, reimbursementID, albatrossAuthToken, 1,
                0);
        assertPayableBillableError(response, 409,
                "Payable and billable flags can only be updated on approved reimbursements");
    }

    
    @Test(dataProvider = "addTimeSheetData")
    public void updatePayableBillable_onlyAgencyApprovers_Test(int timesheetID, int reimbursementID) {
        Response approve = updateReimbursementStatus(timesheetID, reimbursementID, "approve",
                null, albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));

        String restrictedToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        Response response = updatePayableBillableForReimbursement(timesheetID, reimbursementID, restrictedToken, 0, 1);
        assertThat(response.getStatusCode(), is(403));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"),
                is("Only agency approvers can update payable and billable flags"));
        assertThat(jp.getInt("meta.status"), is(403));
        assertThat(jp.get("data"), nullValue());
        Object responseType = jp.get("meta.responseType");
        if (responseType instanceof String) {
            assertThat((String) responseType, is("ERROR"));
        }
    }

    @Test(dataProvider = "buildAgencyData")
    public void updatePayableBillableWithInvoiceLinkedId(int timesheetId, int reimbursementId, String albatrossAuthToken) {
        Response response = updatePayableBillableForReimbursement(timesheetId, reimbursementId, albatrossAuthToken, 1, 0);
        assertThat(response.statusCode(), is(409));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), nullValue());
        assertThat(jp.getString("errors[0].message"), is("Reimbursement cannot be modified while an invoice is linked to this timesheet"));
    }

    private void assertPayableBillableError(Response response, int expectedStatus, String expectedMessage) {
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
    public Object[][] addTimeSheetData() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200,1);
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);
        int reimbursementID = createReimbursement("1 Lunch outside", 40.00, "test.pdf", timesheetIDs.get(0),
                albatrossAuthToken);
        return new Object[][] { { timesheetIDs.get(0), reimbursementID } };
    }
}
