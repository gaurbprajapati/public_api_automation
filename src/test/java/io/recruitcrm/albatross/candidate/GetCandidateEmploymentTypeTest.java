package io.recruitcrm.albatross.candidate;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.rcrm.api.restclient.RestClient;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class GetCandidateEmploymentTypeTest extends TestBase {

    public GetCandidateEmploymentTypeTest() {
        super();
        // TODO Auto-generated constructor stub
    }

    String albatrossAuthToken;
    String apiAuthToken;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
    }

    @Owner("Sampurn Chouksey")
    @Test(groups = "nightly-build")
    public void getEmploymentTypeTest() {
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + albatrossAuthToken);
        Response response = RestClient.doPost("JSON", albatrossURL, "employment-type", authTokenMap, null, true, null);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is not 200");
        Assert.assertEquals(response.jsonPath().getString("message_type"), "is-success", "Message type is not success");
        Assert.assertEquals(response.jsonPath().getList("data").size(), 8, "Size of data is not 8");
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("publicApi//candidate//getCandidateEmploymentType.json"));

    }

    @Owner("Sampurn Chouksey")
    @Test(groups = "nightly-build")
    public void getEmploymentTypeInvalidTokenTest_401() {
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + "invalid_token");
        Response response = RestClient.doPost("JSON", albatrossURL, "employment-type", authTokenMap, null, true, null);

        Assert.assertEquals(response.getStatusCode(), 401, "Status code is not 401");
        Assert.assertTrue(response.jsonPath().getString("error").contains("Unauthorized"), "Error message is not Unauthorized");
    }

    @Owner("Sampurn Chouksey")
    @Test(groups = "nightly-build")
    public void getEmploymentTypeInvalidUrlTest_404() {
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + "invalid_token");
        Response response = RestClient.doPost("JSON", albatrossURL, "/invalid-url", authTokenMap, null, true, null);

        Assert.assertEquals(response.getStatusCode(), 404, "Status code is not 404");
    }

}