package io.recruitcrm.albatross;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.ITestContext;
import org.testng.annotations.*;

import com.qa.api.util.reaper.*;

import io.rcrm.api.commanfunctions.albatross.RBAC6LevelDataProvider;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACGetEnrichmentToggleStatusSecurityTest extends TestBase {

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String ENRICHMENT_TOGGLE_SUCCESS_MESSAGE = "Enrichment Toggle and T&C Status retrieved successfully";
    private static final String STATUS_SUCCESS = "success";
    private static final String MESSAGE_TYPE_SUCCESS = "is-success";

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private String accountId;
    private String basePath = "enrichment/toggle_status";

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        accountId = String.valueOf(ThreadManager.getAccount().getAccountId());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "enrichmentToggleStatusViewAccessData", groups = {"role-based", "enrichment-toggle-status-view-access"})
    public void getEnrichmentToggleStatusRBAC_Test(String userOwner, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        String userId = String.valueOf(userIdsMap.get(userOwner));
        String executorToken = albatrossTknMap.get(executor);

        Map<String, String> queryParams = createQueryParams(userId, accountId);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, executorToken, queryParams, null, true);
        
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription, userId);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription, String userId) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == 200 && SUCCESS_MESSAGE.equals(expectedMessage)) {
            response.then().body("message_type", equalTo(MESSAGE_TYPE_SUCCESS));
            response.then().body("status", equalTo(STATUS_SUCCESS));
            response.then().body("message", equalTo(ENRICHMENT_TOGGLE_SUCCESS_MESSAGE));
            response.then().body("data.accountowner", notNullValue());
        }
    }

    private Map<String, String> createQueryParams(String userId, String accountId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("account_id", accountId);
        queryParams.put("user_id", userId);
        return queryParams;
    }

    @DataProvider(name = "enrichmentToggleStatusViewAccessData", parallel = true)
    public Object[][] enrichmentToggleStatusViewAccessData(ITestContext context) {
        return new Object[][] {
            //With Owner id
        {"AccountOwner", "AccountOwner", 200, "Success", "Account Owner can access enrichment toggle status of Account Owner - TC001"},
        {"AccountOwner", "Admin", 200, "Success", "Admin can access enrichment toggle status of Account Owner - TC002"},
        {"AccountOwner", "TeamMember", 200, "Success", "Team Member can access enrichment toggle status of Account Owner - TC003"},
        {"AccountOwner", "RestrictedTeamMember", 200, "Success", "Restricted Team Member can access enrichment toggle status of Account Owner - TC004"},
        {"AccountOwner", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only can access enrichment toggle status of Account Owner - TC005"},
        {"AccountOwner", "CustomRoleNothing", 200, "Success", "Custom Role Nothing can access enrichment toggle status of Account Owner - TC006"},

        //With Admin id
        {"Admin", "AccountOwner", 200, "Success", "Account Owner can access enrichment toggle status of Admin - TC007"},
        {"Admin", "Admin", 200, "Success", "Admin can access enrichment toggle status of Admin - TC008"},
        {"Admin", "TeamMember", 200, "Success", "Team Member can access enrichment toggle status of Admin - TC009"},
        {"Admin", "RestrictedTeamMember", 200, "Success", "Restricted Team Member can access enrichment toggle status of Admin - TC010"},
        {"Admin", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only can access enrichment toggle status of Admin - TC011"},
        {"Admin", "CustomRoleNothing", 200, "Success", "Custom Role Nothing can access enrichment toggle status of Admin - TC012"},

        //With Team Member id
        {"TeamMember", "AccountOwner", 200, "Success", "Account Owner can access enrichment toggle status of Team Member - TC013"},
        {"TeamMember", "Admin", 200, "Success", "Admin can access enrichment toggle  status of Team Member - TC014"},
        {"TeamMember", "TeamMember", 200, "Success", "Team Member can access enrichment toggle status of Team Member - TC015"},
        {"TeamMember", "RestrictedTeamMember", 200, "Success", "Restricted Team Member can access enrichment toggle status of Team Member - TC016"},
        {"TeamMember", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only can access enrichment toggle status of Team Member - TC017"},
        {"TeamMember", "CustomRoleNothing", 200, "Success", "Custom Role Nothing can access enrichment toggle status of Team Member - TC018"},

        //With Restricted Team Member id
        {"RestrictedTeamMember", "AccountOwner", 200, "Success", "Account Owner can access enrichment toggle status of Restricted Team Member - TC019"},
        {"RestrictedTeamMember", "Admin", 200, "Success", "Admin can access enrichment toggle status of Restricted Team Member - TC020"},
        {"RestrictedTeamMember", "TeamMember", 200, "Success", "Team Member can access enrichment toggle status of Restricted Team Member - TC021"},
        {"RestrictedTeamMember", "RestrictedTeamMember", 200, "Success", "Restricted Team Member can access enrichment toggle status of Restricted Team Member - TC022"},
        {"RestrictedTeamMember", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only can access enrichment toggle status of Restricted Team Member - TC023"},
        {"RestrictedTeamMember", "CustomRoleNothing", 200, "Success", "Custom Role Nothing can access enrichment toggle status of Restricted Team Member - TC024"},

        //With Custom Role Team Only id
        {"CustomRoleTeamOnly", "AccountOwner", 200, "Success", "Account Owner can access enrichment toggle status of Custom Role Team Only - TC025"},
        {"CustomRoleTeamOnly", "Admin", 200, "Success", "Admin can access enrichment toggle status of Custom Role Team Only - TC026"},
        {"CustomRoleTeamOnly", "TeamMember", 200, "Success", "Team Member can access enrichment toggle status of Custom Role Team Only - TC027"},
        {"CustomRoleTeamOnly", "RestrictedTeamMember", 200, "Success", "Restricted Team Member can access enrichment toggle status of Custom Role Team Only - TC028"},
        {"CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only can access enrichment toggle status of Custom Role Team Only - TC029"},
        {"CustomRoleTeamOnly", "CustomRoleNothing", 200, "Success", "Custom Role Nothing can access enrichment toggle status of Custom Role Team Only - TC030"},
    };
    }
}

