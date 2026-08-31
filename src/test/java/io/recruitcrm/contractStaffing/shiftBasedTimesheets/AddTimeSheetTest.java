package io.recruitcrm.contractStaffing.shiftBasedTimesheets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.contractStaffing.TimesheetDate;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class AddTimeSheetTest extends ContractStaffingBaseTest {

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
        public void getFreeSlotsTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
                enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

                Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);
                assertThat(freeSlotsResponse.statusCode(), is(200));

                JsonPath jsonPath = freeSlotsResponse.jsonPath();
                assertThat(jsonPath.getString("meta.message"), is("Empty slots fetched successfully"));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Request is successful"));
                List<Map<String, Object>> freeSlots = jsonPath.getList("data");
                assertThat(freeSlots.isEmpty(), is(false));
                if (timesheetFrequency == 2) {
                        assertThat(freeSlots.size(), is(13));
                } else if (timesheetFrequency == 3) {
                        assertThat(freeSlots.size(), is(7));
                } else {
                        assertThat(freeSlots.size(), is(3));
                }

                assertThat(jsonPath.getLong("data[0].startDate"), notNullValue());
                assertThat(jsonPath.getLong("data[0].endDate"), notNullValue());

                for (int i = 0; i < freeSlots.size(); i++) {
                        long startDate = jsonPath.getLong("data[" + i + "].startDate");
                        long endDate = jsonPath.getLong("data[" + i + "].endDate");
                        assertThat(startDate < endDate, is(true));
                }
                freeSlotsResponse.then().assertThat().body(
                                matchesJsonSchemaInClasspath("privateApi/contractStaffing/GetFreeSlots.json"));
        }

        @Owner("Gaurav Prajapati")
        @Test
        public void getFreeSlotsWithEmptyRequestBodyTest() {

                Response response = getTimeSheetFreeSlots(1, 2, 3,
                                albatrossAuthToken, "0", "0");

                assertThat(response.statusCode(), is(404));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getString("error").contains("Bad Request"), is(true));
        }

        @Owner("Gaurav Prajapati")
        @Test
        public void getFreeSlotsWithInvalidDateRangeTest() {
                Response response = getTimeSheetFreeSlots(1, 2, 3,
                                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, "1754092799");

                assertThat(response.statusCode(), is(422));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.get("message_type"), is("is-danger"));
        }

        @Owner("Gaurav Prajapati")
        @Test
        public void getFreeSlotsWithInvalidContractorIdTest() {
                Response response = getTimeSheetFreeSlots(1, 2, 3,
                                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, "1754092799");

                assertThat(response.statusCode(), is(404));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(404));
                assertThat(jsonPath.getInt("meta.responseType.code"), is(101));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Error while processing request"));
        }

        @Owner("Gaurav Prajapati")
        @Test
        public void getFreeSlotsWithInvalidTimesheetFrequencyTest() {
                Response response = getTimeSheetFreeSlots(1, 2, 3,
                                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, "1754092799");

                assertThat(response.statusCode(), is(422));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.get("message_type"), is("is-danger"));
        }

        @Owner("Gaurav Prajapati")
        @Test
        public void getFreeSlotsWithInvalidTimesheetStartDayTest() {
                Response response = getTimeSheetFreeSlots(1, 2, 3,
                                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, "1754092799");

                assertThat(response.statusCode(), is(422));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.get("message_type"), is("is-danger"));
        }

        @Owner("Gaurav Prajapati")
        @Test
        public void getFreeSlotsWithMultipleContractorsTest() {
                Response response = getTimeSheetFreeSlots(1, 2, 3,
                                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, "1754092799");

                assertThat(response.statusCode(), is(200));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(200));
                assertThat(jsonPath.getString("meta.message"), is("Empty slots fetched successfully"));

                List<Map<String, Object>> freeSlots = jsonPath.getList("data");
                assertThat(freeSlots.isEmpty(), is(false));
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void unauthorizedUserCannotGetFreeSlotsTest() {
                Response response = getTimeSheetFreeSlots(1, 2, 3,
                                albatrossAuthToken + "invalid", DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, "1754092799");

                assertThat(response.statusCode(), is(401));
                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
        public void addTimeSheetTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
                enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

                Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

                JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
                List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

                List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

                Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates,
                                albatrossAuthToken);

                assertThat(addTimesheetResponse.statusCode(), is(200));

                JsonPath jsonPath = addTimesheetResponse.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(200));
                assertThat(jsonPath.getString("meta.message"), is("Timesheets created successfully"));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Request is successful"));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
        public void addTimeSheetWithInvalidFreeSlotTest(int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);
                List<TimesheetDate> invalidTimesheetDates = new ArrayList<>();

                TimesheetDate invalidSlot1 = new TimesheetDate();
                try {
                        java.lang.reflect.Method setStartDate = TimesheetDate.class.getMethod("setStartDate",
                                        long.class);
                        java.lang.reflect.Method setEndDate = TimesheetDate.class.getMethod("setEndDate", long.class);
                        setStartDate.invoke(invalidSlot1, 1751241600L); // Invalid start date
                        setEndDate.invoke(invalidSlot1, (long) DEFAULT_CONTRACT_STAFFING_JOB_START_DATE_EPOCH); // End date after start

                } catch (Exception e) {
                        throw new RuntimeException("TimesheetDate setters not available", e);
                }

                invalidTimesheetDates.add(invalidSlot1);
                TimesheetDate invalidSlot2 = new TimesheetDate();
                try {
                        java.lang.reflect.Method setStartDate = TimesheetDate.class.getMethod("setStartDate",
                                        long.class);
                        java.lang.reflect.Method setEndDate = TimesheetDate.class.getMethod("setEndDate", long.class);

                        setStartDate.invoke(invalidSlot2, 1751414400L); // Invalid start date
                        setEndDate.invoke(invalidSlot2, 1751500800L); // End date after start

                } catch (Exception e) {
                        throw new RuntimeException("TimesheetDate setters not available", e);
                }
                invalidTimesheetDates.add(invalidSlot2);

                Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId), invalidTimesheetDates,
                                albatrossAuthToken);

                assertThat(addTimesheetResponse.statusCode(), is(400));
                JsonPath jsonPath = addTimesheetResponse.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(400));
                assertThat(jsonPath.get("meta.message"), nullValue());
                assertThat(jsonPath.getString("meta.responseType.context"), is("Error while processing request"));
                assertThat(jsonPath.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonPath.getString("meta.timestamp"), notNullValue());
                assertThat(jsonPath.getString("errors[0].message"),
                                is("Start date and end date must be between Job Start Date and Job End Date"));
                assertThat(jsonPath.getInt("errors[0].errorType.code"), is(202));
                assertThat(jsonPath.getString("errors[0].errorType.context"), is("Generic Error"));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
        public void addTimeSheetUnauthorizedUserTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
                enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);

                List<TimesheetDate> timesheetDates = new ArrayList<>();

                TimesheetDate timesheetDate1 = new TimesheetDate();
                try {
                        java.lang.reflect.Method setStartDate = TimesheetDate.class.getMethod("setStartDate",
                                        long.class);
                        java.lang.reflect.Method setEndDate = TimesheetDate.class.getMethod("setEndDate", long.class);

                        setStartDate.invoke(timesheetDate1, (long) DEFAULT_CONTRACT_STAFFING_JOB_START_DATE_EPOCH);
                        setEndDate.invoke(timesheetDate1, 1751414400L);

                } catch (Exception e) {
                        throw new RuntimeException("TimesheetDate setters not available", e);
                }
                timesheetDates.add(timesheetDate1);

                TimesheetDate timesheetDate2 = new TimesheetDate();
                try {
                        java.lang.reflect.Method setStartDate = TimesheetDate.class.getMethod("setStartDate",
                                        long.class);
                        java.lang.reflect.Method setEndDate = TimesheetDate.class.getMethod("setEndDate", long.class);

                        setStartDate.invoke(timesheetDate2, 1751500800L);
                        setEndDate.invoke(timesheetDate2, 1751587200L);

                } catch (Exception e) {
                        throw new RuntimeException("TimesheetDate setters not available", e);
                }
                timesheetDates.add(timesheetDate2);

                String invalidAuthToken = albatrossAuthToken + "invalid";
                Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates,
                                invalidAuthToken);

                assertThat(addTimesheetResponse.statusCode(), is(401));

                JsonPath jsonPath = addTimesheetResponse.jsonPath();

                assertThat(jsonPath.getInt("meta.status"), is(401));
                assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
                assertThat(jsonPath.getInt("meta.responseType.code"), is(104));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Warning"));
                assertThat(jsonPath.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonPath.getString("meta.timestamp"), notNullValue());
                assertThat(jsonPath.getString("data"), is("Invalid token"));
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void addTimeSheetWithInvalidJobIdAndContractorIdTest() {
                int invalidJobId = 9999999; // Non-existent job ID
                int invalidContractorId = 8888888; // Non-existent contractor ID

                List<TimesheetDate> timesheetDates = new ArrayList<>();

                TimesheetDate timesheetDate1 = new TimesheetDate();
                try {
                        java.lang.reflect.Method setStartDate = TimesheetDate.class.getMethod("setStartDate",
                                        long.class);
                        java.lang.reflect.Method setEndDate = TimesheetDate.class.getMethod("setEndDate", long.class);

                        setStartDate.invoke(timesheetDate1, (long) DEFAULT_CONTRACT_STAFFING_JOB_START_DATE_EPOCH);
                        setEndDate.invoke(timesheetDate1, 1751414400L);

                } catch (Exception e) {
                        throw new RuntimeException("TimesheetDate setters not available", e);
                }
                timesheetDates.add(timesheetDate1);

                TimesheetDate timesheetDate2 = new TimesheetDate();
                try {
                        java.lang.reflect.Method setStartDate = TimesheetDate.class.getMethod("setStartDate",
                                        long.class);
                        java.lang.reflect.Method setEndDate = TimesheetDate.class.getMethod("setEndDate", long.class);

                        setStartDate.invoke(timesheetDate2, 1751500800L);
                        setEndDate.invoke(timesheetDate2, 1751587200L);

                } catch (Exception e) {
                        throw new RuntimeException("TimesheetDate setters not available", e);
                }
                timesheetDates.add(timesheetDate2);

                Response addTimesheetResponse = addTimeSheet(invalidJobId, Arrays.asList(invalidContractorId),
                                timesheetDates, albatrossAuthToken);

                assertThat(addTimesheetResponse.statusCode(), is(404));

                JsonPath jsonPath = addTimesheetResponse.jsonPath();

                assertThat(jsonPath.getInt("meta.status"), is(404));
                assertThat(jsonPath.get("meta.message"), nullValue());
                assertThat(jsonPath.getInt("meta.responseType.code"), is(101));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Error while processing request"));
                assertThat(jsonPath.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonPath.getString("meta.timestamp"), notNullValue());
                String expectedMessagePattern = "Some assignments are missing for Job Id: " + invalidJobId +
                                " and contractor Ids: [" + invalidContractorId + "]";
                assertThat(jsonPath.getString("errors[0].message"), is(expectedMessagePattern));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
        public void getAllTimesheetsTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
                enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, timesheetFrequency, 200, 0);
                Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                                albatrossAuthToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);
                JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
                List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");
                List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);
                Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates,
                                albatrossAuthToken);

                Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId, 1, 100, albatrossAuthToken);
                assertThat(getAllTimesheetsResponse.statusCode(), is(200));
                JsonPath jsonPath = getAllTimesheetsResponse.jsonPath();

                assertThat(jsonPath.getInt("meta.status"), is(200));
                assertThat(jsonPath.getString("meta.message"), is("Timesheets fetched successfully"));
                assertThat(jsonPath.getInt("meta.responseType.code"), is(103));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Request is successful"));
                assertThat(jsonPath.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonPath.getString("meta.timestamp"), notNullValue());

                assertThat(jsonPath.get("data"), notNullValue());
                List<Map<String, Object>> timesheets = jsonPath.getList("data");

                assertThat(timesheets.isEmpty(), is(false));

                if (timesheetFrequency == 2) {
                        assertThat(timesheets.size(), is(13));
                } else if (timesheetFrequency == 3) {
                        assertThat(timesheets.size(), is(7));
                } else if (timesheetFrequency == 4) {
                        assertThat(timesheets.size(), is(2));
                }

                long timesheetStartDate = jsonPath.getLong("data[0].timesheetPeriod.timesheetStartDate");
                long timesheetEndDate = jsonPath.getLong("data[0].timesheetPeriod.timesheetEndDate");
                long jobStartDate = jsonPath.getLong("data[0].jobDuration.jobStartDate");
                long jobEndDate = jsonPath.getLong("data[0].jobDuration.jobEndDate");

                assertThat(timesheetStartDate >= jobStartDate, is(true));
                assertThat(timesheetEndDate <= jobEndDate, is(true));
                assertThat(timesheetStartDate < timesheetEndDate, is(true));

                assertThat(jsonPath.getInt("data[0].id"), notNullValue());
                assertThat(jsonPath.get("data[0].timesheetPeriod"), notNullValue());
                assertThat(jsonPath.getLong("data[0].timesheetPeriod.timesheetStartDate"), notNullValue());
                assertThat(jsonPath.getLong("data[0].timesheetPeriod.timesheetEndDate"), notNullValue());
                assertThat(jsonPath.getInt("data[0].timesheetStatusId"), notNullValue());
                assertThat(jsonPath.get("data[0].jobDuration"), notNullValue());
                assertThat(jsonPath.getLong("data[0].jobDuration.jobStartDate"), notNullValue());
                assertThat(jsonPath.getLong("data[0].jobDuration.jobEndDate"), notNullValue());
                assertThat(jsonPath.getString("data[0].payCurrencySymbol"), notNullValue());
                assertThat(jsonPath.getString("data[0].billCurrencySymbol"), notNullValue());
                assertThat(jsonPath.getDouble("data[0].payRate"), notNullValue());
                assertThat(jsonPath.getDouble("data[0].billRate"), notNullValue());
                assertThat(jsonPath.get("data[0].contractor"), notNullValue());
                assertThat(jsonPath.getInt("data[0].contractor.id"), notNullValue());
                assertThat(jsonPath.getString("data[0].contractor.name"), notNullValue());
                assertThat(jsonPath.getLong("data[0].addedOn"), notNullValue());
                assertThat(jsonPath.getLong("data[0].updatedOn"), notNullValue());
                assertThat(jsonPath.get("data[0].addedBy"), notNullValue());
                assertThat(jsonPath.get("data[0].updatedBy"), notNullValue());
                assertThat(jsonPath.getInt("data[0].totalWorkTime"), notNullValue());

                getAllTimesheetsResponse.then().assertThat().body(
                                matchesJsonSchemaInClasspath("privateApi/contractStaffing/GetAllTimesheets.json"));
        }

        @Owner("Gaurav Prajapati")
        @Test
        public void getAllTimesheetsWithInvalidJobIdTest() {
                Response response = getAllTimesheets(9999999, 8888888, 1, 100, albatrossAuthToken);

                assertThat(response.statusCode() == 404 || response.statusCode() == 422, is(true));

                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(404));
                assertThat(jsonPath.getInt("meta.responseType.code"), is(101));
                assertThat(jsonPath.getString("meta.responseType.context"),
                                is("Error while processing request"));
                assertThat(jsonPath.getList("errors"), notNullValue());
                assertThat(jsonPath.getList("errors").isEmpty(), is(false));

        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void getAllTimesheetsUnauthorizedTest() {
                Response response = getAllTimesheets(123, 456, 1, 100, albatrossAuthToken + "invalid");

                assertThat(response.statusCode(), is(401));

                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("meta.status"), is(401));
                assertThat(jsonPath.getString("meta.message"), is("Unauthorised access"));
                assertThat(jsonPath.getString("meta.responseType.context"), is("Warning"));
                assertThat(jsonPath.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonPath.getString("meta.timestamp"), notNullValue());

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
                                { jobId, candidateId, userId, 2, },
                                { jobId, candidateId2, userId, 3 },
                                { jobId, candidateId3, userId, 4 },
                };
        }

}