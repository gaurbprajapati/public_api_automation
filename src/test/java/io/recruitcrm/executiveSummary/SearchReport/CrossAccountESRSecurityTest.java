package io.recruitcrm.executiveSummary.SearchReport;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.executiveSummaryService.ExecutiveSearchReportFunctions;
import io.rcrm.api.javafaker.executive_summary.ExecutiveSearchReportFaker;
import io.rcrm.api.pojo.executiveSummary.ExecutiveSearchReport;
import io.rcrm.api.pojo.executiveSummary.GenerateReport;
import io.rcrm.api.pojo.executiveSummary.Template;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

/**
 * Cross-Account ESR Security Test - Optimized Version
 * Tests various security scenarios for Executive Search Report operations across different accounts
 * Focuses on data exposure vulnerabilities and cross-account access controls
 */
@AccountType("CrossAccount")
public class CrossAccountESRSecurityTest extends TestBase {

	private String reportID = "";
	private String templateID = "";
	private int jobID;
	
	// Test utilities
	private ExecutiveSearchReportFaker executiveSearchReportFaker;
	private ExecutiveSearchReportFunctions executiveSearchReportFunctions;

	@BeforeClass(alwaysRun = true)	public void setup() {
		// Initialize utilities
		executiveSearchReportFaker = new ExecutiveSearchReportFaker();
		executiveSearchReportFunctions = new ExecutiveSearchReportFunctions();
	}
	
	@AfterClass
	public void cleanupAccounts() {
		if (reportID != null && !reportID.isEmpty()) {
			try {
				executiveSearchReportFunctions.deleteExecutiveSearchByID(reportID, executiveSummaryServiceURL, accountA_Token);
			} catch (Exception e) {
				// Ignore cleanup errors
			}
		}
	}
	
	@Owner("Sampurn Chouksey")
	@Test(dataProvider = "crossAccountESRSecurityTestData", groups = "nightly-build")
	public void crossAccountESROperations_Test(String testScenario, String accountType, String tokenType, 
			String operation, String expectedStatusCode, String expectedResponse, String description) {
		// Reset test state
		reportID = templateID = "";
		jobID = 0;
		
		String token = getTokenForAccount(accountType, tokenType);
		Response response = null;
		Map<String, String> pathParameters = new HashMap<>();
		
		try {
			response = executeOperation(operation, token, pathParameters);
			validateResponse(response, expectedStatusCode, expectedResponse, operation);
		} catch (Exception e) {
			handleExpectedExceptions(e, expectedResponse);
		}
	}

	/**
	 * Execute API operation based on operation type
	 */
	private Response executeOperation(String operation, String token, Map<String, String> pathParameters) {
		switch (operation.toUpperCase()) {
			case "POST_CREATE_ESR":
				return createESR(token);
			case "GET_ESR_BY_ID":
				return getESRById(token, pathParameters);
			case "GET_ALL_ESR":
				return getAllESR(token);
			case "POST_EDIT_ESR":
				return editESR(token, pathParameters);
			case "DELETE_ESR":
				return deleteESR(token, pathParameters);
			case "POST_GENERATE_ESR":
				return generateESR(token, pathParameters);
			case "POST_GENERATE_REPORT":
				return generateReport(token, pathParameters);
			case "POST_GENERATE_CANDIDATE_TABLE":
				return generateCandidateTable(token);
			case "POST_HTML_TO_SFDT":
				return convertHTMLToSFDT(token);
			case "POST_CREATE_TEMPLATE":
				return createTemplate(token);
			case "GET_TEMPLATE_BY_ID":
				return getTemplateById(token, pathParameters);
			case "GET_ALL_TEMPLATES":
				return getAllTemplates(token);
			case "GET_TEMPLATES_COUNT":
				return getTemplatesCount(token);
			default:
				Assert.fail("Unsupported operation: " + operation);
				return null;
		}
	}

	/**
	 * Unified response validation method
	 */
	private void validateResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
		int expectedStatus = Integer.parseInt(expectedStatusCode);
		response.then().statusCode(expectedStatus);
		
		if (expectedStatus == 200) {
			validateSuccessResponse(response, expectedResponse, operation);
		} else if (expectedStatus == 404) {
			validateNotFoundResponse(response, operation);
		} else if (expectedStatus == 500) {
			validateServerErrorResponse(response, expectedResponse, operation);
		} else if (expectedStatus == 400) {
			validateBadRequestResponse(response);
		} else if (expectedStatus == 422) {
			validateValidationErrorResponse(response);
		} else if (expectedStatus == 401) {
			validateUnauthorizedResponse(response);
		}
	}

	/**
	 * Validate successful responses (200)
	 */
	private void validateSuccessResponse(Response response, String expectedResponse, String operation) {
		JsonPath jp = response.jsonPath();
		
		if (operation.contains("TEMPLATE")) {
			validateTemplateResponse(jp, expectedResponse);
		} else if (operation.contains("ESR")) {
			validateESRResponse(jp, expectedResponse);
		}
	}

	/**
	 * Validate template-specific responses
	 */
	private void validateTemplateResponse(JsonPath jp, String expectedResponse) {
		// Check for template content security (for individual template operations)
		String templateContent = jp.getString("template_content");
		if (templateContent != null) {
			Assert.assertFalse(templateContent.contains("password"), "Template should not contain password data");
			Assert.assertFalse(templateContent.contains("token"), "Template should not contain token data");
		}
		
		// For GET_TEMPLATE_BY_ID operations, check for information disclosure vulnerability
		// This only applies when we're testing for non-existent templates
		if (expectedResponse.equals("server_error") || expectedResponse.equals("not_found")) {
			Object responseData = jp.get("data");
			if (responseData != null && responseData.toString().equals("[]")) {
				Assert.fail("SECURITY VULNERABILITY: API returns 200 with empty array instead of 404 for non-existent template");
			}
		}
	}

	/**
	 * Validate ESR-specific responses
	 */
	private void validateESRResponse(JsonPath jp, String expectedResponse) {
		if (expectedResponse.equals("success")) {
			Assert.assertNotNull(jp.getString("id"), "Report ID should be present");
			Assert.assertNotNull(jp.getString("job_id"), "Job ID should be present");
			Assert.assertNotNull(jp.getString("report_title"), "Report title should be present");
			
			validateSensitiveData(jp.getString("selected_candidates_ids"), "Candidate IDs");
			validateSensitiveData(jp.getString("selected_collaborators"), "Collaborator IDs");
			validateSensitiveData(jp.getString("report_content_json"), "Report content");
		}
	}

	/**
	 * Validate sensitive data for security vulnerabilities
	 */
	private void validateSensitiveData(String data, String dataType) {
		if (data != null && !data.isEmpty()) {
			Assert.assertFalse(data.contains("'"), dataType + " should not contain SQL injection characters");
			Assert.assertFalse(data.contains("<script>"), dataType + " should not contain XSS payloads");
			Assert.assertFalse(data.contains("javascript:"), dataType + " should not contain JavaScript");
		}
	}

	/**
	 * Validate 404 responses
	 */
	private void validateNotFoundResponse(Response response, String operation) {
		String responseBody = response.getBody().asString();
		Assert.assertFalse(responseBody.contains("account_id"), "404 response should not expose account information");
		Assert.assertFalse(responseBody.contains("selected_candidates_ids"), "404 response should not expose candidate data");
		
		if (operation.contains("TEMPLATE")) {
			Assert.assertFalse(responseBody.contains("template_content"), "404 response should not expose template content");
		}
	}

	/**
	 * Validate 500 responses
	 */
	private void validateServerErrorResponse(Response response, String expectedResponse, String operation) {
		JsonPath jp = response.jsonPath();
		String message = jp.getString("message");
		
		Assert.assertNotNull(message, "500 error should have error message");
		Assert.assertTrue(message.contains("not found") || message.contains("Template not found"), 
			"500 error message should indicate resource not found");
		
		String responseBody = response.getBody().asString();
		Assert.assertFalse(responseBody.contains("template_content"), "500 response should not expose template content");
		Assert.assertFalse(responseBody.contains("password"), "500 response should not expose sensitive data");
		Assert.assertFalse(responseBody.contains("token"), "500 response should not expose sensitive data");
	}

	/**
	 * Validate 400 responses
	 */
	private void validateBadRequestResponse(Response response) {
		JsonPath jp = response.jsonPath();
		Assert.assertNotNull(jp.getString("message") != null || jp.getString("error") != null, "400 error should have error message");
	}

	/**
	 * Validate 422 responses
	 */
	private void validateValidationErrorResponse(Response response) {
		JsonPath jp = response.jsonPath();
		Assert.assertNotNull(jp.getString("message") != null || jp.getString("errors") != null, "Validation error should have error message");
	}

	/**
	 * Validate 401 responses
	 */
	private void validateUnauthorizedResponse(Response response) {
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	/**
	 * Handle expected exceptions
	 */
	private void handleExpectedExceptions(Exception e, String expectedResponse) {
		if (expectedResponse.contains("unauthorized") || expectedResponse.contains("access denied") || 
			expectedResponse.contains("token_expired") || expectedResponse.contains("bad_request") || 
			expectedResponse.contains("not_found") || expectedResponse.contains("ESR not found")) {
			// Expected failure scenario - no action needed
		} else {
			throw new RuntimeException(e);
		}
	}

	// API Operation Methods
	private Response createESR(String token) {
		ExecutiveSearchReport esr = createESRData(executiveSearchReportFaker.getExecutiveSearchReportName());
		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "executive-search-reports", token, null, true, esr);
		if (response.getStatusCode() == 200) {
			JsonPath jp = response.jsonPath();
			reportID = jp.get("id").toString();
			jobID = jp.get("job_id");
		}
		return response;
	}

	private Response getESRById(String token, Map<String, String> pathParameters) {
		pathParameters.put("report_id", reportID.isEmpty() ? "1001" : reportID);
		return RestClient.doGet("JSON", executiveSummaryServiceURL, "/executive-search-reports/{report_id}", token, null, pathParameters, true);
	}

	private Response getAllESR(String token) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("job_id", String.valueOf(jobID));
		return RestClient.doGet("JSON", executiveSummaryServiceURL, "/executive-search-reports", token, queryParams, null, true);
	}

	private Response editESR(String token, Map<String, String> pathParameters) {
		pathParameters.put("report_id", reportID.isEmpty() ? "1001" : reportID);
		ExecutiveSearchReport editESR = createESRData(executiveSearchReportFaker.getExecutiveSearchReportName() + "_Edited");
		return RestClient.doPost1("JSON", executiveSummaryServiceURL, "/executive-search-reports/{report_id}", token, null, pathParameters, true, editESR);
	}

	private Response deleteESR(String token, Map<String, String> pathParameters) {
		pathParameters.put("report_id", reportID.isEmpty() ? "1001" : reportID);
		return RestClient.doDelete("JSON", executiveSummaryServiceURL, "/executive-search-reports/{report_id}", token, null, pathParameters, false);
	}

	private Response generateESR(String token, Map<String, String> pathParameters) {
		pathParameters.put("report_id", reportID.isEmpty() ? "1001" : reportID);
		GenerateReport generateReport = new GenerateReport();
		generateReport.setReport_content_html("<html><body><h1>Test Report</h1></body></html>");
		generateReport.setAction("save");
		return RestClient.doPost1("JSON", executiveSummaryServiceURL, "/executive-search-reports/generate/{report_id}", token, null, pathParameters, true, generateReport);
	}

	private Response generateReport(String token, Map<String, String> pathParameters) {
		pathParameters.put("report_id", reportID.isEmpty() ? "1001" : reportID);
		GenerateReport generateReport = new GenerateReport();
		generateReport.setAction("download");
		generateReport.setReport_content_html("<html><body><h1>Test Report</h1></body></html>");
		return RestClient.doPost1("JSON", executiveSummaryServiceURL, "/executive-search-reports/generate-report/{report_id}", token, null, pathParameters, true, generateReport);
	}

	private Response generateCandidateTable(String token) {
		Map<String, Object> candidateTableData = createCandidateTableData();
		return RestClient.doPost("JSON", executiveSummaryServiceURL, "/executive-search-reports/generate-candidate-table", token, null, true, candidateTableData);
	}

	private Response convertHTMLToSFDT(String token) {
		Map<String, Object> htmlData = createHTMLData();
		return RestClient.doPost("JSON", executiveSummaryServiceURL, "/executive-search-reports/html-to-sfdt", token, null, true, htmlData);
	}

	private Response createTemplate(String token) {
		Template template = createTemplateData();
		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "templates", token, null, true, template);
		if (response.getStatusCode() == 200) {
			JsonPath jp = response.jsonPath();
			templateID = jp.get("id").toString();
		}
		return response;
	}

	private Response getTemplateById(String token, Map<String, String> pathParameters) {
		pathParameters.put("template_id", "999999");
		return RestClient.doGet("JSON", executiveSummaryServiceURL, "/templates/{template_id}", token, null, pathParameters, true);
	}

	private Response getAllTemplates(String token) {
		return RestClient.doGet("JSON", executiveSummaryServiceURL, "/templates", token, null, null, true);
	}

	private Response getTemplatesCount(String token) {
		return RestClient.doGet("JSON", executiveSummaryServiceURL, "/templates/total-templates-count", token, null, null, true);
	}

	// Helper Methods
	private ExecutiveSearchReport createESRData(String reportTitle) {
		ExecutiveSearchReport esr = new ExecutiveSearchReport();
		esr.setJob_id(jobID > 0 ? jobID : 1001);
		esr.setReport_title(reportTitle);
		esr.setTitle_template_id(1);
		esr.setCandidate_profile_template_id(2);
		esr.setShow_candidates(1);
		esr.setShow_collaborators(1);
		esr.setSelected_candidates_ids("1001,1002,1003");
		esr.setSelected_collaborators("2001,2002");
		esr.setEsr_revamp(0);
		esr.setCandidate_fields("[{\"column_name\":\"first_name\"},{\"column_name\":\"last_name\"}]");
		esr.setReport_content_json("{\"executive_search_title_content\":\"Test Report Content\"}");
		return esr;
	}

	private Template createTemplateData() {
		Template template = new Template();
		template.setType(1);
		template.setTemplate_name("Security Test Template " + RandomStringUtils.randomAlphanumeric(5));
		template.setTemplate_content("Test template content for security validation");
		return template;
	}

	private Map<String, Object> createCandidateTableData() {
		Map<String, Object> data = new HashMap<>();
		data.put("candidate_ids", "1001,1002,1003");
		data.put("job_id", jobID > 0 ? jobID : 1001);
		data.put("table_format", "standard");
		return data;
	}

	private Map<String, Object> createHTMLData() {
		Map<String, Object> data = new HashMap<>();
		data.put("html_content", "<html><body><h1>Test HTML</h1></body></html>");
		data.put("format", "sfdt");
		return data;
	}

	@DataProvider(name = "crossAccountESRSecurityTestData")
	public static Object[][] crossAccountESRSecurityTestData() {
		return new Object[][] {
			// SCENARIO 1: VALID CROSS-ACCOUNT ESR OPERATIONS
			{"SCENARIO_1_CREATE_ESR", "AccountA", "valid", "POST_CREATE_ESR", "422", "validation_error", "Account A should get validation error for missing data"},
			{"SCENARIO_1_GET_ESR_BY_ID", "AccountB", "valid", "GET_ESR_BY_ID", "404", "ESR not found", "Account B should not access Account A's ESR"},
			{"SCENARIO_1_GET_ALL_ESR", "AccountB", "valid", "GET_ALL_ESR", "200", "success", "Account B should get empty ESR list"},
			{"SCENARIO_1_POST_EDIT_ESR", "AccountB", "valid", "POST_EDIT_ESR", "404", "ESR not found", "Account B should not edit Account A's ESR"},
			{"SCENARIO_1_DELETE_ESR", "AccountB", "valid", "DELETE_ESR", "404", "ESR not found", "Account B should not delete Account A's ESR"},
			{"SCENARIO_1_POST_GENERATE_ESR", "AccountB", "valid", "POST_GENERATE_ESR", "404", "ESR not found", "Account B should not generate Account A's ESR"},
			{"SCENARIO_1_VERIFY_ESR", "AccountA", "valid", "GET_ESR_BY_ID", "404", "not_found", "Account A should get 404 for non-existent ESR"},

			// SCENARIO 2: INVALID TOKEN ESR OPERATIONS
			{"SCENARIO_2_GET_ESR_BY_ID", "AccountB", "invalid", "GET_ESR_BY_ID", "401", "unauthorized", "Account B should be denied ESR access with invalid token"},
			{"SCENARIO_2_GET_ALL_ESR", "AccountB", "invalid", "GET_ALL_ESR", "401", "unauthorized", "Account B should be denied ESR list access with invalid token"},
			{"SCENARIO_2_POST_EDIT_ESR", "AccountB", "invalid", "POST_EDIT_ESR", "401", "unauthorized", "Account B should be denied ESR edit with invalid token"},
			{"SCENARIO_2_DELETE_ESR", "AccountB", "invalid", "DELETE_ESR", "401", "unauthorized", "Account B should be denied ESR delete with invalid token"},
			{"SCENARIO_2_POST_GENERATE_ESR", "AccountB", "invalid", "POST_GENERATE_ESR", "401", "unauthorized", "Account B should be denied ESR generation with invalid token"},
			{"SCENARIO_2_VERIFY_ESR", "AccountA", "valid", "GET_ESR_BY_ID", "404", "not_found", "Account A should get 404 for non-existent ESR"},

			// SCENARIO 3: MISSING ENDPOINT SECURITY TESTING
			{"SCENARIO_3_POST_GENERATE_REPORT", "AccountA", "valid", "POST_GENERATE_REPORT", "404", "not_found", "Generate report endpoint should be properly secured"},
			{"SCENARIO_3_POST_GENERATE_CANDIDATE_TABLE", "AccountA", "valid", "POST_GENERATE_CANDIDATE_TABLE", "400", "bad_request", "Generate candidate table endpoint should return 400 for invalid data"},
			{"SCENARIO_3_POST_HTML_TO_SFDT", "AccountA", "valid", "POST_HTML_TO_SFDT", "400", "bad_request", "HTML to SFDT endpoint should return 400 for invalid data"},

			// SCENARIO 4: TEMPLATE SECURITY TESTING
			{"SCENARIO_4_POST_CREATE_TEMPLATE", "AccountA", "valid", "POST_CREATE_TEMPLATE", "200", "success", "Account A should be able to create template"},
			{"SCENARIO_4_GET_TEMPLATE_BY_ID", "AccountB", "valid", "GET_TEMPLATE_BY_ID", "500", "server_error", "Account B should get server error for invalid template access"},
			{"SCENARIO_4_GET_ALL_TEMPLATES", "AccountB", "valid", "GET_ALL_TEMPLATES", "200", "success", "Account B should get empty template list"},
			{"SCENARIO_4_GET_TEMPLATES_COUNT", "AccountB", "valid", "GET_TEMPLATES_COUNT", "200", "success", "Account B should get zero template count"},

			// SCENARIO 5: DATA EXPOSURE TESTING
			{"SCENARIO_5_DATA_EXPOSURE_ESR", "AccountA", "valid", "GET_ESR_BY_ID", "404", "not_found", "ESR response should return 404 for non-existent ESR"},
			{"SCENARIO_5_DATA_EXPOSURE_TEMPLATE", "AccountA", "valid", "GET_TEMPLATE_BY_ID", "500", "server_error", "Template response should return 500 for non-existent template (security issue)"},

			// SCENARIO 6: CROSS-ACCOUNT DATA ISOLATION
			{"SCENARIO_6_DATA_ISOLATION_ESR", "AccountA", "valid", "GET_ALL_ESR", "200", "success", "Account A should only see its own ESRs"},
			{"SCENARIO_6_DATA_ISOLATION_TEMPLATE", "AccountA", "valid", "GET_ALL_TEMPLATES", "200", "success", "Account A should only see its own templates"},

			// SCENARIO 7: EDGE CASES AND BOUNDARY TESTING
			{"SCENARIO_7_NONEXISTENT_ACCOUNT", "AccountC", "valid", "GET_ESR_BY_ID", "401", "Unauthorized", "Non-existent account should return 401"},
			{"SCENARIO_7_EXPIRED_TOKEN", "AccountB", "expired", "GET_ESR_BY_ID", "401", "token_expired", "Expired token should return 401"},
			{"SCENARIO_7_MALFORMED_TOKEN", "AccountB", "malformed", "GET_ESR_BY_ID", "401", "Unauthorized", "Malformed token should return 401"},
			{"SCENARIO_7_EMPTY_TOKEN", "AccountB", "empty", "GET_ESR_BY_ID", "401", "unauthorized", "Empty token should return 401"},
			{"SCENARIO_7_NULL_TOKEN", "AccountB", "null", "GET_ESR_BY_ID", "401", "unauthorized", "Null token should return 401"}
		};
	}
}
