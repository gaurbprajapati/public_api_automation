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

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class CreateReimbursementInContractorPortalTest extends InvoiceBaseTest {

    @Test(dataProvider = "addContractorPortalCreateReimbursementData")
    public void createReimbursementWithContractorTkn(int jobId, int candidateId, int userId, int timesheetID,
            String documentToken, String documentFileName, String portalToken) {
        CreateReimbursementRequest createReimbursementRequest = CreateReimbursementRequest.builder()
                .description("1 Lunch outside")
                .amount(40.00)
                .documentToken(documentToken)
                .fileName(documentFileName)
                .build();
        Response createReimbursementResponse = createReimbursement(timesheetID, createReimbursementRequest, portalToken);
        assertThat(createReimbursementResponse.statusCode(), is(201));
        JsonPath createReimbursementJsonPath = createReimbursementResponse.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), is("Reimbursement created successfully"));
        assertThat(createReimbursementJsonPath.getInt("data.id"), greaterThan(0));
        assertThat(createReimbursementJsonPath.getInt("data.status"), is(1));
        assertThat(createReimbursementJsonPath.getDouble("data.amount"), is(40.00));
        assertThat(createReimbursementJsonPath.getInt("data.isPayable"), is(0));
        assertThat(createReimbursementJsonPath.getInt("data.isBillable"), is(0));
        assertThat(createReimbursementJsonPath.get("data.addedOn"), notNullValue());
    }

    @Test(dataProvider = "addContractorPortalCreateReimbursementData")
    public void createReimbursementWithInvalidTkn(int jobId, int candidateId, int userId, int timesheetID,
            String documentToken, String documentFileName, String portalToken) {
        CreateReimbursementRequest createReimbursementRequest = CreateReimbursementRequest.builder()
                .description("1 Lunch outside")
                .amount(40.00)
                .build();
        Response createReimbursementResponse = createReimbursement(timesheetID, createReimbursementRequest,
                portalToken + "123");
        assertThat(createReimbursementResponse.statusCode(), is(401));
        JsonPath createReimbursementJsonPath = createReimbursementResponse.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), is("Unauthorised access"));
    }

    @Test(dataProvider = "addContractorPortalCreateReimbursementData")
    public void createReimbursementWithBlankDescription_Contractor(int jobId, int candidateId, int userId,
            int timesheetID,
            String documentToken, String documentFileName, String portalToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("")
                .amount(40.00)
                .build();
        Response response = createReimbursement(timesheetID, request, portalToken);
        assertThat(response.statusCode(), is(400));
        JsonPath createReimbursementJsonPath = response.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), is("Description cannot be blank"));
    }

    @Test(dataProvider = "crossContractorPortalAnotherTimesheetForCreateData")
    public void createReimbursementWithAnotherContractorTimesheetId_Contractor(int otherContractorTimesheetId,
            String documentToken, String documentFileName, String portalToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("1 Lunch outside")
                .amount(40.00)
                // .documentToken(documentToken)
                // .fileName(documentFileName)
                .build();
        Response response = createReimbursement(otherContractorTimesheetId, request, portalToken);
        assertThat(response.statusCode(), is(404));
        assertThat(response.jsonPath().getString("meta.message"), nullValue());
        assertThat(response.jsonPath().getString("errors[0].message"), is("Timesheet id " + otherContractorTimesheetId + " not found."));
    }

    @Test(dataProvider = "addContractorPortalCreateReimbursementData")
    public void createReimbursementWithBlankAmount_Contractor(int jobId, int candidateId, int userId, int timesheetID,
            String documentToken, String documentFileName, String portalToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("1 Lunch outside")
                .amount(0.00)
                // .documentToken(documentToken)
                // .fileName(documentFileName)
                .build();
        Response response = createReimbursement(timesheetID, request, portalToken);
        assertThat(response.statusCode(), is(400));
        JsonPath createReimbursementJsonPath = response.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), is("Amount must be greater than 0"));
    }

    @Test(dataProvider = "addContractorPortalCreateReimbursementData")
    public void createReimbursementWithInvalidTimesheetId(int jobId, int candidateId, int userId, int timesheetID,
            String documentToken, String documentFileName, String portalToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("1 Lunch outside")
                .amount(40.00)
                // .documentToken(documentToken)
                // .fileName(documentFileName)
                .build();
        int invalidTimesheetId = JavaFakerReimbursement.generateFakerId();
        Response response = createReimbursement(invalidTimesheetId, request, portalToken);
        assertThat(response.statusCode(), is(404));
        JsonPath createReimbursementJsonPath = response.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(createReimbursementJsonPath.getString("errors[0].message"), is("Timesheet id " + invalidTimesheetId + " not found."));
    }

    @Test(dataProvider = "addContractorPortalCreateReimbursementData")
    public void createReimbursementWithEmptyBody_Contractor(int jobId, int candidateId, int userId, int timesheetID,
            String documentToken, String documentFileName, String portalToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder().build();
        Response response = createReimbursement(timesheetID, request, portalToken);
        assertThat(response.statusCode(), is(400));
        assertThat(response.jsonPath().getString("meta.message"), is("Request body is empty"));
    }

    @Test(dataProvider = "addContractorPortalCreateReimbursementDisabledData")
    public void createReimbursementWithReimbursementDisabled_Contractor(int jobId, int candidateId, int userId,
            int timesheetID,
            String documentToken, String documentFileName, String portalToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("1 Lunch outside")
                .amount(40.00)
                .build();
        Response response = createReimbursement(timesheetID, request, portalToken);
        assertThat(response.statusCode(), is(400));
        JsonPath createReimbursementJsonPath = response.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(createReimbursementJsonPath.getString("errors[0].message"), is("Reimbursements are not enabled for this timesheet"));
    }

    @Test(dataProvider = "buildPortalData")
    public void createReimbursementWithInvoiceLinkedId_Contractor(int timesheetId, int reimbursementId, String portalToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("1 Lunch outside")
                .amount(40.00)
                .build();
        Response response = createReimbursement(timesheetId, request, portalToken);
        assertThat(response.statusCode(), is(409));
        JsonPath createReimbursementJsonPath = response.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(createReimbursementJsonPath.getString("errors[0].message"), is("Reimbursement cannot be modified while an invoice is linked to this timesheet"));
    }

    public Response createReimbursement(int timesheetID, CreateReimbursementRequest createReimbursementRequest,
            String authToken) {
        return RestClient.doPostOnce("JSON", timesheetBaseURL, "timesheets/" + timesheetID + "/reimbursements", authToken,
                null, true, createReimbursementRequest);
    }

    @DataProvider(parallel = true)
    public Object[][] addContractorPortalCreateReimbursementData() {
        Object[] row = buildContractorPortalData(1, false);
        int jobId = (Integer) row[0];
        int candidateId = (Integer) row[1];
        int userId = (Integer) row[2];
        int timesheetID = (Integer) row[3];
        String portalToken = (String) row[5];

        // Response uploadReimbursementDocumentResponse = uploadReimbursementDocument("test.pdf", timesheetID,
        //         albatrossAuthToken);
        // assertThat(uploadReimbursementDocumentResponse.statusCode(), is(200));
        // JsonPath uploadJson = uploadReimbursementDocumentResponse.jsonPath();
        // String documentToken = uploadJson.getString("data.documentToken");
        // String documentFileName = uploadJson.getString("data.documentFileName");

        return new Object[][] {
                { jobId, candidateId, userId, timesheetID, null, null, portalToken },
        };
    }

    @DataProvider(parallel = true)
    public Object[][] addContractorPortalCreateReimbursementDisabledData() {
        Object[] row = buildContractorPortalData(0, false);
        int jobId = (Integer) row[0];
        int candidateId = (Integer) row[1];
        int userId = (Integer) row[2];
        int timesheetID = (Integer) row[3];
        String portalToken = (String) row[5];

        // Response uploadReimbursementDocumentResponse = uploadReimbursementDocument("test.pdf", timesheetID,
        //         albatrossAuthToken);
        // assertThat(uploadReimbursementDocumentResponse.statusCode(), is(200));
        // JsonPath uploadJson = uploadReimbursementDocumentResponse.jsonPath();
        // String documentToken = uploadJson.getString("data.documentToken");
        // String documentFileName = uploadJson.getString("data.documentFileName");

        return new Object[][] {
                { jobId, candidateId, userId, timesheetID, null, null, portalToken },
        };
    }
}
