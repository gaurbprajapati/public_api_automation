package io.recruitcrm.contractStaffing.RuleEngine;

import com.qa.api.util.RuleTemplatePayloadUtils;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class RuleTemplateListTests extends RuleEngineBaseTest {

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

        Integer template1 = createAndFetchTemplateId(authToken, "ListTestFirst");
        if (template1 != null) {
            createdTemplateIds.add(template1);
        }

        Integer template2 = createAndFetchComplexTemplateId(authToken, "ListTestSecond");
        if (template2 != null) {
            createdTemplateIds.add(template2);
        }

        Integer template3 = createAndFetchTemplateId(authToken, "ListTestThird");
        if (template3 != null) {
            createdTemplateIds.add(template3);
        }

        Integer template4 = createAndFetchComplexTemplateId(authToken, "ListTestFourth");
        if (template4 != null) {
            createdTemplateIds.add(template4);
        }
    }

    @AfterClass
    public void tearDown() {
        cleanupTemplates(authToken, createdTemplateIds);
    }

    @DataProvider(name = "listTemplateScenarios", parallel = true)
    public Object[][] getListTemplateScenarios() {
        return new Object[][] {
                { "EmptySearch", "", 1, 10, 200 },
                { "SimpleSearch", faker.lorem().word(), 1, 10, 200 },
                { "IndustrySearch", faker.company().industry(), 1, 5, 200 },
                // {"NonExistentSearch", "nonexistent" + faker.number().randomNumber(), 1, 10,
                // 404},
                { "UnauthorizedAccess", "", 1, 10, 401 }
        };
    }

    @Owner("Yash Rampal")
    @Test(dataProvider = "listTemplateScenarios", groups = {"contract_staffing", "nightly-build"})
    public void verifyListTemplatesTest(String scenario, String search, int page, int size, int expectedStatus) {

        Map<String, String> queryParams = RuleTemplatePayloadUtils.getListParams(search, page, size);
        String tokenToUse = scenario.equals("UnauthorizedAccess") ? "invalid_token_123" : authToken;
        Response response = executeGet("rule-engine/rule-template/list", tokenToUse, queryParams);

        if (scenario.equals("UnauthorizedAccess")) {
            validateUnauthorizedResponse(response);
        } else if (expectedStatus == 200) {
            validateTemplateListResponse(response);

            List<Map<String, Object>> data = getResponseData(response);
            if (!search.isEmpty() && !data.isEmpty()) {
                data.forEach(template -> {
                    String templateName = (String) template.get("templateName");
                    assertThat("Template name should contain search term",
                            templateName.toLowerCase(), containsString(search.toLowerCase()));
                });
            }

            assertThat("Page size should not exceed requested", data.size(), lessThanOrEqualTo(size));
        } else {
            assertThat("Response status", response.getStatusCode(), equalTo(expectedStatus));
        }
    }
}