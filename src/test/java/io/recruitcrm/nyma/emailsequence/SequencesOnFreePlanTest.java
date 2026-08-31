package io.recruitcrm.nyma.emailsequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerMails;
import io.rcrm.api.pojo.nyma.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.albatross.EmailTemplatePage;
import io.rcrm.api.pojo.albatross.New_email_templatePage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class SequencesOnFreePlanTest extends TestBase {

	String generatedString = RandomStringUtils.randomAlphabetic(4);
	JavaFakerMails fakerMails = new JavaFakerMails();
	String templateTitle;
	String templateSubject;
	String templateBody;
	int candidateSeqId;
	int candidateSeqStep2Id;
	int candidateSeqStep1Id;
	int candidateEnrollmentId;
	int candidateEnrollmentStepId;
	int userId;
	ArrayList<Integer> candidateList;


	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createEmailSequence() {

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

		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
				createEmailSequence);

		JsonPath jp = response.jsonPath();
		candidateSeqId = jp.get("data.id");

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));
		response.then().body("action_name", Matchers.containsString("Add Sequence"));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void editEmailSequence() {

		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();

		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
		sequenceSetting.setThread_emails_as_replies(1);
		sequenceSetting.setThread_emails_as_replies(1);
		JSONObject settings = new JSONObject(sequenceSetting);

		createEmailSequence.setSeq_title("candidate" + " add sequence test Updated" + generatedString);
		createEmailSequence.setSeq_settings(settings.toString());
		createEmailSequence.setSilent_progress(false);
		createEmailSequence.setSave_steps(0);

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateSeqId));

		Response response = RestClient.doPost1("JSON", nymaURL, "email-sequences/{id}", ThreadManager.getOwnerAlbatrossToken(), null,
				pathParameters, true, createEmailSequence);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));
		response.then().body("action_name", Matchers.containsString("Update Sequence"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void addEmailStepToSequence() {

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

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("id", String.valueOf(candidateSeqId));

		String basePath = "email-sequences/{id}/steps";
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, queryParameters, true,
				addEmailStep);


		JsonPath jp = response.jsonPath();

		candidateSeqStep1Id = jp.get("data[0].id");

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Save sequence steps"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void addTaskStepToSequence() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateSeqId));

		String basePath = "email-sequences/{id}/steps";

		CreateTaskStepToSequencePage createTaskStepToSequence = new CreateTaskStepToSequencePage();
		createTaskStepToSequence.setNo_of_days(2);
		createTaskStepToSequence.setStep_no(2);
		createTaskStepToSequence.setTime(3600);
		createTaskStepToSequence.setType(2);
		createTaskStepToSequence.setReminder(30);
		createTaskStepToSequence.setTask_title("Task step" + generatedString);
		createTaskStepToSequence.setTask_description("Task remainder in sequence" + generatedString);
		createTaskStepToSequence.setUpdate_type("all");
		ArrayList<Object> taskStep = new ArrayList<>();
		taskStep.add(createTaskStepToSequence);
		AddTaskStepsToSequencePage addTaskSteps = new AddTaskStepsToSequencePage();
		addTaskSteps.setSteps(taskStep);
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				addTaskSteps);


		JsonPath jp = response.jsonPath();

		candidateSeqStep2Id = jp.get("data[0].id");

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Save sequence steps"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void addLinkedInStepToSequence() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateSeqId));

		String basePath = "email-sequences/{id}/steps";

		CreateLinkedInStepToSequencePage createLinkedInStepToSequence = new CreateLinkedInStepToSequencePage();
		createLinkedInStepToSequence.setTime(36000);
		createLinkedInStepToSequence.setNo_of_days("2");
		createLinkedInStepToSequence.setUpdate_type("all");
		createLinkedInStepToSequence.setLinkedin_template_title("LinkedIn step" + generatedString);
		createLinkedInStepToSequence.setLinkedin_template_content("LinkedIn remainder in sequence" + generatedString);
		createLinkedInStepToSequence.setStep_no(3);
		createLinkedInStepToSequence.setType(4);
		createLinkedInStepToSequence.setEmail_sms_linkedin_step_cnt(2);


		ArrayList<Object> step = new ArrayList<>();
		step.add(createLinkedInStepToSequence);
		AddTaskStepsToSequencePage addTaskSteps = new AddTaskStepsToSequencePage();
		addTaskSteps.setSteps(step);

		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				addTaskSteps);

		response.then().statusCode(200);
		response.then().body("action_name", Matchers.containsString("Save sequence steps"));
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("Failed To save sequence steps : Linkedin steps are not allowed"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getEmailSequence() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("req_from", "1");
		String basePath = "email-sequences";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Searched successfully"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getEmailSequenceById() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateSeqId));

		String basePath = "email-sequences/{id}";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence fetched successfully."));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getEmailSequenceStats() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateSeqId));

		String basePath = "email-sequences/{id}/stats";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Stats"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void DeleteStepInSequence() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateSeqId));
		pathParameters.put("step_id", String.valueOf(candidateSeqStep2Id));

		String basePath = "email-sequences/{id}/steps/{step_id}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Delete Sequence Step Successful "));
		response.then().body("action_name", Matchers.containsString("Delete Sequence Step"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void cloneSequence() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateSeqId));

		String basePath = "email-sequences/{id}/clone";
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				null);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString(
				"Failed To Clone Sequence : You have reached the maximum number of sequences allowed."));
		response.then().body("message", Matchers.containsString("Please upgrade to business plan to create new ones."));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void createSecondSequence() {

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
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers
				.containsString("Failed To Add Sequence : You have reached the maximum number of sequences allowed."));
		response.then().body("message", Matchers.containsString("Please upgrade to business plan to create new ones."));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void DeleteSequenceTest() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateSeqId));

		String basePath = "email-sequences/{id}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Delete Sequence Successful "));
		response.then().body("action_name", Matchers.containsString("Delete Sequence"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void validateEnrollments() {

		getEnrollmentEntityId();

		String basePath = "enrollments/validate";

		ValidateEnrollmentsPage validateEnrollments = new ValidateEnrollmentsPage();
		validateEnrollments.setEnrollments(candidateList);
		validateEnrollments.setEntity_type(5);
		Response response = RestClient.doPost("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, validateEnrollments);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Validate Prospects Successful "));
		response.then().body("action_name", Matchers.containsString("Validate Prospects"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void enrollToSequence() {

		String basePath = "enrollments";
		UpdateStepsInEnrollmentPage updateStepsInEnrollment = new UpdateStepsInEnrollmentPage();

		ArrayList<Object> steps = new ArrayList<Object>();

		updateStepsInEnrollment.setType(1);
		updateStepsInEnrollment.setStep_no(1);
		updateStepsInEnrollment.setInclude_opt_out_link(1);
		updateStepsInEnrollment.setTemplate_content(templateBody);
		updateStepsInEnrollment.setTemplate_subject(templateSubject);
		updateStepsInEnrollment.setTemplate_title(templateTitle);
		updateStepsInEnrollment.setUpdate_type("all");
		updateStepsInEnrollment.setSeq_step_details_id(candidateSeqStep1Id);
		updateStepsInEnrollment.setTime(3600);
		updateStepsInEnrollment.setNo_of_days(2);

		steps.add(updateStepsInEnrollment);

		EnrollInSequencePage enrollInSequence = new EnrollInSequencePage();
		enrollInSequence.setId(candidateSeqId);
		enrollInSequence.setStart_at_step(1);
		enrollInSequence.setEnrollments(candidateList);
		enrollInSequence.setSteps(steps);

		Response response = RestClient.doPost("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, enrollInSequence);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Enroll in a Sequence Successful "));
		response.then().body("action_name", Matchers.containsString("Enroll in a Sequence"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getEnrollmentsOfSequence() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateSeqId));

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
		candidateEnrollmentId = jp1.get("data.enrollments[0].id");
		candidateEnrollmentStepId = jp1.getInt("data.enrollments[0].steps[0].id");

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Enrollments Fetched successfully"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void updateStepByEnrollmentId() {

		String basePath = "enrollments/{id}";
		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateEnrollmentId));

		UpdateStepsInEnrollmentPage updateStepsInEnrollment = new UpdateStepsInEnrollmentPage();

		ArrayList<Object> steps = new ArrayList<Object>();

		updateStepsInEnrollment.setId(candidateEnrollmentStepId);
		updateStepsInEnrollment.setType(1);
		updateStepsInEnrollment.setInclude_opt_out_link(1);
		updateStepsInEnrollment.setTemplate_content(templateBody+" update");
		updateStepsInEnrollment.setTemplate_subject(templateSubject+" update");
		updateStepsInEnrollment.setTemplate_title(templateTitle+" update");
		updateStepsInEnrollment.setUpdate_type("all");
		updateStepsInEnrollment.setSeq_step_details_id(candidateSeqStep1Id);
		updateStepsInEnrollment.setTime(3600);
		updateStepsInEnrollment.setNo_of_days(2);

		steps.add(updateStepsInEnrollment);

		EnrollInSequencePage enrollInSequence = new EnrollInSequencePage();
		enrollInSequence.setSteps(steps);

		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				enrollInSequence);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Enrollment Updated Successfully"));
		response.then().body("action_name", Matchers.containsString("Update Enrollment"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getEnrollmentDetails() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateEnrollmentId));

		String basePath = "enrollments/{id}";

		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Enrollment Details Fetched successfully"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void unEnrollInSequence() {

		String basePath = "email-sequences/{id}/un-enroll";

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateSeqId));

		ArrayList<Integer> unEnrollmentsId = new ArrayList<Integer>();
		unEnrollmentsId.add(candidateEnrollmentId);

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
	@Test(groups = "nightly-build")
	public void unEnrollAllInSequence() {

		String basePath = "email-sequences/{id}/un-enroll/all";

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateSeqId));

		UnEnrollInSequencePage unEnrollInSequence = new UnEnrollInSequencePage();
		unEnrollInSequence.setFollowup_task(false);

		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				unEnrollInSequence);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("un-enrolled successfully"));
		response.then().body("action_name", Matchers.containsString("Un-enroll Records from Sequence"));

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

	public void getEnrollmentEntityId() {

		AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();
		Response getCandResponse1 = albatrossFunctions1.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
		JsonPath jpCand1 = getCandResponse1.jsonPath();

		AllCrudFunctions albatrossFunctions2 = new AllCrudFunctions();
		Response getCandResponse2 = albatrossFunctions2.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
		JsonPath jpCand2 = getCandResponse2.jsonPath();

		int candID1 = jpCand1.get("data.candidate.id");
		int candID2 = jpCand2.get("data.candidate.id");
		ArrayList<Integer> candList = new ArrayList<Integer>();
		candList.add(candID1);
		candList.add(candID2);

		candidateList = candList;
	}

}
