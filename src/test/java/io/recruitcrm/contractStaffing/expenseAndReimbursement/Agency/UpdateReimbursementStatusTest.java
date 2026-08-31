package io.recruitcrm.contractStaffing.expenseAndReimbursement.Agency;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.ContractStaffing.*;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class UpdateReimbursementStatusTest extends ExpenseAndReimbursementBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
    }

    @Test(dataProvider = "addTimeSheetData")
    public void approveReimbursement_Test(int timesheetID, int reimbursementID) {
        Response response = updateReimbursementStatus(timesheetID, reimbursementID, "approve", null, albatrossAuthToken);
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

    @Test(dataProvider = "addTimeSheetData")
    public void rejectReimbursement_Test(int timesheetID, int reimbursementID) {
        Response response = updateReimbursementStatus(timesheetID, reimbursementID, "reject", "Rejected - insufficient documentation provided", albatrossAuthToken);
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

    @Test(dataProvider = "addTimeSheetData")
    public void approveAlreadyApprovedReimbursement(int timesheetID, int reimbursementID) {
        Response firstApprove = updateReimbursementStatus(timesheetID, reimbursementID, "approve", "Approved by manager - valid expense", albatrossAuthToken);
        assertThat(firstApprove.getStatusCode(), is(200));
        Response secondApprove = updateReimbursementStatus(timesheetID, reimbursementID, "approve", "Second approve attempt", albatrossAuthToken);
        assertUpdateReimbursementStatusError(secondApprove, 409, "Reimbursement is already approved");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void rejectAlreadyRejectedReimbursement(int timesheetID, int reimbursementID) {
        Response firstReject = updateReimbursementStatus(timesheetID, reimbursementID, "reject", "Rejected - insufficient documentation provided", albatrossAuthToken);
        assertThat(firstReject.getStatusCode(), is(200));
        Response secondReject = updateReimbursementStatus(timesheetID, reimbursementID, "reject", "Second reject attempt", albatrossAuthToken);
        assertUpdateReimbursementStatusError(secondReject, 409, "Reimbursement is already rejected");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void rejectApprovedReimbursement(int timesheetID, int reimbursementID) {
        Response approve = updateReimbursementStatus(timesheetID, reimbursementID, "approve", "Approved by manager - valid expense", albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));
        Response rejectAfterApprove = updateReimbursementStatus(timesheetID, reimbursementID, "reject", "Attempt reject after approve", albatrossAuthToken);
        assertUpdateReimbursementStatusError(rejectAfterApprove, 409, "Cannot reject an approved reimbursement");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void approveRejectedReimbursement(int timesheetID, int reimbursementID) {
        Response approve = updateReimbursementStatus(timesheetID, reimbursementID, "reject", "Rejected - insufficient documentation provided", albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));
        Response approveAfterReject = updateReimbursementStatus(timesheetID, reimbursementID, "approve", "Attempt approve after reject", albatrossAuthToken);
        assertUpdateReimbursementStatusError(approveAfterReject, 409, "Reimbursement can only be approved if status is SUBMITTED");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void rejectReimbursementWithNoRemark(int timesheetID, int reimbursementID) {
        Response response = updateReimbursementStatus(timesheetID, reimbursementID, "reject", "", albatrossAuthToken);
        assertUpdateReimbursementStatusError(response, 400, "Remark is mandatory when rejecting a reimbursement");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void updateReimbursementWithInvalidStatus_Test(int timesheetID, int reimbursementID) {
        Response response = updateReimbursementStatus(timesheetID, reimbursementID, "hold", "invalid action", albatrossAuthToken);
        assertUpdateReimbursementStatusError(response, 400, "Invalid reimbursement status ID. Allowed values: 2 (Approved), 3 (Rejected)");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void updateReimbursementInvalidId(int timesheetID, int reimbursementID) {
        int invalidId = JavaFakerReimbursement.generateFakerId();
        Response response = updateReimbursementStatus(timesheetID, invalidId, "approve", "ok", albatrossAuthToken);
        assertUpdateReimbursementStatusError(response, 404, "TimesheetReimbursement id " + invalidId + " not found.");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void updateReimbursementInvalidTimesheetId(int timesheetID, int reimbursementID) {
        int invalidTimesheetId = JavaFakerReimbursement.generateFakerId();
        Response response = updateReimbursementStatus(JavaFakerReimbursement.generateFakerId(), reimbursementID, "approve", "ok", albatrossAuthToken);
        assertUpdateReimbursementStatusError(response, 404, "TimesheetReimbursement id " + reimbursementID + " not found.");
    }

    @Test(dataProvider = "buildAgencyData")
    public void updateReimbursementWithInvoiceLinkedId(int timesheetID, int reimbursementID, String albatrossAuthToken) {
        Response response = updateReimbursementStatus(timesheetID, reimbursementID, "approve", "ok", albatrossAuthToken);
        assertUpdateReimbursementStatusError(response, 409, "Reimbursement cannot be modified while an invoice is linked to this timesheet");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void updateStatusWithInvalidTkn(int timesheetID, int reimbursementID) {
        Response response = updateReimbursementStatus(timesheetID, reimbursementID, "reject", "Rejected - insufficient documentation provided", albatrossAuthToken + "123");
        assertThat(response.getStatusCode(), is(401));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Unauthorised access"));
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
        return new Object[][] { { timesheetIDs.get(0), reimbursementID }};
    }
}
