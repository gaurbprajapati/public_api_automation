package io.recruitcrm.albatross.enterprise;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.*;
import org.testng.annotations.*;
import io.rcrm.api.testbase.TestBase.AccountType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetEnterpriseAddonsLimitsTest extends TestBase {

	private String basePath;
    String albatrossTknA;
	String albatrossTknInvalidA;
    String albatrossTknB;

	@BeforeClass(alwaysRun = true)	public void setup() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
		basePath = "plans-and-billing/get-enterprise-addons-limits";
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getEnterpriseAddonsLimits_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, null, true);
		JsonPath jsonPath = response.jsonPath();

		assertThat(response.getStatusCode(), is(200));
		assertThat(jsonPath.getString("response_message"), is("Successfully retrieved enterprise addon limits"));
		assertThat(jsonPath.getInt("status"), is(200));
		assertThat(jsonPath.getList("data").isEmpty(), is(false));

        List<Map<String, Object>> addons = jsonPath.getList("data");
        boolean hasAdvancedAnalytics = addonExists(addons, "Advanced Analytics", null, true);
        boolean hasCallingCredits = addonExists(addons, "Calling Credits", 25, false);
        boolean hasDataEnrichment = addonExists(addons, "Data Enrichment", 500, false);
        boolean hasJobAdvertising = addonExists(addons, "Job Advertising", 10, false);
        boolean hasLinkedinMessaging = addonExists(addons, "Linkedin Messaging", null, true);
        boolean hasRecruitCraft = addonExists(addons, "RecruitCraft", null, true);
        boolean hasWorkflowAutomation = addonExists(addons, "Workflow Automation", 1000, false);
        
        assertThat("Advanced Analytics addon not found or invalid.", hasAdvancedAnalytics, is(true));
        assertThat("Calling Credits addon not found or invalid.", hasCallingCredits, is(true));
        assertThat("Data Enrichment addon not found or invalid.", hasDataEnrichment, is(true));
        assertThat("Job Advertising addon not found or invalid.", hasJobAdvertising, is(true));
        assertThat("Linkedin Messaging addon not found or invalid.", hasLinkedinMessaging, is(true));
        assertThat("RecruitCraft addon not found or invalid.", hasRecruitCraft, is(true));
        assertThat("Workflow Automation addon not found or invalid.", hasWorkflowAutomation, is(true));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getEnterpriseAddonsLimitsUnauthorized_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, null, true);
		validateUnauthorizedResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getEnterpriseAddonsLimitsWithEmptyToken_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, "", null, null, true);
		validateUnauthorizedResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void crossAccountGetEnterpriseAddonsLimits_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknB, null, null, true);
		JsonPath jsonPath = response.jsonPath();
		assertThat(response.getStatusCode(), is(200));
		assertThat(jsonPath.getString("response_message"), is("Successfully retrieved enterprise addon limits"));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getEnterpriseAddonsLimitsWithAdminToken_Test() {
		String adminToken = getRoleBasedToken("AccountA", "Admin");
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, adminToken, null, null, true);
		validateAccessDeniedResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getEnterpriseAddonsLimitsWithTeamMemberToken_Test() {
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, teamMemberToken, null, null, true);
		validateAccessDeniedResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getEnterpriseAddonsLimitsWithRestrictedTeamMemberToken_Test() {
		String restrictedToken = getRoleBasedToken("AccountA", "Restricted");
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, restrictedToken, null, null, true);
		validateAccessDeniedResponse(response);
	}

    private boolean addonExists(List<Map<String, Object>> addons, String name, Integer limit, boolean noLimit) {
        return addons.stream().anyMatch(
            m -> name.equals(m.get("name")) && (limit == null ? m.get("limit") == null : limit.equals(m.get("limit"))) && Boolean.valueOf(noLimit).equals(m.get("no_limit"))
        );
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
}

