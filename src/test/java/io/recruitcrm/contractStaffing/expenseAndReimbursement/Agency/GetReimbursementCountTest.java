package io.recruitcrm.contractStaffing.expenseAndReimbursement.Agency;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.ContractStaffing.*;
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
public class GetReimbursementCountTest extends ContractStaffingBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
    }

    @Test(dataProvider = "timesheetWithoutReimbursementsData")
    public void getReimbursementCountWithoutReimbursements_Test(int timesheetId) {
        Response response = getReimbursementCount(timesheetId, albatrossAuthToken);
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

    @Test(dataProvider = "timesheetWithReimbursementsData")
    public void getReimbursementCount_Test(int timesheetId) {
        Response response = getReimbursementCount(timesheetId, albatrossAuthToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Reimbursement count fetched successfully"));
        assertThat(jp.getInt("data"), is(2));
    }

    @Test
    public void getReimbursementCountWithInvalidTimesheetId() {
        int nonExistentTimesheetId = JavaFakerReimbursement.generateFakerId();
        Response response = getReimbursementCount(nonExistentTimesheetId, albatrossAuthToken);
        assertThat(response.getStatusCode(), is(404));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), nullValue());
        assertThat(jp.getString("errors[0].message"), is("Timesheet id " + nonExistentTimesheetId + " not found."));
        assertThat(jp.getInt("meta.status"), is(404));
        assertThat(jp.get("data"), nullValue());
        Object responseType = jp.get("meta.responseType");
        if (responseType instanceof String) {
            assertThat((String) responseType, is("ERROR"));
        }
    }

    @Test
    public void getReimbursementCountWithNegativeTimesheetId() {
        Response response = getReimbursementCount(0, albatrossAuthToken);
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
    public Object[][] timesheetWithReimbursementsData() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 1);
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);

        createReimbursement("Expense A", 10.00, "test.pdf", timesheetIDs.get(0), albatrossAuthToken);
        createReimbursement("Expense B", 20.50, "test.pdf", timesheetIDs.get(0), albatrossAuthToken);
        return new Object[][] { { timesheetIDs.get(0) } };
    }

    public Response getReimbursementCount(int timesheetId, String authToken) {
        return RestClient.doGet("JSON", timesheetBaseURL,
                        "timesheets/" + timesheetId + "/reimbursements/count",
                        authToken, null, null, true);
    }
}
