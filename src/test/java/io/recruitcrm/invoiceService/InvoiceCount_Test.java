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
public class InvoiceCount_Test extends TestBase {

    public InvoiceCount_Test() {
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
    String basePath = "invoices/count";

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
    public void getInvoiceCountWithValidToken_Test(int invoiceId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Count Fetched Successfully"));
        response.then().assertThat().body("data.userCount", Matchers.equalTo(1));
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(1));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceCount.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getInvoiceCountWithInvalidToken_Test() {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceCountWithCrossAccountToken_Test(int invoiceId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknB, null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("data.userCount", Matchers.equalTo(0));
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(0));
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Count Fetched Successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceCountWithAdminToken_Test(int invoiceId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Count Fetched Successfully"));
        response.then().assertThat().body("data.userCount", Matchers.equalTo(0));
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(1));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceCount.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceCountWithTeamMemberToken_Test(int invoiceId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Count Fetched Successfully"));
        response.then().assertThat().body("data.userCount", Matchers.equalTo(0));
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(1));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceCount.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceCountWithRestrictedTeamMemberToken_Test(int invoiceId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Count Fetched Successfully"));
        response.then().assertThat().body("data.userCount", Matchers.equalTo(0));
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(0));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceCount.json"));
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
