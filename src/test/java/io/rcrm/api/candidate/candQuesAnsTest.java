package io.rcrm.api.candidate;

import java.util.*;
import org.testng.annotations.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.*;
import io.rcrm.api.pojo.CandQuesAnsWithoutJob;
import io.rcrm.api.pojo.CandidateQuestionAnswer;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class candQuesAnsTest extends TestBase {


    commanFunction function = new commanFunction();
    String slug = "";
    CandQuesAnsWithoutJob candQuesAnsWithoutJob = new CandQuesAnsWithoutJob();
    String apiKeyA;
    String albatrossTknA;
    String albatrossTknB;
    JavaFakerJob jobFaker;
    JavaFakerCandidate candidateFaker;
    String questionsAndAnswersBasePath = "candidates/question-and-answers/{candidate}";
    String questionsAndAnswersAssociatedWithJobBasePath = "candidates/question-and-answers/{candidate}/{job}";
    String jobsBasePath = "jobs/{job}";

    @BeforeClass(alwaysRun = true)    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        candQuesAnsWithoutJob = new CandQuesAnsWithoutJob();
        jobFaker = new JavaFakerJob();
        candidateFaker = new JavaFakerCandidate();
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "getCandidateSlug", groups = "nightly-build")
    public void getCandQuesAndAnswersWithValidResponse200(String candidateSlug) {
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("candidate", candidateSlug);
        CandQuesAnsWithoutJob candQuesAnsWithoutJob = new CandQuesAnsWithoutJob();
        candQuesAnsWithoutJob.setUnanswered(true);

        Response response = RestClient.doGet1("JSON", baseURL, questionsAndAnswersBasePath, apiKeyA, null, pathParamters, true, candQuesAnsWithoutJob);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//CandQuesAnsWithoutJob.json"));
        response.then().statusCode(200);
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void getCandQuesAndAnswersWithInValidResponse404() {
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("candidate", candidateFaker.getInvalidCandidateSlug());

        CandQuesAnsWithoutJob candQuesAnsWithoutJob = new CandQuesAnsWithoutJob();
        candQuesAnsWithoutJob.setUnanswered(true);
        Response response = RestClient.doGet1("JSON", baseURL, questionsAndAnswersBasePath, apiKeyA, null, pathParamters, true, candQuesAnsWithoutJob);

        response.then().statusCode(404);
        response.then().assertThat().body("message", Matchers.is("Candidate doesn't exist"));
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "getCandidateSlug", groups = "nightly-build")
    public void postCandQuesAndAnswersWithValidResponse200(String candidateSlug) {
        CandidateQuestionAnswer candidateQuestionAnswer = prepareQuestionAnswerForCandidate(candidateSlug, albatrossTknA);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("candidate", candidateSlug);
        Response response = RestClient.doPost1("JSON", baseURL, questionsAndAnswersBasePath, apiKeyA, null, pathParameters, true, candidateQuestionAnswer);

        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//candQuesAnsPostWithoutJobAssociate.json"));
        response.then().statusCode(200);
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "getCandidateSlug", groups = "nightly-build")
    public void postCandQuesAndAnswersWithInValidResponse404(String candidateSlug) {
        CandidateQuestionAnswer candidateQuestionAnswer = prepareQuestionAnswerForCandidate(candidateSlug, albatrossTknA);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("candidate", candidateFaker.getInvalidCandidateSlug());

        Response response = RestClient.doPost1("JSON", baseURL, questionsAndAnswersBasePath, apiKeyA, null, pathParameters, true, candidateQuestionAnswer);
        response.then().statusCode(404);
        response.then().assertThat().body("message", Matchers.is("Candidate doesn't exist"));
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "assignCandidateToJob", groups = "nightly-build")
    public void getCandQuesAndAnswersAssociatedWithJobWithValidResponse200(String candidateSlug, String jobSlug) {
        AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();
        albatrossFunctions1.createCandidateQuestion(albatrossURL, albatrossTknA);
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("candidate", candidateSlug);
        pathParamters.put("job", jobSlug);
        CandQuesAnsWithoutJob candQuesAnsWithoutJob = new CandQuesAnsWithoutJob();
        candQuesAnsWithoutJob.setUnanswered(true);
        Response response = RestClient.doGet1("JSON", baseURL, questionsAndAnswersAssociatedWithJobBasePath, apiKeyA, null, pathParamters, true, candQuesAnsWithoutJob);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//getCandQuesAndAnswersAssociatedWithJob.json"));
        response.then().statusCode(200);
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "assignCandidateToJob", groups = "nightly-build")
    public void getCandQuesAndAnswersAssociatedWithJobWithInValidResponse404(String candidateSlug, String jobSlug) {
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("candidate", candidateSlug);
        pathParamters.put("job", candidateFaker.getInvalidCandidateSlug());

        CandQuesAnsWithoutJob candQuesAnsWithoutJob = new CandQuesAnsWithoutJob();
        candQuesAnsWithoutJob.setUnanswered(true);
        Response response = RestClient.doGet1("JSON", baseURL, questionsAndAnswersAssociatedWithJobBasePath, apiKeyA, null, pathParamters, true, candQuesAnsWithoutJob);
        response.then().statusCode(404);
        response.then().assertThat().body("message", Matchers.is("Job doesn't exist"));
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "assignCandidateToJob", groups = "nightly-build")
    public void postCandQuesAndAnswersAssociatedWithJobWithValidResponse200(String candidateSlug, String jobSlug) {
        Response questionResponse = createCandidateQuestionAndAssociateWithJob(jobSlug);
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("candidate", candidateSlug);
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("job_slug", jobSlug);
        pathParamters.put("candidate", candidateSlug);

        CandQuesAnsWithoutJob candQuesAnsWithoutJob = new CandQuesAnsWithoutJob();
        candQuesAnsWithoutJob.setUnanswered(true);
        Response response = RestClient.doGet1("JSON", baseURL, questionsAndAnswersBasePath, apiKeyA, null, pathParamters, true, candQuesAnsWithoutJob);

        String firstQuestionId = questionResponse.jsonPath().getString("id").substring(1, questionResponse.jsonPath().getString("id").length() - 1);
        String[] numbersArray = firstQuestionId.split(",");
        pathParamters.put("job", jobSlug);

        CandQuesAnsWithoutJob question_answers = new CandQuesAnsWithoutJob();
        question_answers.setUnanswered(false);
        question_answers.setAnswer(jobFaker.getJobName());
        question_answers.setQuestion_id(Integer.parseInt(numbersArray[0].trim()));
        CandidateQuestionAnswer candidateQuestionAnswer=new CandidateQuestionAnswer();
        candidateQuestionAnswer.setQuestion_answers(Collections.singletonList(question_answers));

        Response postResponse = RestClient.doPost1("JSON", baseURL, questionsAndAnswersAssociatedWithJobBasePath, apiKeyA, null, pathParamters, true, candidateQuestionAnswer);
        postResponse.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//candQuesAnsPostWithJobAssociated.json"));
        postResponse.then().statusCode(200);
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "assignCandidateToJob", groups = "nightly-build")
    public void postCandQuesAndAnswersAssociatedWithJobWithValidResponse404(String candidateSlug, String jobSlug) {
        Response questionResponse = createCandidateQuestionAndAssociateWithJob(jobSlug);
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("candidate", candidateFaker.getInvalidCandidateSlug());
        CandQuesAnsWithoutJob candQuesAnsWithoutJob = new CandQuesAnsWithoutJob();
        candQuesAnsWithoutJob.setUnanswered(true);
        Response response = RestClient.doGet1("JSON", baseURL, questionsAndAnswersBasePath, apiKeyA, null, pathParamters, true, candQuesAnsWithoutJob);

        String firstQuestionId = questionResponse.jsonPath().getString("id").substring(1, questionResponse.jsonPath().getString("id").length() - 1);
        String[] numbers = firstQuestionId.split(", ");
        String firstNumber = numbers[0];
        pathParamters.put("job", jobSlug);

        CandQuesAnsWithoutJob question_answers = new CandQuesAnsWithoutJob();
        question_answers.setUnanswered(false);
        question_answers.setAnswer(jobFaker.getJobName());
        question_answers.setQuestion_id(Integer.parseInt(firstNumber));
        CandidateQuestionAnswer candidateQuestionAnswer=new CandidateQuestionAnswer();
        candidateQuestionAnswer.setQuestion_answers(Collections.singletonList(question_answers));

        Response postResponse = RestClient.doPost1("JSON", baseURL, questionsAndAnswersAssociatedWithJobBasePath, apiKeyA, null, pathParamters, true, candidateQuestionAnswer);
        postResponse.then().statusCode(404);
        postResponse.then().assertThat().body("message", Matchers.is("Candidate doesn't exist"));
    }

    private CandidateQuestionAnswer prepareQuestionAnswerForCandidate(String candidateSlug, String token) {
        AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();
        albatrossFunctions1.createCandidateQuestion(albatrossURL, token);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("candidate", candidateSlug);
        CandQuesAnsWithoutJob candQuesAnsWithoutJob = new CandQuesAnsWithoutJob();
        candQuesAnsWithoutJob.setUnanswered(true);
        Response response = RestClient.doGet1("JSON", baseURL, questionsAndAnswersBasePath, apiKeyA, null, pathParameters, true, candQuesAnsWithoutJob);
        JsonPath jp = response.jsonPath();
        int questionId = jp.getInt("data[0].'Question ID'");

        CandQuesAnsWithoutJob question_answers = new CandQuesAnsWithoutJob();
        question_answers.setUnanswered(false);
        question_answers.setAnswer(jobFaker.getJobName());
        question_answers.setQuestion_id(questionId);
        CandidateQuestionAnswer candidateQuestionAnswer = new CandidateQuestionAnswer();
        candidateQuestionAnswer.setQuestion_answers(Collections.singletonList(question_answers));

        return candidateQuestionAnswer;
    }

    private Response createCandidateQuestionAndAssociateWithJob(String jobSlug) {
        AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();
        albatrossFunctions1.createCandidateQuestion(albatrossURL, albatrossTknA);

        Map<String, String> queryParameters1 = new HashMap<String, String>();
        queryParameters1.put("limit", "25");
        queryParameters1.put("page", "1");

        Response getQuestionsResponse = RestClient.doGet("JSON", baseURL, "candidate-questions", apiKeyA, queryParameters1, null, true);
        String questionId = getQuestionsResponse.jsonPath().getString("id");
        if (questionId.startsWith("[") && questionId.endsWith("]")) {
            questionId = questionId.substring(1, questionId.length() - 1);
        }

        Job job = new Job(jobFaker.getJobName());
        job.setJob_questions(questionId);
        Map<String, String> pathParamters1 = new HashMap<String, String>();
        pathParamters1.put("job", jobSlug);
        RestClient.doPost1("JSON", baseURL, jobsBasePath, apiKeyA, null, pathParamters1, true, job);
        return getQuestionsResponse;
    }

    @DataProvider(parallel=true)
    public Object[][] getCandidateSlug() {
        String candidateSlug = function.getEntityResponse(baseURL, apiKeyA, "candidate");
        return new Object[][] {
                { candidateSlug }
        };
    }

    @DataProvider(parallel=true)
    public Object[][] assignCandidateToJob() {
        String candidateSlug = function.getEntityResponse(baseURL, apiKeyA, "candidate");
        String jobSlug = function.getEntityResponse(baseURL, apiKeyA, "job");

        JsonPath jp = function.assignJobToCandidate(baseURL, apiKeyA, candidateSlug, jobSlug);

        return new Object[][] {
                { candidateSlug, jobSlug }
        };
    }

}

