package io.recruitcrm.contractStaffing.expenseAndReimbursement.Agency;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.ContractStaffing.JavaFakerReimbursement;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class GetReimbursementList_Test extends ContractStaffingBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
    }

    @Test(dataProvider = "timesheetWithoutReimbursementsData")
    public void getReimbursementListWithoutReimbursement_Test(int timesheetId) {
        Response response = listReimbursements(timesheetId, albatrossAuthToken);
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

    @Test(dataProvider = "timesheetWithReimbursementsMixedStatusData")
    public void getReimbursements_Test(int timesheetId, int reimbSubmittedId, int reimbApprovedId, int reimbRejectedId) {
        Response response = listReimbursements(timesheetId, albatrossAuthToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Reimbursements fetched successfully"));
        assertThat(jp.getInt("meta.status"), is(200));

        List<Object> data = jp.getList("data");
        assertThat(data.size(), is(3));

        Integer statusForSubmitted = null;
        Integer statusForApproved = null;
        Integer statusForRejected = null;
        for (int i = 0; i < data.size(); i++) {
            int id = jp.getInt("data[" + i + "].id");
            int status = jp.getInt("data[" + i + "].status");
            assertThat(jp.getInt("data[" + i + "].timesheetId"), is(timesheetId));
            assertThat(jp.get("data[" + i + "].addedOn"), notNullValue());
            if (id == reimbSubmittedId) {
                statusForSubmitted = status;
                assertThat(jp.getString("data[" + i + "].description"), is("Travel expenses"));
                assertThat(jp.getDouble("data[" + i + "].amount"), is(125.50));
                assertThat(jp.getInt("data[" + i + "].isPayable"), is(0));
                assertThat(jp.getInt("data[" + i + "].isBillable"), is(0));
            } else if (id == reimbApprovedId) {
                statusForApproved = status;
                assertThat(jp.getString("data[" + i + "].description"), is("Parking fee"));
                assertThat(jp.getDouble("data[" + i + "].amount"), is(20.00));
                assertThat(jp.getInt("data[" + i + "].isPayable"), is(1));
                assertThat(jp.getInt("data[" + i + "].isBillable"), is(1));
            } else if (id == reimbRejectedId) {
                statusForRejected = status;
                assertThat(jp.getString("data[" + i + "].description"), is("Parking fee"));
                assertThat(jp.getDouble("data[" + i + "].amount"), is(20.00));
                assertThat(jp.getInt("data[" + i + "].isPayable"), is(0));
                assertThat(jp.getInt("data[" + i + "].isBillable"), is(0));
            }
        }
        assertThat(statusForSubmitted, is(1));
        assertThat(statusForApproved, is(2));
        assertThat(statusForRejected, is(3));
    }

    @Test
    public void getReimbursementsWithInvalidTimesheetId() {
        int invalidTimesheetId = JavaFakerReimbursement.generateFakerId();
        Response response = listReimbursements(invalidTimesheetId, albatrossAuthToken);
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

    @Test
    public void getReimbursementListWithNegativeTimesheetId_Test() {
        Response response = listReimbursements(-1, albatrossAuthToken);
        assertThat(response.getStatusCode(), is(404));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), nullValue());
        assertThat(jp.getString("errors[0].message"), is("Timesheet id -1 not found."));
        assertThat(jp.getInt("meta.status"), is(404));
        assertThat(jp.get("data"), nullValue());
    }

    @Test(dataProvider = "timesheetWithReimbursementsReopenedData")
    public void getReimbursementsWithReopenedReimbursement_Test(int timesheetId, int reimbApprovedId) {
        Response response = listReimbursements(timesheetId, albatrossAuthToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Reimbursements fetched successfully"));
        assertThat(jp.getInt("meta.status"), is(200));
        List<Object> data = jp.getList("data");
        assertThat(data.size(), is(1));
        int id = jp.getInt("data[0].id");
        int status = jp.getInt("data[0].status");
        assertThat(jp.getString("data[0].description"), is("Parking fee"));
        assertThat(jp.getDouble("data[0].amount"), is(20.00));
        assertThat(jp.getInt("data[0].isPayable"), is(0));
        assertThat(jp.getInt("data[0].isBillable"), is(0));
        assertThat(status, is(1));
    }

    @Test(dataProvider = "timesheetWithoutReimbursementsData")
    public void getReimbursementWithInvalidTkn(int timesheetId) {
        Response response = listReimbursements(timesheetId, albatrossAuthToken + "invalid");
        assertThat(response.getStatusCode(), is(401));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Unauthorised access"));
    }

    @DataProvider(parallel = true)
    public Object[][] timesheetWithoutReimbursementsData() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 1);
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);

        return new Object[][] { { timesheetIDs.get(0) } };
    }

    @DataProvider(parallel = true)
    public Object[][] timesheetWithReimbursementsMixedStatusData() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 1);
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);
        int timesheetId = timesheetIDs.get(0);

        int reimbSubmittedId = createReimbursement("Travel expenses", 125.50, "test.pdf", timesheetId,
                albatrossAuthToken);
        int reimbApprovedId = createReimbursement("Parking fee", 20.00, "test.pdf", timesheetId, albatrossAuthToken);
        Response approve = updateReimbursementStatus(timesheetId, reimbApprovedId, "approve","Approved parking", albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));
        int reimbRejectedId = createReimbursement("Parking fee", 20.00, "test.pdf", timesheetId, albatrossAuthToken);
        Response reject = updateReimbursementStatus(timesheetId, reimbRejectedId, "reject",
                "Rejected parking", albatrossAuthToken);
        assertThat(reject.getStatusCode(), is(200));

        return new Object[][] { { timesheetId, reimbSubmittedId, reimbApprovedId, reimbRejectedId } };
    }

    @DataProvider(parallel = true)
    public Object[][] timesheetWithReimbursementsReopenedData() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 1);
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);
        int timesheetId = timesheetIDs.get(0);

        int reimbApprovedId = createReimbursement("Parking fee", 20.00, "test.pdf", timesheetId, albatrossAuthToken);
        Response approve = updateReimbursementStatus(timesheetId, reimbApprovedId, "approve", "Approved parking", albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));
        Response reopen = reopenReimbursement(timesheetId, reimbApprovedId, "Reopened parking", albatrossAuthToken);
        assertThat(reopen.getStatusCode(), is(200));
        
        return new Object[][] { { timesheetId, reimbApprovedId } };
    }

    public Response listReimbursements(int timesheetId, String authToken) {
        return RestClient.doGet("JSON", timesheetBaseURL, "timesheets/" + timesheetId + "/reimbursements", authToken, null, null, true);
    }
}
