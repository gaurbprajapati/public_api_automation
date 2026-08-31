package io.recruitcrm.nyma.emailsequence;

import java.util.*;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerMails;
import io.rcrm.api.pojo.nyma.*;
import io.rcrm.api.pojo.reaper.UpdateEntityRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class EnrollInsequenceTest extends TestBase {

	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	JavaFakerMails fakerMails = new JavaFakerMails();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	String templateTitle = "Email Template Title" + generatedString;
	String templateSubject = "Email Template Subject" + generatedString;
	String templateBody = "Email Template Body" + generatedString;
	int relatedToTypeId=5;
	int seqId;
	int taskStepId;
	int emailStepId;
	int smsStepId;
	ArrayList<Integer> candList;


	@Owner("Ajendra Singh")
	@Test(dataProvider = "getSeqId", priority = 0, groups = "nightly-build")
	public void validateEnrollments(int seqId, int taskstepId, int emailstepId, int smsStepId) {

		String basePath = "enrollments/validate";

		ValidateEnrollmentsPage validateEnrollments = new ValidateEnrollmentsPage();
		validateEnrollments.setEnrollments(candList);
		validateEnrollments.setEntity_type(relatedToTypeId);
		ValidateEnrollmentsPage.StepContains stepContains = new ValidateEnrollmentsPage.StepContains();
		stepContains.setTask(1);
		stepContains.setEmail(1);
		stepContains.setSms(1);
		validateEnrollments.setStep_contains(stepContains);
		Response response = RestClient.doPost("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, validateEnrollments);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Validate Prospects Successful "));
		response.then().body("action_name", Matchers.containsString("Validate Prospects"));

	}

	@Owner("Harika")
	@Test(priority = 1, groups = "nightly-build")
	public void enrollWithInvalidStepCount() {

		String basePath = "enrollments";
		UpdateStepsInEnrollmentPage updateStepsInEnrollment = new UpdateStepsInEnrollmentPage();
		updateStepsInEnrollment.setUpdate_type("settings");
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

		Response response = RestClient.doPost("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, enrollInSequence);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("Failed To Enroll in a Sequence : Steps count doesn't match with sequence steps."));
		response.then().body("action_name", Matchers.containsString("Enroll in a Sequence"));

	}
	
	@Owner("Harika")
	@Test(priority = 2, groups = "nightly-build")
	public void enrollToSequence() {

		String basePath = "enrollments";
		UpdateStepsInEnrollmentPage updateStepsInEnrollment1 = new UpdateStepsInEnrollmentPage();
		updateStepsInEnrollment1.setType(1);
		updateStepsInEnrollment1.setInclude_opt_out_link(1);
		updateStepsInEnrollment1.setTemplate_content(templateBody);
		updateStepsInEnrollment1.setTemplate_subject(templateSubject);
		updateStepsInEnrollment1.setTemplate_title(templateTitle);
		updateStepsInEnrollment1.setUpdate_type("settings");
		updateStepsInEnrollment1.setSeq_step_details_id(emailStepId);
		updateStepsInEnrollment1.setTime(3600);
		updateStepsInEnrollment1.setNo_of_days(2);

		ArrayList<Object> steps = new ArrayList<Object>();
		steps.add(updateStepsInEnrollment1);
		
		UpdateStepsInEnrollmentPage updateStepsInEnrollment2 = new UpdateStepsInEnrollmentPage();
		updateStepsInEnrollment2.setType(2);
		updateStepsInEnrollment2.setUpdate_type("settings");
		updateStepsInEnrollment2.setSeq_step_details_id(taskStepId);
		updateStepsInEnrollment2.setTime(3600);
		updateStepsInEnrollment2.setNo_of_days(2);
		updateStepsInEnrollment2.setReminder("30");
		updateStepsInEnrollment2.setTask_title("Task step enroll update" + generatedString);
		updateStepsInEnrollment2.setTask_description("Task remainder in sequence enroll update" + generatedString);

		steps.add(updateStepsInEnrollment2);

		UpdateStepsInEnrollmentPage updateStepsInEnrollment3 = new UpdateStepsInEnrollmentPage();
		updateStepsInEnrollment3.setType(3);
		updateStepsInEnrollment3.setInclude_opt_out_link(1);
		updateStepsInEnrollment3.setTemplate_content(templateBody);
		updateStepsInEnrollment3.setTemplate_title(templateTitle);
		updateStepsInEnrollment3.setUpdate_type("settings");
		updateStepsInEnrollment3.setSeq_step_details_id(smsStepId);
		updateStepsInEnrollment3.setTime(3600);
		updateStepsInEnrollment3.setNo_of_days(2);

		steps.add(updateStepsInEnrollment3);

		EnrollInSequencePage enrollInSequence = new EnrollInSequencePage();
		enrollInSequence.setId(seqId);
		enrollInSequence.setStart_at_step(1);
		enrollInSequence.setEnrollments(candList);
		enrollInSequence.setSteps(steps);

		Response response = RestClient.doPost("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, enrollInSequence);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Enroll in a Sequence Successful "));
		response.then().body("action_name", Matchers.containsString("Enroll in a Sequence"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getInvalidEnrollments", priority = 3, groups = "nightly-build")
	public void validateInvalidEnrollments(ArrayList<Integer> list) {

		String basePath = "enrollments/validate";

		ValidateEnrollmentsPage validateEnrollments = new ValidateEnrollmentsPage();
		validateEnrollments.setEnrollments(list);
		validateEnrollments.setEntity_type(relatedToTypeId);
		ValidateEnrollmentsPage.StepContains stepContains = new ValidateEnrollmentsPage.StepContains();
		stepContains.setTask(1);
		stepContains.setEmail(1);
		stepContains.setSms(1);
		validateEnrollments.setStep_contains(stepContains);
		Response response = RestClient.doPost("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, validateEnrollments);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("data.enrollments[0].message", Matchers.containsString("SMS Consent Pending"));
		response.then().body("data.enrollments[1].message", Matchers.containsString("Twilio validation error"));
		response.then().body("data.enrollments[2].message", Matchers.containsString("Opted Out of SMS,SMS Consent Pending"));
		response.then().body("message", Matchers.containsString("Failed To Validate Prospects : Prospect validation failed"));
		response.then().body("action_name", Matchers.containsString("Validate Prospects"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getInvalidEnrollments", priority = 4, groups = "nightly-build")
	public void enrollInvalidDataToSequence(ArrayList<Integer> list) {

		String basePath = "enrollments";
		UpdateStepsInEnrollmentPage updateStepsInEnrollment1 = new UpdateStepsInEnrollmentPage();
		updateStepsInEnrollment1.setUpdate_type("settings");
		updateStepsInEnrollment1.setSeq_step_details_id(emailStepId);
		updateStepsInEnrollment1.setTime(3600);
		updateStepsInEnrollment1.setNo_of_days(2);

		ArrayList<Object> steps = new ArrayList<Object>();
		steps.add(updateStepsInEnrollment1);

		UpdateStepsInEnrollmentPage updateStepsInEnrollment2 = new UpdateStepsInEnrollmentPage();

		updateStepsInEnrollment2.setUpdate_type("settings");
		updateStepsInEnrollment2.setSeq_step_details_id(taskStepId);
		updateStepsInEnrollment2.setTime(3600);
		updateStepsInEnrollment2.setNo_of_days(2);

		steps.add(updateStepsInEnrollment2);

		EnrollInSequencePage enrollInSequence = new EnrollInSequencePage();
		enrollInSequence.setId(seqId);
		enrollInSequence.setStart_at_step(1);
		enrollInSequence.setEnrollments(list);
		enrollInSequence.setSteps(steps);

		Response response = RestClient.doPost("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, enrollInSequence);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message",
				Matchers.containsString("Failed To Enroll in a Sequence :"));

	}

	@Owner("Harika")
	@Test(priority = 5, groups = "nightly-build")
	public void validateInvalidAuth() {

		String basePath = "enrollments/validate";

		ValidateEnrollmentsPage validateEnrollments = new ValidateEnrollmentsPage();
		validateEnrollments.setEnrollments(candList);
		validateEnrollments.setEntity_type(relatedToTypeId);
		Response response = RestClient.doPost("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true,
				validateEnrollments);


		response.then().statusCode(401);

	}

	@Owner("Harika")
	@Test(priority = 6, groups = "nightly-build")
	public void enrollInvalidAuth() {

		String basePath = "enrollments";
		UpdateStepsInEnrollmentPage updateStepsInEnrollment = new UpdateStepsInEnrollmentPage();
		updateStepsInEnrollment.setUpdate_type("settings");
		updateStepsInEnrollment.setSeq_step_details_id(emailStepId);
		updateStepsInEnrollment.setTime(3600);
		updateStepsInEnrollment.setNo_of_days(2);

		ArrayList<Object> steps = new ArrayList<Object>();
		steps.add(updateStepsInEnrollment);

		updateStepsInEnrollment.setUpdate_type("settings");
		updateStepsInEnrollment.setSeq_step_details_id(taskStepId);
		updateStepsInEnrollment.setTime(3600);
		updateStepsInEnrollment.setNo_of_days(2);

		steps.add(updateStepsInEnrollment);

		EnrollInSequencePage enrollInSequence = new EnrollInSequencePage();
		enrollInSequence.setId(seqId);
		enrollInSequence.setStart_at_step(1);
		enrollInSequence.setEnrollments(candList);
		enrollInSequence.setSteps(steps);

		Response response = RestClient.doPost("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true,
				enrollInSequence);


		response.then().statusCode(401);

	}

	public ArrayList<Integer> getEnrollment() {
		ReaperIntegration.enableA2PRegistration(ThreadManager.getAccount().getAccountId());
		ReaperIntegration.updateTwilioSubaccount(ThreadManager.getAccount().getAccountId());

		commanFunction function = new commanFunction();

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");

		ReaperIntegration.provideSmsConsentToEntity(ThreadManager.getAccount().getAccountId(), "Candidate", candidateEntitySlug);
		ReaperIntegration.updateEntityColumns(candidateEntitySlug, new UpdateEntityRequest("candidate", Map.of("contactnumber", "+17862337361")));

		int candID1 = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("candidate", candidateEntitySlug)
				.getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
		candList = new ArrayList<Integer>();
		candList.add(candID1);
		return candList;
	}

	@DataProvider
	public Object[][] getSeqId() {

		AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
		Response response = allCrudFunctions.createSequence(nymaURL,ThreadManager.getOwnerAlbatrossToken() ,5);
		JsonPath jp = response.jsonPath();
		seqId = jp.get("data.id");

		Response responseAddEmailStep = allCrudFunctions.addEmailStepToSequence(nymaURL, ThreadManager.getOwnerAlbatrossToken(), seqId, 1);
		JsonPath jpEmailStep = responseAddEmailStep.jsonPath();
		emailStepId = jpEmailStep.get("data[0].id");

		Response responseTaskStep = allCrudFunctions.addTaskStepToSequence(nymaURL, ThreadManager.getOwnerAlbatrossToken(), seqId, 2);
		JsonPath jpTaskStep = responseTaskStep.jsonPath();
		taskStepId = jpTaskStep.get("data[0].id");

		Response responseSmsStep = allCrudFunctions.addSmsStepToSequence(nymaURL, ThreadManager.getOwnerAlbatrossToken(), seqId, 3);
		JsonPath jpSmsStep = responseSmsStep.jsonPath();
		smsStepId = jpSmsStep.get("data[0].id");
		
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		getEnrollment();

		Object data[][] = { { seqId, taskStepId, emailStepId, smsStepId } };
		return data;
	}

	@DataProvider
	public Object[][] getInvalidEnrollments() {
		commanFunction function = new commanFunction();

		//sms consent pending
		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");
		ReaperIntegration.updateEntityColumns(candidateEntitySlug, new UpdateEntityRequest("candidate", Map.of("contactnumber", "+17862337361")));

		commanFunction function1 = new commanFunction();

		//not a local number
		JsonPath jsonCandidate1 = function1.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String candidateEntitySlug1 = jsonCandidate1.get("slug");
		ReaperIntegration.provideSmsConsentToEntity(ThreadManager.getAccount().getAccountId(), "Candidate", candidateEntitySlug1);
		ReaperIntegration.updateEntityColumns(candidateEntitySlug1, new UpdateEntityRequest("candidate", Map.of("contactnumber", "12345")));

		commanFunction function2 = new commanFunction();

		//opt out for sms
		JsonPath jsonCandidate2 = function2.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug2 = jsonCandidate2.get("slug");
		ReaperIntegration.optOutFromSms( "candidate", candidateEntitySlug2);

		int candID1 = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("candidate", candidateEntitySlug)
				.getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
		int candID2 = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("candidate", candidateEntitySlug1)
				.getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
		int candID3 = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("candidate", candidateEntitySlug2)
				.getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());

		ArrayList<Integer> candList = new ArrayList<Integer>();
		candList.add(123);		//invlaid prospect id
		candList.add(111);		//invlaid prospect id
		candList.add(candID1);	//consent pending
		candList.add(candID2);	//invalid phone number
		candList.add(candID3);	//opted out from sms

		Object data[][] = { { candList } };
		return data;
	}

}
