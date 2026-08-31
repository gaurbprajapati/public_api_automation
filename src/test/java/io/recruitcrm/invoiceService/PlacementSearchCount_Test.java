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
import io.rcrm.api.javafaker.JavaFakerPlacement;

import java.util.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class PlacementSearchCount_Test extends TestBase {

    public PlacementSearchCount_Test() {
        super();
    }

    String apiKeyA;
    String apiKeyB;
    String albatrossTknA;
    String albatrossTknB;
    String basePath = "placements/search/count";
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    JavaFakerPlacement placementFaker;
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        apiKeyB = getAccountApiKey("AccountB");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        placementFaker = new JavaFakerPlacement();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void placementSearchCountWithValidToken_Test(int placementId) {
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null,
true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Counts Fetched Successfully"));
        response.then().assertThat().body("data", Matchers.equalTo(1));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//placementSearchCount.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void placementSearchCountWithInvalidToken_Test() {
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null,
            true, getDefaultRequestBody());

        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void placementSearchCountWithCrossAccountToken_Test(int placementId) {
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknB, null, null,
            true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("data", Matchers.equalTo(0));
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Counts Fetched Successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void placementSearchCountWithAdminToken_Test() {
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, null,
            true, getDefaultRequestBody()
        );

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Counts Fetched Successfully"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void placementSearchCountWithTeamMemberToken_Test(int placementId) {
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, null,true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Counts Fetched Successfully"));
        response.then().assertThat().body("data", Matchers.equalTo(1));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void placementSearchCountWithRestrictedToken_Test(int placementId) {
        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, null,
            true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Counts Fetched Successfully"));
        response.then().assertThat().body("data", Matchers.equalTo(0));
    }

    @DataProvider
    public Object[][] getPlacementId() {
        int placementId = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL).jsonPath().get("data.id");
        return new Object[][] {
            { placementId }
        };
    }


    private JSONObject getDefaultRequestBody() {
        ArrayList<Map<String, Object>> filters = new ArrayList<>();
        Map<String, Object> filter = new HashMap<>();
        filter.put("groupType", "placements");
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
