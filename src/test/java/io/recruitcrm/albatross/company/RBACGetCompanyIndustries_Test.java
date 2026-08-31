package io.recruitcrm.albatross.company;

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
public class RBACGetCompanyIndustries_Test extends TestBase {

	private Map<String, String> albatrossTknMap = new HashMap<>();
	Map<String, Integer> userIdsMap = new HashMap<>();
	String basePath = "industries";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getTokenDataForAllUsersInAccount")
	public void getGetCompanyIndustriesWithRBACToken_Test(String token, String role) {
		
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, token, null, true, null);
		
		if(role.equals("CustomRoleNothing")) {
			assertThat("Failed to verify status code for role: " + role,  response.getStatusCode(), is(401));
			assertThat("Failed to verify message for role: " + role, response.jsonPath().getInt("data.defaultIndustries[0].id"), nullValue());
			assertThat("Failed to verify message type for role: " + role, response.jsonPath().getString("message"), is("Unauthorised"));
		} else {
			assertThat("Failed to verify status code for role: " + role,  response.getStatusCode(), is(200));
			assertThat("Failed to verify message for role: " + role, response.jsonPath().getInt("data.defaultIndustries[0].id"), is(0));
			assertThat("Failed to verify message type for role: " + role, response.jsonPath().getString("message_type"), is("is-success"));	
		}	
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