package io.recruitcrm.contractStaffing.hoursBasedRuleEngineCalculation.biweeklyCalculations;

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

import static io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader.TestSuite.HOURS_BIWEEKLY;

/**
 * Biweekly hours-based test: runs full flow (template → entities → timesheet → update → evaluate)
 * driven by BiweeklyHoursCalculationsTest.json test data.
 *
 * Key differences from weekly:
 * - Two weeks of data (Week1 and Week2)
 * - Weekly OT calculated separately for each week
 * - Total amounts sum across both weeks
 */
@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public final class BiweeklyHoursCalculationsTest extends RuleEngineCalculationBase {

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

    @Test(dataProvider = "biweeklyHoursDataProvider")
    public void verifyBiweeklyHoursCalculation(Map<String, Object> scenario) {
        executeHoursBasedTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "biweeklyHoursDataProvider", parallel = true)
    public Object[][] provideBiweeklyHoursTestData() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(HOURS_BIWEEKLY);
        Object[][] data = new Object[scenarios.size()][1];
        for (int i = 0; i < scenarios.size(); i++) {
            data[i][0] = scenarios.get(i);
        }
        return data;
    }
}
