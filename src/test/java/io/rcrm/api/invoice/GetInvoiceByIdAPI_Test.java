package io.rcrm.api.invoice;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.*;
import org.testng.annotations.*;

import java.util.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.JavaFakerInvoice;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetInvoiceByIdAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	String albatrossTknA;
	int accountOwnerId;
	commanFunction function;
	AllCrudFunctions allCrudFunctions;
	JavaFakerInvoice fakerInvoice;
	String basePath = "invoices/{id}";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		function = new commanFunction();
		accountOwnerId = getRoleBasedId("AccountA", "Owner");
		allCrudFunctions = new AllCrudFunctions();
		fakerInvoice = new JavaFakerInvoice();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceId", groups = "nightly-build")
	public void getInvoiceByIdWithValidToken_PublicAPI(int invoiceId, String companySlug) {
		
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, null, pathParams, true);

		response.then().statusCode(200);
		response.then().assertThat().body("id", Matchers.equalTo(invoiceId));
		response.then().assertThat().body("template.template_name", Matchers.is("Full-Time Job"));
		response.then().assertThat().body("billed_to_client.company_slug", Matchers.is(companySlug));
		String[] slugKeys = { "candidate_slugs", "contact_slugs", "company_slugs", "deal_slugs", "job_slugs" };
		Matcher<Collection<?>> empty = Matchers.empty();
		for (String key : slugKeys) {
			response.then().assertThat().body("associated_entities." + key, empty);
		}
		response.then().assertThat().body("total_amount", Matchers.notNullValue());
		response.then().assertThat().body("created_by", Matchers.is(accountOwnerId));
		response.then().assertThat().body("invoice_pdf", Matchers.containsString("http"));
		
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//getInvoiceByIdAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getInvoiceByIdWithInvalidId_PublicAPI() {
		
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(fakerInvoice.getRandomInvoiceId()));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, null, pathParams, true);

		response.then().statusCode(404);
		response.then().assertThat().body("errorMessage", Matchers.notNullValue());
		
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//invoicesNotExistsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getInvoiceByIdWithInvalidToken_PublicAPI() {
		
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", "1");

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA + "123", null, pathParams, true);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceId", groups = "nightly-build")
	public void getInvoiceByIdWithCrossAccountToken_PublicAPI(int invoiceId, String companySlug) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyB, null, pathParams, true);
		response.then().statusCode(404);

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//invoicesNotExistsAPI.json"));
	}

	@DataProvider(parallel = true)
	public Object[][] getInvoiceId() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
		Response response = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
		int invoiceId = response.jsonPath().get("data");
		return new Object[][] { { invoiceId, companySlug } };
	}
}