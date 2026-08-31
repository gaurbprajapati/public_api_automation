package io.recruitcrm.contractStaffing.publicApi.smoke;

import static io.recruitcrm.contractStaffing.common.TimesheetTestDataLoader.TestSuite.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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
public class PublicApiShiftSmokeTest extends PublicApiBaseTest {

    private static final int SMOKE_SCENARIO_LIMIT = 10;

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

    // ========================== Smoke: GET /timesheets/{id} ==========================

    @Test(dataProvider = "smokeDataProvider")
    public void smokeGetTimesheetByIdTest(Map<String, Object> scenario) {
        TestScenarioData data = extractScenarioData(scenario);
        Integer timesheetId = executeMultipleTimeEntryTest(
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

    // ========================== Smoke: GET /timesheets/{id}?work_time_details=1 ==========================

    @Test(dataProvider = "smokeDataProvider")
    public void smokeGetTimesheetWithWorkTimeDetailsTest(Map<String, Object> scenario) {
        TestScenarioData data = extractScenarioData(scenario);
        Integer timesheetId = executeMultipleTimeEntryTest(
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

    // ========================== Smoke: GET /timesheets (list) ==========================

    @Test(dataProvider = "smokeDataProvider")
    public void smokeGetTimesheetsListTest(Map<String, Object> scenario) {
        executeMultipleTimeEntryTest(
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
        assertThat(prefix + ".timesheet_period.start_date", response.jsonPath().get(prefix + ".timesheet_period.start_date"), notNullValue());
        assertThat(prefix + ".timesheet_period.end_date", response.jsonPath().get(prefix + ".timesheet_period.end_date"), notNullValue());
        assertThat(prefix + ".timesheet_status.id", response.jsonPath().get(prefix + ".timesheet_status.id"), notNullValue());
        assertThat(prefix + ".timesheet_status.label", response.jsonPath().getString(prefix + ".timesheet_status.label"), notNullValue());
        assertThat(prefix + ".related_entities_slug.candidate", response.jsonPath().get(prefix + ".related_entities_slug.candidate"), notNullValue());
        assertThat(prefix + ".related_entities_slug.job", response.jsonPath().get(prefix + ".related_entities_slug.job"), notNullValue());
        assertThat(prefix + ".related_entities_slug.company", response.jsonPath().get(prefix + ".related_entities_slug.company"), notNullValue());
        assertThat(prefix + ".hours.total_regular", response.jsonPath().getString(prefix + ".hours.total_regular"), notNullValue());
        assertThat(prefix + ".hours.total_overtime", response.jsonPath().getString(prefix + ".hours.total_overtime"), notNullValue());
        assertThat(prefix + ".hours.total", response.jsonPath().getString(prefix + ".hours.total"), notNullValue());
        assertThat(prefix + ".pay.rate", response.jsonPath().get(prefix + ".pay.rate"), notNullValue());
        assertThat(prefix + ".pay.currency", response.jsonPath().get(prefix + ".pay.currency"), notNullValue());
        assertThat(prefix + ".pay.amount", response.jsonPath().get(prefix + ".pay.amount"), notNullValue());
        assertThat(prefix + ".bill.rate", response.jsonPath().get(prefix + ".bill.rate"), notNullValue());
        assertThat(prefix + ".bill.currency", response.jsonPath().get(prefix + ".bill.currency"), notNullValue());
        assertThat(prefix + ".bill.amount", response.jsonPath().get(prefix + ".bill.amount"), notNullValue());

        assertThat("current_page", response.jsonPath().getInt("current_page"), is(1));
        assertThat("first_page_url", response.jsonPath().getString("first_page_url"), notNullValue());
        assertThat("path", response.jsonPath().getString("path"), notNullValue());
        assertThat("per_page", response.jsonPath().get("per_page"), notNullValue());
    }

    // ========================== Data Provider ==========================

    @DataProvider(name = "smokeDataProvider", parallel = true)
    public Object[][] smokeDataProvider() {
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
        return toProviderRows(TimesheetTestDataLoader.limitScenarios(allScenarios, SMOKE_SCENARIO_LIMIT));
    }
}
