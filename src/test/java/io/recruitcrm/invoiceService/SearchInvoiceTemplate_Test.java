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

import java.util.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class SearchInvoiceTemplate_Test extends TestBase {

    public SearchInvoiceTemplate_Test() {
        super();
    }

    String apiKeyA;
    String apiKeyB;
    String albatrossTknA;
    String albatrossTknB;
    String basePath = "invoices/templates/search";
    Map<String, String> queryParams;
    JavaFakerInvoice invoiceFaker;
    JavaFakerPlacement placementFaker;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        apiKeyB = getAccountApiKey("AccountB");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        queryParams = new LinkedHashMap<>();
        queryParams.put("searchText", "");
        queryParams.put("orderByColumn", "");
        queryParams.put("sortDirection", "");
        invoiceFaker = new JavaFakerInvoice();
        placementFaker = new JavaFakerPlacement();
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getTemplateId", groups = {"invoice_service", "nightly-build"})
    public void searchInvoiceTemplateWithValidToken_Test(int templateId) {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, true, getDefaultRequestBody());
        
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice template list fetched successfully"));
        response.then().assertThat().body("data.templates[0].id", Matchers.equalTo(templateId));
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(4));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//searchInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getTemplateId", groups = {"invoice_service", "nightly-build"})
    public void searchInvoiceTemplateWithValidText_Test(int templateId) {
        queryParams.put("searchText", "Custom");
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, true, getDefaultRequestBody());
        
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice template list fetched successfully"));
        response.then().assertThat().body("data.templates[0].id", Matchers.equalTo(templateId));
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(1));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void searchInvoiceTemplateWithInvalidToken_Test() {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), queryParams, true, getDefaultRequestBody());

        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getTemplateId", groups = {"invoice_service", "nightly-build"})
    public void searchInvoiceTemplateWithCrossAccountToken_Test(int templateId) {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknB, queryParams, true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice template list fetched successfully"));
        response.then().assertThat().body("data.templates[0].id", Matchers.not(Matchers.equalTo(templateId)));
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(3));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//searchInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getTemplateId", groups = {"invoice_service", "nightly-build"})
    public void searchInvoiceTemplateWithAdminToken_Test(int templateId) {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), queryParams, true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice template list fetched successfully"));
        response.then().assertThat().body("data.templates[0].id", Matchers.equalTo(templateId));
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(4));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//searchInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void searchInvoiceTemplateWithTeamMemberToken_Test() {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), queryParams, true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("data.templates", Matchers.empty());
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice template list fetched successfully"));
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(0));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//searchInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void searchInvoiceTemplateWithRestrictedToken_Test() {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), queryParams, true, getDefaultRequestBody());
        
        response.then().statusCode(200);
        response.then().assertThat().body("data.templates", Matchers.empty());
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice template list fetched successfully"));
        response.then().assertThat().body("data.totalCount", Matchers.equalTo(0));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//searchInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void searchInvoiceTemplate_400() {
        JSONObject body = new JSONObject();
        body.put("sortPriorityList", "asc");
        body.put("isPayBill", 0);
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, true, body);
        response.then().statusCode(400);
        response.then().assertThat().body("error", Matchers.containsString("Bad Request"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void searchInvoiceTemplateWithNullPayAndBill_Test() {
        JSONObject body = new JSONObject();
        body.put("sortPriorityList", new ArrayList<>());
        body.put("isPayBill", JSONObject.NULL);
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, queryParams, true, body);
        response.then().statusCode(500);
        response.then().assertThat().body("error", Matchers.containsString("Internal Server Error"));
    }

    @DataProvider
    public Object[][] getTemplateId() {
        Response response1 = function.createInvoiceTemplate(invoiceServiceURL, albatrossTknA, baseURL, apiKeyA, syncFunctionURL);
        response1.then().statusCode(201);
        int templateId = response1.jsonPath().get("data.id");
        return new Object[][] {
            { templateId }
        };
    }

    private JSONObject getDefaultRequestBody() {
        JSONObject body = new JSONObject();
        body.put("sortPriorityList", new ArrayList<>());
        body.put("isPayBill", 0);
        return body;
    }
}

