package io.recruitcrm.albatross.candidate;


import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.CandidateAnswerRequest;
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

import java.util.Map;
import java.util.Iterator;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AnswerCandidateQuestionTest extends TestBase {

    private AllCrudFunctions function;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        function = new AllCrudFunctions();
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "candidateQuestionData", groups = "nightly-build")
    public void answerCandidateQuestionWithValidFields(int candidateId, int questionId) throws Exception {
        CandidateAnswerRequest request = new CandidateAnswerRequest("Test Answer", questionId, null, candidateId);
        String basePath = "candidate-answers";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, request);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
        Assert.assertEquals(response.jsonPath().getString("message_type"), "is-success", "Expected message_type to be 'is-success', but got " + response.jsonPath().getString("message_type"));
        Assert.assertEquals(response.jsonPath().getString("message"), "Answer Edit Successful ", "Expected message to be 'Answer Edit Successful', but got " + response.jsonPath().getString("message"));
        Assert.assertNotNull(response.jsonPath().get("data"), "Expected 'data' to be present in the response");
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "candidateQuestionData", groups = "nightly-build")
    public void answerCandidateQuestionVerify_401(int candidateId, int questionId) {
        CandidateAnswerRequest request = new CandidateAnswerRequest("Test Answer", questionId, null, candidateId);
        String basePath = "candidate-answers";
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken() + "invalid", null, null, true, request.toString());

        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401, but got " + response.getStatusCode());
    }


    @Owner("Akshaya Uppala")
    @Test(dataProvider = "candidateQuestionData", groups = "nightly-build")
    public void answerCandidateQuestionVerify_InvalidQuestionId(int candidateId, int questionId) {
        CandidateAnswerRequest request = new CandidateAnswerRequest("Test Answer", -1, null, candidateId);
        String basePath = "candidate-answers";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, request);

        Assert.assertEquals(response.jsonPath().get("message"), "The selected questionid is invalid.", "Expected message to be 'The selected questionid is invalid.', but got " + response.jsonPath().get("message"));
        Assert.assertEquals(response.jsonPath().get("message_type"), "is-danger", "Expected message_type to be 'is-danger', but got " + response.jsonPath().get("message_type"));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "candidateQuestionData", groups = "nightly-build")
    public void answerCandidateQuestionVerify_422(int candidateId, int questionId) {
        CandidateAnswerRequest request = new CandidateAnswerRequest("", questionId, null, candidateId);
        String basePath = "candidate-answers";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, request);

        Assert.assertEquals(response.getStatusCode(), 422, "Expected status code 422, but got " + response.getStatusCode());
    }

    //for 404 status code i have already reported a bug once it get fixed i will add the test case

    @DataProvider(name = "candidateQuestionData")
    public Object[][] createCandidateQuestionData() {
        JsonPath candidateJsonPath = function.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candidateId = candidateJsonPath.getInt("data.candidate.id");

        function.createCandidateQuestion(albatrossURL, ThreadManager.getOwnerAlbatrossToken());

        GetCandidateQuestionsAndAnswersRequest request = new GetCandidateQuestionsAndAnswersRequest(candidateId, 0, 0);
        Response questionsResponse = RestClient.doPost("JSON", albatrossURL, "candidate-answers/get", ThreadManager.getOwnerAlbatrossToken(), null, true, request);

        Map<String, Object> questions = questionsResponse.jsonPath().getMap("data.questions");

        Assert.assertNotNull(questions, "No questions data found in response");
        Assert.assertFalse(questions.isEmpty(), "Questions data is empty in response");

        Iterator<String> keysIterator = questions.keySet().iterator();
        Assert.assertTrue(keysIterator.hasNext(), "No question keys found in response");

        String firstQuestionKey = keysIterator.next();
        int questionId = Integer.parseInt(firstQuestionKey);

        return new Object[][] {
                { candidateId, questionId }
        };
    }
}