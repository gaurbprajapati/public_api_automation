package io.recruitcrm.albatross.candidate;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.GetCandidateQuestionsAndAnswersRequest;
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
public class GetCandidateQuestionAndAnswersTest extends TestBase {

    private AllCrudFunctions function;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        function = new AllCrudFunctions();
    }



    @Owner("Akshaya Uppala")
    @Test(dataProvider = "candidateData", groups = "nightly-build")
    public void getCandidateAnswersWithValidFields(int candidateId) throws Exception {
        GetCandidateQuestionsAndAnswersRequest request = new GetCandidateQuestionsAndAnswersRequest(candidateId, 0, 0);
        String basePath = "candidate-answers/get";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, request);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
        Assert.assertEquals(response.jsonPath().getString("message_type"), "is-success", "Expected message_type to be 'is-success', but got " + response.jsonPath().getString("message_type"));
        Assert.assertNotNull(response.jsonPath().get("data"), "Expected 'data' to be present in the response");
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "candidateData", groups = "nightly-build")
    public void getCandidateAnswersVerify_401(int candidateId) {
        GetCandidateQuestionsAndAnswersRequest request = new GetCandidateQuestionsAndAnswersRequest(candidateId, 0, 0);
        String basePath = "candidate-answers/get";
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken() + "invalid", null, null, true, request.toString());
        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401, but got " + response.getStatusCode());
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getCandidateAnswersVerify_MissingIdReturnsNullData() {
        GetCandidateQuestionsAndAnswersRequest request = new GetCandidateQuestionsAndAnswersRequest(0, 0, 0);
        String basePath = "candidate-answers/get";
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, request.toString());

        Assert.assertEquals(response.getStatusCode(), 422, "Expected status code 422, but got " + response.getStatusCode());
        Assert.assertTrue(response.jsonPath().getList("data").isEmpty(), "Expected 'data' to be an empty list when 'id' is not provided");    }

    //for 404 and 422 status codes i have already reported a bug once it get fixed i will add the test cases

    @DataProvider(name = "candidateData")
    public Object[][] createCandidateData() {
        JsonPath candidateJsonPath = function.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        function.createCandidateQuestion(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
        int candidateId = candidateJsonPath.getInt("data.candidate.id");
        return new Object[][]{
                { candidateId }
        };
    }
}

