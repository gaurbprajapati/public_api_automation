package io.recruitcrm.contractStaffing.expenseAndReimbursement.ContractorPortal;

import io.rcrm.api.javafaker.ContractStaffing.JavaFakerReimbursement;
import io.rcrm.api.pojo.albatross.contractStaffing.CreateReimbursementRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class UpdateReimbursementInContractorPortalTest extends InvoiceBaseTest {

    @Test(dataProvider = "addContractorPortalTimesheetIdReimbursementIdTokenData")
    public void updateReimbursementWithContractorTkn_Contractor(int timesheetID, int reimbursementID,
            String portalToken) {
        String reimbursementTestImagePath = "/src/main/java/io/rcrm/api/testdata/JobsBanner.jpg";
        Path path = Paths.get(reimbursementTestImagePath);
		File file = new File(path.toString());
		String fileName = path.getFileName().toString();
        Response uploadResponse = uploadReimbursementDocument(fileName, timesheetID, albatrossAuthToken);
        assertThat(uploadResponse.getStatusCode(), is(200));
        JsonPath uploadJson = uploadResponse.jsonPath();
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Train ticket to client site (updated)")
                .amount(48.00)
                .documentToken(uploadJson.getString("data.documentToken"))
                .fileName(uploadJson.getString("data.documentFileName"))
                .build();
        Response response = updateReimbursement(timesheetID, reimbursementID, request, portalToken);
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

    @Test(dataProvider = "addContractorPortalTimesheetDataWithApprovedReimbursement")
    public void updateReimbursementWithApprovedId_Contractor(int timesheetID, int reimbursementID,
            String portalToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Attempt update after approve")
                .amount(10.00)
                .documentToken("receipts/x.pdf")
                .fileName("x.pdf")
                .build();
        Response response = updateReimbursement(timesheetID, reimbursementID, request, portalToken);
        assertUpdateReimbursementError(response, 400, "Reimbursement can only be updated when in Submitted status");
    }
    

    @Test(dataProvider = "addContractorPortalTimesheetIdReimbursementIdTokenData")
    public void updateReimbursementWithBlankDescription_Contractor(int timesheetID, int reimbursementID,
            String portalToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("")
                .amount(48.00)
                .documentToken("receipts/xyz-v2.pdf")
                .fileName("ticket-v2.pdf")
                .build();
        Response response = updateReimbursement(timesheetID, reimbursementID, request, portalToken);
        assertUpdateReimbursementValidationError(response, 400, "Description must be between 1 and 100 characters");
    }

    @Test(dataProvider = "addContractorPortalTimesheetIdReimbursementIdTokenData")
    public void updateReimbursementWithBlankAmount_Contractor(int timesheetID, int reimbursementID,
            String portalToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Train ticket to client site (updated)")
                .amount(0.00)
                .documentToken("receipts/xyz-v2.pdf")
                .fileName("ticket-v2.pdf")
                .build();
        Response response = updateReimbursement(timesheetID, reimbursementID, request, portalToken);
        assertUpdateReimbursementValidationError(response, 400, "Amount must be greater than 0");
    }

    @Test(dataProvider = "crossContractorPortalWrongTimesheetForUpdateData")
    public void updateReimbursementWithAnotherContractorTimesheetId_Contractor(int otherContractorTimesheetId,
            int reimbursementIdForFirstContractor, String portalToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Train ticket to client site (updated)")
                .amount(48.00)
                .documentToken("receipts/xyz-v2.pdf")
                .fileName("ticket-v2.pdf")
                .build();
        Response response = updateReimbursement(otherContractorTimesheetId, reimbursementIdForFirstContractor,
                request, portalToken);
        assertUpdateReimbursementError(response, 404, "Timesheet id " + otherContractorTimesheetId + " not found.");
    }

    @Test(dataProvider = "crossContractorPortalWrongReimbursementIdForUpdateData")
    public void updateReimbursementWithAnotherContractorReimbursementId_Contractor(int timesheetId,
            int otherContractorReimbursementId, String portalToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Train ticket to client site (updated)")
                .amount(48.00)
                .documentToken("receipts/xyz-v2.pdf")
                .fileName("ticket-v2.pdf")
                .build();
        Response response = updateReimbursement(timesheetId, otherContractorReimbursementId, request, portalToken);
        assertUpdateReimbursementError(response, 404, "Reimbursement id " + otherContractorReimbursementId + " not found.");
    }

    @Test(dataProvider = "buildPortalData")
    public void updateReimbursementWithInvoiceLinkedId_Contractor(int timesheetId, int reimbursementId,
            String portalToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Attempt update after invoice / pay-bill link")
                .amount(48.00)
                .documentToken("receipts/xyz-v2.pdf")
                .fileName("ticket-v2.pdf")
                .build();
        Response response = updateReimbursement(timesheetId, reimbursementId, request, portalToken);
        assertThat(response.getStatusCode(), is(409));
        JsonPath updateReimbursementJsonPath = response.jsonPath();
        assertThat(updateReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(updateReimbursementJsonPath.getString("errors[0].message"), is("Reimbursement cannot be modified while an invoice is linked to this timesheet"));
    }

    private void assertUpdateReimbursementValidationError(Response response, int expectedStatus, String expectedMessage) {
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

    @DataProvider(parallel = true)
    public Object[][] addContractorPortalTimesheetDataWithApprovedReimbursement() {
        Object[] row = buildContractorPortalData(1, true);
        int timesheetId = (Integer) row[3];
        int reimbursementId = (Integer) row[4];
        String portalToken = (String) row[5];
        Response approve = updateReimbursementStatus(timesheetId, reimbursementId, "approve",
                "Approved by manager - valid expense", albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));
        return new Object[][] { { timesheetId, reimbursementId, portalToken } };
    }

    public Response updateReimbursement(int timesheetId, int reimbursementId, CreateReimbursementRequest request,
            String authToken) {
        return RestClient.doPatchOnce("JSON", timesheetBaseURL,
                "timesheets/" + timesheetId + "/reimbursements/" + reimbursementId, authToken, null, true, request);
    }
}
