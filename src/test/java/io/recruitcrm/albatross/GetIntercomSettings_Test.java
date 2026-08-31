package io.recruitcrm.albatross;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetIntercomSettings_Test extends TestBase {
    private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	String basePath = "get-intercom-settings";

    @BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
	}

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerUserId", groups = "nightly-build")
    public void getIntercomSettings_Test(String userId) {
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, null, true);
        validateCommonResponse(response);
        assertThat(response.jsonPath().getInt("user.accountid"), is(getAccountId("AccountA")));
        assertThat(response.jsonPath().getString("user.id"), is(userId));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getIntercomSettingsUnauthorized_Test() {
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, null, true);
        validateCommonResponse(response);
        assertThat(response.jsonPath().getString("user"), is(nullValue()));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getIntercomSettingsEmptyToken_Test() {
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, "", null, null, true);
        validateCommonResponse(response);
        assertThat(response.jsonPath().getString("user"), is(nullValue()));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerUserId", groups = "nightly-build")
    public void getIntercomSettingsCrossAccount_Test(String userId) {
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknB, null, null, true);
        validateCommonResponse(response);
        assertThat(response.jsonPath().getInt("user.accountid"), is(not(getAccountId("AccountA"))));
        assertThat(response.jsonPath().getString("user.id"), is(not(userId)));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerUserId", groups = "nightly-build")
    public void getIntercomSettingsAdminToken_Test(String userId) {
        String adminToken = getRoleBasedToken("AccountA", "Admin"); 
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, adminToken, null, null, true);
        validateCommonResponse(response);
        assertThat(response.jsonPath().getInt("user.accountid"), is(getAccountId("AccountA")));
        assertThat(response.jsonPath().getString("user.id"), is(not(userId)));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerUserId", groups = "nightly-build")
    public void getIntercomSettingsTeamMemberToken_Test(String userId) {
        String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, teamMemberToken, null, null, true);
        validateCommonResponse(response);
        assertThat(response.jsonPath().getInt("user.accountid"), is(getAccountId("AccountA")));
        assertThat(response.jsonPath().getString("user.id"), is(not(userId)));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerUserId", groups = "nightly-build")
    public void getIntercomSettingsRestrictedTeamMemberToken_Test(String userId) {
        String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, restrictedTeamMemberToken, null, null, true);
        validateCommonResponse(response);
        assertThat(response.jsonPath().getInt("user.accountid"), is(getAccountId("AccountA")));
        assertThat(response.jsonPath().getString("user.id"), is(not(userId)));
    }

    private void validateCommonResponse(Response response) {
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().getString("status"), is("success"));
        assertThat(response.jsonPath().getString("message_type"), is("is-success"));
    }

    @DataProvider
    public Object[][] getOwnerUserId() {
        Response response = RestClient.doGet("JSON", albatrossURL, "users/all", albatrossTknA, null, null, true);
        assertThat(response.statusCode(), is(200));
        assertThat("Response message_type should be is-success", response.jsonPath().getString("message_type"), is("is-success"));
        int userId = response.jsonPath().getInt("data[1].id");
        return new Object[][] { { String.valueOf(userId) } };
    }
}
