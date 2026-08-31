package io.recruitcrm.nyma.emailsequence;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class DeleteSequenceStepsTest extends TestBase {

	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	int seqId;
	int taskStepId;
	int emailStepId;
	int smsStepId;
	int linkedInStepId;

	@Owner("Harika")
	@Test(priority = 0, groups = "nightly-build")
	public void DeleteTaskStepInSequence() {

		createSequence();

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));
		pathParameters.put("step_id", String.valueOf(taskStepId));

		String basePath = "email-sequences/{id}/steps/{step_id}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);

		Assert.assertNotNull(response);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Delete Sequence Step Successful "));
		response.then().body("action_name", Matchers.containsString("Delete Sequence Step"));

	}

	@Owner("Ajendra Singh")
	@Test (priority = 1, groups = "nightly-build")
	public void DeleteSmsStepInSequence() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));
		pathParameters.put("step_id", String.valueOf(smsStepId));

		String basePath = "email-sequences/{id}/steps/{step_id}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);

		Assert.assertNotNull(response);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Delete Sequence Step Successful "));
		response.then().body("action_name", Matchers.containsString("Delete Sequence Step"));
	}

	@Owner("Harika")
	@Test (priority = 2, groups = "nightly-build")
	public void DeleteLinkedInStepInSequence() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));
		pathParameters.put("step_id", String.valueOf(linkedInStepId));

		String basePath = "email-sequences/{id}/steps/{step_id}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);

		Assert.assertNotNull(response);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Delete Sequence Step Successful "));
		response.then().body("action_name", Matchers.containsString("Delete Sequence Step"));
	}

	@Owner("Harika")
	@Test (priority = 3, groups = "nightly-build")
	public void DeleteEmailStepInSequence() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));
		pathParameters.put("step_id", String.valueOf(emailStepId));

		String basePath = "email-sequences/{id}/steps/{step_id}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);

		Assert.assertNotNull(response);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("Failed To Delete Sequence Step : Sequence must have atleast one saved step."));
		response.then().body("action_name", Matchers.containsString("Delete Sequence Step"));

	}

	@Owner("Harika")
	@Test(priority = 4, groups = "nightly-build")
	public void DeleteStepInvalidId() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));
		pathParameters.put("step_id", String.valueOf(taskStepId + "123"));

		String basePath = "email-sequences/{id}/steps/{step_id}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);

        Assert.assertNotNull(response);
        response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message",
				Matchers.containsString("Failed To Delete Sequence Step : Sequence Step does not exists."));
		response.then().body("action_name", Matchers.containsString("Delete Sequence Step"));

	}

	@Owner("Harika")
	@Test(priority = 5, groups = "nightly-build")
	public void DeleteStepInvalidAuth() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));
		pathParameters.put("step_id", String.valueOf(taskStepId));

		String basePath = "email-sequences/{id}/steps/{step_id}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"x001", null, pathParameters,
				true);


        Assert.assertNotNull(response);
        response.then().statusCode(401);

	}

	@Owner("Harika")
	@Test(priority = 6, groups = "nightly-build")
	public void DeleteStep_422() {

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));
		pathParameters.put("step_id", "abcd");

		String basePath = "email-sequences/{id}/steps/{step_id}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);

		response.then().statusCode(422);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("The step id must be an integer."));
	}

	public void createSequence() {

		Response response = allCrudFunctions.createSequence(nymaURL, ThreadManager.getOwnerAlbatrossToken(), 5);
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
		ReaperIntegration.insertUnipileSubscription(ThreadManager.getAccount().getAccountId(), ThreadManager.getAccount().getOwner().getEmail(), accountOwnerId);

		Response responseLinkedInStep = allCrudFunctions.addLinkedInStepToSequence(nymaURL, ThreadManager.getOwnerAlbatrossToken(), seqId, 4);
		JsonPath jpLinkedInStep = responseLinkedInStep.jsonPath();
		linkedInStepId = jpLinkedInStep.get("data[0].id");

	}

}