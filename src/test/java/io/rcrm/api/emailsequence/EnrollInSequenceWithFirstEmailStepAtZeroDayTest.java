package io.rcrm.api.emailsequence;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerMails;
import io.rcrm.api.pojo.EnrollInSequencePayload;
import io.rcrm.api.pojo.EnrollmentSteps;
import io.rcrm.api.pojo.enrollInSequence;
import io.rcrm.api.pojo.nyma.AddEmailStepsToSequencePage;
import io.rcrm.api.pojo.nyma.CreateEmailSequencePage;
import io.rcrm.api.pojo.nyma.CreateEmailStepToSequencePage;
import io.rcrm.api.pojo.nyma.SequenceSettingPage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class EnrollInSequenceWithFirstEmailStepAtZeroDayTest extends TestBase {

    commanFunction function = new commanFunction();
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    JavaFakerMails fakerMails = new JavaFakerMails();
    int candidateSeqId;
    int contactSeqId;
    int userId;
    int[] CandStepsId = new int[2];
    int[] ContactStepsId = new int[2];
    String accountApiKey;
    String albatrossToken;
    String emailTemplateTitle, emailTemplateSubject, emailTemplateContent;

    @BeforeClass
	public void setUp() {
		albatrossToken = ThreadManager.getOwnerAlbatrossToken();
        accountApiKey = ThreadManager.getAccountApiKey();
        userId = ThreadManager.getOwner().getUserId();
	}

    @Owner("Ajendra Singh")
    @Test(dataProvider = "enrollSequenceDetails", groups = "nightly-build")
    public void enrollInSequenceWithFirstEmailStepAtZeroDay(String entity, int EnrolledBy, int sequenceId) {
        enrollInSequence enrollInSequence = new enrollInSequence();

        enrollInSequence.setSequence_id(sequenceId);
        enrollInSequence.setEnrolled_by(EnrolledBy);
        String prospectSlug;
        switch (entity.toLowerCase()) {
            case "candidates":
                prospectSlug = function.createNewCandidateWithMandatoryFields(baseURL, accountApiKey).jsonPath().get("slug");
                break;
            case "contacts":
                String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, accountApiKey).jsonPath().get("slug");
                prospectSlug = function.createNewContact_POST(baseURL, accountApiKey, companySlug).jsonPath().get("slug");
                break;
            default:
                throw new IllegalArgumentException("Invalid entity type: " + entity);
        }

        enrollInSequence.setProspect_slug(prospectSlug);

        String basePath = entity + "/" + prospectSlug + "/enroll";
        Response response = RestClient.doPost("JSON", baseURL, basePath, accountApiKey, null, true,
                enrollInSequence);

        response.then().statusCode(200);
        response.then().body("id", notNullValue());
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "enrollCandContactInSequence", groups = "nightly-build")
    public void enrollCandContactInSequenceFirstEmailStepAtZeroDay_POST(String entity, int seqId, int[] StepsId) {
        int entityId;
        if (entity.equalsIgnoreCase("Candidates")) {
            Response getCandResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossToken);
            JsonPath jpCand = getCandResponse.jsonPath();
            entityId = jpCand.get("data.candidate.id");
        } else {
            String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, accountApiKey).jsonPath().get("slug");
            String contactSlug = function.createNewContact_POST(baseURL, accountApiKey, companySlug).jsonPath().get("slug");
            JsonPath jpContact = albatrossFunctions.getContactResponse(albatrossURL, albatrossToken, contactSlug).jsonPath();
            entityId = Integer.parseInt(jpContact.get("data.contact.id"));
        }

        EnrollInSequencePayload enrollInSequencePayload = new EnrollInSequencePayload();
        enrollInSequencePayload.setId(seqId);
        enrollInSequencePayload.setStart_at_step(1);
        enrollInSequencePayload.setEnrollments(new int[]{entityId});

        EnrollmentSteps[] steps = new EnrollmentSteps[2];
        steps[0] = new EnrollmentSteps("settings", StepsId[0], 0, 1, 36000, 1, emailTemplateContent, emailTemplateSubject, emailTemplateTitle); // Step 1
        steps[1] = new EnrollmentSteps("settings", StepsId[1], 2, 1, 36000, 1, emailTemplateContent, emailTemplateSubject, emailTemplateTitle); // Step 2

        enrollInSequencePayload.setSteps(steps);
        enrollInSequencePayload.setLinked_email_type(1);

        String basePath = "/enrollments";

        Response response = RestClient.doPost("JSON", nymaURL, basePath, albatrossToken, null, true,
                enrollInSequencePayload);

        response.then().statusCode(200);
        response.then().body("message", Matchers.is("Enroll in a Sequence Successful "));
        response.then().body("message_type", Matchers.is("is-success"));
    }

    @DataProvider(parallel = true)
    public Object[][] enrollSequenceDetails() {
        enrollEmailSequence("candidates");
        enrollEmailSequence("contacts");
        return new Object[][]{{"candidates", userId, candidateSeqId},
                {"contacts", userId, contactSeqId}};
    }

    @DataProvider(parallel = true)
    public Object[][] enrollCandContactInSequence() {
        enrollSequenceDetails();
        return new Object[][]{{"candidates", candidateSeqId, CandStepsId},
                {"contacts", contactSeqId, ContactStepsId}};
    }

    private void enrollEmailSequence(String entity) {
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

        Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", albatrossToken, null, true,
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
        emailTemplateTitle = fakerMails.getRandomRecruitmentWord() + " " + entity + " Email Template";
        emailTemplateSubject = fakerMails.getFakeEmailSubject();
        emailTemplateContent = fakerMails.getFakeEmailBody(5);

        CreateEmailStepToSequencePage createEmailStepToSequence = new CreateEmailStepToSequencePage();
        createEmailStepToSequence.setStep_no(1);
        createEmailStepToSequence.setNo_of_days(0);            //adding email step with 0th day
        createEmailStepToSequence.setTemplate_title(emailTemplateTitle);
        createEmailStepToSequence.setTemplate_subject(emailTemplateSubject);
        createEmailStepToSequence.setTemplate_content(emailTemplateContent);
        createEmailStepToSequence.setTime(3600);
        createEmailStepToSequence.setType(1);
        createEmailStepToSequence.setUpdate_type("all");
        createEmailStepToSequence.setInclude_opt_out_link(1);

        ArrayList<Object> emailStep = new ArrayList<>();
        emailStep.add(createEmailStepToSequence);
        AddEmailStepsToSequencePage addEmailStep = new AddEmailStepsToSequencePage();
        addEmailStep.setSteps(emailStep);

        CreateEmailStepToSequencePage createEmailStepToSequence1 = new CreateEmailStepToSequencePage();
        createEmailStepToSequence1.setStep_no(2);
        createEmailStepToSequence1.setNo_of_days(2);
        createEmailStepToSequence1.setTemplate_title(emailTemplateTitle);
        createEmailStepToSequence1.setTemplate_subject(emailTemplateSubject);
        createEmailStepToSequence1.setTemplate_content(emailTemplateContent);
        createEmailStepToSequence1.setTime(3600);
        createEmailStepToSequence1.setType(1);
        createEmailStepToSequence1.setUpdate_type("all");
        createEmailStepToSequence1.setInclude_opt_out_link(1);

        emailStep.add(createEmailStepToSequence1);
        addEmailStep.setSteps(emailStep);

        String basePath = "email-sequences/{id}/steps";
        Response responseAddEmailStep1 = RestClient.doPost1("JSON", nymaURL, basePath, albatrossToken, null, pathParameters, true, addEmailStep);

        responseAddEmailStep1.then().statusCode(200);

        for (int i = 0; i < 2; i++) {
            if (entity.equals("candidates")) {
                CandStepsId[i] = responseAddEmailStep1.jsonPath().get("data[" + i + "].id");
            } else {
                ContactStepsId[i] = responseAddEmailStep1.jsonPath().get("data[" + i + "].id");
            }
        }
    }
}
