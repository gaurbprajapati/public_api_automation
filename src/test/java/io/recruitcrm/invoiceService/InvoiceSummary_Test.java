package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import java.util.*;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import io.rcrm.api.pojo.invoiceService.InvoiceSummaryRequest;
import io.rcrm.api.pojo.invoiceService.InvoiceSummaryRequest.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class InvoiceSummary_Test extends TestBase {

    String apiKeyA;
    String apiKeyB;
    String albatrossTknA;
    String albatrossTknB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    JavaFakerPlacement placementFaker;
    String basePath = "invoices/summary";

    @BeforeClass(alwaysRun = true)    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        apiKeyB = getAccountApiKey("AccountB");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        placementFaker = new JavaFakerPlacement();
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceIdWithGroupColumns", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSummaryWithValidToken_Test(int invoiceId, String groupColumn) {
        InvoiceSummaryRequest requestBody = createInvoiceSummaryRequest(groupColumn);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Invoice Summary Fetched Successfully"));
        response.then().assertThat().body("data.size()", Matchers.greaterThanOrEqualTo(1));
        response.then().assertThat().body("data[0].name", Matchers.notNullValue());
        response.then().assertThat().body("data[0].totalNumber", Matchers.is(1));
        response.then().assertThat().body("data[0].totalValue", Matchers.greaterThanOrEqualTo(0.0f));
        response.then().assertThat().body("data[0].currencySymbol", Matchers.is("₹"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/invoiceSummary.json"));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSummaryWithInvalidToken_Test() {
        InvoiceSummaryRequest requestBody = createInvoiceSummaryRequest("");
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSummaryWithoutToken_Test() {
        InvoiceSummaryRequest requestBody = createInvoiceSummaryRequest("");
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, "", null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
        response.then().assertThat().body("data", Matchers.is("Missing bearer token in header"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSummaryWithCrossAccountToken_Test(int invoiceId) {
        InvoiceSummaryRequest requestBody = createInvoiceSummaryRequest("");

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknB, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Invoice Summary Fetched Successfully"));
        response.then().assertThat().body("meta.status", Matchers.is(200));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/invoiceSummary.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSummaryWithAdminToken_Test(int invoiceId) {
        InvoiceSummaryRequest requestBody = createInvoiceSummaryRequest("");

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Invoice Summary Fetched Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/invoiceSummary.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSummaryWithTeamMemberToken_Test(int invoiceId) {
        InvoiceSummaryRequest requestBody = createInvoiceSummaryRequest("");

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Invoice Summary Fetched Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/invoiceSummary.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSummaryWithRestrictedTeamMemberToken_Test(int invoiceId) {
        InvoiceSummaryRequest requestBody = createInvoiceSummaryRequest("");

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Invoice Summary Fetched Successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/invoiceSummary.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getInvoiceId", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSummaryCategoriesValidation_Test(int invoiceId) {
        InvoiceSummaryRequest requestBody = createInvoiceSummaryRequest("");

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("data.name", Matchers.hasItems("Total", "Paid", "Due", "Overdue"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/invoiceSummary.json"));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSummaryWithEmptyFilters_Test() {
        InvoiceSummaryRequest request = new InvoiceSummaryRequest();
        request.setAdvancedSearchContext(null);
        request.setFilterSearchList(null);
        request.setBooleanSearchList(null);
        request.setSortPriorityList(new ArrayList<>());

        DefaultFilterListWrapper defaultFilterListWrapper = new DefaultFilterListWrapper();
        DefaultFilterList defaultFilterList = new DefaultFilterList();
        defaultFilterList.setFilters(new ArrayList<>());
        defaultFilterList.setSubGroupJoinOperator("AND");
        defaultFilterListWrapper.setDefaultFilterList(defaultFilterList);
        request.setDefaultFilterList(defaultFilterListWrapper);

        GroupByFields groupByFields = new GroupByFields();
        groupByFields.setGroupKey(new ArrayList<>());
        groupByFields.setGroupColumns(new ArrayList<>());
        request.setGroupByFields(groupByFields);

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, request);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Invoice Summary Fetched Successfully"));
    }

    @DataProvider
    public Object[][] getInvoiceIdWithGroupColumns() {
        Response response = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
        String companySlug = response.jsonPath().getString("slug");
        int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
        Response response1 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
        int invoiceId = response1.jsonPath().get("data");
        
        return new Object[][]{
                {invoiceId, "companyId"},
                {invoiceId, "contactId"},
                {invoiceId, "jobId"},
                {invoiceId, "dealId"},
                {invoiceId, ""}
        };
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

    private InvoiceSummaryRequest createInvoiceSummaryRequest(String groupColumn) {
        InvoiceSummaryRequest request = new InvoiceSummaryRequest();
        request.setAdvancedSearchContext(null);
        request.setFilterSearchList(null);
        request.setBooleanSearchList(null);
        request.setSortPriorityList(new ArrayList<>());

        DefaultFilterListWrapper defaultFilterListWrapper = new DefaultFilterListWrapper();
        DefaultFilterList defaultFilterList = new DefaultFilterList();

        Filter filter = new Filter();
        filter.setGroupType("invoices");
        filter.setDbField("archived");
        filter.setFilterValue("0");
        filter.setFilterType("is");
        filter.setFieldType("text");

        defaultFilterList.setFilters(Arrays.asList(filter));
        defaultFilterList.setSubGroupJoinOperator("AND");
        defaultFilterListWrapper.setDefaultFilterList(defaultFilterList);
        request.setDefaultFilterList(defaultFilterListWrapper);

        GroupByFields groupByFields = new GroupByFields();
        groupByFields.setGroupKey(new ArrayList<>());
        
        if (groupColumn != null && !groupColumn.isEmpty()) {
            groupByFields.setGroupColumns(Arrays.asList(groupColumn));
        } else {
            groupByFields.setGroupColumns(new ArrayList<>());
        }
        request.setGroupByFields(groupByFields);

        return request;
    }

}
