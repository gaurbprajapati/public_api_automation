package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import java.util.*;
import org.hamcrest.Matchers;
import org.testng.annotations.*;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class AccountViewColumnsPlacements_Test extends TestBase {

    String albatrossTknA;
    String albatrossTknB;
    JavaFakerPlacement placementFaker;
    String basePath = "account-view-columns";

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        placementFaker = new JavaFakerPlacement();
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getAccountViewColumnsPlacementsWithValidToken_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entity", "placements");

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, null, true);
        
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Placements Columns Fetched"));
        response.then().assertThat().body("meta.responseType.context", Matchers.is("Request is successful"));
        response.then().assertThat().body("data", Matchers.notNullValue());
        response.then().assertThat().body("data[0].accountViewColumns", Matchers.notNullValue());

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/accountViewColumns.json"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getAccountViewColumnsPlacementsWithInvalidToken_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entity", "placements");

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), queryParams, null, true);
        
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getAccountViewColumnsPlacementsWithoutToken_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entity", "placements");

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, "", queryParams, null, true);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
        response.then().assertThat().body("data", Matchers.is("Missing bearer token in header"));

    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getAccountViewColumnsPlacementsWithCrossAccountToken_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entity", "placements");

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknB, queryParams, null, true);
        
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Placements Columns Fetched"));
        response.then().assertThat().body("meta.status", Matchers.is(200));
        response.then().assertThat().body("meta.responseType.context", Matchers.is("Request is successful"));
        response.then().assertThat().body("meta.responseType.code", Matchers.is(103));
        response.then().assertThat().body("data", Matchers.notNullValue());

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/accountViewColumns.json"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getAccountViewColumnsPlacementsWithAdminToken_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entity", "placements");

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), queryParams, null, true);
        
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Placements Columns Fetched"));
        response.then().assertThat().body("meta.status", Matchers.is(200));
        response.then().assertThat().body("meta.responseType.context", Matchers.is("Request is successful"));
        response.then().assertThat().body("data", Matchers.notNullValue());

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/accountViewColumns.json"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getAccountViewColumnsPlacementsWithTeamMemberToken_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entity", "placements");

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), queryParams, null, true);
        
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Placements Columns Fetched"));
        response.then().assertThat().body("meta.status", Matchers.is(200));
        response.then().assertThat().body("meta.responseType.context", Matchers.is("Request is successful"));
        response.then().assertThat().body("data", Matchers.notNullValue());

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/accountViewColumns.json"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getAccountViewColumnsPlacementsWithRestrictedTeamMemberToken_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entity", "placements");

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), queryParams, null, true);
        
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Placements Columns Fetched"));
        response.then().assertThat().body("meta.status", Matchers.is(200));
        response.then().assertThat().body("meta.responseType.context", Matchers.is("Request is successful"));
        response.then().assertThat().body("data", Matchers.notNullValue());

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/accountViewColumns.json"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getAccountViewColumnsPlacementsWithoutEntityParam_Test() {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true);
        response.then().statusCode(Matchers.is(400));
        response.then().assertThat().body("error", Matchers.containsString("Bad Request"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build") // Issue : Its fetching successfully 
    public void getAccountViewColumnsPlacementsWithInvalidEntity_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entity", "invalid_entity_name");

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, null, true);
        response.then().statusCode(200);
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getAccountViewColumnsPlacementsWithEmptyEntity_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entity", "");

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, null, true);
        response.then().statusCode(500);
        response.then().assertThat().body("error", Matchers.is("Internal Server Error"));
    }
}
