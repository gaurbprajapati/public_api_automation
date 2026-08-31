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
import io.rcrm.api.pojo.invoiceService.Invoice;
import io.rcrm.api.javafaker.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class UpdateInvoice_Test extends TestBase {

    public UpdateInvoice_Test() {
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
    @Test(dataProvider = "getInvoiceData", groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceWithValidToken_Test(int invoiceId, int companyId, String invoicePrefix, String s3Key, int templateId) {
        
        Invoice invoice = function.createInvoicePayload(companyId, invoicePrefix, s3Key, templateId);
        invoice.setDescription("Updated Test Description");
        Response response = RestClient.doPatchOnce("JSON", invoiceServiceURL, basePath + "/" + invoiceId, albatrossTknA, null, true, invoice);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoice Updated Successfully"));
        response.then().assertThat().body("data.description", Matchers.notNullValue());
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//updateInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceData", groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceWithInvalidId_Test(int invoiceId, int companyId, String invoicePrefix, String s3Key, int templateId) {
        Invoice invoice = function.createInvoicePayload(companyId, invoicePrefix, s3Key, templateId);

        Response response = RestClient.doPatchOnce("JSON", invoiceServiceURL, basePath + "/" + placementFaker.getRandomID(), albatrossTknA, null, true, invoice);
        response.then().statusCode(401);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceWithInvalidToken_Test() {
        Invoice invoice = new Invoice();
        invoice.setInvoicePrefix("INV");
        invoice.setInvoiceNumber(fakerInvoice.invoiceNumber());
        invoice.setTemplateId(fakerInvoice.getInvoiceTemplateId());
        invoice.setCompanyId(placementFaker.getRandomID());
        invoice.setStatusId(fakerInvoice.getInvoiceStatusId());
        invoice.setCurrencyId(fakerInvoice.getCurrencyId());
        invoice.setTotalAmount(fakerInvoice.getTotalAmount());

        Response response = RestClient.doPatchOnce("JSON", invoiceServiceURL, basePath + "/" + placementFaker.getRandomID(), placementFaker.getInvalidToken(), null, true, invoice);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceData", groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceWithCrossAccountToken_Test(int invoiceId, int companyId, String invoicePrefix, String s3Key, int templateId) {
        Invoice invoice = function.createInvoicePayload(companyId, invoicePrefix, s3Key, templateId);

        Response response = RestClient.doPatchOnce("JSON", invoiceServiceURL, basePath + "/" + invoiceId, albatrossTknB, null, true, invoice);
        response.then().statusCode(404);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Invoice not found"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceData", groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceWithAdminToken_Test(int invoiceId, int companyId, String invoicePrefix, String s3Key, int templateId) {
        Invoice invoice = function.createInvoicePayload(companyId, invoicePrefix, s3Key, templateId);
        Response response = RestClient.doPatchOnce("JSON", invoiceServiceURL, basePath + "/" + invoiceId, getRoleBasedToken("AccountA", "Admin"), null, true, invoice);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoice Updated Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//updateInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceData", groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceWithTeamMemberToken_Test(int invoiceId, int companyId, String invoicePrefix, String s3Key, int templateId) {
        Invoice invoice = function.createInvoicePayload(companyId, invoicePrefix, s3Key, templateId);

        Response response = RestClient.doPatchOnce("JSON", invoiceServiceURL, basePath + "/" + invoiceId, getRoleBasedToken("AccountA", "Team Member"), null, true, invoice);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoice Updated Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//updateInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceData", groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceWithRestrictedTeamMemberToken_Test(int invoiceId, int companyId, String invoicePrefix, String s3Key, int templateId) {
        Invoice invoice = function.createInvoicePayload(companyId, invoicePrefix, s3Key, templateId);
        
        Response response = RestClient.doPatchOnce("JSON", invoiceServiceURL, basePath + "/" + invoiceId, getRoleBasedToken("AccountA", "Restricted"), null, true, invoice);
        System.out.println(response.jsonPath().prettyPrint());
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Failed to Update Invoice: You are not authorized to update invoice"));
    }

    @DataProvider
    public Object[][] getInvoiceData() {
        Response response = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
        String companySlug = response.jsonPath().getString("slug");
        int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
        Response response1 = function.addBusinessDetails(invoiceServiceURL, albatrossTknA);
        String invoicePrefix = response1.jsonPath().getString("data.invoiceIdPrefix");
        String invoiceNumber = response1.jsonPath().get("data.invoiceIdNumber");
        int accountId = response1.jsonPath().get("data.accountId");
        String fileName = invoicePrefix + "-" + String.valueOf(invoiceNumber);
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("fileName", fileName + ".pdf");
        pathParams.put("acl", "private");
        pathParams.put("key", String.valueOf(accountId) + "/invoices/" + fileName + "/" + fileName + ".pdf");
        Response response2 = RestClient.doGet("JSON", invoiceServiceURL, "invoice/files/generate-upload-url", albatrossTknA, pathParams, null, true);
        response2.then().statusCode(200);
        String s3Key = response2.jsonPath().getString("data.key");
        
        Response createResponse = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
        int invoiceId = createResponse.jsonPath().get("data");
        int templateId = function.getInvoiceTemplateId(invoiceServiceURL, albatrossTknA, "Full-Time Job");
        
        return new Object[][] { { invoiceId, companyId, invoicePrefix, s3Key, templateId } };
    }
}
