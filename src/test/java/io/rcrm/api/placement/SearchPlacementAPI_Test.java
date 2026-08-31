package io.rcrm.api.placement;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class SearchPlacementAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	commanFunction function;
	String albatrossTknA;
	String albatrossTknB;
	int accountOwnerId;
	String basePath = "placements/search";
	JavaFakerPlacement faker;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknB = getTokenForAccount("AccountB", "valid");
		accountOwnerId = getRoleBasedId("AccountA", "Owner");
		function = new commanFunction();
		faker = new JavaFakerPlacement();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "placementSearchData", groups = "nightly-build")
	public void searchPlacementByCandidateData_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		Response candidateResponse = function.getEntityResponseBasedOnSlug(baseURL, apiKeyA, "candidate", candidateSlug);
		candidateResponse.then().statusCode(200);

		String firstName = candidateResponse.jsonPath().getString("first_name");
		String lastName = candidateResponse.jsonPath().getString("last_name");
		String candidateName = (firstName.trim() + " " + lastName.trim()).trim();

		String[] array = { "candidate_slug", "candidate_name", "candidate_name", "candidate_name" };
		String[] value = { candidateSlug, firstName, lastName, candidateName };
		for (int i = 0; i < array.length; i++) {
			Map<String, String> queryParams = new HashMap<>();
			queryParams.put(array[i], value[i]);

			Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

			response.then().statusCode(200);
			response.then().assertThat().body("data", Matchers.notNullValue());
			response.then().assertThat().body("data[0].id", Matchers.equalTo(2));
			response.then().assertThat().body("data[0].candidate_slug", Matchers.equalTo(candidateSlug));

			response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//getAllPlacementsAPI.json"));
		}
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "placementSearchData", groups = "nightly-build")
	public void searchPlacementByCompanyData_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		Response companyResponse = function.getEntityResponseBasedOnSlug(baseURL, apiKeyA, "company", companySlug);
		companyResponse.then().statusCode(200);

		String companyName = companyResponse.jsonPath().getString("company_name");
		if (companyName == null)
			companyName = "";

		String[] array = { "company_slug", "company_name" };
		String[] value = { companySlug, companyName };
		for (int i = 0; i < array.length; i++) {
			Map<String, String> queryParams = new HashMap<>();
			queryParams.put(array[i], value[i]);

			Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

			response.then().statusCode(200);
			response.then().assertThat().body("data", Matchers.notNullValue());
			response.then().assertThat().body("data[0].id", Matchers.equalTo(2));
			response.then().assertThat().body("data[0].company_slug", Matchers.equalTo(companySlug));

			response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//getAllPlacementsAPI.json"));
		}
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "placementSearchData", groups = "nightly-build")
	public void searchPlacementByJobData_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		Response jobResponse = function.getEntityResponseBasedOnSlug(baseURL, apiKeyA, "job", jobSlug);
		jobResponse.then().statusCode(200);

		String jobName = jobResponse.jsonPath().getString("name");

		String[] array = { "job_slug", "job_name" };
		String[] value = { jobSlug, jobName };
		for (int i = 0; i < array.length; i++) {
			Map<String, String> queryParams = new HashMap<>();
			queryParams.put(array[i], value[i]);

			Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

			response.then().statusCode(200);
			response.then().assertThat().body("data", Matchers.notNullValue());
			response.then().assertThat().body("data[0].id", Matchers.equalTo(2));
			response.then().assertThat().body("data[0].job_slug", Matchers.equalTo(jobSlug));

			response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//getAllPlacementsAPI.json"));
		}
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "placementSearchData", groups = "nightly-build")
	public void searchPlacementByContactData_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		Response contactResponse = function.getEntityResponseBasedOnSlug(baseURL, apiKeyA, "contact", contactSlug);
		contactResponse.then().statusCode(200);

		String firstName = contactResponse.jsonPath().getString("first_name");
		String lastName = contactResponse.jsonPath().getString("last_name");
		String contactName = (firstName.trim() + " " + lastName.trim());

		String[] array = { "contact_slug", "contact_name", "contact_name", "contact_name" };
		String[] value = { contactSlug, firstName, lastName, contactName };
		for (int i = 0; i < array.length; i++) {
			Map<String, String> queryParams = new HashMap<>();
			queryParams.put(array[i], value[i]);

			Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

			response.then().statusCode(200);
			response.then().assertThat().body("data", Matchers.notNullValue());
			response.then().assertThat().body("data[0].id", Matchers.equalTo(2));
			response.then().assertThat().body("data[0].contact_slugs", Matchers.hasItem(contactSlug));

			response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//getAllPlacementsAPI.json"));
		}
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "placementSearchData", groups = "nightly-build")
	public void searchPlacementByDealData_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		Response dealResponse = function.getEntityResponseBasedOnSlug(baseURL, apiKeyA, "deal", dealSlug);
		dealResponse.then().statusCode(200);

		String dealName = dealResponse.jsonPath().getString("name");

		String[] array = { "deal_slug", "deal_name" };
		String[] value = { dealSlug, dealName };
		for (int i = 0; i < array.length; i++) {
			Map<String, String> queryParams = new HashMap<>();
			queryParams.put(array[i], value[i]);

			Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

			response.then().statusCode(200);
			response.then().assertThat().body("data", Matchers.notNullValue());
			response.then().assertThat().body("data[0].id", Matchers.equalTo(2));
			response.then().assertThat().body("data[0].deal_slugs", Matchers.hasItem(dealSlug));

			response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//getAllPlacementsAPI.json"));
		}
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "placementSearchData", groups = "nightly-build")
	public void searchPlacementById_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("placement_id", String.valueOf(2));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.notNullValue());
		response.then().assertThat().body("data[0].id", Matchers.equalTo(2));
		response.then().assertThat().body("data[0].company_slug", Matchers.equalTo(companySlug));
		response.then().assertThat().body("data[0].candidate_slug", Matchers.equalTo(candidateSlug));
		response.then().assertThat().body("data[0].job_slug", Matchers.equalTo(jobSlug));
		response.then().assertThat().body("data[0].contact_slugs", Matchers.hasItem(contactSlug));
		response.then().assertThat().body("data[0].deal_slugs", Matchers.hasItem(dealSlug));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//getAllPlacementsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "placementSearchData", groups = "nightly-build")
	public void searchPlacementByCreatedFromAndTo_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String createdFrom = LocalDate.now().minusDays(1).format(dateFormat);
		String createdTo = LocalDate.now().plusDays(1).format(dateFormat);

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("created_from", createdFrom);
		queryParams.put("created_to", createdTo);

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.notNullValue());
		response.then().assertThat().body("data.size()", Matchers.equalTo(2));
		response.then().assertThat().body("data[0].id", Matchers.equalTo(2));
		response.then().assertThat().body("data[0].company_slug", Matchers.equalTo(companySlug));
		response.then().assertThat().body("data[0].candidate_slug", Matchers.equalTo(candidateSlug));
		response.then().assertThat().body("data[0].job_slug", Matchers.equalTo(jobSlug));
		response.then().assertThat().body("data[0].contact_slugs", Matchers.hasItem(contactSlug));
		response.then().assertThat().body("data[0].deal_slugs", Matchers.hasItem(dealSlug));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//getAllPlacementsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementId", groups = "nightly-build")
	public void searchPlacementByInvalidCreatedFromAndTo_PublicAPI(int placementId) {

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String createdFrom = LocalDate.now().minusDays(10).format(dateFormat);
		String createdTo = LocalDate.now().minusDays(5).format(dateFormat);

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("created_from", createdFrom);
		queryParams.put("created_to", createdTo);

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.empty());
		response.then().assertThat().body("data.size()", Matchers.equalTo(0));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "placementSearchData", groups = "nightly-build")
	public void searchPlacementWithSortOrder_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		String[] sortOrders = { "asc", "desc" };
		int[] expectedFirstIds = { 1, 2 };
		int[] expectedSecondIds = { 2, 1 };

		for (int i = 0; i < sortOrders.length; i++) {
			Map<String, String> queryParams = new HashMap<>();
			queryParams.put("created_by_id", String.valueOf(accountOwnerId));
			queryParams.put("sort_by", "createdon");
			queryParams.put("sort_order", sortOrders[i]);

			Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

			response.then().statusCode(200);
			response.then().assertThat().body("data", Matchers.notNullValue());
			response.then().assertThat().body("data.size()", Matchers.equalTo(2));
			response.then().assertThat().body("data[0].id", Matchers.equalTo(expectedFirstIds[i]));
			response.then().assertThat().body("data[1].id", Matchers.equalTo(expectedSecondIds[i]));

			response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//getAllPlacementsAPI.json"));
		}
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementId", groups = "nightly-build")
	public void searchPlacementWithExactSearch_PublicAPI(int placementId) {

		String[] exactSearchValues = { "0", "1" };
		for (int i = 0; i < exactSearchValues.length; i++) {
			Map<String, String> queryParams = new HashMap<>();
			queryParams.put("created_by_name", "Owner");
			queryParams.put("exact_search", exactSearchValues[i]);

			Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

			response.then().statusCode(200);
			if (exactSearchValues[i].equals("0")) {
				response.then().assertThat().body("data", Matchers.not(Matchers.empty()));
				response.then().assertThat().body("data[0].id", Matchers.equalTo(1));
			} else if (exactSearchValues[i].equals("1")) {
				response.then().assertThat().body("data", Matchers.empty());
				response.then().assertThat().body("data.size()", Matchers.equalTo(0));
			}
		}
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "placementSearchData", groups = "nightly-build")
	public void searchPlacementWithMultipleEntitySlugs_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("candidate_slug", candidateSlug);
		queryParams.put("company_slug", companySlug);
		queryParams.put("contact_slug", contactSlug);
		queryParams.put("job_slug", jobSlug);
		queryParams.put("deal_slug", dealSlug);

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.notNullValue());
		response.then().assertThat().body("data[0].id", Matchers.equalTo(2));
		response.then().assertThat().body("data[0].candidate_slug", Matchers.equalTo(candidateSlug));
		response.then().assertThat().body("data[0].company_slug", Matchers.equalTo(companySlug));
		response.then().assertThat().body("data[0].job_slug", Matchers.equalTo(jobSlug));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//getAllPlacementsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getPlacementId", groups = "nightly-build")
	public void searchPlacementWithValidPlacementIdAndInvalidSlugParams_PublicAPI(int placementId) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("candidate_slug", faker.getRandomSlug());
		queryParams.put("company_slug", faker.getRandomSlug());
		queryParams.put("job_slug", faker.getRandomSlug());
		queryParams.put("contact_slug", faker.getRandomSlug());
		queryParams.put("deal_slug", faker.getRandomSlug());
		queryParams.put("placement_id", String.valueOf(1));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.notNullValue());
		response.then().assertThat().body("data[0].id", Matchers.equalTo(1));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//getAllPlacementsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchPlacementWithInvalidSearchParam_PublicAPI() {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("candidateslug", faker.getRandomSlug());

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(400);
		response.then().assertThat().body("errorMessage", Matchers.is("UNSUPPORTED_SEARCH"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//placement//placementUnsupportedSearchAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchPlacementWithInvalidEmail_PublicAPI() {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("created_by_email", "yopmail.com");

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(422);
		response.then().assertThat().body("created_by_email", Matchers.hasItem("The created by email must be a valid email address."));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchPlacementWithInvalidToken_PublicAPI() {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("created_by_id", String.valueOf(accountOwnerId));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA + "123", queryParams, null, true);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "placementSearchData", groups = "nightly-build")
	public void searchPlacementWithCrossAccountToken_PublicAPI(String companySlug, String candidateSlug, String jobSlug, String contactSlug, String dealSlug) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("created_by_id", String.valueOf(accountOwnerId));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyB, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.empty());
		response.then().assertThat().body("data.size()", Matchers.equalTo(0));
	}

	@DataProvider(parallel = true)
	public Object[][] placementSearchData() {
		function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL);
		Response response = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL);
		String companySlug = response.jsonPath().get("data.companySlug");
		String candidateSlug = response.jsonPath().get("data.candidateSlug");
		String jobSlug = response.jsonPath().get("data.jobSlug");
		String contactSlug = response.jsonPath().get("data.associations[0].contacts[0].slug");
		String dealSlug = response.jsonPath().get("data.associations[0].deals[0].slug");
		return new Object[][] { { companySlug, candidateSlug, jobSlug, contactSlug, dealSlug } };
	}

	@DataProvider(parallel = true)
	public Object[][] getPlacementId() {
		int placementId = function.createPlacement(baseURL, apiKeyA, albatrossURL, albatrossTknA, invoiceServiceURL).jsonPath().get("data.id");
		return new Object[][] { { placementId } };
	}
}
