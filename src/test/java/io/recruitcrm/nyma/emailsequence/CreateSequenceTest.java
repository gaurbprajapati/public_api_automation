package io.recruitcrm.nyma.emailsequence;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.nyma.CreateEmailSequencePage;
import io.rcrm.api.pojo.nyma.SequenceSettingPage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class CreateSequenceTest extends TestBase {

	commanFunction function = new commanFunction();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	int seqId;


	@Owner("Harika")
	@Test(priority = 0, groups = "nightly-build")
	public void createEmailSequence() {

		CreateEmailSequencePage createEmailSequencePage = new CreateEmailSequencePage();

		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
		sequenceSetting.setThread_emails_as_replies(1);
		sequenceSetting.setExecute_step_on_business_days(1);

		JSONObject settings = new JSONObject(sequenceSetting);

		createEmailSequencePage.setEntity_type(5);
		createEmailSequencePage.setSeq_title("candidate add sequence test " + generatedString);
		createEmailSequencePage.setSeq_settings(settings.toString());
		createEmailSequencePage.setSilent_progress(false);
		createEmailSequencePage.setSave_steps(0);


		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
				createEmailSequencePage);


		JsonPath jp = response.jsonPath();
		seqId = jp.get("data.id");

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence saved successfully"));
		response.then().body("action_name", Matchers.containsString("Add Sequence"));

	}

	@Owner("Harika")
	@Test(priority = 1, groups = "nightly-build")
	public void createEmailSequenceInvalidAuth() {

		CreateEmailSequencePage createEmailSequencePage = new CreateEmailSequencePage();

		SequenceSettingPage sequenceSettingPage = new SequenceSettingPage();
		sequenceSettingPage.setThread_emails_as_replies(1);
		sequenceSettingPage.setExecute_step_on_business_days(1);

		JSONObject settings = new JSONObject(sequenceSettingPage);

		createEmailSequencePage.setEntity_type(5);
		createEmailSequencePage.setSeq_title("candidate add sequence test " + generatedString);
		createEmailSequencePage.setSeq_settings(settings.toString());
		createEmailSequencePage.setSilent_progress(false);
		createEmailSequencePage.setSave_steps(0);


		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken()+"12345", null, true,
				createEmailSequencePage);

		response.then().statusCode(401);

	}

	@Owner("Harika")
	@Test(priority = 2, groups = "nightly-build")
	public void createSequenceWithEmptyTitle() {

		CreateEmailSequencePage createEmailSequencePage = new CreateEmailSequencePage();

		SequenceSettingPage sequenceSettingPage = new SequenceSettingPage();
		sequenceSettingPage.setThread_emails_as_replies(1);
		sequenceSettingPage.setExecute_step_on_business_days(1);

		JSONObject settings = new JSONObject(sequenceSettingPage);

		createEmailSequencePage.setEntity_type(5);
		createEmailSequencePage.setSeq_title(" ");
		createEmailSequencePage.setSeq_settings(settings.toString());
		createEmailSequencePage.setSilent_progress(false);
		createEmailSequencePage.setSave_steps(0);

		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
				createEmailSequencePage);

		response.then().statusCode(422);
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message", Matchers.containsString("The seq title field is required."));

	}

	@Owner("Harika")
	@Test(priority = 3, groups = "nightly-build")
	public void createSequenceWithInvalidEntity() {

		CreateEmailSequencePage createEmailSequencePage = new CreateEmailSequencePage();

		SequenceSettingPage sequenceSettingPage = new SequenceSettingPage();
		sequenceSettingPage.setThread_emails_as_replies(1);
		sequenceSettingPage.setExecute_step_on_business_days(1);

		JSONObject settings = new JSONObject(sequenceSettingPage);

		createEmailSequencePage.setEntity_type(6);
		createEmailSequencePage.setSeq_title("Invalid add sequence test " + generatedString);
		createEmailSequencePage.setSeq_settings(settings.toString());
		createEmailSequencePage.setSilent_progress(false);
		createEmailSequencePage.setSave_steps(0);

		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
				createEmailSequencePage);

		response.then().statusCode(422);
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message", Matchers.containsString("The selected entity type is invalid."));

	}

	@Owner("Harika")
	@Test(priority = 4, groups = "nightly-build")
	public void createSequenceWithEmptySettings() {

		CreateEmailSequencePage createEmailSequencePage = new CreateEmailSequencePage();

		createEmailSequencePage.setEntity_type(5);
		createEmailSequencePage.setSeq_title("candidate add sequence test " + generatedString);
		createEmailSequencePage.setSeq_settings(" ");
		createEmailSequencePage.setSilent_progress(false);
		createEmailSequencePage.setSave_steps(0);


		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
				createEmailSequencePage);

		response.then().statusCode(422);
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message", Matchers.containsString("The seq settings field is required."));

	}

	@Owner("Harika")
	@Test(priority = 5, groups = "nightly-build")
	public void createSequenceWithExistingTitle() {

		CreateEmailSequencePage createEmailSequencePage = new CreateEmailSequencePage();

		SequenceSettingPage sequenceSettingPage = new SequenceSettingPage();
		sequenceSettingPage.setThread_emails_as_replies(1);
		sequenceSettingPage.setExecute_step_on_business_days(1);

		JSONObject settings = new JSONObject(sequenceSettingPage);

		createEmailSequencePage.setEntity_type(5);
		createEmailSequencePage.setSeq_title("candidate add sequence test " + generatedString);

		createEmailSequencePage.setSeq_settings(settings.toString());
		createEmailSequencePage.setSilent_progress(false);
		createEmailSequencePage.setSave_steps(0);


		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
				createEmailSequencePage);

		response.then().statusCode(200);
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message",
				Matchers.containsString("Failed To Add Sequence : Sequence with this title already exists"));

	}

}
