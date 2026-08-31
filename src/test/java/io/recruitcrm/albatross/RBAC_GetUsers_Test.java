package io.recruitcrm.albatross;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.hamcrest.Matchers;
import java.util.*;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBAC_GetUsers_Test extends TestBase {
    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private final String basePath = "users/{user}";

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String INVALID_ACCESS_MESSAGE = "Invalid Access";
    private static final int SUCCESS_STATUS_CODE = 200;
    private static final int INVALID_ACCESS_STATUS_CODE = 400;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "rbacGetUsersAccessData", groups = {"rbac", "get-users"})
    public void getUsersRBACAccess_Test(String userIdRole, String executorRole, int expectedStatusCode, String expectedMessage, String testDescription) {
        int userId = userIdsMap.get(userIdRole);
        String executorToken = albatrossTknMap.get(executorRole);
        Map<String, String> pathParameters = createUserPathParameters(String.valueOf(userId));
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, executorToken, null, pathParameters, true);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription, userIdRole);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription, String userIdRole) {
        int actualStatusCode = response.getStatusCode();
        
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected Status: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == SUCCESS_STATUS_CODE && SUCCESS_MESSAGE.equals(expectedMessage)) {
            validateSuccessResponse(response, userIdRole);
        } else if (expectedStatusCode == INVALID_ACCESS_STATUS_CODE && INVALID_ACCESS_MESSAGE.equals(expectedMessage)) {
            validateInvalidAccessResponse(response);
        }
    }

    private void validateSuccessResponse(Response response, String userIdRole) {
        response.then().body("message_type", Matchers.is("is-success"));
        
        int expectedUserId = userIdsMap.get(userIdRole);
        String actualUserId = response.jsonPath().getString("data.user.ownerid");
        
        assertThat("User ID should match for " + userIdRole,  actualUserId, is(String.valueOf(expectedUserId)));
        String fullName = response.jsonPath().getString("data.user.Fullname");
        String email = response.jsonPath().getString("data.user.email");
        assertThat("Full name should not be null for " + userIdRole, fullName, notNullValue());
        assertThat("Email should not be null for " + userIdRole, email, notNullValue());
    }

    private void validateInvalidAccessResponse(Response response) {
        response.then().statusCode(INVALID_ACCESS_STATUS_CODE);
    }

    private Map<String, String> createUserPathParameters(String userId) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("user", userId);
        return pathParameters;
    }

    @DataProvider(name = "rbacGetUsersAccessData", parallel = true)
    public Object[][] rbacGetUsersAccessData() {
        return new Object[][] {
            // AccountOwner User ID scenarios (6 scenarios: 1 Success, 5 Invalid Access)
            {"AccountOwner", "AccountOwner", 200, "Success", "Owner Token can view Owner user details - TC001"},
            {"AccountOwner", "Admin", 400, "Invalid Access", "Admin Token CANNOT view Owner user details - TC002"},
            {"AccountOwner", "TeamMember", 400, "Invalid Access", "TeamMember Token CANNOT view Owner user details - TC003"},
            {"AccountOwner", "RestrictedTeamMember", 400, "Invalid Access", "Restricted Team Member Token CANNOT view Owner user details - TC004"},
            {"AccountOwner", "CustomRoleTeamOnly", 400, "Invalid Access", "Custom Role Team Only Token CANNOT view Owner user details - TC005"},
            {"AccountOwner", "CustomRoleNothing", 400, "Invalid Access", "Custom Role Nothing Token CANNOT view Owner user details - TC006"},
            
            // Admin User ID scenarios (6 scenarios: 3 Success, 3 Invalid Access)
            {"Admin", "AccountOwner", 200, "Success", "Owner Token can view Admin user details - TC007"},
            {"Admin", "Admin", 200, "Success", "Admin Token can view Admin user details - TC008"},
            {"Admin", "TeamMember", 400, "Invalid Access", "TeamMember Token CANNOT view Admin user details - TC009"},
            {"Admin", "RestrictedTeamMember", 400, "Invalid Access", "Restricted Team Member Token CANNOT view Admin user details - TC010"},
            {"Admin", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only Token can view Admin user details - TC011"},
            {"Admin", "CustomRoleNothing", 400, "Invalid Access", "Custom Role Nothing Token CANNOT view Admin user details - TC012"},
            
            // TeamMember User ID scenarios (6 scenarios: 3 Success, 3 Invalid Access)
            {"TeamMember", "AccountOwner", 200, "Success", "Owner Token can view TeamMember user details - TC013"},
            {"TeamMember", "Admin", 200, "Success", "Admin Token can view TeamMember user details - TC014"},
            {"TeamMember", "TeamMember", 400, "Invalid Access", "TeamMember Token CANNOT view TeamMember user details - TC015"},
            {"TeamMember", "RestrictedTeamMember", 400, "Invalid Access", "Restricted Team Member Token CANNOT view TeamMember user details - TC016"},
            {"TeamMember", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only Token can view TeamMember user details - TC017"},
            {"TeamMember", "CustomRoleNothing", 400, "Invalid Access", "Custom Role Nothing Token CANNOT view TeamMember user details - TC018"},
            
            // RestrictedTeamMember User ID scenarios (6 scenarios: 3 Success, 3 Invalid Access)
            {"RestrictedTeamMember", "AccountOwner", 200, "Success", "Owner Token can view Recruiter user details - TC019"},
            {"RestrictedTeamMember", "Admin", 200, "Success", "Admin Token can view Recruiter user details - TC020"},
            {"RestrictedTeamMember", "TeamMember", 400, "Invalid Access", "TeamMember Token CANNOT view Recruiter user details - TC021"},
            {"RestrictedTeamMember", "RestrictedTeamMember", 400, "Invalid Access", "Restricted Team Member Token CANNOT view Recruiter user details - TC022"},
            {"RestrictedTeamMember", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only Token can view Recruiter user details - TC023"},
            {"RestrictedTeamMember", "CustomRoleNothing", 400, "Invalid Access", "Custom Role Nothing Token CANNOT view Recruiter user details - TC024"},

            // Custom role id scenarios (6 scenarios: 3 Success, 3 Invalid Access)
            {"CustomRoleTeamOnly", "AccountOwner", 200, "Success", "Owner Token can view Custom Role Team Only user details - TC025"},
            {"CustomRoleTeamOnly", "Admin", 200, "Success", "Admin Token can view Custom Role Team Only user details - TC026"},
            {"CustomRoleTeamOnly", "TeamMember", 400, "Invalid Access", "TeamMember Token CANNOT view Custom Role Team Only user details - TC027"},
            {"CustomRoleTeamOnly", "RestrictedTeamMember", 400, "Invalid Access", "Restricted Team Member Token CANNOT view Custom Role Team Only user details - TC028"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only Token can view Custom Role Team Only user details - TC029"},
            {"CustomRoleTeamOnly", "CustomRoleNothing", 400, "Invalid Access", "Custom Role Nothing Token CANNOT view Custom Role Team Only user details - TC030"},
        };
    }
}

