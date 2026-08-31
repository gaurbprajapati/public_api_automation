package io.recruitcrm.contractStaffing.publicApi.hoursBased;

import static io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader.TestSuite.*;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader;
import io.recruitcrm.contractStaffing.publicApi.common.PublicApiHoursBaseTest;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class GetTimesheetDataTest extends PublicApiHoursBaseTest {

    private final List<Integer> createdTemplateIds = new ArrayList<>();
    private String albatrossAuthToken;
    private String apiAuthToken;
    private commanFunction apiFunction;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        apiFunction = new commanFunction();
    }

    @Test(dataProvider = "hoursBasedDataProvider")
    public void getTimesheetsDataByIDTest(Map<String, Object> scenario) {
        TestScenarioData data = extractScenarioData(scenario);
        Integer timesheetId = executeHoursBasedTest(
                scenario, albatrossAuthToken, apiAuthToken, apiFunction, createdTemplateIds);

        Response response = fetchTimesheetById(timesheetId, apiAuthToken, null);
        System.out.println("[" + data.getTestId() + "] GET timesheets/" + timesheetId
                + " response: " + response.prettyPrint());

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

    @Test(dataProvider = "hoursBasedDataProvider")
    public void getTimesheetsDataWithWorkTimeDetailsTest(Map<String, Object> scenario) {
        TestScenarioData data = extractScenarioData(scenario);
        Integer timesheetId = executeHoursBasedTest(
                scenario, albatrossAuthToken, apiAuthToken, apiFunction, createdTemplateIds);

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

    @DataProvider(name = "hoursBasedDataProvider", parallel = true)
    public Object[][] provideHoursBasedTestData() {
        List<Map<String, Object>> allScenarios = new ArrayList<>();
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_DAILY_OT));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_WEEKLY_OT));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_DOUBLE_RULE));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_UNALLOCATED));
        // allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_SPECIFIC_RANGE));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_BIWEEKLY));
        allScenarios.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_MONTHLY));
        return toProviderRows(allScenarios);
    }
}
