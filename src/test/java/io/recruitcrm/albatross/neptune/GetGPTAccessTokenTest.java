package io.recruitcrm.albatross.neptune;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.neptune.JavaFakerSummary;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetGPTAccessTokenTest extends TestBase {
	JavaFakerSummary javaFakerSummary = new JavaFakerSummary();

	@Owner("Divya")
	@Test()
	public void getGPTAccessToken_Test() {

		String basePath = "user-gpt-token";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.allotted_token", Matchers.notNullValue());
		response.then().body("user", Matchers.notNullValue());
		response.then().assertThat()
				.body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//gptAccessToken.json"));

	}

	@Owner("Divya")
	@Test
	public void unauthorizedUserCannotGenerateUserPrompt_Test_Test() {

		String basePath = "user-gpt-token";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, null, true);

		Assert.assertEquals(response.getStatusCode(), 401);

		response.then().body("error", Matchers.is("Unauthorized"));
		response.then().assertThat().body(JsonSchemaValidator
				.matchesJsonSchemaInClasspath("schemaValidation//rcrm//unauthorizedValidation.json"));
	}

}
