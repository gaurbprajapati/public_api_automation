package io.recruitcrm.albatross.sso;

import java.util.*;

import org.testng.annotations.*;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class GetSSORefreshToken_Test extends TestBase {

	private Map<String, String> albatrossTknMap = new HashMap<>();
	Map<String, Integer> userIdsMap = new HashMap<>();
	String basePath = "sso/refresh-token";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getTokenDataForAllUsersInAccount", groups = "nightly-build")
	public void getSSORefreshToken_Test(String token, String role) {
		
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, token, null, true, null);

		assertThat("Failed to verify status code for role: " + role,  response.getStatusCode(), is(200));
		assertThat("Failed to verify message for role: " + role, response.jsonPath().getString("message"), is("SSO is not enabled for this account"));
		assertThat("Failed to verify message type for role: " + role, response.jsonPath().getString("message_type"), is("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
    public void getSSORefreshTokenUnauthorizedToken_Test() {
        
		String invalidToken = albatrossTknMap.get("AccountOwner") + "123";
        
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, invalidToken, null, true, null);
        
		assertThat("Failed to verify status code : ",  response.getStatusCode(), is(401));
		assertThat("Failed to verify message : ", response.jsonPath().getString("error"), is("Unauthorized"));
    }

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
    public void getSSORefreshTokenEmptyToken_Test() {
        
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, "", null, true, null);
        
		assertThat("Failed to verify status code : ",  response.getStatusCode(), is(401));
		assertThat("Failed to verify message : ", response.jsonPath().getString("error"), is("Unauthorized"));
    }


	@DataProvider
	public Object[][] getTokenDataForAllUsersInAccount() {
		return new Object[][] {
				{ albatrossTknMap.get("AccountOwner"), "AccountOwner" },
				{ albatrossTknMap.get("Admin"), "Admin" },
				{ albatrossTknMap.get("TeamMember"), "TeamMember" },
				{ albatrossTknMap.get("RestrictedTeamMember"), "RestrictedTeamMember" },
				{ albatrossTknMap.get("CustomRoleTeamOnly"), "CustomRoleTeamOnly" },
				{ albatrossTknMap.get("CustomRoleNothing"), "CustomRoleNothing" }
		};
	}

}