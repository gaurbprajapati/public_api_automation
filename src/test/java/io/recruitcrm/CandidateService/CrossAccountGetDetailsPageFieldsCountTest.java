package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountGetDetailsPageFieldsCountTest extends TestBase {

    private int accountA_AccountId;
    private int accountB_AccountId;
    private int accountA_UserId;
    private int accountB_UserId;
    private AllCrudFunctions albatrossFunctions = new AllCrudFunctions();

    @BeforeClass
    public void setUp() {
        // Get dynamic account IDs from TestBase
        accountA_AccountId = accountA.getAccountId();
        accountB_AccountId = accountB.getAccountId();
        
        // Get dynamic user IDs for both accounts
        setupUserIds();
    }
    
    private void setupUserIds() {
        // Get user ID for Account A
        Response getUsersA = albatrossFunctions.getUsers(albatrossURL, accountA_Token);
        assertThat("Failed to get users for Account A", getUsersA.getStatusCode(), equalTo(200));
        JsonPath jpA = getUsersA.jsonPath();
        accountA_UserId = jpA.get("data.records[0].id");
        assertThat("Account A User ID should not be null", accountA_UserId, notNullValue());
        
        // Get user ID for Account B
        Response getUsersB = albatrossFunctions.getUsers(albatrossURL, accountB_Token);
        assertThat("Failed to get users for Account B", getUsersB.getStatusCode(), equalTo(200));
        JsonPath jpB = getUsersB.jsonPath();
        accountB_UserId = jpB.get("data.records[0].id");
        assertThat("Account B User ID should not be null", accountB_UserId, notNullValue());
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountDetailsPageFieldsCountTestData", groups = "nightly-build")
    public void crossAccountDetailsPageFieldsCountOperations_Test(String testScenario, String accountType, String tokenType,
            String operation, String expectedStatusCode, String expectedResponse, String description) {
        
        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "GET_DETAILS_PAGE_FIELDS_COUNT_CROSS_ACCOUNT":
                    // Account B tries to access Account A's data
                    response = getDetailsPageFieldsCount(accountA_AccountId, accountA_UserId, token);
                    break;
            }
            
            verifyResponse(response, expectedStatusCode, expectedResponse, operation);
            
        } catch (Exception e) {
            // Handle expected exceptions
            assertThat("Unexpected exception: " + e.getMessage(), false, is(true));
        }
    }

    private Response getDetailsPageFieldsCount(int accountId, int userId, String token) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", String.valueOf(userId));

        return RestClient.doGet("JSON", candidatesURL, "candidates/details-page/fields-count", 
                token, queryParams, null, true);
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        switch (expectedResponse) {
            case "cross_account_isolation":
                // Should get empty results or unauthorized
                if (response.getStatusCode() == 401) {
                    // Verify empty results or no access to other account's data
                    JsonPath isolationJp = response.jsonPath();
                    assertThat("Meta object should not be null", isolationJp.get("meta"), notNullValue());
                    assertThat("Meta status should be 401", (Integer) isolationJp.get("meta.status"), equalTo(401));
                    assertThat("Data should not be null", isolationJp.get("data"), nullValue());
                    // Cross account isolation - should return data but for the requesting account only
                } else {
                    assertThat("Expected status code", response.getStatusCode(), equalTo(Integer.parseInt(expectedStatusCode)));
                }
                break;
        }
    }

    @DataProvider(name = "crossAccountDetailsPageFieldsCountTestData")
    public static Object[][] crossAccountDetailsPageFieldsCountTestData() {
        return new Object[][] {
            // SCENARIO: Cross account isolation - Account B tries to access Account A's data
            {"SCENARIO_CROSS_ACCOUNT", "AccountB", "valid", "GET_DETAILS_PAGE_FIELDS_COUNT_CROSS_ACCOUNT", "401", "cross_account_isolation", "Account B should not access Account A's data"},
        };
    }
}
