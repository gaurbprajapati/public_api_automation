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
import io.rcrm.api.javafaker.*;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")    
public class GetInvoiceCustomFields_Test extends TestBase {

    public GetInvoiceCustomFields_Test() {
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
    public void getInvoiceTextTypeCustomField_Test(int customFieldId, String fieldName) {
        
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true);
        
        response.then().statusCode(200);
        
        response.then().body("meta.message", Matchers.containsString("Custom Fields fetched successfully"));
        response.then().body("data", Matchers.not(Matchers.empty()));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceCustomFields.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getInvoiceTextTypeCustomFieldWithInvalidToken_Test() {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true);
        
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldIdAndFieldName", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceTextTypeCustomFieldByAdminToken_Test(int customFieldId, String fieldName) {
        
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, null, true);
        
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Custom Fields fetched successfully"));
        response.then().body("data", Matchers.not(Matchers.empty()));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceCustomFields.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldIdAndFieldName", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceTextTypeCustomFieldByTeamMemberToken_Test(int customFieldId, String fieldName) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, null, true);
        
        response.then().statusCode(403);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldIdAndFieldName", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceTextTypeCustomFieldByRestrictedToken_Test(int customFieldId, String fieldName) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, null, true);
        
        response.then().statusCode(403);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.nullValue());
    }

    @DataProvider
    public Object[][] getCustomFieldIdAndFieldName() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("defaultOptionsValue", new ArrayList<>());
        requestBody.put("isDefault", 0);
        requestBody.put("label", faker.getCustomFieldName("text"));
        requestBody.put("type", 1);
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        int id = response.jsonPath().getInt("data.fieldId");
        String fieldName = response.jsonPath().getString("data.fieldLabel");

        return new Object[][] {{id, fieldName}};
    }
}