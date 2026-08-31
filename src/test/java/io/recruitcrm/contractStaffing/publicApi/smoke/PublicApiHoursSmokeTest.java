package io.recruitcrm.contractStaffing.publicApi.smoke;

import static io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader.TestSuite.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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
public class PublicApiHoursSmokeTest extends PublicApiHoursBaseTest {

    private static final int SMOKE_SCENARIO_LIMIT = 3;

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

    @Test(dataProvider = "hoursSmokeDataProvider")
    public void smokeGetTimesheetByIdTest(Map<String, Object> scenario) {
        TestScenarioData data = extractScenarioData(scenario);
        Integer timesheetId = executeHoursBasedTest(
                scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);

        Response response = fetchTimesheetById(timesheetId, apiAuthToken, null);
        System.out.println("[SMOKE " + data.getTestId() + "] GET timesheets/" + timesheetId
                + " response: " + response.prettyPrint());

        String tid = data.getTestId();
        assertBasicResponseValid(response, tid);
        assertTimesheetMetadata(response, tid);
        assertPayBillStructure(response, tid);
        assertPayBillAmounts(response, data);
        assertDailyHoursForAllLogs(response, data);
    }

    @Test(dataProvider = "hoursSmokeDataProvider")
    public void smokeGetTimesheetWithWorkTimeDetailsTest(Map<String, Object> scenario) {
        TestScenarioData data = extractScenarioData(scenario);
        Integer timesheetId = executeHoursBasedTest(
                scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("work_time_details", "1");

        Response response = fetchTimesheetById(timesheetId, apiAuthToken, queryParams);
        System.out.println("[SMOKE " + data.getTestId() + "] GET timesheets/" + timesheetId
                + "?work_time_details=1 response: " + response.prettyPrint());

        String tid = data.getTestId();
        assertBasicResponseValid(response, tid);
        assertTimesheetMetadata(response, tid);
        assertPayBillStructure(response, tid);
        assertPayBillAmounts(response, data);
        assertDailyHoursWithWorkTimeDetails(response, data);
    }

    @Test(dataProvider = "hoursSmokeDataProvider")
    public void smokeGetTimesheetsListTest(Map<String, Object> scenario) {
        executeHoursBasedTest(
                scenario, albatrossAuthToken, apiAuthToken, function, createdTemplateIds);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("limit", "10");
        Response response = fetchTimesheetsList(apiAuthToken, queryParams);

        assertThat("List endpoint status 200", response.getStatusCode(), is(200));
        assertThat("List has data array", response.jsonPath().getList("data"), notNullValue());
        assertThat("List has at least 1 item", response.jsonPath().getList("data").size(), greaterThan(0));

        String prefix = "data[0]";
        assertThat(prefix + ".id", response.jsonPath().get(prefix + ".id"), notNullValue());
        assertThat(prefix + ".timesheet_id", response.jsonPath().getString(prefix + ".timesheet_id"), notNullValue());
        assertThat(prefix + ".timesheet_period.start_date",
                response.jsonPath().get(prefix + ".timesheet_period.start_date"), notNullValue());
        assertThat(prefix + ".timesheet_period.end_date",
                response.jsonPath().get(prefix + ".timesheet_period.end_date"), notNullValue());
        assertThat(prefix + ".timesheet_status.id", response.jsonPath().get(prefix + ".timesheet_status.id"),
                notNullValue());
        assertThat(prefix + ".hours.total_regular", response.jsonPath().getString(prefix + ".hours.total_regular"),
                notNullValue());
        assertThat(prefix + ".hours.total_overtime", response.jsonPath().getString(prefix + ".hours.total_overtime"),
                notNullValue());
        assertThat(prefix + ".pay.amount", response.jsonPath().get(prefix + ".pay.amount"), notNullValue());
        assertThat(prefix + ".bill.amount", response.jsonPath().get(prefix + ".bill.amount"), notNullValue());
    }

    @DataProvider(name = "hoursSmokeDataProvider", parallel = true)
    public Object[][] hoursSmokeDataProvider() {
        List<Map<String, Object>> all = new ArrayList<>();
        all.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_DAILY_OT));
        all.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_WEEKLY_OT));
        all.addAll(TimesheetTestDataLoader.loadScenarios(HOURS_DOUBLE_RULE));
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(all, SMOKE_SCENARIO_LIMIT));
    }
}
