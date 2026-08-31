package io.recruitcrm.nyma.emailsequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.nyma.AddEmailStepsToSequencePage;
import io.rcrm.api.pojo.nyma.CreateEmailSequencePage;
import io.rcrm.api.pojo.nyma.CreateEmailStepToSequencePage;
import io.rcrm.api.pojo.nyma.SequenceSettingPage;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class GetEmailSequenceTest extends TestBase {

	commanFunction function = new commanFunction();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	String seq_title;
	int seqId;


	@BeforeClass(alwaysRun = true)	public void createSequence(){
		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();

		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
		sequenceSetting.setThread_emails_as_replies(1);
		sequenceSetting.setExecute_step_on_business_days(1);
		JSONObject settings = new JSONObject(sequenceSetting);

		createEmailSequence.setEntity_type(5);
		createEmailSequence.setSeq_title("Candidate" + " add sequence test " + generatedString);
		createEmailSequence.setSeq_settings(settings.toString());
		createEmailSequence.setSilent_progress(false);
		createEmailSequence.setSave_steps(0);

		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
				createEmailSequence);
		response.then().statusCode(200);
		JsonPath jp = response.jsonPath();
		int candidateSequenceId = jp.get("data.id");

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateSequenceId));

		CreateEmailStepToSequencePage createEmailStepToSequence = new CreateEmailStepToSequencePage();
		createEmailStepToSequence.setStep_no(1);
		createEmailStepToSequence.setNo_of_days(2);
		createEmailStepToSequence
				.setTemplate_title("Candidate Email Template " + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence
				.setTemplate_subject("Creating email Template for Candidate " + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence
				.setTemplate_content("Candidate Template body " + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence.setTime(3600);
		createEmailStepToSequence.setType(1);
		createEmailStepToSequence.setUpdate_type("all");
		createEmailStepToSequence.setInclude_opt_out_link(1);

		ArrayList<Object> emailStep = new ArrayList<>();
		emailStep.add(createEmailStepToSequence);
		AddEmailStepsToSequencePage addEmailStep = new AddEmailStepsToSequencePage();
		addEmailStep.setSteps(emailStep);

		String basePath = "email-sequences/{id}/steps";
		Response responseAddEmailStep = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParameters, true, addEmailStep);

		responseAddEmailStep.then().statusCode(200);
	}

	@Owner("Harika")
	@Test(priority = 0, groups = "nightly-build")
	public void getEmailSequenceFromList() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("req_from", "1");
		queryParameters.put("sort_by", "seq_title");
		queryParameters.put("sort_order", "desc");
		String basePath = "email-sequences";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Searched successfully"));
	}

	@Owner("Harika")
	@Test(priority = 1, groups = "nightly-build")
	public void getEmailSequenceToEnroll() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("req_from", "2");
		queryParameters.put("entity_type", "5");
		queryParameters.put("sort_by", "seq_title");
		queryParameters.put("sort_order", "desc");
		String basePath = "email-sequences";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);

		JsonPath jp = response.jsonPath();
		seq_title = jp.get("data.sequences[0].seq_title");
		seqId = jp.get("data.sequences[0].id");
		response.then().statusCode(200);

		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Searched successfully"));
	}

	@Owner("Harika")
	@Test(priority = 2, groups = "nightly-build")
	public void getEmailSequenceFromMySeq() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("req_from", "3");
		queryParameters.put("sort_by", "seq_title");
		queryParameters.put("sort_order", "desc");
		String basePath = "email-sequences";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);

		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Searched successfully"));
	}

	@Owner("Harika")
	@Test(priority = 3, groups = "nightly-build")
	public void getEmailSequenceBrowseAll() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("req_from", "4");
		queryParameters.put("sort_by", "seq_title");
		queryParameters.put("sort_order", "desc");
		String basePath = "email-sequences";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Searched successfully"));
	}

	@Owner("Harika")
	@Test(priority = 4, groups = "nightly-build")
	public void searchEmailSequenceByTitle() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("req_from", "2");
		queryParameters.put("entity_type", "5");
		queryParameters.put("seq_tile", seq_title);

		String basePath = "email-sequences";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);

		JsonPath jp = response.jsonPath();
		seq_title = jp.get("data.sequences[0].seq_title");

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("data.sequences[0].seq_title", Matchers.equalTo(seq_title));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Sequence Searched successfully"));

	}

	@Owner("Harika")
	@Test(priority = 5, groups = "nightly-build")
	public void getEmailSequenceFromListInvalidAuth() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("req_from", "1");
		String basePath = "email-sequences";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", queryParameters, null,
				true);

		response.then().statusCode(401);
	}

	@Owner("Harika")
	@Test(priority = 6, groups = "nightly-build")
	public void getEmailSequenceToEnrollInvalidAuth() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("req_from", "2");
		queryParameters.put("entity_type", "5");
		String basePath = "email-sequences";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", queryParameters, null,
				true);

		response.then().statusCode(401);

	}

	@Owner("Harika")
	@Test(priority = 7, groups = "nightly-build")
	public void getEmailSequenceFromMySeqInvalidAuth() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("req_from", "3");
		String basePath = "email-sequences";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", queryParameters, null,
				true);

		response.then().statusCode(401);
	}

	@Owner("Harika")
	@Test(priority = 8, groups = "nightly-build")
	public void getEmailSequenceBrowseAllInvalidAuth() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		queryParameters.put("req_from", "4");
		String basePath = "email-sequences";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", queryParameters, null,
				true);

		response.then().statusCode(401);
	}

	@Owner("Harika")
	@Test(priority = 9, groups = "nightly-build")
	public void getEmailSequenceEmptyParams() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("page", " ");
		queryParameters.put("limit", " ");
		queryParameters.put("req_from", " ");
		String basePath = "email-sequences";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);

		response.then().statusCode(422);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("The req from field is required."));
	}

	@Owner("Harika")
	@Test(priority = 10, groups = "nightly-build")
	public void getEmailSequenceById() {

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
	@Test(priority = 11, groups = "nightly-build")
	public void getEmailSequenceByInvalidId() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId + "123"));

		String basePath = "email-sequences/{id}";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("Sequence not found."));

	}

	@Owner("Harika")
	@Test(priority = 12, groups = "nightly-build")
	public void getEmailSequenceByIdString() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", "abcd");

		String basePath = "email-sequences/{id}";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);
		response.then().statusCode(422);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("The id must be an integer."));

	}

	@Owner("Harika")
	@Test(priority = 13, groups = "nightly-build")
	public void getEmailSequenceByIdInvalidAuth() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, pathParameters,
				true);
		response.then().statusCode(401);

	}

	@Owner("Harika")
	@Test(priority = 14, groups = "nightly-build")
	public void getEmailSequenceStats() {

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
	@Test(priority = 15, groups = "nightly-build")
	public void getEmailSequenceMyStats() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/my-stats";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}

	@Owner("Harika")
	@Test(priority = 16, groups = "nightly-build")
	public void getEmailSequenceStatsInvalidId() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId + "123"));

		String basePath = "email-sequences/{id}/stats";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("Sequence does not exists."));

	}

	@Owner("Harika")
	@Test(priority = 17, groups = "nightly-build")
	public void getEmailSequenceMyStatsInvalidId() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId + "123"));

		String basePath = "email-sequences/{id}/my-stats";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("Sequence does not exists."));

	}

	@Owner("Harika")
	@Test(priority = 18, groups = "nightly-build")
	public void getEmailSequenceStatsInvalidAuth() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/stats";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, pathParameters,
				true);
		response.then().statusCode(401);

	}

	@Owner("Harika")
	@Test(priority = 19, groups = "nightly-build")
	public void getEmailSequenceMyStatsInvalidAuth() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}/my-stats";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, pathParameters,
				true);
		response.then().statusCode(401);

	}

}
