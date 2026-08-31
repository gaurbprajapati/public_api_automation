package io.recruitcrm.nyma.emailsequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerMails;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.albatross.EmailTemplatePage;
import io.rcrm.api.pojo.albatross.New_email_templatePage;
import io.rcrm.api.pojo.nyma.AddEmailStepsToSequencePage;
import io.rcrm.api.pojo.nyma.AddTaskStepsToSequencePage;
import io.rcrm.api.pojo.nyma.CreateEmailSequencePage;
import io.rcrm.api.pojo.nyma.CreateEmailStepToSequencePage;
import io.rcrm.api.pojo.nyma.CreateTaskStepToSequencePage;
import io.rcrm.api.pojo.nyma.EnrollInSequencePage;
import io.rcrm.api.pojo.nyma.SequenceSettingPage;
import io.rcrm.api.pojo.nyma.UnEnrollInSequencePage;
import io.rcrm.api.pojo.nyma.UpdateStepsInEnrollmentPage;
import io.rcrm.api.pojo.nyma.ValidateEnrollmentsPage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class UnEnrollSequenceTest extends TestBase {

	commanFunction function = new commanFunction();
	JavaFakerMails fakerMails = new JavaFakerMails();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	String templateTitle;
	String templateSubject;
	String templateBody;
	int relatedToTypeId = 5;
	ArrayList<Integer> candList;
	int seqId;
	int emailStepId;
	int userId;
	int enrollmentId;

	@Owner("Harika")
	@Test(dataProvider = "getSeqId", priority = 0, groups = "nightly-build")
	public void unEnrollInSequence(int seqId, int enrollmentId) {

		String basePath = "email-sequences/{id}/un-enroll";

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		ArrayList<Integer> unEnrollmentsId = new ArrayList<Integer>();
		unEnrollmentsId.add(enrollmentId);

		UnEnrollInSequencePage unEnrollInSequence = new UnEnrollInSequencePage();
		unEnrollInSequence.setEnrollments(unEnrollmentsId);
		unEnrollInSequence.setFollowup_task(false);

		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				unEnrollInSequence);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("un-enrolled successfully"));
		response.then().body("action_name", Matchers.containsString("Un-enroll Records from Sequence"));
	}

	@Owner("Harika")
	@Test(priority = 1, groups = "nightly-build")
	public void unEnrollInSequenceInvalidSeqId() {

		String basePath = "email-sequences/{id}/un-enroll";

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId + "123"));

		ArrayList<Integer> unEnrollmentsId = new ArrayList<Integer>();
		unEnrollmentsId.add(enrollmentId);

		UnEnrollInSequencePage unEnrollInSequence = new UnEnrollInSequencePage();
		unEnrollInSequence.setEnrollments(unEnrollmentsId);
		unEnrollInSequence.setFollowup_task(false);

		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				unEnrollInSequence);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message",
				Matchers.containsString("Failed To Un-enroll Records from Sequence : Sequence does not exists."));

	}

	@Owner("Harika")
	@Test(priority = 2, groups = "nightly-build")
	public void unEnrollInSequenceInvalidId() {

		String basePath = "email-sequences/{id}/un-enroll";

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		ArrayList<Integer> unEnrollmentsId = new ArrayList<Integer>();
		unEnrollmentsId.add(1234);

		UnEnrollInSequencePage unEnrollInSequence = new UnEnrollInSequencePage();
		unEnrollInSequence.setEnrollments(unEnrollmentsId);
		unEnrollInSequence.setFollowup_task(false);

		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				unEnrollInSequence);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("No records found to Un-enroll"));
		response.then().body("action_name", Matchers.containsString("Un-enroll Records from Sequence"));

	}

	@Owner("Harika")
	@Test(priority = 3, groups = "nightly-build")
	public void unEnrollInSequenceInvalidAuth() {

		String basePath = "email-sequences/{id}/un-enroll";

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		ArrayList<Integer> unEnrollmentsId = new ArrayList<Integer>();
		unEnrollmentsId.add(enrollmentId);

		UnEnrollInSequencePage unEnrollInSequence = new UnEnrollInSequencePage();
		unEnrollInSequence.setEnrollments(unEnrollmentsId);
		unEnrollInSequence.setFollowup_task(false);

		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, pathParameters,
				true, unEnrollInSequence);

		response.then().statusCode(401);

	}

	@Owner("Harika")
	@Test(priority = 4, groups = "nightly-build")
	public void unEnrollAllInSequence() {

		String basePath = "email-sequences/{id}/un-enroll/all";

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		UnEnrollInSequencePage unEnrollInSequence = new UnEnrollInSequencePage();
		unEnrollInSequence.setFollowup_task(false);

		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				unEnrollInSequence);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Un-enroll Records from Sequence"));

	}

	@Owner("Harika")
	@Test(priority = 5, groups = "nightly-build")
	public void unEnrollAllInSequenceInvalidSeqId() {

		String basePath = "email-sequences/{id}/un-enroll/all";

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId + "123"));

		UnEnrollInSequencePage unEnrollInSequence = new UnEnrollInSequencePage();
		unEnrollInSequence.setFollowup_task(false);

		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				unEnrollInSequence);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message",
				Matchers.containsString("Failed To Un-enroll Records from Sequence : Sequence does not exists."));

	}

	@Owner("Harika")
	@Test(priority = 6, groups = "nightly-build")
	public void unEnrollAllInSequenceInvalidAuth() {

		String basePath = "email-sequences/{id}/un-enroll/all";

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		UnEnrollInSequencePage unEnrollInSequence = new UnEnrollInSequencePage();
		unEnrollInSequence.setFollowup_task(false);

		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, pathParameters,
				true, unEnrollInSequence);

		response.then().statusCode(401);

	}

	public void createEmailTemplate(String templateForEntity, String relatedToTypeId) {

		New_email_templatePage new_email_templatePage = new New_email_templatePage();
		new_email_templatePage.setEmailcontext(templateForEntity + " Email Template " + generatedString);
		new_email_templatePage.setRelatedtotypeid(relatedToTypeId);
		new_email_templatePage.setEmailsubject(fakerMails.getFakeEmailSubject());
		new_email_templatePage.setTemplate(fakerMails.getFakeEmailBody(5));
		new_email_templatePage.setShare(false);

		EmailTemplatePage emailTemplatePage = new EmailTemplatePage();
		emailTemplatePage.setNew_email_template(new_email_templatePage);
		Response response = RestClient.doPost("JSON", albatrossURL, "email-templates", ThreadManager.getOwnerAlbatrossToken(), null,
				true, emailTemplatePage);
		JsonPath jp = response.jsonPath();
		templateTitle = jp.get("data.template.emailcontext");
		templateSubject = jp.get("data.template.emailsubject");
		templateBody = jp.get("data.template.template");
	}

	public ArrayList<Integer> getEnrollment() {

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getCandResponse = albatrossFunctions.getCandidateResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(),
				candidateEntitySlug);

		// Verify Response using Assertion and Jsonpath
		JsonPath jpCand = getCandResponse.jsonPath();
		JsonPath jsonCandidate1 = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug1 = jsonCandidate1.get("slug");

		AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();
		Response getCandResponse1 = albatrossFunctions1.getCandidateResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(),
				candidateEntitySlug1);

		// Verify Response using Assertion and Jsonpath
		JsonPath jpCand1 = getCandResponse1.jsonPath();

		int candID1 = jpCand.get("data.candidate.id");
		int candID2 = jpCand1.get("data.candidate.id");
		candList = new ArrayList<Integer>();
		candList.add(candID1);
		candList.add(candID2);

		return candList;
	}

	@DataProvider
	public Object[][] getSeqId() {

		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();

		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
		sequenceSetting.setThread_emails_as_replies(1);
		sequenceSetting.setExecute_step_on_business_days(1);
		JSONObject settings = new JSONObject(sequenceSetting);

		createEmailSequence.setEntity_type(5);
		createEmailSequence.setSeq_title("candidate" + " add sequence test " + generatedString);
		createEmailSequence.setSeq_settings(settings.toString());
		createEmailSequence.setSilent_progress(false);
		createEmailSequence.setSave_steps(0);

		Response sequenceResponse = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
				createEmailSequence);
		
		sequenceResponse.then().statusCode(200);

		JsonPath jp = sequenceResponse.jsonPath();
		seqId = jp.get("data.id");

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		createEmailTemplate("candidate", "5");
		CreateEmailStepToSequencePage createEmailStepToSequence = new CreateEmailStepToSequencePage();
		createEmailStepToSequence.setStep_no(1);
		createEmailStepToSequence.setNo_of_days(2);
		createEmailStepToSequence.setTemplate_title(templateTitle);
		createEmailStepToSequence.setTemplate_subject(templateSubject);
		createEmailStepToSequence.setTemplate_content(templateBody);
		createEmailStepToSequence.setInclude_opt_out_link(1);
		createEmailStepToSequence.setTime(3600);
		createEmailStepToSequence.setType(1);
		createEmailStepToSequence.setUpdate_type("all");
		ArrayList<Object> emailStep = new ArrayList<>();
		emailStep.add(createEmailStepToSequence);
		AddEmailStepsToSequencePage addEmailStep = new AddEmailStepsToSequencePage();
		addEmailStep.setSteps(emailStep);

		String basePath = "email-sequences/{id}/steps";
		Response responseEmailStep = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters,
				true, addEmailStep);

		responseEmailStep.then().statusCode(200);

		JsonPath jpEmailStep = responseEmailStep.jsonPath();
		emailStepId = jpEmailStep.get("data[0].id");

		getEnrollment();

		String basePath1 = "enrollments";
		UpdateStepsInEnrollmentPage updateStepsInEnrollment = new UpdateStepsInEnrollmentPage();
		updateStepsInEnrollment.setType(1);
		updateStepsInEnrollment.setInclude_opt_out_link(1);
		updateStepsInEnrollment.setTemplate_content(templateBody);
		updateStepsInEnrollment.setTemplate_subject(templateSubject);
		updateStepsInEnrollment.setTemplate_title(templateTitle);
		updateStepsInEnrollment.setUpdate_type("all");
		updateStepsInEnrollment.setSeq_step_details_id(emailStepId);
		updateStepsInEnrollment.setTime(3600);
		updateStepsInEnrollment.setNo_of_days(2);

		ArrayList<Object> steps = new ArrayList<Object>();
		steps.add(updateStepsInEnrollment);

		EnrollInSequencePage enrollInSequence = new EnrollInSequencePage();
		enrollInSequence.setId(seqId);
		enrollInSequence.setStart_at_step(1);
		enrollInSequence.setEnrollments(candList);
		enrollInSequence.setSteps(steps);

		Response responseEnroll = RestClient.doPost("JSON", nymaURL, basePath1, ThreadManager.getOwnerAlbatrossToken(), null, true,
				enrollInSequence);

		responseEnroll.then().statusCode(200);

		Map<String, String> pathParams = new HashMap<String, String>();
		pathParams.put("id", String.valueOf(seqId));

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getUsers = albatrossFunctions.getUsers(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
		JsonPath jpuser = getUsers.jsonPath();
		userId = jpuser.get("data.records[0].id");

		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("page", "1");
		queryParams.put("limit", "1");
		queryParams.put("status", "0");
		queryParams.put("enrolled_by[]", String.valueOf(userId));

		String basePathGetEnroll = "email-sequences/{id}/enrollments";

		Response responseEnrollId = RestClient.doGet("JSON", nymaURL, basePathGetEnroll, ThreadManager.getOwnerAlbatrossToken(), queryParams,
				pathParams, true);
		
		responseEnrollId.then().statusCode(200);

		JsonPath jpEnrollId = responseEnrollId.jsonPath();
		enrollmentId = jpEnrollId.get("data.enrollments[0].id");

		Object data[][] = { { seqId, enrollmentId } };
		return data;
	}

}
