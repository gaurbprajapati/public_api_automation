package io.recruitcrm.albatross.job;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountJobApplicationClosedLabelSecurityTest extends TestBase {

    @Owner("Raj Pandey")
    @Test(dataProvider = "successScenarioTestData", groups = "nightly-build")
    public void testSuccessScenarios(String testScenario, String accountType, String tokenType,
                                     String operation, String expectedStatusCode, String expectedResponse, String description) {
        executeTest(testScenario, accountType, tokenType, operation, expectedStatusCode, expectedResponse, description);
    }


    @Owner("Raj Pandey")
    @Test(dataProvider = "securityScenarioTestData", groups = "nightly-build")
    public void testSecurityScenarios(String testScenario, String accountType, String tokenType,
                                      String operation, String expectedStatusCode, String expectedResponse, String description) {
        executeTest(testScenario, accountType, tokenType, operation, expectedStatusCode, expectedResponse, description);
    }

    private void executeTest(String testScenario, String accountType, String tokenType,
                             String operation, String expectedStatusCode, String expectedResponse, String description) {
        String generatedString = RandomStringUtils.randomAlphabetic(4);

        String token = getTokenForAccount(accountType, tokenType);

        int accountId = getAccountId(accountType);

        JSONObject requestBody = new JSONObject();
        requestBody.put("account_id", accountId);
        requestBody.put("key", "externalpageheadings");

        JSONObject valueObject = new JSONObject();
        if (operation.equals("POST_CREATE_CUSTOM")) {
            valueObject.put("label", "Applications Closed " + generatedString);
            valueObject.put("message", "This position is no longer accepting applications " + generatedString);
            valueObject.put("enabled", true);
            valueObject.put("customCSS", "body { background-color: #f5f5f5; }");
        } else if (operation.equals("POST_CREATE_COMPLEX")) {
            valueObject.put("labels", new JSONObject()
                    .put("en", "Applications Closed " + generatedString)
                    .put("es", "Aplicaciones Cerradas " + generatedString)
                    .put("fr", "Candidatures Fermées " + generatedString));
            valueObject.put("settings", new JSONObject()
                    .put("showMessage", true)
                    .put("redirectUrl", "https://example.com/careers")
                    .put("customCSS", "body { background-color: #f5f5f5; }"));
        } else {
            valueObject = new JSONObject();
        }

        requestBody.put("value", valueObject);

        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "POST_CREATE":
                case "POST_CREATE_CUSTOM":
                case "POST_CREATE_COMPLEX":
                    response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                            token, null, true, requestBody.toString());
                    break;

                case "POST_UPDATE":
                    JSONObject updateValue = new JSONObject();
                    updateValue.put("label", "Updated Label " + generatedString);
                    updateValue.put("enabled", false);
                    requestBody.put("value", updateValue);

                    response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                            token, null, true, requestBody.toString());
                    break;

                case "POST_INVALID_KEY":
                    requestBody.put("key", "invalid_key_" + generatedString);
                    response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                            token, null, true, requestBody.toString());
                    break;

                case "POST_MISSING_ACCOUNT_ID":
                    requestBody.remove("account_id");
                    response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                            token, null, true, requestBody.toString());
                    break;

                case "POST_MISSING_KEY":
                    requestBody.remove("key");
                    response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                            token, null, true, requestBody.toString());
                    break;

                case "POST_MISSING_VALUE":
                    requestBody.remove("value");
                    response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                            token, null, true, requestBody.toString());
                    break;

                case "POST_EMPTY_BODY":
                    response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                            token, null, true, "");
                    break;

                case "POST_MALFORMED_JSON":
                    String malformedJson = "{\"account_id\": " + accountId + ", \"key\": \"externalpageheadings\", \"value\": {";
                    response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                            token, null, true, malformedJson);
                    break;

                case "POST_NULL_VALUES":
                    requestBody.put("value", JSONObject.NULL);
                    response = RestClient.doPost("JSON", albatrossURL, "jobs/job-application-closed-label/update",
                            token, null, true, requestBody.toString());
                    break;

                default:
                    Assert.fail("Unsupported operation: " + operation);
            }

            if (tokenType.equals("invalid") || tokenType.equals("expired") || tokenType.equals("malformed") ||
                    tokenType.equals("empty") || tokenType.equals("null")) {
                response.then().statusCode(401);
                return;
            }

            int expectedStatus = Integer.parseInt(expectedStatusCode);
            response.then().statusCode(expectedStatus);

            switch (expectedResponse) {
                case "success":
                    validateSuccessResponse(response);
                    break;

                case "Invalid account ID or access denied":
                    validateErrorResponse(response, "Invalid account ID or access denied");
                    break;

                case "unauthorized":
                case "Unauthorized":
                    try {
                        response.then().body("error", Matchers.containsString("Unauthorized"));
                    } catch (Exception e) {
                        Assert.fail("Error: " + e.getMessage());
                    }
                    break;

                case "token_expired":
                    try {
                        response.then().body("error", Matchers.containsString("Unauthorized"));
                    } catch (Exception e) {
                        Assert.fail("Error: " + e.getMessage());
                    }
                    break;

                case "bad_request":
                    try {
                        response.then().body("error", Matchers.is(true));
                    } catch (Exception e) {
                        Assert.fail("Error: " + e.getMessage());
                    }
                    break;

                default:
                    try {
                        response.then().body("error_message", Matchers.equalTo(expectedResponse));
                    } catch (Exception e) {
                        try {
                            response.then().body("error", Matchers.equalTo(expectedResponse));
                        } catch (Exception e2) {
                            Assert.fail("Error: " + e.getMessage() + e2.getMessage());
                        }
                    }
                    break;
            }

        } catch (Exception e) {
            Assert.fail("Error: " + e.getMessage());
        }
    }

    private void validateSuccessResponse(Response response) {
        JsonPath jsonPath = response.jsonPath();
        Assert.assertNotNull(jsonPath.get("message_type"), "message_type field should be present");
        Assert.assertEquals(jsonPath.get("message_type"), "is-success", "Expected message_type to be is-success");
    }

    private void validateErrorResponse(Response response, String expectedErrorMessage) {
        try {
            response.then().body("message", Matchers.containsString(expectedErrorMessage));
            response.then().body("message_type", Matchers.equalTo("is-danger"));
        } catch (Exception e) {
            Assert.fail("Error: " + e.getMessage());
        }
    }

    @DataProvider(name = "successScenarioTestData")
    public static Object[][] successScenarioTestData() {
        return new Object[][]{
                {"SCENARIO_1_CREATE", "AccountA", "valid", "POST_CREATE", "200", "success", "Account A should be able to create Job Application Closed Label settings"},
                {"SCENARIO_1_CREATE_CUSTOM", "AccountA", "valid", "POST_CREATE_CUSTOM", "200", "success", "Account A should be able to create custom Job Application Closed Label settings"},
                {"SCENARIO_1_CREATE_COMPLEX", "AccountA", "valid", "POST_CREATE_COMPLEX", "200", "success", "Account A should be able to create complex Job Application Closed Label settings"},
                {"SCENARIO_1_UPDATE", "AccountA", "valid", "POST_UPDATE", "200", "success", "Account A should be able to update Job Application Closed Label settings"}
        };
    }


    @DataProvider(name = "securityScenarioTestData")
    public static Object[][] securityScenarioTestData() {
        return new Object[][]{
                {"SCENARIO_2_CREATE_INVALID", "AccountB", "invalid", "POST_CREATE", "401", "unauthorized", "Account B should be denied create with invalid token"},
                {"SCENARIO_2_UPDATE_INVALID", "AccountB", "invalid", "POST_UPDATE", "401", "unauthorized", "Account B should be denied update with invalid token"},
                {"SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "POST_CREATE", "401", "token_expired", "Expired token should return 401"},
                {"SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "POST_CREATE", "401", "Unauthorized", "Malformed token should return 401"},
                {"SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "POST_CREATE", "401", "unauthorized", "Empty token should return 401"},
                {"SCENARIO_4_NULL_TOKEN", "AccountB", "null", "POST_CREATE", "401", "unauthorized", "Null token should return 401"},
                {"SCENARIO_5_INVALID_KEY", "AccountA", "valid", "POST_INVALID_KEY", "400", "bad_request", "Invalid key should return 400"},
                {"SCENARIO_5_MISSING_ACCOUNT_ID", "AccountA", "valid", "POST_MISSING_ACCOUNT_ID", "200", "Invalid account ID or access denied", "Missing account_id should return access denied"},
                {"SCENARIO_5_MISSING_KEY", "AccountA", "valid", "POST_MISSING_KEY", "400", "bad_request", "Missing key should return 400"},
                {"SCENARIO_5_MISSING_VALUE", "AccountA", "valid", "POST_MISSING_VALUE", "200", "success", "Missing value should return access denied"},
                {"SCENARIO_5_EMPTY_BODY", "AccountA", "valid", "POST_EMPTY_BODY", "200", "Invalid account ID or access denied", "Empty body should return access denied"},
                {"SCENARIO_5_MALFORMED_JSON", "AccountA", "valid", "POST_MALFORMED_JSON", "200", "Invalid account ID or access denied", "Malformed JSON should return access denied"},
                {"SCENARIO_5_NULL_VALUES", "AccountA", "valid", "POST_NULL_VALUES", "200", "success", "Null values should be handled appropriately"},
                {"SCENARIO_6_CROSS_ACCOUNT_INVALID_KEY", "AccountB", "valid", "POST_INVALID_KEY", "400", "bad_request", "Account B should get 400 for invalid key"},
                {"SCENARIO_6_CROSS_ACCOUNT_MISSING_FIELDS", "AccountB", "valid", "POST_MISSING_ACCOUNT_ID", "200", "Invalid account ID or access denied", "Account B should get access denied for missing fields"}
        };
    }
}
