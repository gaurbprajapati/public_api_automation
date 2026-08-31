package io.rcrm.api.invoice;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.annotations.*;
import org.json.JSONObject;

import java.util.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetInvoiceCustomFieldsAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	commanFunction function;
	JavaFakerCustomField customFieldFaker;
	String albatrossTknA;
	String basePath = "custom-fields/invoices";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		function = new commanFunction();
		customFieldFaker = new JavaFakerCustomField();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createInvoiceCustomFields", groups = "nightly-build")
	public void getInvoiceCustomFieldsWithValidToken_PublicAPI(int customFieldId1, String customFieldName1, int customFieldId2, String customFieldName2) {

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
	@Test(groups = "nightly-build")
	public void getInvoiceCustomFieldsWithEmptyData_PublicAPI() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("$", Matchers.notNullValue());
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createInvoiceCustomFields", groups = "nightly-build")
	public void getInvoiceCustomFieldsWithDeletedCustomFields_PublicAPI(int customFieldId1, String customFieldName1, int customFieldId2, String customFieldName2) {

		JSONObject requestBody = new JSONObject();
		requestBody.put("label", customFieldName1);
		requestBody.put("type", 1);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("fieldId", String.valueOf(customFieldId1));

		Response deleteResponse = RestClient.doDelete("JSON", invoiceServiceURL, "invoices/custom-fields", albatrossTknA, pathParamters, null, true, requestBody);

		deleteResponse.then().statusCode(200);
		deleteResponse.then().assertThat().body("data", Matchers.is("Custom Field Deleted successfully"));

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("$", Matchers.hasSize(1));
		response.then().assertThat().body("[0].field_id", Matchers.is(customFieldId2));
		response.then().assertThat().body("[0].field_name", Matchers.is(customFieldName2));
		response.then().assertThat().body("[0].default_value", Matchers.is("QA Team,Developer Team"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//job//customFields.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getInvoiceCustomFieldsWithInvalidToken_PublicAPI() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA + "123", null, null, true);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createInvoiceCustomFields", groups = "nightly-build")
	public void getInvoiceCustomFieldsWithCrossAccountToken_PublicAPI(int customFieldId1, String customFieldName1, int customFieldId2, String customFieldName2) {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyB, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("$", Matchers.empty());
	}

	@DataProvider(parallel = true)
	public Object[][] createInvoiceCustomFields() {
		String customFieldName1 = customFieldFaker.getCustomFieldName("invoice");
		Response customFieldResponse1 = function.createInvoiceCustomFieldsResponse(invoiceServiceURL, albatrossTknA, customFieldName1, "text", "");
		int customFieldId1 = customFieldResponse1.jsonPath().getInt("data.fieldId");
		String customFieldName2 = customFieldFaker.getCustomFieldName("invoice");
		Response customFieldResponse2 = function.createInvoiceCustomFieldsResponse(invoiceServiceURL, albatrossTknA, customFieldName2, "dropdown", "QA Team, Developer Team");
		int customFieldId2 = customFieldResponse2.jsonPath().getInt("data.fieldId");
		return new Object[][] { { customFieldId1, customFieldName1, customFieldId2, customFieldName2 } };
	}
}