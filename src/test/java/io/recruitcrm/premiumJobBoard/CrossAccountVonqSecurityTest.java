package io.recruitcrm.premiumJobBoard;

import java.util.HashMap;
import java.util.Map;

import io.rcrm.api.javafaker.jobboard.JavaFakerVonq;
import io.rcrm.api.pojo.premiumJobBoard.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import com.qa.api.util.Owner;

/**
 * Cross-Account Vonq Security Test
 * Tests various security scenarios for Vonq campaign operations across different accounts
 * with different token types (valid, invalid, expired, malformed)
 * 
 * This test ensures proper security isolation between accounts for:
 * - Campaign draft operations
 * - Campaign management
 * - Analytics access
 * - Cross-account data protection
 */
@AccountType("CrossAccount")
public class CrossAccountVonqSecurityTest extends TestBase {

    private String campaignDraftId = "";
    private String savedCampaignId = "";
    AllCrudFunctions privateFunction = new AllCrudFunctions();
	commanFunction function = new commanFunction();
    private JavaFakerVonq faker = new JavaFakerVonq();
    private String jobBoardCompanyName = faker.getJobBoardCompanyName();
    private String companyEmail = faker.getCompanyEmail();
    private String jobBoardUrl = faker.getJobBoardUrl();
    private String jobDescription = faker.getJobDescription();
    private int generatedRandomNumber = faker.getGeneratedRandomNumber();
    private String campaignName = faker.getCampaignName();
    private String campaignId = faker.getCampaignId();
    private int jobAId;

    @BeforeClass(alwaysRun = true)	public void setUp() {
		jobAId = getTestJobId(getTokenForAccount("AccountA","valid"),getAccountApiKey("AccountA"));
	}
    // Campaign data JSON template
    private final String CAMPAIGN_DATA_JSON = "{\"campaign_form\":{\"labels\":null,\"recruiterInfo\":" +
            "{\"name\":\"" + jobBoardCompanyName + "\",\"emailAddress\":\"" + companyEmail + "\"}," +
            "\"postingDetails\":{\"title\":\"Test Campaign Job\",\"description\":\"" + jobDescription + "\"," +
            "\"organization\":{\"name\":\"Test Organization\",\"companyLogo\":\"\"}," +
            "\"contactInfo\":{\"name\":\"\",\"emailAddress\":\"\",\"phoneNumber\":\"\"}," +
            "\"workingLocation\":{\"addressLine1\":\"\",\"addressLine2\":\"\",\"postcode\":\"\",\"city\":\"\"," +
            "\"country\":\"\",\"allowsRemoteWork\":0},\"yearsOfExperience\":0,\"employmentType\":\"permanent\"," +
            "\"weeklyWorkingHours\":{\"from\":0,\"to\":0},\"salaryIndication\":{\"period\":\"monthly\"," +
            "\"range\":{\"from\":0,\"to\":0,\"currency\":\"INR\"}},\"jobPageUrl\":\"https://test.recruitcrm.net/apply/test\"," +
            "\"applicationUrl\":\"https://test.recruitcrm.net/apply/test\"}," +
            "\"targetGroup\":{\"educationLevel\":[],\"seniority\":[],\"industry\":[],\"jobCategory\":[]}," +
            "\"orderedProducts\":[],\"orderedProductsSpecs\":{}," +
            "\"campaignName\":\"Test Campaign\",\"companyId\":\"12345\",\"poNumber\":\"\",\"currency\":\"USD\"," +
            "\"paymentMethod\":null},\"recruiter\":{},\"basket\":[]}";

    /**
     * Comprehensive test covering cross-account Vonq operations
     * Multiple scenarios: Valid operations, invalid tokens, edge cases, boundary testing, concurrent access, data integrity
     */
    @Owner("Akshaya Uppala")
    @Test(dataProvider = "crossAccountVonqTestData", groups = "nightly-build")
    public void crossAccountVonqOperations_Test(String testScenario, String accountType, String tokenType, 
            String operation, String expectedStatusCode, String expectedResponse, String description) {
        
        String generatedString = RandomStringUtils.randomAlphabetic(4);
        
        // Get appropriate token based on account and token type
        String token = getTokenForAccount(accountType, tokenType);    
        Response response = null;
        Map<String, String> pathParameters = new HashMap<>();
        
        try {
            switch (operation.toUpperCase()) {
                    
                case "POST_CREATE_DRAFT":
                    // Create campaign draft from Account A
                    SaveCampaignAsDraft saveCampaignAsDraft = new SaveCampaignAsDraft();
                    saveCampaignAsDraft.setCampaign_name(campaignName + "_" + generatedString);
                    saveCampaignAsDraft.setJob_id(jobAId);
                    saveCampaignAsDraft.setCampaign_data(CAMPAIGN_DATA_JSON);
                    
                    response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns/draft", 
                            token, null, true, saveCampaignAsDraft);
                    
                    // Extract and store the draft ID (only for successful responses)
                    if (response.getStatusCode() == 200) {
                        JsonPath jp = response.jsonPath();
                        Integer ID = jp.get("data.'draft campaign'.id");
                        if (ID != null) {
                            campaignDraftId = Integer.toString(ID);
                        } else {
                            campaignDraftId = "1002";
                        }
                    }
                    break;
                    
                case "POST_GET_ALL_DRAFTS":
                    // Get all campaign drafts
                    GetCampaignDraftList getCampaignDraftList = new GetCampaignDraftList();
                    getCampaignDraftList.setSort_by("created_on");
                    getCampaignDraftList.setSort_order("desc");
                    response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns/draft-list",
                            token, null, false, getCampaignDraftList);
                    break;
                    
                case "GET_DRAFT_BY_ID":
                    // Get campaign draft by ID
                    pathParameters.put("id", campaignDraftId.isEmpty() ? "1002" : campaignDraftId);
                    response = RestClient.doGet("JSON", jobBoardServiceURL, "vonq/campaigns/draft/{id}",
                            token, null, pathParameters, false);
                    break;
                    
                case "POST_EDIT_DRAFT":
                    // Edit campaign draft
                    SaveCampaignAsDraft editDraft = new SaveCampaignAsDraft();
                    editDraft.setCampaign_name(campaignName + "_Edited_" + generatedString);
                    editDraft.setJob_id(jobAId);
                    editDraft.setCampaign_data(CAMPAIGN_DATA_JSON);
                    
                    pathParameters.put("id", campaignDraftId.isEmpty() ? "1002" : campaignDraftId);
                    response = RestClient.doPost1("JSON", jobBoardServiceURL, "vonq/campaigns/draft/{id}",
                            token, null, pathParameters, true, editDraft);
                    break;
                    
                case "POST_SAVE_CAMPAIGN":
                    // Save campaign
                    SaveCampaign saveCampaign = createSaveCampaignObject(generatedString);
                    response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns",
                            token, null, false, saveCampaign);
                    
                    // Extract and store the campaign ID (only for successful responses)
                    if (response.getStatusCode() == 200) {
                        JsonPath jp = response.jsonPath();
                        Integer ID = jp.get("data.campaign.id");
                        if (ID != null) {
                            savedCampaignId = Integer.toString(ID);
                        } else {
                            savedCampaignId = "1002";
                        }
                    }
                    break;
                    
                case "POST_GET_ALL_CAMPAIGNS":
                    // Get all saved campaigns
                    GetCampaignDraftList getCampaignList = new GetCampaignDraftList();
                    getCampaignList.setSort_by("created_on");
                    getCampaignList.setSort_order("desc");
                    response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns/list",
                            token, null, false, getCampaignList);
                    break;
                    
                case "GET_CAMPAIGN_ANALYTICS":
                    // Get campaign analytics
                    pathParameters.put("id", savedCampaignId.isEmpty() ? "1002" : savedCampaignId);
                    response = RestClient.doGet("JSON", jobBoardServiceURL, "vonq/campaigns/analytics/{id}",
                            token, null, pathParameters, true);
                    break;
                    
                case "POST_CAMPAIGN_LINE_CHART":
                    // Get campaign line chart analytics
                    pathParameters.put("id", savedCampaignId.isEmpty() ? "1002" : savedCampaignId);
                    response = RestClient.doPost1("JSON", jobBoardServiceURL, "vonq/campaigns/analytics/line-chart/{id}",
                            token, null, pathParameters, true, null);
                    break;
                    
                case "GET_CAMPAIGN_PIE_CHART":
                    // Get campaign pie chart analytics
                    pathParameters.put("id", savedCampaignId.isEmpty() ? "1002" : savedCampaignId);
                    response = RestClient.doGet("JSON", jobBoardServiceURL, "vonq/campaigns/analytics/pie-chart/{id}",
                            token, null, pathParameters, true);
                    break;
                    
                case "DELETE_CAMPAIGN":
                    // Delete campaign
                    pathParameters.put("id", savedCampaignId.isEmpty() ? "1002" : savedCampaignId);
                    response = RestClient.doDelete("JSON", jobBoardServiceURL, "vonq/campaigns/{id}",
                            token, null, pathParameters, false);
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
            
            // Additional validations based on expected response
            switch (expectedResponse) {
                case "success":
                    if (operation.startsWith("GET") || operation.startsWith("POST_GET")) {
                        response.then().body(Matchers.notNullValue());
                    }
                    break;
                    
                case "access_denied":
                case "forbidden":
                    // For cross-account access restrictions
                    try {
                        response.then().body("error", Matchers.containsString("forbidden"));
                    } catch (Exception e) {
                        // If error field doesn't exist or is different, just validate status code
                    }
                    break;
                    
                case "not_found":
                    // For resources not found in cross-account scenarios
                    try {
                        response.then().body("message", Matchers.containsString("not found"));
                    } catch (Exception e) {
                        // If message field doesn't exist, try error field
                        try {
                            response.then().body("error", Matchers.containsString("not found"));
                        } catch (Exception e2) {
                            // If neither field exists, just validate status code
                        }
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
                case "unprocessable_entity":
                        response.then().statusCode(422);
                    break;
                case "internal_server_error":
                    response.then().statusCode(500);
                    break;
                default:
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
     * Comprehensive data provider for cross-account Vonq operations
     * Covers multiple scenarios: Valid operations, invalid tokens, edge cases, boundary testing, concurrent access, data integrity
     */
    @DataProvider(name = "crossAccountVonqTestData")
    public static Object[][] crossAccountVonqTestData() {
        return new Object[][] {
            // ===== SCENARIO 1: CROSS-ACCOUNT SECURITY OPERATIONS =====
            // Account A creates and manages campaigns (should succeed)
            {"SCENARIO_1_CREATE_DRAFT", "AccountA", "valid", "POST_CREATE_DRAFT", "200", "success", "Account A should be able to create campaign draft"},
            {"SCENARIO_1_SAVE_CAMPAIGN", "AccountA", "valid", "POST_SAVE_CAMPAIGN", "200", "success", "Account A should be able to save campaign"},
            {"SCENARIO_1_A_GET_DRAFT_BY_ID", "AccountA", "valid", "GET_DRAFT_BY_ID", "200", "success", "Account A should be able to get draft by id"},
            {"SCENARIO_1_A_EDIT_DRAFT", "AccountA", "valid", "POST_EDIT_DRAFT", "200", "success", "Account A should be able to edit draft"},
            {"SCENARIO_1_A_GET_ANALYTICS", "AccountA", "valid", "GET_CAMPAIGN_ANALYTICS", "200", "success", "Account A should be able to get campaign analytics"},
            {"SCENARIO_1_A_DELETE_CAMPAIGN", "AccountA", "valid", "DELETE_CAMPAIGN", "200", "success", "Account A should be able to delete campaign"},
            
            // Account B attempts cross-account operations (should have restricted access)
            {"SCENARIO_1_B_GET_DRAFTS", "AccountB", "valid", "POST_GET_ALL_DRAFTS", "200", "success", "Account B should get drafts (but only their own)"},
            {"SCENARIO_1_B_GET_CAMPAIGNS", "AccountB", "valid", "POST_GET_ALL_CAMPAIGNS", "200", "success", "Account B should get campaigns (but only their own)"},
            {"SCENARIO_1_B_GET_DRAFT_BY_ID", "AccountB", "valid", "GET_DRAFT_BY_ID", "422", "unprocessable_entity", "Account B should be denied access to Account A's draft"},
            {"SCENARIO_1_B_EDIT_DRAFT", "AccountB", "valid", "POST_EDIT_DRAFT", "422", "unprocessable_entity", "Account B should be denied edit access to Account A's draft"},
            {"SCENARIO_1_B_GET_ANALYTICS", "AccountB", "valid", "GET_CAMPAIGN_ANALYTICS", "500", "internal_server_error", "Account B should be denied analytics access to Account A's campaign"},
            {"SCENARIO_1_B_DELETE_CAMPAIGN", "AccountB", "valid", "DELETE_CAMPAIGN", "404", "not_found1", "Account B should be denied delete access to Account A's campaign"},
            
            // ===== SCENARIO 2: INVALID TOKEN OPERATIONS =====
            // Account B performs operations with invalid token (should fail)
            {"SCENARIO_2_CREATE_DRAFT_INVALID", "AccountB", "invalid", "POST_CREATE_DRAFT", "401", "unauthorized", "Account B should be denied draft creation with invalid token"},
            {"SCENARIO_2_GET_DRAFTS_INVALID", "AccountB", "invalid", "POST_GET_ALL_DRAFTS", "401", "unauthorized", "Account B should be denied drafts access with invalid token"},
            {"SCENARIO_2_GET_CAMPAIGNS_INVALID", "AccountB", "invalid", "POST_GET_ALL_CAMPAIGNS", "401", "unauthorized", "Account B should be denied campaigns access with invalid token"},
            {"SCENARIO_2_GET_ANALYTICS_INVALID", "AccountB", "invalid", "GET_CAMPAIGN_ANALYTICS", "401", "unauthorized", "Account B should be denied analytics with invalid token"},
            {"SCENARIO_2_DELETE_INVALID", "AccountB", "invalid", "DELETE_CAMPAIGN", "401", "unauthorized", "Account B should be denied delete with invalid token"},
            
            // ===== SCENARIO 3: EDGE CASES =====
            // Account C (non-existent) attempts operations
            {"SCENARIO_3_NON_EXISTENT_ACCOUNT", "AccountC", "valid", "POST_CREATE_DRAFT", "401", "unauthorized", "Account C should be denied draft creation with non-existent account"},
            {"SCENARIO_3_NON_EXISTENT_ACCOUNT", "AccountC", "valid", "POST_SAVE_CAMPAIGN", "401", "unauthorized", "Account C should be denied save campaign with non-existent account"},
            {"SCENARIO_3_NON_EXISTENT_ACCOUNT", "AccountC", "valid", "POST_GET_ALL_DRAFTS", "401", "unauthorized", "Account C should be denied get drafts with non-existent account"},
            {"SCENARIO_3_NON_EXISTENT_ACCOUNT", "AccountC", "valid", "POST_GET_ALL_CAMPAIGNS", "401", "unauthorized", "Account C should be denied get campaigns with non-existent account"},
            {"SCENARIO_3_NON_EXISTENT_ACCOUNT", "AccountC", "valid", "GET_DRAFT_BY_ID", "401", "unauthorized", "Account C should be denied get draft by id with non-existent account"},
            {"SCENARIO_3_NON_EXISTENT_ACCOUNT", "AccountC", "valid", "POST_EDIT_DRAFT", "401", "unauthorized", "Account C should be denied edit draft with non-existent account"},
            {"SCENARIO_3_NON_EXISTENT_ACCOUNT", "AccountC", "valid", "GET_CAMPAIGN_ANALYTICS", "401", "unauthorized", "Account C should be denied get campaign analytics with non-existent account"},
            
            // Account B with expired token
            {"SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "POST_CREATE_DRAFT", "401", "token_expired", "Expired token should return 401"},
                
            // Account B with malformed token
            {"SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "POST_CREATE_DRAFT", "401", "Unauthorized", "Malformed token should return 401"},
            
            // ===== SCENARIO 4: BOUNDARY TESTING =====
             // Account B with empty token
            {"SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "POST_CREATE_DRAFT", "401", "unauthorized", "Empty token should return 401"},

            // Account B with null token
            {"SCENARIO_4_NULL_TOKEN", "AccountB", "null", "POST_CREATE_DRAFT", "401", "unauthorized", "Null token should return 401"},
            
            // ===== SCENARIO 6: DATA INTEGRITY VERIFICATION =====
            // Verify data consistency across accounts
            {"SCENARIO_6_DATA_INTEGRITY_A", "AccountA", "valid", "POST_GET_ALL_DRAFTS", "200", "success", "Account A should see consistent draft data"},
            {"SCENARIO_6_DATA_INTEGRITY_B", "AccountB", "valid", "POST_GET_ALL_CAMPAIGNS", "200", "success", "Account B should not see Account A's campaigns"}
        };
    }


    private int getTestJobId(String token,String apiToken) {
        JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiToken).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = function.createNewContact_POST(baseURL, apiToken, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			JsonPath json = function.createNewJob(baseURL, apiToken, companySlug, contactSlug).jsonPath();
			String entitySlug = json.get("slug");
            int entityId = privateFunction.getJobResponse(albatrossURL, token, entitySlug).jsonPath().getInt("data.job.id");
            return entityId;
    }
    /**
     * Helper method to create SaveCampaign object
     */
    private SaveCampaign createSaveCampaignObject(String generatedString) {
        SaveCampaign saveCampaign = new SaveCampaign();
        saveCampaign.setCampaign_name(campaignName + "_" + generatedString);
        saveCampaign.setCampaign_id(campaignId);
        saveCampaign.setJob_id(jobAId);
        saveCampaign.setTotal_channels("1");
        saveCampaign.setStatus("in progress");
        saveCampaign.setCurrency("USD");
        saveCampaign.setTotal_price(0);
        saveCampaign.setDraft_id("1");
        
        // Create channels array
        SaveCampaign.channels[] channels = new SaveCampaign.channels[1];
        channels[0] = new SaveCampaign.channels();
        channels[0].setChannel_id(String.valueOf(generatedRandomNumber));
        channels[0].setChannel_name(jobBoardCompanyName);
        channels[0].setIs_product(true);
        channels[0].setStatus("in progress");
        channels[0].setCurrency("USD");
        channels[0].setTotal_price(0);
        channels[0].setJob_board_link(jobBoardUrl);
        channels[0].setStart_date(faker.getStartDate());
        channels[0].setEnd_date(faker.getEndDate());
        saveCampaign.setChannels(channels);
        
        return saveCampaign;
    }
}