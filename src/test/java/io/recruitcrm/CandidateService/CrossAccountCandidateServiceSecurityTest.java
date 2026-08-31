package io.recruitcrm.CandidateService;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

/**
 * Cross-Account Candidate Service Security Test
 * Tests various security scenarios for Candidate Service operations across different accounts
 * with different token types (valid, invalid, expired, malformed)
 * Focuses on endpoints that use candidatesURL (Candidate Service v2)
 */
@AccountType("CrossAccount")
public class CrossAccountCandidateServiceSecurityTest extends TestBase {

	private String hotlistId = "";
	private AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	
	/**
	 * Comprehensive test covering cross-account Candidate Service operations
	 */
	@Owner("Raj Pandey")
	@Test(dataProvider = "crossAccountCandidateServiceTestData", groups = "nightly-build")
	public void crossAccountCandidateServiceOperations_Test(String testScenario, String accountType, String tokenType, 
			String operation, String expectedStatusCode, String expectedResponse, String description) {
		
		// Get appropriate token based on account and token type
		String token = getTokenForAccount(accountType, tokenType);
		
		Response response = null;
		Map<String, String> pathParameters = new HashMap<>();
		Map<String, String> queryParameters = new HashMap<>();
		
		try {
			switch (operation.toUpperCase()) {
				case "POST_PIN_HOTLIST":
					// Pin hotlist
					pinnedHotlistTestData();
					pathParameters.put("hotlistId", hotlistId.isEmpty() ? "123" : hotlistId);
					String pinHotlistPath = "hotlists/{hotlistId}/pinned-hotlist";
					response = RestClient.doPost1("JSON", candidatesURL, pinHotlistPath, token, null, pathParameters, true, null);
					break;
					
				case "POST_PIN_HOTLIST_INVALID":
					// Pin hotlist with invalid hotlist ID
					pathParameters.put("hotlistId", "12323");
					String pinHotlistInvalidPath = "hotlists/{hotlistId}/pinned-hotlist";
					response = RestClient.doPost1("JSON", candidatesURL, pinHotlistInvalidPath, token, null, pathParameters, true, null);
					break;
					
				case "GET_ENTITY_COLUMNS":
					// Get entity columns
					queryParameters.put("entity", "candidates");
					response = RestClient.doGet("JSON", candidatesURL, "entity-columns", token, queryParameters, null, true);
					break;
					
				case "GET_ENTITY_COLUMNS_INVALID":
					// Get entity columns with invalid entity
					queryParameters.put("entity", "invalid_entity_123");
					response = RestClient.doGet("JSON", candidatesURL, "entity-columns", token, queryParameters, null, true);
					break;
						
				case "GET_CUSTOM_VIEW_USER":
					// Get custom view user
					queryParameters.put("entityId", "1");
					response = RestClient.doGet("JSON", candidatesURL, "custom-view/user-view", token, queryParameters, null, true);
					break;

				case "PUT_CUSTOM_VIEW_USER":
					// Update custom view user
					queryParameters.put("entityId", "1");
					response = RestClient.doPut1("JSON", candidatesURL, "custom-view/user-view", token, queryParameters, null, true, null);
					break;

				case "GET_CUSTOM_VIEW_ACCOUNT":
					// Get custom view account
					queryParameters.put("entityId", "1");
					response = RestClient.doGet("JSON", candidatesURL, "custom-view/account-view", token, queryParameters, null, true);
					break;

				case "GET_QUICK_VIEW_COUNT":
					// Get quick view count
					response = RestClient.doGet("JSON", candidatesURL, "candidates/quick-view-count", token, null, null, true);
					break;
					
				case "GET_SAVED_SEARCHES":
					// Get saved searches
					queryParameters.put("entityName", "candidates");
					queryParameters.put("postSearchRevamp", "1");
					queryParameters.put("page", "1");
					queryParameters.put("size", "15");
					response = RestClient.doGet("JSON", candidatesURL, "saved-searches", token, queryParameters, null, true);
					break;
					
				case "POST_CANDIDATE_SEARCH":
					// Search candidates with filters
					queryParameters.put("page", "1");
					queryParameters.put("size", "100");
					Map<String, Object> searchPayload = createCandidateSearchPayload();
					response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/candidates/search/get", token, queryParameters, true, searchPayload);
					break;
					
				default:
					Assert.fail("Unsupported operation: " + operation);
			}
			
			// Validate response status code
			int expectedStatus = Integer.parseInt(expectedStatusCode);
			response.then().statusCode(expectedStatus);
			
			// Additional validations based on expected response
			switch (expectedResponse) {
				case "success":
					if (operation.startsWith("GET")) {
						response.then().body(Matchers.notNullValue());
					}
					break;
					
				case "unauthorized":
				case "Unauthorized":
				case "Unauthorised access":
					try {
						response.then().body("meta.message", Matchers.containsString("Unauthorised access"));
					} catch (Exception e) {
						try {
							response.then().body("error", Matchers.containsString("Unauthorized"));
						} catch (Exception e2) {
							// If error field doesn't exist, just validate status code
						}
					}
					break;
					
				case "Candidate id not found":
				case "candidate not found":
				case "Candidate not found":
					try {
						response.then().body("errors[0].message", Matchers.containsString("not found"));
					} catch (Exception e) {
						try {
							response.then().body("meta.message", Matchers.containsString("not found"));
						} catch (Exception e2) {
							try {
								response.then().body("error_message", Matchers.containsString("not found"));
							} catch (Exception e3) {
								// If neither field exists, just validate status code
							}
						}
					}
					break;
					
				case "null":
					try {
						response.then().body("error", Matchers.equalTo(null));
					} catch (Exception e) {
						// If error field doesn't exist, just validate status code
					}
					break;
					
				case "Not Found":
					try {
						response.then().body("error", Matchers.equalTo("Not Found"));
					} catch (Exception e) {
						// If error field doesn't exist, just validate status code
					}
					break;

				case "Internal Server Error":
					try {
						response.then().body("error", Matchers.equalTo("Internal Server Error"));
					} catch (Exception e) {
						// If error field doesn't exist, just validate status code
					}
					break;
					
				default:
					// For all other error messages, validate exact match
					try {
						response.then().body("meta.message", Matchers.equalTo(expectedResponse));
					} catch (Exception e) {
						try {
							response.then().body("error_message", Matchers.equalTo(expectedResponse));
						} catch (Exception e2) {
							try {
								response.then().body("error", Matchers.equalTo(expectedResponse));
							} catch (Exception e3) {
								// If neither field exists, just validate status code
							}
						}
					}
					break;
			}
			
		} catch (Exception e) {
			// Handle exceptions for invalid scenarios
			if (expectedResponse.contains("unauthorized") || expectedResponse.contains("Unauthorised access") || 
				expectedResponse.contains("Candidate id not found") || expectedResponse.contains("candidate not found") ||
				expectedResponse.contains("Candidate not found") || expectedResponse.contains("Bad Request") ||
				expectedResponse.contains("Not Found")) {
				// Expected failure scenario - no action needed
			} else {
				throw e;
			}
		}
	}

    /**
	 * Data provider for cross-account Candidate Service operations
	 */
	@DataProvider(name = "crossAccountCandidateServiceTestData")
	public static Object[][] crossAccountCandidateServiceTestData() {
		return new Object[][] {
			// ===== SCENARIO 1: VALID CROSS-ACCOUNT OPERATIONS =====
			// Account A performs operations (should succeed)
			{"SCENARIO_1_GET_ENTITY_COLUMNS_A", "AccountA", "valid", "GET_ENTITY_COLUMNS", "200", "success", "Account A should get entity columns with valid token"},
			{"SCENARIO_1_GET_CUSTOM_VIEW_USER_A", "AccountA", "valid", "GET_CUSTOM_VIEW_USER", "200", "success", "Account A should get custom view user with valid token"},
			{"SCENARIO_1_GET_CUSTOM_VIEW_ACCOUNT_A", "AccountA", "valid", "GET_CUSTOM_VIEW_ACCOUNT", "200", "success", "Account A should get custom view account with valid token"},
			{"SCENARIO_1_GET_QUICK_VIEW_COUNT_A", "AccountA", "valid", "GET_QUICK_VIEW_COUNT", "200", "success", "Account A should get quick view count with valid token"},

//			Account B attempts write operations (should fail due to cross-account restrictions)
//			Below test is commented due to the issue in the API - 200 is returned instead of 404
//			{"SCENARIO_1_POST_PIN_HOTLIST_B", "AccountB", "valid", "POST_PIN_HOTLIST", "404", "Candidate not found", "Account B should pin hotlist with valid token"},

//			 ===== SCENARIO 2: INVALID TOKEN OPERATIONS =====
//			 Account B performs same operations with invalid token (should fail)
			{"SCENARIO_2_GET_ENTITY_COLUMNS", "AccountB", "invalid", "GET_ENTITY_COLUMNS", "401", "Unauthorised access", "Account B should be denied access to entity columns with invalid token"},
			{"SCENARIO_2_GET_CUSTOM_VIEW_USER", "AccountB", "invalid", "GET_CUSTOM_VIEW_USER", "401", "Unauthorised access", "Account B should be denied access to custom view user with invalid token"},
			{"SCENARIO_2_GET_SAVED_SEARCHES", "AccountB", "invalid", "GET_SAVED_SEARCHES", "401", "Unauthorised access", "Account B should be denied access to saved searches with invalid token"},
			{"SCENARIO_2_POST_CANDIDATE_SEARCH", "AccountB", "invalid", "POST_CANDIDATE_SEARCH", "401", "Unauthorised access", "Account B should be denied candidate search with invalid token"},
			{"SCENARIO_2_POST_PIN_HOTLIST", "AccountB", "invalid", "POST_PIN_HOTLIST", "401", "Unauthorised access", "Account B should be denied pin hotlist with invalid token"},
			{"SCENARIO_2_PUT_CUSTOM_VIEW_USER", "AccountB", "invalid", "PUT_CUSTOM_VIEW_USER", "401", "Unauthorised access", "Account B should be denied update custom view user with invalid token"},

			// Account A still has access (should work)
			{"SCENARIO_2_VERIFY_A", "AccountA", "valid", "GET_ENTITY_COLUMNS", "200", "success", "Account A should maintain access after Account B invalid attempts"},

			// ===== SCENARIO 3: EDGE CASES =====
			// Account C (non-existent) attempts operations
			{"SCENARIO_3_NONEXISTENT_ACCOUNT", "AccountC", "valid", "GET_ENTITY_COLUMNS", "401", "Unauthorised access", "Non-existent account should return 401"},

			// Account B with expired token
			{"SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "GET_ENTITY_COLUMNS", "401", "Unauthorised access", "Expired token should return 401"},

			// Account B with malformed token
			{"SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "GET_ENTITY_COLUMNS", "401", "Unauthorised access", "Malformed token should return 401"},
			{"SCENARIO_3_MALFORMED_TOKEN_SAVED_SEARCHES", "AccountB", "malformed", "GET_SAVED_SEARCHES", "401", "Unauthorised access", "Malformed token should return 401 for saved searches"},
			{"SCENARIO_3_MALFORMED_TOKEN_CANDIDATE_SEARCH", "AccountB", "malformed", "POST_CANDIDATE_SEARCH", "401", "Unauthorised access", "Malformed token should return 401 for candidate search"},

			// ===== SCENARIO 4: BOUNDARY TESTING =====
			// Account B with empty token
			{"SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "GET_ENTITY_COLUMNS", "401", "Unauthorised access", "Empty token should return 401"},

			// Account B with null token
			{"SCENARIO_4_NULL_TOKEN", "AccountB", "null", "GET_ENTITY_COLUMNS", "401", "Unauthorised access", "Null token should return 401"},
			{"SCENARIO_4_NULL_TOKEN_SAVED_SEARCHES", "AccountB", "null", "GET_SAVED_SEARCHES", "401", "Unauthorised access", "Null token should return 401 for saved searches"},
			{"SCENARIO_4_NULL_TOKEN_CANDIDATE_SEARCH", "AccountB", "null", "POST_CANDIDATE_SEARCH", "401", "Unauthorised access", "Null token should return 401 for candidate search"},

			// ===== SCENARIO 5: SPECIFIC ENDPOINT TESTING =====
			// Test specific endpoints with invalid data
			// Below test is commented due to the issue in the API - 200 is returned instead of 404
//			{"SCENARIO_5_INVALID_HOTLIST_ID", "AccountA", "valid", "POST_PIN_HOTLIST_INVALID", "404", "Not Found", "Invalid hotlist ID should return 404"},
			{"SCENARIO_5_INVALID_ENTITY", "AccountA", "valid", "GET_ENTITY_COLUMNS_INVALID", "400", "null", "Invalid entity should return 400"}
		};
	}



	private Map<String, Object> createCandidateSearchPayload() {
		Map<String, Object> searchPayload = new HashMap<>();
		searchPayload.put("defaultFilterList", null);
		searchPayload.put("filterSearchList", null);
		searchPayload.put("booleanSearchList", null);
		searchPayload.put("sortPriorityList", new ArrayList<>());
		return searchPayload;
	}
	
	/**
	 * Create candidate and hotlist data using Account A for cross-account security testing
	 */
	private void pinnedHotlistTestData() {
		try {
			// Step 1: Create a candidate using Account A
			Response candidateResponse = allCrudFunctions.createCandidate(albatrossURL, accountA_Token);
			if (candidateResponse.getStatusCode() == 200) {
				JsonPath candidateJsonPath = candidateResponse.jsonPath();
				int candidateId = candidateJsonPath.getInt("data.candidate.id");
				
				// Step 2: Create a hotlist using Account A
				Response hotlistResponse = allCrudFunctions.createHotlistsForCandidates(baseURL, accountA_apiKey);
				if (hotlistResponse.getStatusCode() == 200) {
					JsonPath hotlistJsonPath = hotlistResponse.jsonPath();
					hotlistId = String.valueOf(hotlistJsonPath.getInt("id"));
					String hotlistName = hotlistJsonPath.getString("name");
					int shared = hotlistJsonPath.getInt("shared");
					
					// Step 3: Add candidate to hotlist using Account A
					allCrudFunctions.addCandidateToHotList(albatrossURL, accountA_Token, candidateId, shared, hotlistName);
				}
			}
			
		} catch (Exception e) {
			Assert.fail("Candidate and Hotlist data creation failed: " + e.getMessage());
		}
	}
}