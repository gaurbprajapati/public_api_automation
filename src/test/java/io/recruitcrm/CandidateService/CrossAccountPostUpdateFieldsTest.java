package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountPostUpdateFieldsTest extends TestBase {

    private int accountA_UserId;
    private AllCrudFunctions albatrossFunctions = new AllCrudFunctions();

    @BeforeClass
    public void setUp() {
        setupCrossAccountTokens();
        
        // Get Account A user ID
        ThreadManager.setAccount(accountA);
        Response getUsersA = albatrossFunctions.getUsers(albatrossURL, accountA_Token);
        assertThat("Failed to get Account A users", getUsersA.getStatusCode(), equalTo(200));
        JsonPath jpA = getUsersA.jsonPath();
        accountA_UserId = jpA.get("data.records[0].id");
        assertThat("Account A User ID should not be null", accountA_UserId, notNullValue());
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountUpdateFieldsTestData", groups = "nightly-build")
    public void crossAccountUpdateFieldsOperations_Test(String testScenario, String accountType, String tokenType,
            String operation, String expectedStatusCode, String expectedResponse, String description) {
        
        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "POST_UPDATE_FIELDS_CROSS_ACCOUNT":
                    // Account B tries to update Account A's user settings
                    response = updateFieldsWithCrossAccountData(token, accountA_UserId);
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unknown operation: " + operation);
            }
            
            verifyResponse(response, expectedStatusCode, expectedResponse, operation);
            
        } catch (Exception e) {
            // Handle expected exceptions for cross-account scenarios
            if ("cross_account_isolation".equals(expectedResponse)) {
                // Cross-account isolation should either return 401 or 403
                assertThat("Cross-account access should be denied", 
                    response.getStatusCode(), anyOf(equalTo(401), equalTo(403)));
            } else {
                throw e;
            }
        }
    }

    private Response updateFieldsWithCrossAccountData(String token, int userId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("key", "entity_view_lock_settings");
        requestBody.put("value", "{\"1\":1,\"2\":1,\"3\":1,\"4\":1,\"5\":1,\"6\":1,\"7\":1,\"9\":0}");
        requestBody.put("tableFlag", "accountsettings");
        requestBody.put("id", userId);

        return RestClient.doPost1("JSON", albatrossURL, "global/update-fields", 
                token, null, null, true, requestBody.toString());
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        switch (expectedResponse) {
            case "cross_account_isolation":
                // Should get unauthorized or forbidden for cross-account access
                assertThat("Cross-account access should be denied", 
                    response.getStatusCode(), anyOf(equalTo(401), equalTo(403)));
                break;
        }
    }

    @DataProvider(name = "crossAccountUpdateFieldsTestData")
    public static Object[][] crossAccountUpdateFieldsTestData() {
        return new Object[][] {
            // SCENARIO 2: Cross account isolation - Account B tries to update Account A's settings
            {"SCENARIO_2_CROSS_ACCOUNT", "AccountB", "valid", "POST_UPDATE_FIELDS_CROSS_ACCOUNT", "401", "cross_account_isolation", "Account B should not access Account A's user settings"},
        };
    }
}
