package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.*;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class DeleteInvoiceTemplate_Test extends TestBase {

    public DeleteInvoiceTemplate_Test() {
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
    String basePath = "invoices/templates/delete";

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
    public void deleteInvoiceTemplateWithValidToken_Test(int templateId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(templateId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice templates deleted successfully"));
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//deleteInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getDefaultTemplateId", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceTemplateWithDefaultTemplate_Test(int templateId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(templateId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice templates deleted successfully"));
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceTemplateWithInvalidId_Test() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(placementFaker.getRandomID()));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice templates deleted successfully"));
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceTemplateWithInvalidToken_Test() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(placementFaker.getRandomID()));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getTemplateId", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceTemplateWithCrossAccountToken_Test(int templateId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(templateId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknB, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice templates deleted successfully"));
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getTemplateId", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceTemplateWithAdminToken_Test(int templateId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(templateId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice templates deleted successfully"));
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//deleteInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getTemplateId", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceTemplateWithTeamMemberToken_Test(int templateId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(templateId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, null, true, requestBody);
        response.then().statusCode(403);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Access Denied"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getTemplateId", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceTemplateWithRestrictedToken_Test(int templateId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("ids", Collections.singletonList(templateId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, null, true, requestBody);
        response.then().statusCode(403);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Access Denied"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceTemplateWithNoId_Test() {
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, null);
        response.then().statusCode(400);
        response.then().assertThat().body("error", Matchers.containsString("Bad Request"));
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

    @DataProvider
    public Object[][] getDefaultTemplateId() {
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

