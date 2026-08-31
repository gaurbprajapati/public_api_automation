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
public class CloneInvoice_Test extends TestBase {

    public CloneInvoice_Test() {
        super();
    }

    String apiKeyA;
    String apiKeyB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    String albatrossTknA;
    String albatrossTknB;
    JavaFakerPlacement placementFaker;
    JavaFakerInvoice fakerInvoice;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        apiKeyB = getAccountApiKey("AccountB");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        placementFaker = new JavaFakerPlacement();
        fakerInvoice = new JavaFakerInvoice();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceWithValidToken_Test(int invoiceId, int companyId) {
        String basePath = "invoices/" + invoiceId + "/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, null);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoice Cloned Successfully"));
        response.then().assertThat().body("data.companyId", Matchers.is(companyId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//cloneInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceWithInvalidId_Test() {
        String basePath = "invoices/" + placementFaker.getRandomID() + "/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, null);
        response.then().statusCode(404);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Invoice not found"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceWithInvalidToken_Test() {
        String basePath = "invoices/" + placementFaker.getRandomID() + "/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, true, null);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceWithCrossAccountToken_Test(int invoiceId, int companyId) {
        String basePath = "invoices/" + invoiceId + "/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknB, null, true, null);
        response.then().statusCode(404);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Invoice not found"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceWithAdminToken_Test(int invoiceId, int companyId) {
        String basePath = "invoices/" + invoiceId + "/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, true, null);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoice Cloned Successfully"));
        response.then().assertThat().body("data.companyId", Matchers.is(companyId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//cloneInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceWithTeamMemberToken_Test(int invoiceId, int companyId) {
        String basePath = "invoices/" + invoiceId + "/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, true, null);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoice Cloned Successfully"));
        response.then().assertThat().body("data.companyId", Matchers.is(companyId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//cloneInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceWithRestrictedTeamMemberToken_Test(int invoiceId, int companyId) {
        String basePath = "invoices/" + invoiceId + "/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, true, null);
        response.then().statusCode(404);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Invoice not found"));
    }

    @DataProvider
    public Object[][] getInvoiceId() {
        Response response = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
        String companySlug = response.jsonPath().getString("slug");
        int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
        Response response1 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
        int invoiceId = response1.jsonPath().get("data");
        return new Object[][] {
            { invoiceId, companyId }
        };
    }
}

