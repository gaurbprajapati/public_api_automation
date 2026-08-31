package io.recruitcrm.contractStaffing.hoursBasedRuleEngineCalculation.monthlyCalculations;

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

import static io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader.TestSuite.HOURS_MONTHLY;

/**
 * Monthly hours-based test: runs full flow (template → entities → timesheet → update → evaluate)
 * driven by MonthlyHoursCalculationsTest.json test data.
 *
 * Key differences from weekly/biweekly:
 * - Monthly timesheet frequency (timesheetFrequency=4)
 * - Full month spans ~4-5 weeks; partial last week handled normally
 * - Daily OT and Specific Hours Range rules apply at daily level
 */
@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public final class MonthlyHoursCalculationsTest extends RuleEngineCalculationBase {

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

    @Test(dataProvider = "monthlyHoursDataProvider")
    public void verifyMonthlyHoursCalculation(Map<String, Object> scenario) {
        executeHoursBasedTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "monthlyHoursDataProvider", parallel = true)
    public Object[][] provideMonthlyHoursTestData() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(HOURS_MONTHLY);
        Object[][] data = new Object[scenarios.size()][1];
        for (int i = 0; i < scenarios.size(); i++) {
            data[i][0] = scenarios.get(i);
        }
        return data;
    }
}
