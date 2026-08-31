package io.recruitcrm.scenariq.scan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class TriggerScanTest extends ScenariqBaseTest {

    private String token;
    private long serviceId;

    @BeforeClass
    public void setup() {
        token = setupScenariqAccount();
        serviceId = createTestService(token);
    }

    // ── Helper ───────────────────────────────────────────────────────────

    private long waitForScanCompletion(String token, long scanId, int maxWaitSeconds) {
        for (int i = 0; i < maxWaitSeconds; i += 5) {
            Response r = getScanDetail(token, scanId);
            String status = r.jsonPath().getString("scanStatus");
            if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                return scanId;
            }
            try { Thread.sleep(5000); } catch (InterruptedException e) { break; }
        }
        return scanId;
    }

    // ── Happy path tests ─────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void triggerScanWithDefaultSettingsTest() {
        Response response = triggerScan(token, serviceId);

        assertThat("Trigger scan with default settings should return 202",
                response.statusCode(), is(202));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Scan id should be present in trigger scan response",
                jsonPath.get("scanId"), notNullValue());
        assertThat("Scan status should be IN_PROGRESS or QUEUED after triggering",
                jsonPath.getString("scanStatus"), anyOf(is("IN_PROGRESS"), is("QUEUED")));

        long scanId = jsonPath.getLong("scanId");
        waitForScanCompletion(token, scanId, 120);
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void triggerScanWithDeterministicModeTest() {
        long newServiceId = createTestService(token);

        Response response = triggerScan(token, newServiceId, false, "DETERMINISTIC", false);

        assertThat("Trigger scan with DETERMINISTIC mode should return 202",
                response.statusCode(), is(202));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Scan id should be present in deterministic scan response",
                jsonPath.get("scanId"), notNullValue());

        long scanId = jsonPath.getLong("scanId");
        waitForScanCompletion(token, scanId, 120);
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void triggerScanWithAiEnabledTest() {
        long newServiceId = createTestService(token);

        Response response = triggerScan(token, newServiceId, true, "AI_FULL", false);

        assertThat("Trigger scan with AI enabled should return 202",
                response.statusCode(), is(202));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Scan id should be present in AI-enabled scan response",
                jsonPath.get("scanId"), notNullValue());

        long scanId = jsonPath.getLong("scanId");
        waitForScanCompletion(token, scanId, 180);
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void triggerScanWithoutAiTest() {
        long newServiceId = createTestService(token);

        Response response = triggerScan(token, newServiceId, false, null, false);

        assertThat("Trigger scan without AI should return 202",
                response.statusCode(), is(202));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Scan id should be present in non-AI scan response",
                jsonPath.get("scanId"), notNullValue());

        long scanId = jsonPath.getLong("scanId");
        waitForScanCompletion(token, scanId, 120);
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void triggerScanForceFullScanTest() {
        long newServiceId = createTestService(token);

        Response response = triggerScan(token, newServiceId, false, "DETERMINISTIC", true);

        assertThat("Trigger scan with forceFullScan should return 202",
                response.statusCode(), is(202));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Scan id should be present in force-full-scan response",
                jsonPath.get("scanId"), notNullValue());

        long scanId = jsonPath.getLong("scanId");
        waitForScanCompletion(token, scanId, 120);
    }

    // ── Error path tests ─────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void triggerScanWithInsufficientCreditsTest() {
        // Create a new account with FREE plan (5 credits) and exhaust all credits
        Object[] newAccount = createScenariqAccount();
        String newToken = (String) newAccount[0];

        // Exhaust credits by triggering multiple scans on separate services
        for (int i = 0; i < 6; i++) {
            long svcId = createTestService(newToken);
            Response scanResp = triggerScan(newToken, svcId);
            if (scanResp.statusCode() == 202) {
                long scanId = scanResp.jsonPath().getLong("scanId");
                waitForScanCompletion(newToken, scanId, 120);
            }
        }

        // One more scan should fail with insufficient credits
        long extraServiceId = createTestService(newToken);
        Response response = triggerScan(newToken, extraServiceId);

        assertThat("Trigger scan with exhausted credits should return 403",
                response.statusCode(), is(403));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void triggerScanWhileAnotherInProgressTest() {
        long newServiceId = createTestService(token);

        // Trigger first scan
        Response firstScan = triggerScan(token, newServiceId);
        assertThat("Prerequisite: first scan trigger should return 202",
                firstScan.statusCode(), is(202));

        // Immediately trigger second scan on same service
        Response secondScan = triggerScan(token, newServiceId);

        assertThat("Trigger scan while another is in progress should return 409",
                secondScan.statusCode(), is(409));

        // Cleanup: wait for first scan to complete
        long scanId = firstScan.jsonPath().getLong("scanId");
        waitForScanCompletion(token, scanId, 120);
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void triggerScanWithInvalidServiceIdTest() {
        Response response = triggerScan(token, 999999L);

        assertThat("Trigger scan with invalid service id should return 404",
                response.statusCode(), is(404));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void triggerScanWithInvalidTokenTest() {
        Response response = triggerScan("invalid_token_abc123", serviceId);

        assertThat("Trigger scan with invalid token should return 401",
                response.statusCode(), is(401));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void triggerScanFromDifferentAccountTest() {
        // Create a second account
        Object[] otherAccount = createScenariqAccount();
        String otherToken = (String) otherAccount[0];

        // Try to trigger scan on the first account's service using the second account's token
        Response response = triggerScan(otherToken, serviceId);

        assertThat("Trigger scan on another account's service should return 404 (tenant isolation)",
                response.statusCode(), is(404));
    }

    // ── DataProvider tests ───────────────────────────────────────────────

    @DataProvider(name = "scanModes")
    public Object[][] scanModes() {
        return new Object[][] {
                {"DETERMINISTIC"},
                {"MISSING_ONLY"},
                {"ALL_ENDPOINTS"}
        };
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "scanModes", groups = {"scenariq", "nightly-build"})
    public void triggerScanWithAllModesTest(String mode) {
        long newServiceId = createTestService(token);

        Response response = triggerScan(token, newServiceId, false, mode, false);

        assertThat("Trigger scan with mode " + mode + " should return 202",
                response.statusCode(), is(202));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Scan id should be present for mode " + mode,
                jsonPath.get("scanId"), notNullValue());

        long scanId = jsonPath.getLong("scanId");
        waitForScanCompletion(token, scanId, 120);
    }
}
