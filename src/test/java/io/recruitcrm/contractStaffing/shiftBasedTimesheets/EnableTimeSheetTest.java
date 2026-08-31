package io.recruitcrm.contractStaffing.shiftBasedTimesheets;

import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.TestUtil;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.contractStaffing.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class EnableTimeSheetTest extends ContractStaffingBaseTest {

        String albatrossAuthToken;
        String apiAuthToken;
        int ownerAccountID;
        commanFunction function;

        @BeforeClass(alwaysRun = true)
        public void Setup() {
                albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
                ownerAccountID = ThreadManager.getAccount().getAccountId();
                apiAuthToken = ThreadManager.getAccountApiKey();
                function = new commanFunction();
                createRuleEngineTemplate(albatrossAuthToken);
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
        public void verifyEnableTimesheetSettingsTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
                Response response = enableTimesheet(candidateId, jobId, ownerAccountID, albatrossAuthToken,
                                timesheetFrequency, 200, 0);

                assertThat(response.statusCode(), is(200));

                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(200));
                assertThat(jsonPath.getString("meta.message"), is("Timesheet setting created successfully"));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Request is successful"));
                assertThat(jsonPath.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonPath.getString("meta.timestamp"), notNullValue());

                response.then().assertThat().body(
                                matchesJsonSchemaInClasspath(
                                                "privateApi/contractStaffing/CreateTimesheetSettings.json"));
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void verifyEnableTimesheetSettingsWithEmptyRequestBodyTest() {
                TimesheetSettings timesheetSettings = new TimesheetSettings();

                Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                                albatrossAuthToken, null, true, timesheetSettings);

                assertThat(response.statusCode(), is(400));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(400));
                assertThat(jsonPath.getString("meta.message"), anyOf(
                                is("Contractor ids cannot be empty"),
                                is("Work days cannot be null"),
                                is("Approver's cannot be null")));
                assertThat(jsonPath.getInt("meta.responseType.code"), is(101));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Error while processing request"));
                assertThat(jsonPath.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonPath.getString("meta.timestamp"), notNullValue());
                assertThat(jsonPath.get("data"), nullValue());
                assertThat(jsonPath.getList("errors").isEmpty(), is(true));
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void verifyEnableTimesheetSettingsWithCalculateBreakTimeTrueTest() {
                // BNP-7584: calculateBreakTime is now @AssertFalse - sending true must be rejected with 400
                Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL,
                                albatrossAuthToken);
                int jobId = (Integer) testData[0];
                int candidateId = (Integer) testData[1];

                TimesheetSettings timesheetSettings = createDefaultTimesheetSettings(jobId, candidateId,
                                ownerAccountID, 2, 0);
                timesheetSettings.setCalculateBreakTime(true);

                Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                                albatrossAuthToken, null, true, timesheetSettings);

                assertThat("calculateBreakTime=true should be rejected", response.statusCode(), is(400));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(400));
                assertThat(jsonPath.getString("meta.message"), is("calculateBreakTime must be false (0)"));
                assertThat(jsonPath.getInt("meta.responseType.code"), is(101));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Error while processing request"));
                assertThat(jsonPath.get("data"), nullValue());
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void verifyEnableTimesheetSettingsWithNullPayCurrencyIdTest() {
                // PAY-712: payCurrencyId is now required. TimesheetSettings POJO declares it as a
                // primitive int, so a raw JSON map (omitting the field entirely) is used to send a
                // real absent/null value instead of the POJO, which can never represent null here.
                Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL,
                                albatrossAuthToken);
                int jobId = (Integer) testData[0];
                int candidateId = (Integer) testData[1];

                Map<String, Object> payload = buildValidTimesheetSettingsPayload(jobId, candidateId);
                payload.remove("payCurrencyId");

                Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                                albatrossAuthToken, null, true, TestUtil.getSerializedJSON(payload));

                assertThat("Missing payCurrencyId should be rejected", response.statusCode(), is(400));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(400));
                assertThat(jsonPath.getString("meta.message"), is("Pay currency id cannot be null"));
                assertThat(jsonPath.get("data"), nullValue());
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void verifyEnableTimesheetSettingsWithNullBillCurrencyIdTest() {
                // PAY-712: billCurrencyId is now required - see verifyEnableTimesheetSettingsWithNullPayCurrencyIdTest
                Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL,
                                albatrossAuthToken);
                int jobId = (Integer) testData[0];
                int candidateId = (Integer) testData[1];

                Map<String, Object> payload = buildValidTimesheetSettingsPayload(jobId, candidateId);
                payload.remove("billCurrencyId");

                Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                                albatrossAuthToken, null, true, TestUtil.getSerializedJSON(payload));

                assertThat("Missing billCurrencyId should be rejected", response.statusCode(), is(400));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(400));
                assertThat(jsonPath.getString("meta.message"), is("Bill currency id cannot be null"));
                assertThat(jsonPath.get("data"), nullValue());
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void verifyEnableTimesheetSettingsWithNullPayRateTest() {
                // PAY-712: payRate is now required - see verifyEnableTimesheetSettingsWithNullPayCurrencyIdTest
                Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL,
                                albatrossAuthToken);
                int jobId = (Integer) testData[0];
                int candidateId = (Integer) testData[1];

                Map<String, Object> payload = buildValidTimesheetSettingsPayload(jobId, candidateId);
                payload.remove("payRate");

                Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                                albatrossAuthToken, null, true, TestUtil.getSerializedJSON(payload));

                assertThat("Missing payRate should be rejected", response.statusCode(), is(400));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(400));
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void verifyEnableTimesheetSettingsWithNullBillRateTest() {
                // PAY-712: billRate is now required - see verifyEnableTimesheetSettingsWithNullPayCurrencyIdTest
                Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL,
                                albatrossAuthToken);
                int jobId = (Integer) testData[0];
                int candidateId = (Integer) testData[1];

                Map<String, Object> payload = buildValidTimesheetSettingsPayload(jobId, candidateId);
                payload.remove("billRate");

                Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                                albatrossAuthToken, null, true, TestUtil.getSerializedJSON(payload));

                assertThat("Missing billRate should be rejected", response.statusCode(), is(400));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(400));
        }

        private Map<String, Object> buildValidTimesheetSettingsPayload(int jobId, int candidateId) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("contractorIds", Arrays.asList(candidateId));
                payload.put("jobId", jobId);
                payload.put("jobStartDate", (long) DEFAULT_CONTRACT_STAFFING_JOB_START_DATE_EPOCH);
                payload.put("jobEndDate", (long) DEFAULT_CONTRACT_STAFFING_JOB_END_DATE_EPOCH);
                payload.put("timesheetFrequency", 2);
                payload.put("timesheetStartDay", 1);

                Map<String, Object> approvers = new HashMap<>();
                approvers.put("agencyIds", Arrays.asList(ownerAccountID));
                approvers.put("clientIds", new ArrayList<>());
                payload.put("approvers", approvers);

                payload.put("payCurrencyId", 53);
                payload.put("payRate", 5000);
                payload.put("billCurrencyId", 53);
                payload.put("billRate", 6000);
                payload.put("workDayIds", Arrays.asList(1, 2, 3, 4, 5, 6));
                payload.put("workLogType", 2);
                payload.put("isPreferencesModified", 1);
                payload.put("calculateBreakTime", false);
                payload.put("workTime", Arrays.asList(0, 0, 0, 0, 0, 0));
                payload.put("workStartTime", Arrays.asList(32400, 32400, 32400, 32400, 32400, 32400));
                payload.put("workEndTime", Arrays.asList(61200, 61200, 61200, 61200, 61200, 61200));
                payload.put("isRemarkMandatory", 1);
                payload.put("isUnplannedHoursPayEnabled", 0);
                payload.put("isReimbursementEnabled", 0);
                return payload;
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void verifyEnableTimesheetSettingsWithInvalidJobDatesTest() {
                Response response = enableTimesheet(123456, 23456, ownerAccountID, albatrossAuthToken, 2, 404, 0);

                assertThat(response.statusCode(), is(404));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(404));
                assertThat(jsonPath.getInt("meta.responseType.code"), is(101));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Error while processing request"));
                assertThat(jsonPath.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonPath.getString("meta.timestamp"), notNullValue());

                List<Map<String, Object>> errors = jsonPath.getList("errors");
                assertThat(errors.isEmpty(), is(false));
                assertThat(jsonPath.getString("errors[0].message"),
                                is("Assignment not found for Job Id: 23456 candidate Ids : [123456]"));
                assertThat(jsonPath.getInt("errors[0].errorType.code"), is(202));
        }

        @Test(dataProvider = "testTimesheetSettingsData", groups = "contract_staffing")
        public void verifyEnableTimesheetSettingsWithReimbursementTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
                Response response = enableTimesheet(candidateId, jobId, ownerAccountID, albatrossAuthToken,
                                timesheetFrequency, 200, 1);

                assertThat(response.statusCode(), is(200));

                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(200));
                assertThat(jsonPath.getString("meta.message"), is("Timesheet setting created successfully"));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Request is successful"));
                assertThat(jsonPath.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonPath.getString("meta.timestamp"), notNullValue());

                response.then().assertThat().body(
                                matchesJsonSchemaInClasspath(
                                                "privateApi/contractStaffing/CreateTimesheetSettings.json"));
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void unauthorizedUserCannotEnableTimesheetSettingsTest() {
                createRuleEngineTemplate(albatrossAuthToken);
                Response response = enableTimesheet(123456, 23456, ownerAccountID, albatrossAuthToken + "abc", 2, 401, 0);
                JsonPath jsonPath = response.jsonPath();

                assertThat(jsonPath.getInt("meta.status"), is(401));
                assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
        public void getTimesheetSettingsByJobAndContractorTest(int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                enableTimesheet(candidateId, jobId, ownerAccountID, albatrossAuthToken, timesheetFrequency, 200, 0);

                String endpoint = "timesheet-settings/job/" + jobId + "/contractor/" + candidateId;

                Response response = retryApiCall(
                                () -> RestClient.doGet("JSON", timesheetBaseURL, endpoint, albatrossAuthToken, null,
                                                null, true),
                                5,
                                500,
                                500,
                                resp -> resp.statusCode() == 200);

                assertThat(response.statusCode(), is(200));

                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(200));
                assertThat(jsonPath.getString("meta.message"), is("Timesheet setting fetched successfully"));
                assertThat(jsonPath.getInt("meta.responseType.code"), is(103));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Request is successful"));
                assertThat(jsonPath.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonPath.getString("meta.timestamp"), notNullValue());

                assertThat(jsonPath.get("data.id"), notNullValue());
                assertThat(jsonPath.getLong("data.jobStartDate"),
                                is((long) DEFAULT_CONTRACT_STAFFING_JOB_START_DATE_EPOCH));
                assertThat(jsonPath.getLong("data.jobEndDate"),
                                is((long) DEFAULT_CONTRACT_STAFFING_JOB_END_DATE_EPOCH));
                assertThat(jsonPath.getInt("data.timesheetFrequency"), is(timesheetFrequency));
                assertThat(jsonPath.getInt("data.timesheetStartDay"), is(1));
                assertThat(jsonPath.getInt("data.payCurrencyId"), is(53));
                assertThat(jsonPath.getInt("data.billCurrencyId"), is(53));
                assertThat(jsonPath.getDouble("data.billRate"), is(6000.0));
                assertThat(jsonPath.getDouble("data.payRate"), is(5000.0));
                assertThat(jsonPath.getInt("data.workLogType"), is(2));
                assertThat(jsonPath.getBoolean("data.calculateBreakTime"), is(false));
                assertThat(jsonPath.getInt("data.isReimbursementEnabled"), is(0));
                // enableTimesheet(...) defaults to Fixed Rate/0/0 (see ContractStaffingBaseTest) since
                // calculateChargeBy/marginPercentage/markupPercentage are @NotNull regardless of mode.
                assertThat(jsonPath.getInt("data.calculateChargeBy"), is(1));
                assertThat(jsonPath.getDouble("data.marginPercentage"), is(0.0));
                assertThat(jsonPath.getDouble("data.markupPercentage"), is(0.0));

                List<Map<String, Object>> templateWorkDays = jsonPath.getList("data.templateWorkDays");
                assertThat(templateWorkDays.size(), is(6));

                List<Map<String, Object>> customRules = jsonPath.getList("data.customRules");
                assertThat(customRules.isEmpty(), is(false));

                response.then().assertThat().body(
                                matchesJsonSchemaInClasspath("privateApi/contractStaffing/GetTimesheetSettings.json"));
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void getTimesheetSettingsWithInvalidJobIdAndContractorIdTest() {
                String endpoint = "timesheet-settings/job/999999/contractor/73995033";

                Response response = RestClient.doGet("JSON", timesheetBaseURL, endpoint,
                                albatrossAuthToken, null, null, true);

                assertThat(response.statusCode(), is(404));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(404));
                assertThat(jsonPath.getInt("meta.responseType.code"), is(101));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Error while processing request"));
                assertThat(jsonPath.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonPath.getString("meta.timestamp"), notNullValue());
                assertThat(jsonPath.get("meta.message"), nullValue());
                assertThat(jsonPath.get("data"), nullValue());

                List<Map<String, Object>> errors = jsonPath.getList("errors");
                assertThat(errors.isEmpty(), is(false));
                assertThat(jsonPath.getString("errors[0].message")
                                .contains("Assignment not found for Job Id: 999999 candidate Id : 73995033"), is(true));
                assertThat(jsonPath.getInt("errors[0].errorType.code"), is(202));
                assertThat(jsonPath.getString("errors[0].errorType.context"), is("Generic Error"));
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void unauthorizedUserCannotGetTimesheetSettingsByJobAndContractorTest() {
                String endpoint = "timesheet-settings/job/3490727/contractor/73995033";

                Response response = RestClient.doGet("JSON", timesheetBaseURL, endpoint,
                                albatrossAuthToken + "invalid", null, null, true);

                assertThat(response.statusCode(), is(401));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
        public void validateTimesheetSettingsTest(int jobId, int contractorId, int userId,
                        int timesheetFrequency) {
                enableTimesheet(contractorId, jobId, ownerAccountID, albatrossAuthToken, timesheetFrequency, 200, 0);

                String endpoint = "timesheet-settings/job/" + jobId + "/contractor/" + contractorId + "/validate";

                Response response = retryApiCall(
                                () -> RestClient.doGet("JSON", timesheetBaseURL, endpoint, albatrossAuthToken, null,
                                                null, true),
                                5,
                                500,
                                500,
                                resp -> resp.statusCode() == 200);

                assertThat(response.statusCode(), is(200));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getString("meta.message"), is("Timesheet setting validation successfully"));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Request is successful"));

                response.then().assertThat().body(
                                matchesJsonSchemaInClasspath(
                                                "privateApi/contractStaffing/ValidateTimesheetSettings.json"));
        }

        @Owner("Gaurav Prajapati")
        @Test
        public void validateTimesheetSettingsWithInvalidJobIdContractorIdTest() {
                String endpoint = "timesheet-settings/job/999999/contractor/73995033/validate";

                Response response = RestClient.doGet("JSON", timesheetBaseURL, endpoint,
                                albatrossAuthToken, null, null, true);

                assertThat(response.statusCode(), is(404));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(404));
                assertThat(jsonPath.getInt("meta.responseType.code"), is(101));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Error while processing request"));
                assertThat(jsonPath.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonPath.getString("meta.timestamp"), notNullValue());

                List<Map<String, Object>> errors = jsonPath.getList("errors");
                assertThat(errors.isEmpty(), is(false));
                assertThat(jsonPath.getString("errors[0].message").contains("Assignment not found"), is(true));
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void unauthorizedUserCannotValidateTimesheetSettingsTest() {
                String endpoint = "timesheet-settings/job/3490727/contractor/73995033/validate";

                Response response = RestClient.doGet("JSON", timesheetBaseURL, endpoint,
                                albatrossAuthToken + "invalid", null, null, true);

                assertThat(response.statusCode(), is(401));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
        }

        @Test(dataProvider = "testTimesheetSettingsData")
        public void getTimesheetSettingsByJobAndContractorWithReimbursementTest(int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                enableTimesheet(candidateId, jobId, ownerAccountID, albatrossAuthToken, timesheetFrequency, 200, 1);

                String endpoint = "timesheet-settings/job/" + jobId + "/contractor/" + candidateId;

                Response response = retryApiCall(
                                () -> RestClient.doGet("JSON", timesheetBaseURL, endpoint, albatrossAuthToken, null,
                                                null, true),
                                5,
                                500,
                                500,
                                resp -> resp.statusCode() == 200);

                assertThat(response.statusCode(), is(200));

                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(200));
                assertThat(jsonPath.getString("meta.message"), is("Timesheet setting fetched successfully"));
                assertThat(jsonPath.getInt("meta.responseType.code"), is(103));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Request is successful"));
                assertThat(jsonPath.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonPath.getString("meta.timestamp"), notNullValue());

                assertThat(jsonPath.get("data.id"), notNullValue());
                assertThat(jsonPath.getLong("data.jobStartDate"),
                                is((long) DEFAULT_CONTRACT_STAFFING_JOB_START_DATE_EPOCH));
                assertThat(jsonPath.getLong("data.jobEndDate"),
                                is((long) DEFAULT_CONTRACT_STAFFING_JOB_END_DATE_EPOCH));
                assertThat(jsonPath.getInt("data.timesheetFrequency"), is(timesheetFrequency));
                assertThat(jsonPath.getInt("data.timesheetStartDay"), is(1));
                assertThat(jsonPath.getInt("data.payCurrencyId"), is(53));
                assertThat(jsonPath.getInt("data.billCurrencyId"), is(53));
                assertThat(jsonPath.getDouble("data.billRate"), is(6000.0));
                assertThat(jsonPath.getDouble("data.payRate"), is(5000.0));
                assertThat(jsonPath.getInt("data.workLogType"), is(2));
                assertThat(jsonPath.getBoolean("data.calculateBreakTime"), is(false));
                assertThat(jsonPath.getInt("data.isReimbursementEnabled"), is(1));

                List<Map<String, Object>> templateWorkDays = jsonPath.getList("data.templateWorkDays");
                assertThat(templateWorkDays.size(), is(6));

                List<Map<String, Object>> customRules = jsonPath.getList("data.customRules");
                assertThat(customRules.isEmpty(), is(false));

                response.then().assertThat().body(
                                matchesJsonSchemaInClasspath("privateApi/contractStaffing/GetTimesheetSettings.json"));
        }


        @DataProvider(parallel = true)
        public Object[][] testTimesheetSettingsData() {
                Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL,
                                albatrossAuthToken);
                int jobId = (Integer) testData[0];
                int candidateId = (Integer) testData[1];
                int candidateId2 = (Integer) testData[2];
                int candidateId3 = (Integer) testData[3];
                int userId = (Integer) testData[4];

                return new Object[][] {
                                { jobId, candidateId, userId, 2 },
                                { jobId, candidateId2, userId, 3 },
                                { jobId, candidateId3, userId, 4 },
                };
        }
}