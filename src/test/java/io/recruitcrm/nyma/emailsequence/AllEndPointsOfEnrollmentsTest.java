package io.recruitcrm.nyma.emailsequence;

import java.util.*;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerMails;
import io.rcrm.api.pojo.nyma.*;
import io.rcrm.api.pojo.reaper.UpdateEntityRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.EmailTemplatePage;
import io.rcrm.api.pojo.albatross.New_email_templatePage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email1|Email2")
public class AllEndPointsOfEnrollmentsTest extends TestBase {

	commanFunction function = new commanFunction();
	JavaFakerMails fakerMails = new JavaFakerMails();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	String templateTitle;
	String templateSubject;
	String templateBody;
	int candidateSeqId;
	int contactSeqId;
	int candidateSeqStepId;
	int contactSeqStepId;
	int candidateEnrollmentId;
	int contactEnrollmentId;
	int candidateEnrollmentStepId;
	int contactEnrollmentStepId;
	int userId;
	int pausedLinkedEmail;
	String albatrossToken;

	@BeforeClass
	public void setUp() {
		albatrossToken = ThreadManager.getOwnerAlbatrossToken();
		userId = ThreadManager.getOwner().getUserId();
	}

	@Owner("Harika")
	@Test(dataProvider = "getSeqId", priority = 0, groups = "nightly-build")
	public void createEmailSequence(String entityType, int relatedToTypeId, int seqId) {

		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();

		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
		sequenceSetting.setThread_emails_as_replies(1);
		sequenceSetting.setExecute_step_on_business_days(1);

		JSONObject settings = new JSONObject(sequenceSetting);

		createEmailSequence.setEntity_type(relatedToTypeId);
		createEmailSequence.setSeq_title(entityType + " add sequence test " + generatedString);
		createEmailSequence.setSeq_settings(settings.toString());
		createEmailSequence.setSilent_progress(false);
		createEmailSequence.setSave_steps(0);

		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", albatrossToken, null, true,
				createEmailSequence);

		JsonPath jp = response.jsonPath();
		if (relatedToTypeId == 2) {
			contactSeqId = jp.get("data.id");
		} else {
			candidateSeqId = jp.get("data.id");
		}
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));
		response.then().body("action_name", Matchers.containsString("Add Sequence"));
	}

	@Owner("Harika")
	@Test(dataProvider = "getSeqId", priority = 1, groups = "nightly-build")
	public void addEmailStepToSequence(String entityType, int relatedToTypeId, int seqId) {

		createEmailTemplate(entityType,Integer.toString(relatedToTypeId));
		CreateEmailStepToSequencePage createEmailStepToSequence = new CreateEmailStepToSequencePage();
		createEmailStepToSequence.setStep_no(1);
		createEmailStepToSequence.setNo_of_days(2);
		createEmailStepToSequence.setTemplate_title(templateTitle);
		createEmailStepToSequence.setTemplate_subject(templateSubject);
		createEmailStepToSequence.setTemplate_content(templateBody);
		createEmailStepToSequence.setTime(3600);
		createEmailStepToSequence.setType(1);
		createEmailStepToSequence.setInclude_opt_out_link(1);
		createEmailStepToSequence.setUpdate_type("all");
		ArrayList<Object> emailStep = new ArrayList<>();
		emailStep.add(createEmailStepToSequence);
		AddEmailStepsToSequencePage addEmailStep = new AddEmailStepsToSequencePage();
		addEmailStep.setSteps(emailStep);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/steps";
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, albatrossToken, null, queryParameters, true,
				addEmailStep);


		JsonPath jp = response.jsonPath();
		if (relatedToTypeId == 2) {
			contactSeqStepId = jp.get("data[0].id");
		} else {
			candidateSeqStepId = jp.get("data[0].id");
		}

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));


	}

	@Owner("Harika")
	@Test(dataProvider = "getEnrollmentDetails", priority = 2, groups = "nightly-build")
	public void validateEnrollments(int relatedToTypeId, int seqId, int stepId, ArrayList<Integer> list,
			int enrollmentId, int enrollmentStepId) {

		String basePath = "enrollments/validate";

		ValidateEnrollmentsPage validateEnrollments = new ValidateEnrollmentsPage();
		validateEnrollments.setEnrollments(list);
		validateEnrollments.setEntity_type(relatedToTypeId);
		ValidateEnrollmentsPage.StepContains stepContains = new ValidateEnrollmentsPage.StepContains();
		stepContains.setTask(1);
		stepContains.setEmail(1);
		stepContains.setSms(1);
		validateEnrollments.setStep_contains(stepContains);
		Response response = RestClient.doPost("JSON", nymaURL, basePath, albatrossToken, null, true, validateEnrollments);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Validate Prospects Successful "));
		response.then().body("action_name", Matchers.containsString("Validate Prospects"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getEnrollmentDetails", priority = 3, groups = "nightly-build")
	public void enrollToSequence(int relatedToTypeId, int seqId, int stepId, ArrayList<Integer> list, int enrollmentId,
			int enrollmentStepId) {

		String basePath = "enrollments";
		UpdateStepsInEnrollmentPage updateStepsInEnrollment = new UpdateStepsInEnrollmentPage();

		ArrayList<Object> steps = new ArrayList<Object>();

		updateStepsInEnrollment.setType(1);
		updateStepsInEnrollment.setInclude_opt_out_link(1);
		updateStepsInEnrollment.setTemplate_content(templateBody);
		updateStepsInEnrollment.setTemplate_subject(templateSubject);
		updateStepsInEnrollment.setTemplate_title(templateTitle);
		updateStepsInEnrollment.setUpdate_type("all");
		updateStepsInEnrollment.setSeq_step_details_id(stepId);
		updateStepsInEnrollment.setTime(3600);
		updateStepsInEnrollment.setNo_of_days(2);

		steps.add(updateStepsInEnrollment);

		EnrollInSequencePage enrollInSequence = new EnrollInSequencePage();
		enrollInSequence.setId(seqId);
		enrollInSequence.setStart_at_step(1);
		enrollInSequence.setEnrollments(list);
		enrollInSequence.setSteps(steps);
		enrollInSequence.setLinked_email_type(2);

		Response response = RestClient.doPost("JSON", nymaURL, basePath, albatrossToken, null, true, enrollInSequence);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Enroll in a Sequence Successful "));
		response.then().body("action_name", Matchers.containsString("Enroll in a Sequence"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getSeqId", priority = 4, groups = "nightly-build")
	public void getEnrollmentsOfSequence(String entityType, int relatedToTypeId, int seqId) {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("status", "0");
		queryParameters.put("enrolled_by[]", String.valueOf(userId));

		String basePath = "email-sequences/{id}/enrollments";

		Response response = RestClient.doGet("JSON", nymaURL, basePath, albatrossToken, queryParameters, pathParameters,
				true);

		JsonPath jp1 = response.jsonPath();
		if (relatedToTypeId == 2) {
			contactEnrollmentId = jp1.get("data.enrollments[0].id");
			contactEnrollmentStepId = jp1.getInt("data.enrollments[0].steps[0].id");
		} else {
			candidateEnrollmentId = jp1.get("data.enrollments[0].id");
			candidateEnrollmentStepId = jp1.getInt("data.enrollments[0].steps[0].id");
		}

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Enrollments Fetched successfully"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getEnrollmentDetails", priority = 5, groups = "nightly-build")
	public void updateStepByEnrollmentId(int relatedToTypeId, int seqId, int stepId, ArrayList<Integer> list,
			int enrollmentId, int enrollmentStepId) {

		String basePath = "enrollments/{id}";
		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(enrollmentId));

		UpdateStepsInEnrollmentPage updateStepsInEnrollment = new UpdateStepsInEnrollmentPage();

		ArrayList<Object> steps = new ArrayList<Object>();

		updateStepsInEnrollment.setType(1);
		updateStepsInEnrollment.setInclude_opt_out_link(1);
		updateStepsInEnrollment.setTemplate_content(templateBody+" update");
		updateStepsInEnrollment.setTemplate_subject(templateSubject+" update");
		updateStepsInEnrollment.setTemplate_title(templateTitle+" update");
		updateStepsInEnrollment.setUpdate_type("all");
		updateStepsInEnrollment.setTime(4000);
		updateStepsInEnrollment.setNo_of_days(3);
		updateStepsInEnrollment.setStep_no(1);
		updateStepsInEnrollment.setId(enrollmentStepId);

		steps.add(updateStepsInEnrollment);

		EnrollInSequencePage enrollInSequence = new EnrollInSequencePage();
		enrollInSequence.setSteps(steps);

		Response response = RestClient.doPost1("JSON", nymaURL, basePath, albatrossToken, null, pathParameters, true,
				enrollInSequence);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Enrollment Updated Successfully"));
		response.then().body("action_name", Matchers.containsString("Update Enrollment"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getEnrollmentId", priority = 6, groups = "nightly-build")
	public void getEnrollmentDetails(int seqId, int enrollmentId) {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(enrollmentId));

		String basePath = "enrollments/{id}";

		Response response = RestClient.doGet("JSON", nymaURL, basePath, albatrossToken, null, pathParameters, true);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message",
				Matchers.containsString("Sequence Enrollment Details Fetched successfully"));
		response.then().body("data.linked_email_type", Matchers.either(Matchers.equalTo(1)).or(Matchers.equalTo(2)));
	}

	@Owner("Harika")
	@Test(dataProvider = "getEnrollmentId", priority = 7, groups = "nightly-build")
	public void unEnrollInSequence(int seqId, int enrollmentId) {

		String basePath = "email-sequences/{id}/un-enroll";

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		ArrayList<Integer> unEnrollmentsId = new ArrayList<Integer>();
		unEnrollmentsId.add(enrollmentId);

		UnEnrollInSequencePage unEnrollInSequence = new UnEnrollInSequencePage();
		unEnrollInSequence.setEnrollments(unEnrollmentsId);
		unEnrollInSequence.setFollowup_task(false);

		Response response = RestClient.doPost1("JSON", nymaURL, basePath, albatrossToken, null, pathParameters, true,
				unEnrollInSequence);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("un-enrolled successfully"));
		response.then().body("action_name", Matchers.containsString("Un-enroll Records from Sequence"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getEnrollmentId", priority = 8, groups = "nightly-build")
	public void unEnrollAllInSequence(int seqId, int enrollmentId) {

		String basePath = "email-sequences/{id}/un-enroll/all";

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		UnEnrollInSequencePage unEnrollInSequence = new UnEnrollInSequencePage();
		unEnrollInSequence.setFollowup_task(false);

		Response response = RestClient.doPost1("JSON", nymaURL, basePath, albatrossToken, null, pathParameters, true,
				unEnrollInSequence);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("un-enrolled successfully"));
		response.then().body("action_name", Matchers.containsString("Un-enroll Records from Sequence"));

	}

	@Owner("Harika")
	@Test(priority = 9, groups = "nightly-build")
	public void getPausedEnrollments(){

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");

		int candID1 = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("candidate", candidateEntitySlug)
				.getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
		ArrayList<Integer> candList = new ArrayList<Integer>();
		candList.add(candID1);

		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();

		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
		sequenceSetting.setThread_emails_as_replies(1);
		sequenceSetting.setExecute_step_on_business_days(1);

		JSONObject settings = new JSONObject(sequenceSetting);

		createEmailSequence.setEntity_type(5);
		createEmailSequence.setSeq_title(5 + " add sequence test " + generatedString);
		createEmailSequence.setSeq_settings(settings.toString());
		createEmailSequence.setSilent_progress(false);
		createEmailSequence.setSave_steps(0);

		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", albatrossToken, null, true,
				createEmailSequence);

		JsonPath jp = response.jsonPath();

		int candidateSeqId = jp.get("data.id");

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));

		createEmailTemplate("candidate",Integer.toString(5));
		CreateEmailStepToSequencePage createEmailStepToSequence = new CreateEmailStepToSequencePage();
		createEmailStepToSequence.setStep_no(1);
		createEmailStepToSequence.setNo_of_days(2);
		createEmailStepToSequence.setTemplate_title(templateTitle);
		createEmailStepToSequence.setTemplate_subject(templateSubject);
		createEmailStepToSequence.setTemplate_content(templateBody);
		createEmailStepToSequence.setTime(3600);
		createEmailStepToSequence.setType(1);
		createEmailStepToSequence.setInclude_opt_out_link(1);
		createEmailStepToSequence.setUpdate_type("all");
		ArrayList<Object> emailStep = new ArrayList<>();
		emailStep.add(createEmailStepToSequence);
		AddEmailStepsToSequencePage addEmailStep = new AddEmailStepsToSequencePage();
		addEmailStep.setSteps(emailStep);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("id", String.valueOf(candidateSeqId));

		String basePath = "email-sequences/{id}/steps";
		Response response1 = RestClient.doPost1("JSON", nymaURL, basePath, albatrossToken, null, queryParameters, true,
				addEmailStep);


		JsonPath jp1 = response1.jsonPath();
		int candidateSeqStepId = jp1.get("data[0].id");

		response1.then().statusCode(200);
		response1.then().body("status", Matchers.containsString("success"));

		String basePath1 = "enrollments";
		UpdateStepsInEnrollmentPage updateStepsInEnrollment = new UpdateStepsInEnrollmentPage();

		ArrayList<Object> steps = new ArrayList<Object>();

		updateStepsInEnrollment.setType(1);
		updateStepsInEnrollment.setInclude_opt_out_link(1);
		updateStepsInEnrollment.setTemplate_content(templateBody);
		updateStepsInEnrollment.setTemplate_subject(templateSubject);
		updateStepsInEnrollment.setTemplate_title(templateTitle);
		updateStepsInEnrollment.setUpdate_type("all");
		updateStepsInEnrollment.setSeq_step_details_id(candidateSeqStepId);
		updateStepsInEnrollment.setTime(3600);
		updateStepsInEnrollment.setNo_of_days(2);

		steps.add(updateStepsInEnrollment);

		EnrollInSequencePage enrollInSequence = new EnrollInSequencePage();
		enrollInSequence.setId(candidateSeqId);
		enrollInSequence.setStart_at_step(1);
		enrollInSequence.setEnrollments(candList);
		enrollInSequence.setSteps(steps);
		enrollInSequence.setLinked_email_type(2);

		Response response2 = RestClient.doPost("JSON", nymaURL, basePath1, albatrossToken, null, true, enrollInSequence);


		response2.then().statusCode(200);
		response2.then().body("status", Matchers.containsString("success"));

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateSeqId));

		Map<String, String> queryParameters1 = new HashMap<String, String>();
		queryParameters1.put("page", "1");
		queryParameters1.put("limit", "1");
		queryParameters1.put("status", "0");
		queryParameters1.put("enrolled_by[]", String.valueOf(userId));

		String basePath2 = "email-sequences/{id}/enrollments";

		Response response3 = RestClient.doGet("JSON", nymaURL, basePath2, albatrossToken, queryParameters1, pathParameters,
				true);
		response3.then().statusCode(200);
		response3.then().body("status", Matchers.containsString("success"));

		JsonPath jp3 = response3.jsonPath();
		int candidateEnrollmentId = jp3.get("data.enrollments[0].id");


		pauseEnrollment(candidateEnrollmentId);

		Map<String, String> queryParameters2 = new HashMap<>();
		queryParameters2.put("linked_email_type[]", "1");
		queryParameters2.put("linked_email_type[]", "2");

		String basePath3 = "email-sequences/paused-records";
		Response response4 = RestClient.doGet("JSON", nymaURL, basePath3, albatrossToken, queryParameters2, null, true);
		response4.then().statusCode(200);
		response4.then().body("status", Matchers.containsString("success"));
		response4.then().body("message_type", Matchers.containsString("is-success"));
		response4.then().body("message", Matchers.containsString("Paused enrollments fetched successfully"));
		response4.then().body("data.paused_linked_email[0]", Matchers.either(Matchers.equalTo("1")).or(Matchers.equalTo("2")));

		pausedLinkedEmail = Integer.parseInt(response4.jsonPath().get("data.paused_linked_email[0]"));
	}

	@Owner("Ajendra Singh")
	@Test(priority = 10, groups = "nightly-build")
	public void resumeEnrollment(){
		String basePath = "enrollments/resume";
		ArrayList<Integer> linkedEmailType = new ArrayList<>();
		linkedEmailType.add(pausedLinkedEmail);
		ResumeEnrollmentsPage resumeEnrollmentsPage = new ResumeEnrollmentsPage(1, linkedEmailType);
		Response response = RestClient.doPost("JSON", nymaURL, basePath, albatrossToken, null, true, resumeEnrollmentsPage);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Resume Enrollments Successful"));
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
		Response response = RestClient.doPost("JSON", albatrossURL, "email-templates", albatrossToken, null,
				true, emailTemplatePage);
		JsonPath jp = response.jsonPath();
		templateTitle = jp.get("data.template.emailcontext");
		templateSubject = jp.get("data.template.emailsubject");
		templateBody = jp.get("data.template.template");
	}

	@DataProvider
	public Object[][] getEnrollmentDetails() {

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");
		ReaperIntegration.provideSmsConsentToEntity(ThreadManager.getAccount().getAccountId(), "Candidate", candidateEntitySlug);

		JsonPath jsonCandidate1 = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug1 = jsonCandidate1.get("slug");
		ReaperIntegration.provideSmsConsentToEntity(ThreadManager.getAccount().getAccountId(), "Candidate", candidateEntitySlug1);

		ReaperIntegration.updateEntityColumns(candidateEntitySlug, new UpdateEntityRequest("candidate", Map.of("contactnumber", "+17862337361")));
		ReaperIntegration.updateEntityColumns(candidateEntitySlug1, new UpdateEntityRequest("candidate", Map.of("contactnumber", "+12512630796")));

		int candID1 = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("candidate", candidateEntitySlug)
				.getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
		int candID2 = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("candidate", candidateEntitySlug1)
				.getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
		ArrayList<Integer> candList = new ArrayList<Integer>();
		candList.add(candID1);
		candList.add(candID2);

		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String companyEntitySlug = jsonCompany.get("slug");
		JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companyEntitySlug)
				.jsonPath();

		String contactEntitySlug = jsonContact.get("slug");
		ReaperIntegration.provideSmsConsentToEntity(ThreadManager.getAccount().getAccountId(), "Contact", contactEntitySlug);
		ReaperIntegration.updateEntityColumns(contactEntitySlug, new UpdateEntityRequest("contact", Map.of("contactnumber", "+17862337361")));

		JsonPath jsonCompany1 = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String companyEntitySlug1 = jsonCompany1.get("slug");
		JsonPath jsonContact1 = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companyEntitySlug1)
				.jsonPath();

		String contactEntitySlug1 = jsonContact1.get("slug");
		ReaperIntegration.provideSmsConsentToEntity(ThreadManager.getAccount().getAccountId(), "Contact", contactEntitySlug1);
		ReaperIntegration.updateEntityColumns(contactEntitySlug1, new UpdateEntityRequest("contact", Map.of("contactnumber", "+12512630796")));

		int contactID1 = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("contact", contactEntitySlug)
				.getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
		int contactID2 = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("contact", contactEntitySlug1)
				.getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
		ArrayList<Integer> contactList = new ArrayList<Integer>();
		contactList.add(contactID1);
		contactList.add(contactID2);

		Object data[][] = {
				{ 5, candidateSeqId, candidateSeqStepId, candList, candidateEnrollmentId, candidateEnrollmentStepId },
				{ 2, contactSeqId, contactSeqStepId, contactList, contactEnrollmentId, contactEnrollmentStepId } };
		return data;
	}

	@DataProvider
	public Object[][] getSeqId() {

		Object data[][] = { { "candidate", 5, candidateSeqId }, { "contact", 2, contactSeqId } };
		return data;
	}

	@DataProvider
	public Object[][] getEnrollmentId() {

		Object data[][] = { { candidateSeqId, candidateEnrollmentId }, { contactSeqId, contactEnrollmentId } };
		return data;
	}

}
