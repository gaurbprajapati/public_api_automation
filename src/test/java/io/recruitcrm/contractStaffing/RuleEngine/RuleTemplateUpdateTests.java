package io.recruitcrm.contractStaffing.RuleEngine;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;
import java.util.Arrays;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class RuleTemplateUpdateTests extends RuleEngineBaseTest {

    private String authToken;
    private Integer templateId;
    private List<Integer> createdTemplateIds = new ArrayList<>();
    String albatrossAuthToken;
    String apiAuthToken;
    int ownerAccountID;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws InterruptedException {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        apiAuthToken = ThreadManager.getAccountApiKey();

        authToken = albatrossAuthToken;

        Integer updateTemplate1 = createAndFetchTemplateId(authToken, "UpdateTestFirst");
        if (updateTemplate1 != null) {
            createdTemplateIds.add(updateTemplate1);
            templateId = updateTemplate1;
        }

        Integer updateTemplate2 = createAndFetchComplexTemplateId(authToken, "UpdateTestSecond");
        if (updateTemplate2 != null) {
            createdTemplateIds.add(updateTemplate2);
        }
    }

    @AfterClass
    public void tearDown() {
        cleanupTemplates(authToken, createdTemplateIds);
    }

    @DataProvider(name = "updateTemplateScenarios", parallel = true)
    public Object[][] getUpdateTemplateScenarios() {
        return new Object[][] {
                { "ValidUpdate", 200, "updated successfully" },
                { "NonExistentTemplate", 400, "Template not found" },
                { "UnauthorizedAccess", 401, "Unauthorized" }
        };
    }

    @Owner("Yash Rampal")
    @Test(dataProvider = "updateTemplateScenarios", groups = {"contract_staffing", "nightly-build"})
    public void verifyUpdateRuleTemplateTest(String scenario, int expectedStatus, String expectedMessage) {
        Integer updateTemplateId = null;

        switch (scenario) {
            case "ValidUpdate":
                if (templateId != null) {
                    updateTemplateId = templateId;
                } else {
                    return;
                }
                break;
            case "NonExistentTemplate":
                updateTemplateId = 999999;
                break;
            case "UnauthorizedAccess":
                if (templateId != null) {
                    updateTemplateId = templateId;
                } else {
                    updateTemplateId = 1;
                }
                break;
        }

        String updatedName = "Updated Template Name " + System.currentTimeMillis();
        Map<String, Object> updatePayload = buildTemplatePayload(
                updatedName,
                SHIFTS_METHOD,
                false,
                Arrays.asList(1, 2, 3, 4, 5),
                new ArrayList<>());
        updatePayload.put("id", updateTemplateId);

        String tokenToUse = scenario.equals("UnauthorizedAccess") ? "invalid_token_123" : albatrossAuthToken;
        Response response = executePatch("rule-engine/rule-template/" + updateTemplateId, tokenToUse, updatePayload);

        if (scenario.equals("UnauthorizedAccess")) {
            validateUnauthorizedResponse(response);
        } else if (scenario.equals("NonExistentTemplate")) {
            validateBadRequestResponse(response, expectedMessage);
        } else if (expectedStatus == 200) {
            validateSingleItemResponse(response);

            Map<String, Object> updatedTemplate = getTemplateById(albatrossAuthToken, updateTemplateId);

            if (updatedTemplate != null) {
                assertThat("Updated template should be retrievable", updatedTemplate, notNullValue());
                assertThat("Template name should be updated",
                        updatedTemplate.get("templateName"), equalTo(updatedName));

                assertThat("Template ID should remain unchanged",
                        (Integer) updatedTemplate.get("id"), equalTo(updateTemplateId));
            }
        } else {
            assertThat("Response status", response.getStatusCode(), equalTo(expectedStatus));
        }
    }
}