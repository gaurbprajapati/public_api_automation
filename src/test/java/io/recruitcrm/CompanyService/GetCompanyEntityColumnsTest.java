package io.recruitcrm.CompanyService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.pojo.albatross.CustomFieldAlbatross;
import io.rcrm.api.pojo.albatross.ExtraField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCompanyEntityColumnsTest extends TestBase {

	private static final String BASE_PATH = "entity-columns";
	JavaFakerCustomField faker = new JavaFakerCustomField();
	String albatrossAuthToken;
	String createdFieldName; // Store the field name for verification

	public GetCompanyEntityColumnsTest() {
		super();
	}

	@BeforeClass(alwaysRun = true)
	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		createdFieldName = createCompanyCustomFields("text", "text");
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"company_service", "nightly-build"})
	public void getCompanyEntityColumnsTest_200() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "companies");
		Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message").toString(), "Entity Column Fetched Successfully");
		Map<String, Object> columnsMap = response.jsonPath().getMap("data[0].columns");
		List<String> requiredColumns = Arrays.asList(
				"id", "parentcompanyid", "parentcompanyslug", "haschildren", "hasparent", "srno",
				"companyname", "aboutcompany", "parentcompanyname", "address", "city", "industryid",
				"industry", "website", "totalopenjob", "totalclosedjob", "totalonholdjob",
				"totalcanceledjob", "logo", "ownerid", "profilefacebook", "profiletwitter",
				"profilelinkedin", "ownername", "creatorname", "accountid", "deleted", "createdby",
				"createdon", "updatedon", "updatedby", "slug", "note", "hotlist", "off_limit_status",
				"contactid", "contact_linked", "locality", "state", "country", "postal_code",
				"linkedin_id", "indeed_opted_out", "existing_contact",
				"last_meeting_created_on", "parentcompanywebsite", "parentcompanyprofilelinkedin");
		for (String column : requiredColumns) {
			Assert.assertTrue(columnsMap.containsKey(column), "Missing expected column: " + column);
		}
		boolean isCustomFieldPresent = columnsMap.keySet().stream().anyMatch(name -> name.startsWith("custcolumn"));
		Assert.assertTrue(isCustomFieldPresent, "No custom field column found in the response!");

		// Validate id column structure - verify all required fields are present
		Map<String, Object> idColumn = response.jsonPath().getMap("data[0].columns.id");
		assertThat("Id column should not be null", idColumn, notNullValue());
		String[] idColumnRequiredFields = {"detailPageOrderV2", "detailPageVisible", "primaryOrder"};
		for (String field : idColumnRequiredFields) {
			assertThat("Id column missing required field: " + field, idColumn.containsKey(field), is(true));
		}
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"company_service", "nightly-build"})
	public void getCompanyEntityColumnsUnauthorizedTest_401() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "companies");

		Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, "InvalidToken", queryParameters, null, true);
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(response.jsonPath().get("meta.message"), "Unauthorised access");
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"company_service", "nightly-build"})
	public void getCompanyEntityColumnsTest_404() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "companies");

		Response response = RestClient.doGet("JSON", companyServiceURL, "ntity-columns", albatrossAuthToken, queryParameters, null, true); // Incorrect URL
		Assert.assertEquals(response.getStatusCode(), 404);
		Assert.assertEquals(response.jsonPath().get("error"), "Not Found");
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"company_service", "nightly-build"})
	public void getCompanyEntityColumnsTest_400() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", ""); // Missing entity

		Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);
		Assert.assertEquals(response.getStatusCode(), 400);
		Assert.assertEquals(response.jsonPath().get("meta.responseType.context"), "Error while processing request");
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"company_service", "nightly-build"})
	public void getCompanyEntityColumnsTest_405() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "companies");

		Response response = RestClient.doPost("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken,
				queryParameters, true, null);
		Assert.assertEquals(response.getStatusCode(), 405);
		Assert.assertEquals(response.jsonPath().get("error"), "Method Not Allowed");
	}

	@Owner("Suhel Bhadane")
	@Test(dataProvider = "entityDataProvider", groups = {"company_service", "nightly-build"})
	public void getCompanyEntityColumnsTestInvalidEntityTypes_400(String entity, int expectedStatus) {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", entity);

		Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken,
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), expectedStatus, "Unexpected status for entity: " + entity);

		if (expectedStatus == 400) {
			Assert.assertEquals(response.jsonPath().getInt("meta.status"), 400, "Incorrect status code in response");
			Assert.assertEquals(response.jsonPath().getInt("meta.responseType.code"), 101, "Incorrect response code");
			Assert.assertEquals(response.jsonPath().getString("meta.responseType.context"), "Error while processing request", "Incorrect response context");
			Assert.assertEquals(response.jsonPath().getString("errors[0].message"), "Entity must be one of the predefined enum values", "Incorrect error message");
			Assert.assertEquals(response.jsonPath().getInt("errors[0].errorType.code"), 201, "Incorrect error type code");
			Assert.assertEquals(response.jsonPath().getString("errors[0].errorType.context"), "Validation Error", "Incorrect error type context");
		}
	}

	public String createCompanyCustomFields(String extraFieldType, String defaultValue) {
		String customFieldName = faker.getCustomFieldName("companies");
		String basePath = "custom-fields";

		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(3);
		extraField.setExtrafieldname(customFieldName);
		extraField.setDefaultvalue(faker.getDefaultvalue(defaultValue));
		extraField.setExtrafieldtype(extraFieldType);
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
		Assert.assertEquals(response.getStatusCode(), 200);
		return customFieldName;
	}

	@DataProvider(name = "entityDataProvider")
	public Object[][] entityDataProvider() {
		return new Object[][] { { "candidates", 422 }, { "jobs", 404 }, { "contacts", 404 },
				{ "12345", 400 }, { "invalidEntity1121309", 400 }, { "@#$@", 400 }, };
	}
}

