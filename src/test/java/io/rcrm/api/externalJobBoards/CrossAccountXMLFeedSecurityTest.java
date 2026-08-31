package io.rcrm.api.externalJobBoards;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerXmlFeed;
import io.rcrm.api.pojo.albatross.xmlfeed.SaveCustomXmlFeed;
import io.rcrm.api.pojo.albatross.xmlfeed.ListXmlFeeds;
import io.rcrm.api.pojo.albatross.xmlfeed.PreselectXMLFeed;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountXMLFeedSecurityTest extends TestBase {

  private String xmlFeedId = "";
  private String accountSlug = "";

  private JavaFakerXmlFeed javaFakerXmlFeed = new JavaFakerXmlFeed();

  @Owner("Smit Patel")
  @Test(dataProvider = "crossAccountXMLFeedTestData", groups = "nightly-build")
  public void crossAccountXMLFeedOperations_Test(String testScenario, String accountType, String tokenType,
      String operation, String expectedStatusCode, String expectedResponse, String description) {

    // Get appropriate token based on account and token type
    String token = getTokenForAccount(accountType, tokenType);

    SaveCustomXmlFeed xmlFeedData = createXMLFeedData();
    ListXmlFeeds listXmlFeeds = new ListXmlFeeds("updated_on", "asc");

    Response response = null;
    Map<String, String> pathParameters = new HashMap<>();

    try {
      switch (operation.toUpperCase()) {
        case "POST_SAVE_XML_FEED":
          // Save custom XML feed
          response = RestClient.doPost("JSON", jobBoardServiceURL, "custom-xml/save", token, null, true, xmlFeedData);

          if (response.getStatusCode() == 200) {
            // Extract and store the XML feed ID if successful
            try {
              JsonPath jp = response.jsonPath();
              if (jp.get("data.id") != null) {
                xmlFeedId = jp.get("data.id").toString();
              }

            } catch (Exception e) {
            }
          }
          break;

        case "POST_UPDATE_XML_FEED":
          // Update custom XML feed
          String updatePath = "custom-xml/update-feed/" + xmlFeedId;
          response = RestClient.doPost1("JSON", jobBoardServiceURL, updatePath, token, null, pathParameters, true,
              xmlFeedData);
          break;

        case "POST_LIST_XML_FEEDS":
          // List custom XML feeds
          String listPath = "custom-xml/list";
          response = RestClient.doPost("JSON", jobBoardServiceURL, listPath, token, null, true, listXmlFeeds);
          break;

        case "POST_MARK_DEFAULT":
          // Mark job board as default
          PreselectXMLFeed preSelectXMLFeed = new PreselectXMLFeed(19, 1, getAccountId("AccountA"), 1, "JobRapido");
          String markDefaultPath = "custom-xml/mark-jobboard-default";
          response = RestClient.doPost("JSON", jobBoardServiceURL, markDefaultPath, token, null, true, preSelectXMLFeed);
          break;

        case "POST_GENERATE_PREVIEW":
          // Generate XML preview
          String useAccountSlug = accountSlug.isEmpty() ? "test-account-slug" : accountSlug;
          pathParameters.put("accountSlug", useAccountSlug);
          String previewPath = "custom-xml/" + useAccountSlug;
          response = RestClient.doPost1("JSON", jobBoardServiceURL, previewPath, token, null, pathParameters, true,
              xmlFeedData);
          break;

        case "DELETE_XML_FEED":
          // Delete custom XML feed
          String deleteXmlFeedId = xmlFeedId.isEmpty() ? "1001" : xmlFeedId;
          pathParameters.put("xmlId", deleteXmlFeedId);
          String deletePath = "custom-xml/{xmlId}";
          response = RestClient.doDelete("JSON", jobBoardServiceURL, deletePath, token, null, pathParameters, false);
          break;

        case "POST_GET_XML_FEEDS_FOR_JOB":
          // Get XML feeds for job
          Map<String, Object> jobData = new HashMap<>();
          jobData.put("job_id", "test-job-id");
          jobData.put("account_id", "test-account-id");
          String getXmlFeedsPath = "xml-feeds/get-xml-feeds-for-job";
          response = RestClient.doPost("JSON", jobBoardServiceURL, getXmlFeedsPath, token, null, true, jobData);
          break;

        default:
          Assert.fail("Unsupported operation: " + operation);
      }

      int expectedStatus = Integer.parseInt(expectedStatusCode);

      response.then().statusCode(expectedStatus);

      switch (expectedResponse) {
        case "success":
          if (operation.startsWith("GET") || operation.startsWith("POST_LIST")) {
            response.then().body(Matchers.notNullValue());
          }
          break;

        case "bad_request":
          try {
            response.then().body("error", Matchers.containsString("validation"));
          } catch (Exception e) {
            Assert.fail("Error field does not exist");
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
          try {
            response.then().body("error", Matchers.containsString("Unauthorized"));
          } catch (Exception e) {
            Assert.fail("Error field does not exist");
          }
          break;

        case "not_found":
          break;

        case "XML feed not found":
        case "Account not found":
          // XML feed specific error messages
          try {
            response.then().body("message", Matchers.containsString(expectedResponse));
          } catch (Exception e) {
            try {
              response.then().body("error", Matchers.containsString(expectedResponse));
            } catch (Exception e2) {
              // If neither field exists, just validate status code
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
          expectedResponse.contains("not_found") || expectedResponse.contains("XML feed") ||
          expectedResponse.contains("Account not found")) {
        // Expected failure scenario - no action needed
      } else {
        throw e;
      }
    }
  }

  @DataProvider(name = "crossAccountXMLFeedTestData")
  public static Object[][] crossAccountXMLFeedTestData() {
    return new Object[][] {
        // ===== SCENARIO 1: VALID CROSS-ACCOUNT OPERATIONS =====
        // Account A creates XML feed (should succeed)
        { "SCENARIO_1_SAVE_XML_FEED", "AccountA", "valid", "POST_SAVE_XML_FEED", "200", "success",
            "Account A should be able to create XML feed" },

        // Account B performs all operations with valid token (cross-account access
        // patterns)
        { "SCENARIO_1_UPDATE_XML_FEED", "AccountB", "valid", "POST_UPDATE_XML_FEED", "404", "not_found",
            "Account B should not update Account A's XML feed" },
        { "SCENARIO_1_LIST_XML_FEEDS", "AccountB", "valid", "POST_LIST_XML_FEEDS", "200", "success",
            "Account B should get XML feeds list with valid token" },
        { "SCENARIO_1_MARK_DEFAULT", "AccountB", "valid", "POST_MARK_DEFAULT", "401", "not_found",
            "Account B should not mark Account A's XML feed as default" },
        { "SCENARIO_1_GENERATE_PREVIEW", "AccountB", "valid", "POST_GENERATE_PREVIEW", "404", "not_found",
            "Account B should not generate preview for Account A's data" },
        { "SCENARIO_1_GET_XML_FEEDS_FOR_JOB", "AccountB", "valid", "POST_GET_XML_FEEDS_FOR_JOB", "200", "success",
            "Account B should get XML feeds for job with valid token" },
        { "SCENARIO_1_DELETE_XML_FEED", "AccountB", "valid", "DELETE_XML_FEED", "404", "not_found",
            "Account B should not delete Account A's XML feed" },

        // Account A verifies data integrity (should still work)
        { "SCENARIO_1_VERIFY", "AccountA", "valid", "POST_UPDATE_XML_FEED", "200", "success",
            "Account A should still access data after Account B operations" },

        // ===== SCENARIO 2: INVALID TOKEN OPERATIONS =====
        // Account B performs same operations with invalid token (should fail)
        { "SCENARIO_2_SAVE_XML_FEED", "AccountB", "invalid", "POST_SAVE_XML_FEED", "401", "unauthorized",
            "Account B should be denied save with invalid token" },
        { "SCENARIO_2_UPDATE_XML_FEED", "AccountB", "invalid", "POST_UPDATE_XML_FEED", "401", "unauthorized",
            "Account B should be denied update with invalid token" },
        { "SCENARIO_2_LIST_XML_FEEDS", "AccountB", "invalid", "POST_LIST_XML_FEEDS", "401", "unauthorized",
            "Account B should be denied list access with invalid token" },
        { "SCENARIO_2_MARK_DEFAULT", "AccountB", "invalid", "POST_MARK_DEFAULT", "401", "unauthorized",
            "Account B should be denied mark default with invalid token" },
        { "SCENARIO_2_GENERATE_PREVIEW", "AccountB", "invalid", "POST_GENERATE_PREVIEW", "401", "unauthorized",
            "Account B should be denied preview generation with invalid token" },
        { "SCENARIO_2_GET_XML_FEEDS_FOR_JOB", "AccountB", "invalid", "POST_GET_XML_FEEDS_FOR_JOB", "401",
            "unauthorized",
            "Account B should be denied get XML feeds for job with invalid token" },
        { "SCENARIO_2_DELETE_XML_FEED", "AccountB", "invalid", "DELETE_XML_FEED", "401", "unauthorized",
            "Account B should be denied delete with invalid token" },

        // Account A still has access (should work)
        { "SCENARIO_2_VERIFY", "AccountA", "valid", "POST_LIST_XML_FEEDS", "200", "success",
            "Account A should maintain access after Account B invalid attempts" },

        // ===== SCENARIO 3: EDGE CASES =====
        // Account C (non-existent) attempts operations
        { "SCENARIO_3_NONEXISTENT_ACCOUNT", "AccountC", "valid", "POST_SAVE_XML_FEED", "401", "Unauthorized",
            "Non-existent account should return 401" },

        // Account B with expired token
        { "SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "POST_SAVE_XML_FEED", "401", "token_expired",
            "Expired token should return 401" },

        // Account B with malformed token
        { "SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "POST_SAVE_XML_FEED", "401", "Unauthorized",
            "Malformed token should return 401" },

        // ===== SCENARIO 4: BOUNDARY TESTING =====
        // Account B with empty token
        { "SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "POST_SAVE_XML_FEED", "401", "unauthorized",
            "Empty token should return 401" },

        // Account B with null token
        { "SCENARIO_4_NULL_TOKEN", "AccountB", "null", "POST_SAVE_XML_FEED", "401", "unauthorized",
            "Null token should return 401" },

        // ===== SCENARIO 5: DATA INTEGRITY VERIFICATION =====
        // Verify data consistency across accounts
        { "SCENARIO_5_DATA_INTEGRITY_A", "AccountA", "valid", "POST_LIST_XML_FEEDS", "200", "success",
            "Account A should see consistent data" },
        { "SCENARIO_5_DATA_INTEGRITY_B", "AccountB", "valid", "POST_LIST_XML_FEEDS", "200", "success",
            "Account B should see own data only" }
    };
  }

  public String getAccountSlug(String accountType) {
    if (accountType.equals("AccountA")) {
      return "accounta";
    } else if (accountType.equals("AccountB")) {
      return "accountb";
    } else if (accountType.equals("AccountC")) {
      return "accountc";
    }
    return accountType;
  }

  private SaveCustomXmlFeed createXMLFeedData() {
    SaveCustomXmlFeed xmlFeed = new SaveCustomXmlFeed();

    // Generate test data
    String title = javaFakerXmlFeed.getXmlFeedTitle();
    String xmlHeader = javaFakerXmlFeed.getXmlHeader();
    String xmlBody = javaFakerXmlFeed.getXmlBody();
    int decodeValue = javaFakerXmlFeed.getDecodeValue();
    int preselectValue = javaFakerXmlFeed.getPreselectValue();
    int jobLastUpdatedLimit = javaFakerXmlFeed.getJobLastUpdatedOnLimit();

    // Set XML feed data
    xmlFeed.setTitle(title);
    xmlFeed.setParent_xml(xmlHeader);
    xmlFeed.setDynamic_job_xml(xmlBody);
    xmlFeed.setDecode_xml_data(decodeValue);
    xmlFeed.setIs_preselect_xml(preselectValue);
    xmlFeed.setJob_last_updatedon_limit(jobLastUpdatedLimit);

    return xmlFeed;
  }
}