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
public class CloneInvoiceTemplate_Test extends TestBase {

    public CloneInvoiceTemplate_Test() {
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
    @Test(dataProvider = "getTemplateId", groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceTemplateWithValidToken_Test(int templateId) {
        String basePath = "invoices/templates/" + templateId + "/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, null);
        response.then().statusCode(201);
        response.then().body("meta.message", Matchers.containsString("Invoice template cloned successfully"));
        response.then().assertThat().body("data.id", Matchers.notNullValue());
        response.then().assertThat().body("data.templateName", Matchers.containsString("Clone of"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//cloneInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceTemplateWithInvalidId_Test() {
        String basePath = "invoices/templates/" + placementFaker.getRandomID() + "/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, null);
        response.then().statusCode(404);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Invoice Template Not Found"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceTemplateWithInvalidToken_Test() {
        String basePath = "invoices/templates/" + placementFaker.getRandomID() + "/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, true, null);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getTemplateId", groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceTemplateWithCrossAccountToken_Test(int templateId) {
        String basePath = "invoices/templates/" + templateId + "/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknB, null, true, null);
        response.then().statusCode(404);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Invoice Template Not Found"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getTemplateId", groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceTemplateWithAdminToken_Test(int templateId) {
        String basePath = "invoices/templates/" + templateId + "/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, true, null);
        response.then().statusCode(201);
        response.then().body("meta.message", Matchers.containsString("Invoice template cloned successfully"));
        response.then().assertThat().body("data.id", Matchers.notNullValue());
        response.then().assertThat().body("data.templateName", Matchers.containsString("Clone of"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//cloneInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getTemplateId", groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceTemplateWithTeamMemberToken_Test(int templateId) {
        String basePath = "invoices/templates/" + templateId + "/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, true, null);
        response.then().statusCode(403);
        response.then().assertThat().body("data.templateId", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Access Denied"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getTemplateId", groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceTemplateWithRestrictedTeamMemberToken_Test(int templateId) {
        String basePath = "invoices/templates/" + templateId + "/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, true, null);
        response.then().statusCode(403);
        response.then().assertThat().body("data.templateId", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Access Denied"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void cloneInvoiceTemplateWithNullTemplateId_Test() {
        String basePath = "invoices/templates/null/clone";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, null);
        response.then().statusCode(400);
        response.then().body("error", Matchers.containsString("Bad Request"));
    }

    @DataProvider
    public Object[][] getTemplateId() {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("searchText", "");
        queryParams.put("orderByColumn", "");
        queryParams.put("sortDirection", "");
        JSONObject body = new JSONObject();
        body.put("sortPriorityList", new ArrayList<>());
        body.put("isPayBill", 0);
        Response response = RestClient.doPost("JSON", invoiceServiceURL, "invoices/templates/search", albatrossTknA, queryParams, true, body);
        response.then().statusCode(200);
        int templateId = response.jsonPath().get("data.templates[0].id");
        return new Object[][] {
            { templateId }
        };
    }
}

