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
public class GetScanReportTest extends ScenariqBaseTest {

    private String token;
    private long serviceId;
    private long completedScanId;

    @BeforeClass
    public void setup() {
        token = setupScenariqAccount();
        serviceId = createTestService(token);

        // Trigger a scan and wait for it to complete
        Response scanResp = triggerScan(token, serviceId);
        assertThat("Prerequisite: scan trigger should return 202",
                scanResp.statusCode(), is(202));
        completedScanId = scanResp.jsonPath().getLong("scanId");
        waitForScanCompletion(token, completedScanId, 120);
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
    public void getScanReportTest() {
        Response response = getScanReport(token, completedScanId);

        assertThat("Get scan report should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Scan id should be present in the report",
                jsonPath.get("scanId"), notNullValue());
        assertThat("Total endpoints count should be present",
                jsonPath.get("totalEndpoints"), notNullValue());
        assertThat("Total endpoints should be a non-negative number",
                jsonPath.getInt("totalEndpoints"), greaterThanOrEqualTo(0));
    }

    // ── Error path tests ─────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getScanReportWithInvalidScanIdTest() {
        Response response = getScanReport(token, 999999L);

        assertThat("Get scan report with invalid scan id should return 404",
                response.statusCode(), is(404));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getScanReportWithInvalidTokenTest() {
        Response response = getScanReport("invalid_token_abc123", completedScanId);

        assertThat("Get scan report with invalid token should return 401",
                response.statusCode(), is(401));
    }
}
