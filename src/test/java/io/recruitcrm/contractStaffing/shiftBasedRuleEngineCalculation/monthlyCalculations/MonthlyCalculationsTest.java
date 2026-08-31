package io.recruitcrm.contractStaffing.shiftBasedRuleEngineCalculation.monthlyCalculations;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.shiftBasedRuleEngineCalculation.MultipleTimeEntryBaseTest;
import io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader.TestSuite.SHIFT_MONTHLY;
import com.qa.api.util.Owner;

/**
 * Monthly calculations test: runs full flow (template → entities → timesheet → multi-entry update → evaluate)
 * driven by MonthlyCalculationsTest.json test data.
 *
 * Key differences from weekly/biweekly:
 * - Monthly timesheet frequency (timesheetFrequency=4)
 * - No weekly overtime (WOT is not applicable for monthly timesheets)
 * - Daily OT, BS, AS, SR and break rules still apply at daily level
 * - Full month spans ~4-5 weeks; partial last week handled normally
 */
@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public final class MonthlyCalculationsTest extends MultipleTimeEntryBaseTest {

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
    @Test(dataProvider = "monthlyDataProvider")
    public void verifyMonthlyCalculations(Map<String, Object> scenario) {
        executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "monthlyDataProvider", parallel = true)
    public Object[][] provideMonthlyTestData() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(SHIFT_MONTHLY);
        Object[][] data = new Object[scenarios.size()][1];
        for (int i = 0; i < scenarios.size(); i++) {
            data[i][0] = scenarios.get(i);
        }
        return data;
    }
}
