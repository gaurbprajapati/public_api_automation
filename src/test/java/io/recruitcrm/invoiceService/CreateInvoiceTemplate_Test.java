package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.*;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.*;
import io.rcrm.api.pojo.invoiceService.InvoiceTemplate;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CreateInvoiceTemplate_Test extends TestBase{
    
    public CreateInvoiceTemplate_Test() {
        super();
    }
    
    String apiKeyA;
    String apiKeyB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    String albatrossTknA;
    String albatrossTknB;
    JavaFakerPlacement placementFaker;
    String basePath = "invoices/templates";
    JavaFakerInvoice invoiceFaker;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        apiKeyB = getAccountApiKey("AccountB");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        placementFaker = new JavaFakerPlacement();
        invoiceFaker = new JavaFakerInvoice();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "createInvoiceTemplateData", groups = {"invoice_service", "nightly-build"})
    public void createInvoiceTemplateWithValidToken_Test(InvoiceTemplate invoiceTemplate) {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, invoiceTemplate);
        response.then().statusCode(201);
        response.then().body("meta.message", Matchers.containsString("Invoice template created successfully"));
        response.then().assertThat().body("data.templateName", Matchers.containsString(invoiceTemplate.getTemplateName()));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceTemplateWithInvalidToken_Test() {
        InvoiceTemplate invoiceTemplate = new InvoiceTemplate();
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, true, invoiceTemplate);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "createInvoiceTemplateData", groups = {"invoice_service", "nightly-build"})
    public void createInvoiceTemplateWithCrossAccountToken_Test(InvoiceTemplate invoiceTemplate) {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknB, null, true, invoiceTemplate);
        response.then().statusCode(201);
        response.then().body("meta.message", Matchers.containsString("Invoice template created successfully"));
        response.then().assertThat().body("data.templateName", Matchers.containsString(invoiceTemplate.getTemplateName()));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "createInvoiceTemplateData", groups = {"invoice_service", "nightly-build"})
    public void createInvoiceTemplateWithAdminToken_Test(InvoiceTemplate invoiceTemplate) {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, true, invoiceTemplate);
        response.then().statusCode(201);
        response.then().body("meta.message", Matchers.containsString("Invoice template created successfully"));
        response.then().assertThat().body("data.templateName", Matchers.containsString(invoiceTemplate.getTemplateName()));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "createInvoiceTemplateData", groups = {"invoice_service", "nightly-build"})
    public void createInvoiceTemplateWithTeamMemberToken_Test(InvoiceTemplate invoiceTemplate) {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, true, invoiceTemplate);
        response.then().statusCode(403);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Access Denied"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "createInvoiceTemplateData", groups = {"invoice_service", "nightly-build"})
    public void createInvoiceTemplateWithRestrictedTeamMemberToken_Test(InvoiceTemplate invoiceTemplate) {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, true, invoiceTemplate);
        response.then().statusCode(403);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Access Denied"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "createInvoiceTemplateData", groups = {"invoice_service", "nightly-build"})
    public void createInvoiceTemplateWithInvalidData_Test(InvoiceTemplate invoiceTemplate) {
        invoiceTemplate.setTemplateName(null);
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, invoiceTemplate);
        response.then().statusCode(400);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Template Name Cannot Be Empty"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceTemplateWithEmptyTemplateItems_Test() {
        InvoiceTemplate invoiceTemplate = new InvoiceTemplate();
        invoiceTemplate.setTemplateName(invoiceFaker.getInvoiceTemplateName());
        invoiceTemplate.setTemplateItems("");
        invoiceTemplate.setSharedWith("");
        invoiceTemplate.setDueIn("");
        invoiceTemplate.setTemplateTheme("");
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, invoiceTemplate);
        response.then().statusCode(500);
        response.then().body("error", Matchers.containsString("Internal Server Error"));
    }

    @DataProvider
    public Object[][] createInvoiceTemplateData() {
        Response usersResponse = function.getUsers(baseURL, apiKeyA);
        usersResponse.then().statusCode(200);
        JsonPath usersJsonPath = usersResponse.jsonPath();
        int userId = usersJsonPath.get("[0].id");

        Response response = function.generateInvoiceTableWithDefaultValues(syncFunctionURL, albatrossTknA);
        response.then().statusCode(200);
        String sfdtContent = response.jsonPath().get("sfdt");

        List<JSONObject> templateItems = new ArrayList<>();
        JSONObject templateItem = new JSONObject();
        templateItem.put("formula", "");
        templateItem.put("field_id", 1);
        templateItem.put("field_type", 4);
        templateItem.put("field_label", "Amount");
        templateItem.put("default_field_label", "Amount");
        templateItem.put("sequence_number", 1);
        templateItems.add(templateItem);

        InvoiceTemplate invoiceTemplate = function.createInvoiceTemplatePayload(invoiceFaker.getInvoiceTemplateName(), Arrays.asList(userId), new ArrayList<>(), "7 Days", sfdtContent, templateItems);
        return new Object[][] { { invoiceTemplate } };
    }
}
