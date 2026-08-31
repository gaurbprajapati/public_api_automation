package io.recruitcrm.scenariq.scan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.List;

@TestBase.AccountType("NotRequired")
public class GetScansTest extends ScenariqBaseTest {

    private String token;
    private long serviceId;
    private long completedScanId;

    @BeforeClass
    public void setup() {
        token = setupScenariqAccount();
        serviceId = createTestService(token);

        // Trigger and wait for a scan to complete so we have data to query
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

    // ── Active scans tests ───────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getActiveScansTest() {
        // Trigger a new scan so there is an active one
        long newServiceId = createTestService(token);
        Response scanResp = triggerScan(token, newServiceId);
        assertThat("Prerequisite: scan trigger should return 202",
                scanResp.statusCode(), is(202));

        Response response = getActiveScans(token);

        assertThat("Get active scans should return 200",
                response.statusCode(), is(200));

        List<?> scans = response.jsonPath().getList("$");
        assertThat("Active scans list should not be null",
                scans, notNullValue());
        assertThat("Active scans list should contain at least one scan",
                scans.size(), greaterThanOrEqualTo(1));

        // Cleanup: wait for scan to complete
        long scanId = scanResp.jsonPath().getLong("scanId");
        waitForScanCompletion(token, scanId, 120);
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getActiveScansWithNoActiveScanTest() {
        // Create a fresh account with no scans running
        Object[] newAccount = createScenariqAccount();
        String newToken = (String) newAccount[0];

        Response response = getActiveScans(newToken);

        assertThat("Get active scans with none running should return 200",
                response.statusCode(), is(200));

        List<?> scans = response.jsonPath().getList("$");
        assertThat("Active scans list should be empty when no scans are running",
                scans, hasSize(0));
    }

    // ── Scan detail tests ────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getScanDetailTest() {
        Response response = getScanDetail(token, completedScanId);

        assertThat("Get scan detail should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Scan id should match the requested scan id",
                jsonPath.getLong("scanId"), is(completedScanId));
        assertThat("Scan status should be present",
                jsonPath.getString("scanStatus"), notNullValue());
        assertThat("Service id should be present in scan detail",
                jsonPath.get("serviceId"), notNullValue());
        assertThat("Created at timestamp should be present",
                jsonPath.getString("createdAt"), notNullValue());
        assertThat("Scan status should be COMPLETED or FAILED",
                jsonPath.getString("scanStatus"), anyOf(is("COMPLETED"), is("FAILED")));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getScanDetailWithInvalidIdTest() {
        Response response = getScanDetail(token, 999999L);

        assertThat("Get scan detail with invalid id should return 404",
                response.statusCode(), is(404));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getScanDetailFromDifferentAccountTest() {
        Object[] otherAccount = createScenariqAccount();
        String otherToken = (String) otherAccount[0];

        Response response = getScanDetail(otherToken, completedScanId);

        assertThat("Get scan detail from different account should return 404 (tenant isolation)",
                response.statusCode(), is(404));
    }

    // ── Get all scans tests ──────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getAllScansTest() {
        Response response = getAllScans(token, null, null, 0, 10);

        assertThat("Get all scans should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Content list should be present in paginated response",
                jsonPath.getList("content"), notNullValue());
        assertThat("Total elements should be at least 1",
                jsonPath.getInt("totalElements"), greaterThanOrEqualTo(1));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getAllScansWithServiceFilterTest() {
        Response response = getAllScans(token, serviceId, null, 0, 10);

        assertThat("Get all scans filtered by service should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        List<?> content = jsonPath.getList("content");
        assertThat("Content list should not be null when filtering by service",
                content, notNullValue());
        assertThat("All returned scans should belong to the filtered service",
                content.size(), greaterThanOrEqualTo(1));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getAllScansWithStatusFilterTest() {
        Response response = getAllScans(token, null, "COMPLETED", 0, 10);

        assertThat("Get all scans filtered by COMPLETED status should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Content list should be present when filtering by status",
                jsonPath.getList("content"), notNullValue());
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getAllScansWithPaginationTest() {
        Response response = getAllScans(token, null, null, 0, 5);

        assertThat("Get all scans with pagination should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Page size should match requested size",
                jsonPath.getInt("size"), is(5));
        assertThat("Page number should match requested page",
                jsonPath.getInt("number"), is(0));
        assertThat("Total pages should be present",
                jsonPath.get("totalPages"), notNullValue());
        assertThat("Total elements should be present",
                jsonPath.get("totalElements"), notNullValue());
    }

    // ── Service scans tests ──────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getServiceScansTest() {
        Response response = getServiceScans(token, serviceId);

        assertThat("Get service scans should return 200",
                response.statusCode(), is(200));

        List<?> scans = response.jsonPath().getList("$");
        assertThat("Service scans list should not be null",
                scans, notNullValue());
        assertThat("Service scans list should contain at least one scan",
                scans.size(), greaterThanOrEqualTo(1));
    }

    // ── Auth error tests ─────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getAllScansWithInvalidTokenTest() {
        Response response = getAllScans("invalid_token_abc123", null, null, 0, 10);

        assertThat("Get all scans with invalid token should return 401",
                response.statusCode(), is(401));
    }
}
