package io.recruitcrm.contractStaffing.publicApi.shiftBased;

import static io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader.TestSuite.*;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.publicApi.common.PublicApiBaseTest;
import io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class GetTimesheetDataTest extends PublicApiBaseTest {

    private final List<Integer> createdTemplateIds = new ArrayList<>();
    String albatrossAuthToken;
    String apiAuthToken;
    commanFunction apiFunction;

    @BeforeClass
    public void Setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        apiFunction = new commanFunction();
    }

    // ========================== Test Methods ==========================

    @Test(dataProvider = "singleRuleDataProvider")
    public void getTimesheetsDataByIDTest(Map<String, Object> scenario) {
        TestScenarioData data = extractScenarioData(scenario);
        Integer timesheetId = executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, apiFunction, createdTemplateIds);

        Response response = fetchTimesheetById(timesheetId, apiAuthToken, null);
        System.out.println("[" + data.getTestId() + "] GET timesheets/" + timesheetId + " response: " + response.prettyPrint());

        String tid = data.getTestId();
        assertBasicResponseValid(response, tid);
        assertTimesheetMetadata(response, tid);
        assertPayBillStructure(response, tid);
        assertPayBillAmounts(response, data);
        assertAmountSplit(response, data);
        assertWeeklyOvertimeObject(response, data);
        assertDailyHoursForAllLogs(response, data);
        assertOvertimeDetailsForAllLogs(response, data);
    }

    @Test(dataProvider = "singleRuleDataProvider")
    public void getTimesheetsDataWithWorkTimeDetailsTest(Map<String, Object> scenario) {
        TestScenarioData data = extractScenarioData(scenario);
        Integer timesheetId = executeMultipleTimeEntryTest(scenario, albatrossAuthToken, apiAuthToken, apiFunction, createdTemplateIds);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("work_time_details", "1");

        Response response = fetchTimesheetById(timesheetId, apiAuthToken, queryParams);
        System.out.println("[" + data.getTestId() + "] GET timesheets/" + timesheetId
                + "?work_time_details=1 response: " + response.prettyPrint());

        String tid = data.getTestId();
        assertBasicResponseValid(response, tid);
        assertTimesheetMetadata(response, tid);
        assertPayBillStructure(response, tid);
        assertPayBillAmounts(response, data);
        assertAmountSplit(response, data);
        assertWeeklyOvertimeObject(response, data);
        assertDailyHoursWithWorkTimeDetails(response, data);
        assertOvertimeDetailsForAllLogs(response, data);
    }

    // ========================== Data Provider ==========================

    @DataProvider(name = "singleRuleDataProvider", parallel = true)
    public Object[][] provideSingleRuleTestData() {
        List<Map<String, Object>> allScenarios = new ArrayList<>();
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_NO_RULE));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_SINGLE_RULE));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_DAILY_OT));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_WEEKLY_OT));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_SPECIFIC_RANGE));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_DOUBLE_RULE));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_THREE_RULE));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_FOUR_RULE));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_FIVE_RULE));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_BIWEEKLY));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_MONTHLY));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(SHIFT_UNALLOCATED));

        return toProviderRows(allScenarios);
    }
}
