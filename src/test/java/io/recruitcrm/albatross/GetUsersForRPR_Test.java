package io.recruitcrm.albatross;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import java.util.*;
import org.hamcrest.Matchers;
import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetUsersForRPR_Test extends TestBase {

    private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	String basePath = "global/get-users-for-rpr";
	Map<String, String> queryParameters;

    @BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
		queryParameters = new HashMap<String, String>();
	}

    @Owner("Smit Patel")
    @Test(dataProvider = "getUsersForRPRData", groups = "nightly-build")
	public void getUsersForRPR_Test(String mode) {
		queryParameters.put("report", mode);
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTknA, queryParameters,null, true,null);
		validateSuccessResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getUsersForRPR_InvalidToken_Test() {
		queryParameters.put("report", "recruiter");
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTknInvalidA, queryParameters,null, true,null);
		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getUsersForRPR_EmptyToken_Test() {
		queryParameters.put("report", "recruiter");
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, "", queryParameters,null, true,null);
		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getUsersForRPR_CrossAccount_Test() {
		List<String> accountAOwnerAdminEmails = getAccountAOwnerAndAdminEmails();
		queryParameters.put("report", "recruiter");

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTknB, queryParameters,null, true,null);
		validateSuccessResponse(response);
		List<String> returnedUserEmails = response.jsonPath().getList("data.email", String.class);
		for (String email : returnedUserEmails) {
			assertThat("User email from AccountB should not be from AccountA owner/admin", accountAOwnerAdminEmails, not(hasItem(email)));
		}
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getUsersForRPR_AdminToken_Test() {
		String adminToken = getRoleBasedToken("AccountA", "Admin");
		queryParameters.put("report", "recruiter");
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, adminToken, queryParameters,null, true,null);
		validateSuccessResponse(response);
		response.then().body("data.size()", Matchers.is(4));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getUsersForRPR_TeamMemberToken_Test() {
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
		queryParameters.put("report", "recruiter");
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, teamMemberToken, queryParameters,null, true,null);
		validateSuccessResponse(response);
		response.then().body("data.size()", Matchers.is(1));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getUsersForRPR_RestrictedTeamMemberToken_Test() {
		String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
		queryParameters.put("report", "recruiter");
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, restrictedTeamMemberToken, queryParameters,null, true,null);
		validateSuccessResponse(response);
		response.then().body("data.size()", Matchers.is(1));
	}

	private void validateSuccessResponse(Response response) {
		response.then().statusCode(200);
		response.then().body("message_type", Matchers.equalToIgnoringCase("is-success"));
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("data[0].name", Matchers.notNullValue());
		response.then().assertThat().body(matchesJsonSchemaInClasspath("userForReportsData.json"));
	}

	private List<String> getAccountAOwnerAndAdminEmails() {
		Response response = RestClient.doGet("JSON", albatrossURL, "users/all", albatrossTknA, null, null, true);
		assertThat(response.statusCode(), is(200));
		assertThat("Response message_type should be is-success", response.jsonPath().getString("message_type"), is("is-success"));
		List<Map<String, Object>> users = response.jsonPath().getList("data");
		List<String> ownerAdminEmails = users == null ? new ArrayList<>() : users.stream()
			.filter(user -> {
				String role = (String) user.get("role");
				return "Owner".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role);
			})
			.map(user -> (String) user.get("email"))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		return ownerAdminEmails;
	}

	@DataProvider
	public Object[][] getUsersForRPRData() {
		return new Object[][] {
			{ "recruiter" },
			{ "dashboardjobs" },
			{ "dashboardcompanies" },
			{ "dashboarddeals" },
			{ "dashboardcandidates" },
			{ "tasks" },
		};
	}
}
