package io.recruitcrm.contractStaffing.shiftBasedRuleEngineCalculation.weeklyCalculations.testClass;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.shiftBasedRuleEngineCalculation.MultipleTimeEntryBaseTest;
import io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader.TestSuite.SHIFT_FOUR_RULE;
import com.qa.api.util.Owner;

/**
 * Four-rule test: runs full flow (template → entities → timesheet → multi-entry update → evaluate)
 * driven by FourRuleTest.json test data.
 */
@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public final class FourRuleTest extends MultipleTimeEntryBaseTest {

    private List<Integer> createdTemplateIds = new ArrayList<>();
    String albatrossAuthToken;
    String apiAuthToken;
    commanFunction function;

    @BeforeClass
    public void Setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        function = new commanFunction();
    }

    @Owner("Yash Rampal")
    @Test(dataProvider = "fourRuleDataProvider")
    public void verifyFourRuleMultipleTimeEntry(Map<String, Object> scenario) {
        executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "fourRuleDataProvider",parallel = true)
    public Object[][] provideFourRuleTestData() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(SHIFT_FOUR_RULE);
        Object[][] data = new Object[scenarios.size()][1];
        for (int i = 0; i < scenarios.size(); i++) {
            data[i][0] = scenarios.get(i);
        }
        return data;
    }
}
