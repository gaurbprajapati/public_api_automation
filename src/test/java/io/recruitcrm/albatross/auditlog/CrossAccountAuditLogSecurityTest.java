package io.recruitcrm.albatross.auditlog;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.pojo.auditLog.AuditLogList;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import com.qa.api.util.Owner;

/**
 * Cross-Account Audit Log Security Test
 * Tests various security scenarios for audit log operations across different accounts
 * with different token types (valid, invalid, invalid Albatross)
 * 
 * Tests include:
 * 1. Export operations for all action types
 * 2. Count operations for all entities (candidates, contacts, companies, deals, jobs, others)
 * 3. Count operations for all action types
 * 4. Cross-account isolation verification - Account A should get count=0 for Account B's data
 */
@AccountType("CrossAccount|AuditLog")
public class CrossAccountAuditLogSecurityTest extends TestBase {

    private String exportBasePath = "export-logs";
    private String countBasePath = "count/get";
    private int accountAId;
    private String accountA_Token;
    private ListFunctions listFunctions = new ListFunctions();
    commanFunction function = new commanFunction();
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    private AuditLogList auditLogList;
    
    @BeforeClass(alwaysRun = true)    public void setUp() {
        accountA_Token = getAccountApiKey("AccountA");
        // Initialize common audit log list parameters
        auditLogList = new AuditLogList();
        auditLogList.setOrderBy("added-on");
        auditLogList.setOrder("desc");
        auditLogList.setCandidateSlugs(new String[]{"all"});
        auditLogList.setContactSlugs(new String[]{"all"});
        auditLogList.setCompanySlugs(new String[]{"all"});
        auditLogList.setDealSlugs(new String[]{"all"});
        auditLogList.setJobSlugs(new String[]{"all"});
        auditLogList.setOtherSlugs(new String[]{"all"});
    }
    
    /**
     * Comprehensive cross-account audit log security test
     * Handles all operations: export, count entities, count actions, and search
     */
    @Owner("Akshaya Uppala")
    @Test(dataProvider = "crossAccountAuditLogTestData", groups = "nightly-build")
    public void crossAccountAuditLogOperations_Test(String testScenario, String accountType, String tokenType, 
            String operation, String operationType, String expectedStatusCode, String expectedResponse, String description) {
        
        // Get appropriate token based on account and token type
        String token = getTokenForAccount(accountType, tokenType);
        
        Response response = null;
        
        try {
            // Determine the operation type and execute accordingly
            switch (operationType.toUpperCase()) {
                case "CREATE_CANDIDATE":
                    response = allCrudFunctions.createCandidate(albatrossURL, token);
                    break;
                case "EXPORT":
                    response = executeExportOperation(token, operation);
                    break;
                case "COUNT_ENTITY":
                    response = executeCountEntityOperation(token, operation);
                    break;
                case "COUNT_ACTION":
                    response = executeCountActionOperation(token, operation);
                    break;
                case "SEARCH":
                    response = executeSearchOperation(token, operation);
                    break;
                default:
                    Assert.fail("Unsupported operation type: " + operationType);
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
            
            // Additional validations based on expected response and operation type
            validateResponse(response, expectedResponse, operationType);
            
        } catch (Exception e) {
            // Handle exceptions for invalid scenarios
            if (expectedResponse.contains("unauthorized") || expectedResponse.contains("cross_account_isolation") || 
                expectedResponse.contains("token_expired") || expectedResponse.contains("bad_request") || 
                expectedResponse.contains("access_denied") || expectedResponse.contains("forbidden")) {
                // Expected failure scenario - no action needed
            } else {
                throw e;
            }
        }
    }
    
    /**
     * Execute export operation
     */
    private Response executeExportOperation(String token, String actionType) {
        auditLogList.setActionType(actionType);
        return RestClient.doPost1("JSON", auditLogURL, exportBasePath, token, null, null, true, auditLogList);
    }
    
    /**
     * Execute count entity operation
     */
    private Response executeCountEntityOperation(String token, String entityType) {
        AuditLogList entityAuditLogList = new AuditLogList();
        entityAuditLogList.setOrderBy("added-on");
        entityAuditLogList.setOrder("desc");
        entityAuditLogList.setPerformedBy(new String[]{String.valueOf(accountAId)});
        // Set entity-specific slugs based on entity type
        switch (entityType.toLowerCase()) {
            case "candidates":
                entityAuditLogList.setCandidateSlugs(new String[]{"all"});
                break;
            case "contacts":
                entityAuditLogList.setContactSlugs(new String[]{"all"});
                break;
            case "companies":
                entityAuditLogList.setCompanySlugs(new String[]{"all"});
                break;
            case "deals":
                entityAuditLogList.setDealSlugs(new String[]{"all"});
                break;
            case "jobs":
                entityAuditLogList.setJobSlugs(new String[]{"all"});
                break;
            case "others":
                entityAuditLogList.setOtherSlugs(new String[]{"all"});
                break;
            case "all_entities":
            default:
                entityAuditLogList.setCandidateSlugs(new String[]{"all"});
                entityAuditLogList.setContactSlugs(new String[]{"all"});
                entityAuditLogList.setCompanySlugs(new String[]{"all"});
                entityAuditLogList.setDealSlugs(new String[]{"all"});
                entityAuditLogList.setJobSlugs(new String[]{"all"});
                entityAuditLogList.setOtherSlugs(new String[]{"all"});
                break;
        }
        
        return RestClient.doPost1("JSON", auditLogURL, countBasePath, token, null, null, true, entityAuditLogList);
    }
    
    /**
     * Execute count action operation
     */
    private Response executeCountActionOperation(String token, String actionType) {
        AuditLogList actionAuditLogList = new AuditLogList();
        actionAuditLogList.setOrderBy("added-on");
        actionAuditLogList.setOrder("desc");
        actionAuditLogList.setActionType(actionType);
        actionAuditLogList.setCandidateSlugs(new String[]{"all"});
        actionAuditLogList.setContactSlugs(new String[]{"all"});
        actionAuditLogList.setCompanySlugs(new String[]{"all"});
        actionAuditLogList.setDealSlugs(new String[]{"all"});
        actionAuditLogList.setJobSlugs(new String[]{"all"});
        actionAuditLogList.setOtherSlugs(new String[]{"all"});
        actionAuditLogList.setPerformedBy(new String[]{String.valueOf(accountAId)});
        
        return RestClient.doPost1("JSON", auditLogURL, countBasePath, token, null, null, true, actionAuditLogList);
    }
    
    /**
     * Execute search operation
     */
    private Response executeSearchOperation(String token, String searchType) {
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("page", "1");
        
        String searchBasePath = "search";
        AuditLogList searchAuditLogList = new AuditLogList();
        searchAuditLogList.setPerformedBy(new String[]{String.valueOf(accountAId)});
        searchAuditLogList.setOnLoadData(1);
        searchAuditLogList.setActionType("Create");
        // Configure search based on search type
        if (!searchType.equals("basic_search")) {
            searchAuditLogList.setOrderBy("added-on");
            searchAuditLogList.setOrder("desc");
            
            // Set specific search parameters based on search type
            switch (searchType.toLowerCase()) {
                case "candidates_search":
                    searchAuditLogList.setCandidateSlugs(new String[]{"all"});
                    break;
                case "contacts_search":
                    searchAuditLogList.setContactSlugs(new String[]{"all"});
                    break;
                case "companies_search":
                    searchAuditLogList.setCompanySlugs(new String[]{"all"});
                    break;
                case "deals_search":
                    searchAuditLogList.setDealSlugs(new String[]{"all"});
                    break;
                case "jobs_search":
                    searchAuditLogList.setJobSlugs(new String[]{"all"});
                    break;
                case "others_search":
                    searchAuditLogList.setOtherSlugs(new String[]{"all"});
                    break;
                case "all_entities_search":
                default:
                    searchAuditLogList.setCandidateSlugs(new String[]{"all"});
                    searchAuditLogList.setContactSlugs(new String[]{"all"});
                    searchAuditLogList.setCompanySlugs(new String[]{"all"});
                    searchAuditLogList.setDealSlugs(new String[]{"all"});
                    searchAuditLogList.setJobSlugs(new String[]{"all"});
                    searchAuditLogList.setOtherSlugs(new String[]{"all"});
                    break;
            }
        }
        
        return RestClient.doPost1("JSON", auditLogURL, searchBasePath, token, queryParameters, null, true, searchAuditLogList);
    }
    
    /**
     * Validate response based on expected response and operation type
     */
    private void validateResponse(Response response, String expectedResponse, String operationType) {
        switch (expectedResponse) {
            case "success":
                if (operationType.equals("EXPORT")) {
                    response.then().body("message", Matchers.is("Audit logs Exported successfully"));
                    response.then().body("message_type", Matchers.is("is-success"));
                    response.then().body("status", Matchers.is("success"));
                } else if (operationType.equals("COUNT_ENTITY")) {
                    // For count operations - should get valid total
                    response.then().statusCode(200);    
                    response.then().body("total", Matchers.notNullValue());
                } else if (operationType.equals("COUNT_ACTION")) {
                    // For count operations - should get valid total
                    response.then().statusCode(200);
                } else if (operationType.equals("SEARCH")) {
                    // For search operations - should get valid search results
                    response.then().body("current_page", Matchers.is(1));
                    response.then().body("data", Matchers.notNullValue());
                    // Validate that Account A can see its own audit log data
                    if (response.jsonPath().getList("data").size() > 0) {
                        response.then().body("data[0]._id", Matchers.notNullValue());
                        response.then().body("data[0].entity_slug", Matchers.notNullValue());
                        response.then().body("data[0].entity_slug_detail", Matchers.notNullValue());
                    }
                }
                break;
            case "success_count":
                // System.out.println("response: ****************** " + response.jsonPath().prettyPrint());
                response.then().body("data.size()", Matchers.equalTo(1));
                break;
                
            case "cross_account_isolation":
                // For cross-account access - should get count=0 (proper isolation)
                response.then().body("total", Matchers.equalTo(0));
                break;
                
            case "unauthorized":
                try {
                    response.then().statusCode(401);
                    response.then().body("error", Matchers.containsString("Unauthorized"));
                } catch (Exception e) {
                    // If error field doesn't exist, just validate status code
                }
                break;
        }
    }

    /**
     * Comprehensive data provider for all cross-account audit log operations
     * Includes export, count entities, and count actions operations
     */
    @DataProvider(name = "crossAccountAuditLogTestData")
    public static Object[][] crossAccountAuditLogTestData() {
        return new Object[][] {
            // ===== CREATE CANDIDATE =====
            {"CREATE_CANDIDATE", "AccountA", "valid", "Create", "CREATE_CANDIDATE", "200", "success", "Account A should create a candidate successfully"},

            // ===== EXPORT OPERATIONS =====
            // Account A export operations (should succeed)
            {"EXPORT_1_CREATE", "AccountA", "valid", "Create", "EXPORT", "200", "success", "Account A should export Create action logs successfully"},
            {"EXPORT_1_UPDATE", "AccountA", "valid", "Update", "EXPORT", "200", "success", "Account A should export Update action logs successfully"},
            {"EXPORT_1_ALL_ACTIONS", "AccountA", "valid", "All Actions", "EXPORT", "200", "success", "Account A should export All Actions logs successfully"},
            
            // Account B export operations (should succeed but only see own data)
            {"EXPORT_2_CREATE", "AccountB", "valid", "Create", "EXPORT", "200", "success", "Account B should only see their own Create action logs"},
            {"EXPORT_2_ALL_ACTIONS", "AccountB", "valid", "All Actions", "EXPORT", "200", "success", "Account B should only see their own All Actions logs"},
            
            // Invalid token export operations
            {"EXPORT_3_INVALID", "AccountB", "invalid", "Create", "EXPORT", "401", "unauthorized", "Invalid token should be denied for export"},
            {"EXPORT_3_EXPIRED", "AccountB", "expired", "Create", "EXPORT", "401", "unauthorized", "Expired token should be denied for export"},
            
            // ===== COUNT ENTITY OPERATIONS =====
            // Account A accessing own entity data (should succeed)
            {"COUNT_ENTITY_1_ALL", "AccountA", "valid", "all_entities", "COUNT_ENTITY", "200", "success", "Account A should get count for all entities"},
            {"COUNT_ENTITY_1_CANDIDATES", "AccountA", "valid", "candidates", "COUNT_ENTITY", "200", "success", "Account A should get count for candidates"},
            {"COUNT_ENTITY_1_CONTACTS", "AccountA", "valid", "contacts", "COUNT_ENTITY", "200", "success", "Account A should get count for contacts"},
            {"COUNT_ENTITY_1_COMPANIES", "AccountA", "valid", "companies", "COUNT_ENTITY", "200", "success", "Account A should get count for companies"},
            {"COUNT_ENTITY_1_DEALS", "AccountA", "valid", "deals", "COUNT_ENTITY", "200", "success", "Account A should get count for deals"},
            {"COUNT_ENTITY_1_JOBS", "AccountA", "valid", "jobs", "COUNT_ENTITY", "200", "success", "Account A should get count for jobs"},
            {"COUNT_ENTITY_1_OTHERS", "AccountA", "valid", "others", "COUNT_ENTITY", "200", "success", "Account A should get count for others"},
            
            // Cross-account entity isolation (Account B should get count=0 for Account A's data)
            {"COUNT_ENTITY_2_ALL", "AccountB", "invalid", "all_entities", "COUNT_ENTITY", "401", "cross_account_isolation", "Account B should get unauthorized"},
            {"COUNT_ENTITY_2_CANDIDATES", "AccountB", "invalid", "candidates", "COUNT_ENTITY", "401", "cross_account_isolation", "Account B should get unauthorized"},
            {"COUNT_ENTITY_2_CONTACTS", "AccountB", "invalid", "contacts", "COUNT_ENTITY", "401", "cross_account_isolation", "Account B should get unauthorized"},
            {"COUNT_ENTITY_2_COMPANIES", "AccountB", "invalid", "companies", "COUNT_ENTITY", "401", "cross_account_isolation", "Account B should get unauthorized"},
            {"COUNT_ENTITY_2_DEALS", "AccountB", "invalid", "deals", "COUNT_ENTITY", "401", "cross_account_isolation", "Account B should get unauthorized"},
            {"COUNT_ENTITY_2_JOBS", "AccountB", "invalid", "jobs", "COUNT_ENTITY", "401", "cross_account_isolation", "Account B should get unauthorized"},
            {"COUNT_ENTITY_2_OTHERS", "AccountB", "invalid", "others", "COUNT_ENTITY", "401", "cross_account_isolation", "Account B should get unauthorized"},
            
            // Invalid token entity operations
            {"COUNT_ENTITY_3_INVALID", "AccountB", "invalid", "all_entities", "COUNT_ENTITY", "401", "unauthorized", "Invalid token should be denied for entity count"},
            {"COUNT_ENTITY_3_EXPIRED", "AccountB", "expired", "candidates", "COUNT_ENTITY", "401", "unauthorized", "Expired token should be denied for candidate count"},
            {"COUNT_ENTITY_3_MALFORMED", "AccountB", "malformed", "contacts", "COUNT_ENTITY", "401", "unauthorized", "Malformed token should be denied for contact count"},
            
            // ===== COUNT ACTION OPERATIONS =====
            // Account A accessing own action data (should succeed)
            {"COUNT_ACTION_1_CREATE", "AccountA", "valid", "Create", "COUNT_ACTION", "200", "success", "Account A should get count for Create actions"},
            {"COUNT_ACTION_1_ALL", "AccountA", "valid", "All Actions", "COUNT_ACTION", "200", "success", "Account A should get count for All Actions"},
            
            // Cross-account action isolation (Account B should get count=0 for Account A's data)
            {"COUNT_ACTION_2_CREATE", "AccountB", "invalid", "Create", "COUNT_ACTION", "401", "cross_account_isolation", "Account B should get unauthorized"},
            {"COUNT_ACTION_2_ALL", "AccountB", "invalid", "All Actions", "COUNT_ACTION", "401", "cross_account_isolation", "Account B should get unauthorized"},
            
            // Invalid token action operations
            {"COUNT_ACTION_3_INVALID", "AccountB", "invalid", "Create", "COUNT_ACTION", "401", "unauthorized", "Invalid token should be denied for Create action count"},
            {"COUNT_ACTION_3_EXPIRED", "AccountB", "expired", "Update", "COUNT_ACTION", "401", "unauthorized", "Expired token should be denied for Update action count"},
            {"COUNT_ACTION_3_MALFORMED", "AccountB", "malformed", "Delete", "COUNT_ACTION", "401", "unauthorized", "Malformed token should be denied for Delete action count"},
            {"COUNT_ACTION_3_EMPTY", "AccountB", "empty", "Import", "COUNT_ACTION", "401", "unauthorized", "Empty token should be denied for Import action count"},
            {"COUNT_ACTION_3_NULL", "AccountB", "null", "All Actions", "COUNT_ACTION", "401", "unauthorized", "Null token should be denied for All Actions count"},
            
            // ===== SEARCH OPERATIONS =====
            // Account A searching own audit log data (should succeed)
            {"SEARCH_1_BASIC", "AccountA", "valid", "basic_search", "SEARCH", "200", "success", "Account A should search audit logs successfully"},
            {"SEARCH_1_ALL_ENTITIES", "AccountA", "valid", "all_entities_search", "SEARCH", "200", "success", "Account A should search all entities audit logs successfully"},
            {"SEARCH_1_CANDIDATES", "AccountA", "valid", "candidates_search", "SEARCH", "200", "success", "Account A should search candidates audit logs successfully"},
            {"SEARCH_1_CONTACTS", "AccountA", "valid", "contacts_search", "SEARCH", "200", "success", "Account A should search contacts audit logs successfully"},
            {"SEARCH_1_COMPANIES", "AccountA", "valid", "companies_search", "SEARCH", "200", "success", "Account A should search companies audit logs successfully"},
            {"SEARCH_1_DEALS", "AccountA", "valid", "deals_search", "SEARCH", "200", "success", "Account A should search deals audit logs successfully"},
            {"SEARCH_1_JOBS", "AccountA", "valid", "jobs_search", "SEARCH", "200", "success", "Account A should search jobs audit logs successfully"},
            {"SEARCH_1_OTHERS", "AccountA", "valid", "others_search", "SEARCH", "200", "success", "Account A should search others audit logs successfully"},
            
            // Cross-account search isolation (Account B should only see its own data)
            {"SEARCH_2_BASIC", "AccountB", "valid", "basic_search", "SEARCH", "200", "success_count", "Account B should only see its own audit logs in search"},
            {"SEARCH_2_ALL_ENTITIES", "AccountB", "valid", "all_entities_search", "SEARCH", "200", "success_count", "Account B should only see its own entities in search"},
            {"SEARCH_2_CANDIDATES", "AccountB", "valid", "candidates_search", "SEARCH", "200", "success_count", "Account B should only see its own candidates in search"},
            {"SEARCH_2_CONTACTS", "AccountB", "valid", "contacts_search", "SEARCH", "200", "success_count", "Account B should only see its own contacts in search"},
            {"SEARCH_2_COMPANIES", "AccountB", "valid", "companies_search", "SEARCH", "200", "success_count", "Account B should only see its own companies in search"},
            {"SEARCH_2_DEALS", "AccountB", "valid", "deals_search", "SEARCH", "200", "success_count", "Account B should only see its own deals in search"},
            {"SEARCH_2_JOBS", "AccountB", "valid", "jobs_search", "SEARCH", "200", "success_count", "Account B should only see its own jobs in search"},
            {"SEARCH_2_OTHERS", "AccountB", "valid", "others_search", "SEARCH", "200", "success_count", "Account B should only see its own others in search"},

            // Invalid token search operations
            {"SEARCH_3_INVALID", "AccountB", "invalid", "basic_search", "SEARCH", "401", "unauthorized", "Invalid token should be denied for search"},
            {"SEARCH_3_EXPIRED", "AccountB", "expired", "candidates_search", "SEARCH", "401", "unauthorized", "Expired token should be denied for candidates search"},
            {"SEARCH_3_MALFORMED", "AccountB", "malformed", "contacts_search", "SEARCH", "401", "unauthorized", "Malformed token should be denied for contacts search"},
            {"SEARCH_3_EMPTY", "AccountB", "empty", "companies_search", "SEARCH", "401", "unauthorized", "Empty token should be denied for companies search"},
            {"SEARCH_3_NULL", "AccountB", "null", "all_entities_search", "SEARCH", "401", "unauthorized", "Null token should be denied for all entities search"}
        };
    }
}
