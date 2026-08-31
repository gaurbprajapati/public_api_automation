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
public class AddEditSequenceStepsTest extends TestBase {

	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	JavaFakerMails fakerMails = new JavaFakerMails();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	String templateTitle;
	String templateSubject;
	String templateBody;
	int seqId;
	int taskStepId;
	int emailStepId;
	int smsStepId;
	int linkedInStepId;


	@Owner("Harika")
	@Test(priority = 0, groups = "nightly-build")
	public void updateTemplateOfEmailStep() {

		createSequence();

		createEmailTemplate("candidate updated", "5");
		CreateEmailStepToSequencePage createEmailStepToSequence = new CreateEmailStepToSequencePage();
		createEmailStepToSequence.setId(emailStepId);
		createEmailStepToSequence.setStep_no(1);
		createEmailStepToSequence.setNo_of_days(2);
		createEmailStepToSequence.setTemplate_title(templateTitle);
		createEmailStepToSequence.setTemplate_subject(templateSubject);
		createEmailStepToSequence.setTemplate_content(templateBody);
		createEmailStepToSequence.setTime(3600);
		createEmailStepToSequence.setType(1);
		createEmailStepToSequence.setUpdate_type("template");
		createEmailStepToSequence.setInclude_opt_out_link(1);
		ArrayList<Object> emailStep = new ArrayList<>();
		emailStep.add(createEmailStepToSequence);
		AddEmailStepsToSequencePage addEmailStep = new AddEmailStepsToSequencePage();
		addEmailStep.setSteps(emailStep);

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/steps";
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				addEmailStep);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Save sequence steps"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));

	}

	@Owner("Harika")
	@Test(priority = 1, groups = "nightly-build")
	public void updateTemplateOfTaskStep() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/steps";

		CreateTaskStepToSequencePage createTaskStepToSequence = new CreateTaskStepToSequencePage();
		createTaskStepToSequence.setId(taskStepId);
		createTaskStepToSequence.setNo_of_days(2);
		createTaskStepToSequence.setStep_no(2);
		createTaskStepToSequence.setTime(3600);
		createTaskStepToSequence.setType(2);
		createTaskStepToSequence.setReminder(30);
		createTaskStepToSequence.setTask_title("Task step update" + generatedString);
		createTaskStepToSequence.setTask_description("Task remainder in sequence update" + generatedString);
		createTaskStepToSequence.setUpdate_type("template");
		ArrayList<Object> taskStep = new ArrayList<>();
		taskStep.add(createTaskStepToSequence);
		AddTaskStepsToSequencePage addTaskSteps = new AddTaskStepsToSequencePage();
		addTaskSteps.setSteps(taskStep);
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				addTaskSteps);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Save sequence steps"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));

	}

	@Owner("Ajendra Singh")
	@Test(priority = 2, groups = "nightly-build")
	public void updateTemplateofSmsStep(){

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/steps";

		CreateSmsStepToSequencePage createSmsStepToSequencePage = new CreateSmsStepToSequencePage();
		createSmsStepToSequencePage.setId(smsStepId);
		createSmsStepToSequencePage.setType(3);
		createSmsStepToSequencePage.setStep_no(1);
		createSmsStepToSequencePage.setNo_of_days(2);
		createSmsStepToSequencePage.setSms_template_title("Sms step template " + generatedString);
		createSmsStepToSequencePage.setSms_template_content("Sms step template " + generatedString);
		createSmsStepToSequencePage.setTime(3600);
		createSmsStepToSequencePage.setUpdate_type("template");

		ArrayList<Object> taskStep = new ArrayList<>();
		taskStep.add(createSmsStepToSequencePage);
		AddTaskStepsToSequencePage addTaskSteps = new AddTaskStepsToSequencePage();
		addTaskSteps.setSteps(taskStep);
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				addTaskSteps);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Save sequence steps"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));
	}

	@Owner("Harika")
	@Test(priority = 3, groups = "nightly-build")
	public void updateTemplateOfLinkedInStep(){

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/steps";

		CreateLinkedInStepToSequencePage createLinkedInStepToSequence = new CreateLinkedInStepToSequencePage();
		createLinkedInStepToSequence.setId(String.valueOf(linkedInStepId));
		createLinkedInStepToSequence.setTime(36000);
		createLinkedInStepToSequence.setNo_of_days("2");
		createLinkedInStepToSequence.setUpdate_type("template");
		createLinkedInStepToSequence.setLinkedin_template_title("LinkedIn step template update" + RandomStringUtils.randomAlphabetic(4));
		createLinkedInStepToSequence.setLinkedin_template_content("LinkedIn remainder in sequence template update" + RandomStringUtils.randomAlphabetic(4));
		createLinkedInStepToSequence.setStep_no(4);
		createLinkedInStepToSequence.setType(4);
		createLinkedInStepToSequence.setEmail_sms_linkedin_step_cnt(3);

		ArrayList<Object> taskStep = new ArrayList<>();
		taskStep.add(createLinkedInStepToSequence);
		AddTaskStepsToSequencePage addTaskSteps = new AddTaskStepsToSequencePage();
		addTaskSteps.setSteps(taskStep);
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				addTaskSteps);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Save sequence steps"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));
	}

	@Owner("Harika")
	@Test(priority = 4, groups = "nightly-build")
	public void updateSettingsOfEmailStep() {

		CreateEmailStepToSequencePage createEmailStepToSequence = new CreateEmailStepToSequencePage();
		createEmailStepToSequence.setId(emailStepId);
		createEmailStepToSequence.setStep_no(1);
		createEmailStepToSequence.setNo_of_days(3);
		createEmailStepToSequence.setTemplate_title(templateTitle);
		createEmailStepToSequence.setTemplate_subject(templateSubject);
		createEmailStepToSequence.setTemplate_content(templateBody);
		createEmailStepToSequence.setTime(3600 + 1800);
		createEmailStepToSequence.setType(1);
		createEmailStepToSequence.setUpdate_type("settings");
		createEmailStepToSequence.setInclude_opt_out_link(1);
		ArrayList<Object> emailStep = new ArrayList<>();
		emailStep.add(createEmailStepToSequence);
		AddEmailStepsToSequencePage addEmailStep = new AddEmailStepsToSequencePage();
		addEmailStep.setSteps(emailStep);

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/steps";
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				addEmailStep);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Save sequence steps"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));

	}

	@Owner("Harika")
	@Test(priority = 5, groups = "nightly-build")
	public void updateSettingsOfTaskStep() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/steps";

		CreateTaskStepToSequencePage createTaskStepToSequence = new CreateTaskStepToSequencePage();
		createTaskStepToSequence.setId(taskStepId);
		createTaskStepToSequence.setNo_of_days(3);
		createTaskStepToSequence.setStep_no(2);
		createTaskStepToSequence.setTime(3600 + 1800);
		createTaskStepToSequence.setType(2);
		createTaskStepToSequence.setReminder(30);
		createTaskStepToSequence.setTask_title("Task step update" + generatedString);
		createTaskStepToSequence.setTask_description("Task remainder in sequence update" + generatedString);
		createTaskStepToSequence.setUpdate_type("settings");
		ArrayList<Object> taskStep = new ArrayList<>();
		taskStep.add(createTaskStepToSequence);
		AddTaskStepsToSequencePage addTaskSteps = new AddTaskStepsToSequencePage();
		addTaskSteps.setSteps(taskStep);
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				addTaskSteps);


		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Save sequence steps"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));

	}

	@Owner("Ajendra Singh")
	@Test(priority = 6, groups = "nightly-build")
	public void updateSettingsOfSmsStep() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/steps";

		CreateSmsStepToSequencePage createSmsStepToSequencePage = new CreateSmsStepToSequencePage();
		createSmsStepToSequencePage.setId(smsStepId);
		createSmsStepToSequencePage.setType(3);
		createSmsStepToSequencePage.setStep_no(1);
		createSmsStepToSequencePage.setNo_of_days(5);
		createSmsStepToSequencePage.setSms_template_title("Sms step template " + generatedString);
		createSmsStepToSequencePage.setSms_template_content("Sms step template " + generatedString);
		createSmsStepToSequencePage.setTime(3600 + 1800);
		createSmsStepToSequencePage.setUpdate_type("settings");
		ArrayList<Object> smsStep = new ArrayList<>();
		smsStep.add(createSmsStepToSequencePage);
		AddTaskStepsToSequencePage addSmsSteps = new AddTaskStepsToSequencePage();
		addSmsSteps.setSteps(smsStep);
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				addSmsSteps);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Save sequence steps"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));

	}

	@Owner("Harika")
	@Test(priority = 7, groups = "nightly-build")
	public void addEmailStepToSequenceInvalidAuth() {

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

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/steps";
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, pathParameters,
				true, addEmailStep);

		response.then().statusCode(401);

	}

	@Owner("Harika")
	@Test(priority = 8, groups = "nightly-build")
	public void addTaskStepToSequenceInvalidAuth() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

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
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, pathParameters,
				true, addTaskSteps);

		response.then().statusCode(401);

	}

	@Owner("Ajendra Singh")
	@Test(priority = 9, groups = "nightly-build")
	public void addSmsStepToSequenceInvalidAuth(){

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/steps";

		CreateSmsStepToSequencePage createSmsStepToSequencePage = new CreateSmsStepToSequencePage();
		createSmsStepToSequencePage.setId(smsStepId);
		createSmsStepToSequencePage.setType(3);
		createSmsStepToSequencePage.setStep_no(1);
		createSmsStepToSequencePage.setNo_of_days(2);
		createSmsStepToSequencePage.setSms_template_title("Sms step template " + generatedString);
		createSmsStepToSequencePage.setSms_template_content("Sms step template " + generatedString);
		createSmsStepToSequencePage.setTime(3600);
		createSmsStepToSequencePage.setUpdate_type("all");

		ArrayList<Object> taskStep = new ArrayList<>();
		taskStep.add(createSmsStepToSequencePage);
		AddTaskStepsToSequencePage addTaskSteps = new AddTaskStepsToSequencePage();
		addTaskSteps.setSteps(taskStep);
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken() +"1234", null, pathParameters, true,
				addTaskSteps);

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

	public void createSequence() {

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

		Response usersResponse = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
		usersResponse.then().statusCode(200);
		JsonPath user = usersResponse.jsonPath();
		int accountOwnerId = user.get("[0].id");
		ReaperIntegration.insertUnipileSubscription(ThreadManager.getAccount().getAccountId(),ThreadManager.getAccount().getOwner().getEmail(),accountOwnerId);

		Response responseLinkedInStep = allCrudFunctions.addLinkedInStepToSequence(nymaURL, ThreadManager.getOwnerAlbatrossToken(), seqId, 4);
		JsonPath jpLinkedInStep = responseLinkedInStep.jsonPath();
		linkedInStepId = jpLinkedInStep.get("data[0].id");

	}
}
