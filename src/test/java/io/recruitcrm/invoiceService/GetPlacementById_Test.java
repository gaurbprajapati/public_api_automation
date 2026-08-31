package io.recruitcrm.invoiceService;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetPlacementById_Test extends TestBase {

    public GetPlacementById_Test() {
        super();
    }

    String apiKeyA;
    String apiKeyB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    String albatrossTknA;
    String albatrossTknB;
    JavaFakerPlacement placementFaker;
    String basePath = "placements/";

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
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void getPlacementByIdWithValidToken_Test(int placementId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath+placementId, albatrossTknA, null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placement Fetched Successfully"));
        response.then().assertThat().body("data.id", Matchers.is(placementId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getPlacementById.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void getPlacementByIdWithInvalidId_Test(int placementId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath+placementFaker.getRandomID(), albatrossTknA, null, null, true);
        response.then().statusCode(404);
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getPlacementByIdWithUnauthorizedAccess_Test() {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath+placementFaker.getRandomID(), albatrossTknA+"123", null, null, true);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void getPlacementByIdWithCrossAccountToken_Test(int placementId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath+placementId, albatrossTknB, null, null, true);
        response.then().statusCode(404);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void getPlacementByIdWithAdminToken_Test(int placementId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath+placementId, getRoleBasedToken("AccountA", "Admin"), null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placement Fetched Successfully"));
        response.then().assertThat().body("data.id", Matchers.is(placementId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getPlacementById.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void getPlacementByIdWithTeamMemberToken_Test(int placementId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath+placementId, getRoleBasedToken("AccountA", "Team Member"), null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placement Fetched Successfully"));
        response.then().assertThat().body("data.id", Matchers.is(placementId));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void getPlacementByIdWithRestrictedTeamMemberToken_Test(int placementId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath+placementId, getRoleBasedToken("AccountA", "Restricted"), null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placement Fetched Successfully"));
    }

    @DataProvider
    public Object[][] getPlacementId() {
        int placementId = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL).jsonPath().get("data.id");
        return new Object[][] {
            { placementId }
        };
    }
}
