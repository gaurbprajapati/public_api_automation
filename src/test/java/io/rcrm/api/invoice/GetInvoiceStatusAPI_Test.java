package io.rcrm.api.invoice;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetInvoiceStatusAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	commanFunction function;
	String basePath = "invoice-status";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		function = new commanFunction();
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getInvoiceStatusWithValidToken_PublicAPI() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("$", Matchers.hasSize(6));
		response.then().assertThat().body("id", Matchers.hasItems(1, 2, 3, 4, 5, 6));
		response.then().assertThat().body("status_label", Matchers.hasItems("Draft", "Paid", "Sent to Client", "Overdue", "Unpaid", "Cancelled"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//getInvoiceStatusAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getInvoiceStatusWithInvalidToken_PublicAPI() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA + "123", null, null, true);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getInvoiceStatusWithInvalidRequestType_PublicAPI() {

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, null);

		response.then().statusCode(405);
		response.then().assertThat().body("exception", Matchers.is("Symfony\\Component\\HttpKernel\\Exception\\MethodNotAllowedHttpException"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getInvoiceStatusWithCrossAccountToken_PublicAPI() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyB, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("$", Matchers.hasSize(6));
		response.then().assertThat().body("id", Matchers.hasItems(1, 2, 3, 4, 5, 6));
		response.then().assertThat().body("status_label", Matchers.hasItems("Draft", "Paid", "Sent to Client", "Overdue", "Unpaid", "Cancelled"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//getInvoiceStatusAPI.json"));
	}
}