package io.recruitcrm.contractStaffing.expenseAndReimbursement.Agency;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.ContractStaffing.*;
import io.rcrm.api.pojo.albatross.contractStaffing.CreateReimbursementRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class UpdateReimbursementTest extends ExpenseAndReimbursementBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
    }

    @Test(dataProvider = "addTimeSheetData")
    public void updateReimbursement_Test(int timesheetID, int reimbursementID) {
        Response uploadResponse = uploadReimbursementDocument("ticket-v2.pdf", timesheetID, albatrossAuthToken);
        assertThat(uploadResponse.getStatusCode(), is(200));
        JsonPath uploadJson = uploadResponse.jsonPath();
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Train ticket to client site (updated)")
                .amount(48.00)
                .documentToken(uploadJson.getString("data.documentToken"))
                .fileName(uploadJson.getString("data.documentFileName"))
                .build();
        Response response = updateReimbursement(timesheetID, reimbursementID, request, albatrossAuthToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Reimbursement updated successfully"));
        assertThat(jp.getString("meta.responseType.context"), is("Request is successful"));
        assertThat(jp.getInt("meta.responseType.code"), is(103));
        assertThat(jp.getInt("data.id"), is(reimbursementID));
        assertThat(jp.getInt("data.timesheetId"), is(timesheetID));
        assertThat(jp.getString("data.description"), is("Train ticket to client site (updated)"));
        assertThat(jp.getDouble("data.amount"), is(48.00));
        assertThat(jp.getInt("data.status"), is(1));
        assertThat(jp.getString("data.statusLabel"), is("Submitted"));
        assertThat(jp.getInt("data.isPayable"), is(0));
        assertThat(jp.getInt("data.isBillable"), is(0));
    }

    @Test(dataProvider = "addTimeSheetDataWithApprovedReimbursement")
    public void updateReimbursementWithApprovedId(int timesheetID, int reimbursementID) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Attempt update after approve")
                .amount(10.00)
                .documentToken("receipts/x.pdf")
                .fileName("x.pdf")
                .build();
        Response response = updateReimbursement(timesheetID, reimbursementID, request, albatrossAuthToken);
        assertUpdateReimbursementError(response, 400, "Reimbursement can only be updated when in Submitted status");
    }

    @Test(dataProvider = "addTimeSheetDataWithRejectedReimbursement")
    public void updateReimbursementWithRejectedId(int timesheetID, int reimbursementID) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Attempt update after reject")
                .amount(10.00)
                .documentToken("receipts/x.pdf")
                .fileName("x.pdf")
                .build();
        Response response = updateReimbursement(timesheetID, reimbursementID, request, albatrossAuthToken);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Reimbursement updated successfully"));
        assertThat(jp.getString("meta.responseType.context"), is("Request is successful"));
        assertThat(jp.getInt("meta.responseType.code"), is(103));
        assertThat(jp.getInt("data.id"), is(reimbursementID));
        assertThat(jp.getInt("data.timesheetId"), is(timesheetID));
        assertThat(jp.getString("data.description"), is("Attempt update after reject"));
        assertThat(jp.getDouble("data.amount"), is(10.00));
        assertThat(jp.getInt("data.status"), is(1));
        assertThat(jp.getString("data.statusLabel"), is("Submitted"));
        assertThat(jp.getInt("data.isPayable"), is(0));
        assertThat(jp.getInt("data.isBillable"), is(0));
    }

    @Test(dataProvider = "addTimeSheetData")
    public void updateReimbursementWithInvalidReimbursementId(int timesheetID, int reimbursementID) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Train ticket to client site (updated)")
                .amount(48.00)
                .documentToken("receipts/xyz-v2.pdf")
                .fileName("ticket-v2.pdf")
                .build();
        int invalidReimbursementId = JavaFakerReimbursement.generateFakerId();
        Response response = updateReimbursement(timesheetID, invalidReimbursementId, request,
                albatrossAuthToken);
        assertUpdateReimbursementError(response, 404, "TimesheetReimbursement id " + invalidReimbursementId + " not found.");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void updateReimbursementWithInvalidTimesheetId(int timesheetID, int reimbursementID) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Train ticket to client site (updated)")
                .amount(48.00)
                .documentToken("receipts/xyz-v2.pdf")
                .fileName("ticket-v2.pdf")
                .build();
        int invalidTimesheetId = JavaFakerReimbursement.generateFakerId();
        Response response = updateReimbursement(invalidTimesheetId, reimbursementID, request,
                albatrossAuthToken);
        assertUpdateReimbursementError(response, 404, "TimesheetReimbursement id " + reimbursementID + " not found.");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void updateReimbursementWithBlankDescription(int timesheetID, int reimbursementID) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("")
                .amount(48.00)
                .documentToken("receipts/xyz-v2.pdf")
                .fileName("ticket-v2.pdf")
                .build();
        Response response = updateReimbursement(timesheetID, JavaFakerReimbursement.generateFakerId(), request,
                albatrossAuthToken);
        assertUpdateReimbursementErrorRequiredFields(response, 400, "Description must be between 1 and 100 characters");
    }

    @Test(dataProvider = "addTimeSheetData")
    public void updateReimbursementWithBlankAmount(int timesheetID, int reimbursementID) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Train ticket to client site (updated)")
                .amount(0.00)
                .documentToken("receipts/xyz-v2.pdf")
                .fileName("ticket-v2.pdf")
                .build();
        Response response = updateReimbursement(timesheetID, JavaFakerReimbursement.generateFakerId(), request,
                albatrossAuthToken);
        assertUpdateReimbursementErrorRequiredFields(response, 400, "Amount must be greater than 0");
    }

    @Test(dataProvider = "buildAgencyData")
    public void updateReimbursementWithInvoiceLinkedId(int timesheetID, int reimbursementID, String albatrossAuthToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Train ticket to client site (updated)")
                .amount(1.00)
                .documentToken("receipts/xyz-v2.pdf")
                .fileName("ticket-v2.pdf")
                .build();
        Response response = updateReimbursement(timesheetID, reimbursementID, request, albatrossAuthToken);
        assertThat(response.statusCode(), is(409));
        JsonPath updateReimbursementJsonPath = response.jsonPath();
        assertThat(updateReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(updateReimbursementJsonPath.getString("errors[0].message"), is("Reimbursement cannot be modified while an invoice is linked to this timesheet"));
    }

    private void assertUpdateReimbursementError(Response response, int expectedStatus, String expectedMessage) {
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

    private void assertUpdateReimbursementErrorRequiredFields(Response response, int expectedStatus, String expectedMessage) {
        assertThat(response.getStatusCode(), is(expectedStatus));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is(expectedMessage));
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

    @DataProvider(parallel = true)
    public Object[][] addTimeSheetDataWithApprovedReimbursement() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200,1);
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);
        int reimbursementID = createReimbursement("1 Lunch outside", 40.00, "test.pdf", timesheetIDs.get(0),
                albatrossAuthToken);
        updateReimbursementStatus(timesheetIDs.get(0), reimbursementID, "approve", "Approved by manager - valid expense", albatrossAuthToken);
        return new Object[][] { { timesheetIDs.get(0), reimbursementID } };
    }

    @DataProvider(parallel = true)
    public Object[][] addTimeSheetDataWithRejectedReimbursement() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();
        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200,1);
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);
        int reimbursementID = createReimbursement("1 Lunch outside", 40.00, "test.pdf", timesheetIDs.get(0),
                albatrossAuthToken);
        updateReimbursementStatus(timesheetIDs.get(0), reimbursementID, "reject", "Rejected - insufficient documentation provided", albatrossAuthToken);
        return new Object[][] { { timesheetIDs.get(0), reimbursementID } };
    }

    public Response updateReimbursement(int timesheetId, int reimbursementId, CreateReimbursementRequest request, String authToken) {

        return RestClient.doPatchOnce("JSON", timesheetBaseURL, "timesheets/" + timesheetId + "/reimbursements/" + reimbursementId,
                authToken, null, true, request);
    }
}
