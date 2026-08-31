package io.recruitcrm.invoiceService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import io.rcrm.api.pojo.invoiceService.CreatePlacement;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.pojo.invoiceService.CreatePlacement;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CreatePlacement_Test extends TestBase {

    public CreatePlacement_Test() {
        super();
    }

    String apiKeyA;
    String apiKeyB;
    commanFunction function;
	AllCrudFunctions allCrudFunctions;
    String albatrossTknA;
    String albatrossTknB;
    JavaFakerPlacement placementFaker;
    String basePath;
    @BeforeClass(alwaysRun = true)
	public void setUp() {
		apiKeyA =  getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        placementFaker = new JavaFakerPlacement();
        basePath = "placements";
	}

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getEntityIds", groups = {"invoice_service", "nightly-build"})
    public void createPlacementWithValidToken_Test(int candidateId, int companyId, int contactId, int jobId, int dealId) {
        Map<String, List<Integer>> associationIds = new HashMap<>();
        associationIds.put("2", Arrays.asList(contactId));
        associationIds.put("11", Arrays.asList(dealId));

        CreatePlacement placementRequest = new CreatePlacement(companyId, jobId, candidateId, placementFaker.getCurrencyId(), associationIds);

        Response response = RestClient.doPost1( "JSON", invoiceServiceURL,  basePath, albatrossTknA,  null, null,
            true, placementRequest );

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placement Created Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createPlacement.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getEntityIds", groups = {"invoice_service", "nightly-build"})
    public void createPlacementWithInvalidId_Test(int candidateId, int companyId, int contactId, int jobId, int dealId) {
        Map<String, List<Integer>> associationIds = new HashMap<>();
        associationIds.put("2", Arrays.asList(placementFaker.getRandomID()));
        associationIds.put("11", Arrays.asList(dealId));

        CreatePlacement placementRequest = new CreatePlacement(placementFaker.getRandomID(), jobId, candidateId, placementFaker.getCurrencyId(), associationIds);

        Response response = RestClient.doPost1( "JSON", invoiceServiceURL,  basePath, albatrossTknA,  null, null,
            true, placementRequest );

        response.then().statusCode(404);
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createPlacementWithInvalidToken_Test( ) {
        CreatePlacement placementRequest = new CreatePlacement(placementFaker.getRandomID(), placementFaker.getRandomID(), placementFaker.getRandomID(), placementFaker.getCurrencyId(), null);

        Response response = RestClient.doPost1( "JSON", invoiceServiceURL,  basePath, placementFaker.getInvalidToken(),  null, null,
            true, placementRequest );

        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }


    //Cross Account
    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getEntityIds", groups = {"invoice_service", "nightly-build"})
    public void createPlacementWithCrossAccountToken_Test(int candidateId, int companyId, int contactId, int jobId, int dealId) {
        Map<String, List<Integer>> associationIds = new HashMap<>();
        associationIds.put("2", Arrays.asList(contactId));
        associationIds.put("11", Arrays.asList(dealId));

        CreatePlacement placementRequest = new CreatePlacement(companyId, jobId, candidateId, placementFaker.getCurrencyId(), associationIds);

        Response response = RestClient.doPost1( "JSON", invoiceServiceURL,  basePath, albatrossTknB,  null, null,
            true, placementRequest );

        response.then().statusCode(404);
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    // Role Based Access Control

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getEntityIds", groups = {"invoice_service", "nightly-build"})
    public void createPlacementWithAdminToken_Test(int candidateId, int companyId, int contactId, int jobId, int dealId) {
        Map<String, List<Integer>> associationIds = new HashMap<>();
        associationIds.put("2", Arrays.asList(contactId));
        associationIds.put("11", Arrays.asList(dealId));

        CreatePlacement placementRequest = new CreatePlacement(companyId, jobId, candidateId, placementFaker.getCurrencyId(), associationIds);

        Response response = RestClient.doPost1( "JSON", invoiceServiceURL,  basePath, getRoleBasedToken("AccountA", "Admin"),  null, null,
            true, placementRequest );

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placement Created Successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getEntityIds", groups = {"invoice_service", "nightly-build"})
    public void createPlacementWithTeamMemberToken_Test(int candidateId, int companyId, int contactId, int jobId, int dealId) {
        Map<String, List<Integer>> associationIds = new HashMap<>();
        associationIds.put("2", Arrays.asList(contactId));
        associationIds.put("11", Arrays.asList(dealId));

        CreatePlacement placementRequest = new CreatePlacement(companyId, jobId, candidateId, placementFaker.getCurrencyId(), associationIds);

        Response response = RestClient.doPost1( "JSON", invoiceServiceURL,  basePath, getRoleBasedToken("AccountA", "Team Member"),  null, null,
            true, placementRequest );

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placement Created Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createPlacement.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getEntityIds", groups = {"invoice_service", "nightly-build"})
    public void createPlacementWithRestrictedTeamMemberToken_Test(int candidateId, int companyId, int contactId, int jobId, int dealId) {
        Map<String, List<Integer>> associationIds = new HashMap<>();
        associationIds.put("2", Arrays.asList(contactId));
        associationIds.put("11", Arrays.asList(dealId));

        CreatePlacement placementRequest = new CreatePlacement(companyId, jobId, candidateId, placementFaker.getCurrencyId(), associationIds);

        Response response = RestClient.doPost1( "JSON", invoiceServiceURL,  basePath, getRoleBasedToken("AccountA", "Restricted"),  null, null,
            true, placementRequest );

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placement Created Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createPlacement.json"));
    }

    
    @DataProvider
    public Object[][] getEntityIds() {
        Response response = function.createNewCandidateWithMandatoryFields(baseURL, apiKeyA);
        String candidateSlug = response.jsonPath().getString("slug");
        int candidateId = allCrudFunctions.getCandidateResponse(albatrossURL, albatrossTknA,candidateSlug).jsonPath().get("data.candidate.id");
        Response response1 = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
        String companySlug = response1.jsonPath().getString("slug");
        int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA,companySlug).jsonPath().get("data.company.id");
        Response contactResponse = function.createNewContact_POST(baseURL, apiKeyA, companySlug);
        String contactSlug = contactResponse.jsonPath().getString("slug");
        int contactId = Integer.parseInt(allCrudFunctions.getContactResponse(albatrossURL, albatrossTknA,contactSlug).jsonPath().get("data.contact.id"));
        Response response2 = function.createNewJob(baseURL, apiKeyA, companySlug, contactSlug);
        String jobSlug = response2.jsonPath().getString("slug");
        int jobId = allCrudFunctions.getJobResponse(albatrossURL, albatrossTknA,jobSlug).jsonPath().get("data.job.id");
        Response dealResponse = function.createNewDealWithMandatoryFields(baseURL, apiKeyA, companySlug, contactSlug, jobSlug);
        String dealSlug = dealResponse.jsonPath().getString("slug");
        int dealId = allCrudFunctions.getDealResponse(albatrossURL, albatrossTknA,dealSlug).jsonPath().get("data.deal.id");

        function.assignCandidateToJobBySlug(baseURL, apiKeyA, candidateSlug, jobSlug);

        return new Object[][] {
            { candidateId, companyId, contactId, jobId, dealId }
        };
    }
}
