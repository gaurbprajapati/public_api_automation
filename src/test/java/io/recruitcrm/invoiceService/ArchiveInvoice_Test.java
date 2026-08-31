package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.json.*;
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
public class ArchiveInvoice_Test extends TestBase {

    public ArchiveInvoice_Test() {
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
    String basePath = "invoices/archive";

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
    public void archiveInvoiceWithValidToken_Test(int invoiceId) {
        JSONObject request = new JSONObject();
        JSONArray invoiceIds = new JSONArray();
        invoiceIds.put(invoiceId);
        request.put("invoiceIds", invoiceIds);

        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, request);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoices Archived Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//archiveInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void archiveInvoiceWithInvalidId_Test() {
        JSONObject request = new JSONObject();
        JSONArray invoiceIds = new JSONArray();
        invoiceIds.put(placementFaker.getRandomID());
        request.put("invoiceIds", invoiceIds);

        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, request);
        response.then().statusCode(401);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Failed to Archive Invoice(s) : You are not authorized to archive selected records."));
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void archiveInvoiceWithInvalidToken_Test() {
        JSONObject request = new JSONObject();
        JSONArray invoiceIds = new JSONArray();
        invoiceIds.put(placementFaker.getRandomID());
        request.put("invoiceIds", invoiceIds);

        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, true, request);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void archiveInvoiceWithCrossAccountToken_Test(int invoiceId) {
        JSONObject request = new JSONObject();
        JSONArray invoiceIds = new JSONArray();
        invoiceIds.put(invoiceId);
        request.put("invoiceIds", invoiceIds);

        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, albatrossTknB, null, true, request);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoices Archived Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//archiveInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void archiveInvoiceWithAdminToken_Test(int invoiceId) {
        JSONObject request = new JSONObject();
        JSONArray invoiceIds = new JSONArray();
        invoiceIds.put(invoiceId);
        request.put("invoiceIds", invoiceIds);

        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, true, request);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoices Archived Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//archiveInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void archiveInvoiceWithTeamMemberToken_Test(int invoiceId) {
        JSONObject request = new JSONObject();
        JSONArray invoiceIds = new JSONArray();
        invoiceIds.put(invoiceId);
        request.put("invoiceIds", invoiceIds);

        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, true, request);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Archived Successfully"));
    } 

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void archiveInvoiceWithRestrictedTeamMemberToken_Test(int invoiceId) {
        JSONObject request = new JSONObject();
        JSONArray invoiceIds = new JSONArray();
        invoiceIds.put(invoiceId);
        request.put("invoiceIds", invoiceIds);

        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, true, request);
        response.then().statusCode(401);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Failed to Archive Invoice(s) : You are not authorized to archive selected records"));
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("data", Matchers.nullValue());
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

