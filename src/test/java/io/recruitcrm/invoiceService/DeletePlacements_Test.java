package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.*;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class DeletePlacements_Test extends TestBase {

    public DeletePlacements_Test() {
        super();
    }

    String apiKeyA;
    String apiKeyB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    String albatrossTknA;
    String albatrossTknB;
    JavaFakerPlacement placementFaker;
    String basePath = "placements/delete";

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
    public void deletePlacementsWithValidToken_Test(int placementId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(placementId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Deleted Successfully"));
        response.then().assertThat().body("data[0]", Matchers.is(placementId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//archivePlacements.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void deletePlacementsWithInvalidId_Test() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(placementFaker.getRandomID()));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void deletePlacementsWithInvalidToken_Test() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(placementFaker.getRandomID()));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void deletePlacementsWithCrossAccountToken_Test(int placementId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(placementId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknB, null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void deletePlacementsWithAdminToken_Test(int placementId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(placementId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Deleted Successfully"));
        response.then().assertThat().body("data[0]", Matchers.is(placementId));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void deletePlacementsWithTeamMemberToken_Test(int placementId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(placementId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("You are not authorized to perform this action"));
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void deletePlacementsWithRestrictedTeamMemberToken_Test(int placementId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(placementId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("You are not authorized to perform this action"));
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    @DataProvider
    public Object[][] getPlacementId() {
        int placementId = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL).jsonPath().get("data.id");
        return new Object[][] {
            { placementId }
        };
    }
}
