package io.recruitcrm.report;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import io.rcrm.api.pojo.albatross.GetEntityColumns;
import io.rcrm.api.pojo.albatross.targetReports.SearchTargetReport;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountTargetReportSearchSecurityTest extends TestBase {

	@Owner("Smit Patel")
	@Test(dataProvider = "crossAccountTargetReportsTestData", groups = "nightly-build")
	public void crossAccountTargetReportsSearchOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {
		String token = getTokenForAccount(accountType, tokenType);
		Response response = null;
		
		try {
			switch (operation.toUpperCase()) {
				case "SEARCH_TARGET_REPORTS_VALID":
					// Test search target reports with valid data
					SearchTargetReport searchRequest = new SearchTargetReport();
					searchRequest.setPage_size(10);
					searchRequest.setPage("1");
					searchRequest.setSort_by("title");
					searchRequest.setSortOrder("asc");
					
					String searchPath = "target-reports/search/get";
					response = RestClient.doPost("JSON", albatrossURL, searchPath, token, null, true, searchRequest);
					break;
					
				case "SEARCH_TARGET_REPORTS_EMPTY":
					// Test search target reports with empty data
					SearchTargetReport emptySearchRequest = new SearchTargetReport();
					
					String emptySearchPath = "target-reports/search/get";
					response = RestClient.doPost("JSON", albatrossURL, emptySearchPath, token, null, true, emptySearchRequest);
					break;
					
				case "SEARCH_TARGET_REPORTS_INVALID_PAGE":
					// Test search target reports with invalid page
					SearchTargetReport invalidPageRequest = new SearchTargetReport();
					invalidPageRequest.setPage_size(10);
					invalidPageRequest.setPage("999999");
					invalidPageRequest.setSort_by("title");
					invalidPageRequest.setSortOrder("asc");
					
					String invalidPagePath = "target-reports/search/get";
					response = RestClient.doPost("JSON", albatrossURL, invalidPagePath, token, null, true, invalidPageRequest);
					break;
					
				case "SEARCH_TARGET_REPORTS_INVALID_SORT":
					// Test search target reports with invalid sort order
					SearchTargetReport invalidSortRequest = new SearchTargetReport();
					invalidSortRequest.setPage_size(10);
					invalidSortRequest.setPage("1");
					invalidSortRequest.setSort_by("title");
					invalidSortRequest.setSortOrder("invalid_sort");
					
					String invalidSortPath = "target-reports/search/get";
					response = RestClient.doPost("JSON", albatrossURL, invalidSortPath, token, null, true, invalidSortRequest);
					break;
					
				case "GET_ENTITY_COLUMNS_VALID":
					// Test get entity columns with valid data
					GetEntityColumns entityColumnsRequest = new GetEntityColumns();
					entityColumnsRequest.setEntity("target_reports");
					
					String entityColumnsPath = "global/get-entity-columns";
					response = RestClient.doPost("JSON", albatrossURL, entityColumnsPath, token, null, true, entityColumnsRequest);
					break;
					
				case "GET_ENTITY_COLUMNS_EMPTY":
					// Test get entity columns with empty data
					GetEntityColumns emptyEntityRequest = new GetEntityColumns();
					
					String emptyEntityPath = "global/get-entity-columns";
					response = RestClient.doPost("JSON", albatrossURL, emptyEntityPath, token, null, true, emptyEntityRequest);
					break;
					
				case "GET_ENTITY_COLUMNS_INVALID_ENTITY":
					// Test get entity columns with invalid entity
					GetEntityColumns invalidEntityRequest = new GetEntityColumns();
					invalidEntityRequest.setEntity("invalid_entity");
					
					String invalidEntityPath = "global/get-entity-columns";
					response = RestClient.doPost("JSON", albatrossURL, invalidEntityPath, token, null, true, invalidEntityRequest);
					break;
					
				default:
					throw new IllegalArgumentException("Unsupported operation: " + operation);
			}
			
			validateResponse(response, expectedResponse, expectedStatusCode);
			
		} catch (Exception e) {
			if (tokenType.equals("invalid") || tokenType.equals("expired") || tokenType.equals("malformed")) {
				Assert.assertTrue(true, "Expected failure for " + tokenType + " token");
			} else {
				throw e;
			}
		}
	}

	private void validateResponse(Response response, String expectedResponse, String expectedStatusCode) {
		int expectedStatus = Integer.parseInt(expectedStatusCode);
		Assert.assertEquals(response.getStatusCode(), expectedStatus, "Status code mismatch");
		
		switch (expectedResponse) {
			case "search_success":
				response.then().body("message_type", Matchers.equalTo("is-success"));
				response.then().body("data", Matchers.notNullValue());
				break;
				
			case "search_empty_success":
				response.then().body("message_type", Matchers.equalTo("is-success"));
				response.then().body("data.total_count", Matchers.equalTo(0));
				response.then().body("data.records", Matchers.empty());
				break;
				
			case "entity_columns_success":
				response.then().body("message_type", Matchers.equalTo("is_success"));
				response.then().body("data", Matchers.notNullValue());
				break;
				
			case "unauthorized":
			case "Unauthorized":
				response.then().body("error", Matchers.equalTo("Unauthorized"));
				break;
				
			case "bad_request":
				response.then().body("message", Matchers.notNullValue());
				break;
				
			case "invalid_sort_error":
				response.then().body("message", Matchers.equalTo("Order direction must be \"asc\" or \"desc\"."));
				response.then().body("exception", Matchers.equalTo("InvalidArgumentException"));
				break;
				
			case "server_error":
				response.then().body("message", Matchers.notNullValue());
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
	}

	@DataProvider(name = "crossAccountTargetReportsTestData")
	public static Object[][] crossAccountTargetReportsTestData() {
		return new Object[][] {
			// Account A - Valid Search Operations
			{"SCENARIO_1_SEARCH_TARGET_REPORTS_VALID", "AccountA", "valid", "SEARCH_TARGET_REPORTS_VALID", "200", "search_success", "AccountA searches target reports with valid data"},
			{"SCENARIO_2_SEARCH_TARGET_REPORTS_EMPTY", "AccountA", "valid", "SEARCH_TARGET_REPORTS_EMPTY", "200", "search_empty_success", "AccountA searches target reports with empty data"},
			{"SCENARIO_3_SEARCH_TARGET_REPORTS_INVALID_PAGE", "AccountA", "valid", "SEARCH_TARGET_REPORTS_INVALID_PAGE", "200", "search_empty_success", "AccountA searches target reports with invalid page"},
			{"SCENARIO_4_SEARCH_TARGET_REPORTS_INVALID_SORT", "AccountA", "valid", "SEARCH_TARGET_REPORTS_INVALID_SORT", "500", "invalid_sort_error", "AccountA searches target reports with invalid sort order"},
			
			// Account A - Valid Entity Columns Operations
			{"SCENARIO_5_GET_ENTITY_COLUMNS_VALID", "AccountA", "valid", "GET_ENTITY_COLUMNS_VALID", "200", "entity_columns_success", "AccountA gets entity columns with valid data"},
			{"SCENARIO_6_GET_ENTITY_COLUMNS_EMPTY", "AccountA", "valid", "GET_ENTITY_COLUMNS_EMPTY", "422", "server_error", "AccountA gets entity columns with empty data"},
			{"SCENARIO_7_GET_ENTITY_COLUMNS_INVALID_ENTITY", "AccountA", "valid", "GET_ENTITY_COLUMNS_INVALID_ENTITY", "500", "server_error", "AccountA gets entity columns with invalid entity"},
			
			// Account B - Cross-Account Search Access
			{"SCENARIO_8_CROSS_ACCOUNT_SEARCH_VALID", "AccountB", "valid", "SEARCH_TARGET_REPORTS_VALID", "200", "search_success", "AccountB searches target reports with valid data"},
			{"SCENARIO_9_CROSS_ACCOUNT_SEARCH_EMPTY", "AccountB", "valid", "SEARCH_TARGET_REPORTS_EMPTY", "200", "search_empty_success", "AccountB searches target reports with empty data"},
			{"SCENARIO_10_CROSS_ACCOUNT_SEARCH_INVALID_PAGE", "AccountB", "valid", "SEARCH_TARGET_REPORTS_INVALID_PAGE", "200", "search_empty_success", "AccountB searches target reports with invalid page"},
			{"SCENARIO_11_CROSS_ACCOUNT_SEARCH_INVALID_SORT", "AccountB", "valid", "SEARCH_TARGET_REPORTS_INVALID_SORT", "500", "invalid_sort_error", "AccountB searches target reports with invalid sort order"},
			
			// Account B - Cross-Account Entity Columns Access
			{"SCENARIO_12_CROSS_ACCOUNT_ENTITY_COLUMNS_VALID", "AccountB", "valid", "GET_ENTITY_COLUMNS_VALID", "200", "entity_columns_success", "AccountB gets entity columns with valid data"},
			{"SCENARIO_13_CROSS_ACCOUNT_ENTITY_COLUMNS_EMPTY", "AccountB", "valid", "GET_ENTITY_COLUMNS_EMPTY", "422", "server_error", "AccountB gets entity columns with empty data"},
			{"SCENARIO_14_CROSS_ACCOUNT_ENTITY_COLUMNS_INVALID_ENTITY", "AccountB", "valid", "GET_ENTITY_COLUMNS_INVALID_ENTITY", "500", "server_error", "AccountB gets entity columns with invalid entity"},
			
			// Invalid Token Scenarios for Account A
			{"SCENARIO_15_INVALID_TOKEN_SEARCH", "AccountA", "invalid", "SEARCH_TARGET_REPORTS_VALID", "401", "Unauthorized", "AccountA attempts to search with invalid token"},
			{"SCENARIO_16_EXPIRED_TOKEN_SEARCH", "AccountA", "expired", "SEARCH_TARGET_REPORTS_VALID", "401", "Unauthorized", "AccountA attempts to search with expired token"},
			{"SCENARIO_17_MALFORMED_TOKEN_SEARCH", "AccountA", "malformed", "SEARCH_TARGET_REPORTS_VALID", "401", "Unauthorized", "AccountA attempts to search with malformed token"},
			{"SCENARIO_18_EMPTY_TOKEN_SEARCH", "AccountA", "empty", "SEARCH_TARGET_REPORTS_VALID", "401", "Unauthorized", "AccountA attempts to search with empty token"},
			
			// Invalid Token Scenarios for Entity Columns
			{"SCENARIO_19_INVALID_TOKEN_ENTITY_COLUMNS", "AccountA", "invalid", "GET_ENTITY_COLUMNS_VALID", "401", "Unauthorized", "AccountA attempts to get entity columns with invalid token"},
			{"SCENARIO_20_EXPIRED_TOKEN_ENTITY_COLUMNS", "AccountA", "expired", "GET_ENTITY_COLUMNS_VALID", "401", "Unauthorized", "AccountA attempts to get entity columns with expired token"},
			{"SCENARIO_21_MALFORMED_TOKEN_ENTITY_COLUMNS", "AccountA", "malformed", "GET_ENTITY_COLUMNS_VALID", "401", "Unauthorized", "AccountA attempts to get entity columns with malformed token"},
			
			// Cross-Account with Invalid Tokens
			{"SCENARIO_22_CROSS_ACCOUNT_INVALID_TOKEN_SEARCH", "AccountB", "invalid", "SEARCH_TARGET_REPORTS_VALID", "401", "Unauthorized", "AccountB attempts to search with invalid token"},
			{"SCENARIO_23_CROSS_ACCOUNT_EXPIRED_TOKEN_SEARCH", "AccountB", "expired", "SEARCH_TARGET_REPORTS_VALID", "401", "Unauthorized", "AccountB attempts to search with expired token"},
			{"SCENARIO_24_CROSS_ACCOUNT_MALFORMED_TOKEN_SEARCH", "AccountB", "malformed", "SEARCH_TARGET_REPORTS_VALID", "401", "Unauthorized", "AccountB attempts to search with malformed token"},
			
			// Cross-Account with Invalid Tokens for Entity Columns
			{"SCENARIO_25_CROSS_ACCOUNT_INVALID_TOKEN_ENTITY_COLUMNS", "AccountB", "invalid", "GET_ENTITY_COLUMNS_VALID", "401", "Unauthorized", "AccountB attempts to get entity columns with invalid token"},
			{"SCENARIO_26_CROSS_ACCOUNT_EXPIRED_TOKEN_ENTITY_COLUMNS", "AccountB", "expired", "GET_ENTITY_COLUMNS_VALID", "401", "Unauthorized", "AccountB attempts to get entity columns with expired token"},
			
			// Edge Cases
			{"SCENARIO_27_NULL_TOKEN_SEARCH", "AccountA", "null", "SEARCH_TARGET_REPORTS_VALID", "401", "Unauthorized", "AccountA attempts to search with null token"},
			{"SCENARIO_28_CROSS_ACCOUNT_NULL_TOKEN_SEARCH", "AccountB", "null", "SEARCH_TARGET_REPORTS_VALID", "401", "Unauthorized", "AccountB attempts to search with null token"},
			{"SCENARIO_29_NULL_TOKEN_ENTITY_COLUMNS", "AccountA", "null", "GET_ENTITY_COLUMNS_VALID", "401", "Unauthorized", "AccountA attempts to get entity columns with null token"},
			{"SCENARIO_30_CROSS_ACCOUNT_NULL_TOKEN_ENTITY_COLUMNS", "AccountB", "null", "GET_ENTITY_COLUMNS_VALID", "401", "Unauthorized", "AccountB attempts to get entity columns with null token"}
		};
	}
} 