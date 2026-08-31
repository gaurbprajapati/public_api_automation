package io.recruitcrm.invoiceService;

import java.util.Arrays;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import io.rcrm.api.pojo.ExportFilesRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class ExportFiles_Test extends TestBase {

    String albatrossTknA;
    String albatrossTknB;
    JavaFakerPlacement placementFaker;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    String apiKeyA;
    String basePath = "export-files";

    @BeforeClass(alwaysRun = true)    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        placementFaker = new JavaFakerPlacement();
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdAndEntityName", groups = "nightly-build")
    public void exportFilesWithValidToken_Test(int invoiceId, String entityName) {
        ExportFilesRequest requestBody = new ExportFilesRequest();
        requestBody.setEntityIds(Arrays.asList(invoiceId));
        requestBody.setColumnKeys(Arrays.asList("invoicePdf"));
        requestBody.setEntityName(entityName);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(Matchers.is(200));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdAndEntityName", groups = "nightly-build")
    public void exportFilesWithInvalidToken_Test(int invoiceId, String entityName) {
        ExportFilesRequest requestBody = new ExportFilesRequest();
        requestBody.setEntityIds(Arrays.asList(invoiceId));
        requestBody.setColumnKeys(Arrays.asList("invoicePdf"));
        requestBody.setEntityName(entityName);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true, requestBody);
        response.then().statusCode(Matchers.is(401));
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdAndEntityName", groups = "nightly-build")
    public void exportFilesWithoutToken_Test(int invoiceId, String entityName) {
        ExportFilesRequest requestBody = new ExportFilesRequest();
        requestBody.setEntityIds(Arrays.asList(invoiceId));
        requestBody.setColumnKeys(Arrays.asList("invoicePdf"));
        requestBody.setEntityName(entityName);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, "", null, null, true, requestBody);
        response.then().statusCode(Matchers.is(401));
        response.then().body("data", Matchers.containsString("Missing bearer token in header"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdAndEntityName", groups = "nightly-build")
    public void exportFilesWithCrossAccountToken_Test(int invoiceId, String entityName) {
        ExportFilesRequest requestBody = new ExportFilesRequest();
        requestBody.setEntityIds(Arrays.asList(invoiceId));
        requestBody.setColumnKeys(Arrays.asList("invoicePdf"));
        requestBody.setEntityName(entityName);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknB, null, null, true, requestBody);
        response.then().statusCode(Matchers.is(200));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdAndEntityName", groups = "nightly-build")
    public void exportFilesWithAdminToken_Test(int invoiceId, String entityName) {
        ExportFilesRequest requestBody = new ExportFilesRequest();
        requestBody.setEntityIds(Arrays.asList(invoiceId));
        requestBody.setColumnKeys(Arrays.asList("invoicePdf"));
        requestBody.setEntityName(entityName);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, null, true, requestBody);
        response.then().statusCode(Matchers.is(200));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdAndEntityName", groups = "nightly-build")
    public void exportFilesWithTeamMemberToken_Test(int invoiceId, String entityName) {
        ExportFilesRequest requestBody = new ExportFilesRequest();
        requestBody.setEntityIds(Arrays.asList(invoiceId));
        requestBody.setColumnKeys(Arrays.asList("invoicePdf"));
        requestBody.setEntityName(entityName);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, null, true, requestBody);
        response.then().statusCode(Matchers.is(200));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdAndEntityName", groups = "nightly-build")
    public void exportFilesWithRestrictedTeamMemberToken_Test(int invoiceId, String entityName) {
        ExportFilesRequest requestBody = new ExportFilesRequest();
        requestBody.setEntityIds(Arrays.asList(invoiceId));
        requestBody.setColumnKeys(Arrays.asList("invoicePdf"));
        requestBody.setEntityName(entityName);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, null, true, requestBody);
        response.then().statusCode(Matchers.is(200));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdAndEntityName", groups = "nightly-build")
    public void exportFilesWithEmptyEntityIds_Test(int invoiceId, String entityName) {
        ExportFilesRequest requestBody = new ExportFilesRequest();
        requestBody.setEntityIds(Arrays.asList());
        requestBody.setColumnKeys(Arrays.asList("invoicePdf"));
        requestBody.setEntityName(entityName);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        
        response.then().statusCode(Matchers.is(400));
        response.then().body("errors[0].message", Matchers.containsString("Entity IDs list cannot be empty"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdAndEntityName", groups = "nightly-build")
    public void exportFilesWithEmptyColumnKeys_Test(int invoiceId, String entityName) {
        ExportFilesRequest requestBody = new ExportFilesRequest();
        requestBody.setEntityIds(Arrays.asList(invoiceId));
        requestBody.setColumnKeys(Arrays.asList());
        requestBody.setEntityName(entityName);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(Matchers.is(400));
        response.then().body("errors[0].message", Matchers.containsString("Column keys list cannot be empty"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdAndEntityName", groups = "nightly-build")
    public void exportFilesWithEmptyEntityName_Test(int invoiceId, String entityName) {
        ExportFilesRequest requestBody = new ExportFilesRequest();
        requestBody.setEntityIds(Arrays.asList(invoiceId));
        requestBody.setColumnKeys(Arrays.asList("invoicePdf"));
        requestBody.setEntityName("");

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(Matchers.is(200));
        response.then().body("meta.message", Matchers.containsString("Invalid entity name"));
        response.then().body("data", Matchers.nullValue());
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdAndEntityName", groups = "nightly-build")
    public void exportFilesWithNullEntityName_Test(int invoiceId, String entityName) {
        ExportFilesRequest requestBody = new ExportFilesRequest();
        requestBody.setEntityIds(Arrays.asList(invoiceId));
        requestBody.setColumnKeys(Arrays.asList("invoicePdf"));
        requestBody.setEntityName(null);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        
        response.then().statusCode(Matchers.is(400));
        response.then().body("errors[0].message", Matchers.containsString("Entity name is required"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdAndEntityName", groups = "nightly-build")
    public void exportFilesWithInvalidEntityName_Test(int invoiceId, String entityName) {
        ExportFilesRequest requestBody = new ExportFilesRequest();
        requestBody.setEntityIds(Arrays.asList(invoiceId));
        requestBody.setColumnKeys(Arrays.asList("invoicePdf"));
        requestBody.setEntityName("invalid_entity_name");

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        
        response.then().statusCode(Matchers.is(200));
        response.then().body("meta.message", Matchers.containsString("Invalid entity name"));
        response.then().body("data", Matchers.nullValue());
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdAndEntityName", groups = "nightly-build") // Issue : Its giving successfull response for invalid column keys
    public void exportFilesWithInvalidColumnKeys_Test(int invoiceId, String entityName) {
        ExportFilesRequest requestBody = new ExportFilesRequest();
        requestBody.setEntityIds(Arrays.asList(invoiceId));
        requestBody.setColumnKeys(Arrays.asList("invalidColumn"));
        requestBody.setEntityName(entityName);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(Matchers.is(200));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdAndEntityName", groups = "nightly-build") // Issue : Its giving successfull response for non existent entity id
    public void exportFilesWithNonExistentEntityId_Test(int invoiceId, String entityName) {
        ExportFilesRequest requestBody = new ExportFilesRequest();
        requestBody.setEntityIds(Arrays.asList(placementFaker.getRandomID()));
        requestBody.setColumnKeys(Arrays.asList("invoicePdf"));
        requestBody.setEntityName(entityName);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(Matchers.is(200));
    }


    @DataProvider
    public Object[][] getInvoiceId() {
        Response response = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
        String companySlug = response.jsonPath().getString("slug");
        int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
        Response response1 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
        int invoiceId = response1.jsonPath().get("data");
        return new Object[][] {
            { invoiceId }
        };
    }

    @DataProvider
    public Object[][] getEntityNameData() {
        return new Object[][] {
            { "placements" },
            { "invoices" }
        };
    }

    @DataProvider
    public Object[][] getInvoiceIdAndEntityName() {
        Response response = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
        String companySlug = response.jsonPath().getString("slug");
        int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
        Response response1 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
        int invoiceId = response1.jsonPath().get("data");
        
        String[] entityNames = {"placements" , "invoices"};
        Object[][] combinedData = new Object[entityNames.length][2];
        
        for (int i = 0; i < entityNames.length; i++) {
            combinedData[i][0] = invoiceId;
            combinedData[i][1] = entityNames[i];
        }
        return combinedData;
    }

}
