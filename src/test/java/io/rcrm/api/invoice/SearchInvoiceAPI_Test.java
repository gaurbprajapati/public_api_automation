package io.rcrm.api.invoice;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.UpdateInvoiceRequest;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.JavaFakerInvoice;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class SearchInvoiceAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	String albatrossTknA;
	commanFunction function;
	AllCrudFunctions allCrudFunctions;
	JavaFakerInvoice fakerInvoice;
	int accountOwnerId;
	int accountAdminId;
	String basePath = "invoices/search";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		accountOwnerId = getRoleBasedId("AccountA", "Owner");
		accountAdminId = getRoleBasedId("AccountA", "Admin");
		function = new commanFunction();
		allCrudFunctions = new AllCrudFunctions();
		fakerInvoice = new JavaFakerInvoice();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceSearchData", groups = "nightly-build")
	public void searchInvoiceByInvoiceId_PublicAPI(int invoiceId, String invoiceIdStr, String companySlug, int invoiceStatus) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("invoice_id", invoiceIdStr);

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.notNullValue());
		response.then().assertThat().body("data[0].id", Matchers.equalTo(invoiceId));
		response.then().assertThat().body("data[0].invoice_id", Matchers.equalTo(invoiceIdStr));
		response.then().assertThat().body("data[0].billed_to_client.company_slug", Matchers.equalTo(companySlug));
		response.then().assertThat().body("data[0].invoice_pdf", Matchers.containsString("https://mystafflocal-mumbai.s3.ap-south-1.amazonaws.com"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//searchInvoicesAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getAssociatedSlugsSearchData", groups = "nightly-build")
	public void searchInvoiceByAssociatedSlugs_PublicAPI(int invoiceId, String queryParamName, String queryParamValue, String candidateSlug, String companySlug, String contactSlug, String jobSlug, String dealSlug) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put(queryParamName, queryParamValue);

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data[0].id", Matchers.equalTo(invoiceId));
		response.then().assertThat().body("data[0].associated_entities.candidate_slugs", Matchers.hasItem(candidateSlug));
		response.then().assertThat().body("data[0].associated_entities.company_slugs", Matchers.hasItem(companySlug));
		response.then().assertThat().body("data[0].associated_entities.contact_slugs", Matchers.hasItem(contactSlug));
		response.then().assertThat().body("data[0].associated_entities.job_slugs", Matchers.hasItem(jobSlug));
		response.then().assertThat().body("data[0].associated_entities.deal_slugs", Matchers.hasItem(dealSlug));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//searchInvoicesAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceSearchData", groups = "nightly-build")
	public void searchInvoiceByOwnerCreatedBy_PublicAPI(int invoiceId, String invoiceIdStr, String companySlug, int invoiceStatus) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("created_by", String.valueOf(accountOwnerId));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.notNullValue());
		response.then().assertThat().body("data[0].id", Matchers.equalTo(invoiceId));
		response.then().assertThat().body("data[0].invoice_id", Matchers.equalTo(invoiceIdStr));
		response.then().assertThat().body("data[0].billed_to_client.company_slug", Matchers.equalTo(companySlug));
		response.then().assertThat().body("data[0].created_by", Matchers.equalTo(accountOwnerId));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//searchInvoicesAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceSearchData", groups = "nightly-build")
	public void searchInvoiceByAdminCreatedBy_PublicAPI(int invoiceId, String invoiceIdStr, String companySlug, int invoiceStatus) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("created_by", String.valueOf(accountAdminId));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.empty());

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//invoiceEmptyResponseAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceDatesData", groups = "nightly-build")
	public void searchInvoiceByValidIssueDateRange_PublicAPI(int invoiceId, String invoiceIssueDate, String invoiceDueDate) {

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate issueDate = Instant.parse(invoiceIssueDate).atZone(ZoneOffset.UTC).toLocalDate();
		String issueDateFrom = issueDate.minusDays(3).format(dateFormat);
		String issueDateTo = issueDate.plusDays(3).format(dateFormat);

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("issue_date_from", issueDateFrom);
		queryParams.put("issue_date_to", issueDateTo);

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.notNullValue());
		response.then().assertThat().body("data[0].id", Matchers.equalTo(invoiceId));
		response.then().assertThat().body("data[0].issue_date", Matchers.equalTo(invoiceIssueDate));
		response.then().assertThat().body("data[0].due_date", Matchers.equalTo(invoiceDueDate));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//searchInvoicesAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceDatesData", groups = "nightly-build")
	public void searchInvoiceByInvalidIssueDateRange_PublicAPI(int invoiceId, String invoiceIssueDate, String invoiceDueDate) {

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate issueDate = Instant.parse(invoiceIssueDate).atZone(ZoneOffset.UTC).toLocalDate();
		String issueDateFrom = issueDate.minusDays(3).format(dateFormat);
		String issueDateTo = issueDate.minusDays(2).format(dateFormat);

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("issue_date_from", issueDateFrom);
		queryParams.put("issue_date_to", issueDateTo);

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.empty());

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//invoiceEmptyResponseAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceDatesData", groups = "nightly-build")
	public void searchInvoiceByValidDueDateRange_PublicAPI(int invoiceId, String invoiceIssueDate, String invoiceDueDate) {

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate issueDate = Instant.parse(invoiceDueDate).atZone(ZoneOffset.UTC).toLocalDate();
		String issueDateFrom = issueDate.minusDays(3).format(dateFormat);
		String issueDateTo = issueDate.plusDays(3).format(dateFormat);

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("due_date_from", issueDateFrom);
		queryParams.put("due_date_to", issueDateTo);

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.notNullValue());
		response.then().assertThat().body("data[0].id", Matchers.equalTo(invoiceId));
		response.then().assertThat().body("data[0].issue_date", Matchers.equalTo(invoiceIssueDate));
		response.then().assertThat().body("data[0].due_date", Matchers.equalTo(invoiceDueDate));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//searchInvoicesAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceDatesData", groups = "nightly-build")
	public void searchInvoiceByInvalidDueDateRange_PublicAPI(int invoiceId, String invoiceIssueDate, String invoiceDueDate) {

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate issueDate = Instant.parse(invoiceDueDate).atZone(ZoneOffset.UTC).toLocalDate();
		String issueDateFrom = issueDate.minusDays(3).format(dateFormat);
		String issueDateTo = issueDate.minusDays(2).format(dateFormat);

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("due_date_from", issueDateFrom);
		queryParams.put("due_date_to", issueDateTo);

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.empty());

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//invoiceEmptyResponseAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceSearchData", groups = "nightly-build")
	public void searchInvoiceByStatusId_PublicAPI(int invoiceId, String invoiceIdStr, String companySlug, int invoiceStatus) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("status_id", String.valueOf(invoiceStatus));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.notNullValue());
		response.then().assertThat().body("data[0].id", Matchers.equalTo(invoiceId));
		response.then().assertThat().body("data[0].invoice_id", Matchers.equalTo(invoiceIdStr));
		response.then().assertThat().body("data[0].invoice_status", Matchers.equalTo(invoiceStatus));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//searchInvoicesAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceSearchData", groups = "nightly-build")
	public void searchInvoiceByInvalidStatusId_PublicAPI(int invoiceId, String invoiceIdStr, String companySlug, int invoiceStatus) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("status_id", String.valueOf(fakerInvoice.getRandomInvoiceId()));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.empty());

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//invoiceEmptyResponseAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceLimitParamsWithIds", groups = "nightly-build")
	public void searchInvoiceWithLimitAndPageParams_PublicAPI(int limit, int page, Integer expectedDataId1, Integer expectedDataId2, int expectedSize, String expectedData0InvoiceIdStr, String expectedData1InvoiceIdStr, String companySlug) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("created_by", String.valueOf(accountOwnerId));
		queryParams.put("sort_by", "created_on");
		queryParams.put("sort_order", "asc");
		queryParams.put("limit", String.valueOf(limit));
		queryParams.put("page", String.valueOf(page));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.hasSize(expectedSize));

		switch (expectedSize) {
		case 0:
			response.then().assertThat().body("data", Matchers.empty());
			response.then().assertThat()
					.body(matchesJsonSchemaInClasspath("publicApi//invoice//invoiceEmptyResponseAPI.json"));
			break;
		default:
			response.then().assertThat().body("data", Matchers.notNullValue());
			response.then().assertThat().body("data[0].id", Matchers.equalTo(expectedDataId1));
			if (expectedData0InvoiceIdStr != null)
				response.then().assertThat().body("data[0].invoice_id", Matchers.equalTo(expectedData0InvoiceIdStr));
			if (expectedSize > 1) {
				response.then().assertThat().body("data[1].id", Matchers.equalTo(expectedDataId2));
				if (expectedData1InvoiceIdStr != null)
					response.then().assertThat().body("data[1].invoice_id",
							Matchers.equalTo(expectedData1InvoiceIdStr));
			}
			response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//searchInvoicesAPI.json"));
			break;
		}
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceSortOrderParamsWithIds", groups = "nightly-build")
	public void searchInvoiceWithSortOrderParams_PublicAPI(int invoiceId1, int invoiceId2, String invoiceIdStr1, String invoiceIdStr2, String companySlug) {

		String[] sortByValues = { "created_on", "issue_date", "due_date" };
		String[] sortOrders = { "asc", "desc" };

		for (String sortBy : sortByValues) {
			for (String sortOrder : sortOrders) {
				Map<String, String> queryParams = new HashMap<>();
				queryParams.put("created_by", String.valueOf(accountOwnerId));
				queryParams.put("sort_by", sortBy);
				queryParams.put("sort_order", sortOrder);

				Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

				response.then().statusCode(200);
				response.then().assertThat().body("data", Matchers.notNullValue());
				response.then().assertThat().body("data.size()", Matchers.equalTo(2));

				String date1 = response.jsonPath().getString("data[0]." + sortBy);
				String date2 = response.jsonPath().getString("data[1]." + sortBy);
				int comparison = date1.compareTo(date2);
				if (sortOrder.equals("asc"))
					Assert.assertTrue(comparison <= 0, "Expected " + sortBy + " in ascending order: data[0]=" + date1 + ", data[1]=" + date2);
				else if (sortOrder.equals("desc"))
					Assert.assertTrue(comparison >= 0, "Expected " + sortBy + " in descending order: data[0]=" + date1 + ", data[1]=" + date2);

				response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//searchInvoicesAPI.json"));
			}
		}
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchInvoiceWithMultipleAssociatedSlugsParams_PublicAPI() {
		
		Object[] setup = createAssociatedSlugsSearchSetup();
		int invoiceId = (Integer) setup[0];
		String candidateSlug = (String) setup[1];
		String companySlug = (String) setup[2];
		String contactSlug = (String) setup[3];
		String jobSlug = (String) setup[4];
		String dealSlug = (String) setup[5];

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("associated_candidate_slug", candidateSlug);
		queryParams.put("associated_company_slug", companySlug);
		queryParams.put("associated_contact_slug", contactSlug);
		queryParams.put("associated_job_slug", jobSlug);
		queryParams.put("associated_deal_slug", dealSlug);

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data[0].id", Matchers.equalTo(invoiceId));
		response.then().assertThat().body("data[0].associated_entities.candidate_slugs", Matchers.hasItem(candidateSlug));
		response.then().assertThat().body("data[0].associated_entities.company_slugs", Matchers.hasItem(companySlug));
		response.then().assertThat().body("data[0].associated_entities.contact_slugs", Matchers.hasItem(contactSlug));
		response.then().assertThat().body("data[0].associated_entities.job_slugs", Matchers.hasItem(jobSlug));
		response.then().assertThat().body("data[0].associated_entities.deal_slugs", Matchers.hasItem(dealSlug));
		
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//searchInvoicesAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchInvoiceWithInvalidSearchParam_PublicAPI() {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("invalid_param", "value");

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, queryParams, null, true);

		response.then().statusCode(500);
		response.then().assertThat().body("errorMessage", Matchers.is("UNSUPPORTED_SEARCH"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//invoiceUnsupportedSearchAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchInvoiceWithInvalidToken_PublicAPI() {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("invoice_id", String.valueOf(fakerInvoice.getRandomInvoiceId()));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA + "123", queryParams, null, true);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceSearchData", groups = "nightly-build")
	public void searchInvoiceWithCrossAccountToken_PublicAPI(int invoiceId, String invoiceIdStr, String companySlug, int invoiceStatus) {

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("invoice_id", invoiceIdStr);

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyB, queryParams, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("data", Matchers.empty());

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//invoiceEmptyResponseAPI.json"));
	}

	@DataProvider(parallel = true)
	public Object[][] getInvoiceSearchData() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
		Response createResponse = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
		int invoiceId = createResponse.jsonPath().get("data");

		Response getResponse = RestClient.doGet("JSON", baseURL, "invoices/" + invoiceId, apiKeyA, null, null, false);
		createResponse.then().statusCode(200);

		String invoiceIdStr = getResponse.jsonPath().getString("invoice_id");
		int invoiceStatus = getResponse.jsonPath().getInt("invoice_status");

		return new Object[][] { { invoiceId, invoiceIdStr, companySlug, invoiceStatus } };
	}

	@DataProvider(parallel = true)
	public Object[][] getInvoiceLimitAndPageData() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");

		Response createResponse1 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
		int invoiceId1 = createResponse1.jsonPath().get("data");

		Response createResponse2 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
		int invoiceId2 = createResponse2.jsonPath().get("data");

		return new Object[][] { { invoiceId1, invoiceId2, companySlug } };
	}

	@DataProvider(parallel = true)
	public Object[][] getInvoiceDatesData() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
		Response createResponse = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
		int invoiceId = createResponse.jsonPath().get("data");

		Response getResponse = RestClient.doGet("JSON", baseURL, "invoices/" + invoiceId, apiKeyA, null, null, false);
		createResponse.then().statusCode(200);

		String invoiceIssueDate = getResponse.jsonPath().getString("issue_date");
		String invoiceDueDate = getResponse.jsonPath().getString("due_date");

		return new Object[][] { { invoiceId, invoiceIssueDate, invoiceDueDate } };
	}

	private Object[] createAssociatedSlugsSearchSetup() {
		String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		String contactSlug = function.createNewContact_POST(baseURL, apiKeyA, companySlug).jsonPath().getString("slug");
		String jobSlug = function.createNewJob(baseURL, apiKeyA, companySlug, contactSlug).jsonPath().getString("slug");
		String dealSlug = function.createNewDealWithMandatoryFields(baseURL, apiKeyA).jsonPath().get("slug");

		int candidateId = allCrudFunctions.getCandidateResponse(albatrossURL, albatrossTknA, candidateSlug).jsonPath().getInt("data.candidate.id");
		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().getInt("data.company.id");
		int contactId = allCrudFunctions.getContactResponse(albatrossURL, albatrossTknA, contactSlug).jsonPath().getInt("data.contact.id");
		int jobId = allCrudFunctions.getJobResponse(albatrossURL, albatrossTknA, jobSlug).jsonPath().getInt("data.job.id");
		int dealId = allCrudFunctions.getDealResponse(albatrossURL, albatrossTknA, dealSlug).jsonPath().getInt("data.deal.id");

		Response createResponse = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
		int invoiceId = createResponse.jsonPath().get("data");

		Map<String, List<Integer>> associations = new HashMap<>();
		associations.put("2", Collections.singletonList(contactId));
		associations.put("3", Collections.singletonList(companyId));
		associations.put("4", Collections.singletonList(jobId));
		associations.put("5", Collections.singletonList(candidateId));
		associations.put("11", Collections.singletonList(dealId));

		UpdateInvoiceRequest body = new UpdateInvoiceRequest();
		body.setAssociations(associations);

		Response patchResponse = RestClient.doPatchOnce("JSON", invoiceServiceURL, "invoices/" + invoiceId, albatrossTknA, null, true, body);
		patchResponse.then().statusCode(200);

		return new Object[] { invoiceId, candidateSlug, companySlug, contactSlug, jobSlug, dealSlug };
	}

	@DataProvider(parallel = true)
	public Object[][] getAssociatedSlugsSearchData() {
		Object[] setup = createAssociatedSlugsSearchSetup();
		int invoiceId = (Integer) setup[0];
		String candidateSlug = (String) setup[1];
		String companySlug = (String) setup[2];
		String contactSlug = (String) setup[3];
		String jobSlug = (String) setup[4];
		String dealSlug = (String) setup[5];

		return new Object[][] {
				{ invoiceId, "associated_candidate_slug", candidateSlug, candidateSlug, companySlug, contactSlug, jobSlug, dealSlug },
				{ invoiceId, "associated_company_slug", companySlug, candidateSlug, companySlug, contactSlug, jobSlug, dealSlug },
				{ invoiceId, "associated_contact_slug", contactSlug, candidateSlug, companySlug, contactSlug, jobSlug, dealSlug },
				{ invoiceId, "associated_job_slug", jobSlug, candidateSlug, companySlug, contactSlug, jobSlug, dealSlug },
				{ invoiceId, "associated_deal_slug", dealSlug, candidateSlug, companySlug, contactSlug, jobSlug, dealSlug } };
	}

	@DataProvider(parallel = true)
	public Object[][] getInvoiceLimitParamsWithIds() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");

		int invoiceId1 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId).jsonPath().get("data");
		Response getResponse1 = RestClient.doGet("JSON", baseURL, "invoices/" + invoiceId1, apiKeyA, null, null, false);
		getResponse1.then().statusCode(200);
		String invoiceIdStr1 = getResponse1.jsonPath().getString("invoice_id");

		int invoiceId2 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId).jsonPath().get("data");
		Response getResponse2 = RestClient.doGet("JSON", baseURL, "invoices/" + invoiceId2, apiKeyA, null, null, false);
		getResponse2.then().statusCode(200);
		String invoiceIdStr2 = getResponse2.jsonPath().getString("invoice_id");

		return new Object[][] {
				// limit, page, expectedDataId1, expectedDataId2, expectedSize, expectedData0InvoiceIdStr, expectedData1InvoiceIdStr, companySlug
				{ 1, 1, invoiceId1, null, 1, invoiceIdStr1, null, companySlug },
				{ 1, 2, invoiceId2, null, 1, invoiceIdStr2, null, companySlug },
				{ 2, 1, invoiceId1, invoiceId2, 2, invoiceIdStr1, invoiceIdStr2, companySlug },
				{ 2, 2, null, null, 0, null, null, companySlug } };
	}

	@DataProvider(parallel = true)
	public Object[][] getInvoiceSortOrderParamsWithIds() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");

		int invoiceId1 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId).jsonPath().get("data");
		Response getResponse1 = RestClient.doGet("JSON", baseURL, "invoices/" + invoiceId1, apiKeyA, null, null, false);
		getResponse1.then().statusCode(200);
		String invoiceIdStr1 = getResponse1.jsonPath().getString("invoice_id");

		int invoiceId2 = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId).jsonPath().get("data");
		Response getResponse2 = RestClient.doGet("JSON", baseURL, "invoices/" + invoiceId2, apiKeyA, null, null, false);
		getResponse2.then().statusCode(200);
		String invoiceIdStr2 = getResponse2.jsonPath().getString("invoice_id");

		return new Object[][] { { invoiceId1, invoiceId2, invoiceIdStr1, invoiceIdStr2, companySlug }, };
	}
}