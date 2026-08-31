package io.rcrm.api.invoice;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetInvoiceTemplatesAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	String albatrossTknA;
	String albatrossTknB;
	commanFunction function;
	String basePath = "invoice-templates";
	int accountOwnerId;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknB = getTokenForAccount("AccountB", "valid");
		function = new commanFunction();
		accountOwnerId = getRoleBasedId("AccountA", "Owner");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getInvoiceStandardTemplatesWithValidToken_PublicAPI() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("$", Matchers.notNullValue());
		response.then().assertThat().body("$", Matchers.hasSize(3));
		response.then().assertThat().body("template_name", Matchers.hasItems("Consulting Services", "Part-Time Job", "Full-Time Job"));
		response.then().assertThat().body("due_date", Matchers.hasItem("30 Days"));

		List<String> expectedTemplateNames = Arrays.asList(
				"Consulting Services", 
				"Part-Time Job", 
				"Full-Time Job");
		List<List<String>> expectedFieldNames = Arrays.asList(
				Arrays.asList("Description", "Hours", "Bill Rate", "Amount"),
				Arrays.asList("Hours", "Bill Rate", "Start Date", "End Date", "Amount"),
				Arrays.asList("Salary", "Placement Fee", "Start Date", "Amount"));

		for (int i = 0; i < expectedTemplateNames.size(); i++) {
			String prefix = "[" + i + "]";
			response.then().assertThat().body(prefix + ".template_name", Matchers.is(expectedTemplateNames.get(i)));
			response.then().assertThat().body(prefix + ".shared_with.users", Matchers.hasItem(accountOwnerId));
			response.then().assertThat().body(prefix + ".invoice_fields.size()", Matchers.greaterThanOrEqualTo(1));
			response.then().assertThat().body(prefix + ".due_date", Matchers.is("30 Days"));
			response.then().assertThat().body(prefix + ".created_by", Matchers.is(accountOwnerId));
			response.then().assertThat().body(prefix + ".invoice_fields.field_name", Matchers.hasItems(expectedFieldNames.get(i).toArray(new String[0])));
		}

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//getInvoiceTemplatesAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getTemplateId", groups = "nightly-build")
	public void getInvoiceCustomTemplatesWithValidToken_PublicAPI(int templateId, String templateName) {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("$", Matchers.hasSize(4));
		response.then().assertThat().body("[0].template_id", Matchers.notNullValue());
		response.then().assertThat().body("[0].template_name", Matchers.is(templateName));
		response.then().assertThat().body("[0].shared_with.users[0]", Matchers.is(accountOwnerId));
		response.then().assertThat().body("[0].invoice_fields.size()", Matchers.is(1));
		response.then().assertThat().body("[0].due_date", Matchers.containsString("7 Days"));
		response.then().assertThat().body("[0].created_by", Matchers.is(accountOwnerId));
		response.then().assertThat().body("[0].invoice_fields[0].field_id", Matchers.is(1));
		response.then().assertThat().body("[0].invoice_fields[0].field_name", Matchers.is("Amount"));
		response.then().assertThat().body("[0].invoice_fields[0].field_type", Matchers.is("number"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//getInvoiceTemplatesAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getInvoiceTemplatesWithInvalidRequestType_PublicAPI() {

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, null);

		response.then().statusCode(405);
		response.then().assertThat().body("exception", Matchers.is("Symfony\\Component\\HttpKernel\\Exception\\MethodNotAllowedHttpException"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getInvoiceTemplatesWithInvalidToken_PublicAPI() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA + "123", null, null, true);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getTemplateId", groups = "nightly-build")
	public void getInvoiceTemplatesWithCrossAccountToken_PublicAPI(int templateId, String templateName) {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyB, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("$", Matchers.notNullValue());
		response.then().assertThat().body("$", Matchers.hasSize(3));
		response.then().assertThat().body("template_name", Matchers.hasItems("Consulting Services", "Part-Time Job", "Full-Time Job"));
		response.then().assertThat().body("due_date", Matchers.hasItem("30 Days"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//getInvoiceTemplatesAPI.json"));
	}

	@DataProvider(parallel = true)
	public Object[][] getTemplateId() {
		Response response = function.createInvoiceTemplate(invoiceServiceURL, albatrossTknA, baseURL, apiKeyA, syncFunctionURL);
		int templateId = response.jsonPath().get("data.id");
		String templateName = response.jsonPath().get("data.templateName");
		return new Object[][] { { templateId, templateName } };
	}
}