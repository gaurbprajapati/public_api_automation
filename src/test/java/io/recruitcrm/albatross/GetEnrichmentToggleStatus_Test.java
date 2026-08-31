package io.recruitcrm.albatross;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import java.util.*;
import io.rcrm.api.commanfunctions.commanFunction;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetEnrichmentToggleStatus_Test extends TestBase {

    private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
    private String accountA_Id;
    commanFunction function = new commanFunction();
	String basePath = "enrichment/toggle_status";

    @BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        accountA_Id = String.valueOf(getAccountId("AccountA"));
	}

    @Owner("Smit Patel")
    @Test(dataProvider = "getUserId", groups = "nightly-build")
    public void getEnrichmentToggleStatus_Test(String userId) {
        Map<String, String> queryParams = createQueryParams(userId, accountA_Id);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, queryParams, null, true);
        validateSuccessResponse(response);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerId", groups = "nightly-build")
    public void getEnrichmentToggleStatusUnauthorized_Test(String userId) {
        Map<String, String> queryParams = createQueryParams(userId, accountA_Id);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknInvalidA, queryParams, null, true);
        assertThat(response.statusCode(), is(401));
        assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerId", groups = "nightly-build")
    public void getEnrichmentToggleStatusEmptyToken_Test(String userId) {
        Map<String, String> queryParams = createQueryParams(userId, accountA_Id);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, "", queryParams, null, true);
        assertThat(response.statusCode(), is(401));
        assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getEnrichmentToggleStatusEmptyUserId_Test() {
        Map<String, String> queryParams = createQueryParams("", accountA_Id);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, queryParams, null, true);
        validateSuccessResponse(response);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerId", groups = "nightly-build")
    public void getEnrichmentToggleStatusEmptyAccountId_Test(String userId) {
        Map<String, String> queryParams = createQueryParams(userId, "");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, queryParams, null, true);
        validateSuccessResponse(response);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerId", groups = "nightly-build")
    public void getEnrichmentToggleStatusCrossAccount_Test(String userId) {
        Map<String, String> queryParams = createQueryParams(userId, accountA_Id);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknB, queryParams, null, true);
        // assertThat(response.statusCode(), is(401));  // Cross account access issue reported : TITAN-21462
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerId", groups = "nightly-build")
    public void getEnrichmentToggleStatusAdminToken_Test(String userId) {
        Map<String, String> queryParams = createQueryParams(userId, accountA_Id);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, getRoleBasedToken("AccountA", "Admin"), queryParams, null, true);
        validateSuccessResponse(response);
        assertThat(response.jsonPath().getString("data.accountowner"), is(userId));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerId", groups = "nightly-build")
    public void getEnrichmentToggleStatusTeamMemberToken_Test(String userId) {
        Map<String, String> queryParams = createQueryParams(userId, accountA_Id);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, getRoleBasedToken("AccountA", "Team Member"), queryParams, null, true);
        validateSuccessResponse(response);
        assertThat(response.jsonPath().getString("data.accountowner"), is(userId));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getOwnerId", groups = "nightly-build")
    public void getEnrichmentToggleStatusRestrictedTeamMemberToken_Test(String userId) {
        Map<String, String> queryParams = createQueryParams(userId, accountA_Id);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, getRoleBasedToken("AccountA", "Restricted"), queryParams, null, true);
        validateSuccessResponse(response);
        assertThat(response.jsonPath().getString("data.accountowner"), is(userId));
    }

    private Map<String, String> createQueryParams(String userId, String accountId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("account_id", accountId);
        queryParams.put("user_id", userId);
        return queryParams;
    }

    private void validateSuccessResponse(Response response) {
        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getString("message_type"), is("is-success"));
        assertThat(response.jsonPath().getString("status"), is("success"));
        assertThat(response.jsonPath().getString("message"), is("Enrichment Toggle and T&C Status retrieved successfully"));
    }

    private Response getUsersResponse(String accountId) {
        Response response = function.getUsers(baseURL, getAccountApiKey("AccountA"));
        assertThat(response.statusCode(), is(200));
        return response;
    }

    @DataProvider
    public Object[][] getUserId() {
        Response response = getUsersResponse("AccountA");
        String ownerId = response.jsonPath().getString("[0].id");
        String adminId = response.jsonPath().getString("[1].id");
        String teamMemberId = response.jsonPath().getString("[3].id");
        String restrictedTeamMemberId = response.jsonPath().getString("[2].id");
        return new Object[][] { 
            { String.valueOf(ownerId) },
            { String.valueOf(adminId) },
            { String.valueOf(teamMemberId) },
            { String.valueOf(restrictedTeamMemberId) }
        };
    }

    @DataProvider
    public Object[][] getOwnerId() {
        Response response = getUsersResponse("AccountA");
        String ownerId = response.jsonPath().getString("[0].id");
        return new Object[][] { 
            { String.valueOf(ownerId) }
        };
    }
}

