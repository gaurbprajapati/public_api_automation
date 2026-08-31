package io.recruitcrm.scenariq.endpoint;

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
public class AutomationPromptTest extends ScenariqBaseTest {

    private String token;
    private long serviceId;
    private long endpointId;

    @BeforeClass
    public void setup() {
        token = setupScenariqAccount();
        serviceId = createTestService(token);

        // Trigger a scan and wait for completion to get endpoints
        Response scanResp = triggerScan(token, serviceId);
        assertThat("Prerequisite: scan trigger should return 202",
                scanResp.statusCode(), is(202));
        long scanId = scanResp.jsonPath().getLong("scanId");
        waitForScanCompletion(token, scanId, 120);

        // Get the scan report to find an endpoint id
        Response reportResp = getScanReport(token, scanId);
        assertThat("Prerequisite: scan report should return 200",
                reportResp.statusCode(), is(200));

        // Extract the first endpoint id from the report
        List<Object> endpoints = reportResp.jsonPath().getList("endpoints");
        if (endpoints != null && !endpoints.isEmpty()) {
            endpointId = reportResp.jsonPath().getLong("endpoints[0].id");
        } else {
            // Fallback: try to get endpoints from scan detail
            Response detailResp = getScanDetail(token, scanId);
            endpointId = detailResp.jsonPath().getLong("endpoints[0].id");
        }
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

    // ── Get prompt before generation ─────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"}, priority = 1)
    public void getAutomationPromptWhenNotGeneratedTest() {
        Response response = getAutomationPrompt(token, endpointId);

        assertThat("Get automation prompt when not generated should return 204",
                response.statusCode(), is(204));
    }

    // ── Generate prompt ──────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"}, priority = 2)
    public void generateAutomationPromptTemplateTest() {
        Response response = generateAutomationPrompt(token, endpointId, "TEMPLATE");

        assertThat("Generate automation prompt with TEMPLATE mode should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Prompt content should be present in the response",
                jsonPath.getString("prompt"), notNullValue());
        assertThat("Prompt content should not be empty",
                jsonPath.getString("prompt"), not(emptyOrNullString()));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"}, priority = 3)
    public void generateAutomationPromptRegenerateTest() {
        // Generate again to verify it can be re-generated
        Response response = generateAutomationPrompt(token, endpointId, "TEMPLATE");

        assertThat("Re-generate automation prompt should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Prompt content should be present on re-generation",
                jsonPath.getString("prompt"), notNullValue());
    }

    // ── Get prompt after generation ──────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"}, priority = 4)
    public void getAutomationPromptAfterGenerationTest() {
        // Ensure prompt has been generated first
        Response genResp = generateAutomationPrompt(token, endpointId, "TEMPLATE");
        assertThat("Prerequisite: generate prompt should return 200",
                genResp.statusCode(), is(200));

        Response response = getAutomationPrompt(token, endpointId);

        assertThat("Get automation prompt after generation should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Prompt content should be present after generation",
                jsonPath.getString("prompt"), notNullValue());
        assertThat("Prompt content should not be empty after generation",
                jsonPath.getString("prompt"), not(emptyOrNullString()));
    }

    // ── Record copy event ────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"}, priority = 5)
    public void recordPromptCopyTest() {
        Response response = recordPromptCopy(token, endpointId);

        assertThat("Record prompt copy event should return 204",
                response.statusCode(), is(204));
    }

    // ── Error path tests ─────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void generatePromptWithInvalidEndpointIdTest() {
        Response response = generateAutomationPrompt(token, 999999L, "TEMPLATE");

        assertThat("Generate prompt with invalid endpoint id should return 404",
                response.statusCode(), is(404));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void generatePromptWithInvalidTokenTest() {
        Response response = generateAutomationPrompt("invalid_token_abc123", endpointId, "TEMPLATE");

        assertThat("Generate prompt with invalid token should return 401",
                response.statusCode(), is(401));
    }
}
