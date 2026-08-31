package io.rcrm.api.invoice;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
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
public class DeleteInvoiceByIdAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	commanFunction function;
	JavaFakerInvoice fakerInvoice;
	AllCrudFunctions allCrudFunctions;
	String albatrossTknA;
	String basePath = "invoices/{id}";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		function = new commanFunction();
		allCrudFunctions = new AllCrudFunctions();
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		fakerInvoice = new JavaFakerInvoice();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceId")
	public void deleteInvoiceByIdWithValidToken_PublicAPI(int invoiceId) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiKeyA, null, pathParams, true);

		response.then().statusCode(200);
		response.then().assertThat().body("message", Matchers.is("Deleted Successfully"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//deleteInvoiceAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceId", groups = "nightly-build")
	public void deleteAlreadyDeletedInvoice_PublicAPI(int invoiceId) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		Response deleteResponse = RestClient.doDelete("JSON", baseURL, basePath, apiKeyA, null, pathParams, true);
		deleteResponse.then().statusCode(200);
		deleteResponse.then().assertThat().body("message", Matchers.is("Deleted Successfully"));

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiKeyA, null, pathParams, true);

		response.then().statusCode(404);
		response.then().assertThat().body("errorMessage", Matchers.is("Invoice not found"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//invoicesNotExistsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void deleteInvoiceByIdWithInvalidId_PublicAPI() {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(fakerInvoice.getRandomInvoiceId()));

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiKeyA, null, pathParams, true);
		response.then().statusCode(404);
		response.then().assertThat().body("errorMessage", Matchers.is("Invoice not found"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//invoicesNotExistsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void deleteInvoiceByIdWithInvalidToken_PublicAPI() {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", "1");

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiKeyA + "123", null, pathParams, true);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceId", groups = "nightly-build")
	public void deleteInvoiceByIdWithCrossAccountToken_PublicAPI(int invoiceId) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiKeyB, null, pathParams, true);

		response.then().statusCode(404);
		response.then().assertThat().body("errorMessage", Matchers.is("Invoice not found"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//invoicesNotExistsAPI.json"));
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