package io.rcrm.api.placement;

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
public class GetPlacementsCustomFieldsAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	commanFunction function;
	String albatrossTknA;
	String albatrossTknB;
	JavaFakerCustomField customFieldFaker;
	String basePath = "custom-fields/placements";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknB = getTokenForAccount("AccountB", "valid");
		function = new commanFunction();
		customFieldFaker = new JavaFakerCustomField();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createPlacementCustomField", groups = "nightly-build")
	public void getPlacementsCustomFieldsWithValidToken_PublicAPI(int customFieldId1, String customFieldName1, int customFieldId, int customFieldId2, String customFieldName2) {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("$", Matchers.hasSize(2));
		response.then().assertThat().body("[0].field_id", Matchers.is(customFieldId1));
		response.then().assertThat().body("[0].field_name", Matchers.is(customFieldName1));
		response.then().assertThat().body("[1].field_id", Matchers.is(customFieldId2));
		response.then().assertThat().body("[1].field_name", Matchers.is(customFieldName2));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//job//customFields.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createPlacementCustomField", groups = "nightly-build")
	public void getPlacementsCustomFieldsWithDeletedCustomFields_PublicAPI(int customFieldId1, String customFieldName1, int customFieldId, int customFieldId2, String customFieldName2) {

		Response deleteResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields/delete/" + customFieldId, albatrossTknA, null, false, null);

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
	@Test(groups = "nightly-build")
	public void getPlacementsCustomFieldsWithEmptyData_PublicAPI() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("$", Matchers.empty());
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getPlacementsCustomFieldsWithInvalidToken_PublicAPI() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyA + "123", null, null, true);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createPlacementCustomField", groups = "nightly-build")
	public void getPlacementsCustomFieldsWithCrossAccountToken_PublicAPI(int customFieldId1, String customFieldName1, int customFieldId, int customFieldId2, String customFieldName2) {

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiKeyB, null, null, true);

		response.then().statusCode(200);
		response.then().assertThat().body("$", Matchers.empty());
	}

	@DataProvider(parallel = true)
	public Object[][] createPlacementCustomField() {
		String customFieldName1 = customFieldFaker.getCustomFieldName("placement");
		Response customFieldResponse1 = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, "placement", customFieldName1, "text", "");
		int customFieldId1 = customFieldResponse1.jsonPath().get("data.custumField.columnid");
		int customFieldId = customFieldResponse1.jsonPath().get("data.custumField.id");
		String customFieldName2 = customFieldFaker.getCustomFieldName("placement");
		Response customFieldResponse2 = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, "placement", customFieldName2, "dropdown", "QA Team, Developer Team");
		int customFieldId2 = customFieldResponse2.jsonPath().get("data.custumField.columnid");
		return new Object[][] { { customFieldId1, customFieldName1, customFieldId, customFieldId2, customFieldName2 } };
	}
}