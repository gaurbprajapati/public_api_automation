package io.rcrm.api.emailsequence;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.UpdateFields;
import io.rcrm.api.pojo.enrollInSequence;
import io.rcrm.api.pojo.nyma.*;
import io.rcrm.api.pojo.reaper.UpdateEntityRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

import static org.hamcrest.Matchers.notNullValue;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class EnrollInSmsSequenceTest extends TestBase {

    public EnrollInSmsSequenceTest() {
        // TODO Auto-generated constructor stub
        super();
    }

    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();
    String generatedString = RandomStringUtils.randomAlphabetic(4);
    int candidateSeqId;
    int contactSeqId;
    int userId;
    String candidateEntitySlug;
    int enrollmentId;
    int relatedToTypeId = 5;
    ArrayList<Integer> candList;
    String albatrossAuthToken;
    String apiToken;
    int accountId;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiToken = ThreadManager.getAccountApiKey();
        accountId = ThreadManager.getAccount().getAccountId();
        userId = ThreadManager.getOwner().getUserId();
    }


    @Owner("Ajendra Singh")
    @Test(dataProvider = "enrollSequenceDetails", priority = 0, groups = "nightly-build")
    public void enrollInSequenceWithMandatoryFields_POST(String entity, int EnrolledBy, int sequenceId, String prospectSlug) {
        enrollInSequence enrollInSequence = new enrollInSequence();
        enrollInSequence.setSequence_id(sequenceId);
        enrollInSequence.setEnrolled_by(EnrolledBy);
        enrollInSequence.setProspect_slug(prospectSlug);

        String basePath = entity + "/" + prospectSlug + "/enroll";

        Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true,
                enrollInSequence);
        response.then().statusCode(200);
        response.then().body("id", notNullValue());
    }

    @Owner("Ajendra Singh")
    @Test(priority = 1, groups = "nightly-build")
    public void getSmsSequenceListing() {
        String basePath = "email-sequences";
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("limit", "10");
        queryParameters.put("page", "1");
        queryParameters.put("req_from", "1");

        Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
        response.then().statusCode(200);
        response.then().body("data.sequences[0].id", Matchers.is(candidateSeqId));
        response.then().body("data.sequences[0].paused_due_to_low_credits", Matchers.is(0));
        response.then().body("data.sequences[0].low_sms_credit_warning", Matchers.is(true));
    }

    @Owner("Ajendra Singh")
    @Test(priority = 2, groups = "nightly-build")
    public void getSmsSequenceDetails() {

        String basePath = "email-sequences/{id}";
        Map<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("id", String.valueOf(candidateSeqId));

        Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);
        response.then().statusCode(200);
        response.then().body("message", Matchers.is("Sequence fetched successfully."));
        response.then().body("data.id", Matchers.is(candidateSeqId));
        response.then().body("data.steps[0].type", Matchers.is(3));        // 1 for email, 2 for task , 3 for sms
        response.then().body("data.active_paused_enrollment", Matchers.is(1));
    }

    @Owner("Ajendra Singh")
    @Test(priority = 3, groups = "nightly-build")
    public void getEnrollmentsList() {
        String basePath = "email-sequences/{id}/enrollments";
        Map<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("id", String.valueOf(candidateSeqId));

        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("limit", "10");
        queryParameters.put("page", "1");
        queryParameters.put("status", "0");
        queryParameters.put("enrolled_by[]", String.valueOf(userId));

        Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, pathParameters, true);
        response.then().statusCode(200);
        enrollmentId = response.jsonPath().get("data.enrollments[0].id");
    }

    @Owner("Ajendra Singh")
    @Test(priority = 4, groups = "nightly-build")
    public void getEnrollmentsDetails() {
        String basePath = "enrollments/{enrollment-id}";
        Map<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("enrollment-id", String.valueOf(enrollmentId));

        Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);
        response.then().statusCode(200);
        response.then().body("message", Matchers.is("Sequence Enrollment Details Fetched successfully"));
        response.then().body("data.id", Matchers.is(enrollmentId));
        response.then().body("data.enrolled_by", Matchers.is(userId));
    }


    @DataProvider
    public Object[][] enrollSequenceDetails() {
        ReaperIntegration.enableA2PRegistration(ThreadManager.getAccount().getAccountId());
        ReaperIntegration.updateTwilioSubaccount(ThreadManager.getAccount().getAccountId());
        candidateEntitySlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
        Map<String, String> columnsAndValue = new HashMap<>();
        columnsAndValue.put("contactnumber", "+17862337361");
        UpdateEntityRequest updateEntityRequest = new UpdateEntityRequest();
        updateEntityRequest.setEntityType("candidate");
        updateEntityRequest.setColumnsAndValue(columnsAndValue);
        ReaperIntegration.updateEntityColumns(candidateEntitySlug, updateEntityRequest);
        ReaperIntegration.provideSmsConsentToEntity(accountId, "Candidate", candidateEntitySlug);

        createSequenceWithSmsStep("candidates");
        return new Object[][]{{"candidates", userId, candidateSeqId, candidateEntitySlug}
        };
    }

    private void createSequenceWithSmsStep(String entity) {
        CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();
        SequenceSettingPage sequenceSetting = new SequenceSettingPage();
        sequenceSetting.setThread_emails_as_replies(1);
        sequenceSetting.setExecute_step_on_business_days(1);
        JSONObject settings = new JSONObject(sequenceSetting);

        createEmailSequence.setEntity_type(entity.equals("candidates") ? 5 : 2);
        createEmailSequence.setSeq_title(entity + " add sequence test " + RandomStringUtils.randomAlphabetic(4));
        createEmailSequence.setSeq_settings(settings.toString());
        createEmailSequence.setSilent_progress(false);
        createEmailSequence.setSave_steps(0);

        Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
                createEmailSequence);
        JsonPath jp = response.jsonPath();
        int seqId = jp.get("data.id");
        response.then().statusCode(200);
        if (entity.equals("candidates")) {
            candidateSeqId = seqId;
        } else {
            contactSeqId = seqId;
        }

        Map<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("id", String.valueOf(seqId));

        CreateSmsStepToSequencePage createSmsStepToSequencePage = new CreateSmsStepToSequencePage();
        createSmsStepToSequencePage.setType(3);
        createSmsStepToSequencePage.setStep_no(1);
        createSmsStepToSequencePage.setNo_of_days(2);
        createSmsStepToSequencePage
                .setSms_template_title(entity + " SMS Template " + RandomStringUtils.randomAlphabetic(4));
        createSmsStepToSequencePage
                .setSms_template_content(entity + " Template body " + RandomStringUtils.randomAlphabetic(4));
        createSmsStepToSequencePage.setTime(3600);
        createSmsStepToSequencePage.setUpdate_type("all");

        ArrayList<Object> smsStep = new ArrayList<>();
        smsStep.add(createSmsStepToSequencePage);
        AddSmsStepsToSequencePage addSmsStep = new AddSmsStepsToSequencePage();
        addSmsStep.setSteps(smsStep);


        String basePath = "email-sequences/{id}/steps";
        Response responseAddEmailStep = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true, addSmsStep);

        responseAddEmailStep.then().statusCode(200);
    }
}
