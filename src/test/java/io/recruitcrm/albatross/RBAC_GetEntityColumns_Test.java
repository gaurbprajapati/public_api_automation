package io.recruitcrm.albatross;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.*;
import org.testng.annotations.*;

import io.rcrm.api.pojo.albatross.GetEntityColumns;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBAC_GetEntityColumns_Test extends TestBase {
    private Map<String, String> albatrossTknMap;
    private final String basePath = "global/get-entity-columns";
    private GetEntityColumns getEntityColumns = new GetEntityColumns();

    private static final String SUCCESS_MESSAGE = "Success";
    private static final int SUCCESS_STATUS_CODE = 200;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        Map<String, Integer> userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "rbacGetEntityColumnsAccessData", groups = {"rbac", "get-entity-columns"})
    public void getEntityColumnsRBACAccess_Test(String executorRole, String entity, int expectedStatusCode, String expectedMessage, String testDescription) {
        String executorToken = albatrossTknMap.get(executorRole);
        getEntityColumns.setEntity(entity);
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, executorToken, null, true, getEntityColumns);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription, entity);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription, String entity) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected Status: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }
        if (expectedStatusCode == SUCCESS_STATUS_CODE && SUCCESS_MESSAGE.equals(expectedMessage)) {
            validateSuccessResponse(response, entity, testDescription);
        }
    }

    private void validateSuccessResponse(Response response, String entity, String testDescription) {
        response.then().body("message_type", Matchers.is("is_success"));
        response.then().body("status", Matchers.is("success"));
        assertThat("Response data should not be null", response.jsonPath().get("data"), notNullValue());
        
        HashMap<String, String> entityMap = new HashMap<>();
        entityMap.put("candidates", "candidate");
        entityMap.put("jobs", "job");
        entityMap.put("contacts", "contact");
        entityMap.put("companies", "company");
        entityMap.put("tasks", "tasks");
        
        String expectedEntity = entityMap.get(entity);
        assertThat("Entity in response should match - " + testDescription, response.jsonPath().getString("data.columns.id.entity"), is(expectedEntity));
    }

    @DataProvider(name = "rbacGetEntityColumnsAccessData")
    public Object[][] rbacGetEntityColumnsAccessData() {
        return new Object[][] {
            // ERB created for Custom Role Nothing - TITAN-21873

            // Candidates entity scenarios
            {"AccountOwner", "candidates", 200, "Success", "Owner Token can get candidates entity columns - TC001"},
            {"Admin", "candidates", 200, "Success", "Admin Token can get candidates entity columns - TC002"},
            {"TeamMember", "candidates", 200, "Success", "TeamMember Token can get candidates entity columns - TC003"},
            {"RestrictedTeamMember", "candidates", 200, "Success", "Restricted Team Member Token can get candidates entity columns - TC004"},
            {"CustomRoleTeamOnly", "candidates", 200, "Success", "Custom Role Team Only Token can get candidates entity columns - TC005"},
            {"CustomRoleNothing", "candidates", 200, "Success", "Custom Role Nothing Token can get candidates entity columns - TC006"},

            // Companies entity scenarios
            {"AccountOwner", "companies", 200, "Success", "Owner Token can get companies entity columns - TC019"},
            {"Admin", "companies", 200, "Success", "Admin Token can get companies entity columns - TC020"},
            {"TeamMember", "companies", 200, "Success", "TeamMember Token can get companies entity columns - TC021"},
            {"RestrictedTeamMember", "companies", 200, "Success", "Restricted Team Member Token can get companies entity columns - TC022"},
            {"CustomRoleTeamOnly", "companies", 200, "Success", "Custom Role Team Only Token can get companies entity columns - TC023"},
            {"CustomRoleNothing", "companies", 200, "Success", "Custom Role Nothing Token can get companies entity columns - TC024"},
            
            // Contacts entity scenarios
            {"AccountOwner", "contacts", 200, "Success", "Owner Token can get contacts entity columns - TC007"},
            {"Admin", "contacts", 200, "Success", "Admin Token can get contacts entity columns - TC008"},
            {"TeamMember", "contacts", 200, "Success", "TeamMember Token can get contacts entity columns - TC009"},
            {"RestrictedTeamMember", "contacts", 200, "Success", "Restricted Team Member Token can get contacts entity columns - TC010"},
            {"CustomRoleTeamOnly", "contacts", 200, "Success", "Custom Role Team Only Token can get contacts entity columns - TC011"},
            {"CustomRoleNothing", "contacts", 200, "Success", "Custom Role Nothing Token can get contacts entity columns - TC012"},
            
            // Jobs entity scenarios
            {"AccountOwner", "jobs", 200, "Success", "Owner Token can get jobs entity columns - TC013"},
            {"Admin", "jobs", 200, "Success", "Admin Token can get jobs entity columns - TC014"},
            {"TeamMember", "jobs", 200, "Success", "TeamMember Token can get jobs entity columns - TC015"},
            {"RestrictedTeamMember", "jobs", 200, "Success", "Restricted Team Member Token can get jobs entity columns - TC016"},
            {"CustomRoleTeamOnly", "jobs", 200, "Success", "Custom Role Team Only Token can get jobs entity columns - TC017"},
            {"CustomRoleNothing", "jobs", 200, "Success", "Custom Role Nothing Token can get jobs entity columns - TC018"},
            
            // Tasks scenarios
            {"AccountOwner", "tasks", 200, "Success", "Owner Token can get tasks entity columns - TC025"},
            {"Admin", "tasks", 200, "Success", "Admin Token can get tasks entity columns - TC026"},
            {"TeamMember", "tasks", 200, "Success", "TeamMember Token can get tasks entity columns - TC027"},
            {"RestrictedTeamMember", "tasks", 200, "Success", "Restricted Team Member Token can get tasks entity columns - TC028"},
            {"CustomRoleTeamOnly", "tasks", 200, "Success", "Custom Role Team Only Token can get tasks entity columns - TC029"},
            {"CustomRoleNothing", "tasks", 200, "Success", "Custom Role Nothing Token can get tasks entity columns - TC030"},
        };
    }
}

