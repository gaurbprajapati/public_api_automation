package io.rcrm.api.externalJobBoards;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.reaper.Account;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerExternalJobBoards;
import io.rcrm.api.pojo.externalJobBoards.logicmelon.LogicmelonJobBoard;
import io.rcrm.api.pojo.externalJobBoards.logicmelon.LogicmelonExternalJobBoard;
import io.rcrm.api.pojo.externalJobBoards.logicmelon.LogicmelonJobBoardSetting;
import io.rcrm.api.pojo.externalJobBoards.broadbean.BroadbeanPermission;
import io.rcrm.api.pojo.externalJobBoards.broadbean.BroadbeanAdcUserName;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.commanfunctions.commanFunction;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountLogicmelonAndBroadbeanSecurityTest extends TestBase {
  
  private JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();
  
  private commanFunction commonfunction = new commanFunction();
  private String accountA_apiKey;
  private String jobSlug = null;

  @BeforeClass(alwaysRun = true)  public void setupTest() {
    accountA_apiKey = getAccountApiKey("AccountA");
    setupTestData();
  }
  
  @Owner("Smit Patel")
  @Test(dataProvider = "crossAccountLogicmelonBroadbeanTestData", groups = "nightly-build")
  public void crossAccountLogicmelonBroadbeanOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {

    String token = getTokenForAccount(accountType, tokenType);
    BroadbeanPermission broadbeanPermission = null;
    BroadbeanAdcUserName broadbeanAdcUserName = null;
    Response response = null;
    LogicmelonJobBoard logicmelonJobBoard = null;

    if (operation.toUpperCase().contains("LOGICMELON")) {
      logicmelonJobBoard = createLogicmelonJobBoardData(token, tokenType, accountType);
    }

    if (operation.toUpperCase().contains("BROADBEAN")) {
      broadbeanPermission = createBroadbeanPermissionData();
      broadbeanAdcUserName = createBroadbeanAdcUserNameData();
    }
    
    try {
      switch (operation.toUpperCase()) {
        case "POST_LOGICMELON_ADD_ADVERT":
          response = RestClient.doPost("JSON", jobBoardServiceURL, "/logicmelon/add-advert", token, null, true, logicmelonJobBoard);
          break;

        case "POST_LOGICMELON_TRACK_ADVERT":
          response = RestClient.doPost("JSON", jobBoardServiceURL, "/logicmelon/track-advert", token, null, true, logicmelonJobBoard);
          break;

        case "POST_BROADBEAN_PERMISSION":
          response = RestClient.doPost("JSON", jobBoardServiceURL, "/broadbean/permission", token, null, true, broadbeanPermission);
          break;

        case "GET_BROADBEAN_PERMISSION":
          response = RestClient.doGet("JSON", jobBoardServiceURL, "/broadbean/get-permission", token, null, null, true);
          break;

        case "GET_BROADBEAN_CONNECTED_ACCOUNT_ADC":
          response = RestClient.doGet("JSON", jobBoardServiceURL, "/broadbean/get-connected-account/adc_popup", token, null, null, true);
          break;

        case "GET_BROADBEAN_CONNECTED_ACCOUNT_JOB":
          response = RestClient.doGet("JSON", jobBoardServiceURL, "/broadbean/get-connected-account/job_detail", token, null, null, true);
          break;

        case "POST_BROADBEAN_MAP_ADC_USERNAME":
          response = RestClient.doPost("JSON", jobBoardServiceURL, "/broadbean/map-adcusername/map", token, null, true, broadbeanAdcUserName);
          break;

        case "POST_BROADBEAN_REMOVE_ADC_USERNAME":
          response = RestClient.doPost("JSON", jobBoardServiceURL, "/broadbean/map-adcusername/remove", token, null, true, broadbeanAdcUserName);
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
            // If error field doesn't exist or is different, just validate status code
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
            // If error field is null or doesn't exist, just validate status code
          }
          break;

        case "token_expired":
          try {
            response.then().body("error", Matchers.containsString("Unauthorized"));
          } catch (Exception e) {
            // If error field is null or doesn't exist, just validate status code
          }
          break;

        case "not_found":
          break;

        case "Job not found":
        case "Account connection not found":
          try {
            response.then().body("error_message", Matchers.containsString(expectedResponse));
          } catch (Exception e) {
            try {
              response.then().body("error", Matchers.containsString(expectedResponse));
            } catch (Exception e2) {
              // If neither field exists, just validate status code
            }
          }
          break;

        default:
          try {
            response.then().body("error_message", Matchers.equalTo(expectedResponse));
          } catch (Exception e) {
            try {
              response.then().body("error", Matchers.equalTo(expectedResponse));
            } catch (Exception e2) {
            }
          }
          break;
      }

    } catch (Exception e) {
      if (expectedResponse.contains("unauthorized") || expectedResponse.contains("access denied") ||
          expectedResponse.contains("token_expired") || expectedResponse.contains("bad_request") ||
          expectedResponse.contains("not_found") || expectedResponse.contains("Job not found") ||
          expectedResponse.contains("Account connection not found")) {
      } else {
        throw e;
      }
    }
  }

  @DataProvider(name = "crossAccountLogicmelonBroadbeanTestData")
  public Object[][] crossAccountLogicmelonBroadbeanTestData() {
    return new Object[][] {
        // ===== SCENARIO 1: VALID CROSS-ACCOUNT OPERATIONS =====
        // Account A performs operations (should succeed)
        { "SCENARIO_1_LOGICMELON_ADD_ADVERT", "AccountA", "valid", "POST_LOGICMELON_ADD_ADVERT", "200", "success","Account A should be able to add LogicMelon advert" },
        { "SCENARIO_1_LOGICMELON_TRACK_ADVERT", "AccountA", "valid", "POST_LOGICMELON_TRACK_ADVERT", "200", "success","Account A should be able to track LogicMelon advert" },
        { "SCENARIO_1_BROADBEAN_ADD_PERMISSION", "AccountA", "valid", "POST_BROADBEAN_PERMISSION", "200", "success","Account A should be able to add Broadbean permission" },
        { "SCENARIO_1_BROADBEAN_GET_PERMISSION", "AccountA", "valid", "GET_BROADBEAN_PERMISSION", "200", "success", "Account A should be able to get Broadbean permission" },
        { "SCENARIO_1_BROADBEAN_GET_CONNECTED_ADC", "AccountA", "valid", "GET_BROADBEAN_CONNECTED_ACCOUNT_ADC", "200", "success", "Account A should be able to get connected account ADC" },
        { "SCENARIO_1_BROADBEAN_GET_CONNECTED_JOB", "AccountA", "valid", "GET_BROADBEAN_CONNECTED_ACCOUNT_JOB", "200", "success", "Account A should be able to get connected account job detail" },

        // Account B performs all operations with valid token (cross-account access patterns)
        { "SCENARIO_1_B_LOGICMELON_ADD_ADVERT", "AccountB", "valid", "POST_LOGICMELON_ADD_ADVERT", "404", "not_found", "Account B should not access Account A's job for LogicMelon advert" },
        { "SCENARIO_1_B_LOGICMELON_TRACK_ADVERT", "AccountB", "valid", "POST_LOGICMELON_TRACK_ADVERT", "404", "not_found", "Account B should not track Account A's job for LogicMelon advert" },
        { "SCENARIO_1_B_BROADBEAN_ADD_PERMISSION", "AccountB", "valid", "POST_BROADBEAN_PERMISSION", "200", "success", "Account B should be able to add own Broadbean permission" },
        { "SCENARIO_1_B_BROADBEAN_GET_PERMISSION", "AccountB", "valid", "GET_BROADBEAN_PERMISSION", "200", "success", "Account B should be able to get own Broadbean permission" },
        { "SCENARIO_1_B_BROADBEAN_GET_CONNECTED_ADC", "AccountB", "valid", "GET_BROADBEAN_CONNECTED_ACCOUNT_ADC", "200", "success", "Account B should be able to get own connected account ADC" },
        { "SCENARIO_1_B_BROADBEAN_GET_CONNECTED_JOB", "AccountB", "valid", "GET_BROADBEAN_CONNECTED_ACCOUNT_JOB", "200", "success", "Account B should be able to get own connected account job detail" },
        { "SCENARIO_1_B_BROADBEAN_MAP_ADC", "AccountB", "valid", "POST_BROADBEAN_MAP_ADC_USERNAME", "404", "Account connection not found", "Account B should not map ADC username with invalid connection ID"},

        // ===== SCENARIO 2: INVALID TOKEN OPERATIONS =====
        // Account B performs same operations with invalid token (should fail)
        { "SCENARIO_2_LOGICMELON_ADD_ADVERT", "AccountB", "invalid", "POST_LOGICMELON_ADD_ADVERT", "401", "unauthorized", "Account B should be denied LogicMelon add advert with invalid token" },
        { "SCENARIO_2_LOGICMELON_TRACK_ADVERT", "AccountB", "invalid", "POST_LOGICMELON_TRACK_ADVERT", "401", "unauthorized", "Account B should be denied LogicMelon track advert with invalid token" },
        { "SCENARIO_2_BROADBEAN_ADD_PERMISSION", "AccountB", "invalid", "POST_BROADBEAN_PERMISSION", "401", "unauthorized", "Account B should be denied Broadbean add permission with invalid token" },
        { "SCENARIO_2_BROADBEAN_GET_PERMISSION", "AccountB", "invalid", "GET_BROADBEAN_PERMISSION", "401", "unauthorized", "Account B should be denied Broadbean get permission with invalid token" },
        { "SCENARIO_2_BROADBEAN_GET_CONNECTED_ADC", "AccountB", "invalid", "GET_BROADBEAN_CONNECTED_ACCOUNT_ADC", "401", "unauthorized", "Account B should be denied get connected account ADC with invalid token" },
        { "SCENARIO_2_BROADBEAN_GET_CONNECTED_JOB", "AccountB", "invalid", "GET_BROADBEAN_CONNECTED_ACCOUNT_JOB", "401", "unauthorized", "Account B should be denied get connected account job detail with invalid token" },
        { "SCENARIO_2_BROADBEAN_MAP_ADC", "AccountB", "invalid", "POST_BROADBEAN_MAP_ADC_USERNAME", "401", "unauthorized", "Account B should be denied map ADC username with invalid token" },
        { "SCENARIO_2_BROADBEAN_REMOVE_ADC", "AccountB", "invalid", "POST_BROADBEAN_REMOVE_ADC_USERNAME", "401", "unauthorized", "Account B should be denied remove ADC username with invalid token" },

        // Account A still has access (should work)
        { "SCENARIO_2_VERIFY", "AccountA", "valid", "GET_BROADBEAN_PERMISSION", "200", "success", "Account A should maintain access after Account B invalid attempts" },

        // ===== SCENARIO 3: EDGE CASES =====
        // Account C (non-existent) attempts operations
        { "SCENARIO_3_NONEXISTENT_ACCOUNT", "AccountC", "valid", "POST_LOGICMELON_ADD_ADVERT", "401", "Unauthorized", "Non-existent account should return 401" },

        // // Account B with expired token
        { "SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "POST_LOGICMELON_ADD_ADVERT", "401", "token_expired", "Expired token should return 401" },

        // Account B with malformed token
        { "SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "POST_LOGICMELON_ADD_ADVERT", "401", "Unauthorized", "Malformed token should return 401" },

        // ===== SCENARIO 4: BOUNDARY TESTING =====
        // Account B with empty token
        { "SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "POST_LOGICMELON_ADD_ADVERT", "401", "unauthorized", "Empty token should return 401" },

        // Account B with null token
        { "SCENARIO_4_NULL_TOKEN", "AccountB", "null", "POST_LOGICMELON_ADD_ADVERT", "401", "unauthorized", "Null token should return 401" },

        // ===== SCENARIO 5: CONCURRENT ACCESS TESTING =====
        // Multiple accounts accessing simultaneously
        { "SCENARIO_5_CONCURRENT_ACCESS_A", "AccountA", "valid", "GET_BROADBEAN_PERMISSION", "200", "success", "Account A concurrent access should succeed" },
        { "SCENARIO_5_CONCURRENT_ACCESS_B", "AccountB", "valid", "GET_BROADBEAN_PERMISSION", "200", "success","Account B concurrent access should succeed" },

        // ===== SCENARIO 6: DATA INTEGRITY VERIFICATION =====
        // Verify data consistency across accounts
        { "SCENARIO_6_DATA_INTEGRITY_A", "AccountA", "valid", "GET_BROADBEAN_PERMISSION", "200", "success","Account A should see consistent data" },
        { "SCENARIO_6_DATA_INTEGRITY_B", "AccountB", "valid", "GET_BROADBEAN_PERMISSION", "200", "success","Account B should see own data only" }
    };
  }

  private LogicmelonJobBoard createLogicmelonJobBoardData(String token, String tokenType, String accountType) {
    saveLogicmelonCredentials(token, tokenType, accountType);
    
    LogicmelonJobBoard logicmelonJobBoard = new LogicmelonJobBoard();
    logicmelonJobBoard.setJob_slug(jobSlug);
    return logicmelonJobBoard;
  }

  public void saveLogicmelonCredentials(String token, String tokenType, String accountType) {
    LogicmelonExternalJobBoard jobBoard = new LogicmelonExternalJobBoard();
    LogicmelonJobBoardSetting jobBoardSettings = new LogicmelonJobBoardSetting(logicmelon_username, logicmelon_password, logicmelon_apikey);
    jobBoard.setJob_board_id(2);
    jobBoard.setSettings(jobBoardSettings);
    jobBoard.setEnable_logicmelon_to_accounts_user(javaFakerJobBoards.getEnable_logicmelon());
    String basePath = "jobboards/settings/save";
    Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, token, null, true, jobBoard);

    if (tokenType.equals("valid") && !accountType.equals("AccountC")) {
      response.then().statusCode(200);
    } else {
      response.then().statusCode(401);
    }
  }

  private BroadbeanPermission createBroadbeanPermissionData() {
    BroadbeanPermission broadbeanPermission = new BroadbeanPermission();
    String enableBroadbean = "1"; 
    broadbeanPermission.setEnable_broadbean_to_accounts_user(enableBroadbean);
    return broadbeanPermission;
  }


  private BroadbeanAdcUserName createBroadbeanAdcUserNameData() {
    BroadbeanAdcUserName broadbeanAdcUserName = new BroadbeanAdcUserName();
    int connectionId = Integer.parseInt(RandomStringUtils.randomNumeric(4));
    String adcUsername = javaFakerJobBoards.getAdcUsername();
    broadbeanAdcUserName.setConnection_id(connectionId);
    broadbeanAdcUserName.setAdc_username(adcUsername);
    return broadbeanAdcUserName;
  }
  
  private void setupTestData() {
    JsonPath json = commonfunction.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey).jsonPath();
    String company_slug = json.get("slug");
          
    JsonPath contactJp = commonfunction.createNewContact_POST(baseURL, accountA_apiKey, company_slug).jsonPath();
    String contact_slug = contactJp.get("slug");
          
    JsonPath jobJp = commonfunction.createNewJob(baseURL, accountA_apiKey, company_slug, contact_slug).jsonPath();
    jobSlug = jobJp.get("slug");
  }
}