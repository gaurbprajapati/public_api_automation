package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.*;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import org.json.JSONObject;
import io.restassured.path.json.JsonPath;
import io.rcrm.api.pojo.invoiceService.*;
import org.json.JSONArray;
import io.rcrm.api.javafaker.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class InvoiceEntityCustomFields_Test extends TestBase {
    
    String apiKeyA;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    String albatrossTknA;
    String albatrossTknB;
    JavaFakerPlacement placementFaker;
    JavaFakerCustomField faker;
    JavaFakerInvoice invoiceFaker;
    String basePath = "invoices";


    @BeforeClass(alwaysRun = true)    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        placementFaker = new JavaFakerPlacement();
        faker = new JavaFakerCustomField();
        invoiceFaker = new JavaFakerInvoice();
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "createInvoice", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceEntityCustomFieldsWithValidToken_Test(int invoiceId, String candidateSlug, String candidateName) {
        
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath + "/" + invoiceId + "/entity-custom-fields", albatrossTknA, null, null, true);
        
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Entity Custom Fields Fetched Successfully"));
        response.then().assertThat().body("data.candidate." + candidateSlug + ".slug", Matchers.is(candidateSlug));
        response.then().assertThat().body("data.candidate." + candidateSlug + ".name", Matchers.is(candidateName));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/invoiceEntityCustomFields.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "createInvoice", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceEntityCustomFieldsWithInvalidToken_Test(int invoiceId, String candidateSlug, String candidateName) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath + "/" + invoiceId + "/entity-custom-fields", placementFaker.getInvalidToken(), null, null, true);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "createInvoice", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceEntityCustomFieldsWithoutToken_Test(int invoiceId, String candidateSlug, String candidateName) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath + "/" + invoiceId + "/entity-custom-fields", "", null, null, true);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
        response.then().assertThat().body("data", Matchers.is("Missing bearer token in header"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "createInvoice", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceEntityCustomFieldsWithCrossAccountToken_Test(int invoiceId, String candidateSlug, String candidateName) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath + "/" + invoiceId + "/entity-custom-fields", albatrossTknB, null, null, true);
        response.then().statusCode(Matchers.is(404));
        response.then().assertThat().body("errors[0].message", Matchers.is("Invoice not found"));
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "createInvoice", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceEntityCustomFieldsWithAdminToken_Test(int invoiceId, String candidateSlug, String candidateName) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath + "/" + invoiceId + "/entity-custom-fields", getRoleBasedToken("AccountA", "Admin"), null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Entity Custom Fields Fetched Successfully"));
        response.then().assertThat().body("data.candidate." + candidateSlug + ".slug", Matchers.is(candidateSlug));
        response.then().assertThat().body("data.candidate." + candidateSlug + ".name", Matchers.is(candidateName));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "createInvoice", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceEntityCustomFieldsWithTeamMemberToken_Test(int invoiceId, String candidateSlug, String candidateName) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath + "/" + invoiceId + "/entity-custom-fields", getRoleBasedToken("AccountA", "Team Member"), null, null, true);
        response.then().statusCode(Matchers.is(200));
        response.then().assertThat().body("meta.message", Matchers.is("Entity Custom Fields Fetched Successfully"));
        response.then().assertThat().body("data.candidate." + candidateSlug + ".slug", Matchers.is(candidateSlug));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "createInvoice", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceEntityCustomFieldsWithRestrictedTeamMemberToken_Test(int invoiceId, String candidateSlug, String candidateName) {   
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath + "/" + invoiceId + "/entity-custom-fields", getRoleBasedToken("AccountA", "Restricted"), null, null, true);
        response.then().statusCode(Matchers.is(404));
        response.then().assertThat().body("errors[0].message", Matchers.is("Invoice not found"));
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getInvoiceEntityCustomFieldsWithInvalidInvoiceId_Test() {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath + "/999999/entity-custom-fields", albatrossTknA, null, null, true);
        response.then().statusCode(Matchers.is(404));
        response.then().assertThat().body("errors[0].message", Matchers.is("Invoice not found"));
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getInvoiceEntityCustomFieldsWithNonNumericInvoiceId_Test() {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath + "/abc/entity-custom-fields", albatrossTknA, null, null, true);
        response.then().statusCode(Matchers.is(400));
    }

    @DataProvider(parallel=true)
    public Object[][] createInvoice() {
        Response candidateCustomFieldResponse = function.createInvoiceCustomFieldsResponse(invoiceServiceURL, albatrossTknA, "Candidate CF", "candidate", "");
        int candidateCustomFieldId = candidateCustomFieldResponse.jsonPath().getInt("data.fieldId");
        Map<String, Integer> templateData = createInvoiceTemplateData(candidateCustomFieldId);
        int templateId = templateData.get("templateId");

        Response candidateResponse = function.createNewCandidateWithMandatoryFields(baseURL, apiKeyA);
        candidateResponse.then().statusCode(200);
        String candidateSlug = candidateResponse.jsonPath().getString("slug");
        String candidateName = candidateResponse.jsonPath().getString("first_name") + " " + candidateResponse.jsonPath().getString("last_name");
        
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

        Invoice invoice = function.createInvoicePayload(companyId, invoicePrefix, s3Key, templateId);
        List<Map<String, Object>> invoiceItemsList = new ArrayList<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("fieldId", templateData.get("customFieldId"));
        field1.put("fieldValue", candidateSlug);
        invoiceItemsList.add(field1);
        
        Map<String, Object> field2 = new HashMap<>();
        field2.put("fieldId", 1);
        field2.put("fieldValue", String.valueOf(invoiceFaker.getTotalAmount()));
        invoiceItemsList.add(field2);

        List<List<Map<String, Object>>> outerArray = new ArrayList<>();
        outerArray.add(invoiceItemsList);
        JSONArray invoiceItemsArray = new JSONArray(outerArray);
        invoice.setInvoiceItems(invoiceItemsArray.toString());

        Response response3 = RestClient.doPost("JSON", invoiceServiceURL, "invoices", albatrossTknA, null, true, invoice);
        response3.then().statusCode(200);
        response3.then().body("meta.message", Matchers.containsString("Invoice Created Successfully"));
        int invoiceId = response3.jsonPath().getInt("data");
        return new Object[][] { { invoiceId , candidateSlug, candidateName } };
    }

    public Map<String, Integer> createInvoiceTemplateData(int candidateCustomFieldId) {
        Response usersResponse = function.getUsers(baseURL, apiKeyA);
        usersResponse.then().statusCode(200);
        JsonPath usersJsonPath = usersResponse.jsonPath();
        int userId = usersJsonPath.get("[0].id");

        Response response = function.generateInvoiceTableWithDefaultValues(syncFunctionURL, albatrossTknA);
        response.then().statusCode(200);
        String sfdtContent = response.jsonPath().get("sfdt");

        Object[][] itemConfigs = { 
                { candidateCustomFieldId, 7, "Candidate CF", 1 },
                { 1, 4, "Amount", 1 } };
        List<JSONObject> templateItems = new ArrayList<>();
        for (Object[] config : itemConfigs) {
            JSONObject item = new JSONObject();
            item.put("formula", "");
            item.put("field_id", config[0]);
            item.put("field_type", config[1]);
            item.put("field_label", config[2]);
            item.put("default_field_label", config[2]);
            item.put("sequence_number", config[3]);
            templateItems.add(item);
        }

        InvoiceTemplate invoiceTemplatePayload = function.createInvoiceTemplatePayload(invoiceFaker.getInvoiceTemplateName(), Arrays.asList(userId), new ArrayList<>(), "7 Days", sfdtContent, templateItems);
        Response response2 = RestClient.doPost("JSON", invoiceServiceURL, "invoices/templates", albatrossTknA, null, true, invoiceTemplatePayload);
        response2.then().statusCode(201);
        response2.then().body("meta.message", Matchers.containsString("Invoice template created successfully"));
        int templateId = response2.jsonPath().getInt("data.id");

        Map<String, Integer> result = new HashMap<>();
        result.put("customFieldId", candidateCustomFieldId);
        result.put("templateId", templateId);
        return result; 
    }
}
