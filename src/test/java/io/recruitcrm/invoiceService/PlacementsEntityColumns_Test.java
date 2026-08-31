package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.JavaFakerPlacement;

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class PlacementsEntityColumns_Test extends TestBase {

    public PlacementsEntityColumns_Test() {
        super();
    }

    String albatrossTknA;
    String albatrossTknB;
    String basePath = "entity-columns";
    Map<String, String> paramsMap = new HashMap<>();
    JavaFakerPlacement placementFaker;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        paramsMap.put("entity", "placements");
        placementFaker = new JavaFakerPlacement();
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getEntityColumnsWithValidToken_Test() {

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, paramsMap, null, true);

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Columns Fetched"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getEntityColumnsWithInvalidToken_Test() {
        paramsMap.put("entity", "placements");

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), paramsMap, null, true);

        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getEntityColumnsWithCrossAccountToken_Test() {

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknB, paramsMap, null, true);

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Columns Fetched"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getEntityColumnsWithAdminToken_Test() {

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), paramsMap, null, true);

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Columns Fetched"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getEntityColumnsWithTeamMemberToken_Test() {

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), paramsMap, null, true);

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Columns Fetched"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getEntityColumnsWithRestrictedToken_Test() {

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), paramsMap, null, true);

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Columns Fetched"));
    }
}
