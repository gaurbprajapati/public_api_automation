package io.rcrm.api.invoice;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetInvoiceInternalCustomFieldsAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	commanFunction function;
	JavaFakerCustomField customFieldFaker;
	String albatrossTknA;
	String basePath = "internal-custom-fields/invoices";
	int columnId = 1;

	@BeforeClass
	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		function = new commanFunction();
		customFieldFaker = new JavaFakerCustomField();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceInternalCustomFieldsData")	
	public void getInvoiceInternalCustomFieldsWithValidToken_PublicAPI(int customFieldId1, String customFieldName1, int customFieldDbId1, int customFieldId2, String customFieldName2) {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, null, null, true);
		response.then().statusCode(200);

		response.then().assertThat().body("$", Matchers.hasSize(2));
		response.then().assertThat().body("[0].field_id", Matchers.is(customFieldId1));
		response.then().assertThat().body("[0].field_name", Matchers.is(customFieldName1));
		response.then().assertThat().body("[0].default_value", Matchers.is(""));
		response.then().assertThat().body("[1].field_id", Matchers.is(customFieldId2));
		response.then().assertThat().body("[1].field_name", Matchers.is(customFieldName2));
		response.then().assertThat().body("[1].default_value", Matchers.is("QA Team,Developer Team"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//getCustomFieldsInvoicesAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test
	public void getInvoiceInternalCustomFieldsWithEmptyData_PublicAPI() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("$", Matchers.notNullValue());
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceInternalCustomFieldsData")
	public void getInvoiceInternalCustomFieldsWithDeletedCustomFields_PublicAPI(int customFieldId1, String customFieldName1, int customFieldDbId1, int customFieldId2, String customFieldName2) {

		Response deleteResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields/delete/" + customFieldDbId1, albatrossTknA, null, false, null);

		deleteResponse.then().statusCode(200);
		deleteResponse.then().assertThat().body("message", Matchers.is("Custom Field Deleted Successfully"));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("$", Matchers.hasSize(1));
		response.then().assertThat().body("[0].field_id", Matchers.is(customFieldId2));
		response.then().assertThat().body("[0].field_name", Matchers.is(customFieldName2));
		response.then().assertThat().body("[0].default_value", Matchers.is("QA Team,Developer Team"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//job//customFields.json"));
	}

	@Owner("Sai Teja SG")
	@Test
	public void getInvoiceInternalCustomFieldsWithInvalidToken_PublicAPI() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA + "123", null, null, true);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test
	public void getInvoiceInternalCustomFieldsWithInvalidRequestType_PublicAPI() {

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, null);

		response.then().statusCode(405);
		response.then().assertThat().body("exception", Matchers.is("Symfony\\Component\\HttpKernel\\Exception\\MethodNotAllowedHttpException"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceInternalCustomFieldsData")
	public void getInvoiceInternalCustomFieldsWithCrossAccountToken_PublicAPI(int customFieldId1, String customFieldName1, int customFieldDbId1, int customFieldId2, String customFieldName2) {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyB, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("$", Matchers.empty());
	}

	@DataProvider(parallel = true)
	public Object[][] getInvoiceInternalCustomFieldsData() {
		String customFieldName1 = customFieldFaker.getCustomFieldName("invoice");
		Response customFieldResponse1 = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, "invoice", customFieldName1, "text", "", columnId);
		int customFieldId1 = customFieldResponse1.jsonPath().getInt("data.custumField.columnid");
		int customFieldDbId1 = customFieldResponse1.jsonPath().getInt("data.custumField.id");
		String customFieldName2 = customFieldFaker.getCustomFieldName("invoice");
		Response customFieldResponse2 = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, "invoice", customFieldName2, "dropdown", "QA Team, Developer Team", columnId);
		int customFieldId2 = customFieldResponse2.jsonPath().getInt("data.custumField.columnid");
		return new Object[][] { { customFieldId1, customFieldName1, customFieldDbId1, customFieldId2, customFieldName2 } };
	}
}