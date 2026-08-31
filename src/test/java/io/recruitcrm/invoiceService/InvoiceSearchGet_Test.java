package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

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

import java.util.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class InvoiceSearchGet_Test extends TestBase {

    public InvoiceSearchGet_Test() {
        super();
    }

    String apiKeyA;
    String apiKeyB;
    String albatrossTknA;
    String albatrossTknB;
    String basePath = "invoices/search/get";
    Map<String, String> paramsMap;
    JavaFakerInvoice invoiceFaker;
    JavaFakerPlacement placementFaker;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        apiKeyB = getAccountApiKey("AccountB");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        paramsMap = new LinkedHashMap<>();
        paramsMap.put("page", "1");
        paramsMap.put("size", "100");
        invoiceFaker = new JavaFakerInvoice();
        placementFaker = new JavaFakerPlacement();
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void invoiceSearchGetWithValidToken_Test(int invoiceId) {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, paramsMap, true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("data[0].id", Matchers.equalTo(invoiceId));
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Fetched Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//invoiceSearchGet.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void invoiceSearchGetWithInvalidToken_Test() {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), paramsMap, true, getDefaultRequestBody());

        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void invoiceSearchGetWithCrossAccountToken_Test(int invoiceId) {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknB, paramsMap, true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("data[0].id", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Fetched Successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void invoiceSearchGetWithAdminToken_Test(int invoiceId) {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), paramsMap, true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("data[0].id", Matchers.equalTo(invoiceId));
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Fetched Successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void invoiceSearchGetWithTeamMemberToken_Test(int invoiceId) {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), paramsMap, true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("data[0].id", Matchers.equalTo(invoiceId));
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Fetched Successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void invoiceSearchGetWithRestrictedToken_Test() {
        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), paramsMap, true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("data.size()", Matchers.equalTo(0));
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoices Fetched Successfully"));
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

    private JSONObject getDefaultRequestBody() {
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
}
