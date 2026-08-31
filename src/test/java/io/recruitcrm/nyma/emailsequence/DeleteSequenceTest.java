package io.recruitcrm.nyma.emailsequence;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
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
public class DeleteSequenceTest extends TestBase {

	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	commanFunction function = new commanFunction();
	int seqId;


	@Owner("Harika")
	@Test(priority = 0, groups = "nightly-build")
	public void DeleteSequenceById() {

		createSequence();

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

	@Owner("Harika")
	@Test(priority = 1, groups = "nightly-build")
	public void DeleteSequenceByIdInvalid() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message",
				Matchers.containsString("Failed To Delete Sequence : Sequence does not exists."));
		response.then().body("action_name", Matchers.containsString("Delete Sequence"));

	}

	@Owner("Harika")
	@Test(priority = 2, groups = "nightly-build")
	public void DeleteSequenceInvalidAuth() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		String basePath = "email-sequences/{id}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"x001", null, pathParameters,
				true);


		response.then().statusCode(401);

	}

	public void createSequence() {

		Response response = allCrudFunctions.createSequence(nymaURL,ThreadManager.getOwnerAlbatrossToken() ,5);
		JsonPath jp = response.jsonPath();
		seqId = jp.get("data.id");

		allCrudFunctions.addEmailStepToSequence(nymaURL, ThreadManager.getOwnerAlbatrossToken(), seqId, 1);

		allCrudFunctions.addTaskStepToSequence(nymaURL, ThreadManager.getOwnerAlbatrossToken(), seqId, 2);

		allCrudFunctions.addSmsStepToSequence(nymaURL, ThreadManager.getOwnerAlbatrossToken(), seqId, 3);

		Response usersResponse = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
		usersResponse.then().statusCode(200);
		JsonPath user = usersResponse.jsonPath();
		int accountOwnerId = user.get("[0].id");
		ReaperIntegration.insertUnipileSubscription(ThreadManager.getAccount().getAccountId(),ThreadManager.getAccount().getOwner().getEmail(),accountOwnerId);

		allCrudFunctions.addLinkedInStepToSequence(nymaURL, ThreadManager.getOwnerAlbatrossToken(), seqId, 4);
	}

}