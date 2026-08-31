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
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetAllPlacementsAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	commanFunction function;
	String albatrossTknA;
	String albatrossTknB;
	String basePath = "placements";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknB = getTokenForAccount("AccountB", "valid");
		function = new commanFunction();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementSortingParamsWithIds", groups = "nightly-build")
	public void getAllPlacementsWithSortingParams_PublicAPI(String sortBy, String sortOrder, int limit, int page, int expectedDataId1, Integer expectedDataId2, int expectedSize) {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", sortBy);
		queryParameters.put("sort_order", sortOrder);
		queryParameters.put("limit", String.valueOf(limit));
		queryParameters.put("page", String.valueOf(page));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParameters, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.notNullValue());
		response.then().assertThat().body("data", Matchers.hasSize(expectedSize));
		response.then().assertThat().body("data[0].id", Matchers.equalTo(expectedDataId1));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//getAllPlacementsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementLimitParamsWithIds", groups = "nightly-build")
	public void getAllPlacementsWithLimitParams_PublicAPI(int limit, int page, int expectedDataId1, Integer expectedDataId2, int expectedSize) {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "createdon");
		queryParameters.put("sort_order", "asc");
		queryParameters.put("limit", String.valueOf(limit));
		queryParameters.put("page", String.valueOf(page));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParameters, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.notNullValue());
		response.then().assertThat().body("data", Matchers.hasSize(expectedSize));
		response.then().assertThat().body("data[0].id", Matchers.equalTo(expectedDataId1));
		if (expectedDataId2 != null) {
			response.then().assertThat().body("data[1].id", Matchers.equalTo(expectedDataId2));
		}

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//getAllPlacementsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementId", groups = "nightly-build")
	public void getAllPlacementsWithInvalidPageNumber_PublicAPI(int placementId) {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "createdon");
		queryParameters.put("sort_order", "asc");
		queryParameters.put("limit", String.valueOf(2));
		queryParameters.put("page", String.valueOf(2));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParameters, null, true);

		response.then().statusCode(404);
		response.then().assertThat().body("errorMessage", Matchers.is("Placements doesn't exist"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//placementsNotExistsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getAllPlacementsWithMissingSortParams_PublicAPI() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "createdon");

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParameters, null, true);

		response.then().statusCode(422);
		response.then().assertThat().body("sort_order[0]", Matchers.is("The sort order field is required when sort by is present."));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getAllPlacementsWithInvalidSortParams_PublicAPI() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "createdat");
		queryParameters.put("sort_order", "ascending");

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParameters, null, true);

		response.then().statusCode(422);
		response.then().assertThat().body("sort_by[0]", Matchers.is("The selected sort by is invalid."));
		response.then().assertThat().body("sort_order[0]", Matchers.is("The selected sort order is invalid."));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getAllPlacementsWithInvalidToken_PublicAPI() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA + "123", null, null, true);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementId", groups = "nightly-build")
	public void getAllPlacementsWithCrossAccountToken_PublicAPI(int placementId) {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyB, null, null, true);

		response.then().statusCode(404);
		response.then().assertThat().body("errorMessage", Matchers.is("Placements doesn't exist"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//placementsNotExistsAPI.json"));
	}

	@DataProvider(parallel = true)
	public Object[][] getPlacementSortingParamsWithIds() {
		function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL).jsonPath().get("data.id");
		function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL).jsonPath().get("data.id");
		return new Object[][] {
				// sort_by, sort_order, limit, page, expectedId1, expectedId2, expectedSize
				{ "createdon", "asc", 10, 1, 1, 2, 2 }, { "createdon", "desc", 10, 1, 2, 1, 2 },
				{ "updatedon", "asc", 10, 1, 1, 2, 2 }, { "updatedon", "desc", 10, 1, 2, 1, 2 } };
	}

	@DataProvider(parallel = true)
	public Object[][] getPlacementLimitParamsWithIds() {
		function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL).jsonPath().get("data.id");
		function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL).jsonPath().get("data.id");
		return new Object[][] {
				// limit, page, expectedDataId1, expectedDataId2, expectedSize
				{ 1, 1, 1, null, 1 }, { 1, 2, 2, null, 1 }, { 2, 1, 1, 2, 2 } };
	}

	@DataProvider(parallel = true)
	public Object[][] getPlacementId() {
		int placementId = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL).jsonPath().get("data.id");
		return new Object[][] { { placementId } };
	}

}