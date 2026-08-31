package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.*;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.pojo.BulkCreatePlacementsRequest;
import io.rcrm.api.pojo.PlacementItem;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import com.qa.api.util.Owner;

// This endpoint is deprecated scripts are removed from regression suite
@AccountType("CrossAccount")
public class BulkCreatePlacements_Test extends TestBase {

    String apiKeyA;
    String apiKeyB;
    String albatrossTknA;
    String albatrossTknB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    JavaFakerPlacement placementFaker;
    String basePath = "placements/bulk-create";

    @BeforeClass(alwaysRun = true)    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        apiKeyB = getAccountApiKey("AccountB");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        placementFaker = new JavaFakerPlacement();
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getMultipleCandidatesAndJobs", groups = "nightly-build")
    public void bulkCreatePlacementsWithValidToken_Test(int candidateId1, int candidateId2, int jobId1, int jobId2) {
        PlacementItem placement1 = new PlacementItem();
        placement1.setCandidateId(candidateId1);
        placement1.setJobId(jobId1);

        PlacementItem placement2 = new PlacementItem();
        placement2.setCandidateId(candidateId1);
        placement2.setJobId(jobId2);

        PlacementItem placement3 = new PlacementItem();
        placement3.setCandidateId(candidateId2);
        placement3.setJobId(jobId1);

        PlacementItem placement4 = new PlacementItem();
        placement4.setCandidateId(candidateId2);
        placement4.setJobId(jobId2);

        BulkCreatePlacementsRequest requestBody = new BulkCreatePlacementsRequest();
        requestBody.setPlacements(Arrays.asList(placement1, placement2, placement3, placement4));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Placements created successfully"));
        response.then().assertThat().body("data.totalRequested", Matchers.is(4));
        response.then().assertThat().body("data.totalCreated", Matchers.is(4));
        response.then().assertThat().body("data.totalSkipped", Matchers.is(0));
        response.then().assertThat().body("data.totalFailed", Matchers.is(0));
        response.then().assertThat().body("data.createdPlacements.size()", Matchers.is(4));
        response.then().assertThat().body("data.createdPlacements[0].candidateId", Matchers.is(candidateId1));
        response.then().assertThat().body("data.createdPlacements[0].jobId", Matchers.is(jobId1));
        response.then().assertThat().body("data.createdPlacements[1].candidateId", Matchers.is(candidateId1));
        response.then().assertThat().body("data.createdPlacements[1].jobId", Matchers.is(jobId2));
        response.then().assertThat().body("data.createdPlacements[2].candidateId", Matchers.is(candidateId2));
        response.then().assertThat().body("data.createdPlacements[2].jobId", Matchers.is(jobId1));
        response.then().assertThat().body("data.createdPlacements[3].candidateId", Matchers.is(candidateId2));
        response.then().assertThat().body("data.createdPlacements[3].jobId", Matchers.is(jobId2));

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/bulkCreatePlacements.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getMultipleCandidatesAndJobs", groups = "nightly-build")
    public void bulkCreatePlacementsSkipDuplicatePlacements_Test(int candidateId, int candidateId2, int jobId, int jobId2) {
        PlacementItem placement1 = new PlacementItem();
        placement1.setCandidateId(candidateId);
        placement1.setJobId(jobId);

        BulkCreatePlacementsRequest requestBody = new BulkCreatePlacementsRequest();
        requestBody.setPlacements(Arrays.asList(placement1));
        Response response1 = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response1.then().statusCode(200);
        response1.then().assertThat().body("meta.message", Matchers.is("Placements created successfully"));
        response1.then().assertThat().body("data.totalRequested", Matchers.is(1));
        response1.then().assertThat().body("data.totalCreated", Matchers.is(1));
        response1.then().assertThat().body("data.totalSkipped", Matchers.is(0));
        response1.then().assertThat().body("data.createdPlacements.size()", Matchers.is(1));

        PlacementItem placement2 = new PlacementItem();
        placement2.setCandidateId(candidateId);
        placement2.setJobId(jobId);

        BulkCreatePlacementsRequest requestBody2 = new BulkCreatePlacementsRequest();
        requestBody2.setPlacements(Arrays.asList(placement2));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Placements created successfully"));
        response.then().assertThat().body("data.totalRequested", Matchers.is(1));
        response.then().assertThat().body("data.totalCreated", Matchers.is(0));
        response.then().assertThat().body("data.totalSkipped", Matchers.is(1));
        response.then().assertThat().body("data.totalFailed", Matchers.is(0));
        response.then().assertThat().body("data.createdPlacements.size()", Matchers.is(0));

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/bulkCreatePlacements.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getMultipleCandidatesAndJobs", groups = "nightly-build")
    public void bulkCreatePlacementsWithInvalidToken_Test(int candidateId1, int candidateId2, int jobId1, int jobId2) {
        PlacementItem placement1 = new PlacementItem();
        placement1.setCandidateId(candidateId1);
        placement1.setJobId(jobId1);

        BulkCreatePlacementsRequest requestBody = new BulkCreatePlacementsRequest();
        requestBody.setPlacements(Arrays.asList(placement1));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true, requestBody);
        response.then().statusCode(Matchers.is(401));
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getMultipleCandidatesAndJobs", groups = "nightly-build")
    public void bulkCreatePlacementsWithoutToken_Test(int candidateId1, int candidateId2, int jobId1, int jobId2) {
        PlacementItem placement1 = new PlacementItem();
        placement1.setCandidateId(candidateId1);
        placement1.setJobId(jobId1);

        BulkCreatePlacementsRequest requestBody = new BulkCreatePlacementsRequest();
        requestBody.setPlacements(Arrays.asList(placement1));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, "", null, null, true, requestBody);
        response.then().statusCode(Matchers.is(401));
        response.then().assertThat().body("meta.message", Matchers.containsString("Unauthorised access"));
        response.then().assertThat().body("data", Matchers.containsString("Missing bearer token in header"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getMultipleCandidatesAndJobs", groups = "nightly-build")
    public void bulkCreatePlacementsWithCrossAccountToken_Test(int candidateId1, int candidateId2, int jobId1, int jobId2) {
        PlacementItem placement1 = new PlacementItem();
        placement1.setCandidateId(candidateId1);
        placement1.setJobId(jobId1);

        BulkCreatePlacementsRequest requestBody = new BulkCreatePlacementsRequest();
        requestBody.setPlacements(Arrays.asList(placement1));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknB, null, null, true, requestBody);
        
        response.then().statusCode(Matchers.is(200));

        response.then().assertThat().body("data.totalRequested", Matchers.is(1));
        response.then().assertThat().body("data.totalFailed", Matchers.is(1));
        response.then().assertThat().body("data.createdPlacements", Matchers.empty());
        response.then().assertThat().body("data.errors.size()", Matchers.is(1));
        response.then().assertThat().body("data.errors[0].candidateId", Matchers.is(candidateId1));
        response.then().assertThat().body("data.errors[0].jobId", Matchers.is(jobId1));
        response.then().assertThat().body("data.errors[0].errorMessage", Matchers.containsString("Candidate not found")); 
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getMultipleCandidatesAndJobs", groups = "nightly-build")
    public void bulkCreatePlacementsWithAdminToken_Test(int candidateId1, int candidateId2, int jobId1, int jobId2) {
        PlacementItem placement1 = new PlacementItem();
        placement1.setCandidateId(candidateId1);
        placement1.setJobId(jobId1);

        BulkCreatePlacementsRequest requestBody = new BulkCreatePlacementsRequest();
        requestBody.setPlacements(Arrays.asList(placement1));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, null, true, requestBody);
        
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Placements created successfully"));
        response.then().assertThat().body("meta.status", Matchers.is(200));
        response.then().assertThat().body("meta.responseType.context", Matchers.is("Request is successful"));
        response.then().assertThat().body("data.totalRequested", Matchers.is(1));
        response.then().assertThat().body("data.totalCreated", Matchers.is(1));
        response.then().assertThat().body("data.totalSkipped", Matchers.is(0));
        response.then().assertThat().body("data.totalFailed", Matchers.is(0));
        response.then().assertThat().body("data.createdPlacements.size()", Matchers.is(1));
        response.then().assertThat().body("data.createdPlacements[0].candidateId", Matchers.is(candidateId1));
        response.then().assertThat().body("data.createdPlacements[0].jobId", Matchers.is(jobId1));

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/bulkCreatePlacements.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getMultipleCandidatesAndJobs", groups = "nightly-build")
    public void bulkCreatePlacementsWithTeamMemberToken_Test(int candidateId1, int candidateId2, int jobId1, int jobId2) {
        PlacementItem placement1 = new PlacementItem();
        placement1.setCandidateId(candidateId1);
        placement1.setJobId(jobId1);

        BulkCreatePlacementsRequest requestBody = new BulkCreatePlacementsRequest();
        requestBody.setPlacements(Arrays.asList(placement1));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, null, true, requestBody);
        response.then().statusCode(Matchers.is(403));
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Access Denied"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/validationErrorResponse.json"));

    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getMultipleCandidatesAndJobs", groups = "nightly-build")
    public void bulkCreatePlacementsWithRestrictedTeamMemberToken_Test(int candidateId1, int candidateId2, int jobId1, int jobId2) {
        PlacementItem placement1 = new PlacementItem();
        placement1.setCandidateId(candidateId1);
        placement1.setJobId(jobId1);

        BulkCreatePlacementsRequest requestBody = new BulkCreatePlacementsRequest();
        requestBody.setPlacements(Arrays.asList(placement1));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, null, true, requestBody);
        response.then().statusCode(Matchers.is(403));
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Access Denied"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/validationErrorResponse.json"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void bulkCreatePlacementsWithEmptyArray_Test() {
        BulkCreatePlacementsRequest requestBody = new BulkCreatePlacementsRequest();
        requestBody.setPlacements(Arrays.asList());

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(Matchers.is(200));
        response.then().assertThat().body("data.totalRequested", Matchers.is(0));
        response.then().assertThat().body("data.createdPlacements", Matchers.empty());
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void bulkCreatePlacementsWithNullPlacements_Test() {
        BulkCreatePlacementsRequest requestBody = new BulkCreatePlacementsRequest();
        requestBody.setPlacements(null);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(Matchers.is(400));
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Placements list cannot be null"));
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/validationErrorResponse.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getMultipleCandidatesAndJobs", groups = "nightly-build")
    public void bulkCreatePlacementsWithInvalidCandidateId_Test(int candidateId1, int candidateId2, int jobId1, int jobId2) {
        PlacementItem placement1 = new PlacementItem();
        int candidateId = placementFaker.getRandomID();
        placement1.setCandidateId(candidateId);
        placement1.setJobId(jobId1);

        BulkCreatePlacementsRequest requestBody = new BulkCreatePlacementsRequest();
        requestBody.setPlacements(Arrays.asList(placement1));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(Matchers.is(200));
        response.then().assertThat().body("data.totalRequested", Matchers.is(1));
        response.then().assertThat().body("data.totalFailed", Matchers.is(1));
        response.then().assertThat().body("data.createdPlacements", Matchers.empty());
        response.then().assertThat().body("data.errors.size()", Matchers.is(1));
        response.then().assertThat().body("data.errors[0].candidateId", Matchers.is(candidateId));
        response.then().assertThat().body("data.errors[0].jobId", Matchers.is(jobId1));
        response.then().assertThat().body("data.errors[0].errorMessage", Matchers.containsString("Candidate not found")); 
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getMultipleCandidatesAndJobs", groups = "nightly-build")
    public void bulkCreatePlacementsWithInvalidJobId_Test(int candidateId1, int candidateId2, int jobId1, int jobId2) {
        PlacementItem placement1 = new PlacementItem();
        int jobId = placementFaker.getRandomID();
        placement1.setCandidateId(candidateId1);
        placement1.setJobId(jobId);

        BulkCreatePlacementsRequest requestBody = new BulkCreatePlacementsRequest();
        requestBody.setPlacements(Arrays.asList(placement1));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        
        response.then().statusCode(Matchers.is(200));
        response.then().assertThat().body("data.totalRequested", Matchers.is(1));
        response.then().assertThat().body("data.totalFailed", Matchers.is(1));
        response.then().assertThat().body("data.createdPlacements", Matchers.empty());
        response.then().assertThat().body("data.errors.size()", Matchers.is(1));
        response.then().assertThat().body("data.errors[0].candidateId", Matchers.is(candidateId1));
        response.then().assertThat().body("data.errors[0].jobId", Matchers.is(jobId));
        response.then().assertThat().body("data.errors[0].errorMessage", Matchers.containsString("Job not found")); 
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getMultipleCandidatesAndJobs", groups = "nightly-build")
    public void bulkCreatePlacementsWithNullCandidateId_Test(int candidateId1, int candidateId2, int jobId1, int jobId2) {
        PlacementItem placement1 = new PlacementItem();
        placement1.setCandidateId(null);
        placement1.setJobId(jobId1);

        BulkCreatePlacementsRequest requestBody = new BulkCreatePlacementsRequest();
        requestBody.setPlacements(Arrays.asList(placement1));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(Matchers.is(400));
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Candidate ID cannot be null"));
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/validationErrorResponse.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getMultipleCandidatesAndJobs", groups = "nightly-build")
    public void bulkCreatePlacementsWithNullJobId_Test(int candidateId1, int candidateId2, int jobId1, int jobId2) {
        PlacementItem placement1 = new PlacementItem();
        placement1.setCandidateId(candidateId1);
        placement1.setJobId(null);

        BulkCreatePlacementsRequest requestBody = new BulkCreatePlacementsRequest();
        requestBody.setPlacements(Arrays.asList(placement1));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        
        response.then().statusCode(Matchers.is(400));
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Job ID cannot be null"));
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/validationErrorResponse.json"));
    }

    @DataProvider
    public Object[][] getMultipleCandidatesAndJobs() {
        Response response1 = function.createNewCandidateWithMandatoryFields(baseURL, apiKeyA);
        String candidateSlug1 = response1.jsonPath().getString("slug");
        int candidateId1 = allCrudFunctions.getCandidateResponse(albatrossURL, albatrossTknA, candidateSlug1).jsonPath().get("data.candidate.id");

        Response response2 = function.createNewCandidateWithMandatoryFields(baseURL, apiKeyA);
        String candidateSlug2 = response2.jsonPath().getString("slug");
        int candidateId2 = allCrudFunctions.getCandidateResponse(albatrossURL, albatrossTknA, candidateSlug2).jsonPath().get("data.candidate.id");

        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
        String companySlug = companyResponse.jsonPath().getString("slug");

        Response contactResponse = function.createNewContact_POST(baseURL, apiKeyA, companySlug);
        String contactSlug = contactResponse.jsonPath().getString("slug");

        Response jobResponse1 = function.createNewJob(baseURL, apiKeyA, companySlug, contactSlug);
        String jobSlug1 = jobResponse1.jsonPath().getString("slug");
        int jobId1 = allCrudFunctions.getJobResponse(albatrossURL, albatrossTknA, jobSlug1).jsonPath().get("data.job.id");

        Response jobResponse2 = function.createNewJob(baseURL, apiKeyA, companySlug, contactSlug);
        String jobSlug2 = jobResponse2.jsonPath().getString("slug");
        int jobId2 = allCrudFunctions.getJobResponse(albatrossURL, albatrossTknA, jobSlug2).jsonPath().get("data.job.id");

        function.assignCandidateToJobBySlug(baseURL, apiKeyA, candidateSlug1, jobSlug1);
        function.assignCandidateToJobBySlug(baseURL, apiKeyA, candidateSlug1, jobSlug2);
        function.assignCandidateToJobBySlug(baseURL, apiKeyA, candidateSlug2, jobSlug1);
        function.assignCandidateToJobBySlug(baseURL, apiKeyA, candidateSlug2, jobSlug2);

        return new Object[][] {
            { candidateId1, candidateId2, jobId1, jobId2 }
        };
    }
}
