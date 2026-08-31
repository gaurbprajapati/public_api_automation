package io.recruitcrm.contractStaffing.expenseAndReimbursement.Agency;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.ContractStaffing.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;


import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class ReopenReimbursementTest extends ExpenseAndReimbursementBaseTest {

    private static final String REOPEN_REMARK = "Reopened for contractor correction";

    private String albatrossAuthToken;
    private String apiAuthToken;
    private JavaFakerReimbursement fakerReimbursement;
    
    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
        fakerReimbursement = new JavaFakerReimbursement();
    }

    @Test(dataProvider = "addTimeSheetData")
    public void reopenReimbursement_Test(int timesheetID, int reimbursementID) {
        Response reject = updateReimbursementStatus(timesheetID, reimbursementID, "approve",
                null, albatrossAuthToken);
        assertThat(reject.getStatusCode(), is(200));

        Response response = reopenReimbursement(timesheetID, reimbursementID, REOPEN_REMARK, albatrossAuthToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Reimbursement reopened successfully"));
        assertThat(jp.getInt("meta.status"), is(200));
        assertThat(jp.getInt("data.id"), is(reimbursementID));
        assertThat(jp.getInt("data.timesheetId"), is(timesheetID));
        assertThat(jp.getInt("data.status"), is(1));
        assertThat(jp.getInt("data.isPayable"), is(0));
        assertThat(jp.getInt("data.isBillable"), is(0));
    }

    @Test(dataProvider = "addTimeSheetData")
    public void reopenRejectedReimbursement_Test(int timesheetID, int reimbursementID) {
        Response reject = updateReimbursementStatus(timesheetID, reimbursementID, "reject",
                "Rejected - will reopen for correction", albatrossAuthToken);
        assertThat(reject.getStatusCode(), is(200));

        Response response = reopenReimbursement(timesheetID, reimbursementID, REOPEN_REMARK, albatrossAuthToken);
        assertReopenReimbursementError(response, 409, "Reimbursement cannot be reopened in its current state");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void reopenReimbursementRemarkExceedsMaxLength_Test(int timesheetID, int reimbursementID) {
        Response reject = updateReimbursementStatus(timesheetID, reimbursementID, "reject",
                "Rejected - remark length test", albatrossAuthToken);
        assertThat(reject.getStatusCode(), is(200));

        String remarkOverMax = String.join("", Collections.nCopies(1001, "x"));
        Response response = reopenReimbursement(timesheetID, reimbursementID, remarkOverMax, albatrossAuthToken);
        assertThat(response.getStatusCode(), is(400));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("remark: size must be between 1 and 1000"));
        assertThat(jp.getInt("meta.status"), is(400));
        assertThat(jp.get("data"), nullValue());
        Object responseType = jp.get("meta.responseType");
        if (responseType instanceof String) {
            assertThat((String) responseType, is("ERROR"));
        }
    }

    @Test(dataProvider = "addTimeSheetData")
    public void reopenReimbursementsWithInvalidReimbursementId(int timesheetID, int reimbursementID) {
        Response reject = updateReimbursementStatus(timesheetID, reimbursementID, "reject",
                "Rejected - not found test", albatrossAuthToken);
        assertThat(reject.getStatusCode(), is(200));

        int invalidReimbursementId = JavaFakerReimbursement.generateFakerId();
        Response response = reopenReimbursement(timesheetID, invalidReimbursementId, REOPEN_REMARK, albatrossAuthToken);
        assertReopenReimbursementError(response, 404, "TimesheetReimbursement id " + invalidReimbursementId + " not found.");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void reopenReimbursementWithInvalidTimesheetId(int timesheetID, int reimbursementID) {
        Response reject = updateReimbursementStatus(timesheetID, reimbursementID, "reject",
                "Rejected - not found test", albatrossAuthToken);
        assertThat(reject.getStatusCode(), is(200));

        int invalidTimesheetId = JavaFakerReimbursement.generateFakerId();
        Response response = reopenReimbursement(invalidTimesheetId, reimbursementID, REOPEN_REMARK, albatrossAuthToken);
        assertReopenReimbursementError(response, 404, "Timesheet id " + invalidTimesheetId + " not found.");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void reopenReimbursementWithPendingId(int timesheetID, int reimbursementID) {
        Response response = reopenReimbursement(timesheetID, reimbursementID, REOPEN_REMARK, albatrossAuthToken);
        assertReopenReimbursementError(response, 409, "Reimbursement cannot be reopened in its current state");
    }

    private void assertReopenReimbursementError(Response response, int expectedStatus, String expectedMessage) {
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

    @Test(dataProvider = "addTimeSheetData")
    public void reopenReimbursementWithEmptyRemark(int timesheetID, int reimbursementID) {
        Response reject = updateReimbursementStatus(timesheetID, reimbursementID, "reject",
                "Rejected - will reopen for correction", albatrossAuthToken);
        assertThat(reject.getStatusCode(), is(200));

        Response response = reopenReimbursement(timesheetID, reimbursementID, "", albatrossAuthToken);
        assertThat(response.getStatusCode(), is(400));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("remark: must not be blank"));
    }

    @Test(dataProvider = "addTimeSheetData")
    public void reopenReimbursementWithEmptyPayload(int timesheetID, int reimbursementID) {
        Response reject = updateReimbursementStatus(timesheetID, reimbursementID, "reject",
                "Rejected - will reopen for correction", albatrossAuthToken);
        assertThat(reject.getStatusCode(), is(200));

        Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheets/" + timesheetID + "/reimbursements/" + reimbursementID + "/reopen",
        albatrossAuthToken, null, true, null);
        assertThat(response.getStatusCode(), is(400));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Request body is required"));
        assertThat(jp.getInt("meta.status"), is(400));
    }

    @Test(dataProvider = "addTimeSheetData")
    public void reopenReimbursementInvalidTkn(int timesheetID, int reimbursementID) {
        Response reject = updateReimbursementStatus(timesheetID, reimbursementID, "reject",
                "Rejected - will reopen for correction", albatrossAuthToken);
        assertThat(reject.getStatusCode(), is(200));

        Response response = reopenReimbursement(timesheetID, reimbursementID, REOPEN_REMARK, albatrossAuthToken + "123å");
        assertThat(response.getStatusCode(), is(401));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Unauthorised access"));
    }

    @Test(dataProvider = "buildAgencyData")
    public void reopenReimbursementWithInvoiceLinkedId(int timesheetId, int reimbursementId, String albatrossAuthToken) {
        Response response = reopenReimbursement(timesheetId, reimbursementId, REOPEN_REMARK, albatrossAuthToken);
        assertThat(response.statusCode(), is(409));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), nullValue());
        assertThat(jp.getString("errors[0].message"), is("Reimbursement cannot be modified while an invoice is linked to this timesheet"));
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
