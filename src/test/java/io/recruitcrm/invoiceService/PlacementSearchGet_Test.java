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
public class PlacementSearchGet_Test extends TestBase {

    public PlacementSearchGet_Test() {
        super();
    }

    String apiKeyA;
    String apiKeyB;
    String albatrossTknA;
    String albatrossTknB;
    String basePath = "placements/search/get";
    Map<String, String> paramsMap;
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
        placementFaker = new JavaFakerPlacement();
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void placementSearchGetWithValidToken_Test(int placementId) {

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, paramsMap, true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("data[0].id", Matchers.equalTo(placementId));
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Fetched Successfully"));

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//placementSearchGet.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void placementSearchGetWithInvalidToken_Test() {

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), paramsMap, true, getDefaultRequestBody());

        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void placementSearchGetWithCrossAccountToken_Test(int placementId) {

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknB, paramsMap, true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("data[0].id", Matchers.nullValue());
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Fetched Successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void placementSearchGetWithAdminToken_Test(int placementId) {

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), paramsMap, true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("data[0].id", Matchers.equalTo(placementId));
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Fetched Successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void placementSearchGetWithTeamMemberToken_Test(int placementId) {

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), paramsMap, true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("data[0].id", Matchers.equalTo(placementId));
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Fetched Successfully"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPlacementId", groups = {"invoice_service", "nightly-build"})
    public void placementSearchGetWithRestrictedToken_Test(int placementId) {

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), paramsMap, true, getDefaultRequestBody());

        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements Fetched Successfully"));
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
