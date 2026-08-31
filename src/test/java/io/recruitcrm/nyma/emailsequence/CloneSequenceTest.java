package io.recruitcrm.nyma.emailsequence;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerMails;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class CloneSequenceTest extends TestBase {

	commanFunction function = new commanFunction();
	JavaFakerMails fakerMails = new JavaFakerMails();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	int seqId;
	String templateTitle;
	String templateSubject;
	String templateBody;


	@Owner("Harika")
	@Test(priority = 0, groups = "nightly-build")
	public void cloneSequence() {

		createSequence();

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

	@Owner("Harika")
	@Test(priority = 1, groups = "nightly-build")
	public void cloneSequenceWithInvalidId() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId + "123"));

		String basePath = "email-sequences/{id}/clone";
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
				null);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message",
				Matchers.containsString("Failed To Clone Sequence : Sequence does not exists."));

	}

	@Owner("Harika")
	@Test(priority = 2, groups = "nightly-build")
	public void cloneSequenceWithInvalidAuth() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId + "123"));

		String basePath = "email-sequences/{id}/clone";
		Response response = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"x001", null, pathParameters,
				true, null);

		response.then().statusCode(401);

	}

	public void createSequence() {

		AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
		Response response = allCrudFunctions.createSequence(nymaURL,ThreadManager.getOwnerAlbatrossToken() ,5);
		JsonPath jp = response.jsonPath();
		seqId = jp.get("data.id");

		allCrudFunctions.addEmailStepToSequence(nymaURL, ThreadManager.getOwnerAlbatrossToken(), seqId, 1);

		allCrudFunctions.addTaskStepToSequence(nymaURL, ThreadManager.getOwnerAlbatrossToken(), seqId, 2);

		allCrudFunctions.addSmsStepToSequence(nymaURL, ThreadManager.getOwnerAlbatrossToken(), seqId, 3);

		commanFunction function = new commanFunction();
		Response usersResponse = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
		usersResponse.then().statusCode(200);
		JsonPath user = usersResponse.jsonPath();
		int accountOwnerId = user.get("[0].id");
		ReaperIntegration.insertUnipileSubscription(ThreadManager.getAccount().getAccountId(),ThreadManager.getAccount().getOwner().getEmail(),accountOwnerId);

		allCrudFunctions.addLinkedInStepToSequence(nymaURL, ThreadManager.getOwnerAlbatrossToken(), seqId, 4);

	}

}