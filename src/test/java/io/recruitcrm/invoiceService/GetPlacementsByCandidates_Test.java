package io.recruitcrm.invoiceService;

import java.util.Collections;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetPlacementsByCandidates_Test extends TestBase {

    public GetPlacementsByCandidates_Test() {
        super();
    }

    String apiKeyA;
    String apiKeyB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    String albatrossTknA;
    String albatrossTknB;
    JavaFakerPlacement placementFaker;
    String basePath = "placements/get-placements-by-candidates";

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
    @Test(dataProvider = "getCandidateId", groups = {"invoice_service", "nightly-build"})
    public void getPlacementsByValidCandidate_Test(int candidateId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("candidateIds", Collections.singletonList(candidateId));

        Response response = RestClient.doPost1( "JSON", invoiceServiceURL, basePath , albatrossTknA,  null, null,true, requestBody);
        
        response.then().assertThat().body("data.placementsByCandidates.size()", Matchers.is(1));
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements by candidates fetched successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath( "privateApi//invoiceService//getPlacementsByCandidates.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getPlacementsByInvalidId_Test() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("candidateIds", Collections.singletonList(placementFaker.getRandomID()));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody );
        response.then().statusCode(200);
        response.then().assertThat().body("data.placementsByCandidates.size()", Matchers.equalTo(0));
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements by candidates fetched successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getPlacementsByInvalidRequestType_Test() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("candidateIds", Collections.singletonList(placementFaker.getRandomIDWithMoreThan10Digits()));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true, requestBody );
        response.then().statusCode(400);
        response.then().assertThat().body("error", Matchers.is("Bad Request"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void getPlacementsByInvalidToken_Test() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("candidateIds", Collections.singletonList(placementFaker.getRandomID()));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true, requestBody );

        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCandidateId", groups = {"invoice_service", "nightly-build"})
    public void getPlacementsByCrossAccountToken_Test(int candidateId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("candidateIds", Collections.singletonList(candidateId));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, albatrossTknB, null, null, true, requestBody );
        response.then().statusCode(200);
        response.then().assertThat().body("data.placementsByCandidates.size()", Matchers.equalTo(0));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCandidateId", groups = {"invoice_service", "nightly-build"})
    public void getPlacementsByAdminToken_Test(int candidateId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("candidateIds", Collections.singletonList(candidateId));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, null,true, requestBody );
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements by candidates fetched successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCandidateId", groups = {"invoice_service", "nightly-build"})
    public void getPlacementsByTeamMemberToken_Test(int candidateId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("candidateIds", Collections.singletonList(candidateId));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, null, true, requestBody );
        
        response.then().statusCode(200);
        response.then().assertThat().body("data.placementsByCandidates.size()", Matchers.equalTo(1));
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements by candidates fetched successfully"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCandidateId", groups = {"invoice_service", "nightly-build"})
    public void getPlacementsByRestrictedTeamMemberToken_Test(int candidateId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("candidateIds", Collections.singletonList(candidateId));

        Response response = RestClient.doPost1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, null, true, requestBody );
        response.then().statusCode(200);
        response.then().assertThat().body("data.placementsByCandidates.size()", Matchers.equalTo(0));
        response.then().assertThat().body("meta.message", Matchers.containsString("Placements by candidates fetched successfully"));
    }

    @DataProvider
    public Object[][] getCandidateId() {
        Response createPlacementResponse = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL);
        int candidateId = createPlacementResponse.jsonPath().get("data.candidateId");

        return new Object[][] {
            { candidateId }
        };
    }
}
