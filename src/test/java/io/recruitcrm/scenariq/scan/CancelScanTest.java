package io.recruitcrm.scenariq.scan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class CancelScanTest extends ScenariqBaseTest {

    private String token;
    private long serviceId;

    @BeforeClass
    public void setup() {
        token = setupScenariqAccount();
        serviceId = createTestService(token);
    }

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

    // ── Happy path ───────────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void cancelInProgressScanTest() {
        long newServiceId = createTestService(token);
        Response scanResp = triggerScan(token, newServiceId);
        assertThat("Prerequisite: scan trigger should return 202",
                scanResp.statusCode(), is(202));
        long scanId = scanResp.jsonPath().getLong("scanId");

        Response response = cancelScan(token, scanId);

        assertThat("Cancel in-progress scan should return 200",
                response.statusCode(), is(200));

        // Verify the scan status is now CANCELLED
        Response detail = getScanDetail(token, scanId);
        assertThat("Scan status should be CANCELLED after cancellation",
                detail.jsonPath().getString("scanStatus"), is("CANCELLED"));
    }

    // ── Error path tests ─────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void cancelAlreadyCompletedScanTest() {
        long newServiceId = createTestService(token);
        Response scanResp = triggerScan(token, newServiceId);
        assertThat("Prerequisite: scan trigger should return 202",
                scanResp.statusCode(), is(202));
        long scanId = scanResp.jsonPath().getLong("scanId");

        // Wait for scan to complete
        waitForScanCompletion(token, scanId, 120);

        Response response = cancelScan(token, scanId);

        assertThat("Cancel already completed scan should return 409",
                response.statusCode(), is(409));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void cancelAlreadyCancelledScanTest() {
        long newServiceId = createTestService(token);
        Response scanResp = triggerScan(token, newServiceId);
        assertThat("Prerequisite: scan trigger should return 202",
                scanResp.statusCode(), is(202));
        long scanId = scanResp.jsonPath().getLong("scanId");

        // Cancel the scan first
        Response firstCancel = cancelScan(token, scanId);
        assertThat("Prerequisite: first cancel should return 200",
                firstCancel.statusCode(), is(200));

        // Try to cancel again
        Response response = cancelScan(token, scanId);

        assertThat("Cancel already cancelled scan should return 409",
                response.statusCode(), is(409));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void cancelScanWithInvalidIdTest() {
        Response response = cancelScan(token, 999999L);

        assertThat("Cancel scan with invalid id should return 404",
                response.statusCode(), is(404));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void cancelScanWithInvalidTokenTest() {
        Response response = cancelScan("invalid_token_abc123", 1L);

        assertThat("Cancel scan with invalid token should return 401",
                response.statusCode(), is(401));
    }
}
