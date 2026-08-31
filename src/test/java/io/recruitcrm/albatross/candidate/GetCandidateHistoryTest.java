package io.recruitcrm.albatross.candidate;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.reaper.ThreadManager;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCandidateHistoryTest extends TestBase {

    AllCrudFunctions function = new AllCrudFunctions();
    commanFunction commanFunction = new commanFunction();
    private String albatrossToken;
    private String accountAPIKey;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossToken = ThreadManager.getOwnerAlbatrossToken();
        accountAPIKey = ThreadManager.getAccountApiKey();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "createData", groups = "nightly-build")
    public void getCandidateHistoryWithValid_200(int candidateId) {
        String basePath = "candidates/" + candidateId + "/history/get";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossToken, null, true, null);

        JsonPath jp = response.jsonPath();

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
        Assert.assertEquals(jp.get("message_type"), "is-success", "Expected message_type to be is-success, but got " + jp.get("message_type"));
        Assert.assertNotNull(jp.get("data"), "Expected data to be present");
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "createData", groups = "nightly-build")
    public void getCandidateHistoryVerify_401(int candidateId) {
        String basePath = "candidates/" + candidateId + "/history/get";
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossToken + "invalid", null, null, true, null);

        JsonPath jp = response.jsonPath();

        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401, but got " + response.getStatusCode());
        Assert.assertEquals(jp.get("error"), "Unauthorized", "Expected error to be Unauthorized, but got " + jp.get("error"));
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void getCandidateHistoryVerify_404() {
        String basePath = "candidates/999999999/history/get";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossToken, null, true, null);

        JsonPath jp = response.jsonPath();

        //Verifying 200 because endpoint is returning 200 with failed message instead of 404
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
        Assert.assertEquals(jp.get("message_type"), "is-danger", "Expected message_type to be is-danger, but got " + jp.get("message_type"));
    }

    @DataProvider
    private Object[][] createData() {
        Response response = function.createCandidate(albatrossURL, albatrossToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
        int candidateId = response.jsonPath().getInt("data.candidate.id");
        String candidateSlug = response.jsonPath().getString("data.candidate.slug");

        String jobSlug = commanFunction.getEntityResponse(baseURL, accountAPIKey, "job");

        Response response1 = commanFunction.assignCandidateByJobSlugAndCandidateSlug(baseURL, accountAPIKey, jobSlug, candidateSlug);
        Assert.assertEquals(response1.getStatusCode(), 200, "Expected status code 200, but got " + response1.getStatusCode());

        return new Object[][] { {candidateId} };
    }
}
