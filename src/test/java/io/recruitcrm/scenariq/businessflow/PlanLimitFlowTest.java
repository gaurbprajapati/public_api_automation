package io.recruitcrm.scenariq.businessflow;

import com.qa.api.util.Owner;
import io.rcrm.api.pojo.scenariq.*;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Plan limit enforcement flow tests.
 * Covers: FREE plan service limit, credit limit, and invite limit enforcement.
 */
@AccountType("NotRequired")
public class PlanLimitFlowTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void freePlanServiceLimitTest() {

        // Step 1: Create account (FREE plan)
        Object[] accountData = createScenariqAccount();
        String token = (String) accountData[0];
        String password = (String) accountData[2];

        // Verify this is a FREE plan account
        Response profileResp = getProfile(token);
        assertThat("Get profile should return 200", profileResp.getStatusCode(), is(200));
        assertThat("New account should be on FREE plan",
                profileResp.jsonPath().getString("planType"), is("FREE"));
        int maxServices = profileResp.jsonPath().getInt("maxServices");
        assertThat("FREE plan must have a service limit", maxServices, is(greaterThan(0)));

        // Step 2: Register first service - 201
        long firstServiceId = createTestService(token);
        assertThat("First service registration should succeed", firstServiceId, is(greaterThan(0L)));

        // Step 3: Register second service - 403 (PLAN_LIMIT_EXCEEDED)
        RegisterServiceRequest secondReq = RegisterServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName())
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .backendBranch("main")
                .automationBranch("main")
                .build();
        Response secondResp = registerService(token, secondReq);
        assertThat("Second service registration should be rejected with 403",
                secondResp.getStatusCode(), is(403));

        // Step 4: Verify error response has limitType and message
        String responseBody = secondResp.getBody().asString();
        assertThat("Error response must indicate plan limit exceeded",
                responseBody, containsString("PLAN_LIMIT_EXCEEDED"));
        assertThat("Error response must indicate SERVICE_LIMIT type",
                responseBody, containsString("SERVICE_LIMIT"));

        // Cleanup
        deleteService(token, firstServiceId);
        deleteScenariqAccount(token, password);
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void freePlanCreditLimitTest() {

        // Step 1: Create account (FREE plan, limited credits)
        Object[] accountData = createScenariqAccount();
        String token = (String) accountData[0];
        String password = (String) accountData[2];

        Response profileResp = getProfile(token);
        assertThat("Get profile should return 200", profileResp.getStatusCode(), is(200));
        int totalCredits = profileResp.jsonPath().getInt("scanCreditsTotal");
        int remainingCredits = profileResp.jsonPath().getInt("scanCreditsRemaining");
        assertThat("FREE plan must have scan credits", totalCredits, is(greaterThan(0)));

        // Step 2: Register service for scanning
        long serviceId = createTestService(token);

        // Step 3: Trigger scans until credits exhausted
        int scansTriggered = 0;
        for (int i = 0; i < remainingCredits; i++) {
            Response scanResp = triggerScan(token, serviceId);
            if (scanResp.getStatusCode() == 202) {
                scansTriggered++;
                long scanId = scanResp.jsonPath().getLong("id");
                // Cancel immediately to free up processing
                cancelScan(token, scanId);
            } else {
                break;
            }
        }

        // Step 4: Next scan should fail - 403 (CREDIT_LIMIT)
        Response limitResp = triggerScan(token, serviceId);
        if (scansTriggered >= remainingCredits) {
            assertThat("Scan after credits exhausted should return 403",
                    limitResp.getStatusCode(), is(403));
            String responseBody = limitResp.getBody().asString();
            assertThat("Error response must indicate credit limit reached",
                    responseBody, containsString("CREDIT_LIMIT"));
        }

        // Cleanup
        deleteService(token, serviceId);
        deleteScenariqAccount(token, password);
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void freePlanInviteLimitTest() {

        // Step 1: Create account (FREE plan, max invites)
        Object[] accountData = createScenariqAccount();
        String token = (String) accountData[0];
        String password = (String) accountData[2];

        Response profileResp = getProfile(token);
        assertThat("Get profile should return 200", profileResp.getStatusCode(), is(200));
        int maxInvites = profileResp.jsonPath().getInt("maxInvites");
        assertThat("FREE plan must have an invite limit", maxInvites, is(greaterThan(0)));

        // Step 2: Invite users up to the limit - all 201
        for (int i = 0; i < maxInvites; i++) {
            InviteUserRequest inviteReq = InviteUserRequest.builder()
                    .email(scenariqFaker.getEmail()).role("MEMBER").build();
            Response inviteResp = inviteUser(token, inviteReq);
            assertThat("Invite #" + (i + 1) + " should return 201",
                    inviteResp.getStatusCode(), is(201));
        }

        // Step 3: Next invite should fail - 403 (INVITE_LIMIT)
        InviteUserRequest extraInviteReq = InviteUserRequest.builder()
                .email(scenariqFaker.getEmail()).role("MEMBER").build();
        Response extraInviteResp = inviteUser(token, extraInviteReq);
        assertThat("Invite beyond limit should return 403",
                extraInviteResp.getStatusCode(), is(403));

        String responseBody = extraInviteResp.getBody().asString();
        assertThat("Error response must indicate invite limit reached",
                responseBody, containsString("INVITE_LIMIT"));

        // Cleanup
        deleteScenariqAccount(token, password);
    }
}
