package io.recruitcrm.scenariq.scan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class ResumeEnrichmentTest extends ScenariqBaseTest {

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
    public void resumeEnrichmentOnCompletedScanTest() {
        // Trigger a DETERMINISTIC scan (no AI) and wait for it to complete
        long newServiceId = createTestService(token);
        Response scanResp = triggerScan(token, newServiceId, false, "DETERMINISTIC", false);
        assertThat("Prerequisite: scan trigger should return 202",
                scanResp.statusCode(), is(202));
        long scanId = scanResp.jsonPath().getLong("scanId");
        waitForScanCompletion(token, scanId, 120);

        // Verify the scan completed before resuming
        Response detail = getScanDetail(token, scanId);
        assertThat("Prerequisite: scan should be COMPLETED before resuming enrichment",
                detail.jsonPath().getString("scanStatus"), is("COMPLETED"));

        // Resume AI enrichment on the completed deterministic scan
        Response response = resumeEnrichment(token, scanId);

        assertThat("Resume enrichment on completed deterministic scan should return 202",
                response.statusCode(), is(202));
    }

    // ── Error path tests ─────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void resumeEnrichmentOnInProgressScanTest() {
        long newServiceId = createTestService(token);
        Response scanResp = triggerScan(token, newServiceId);
        assertThat("Prerequisite: scan trigger should return 202",
                scanResp.statusCode(), is(202));
        long scanId = scanResp.jsonPath().getLong("scanId");

        // Try to resume enrichment while scan is still in progress
        Response response = resumeEnrichment(token, scanId);

        assertThat("Resume enrichment on in-progress scan should return 409",
                response.statusCode(), is(409));

        // Cleanup: wait for scan to complete
        waitForScanCompletion(token, scanId, 120);
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void resumeEnrichmentWithInvalidScanIdTest() {
        Response response = resumeEnrichment(token, 999999L);

        assertThat("Resume enrichment with invalid scan id should return 404",
                response.statusCode(), is(404));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void resumeEnrichmentWithInvalidTokenTest() {
        Response response = resumeEnrichment("invalid_token_abc123", 1L);

        assertThat("Resume enrichment with invalid token should return 401",
                response.statusCode(), is(401));
    }
}
