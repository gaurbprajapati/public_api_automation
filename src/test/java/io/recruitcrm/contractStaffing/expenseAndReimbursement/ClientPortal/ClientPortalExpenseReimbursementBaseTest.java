package io.recruitcrm.contractStaffing.expenseAndReimbursement.ClientPortal;

import com.qa.api.util.reaper.PortalThreadManager;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.ContractStaffing.JavaFakerReimbursement;
import io.restassured.path.json.JsonPath;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import org.testng.annotations.*;

import java.util.*;

public abstract class ClientPortalExpenseReimbursementBaseTest extends ContractStaffingBaseTest {

    protected String albatrossAuthToken;
    protected String apiAuthToken;
    protected int accountId;
    protected int rcrmUserId;
    protected String rcrmEmailID;
    JavaFakerReimbursement fakerReimbursement = new JavaFakerReimbursement();
    protected final AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    @BeforeClass(alwaysRun = true)
    public void clientPortalExpenseReimbursementBaseSetup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        accountId = ThreadManager.getAccount().getAccountId();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
        fakerReimbursement = new JavaFakerReimbursement();
        rcrmUserId = ThreadManager.getOwner().getUserId();
        rcrmEmailID = ThreadManager.getOwner().getEmail();
    }

    
    protected Object[] buildPortalData(int reimbursementEnabled, boolean createReimbursementLine) {
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
        int companyId = jobJsonPath.getInt("data.job.companyid");
        int contactId = jobJsonPath.getInt("data.job.contactid");

        String email = JavaFakerReimbursement.generateFakerEmail();
        allCrudFunctions.updateCustomField("contact", albatrossURL, contactId, albatrossAuthToken, "email", email);

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, reimbursementEnabled);
        enableVmsLink(jobId, albatrossAuthToken);
        submitCandidateUrl(jobId, albatrossAuthToken);
        createClientPortalAccountAndLogin(contactId, "John", "Doe", email, accountId, jobId, true, "Example Company", companyId, rcrmEmailID, rcrmUserId);
        String portalToken = PortalThreadManager.getBearerToken();
        
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);
        int timesheetId = timesheetIDs.get(0);
        Integer reimbursementId = null;
        if (createReimbursementLine) {
            reimbursementId = createReimbursement("1 Lunch outside", 40.00, "test.pdf", timesheetId,
                    albatrossAuthToken);
        }
        return new Object[] { jobId, candidateId, userId, timesheetId, reimbursementId, portalToken };
    }

    @DataProvider(parallel = true)
    public Object[][] addPortalTimesheetData() {
        Object[] row = buildPortalData(1, true);
        return new Object[][] { {
                row[0], row[1], row[2], row[3], row[4], row[5]
        } };
    }

    @DataProvider(parallel = true)
    public Object[][] addPortalTimesheetIdReimbursementIdTokenData() {
        Object[] row = buildPortalData(1, true);
        return new Object[][] { { row[3], row[4], row[5] } };
    }

   
    @DataProvider(parallel = true)
    public Object[][] crossPortalOtherClientsResourceData() {
        Object[] rowA = buildPortalData(1, true);
        Object[] rowB = buildPortalData(1, true);
        int timesheetIdA = (Integer) rowA[3];
        int reimbursementIdA = (Integer) rowA[4];
        String portalTokenB = (String) rowB[5];
        return new Object[][] { { timesheetIdA, reimbursementIdA, portalTokenB } };
    }
}
