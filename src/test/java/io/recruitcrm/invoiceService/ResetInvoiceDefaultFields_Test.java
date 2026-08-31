package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class ResetInvoiceDefaultFields_Test extends TestBase {

    public ResetInvoiceDefaultFields_Test() {
        super();
    }

    String apiKeyA;
    String apiKeyB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    String albatrossTknA;
    String albatrossTknB;
    JavaFakerPlacement placementFaker;
    String basePath = "invoices/default-fields/reset-field-labels";

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
    @Test(groups = {"invoice_service", "nightly-build"})
    public void resetInvoiceDefaultFieldsWithValidToken_Test() {
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, null);
        
        response.then().statusCode(200);
        response.then().body("data", Matchers.containsString("Field Labels Reset successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//resetInvoiceDefaultFields.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void resetInvoiceDefaultFieldsWithInvalidToken_Test() {
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, true, null);
        
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void resetInvoiceDefaultFieldsWithCrossAccountToken_Test() {
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, albatrossTknB, null, true, null);
        
        response.then().statusCode(200);
        response.then().body("data", Matchers.containsString("Field Labels Reset successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//resetInvoiceDefaultFields.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void resetInvoiceDefaultFieldsWithAdminToken_Test() {
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, true, null);
        
        response.then().statusCode(200);
        response.then().body("data", Matchers.containsString("Field Labels Reset successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//resetInvoiceDefaultFields.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void resetInvoiceDefaultFieldsWithTeamMemberToken_Test() {
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, true, null);
        
        response.then().statusCode(403);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Access Denied"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void resetInvoiceDefaultFieldsWithRestrictedToken_Test() {
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, true, null);
        
        response.then().statusCode(403);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Access Denied"));
    }
}
