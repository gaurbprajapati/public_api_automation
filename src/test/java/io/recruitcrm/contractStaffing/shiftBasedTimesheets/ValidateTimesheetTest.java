package io.recruitcrm.contractStaffing.shiftBasedTimesheets;

import io.rcrm.api.testbase.TestBase;
import com.qa.api.util.reaper.ThreadManager;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class ValidateTimesheetTest extends ContractStaffingBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;
    int ownerAccountID;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplate(albatrossAuthToken);
        ownerAccountID = ThreadManager.getAccount().getAccountId();
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void validateTimeLogsWithValidTimesheetIdsTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
        enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

        List<Integer> timesheetIds = createTimesheetsForValidation(jobId, candidateId, timesheetFrequency, albatrossAuthToken);

        Response response = validateTimeLogs(timesheetIds, albatrossAuthToken);

        assertThat(response.statusCode(), is(200));
        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getInt("meta.status"), is(200));
        assertThat(jsonPath.getString("meta.message"), is("Time logs fetched successfully"));

        assertThat(jsonPath.get("data"), notNullValue());
        assertThat(jsonPath.get("data.timesheetSettingsMetaData"), notNullValue());
        assertThat(jsonPath.get("data.timeLogs"), notNullValue());

        assertThat(jsonPath.get("data.timesheetSettingsMetaData.timesheetSettingId"), notNullValue());
        assertThat(jsonPath.get("data.timesheetSettingsMetaData.workLogType"), notNullValue());
        assertThat(jsonPath.get("data.timesheetSettingsMetaData.timelogsMetaData"), notNullValue());

        List<Map<String, Object>> timeLogs = jsonPath.getList("data.timeLogs");
        assertThat(timeLogs, is(not(empty())));
        int expectedTimeLogsLength = getExpectedTimeLogsLength(timesheetFrequency);
        assertThat(timeLogs.size(), is(expectedTimeLogsLength));

        for (Map<String, Object> timeLog : timeLogs) {
            assertThat(timeLog.get("id"), notNullValue());
            assertThat(timeLog.get("date"), notNullValue());
            assertThat(timeLog.get("timesheetId"), notNullValue());
            assertThat(timeLog.get("timesheetPeriod"), notNullValue());
        }

        validateTimesheetIdsInTimeLogs(timeLogs, timesheetIds);

        List<Map<String, Object>> timelogsMetaData = jsonPath.getList("data.timesheetSettingsMetaData.timelogsMetaData");
        assertThat(timelogsMetaData, is(not(empty())));

        for (Map<String, Object> metaData : timelogsMetaData) {
            List<Map<String, Object>> templateWorkDays = (List<Map<String, Object>>) metaData.get("templateWorkDays");
            assertThat(templateWorkDays, notNullValue());
            assertThat(templateWorkDays.size(), is(6));
            for (Map<String, Object> workDay : templateWorkDays) {
                assertThat(workDay.get("workDayId"), notNullValue());
                assertThat(workDay.get("workTime"), notNullValue());
                assertThat(workDay.get("workStartTime"), notNullValue());
                assertThat(workDay.get("workEndTime"), notNullValue());
            }
        }

        List<Integer> agencyIds = jsonPath.getList("data.timesheetSettingsMetaData.approvers.agencyIds");
        assertThat(agencyIds, notNullValue());
        assertThat(agencyIds, not(empty()));
        for (Integer agencyId : agencyIds) {
            assertThat(agencyId, notNullValue());
            assertThat(agencyId, greaterThan(0));
        }

        int agencyIdFromResponse = jsonPath.getInt("data.timesheetSettingsMetaData.approvers.agencyIds[0]");
        assertThat(agencyIdFromResponse, is(userId));

        validateTimesheetPeriods(timeLogs, timesheetFrequency);

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/contractStaffing/ValidateTimeLogsSchema.json"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void validateTimeLogsWithEmptyTimesheetIdsTest() {
        List<Integer> emptyTimesheetIds = new ArrayList<>();
        Response response = validateTimeLogs(emptyTimesheetIds, albatrossAuthToken);
        assertThat(response.statusCode(), is(400));
        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("errors[0].message"), is("At least one timesheet id is required"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void validateTimeLogsWithInvalidTimesheetIdsTest() {
        List<Integer> invalidTimesheetIds = Arrays.asList(999999, 888888);
        Response response = validateTimeLogs(invalidTimesheetIds, albatrossAuthToken);
        assertThat(response.statusCode(), is(500));
        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("error"), is("Internal Server Error"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void validateTimeLogsWithNullTimesheetIdsTest() {
        Response response = validateTimeLogs(null, albatrossAuthToken);
        assertThat(response.statusCode(), is(400));
        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("errors[0].message"), is("At least one timesheet id is required"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void validateTimeLogsWithMixedValidInvalidIdsTest() {
        List<Integer> mixedTimesheetIds = Arrays.asList(999999, 18108, 888888);
        Response response = validateTimeLogs(mixedTimesheetIds, albatrossAuthToken);
        assertThat(response.statusCode(), is(500));
        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("error"), is("Internal Server Error"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void validateTimeLogsWithUnauthorizedTokenTest() {
        List<Integer> validTimesheetIds = Arrays.asList(18109, 18108);
        Response response = validateTimeLogs(validTimesheetIds, albatrossAuthToken + "invalid");
        assertThat(response.statusCode(), is(401));
        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void verifyTimeLogDealTimesheetsTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
        enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

        List<Integer> timesheetIds = createTimesheetsForValidation(jobId, candidateId, timesheetFrequency, albatrossAuthToken);

        Response response = validateDealTimeLogs(timesheetIds, albatrossAuthToken);

        assertThat(response.statusCode(), is(200));
        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("meta.message"), is("Time logs fetched successfully"));
        assertThat(jsonPath.getString("meta.requestUuid"), notNullValue());
        assertThat(jsonPath.getString("meta.timestamp"), notNullValue());

        assertThat(jsonPath.get("data"), notNullValue());
        assertThat(jsonPath.get("data.timesheetSettingsMetaData"), notNullValue());
        assertThat(jsonPath.get("data.contractorsLogData"), notNullValue());

        assertThat(jsonPath.getInt("data.timesheetSettingsMetaData.contractorId"), is(candidateId));
        assertThat(jsonPath.getInt("data.timesheetSettingsMetaData.workLogType"), is(2));
        assertThat(jsonPath.getLong("data.timesheetSettingsMetaData.periodStart"),
                is((long) DEFAULT_CONTRACT_STAFFING_JOB_START_DATE_EPOCH));
        assertThat(jsonPath.getLong("data.timesheetSettingsMetaData.periodEnd"), is(getExpectedPeriodEnd(timesheetFrequency)));
        assertThat(jsonPath.getInt("data.timesheetSettingsMetaData.timesheetId"), greaterThan(0));
        assertThat(jsonPath.getInt("data.timesheetSettingsMetaData.timesheetSettingId"), greaterThan(0));

        List<Map<String, Object>> contractorsLogData = jsonPath.getList("data.contractorsLogData");
        assertThat(contractorsLogData, notNullValue());
        assertThat(contractorsLogData, not(empty()));
        assertThat(contractorsLogData.size(), is(1));

        Map<String, Object> contractorData = contractorsLogData.get(0);
        assertThat(contractorData.get("id"), is(candidateId));
        assertThat(contractorData.get("contractorName"), notNullValue());
        assertThat(contractorData.get("contractorProfilePicUrl"), notNullValue());
        assertThat(contractorData.get("calculateBreakTime"), is(false));
        assertThat(contractorData.get("breakTimeThreshold"), is(0));

        List<Map<String, Object>> templateWorkDays = (List<Map<String, Object>>) contractorData.get("templateWorkDays");
        assertThat(templateWorkDays, notNullValue());
        assertThat(templateWorkDays.size(), is(6));

        for (int i = 0; i < templateWorkDays.size(); i++) {
            Map<String, Object> workDay = templateWorkDays.get(i);
            assertThat(workDay.get("workDayId"), is(i + 1));
            assertThat(workDay.get("workTime"), is(0));
            assertThat(workDay.get("workStartTime"), is(32400));
            assertThat(workDay.get("workEndTime"), is(61200));
        }

        List<Map<String, Object>> timeLogs = (List<Map<String, Object>>) contractorData.get("timeLogs");
        assertThat(timeLogs, notNullValue());
        assertThat(timeLogs, not(empty()));

        int expectedTimeLogsCount = getExpectedTimeLogsCountForDeal(timesheetFrequency);
        assertThat(timeLogs.size(), is(expectedTimeLogsCount));

        for (Map<String, Object> timeLog : timeLogs) {
            assertThat(timeLog.get("id"), notNullValue());
            assertThat(timeLog.get("date"), notNullValue());
            assertThat(timeLog.get("dayTypeId"), notNullValue());
            assertThat(timeLog.get("timesheetId"), notNullValue());
            Integer timeLogTimesheetId = (Integer) timeLog.get("timesheetId");
            assertThat(timesheetIds.contains(timeLogTimesheetId), is(true));
            assertThat(timeLog.get("workTime"), nullValue());
            assertThat(timeLog.get("workStartTime"), nullValue());
            assertThat(timeLog.get("workEndTime"), nullValue());
            assertThat(timeLog.get("breakTime"), nullValue());
            assertThat(timeLog.get("breakIntervals"), nullValue());
            assertThat(timeLog.get("overTime"), nullValue());
            assertThat(timeLog.get("remark"), nullValue());
            assertThat(timeLog.get("totalTime"), nullValue());
            assertThat(timeLog.get("timesheetPeriod"), nullValue());
        }

        assertThat(contractorData.get("totalTime"), is(0));
        assertThat(contractorData.get("totalOvertime"), is(0));

        int agencyIds = jsonPath.getInt("data.contractorsLogData[0].approvers.agencyIds[0]");
        assertThat(agencyIds, is(userId));

        List<Map<String, Object>> contractorsErrorData = jsonPath.getList("data.contractorsErrorData");
        if (contractorsErrorData != null && !contractorsErrorData.isEmpty()) {
            for (Map<String, Object> errorData : contractorsErrorData) {
                assertThat(errorData.get("id"), notNullValue());
                assertThat(errorData.get("timesheetId"), notNullValue());
                assertThat(errorData.get("timesheetPeriod"), notNullValue());
                assertThat(errorData.get("error"), is("different_period"));
                assertThat(errorData.get("contractorName"), notNullValue());
                assertThat(errorData.get("contractorJobName"), notNullValue());
                assertThat(errorData.get("contractorSerialNumber"), notNullValue());
                assertThat(errorData.get("id"), is(candidateId));
            }
        }

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/contractStaffing/ValidateDealTimeLogsSchema.json"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void validateDealTimeLogsWithInvalidTimesheetIdsTest() {
        List<Integer> invalidTimesheetIds = Arrays.asList(24234);
        Response response = validateDealTimeLogs(invalidTimesheetIds, albatrossAuthToken);
        assertThat(response.statusCode(), is(500));
        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("error"), is("Internal Server Error"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void validateDealTimeLogsWithUnauthorizedTokenTest() {
        String invalidToken = "invalid_token";
        List<Integer> timesheetIds = Arrays.asList(18584, 18581, 18577);
        Response response = validateDealTimeLogs(timesheetIds, invalidToken);
        assertThat(response.statusCode(), is(401));
        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
    }

    @DataProvider(parallel = true)
    public Object[][] testTimesheetSettingsData() {
        Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int candidateId2 = ((Number) testData[2]).intValue();
        int candidateId3 = ((Number) testData[3]).intValue();
        int userId = ((Number) testData[4]).intValue();

        String candidateSlug = ((String) testData[5]).toString();
        String candidateSlug2 = ((String) testData[6]).toString();
        String candidateSlug3 = ((String) testData[7]).toString();
        String companySlug = ((String) testData[8]).toString();
        String contactSlug = ((String) testData[9]).toString();
        String jobSlug = ((String) testData[10]).toString();

        Map<Integer, String> baseFields = new HashMap<>();
        baseFields.put(5, companySlug);
        baseFields.put(6, jobSlug);
        baseFields.put(7, contactSlug);

        List<String> candidateSlugs = Arrays.asList(candidateSlug, candidateSlug2, candidateSlug3);

        List<String> dealSlugs = candidateSlugs.stream()
                .map(candidateSlugValue -> {
                    HashMap<Integer, String> fieldsMap = new HashMap<>(baseFields);
                    fieldsMap.put(8, candidateSlugValue);
                    return function.createNewDealWithSpecifiedFields(baseURL, apiAuthToken, fieldsMap);
                })
                .map(response -> response.jsonPath().getString("slug"))
                .collect(Collectors.toList());

        String dealSlug1 = dealSlugs.get(0);
        return new Object[][]{
                {jobId, candidateId, userId, 2},
                {jobId, candidateId2, userId, 3},
                {jobId, candidateId3, userId, 4},
        };
    }

}
