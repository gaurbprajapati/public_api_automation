package io.recruitcrm.contractStaffing.expenseAndReimbursement.Agency;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.ContractStaffing.JavaFakerReimbursement;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class DeleteReimbursementTest extends ExpenseAndReimbursementBaseTest {

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
    public void deleteReimbursement_Test(int jobId, int candidateId, int userId, int timesheetID, int reimbursementID) {
        Response deleteReimbursementResponse = deleteReimbursement(timesheetID, reimbursementID, albatrossAuthToken);
        assertThat(deleteReimbursementResponse.statusCode(), is(200));
        JsonPath deleteReimbursementJsonPath = deleteReimbursementResponse.jsonPath();
        assertThat(deleteReimbursementJsonPath.getString("meta.message"), is("Reimbursement deleted successfully")); 
    }

    @Test(dataProvider = "addTimeSheetDataWithoutReimbursement")
    public void deleteReimbursementWithInvalidReimbursementID(int jobId, int candidateId, int userId, int timesheetID) {
        int invalidReimbursementId = fakerReimbursement.generateFakerId();
        Response deleteReimbursementResponse = deleteReimbursement(timesheetID, invalidReimbursementId, albatrossAuthToken);
        assertThat(deleteReimbursementResponse.statusCode(), is(404));
        JsonPath deleteReimbursementJsonPath = deleteReimbursementResponse.jsonPath();
        assertThat(deleteReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(deleteReimbursementJsonPath.getString("errors[0].message"), is("TimesheetReimbursement id " + invalidReimbursementId + " not found."));
    }

    @Test(dataProvider = "addTimeSheetData")
    public void deleteReimbursementWithInvalidTimesheetID(int jobId, int candidateId, int userId, int timesheetID, int reimbursementID) {
        int invalidTimesheetId = fakerReimbursement.generateFakerId();
        Response deleteReimbursementResponse = deleteReimbursement(invalidTimesheetId, reimbursementID, albatrossAuthToken);
        assertThat(deleteReimbursementResponse.statusCode(), is(404));
        JsonPath deleteReimbursementJsonPath = deleteReimbursementResponse.jsonPath();
        assertThat(deleteReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(deleteReimbursementJsonPath.getString("errors[0].message"), is("TimesheetReimbursement id " + reimbursementID + " not found."));
    }

    @Test
    public void deleteReimbursementWithInvalidTkn() {
        Response deleteReimbursementResponse = deleteReimbursement(fakerReimbursement.generateFakerId(), fakerReimbursement.generateFakerId(), albatrossAuthToken + "123");
        assertThat(deleteReimbursementResponse.statusCode(), is(401));
        JsonPath deleteReimbursementJsonPath = deleteReimbursementResponse.jsonPath();
        assertThat(deleteReimbursementJsonPath.getString("meta.message"), is("Unauthorised access"));
    }

    @Test(dataProvider = "addTimeSheetDataWithApprovedReimbursement")
    public void deleteApprovedReimbursement_Test(int jobId, int candidateId, int userId, int timesheetID, int reimbursementID) {
        Response deleteReimbursementResponse = deleteReimbursement(timesheetID, reimbursementID, albatrossAuthToken);
        assertThat(deleteReimbursementResponse.statusCode(), is(200));
        JsonPath deleteReimbursementJsonPath = deleteReimbursementResponse.jsonPath();
        assertThat(deleteReimbursementJsonPath.getString("meta.message"), is("Reimbursement deleted successfully")); 
    }

    @Test(dataProvider = "buildAgencyData")
    public void deleteReimbursementWithInvoiceLinkedId(int timesheetId, int reimbursementId, String albatrossAuthToken) {
        Response deleteReimbursementResponse = deleteReimbursement(timesheetId, reimbursementId, albatrossAuthToken);
        assertThat(deleteReimbursementResponse.statusCode(), is(409));
        JsonPath deleteReimbursementJsonPath = deleteReimbursementResponse.jsonPath();
        assertThat(deleteReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(deleteReimbursementJsonPath.getString("errors[0].message"), is("Reimbursement cannot be modified while an invoice is linked to this timesheet"));
    }

    @DataProvider(parallel = true)
    public Object[][] addTimeSheetData() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 1);
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);
        int reimbursementID = createReimbursement("1 Lunch outside", 40.00, "test.pdf", timesheetIDs.get(0), albatrossAuthToken);
        return new Object[][]{
                {jobId, candidateId, userId, timesheetIDs.get(0), reimbursementID},
        };
    }

    @DataProvider(parallel = true)
    public Object[][] addTimeSheetDataWithoutReimbursement() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();
        
        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 0);
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);
        return new Object[][]{
                {jobId, candidateId, userId, timesheetIDs.get(0)},
        };
    }

    @DataProvider(parallel = true)
    public Object[][] addTimeSheetDataWithApprovedReimbursement() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 1);
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);
        int reimbursementID = createReimbursement("1 Lunch outside", 40.00, "test.pdf", timesheetIDs.get(0), albatrossAuthToken);
        updateReimbursementStatus(timesheetIDs.get(0), reimbursementID, "approve", "Approved by manager - valid expense", albatrossAuthToken);
        return new Object[][]{
                {jobId, candidateId, userId, timesheetIDs.get(0), reimbursementID},
        };
    }

    public Response deleteReimbursement(int timesheetID, int reimbursementID, String authToken) {
        return RestClient.doDelete("JSON", timesheetBaseURL, "timesheets/" + timesheetID + "/reimbursements/" + reimbursementID, authToken, null, null, true);
    }
}
