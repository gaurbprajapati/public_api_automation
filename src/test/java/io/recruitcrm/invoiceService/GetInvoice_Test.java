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
public class GetInvoice_Test extends TestBase {

    public GetInvoice_Test() {
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
    String basePath = "invoices";

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
    public void getInvoiceWithValidToken_Test(int invoiceId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath+"/"+invoiceId, albatrossTknA, null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Fetched Successfully"));
        response.then().assertThat().body("data.id", Matchers.is(invoiceId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getInvoiceWithInvalidId_Test() {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath+"/"+placementFaker.getRandomID(), albatrossTknA, null, null, true);
        response.then().statusCode(404);
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Invoice not found"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getInvoiceWithInvalidToken_Test() {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath+"/"+placementFaker.getRandomID(), placementFaker.getInvalidToken(), null, null, true);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceWithCrossAccountToken_Test(int invoiceId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath+"/"+invoiceId, albatrossTknB, null, null, true);
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Invoice not found"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceWithAdminToken_Test(int invoiceId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath+"/"+invoiceId, getRoleBasedToken("AccountA", "Admin"), null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Fetched Successfully"));
        response.then().assertThat().body("data.id", Matchers.is(invoiceId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceWithTeamMemberToken_Test(int invoiceId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath+"/"+invoiceId, getRoleBasedToken("AccountA", "Team Member"), null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Fetched Successfully"));
        response.then().assertThat().body("data.id", Matchers.is(invoiceId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceWithRestrictedTeamMemberToken_Test(int invoiceId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath+"/"+invoiceId, getRoleBasedToken("AccountA", "Restricted"), null, null, true);
        response.then().statusCode(404);
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("data", Matchers.nullValue());
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
            { invoiceId }
        };
    }

}
