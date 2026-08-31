package io.recruitcrm.contractStaffing.expenseAndReimbursement.ContractorPortal;

import com.qa.api.util.reaper.PortalThreadManager;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.ContractStaffing.JavaFakerPayBillStatus;
import io.rcrm.api.javafaker.ContractStaffing.JavaFakerReimbursement;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import org.testng.annotations.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * VMS contractor portal ({@code createPortalAccountAndLogin(3, ...)} + contractor bearer token).
 * Parallel to {@link io.recruitcrm.contractStaffing.expenseAndReimbursement.ClientPortal.ClientPortalExpenseReimbursementBaseTest}
 * which uses client portal ({@code entityType == 1}).
 */
public abstract class ContractorPortalExpenseReimbursementBaseTest extends ContractStaffingBaseTest {

    protected String albatrossAuthToken;
    protected String apiAuthToken;
    protected int accountId;
    protected final AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    @BeforeClass(alwaysRun = true)
    public void contractorPortalExpenseReimbursementBaseSetup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        accountId = ThreadManager.getAccount().getAccountId();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
    }

    protected Object[] buildContractorPortalData(int reimbursementEnabled, boolean createReimbursementLine) {
        String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath()
                .getString("slug");
        String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath()
                .getString("slug");
        String contactSlug = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath()
                .getString("slug");
        String jobSlug = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath()
                .getString("slug");
        function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug, jobSlug);
        int userId = function.getUsers(baseURL, apiAuthToken).jsonPath().getInt("[0].id");
        int candidateId = function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug, "candidate")
                .jsonPath().getInt("data.candidate.id");
        JsonPath jobJsonPath = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job").jsonPath();
        int jobId = jobJsonPath.getInt("data.job.id");

        String email = JavaFakerReimbursement.generateFakerEmail();
        allCrudFunctions.updateCustomField("candidate", albatrossURL, candidateId, albatrossAuthToken, "email", email);
        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, reimbursementEnabled);
        enableVmsLink(jobId, albatrossAuthToken);
        submitCandidateUrl(jobId, albatrossAuthToken);
        createContractorPortalAccountAndLogin(candidateId, "John", "Doe", email, accountId);
        String portalToken = PortalThreadManager.getBearerToken();
        List<Integer> timesheetIDs = createSingleTimesheetForValidationForPortal(jobId, candidateId, 2, portalToken);
        int timesheetId = timesheetIDs.get(0);
        Integer reimbursementId = null;
        if (createReimbursementLine) {
            reimbursementId = createReimbursement("1 Lunch outside", 40.00, "test.pdf", timesheetId,
            portalToken);
        }
        return new Object[] { jobId, candidateId, userId, timesheetId, reimbursementId, portalToken };
    }

    @DataProvider(parallel = true)
    public Object[][] addContractorPortalTimesheetData() {
        Object[] row = buildContractorPortalData(1, true);
        return new Object[][] { {
                row[0], row[1], row[2], row[3], row[4], row[5]
        } };
    }

    @DataProvider(parallel = true)
    public Object[][] addContractorPortalTimesheetIdReimbursementIdTokenData() {
        Object[] row = buildContractorPortalData(1, true);
        return new Object[][] { { row[3], row[4], row[5] } };
    }

    @DataProvider(parallel = true)
    public Object[][] crossContractorPortalOtherUsersResourceData() {
        Object[] rowA = buildContractorPortalData(1, true);
        Object[] rowB = buildContractorPortalData(1, true);
        int timesheetIdA = (Integer) rowA[3];
        int reimbursementIdA = (Integer) rowA[4];
        String portalTokenB = (String) rowB[5];
        return new Object[][] { { timesheetIdA, reimbursementIdA, portalTokenB } };
    }

    /**
     * Marks pay-bill / invoice-side state on the timesheet ({@code PATCH .../timesheets/invoices/{id}/pay-bill-status})
     * so reimbursement mutations can be asserted as blocked in contractor-portal tests.
     */
    protected void markTimesheetPayBillPaidForInvoiceFlow(int timesheetId, String agencyAuthToken) {
        JavaFakerPayBillStatus payBillFaker = new JavaFakerPayBillStatus();
        Map<String, Object> payload = createPayBillStatusPayload(
                payBillFaker.getPayBillTypePaid(),
                payBillFaker.getPayStatusIdPaid(),
                payBillFaker.getPayoutNumber(),
                payBillFaker.getPayoutPaidOnCurrent());
        Response response = updatePayBillStatusWithInvalidData(timesheetId, payload, agencyAuthToken);
        assertThat(response.getStatusCode(), is(200));
    }

    @DataProvider(parallel = true)
    public Object[][] crossContractorPortalWrongTimesheetForUpdateData() {
        Object[] rowA = buildContractorPortalData(1, true);
        Object[] rowB = buildContractorPortalData(1, true);
        return new Object[][] { { (Integer) rowB[3], (Integer) rowA[4], (String) rowA[5] } };
    }

    @DataProvider(parallel = true)
    public Object[][] crossContractorPortalWrongReimbursementIdForUpdateData() {
        Object[] rowA = buildContractorPortalData(1, true);
        Object[] rowB = buildContractorPortalData(1, true);
        return new Object[][] { { (Integer) rowA[3], (Integer) rowB[4], (String) rowA[5] } };
    }

    @DataProvider(parallel = true)
    public Object[][] crossContractorPortalDeleteOtherContractorReimbursementData() {
        Object[] rowA = buildContractorPortalData(1, true);
        Object[] rowB = buildContractorPortalData(1, true);
        return new Object[][] { { (Integer) rowB[3], (Integer) rowB[4], (String) rowA[5] } };
    }

    @DataProvider(parallel = true)
    public Object[][] crossContractorPortalAnotherTimesheetForCreateData() {
        Object[] rowA = buildContractorPortalData(1, false);
        Object[] rowB = buildContractorPortalData(1, false);
        int timesheetB = (Integer) rowB[3];
        String reimbursementTestImagePath = "/src/main/java/io/rcrm/api/testdata/JobsBanner.jpg";
        Path path = Paths.get(reimbursementTestImagePath);
		File file = new File(path.toString());
		String fileName = path.getFileName().toString();
        Response upload = uploadReimbursementDocument(fileName, timesheetB, albatrossAuthToken);
        assertThat(upload.getStatusCode(), is(200));
        JsonPath uploadJson = upload.jsonPath();
        return new Object[][] { {
                timesheetB,
                uploadJson.getString("data.documentToken"),
                uploadJson.getString("data.documentFileName"),
                (String) rowA[5],
        } };
    }

    @DataProvider(parallel = true)
    public Object[][] contractorPortalInvoiceLinkedCreateData() {
        Object[] row = buildContractorPortalData(1, false);
        int timesheetId = (Integer) row[3];
        String portalToken = (String) row[5];
        Response upload = uploadReimbursementDocument("test.pdf", timesheetId, albatrossAuthToken);
        assertThat(upload.getStatusCode(), is(200));
        JsonPath uploadJson = upload.jsonPath();
        markTimesheetPayBillPaidForInvoiceFlow(timesheetId, albatrossAuthToken);
        return new Object[][] { {
                timesheetId,
                uploadJson.getString("data.documentToken"),
                uploadJson.getString("data.documentFileName"),
                portalToken,
        } };
    }

    @DataProvider(parallel = true)
    public Object[][] contractorPortalInvoiceLinkedWithReimbursementData() {
        Object[] row = buildContractorPortalData(1, true);
        int timesheetId = (Integer) row[3];
        int reimbursementId = (Integer) row[4];
        String portalToken = (String) row[5];
        markTimesheetPayBillPaidForInvoiceFlow(timesheetId, albatrossAuthToken);
        return new Object[][] { { timesheetId, reimbursementId, portalToken } };
    }
}
