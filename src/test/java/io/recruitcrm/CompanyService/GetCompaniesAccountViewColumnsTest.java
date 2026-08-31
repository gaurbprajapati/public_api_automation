package io.recruitcrm.CompanyService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

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
public class GetCompaniesAccountViewColumnsTest extends TestBase {

	private static final String BASE_PATH = "account-view-columns";
	JavaFakerCustomField faker = new JavaFakerCustomField();
	String albatrossAuthToken;

	public GetCompaniesAccountViewColumnsTest() {
		super();
	}

	@BeforeClass(alwaysRun = true)
	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"company_service", "nightly-build"})
	public void getCompaniesAccountViewColumns_200() {
		String customTextField = createCompanyCustomFields("text", "text");
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "companies");
		Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);
        assert response != null;
        assertThat(response.getStatusCode(), equalTo(200));
		// Validate meta information
		assertThat("Expected success message", response.jsonPath().getString("meta.message"), equalTo("Account View Columns Fetched Successfully"));
		assertThat("Expected response type code 103", response.jsonPath().getInt("meta.responseType.code"), equalTo(103));
		assertThat("Expected success context", response.jsonPath().getString("meta.responseType.context"), equalTo("Request is successful"));
		assertThat("Expected status 200 in meta", response.jsonPath().getInt("meta.status"), equalTo(200));
		assertThat("Expected timestamp not null", response.jsonPath().get("meta.timestamp"), notNullValue());
		assertThat("Expected requestUuid not null", response.jsonPath().get("meta.requestUuid"), notNullValue());

		// Validate data array exists and has at least one element
		assertThat("Data array should not be empty", response.jsonPath().getList("data").size(), greaterThan(0));
		assertThat("First data element should not be null", response.jsonPath().get("data[0]"), notNullValue());
		
		// Validate accountViewColumns structure
		Map<String, Object> accountViewColumnsMap = response.jsonPath().getMap("data[0].accountViewColumns");
		assertThat("Account view columns should not be null", accountViewColumnsMap, notNullValue());

		// Validate important required columns based on actual API response structure
		String[] requiredColumns = {
				"id", "parentcompanyid", "parentcompanyslug", "haschildren", "hasparent", "srno", 
				"companyname", "aboutcompany", "parentcompanyname", "address", "city", "industryid", 
				"industry", "website", "totalopenjob", "totalclosedjob", "totalonholdjob", 
				"totalcanceledjob", "logo", "ownerid", "profilefacebook", "profiletwitter",
				"profilelinkedin", "ownername", "creatorname", "accountid", "deleted", "createdby", 
				"createdon", "updatedon", "updatedby", "slug", "note", "hotlist", "off_limit_status", 
				"contactid", "contact_linked", "locality", "state", "country", "postal_code", 
				"linkedin_id", "indeed_opted_out", "existing_contact",
				"last_meeting_created_on", "parentcompanywebsite", "parentcompanyprofilelinkedin"
		};
		for (String column : requiredColumns) {
			assertThat("Missing expected column: " + column, accountViewColumnsMap.containsKey(column), is(true));
		}
		
		// Validate id column structure - verify all required fields are present
		Map<String, Object> idColumn = response.jsonPath().getMap("data[0].accountViewColumns.id");
		assertThat("Id column should not be null", idColumn, notNullValue());
		String[] idColumnRequiredFields = {
				"entity", "field", "visible", "visible_locked",
				"detailPageOrder", "detailPageOrderV2", "listPageOrder", "detailPageVisible"
		};
		for (String field : idColumnRequiredFields) {
			assertThat("Id column missing required field: " + field, idColumn.containsKey(field), is(true));
		}
		// Validate custom field is present
		Map<String, Object> accountViewColumns = response.jsonPath().getMap("data[0].accountViewColumns");
		boolean isCustomFieldPresent = accountViewColumns.entrySet().stream().anyMatch(entry -> entry.getValue() instanceof Map && customTextField.equals(((Map<?, ?>) entry.getValue()).get("label")));
		assertThat("Custom field '" + customTextField + "' not found in accountViewColumns!", isCustomFieldPresent, is(true));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"company_service", "nightly-build"})
	public void getCompaniesAccountViewColumns_401() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "companies");

		Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, "InvalidToken", queryParameters, null, true);

        assert response != null;
        assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
		assertThat("Expected unauthorized message", response.jsonPath().getString("meta.message"), equalTo("Unauthorised access"));
	}

	@Owner("Suhel Bhadane")
	@Test(dataProvider = "unauthorizedTokenProvider", groups = {"company_service", "nightly-build"})
	public void getCompaniesAccountViewColumns_401_WithNonOwnerTokens(String role, String token) {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "companies");

		Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, token, queryParameters, null, true);
        assert response != null;
        assertThat("Expected status code 403 for " + role + " token", response.getStatusCode(), equalTo(403));

		assertThat("Expected unauthorized message for " + role + " token", response.jsonPath().getString("errors[0].message"), equalTo("Only account owner can access account view columns."));
	}


	@Owner("Suhel Bhadane")
	@Test(groups = {"company_service", "nightly-build"})
	public void getCompaniesAccountViewColumns_404() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "companies");

		Response response = RestClient.doGet("JSON", companyServiceURL, "ccount-view-columns", albatrossAuthToken, queryParameters, null, true);

        assert response != null;
        assertThat("Expected status code 404", response.getStatusCode(), equalTo(404));
		assertThat("Expected not found error", response.jsonPath().getString("error"), equalTo("Not Found"));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"company_service", "nightly-build"})
	public void getCompaniesAccountViewColumns_405() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "companies");

		Response response = RestClient.doPost("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, false, null);

		assertThat("Expected status code 405", response.getStatusCode(), equalTo(405));
		assertThat("Expected method not allowed error", response.jsonPath().getString("error"), equalTo("Method Not Allowed"));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"company_service", "nightly-build"})
	public void getCompaniesAccountViewColumns_400() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", ""); // Empty entity parameter

		Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assert response != null;
        assertThat("Expected status code 400", response.getStatusCode(), equalTo(400));
		assertThat("Expected error context", response.jsonPath().getString("meta.responseType.context"), equalTo("Error while processing request"));
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

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false,
				customField);
		assertThat(response.getStatusCode(), equalTo(200));
		return customFieldName;
	}


	@DataProvider(name = "unauthorizedTokenProvider")
	public Object[][] unauthorizedTokenProvider() {
		return new Object[][] {
				{ "Admin", ThreadManager.getAlbatrossToken("Admin") },
				{ "TeamMember", ThreadManager.getAlbatrossToken("TeamMember") },
				{ "RestrictedTeamMember", ThreadManager.getAlbatrossToken("RestrictedTeamMember") }
		};
	}
}

