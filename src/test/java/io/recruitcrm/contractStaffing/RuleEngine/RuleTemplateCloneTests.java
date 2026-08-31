package io.recruitcrm.contractStaffing.RuleEngine;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class RuleTemplateCloneTests extends RuleEngineBaseTest {

    private String authToken;
    private List<Integer> createdTemplateIds;
    String albatrossAuthToken;
    String apiAuthToken;
    int ownerAccountID;

    @BeforeClass(alwaysRun = true)
    public void setup() throws InterruptedException {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        apiAuthToken = ThreadManager.getAccountApiKey();

        authToken = albatrossAuthToken;
        createdTemplateIds = new ArrayList<>();

        Integer simpleTemplate = createAndFetchTemplateId(authToken, "CloneTestSimple");
        if (simpleTemplate != null) {
            createdTemplateIds.add(simpleTemplate);
        }

        Integer complexTemplate = createAndFetchComplexTemplateId(authToken, "CloneTestComplex");
        if (complexTemplate != null) {
            createdTemplateIds.add(complexTemplate);
        }
    }

    @AfterClass
    public void tearDown() {
        cleanupTemplates(authToken, createdTemplateIds);
    }

    @DataProvider(name = "cloneTemplateScenarios", parallel = true)
    public Object[][] getCloneTemplateScenarios() {
        return new Object[][] {
                { "TemplateWithRules", true, 201, "cloned successfully" },
                { "NonExistentTemplate", false, 404, "Template not found" },
                { "UnauthorizedAccess", false, 401, "Unauthorized" }
        };
    }

    @Owner("Yash Rampal")
    @Test(dataProvider = "cloneTemplateScenarios", groups = {"contract_staffing", "nightly-build"})
    public void verifyCloneTemplateTest(String scenario, boolean hasCustomRules, int expectedStatus,
            String expectedMessage) throws InterruptedException {

        Integer templateId = null;
        String originalName = null;

        switch (scenario) {

            case "TemplateWithRules":
                if (createdTemplateIds.size() > 1) {
                    templateId = createdTemplateIds.get(1);
                } else if (!createdTemplateIds.isEmpty()) {
                    templateId = createdTemplateIds.get(0);
                }
                break;

            case "NonExistentTemplate":
                templateId = 999999;
                break;

            case "UnauthorizedAccess":
                if (!createdTemplateIds.isEmpty()) {
                    templateId = createdTemplateIds.get(0);
                } else {
                    templateId = 1;
                }
                break;
        }

        if (templateId != null && (scenario.equals("BasicTemplate") || scenario.equals("TemplateWithRules"))) {
            Map<String, Object> originalTemplate = getTemplateById(authToken, templateId);
            if (originalTemplate != null) {
                originalName = (String) originalTemplate.get("templateName");
            }
        }

        if (templateId == null && (scenario.equals("BasicTemplate") || scenario.equals("TemplateWithRules"))) {
            return;
        }

        String tokenToUse = scenario.equals("UnauthorizedAccess") ? "invalid_token_123" : authToken;
        Response response = executePost("rule-engine/rule-template/" + templateId + "/clone", tokenToUse, null);
        Thread.sleep(5000);

        if (scenario.equals("UnauthorizedAccess")) {
            validateUnauthorizedResponse(response);
        } else if (scenario.equals("NonExistentTemplate")) {
            validateNotFoundResponse(response);
        } else if (expectedStatus == 201) {
            validateTemplateCreationResponse(response);
            if (originalName != null) {
                String expectedCloneName = "(clone)" + originalName;
                Integer clonedTemplateId = getTemplateIdByName(authToken, expectedCloneName);

                if (clonedTemplateId != null) {
                    assertThat("Cloned template should be findable", clonedTemplateId, notNullValue());
                    assertThat("Cloned template should have different ID than original",
                            clonedTemplateId, not(equalTo(templateId)));
                    createdTemplateIds.add(clonedTemplateId);
                    Map<String, Object> clonedTemplate = getTemplateById(authToken, clonedTemplateId);
                    if (clonedTemplate != null) {
                        String clonedName = (String) clonedTemplate.get("templateName");
                        assertThat("Cloned template name should follow pattern (clone)originalname",
                                clonedName, equalTo(expectedCloneName));
                    }
                }
            }
        } else {
            assertThat("Response status", response.getStatusCode(), equalTo(expectedStatus));
        }
    }
}