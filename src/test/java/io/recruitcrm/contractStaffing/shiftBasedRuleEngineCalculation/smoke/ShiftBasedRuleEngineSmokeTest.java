package io.recruitcrm.contractStaffing.shiftBasedRuleEngineCalculation.smoke;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.shiftBasedRuleEngineCalculation.MultipleTimeEntryBaseTest;
import io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader.TestSuite.*;
import com.qa.api.util.Owner;

/**
 * Subset of shift-based rule engine multiple-time-entry flows: up to five JSON scenarios per
 * original suite (single / double / … / monthly) for quick smoke runs.
 */
@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public final class ShiftBasedRuleEngineSmokeTest extends MultipleTimeEntryBaseTest {

    private static final int SMOKE_SCENARIO_LIMIT = 5;

    private final List<Integer> createdTemplateIds= new ArrayList<>();
    private String albatrossAuthToken;
    private String apiAuthToken;
    private commanFunction function;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        function = new commanFunction();
    }

    private static Object[][] toProviderRows(List<Map<String, Object>> scenarios) {
        Object[][] data = new Object[scenarios.size()][1];
        for (int i = 0; i < scenarios.size(); i++) {
            data[i][0] = scenarios.get(i);
        }
        return data;
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "smokeSingleRuleDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void smokeVerifySingleRuleMultipleTimeEntry(Map<String, Object> scenario) {
        executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "smokeSingleRuleDataProvider", parallel = true)
    public Object[][] smokeSingleRuleDataProvider() {
        List<Map<String, Object>> all = new ArrayList<>();
        all.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_SINGLE_RULE));
        all.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_DAILY_OT));
        all.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_WEEKLY_OT));
        all.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_SPECIFIC_RANGE));
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(all, SMOKE_SCENARIO_LIMIT));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "smokeDoubleRuleDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void smokeVerifyDoubleRuleMultipleTimeEntry(Map<String, Object> scenario) {
        executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "smokeDoubleRuleDataProvider", parallel = true)
    public Object[][] smokeDoubleRuleDataProvider() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(SHIFT_DOUBLE_RULE);
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(scenarios, SMOKE_SCENARIO_LIMIT));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "smokeThreeRuleDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void smokeVerifyThreeRuleMultipleTimeEntry(Map<String, Object> scenario) {
        executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "smokeThreeRuleDataProvider", parallel = true)
    public Object[][] smokeThreeRuleDataProvider() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(SHIFT_THREE_RULE);
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(scenarios, SMOKE_SCENARIO_LIMIT));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "smokeFourRuleDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void smokeVerifyFourRuleMultipleTimeEntry(Map<String, Object> scenario) {
        executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "smokeFourRuleDataProvider", parallel = true)
    public Object[][] smokeFourRuleDataProvider() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(SHIFT_FOUR_RULE);
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(scenarios, SMOKE_SCENARIO_LIMIT));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "smokeFiveRuleDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void smokeVerifyFiveRuleMultipleTimeEntry(Map<String, Object> scenario) {
        executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "smokeFiveRuleDataProvider", parallel = true)
    public Object[][] smokeFiveRuleDataProvider() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(SHIFT_FIVE_RULE);
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(scenarios, SMOKE_SCENARIO_LIMIT));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "smokeNoRuleDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void smokeVerifyNoRuleMultipleTimeEntry(Map<String, Object> scenario) {
        executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "smokeNoRuleDataProvider", parallel = true)
    public Object[][] smokeNoRuleDataProvider() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(SHIFT_NO_RULE);
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(scenarios, SMOKE_SCENARIO_LIMIT));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "smokeBiweeklyDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void smokeVerifyBiweeklyCalculations(Map<String, Object> scenario) {
        executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "smokeBiweeklyDataProvider", parallel = true)
    public Object[][] smokeBiweeklyDataProvider() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(SHIFT_BIWEEKLY);
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(scenarios, SMOKE_SCENARIO_LIMIT));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "smokeMonthlyDataProvider", groups = {"contract_staffing", "nightly-build"})
    public void smokeVerifyMonthlyCalculations(Map<String, Object> scenario) {
        executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "smokeMonthlyDataProvider", parallel = true)
    public Object[][] smokeMonthlyDataProvider() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(SHIFT_MONTHLY);
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(scenarios, SMOKE_SCENARIO_LIMIT));
    }

    @Test(dataProvider = "smokeUnallocatedHoursDataProvider")
    public void smokeVerifyUnallocatedHoursCalculations(Map<String, Object> scenario) {
        executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "smokeUnallocatedHoursDataProvider", parallel = true)
    public Object[][] smokeUnallocatedHoursDataProvider() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(SHIFT_UNALLOCATED);
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(scenarios, SMOKE_SCENARIO_LIMIT));
    }
}
