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
public class RuleTemplateDeleteTests extends RuleEngineBaseTest {
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
        Integer template1 = createAndFetchTemplateId(authToken, "DeleteTestOne");
        if (template1 != null) {
            createdTemplateIds.add(template1);
        }

        Integer template2 = createAndFetchComplexTemplateId(authToken, "DeleteTestTwo");
        if (template2 != null) {
            createdTemplateIds.add(template2);
        }

        Integer template3 = createAndFetchTemplateId(authToken, "DeleteTestThree");
        if (template3 != null) {
            createdTemplateIds.add(template3);
        }
    }

    @AfterClass
    public void tearDown() {
        cleanupTemplates(albatrossAuthToken, createdTemplateIds);
    }

    @DataProvider(name = "deleteTemplateScenarios", parallel = true)
    public Object[][] getDeleteTemplateScenarios() {
        return new Object[][] {
                { "ValidTemplate", 200, "deleted successfully" },
                { "NonExistentTemplate", 404, "Template not found" },
                { "UnauthorizedAccess", 401, "Unauthorized" }
        };
    }

    @Owner("Yash Rampal")
    @Test(dataProvider = "deleteTemplateScenarios", groups = {"contract_staffing", "nightly-build"})
    public void verifyDeleteTemplateTest(String scenario, int expectedStatus, String expectedMessage) {

        Integer templateId = null;

        switch (scenario) {
            case "ValidTemplate":
                if (!createdTemplateIds.isEmpty()) {
                    templateId = createdTemplateIds.get(0);
                    createdTemplateIds.remove(0);
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

        String tokenToUse = scenario.equals("UnauthorizedAccess") ? "invalid_token_123" : albatrossAuthToken;
        Response response = executeDelete("rule-engine/rule-template/" + templateId, tokenToUse);
        assertThat("Response status", response.getStatusCode(), equalTo(expectedStatus));

        if (expectedStatus == 200 && scenario.equals("ValidTemplate")) {
            Map<String, Object> meta = getResponseMeta(response);
            assertThat("Success message", meta.get("message").toString(), containsString(expectedMessage));

            Response getResponse = executeGet("rule-engine/rule-template/" + templateId, albatrossAuthToken, null);
            assertThat("Deleted template should not exist", getResponse.getStatusCode(), equalTo(404));
        }
    }
}
