package io.recruitcrm.albatross;

import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.ITestContext;
import org.testng.annotations.*;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACGetUserGPTAccessTokenSecurityTest extends TestBase {

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String STATUS_SUCCESS = "success";

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private String basePath = "user-gpt-token";

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "gptTokenAccessData", groups = {"role-based", "gpt-token-access"})
    public void getUserGPTTokenRBAC_Test(String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        String executorToken = albatrossTknMap.get(executor);

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, executorToken, null, null, true);
        
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == 200 && SUCCESS_MESSAGE.equals(expectedMessage)) {
            response.then().body("data.allotted_token", notNullValue());
            response.then().body("user", notNullValue());
            response.then().body("status", equalTo(STATUS_SUCCESS));
        }
    }

    @DataProvider(name = "gptTokenAccessData", parallel = true)
    public Object[][] gptTokenAccessData(ITestContext context) {
        return new Object[][] {
            {"AccountOwner", 200, "Success", "Account Owner can access their own GPT token - TC001"},
            {"Admin", 200, "Success", "Admin can access their own GPT token - TC002"},
            {"TeamMember", 200, "Success", "Team Member can access their own GPT token - TC003"},
            {"RestrictedTeamMember", 200, "Success", "Restricted Team Member can access their own GPT token - TC004"},
            {"CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only can access their own GPT token - TC005"},
            {"CustomRoleNothing", 200, "Success", "Custom Role Nothing can access their own GPT token - TC006"},
        };
    }
}

