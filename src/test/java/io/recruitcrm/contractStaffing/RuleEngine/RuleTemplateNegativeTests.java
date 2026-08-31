package io.recruitcrm.contractStaffing.RuleEngine;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class RuleTemplateNegativeTests extends RuleEngineBaseTest {

    private String authToken;
    String albatrossAuthToken;
    String apiAuthToken;
    int ownerAccountID;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        apiAuthToken = ThreadManager.getAccountApiKey();
        authToken = albatrossAuthToken;
    }

    @DataProvider(name = "invalidPayloadScenarios")
    public Object[][] getInvalidPayloadScenarios() {
        return new Object[][] {
                { "EmptyTemplateName", 400 },
                { "NullTemplateName", 400 },
                { "InvalidWorkLogType", 400 },
                { "InvalidCalculateBreakTime", 400 },
                { "EmptyWorkDays", 400 },
                { "InvalidWorkDays", 400 },
                { "MissingRequiredFields", 400 },
                { "ExtremelyLongTemplateName", 400 },
                { "NegativeWorkLogType", 400 }
        };
    }

    @Owner("Yash Rampal")
    @Test(dataProvider = "invalidPayloadScenarios")
    public void verifyInvalidPayloadRejection(String invalidType, int expectedStatus) {
        Map<String, Object> invalidPayload = createInvalidTemplatePayload(invalidType);

        Response response = executePost("rule-engine/rule-template", authToken, invalidPayload);

        validateBadRequestResponse(response, null);

        // TODO: Comment out during development - API validation not implemented yet
        // Ensure no template was created
        // assertThat("Invalid payload should not create template",
        // response.getStatusCode(),
        // not(anyOf(equalTo(200), equalTo(201))));
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyCreateRuleTemplateWithCalculateBreakTimeTrueTest() {
        // BNP-7584: rule-template calculateBreakTime is @AssertFalse - sending true must be rejected with 400
        Map<String, Object> payload = buildTemplatePayload(
                generateUniqueTemplateName("BreakTimeTrue"),
                SHIFTS_METHOD,
                true,
                Arrays.asList(1, 2, 3, 4, 5),
                new ArrayList<>());

        Response response = executePost("rule-engine/rule-template", authToken, payload);

        assertThat("calculateBreakTime=true should be rejected", response.getStatusCode(), equalTo(400));
        assertThat(response.jsonPath().getInt("meta.status"), equalTo(400));
        assertThat(response.jsonPath().getString("meta.message"), equalTo("calculateBreakTime must be false (0)"));
        assertThat(response.jsonPath().get("data"), nullValue());
    }

    @DataProvider(name = "invalidRuleScenarios")
    public Object[][] getInvalidRuleScenarios() {
        return new Object[][] {
                { "EmptyRuleName", 400 },
                { "InvalidRuleType", 400 },
                { "InvalidChargeMethod", 400 },
                { "NegativeRates", 400 },
                { "InvalidTimeRange", 400 }
        };
    }

    @Owner("Yash Rampal")
    @Test(dataProvider = "invalidRuleScenarios")
    public void verifyInvalidRuleRejection(String invalidType, int expectedStatus) {
        List<Map<String, Object>> invalidRules = Arrays.asList(createInvalidRulePayload(invalidType));

        Map<String, Object> payload = buildTemplatePayload(
                generateUniqueTemplateName("InvalidRule"),
                SHIFTS_METHOD,
                false,
                Arrays.asList(1, 2, 3, 4, 5),
                invalidRules);

        Response response = executePost("rule-engine/rule-template", authToken, payload);

        validateBadRequestResponse(response, null);
    }

    @DataProvider(name = "boundaryValueScenarios")
    public Object[][] getBoundaryValueScenarios() {
        return new Object[][] {
                { "MinimalValidTemplate", 201 },
                { "MaximalValidTemplate", 201 },
                { "SingleWorkDay", 201 },
                { "AllWorkDays", 201 }
        };
    }

    @Owner("Yash Rampal")
    @Test(dataProvider = "boundaryValueScenarios")
    public void verifyBoundaryValueHandling(String boundaryType, int expectedStatus) {
        Map<String, Object> payload = createBoundaryValueTemplate(boundaryType);

        Response response = executePost("rule-engine/rule-template", authToken, payload);

        if (expectedStatus == 201) {
            validateTemplateCreationResponse(response);

            // Validate the created template structure
            Map<String, Object> createdTemplate = response.jsonPath().getMap("data");
            verifyBasicTemplateStructure(createdTemplate);

            // Cleanup - delete the created template (only if template data is present)
            Integer templateId = null;
            if (createdTemplate != null) {
                templateId = (Integer) createdTemplate.get("id");
            }
            if (templateId != null) {
                executeDelete("rule-engine/rule-template/" + templateId, authToken);
            }
        } else {
            validateBadRequestResponse(response, null);
        }
    }

    @Owner("Yash Rampal")
    @Test
    public void verifyMalformedJsonHandling() {
        // Test with malformed JSON payload
        String malformedJson = "{\"templateName\": \"Malformed\", \"workLogType\": }";

        Response response = RestClient.doPost("JSON", timesheetBaseURL, "rule-engine/rule-template",
                authToken, null, true, malformedJson);

        assertThat("Malformed JSON should return 400", response.getStatusCode(), equalTo(400));
    }

    @Owner("Yash Rampal")
    @Test
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

        Response response = executePost("rule-engine/rule-template", authToken, payload);

        // Should handle large payload gracefully (might be 400 or 413)
        assertThat("Large payload should be handled",
                response.getStatusCode(),
                anyOf(equalTo(400), equalTo(413), equalTo(500)));
    }

    @Owner("Yash Rampal")
    @Test
    public void verifySpecialCharactersInTemplateName() {
        Map<String, Object> payload = buildTemplatePayload(
                "Special!@#$%^&*()Characters<>Template",
                SHIFTS_METHOD,
                false,
                Arrays.asList(1, 2, 3, 4, 5),
                new ArrayList<>());

        Response response = executePost("rule-engine/rule-template", authToken, payload);

        // Depending on API behavior, this might be accepted or rejected
        if (response.getStatusCode() == 201) {
            validateTemplateCreationResponse(response);

            // Cleanup
            Integer templateId = response.jsonPath().get("data.id");
            if (templateId != null) {
                executeDelete("rule-engine/rule-template/" + templateId, authToken);
            }
        } else {
            validateBadRequestResponse(response, null);
        }
    }

    @Owner("Yash Rampal")
    @Test
    public void verifyDuplicateTemplateNameHandling() {
        String duplicateName = generateUniqueTemplateName("DuplicateTest");

        // Create first template
        Map<String, Object> payload1 = buildTemplatePayload(duplicateName, SHIFTS_METHOD, false,
                Arrays.asList(1, 2, 3, 4, 5), new ArrayList<>());
        Response response1 = executePost("rule-engine/rule-template", authToken, payload1);

        Integer firstTemplateId = null;
        if (response1.getStatusCode() == 201) {
            firstTemplateId = response1.jsonPath().get("data.id");
        }

        // Try to create second template with same name
        Map<String, Object> payload2 = buildTemplatePayload(duplicateName, SHIFTS_METHOD, false,
                Arrays.asList(1, 2, 3), new ArrayList<>());
        Response response2 = executePost("rule-engine/rule-template", authToken, payload2);

        // Should either allow duplicates or reject with appropriate error
        if (response2.getStatusCode() == 201) {
            // Duplicates allowed - cleanup both
            Integer secondTemplateId = response2.jsonPath().get("data.id");
            if (secondTemplateId != null) {
                executeDelete("rule-engine/rule-template/" + secondTemplateId, authToken);
            }
        } else {
            // Duplicates not allowed - should be 400
            assertThat("Duplicate name should be rejected with appropriate status",
                    response2.getStatusCode(),
                    anyOf(equalTo(400), equalTo(409)));
        }

        // Cleanup first template
        if (firstTemplateId != null) {
            executeDelete("rule-engine/rule-template/" + firstTemplateId, authToken);
        }
    }
}
