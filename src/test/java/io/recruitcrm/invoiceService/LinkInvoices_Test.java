package io.recruitcrm.invoiceService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import io.rcrm.api.pojo.invoiceService.LinkInvoicesRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class LinkInvoices_Test extends TestBase {

    String apiKeyA;
    String albatrossTknA;
    String albatrossTknB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    JavaFakerPlacement placementFaker;
    String basePath = "placements/link-invoices";

    @BeforeClass(alwaysRun = true)    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        placementFaker = new JavaFakerPlacement();
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPlacementAndInvoiceIds", groups = {"invoice_service", "nightly-build"})
    public void linkInvoicesWithValidToken_Test(List<Integer> placementIds, List<Integer> invoiceIds) {
        LinkInvoicesRequest requestBody = new LinkInvoicesRequest();
        requestBody.setPlacementIds(placementIds);
        requestBody.setInvoiceIds(invoiceIds);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Invoices Linked Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/linkInvoices.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPlacementAndInvoiceIds", groups = {"invoice_service", "nightly-build"})
    public void linkInvoicesWithInvalidToken_Test(List<Integer> placementIds, List<Integer> invoiceIds) {
        LinkInvoicesRequest requestBody = new LinkInvoicesRequest();
        requestBody.setPlacementIds(placementIds);
        requestBody.setInvoiceIds(invoiceIds);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPlacementAndInvoiceIds", groups = {"invoice_service", "nightly-build"})
    public void linkInvoicesWithoutToken_Test(List<Integer> placementIds, List<Integer> invoiceIds) {
        LinkInvoicesRequest requestBody = new LinkInvoicesRequest();
        requestBody.setPlacementIds(placementIds);
        requestBody.setInvoiceIds(invoiceIds);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, "", null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
        response.then().assertThat().body("data", Matchers.is("Missing bearer token in header"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPlacementAndInvoiceIds", groups = {"invoice_service", "nightly-build"})
    public void linkInvoicesWithCrossAccountToken_Test(List<Integer> placementIds, List<Integer> invoiceIds) {
        LinkInvoicesRequest requestBody = new LinkInvoicesRequest();
        requestBody.setPlacementIds(placementIds);
        requestBody.setInvoiceIds(invoiceIds);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknB, null, true, requestBody);
        response.then().statusCode(404);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("These invoices do not exist in the database."));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPlacementAndInvoiceIds", groups = {"invoice_service", "nightly-build"})
    public void linkInvoicesWithAdminToken_Test(List<Integer> placementIds, List<Integer> invoiceIds) {
        LinkInvoicesRequest requestBody = new LinkInvoicesRequest();
        requestBody.setPlacementIds(placementIds);
        requestBody.setInvoiceIds(invoiceIds);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Invoices Linked Successfully"));
        response.then().assertThat().body("meta.status", Matchers.is(200));
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPlacementAndInvoiceIds", groups = {"invoice_service", "nightly-build"})
    public void linkInvoicesWithTeamMemberToken_Test(List<Integer> placementIds, List<Integer> invoiceIds) {
        LinkInvoicesRequest requestBody = new LinkInvoicesRequest();
        requestBody.setPlacementIds(placementIds);
        requestBody.setInvoiceIds(invoiceIds);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Invoices Linked Successfully"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPlacementAndInvoiceIds", groups = {"invoice_service", "nightly-build"})
    public void linkInvoicesWithRestrictedTeamMemberToken_Test(List<Integer> placementIds, List<Integer> invoiceIds) {
        LinkInvoicesRequest requestBody = new LinkInvoicesRequest();
        requestBody.setPlacementIds(placementIds);
        requestBody.setInvoiceIds(invoiceIds);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Invoices Linked Successfully"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPlacementAndInvoiceIds", groups = {"invoice_service", "nightly-build"}) // Issue : Its returning Unauthorized error instead of Bad Request error
    public void linkInvoicesWithEmptyPlacementIds_Test(List<Integer> placementIds, List<Integer> invoiceIds) {
        LinkInvoicesRequest requestBody = new LinkInvoicesRequest();
        requestBody.setPlacementIds(new ArrayList<>());
        requestBody.setInvoiceIds(invoiceIds);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("You are not authorized to perform this action"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPlacementAndInvoiceIds", groups = {"invoice_service", "nightly-build"}) // Issue : Its returning success response instead of Bad Request error
    public void linkInvoicesWithEmptyInvoiceIds_Test(List<Integer> placementIds, List<Integer> invoiceId) {
        LinkInvoicesRequest requestBody = new LinkInvoicesRequest();
        requestBody.setPlacementIds(placementIds);
        requestBody.setInvoiceIds(new ArrayList<>());

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, requestBody);
        response.then().statusCode(200);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPlacementAndInvoiceIds", groups = {"invoice_service", "nightly-build"})
    public void linkInvoicesWithInvalidPlacementId_Test(List<Integer> placementIds, List<Integer> invoiceIds) {
        LinkInvoicesRequest requestBody = new LinkInvoicesRequest();
        requestBody.setPlacementIds(Arrays.asList(placementFaker.getRandomID()));
        requestBody.setInvoiceIds(invoiceIds);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, requestBody);
        response.then().statusCode(404);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("These placements do not exist in the database."));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPlacementAndInvoiceIds", groups = {"invoice_service", "nightly-build"})
    public void linkInvoicesWithInvalidInvoiceId_Test(List<Integer> placementIds, List<Integer> invoiceIds) {
        LinkInvoicesRequest requestBody = new LinkInvoicesRequest();
        requestBody.setPlacementIds(placementIds);
        requestBody.setInvoiceIds(Arrays.asList(placementFaker.getRandomID()));

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, requestBody);
        response.then().statusCode(404);
        response.then().assertThat().body("errors[0].message", Matchers.containsString("These invoices do not exist in the database."));
    }

    @DataProvider(parallel = true)
    public Object[][] getPlacementAndInvoiceIds() {
        int placementId1 = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL).jsonPath().get("data.id");
        
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
        String companySlug = companyResponse.jsonPath().getString("slug");
        int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
        Response invoiceResponse = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
        int invoiceId = invoiceResponse.jsonPath().get("data");
        
        List<Integer> placementIds = Arrays.asList(placementId1);
        List<Integer> invoiceIds = Arrays.asList(invoiceId);
        
        return new Object[][] {
            { placementIds, invoiceIds }
        };
    }
}
