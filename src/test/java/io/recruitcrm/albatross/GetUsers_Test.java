package io.recruitcrm.albatross;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import java.util.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetUsers_Test extends TestBase {

    private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	String basePath = "users/{user}";

    @BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
	}

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerUserId", groups = "nightly-build")
    public void getUsers_Test(String userId, String ownerFullName, String ownerEmail) {
		Map<String, String> pathParameters = createUserPathParameters(userId);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, pathParameters, true);
        validateSuccessResponse(response, userId, ownerFullName, ownerEmail);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerUserId", groups = "nightly-build")
    public void getUsersUnauthorized_Test(String userId, String ownerFullName, String ownerEmail) {
		Map<String, String> pathParameters = createUserPathParameters(userId);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, pathParameters, true);
        validateUnauthorizedResponse(response);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerUserId", groups = "nightly-build")
    public void getUsersEmptyToken_Test(String userId, String ownerFullName, String ownerEmail) {
		Map<String, String> pathParameters = createUserPathParameters(userId);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, "", null, pathParameters, true);
        validateUnauthorizedResponse(response);
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getUsersEmptyUserId_Test() {
        Map<String, String> pathParameters = createUserPathParameters("");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, pathParameters, true);
        assertThat(response.statusCode(), is(404));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getUsersForInvalidUserId_Test() {
		Map<String, String> pathParameters = createUserPathParameters("invalid");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, pathParameters, true);
        validateInvalidResponse(response);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerUserId", groups = "nightly-build")
    public void getUsersCrossAccount_Test(String userId, String ownerFullName, String ownerEmail) {
		Map<String, String> pathParameters = createUserPathParameters(userId);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknB, null, pathParameters, true);
        validateInvalidResponse(response);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerUserId", groups = "nightly-build")
    public void getOwnerUserAdminToken_Test(String userId, String ownerFullName, String ownerEmail) {
		Map<String, String> pathParameters = createUserPathParameters(userId);
        String adminToken = getRoleBasedToken("AccountA", "Admin");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, adminToken, null, pathParameters, true);
        // ERB is created for this test, response is supposed to be invalid
        validateSuccessResponse(response, userId, ownerFullName, ownerEmail);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getRestrictedTeamMemberUserId", groups = "nightly-build")
    public void getRestrictedTeamMemberUserAdminToken_Test(String userId, String restrictedTeamMemberFullName, String restrictedTeamMemberEmail) {
		Map<String, String> pathParameters = createUserPathParameters(userId);
        String adminToken = getRoleBasedToken("AccountA", "Admin");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, adminToken, null, pathParameters, true);
        validateSuccessResponse(response, userId, restrictedTeamMemberFullName, restrictedTeamMemberEmail);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerUserId", groups = "nightly-build")
    public void getUsersTeamMemberToken_Test(String userId, String ownerFullName, String ownerEmail) {
		Map<String, String> pathParameters = createUserPathParameters(userId);
        String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, teamMemberToken, null, pathParameters, true);
        // ERB is created for this test, response is supposed to be invalid
        validateSuccessResponse(response, userId, ownerFullName, ownerEmail);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerUserId", groups = "nightly-build")
    public void getUsersRestrictedTeamMemberToken_Test(String userId, String ownerFullName, String ownerEmail) {
		Map<String, String> pathParameters = createUserPathParameters(userId);
        String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, restrictedTeamMemberToken, null, pathParameters, true);
        // ERB is created for this test, response is supposed to be invalid
        validateSuccessResponse(response, userId, ownerFullName, ownerEmail);
    }

    private void validateSuccessResponse(Response response, String userId, String fullName, String email) {
        assertThat(response.statusCode(), is(200));
        assertThat("Response message_type should be is-success", response.jsonPath().getString("message_type"), is("is-success"));
        assertThat("User ID should match", response.jsonPath().getString("data.user.ownerid"), is(userId));
        assertThat("User name should match", response.jsonPath().getString("data.user.Fullname"), is(fullName));
        assertThat("User email should match", response.jsonPath().getString("data.user.email"), is(email));
    }

    private void validateUnauthorizedResponse(Response response) {
        assertThat(response.statusCode(), is(401));
        assertThat("Response error should be Unauthorized", response.jsonPath().getString("error"), is("Unauthorized"));
    }

    private void validateInvalidResponse(Response response) {
        assertThat(response.statusCode(), is(200));
        assertThat("Response message should be Could not find user!", response.jsonPath().getString("message"), is("Could not find user!"));
        assertThat("Response message_type should be is-danger", response.jsonPath().getString("message_type"), is("is-danger"));
    }

    private Map<String, String> createUserPathParameters(String userId) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("user", userId);
        return pathParameters;
    }

    private Object[][] getUserDataByIndex(int userIndex) {
        Response response = RestClient.doGet("JSON", albatrossURL, "users/all", albatrossTknA, null, null, true);
        assertThat(response.statusCode(), is(200));
        assertThat("Response message_type should be is-success", response.jsonPath().getString("message_type"), is("is-success"));
        int userId = response.jsonPath().getInt("data[" + userIndex + "].id");
        String fullName = response.jsonPath().getString("data[" + userIndex + "].name");
        String email = response.jsonPath().getString("data[" + userIndex + "].email");
        return new Object[][] { { String.valueOf(userId), fullName, email } };
    }

    @DataProvider
    public Object[][] getOwnerUserId() {
        return getUserDataByIndex(1);
    }

    @DataProvider
    public Object[][] getRestrictedTeamMemberUserId() {
        return getUserDataByIndex(3);
    }
}
