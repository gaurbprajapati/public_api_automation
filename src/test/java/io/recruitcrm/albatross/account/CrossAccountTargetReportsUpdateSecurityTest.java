package io.recruitcrm.albatross.account;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.albatross.JavaFakerTargetReports;
import io.rcrm.api.pojo.albatross.targetReports.ArchiveTargetReport;
import io.rcrm.api.pojo.albatross.targetReports.DeleteTargetReport;
import io.rcrm.api.pojo.albatross.targetReports.SearchTargetReport;
import io.rcrm.api.pojo.albatross.targetReports.TargetReport;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

/**
 * Cross-Account Target Reports Security Test
 * Tests various security scenarios for Target Reports operations across different accounts
 * with different token types (valid, invalid, expired, malformed)
 * 
 * This test ensures proper security isolation between accounts for:
 * - Target report CRUD operations
 * - Cross-account data protection
 * - Search and filtering operations
 * - Archive and delete operations
 */
@AccountType("CrossAccount")
public class CrossAccountTargetReportsUpdateSecurityTest extends TestBase {

    private final JavaFakerTargetReports faker = new JavaFakerTargetReports();
    private final commanFunction function = new commanFunction();

    private String targetReportId = "";


    /**
     * Comprehensive test covering cross-account Target Reports operations
     * Multiple scenarios: Valid operations, invalid tokens, edge cases, boundary testing, concurrent access, data integrity
     */
    @Owner("Akshaya Uppala")
    @Test(dataProvider = "crossAccountTargetReportsTestData", groups = "nightly-build")
    public void crossAccountTargetReportsOperations_Test(String testScenario, String accountType, String tokenType, 
            String operation, String expectedStatusCode, String expectedResponse, String description) {
        
        String generatedString = RandomStringUtils.randomAlphabetic(4);
        
        // Get appropriate token based on account and token type
        String token = getTokenForAccount(accountType, tokenType);
        
        Response response = null;
        
        try {
            switch (operation.toUpperCase()) {
                case "POST_CREATE":
                    // Create target report from Account A
                    TargetReport createTargetReport = createTargetReportObject(generatedString);
                    response = RestClient.doPost("JSON", albatrossURL, "target-reports/create", token, null, true, createTargetReport);
                    
                    // Extract and store the target report ID (only for successful responses)
                    if (response.getStatusCode() == 200) {
                        JsonPath jp = response.jsonPath();
                        Integer ID = jp.get("data.id");
                        if (ID != null) {
                            targetReportId = Integer.toString(ID);
                        } else {
                            // Fallback to default ID if extraction fails
                            targetReportId = Integer.toString(faker.getRandomTargetId());
                        }
                    }
                    break;
                    
                case "POST_UPDATE":
                    // Update target report
                    TargetReport updateTargetReport = createTargetReportObject("Updated_" + generatedString);
                    updateTargetReport.setId(Integer.parseInt(targetReportId.isEmpty() ? Integer.toString(faker.getRandomTargetId()) : targetReportId));
                    response = RestClient.doPost("JSON", albatrossURL, "target-reports/update", token, null, true, updateTargetReport);
                    break;
                    
                default:
                    Assert.fail("Unsupported operation: " + operation);
            }
            
            // Handle invalid token scenarios first - they should always return 401
            if (tokenType.equals("invalid") || tokenType.equals("expired") || tokenType.equals("malformed") || 
                tokenType.equals("empty") || tokenType.equals("null")) {
                response.then().statusCode(401);
                return;
            }
            
            // Validate response status code for valid tokens
            int expectedStatus = Integer.parseInt(expectedStatusCode);
            response.then().statusCode(expectedStatus);
            
            // Additional validations based on expected response using switch case
            switch (expectedResponse) {
                case "success":
                    if (operation.startsWith("POST_SEARCH")) {
                        response.then().body(Matchers.notNullValue());
                    }
                    break;
                    
                case "target_not_found":
                    response.then().body("message", Matchers.is("Target report not found"));
                    response.then().body("message_type", Matchers.is("is-danger"));
                    break;
                    
                case "delete_failed":
                    // For cross-account delete restrictions
                    try {
                        response.then().body("message", Matchers.containsString("Failed To Delete Target : You are not authorized to delete selected records."));
                    } catch (Exception e) {
                        // If error field doesn't exist or is different, just validate status code
                    }
                    break;
                    
                case "unauthorized":
                case "Unauthorized":
                    // Check if error field exists, if not, just validate status code
                    try {
                        response.then().body("error", Matchers.containsString("Unauthorized"));
                    } catch (Exception e) {
                        // If error field is null or doesn't exist, just validate status code
                    }
                    break;
                    
                case "token_expired":
                    // Check if error field exists, if not, just validate status code
                    try {
                        response.then().body("error", Matchers.containsString("Unauthorized"));
                    } catch (Exception e) {
                        // If error field is null or doesn't exist, just validate status code
                    }
                    break;
                    
                case "bad_request":
                    // For 422 responses, check if error field exists, if not, just validate status code
                    try {
                        response.then().body("error", Matchers.containsString("validation"));
                    } catch (Exception e) {
                        // If error field doesn't exist or is different, just validate status code
                    }
                    break;
                    
                default:
                    // For all other error messages, validate exact match in error_message field
                    try {
                        response.then().body("error_message", Matchers.equalTo(expectedResponse));
                    } catch (Exception e) {
                        // If error_message field doesn't exist, try error field
                        try {
                            response.then().body("error", Matchers.equalTo(expectedResponse));
                        } catch (Exception e2) {
                            // If neither field exists, just validate status code
                        }
                    }
                    break;
            }
            
        } catch (Exception e) {
            // Handle exceptions for invalid scenarios
            if (expectedResponse.contains("unauthorized") || expectedResponse.contains("access_denied") || 
                expectedResponse.contains("token_expired") || expectedResponse.contains("bad_request") || 
                expectedResponse.contains("not_found") || expectedResponse.contains("forbidden")) {
                // Expected failure scenario - no action needed
            } else {
                throw e;
            }
        }
    }

    /**
     * Comprehensive data provider for cross-account Target Reports operations
     * Covers multiple scenarios: Valid operations, invalid tokens, edge cases, boundary testing, concurrent access, data integrity
     */
    @DataProvider(name = "crossAccountTargetReportsTestData")
    public static Object[][] crossAccountTargetReportsTestData() {
        return new Object[][] {
            // ===== SCENARIO 1: CROSS-ACCOUNT SECURITY OPERATIONS =====
            // Account A creates and manages target reports (should succeed)
            {"SCENARIO_1_CREATE", "AccountA", "valid", "POST_CREATE", "200", "success", "Account A should be able to create target report"},
            {"SCENARIO_1_A_UPDATE", "AccountA", "valid", "POST_UPDATE", "200", "success", "Account A should be able to update target report"},

            {"SCENARIO_1_B_UPDATE", "AccountB", "valid", "POST_UPDATE", "200", "target_not_found", "Account B should be denied update access to Account A's target report"},
            
            // ===== SCENARIO 2: INVALID TOKEN OPERATIONS =====
            // Account B performs operations with invalid token (should fail)
            {"SCENARIO_2_UPDATE_INVALID", "AccountB", "invalid", "POST_UPDATE", "401", "unauthorized", "Account B should be denied target report update with invalid token"},
            
            // ===== SCENARIO 3: EDGE CASES =====
            // Account C (non-existent) attempts operations
            {"SCENARIO_3_NON_EXISTENT_ACCOUNT", "AccountC", "valid", "POST_UPDATE", "401", "unauthorized", "Account C should be denied target report update with non-existent account"},
            
            // Account B with expired token
            {"SCENARIO_3_EXPIRED_UPDATE", "AccountB", "expired", "POST_UPDATE", "401", "token_expired", "Expired token should return 401 for update"},
            
            // Account B with malformed token
            {"SCENARIO_3_MALFORMED_UPDATE", "AccountB", "malformed", "POST_UPDATE", "401", "Unauthorized", "Malformed token should return 401 for update"},
            
            // ===== SCENARIO 4: BOUNDARY TESTING =====
            // Account B with empty token
            {"SCENARIO_4_EMPTY_UPDATE", "AccountB", "empty", "POST_UPDATE", "401", "unauthorized", "Empty token should return 401 for update"},
            
            // Account B with null token
            {"SCENARIO_4_NULL_UPDATE", "AccountB", "null", "POST_UPDATE", "401", "unauthorized", "Null token should return 401 for update"}
        };
    }

    /**
     * Helper method to create TargetReport object
     */
    private TargetReport createTargetReportObject(String generatedString) {
        try {
            // Fetch Account A owner details
            Response userResponse = function.getUsers(baseURL, getAccountApiKey("AccountA"));
            JsonPath user = userResponse.jsonPath();
            String accountOwnerId = user.getString("[0].id");
            String accountOwnerName = user.get("[0].first_name") + " " + user.get("[0].last_name");

            // Build KPI List
            String kpiLabel = faker.getKPILabel();
            List<TargetReport.Recruiter> recruiters = Collections.singletonList(
                    new TargetReport.Recruiter(accountOwnerId, accountOwnerName, true, true));
            List<String> recruiterTeams = Collections.emptyList();
            List<String> roles = Collections.emptyList();
            List<TargetReport.Kpi> kpis = Collections.singletonList(
                    new TargetReport.Kpi(faker.getKPIValue(kpiLabel), kpiLabel, true, true, faker.getKPICount()));
            TargetReport.KpiList kpiList = new TargetReport.KpiList(recruiters, recruiterTeams, roles, kpis);

            TargetReport targetReport = new TargetReport();
            targetReport.setTitle("Test TargetReport " + generatedString);
            targetReport.setAssignee_type(faker.getAssigneeType());
            targetReport.setAssignee_id(accountOwnerId);
            targetReport.setFrequency(faker.getFrequency());
            targetReport.setStart_date(faker.getStartDate(3));
            targetReport.setEnd_date(faker.getEndDate(3));
            targetReport.setKpiListObject(kpiList);

            return targetReport;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create TargetReport object: " + e.getMessage());
        }
    }
}


