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
import io.rcrm.api.pojo.invoiceService.CreatePlacement;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class getUnplacedCandidatesByJob_Test extends TestBase {

    String apiKeyA;
    String apiKeyB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    String albatrossTknA;
    String albatrossTknB;
    JavaFakerPlacement placementFaker;
    String basePath = "placements/unplaced-candidates-by-job";
    Map<String, String> paramsMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        apiKeyB = getAccountApiKey("AccountB");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        placementFaker = new JavaFakerPlacement();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getJobId", groups = {"invoice_service", "nightly-build"})
    public void getUnplacedCandidatesByJobWithValidToken_Test(int jobId, int candidateId) {
        paramsMap.put("jobId", String.valueOf(jobId));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, paramsMap, null, true);
        response.then().statusCode(200);
        
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getUnplacedCandidatesByJob.json"));
        response.then().assertThat().body("meta.message", Matchers.containsString("Unplaced candidates for job fetched successfully"));
        response.then().assertThat().body("data[0].id", Matchers.is(candidateId));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getUnplacedCandidatesByJobWithInvalidJobId_Test() {
        int invalidJobId = placementFaker.getRandomID();
        paramsMap.put("jobId", String.valueOf(invalidJobId));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, paramsMap, null, true);
        
        response.then().statusCode(404);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getUnplacedCandidatesByJobWithInvalidToken_Test() {
        int jobId = placementFaker.getRandomID();
        paramsMap.put("jobId", String.valueOf(jobId));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), paramsMap, null, true);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getJobId", groups = {"invoice_service", "nightly-build"})
    public void getUnplacedCandidatesByJobWithCrossAccountToken_Test(int jobId, int candidateId) {
        paramsMap.put("jobId", String.valueOf(jobId));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknB, paramsMap, null, true);
        
        response.then().statusCode(404);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getJobId", groups = {"invoice_service", "nightly-build"})
    public void getUnplacedCandidatesByJobWithAdminToken_Test(int jobId, int candidateId) {
        paramsMap.put("jobId", String.valueOf(jobId));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), paramsMap, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Unplaced candidates for job fetched successfully"));
        response.then().assertThat().body("data[0].id", Matchers.is(candidateId));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getJobId", groups = {"invoice_service", "nightly-build"})
    public void getUnplacedCandidatesByJobWithTeamMemberToken_Test(int jobId, int candidateId) {
        paramsMap.put("jobId", String.valueOf(jobId));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), paramsMap, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("data[0].id", Matchers.is(candidateId));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getJobId", groups = {"invoice_service", "nightly-build"})
    public void getUnplacedCandidatesByJobWithRestrictedTeamMemberToken_Test(int jobId, int candidateId) {
        paramsMap.put("jobId", String.valueOf(jobId));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), paramsMap, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("data.size()", Matchers.is(0));
    }

    @DataProvider
    public Object[][] getJobId() {
        Response createPlacementResponse = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL);
        int jobId = createPlacementResponse.jsonPath().get("data.jobId");
        String jobSlug = createPlacementResponse.jsonPath().get("data.jobSlug");

        Response resp1 = function.createNewCandidateWithMandatoryFields(baseURL, apiKeyA);
        String candidateSlug1 = resp1.jsonPath().getString("slug");
        int candidateId1 = allCrudFunctions.getCandidateResponse(albatrossURL, albatrossTknA,candidateSlug1).jsonPath().get("data.candidate.id");

        function.assignCandidateToJobBySlug(baseURL, apiKeyA, candidateSlug1, jobSlug);

        return new Object[][] {
            { jobId, candidateId1}
        };
    }
}
