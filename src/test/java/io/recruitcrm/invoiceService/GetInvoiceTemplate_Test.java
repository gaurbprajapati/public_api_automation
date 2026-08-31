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
public class GetInvoiceTemplate_Test extends TestBase {

    public GetInvoiceTemplate_Test() {
        super();
    }

    String apiKeyA;
    String apiKeyB;
    String albatrossTknA;
    String albatrossTknB;
    JavaFakerInvoice invoiceFaker;
    JavaFakerPlacement placementFaker;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    String basePath = "invoices/templates/{id}";

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        apiKeyB = getAccountApiKey("AccountB");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        invoiceFaker = new JavaFakerInvoice();
        placementFaker = new JavaFakerPlacement();
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getDefaultTemplateId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceTemplateWithValidToken_Test(int templateId) {
        Map<String, String> pathParams = new LinkedHashMap<>();
        pathParams.put("id", String.valueOf(templateId));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, null, pathParams, true);

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice template fetched successfully"));
        response.then().assertThat().body("data.id", Matchers.equalTo(templateId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getInvoiceTemplateWithInvalidId_Test() {
        Map<String, String> pathParams = new LinkedHashMap<>();
        pathParams.put("id", String.valueOf(placementFaker.getRandomID()));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, null, pathParams, true);

        response.then().statusCode(404);
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getInvoiceTemplateWithInvalidToken_Test() {
        Map<String, String> pathParams = new LinkedHashMap<>();
        pathParams.put("id", String.valueOf(placementFaker.getRandomID()));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, pathParams, true);

        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getDefaultTemplateId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceTemplateWithCrossAccountToken_Test(int templateId) {
        Map<String, String> pathParams = new LinkedHashMap<>();
        pathParams.put("id", String.valueOf(templateId));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknB, null, pathParams, true);

        response.then().statusCode(404);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getDefaultTemplateId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceTemplateWithAdminToken_Test(int templateId) {
        Map<String, String> pathParams = new LinkedHashMap<>();
        pathParams.put("id", String.valueOf(templateId));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, pathParams, true);

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice template fetched successfully"));
        response.then().assertThat().body("data.id", Matchers.equalTo(templateId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getDefaultTemplateId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceTemplateWithTeamMemberToken_Test(int templateId) {
        Map<String, String> pathParams = new LinkedHashMap<>();
        pathParams.put("id", String.valueOf(templateId));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, pathParams, true);

        response.then().statusCode(401);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getDefaultTemplateId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceTemplateWithRestrictedToken_Test(int templateId) {
        Map<String, String> pathParams = new LinkedHashMap<>();
        pathParams.put("id", String.valueOf(templateId));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, pathParams, true);

        response.then().statusCode(401);
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomTemplateId", groups = {"invoice_service", "nightly-build"})
    public void getCustomInvoiceTemplateWithValidToken_Test(int templateId) {
        Map<String, String> pathParams = new LinkedHashMap<>();
        pathParams.put("id", String.valueOf(templateId));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, null, pathParams, true);

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice template fetched successfully"));
        response.then().assertThat().body("data.id", Matchers.equalTo(templateId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceTemplate.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomTemplateId", groups = {"invoice_service", "nightly-build"})
    public void getCustomInvoiceTemplateWithRestrictedToken_Test(int templateId) {
        Map<String, String> pathParams = new LinkedHashMap<>();
        pathParams.put("id", String.valueOf(templateId));
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, pathParams, true);

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice template fetched successfully"));
        response.then().assertThat().body("data.id", Matchers.equalTo(templateId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceTemplate.json"));
    }

    @DataProvider
    public Object[][] getCustomTemplateId() {
        Response usersResponse = function.getUsers(baseURL, apiKeyA);
        usersResponse.then().statusCode(200);
        JsonPath usersJsonPath = usersResponse.jsonPath();
        ArrayList<Integer> userId = new ArrayList<>();
        userId.add(usersJsonPath.get("[0].id"));
        userId.add(usersJsonPath.get("[1].id"));
        userId.add(usersJsonPath.get("[2].id"));
        userId.add(usersJsonPath.get("[3].id"));

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

        InvoiceTemplate invoiceTemplate = function.createInvoiceTemplatePayload(invoiceFaker.getInvoiceTemplateName(), userId, new ArrayList<>(), "7 Days", sfdtContent, templateItems);
        Response createResponse = RestClient.doPost("JSON", invoiceServiceURL, "invoices/templates", albatrossTknA, null, true, invoiceTemplate);
        createResponse.then().statusCode(201);
        int templateId = createResponse.jsonPath().get("data.id");
        
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

