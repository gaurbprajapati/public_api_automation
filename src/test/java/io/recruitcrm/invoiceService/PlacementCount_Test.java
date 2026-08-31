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
public class PlacementCount_Test extends TestBase {

    public PlacementCount_Test() {
        super();
    }

    String apiKeyA;
    String apiKeyB;
    String albatrossTknA;
    String albatrossTknB;
    String basePath = "placements/count";
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    JavaFakerPlacement placementFaker;
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
    public void placementCountWithValidToken_Test(int placementId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true);

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Quick View Count Fetched Successfully"));
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(1));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//placementCount.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void placementCountWithInvalidToken_Test() {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true);

        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void placementCountWithCrossAccountToken_Test(int placementId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknB, null, null, true);

        response.then().statusCode(200);
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(0));
        response.then().assertThat().body("meta.message", Matchers.containsString("Quick View Count Fetched Successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void placementCountWithAdminToken_Test() {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, null, true);

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Quick View Count Fetched Successfully"));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"}, dataProvider = "getPlacementId")
    public void placementCountWithTeamMemberToken_Test(int placementId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, null, true );

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Quick View Count Fetched Successfully"));
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(1));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"}, dataProvider = "getPlacementId")
    public void placementCountWithRestrictedToken_Test(int placementId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, null, true);

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Quick View Count Fetched Successfully"));
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(0));
    }

    @DataProvider
    public Object[][] getPlacementId() {
        int placementId = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL).jsonPath().get("data.id");
        return new Object[][] {
            { placementId }
        };
    }
}
