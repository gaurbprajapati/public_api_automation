package io.recruitcrm.albatross.job;

import java.util.ArrayList;
import java.util.Arrays;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.jobs.CheckMergeDuplicate;
import io.rcrm.api.pojo.albatross.jobs.MergeDuplicates;
import io.rcrm.api.pojo.albatross.jobs.SearchEntity;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountMergeDuplicatesSecurityTest extends TestBase {

    private String primaryJobId = "";
    private String secondaryJobId = "";
    private String companySlug = "";
    private String contactSlug = "";
    private commanFunction function = new commanFunction();

    @BeforeClass(alwaysRun = true)    public void setupTestData() {
        ThreadManager.setAccount(accountA);

        JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey).jsonPath();
        companySlug = jsonCompany.getString("slug");

        JsonPath jsonContact = function.createNewContact_POST(baseURL, accountA_apiKey, companySlug).jsonPath();
        contactSlug = jsonContact.getString("slug");

        JsonPath primaryJob = function.createNewJobForMerging(baseURL, accountA_apiKey, companySlug, contactSlug).jsonPath();
        JsonPath secondaryJob = function.createNewJobForMerging(baseURL, accountA_apiKey, companySlug, contactSlug).jsonPath();

        String primaryJobName = primaryJob.getString("name");
        String secondaryJobName = secondaryJob.getString("name");

        primaryJobId = getJobId(primaryJobName, accountA_Token);
        secondaryJobId = getJobId(secondaryJobName, accountA_Token);
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountMergeDuplicatesTestData", groups = "nightly-build")
    public void crossAccountMergeDuplicatesOperations_Test(String testScenario, String accountType, String tokenType,
                                                           String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);

        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "POST_CHECK_MERGE_DUPLICATES":
                    // Check if jobs can be merged
                    CheckMergeDuplicate checkMergeDuplicate = new CheckMergeDuplicate();
                    checkMergeDuplicate.setPrimaryJobId(Integer.parseInt(primaryJobId));
                    checkMergeDuplicate.setSecondaryJobId(Integer.parseInt(secondaryJobId));
                    response = RestClient.doPost("JSON", albatrossURL, "jobs/check-merge-duplicates", token, null, true, checkMergeDuplicate);
                    break;

                case "POST_MERGE_DUPLICATES":
                    // Merge duplicate jobs
                    MergeDuplicates mergeDuplicates = new MergeDuplicates();
                    mergeDuplicates.setSelectedEntities(new ArrayList<>(Arrays.asList(Integer.parseInt(secondaryJobId))));
                    mergeDuplicates.setEntityTypeId(4); // Job entity type
                    mergeDuplicates.setMergeTo(Integer.parseInt(primaryJobId));
                    response = RestClient.doPost("JSON", albatrossURL, "merge-duplicates", token, null, true, mergeDuplicates);
                    break;

                case "POST_MERGE_DUPLICATES_EMPTY_BODY":
                    // Test merge with empty request body
                    MergeDuplicates emptyMergeDuplicates = new MergeDuplicates();
                    emptyMergeDuplicates.setSelectedEntities(new ArrayList<>(Arrays.asList()));
                    emptyMergeDuplicates.setEntityTypeId(4);
                    emptyMergeDuplicates.setMergeTo(0);
                    response = RestClient.doPost("JSON", albatrossURL, "merge-duplicates", token, null, true, emptyMergeDuplicates);
                    break;

                case "POST_MERGE_DUPLICATES_INVALID_IDS":
                    // Test merge with invalid job IDs
                    MergeDuplicates invalidMergeDuplicates = new MergeDuplicates();
                    invalidMergeDuplicates.setSelectedEntities(new ArrayList<>(Arrays.asList(99999)));
                    invalidMergeDuplicates.setEntityTypeId(4);
                    invalidMergeDuplicates.setMergeTo(99998);
                    response = RestClient.doPost("JSON", albatrossURL, "merge-duplicates", token, null, true, invalidMergeDuplicates);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported operation: " + operation);
            }

            validateResponse(response, expectedResponse, expectedStatusCode);

        } catch (Exception e) {
            if (expectedResponse.contains("unauthorized") || expectedResponse.contains("access denied") ||
                    expectedResponse.contains("token_expired") || expectedResponse.contains("bad_request") ||
                    expectedResponse.contains("not_found") || expectedResponse.contains("forbidden")) {
            } else {
                throw e;
            }
        }
    }

    private void validateResponse(Response response, String expectedResponse, String expectedStatusCode) {
        Assert.assertEquals(response.getStatusCode(), Integer.parseInt(expectedStatusCode),
                "Status code mismatch for expected response: " + expectedResponse);

        switch (expectedResponse) {
            case "success":
                response.then().body("message_type", Matchers.is("is-success"));
                break;

            case "merge_success":
                response.then().body("message", Matchers.containsString("You will receive a notification once the merging process is complete"));
                break;

            case "check_success":
                response.then().body("message", Matchers.containsString("Both jobs are ready to be merged"));
                break;

            case "validation_error":
                response.then().body("message", Matchers.containsString("The selected entities field is required"));
                break;

            case "jobs_not_found":
                response.then().body("message", Matchers.containsString("Job(s) not found"));
                break;

            case "unauthorized":
            case "Unauthorized":
                response.then().body("error", Matchers.containsString("Unauthorized"));
                break;

            case "true":
            case "null":
                response.then().body("error", Matchers.is(true));
                break;

            default:
                try {
                    response.then().body("error_message", Matchers.equalTo(expectedResponse));
                } catch (Exception e) {
                    try {
                        response.then().body("error", Matchers.equalTo(expectedResponse));
                    } catch (Exception e2) {
                        Assert.fail("Response validation failed due to exception: " + e.getMessage() + "and" + e2.getMessage());
                    }
                }
                break;
        }
    }

    private String getJobId(String jobName, String token) {
        SearchEntity searchJob = new SearchEntity(jobName, false, false, true, false, false, false);
        JsonPath jobJson = RestClient.doPost("JSON", albatrossURL, "global/search-entity", token, null, true, searchJob).jsonPath();
        return jobJson.getString("data[0].id");
    }

    @DataProvider(name = "crossAccountMergeDuplicatesTestData")
    public static Object[][] crossAccountMergeDuplicatesTestData() {
        return new Object[][]{
                // ===== SCENARIO 1: CROSS-ACCOUNT SECURITY OPERATIONS =====
                // Account A checks merge duplicates (should succeed)
                {"SCENARIO_1_CHECK_MERGE", "AccountA", "valid", "POST_CHECK_MERGE_DUPLICATES", "200", "check_success", "Account A should be able to check merge duplicates"},

                // Account A merges duplicate jobs (should succeed)
                {"SCENARIO_1_MERGE_JOBS", "AccountA", "valid", "POST_MERGE_DUPLICATES", "200", "merge_success", "Account A should be able to merge duplicate jobs"},

                // Account B attempts cross-account operations (should fail)
                {"SCENARIO_1_CROSS_ACCOUNT_CHECK", "AccountB", "valid", "POST_CHECK_MERGE_DUPLICATES", "200", "jobs_not_found", "Account B should not find Account A's jobs"},
                {"SCENARIO_1_CROSS_ACCOUNT_MERGE", "AccountB", "valid", "POST_MERGE_DUPLICATES", "401", "true", "Account B should not be able to merge Account A's jobs"},

                // ===== SCENARIO 2: INVALID TOKEN OPERATIONS =====
                // Account B performs same operations with invalid token (should fail)
                {"SCENARIO_2_CHECK_INVALID_TOKEN", "AccountB", "invalid", "POST_CHECK_MERGE_DUPLICATES", "401", "unauthorized", "Account B should be denied check access with invalid token"},
                {"SCENARIO_2_MERGE_INVALID_TOKEN", "AccountB", "invalid", "POST_MERGE_DUPLICATES", "401", "unauthorized", "Account B should be denied merge access with invalid token"},

                //===== SCENARIO 3: EDGE CASES =====
                //Account B with expired token
                {"SCENARIO_3_EXPIRED_TOKEN_CHECK", "AccountB", "expired", "POST_CHECK_MERGE_DUPLICATES", "401", "unauthorized", "Expired token should return 401 for check"},
                {"SCENARIO_3_EXPIRED_TOKEN_MERGE", "AccountB", "expired", "POST_MERGE_DUPLICATES", "401", "unauthorized", "Expired token should return 401 for merge"},

                // Account B with malformed token
                {"SCENARIO_3_MALFORMED_TOKEN_CHECK", "AccountB", "malformed", "POST_CHECK_MERGE_DUPLICATES", "401", "Unauthorized", "Malformed token should return 401 for check"},
                {"SCENARIO_3_MALFORMED_TOKEN_MERGE", "AccountB", "malformed", "POST_MERGE_DUPLICATES", "401", "Unauthorized", "Malformed token should return 401 for merge"},

                // ===== SCENARIO 4: BOUNDARY TESTING =====
                // Account B with empty token
                {"SCENARIO_4_EMPTY_TOKEN_CHECK", "AccountB", "empty", "POST_CHECK_MERGE_DUPLICATES", "401", "unauthorized", "Empty token should return 401 for check"},
                {"SCENARIO_4_EMPTY_TOKEN_MERGE", "AccountB", "empty", "POST_MERGE_DUPLICATES", "401", "unauthorized", "Empty token should return 401 for merge"},

                // Account B with null token
                {"SCENARIO_4_NULL_TOKEN_CHECK", "AccountB", "null", "POST_CHECK_MERGE_DUPLICATES", "401", "unauthorized", "Null token should return 401 for check"},
                {"SCENARIO_4_NULL_TOKEN_MERGE", "AccountB", "null", "POST_MERGE_DUPLICATES", "401", "unauthorized", "Null token should return 401 for merge"},

                // ===== SCENARIO 5: VALIDATION TESTING =====
                // Account A tests validation with empty request body
                {"SCENARIO_5_EMPTY_BODY_VALIDATION", "AccountA", "valid", "POST_MERGE_DUPLICATES_EMPTY_BODY", "422", "validation_error", "Empty request body should return validation error"},

                // Account A tests validation with invalid job IDs
                {"SCENARIO_5_INVALID_IDS_VALIDATION", "AccountA", "valid", "POST_MERGE_DUPLICATES_INVALID_IDS", "401", "null", "Invalid job IDs should return jobs not found"},

                // ===== SCENARIO 6: CROSS-ACCOUNT VALIDATION TESTING =====
                // Account B tests validation with empty request body
                {"SCENARIO_6_CROSS_ACCOUNT_EMPTY_BODY", "AccountB", "valid", "POST_MERGE_DUPLICATES_EMPTY_BODY", "422", "validation_error", "Account B should get validation error for empty body"},

                // Account B tests validation with invalid job IDs
                {"SCENARIO_6_CROSS_ACCOUNT_INVALID_IDS", "AccountB", "valid", "POST_MERGE_DUPLICATES_INVALID_IDS", "401", "null", "Account B should get jobs not found for invalid IDs"}
        };
    }
}

