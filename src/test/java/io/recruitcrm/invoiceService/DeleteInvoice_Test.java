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
public class DeleteInvoice_Test extends TestBase {

    public DeleteInvoice_Test() {
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
    String basePath = "invoices/delete";

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
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceWithValidToken_Test(int invoiceId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("invoiceIds", Collections.singletonList(invoiceId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Deleted Successfully"));
        response.then().assertThat().body("data.invoiceIds[0]", Matchers.is(invoiceId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//deleteInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceWithInvalidId_Test() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("invoiceIds", Collections.singletonList(placementFaker.getRandomID()));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.nullValue());
        response.then().assertThat().body("errors[0].message", Matchers.containsString("Failed to Delete Invoice(s) : You are not authorized to delete selected records"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceWithInvalidToken_Test() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("invoiceIds", Collections.singletonList(placementFaker.getRandomID()));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceWithCrossAccountToken_Test(int invoiceId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("invoiceIds", Collections.singletonList(invoiceId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknB, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Deleted Successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceWithAdminToken_Test(int invoiceId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("invoiceIds", Collections.singletonList(invoiceId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Deleted Successfully"));
        response.then().assertThat().body("data.invoiceIds[0]", Matchers.is(invoiceId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//deleteInvoice.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceWithTeamMemberToken_Test(int invoiceId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("invoiceIds", Collections.singletonList(invoiceId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("You are not authorized to perform this action"));
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void deleteInvoiceWithRestrictedTeamMemberToken_Test(int invoiceId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("invoiceIds", Collections.singletonList(invoiceId));
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("You are not authorized to perform this action"));
        response.then().assertThat().body("data", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.nullValue());
    }

    @DataProvider
    public Object[][] getInvoiceId() {
        Response response = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
        String companySlug = response.jsonPath().getString("slug");
        int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
        Response response1 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
        System.out.println(response1.jsonPath().prettyPrint());
        int invoiceId = response1.jsonPath().get("data");
        return new Object[][] {
            { invoiceId }
        };
    }

}