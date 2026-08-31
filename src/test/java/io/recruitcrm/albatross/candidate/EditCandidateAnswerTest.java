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
public class EditCandidateAnswerTest extends TestBase {

    private AllCrudFunctions function;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        function = new AllCrudFunctions();
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "candidateAnswerData", groups = "nightly-build")
    public void editCandidateAnswerWithValidFields(int candidateId, int questionId, int answerId) throws Exception {
        CandidateAnswerRequest request = new CandidateAnswerRequest("Updated Test Answer", questionId, answerId, candidateId);
        String basePath = "candidate-answers/" + answerId;
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, request);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
        Assert.assertEquals(response.jsonPath().getString("message_type"), "is-success", "Expected message_type to be 'is-success', but got " + response.jsonPath().getString("message_type"));
        Assert.assertEquals(response.jsonPath().getString("message"), "Field Updated Successfully", "Expected message to be 'Field Updated Successfully', but got " + response.jsonPath().getString("message"));
        Assert.assertNotNull(response.jsonPath().get("data"), "Expected 'data' to be present in the response");
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "candidateAnswerData", groups = "nightly-build")
    public void editCandidateAnswerVerify_401(int candidateId, int questionId, int answerId) {
        CandidateAnswerRequest request = new CandidateAnswerRequest("Updated Test Answer", questionId, answerId, candidateId);
        String basePath = "candidate-answers/" + answerId;
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken() + "invalid", null, true, request.toString());

        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401, but got " + response.getStatusCode());
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "candidateAnswerData", groups = "nightly-build")
    public void editCandidateAnswerVerify_InvalidQuestionId(int candidateId, int questionId, int answerId) {
        CandidateAnswerRequest request = new CandidateAnswerRequest("Updated Test Answer", -1, answerId, candidateId);
        String basePath = "candidate-answers/" + answerId;
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, request);

        Assert.assertEquals(response.jsonPath().getString("message"), "Failed To Answer Edit : Question not found", "Expected message 'Failed To Answer Edit : Question not found', but got " + response.jsonPath().getString("message"));
        Assert.assertEquals(response.jsonPath().getString("message_type"), "is-danger", "Expected message_type to be 'is-danger', but got " + response.jsonPath().getString("message_type"));
        Assert.assertTrue(response.jsonPath().getList("data").isEmpty(), "Expected 'data' to be an empty array");
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "candidateAnswerData", groups = "nightly-build")
    public void editCandidateAnswerVerify_422(int candidateId, int questionId, int answerId) {
        CandidateAnswerRequest request = new CandidateAnswerRequest("", questionId, answerId, candidateId);
        String basePath = "candidate-answers/" + answerId;
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, request);

        Assert.assertEquals(response.getStatusCode(), 422, "Expected status code 422, but got " + response.getStatusCode());
    }

    //for 404 status code i have already reported a bug once it get fixed i will add the test case

    @DataProvider(name = "candidateAnswerData")
    public Object[][] createCandidateAnswerData() {
        JsonPath candidateJsonPath = function.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candidateId = candidateJsonPath.getInt("data.candidate.id");
        function.createCandidateQuestion(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
        GetCandidateQuestionsAndAnswersRequest questionsRequest = new GetCandidateQuestionsAndAnswersRequest(candidateId, 0, 0);
        Response questionsResponse = RestClient.doPost("JSON", albatrossURL, "candidate-answers/get", ThreadManager.getOwnerAlbatrossToken(), null, true, questionsRequest);

        Map<String, Object> questions = questionsResponse.jsonPath().getMap("data.questions");

        Assert.assertNotNull(questions, "No questions data found in response");
        Assert.assertFalse(questions.isEmpty(), "Questions data is empty in response");

        Iterator<String> keysIterator = questions.keySet().iterator();
        Assert.assertTrue(keysIterator.hasNext(), "No question keys found in response");

        String firstQuestionKey = keysIterator.next();
        int questionId = Integer.parseInt(firstQuestionKey);

        CandidateAnswerRequest answerRequest = new CandidateAnswerRequest("Initial Test Answer", questionId, null, candidateId);
        Response answerResponse = RestClient.doPost("JSON", albatrossURL, "candidate-answers", ThreadManager.getOwnerAlbatrossToken(), null, true, answerRequest);
        Assert.assertEquals(answerResponse.getStatusCode(), 200, "Failed to create candidate answer");

        Response answersResponse = RestClient.doPost("JSON", albatrossURL, "candidate-answers/get", ThreadManager.getOwnerAlbatrossToken(), null, true, questionsRequest);

        Map<String, Object> updatedQuestions = answersResponse.jsonPath().getMap("data.questions");
        Assert.assertNotNull(updatedQuestions, "No questions data found in response after creating answer");

        Map<String, Object> questionData = (Map<String, Object>) updatedQuestions.get(String.valueOf(questionId));
        Assert.assertNotNull(questionData, "Question data not found for question ID: " + questionId);

        int answerId = ((Number) questionData.get("answerid")).intValue();
        Assert.assertTrue(answerId > 0, "Invalid answer ID retrieved: " + answerId);

        return new Object[][] {
                { candidateId, questionId, answerId }
        };
    }
}