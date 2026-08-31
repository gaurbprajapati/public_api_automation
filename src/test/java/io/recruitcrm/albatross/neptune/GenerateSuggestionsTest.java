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
import io.rcrm.api.pojo.neptune.GenerateSuggestions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GenerateSuggestionsTest extends TestBase {
	JavaFakerSummary javaFakerSummary = new JavaFakerSummary();

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void getSuggestions_Test() {

		String basePath = "get-suggestions";
		String data = javaFakerSummary.getRandomString(50);
		GenerateSuggestions generateSuggestions = new GenerateSuggestions(data);

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				generateSuggestions);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.generated_text", Matchers.notNullValue());
		response.then().body("meta.message", Matchers.is("Suggestions generated successfully"));
	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void getSuggestionsWithNullValue_Test() {

		String basePath = "get-suggestions";
		GenerateSuggestions generateSuggestions = new GenerateSuggestions(null);

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				generateSuggestions);
		Assert.assertEquals(response.getStatusCode(), 400);
		response.then().body("data", Matchers.nullValue());

		response.then().body("errors.errors.data.errorMsg", Matchers.is("Input should be a valid string"));
		response.then().assertThat().body(JsonSchemaValidator
				.matchesJsonSchemaInClasspath("schemaValidation//rcrm//nullValueForSuggestion.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetSuggestions_Test() {

		String basePath = "get-suggestions";
		String data = javaFakerSummary.getRandomString(50);
		GenerateSuggestions generateSuggestions = new GenerateSuggestions(data);

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true,
				generateSuggestions);
		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("detail", Matchers.is("Unauthorized"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//unauthorizedGPTAccess.json"));

	}

}
