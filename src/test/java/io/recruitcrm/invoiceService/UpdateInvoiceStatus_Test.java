package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.json.JSONObject;
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
public class UpdateInvoiceStatus_Test extends TestBase {

    public UpdateInvoiceStatus_Test() {
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
    public void updateInvoiceStatusWithValidToken_Test(int invoiceId) {
        JSONObject request = new JSONObject();
        request.put("statusId", 3);
        request.put("statusDate", fakerInvoice.getIssueDate());
        request.put("remark", "Status updated successfully");

        String basePath = "invoices/" + invoiceId + "/status";
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, request);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoice Status Updated Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//updateInvoiceStatus.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceStatusWithInvalidId_Test() {
        JSONObject request = new JSONObject();
        request.put("statusId", 3);
        request.put("statusDate", fakerInvoice.getIssueDate());
        request.put("remark", "Status updated successfully");

        String basePath = "invoices/" + placementFaker.getRandomID() + "/status";
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, request);
        response.then().statusCode(401);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Failed to Update Invoice Status : You are not authorized to edit record."));
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceStatusWithInvalidToken_Test() {
        JSONObject request = new JSONObject();
        request.put("statusId", 3);
        request.put("statusDate", fakerInvoice.getIssueDate());
        request.put("remark", "Status updated successfully");

        String basePath = "invoices/" + placementFaker.getRandomID() + "/status";
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, true, request);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceStatusWithCrossAccountToken_Test(int invoiceId) {
        JSONObject request = new JSONObject();
        request.put("statusId", 3);
        request.put("statusDate", fakerInvoice.getIssueDate());
        request.put("remark", "Status updated successfully");

        String basePath = "invoices/" + invoiceId + "/status";
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, albatrossTknB, null, true, request);
        response.then().statusCode(404);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Invoice not found"));
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceStatusWithAdminToken_Test(int invoiceId) {
        JSONObject request = new JSONObject();
        request.put("statusId", 4);
        request.put("statusDate", fakerInvoice.getIssueDate());
        request.put("remark", "Admin updated status");

        String basePath = "invoices/" + invoiceId + "/status";
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, true, request);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoice Status Updated Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//updateInvoiceStatus.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceStatusWithTeamMemberToken_Test(int invoiceId) {
        JSONObject request = new JSONObject();
        request.put("statusId", 4);
        request.put("statusDate", fakerInvoice.getIssueDate());
        request.put("remark", "Team member updated status");

        String basePath = "invoices/" + invoiceId + "/status";
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, true, request);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Invoice Status Updated Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//updateInvoiceStatus.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceStatusWithRestrictedTeamMemberToken_Test(int invoiceId) {
        JSONObject request = new JSONObject();
        request.put("statusId", 4);
        request.put("statusDate", fakerInvoice.getIssueDate());
        request.put("remark", "Restricted member updated status");

        String basePath = "invoices/" + invoiceId + "/status";
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, true, request);
        response.then().statusCode(401);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Failed to Update Invoice Status : You are not authorized to edit record."));
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceStatusWithInvalidStatusId_Test(int invoiceId) {
        JSONObject request = new JSONObject();
        request.put("statusId", fakerInvoice.getInvalidStatusId());
        request.put("statusDate", fakerInvoice.getIssueDate());
        request.put("remark", "Invalid status ID");

        String basePath = "invoices/" + invoiceId + "/status";
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, request);
        response.then().statusCode(404);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void updateInvoiceStatusWithMissingStatusId_Test(int invoiceId) {
        JSONObject request = new JSONObject();
        request.put("statusDate", fakerInvoice.getIssueDate());
        request.put("remark", "Missing status ID");

        String basePath = "invoices/" + invoiceId + "/status";
        Response response = RestClient.doPut("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, request);
        response.then().statusCode(500);
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

