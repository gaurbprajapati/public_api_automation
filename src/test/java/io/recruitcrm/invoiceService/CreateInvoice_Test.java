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
public class CreateInvoice_Test extends TestBase {

    public CreateInvoice_Test() {
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
    String basePath;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        apiKeyB = getAccountApiKey("AccountB");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        placementFaker = new JavaFakerPlacement();
        basePath = "invoices";
        fakerInvoice = new JavaFakerInvoice();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "addBusinessDetails", groups = {"invoice_service", "nightly-build"})
    public void createInvoiceWithValidToken_Test(int companyId, String invoicePrefix, String s3Key, int templateId) {

        Invoice invoice = function.createInvoicePayload(companyId, invoicePrefix, s3Key, templateId);
        
        Response response = RestClient.doPost("JSON", invoiceServiceURL, "invoices", albatrossTknA, null, true, invoice);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoice Created Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceWithInvalidToken_Test() {
        Invoice invoice = new Invoice();
        Response response = RestClient.doPost("JSON", invoiceServiceURL, "invoices", placementFaker.getInvalidToken(), null, true, invoice);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "addBusinessDetails", groups = {"invoice_service", "nightly-build"})
    public void createInvoiceWithCrossAccountToken_Test(int companyId, String invoicePrefix, String s3Key, int templateId) {

        Invoice invoice = function.createInvoicePayload(companyId, invoicePrefix, s3Key, templateId);
        Response response = RestClient.doPost("JSON", invoiceServiceURL, "invoices", albatrossTknB, null, true, invoice);
        response.then().statusCode(404);
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Company not found"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "addBusinessDetails", groups = {"invoice_service", "nightly-build"})
    public void createInvoiceWithAdminToken_Test(int companyId, String invoicePrefix, String s3Key, int templateId) {

        Invoice invoice = function.createInvoicePayload(companyId, invoicePrefix, s3Key, templateId);
        
        Response response = RestClient.doPost("JSON", invoiceServiceURL, "invoices", getRoleBasedToken("AccountA", "Admin"), null, true, invoice);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoice Created Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "addBusinessDetails", groups = {"invoice_service", "nightly-build"})
    public void createInvoiceWithTeamMemberToken_Test(int companyId, String invoicePrefix, String s3Key, int templateId) {

        Invoice invoice = function.createInvoicePayload(companyId, invoicePrefix, s3Key, templateId);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, "invoices", getRoleBasedToken("AccountA", "Team Member"), null, true, invoice);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoice Created Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "addBusinessDetails", groups = {"invoice_service", "nightly-build"})
    public void createInvoiceWithRestrictedTeamMemberToken_Test(int companyId, String invoicePrefix, String s3Key, int templateId) {

        Invoice invoice = function.createInvoicePayload(companyId, invoicePrefix, s3Key, templateId);
        
        Response response = RestClient.doPost("JSON", invoiceServiceURL, "invoices", getRoleBasedToken("AccountA", "Restricted"), null, true, invoice);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoice Created Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoice.json"));
    }

    @DataProvider
    public Object[][] addBusinessDetails() {
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
        int templateId = function.getInvoiceTemplateId(invoiceServiceURL, albatrossTknA, "Full-Time Job");
        return new Object[][] { { companyId, invoicePrefix, s3Key, templateId } };
    }
}
