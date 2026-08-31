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
import io.rcrm.api.javafaker.JavaFakerPlacement;
import io.rcrm.api.pojo.GroupedByRequest;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class UpdateGroupedBy_Test extends TestBase {

    String apiKeyA;
    String albatrossTknA;
    String albatrossTknB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    JavaFakerPlacement placementFaker;
    String basePath = "custom-view/grouped-by";

    @BeforeClass(alwaysRun = true)    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        placementFaker = new JavaFakerPlacement();
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getEntityIdAndGroupedByValues", groups = {"invoice_service", "nightly-build"})
    public void updateGroupedByWithValidToken_Test(int entityId, String groupedByValue) {
        GroupedByRequest requestBody = new GroupedByRequest();
        requestBody.setEntityId(entityId);
        requestBody.setGroupedBy(groupedByValue);

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Grouped-by preference updated successfully."));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/groupedByResponse.json"));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateGroupedByWithInvalidToken_Test() {
        GroupedByRequest requestBody = new GroupedByRequest();
        requestBody.setEntityId(7);
        requestBody.setGroupedBy("");

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateGroupedByWithoutToken_Test() {
        GroupedByRequest requestBody = new GroupedByRequest();
        requestBody.setEntityId(7);
        requestBody.setGroupedBy("");

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, "", null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
        response.then().assertThat().body("data", Matchers.is("Missing bearer token in header"));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateGroupedByWithCrossAccountToken_Test() {
        GroupedByRequest requestBody = new GroupedByRequest();
        requestBody.setEntityId(7);
        requestBody.setGroupedBy("");
        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, albatrossTknB, null, null, true, requestBody);
        response.then().statusCode(Matchers.is(200));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateGroupedByWithAdminToken_Test() {
        GroupedByRequest requestBody = new GroupedByRequest();
        requestBody.setEntityId(7);
        requestBody.setGroupedBy("");

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Grouped-by preference updated successfully."));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateGroupedByWithTeamMemberToken_Test() {
        GroupedByRequest requestBody = new GroupedByRequest();
        requestBody.setEntityId(7);
        requestBody.setGroupedBy("");

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, null, true, requestBody);
        response.then().statusCode(Matchers.is(200));
        response.then().assertThat().body("meta.message", Matchers.is("Grouped-by preference updated successfully."));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateGroupedByWithRestrictedTeamMemberToken_Test() {
        GroupedByRequest requestBody = new GroupedByRequest();
        requestBody.setEntityId(7);
        requestBody.setGroupedBy("");

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, null, true, requestBody);
        response.then().statusCode(Matchers.is(200));
        response.then().assertThat().body("meta.message", Matchers.is("Grouped-by preference updated successfully."));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateGroupedByWithNullEntityId_Test() {
        GroupedByRequest requestBody = new GroupedByRequest();
        requestBody.setEntityId(null);
        requestBody.setGroupedBy("");

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(400);
        response.then().assertThat().body("errors[0].message", Matchers.is("Field entityId cannot be null."));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateGroupedByWithInvalidEntityId_Test() {
        GroupedByRequest requestBody = new GroupedByRequest();
        requestBody.setEntityId(999999);
        requestBody.setGroupedBy("");

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(200);
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateGroupedByWithNullGroupedBy_Test() {
        GroupedByRequest requestBody = new GroupedByRequest();
        requestBody.setEntityId(7);
        requestBody.setGroupedBy(null);

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Grouped-by preference updated successfully."));
    }

    @DataProvider
    public Object[][] getEntityIdAndGroupedByValues() {
        return new Object[][]{
                {7, ""},
                {7, "companyId"},
                {7, "contactId"},
                {7, "jobId"},
                {7, "dealId"},
                {15, ""},
                {15, "companyId"},
                {15, "jobId"},
                {15, "createdOnMonth"},
                {15, "createdOnQuarter"},
                {15, "createdOnYear"}
        };
    }

}
