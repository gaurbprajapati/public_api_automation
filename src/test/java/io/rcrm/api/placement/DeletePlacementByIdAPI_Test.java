package io.rcrm.api.placement;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import java.util.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class DeletePlacementByIdAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	commanFunction function;
	String albatrossTknA;
	String albatrossTknB;
	JavaFakerPlacement placementFaker;
	String basePath = "placements/{id}";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknB = getTokenForAccount("AccountB", "valid");
		function = new commanFunction();
		placementFaker = new JavaFakerPlacement();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementId", groups = "nightly-build")
	public void deletePlacementByIdWithValidToken_PublicAPI(int placementId) {

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("id", String.valueOf(1));

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiKeyA, null, pathParameters, true);

		response.then().statusCode(200);
		response.then().body(Matchers.containsString("Deleted Successfully"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//deletePlacementByIdSuccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementId", groups = "nightly-build")
	public void deleteAlreadyDeletedPlacement_PublicAPI(int placementId) {

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("id", String.valueOf(1));

		Response deleteResponse = RestClient.doDelete("JSON", baseURL, basePath, apiKeyA, null, pathParameters, true);

		deleteResponse.then().statusCode(200);
		deleteResponse.then().assertThat().body(Matchers.containsString("Deleted Successfully"));

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiKeyA, null, pathParameters, true);

		response.then().statusCode(404);
		response.then().assertThat().body("errorMessage", Matchers.is("Placement doesn't exist"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//placementsNotExistsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void deletePlacementByIdWithInvalidId_PublicAPI() {

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("id", String.valueOf(placementFaker.getRandomID()));

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiKeyA, null, pathParameters, true);

		response.then().statusCode(404);
		response.then().assertThat().body("errorMessage", Matchers.is("Placement doesn't exist"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//placementsNotExistsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void deletePlacementByIdWithInvalidToken_PublicAPI() {

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("id", String.valueOf(placementFaker.getRandomID()));

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiKeyA + "123", null, pathParameters, true);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementId", groups = "nightly-build")
	public void deletePlacementByIdWithCrossAccountToken_PublicAPI(int placementId) {

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("id", String.valueOf(placementId));

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiKeyB, null, pathParameters, true);

		response.then().statusCode(404);
		response.then().assertThat().body("errorMessage", Matchers.is("Placement doesn't exist"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//placementsNotExistsAPI.json"));
	}

	@DataProvider(parallel = true)
	public Object[][] getPlacementId() {
		int placementId = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL).jsonPath().get("data.id");
		return new Object[][] { { placementId } };
	}
}