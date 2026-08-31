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

import static io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader.TestSuite.*;
import com.qa.api.util.Owner;

/**
 * Single-rule test: runs full flow (template → entities → timesheet → multi-entry update → evaluate)
 * driven by ALL single-rule JSON files:
 *   - SingleRuleTest.json          (base single-rule scenarios)
 *   - DailyOvertimeRuleTest.json   (daily overtime single-rule scenarios)
 *   - WeeklyOvertimeTest.json      (weekly overtime single-rule scenarios)
 *   - SpecificRangeRuleTest.json   (specific range single-rule scenarios)
 */
@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public final class SingleRuleTest extends MultipleTimeEntryBaseTest {

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
    @Test(dataProvider = "singleRuleDataProvider")
    public void verifySingleRuleMultipleTimeEntry(Map<String, Object> scenario) {
        executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "singleRuleDataProvider" , parallel = true)
    public Object[][] provideSingleRuleTestData() {
        List<Map<String, Object>> allScenarios = new ArrayList<>();
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_SINGLE_RULE));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_DAILY_OT));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_WEEKLY_OT));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_SPECIFIC_RANGE));

        Object[][] data = new Object[allScenarios.size()][1];
        for (int i = 0; i < allScenarios.size(); i++) {
            data[i][0] = allScenarios.get(i);
        }
        return data;
    }
}
