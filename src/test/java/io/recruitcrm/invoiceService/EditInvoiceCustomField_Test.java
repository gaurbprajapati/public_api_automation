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
public class EditInvoiceCustomField_Test extends TestBase {

    public EditInvoiceCustomField_Test() {
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
    @Test(dataProvider = "getCustomFieldId", groups = {"invoice_service", "nightly-build"})
    public void editInvoiceTextTypeCustomField_Test(int customFieldId) {
        String customFieldName = faker.getCustomFieldName("text");

        Response response = editInvoiceCustomField(customFieldId, customFieldName, faker.getCustomFieldId("text"), albatrossTknA);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.is("Custom Field Updated successfully"));
        response.then().body("data.fieldLabel", Matchers.is(customFieldName));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldId", groups = {"invoice_service", "nightly-build"})
    public void editInvoiceLongTextTypeCustomField_Test(int customFieldId) {
        String customFieldName = faker.getCustomFieldName("longtext");
        
        Response response = editInvoiceCustomField(customFieldId, customFieldName, faker.getCustomFieldId("longtext"), albatrossTknA);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.is("Custom Field Updated successfully"));
        response.then().body("data.fieldLabel", Matchers.is(customFieldName));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceCustomField.json"));
    }
    
    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldId", groups = {"invoice_service", "nightly-build"})
    public void editInvoiceNumberTypeCustomField_Test(int customFieldId) {
        String customFieldName = faker.getCustomFieldName("number");
        
        Response response = editInvoiceCustomField(customFieldId, customFieldName, faker.getCustomFieldId("number"), albatrossTknA);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.is("Custom Field Updated successfully"));
        response.then().body("data.fieldLabel", Matchers.is(customFieldName));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceCustomField.json"));
    }
    
    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldId", groups = {"invoice_service", "nightly-build"})
    public void editInvoiceDateTypeCustomField_Test(int customFieldId) {
        String customFieldName = faker.getCustomFieldName("date");
        
        Response response = editInvoiceCustomField(customFieldId, customFieldName, faker.getCustomFieldId("date"), albatrossTknA);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.is("Custom Field Updated successfully"));
        response.then().body("data.fieldLabel", Matchers.is(customFieldName));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldId", groups = {"invoice_service", "nightly-build"})
    public void editInvoiceDropdownTypeCustomField_Test(int customFieldId) {
        String customFieldName = faker.getCustomFieldName("dropdown");
        
        Response response = editInvoiceCustomField(customFieldId, customFieldName, faker.getCustomFieldId("dropdown"), albatrossTknA);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.is("Custom Field Updated successfully"));
        response.then().body("data.fieldLabel", Matchers.is(customFieldName));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldId", groups = {"invoice_service", "nightly-build"})
    public void editInvoiceMultiselectTypeCustomField_Test(int customFieldId) {
        String customFieldName = faker.getCustomFieldName("multiselect");
        
        Response response = editInvoiceCustomField(customFieldId, customFieldName, faker.getCustomFieldId("multiselect"), albatrossTknA);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.is("Custom Field Updated successfully"));
        response.then().body("data.fieldLabel", Matchers.is(customFieldName));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldId", groups = {"invoice_service", "nightly-build"})
    public void editInvoiceTextTypeCustomFieldWithInvalidToken_Test(int customFieldId) {
        String customFieldName = faker.getCustomFieldName("unauthorized");
        
        Response response = editInvoiceCustomField(customFieldId, customFieldName, faker.getCustomFieldId("text"), placementFaker.getInvalidToken());
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldId", groups = {"invoice_service", "nightly-build"})
    public void editInvoiceLongTextTypeCustomFieldByAdminToken_Test(int customFieldId) {
        String customFieldName = faker.getCustomFieldName("longtext");
        
        Response response = editInvoiceCustomField(customFieldId, customFieldName, faker.getCustomFieldId("longtext"), getRoleBasedToken("AccountA", "Admin"));
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.is("Custom Field Updated successfully"));
        response.then().body("data.fieldLabel", Matchers.is(customFieldName));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceCustomField.json"));
    }
    
    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldId", groups = {"invoice_service", "nightly-build"})
    public void editInvoiceLongTextTypeCustomFieldByTeamMemberToken_Test(int customFieldId) {
        String customFieldName = faker.getCustomFieldName("longtext");
        
        Response response = editInvoiceCustomField(customFieldId, customFieldName, faker.getCustomFieldId("longtext"), getRoleBasedToken("AccountA", "Team Member"));
        response.then().statusCode(403);

        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldId", groups = {"invoice_service", "nightly-build"})
    public void editInvoiceLongTextTypeCustomFieldByRestrictedToken_Test(int customFieldId) {
        String customFieldName = faker.getCustomFieldName("longtext");
        
        Response response = editInvoiceCustomField(customFieldId, customFieldName, faker.getCustomFieldId("longtext"), getRoleBasedToken("AccountA", "Restricted"));
        response.then().statusCode(403);

        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.nullValue());
    }

    public Response editInvoiceCustomField(int customFieldId, String customFieldName, int customFieldType, String albatrossTkn) {
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("fieldId", String.valueOf(customFieldId));
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("defaultOptionsValue", new ArrayList<>());
        requestBody.put("isDefault", 0);
        requestBody.put("label", customFieldName);
        requestBody.put("type", customFieldType);
        if (customFieldType == 5 || customFieldType == 6) {
            requestBody.put("defaultOptionsValue", getOptionsList());
        }
        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, albatrossTkn, pathParamters, null, true, requestBody);
        return response;
    }
    
    @DataProvider
    public Object[][] getCustomFieldId(Method method) {
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

        return new Object[][] {{id}};
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
