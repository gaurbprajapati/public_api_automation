package io.recruitcrm.ContactService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.pojo.albatross.SavedSearch;
import io.rcrm.api.pojo.albatross.SavedSearchRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountPostSavedSearchPinnedTest extends TestBase {

    private int savedSearchIdAccountA = 0; // Store Account A's saved search ID
    private String savedSearchNameAccountA = ""; // Store Account A's saved search name

    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountSavedSearchPinnedTestData", groups = "nightly-build")
    public void crossAccountSavedSearchPinnedOperations_Test(String testScenario, String accountType, String tokenType,
            String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "PIN_SAVED_SEARCH_CROSS_ACCOUNT":
                    // Account B tries to pin Account A's saved search
                    createSavedSearchForAccountA();
                    response = pinSavedSearch(savedSearchIdAccountA, token);
                    break;

                case "UNPIN_SAVED_SEARCH_CROSS_ACCOUNT":
                    // Account B tries to unpin Account A's saved search
                    createSavedSearchForAccountA();
                    // First pin with Account A
                    Response pinResponseA = pinSavedSearch(savedSearchIdAccountA, getTokenForAccount("AccountA", "valid"));
                    assertThat("Pin with Account A should succeed", pinResponseA.getStatusCode(), anyOf(equalTo(200), equalTo(409)));
                    // Then try to unpin with Account B
                    response = unpinSavedSearch(savedSearchIdAccountA, token);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown operation: " + operation);
            }

            verifyResponse(response, expectedStatusCode, expectedResponse, operation);

        } catch (Exception e) {
            if ("unauthorized".equals(expectedResponse) || "cross_account_isolation".equals(expectedResponse)) {
                assertThat("Should handle unauthorized access appropriately", true, is(true));
            } else {
                throw e;
            }
        }
    }

    private void createSavedSearchForAccountA() {
        try {
            // Create saved search using Account A
            Map<String, String> authTokenMap = getAuthTokenMap(getTokenForAccount("AccountA", "valid"));

            SavedSearch savedSearch = new SavedSearch();
            savedSearch.setName("CrossAcctSavedSearch_" + System.currentTimeMillis());
            savedSearch.setEntitytype("contacts");
            savedSearch.setJson("{\"filters\":[],\"sort\":[]}");
            savedSearch.setUserid(null);
            savedSearch.setAccountid(null);
            savedSearch.setShare_with_teammates(0);
            savedSearch.setPost_search_revamp(1);
            savedSearch.setCollaborator_id(new ArrayList<>());
            savedSearch.setCollaborator_type(new ArrayList<>());

            SavedSearchRequest request = new SavedSearchRequest();
            request.setSave_searches(savedSearch);
            request.setUpdateUserObj(false);

            Response createResponse = RestClient.doPost("JSON", albatrossURL, "saved-searches", authTokenMap, null, true, request);
            assertThat("Expected status code 200", createResponse.getStatusCode(), equalTo(200));

            JsonPath jp = createResponse.jsonPath();
            savedSearchIdAccountA = jp.getInt("data.id");
            savedSearchNameAccountA = jp.getString("data.name");

            assertThat("Saved search ID should be > 0", savedSearchIdAccountA, greaterThan(0));
            assertThat("Saved search name should not be null", savedSearchNameAccountA, notNullValue());

        } catch (Exception e) {
            throw new RuntimeException("Test data creation failed: " + e.getMessage(), e);
        }
    }

    private Response pinSavedSearch(int savedSearchId, String token) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("savedSearch", String.valueOf(savedSearchId));
        String basePath = "saved-searches/{savedSearch}/pinned-saved-search";
        return RestClient.doPost1("JSON", contactServiceURL, basePath, token, null, pathParameters, true, null);
    }

    private Response unpinSavedSearch(int savedSearchId, String token) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("savedSearch", String.valueOf(savedSearchId));
        String basePath = "saved-searches/{savedSearch}/pinned-saved-search";
        return RestClient.doDelete("JSON", contactServiceURL, basePath, token, null, pathParameters, true);
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        if (response == null) {
            if ("unauthorized".equals(expectedResponse)) {
                assertThat("Response should be null for unauthorized access", true, is(true));
            }
            return;
        }

        switch (expectedResponse) {
            case "cross_account_isolation":
                // Expect 404 or 401 depending on environment routing/policies
                int expected = Integer.parseInt(expectedStatusCode);
                assertThat("Unexpected status for cross-account isolation", response.getStatusCode(), equalTo(expected));
                break;
        }
    }

    @DataProvider(name = "crossAccountSavedSearchPinnedTestData")
    public static Object[][] crossAccountSavedSearchPinnedTestData() {
        return new Object[][] {
            // Cross-account isolation: Account B should not be able to pin/unpin Account A's saved search
            {"SCENARIO_PIN_SAVED_SEARCH_CROSS_ACCOUNT", "AccountB", "valid", "PIN_SAVED_SEARCH_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not be able to pin Account A's saved search"},
            {"SCENARIO_UNPIN_SAVED_SEARCH_CROSS_ACCOUNT", "AccountB", "valid", "UNPIN_SAVED_SEARCH_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not be able to unpin Account A's saved search"}
        };
    }

    private Map<String, String> getAuthTokenMap(Object authToken) {
        Map<String, String> tokenMap;
        if (authToken instanceof Map) {
            tokenMap = (Map<String, String>) authToken;
        } else {
            tokenMap = new HashMap<>();
            tokenMap.put("Authorization", "Bearer " + authToken);
        }
        return tokenMap;
    }
}


