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
public class RBAC_GetJobStatusByAccount_Test extends TestBase {
    private Map<String, String> albatrossTknMap;
    private final String basePath = "jobs/job-status-by-account/get";

    private static final String SUCCESS_MESSAGE = "Success";
    private static final int SUCCESS_STATUS_CODE = 200;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        Map<String, Integer> userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "rbacGetJobStatusByAccountAccessData", groups = {"rbac", "get-job-status-by-account"})
    public void getJobStatusByAccountRBACAccess_Test(String executorRole, int expectedStatusCode, String expectedMessage, String testDescription) {
        String executorToken = albatrossTknMap.get(executorRole);
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, executorToken, null, true, null);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected Status: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }
        if (expectedStatusCode == SUCCESS_STATUS_CODE && SUCCESS_MESSAGE.equals(expectedMessage)) {
            validateSuccessResponse(response);
        }
    }

    private void validateSuccessResponse(Response response) {
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("status", Matchers.containsString("success"));
        assertThat("Default job status should not be null", response.jsonPath().get("data.defaultJobStatus"), notNullValue());
        assertThat("Customized job status should not be null", response.jsonPath().get("data.customizedJobStatus"), notNullValue());
    }

    @DataProvider(name = "rbacGetJobStatusByAccountAccessData", parallel = true)
    public Object[][] rbacGetJobStatusByAccountAccessData() {
        return new Object[][] {
            {"AccountOwner", 200, "Success", "Owner Token can get job status by account - TC001"},
            {"Admin", 200, "Success", "Admin Token can get job status by account - TC002"},
            {"TeamMember", 200, "Success", "TeamMember Token can get job status by account - TC003"},
            {"RestrictedTeamMember", 200, "Success", "Restricted Team Member Token can get job status by account - TC004"},
            {"CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only Token can get job status by account - TC005"},
            {"CustomRoleNothing", 200, "Success", "Custom Role Nothing Token can get job status by account - TC006"},
        };
    }
}

