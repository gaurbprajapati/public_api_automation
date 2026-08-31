package io.recruitcrm.contractStaffing.shiftBasedRuleEngineCalculation.UnallocatedHoursCalculations;

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

import static io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader.TestSuite.SHIFT_UNALLOCATED;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class UnallocatedHoursTest extends MultipleTimeEntryBaseTest {

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

    @Test(dataProvider = "unallocatedHoursDataProvider")
    public void verifyUnallocatedHoursCalculationTest(Map<String, Object> scenario) {
        executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);
    }

    @DataProvider(name = "unallocatedHoursDataProvider", parallel = true)
    public Object[][] provideUnallocatedHoursTestData() {
        List<Map<String, Object>> scenarios = TimesheetTestDataLoader.loadScenarios(SHIFT_UNALLOCATED);
        Object[][] data = new Object[scenarios.size()][1];
        for (int i = 0; i < scenarios.size(); i++) {
            data[i][0] = scenarios.get(i);
        }
        return data;
    }
}
