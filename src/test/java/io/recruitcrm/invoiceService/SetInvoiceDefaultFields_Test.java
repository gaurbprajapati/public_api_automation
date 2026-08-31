package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
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
public class SetInvoiceDefaultFields_Test extends TestBase {

    public SetInvoiceDefaultFields_Test() {
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
    String basePath = "invoices/default-fields";

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
    public void setInvoiceDefaultFieldsWithValidToken_Test(int customFieldId) {
        String customLabel = faker.getCustomFieldName("text");
        String endpoint = basePath + "?custom_label=" + customLabel + "&custom_field_id=" + customFieldId;
        Response response = RestClient.doPost("JSON", invoiceServiceURL, endpoint, albatrossTknA, null, true, null);
        
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Default Field Label Updated successfully"));
        response.then().body("data.label", Matchers.is(customLabel));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//setInvoiceDefaultFields.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void setInvoiceDefaultFieldsWithInvalidToken_Test() {
        String endpoint = basePath + "?custom_label=100&custom_field_id=1";
        Response response = RestClient.doPost("JSON", invoiceServiceURL, endpoint, placementFaker.getInvalidToken(), null, true, null);
        
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldId", groups = {"invoice_service", "nightly-build"})
    public void setInvoiceDefaultFieldsWithCrossAccountToken_Test(int customFieldId) {
        String customLabel = faker.getCustomFieldName("text");
        String endpoint = basePath + "?custom_label=" + customLabel + "&custom_field_id=" + customFieldId;
        Response response = RestClient.doPost("JSON", invoiceServiceURL, endpoint, albatrossTknB, null, true, null);
        
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Default Field Label Updated successfully"));
        response.then().body("data.label", Matchers.is(customLabel));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldId", groups = {"invoice_service", "nightly-build"})
    public void setInvoiceDefaultFieldsWithAdminToken_Test(int customFieldId) {
        String customLabel = faker.getCustomFieldName("text");
        String endpoint = basePath + "?custom_label=" + customLabel + "&custom_field_id=" + customFieldId;
        Response response = RestClient.doPost("JSON", invoiceServiceURL, endpoint, getRoleBasedToken("AccountA", "Admin"), null, true, null);
        
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.containsString("Default Field Label Updated successfully"));
        response.then().body("data.label", Matchers.is(customLabel));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//setInvoiceDefaultFields.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldId", groups = {"invoice_service", "nightly-build"})
    public void setInvoiceDefaultFieldsWithTeamMemberToken_Test(int customFieldId) {
        String customLabel = faker.getCustomFieldName("text");
        String endpoint = basePath + "?custom_label=" + customLabel + "&custom_field_id=" + customFieldId;
        Response response = RestClient.doPost("JSON", invoiceServiceURL, endpoint, getRoleBasedToken("AccountA", "Team Member"), null, true, null);
        
        response.then().statusCode(403);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Access Denied"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCustomFieldId", groups = {"invoice_service", "nightly-build"})
    public void setInvoiceDefaultFieldsWithRestrictedToken_Test(int customFieldId) {
        String customLabel = faker.getCustomFieldName("text");
        String endpoint = basePath + "?custom_label=" + customLabel + "&custom_field_id=" + customFieldId;
        Response response = RestClient.doPost("JSON", invoiceServiceURL, endpoint, getRoleBasedToken("AccountA", "Restricted"), null, true, null);
        
        response.then().statusCode(403);
        response.then().body("meta.message", Matchers.nullValue());
        response.then().body("data", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Access Denied"));
    }

    @DataProvider
    public Object[][] getCustomFieldId() {

        Response getResponse = RestClient.doGet("JSON", invoiceServiceURL, "invoices/custom-fields", albatrossTknA, null, null, true);
        getResponse.then().statusCode(200);
        int customFieldId = getResponse.jsonPath().getInt("data[0].fieldId");
        
        return new Object[][] {{customFieldId}};
    }
}
