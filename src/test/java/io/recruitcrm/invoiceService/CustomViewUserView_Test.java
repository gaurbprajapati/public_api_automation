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
public class CustomViewUserView_Test extends TestBase {

    String albatrossTknA;
    String albatrossTknB;
    int accountIdA;
    int accountIdB;
    JavaFakerPlacement placementFaker;
    String basePath = "custom-view/user-view";

    @BeforeClass(alwaysRun = true)    public void setUp() {
        accountIdA = getAccountId("AccountA");
        accountIdB = getAccountId("AccountB");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        placementFaker = new JavaFakerPlacement();
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getEntityIdData", groups = {"invoice_service", "nightly-build"})
    public void getCustomViewUserViewWithValidToken_Test(String entityId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityId", entityId);

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, null, true);
        
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("User View Fetched Successfully."));
        response.then().assertThat().body("data.accountId", Matchers.is(accountIdA));
        response.then().assertThat().body("data.entityId", Matchers.is(Integer.parseInt(entityId)));
        response.then().assertThat().body("data.listActions", Matchers.notNullValue());
        response.then().assertThat().body("data.groupedBy", Matchers.notNullValue());
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/customViewUserView.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getEntityIdData", groups = {"invoice_service", "nightly-build"})
    public void getCustomViewUserViewWithInvalidToken_Test(String entityId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityId", entityId);

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), queryParams, null, true);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getEntityIdData", groups = {"invoice_service", "nightly-build"})
    public void getCustomViewUserViewWithoutToken_Test(String entityId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityId", entityId);

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, "", queryParams, null, true);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
        response.then().assertThat().body("data", Matchers.is("Missing bearer token in header"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getEntityIdData", groups = {"invoice_service", "nightly-build"})
    public void getCustomViewUserViewWithCrossAccountToken_Test(String entityId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityId", entityId);

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknB, queryParams, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("User View Fetched Successfully."));
        response.then().assertThat().body("data.accountId", Matchers.is(accountIdB));
        response.then().assertThat().body("data.entityId", Matchers.is(Integer.parseInt(entityId)));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/customViewUserView.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getEntityIdData", groups = {"invoice_service", "nightly-build"})
    public void getCustomViewUserViewWithAdminToken_Test(String entityId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityId", entityId);

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), queryParams, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("User View Fetched Successfully."));
        response.then().assertThat().body("data.accountId", Matchers.is(accountIdA));
        response.then().assertThat().body("data.entityId", Matchers.is(Integer.parseInt(entityId)));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/customViewUserView.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getEntityIdData", groups = {"invoice_service", "nightly-build"})
    public void getCustomViewUserViewWithTeamMemberToken_Test(String entityId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityId", entityId);

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), queryParams, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("User View Fetched Successfully."));
        response.then().assertThat().body("data.accountId", Matchers.is(accountIdA));
        response.then().assertThat().body("data.entityId", Matchers.is(Integer.parseInt(entityId)));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/customViewUserView.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getEntityIdData", groups = {"invoice_service", "nightly-build"})
    public void getCustomViewUserViewWithRestrictedTeamMemberToken_Test(String entityId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityId", entityId);

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), queryParams, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("User View Fetched Successfully."));
        response.then().assertThat().body("data.accountId", Matchers.is(accountIdA));
        response.then().assertThat().body("data.entityId", Matchers.is(Integer.parseInt(entityId)));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/customViewUserView.json"));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getCustomViewUserViewWithoutEntityIdParam_Test() {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true);
        response.then().statusCode(Matchers.is(400));
        response.then().assertThat().body("errors[0].message", Matchers.is("Query parameter entityId cannot be null."));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"}) // Issue : Its fetching invalid entity id as well
    public void getCustomViewUserViewWithInvalidEntityId_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityId", "999999");

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, null, true);
        response.then().statusCode(Matchers.is(200));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getCustomViewUserViewWithEmptyEntityId_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityId", "");
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, null, true);
        response.then().statusCode(Matchers.is(400));
        response.then().assertThat().body("errors[0].message", Matchers.is("Query parameter entityId cannot be null."));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getCustomViewUserViewWithNonNumericEntityId_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityId", "abc");

        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, null, true);
        response.then().statusCode(Matchers.is(400));
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Failed to convert property value of type 'java.lang.String' to required type 'java.lang.Integer' for property 'entityId'; For input string:"));
    }

    @DataProvider
    public Object[][] getEntityIdData() {
        return new Object[][] { {"16"}, {"15"} };
    }
}
