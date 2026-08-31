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
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CreateInvoiceCustomField_Test extends TestBase{
    
    public CreateInvoiceCustomField_Test() {
        super();
    }
    
    String apiKeyA;
    String apiKeyB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    String albatrossTknA;
    String albatrossTknB;
    JavaFakerPlacement placementFaker;
    String basePath = "invoices/custom-fields";
    JavaFakerCustomField faker;

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
        placementFaker = new JavaFakerPlacement();
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceTextTypeCustomField_Test() {
        String customFieldName = faker.getCustomFieldName("text");
        
        Response response = createInvoiceCustomField(customFieldName, faker.getCustomFieldId("text"), albatrossTknA, false);

        response.then().statusCode(200);

        response.then().body("meta.message", Matchers.is("Custom Field Created successfully"));
        response.then().body("data.fieldLabel", Matchers.is(customFieldName));
        response.then().body("data.fieldType", Matchers.is(faker.getCustomFieldId("text")));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceNumberTypeCustomField_Test() {
        String customFieldName = faker.getCustomFieldName("number");
        
        Response response = createInvoiceCustomField(customFieldName, faker.getCustomFieldId("number"), albatrossTknA, false);

        response.then().statusCode(200);

        response.then().body("meta.message", Matchers.is("Custom Field Created successfully"));
        response.then().body("data.fieldLabel", Matchers.is(customFieldName));
        response.then().body("data.fieldType", Matchers.is(faker.getCustomFieldId("number")));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceDateTypeCustomField_Test() {
        String customFieldName = faker.getCustomFieldName("date");
        
        Response response = createInvoiceCustomField(customFieldName, faker.getCustomFieldId("date"), albatrossTknA, false);

        response.then().statusCode(200);

        response.then().body("meta.message", Matchers.is("Custom Field Created successfully"));
        response.then().body("data.fieldLabel", Matchers.is(customFieldName));
        response.then().body("data.fieldType", Matchers.is(faker.getCustomFieldId("date")));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceLongTextTypeCustomField_Test() {
        String customFieldName = faker.getCustomFieldName("longtext");
        
        Response response = createInvoiceCustomField(customFieldName, faker.getCustomFieldId("longtext"), albatrossTknA, false);

        response.then().statusCode(200);

        response.then().body("meta.message", Matchers.is("Custom Field Created successfully"));
        response.then().body("data.fieldLabel", Matchers.is(customFieldName));
        response.then().body("data.fieldType", Matchers.is(faker.getCustomFieldId("longtext")));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceDropdownTypeCustomField_Test() {
        String customFieldName = faker.getCustomFieldName("dropdown");
        
        Response response = createInvoiceCustomField(customFieldName, faker.getCustomFieldId("dropdown"), albatrossTknA, true);

        response.then().statusCode(200);

        response.then().body("meta.message", Matchers.is("Custom Field Created successfully"));
        response.then().body("data.fieldLabel", Matchers.is(customFieldName));
        response.then().body("data.fieldType", Matchers.is(faker.getCustomFieldId("dropdown")));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceMultiselectTypeCustomField_Test() {
        String customFieldName = faker.getCustomFieldName("multiselect");
        
        Response response = createInvoiceCustomField(customFieldName, faker.getCustomFieldId("multiselect"), albatrossTknA, true);

        response.then().statusCode(200);

        response.then().body("meta.message", Matchers.is("Custom Field Created successfully"));
        response.then().body("data.fieldLabel", Matchers.is(customFieldName));
        response.then().body("data.fieldType", Matchers.is(faker.getCustomFieldId("multiselect")));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceTextTypeCustomFieldWithInvalidToken_Test() {
        String customFieldName = faker.getCustomFieldName("unauthorized");
        
        Response response = createInvoiceCustomField(customFieldName, faker.getCustomFieldId("text"), placementFaker.getInvalidToken(), true);
        response.then().statusCode(401);
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceTextTypeCustomFieldByAdminToken_Test() {
        String customFieldName = faker.getCustomFieldName("admin");
        
        Response response = createInvoiceCustomField(customFieldName, faker.getCustomFieldId("text"), getRoleBasedToken("AccountA", "Admin"), false);
        response.then().statusCode(200);

        response.then().body("meta.message", Matchers.is("Custom Field Created successfully"));
        response.then().body("data.fieldLabel", Matchers.is(customFieldName));
        response.then().body("data.fieldType", Matchers.is(faker.getCustomFieldId("text")));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//createInvoiceCustomField.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceTextTypeCustomFieldByTeamMemberToken_Test() {
        String customFieldName = faker.getCustomFieldName("teammember");
        
        Response response = createInvoiceCustomField(customFieldName, faker.getCustomFieldId("text"), getRoleBasedToken("AccountA", "Team Member"), false);
        response.then().statusCode(403);

        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceTextTypeCustomFieldByRestrictedToken_Test() {
        String customFieldName = faker.getCustomFieldName("restricted");
        
        Response response = createInvoiceCustomField(customFieldName, faker.getCustomFieldId("text"), getRoleBasedToken("AccountA", "Restricted"), false);
        response.then().statusCode(403);

        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.nullValue());
    }

    public Response createInvoiceCustomField(String customFieldName, int customFieldType, String albatrossTkn, boolean options) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("defaultOptionsValue", new ArrayList<>());
        requestBody.put("isDefault", 0);
        requestBody.put("label", customFieldName);
        requestBody.put("type", customFieldType);
        if (options) {
            ArrayList<JSONObject> optionsList = new ArrayList<>();
            for (int i = 1; i <= 2; i++) {
                JSONObject option = new JSONObject();
                option.put("label", faker.getRandomOptionsValue());
                option.put("sequence_no", i);
                optionsList.add(option);
            }
            requestBody.put("defaultOptionsValue", optionsList);
        }
        return RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTkn, null, null, true, requestBody);
    }

}
