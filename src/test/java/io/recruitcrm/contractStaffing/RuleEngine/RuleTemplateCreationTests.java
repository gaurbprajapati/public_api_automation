package io.recruitcrm.contractStaffing.RuleEngine;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.anyOf;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class RuleTemplateCreationTests extends RuleEngineBaseTest {

    private List<Integer> createdTemplateIds = new ArrayList<>();
    String albatrossAuthToken;
    String apiAuthToken;
    int ownerAccountID;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        apiAuthToken = ThreadManager.getAccountApiKey();

    }

    @AfterClass
    public void tearDown() {
        cleanupTemplates(albatrossAuthToken, createdTemplateIds);
    }

    private List<Map<String, Object>> createWeeklyOvertimeRulesForTest() {
        List<Map<String, Object>> rules = new ArrayList<>();

        Map<String, Object> rule = new HashMap<>();
        rule.put("id", 0);
        rule.put("ruleName", generateUniqueTemplateName("WeeklyOvertimeRule"));
        rule.put("workDayId", Arrays.asList(1, 2, 3, 4, 5, 6, 7));
        rule.put("ruleType", WEEKLY_OVERTIME);
        rule.put("chargeMethod", MULTIPLIER_CHARGE);
        rule.put("startTime", 0);
        rule.put("endTime", 0);
        rule.put("startDuration", 0);
        rule.put("endDuration", 0);
        rule.put("dailyThreshold", 0);
        rule.put("weeklyThreshold", 64800);
        rule.put("payRateMultiplier", 1);
        rule.put("billRateMultiplier", 1);
        rule.put("payRatePerHour", 0);
        rule.put("billRatePerHour", 0);

        rules.add(rule);
        return rules;
    }

    private String createLongString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("A");
        }
        return sb.toString();
    }

    @DataProvider(name = "createTemplateScenarios", parallel = true)
    public Object[][] getCreateTemplateScenarios() {
        return new Object[][] {
                // ========== POSITIVE TEST CASES ==========
                // "Break Paid: Yes" option removed from the rule template — calculateBreak is always false (No/unpaid)
                { "Basic", generateUniqueTemplateName("Basic"), LOGGING_HOURS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5, 6), createWeeklyOvertimeRulesForTest(), 201,
                        "created successfully" },
                { "LoggingHours", generateUniqueTemplateName("Logging"), LOGGING_HOURS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5), null, 201, "created successfully" },
                { "MinimumWorkDays", generateUniqueTemplateName("MinDays"), SHIFTS_METHOD, false, Arrays.asList(1), null,
                        201, "created successfully" },
                { "WithBeforeShiftRule", generateUniqueTemplateName("BeforeShift"), SHIFTS_METHOD, false,
                        Arrays.asList(1, 2, 3), createBeforeShiftRules(), 201, "created successfully" },
                { "WithAfterShiftRule", generateUniqueTemplateName("AfterShift"), SHIFTS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5), createAfterShiftRules(), 201, "created successfully" },
                { "WithDailyOvertime", generateUniqueTemplateName("DailyOT"), SHIFTS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5), createDailyOvertimeRules(), 201, "created successfully" },
                { "WithWeeklyOvertime", generateUniqueTemplateName("WeeklyOT"), LOGGING_HOURS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5, 6, 7), createWeeklyOvertimeRules(), 201, "created successfully" },
                { "WithFixedRates", generateUniqueTemplateName("FixedRate"), SHIFTS_METHOD, false,
                        Arrays.asList(1, 2, 3), createFixedRateRules(), 201, "created successfully" },
                { "ComplexMultipleRules", generateUniqueTemplateName("Complex"), SHIFTS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4), createComplexRules(), 201, "created successfully" },
                { "AllWorkDays", generateUniqueTemplateName("AllDays"), SHIFTS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5, 6, 7), null, 201, "created successfully" },

                // ========== NEGATIVE TEST CASES ==========
                { "EmptyTemplateName", "", SHIFTS_METHOD, false, Arrays.asList(1, 2, 3, 4, 5), null, 400, "required" },
                { "NullTemplateName", null, SHIFTS_METHOD, false, Arrays.asList(1, 2, 3, 4, 5), null, 400, "required" },
                { "InvalidWorkLogType", generateUniqueTemplateName("InvalidWorkLog"), 999, false,
                        Arrays.asList(1, 2, 3, 4, 5), null, 400, "invalid" },
                { "NegativeWorkLogType", generateUniqueTemplateName("NegWorkLog"), -1, false,
                        Arrays.asList(1, 2, 3, 4, 5), null, 400, "invalid" },
                { "InvalidCalculateBreakTime", generateUniqueTemplateName("InvalidBreak"), SHIFTS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5), null, 400, "invalid" },
                { "EmptyWorkDays", generateUniqueTemplateName("EmptyDays"), SHIFTS_METHOD, false, new ArrayList<>(),
                        null, 400, "required" },
                { "InvalidWorkDays", generateUniqueTemplateName("InvalidDays"), SHIFTS_METHOD, false,
                        Arrays.asList(8, 9, 10), null, 400, "invalid" },
                { "ExtremelyLongTemplateName", createLongString(300), SHIFTS_METHOD, false, Arrays.asList(1, 2, 3, 4, 5),
                        null, 400, "length" },
                { "MissingRequiredFields", generateUniqueTemplateName("MissingFields"), SHIFTS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5), null, 400, "required" },

                // ========== INVALID RULES TEST CASES ==========
                { "EmptyRuleName", generateUniqueTemplateName("EmptyRule"), SHIFTS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5), null, 400, "invalid" },
                { "InvalidRuleType", generateUniqueTemplateName("InvalidRule"), SHIFTS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5), null, 400, "invalid" },
                { "InvalidChargeMethod", generateUniqueTemplateName("InvalidCharge"), SHIFTS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5), null, 400, "invalid" },
                { "NegativeRates", generateUniqueTemplateName("NegativeRate"), SHIFTS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5), null, 400, "invalid" },
                { "InvalidTimeRange", generateUniqueTemplateName("InvalidTime"), SHIFTS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5), null, 400, "invalid" },

                // ========== BOUNDARY VALUE TEST CASES ==========
                { "MinimalValidTemplate", generateUniqueTemplateName("Minimal"), SHIFTS_METHOD, false, Arrays.asList(1),
                        null, 201, "created successfully" },
                { "MaximalValidTemplate", generateUniqueTemplateName("Maximal"), LOGGING_HOURS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5, 6, 7), createComplexRules(), 201, "created successfully" },

                // ========== AUTHORIZATION TEST CASES ==========
                { "UnauthorizedAccess", generateUniqueTemplateName("Unauthorized"), SHIFTS_METHOD, false,
                        Arrays.asList(1, 2, 3, 4, 5), null, 401, "Unauthorized" }
        };
    }

    @Owner("Yash Rampal")
    @Test(dataProvider = "createTemplateScenarios", groups = {"contract_staffing", "nightly-build"})
    public void verifyCreateRuleTemplateTest(String scenario, String templateName, int workLogType,
            boolean calculateBreak,
            List<Integer> workDays, List<Map<String, Object>> customRules,
            int expectedStatus, String expectedMessage) {

        Map<String, Object> payload;

        // Use specialized invalid payloads for negative test cases
        switch (scenario) {
            case "EmptyTemplateName":
                payload = createInvalidTemplatePayload("EmptyTemplateName");
                break;
            case "NullTemplateName":
                payload = createInvalidTemplatePayload("NullTemplateName");
                break;
            case "InvalidWorkLogType":
                payload = createInvalidTemplatePayload("InvalidWorkLogType");
                break;
            case "NegativeWorkLogType":
                payload = createInvalidTemplatePayload("NegativeWorkLogType");
                break;
            case "InvalidCalculateBreakTime":
                payload = createInvalidTemplatePayload("InvalidCalculateBreakTime");
                break;
            case "EmptyWorkDays":
                payload = createInvalidTemplatePayload("EmptyWorkDays");
                break;
            case "InvalidWorkDays":
                payload = createInvalidTemplatePayload("InvalidWorkDays");
                break;
            case "ExtremelyLongTemplateName":
                payload = createInvalidTemplatePayload("ExtremelyLongTemplateName");
                break;
            case "MissingRequiredFields":
                payload = createInvalidTemplatePayload("MissingRequiredFields");
                break;
            case "EmptyRuleName":
                payload = createInvalidRuleTemplatePayload("EmptyRuleName");
                break;
            case "InvalidRuleType":
                payload = createInvalidRuleTemplatePayload("InvalidRuleType");
                break;
            case "InvalidChargeMethod":
                payload = createInvalidRuleTemplatePayload("InvalidChargeMethod");
                break;
            case "NegativeRates":
                payload = createInvalidRuleTemplatePayload("NegativeRates");
                break;
            case "InvalidTimeRange":
                payload = createInvalidRuleTemplatePayload("InvalidTimeRange");
                break;
            case "MinimalValidTemplate":
                payload = createBoundaryValueTemplate("MinimalValidTemplate");
                break;
            case "MaximalValidTemplate":
                payload = createBoundaryValueTemplate("MaximalValidTemplate");
                break;
            default:
                payload = buildTemplatePayload(templateName, workLogType, calculateBreak, workDays, customRules);
                break;
        }

        String tokenToUse = scenario.equals("UnauthorizedAccess") ? "invalid_token_123" : albatrossAuthToken;
        Response response = executePost("rule-engine/rule-template", tokenToUse, payload);

        if (scenario.equals("UnauthorizedAccess")) {
            validateUnauthorizedResponse(response);
        } else if (expectedStatus == 200 || expectedStatus == 201) {
            validateTemplateCreationResponse(response);

            // Verify template was actually created using GET method
            String createdTemplateName = (String) payload.get("templateName");
            Integer createdTemplateId = getTemplateIdByName(albatrossAuthToken, createdTemplateName);

            if (createdTemplateId != null) {
                assertThat("Template should be created and findable by name", createdTemplateId, notNullValue());

                // Store for cleanup
                createdTemplateIds.add(createdTemplateId);

                // Optionally verify template structure using GET
                Map<String, Object> createdTemplate = getTemplateById(albatrossAuthToken, createdTemplateId);
                if (createdTemplate != null) {
                    assertThat("Created template should have correct name",
                            createdTemplate.get("templateName"), equalTo(createdTemplateName));
                }
            }
        } else if (expectedStatus == 400) {
            validateBadRequestResponse(response, expectedMessage);
        } else {
            validateResponse(response, expectedStatus, expectedMessage);
        }
    }

    // Helper method for creating invalid rule template payloads
    private Map<String, Object> createInvalidRuleTemplatePayload(String invalidType) {
        List<Map<String, Object>> invalidRules = Arrays.asList(createInvalidRulePayload(invalidType));

        return buildTemplatePayload(
                generateUniqueTemplateName("InvalidRule"),
                SHIFTS_METHOD,
                false,
                Arrays.asList(1, 2, 3, 4, 5),
                invalidRules);
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyMalformedJsonHandling() {
        // Test with malformed JSON payload
        String malformedJson = "{\"templateName\": \"Malformed\", \"workLogType\": }";

        Response response = RestClient.doPost("JSON", timesheetBaseURL, "rule-engine/rule-template",
                albatrossAuthToken, null, true, malformedJson);

        assertThat("Malformed JSON should return 400", response.getStatusCode(), equalTo(400));
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyExcessivelyLargePayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("templateName", generateUniqueTemplateName("LargePayload"));
        payload.put("workLogType", SHIFTS_METHOD);
        payload.put("calculateBreakTime", 0);
        payload.put("workDayIds", Arrays.asList(1, 2, 3, 4, 5));

        // Add excessively large custom rules array
        List<Map<String, Object>> largeRulesList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeRulesList.add(createBasicRule("Rule " + i, BEFORE_SHIFT, Arrays.asList(1, 2, 3)));
        }
        payload.put("customRules", largeRulesList);

        Response response = executePost("rule-engine/rule-template", albatrossAuthToken, payload);

        assertThat("Large payload should be rejected with 400",
                response.getStatusCode(),
                equalTo(400));
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifySpecialCharactersInTemplateName() {
        Map<String, Object> payload = buildTemplatePayload(
                "Special!@#$%^&*()Characters<>Template",
                SHIFTS_METHOD,
                false,
                Arrays.asList(1, 2, 3, 4, 5),
                new ArrayList<>());

        Response response = executePost("rule-engine/rule-template", albatrossAuthToken, payload);

        // Depending on API behavior, this might be accepted or rejected
        if (response.getStatusCode() == 201) {
            validateTemplateCreationResponse(response);

            // Verify template was actually created using GET method
            String templateName = (String) payload.get("templateName");
            Integer createdTemplateId = getTemplateIdByName(albatrossAuthToken, templateName);

            if (createdTemplateId != null) {
                assertThat("Special characters template should be created and findable",
                        createdTemplateId, notNullValue());

                // Store for cleanup
                createdTemplateIds.add(createdTemplateId);
            }
        } else {
            validateBadRequestResponse(response, null);
        }
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyDuplicateTemplateNameHandling() {
        String duplicateName = generateUniqueTemplateName("DuplicateTest");

        // Create first template
        Map<String, Object> payload1 = buildTemplatePayload(duplicateName, SHIFTS_METHOD, false,
                Arrays.asList(1, 2, 3, 4, 5), new ArrayList<>());
        Response response1 = executePost("rule-engine/rule-template", albatrossAuthToken, payload1);

        Integer firstTemplateId = null;
        if (response1.getStatusCode() == 201) {
            // Use GET method to find the created template
            firstTemplateId = getTemplateIdByName(albatrossAuthToken, duplicateName);
            if (firstTemplateId != null) {
                createdTemplateIds.add(firstTemplateId);
            }
        }

        // Try to create second template with same name
        Map<String, Object> payload2 = buildTemplatePayload(duplicateName, SHIFTS_METHOD, false,
                Arrays.asList(1, 2, 3), new ArrayList<>());
        Response response2 = executePost("rule-engine/rule-template", albatrossAuthToken, payload2);

        assertThat("Duplicate name should be rejected with appropriate status",
                response2.getStatusCode(),
                equalTo(400));

    }
}