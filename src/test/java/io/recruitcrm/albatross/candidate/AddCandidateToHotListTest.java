package io.recruitcrm.albatross.candidate;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.candidateService.AddToHotlistRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.rcrm.api.testbase.TestBase.AccountType;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class AddCandidateToHotListTest extends TestBase {

    public AddCandidateToHotListTest() {
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

        return new Object[][]{
                {candidateId, hotlistName, shared}
        };
    }


    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "candidateHotlistData", groups = "nightly-build")
    public void addToHotlistWithValidFieldsTest_200(int candidateId, String hotlistName, int shared) {
        String basePath = "hotlists";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + ThreadManager.getOwnerAlbatrossToken());
        // Prepare request body
        AddToHotlistRequest requestBody = new AddToHotlistRequest();
        requestBody.setEntity_name("candidates");
        requestBody.setSelectedrows(new int[]{candidateId});
        requestBody.setShared(shared == 1);
        requestBody.setName(new String[]{hotlistName});
        requestBody.setUpdateUserObj(false);
        requestBody.setFrom_add_to_hotlist_modal(true);

        // Make the API call
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, headers, null, true, requestBody);
        // Assertions
        assertThat("Expected 200 Not Found, but received a different status code", response.getStatusCode(), is(200));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Failed to verify status Success", jsonPath.getString("status"), is("success"));
        assertThat("Failed to add hotlist Successfully", jsonPath.getString("message"), containsString("Add To Hotlist Successful"));
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "candidateHotlistData", groups = "nightly-build")
    public void addToHotlistWithUnauthorizedAccessTest_401(int candidateId, String hotlistName, int shared) {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + "invalid_token");
        AddToHotlistRequest requestBody = new AddToHotlistRequest();
        requestBody.setEntity_name("candidates");
        requestBody.setSelectedrows(new int[]{candidateId});
        requestBody.setShared(shared == 1);
        requestBody.setName(new String[]{hotlistName});
        requestBody.setUpdateUserObj(false);
        requestBody.setFrom_add_to_hotlist_modal(true);
        Response response = RestClient.doPost("JSON", albatrossURL, "hotlists", headers, null, true, requestBody);

        assertThat("Expected 401 Not Found, but received a different status code", response.getStatusCode(), is(401));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Failed to verify error message", jsonPath.getString("error"), containsString("Unauthorized"));
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "candidateHotlistData", groups = "nightly-build")
    public void addToHotlistWithInvalidUrlTest_404(int candidateId, String hotlistName, int shared) {
        String basePath = "hotlists" + "123";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + ThreadManager.getOwnerAlbatrossToken());
        AddToHotlistRequest requestBody = new AddToHotlistRequest();
        requestBody.setEntity_name("candidates");
        requestBody.setSelectedrows(new int[]{candidateId});
        requestBody.setShared(shared == 1);
        requestBody.setName(new String[]{hotlistName});
        requestBody.setUpdateUserObj(false);
        requestBody.setFrom_add_to_hotlist_modal(true);
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, headers, null, true, requestBody);

        assertThat("Expected 404 Not Found, but received a different status code", response.getStatusCode(), is(404));
        String errorMessage = response.jsonPath().getString("message");
        Assert.assertNotNull(errorMessage, "Error message should not be null for 404 response");
        Assert.assertTrue(errorMessage.contains("HTTP Error"), "Error message does not indicate a 404 HTTP Error");
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "candidateHotlistData", groups = "nightly-build")
    public void addToHotlistWithInvalidBodyTest_422(int candidateId, String hotlistName, int shared) {
        String basePath = "hotlists";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + ThreadManager.getOwnerAlbatrossToken());
        AddToHotlistRequest requestBody = new AddToHotlistRequest();
        requestBody.setEntity_name("candidates");
        requestBody.setSelectedrows(new int[]{candidateId});
        requestBody.setShared(shared == 1);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, headers, null, true, requestBody);

        assertThat("Status code is not 422", response.getStatusCode(), is(422));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Failed to verify 422 status code", jsonPath.getString("message"), containsString("Failed To Create Hotlist : The name field is required."));
    }
}
