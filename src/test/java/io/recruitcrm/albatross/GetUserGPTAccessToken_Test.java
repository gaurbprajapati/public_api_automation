package io.recruitcrm.albatross;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import java.util.*;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.rcrm.api.commanfunctions.commanFunction;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetUserGPTAccessToken_Test extends TestBase {

    private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
    private String accountA_APIKey;
    commanFunction function = new commanFunction();
	String basePath = "user-gpt-token";

    @BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        accountA_APIKey = getAccountApiKey("AccountA");
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getUserGPTToken_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, null, true);
        validateSuccessResponse(response);
	}

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getUserGPTTokenUnauthorized_Test() {
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, null, true);
        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getUserGPTTokenEmptyToken_Test() {
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, "", null, null, true);
        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getAccountA_UserDetails", groups = "nightly-build")
    public void getUserGPTTokenCrossAccount_Test(String accountA_UserDetails) {
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknB, null, null, true);
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().getString("data.allotted_token"), is(notNullValue()));
        assertThat(response.jsonPath().getString("user"), is(not(accountA_UserDetails)));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getUserGPTTokenAdminToken_Test() {
        String adminToken = getRoleBasedToken("AccountA", "Admin");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, adminToken, null, null, true);
        validateSuccessResponse(response);
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getUserGPTTokenTeamMemberToken_Test() {
        String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, teamMemberToken, null, null, true);
        validateSuccessResponse(response);
    }   

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getUserGPTTokenRestrictedTeamMemberToken_Test() {
        String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, restrictedTeamMemberToken, null, null, true);
        validateSuccessResponse(response);
    }

    private void validateSuccessResponse(Response response) {
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().getString("data.allotted_token"), is(notNullValue()));
        assertThat(response.jsonPath().getString("user"), is(notNullValue()));
        assertThat(response.jsonPath().getString("status"), is("success"));
    }

    @DataProvider
    public Object[][] getAccountA_UserDetails() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, null, true);
		validateSuccessResponse(response);
        return new Object[][] { { response.jsonPath().getString("user") } };
    }
}
