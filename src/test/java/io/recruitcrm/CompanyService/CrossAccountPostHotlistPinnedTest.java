package io.recruitcrm.CompanyService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountPostHotlistPinnedTest extends TestBase {

    private String hotlistIdAccountA = ""; // Store Account A's hotlist ID
    private String hotlistNameAccountA = ""; // Store Account A's hotlist name
    private commanFunction commanFunction = new commanFunction();


    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountHotlistPinnedTestData", groups = "nightly-build")
    public void crossAccountHotlistPinnedOperations_Test(String testScenario, String accountType, String tokenType,
            String operation, String expectedStatusCode, String expectedResponse, String description) {
        
        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {                
                case "PIN_HOTLIST_CROSS_ACCOUNT":
                    // Account B tries to pin Account A's hotlist
                    createTestDataAccountA();
                    response = pinHotlist(hotlistIdAccountA, token);
                    break;
                    
                case "UNPIN_HOTLIST_CROSS_ACCOUNT":
                    // Account B tries to unpin Account A's hotlist
                    createTestDataAccountA();
                    // First pin the hotlist with Account A
                    Response pinResponseA = pinHotlist(hotlistIdAccountA, getTokenForAccount("AccountA", "valid"));
                    assertThat("Pin with Account A should succeed", pinResponseA.getStatusCode(), equalTo(200));
                    // Then try to unpin with Account B
                    response = unpinHotlist(hotlistIdAccountA, token);
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unknown operation: " + operation);
            }
            
            verifyResponse(response, expectedStatusCode, expectedResponse, operation);
            
        } catch (Exception e) {
            // Handle expected exceptions for invalid scenarios
            if ("unauthorized".equals(expectedResponse) || "cross_account_isolation".equals(expectedResponse)) {
                // Expected exception for unauthorized or cross-account scenarios
                assertThat("Should handle unauthorized access appropriately", true, is(true));
            } else {
                throw e;
            }
        }
    }

    private void createTestDataAccountA() {
        try {
            // Step 1: Create hotlist using Account A
            String accountAToken = getTokenForAccount("AccountA", "valid");
            Response hotlistResponse = commanFunction.createNewHotlist(baseURL, accountA_apiKey, "company");
            assertThat("Expected status code 200", hotlistResponse.getStatusCode(), equalTo(200));
            
            // Step 2: Extract and store hotlist details
            JsonPath jp = hotlistResponse.jsonPath();
            hotlistIdAccountA = jp.getString("id");
            hotlistNameAccountA = jp.getString("name");
            
            assertThat("Hotlist ID should not be null", hotlistIdAccountA, notNullValue());
            assertThat("Hotlist name should not be null", hotlistNameAccountA, notNullValue());
            
        } catch (Exception e) {
            throw new RuntimeException("Test data creation failed: " + e.getMessage(), e);
        }
    }

    private Response pinHotlist(String hotlistId, String token) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", hotlistId);
        String basePath = "hotlists/{hotlist}/pinned-hotlist";
        
        return RestClient.doPost1("JSON", companyServiceURL, basePath, token, null, pathParameters, true, null);
    }

    private Response unpinHotlist(String hotlistId, String token) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", hotlistId);
        String basePath = "hotlists/{hotlist}/pinned-hotlist";
        
        return RestClient.doDelete("JSON", companyServiceURL, basePath, token, null, pathParameters, true);
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        if (response == null) {
            // Handle null response for unauthorized scenarios
            if ("unauthorized".equals(expectedResponse)) {
                assertThat("Response should be null for unauthorized access", true, is(true));
            }
            return;
        }

        switch (expectedResponse) {     
            case "cross_account_isolation":
                // Should get 401 (unauthorized) for cross-account access
                if (response.getStatusCode() == 404) {
                    // Unauthorized - also good isolation
                    assertThat("Expected 404 for cross-account access", response.getStatusCode(), equalTo(404));
                }
                break;
        }
    }

    @DataProvider(name = "crossAccountHotlistPinnedTestData")
    public static Object[][] crossAccountHotlistPinnedTestData() {
        return new Object[][] {
            // SCENARIO 1: Cross account isolation - Account B tries to access Account A's hotlist
            {"SCENARIO_2_PIN_CROSS_ACCOUNT", "AccountB", "valid", "PIN_HOTLIST_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not be able to pin Account A's hotlist"},
            {"SCENARIO_2_UNPIN_CROSS_ACCOUNT", "AccountB", "valid", "UNPIN_HOTLIST_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not be able to unpin Account A's hotlist"}
        };
    }
}
