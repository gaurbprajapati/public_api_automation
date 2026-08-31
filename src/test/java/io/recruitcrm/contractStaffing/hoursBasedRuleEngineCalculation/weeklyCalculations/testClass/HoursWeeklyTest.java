package io.recruitcrm.contractStaffing.hoursBasedRuleEngineCalculation.weeklyCalculations.testClass;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.hoursBasedRuleEngineCalculation.RuleEngineCalculationBase;
import io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader.TestSuite.*;

/**
 * Weekly hours-based test: runs full flow (template → entities → timesheet → update → evaluate)
 * driven by ALL weekly JSON files:
 *   - DailyOTHoursTest.json        (daily overtime hours-based scenarios)
 *   - WeeklyOTHoursTest.json       (weekly overtime hours-based scenarios)
 *   - DoubleRuleHoursTest.json     (DailyOT + WeeklyOT combination scenarios)
 *   - UnallocatedHoursTest.json    (unallocated hours with isUnplannedHoursPayEnabled=1)
 *   - SpecificHoursRangeTest.json  (specific hours range scenarios)
 */
@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public final class HoursWeeklyTest extends RuleEngineCalculationBase {

    private final List<Integer> createdTemplateIds = new ArrayList<>();
    private String albatrossAuthToken;
    private String apiAuthToken;
    private commanFunction function;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        function = new commanFunction();
    }

    @Test(dataProvider = "hoursWeeklyDataProvider")
    public void verifyHoursWeeklyCalculation(Map<String, Object> scenario) {
        executeHoursBasedTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "hoursWeeklyDataProvider", parallel = true)
    public Object[][] provideHoursWeeklyTestData() {
        List<Map<String, Object>> allScenarios = new ArrayList<>();
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_DAILY_OT));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_WEEKLY_OT));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_DOUBLE_RULE));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_UNALLOCATED));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_SPECIFIC_RANGE));

        Object[][] data = new Object[allScenarios.size()][1];
        for (int i = 0; i < allScenarios.size(); i++) {
            data[i][0] = allScenarios.get(i);
        }
        return data;
    }
}
