package io.recruitcrm.nyma.emailsequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerMails;
import io.rcrm.api.pojo.nyma.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
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

@AccountType("Business|AlbatrossTkn|Email")
public class AllEndpointsOfEmailSequencingTest extends TestBase {

	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	JavaFakerMails fakerMails = new JavaFakerMails();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	String templateTitle;
	String templateSubject;
	String templateBody;
	int candidateSeqId;
	int contactSeqId;
	int candidateSeqStepId;
	int contactSeqStepId;


	@Owner("Harika")
	@Test(dataProvider = "getRelateToTypeId", priority = 0, groups = "nightly-build")
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

		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
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
	@Test(dataProvider = "getRelateToTypeId", priority = 1, groups = "nightly-build")
	public void editEmailSequence(String entityType, int relatedToTypeId, int seqId) {

		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();

		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
		sequenceSetting.setThread_emails_as_replies(1);
		sequenceSetting.setExecute_step_on_business_days(1);
		JSONObject settings = new JSONObject(sequenceSetting);

		createEmailSequence.setSeq_title(entityType + " add sequence test Updated" + generatedString);
		createEmailSequence.setSeq_settings(settings.toString());
		createEmailSequence.setSilent_progress(false);
		createEmailSequence.setSave_steps(0);

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		Response response = RestClient.doPost1("JSON", nymaURL, "email-sequences/{id}", ThreadManager.getOwnerAlbatrossToken(), null,
				pathParameters, true, createEmailSequence);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));
		response.then().body("action_name", Matchers.containsString("Update Sequence"));

	}

	

	@Owner("Harika")
	@Test(dataProvider = "getRelateToTypeId", priority = 2, groups = "nightly-build")
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
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, queryParameters, true,
				addEmailStep);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Save sequence steps"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getStepId", priority = 3, groups = "nightly-build")
	public void addTaskStepToSequence(int relatedToTypeId, int seqId, int stepId) {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		int taskTypeId = allCrudFunctions.getTaskTypeId(albatrossURL,ThreadManager.getOwnerAlbatrossToken()).jsonPath().get("data.customizeTaskType[0].id");

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
		createTaskStepToSequence.setTask_type(taskTypeId);
		ArrayList<Object> taskStep = new ArrayList<>();
		taskStep.add(createTaskStepToSequence);
		AddTaskStepsToSequencePage addTaskSteps = new AddTaskStepsToSequencePage();
		addTaskSteps.setSteps(taskStep);
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				addTaskSteps);


		JsonPath jp = response.jsonPath();

		if (relatedToTypeId == 2) {
			contactSeqStepId = jp.get("data[0].id");
		} else {
			candidateSeqStepId = jp.get("data[0].id");
		}

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Save sequence steps"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getStepId", priority = 4, groups = "nightly-build")
	public void addLinkedInStepToSequence(int relatedToTypeId, int seqId, int stepId) {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

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

		for(int i = 0; i < 2; i++) {
			if (i == 1) {
				Response usersResponse = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
				usersResponse.then().statusCode(200);
				JsonPath user = usersResponse.jsonPath();
				int accountOwnerid = user.get("[0].id");
				ReaperIntegration.insertUnipileSubscription(ThreadManager.getAccount().getAccountId(), ThreadManager.getAccount().getOwner().getEmail(), accountOwnerid);
			}
			Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
					addTaskSteps);

			response.then().statusCode(200);
			response.then().body("action_name", Matchers.containsString("Save sequence steps"));

			if (i == 0 && relatedToTypeId==5) {
				response.then().body("status", Matchers.containsString("fail"));
				response.then().body("message_type", Matchers.containsString("is-danger"));
				response.then().body("message", Matchers.containsString("Failed To save sequence steps : Linkedin steps are not allowed"));
			} else {
				response.then().body("status", Matchers.containsString("success"));
				response.then().body("message_type", Matchers.containsString("is-success"));
				response.then().body("message", Matchers.containsString("Sequence saved successfully"));
			}
		}

	}



	@Owner("Harika")
	@Test(priority = 5, groups = "nightly-build")
	public void getEmailSequence() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("req_from","1");
		String basePath = "email-sequences";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Searched successfully"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getSequenceId", priority = 6, groups = "nightly-build")
	public void getEmailSequenceById(int seqId) {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence fetched successfully."));

	}

	@Owner("Harika")
	@Test(dataProvider = "getSequenceId", priority = 7, groups = "nightly-build")
	public void getEmailSequenceStats(int seqId) {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/stats";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Stats"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getStepId", priority = 8, groups = "nightly-build")
	public void DeleteStepInSequence(int relatedToTypeId, int seqId, int stepId) {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));
		pathParameters.put("step_id", String.valueOf(stepId));

		String basePath = "email-sequences/{id}/steps/{step_id}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Delete Sequence Step Successful "));
		response.then().body("action_name", Matchers.containsString("Delete Sequence Step"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getSequenceId", priority = 9, groups = "nightly-build")
	public void cloneSequence(int seqId) {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/clone";
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				null);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Clone Sequence Successful "));

	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getSequenceId", priority = 10, groups = "nightly-build")
	public void DeleteSequenceTest(int seqId) {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Delete Sequence Successful "));
		response.then().body("action_name", Matchers.containsString("Delete Sequence"));

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

	@DataProvider
	public Object[][] getRelateToTypeId() {
		Object data[][] = { { "Candidate", 5, candidateSeqId }, { "Contacts", 2, contactSeqId } };
		return data;
	}

	@DataProvider
	public Object[][] getSequenceId() {
		Object data[][] = { { candidateSeqId }, { contactSeqId } };
		return data;
	}

	@DataProvider
	public Object[][] getStepId() {
		Object data[][] = { { 5, candidateSeqId, candidateSeqStepId }, { 2, contactSeqId, contactSeqStepId } };
		return data;
	}

}
