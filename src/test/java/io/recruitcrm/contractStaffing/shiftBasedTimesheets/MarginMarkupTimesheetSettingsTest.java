package io.recruitcrm.contractStaffing.shiftBasedTimesheets;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
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

import java.math.BigDecimal;
import com.qa.api.util.Owner;

/**
 * Covers PAY-406 (Margin and Markup based Rates) for POST /v1/timesheet-settings — calculateChargeBy/
 * marginPercentage/markupPercentage. Per the current LLD, out-of-range margin/markup values are CLAMPED
 * (200, stored value adjusted) rather than rejected, and all 3 fields are @NotNull regardless of mode.
 * Complements the baseline coverage already in EnableTimeSheetTest — that class asserts the Fixed Rate
 * default contract; this class covers the margin/markup-specific modes and clamp/validation behavior.
 */
@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class MarginMarkupTimesheetSettingsTest extends ContractStaffingBaseTest {

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
        @Test(dataProvider = "marginMarkupTestData", groups = {"contract_staffing", "nightly-build"})
        public void enableTimesheetSettingsInMarginModeTest(int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                enableTimesheetWithChargeMode(candidateId, jobId, ownerAccountID, albatrossAuthToken,
                                timesheetFrequency, 0, 2, new BigDecimal("30.00"), new BigDecimal("42.85"), 200);

                Response response = fetchTimesheetSettings(jobId, candidateId);

                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("data.calculateChargeBy"), is(2));
                assertThat(jsonPath.getDouble("data.marginPercentage"), is(30.00));
                assertThat(jsonPath.getDouble("data.markupPercentage"), is(42.85));

                response.then().assertThat().body(
                                matchesJsonSchemaInClasspath("privateApi/contractStaffing/GetTimesheetSettings.json"));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "marginMarkupTestData", groups = {"contract_staffing", "nightly-build"})
        public void enableTimesheetSettingsInMarkupModeTest(int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                enableTimesheetWithChargeMode(candidateId, jobId, ownerAccountID, albatrossAuthToken,
                                timesheetFrequency, 0, 3, new BigDecimal("33.33"), new BigDecimal("50.00"), 200);

                Response response = fetchTimesheetSettings(jobId, candidateId);

                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("data.calculateChargeBy"), is(3));
                assertThat(jsonPath.getDouble("data.marginPercentage"), is(33.33));
                assertThat(jsonPath.getDouble("data.markupPercentage"), is(50.00));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "marginMarkupTestData", groups = {"contract_staffing", "nightly-build"})
        public void enableTimesheetSettingsInFixedRateModeTest(int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                enableTimesheetWithChargeMode(candidateId, jobId, ownerAccountID, albatrossAuthToken,
                                timesheetFrequency, 0, 1, BigDecimal.ZERO, BigDecimal.ZERO, 200);

                Response response = fetchTimesheetSettings(jobId, candidateId);

                JsonPath jsonPath = response.jsonPath();
                assertThat(jsonPath.getInt("data.calculateChargeBy"), is(1));
                assertThat(jsonPath.getDouble("data.marginPercentage"), is(0.0));
                assertThat(jsonPath.getDouble("data.markupPercentage"), is(0.0));
        }

        // ── Clamp behavior (out-of-range values are stored, not rejected) ─────────────────────

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "marginMarkupTestData", groups = {"contract_staffing", "nightly-build"})
        public void marginPercentageBelowMinimumIsClampedTest(int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                enableTimesheetWithChargeMode(candidateId, jobId, ownerAccountID, albatrossAuthToken,
                                timesheetFrequency, 0, 2, new BigDecimal("-150.00"), new BigDecimal("0.00"), 200);

                Response response = fetchTimesheetSettings(jobId, candidateId);
                assertThat("margin_percentage below -99.99 must be clamped to -99.99, not rejected",
                                response.jsonPath().getDouble("data.marginPercentage"), is(-99.99));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "marginMarkupTestData", groups = {"contract_staffing", "nightly-build"})
        public void marginPercentageAboveMaximumIsClampedTest(int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                enableTimesheetWithChargeMode(candidateId, jobId, ownerAccountID, albatrossAuthToken,
                                timesheetFrequency, 0, 2, new BigDecimal("150.00"), new BigDecimal("0.00"), 200);

                Response response = fetchTimesheetSettings(jobId, candidateId);
                assertThat("margin_percentage above 100 must be clamped to 100, not rejected",
                                response.jsonPath().getDouble("data.marginPercentage"), is(100.0));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "marginMarkupTestData", groups = {"contract_staffing", "nightly-build"})
        public void markupPercentageBelowMinimumIsClampedTest(int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                enableTimesheetWithChargeMode(candidateId, jobId, ownerAccountID, albatrossAuthToken,
                                timesheetFrequency, 0, 3, new BigDecimal("0.00"), new BigDecimal("-500.00"), 200);

                Response response = fetchTimesheetSettings(jobId, candidateId);
                assertThat("markup_percentage below -100 must be clamped to -100, not rejected",
                                response.jsonPath().getDouble("data.markupPercentage"), is(-100.0));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "marginMarkupTestData", groups = {"contract_staffing", "nightly-build"})
        public void markupPercentageAboveMaximumIsClampedTest(int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                enableTimesheetWithChargeMode(candidateId, jobId, ownerAccountID, albatrossAuthToken,
                                timesheetFrequency, 0, 3, new BigDecimal("0.00"), new BigDecimal("15000.00"), 200);

                Response response = fetchTimesheetSettings(jobId, candidateId);
                assertThat("markup_percentage above 10000 must be clamped to 10000, not rejected",
                                response.jsonPath().getDouble("data.markupPercentage"), is(10000.0));
        }

        // ── @NotNull / invalid enum validation (calculateChargeBy/marginPercentage/markupPercentage) ──

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "marginMarkupTestData", groups = {"contract_staffing", "nightly-build"})
        public void nullCalculateChargeByIsRejectedTest(int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                Response response = buildAndSendTimesheetSettingsWithChargeMode(candidateId, jobId, ownerAccountID,
                                albatrossAuthToken, timesheetFrequency, 0, null, BigDecimal.ZERO, BigDecimal.ZERO);

                assertThat(response.statusCode(), is(400));
                assertThat(response.jsonPath().getString("meta.message"), is("calculateChargeBy must not be null"));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "marginMarkupTestData", groups = {"contract_staffing", "nightly-build"})
        public void nullMarginPercentageIsRejectedTest(int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                Response response = buildAndSendTimesheetSettingsWithChargeMode(candidateId, jobId, ownerAccountID,
                                albatrossAuthToken, timesheetFrequency, 0, 2, null, BigDecimal.ZERO);

                assertThat(response.statusCode(), is(400));
                assertThat(response.jsonPath().getString("meta.message"), is("Margin percentage cannot be null"));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "marginMarkupTestData", groups = {"contract_staffing", "nightly-build"})
        public void nullMarkupPercentageIsRejectedTest(int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                Response response = buildAndSendTimesheetSettingsWithChargeMode(candidateId, jobId, ownerAccountID,
                                albatrossAuthToken, timesheetFrequency, 0, 3, BigDecimal.ZERO, null);

                assertThat(response.statusCode(), is(400));
                assertThat(response.jsonPath().getString("meta.message"), is("Markup percentage cannot be null"));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "marginMarkupTestData", groups = {"contract_staffing", "nightly-build"})
        public void invalidCalculateChargeByEnumValueIsRejectedTest(int jobId, int candidateId, int userId,
                        int timesheetFrequency) {
                Response response = buildAndSendTimesheetSettingsWithChargeMode(candidateId, jobId, ownerAccountID,
                                albatrossAuthToken, timesheetFrequency, 0, 5, BigDecimal.ZERO, BigDecimal.ZERO);

                assertThat(response.statusCode(), is(400));
                assertThat(response.jsonPath().getString("meta.message"), is("Invalid value for calculateChargeBy"));
        }

        // ── payRate/billRate = 0 is now valid (was rejected before this feature; @Positive -> @PositiveOrZero) ──

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "marginMarkupTestData", groups = {"contract_staffing", "nightly-build"})
        public void zeroPayRateIsNowAcceptedTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
                io.rcrm.api.pojo.albatross.contractStaffing.TimesheetSettings settings =
                                createDefaultTimesheetSettings(jobId, candidateId, ownerAccountID, timesheetFrequency, 0);
                settings.setPayRate(0);

                Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                                albatrossAuthToken, null, true, settings);

                assertThat("payRate=0 must be accepted now that the DTO uses @PositiveOrZero instead of @Positive",
                                response.statusCode(), is(200));
        }

        @Owner("Gaurav Prajapati")
        @Test(dataProvider = "marginMarkupTestData", groups = {"contract_staffing", "nightly-build"})
        public void zeroBillRateIsNowAcceptedTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
                io.rcrm.api.pojo.albatross.contractStaffing.TimesheetSettings settings =
                                createDefaultTimesheetSettings(jobId, candidateId, ownerAccountID, timesheetFrequency, 0);
                settings.setBillRate(0);

                Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                                albatrossAuthToken, null, true, settings);

                assertThat("billRate=0 must be accepted now that the DTO uses @PositiveOrZero instead of @Positive",
                                response.statusCode(), is(200));
        }

        // ── Helpers ─────────────────────────────────────────────────────────────────────────

        private Response fetchTimesheetSettings(int jobId, int candidateId) {
                Response response = retryApiCall(
                                () -> getTimesheetSettingsByJobAndContractor(jobId, candidateId, albatrossAuthToken),
                                5, 500, 500, resp -> resp.statusCode() == 200);
                assertThat(response.statusCode(), is(200));
                return response;
        }

        @DataProvider(parallel = true)
        public Object[][] marginMarkupTestData() {
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
