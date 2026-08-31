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
public class GetInvoiceHistory_Test extends TestBase {

    public GetInvoiceHistory_Test() {
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
    @Test(dataProvider = "getInvoiceIdWithHistory", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceHistoryWithValidToken_Test(int invoiceId) {
        String basePath = "invoices/" + invoiceId + "/history";
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true);
        response.then().statusCode(200);
        
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice History Fetched Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceHistory.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getInvoiceHistoryWithInvalidId_Test() {
        String basePath = "invoices/" + placementFaker.getRandomID() + "/history";
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Failed to View Invoice History : You are not authorized to view invoice history."));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getInvoiceHistoryWithInvalidToken_Test() {
        String basePath = "invoices/" + placementFaker.getRandomID() + "/history";
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceIdWithHistory", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceHistoryWithCrossAccountToken_Test(int invoiceId) {
        String basePath = "invoices/" + invoiceId + "/history";
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknB, null, null, true);
        response.then().statusCode(404);
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Invoice not found"));

    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceIdWithHistory", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceHistoryWithAdminToken_Test(int invoiceId) {
        String basePath = "invoices/" + invoiceId + "/history";
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice History Fetched Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceHistory.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceIdWithHistory", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceHistoryWithTeamMemberToken_Test(int invoiceId) {
        String basePath = "invoices/" + invoiceId + "/history";
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice History Fetched Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceHistory.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceIdWithHistory", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceHistoryWithRestrictedTeamMemberToken_Test(int invoiceId) {
        String basePath = "invoices/" + invoiceId + "/history";
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, null, true);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Failed to View Invoice History : You are not authorized to view invoice history."));
    }

    @DataProvider
    public Object[][] getInvoiceIdWithHistory() {
        Response response = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
        String companySlug = response.jsonPath().getString("slug");
        int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
        Response response1 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
        int invoiceId = response1.jsonPath().get("data");
        
        JSONObject statusRequest = new JSONObject();
        statusRequest.put("statusId", fakerInvoice.getInvoiceStatusId());
        statusRequest.put("statusDate", fakerInvoice.getIssueDate());
        statusRequest.put("remark", "First status update");
        
        String statusPath = "invoices/" + invoiceId + "/status";
        Response statusResponse = RestClient.doPut("JSON", invoiceServiceURL, statusPath, albatrossTknA, null, true, statusRequest);
        statusResponse.then().statusCode(200);
        
        JSONObject statusRequest2 = new JSONObject();
        statusRequest2.put("statusId", fakerInvoice.getInvoiceStatusId());
        statusRequest2.put("statusDate", fakerInvoice.getIssueDate());
        statusRequest2.put("remark", "Second status update");
        
        Response statusResponse2 = RestClient.doPut("JSON", invoiceServiceURL, statusPath, albatrossTknA, null, true, statusRequest2);
        statusResponse2.then().statusCode(200);
        
        return new Object[][] {
            { invoiceId }
        };
    }
}

