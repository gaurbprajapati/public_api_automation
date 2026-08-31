package io.recruitcrm.contractStaffing.RuleEngine;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class RuleTemplateGetTests extends RuleEngineBaseTest {

    private String authToken;
    private List<Integer> createdTemplateIds = new ArrayList<>();
    String albatrossAuthToken;
    String apiAuthToken;
    int ownerAccountID;

    @BeforeClass(alwaysRun = true)
    public void setup() throws InterruptedException {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        apiAuthToken = ThreadManager.getAccountApiKey();

        authToken = albatrossAuthToken;

        Integer simpleTemplateId = createAndFetchTemplateId(authToken, "GetTestSimple");
        if (simpleTemplateId != null) {
            createdTemplateIds.add(simpleTemplateId);
        }

        Integer complexTemplateId = createAndFetchComplexTemplateId(authToken, "GetTestComplex");
        if (complexTemplateId != null) {
            createdTemplateIds.add(complexTemplateId);
        }

        Integer basicTemplateId = createAndFetchTemplateId(authToken, "GetTestBasic");
        if (basicTemplateId != null) {
            createdTemplateIds.add(basicTemplateId);
        }

    }

    @AfterClass
    public void tearDown() {
        cleanupTemplates(authToken, createdTemplateIds);
    }

    @DataProvider(name = "getTemplateScenarios", parallel = true)
    public Object[][] getGetTemplateScenarios() {
        return new Object[][] {
                { "ValidTemplate", null, 200, true },
                { "NonExistentTemplate", 999999, 404, false },
                { "InvalidId", -1, 404, false },
                { "ZeroId", 0, 404, false },
                { "UnauthorizedAccess", null, 401, false }
        };
    }

    @Owner("Yash Rampal")
    @Test(dataProvider = "getTemplateScenarios", groups = {"contract_staffing", "nightly-build"})
    public void verifyGetTemplateByIdTest(String scenario, Integer providedId, int expectedStatus,
            boolean validateStructure) {

        Integer templateId = providedId;

        switch (scenario) {
            case "ValidTemplate":
                if (!createdTemplateIds.isEmpty()) {
                    templateId = createdTemplateIds.get(0);
                }
                break;
            case "NonExistentTemplate":
                templateId = 999999;
                break;
            case "InvalidId":
                templateId = -1;
                break;
            case "ZeroId":
                templateId = 0;
                break;
            case "UnauthorizedAccess":
                if (!createdTemplateIds.isEmpty()) {
                    templateId = createdTemplateIds.get(0);
                } else {
                    templateId = 1;
                }
                break;
        }

        if (templateId == null && scenario.equals("ValidTemplate")) {
            return;
        }

        String tokenToUse = scenario.equals("UnauthorizedAccess") ? "invalid_token_123" : authToken;
        Response response = executeGet("rule-engine/rule-template/" + templateId, tokenToUse, null);

        if (scenario.equals("UnauthorizedAccess")) {
            validateUnauthorizedResponse(response);
        } else if (scenario.equals("InvalidTemplate")) {
            validateNotFoundResponse(response);
        } else if (expectedStatus == 200) {
            validateSingleItemResponse(response);

            if (validateStructure) {
                Map<String, Object> data = response.jsonPath().getMap("data");
                verifyBasicTemplateStructure(data);

                if (data.containsKey("customRules") && data.get("customRules") != null) {
                    List<Map<String, Object>> rules = (List<Map<String, Object>>) data.get("customRules");
                    if (rules != null && !rules.isEmpty()) {
                        rules.forEach(this::verifyRuleStructure);
                    }
                }
            }
        } else {
            assertThat("Response status", response.getStatusCode(), equalTo(expectedStatus));
        }
    }
}