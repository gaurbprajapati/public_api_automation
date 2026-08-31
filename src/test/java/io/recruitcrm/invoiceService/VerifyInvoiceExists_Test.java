package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.*;

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
public class VerifyInvoiceExists_Test extends TestBase {

    public VerifyInvoiceExists_Test() {
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
    String basePath = "invoices/exists";

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
    @Test(dataProvider = "getInvoiceData")
    public void verifyInvoiceExistsWithValidToken_Test(String prefix, String number) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("prefix", prefix);
        queryParams.put("number", number);
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Data Fetched Successfully"));
        response.then().assertThat().body("data.present", Matchers.is(true));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//verifyInvoiceExists.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceData")
    public void verifyInvoiceExistsWithInvalidPrefix_Test(String prefix, String number) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("prefix", fakerInvoice.invoicePrefix());
        queryParams.put("number", number);
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Data Fetched Successfully"));
        response.then().assertThat().body("data.present", Matchers.is(false));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//verifyInvoiceExists.json"));
    }
    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void verifyInvoiceExistsWithInvalidToken_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("prefix", fakerInvoice.invoicePrefix());
        queryParams.put("number", fakerInvoice.invoiceNumber());
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), queryParams, null, true);
        response.then().statusCode(404);
        response.then().assertThat().body("html.head.title", Matchers.containsString("HTTP Status 404"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceData", groups = {"invoice_service", "nightly-build"})
    public void verifyInvoiceExistsWithCrossAccountToken_Test(String prefix, String number) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("prefix", prefix);
        queryParams.put("number", number);
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknB, queryParams, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Data Fetched Successfully"));
        response.then().assertThat().body("data.present", Matchers.is(false));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//verifyInvoiceExists.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceData", groups = {"invoice_service", "nightly-build"})
    public void verifyInvoiceExistsWithAdminToken_Test(String prefix, String number) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("prefix", prefix);
        queryParams.put("number", number);
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), queryParams, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Data Fetched Successfully"));
        response.then().assertThat().body("data.present", Matchers.is(true));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//verifyInvoiceExists.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceData", groups = {"invoice_service", "nightly-build"})
    public void verifyInvoiceExistsWithTeamMemberToken_Test(String prefix, String number) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("prefix", prefix);
        queryParams.put("number", number);
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), queryParams, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Data Fetched Successfully"));
        response.then().assertThat().body("data.present", Matchers.is(true));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//verifyInvoiceExists.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceData", groups = {"invoice_service", "nightly-build"})
    public void verifyInvoiceExistsWithRestrictedTeamMemberToken_Test(String prefix, String number) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("prefix", prefix);
        queryParams.put("number", number);
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), queryParams, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Data Fetched Successfully"));
        response.then().assertThat().body("data.present", Matchers.is(true));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//verifyInvoiceExists.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void verifyInvoiceExistsWithMissingPrefix_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("number", fakerInvoice.invoiceNumber());
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, null, true);
        response.then().statusCode(400);
        response.then().assertThat().body("error", Matchers.containsString("Bad Request"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void verifyInvoiceExistsWithMissingNumber_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("prefix", fakerInvoice.invoicePrefix());
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, null, true);
        response.then().statusCode(400);
        response.then().assertThat().body("error", Matchers.containsString("Bad Request"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void verifyInvoiceExistsWithEmptyParameters_Test() {
        Map<String, String> queryParams = new HashMap<>();
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, null, true);
        response.then().statusCode(400);
        response.then().assertThat().body("error", Matchers.containsString("Bad Request"));
    }

    @DataProvider
    public Object[][] getInvoiceData() {
        Response response = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
        String companySlug = response.jsonPath().getString("slug");
        int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");

        Response invoiceResponse = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
        int invoiceId = invoiceResponse.jsonPath().get("data");
        Response getInvoiceResponse = RestClient.doGet("JSON", invoiceServiceURL, "invoices/"+invoiceId, albatrossTknA, null, null, true);
        getInvoiceResponse.then().statusCode(200);
        String invoicePrefix = getInvoiceResponse.jsonPath().getString("data.invoicePrefix");
        String invoiceNumber = getInvoiceResponse.jsonPath().getString("data.invoiceNumber");

        return new Object[][] {
            { invoicePrefix, invoiceNumber }
        };
    }
}
