package io.recruitcrm.albatross.chromeExtension;

import java.util.*;
import io.rcrm.api.commanfunctions.commanFunction;
import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetDefaultOptionsTest_ExtensionTest extends TestBase {
    
	private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	String basePath = "extensions/chrome/custom-fields/get-default-options/{entityTypeId}";
	commanFunction function = new commanFunction();

	@BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
    public void getCandidateDefaultOptions_Test(Response companyResponse, Response contactResponse, Response candidateResponse) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("5");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, albatrossTknA, null, pathParamters, true);
		validateDefaultOptions(response, "Candidate_Option 1", "Candidate_Option 2");
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "createCustomFields", groups = "nightly-build")
    public void getCompanyDefaultOptions_Test(Response companyResponse, Response contactResponse, Response candidateResponse) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("3");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, albatrossTknA, null, pathParamters, true);
		validateDefaultOptions(response, "Company_Option 1", "Company_Option 2");
    }

	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
    public void getContactDefaultOptions_Test(Response companyResponse, Response contactResponse, Response candidateResponse) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("2");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, albatrossTknA, null, pathParamters, true);
		validateDefaultOptions(response, "Contact_Option 1", "Contact_Option 2");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
    public void getDefaultOptionsUnauthorized_Test(Response companyResponse, Response contactResponse, Response candidateResponse) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("2");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, pathParamters, true);
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message_type"), is("is-danger"));
		assertThat(response.jsonPath().getString("message"), is("Unauthorized access"));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
    public void getDefaultOptionsEmptyToken_Test(Response companyResponse, Response contactResponse, Response candidateResponse) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("2");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, "", null, pathParamters, true);
		assertThat(response.getStatusCode(), is(401));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
    public void getDefaultOptionsInvalidEntityTypeId_Test() {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("20");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, albatrossTknA, null, pathParamters, true);
		validateEmptyDataResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
    public void getDefaultOptionsEmptyEntityTypeId_Test() {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, albatrossTknA, null, pathParamters, true);
		assertThat(response.getStatusCode(), is(404));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
    public void getDefaultOptionsCrossAccount_Test(Response companyResponse, Response contactResponse, Response candidateResponse) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("2");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, albatrossTknB, null, pathParamters, true);
		validateEmptyDataResponse(response);
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
    public void getDefaultOptionsAdmin_Test(Response companyResponse, Response contactResponse, Response candidateResponse) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("5");
		String adminToken = getRoleBasedToken("AccountA", "Admin");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, adminToken, null, pathParamters, true);
		validateDefaultOptions(response, "Candidate_Option 1", "Candidate_Option 2");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
    public void getDefaultOptionsTeamMember_Test(Response companyResponse, Response contactResponse, Response candidateResponse) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("5");
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, teamMemberToken, null, pathParamters, true);
		validateDefaultOptions(response, "Candidate_Option 1", "Candidate_Option 2");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
    public void getDefaultOptionsRestrictedTeamMember_Test(Response companyResponse, Response contactResponse, Response candidateResponse) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("5");
		String restrictedToken = getRoleBasedToken("AccountA", "Restricted");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, restrictedToken, null, pathParamters, true);
		validateDefaultOptions(response, "Candidate_Option 1", "Candidate_Option 2");
	}

	private Map<String, String> createEntityTypeIdPathParameters(String entityTypeId) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("entityTypeId", entityTypeId);
        return pathParameters;
    }

	private void validateDefaultOptions(Response response, String option1, String option2) {
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
		assertThat(response.jsonPath().getString("message"), is("Default options for entity custom fields"));
		assertThat(response.jsonPath().getString("data.values()[0][0].label"), is(option1));
		assertThat(response.jsonPath().getString("data.values()[0][1].label"), is(option2));
	}

	private void validateEmptyDataResponse(Response response) {
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
		assertThat(response.jsonPath().getString("message"), is("Default options for entity custom fields"));
		assertThat(response.jsonPath().getList("data").isEmpty(), is(true));
	}

	@DataProvider
    public Object[][] createCustomFields() {
        Response companyResponse = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, "company", "companyField", "dropdown", "Company_Option 1,Company_Option 2");
		Response contactResponse = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, "contact", "contactField", "dropdown", "Contact_Option 1,Contact_Option 2");
		Response candidateResponse = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, "candidate", "candidateField", "dropdown", "Candidate_Option 1,Candidate_Option 2");
        return new Object[][] { { companyResponse, contactResponse, candidateResponse } };
    }
}
