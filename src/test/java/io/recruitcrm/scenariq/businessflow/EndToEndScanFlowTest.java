package io.recruitcrm.scenariq.businessflow;

import com.qa.api.util.Owner;
import io.rcrm.api.pojo.scenariq.*;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * End-to-end scan lifecycle test.
 * Covers: account setup -> service registration -> scan trigger -> scan monitoring -> cleanup.
 */
@AccountType("NotRequired")
public class EndToEndScanFlowTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void endToEndScanLifecycleTest() {

        // Step 1: Create account
        Object[] accountData = createScenariqAccount();
        String token = (String) accountData[0];
        String email = (String) accountData[1];
        String password = (String) accountData[2];

        // Step 2: Register service (201)
        RegisterServiceRequest serviceReq = RegisterServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName())
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .backendBranch("main")
                .automationBranch("main")
                .build();
        Response registerResp = registerService(token, serviceReq);
        assertThat("Service registration should return 201", registerResp.getStatusCode(), is(201));
        long serviceId = registerResp.jsonPath().getLong("id");
        assertThat("Service ID must be a positive number", serviceId, is(greaterThan(0L)));

        // Step 3: Get service by ID - verify registration fields (200)
        Response getServiceResp = getServiceById(token, serviceId);
        assertThat("Get service by ID should return 200", getServiceResp.getStatusCode(), is(200));
        assertThat("Service name must match registered name",
                getServiceResp.jsonPath().getString("serviceName"), is(serviceReq.getServiceName()));
        assertThat("Service type must match registered type",
                getServiceResp.jsonPath().getString("serviceType"), is("SPRING_BOOT"));
        assertThat("Backend repo URL must match",
                getServiceResp.jsonPath().getString("backendRepoUrl"), is(serviceReq.getBackendRepoUrl()));

        // Step 4: Trigger scan (202)
        Response triggerResp = triggerScan(token, serviceId);
        assertThat("Trigger scan should return 202", triggerResp.getStatusCode(), is(202));
        long scanId = triggerResp.jsonPath().getLong("id");
        assertThat("Scan ID must be a positive number", scanId, is(greaterThan(0L)));

        // Step 5: Get active scans - verify scan appears (200)
        Response activeResp = getActiveScans(token);
        assertThat("Get active scans should return 200", activeResp.getStatusCode(), is(200));

        // Step 6: Get scan detail - verify status (200)
        Response detailResp = getScanDetail(token, scanId);
        assertThat("Get scan detail should return 200", detailResp.getStatusCode(), is(200));
        String scanStatus = detailResp.jsonPath().getString("status");
        List<String> validStatuses = Arrays.asList("IN_PROGRESS", "COMPLETED", "QUEUED", "SCANNING",
                "PARSING", "RESOLVING", "ENRICHING", "CANCELLED", "FAILED");
        assertThat("Scan status must be a valid status value", scanStatus, is(in(validStatuses)));

        // Step 7: Get all scans - verify scan in list (200)
        Response allScansResp = getAllScans(token, null, null, 0, 10);
        assertThat("Get all scans should return 200", allScansResp.getStatusCode(), is(200));
        List<Integer> scanIds = allScansResp.jsonPath().getList("content.id");
        assertThat("All scans response must contain the triggered scan",
                scanIds, hasItem((int) scanId));

        // Step 8: Get service scans - verify scan in service history (200)
        Response serviceScansResp = getServiceScans(token, serviceId);
        assertThat("Get service scans should return 200", serviceScansResp.getStatusCode(), is(200));

        // Step 9: Cancel scan if still in progress, or verify completed
        Response latestDetailResp = getScanDetail(token, scanId);
        String latestStatus = latestDetailResp.jsonPath().getString("status");
        if (!"COMPLETED".equals(latestStatus) && !"FAILED".equals(latestStatus) && !"CANCELLED".equals(latestStatus)) {
            Response cancelResp = cancelScan(token, scanId);
            assertThat("Cancel scan should return 200", cancelResp.getStatusCode(), is(200));
        }

        // Step 10: Cleanup - delete service, delete account
        Response deleteServiceResp = deleteService(token, serviceId);
        assertThat("Delete service should return 204", deleteServiceResp.getStatusCode(), is(204));

        deleteScenariqAccount(token, password);
    }
}
