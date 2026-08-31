package io.recruitcrm.albatross.candidate;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.candidateService.RemoveFromHotlistRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class RemoveCandidateFromHotListTest extends TestBase {
    public RemoveCandidateFromHotListTest() {
        // TODO Auto-generated constructor stub
        super();
    }

    AllCrudFunctions function;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        function = new AllCrudFunctions();
    }

    @DataProvider(name = "candidateHotlistData")
    public Object[][] createCandidateData() {
        JsonPath candidateJsonPath = function.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candidateId = candidateJsonPath.getInt("data.candidate.id");
        JsonPath hotlistDetails = function.createHotlistsForCandidates(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        String hotlistName = hotlistDetails.getString("name");
        int shared = hotlistDetails.getInt("shared");
        int hotlistid = hotlistDetails.getInt("id");
        Response response = function.addCandidateToHotList(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), candidateId, shared, hotlistName);

        return new Object[][]{
                {candidateId, hotlistid}
        };
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "candidateHotlistData", groups = "nightly-build")
    public void removeCandidateFromHotlistTest_200(int candidateId, int hotlistId) {
        String basePath = "hotlists/" + hotlistId + "/remove";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + ThreadManager.getOwnerAlbatrossToken());
        RemoveFromHotlistRequest requestBody = new RemoveFromHotlistRequest();
        requestBody.setEntityname("candidates");
        requestBody.setSelectedrows(new int[]{candidateId});
        requestBody.setHotlistid(hotlistId);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, headers, null, true, requestBody);

        assertThat("Expected 200 Not Found, but received a different status code", response.getStatusCode(), is(200));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Failed to verify success message", jsonPath.getString("status"), is("success"));
        assertThat("Failed to remove From Hotlist Successful", jsonPath.getString("message"), containsString("Remove From Hotlist Successful"));
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "candidateHotlistData", groups = "nightly-build")
    public void removeCandidateWithInvalidAuthTokenTest_401(int candidateId, int hotlistId) {
        String basePath = "hotlists/" + hotlistId + "/remove";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + ThreadManager.getOwnerAlbatrossToken() + "123");

        RemoveFromHotlistRequest requestBody = new RemoveFromHotlistRequest();
        requestBody.setEntityname("candidates");
        requestBody.setSelectedrows(new int[]{candidateId});
        requestBody.setHotlistid(hotlistId);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, headers, null, true, requestBody);
        assertThat("Expected 401 Not Found, but received a different status code", response.getStatusCode(), is(401));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Failed to verify error", jsonPath.getString("error"), is("Unauthorized"));
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "candidateHotlistData", groups = "nightly-build")
    public void removeCandidateWithInvalidUrlTest_404(int candidateId, int hotlistId) {
        String basePath = "hotlists/" + hotlistId + "/remove" + "123";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + ThreadManager.getOwnerAlbatrossToken());

        RemoveFromHotlistRequest requestBody = new RemoveFromHotlistRequest();
        requestBody.setEntityname("candidates");
        requestBody.setSelectedrows(new int[]{candidateId});
        requestBody.setHotlistid(hotlistId);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, headers, null, true, requestBody);

        assertThat("Expected 404 Not Found, but received a different status code", response.getStatusCode(), is(404));
        String errorMessage = response.jsonPath().getString("message");
        Assert.assertNotNull(errorMessage, "Error message should not be null for 404 response");
        Assert.assertTrue(errorMessage.contains("HTTP Error"), "Error message does not indicate a 404 HTTP Error");
    }
    //there was an bug while verifying the 422 status code reported.

}


