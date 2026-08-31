package io.rcrm.api.placement;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.*;

import java.util.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.*;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class EditPlacementByIdAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	commanFunction function;
	AllCrudFunctions allCrudFunctions;
	String albatrossTknA;
	String albatrossTknB;
	JavaFakerPlacement placementFaker;
	JavaFakerCustomField customFieldFaker;
	String basePath = "placements/{id}";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknB = getTokenForAccount("AccountB", "valid");
		function = new commanFunction();
		allCrudFunctions = new AllCrudFunctions();
		placementFaker = new JavaFakerPlacement();
		customFieldFaker = new JavaFakerCustomField();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementIdAndSlugs", groups = "nightly-build")
	public void editPlacementByIdWithStandardFields_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("id", String.valueOf(1));

		EditPlacementRequest body = buildEditPlacementBody(companySlug, candidateSlug, jobSlug, contactSlug, dealSlug, placementFaker.getCurrencyId(), null);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParameters, true, body);

		response.then().statusCode(200);
		response.then().body("id", Matchers.equalTo(1));
		response.then().body("message", Matchers.is("Placement updated successfully"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//updatePlacementAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createPlacementAndCustomField", groups = "nightly-build")
	public void editPlacementByIdWithCustomFields_PublicAPI(int placementId, int customFieldId1, int customFieldId2, String candidateSlug, String jobSlug) {

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("id", String.valueOf(1));

		String customFieldValue = customFieldFaker.getRandomCustomTextValue();

		List<CustomField> customFields = Arrays.asList(
				new CustomField(customFieldId1, customFieldValue, null, null, null),
				new CustomField(customFieldId2, "QA Team", null, null, null));

		EditPlacementRequest body = buildEditPlacementBody("", "", "", "", "", placementFaker.getCurrencyId(), customFields);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParameters, true, body);

		response.then().statusCode(200);
		response.then().body("id", Matchers.equalTo(1));
		response.then().body("message", Matchers.is("Placement updated successfully"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//updatePlacementAPI.json"));

		Response placementResponse = RestClient.doGet("JSON", invoiceServiceURL, "placements/" + placementId, albatrossTknA, null, null, true);

		placementResponse.then().statusCode(200);
		placementResponse.then().assertThat().body("meta.message", Matchers.is("Placement Fetched Successfully"));
		placementResponse.then().assertThat().body("data.custcolumn" + customFieldId1, Matchers.is(customFieldValue));
		placementResponse.then().assertThat().body("data.custcolumn" + customFieldId2, Matchers.is("QA Team"));
		placementResponse.then().assertThat().body("data.candidateSlug", Matchers.is(candidateSlug));
		placementResponse.then().assertThat().body("data.jobSlug", Matchers.is(jobSlug));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementIdAndSlugs", groups = "nightly-build")
	public void editPlacementByIdWithInvalidContactSlug_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("id", String.valueOf(1));

		String contactSlugNew = function.createNewContact_POST(baseURL, apiKeyA, "").jsonPath().getString("slug");

		EditPlacementRequest body = buildEditPlacementBody("", "", "", contactSlugNew, "", placementFaker.getCurrencyId(), null);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParameters, true, body);

		response.then().statusCode(422);
		response.then().assertThat().body("contact_slugs[0]", Matchers.is("The contact(s) must belong to the specified company."));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementIdAndSlugs", groups = "nightly-build")
	public void editPlacementWithSameValues_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("id", String.valueOf(1));

		EditPlacementRequest body = buildEditPlacementBody(companySlug, candidateSlug, jobSlug, contactSlug, dealSlug, null, null);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParameters, true, body);

		response.then().statusCode(422);
		response.then().body("errorMessage", Matchers.is("At least one value must change!"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//placementsReupdateAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementIdAndSlugs", groups = "nightly-build")
	public void editPlacementWithNullValues_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("id", String.valueOf(1));

		JSONObject body = new JSONObject();
		body.put("company_slug", JSONObject.NULL);
		body.put("candidate_slug", JSONObject.NULL);
		body.put("job_slug", JSONObject.NULL);
		body.put("contact_slugs", JSONObject.NULL);
		body.put("deal_slugs", JSONObject.NULL);
		body.put("currency_id", JSONObject.NULL);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParameters, true, body);

		response.then().statusCode(422);
		response.then().body("company_slug", Matchers.hasItem("Invalid company slug"));
		response.then().body("candidate_slug", Matchers.hasItems("Invalid candidate slug", "The candidate must belong to the specified job."));
		response.then().body("job_slug", Matchers.hasItem("The job(s) must belong to the specified company."));
		response.then().body("contact_slugs", Matchers.hasItems("The contact slugs must be a string.", "The contact(s) must belong to the specified company."));
		response.then().body("deal_slugs", Matchers.hasItems("The deal slugs must be a string.", "The deal(s) must belong to the specified job."));
		response.then().body("currency_id", Matchers.hasItem("The selected currency id is invalid."));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//placementValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void editPlacementByIdWithInvalidId_PublicAPI() {

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("id", String.valueOf(placementFaker.getRandomID()));

		EditPlacementRequest body = buildEditPlacementBody("", "", "", "", "", 1, new ArrayList<>());

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParameters, true, body);

		response.then().statusCode(404);
		response.then().assertThat().body("errorMessage", Matchers.is("Placement doesn't exist"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//placementsNotExistsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void editPlacementByIdWithInvalidToken_PublicAPI() {

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("id", String.valueOf(1));

		EditPlacementRequest body = buildEditPlacementBody("", "", "", "", "", 1, new ArrayList<>());

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA + "123", null, pathParameters, true, body);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementIdAndSlugs", groups = "nightly-build")
	public void editPlacementByIdWithCrossAccountToken_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("id", String.valueOf(1));

		EditPlacementRequest body = buildEditPlacementBody(companySlug, candidateSlug, jobSlug, contactSlug, dealSlug, placementFaker.getCurrencyId(), null);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyB, null, pathParameters, true, body);

		response.then().statusCode(422);
		response.then().assertThat().body("company_slug[0]", Matchers.is("Invalid company slug"));
		response.then().assertThat().body("candidate_slug[0]", Matchers.is("Invalid candidate slug"));
		response.then().assertThat().body("candidate_slug[1]", Matchers.is("The candidate must belong to the specified job."));
		response.then().assertThat().body("job_slug[0]", Matchers.is("The job(s) must belong to the specified company."));
		response.then().assertThat().body("contact_slugs[0]", Matchers.is("The contact(s) must belong to the specified company."));
		response.then().assertThat().body("deal_slugs[0]", Matchers.is("The deal(s) must belong to the specified job."));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//placementValidationErrorsAPI.json"));
	}

	private EditPlacementRequest buildEditPlacementBody(String companySlug, String candidateSlug, String jobSlug, String contactSlugs, String dealSlugs, Integer currencyId, List<CustomField> customFields) {
		EditPlacementRequest body = new EditPlacementRequest();
		body.setCompany_slug(companySlug);
		body.setCandidate_slug(candidateSlug);
		body.setJob_slug(jobSlug);
		body.setContact_slugs(contactSlugs);
		body.setDeal_slugs(dealSlugs);
		body.setCurrency_id(currencyId);
		body.setCustom_fields(customFields != null ? customFields : new ArrayList<>());
		return body;
	}

	@DataProvider(parallel = true)
	public Object[][] getPlacementIdAndSlugs() {
		Response response = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL);
		String companySlug = response.jsonPath().get("data.companySlug");
		String candidateSlug = response.jsonPath().get("data.candidateSlug");
		String jobSlug = response.jsonPath().get("data.jobSlug");
		String contactSlug = response.jsonPath().get("data.associations[0].contacts[0].slug");
		String dealSlug = response.jsonPath().get("data.associations[0].deals[0].slug");

		return new Object[][] { { companySlug, candidateSlug, jobSlug, contactSlug, dealSlug } };
	}

	@DataProvider(parallel = true)
	public Object[][] createPlacementAndCustomField() {
		Response response = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL);
		int placementId = response.jsonPath().get("data.id");
		String candidateSlug = response.jsonPath().get("data.candidateSlug");
		String jobSlug = response.jsonPath().get("data.jobSlug");
		Response customFieldResponse1 = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, "placement", customFieldFaker.getCustomFieldName("placement"), "text", "");
		int customFieldId1 = customFieldResponse1.jsonPath().get("data.custumField.columnid");
		Response customFieldResponse2 = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, "placement", customFieldFaker.getCustomFieldName("placement"), "dropdown", "QA Team, Developer Team");
		int customFieldId2 = customFieldResponse2.jsonPath().get("data.custumField.columnid");
		return new Object[][] { { placementId, customFieldId1, customFieldId2, candidateSlug, jobSlug } };
	}
}