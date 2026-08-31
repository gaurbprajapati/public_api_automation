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
public class EditSequenceTest extends TestBase {

	commanFunction function = new commanFunction();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	int seqId;


	

	@Owner("Harika")
	@Test(dataProvider = "getSeqId", priority = 0, groups = "nightly-build")
	public void editEmailSequence(int seqId) {

		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();

		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
        sequenceSetting.setThread_emails_as_replies(1);
        sequenceSetting.setExecute_step_on_business_days(1);
        JSONObject settings = new JSONObject(sequenceSetting);
        
		createEmailSequence.setSeq_title("candidate add sequence test Updated" + generatedString);
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
	@Test(priority = 1, groups = "nightly-build")
	public void editEmailSequenceInvalidAuth() {

		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();

		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
        sequenceSetting.setThread_emails_as_replies(1);
        sequenceSetting.setExecute_step_on_business_days(1);
        JSONObject settings = new JSONObject(sequenceSetting);
        
		createEmailSequence.setSeq_title("candidate add sequence test Updated" + generatedString);
		createEmailSequence.setSeq_settings(settings.toString());
		createEmailSequence.setSilent_progress(false);
		createEmailSequence.setSave_steps(0);

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		Response response = RestClient.doPost1("JSON", nymaURL, "email-sequences/{id}", ThreadManager.getOwnerAlbatrossToken()+"x001", null,
				pathParameters, true, createEmailSequence);


		response.then().statusCode(401);

	}

	@Owner("Harika")
	@Test(priority = 2, groups = "nightly-build")
	public void editEmailSequenceEmptyTitle() {

		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();
		
		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
        sequenceSetting.setThread_emails_as_replies(1);
        sequenceSetting.setExecute_step_on_business_days(1);
        JSONObject settings = new JSONObject(sequenceSetting);

		createEmailSequence.setSeq_title(" ");
		createEmailSequence.setSeq_settings(settings.toString());
		createEmailSequence.setSilent_progress(false);
		createEmailSequence.setSave_steps(0);

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		Response response = RestClient.doPost1("JSON", nymaURL, "email-sequences/{id}", ThreadManager.getOwnerAlbatrossToken(), null,
				pathParameters, true, createEmailSequence);


		response.then().statusCode(422);
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message", Matchers.containsString("The seq title field is required."));

	}

	@Owner("Harika")
	@Test(priority = 3, groups = "nightly-build")
	public void editEmailSequenceEmptySettings() {

		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();

		createEmailSequence.setSeq_title("candidate add sequence test Updated" + generatedString);
		createEmailSequence.setSeq_settings(" ");
		createEmailSequence.setSilent_progress(false);
		createEmailSequence.setSave_steps(0);

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		Response response = RestClient.doPost1("JSON", nymaURL, "email-sequences/{id}", ThreadManager.getOwnerAlbatrossToken(), null,
				pathParameters, true, createEmailSequence);


		response.then().statusCode(422);
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message", Matchers.containsString("The seq settings field is required."));

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

		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
				createEmailSequence);

		response.then().statusCode(200);

		JsonPath jp = response.jsonPath();
		seqId = jp.get("data.id");

		Object data[][] = { { seqId } };
		return data;
	}

}
