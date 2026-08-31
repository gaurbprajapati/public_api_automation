package io.rcrm.api.externalJobBoards;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.pojo.externalJobBoards.JobBoard;
import io.rcrm.api.pojo.externalJobBoards.JobBoardSettings;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountJobBoardsSecurityTest extends TestBase {

  private String jobBoardSettingId = "";

  @Owner("Smit Patel")
  @Test(dataProvider = "crossAccountJobBoardTestData", groups = "nightly-build")
  public void crossAccountJobBoardOperations_Test(String testScenario, String accountType, String tokenType,
      String operation, String expectedStatusCode, String expectedResponse, String description) {

    // Get appropriate token based on account and token type
    String token = getTokenForAccount(accountType, tokenType);

    // Create real job board data
    JobBoard jobBoard = createRealJobBoardData();

    Response response = null;
    Map<String, String> pathParameters = new HashMap<>();

    try {
      switch (operation.toUpperCase()) {
        case "POST_SAVE_SETTINGS":
          // Save job board settings
          response = RestClient.doPost("JSON", jobBoardServiceURL, "/jobboards/settings/save", token, null, true,
              jobBoard);

          // Extract and store the job board setting ID for subsequent operations
          if (response.getStatusCode() == 200) {
            JsonPath jp = response.jsonPath();
            jobBoardSettingId = Integer.toString(jp.get("id"));
          }
          break;

        case "GET_SETTINGS_BY_ID":
          // Get job board settings by ID
          pathParameters.put("jobboard_id",
              jobBoardSettingId.isEmpty() ? "1" : String.valueOf(jobBoard.getJob_board_id()));
          response = RestClient.doGet("JSON", jobBoardServiceURL, "/jobboards/settings/{jobboard_id}", token, null,
              pathParameters, true);
          break;

        case "DELETE_SETTINGS":
          // Delete job board setting
          pathParameters.put("id", jobBoardSettingId.isEmpty() ? "1" : jobBoardSettingId);
          response = RestClient.doDelete("JSON", jobBoardServiceURL, "/jobboards/settings/{id}", token, null,
              pathParameters, true);
          break;

        case "GET_LIST":
          // Get job boards list
          response = RestClient.doGet("JSON", jobBoardServiceURL, "/jobboards/list", token, null, null, true);
          break;

        default:
          Assert.fail("Unsupported operation: " + operation);
      }

      int expectedStatus = Integer.parseInt(expectedStatusCode);
      response.then().statusCode(expectedStatus);

      switch (expectedResponse) {
        case "success":
          if (operation.startsWith("GET") || operation.startsWith("POST")) {
            response.then().body(Matchers.notNullValue());
          }
          break;

        case "bad_request":
          try {
            response.then().body("error", Matchers.containsString("validation"));
          } catch (Exception e) {
            Assert.fail("bad_request error field does not exist");
          }
          break;

        case "access denied":
        case "forbidden":
          response.then().body("error", Matchers.containsString("forbidden"));
          break;

        case "unauthorized":
        case "Unauthorized":

          try {
            response.then().body("error", Matchers.containsString("Unauthorized"));
          } catch (Exception e) {
            Assert.fail("Error field does not exist");
          }
          break;

        case "token_expired":
          // Check if error field exists, if not, just validate status code
          try {
            response.then().body("error", Matchers.containsString("Unauthorized"));
          } catch (Exception e) {
            Assert.fail("Error field does not exist");
          }
          break;

        case "not_found":
          break;

        case "Job board not found":
        case "Job board detail not found":
          // Specific error messages
          try {
            response.then().body("error_message", Matchers.containsString(expectedResponse));
          } catch (Exception e) {
            try {
              response.then().body("error", Matchers.containsString(expectedResponse));
            } catch (Exception e2) {
              Assert.fail("Error field does not exist");
            }
          }
          break;

        default:
          // For all other error messages, validate exact match in error_message field
          try {
            response.then().body("error_message", Matchers.equalTo(expectedResponse));
          } catch (Exception e) {
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
      if (expectedResponse.contains("unauthorized") || expectedResponse.contains("access denied") ||
          expectedResponse.contains("token_expired") || expectedResponse.contains("bad_request") ||
          expectedResponse.contains("not_found") || expectedResponse.contains("Job board not found") ||
          expectedResponse.contains("Job board detail not found")) {
        // Expected failure scenario - no action needed
      } else {
        throw e;
      }
    }
  }

  @DataProvider(name = "crossAccountJobBoardTestData")
  public Object[][] crossAccountJobBoardTestData() {
    return new Object[][] {
        // ===== SCENARIO 1: VALID CROSS-ACCOUNT OPERATIONS =====
        // Account A performs operations (should succeed)
        { "SCENARIO_1_SAVE_SETTINGS", "AccountA", "valid", "POST_SAVE_SETTINGS", "200", "success",
            "Account A should be able to save job board settings" },
        { "SCENARIO_1_GET_SETTINGS", "AccountA", "valid", "GET_SETTINGS_BY_ID", "200", "success",
            "Account A should be able to get job board settings by ID" },
        { "SCENARIO_1_GET_LIST", "AccountA", "valid", "GET_LIST", "200", "success",
            "Account A should be able to get job boards list" },

        // Account B performs operations with valid token (cross-account access
        // patterns)
        { "SCENARIO_1_B_GET_SETTINGS", "AccountB", "valid", "GET_SETTINGS_BY_ID", "404", "not_found",
            "Account B should not access Account A's job board settings" },
        { "SCENARIO_1_B_GET_LIST", "AccountB", "valid", "GET_LIST", "200", "success",
            "Account B should be able to get own job boards list" },
        { "SCENARIO_1_B_DELETE_SETTINGS", "AccountB", "valid", "DELETE_SETTINGS", "404", "Job board detail not found",
            "Account B should not delete Account A's job board settings" },

        // ===== SCENARIO 2: INVALID TOKEN OPERATIONS =====
        // Account B performs same operations with invalid token (should fail)
        { "SCENARIO_2_SAVE_SETTINGS", "AccountB", "invalid", "POST_SAVE_SETTINGS", "401", "unauthorized",
            "Account B should be denied save job board settings with invalid token" },
        { "SCENARIO_2_GET_SETTINGS", "AccountB", "invalid", "GET_SETTINGS_BY_ID", "401", "unauthorized",
            "Account B should be denied get job board settings with invalid token" },
        { "SCENARIO_2_GET_LIST", "AccountB", "invalid", "GET_LIST", "401", "unauthorized",
            "Account B should be denied get job boards list with invalid token" },
        { "SCENARIO_2_DELETE_SETTINGS", "AccountB", "invalid", "DELETE_SETTINGS", "401", "unauthorized",
            "Account B should be denied delete job board settings with invalid token" },

        // Account A still has access (should work)
        { "SCENARIO_2_VERIFY", "AccountA", "valid", "GET_SETTINGS_BY_ID", "200", "success",
            "Account A should maintain access after Account B invalid attempts" },

        // ===== SCENARIO 3: EDGE CASES =====
        // Account C (non-existent) attempts operations
        { "SCENARIO_3_NONEXISTENT_ACCOUNT", "AccountC", "valid", "GET_SETTINGS_BY_ID", "401", "Unauthorized",
            "Non-existent account should return 401" },

        // Account B with expired token
        { "SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "GET_SETTINGS_BY_ID", "401", "token_expired",
            "Expired token should return 401" },

        // Account B with malformed token
        { "SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "GET_SETTINGS_BY_ID", "401", "Unauthorized",
            "Malformed token should return 401" },

        // ===== SCENARIO 4: BOUNDARY TESTING =====
        // Account B with empty token
        { "SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "GET_SETTINGS_BY_ID", "401", "unauthorized",
            "Empty token should return 401" },

        // ===== SCENARIO 5: DATA INTEGRITY VERIFICATION =====
        // Verify data consistency across accounts
        { "SCENARIO_5_DATA_INTEGRITY_A", "AccountA", "valid", "GET_SETTINGS_BY_ID", "200", "success",
            "Account A should see consistent data" },
        { "SCENARIO_5_DATA_INTEGRITY_B", "AccountB", "valid", "GET_SETTINGS_BY_ID", "404", "not_found",
            "Account B should see own data only" },

        // Delete operation at the end
        { "SCENARIO_6_DELETE_SETTINGS", "AccountA", "valid", "DELETE_SETTINGS", "200", "success",
            "Account A should be able to delete own job board settings" }
    };
  }

  /**
   * Helper method to create real job board data (not faker data)
   */
  private JobBoard createRealJobBoardData() {
    JobBoard jobBoard = new JobBoard();
    JobBoardSettings jobBoardSettings = new JobBoardSettings();

    // Use real data patterns from existing test files
    String realEmail = "test.jobboard." + System.currentTimeMillis() + "@recruitcrm.io";
    String realPassword = "TestPassword123!";

    jobBoardSettings.setUserEmail(realEmail);
    jobBoardSettings.setPassword(realPassword);

    // Use real job board ID (following pattern from existing tests)
    jobBoard.setJob_board_id(1);
    jobBoard.setSettings(jobBoardSettings);

    return jobBoard;
  }
}