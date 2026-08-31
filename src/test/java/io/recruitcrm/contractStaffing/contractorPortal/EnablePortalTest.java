package io.recruitcrm.contractStaffing.contractorPortal;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.contractStaffing.*;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.*;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.javafaker.ContractStaffing.TimesheetFaker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class EnablePortalTest extends ContractStaffingBaseTest {

    String albatrossAuthToken;
    String apiAuthToken;
    int ownerAccountID;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    TimesheetFaker timesheetFaker;

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
    @Test(dataProvider = "updatePortalStatusDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void updatePortalStatusTest(int contractorId, String firstName, String lastName, String contractorEmail, int recruiterUserId, String recruiterName) {
        Response response = updatePortalStatus(firstName, lastName, contractorEmail, recruiterUserId, 1, recruiterName, contractorId, albatrossAuthToken);

        Assert.assertEquals(response.statusCode(), 200);
        JsonPath jsonPath = response.jsonPath();
        String message = jsonPath.getString("message");
        assertThat("Response should contain success message", message, containsString("Portal status updated successfully"));
        assertThat("data.portalStatus should be 1", jsonPath.get("data.portalStatus"), is(1));
        assertThat("data.contractorId should be " + contractorId + " but got " + jsonPath.get("data.contractorId"), jsonPath.get("data.contractorId"), is(contractorId));
        assertThat("data.inviteCount should be 1", jsonPath.get("data.inviteCount"), is(1));
        assertThat("data.lastInviteSentAt should not be null", jsonPath.get("data.lastInviteSentAt"), notNullValue());
        assertThat("data.updatedBy should be " + recruiterUserId, jsonPath.get("data.updatedBy"), is(recruiterUserId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/contractStaffing/UpdatePortalStatus.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void updatePortalStatusWithEmptyRequestBodyTest() {
        Response response = updatePortalStatus("", "", "", 0, 1, "", 0, albatrossAuthToken);

        assertThat("Status code for Empty request body should be 422 but got " + response.statusCode(), response.statusCode(), is(422));
        assertThat("Message for Empty request body is wrong got " + response.jsonPath().getString("message"), response.jsonPath().getString("message"), is("The contractor email field is required.,The first name field is required."));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "updatePortalStatusDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void unauthorizedUserCannotUpdatePortalStatusTest(int contractorId, String firstName, String lastName, String contractorEmail, int recruiterUserId, String recruiterName) {
        Response response = updatePortalStatus(firstName, lastName, contractorEmail, recruiterUserId, 1, recruiterName, contractorId, albatrossAuthToken + "123");

        assertThat("Status code for Unauthorized user should be 401 but got " + response.statusCode(), response.statusCode(), is(401));
        assertThat("Error for Unauthorized user should be Unauthorized but got " + response.jsonPath().getString("error"), response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "updatePortalStatusDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void updatePortalStatusWithInvalidContractorIdTest(int contractorId, String firstName, String lastName, String contractorEmail, int recruiterUserId, String recruiterName) {
        Response response = updatePortalStatus(firstName, lastName, contractorEmail, recruiterUserId, 1, recruiterName, contractorId, albatrossAuthToken + "123");

        assertThat("Status code for Invalid contractor ID should be 401 but got " + response.statusCode(), response.statusCode(), is(401));
        assertThat("Error for Invalid contractor ID should be Unauthorized but got " + response.jsonPath().getString("error"), response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "updatePortalStatusDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void updatePortalStatusWithInvalidEmailTest(int contractorId, String firstName, String lastName, String contractorEmail, int recruiterUserId, String recruiterName) {
        Response response = updatePortalStatus(firstName, lastName, timesheetFaker.generateFakerEmail(), recruiterUserId, 1, recruiterName, contractorId, albatrossAuthToken);

        assertThat("Status code for Invalid email should be 400 but got " + response.statusCode(), response.statusCode(), is(200));
        assertThat( response.jsonPath().getString("message"), is("Provided contractorEmail does not belong to this contractor."));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "updatePortalStatusDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void updatePortalStatusWithInvalidPortalStatusTest(int contractorId, String firstName, String lastName, String contractorEmail, int recruiterUserId, String recruiterName) {
        Response response = updatePortalStatus(firstName, lastName, contractorEmail, recruiterUserId, 0, recruiterName, contractorId, albatrossAuthToken);

        assertThat("Status code for Invalid portal status should be 422 but got " + response.statusCode(), response.statusCode(), is(422));
        JsonPath jsonPath = response.jsonPath();
        String message = jsonPath.getString("message");
        assertThat("Response should contain success message", message, containsString("The selected portal status is invalid."));
    }

    public Response updatePortalStatus(String firstName, String lastName, String contractorEmail, int recruiterUserId,int portalStatus, String recruiterName, int contractorId, String authToken) {
        UpdatePortalStatusRequest request = UpdatePortalStatusRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .contractorEmail(contractorEmail)
                .recruiterUserId(recruiterUserId)
                .portalStatus(portalStatus)
                .recruiterName(recruiterName)
                .contractorId(contractorId)
                .build();
        return RestClient.doPost("JSON", albatrossURL, "contract-staffing/contractor/update-portal-status", authToken, null, true, request);
    }

    @DataProvider
    public Object[][] updatePortalStatusDataProvider() {
        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String candidateSlug = jsonCandidate.getString("slug");
        String firstName = jsonCandidate.getString("first_name");
        String lastName = jsonCandidate.getString("last_name");
        String contractorEmail = jsonCandidate.getString("email");

        Response usersResponse = function.getUsers(baseURL, apiAuthToken);
        usersResponse.then().statusCode(200);
        JsonPath usersJsonPath = usersResponse.jsonPath();
        int recruiterUserId = usersJsonPath.getInt("[0].id");
        String recruiterName = usersJsonPath.getString("[0].first_name") + " " + usersJsonPath.getString("[0].last_name");

        int contractorId = function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug, "candidate").jsonPath().getInt("data.candidate.id");
        String jobSlug = function.getEntityResponse(baseURL, apiAuthToken, "job");
        int jobId = allCrudFunctions.getJobResponse(albatrossURL, albatrossAuthToken,jobSlug).jsonPath().getInt("data.job.id");
        function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug, jobSlug);

        enableTimesheet(contractorId, jobId, recruiterUserId, albatrossAuthToken, 2, 200, 0);

        return new Object[][] {
                { contractorId, firstName, lastName, contractorEmail, recruiterUserId, recruiterName }
        };
    }
}

