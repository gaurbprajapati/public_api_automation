package io.recruitcrm.contractStaffing.hoursBasedRuleEngineCalculation.smoke;

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
 * Subset of hours-based rule engine flows: up to {@code SMOKE_SCENARIO_LIMIT} JSON scenarios
 * per original suite (DailyOT / WeeklyOT / SpecificHoursRange / Biweekly / Monthly)
 * for quick smoke runs.
 */
@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public final class HoursBasedRuleEngineSmokeTest extends RuleEngineCalculationBase {

    private static final int SMOKE_SCENARIO_LIMIT = 1;

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

    private static Object[][] toProviderRows(List<Map<String, Object>> scenarios) {
        Object[][] data = new Object[scenarios.size()][1];
        for (int i = 0; i < scenarios.size(); i++) {
            data[i][0] = scenarios.get(i);
        }
        return data;
    }

    @Test(dataProvider = "smokeDailyOTDataProvider")
    public void smokeVerifyDailyOTHoursCalculation(Map<String, Object> scenario) {
        executeHoursBasedTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "smokeDailyOTDataProvider", parallel = true)
    public Object[][] smokeDailyOTDataProvider() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(HOURS_DAILY_OT);
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(scenarios, SMOKE_SCENARIO_LIMIT));
    }

    @Test(dataProvider = "smokeWeeklyOTDataProvider")
    public void smokeVerifyWeeklyOTHoursCalculation(Map<String, Object> scenario) {
        executeHoursBasedTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "smokeWeeklyOTDataProvider", parallel = true)
    public Object[][] smokeWeeklyOTDataProvider() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(HOURS_WEEKLY_OT);
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(scenarios, SMOKE_SCENARIO_LIMIT));
    }

    @Test(dataProvider = "smokeSpecificHoursRangeDataProvider")
    public void smokeVerifySpecificHoursRangeCalculation(Map<String, Object> scenario) {
        executeHoursBasedTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "smokeSpecificHoursRangeDataProvider", parallel = true)
    public Object[][] smokeSpecificHoursRangeDataProvider() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(HOURS_SPECIFIC_RANGE);
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(scenarios, SMOKE_SCENARIO_LIMIT));
    }

    @Test(dataProvider = "smokeBiweeklyHoursDataProvider")
    public void smokeVerifyBiweeklyHoursCalculation(Map<String, Object> scenario) {
        executeHoursBasedTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "smokeBiweeklyHoursDataProvider", parallel = true)
    public Object[][] smokeBiweeklyHoursDataProvider() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(HOURS_BIWEEKLY);
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(scenarios, SMOKE_SCENARIO_LIMIT));
    }

    @Test(dataProvider = "smokeMonthlyHoursDataProvider")
    public void smokeVerifyMonthlyHoursCalculation(Map<String, Object> scenario) {
        executeHoursBasedTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "smokeMonthlyHoursDataProvider", parallel = true)
    public Object[][] smokeMonthlyHoursDataProvider() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(HOURS_MONTHLY);
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(scenarios, SMOKE_SCENARIO_LIMIT));
    }
}
