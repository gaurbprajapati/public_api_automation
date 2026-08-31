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
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class InvoiceSearchCount_Test extends TestBase {

    public InvoiceSearchCount_Test() {
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
    String basePath = "invoices/search/count";

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
    public void getInvoiceSearchCountWithValidToken_Test(int invoiceId) {
        JSONObject request = createSearchCountRequest();
        
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, request);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Count Fetched Successfully"));
        response.then().assertThat().body("data", Matchers.equalTo(1));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceSearchCount.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSearchCountWithInvalidToken_Test() {
        JSONObject request = createSearchCountRequest();
        
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, true, request);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSearchCountWithCrossAccountToken_Test(int invoiceId) {
        JSONObject request = createSearchCountRequest();
        
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknB, null, true, request);
        response.then().statusCode(200);
        response.then().assertThat().body("data", Matchers.equalTo(0));
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Count Fetched Successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSearchCountWithAdminToken_Test(int invoiceId) {
        JSONObject request = createSearchCountRequest();
        
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, true, request);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Count Fetched Successfully"));
        response.then().assertThat().body("data", Matchers.equalTo(1));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceSearchCount.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSearchCountWithTeamMemberToken_Test(int invoiceId) {
        JSONObject request = createSearchCountRequest();
        
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, true, request);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Count Fetched Successfully"));
        response.then().assertThat().body("data", Matchers.equalTo(1));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceSearchCount.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSearchCountWithRestrictedTeamMemberToken_Test(int invoiceId) {
        JSONObject request = createSearchCountRequest();
        
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, true, request);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Count Fetched Successfully"));
        response.then().assertThat().body("data", Matchers.equalTo(0));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//getInvoiceSearchCount.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSearchCountWithEmptyFilters_Test(int invoiceId) {
        JSONObject request = new JSONObject();
        request.put("defaultFilterList", new JSONObject());
        
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, request);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Count Fetched Successfully"));
        response.then().assertThat().body("data", Matchers.equalTo(1));
    }

    private JSONObject createSearchCountRequest() {
        ArrayList<Map<String, Object>> filters = new ArrayList<>();
        Map<String, Object> filter = new HashMap<>();
        filter.put("groupType", "invoices");
        filter.put("dbField", "archived");
        filter.put("filterValue", "0");
        filter.put("filterType", "is");
        filter.put("fieldType", "text");
        filters.add(filter);

        JSONObject defaultFilterList = new JSONObject();
        defaultFilterList.put("filters", filters);
        defaultFilterList.put("subGroupJoinOperator", "AND");

        JSONObject body = new JSONObject();
        body.put("defaultFilterList", defaultFilterList);

        return body;
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
}
