package io.recruitcrm.CandidateService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetSocialLinksTest extends TestBase {
	String albatrossAuthToken;

	@BeforeClass(alwaysRun = true)
	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Ajendra Singh")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getSocialLinksTest_200() {
		String basePath = "candidates/social-links";
		
		Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, null, true);

		response.then().statusCode(200);
		
		// JSON Schema Validation
		response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/socialLinksUnified.json"));
		
		// Additional validations
		assertThat(response.jsonPath().getString("meta.message"), equalTo("Social Link Fetched Successfully"));
		assertThat(response.jsonPath().getString("meta.responseType.context"), equalTo("Request is successful"));
		assertThat(response.jsonPath().getInt("meta.responseType.code"), equalTo(103));
		assertThat(response.jsonPath().get("data"), notNullValue());
		assertThat(response.jsonPath().get("meta.requestUuid"), notNullValue());
		assertThat(response.jsonPath().get("meta.timestamp"), notNullValue());
	}

	@Owner("Ajendra Singh")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getSocialLinksUnauthorizedTest_401() {
		String basePath = "candidates/social-links";
		
		Response response = RestClient.doGet("JSON", candidatesURL, basePath, "InvalidToken", null, null, true);
		
		response.then().statusCode(401);
		
		// JSON Schema Validation for 401 response
		response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/socialLinksUnified.json"));
		
		// Additional validations
		assertThat(response.jsonPath().getString("meta.message"), equalTo("Unauthorised access"));
		assertThat(response.jsonPath().getInt("meta.status"), equalTo(401));
		assertThat(response.jsonPath().getInt("meta.responseType.code"), equalTo(104));
		assertThat(response.jsonPath().getString("meta.responseType.context"), equalTo("Warning"));
		assertThat(response.jsonPath().getString("data"), equalTo("Internal Server Error"));
		assertThat(response.jsonPath().get("errors"), notNullValue());
		assertThat(response.jsonPath().get("meta.requestUuid"), notNullValue());
		assertThat(response.jsonPath().get("meta.timestamp"), notNullValue());
	}

	@Owner("Ajendra Singh")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getSocialLinksTest_404() {
		String basePath = "social-link"; // Incorrect URL
		
		Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, null, true);
		
		response.then().statusCode(404);
		assertThat(response.jsonPath().getString("error"), equalTo("Not Found"));
		assertThat(response.jsonPath().getString("path"), equalTo("/v2/social-link"));
	}
}
