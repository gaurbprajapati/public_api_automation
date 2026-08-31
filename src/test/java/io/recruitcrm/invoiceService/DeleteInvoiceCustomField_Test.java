package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.lang.reflect.Method;
import java.util.*;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")    
public class DeleteInvoiceCustomField_Test extends TestBase {

    public DeleteInvoiceCustomField_Test() {
        super();
    }
    
    String apiKeyA;
    String apiKeyB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    String albatrossTknA;
    String albatrossTknB;
    JavaFakerPlacement placementFaker;
    JavaFakerCustomField faker;
    String basePath = "invoices/custom-fields";

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        apiKeyB = getAccountApiKey("AccountB");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        placementFaker = new JavaFakerPlacement();
        faker = new JavaFakerCustomField();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldIdAndFieldName", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceTextTypeCustomField_Test(int customFieldId, String fieldName) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("isDefault", 0);
        requestBody.put("label", fieldName);
        requestBody.put("type", faker.getCustomFieldId("text"));

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("fieldId", String.valueOf(customFieldId));
        
        Response response = RestClient.doDelete("JSON", invoiceServiceURL, basePath, albatrossTknA, pathParamters, null, true, requestBody);
        
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.containsString("Custom Field Deleted successfully"));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//deleteInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldIdAndFieldName", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceLongTextTypeCustomField_Test(int customFieldId, String fieldName) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("isDefault", 0);
        requestBody.put("label", fieldName);
        requestBody.put("type", faker.getCustomFieldId("longtext"));

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("fieldId", String.valueOf(customFieldId));
        
        Response response = RestClient.doDelete("JSON", invoiceServiceURL, basePath, albatrossTknA, pathParamters, null, true, requestBody);
        
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.containsString("Custom Field Deleted successfully"));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//deleteInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldIdAndFieldName", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceNumberTypeCustomField_Test(int customFieldId, String fieldName) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("isDefault", 0);
        requestBody.put("label", fieldName);
        requestBody.put("type", faker.getCustomFieldId("number"));

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("fieldId", String.valueOf(customFieldId));
        
        Response response = RestClient.doDelete("JSON", invoiceServiceURL, basePath, albatrossTknA, pathParamters, null, true, requestBody);
        
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.containsString("Custom Field Deleted successfully"));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//deleteInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldIdAndFieldName", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceDateTypeCustomField_Test(int customFieldId, String fieldName) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("isDefault", 0);
        requestBody.put("label", fieldName);
        requestBody.put("type", faker.getCustomFieldId("date"));

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("fieldId", String.valueOf(customFieldId));
        
        Response response = RestClient.doDelete("JSON", invoiceServiceURL, basePath, albatrossTknA, pathParamters, null, true, requestBody);
        
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.containsString("Custom Field Deleted successfully"));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//deleteInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldIdAndFieldName", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceDropdownTypeCustomField_Test(int customFieldId, String fieldName) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("isDefault", 0);
        requestBody.put("label", fieldName);
        requestBody.put("type", faker.getCustomFieldId("dropdown"));

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("fieldId", String.valueOf(customFieldId));
        
        Response response = RestClient.doDelete("JSON", invoiceServiceURL, basePath, albatrossTknA, pathParamters, null, true, requestBody);
        
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.containsString("Custom Field Deleted successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldIdAndFieldName", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceMultiselectTypeCustomField_Test(int customFieldId, String fieldName) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("isDefault", 0);
        requestBody.put("label", fieldName);
        requestBody.put("type", faker.getCustomFieldId("multiselect"));

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("fieldId", String.valueOf(customFieldId));
        
        Response response = RestClient.doDelete("JSON", invoiceServiceURL, basePath, albatrossTknA, pathParamters, null, true, requestBody);
        
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.containsString("Custom Field Deleted successfully"));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//deleteInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldIdAndFieldName", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceTextTypeCustomFieldWithInvalidToken_Test(int customFieldId, String fieldName) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("isDefault", 0);
        requestBody.put("label", fieldName);
        requestBody.put("type", faker.getCustomFieldId("text"));

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("fieldId", String.valueOf(customFieldId));

        Response response = RestClient.doDelete("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), pathParamters, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldIdAndFieldName", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceTextTypeCustomFieldByAdminToken_Test(int customFieldId, String fieldName) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("isDefault", 0);
        requestBody.put("label", fieldName);
        requestBody.put("type", faker.getCustomFieldId("text"));

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("fieldId", String.valueOf(customFieldId));

        
        Response response = RestClient.doDelete("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), pathParamters, null, true, requestBody);
        
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.containsString("Custom Field Deleted successfully"));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//deleteInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldIdAndFieldName", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceTextTypeCustomFieldByTeamMemberToken_Test(int customFieldId, String fieldName) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("isDefault", 0);
        requestBody.put("label", fieldName);
        requestBody.put("type", faker.getCustomFieldId("text"));

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("fieldId", String.valueOf(customFieldId));

        Response response = RestClient.doDelete("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), pathParamters, null, true, requestBody);
        
        response.then().statusCode(403);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldIdAndFieldName", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceTextTypeCustomFieldByRestrictedToken_Test(int customFieldId, String fieldName) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("isDefault", 0);
        requestBody.put("label", fieldName);
        requestBody.put("type", faker.getCustomFieldId("text"));
    
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("fieldId", String.valueOf(customFieldId));

        Response response = RestClient.doDelete("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), pathParamters, null, true, requestBody);
        
        response.then().statusCode(403);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.nullValue());
    }

    @DataProvider
    public Object[][] getCustomFieldIdAndFieldName(Method method) {
        JSONObject requestBody = new JSONObject();
        String fieldType = "text";
        for (String ft : Arrays.asList("Dropdown", "Multiselect", "LongText", "Number", "Date")) {
            if (method.getName().contains(ft)) {
                fieldType = ft.toLowerCase();
                break;
            }
        }

        if ("dropdown".equals(fieldType) || "multiselect".equals(fieldType)) {
            requestBody.put("defaultOptionsValue", getOptionsList());
        } else {
            requestBody.put("defaultOptionsValue", new ArrayList<>());
        }

        requestBody.put("type", faker.getCustomFieldId(fieldType));
        requestBody.put("isDefault", 0);
        requestBody.put("label", faker.getCustomFieldName("custom"));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        int id = response.jsonPath().getInt("data.fieldId");
        String fieldName = response.jsonPath().getString("data.fieldLabel");

        return new Object[][] {{id, fieldName}};
    }

    private ArrayList<JSONObject> getOptionsList() {
        ArrayList<JSONObject> optionsList = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            JSONObject option = new JSONObject();
            option.put("label", faker.getRandomOptionsValue());
            option.put("sequence_no", i);
            optionsList.add(option);
        }
        return optionsList;
    }
}