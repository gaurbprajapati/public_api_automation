package io.recruitcrm.contractStaffing.contractorPortal;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.ContractStaffing.TimesheetFaker;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.testbase.TestBase;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class ContractorTimesheetEnabledTest extends ContractStaffingBaseTest {

    String albatrossAuthToken;
    String apiAuthToken;
    int ownerAccountID;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;

    @BeforeClass(alwaysRun = true)
    public void Setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        apiAuthToken = ThreadManager.getAccountApiKey();
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        createRuleEngineTemplate(albatrossAuthToken);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "contractorTimesheetEnabledDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void getContractorTimesheetEnabledTest(int contractorId) {
        Response response = getContractorTimesheetEnabled(contractorId, albatrossAuthToken);

        assertThat("Status code should be 200 but got " + response.statusCode(), response.statusCode(), is(200));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Message should be Timesheet enabled status fetched successfully but got " + jsonPath.get("meta.message"), jsonPath.get("meta.message"), is("Timesheet enabled status fetched successfully"));
        assertThat("Data should be 0 but got " + jsonPath.get("data"), jsonPath.get("data"), is(0));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "contractorTimesheetEnabledWithCustomTimesheetSettings", groups = {"contract_staffing", "nightly-build"})
    public void getContractorTimesheetEnabledWithCustomTimesheetSettingsTest(int contractorId) {
        Response response = getContractorTimesheetEnabled(contractorId, albatrossAuthToken);

        assertThat("Status code should be 200 but got " + response.statusCode(), response.statusCode(), is(200));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Message should be Timesheet enabled status fetched successfully but got " + jsonPath.get("meta.message"), jsonPath.get("meta.message"), is("Timesheet enabled status fetched successfully"));
        assertThat("Data should be 1 but got " + jsonPath.get("data"), jsonPath.get("data"), is(1));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/contractStaffing/GetContractorTimesheetEnabled.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "contractorTimesheetWithoutTimeSheetEnabled", groups = {"contract_staffing", "nightly-build"})
    public void getContractorTimesheetEnabledWithoutTimeSheetEnabledTest(int contractorId) {
        Response response = getContractorTimesheetEnabled(contractorId, albatrossAuthToken);

        assertThat("Status code should be 200 but got " + response.statusCode(), response.statusCode(), is(200));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Message should be Timesheet enabled status fetched successfully but got " + jsonPath.get("meta.message"), jsonPath.get("meta.message"), is("Timesheet enabled status fetched successfully"));
        assertThat("Data should be 0 but got " + jsonPath.get("data"), jsonPath.get("data"), nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "contractorTimesheetEnabledDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void unauthorizedUserCannotGetContractorTimesheetEnabledTest(int contractorId) {
        Response response = getContractorTimesheetEnabled(contractorId, albatrossAuthToken + "123");

        assertThat("Status code for Unauthorized user should be 401 but got " + response.statusCode(), response.statusCode(), is(401));
        assertThat("Message should be Unauthorised access but got " + response.jsonPath().getString("meta.message"), response.jsonPath().getString("meta.message"), is("Unauthorised access"));
        assertThat("Data should be null but got " + response.jsonPath().get("data"), response.jsonPath().get("data"), is("Invalid token"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void getContractorTimesheetEnabledWithInvalidContractorIdTest() {
        Response response = getContractorTimesheetEnabled(TimesheetFaker.generateFakerInt(10), albatrossAuthToken);

        assertThat("Status code for invalid contractor should be 200 but got " + response.statusCode(),
                response.statusCode(), is(200));
        assertThat("Data should be null but got " + response.jsonPath().get("data"), response.jsonPath().get("data"), nullValue());
    }

    public Response getContractorTimesheetEnabled(int contractorId, String authToken) {
        return RestClient.doGet("JSON", timesheetBaseURL, "contractor/" + contractorId + "/timesheet-enabled",
                        authToken, null, null, true);
    }

    @DataProvider
    public Object[][] contractorTimesheetEnabledDataProvider() {
        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String candidateSlug = jsonCandidate.getString("slug");
        int contractorId = function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug, "candidate")
                .jsonPath().getInt("data.candidate.id");

        Response usersResponse = function.getUsers(baseURL, apiAuthToken);
        usersResponse.then().statusCode(200);
        int recruiterUserId = usersResponse.jsonPath().getInt("[0].id");

        String jobSlug = function.getEntityResponse(baseURL, apiAuthToken, "job");
        int jobId = allCrudFunctions.getJobResponse(albatrossURL, albatrossAuthToken, jobSlug).jsonPath().getInt("data.job.id");
        function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug, jobSlug);

        enableTimesheet(contractorId, jobId, recruiterUserId, albatrossAuthToken, 2, 200, 0);

        return new Object[][] {
                { contractorId }
        };
    }

    @DataProvider
    public Object[][] contractorTimesheetWithoutTimeSheetEnabled() {
        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String candidateSlug = jsonCandidate.getString("slug");
        int contractorId = function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug, "candidate")
                .jsonPath().getInt("data.candidate.id");

        String jobSlug = function.getEntityResponse(baseURL, apiAuthToken, "job");
        function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug, jobSlug);

        return new Object[][] {
                { contractorId }
        };
    }

    @DataProvider
    public Object[][] contractorTimesheetEnabledWithCustomTimesheetSettings() {
        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String candidateSlug = jsonCandidate.getString("slug");
        int contractorId = function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug, "candidate")
                .jsonPath().getInt("data.candidate.id");

        Response usersResponse = function.getUsers(baseURL, apiAuthToken);
        usersResponse.then().statusCode(200);
        int recruiterUserId = usersResponse.jsonPath().getInt("[0].id");

        String jobSlug = function.getEntityResponse(baseURL, apiAuthToken, "job");
        int jobId = allCrudFunctions.getJobResponse(albatrossURL, albatrossAuthToken, jobSlug).jsonPath().getInt("data.job.id");
        function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug, jobSlug);

        enableTimesheet(contractorId, jobId, recruiterUserId, albatrossAuthToken, 2, "currPrev30", "currNext30", 200, 0);

        return new Object[][] {
                { contractorId }
        };
    }
}
