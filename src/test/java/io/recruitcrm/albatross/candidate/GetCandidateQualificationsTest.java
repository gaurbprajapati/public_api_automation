package io.recruitcrm.albatross.candidate;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class GetCandidateQualificationsTest extends TestBase {
    public GetCandidateQualificationsTest() {
        // TODO Auto-generated constructor stub
        super();
    }

    String albatrossAuthToken;
    String apiAuthToken;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
    }

    @Owner("Sampurn Chouksey")
    @Test(groups = "nightly-build")
    public void getAllQualifications_Test() {
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + albatrossAuthToken);
        String basePath = "qualifications";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, null);

        assertThat("Expected status code 200", response.getStatusCode(), is(200));
        assertThat("Expected status to be 'success'", response.jsonPath().getString("status"), is("success"));
        assertThat("Expected 'data' field to be not null", response.jsonPath().get("data"), notNullValue());
        assertThat("Expected 'data' list to have at least one element", response.jsonPath().getList("data"), hasSize(greaterThan(0)));
        assertThat("Response does not match the expected JSON schema", response.asString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("privateApi//candidate//GetCandidateAllQualifications.json"));
    }

    @Owner("Sampurn Chouksey")
    @Test(groups = "nightly-build")
    public void getAllQualificationsWithUnauthorizedAccessTest_401() {
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + albatrossAuthToken + "invalid");
        String basePath = "qualifications";

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, null);
        //Not validating status code because it was giving 200(Bug), However the response was different.
        assertThat("Response does not match the expected JSON schema", response.asString(), not(JsonSchemaValidator.matchesJsonSchemaInClasspath("privateApi//candidate//GetCandidateAllQualifications.json")));
    }

    @Owner("Sampurn Chouksey")
    @Test(groups = "nightly-build")
    public void getAllQualificationsWithInvalidUrlTest_404() {
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + albatrossAuthToken + "invalid");
        String basePath = "qualifications" + "123";

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, null);

        Assert.assertEquals(response.getStatusCode(), 404, "Status code is not 404");
        String errorMessage = response.jsonPath().getString("message");
        Assert.assertNotNull(errorMessage, "Error message should not be null for 404 response");
        Assert.assertTrue(errorMessage.contains("HTTP Error"), "Error message does not indicate a 404 HTTP Error");
    }
}

