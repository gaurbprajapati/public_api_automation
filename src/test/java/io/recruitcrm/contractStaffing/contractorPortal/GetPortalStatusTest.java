package io.recruitcrm.contractStaffing.contractorPortal;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.ContractStaffing.TimesheetFaker;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import io.rcrm.api.testbase.TestBase;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class GetPortalStatusTest extends ContractStaffingBaseTest {

    String albatrossAuthToken;
    String apiAuthToken;
    int ownerAccountID;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    TimesheetFaker timesheetFaker;
    String basePath = "contract-staffing/contractor/get-portal-status";

    @BeforeClass(alwaysRun = true)
    public void Setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        apiAuthToken = ThreadManager.getAccountApiKey();
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        timesheetFaker = new TimesheetFaker();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPortalStatusDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void getPortalStatusTest(int contractorId, int recruiterUserId) {
        Response response = getPortalStatus(contractorId, albatrossAuthToken);

        assertThat("Status code for Get portal status should be 200 but got " + response.statusCode(), response.statusCode(), is(200));
        JsonPath jsonPath = response.jsonPath();
        assertThat("data should not be null", jsonPath.get("data"), notNullValue());
        assertThat("Portal status should be present", jsonPath.get("data.portalStatus"), is(1));
        assertThat("Invite count should be 1", jsonPath.get("data.inviteCount"), is(1));
        assertThat("Last invite sent at should not be null", jsonPath.get("data.lastInviteSentAt"), notNullValue());
        assertThat("UpdatedBy should be " + recruiterUserId, jsonPath.get("data.updatedBy"), equalTo(recruiterUserId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/contractStaffing/GetPortalStatus.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void getPortalStatusWithMissingContractorIdTest() {
        Map<String, String> emptyParams = new HashMap<>();
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, emptyParams, null, true);

        assertThat("Status code for Missing contractor ID should be 400 or 422 but got " + response.statusCode(), response.statusCode(), is(422));
        assertThat("Message  should be The contractor id field is required. but got " + response.jsonPath().getString("message"), response.jsonPath().getString("message"), is("The contractor id field is required."));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPortalStatusDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void unauthorizedUserCannotGetPortalStatusTest(int contractorId, int recruiterUserId) {
        Response response = getPortalStatus(contractorId, albatrossAuthToken + "123");
        
        assertThat("Status code for Unauthorized user should be 401 but got " + response.statusCode(), response.statusCode(), is(401));
        assertThat("Error for Unauthorized user should be Unauthorized but got " + response.jsonPath().getString("error"), response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void getPortalStatusWithInvalidContractorIdTest() {
        Response response = getPortalStatus(timesheetFaker.generateFakerId(), albatrossAuthToken);

        assertThat("Status code for Invalid contractor ID should be 200 but got " + response.statusCode(), response.statusCode(), is(200));
        assertThat("Message for Invalid contractor ID should be Portal status not found for the given contractor but got " + response.jsonPath().getString("message"), response.jsonPath().getString("message"), is("Portal status not found for the given contractor."));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void getPortalStatusWithEmptyContractorIdTest() {
        Map<String, String> emptyParams = new HashMap<>();
        emptyParams.put("contractorId", "");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, emptyParams, null, true);

        assertThat("Status code for Empty contractor ID should be 422 but got " + response.statusCode(), response.statusCode(), is(422));
        assertThat("Message should be The contractor id field is required. but got " + response.jsonPath().getString("message"), response.jsonPath().getString("message"), is("The contractor id field is required."));
        assertThat("data should be empty", response.jsonPath().get("data.size()"), is(0));
    }

    public Response getPortalStatus(int contractorId, String authToken) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("contractorId", String.valueOf(contractorId));
        return RestClient.doGet("JSON", albatrossURL, basePath, authToken, queryParameters, null, true);
    }

    @DataProvider
    public Object[][] getPortalStatusDataProvider() {
        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String candidateSlug = jsonCandidate.getString("slug");
        String firstName = jsonCandidate.getString("first_name");
        String lastName = jsonCandidate.getString("last_name");
        String contractorEmail = jsonCandidate.getString("email");
        int contractorId = function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug, "candidate").jsonPath().getInt("data.candidate.id");

        Response usersResponse = function.getUsers(baseURL, apiAuthToken);
        usersResponse.then().statusCode(200);
        int recruiterUserId = usersResponse.jsonPath().getInt("[0].id");
        String recruiterName = usersResponse.jsonPath().getString("[0].first_name") + " " + usersResponse.jsonPath().getString("[0].last_name");
        
        String jobSlug = function.getEntityResponse(baseURL, apiAuthToken, "job");
        int jobId = allCrudFunctions.getJobResponse(albatrossURL, albatrossAuthToken, jobSlug).jsonPath().getInt("data.job.id");
        function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug, jobSlug);

        enableTimesheet(contractorId, jobId, recruiterUserId, albatrossAuthToken, 2, 200, 0);

        EnablePortal(contractorId,firstName,lastName,contractorEmail,recruiterUserId,recruiterName, albatrossAuthToken);

        return new Object[][] {
                { contractorId, recruiterUserId }
        };
    }
}
