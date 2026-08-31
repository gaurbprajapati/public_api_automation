package io.recruitcrm.invoiceService;

import java.util.Arrays;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import io.rcrm.api.pojo.invoiceService.LinkInvoicesRequest;
import io.rcrm.api.pojo.invoiceService.UnlinkInvoiceRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class UnlinkInvoice_Test extends TestBase {

    String apiKeyA;
    String albatrossTknA;
    String albatrossTknB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    JavaFakerPlacement placementFaker;
    String basePath = "placements/unlink-invoice";

    @BeforeClass(alwaysRun = true)    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        placementFaker = new JavaFakerPlacement();
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getLinkedPlacementAndInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void unlinkInvoiceWithValidToken_Test(int placementId, int invoiceId) {
        UnlinkInvoiceRequest requestBody = new UnlinkInvoiceRequest();
        requestBody.setPlacementId(placementId);
        requestBody.setInvoiceId(invoiceId);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Invoice Unlinked Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/unlinkInvoice.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getLinkedPlacementAndInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void unlinkInvoiceWithInvalidToken_Test(int placementId, int invoiceId) {
        UnlinkInvoiceRequest requestBody = new UnlinkInvoiceRequest();
        requestBody.setPlacementId(placementId);
        requestBody.setInvoiceId(invoiceId);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getLinkedPlacementAndInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void unlinkInvoiceWithoutToken_Test(int placementId, int invoiceId) {
        UnlinkInvoiceRequest requestBody = new UnlinkInvoiceRequest();
        requestBody.setPlacementId(placementId);
        requestBody.setInvoiceId(invoiceId);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, "", null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
        response.then().assertThat().body("data", Matchers.is("Missing bearer token in header"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getLinkedPlacementAndInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void unlinkInvoiceWithCrossAccountToken_Test(int placementId, int invoiceId) {
        UnlinkInvoiceRequest requestBody = new UnlinkInvoiceRequest();
        requestBody.setPlacementId(placementId);
        requestBody.setInvoiceId(invoiceId);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknB, null, true, requestBody);
        response.then().statusCode(404);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("not found"));

    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getLinkedPlacementAndInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void unlinkInvoiceWithAdminToken_Test(int placementId, int invoiceId) {
        UnlinkInvoiceRequest requestBody = new UnlinkInvoiceRequest();
        requestBody.setPlacementId(placementId);
        requestBody.setInvoiceId(invoiceId);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Invoice Unlinked Successfully"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getLinkedPlacementAndInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void unlinkInvoiceWithTeamMemberToken_Test(int placementId, int invoiceId) {
        UnlinkInvoiceRequest requestBody = new UnlinkInvoiceRequest();
        requestBody.setPlacementId(placementId);
        requestBody.setInvoiceId(invoiceId);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Invoice Unlinked Successfully"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getLinkedPlacementAndInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void unlinkInvoiceWithRestrictedTeamMemberToken_Test(int placementId, int invoiceId) {
        UnlinkInvoiceRequest requestBody = new UnlinkInvoiceRequest();
        requestBody.setPlacementId(placementId);
        requestBody.setInvoiceId(invoiceId);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Invoice Unlinked Successfully"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getLinkedPlacementAndInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void unlinkInvoiceWithNullPlacementId_Test(int placementId, int invoiceId) {
        UnlinkInvoiceRequest requestBody = new UnlinkInvoiceRequest();
        requestBody.setPlacementId(null);
        requestBody.setInvoiceId(invoiceId);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, requestBody);
        response.then().statusCode(400);
        response.then().assertThat().body("errors[0].message", Matchers.is("placementId is required"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getLinkedPlacementAndInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void unlinkInvoiceWithNullInvoiceId_Test(int placementId, int invoiceId) {
        UnlinkInvoiceRequest requestBody = new UnlinkInvoiceRequest();
        requestBody.setPlacementId(placementId);
        requestBody.setInvoiceId(null);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, requestBody);
        response.then().statusCode(400);
        response.then().assertThat().body("errors[0].message", Matchers.is("invoiceId is required"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getLinkedPlacementAndInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void unlinkInvoiceWithInvalidPlacementId_Test(int placementId, int invoiceId) {
        UnlinkInvoiceRequest requestBody = new UnlinkInvoiceRequest();
        requestBody.setPlacementId(placementFaker.getRandomID());
        requestBody.setInvoiceId(invoiceId);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, requestBody);
        response.then().statusCode(404);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("not found"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getLinkedPlacementAndInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void unlinkInvoiceWithInvalidInvoiceId_Test(int placementId, int invoiceId) {
        UnlinkInvoiceRequest requestBody = new UnlinkInvoiceRequest();
        requestBody.setPlacementId(placementId);
        requestBody.setInvoiceId(placementFaker.getRandomID());

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, requestBody);
        response.then().statusCode(404);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("not found"));
    }

    @DataProvider(parallel = true)
    public Object[][] getLinkedPlacementAndInvoiceId() {
        int placementId = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL).jsonPath().get("data.id");
        
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
        String companySlug = companyResponse.jsonPath().getString("slug");
        int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
        Response invoiceResponse = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
        int invoiceId = invoiceResponse.jsonPath().get("data");
        
        LinkInvoicesRequest linkRequestBody = new LinkInvoicesRequest();
        linkRequestBody.setPlacementIds(Arrays.asList(placementId));
        linkRequestBody.setInvoiceIds(Arrays.asList(invoiceId));
        
        Response linkResponse = RestClient.doPost("JSON", invoiceServiceURL, "placements/link-invoices", albatrossTknA, null, true, linkRequestBody);
        linkResponse.then().statusCode(200);
        linkResponse.then().assertThat().body("meta.message", Matchers.is("Invoices Linked Successfully"));
        
        return new Object[][] {
            { placementId, invoiceId }
        };
    }
}
