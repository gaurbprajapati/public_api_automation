package io.rcrm.api.invoice;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetAllInvoicesAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	String albatrossTknA;
	commanFunction function;
	AllCrudFunctions allCrudFunctions;
	String basePath = "invoices";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		function = new commanFunction();
		allCrudFunctions = new AllCrudFunctions();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceCreatedOnAndUpdatedOnSortingParams", groups = "nightly-build")
	public void getAllInvoicesWithCreatedOnAndUpdatedOnSortingParams_PublicAPI(String sortBy, String sortOrder, int invoiceId1, int invoiceId2, String companySlug) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("sort_by", sortBy);
		queryParams.put("sort_order", sortOrder);
		queryParams.put("limit", String.valueOf(10));
		queryParams.put("page", String.valueOf(1));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data[0].id", Matchers.equalTo(invoiceId1));
		response.then().assertThat().body("data[1].id", Matchers.equalTo(invoiceId2));
		response.then().assertThat().body("data[0].billed_to_client.company_slug", Matchers.equalTo(companySlug));
		response.then().assertThat().body("data[1].billed_to_client.company_slug", Matchers.equalTo(companySlug));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//getAllInvoicesAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIssueDateAndDueDateSortingParams", groups = "nightly-build")
	public void getAllInvoicesWithIssueDateAndDueDateSortingParams_PublicAPI(String sortBy, String sortOrder) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("sort_by", sortBy);
		queryParams.put("sort_order", sortOrder);
		queryParams.put("limit", String.valueOf(10));
		queryParams.put("page", String.valueOf(1));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data.size()", Matchers.equalTo(2));

		String date1 = response.jsonPath().getString("data[0]." + sortBy);
		String date2 = response.jsonPath().getString("data[1]." + sortBy);
		int comparison = date1.compareTo(date2);
		if (sortOrder.equals("asc"))
			Assert.assertTrue(comparison <= 0, "Expected " + sortBy + " in ascending order: data[0]=" + date1 + ", data[1]=" + date2);
		else
			Assert.assertTrue(comparison >= 0, "Expected " + sortBy + " in descending order: data[0]=" + date1 + ", data[1]=" + date2);

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//getAllInvoicesAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceLimitParamsWithIds", groups = "nightly-build")
	public void getAllInvoicesWithLimitParams_PublicAPI(int limit, int page, int expectedDataId1, Integer expectedDataId2, int expectedSize) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("sort_by", "created_on");
		queryParams.put("sort_order", "asc");
		queryParams.put("limit", String.valueOf(limit));
		queryParams.put("page", String.valueOf(page));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.notNullValue());
		response.then().assertThat().body("data", Matchers.hasSize(expectedSize));
		response.then().assertThat().body("data[0].id", Matchers.equalTo(expectedDataId1));
		if (expectedDataId2 != null) {
			response.then().assertThat().body("data[1].id", Matchers.equalTo(expectedDataId2));
		}

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//getAllInvoicesAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceId", groups = "nightly-build")
	public void getAllInvoicesWithInvalidPageNumber_PublicAPI(int invoiceId) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("page", "2");

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.empty());

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//getInvoicesPaginationAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getAllInvoicesWithMissingSortParams_PublicAPI() {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("sort_by", "created_on");

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(422);
		response.then().assertThat().body("sort_order[0]", Matchers.is("The sort order field is required when sort by is present."));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getAllInvoicesWithInvalidSortParams_PublicAPI() {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("sort_by", "createdat");
		queryParams.put("sort_order", "ascending");

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(422);
		response.then().assertThat().body("sort_by[0]", Matchers.is("The selected sort by is invalid."));
		response.then().assertThat().body("sort_order[0]", Matchers.is("The selected sort order is invalid."));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getAllInvoicesWithInvalidToken_PublicAPI() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA + "123", null, null, true);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceId", groups = "nightly-build")
	public void getAllInvoicesWithCrossAccountToken_PublicAPI(int invoiceId) {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyB, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.empty());

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//getInvoicesPaginationAPI.json"));
	}

	@DataProvider(parallel = true)
	public Object[][] getInvoiceCreatedOnAndUpdatedOnSortingParams() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
		int invoiceId1 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId).jsonPath().get("data");
		int invoiceId2 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId).jsonPath().get("data");
		return new Object[][] { 
			{ "created_on", "asc", invoiceId1, invoiceId2, companySlug },
			{ "created_on", "desc", invoiceId2, invoiceId1, companySlug },
			{ "updated_on", "asc", invoiceId1, invoiceId2, companySlug },
			{ "updated_on", "desc", invoiceId2, invoiceId1, companySlug } };
	}

	@DataProvider(parallel = true)
	public Object[][] getInvoiceLimitParamsWithIds() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
		int invoiceId1 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId).jsonPath().get("data");
		int invoiceId2 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId).jsonPath().get("data");
		return new Object[][] {
				// limit, page, expectedDataId1, expectedDataId2, expectedSize
				{ 1, 1, invoiceId1, null, 1 }, 
				{ 1, 2, invoiceId2, null, 1 }, 
				{ 2, 1, invoiceId1, invoiceId2, 2 } };
	}

	@DataProvider(parallel = true)
	public Object[][] getInvoiceIssueDateAndDueDateSortingParams() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
		function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
		function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
		return new Object[][] { 
			{ "issue_date", "asc" }, 
			{ "issue_date", "desc" },
			{ "due_date", "asc" },
			{ "due_date", "desc" } };
	}

	@DataProvider(parallel = true)
	public Object[][] getInvoiceId() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
		Response response = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
		int invoiceId = response.jsonPath().get("data");
		return new Object[][] { { invoiceId } };
	}
}