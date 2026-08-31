package io.recruitcrm.albatross.enterprise;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import org.testng.annotations.*;
import io.rcrm.api.testbase.TestBase.AccountType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetCurrentAddonsLimitsTest extends TestBase {

	private String basePath;
	String albatrossTknA;
	String albatrossTknInvalidA;
    String albatrossTknB;

	@BeforeClass(alwaysRun = true)	public void setup() {
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
		basePath = "plans-and-billing/get-current-addons-limits";
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getCurrentAddonsLimitsForEnterprise_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, null, true);
		validateSuccessResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getCurrentAddonsLimitsForEnterpriseUnauthorized_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, null, true);
		validateUnauthorizedResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getCurrentAddonsLimitsForEnterpriseWithEmptyToken_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, "", null, null, true);
		validateUnauthorizedResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void crossAccountGetCurrentAddonsLimitsForEnterprise_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknB, null, null, true);
		validateSuccessResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getCurrentAddonsLimitsForEnterpriseWithAdminToken_Test() {
		String adminToken = getRoleBasedToken("AccountA", "Admin");
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, adminToken, null, null, true);
		validateAccessDeniedResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getCurrentAddonsLimitsForEnterpriseWithTeamMemberToken_Test() {
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, teamMemberToken, null, null, true);
		validateAccessDeniedResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getCurrentAddonsLimitsForEnterpriseWithRestrictedTeamMemberToken_Test() {
		String restrictedToken = getRoleBasedToken("AccountA", "Restricted");
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, restrictedToken, null, null, true);
		validateAccessDeniedResponse(response);
	}

	private void validateAccessDeniedResponse(Response response) {
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Access Denied"));
		assertThat(response.jsonPath().getString("message_type"), is("is-danger"));
	}

	private void validateUnauthorizedResponse(Response response) {
		assertThat(response.getStatusCode(), is(401));
		assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
	}

	private void validateSuccessResponse(Response response) {
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("response_message"), is("Successfully retrieved addons current subscriptions"));
		assertThat(response.jsonPath().getInt("status"), is(200));
	}
}

