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
import io.rcrm.api.pojo.nyma.UpdateStepsInEnrollmentPage;
import io.rcrm.api.pojo.nyma.ValidateEnrollmentsPage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class GetEnrollmentsTest extends TestBase {

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
	public void getMyEnrollments(int seqId) {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getUsers = albatrossFunctions.getUsers(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
		JsonPath jp = getUsers.jsonPath();
		userId = jp.get("data.records[0].id");

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("status", "0");
		queryParameters.put("enrolled_by[]", String.valueOf(userId));

		String basePath = "email-sequences/{id}/enrollments";

		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, pathParameters,
				true);

		JsonPath jp1 = response.jsonPath();
		enrollmentId = jp1.get("data.enrollments[0].id");

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Enrollments Fetched successfully"));

	}

	@Owner("Harika")
	@Test(priority = 1, groups = "nightly-build")
	public void getAllEnrollments() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getUsers = albatrossFunctions.getUsers(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
		JsonPath jp = getUsers.jsonPath();
		int arraySize = jp.getInt("data.records.size()");

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("status", "0");

		StringBuilder enrolledByParam = new StringBuilder();
		for (int i = 0; i < arraySize; i++) {
			userId = jp.get("data.records[" + i + "].id");
			if (enrolledByParam.length() > 0) {
				enrolledByParam.append("&enrolled_by[]=");
			}
			enrolledByParam.append(userId);
		}

		queryParameters.put("enrolled_by[]", enrolledByParam.toString());

		String basePath = "email-sequences/{id}/enrollments";

		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, pathParameters,
				true);

		JsonPath jp1 = response.jsonPath();
		enrollmentId = jp1.get("data.enrollments[0].id");

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Enrollments Fetched successfully"));

	}

	@Owner("Harika")
	@Test(priority = 2, groups = "nightly-build")
	public void getEnrollmentsInvalidId() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId + "123"));

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getUsers = albatrossFunctions.getUsers(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
		JsonPath jp = getUsers.jsonPath();
		userId = jp.get("data.records[0].id");

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("status", "0");
		queryParameters.put("enrolled_by[]", String.valueOf(userId));

		String basePath = "email-sequences/{id}/enrollments";

		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, pathParameters,
				true);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("Sequence does not exists."));
	}

	@Owner("Harika")
	@Test(priority = 3, groups = "nightly-build")
	public void getEnrollmentsInvalidParams() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId + "123"));

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getUsers = albatrossFunctions.getUsers(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
		JsonPath jp = getUsers.jsonPath();
		userId = jp.get("data.records[0].id");

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "");
		queryParameters.put("limit", "");
		queryParameters.put("status", "");
		queryParameters.put("enrolled_by[]", "");

		String basePath = "email-sequences/{id}/enrollments";

		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, pathParameters,
				true);

		response.then().statusCode(422);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString(
				"The page field is required.,The limit field is required.,The status field is required."));
	}

	@Owner("Harika")
	@Test(priority = 4, groups = "nightly-build")
	public void getEnrollmentsInvalidAuth() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getUsers = albatrossFunctions.getUsers(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
		JsonPath jp = getUsers.jsonPath();
		userId = jp.get("data.records[0].id");

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "");
		queryParameters.put("limit", "");
		queryParameters.put("status", "");
		queryParameters.put("enrolled_by[]", "");

		String basePath = "email-sequences/{id}/enrollments";

		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", queryParameters,
				pathParameters, true);

		response.then().statusCode(401);

	}

	@Owner("Harika")
	@Test(priority = 5, groups = "nightly-build")
	public void getEnrollmentDetails() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(enrollmentId));

		String basePath = "enrollments/{id}";

		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Enrollment Details Fetched successfully"));

	}

	@Owner("Harika")
	@Test(priority = 6, groups = "nightly-build")
	public void getEnrollmentDetailsInvalidId() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(enrollmentId + "123"));

		String basePath = "enrollments/{id}";

		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("Enrollment does not exists."));

	}

	@Owner("Harika")
	@Test(priority = 7, groups = "nightly-build")
	public void getEnrollmentDetails_422() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", "abc");

		String basePath = "enrollments/{id}";

		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);

		response.then().statusCode(422);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("The id must be an integer."));

	}

	@Owner("Harika")
	@Test(priority = 8, groups = "nightly-build")
	public void getEnrollmentDetailsInvalidAuth() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(enrollmentId));

		String basePath = "enrollments/{id}";

		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, pathParameters,
				true);

		response.then().statusCode(401);

	}

	public void createEmailTemplate(String templateForEntity, String relatedToTypeId) {

		New_email_templatePage new_email_templatePage = new New_email_templatePage();
		new_email_templatePage.setEmailcontext(templateForEntity + " Email Template " + generatedString);
		new_email_templatePage.setRelatedtotypeid(relatedToTypeId);
		new_email_templatePage.setEmailsubject(fakerMails.getRandomRecruitmentWord() + " " + fakerMails.getRandomRecruitmentWord());
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
		JSONObject settings = new JSONObject(sequenceSetting);

		createEmailSequence.setEntity_type(5);
		createEmailSequence.setSeq_title("candidate" + " add sequence test " + generatedString);
		createEmailSequence.setSeq_settings(settings.toString());
		createEmailSequence.setSilent_progress(false);

		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
				createEmailSequence);

		response.then().statusCode(200);

		JsonPath jp = response.jsonPath();
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

		Response responseEnroll = RestClient.doPost("JSON", nymaURL, "enrollments", ThreadManager.getOwnerAlbatrossToken(), null, true,
				enrollInSequence);

		responseEnroll.then().statusCode(200);

		Object data[][] = { { seqId } };
		return data;
	}

}
