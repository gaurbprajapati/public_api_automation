package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.statusFilter;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.*;
import io.recruitcrm.contractStaffing.Filters.common.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.*;

import java.util.*;

import static io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.TimesheetFilterTestSupport.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class TimesheetStatusFilterTest extends ContractStaffingFilterBase {

    private Map<Integer, Integer> timesheetsByStatus = new HashMap<>();

    @BeforeClass
    public void setUp() {
        ensureTimesheetStatusTestData();
    }

    private synchronized void ensureTimesheetStatusTestData() {
        if (!timesheetsByStatus.isEmpty()) {
            return;
        }
        initializeAuthAndFunction();
        createTimesheetsWithDifferentStatuses();
    }

    @Test(dataProvider = "timesheetStatusFilterData")
    public void timesheetStatusFilterTest(String testId, String filterType, String filterValue,
                                          String expectedResult, Integer verifyTimesheetId,
                                          Integer excludeTimesheetId) {
        JSONObject payload = new TimesheetFilterPayloadBuilder()
                .addTimesheetStatus(filterType, filterValue)
                .build();

        Response response = postTimesheetSearchGet(payload);
        assertThat(testId + ": Response code should be 200", response.getStatusCode(), equalTo(200));
        assertThat(testId + ": Message should be correct", response.jsonPath().get("meta.message"),
                equalTo("Timesheets fetched successfully"));

        JSONArray data = getFilteredData(response);
        validateTimesheetStatusFilteredData(data, filterType, filterValue, expectedResult, testId,
                verifyTimesheetId, excludeTimesheetId);
    }

    @DataProvider(name = "timesheetStatusFilterData", parallel = true)
    public Object[][] timesheetStatusFilterDataProvider() {
        ensureTimesheetStatusTestData();
        int nonExistentStatusId = getNonExistentStatusId();
        return new Object[][] {
                {"TS001", "is", "[1]", "NonEmpty", timesheetsByStatus.get(1), null},
                {"TS002", "is", "[2]", "NonEmpty", timesheetsByStatus.get(2), null},
                {"TS003", "is", "[3]", "NonEmpty", timesheetsByStatus.get(3), null},
                {"TS004", "is", "[4]", "NonEmpty", timesheetsByStatus.get(4), null},
                {"TS005", "is_not", "[1]", "NonEmpty", null, timesheetsByStatus.get(1)},
                {"TS006", "is_not", "[2]", "NonEmpty", null, timesheetsByStatus.get(2)},
                {"TS007", "is_not", "[3]", "NonEmpty", null, timesheetsByStatus.get(3)},
                {"TS008", "is_not", "[4]", "NonEmpty", null, timesheetsByStatus.get(4)},
                {"TS009", "contains_at_least_one", "[1,2]", "NonEmpty", timesheetsByStatus.get(1), null},
                {"TS010", "contains_at_least_one", "[3,4]", "NonEmpty", timesheetsByStatus.get(3), null},
                {"TS011", "contains_at_least_one", "[1,2,3,4]", "NonEmpty", timesheetsByStatus.get(1), null},
                {"TS012", "does_not_contain", "[1]", "NonEmpty", null, timesheetsByStatus.get(1)},
                {"TS013", "does_not_contain", "[1,2]", "NonEmpty", null, timesheetsByStatus.get(1)},
                {"TS014", "does_not_contain", "[3,4]", "NonEmpty", null, timesheetsByStatus.get(3)},
                {"TS015", "has_any_value", "", "NonEmpty", null, null},
                {"TS016", "is_empty", "", "Empty", null, null},
                {"TS017", "is", "[" + nonExistentStatusId + "]", "Empty", null, null},
                {"TS018", "contains_at_least_one", "[" + nonExistentStatusId + "]", "Empty", null, null}
        };
    }

    public void createTimesheetsWithDifferentStatuses() {
        try {
            Map<String, Object> config = getFilterTestTimesheetConfig();
            String templateName = ruleEngineenFake.getTestTemplateName("FilterTest");
            List<Integer> workDayIds = Arrays.asList(1, 2, 3, 4, 5);
            List<Map<String, Object>> customRules = buildCustomRulesFromDescription((String) config.get("rulesApplied"),
                    workDayIds, (Double) config.get("payRate"), (Double) config.get("billRate"), "Shift");
            Integer templateId = createRuleTemplate(albatrossAuthToken, templateName, workDayIds, (String) config.get("regularHours"),
                    customRules, (String) config.get("breakBillable"), SHIFTS_LOGGING, (Integer) config.get("breakTimeThreshold"));
            assertThat("Template should be created", templateId, notNullValue());

            for (int statusId = 1; statusId <= 4; statusId++) {
                int timesheetId = createSingleTimesheetWithStatus(templateId, statusId);
                timesheetsByStatus.put(statusId, timesheetId);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            throw new AssertionError("Error creating test data: " + e.getMessage(), e);
        }
    }

    private int createSingleTimesheetWithStatus(Integer templateId, int targetStatusId) {
        try {
            JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
            String candidateSlug = jsonCandidate.getString("slug");
            Integer realCandidateId = getRealCandidateId(albatrossAuthToken, candidateSlug);
            assertThat("Real candidate ID should be fetched", realCandidateId, notNullValue());

            JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
            String companySlug = jsonCompany.getString("slug");

            JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
            String contactSlug = jsonContact.getString("slug");

            JsonPath jsonJob = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
            String jobSlug = jsonJob.getString("slug");

            Response usersResponse = function.getUsers(baseURL, apiAuthToken);
            int userId = usersResponse.jsonPath().getInt("[0].id");

            int jobId = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job")
                    .jsonPath().getInt("data.job.id");

            assignCandidateToJob(apiAuthToken, candidateSlug, jobSlug);

            Map<String, Object> config = getFilterTestTimesheetConfig();
            Response timesheetResponse = enableWeeklyTimesheetWithDynamicValues(albatrossAuthToken, jobId,
                    realCandidateId, userId, templateId, (String) config.get("dayPattern"), (String) config.get("regularHours"),
                    (String) config.get("rulesApplied"), (Double) config.get("payRate"), (Double) config.get("billRate"),
                    (String) config.get("breakBillable"), (Long) config.get("jobStartDate"), (Long) config.get("jobEndDate"),
                    (Integer) config.get("timesheetFrequency"), (Integer) config.get("timesheetStartDay"),
                    (Integer) config.get("payCurrencyId"), (Integer) config.get("billCurrencyId"),
                    (Integer) config.get("breakTimeThreshold"));
            assertThat("Timesheet settings should succeed", timesheetResponse.getStatusCode(), equalTo(200));

            Response freeSlotsResponse = getFreeSlotsForTimesheet(albatrossAuthToken, realCandidateId,
                    (Long) config.get("jobStartDate"), (Long) config.get("jobEndDate"),
                    (Integer) config.get("timesheetFrequency"), (Integer) config.get("timesheetStartDay"));
            assertThat("Free slots should return 200", freeSlotsResponse.getStatusCode(), equalTo(200));

            Response createTimesheetResponse = createTimesheetFromSlots(albatrossAuthToken, jobId,
                    realCandidateId, freeSlotsResponse);
            assertThat("Create timesheet should return 200", createTimesheetResponse.getStatusCode(), equalTo(200));

            Response timesheetsResponse = getTimesheetsForContractor(albatrossAuthToken, jobId, realCandidateId);
            assertThat("Get timesheets should return 200", timesheetsResponse.getStatusCode(), equalTo(200));

            List<Map<String, Object>> timesheets = timesheetsResponse.jsonPath().getList("data");
            assertThat("Timesheets should not be empty", timesheets.isEmpty(), is(false));

            Integer timesheetId = (Integer) timesheets.get(0).get("id");
            assertThat("Timesheet ID should be extracted", timesheetId, notNullValue());

            if (targetStatusId == 1) {
                return timesheetId;
            }

            Response timeLogsResponse = getTimeLogsForTimesheet(albatrossAuthToken, timesheetId);
            assertThat("Time logs should return 200", timeLogsResponse.getStatusCode(), equalTo(200));

            String actualWorkTime = "Mon: 9:00-17:00, Tue: 9:00-17:00, Wed: 9:00-17:00, Thu: 9:00-17:00, Fri: 9:00-17:00";
            updateTimeEntriesWithCsvData(albatrossAuthToken, timesheetId, SHIFTS_LOGGING,
                    timeLogsResponse, (String) config.get("dayPattern"), (String) config.get("regularHours"),
                    actualWorkTime, "None", (String) config.get("breakBillable"),
                    (Integer) config.get("timesheetFrequency"), (Long) config.get("jobStartDate"),
                    (Integer) config.get("breakTimeThreshold"), null);

            if (targetStatusId == 2) {
                return timesheetId;
            }

            Response statusResponse;
            if (targetStatusId == 3) {
                statusResponse = setTimesheetToRejected(timesheetId, albatrossAuthToken);
            } else {
                statusResponse = setTimesheetToApproved(timesheetId, albatrossAuthToken);
            }
            assertThat("Set status should succeed", statusResponse.getStatusCode(), equalTo(201));

            return timesheetId;
        } catch (Exception e) {
            throw new AssertionError("Error creating timesheet with status " + targetStatusId + ": " + e.getMessage(), e);
        }
    }

    public void validateTimesheetStatusFilteredData(JSONArray data, String filterType, String filterValue,
                                                    String expectedResult, String testId,
                                                    Integer verifyTimesheetId, Integer excludeTimesheetId) {
        if ("Empty".equals(expectedResult)) {
            assertThat(testId + ": Should return no data", data.length(), equalTo(0));
            return;
        }

        assertThat(testId + ": Should return timesheets", data.length(), greaterThan(0));
        assertTimesheetPresent(data, verifyTimesheetId, testId);
        assertTimesheetAbsent(data, excludeTimesheetId, testId);

        if ("has_any_value".equals(filterType)) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject timesheet = data.getJSONObject(i);
                Integer timesheetStatusId = timesheet.optInt("timesheetStatusId", -1);
                assertThat(testId + ": timesheetStatusId should exist", timesheetStatusId, not(equalTo(-1)));
                assertThat(testId + ": timesheetStatusId should be valid", timesheetStatusId,
                        allOf(greaterThanOrEqualTo(1), lessThanOrEqualTo(4)));
            }
            return;
        }

        if ("is_empty".equals(filterType)) {
            return;
        }

        List<Integer> expectedStatusIds = parseBracketedIntList(filterValue);

        for (int i = 0; i < data.length(); i++) {
            JSONObject timesheet = data.getJSONObject(i);
            Integer timesheetStatusId = timesheet.optInt("timesheetStatusId", -1);
            assertThat(testId + ": timesheetStatusId should exist", timesheetStatusId, not(equalTo(-1)));

            switch (filterType) {
                case "is":
                    assertThat(testId + ": Status should match", expectedStatusIds, hasItem(timesheetStatusId));
                    break;
                case "is_not":
                    assertThat(testId + ": Status should not match", expectedStatusIds, not(hasItem(timesheetStatusId)));
                    break;
                case "contains_at_least_one":
                    assertThat(testId + ": Status should be in list", expectedStatusIds, hasItem(timesheetStatusId));
                    break;
                case "does_not_contain":
                    assertThat(testId + ": Status should not be in list", expectedStatusIds, not(hasItem(timesheetStatusId)));
                    break;
                default:
                    throw new AssertionError(testId + ": Unsupported filter type: " + filterType);
            }
        }
    }
}
