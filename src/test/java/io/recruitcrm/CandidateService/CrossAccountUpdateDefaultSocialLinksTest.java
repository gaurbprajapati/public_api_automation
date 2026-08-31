package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountUpdateDefaultSocialLinksTest extends TestBase {

    private int candidateIdAccountA;
    private String customColumnIdAccountA;
    private AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    private commanFunction function = new commanFunction();

    @BeforeClass
    public void setUp() {
        // Create test data for Account A
        createTestDataAccountA();
    }

    private void createTestDataAccountA() {
        try {
            // Step 1: Get an existing candidate by slug using Account A
            String candidateSlug = function.getEntityResponse(baseURL, accountA_apiKey, "candidate");
            assertThat("Failed to get candidate slug", candidateSlug, notNullValue());

            Response candidateResponse = albatrossFunctions.getCandidateResponse(albatrossURL, accountA_Token, candidateSlug);
            assertThat("Failed to get candidate by slug", candidateResponse.getStatusCode(), equalTo(200));

            JsonPath candidateJp = candidateResponse.jsonPath();
            candidateIdAccountA = candidateJp.get("data.candidate.id");
            assertThat("Candidate ID should not be null", candidateIdAccountA, notNullValue());

            // Step 2: Create a social_profile custom field for candidate using Account A with existing function
            String customFieldName = "Social Profile Test Field Account A";
            String customFieldType = "social_profile";
            String defaultOptions = ""; // No options needed for social_profile type

            commanFunction function = new commanFunction();
            Response customFieldResponse = function.createCustomFieldsResponse(albatrossURL, accountA_Token,
                    "candidate", customFieldName, customFieldType, defaultOptions);
            assertThat("Failed to create custom field", customFieldResponse.getStatusCode(), equalTo(200));

            JsonPath customFieldJp = customFieldResponse.jsonPath();
            int actualCustomColumnId = customFieldJp.get("data.custumField.columnid");
            customColumnIdAccountA = "custcolumn" + String.valueOf(actualCustomColumnId);
            assertThat("Custom column ID should not be null", actualCustomColumnId, notNullValue());

        } catch (Exception e) {
            throw new RuntimeException("Test data creation failed: " + e.getMessage(), e);
        }
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountSocialLinksTestData", groups = "nightly-build")
    public void crossAccountUpdateDefaultSocialLinksOperations_Test(String testScenario, String accountType, String tokenType,
                                                                    String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "UPDATE_SOCIAL_LINKS_CROSS_ACCOUNT":
                    // Account B tries to update Account A's candidate social links
                    response = updateSocialLinks(token, candidateIdAccountA, customColumnIdAccountA);
                    break;
            }

            verifyResponse(response, expectedStatusCode, expectedResponse, operation, description);

        } catch (Exception e) {
            // Handle expected exceptions
            if (expectedResponse.equals("unauthorized") || expectedResponse.equals("forbidden")) {
                // Expected exception for unauthorized access
                assertThat("Expected unauthorized access", true, is(true));
            } else {
                throw e;
            }
        }
    }

    private Response updateSocialLinks(String token, int candidateId, String customColumnId) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("xingUrl", "https://www.xing.com");
        requestBody.put("linkedinUrl", "https://www.linkedin.com");
        requestBody.put("twitterUrl", "https://www.twitter.com");
        requestBody.put("facebookUrl", "https://www.fb.com");
        requestBody.put("githubUrl", "https://www.github.com");
        requestBody.put("candidateId", candidateId);
        requestBody.put("entityId", 5);

        // Create socialFieldUrls array
        JSONArray socialFieldUrls = new JSONArray();
        JSONObject socialField = new JSONObject();
        socialField.put("customColumnId", customColumnId);
        socialField.put("url", "https://www.linkedinsingle.com");
        socialFieldUrls.put(socialField);
        requestBody.put("socialFieldUrls", socialFieldUrls);

        // Make API call
        return RestClient.doPost1("JSON", albatrossURL, "candidates/update-default-social-links",
                token, null, null, true, requestBody.toString());
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation, String description) {
        int expectedStatus = Integer.parseInt(expectedStatusCode);

        switch (expectedResponse) {
            case "cross_account_isolation":
                // Should get empty results or unauthorized
                if (response.getStatusCode() == 200) {
                    // Verify empty results or no access to other account's data
                    JsonPath crossAccountJp = response.jsonPath();
                    // Check if response indicates no access to other account's data
                    assertThat("The message should be Update Candidate : Access Denied",
                            crossAccountJp.get("message"), equalTo("Update Candidate : Access Denied"));
                    assertThat("Should not have access to other account's data",
                            (Integer) crossAccountJp.get("data.size()"), equalTo(0));
                } else {
                    assertThat("Expected status code " + expectedStatusCode + " for " + description,
                            response.getStatusCode(), equalTo(expectedStatus));
                }
                break;
        }
    }


    @DataProvider(name = "crossAccountSocialLinksTestData")
    public static Object[][] crossAccountSocialLinksTestData() {
        return new Object[][]{
                // SCENARIO 2: Cross account isolation - Account B tries to access Account A's data
                {"SCENARIO_2_CROSS_ACCOUNT", "AccountB", "valid", "UPDATE_SOCIAL_LINKS_CROSS_ACCOUNT", "200", "cross_account_isolation", "Account B should not access Account A's candidate data"},
        };
    }
}
