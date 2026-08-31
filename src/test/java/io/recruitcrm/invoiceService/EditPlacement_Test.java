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
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class EditPlacement_Test extends TestBase {

    public EditPlacement_Test() {
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
        basePath = "placements/";
	}

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getEntityIds", groups = {"invoice_service", "nightly-build"})
    public void editPlacementWithValidToken_Test(int candidateId, int companyId, int contactId, int jobId, int dealId, int placementId) {
        Map<String, List<Integer>> associationIds = new HashMap<>();
        associationIds.put("2", Arrays.asList(contactId));
        associationIds.put("11", Arrays.asList(dealId));

        CreatePlacement placementRequest = new CreatePlacement(companyId, jobId, candidateId, placementFaker.getCurrencyId(), associationIds);

        Response response = RestClient.doPatchOnce( "JSON", invoiceServiceURL,  basePath+placementId, albatrossTknA,  null,
            true, placementRequest );

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placement Updated Successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getEntityIds", groups = {"invoice_service", "nightly-build"})
    public void editPlacementWithInvalidId_Test(int candidateId, int companyId, int contactId, int jobId, int dealId, int placementId) {
        Map<String, List<Integer>> associationIds = new HashMap<>();
        associationIds.put("2", Arrays.asList(contactId));
        associationIds.put("11", Arrays.asList(dealId));

        CreatePlacement placementRequest = new CreatePlacement(placementFaker.getRandomID(), jobId, candidateId, placementFaker.getCurrencyId(), associationIds);

        Response response = RestClient.doPatchOnce( "JSON", invoiceServiceURL,  basePath+placementId, albatrossTknA,  null,
            true, placementRequest );

        response.then().statusCode(404);
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void editPlacementWithInvalidToken_Test( ) {
        CreatePlacement placementRequest = new CreatePlacement(placementFaker.getRandomID(), placementFaker.getRandomID(), placementFaker.getRandomID(), placementFaker.getCurrencyId(), null);

        Response response = RestClient.doPatchOnce( "JSON", invoiceServiceURL,  basePath+placementFaker.getRandomID(), placementFaker.getInvalidToken(),  null,
            true, placementRequest );

        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }


    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getEntityIds", groups = {"invoice_service", "nightly-build"})
    public void editPlacementWithCrossAccountToken_Test(int candidateId, int companyId, int contactId, int jobId, int dealId, int placementId) {
        Map<String, List<Integer>> associationIds = new HashMap<>();
        associationIds.put("2", Arrays.asList(contactId));
        associationIds.put("11", Arrays.asList(dealId));

        CreatePlacement placementRequest = new CreatePlacement(companyId, jobId, candidateId, placementFaker.getCurrencyId(), associationIds);

        Response response = RestClient.doPatchOnce( "JSON", invoiceServiceURL,  basePath+placementId, albatrossTknB,  null,
            true, placementRequest );

        response.then().statusCode(401);
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getEntityIds", groups = {"invoice_service", "nightly-build"})
    public void editPlacementWithAdminToken_Test(int candidateId, int companyId, int contactId, int jobId, int dealId, int placementId) {
        Map<String, List<Integer>> associationIds = new HashMap<>();
        associationIds.put("2", Arrays.asList(contactId));
        associationIds.put("11", Arrays.asList(dealId));

        CreatePlacement placementRequest = new CreatePlacement(companyId, jobId, candidateId, placementFaker.getCurrencyId(), associationIds);

        Response response = RestClient.doPatchOnce( "JSON", invoiceServiceURL,  basePath+placementId, getRoleBasedToken("AccountA", "Admin"),  null,
            true, placementRequest );

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placement Updated Successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getEntityIds", groups = {"invoice_service", "nightly-build"})
    public void editPlacementWithTeamMemberToken_Test(int candidateId, int companyId, int contactId, int jobId, int dealId, int placementId) {
        Map<String, List<Integer>> associationIds = new HashMap<>();
        associationIds.put("2", Arrays.asList(contactId));
        associationIds.put("11", Arrays.asList(dealId));

        CreatePlacement placementRequest = new CreatePlacement(companyId, jobId, candidateId, placementFaker.getCurrencyId(), associationIds);

        Response response = RestClient.doPatchOnce( "JSON", invoiceServiceURL,  basePath+placementId, getRoleBasedToken("AccountA", "Team Member"),  null,
            true, placementRequest );

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placement Updated Successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getEntityIds", groups = {"invoice_service", "nightly-build"})
    public void editPlacementWithRestrictedTeamMemberToken_Test(int candidateId, int companyId, int contactId, int jobId, int dealId, int placementId) {
        Map<String, List<Integer>> associationIds = new HashMap<>();
        associationIds.put("2", Arrays.asList(contactId));
        associationIds.put("11", Arrays.asList(dealId));

        CreatePlacement placementRequest = new CreatePlacement(companyId, jobId, candidateId, placementFaker.getCurrencyId(), associationIds);

        Response response = RestClient.doPatchOnce( "JSON", invoiceServiceURL,  basePath+placementId, getRoleBasedToken("AccountA", "Restricted"),  null,
            true, placementRequest );

            response.then().statusCode(401);
            response.then().assertThat().body("data", Matchers.nullValue());
            response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    
    @DataProvider
    public Object[][] getEntityIds() {
        Response createPlacementResponse = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL);
        int placementId = createPlacementResponse.jsonPath().get("data.id");
        int candidateId = createPlacementResponse.jsonPath().get("data.candidateId");
        int companyId = createPlacementResponse.jsonPath().get("data.companyId");
        int contactId = createPlacementResponse.jsonPath().get("data.associations[0].contacts[0].contactId");
        int jobId = createPlacementResponse.jsonPath().get("data.jobId");
        String companySlug = createPlacementResponse.jsonPath().get("data.companySlug");
        String contactSlug = createPlacementResponse.jsonPath().get("data.associations[0].contacts[0].slug");
        String jobSlug = createPlacementResponse.jsonPath().get("data.jobSlug");

        Response dealResponse1 = function.createNewDealWithMandatoryFields(baseURL, apiKeyA, companySlug, contactSlug, jobSlug);
        String dealSlug1 = dealResponse1.jsonPath().getString("slug");
        int dealId1 = allCrudFunctions.getDealResponse(albatrossURL, albatrossTknA,dealSlug1).jsonPath().get("data.deal.id");

        return new Object[][] {
            { candidateId, companyId, contactId, jobId, dealId1, placementId }
        };
    }

}
