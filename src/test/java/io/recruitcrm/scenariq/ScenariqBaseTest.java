package io.recruitcrm.scenariq;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.scenariq.JavaFakerScenariq;
import io.rcrm.api.pojo.scenariq.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.*;

/**
 * Base test class for ScenarIQ API automation.
 *
 * ScenarIQ is a separate backend service with its own JWT authentication
 * (signup/login endpoints), independent of Reaper account provisioning.
 *
 * Subclasses should annotate with {@code @AccountType("NotRequired")} to
 * skip Reaper provisioning, then call {@link #setupScenariqAccount()} or
 * {@link #createScenariqAccount()} in their {@code @BeforeClass} to
 * provision a fresh ScenarIQ account.
 *
 * The base URL is read from system property {@code -Dscenariq_url}
 * (default: {@code http://localhost:8081/api/coverage}).
 */
public class ScenariqBaseTest extends TestBase {

    protected static final String scenariqBaseURL;
    protected static final JavaFakerScenariq scenariqFaker = new JavaFakerScenariq();

    // ThreadManager keys for ScenarIQ-specific state
    protected static final String SCENARIQ_TOKEN_KEY = "scenariq_jwt_token";
    protected static final String SCENARIQ_USER_ID_KEY = "scenariq_user_id";
    protected static final String SCENARIQ_ACCOUNT_ID_KEY = "scenariq_account_id";
    protected static final String SCENARIQ_EMAIL_KEY = "scenariq_email";
    protected static final String SCENARIQ_PASSWORD_KEY = "scenariq_password";

    static {
        scenariqBaseURL = System.getProperty("scenariq_url", "http://localhost:8081/api/coverage");
    }

    // ── Auth helpers ──────────────────────────────────────────────────────

    protected Response signup(SignupRequest request) {
        return RestClient.doPost("JSON", scenariqBaseURL, "auth/signup", null, null, true, request);
    }

    protected Response login(LoginRequest request) {
        return RestClient.doPost("JSON", scenariqBaseURL, "auth/login", null, null, true, request);
    }

    protected Response logout(String token) {
        return RestClient.doPost("JSON", scenariqBaseURL, "auth/logout", token, null, true, null);
    }

    protected Response getProfile(String token) {
        return RestClient.doGet("JSON", scenariqBaseURL, "auth/profile", token, null, null, true);
    }

    protected Response updateProfile(String token, UpdateProfileRequest request) {
        return RestClient.doPut("JSON", scenariqBaseURL, "auth/profile", token, null, true, request);
    }

    protected Response changePassword(String token, ChangePasswordRequest request) {
        return RestClient.doPut("JSON", scenariqBaseURL, "auth/profile/password", token, null, true, request);
    }

    protected Response updateGitToken(String token, UpdateGitTokenRequest request) {
        return RestClient.doPut("JSON", scenariqBaseURL, "auth/profile/git-token", token, null, true, request);
    }

    protected Response deleteAccount(String token, CancellationFeedbackRequest request) {
        return RestClient.doDelete("JSON", scenariqBaseURL, "auth/profile", token, null, null, true, request);
    }

    protected Response validateResetToken(String resetToken) {
        Map<String, String> params = new HashMap<>();
        params.put("token", resetToken);
        return RestClient.doGet("JSON", scenariqBaseURL, "auth/reset-password/validate", null, params, null, true);
    }

    protected Response resetPassword(ResetPasswordRequest request) {
        return RestClient.doPost("JSON", scenariqBaseURL, "auth/reset-password", null, null, true, request);
    }

    // ── Account / Team helpers ────────────────────────────────────────────

    protected Response inviteUser(String token, InviteUserRequest request) {
        return RestClient.doPost("JSON", scenariqBaseURL, "accounts/invite", token, null, true, request);
    }

    protected Response getInvitations(String token) {
        return RestClient.doGet("JSON", scenariqBaseURL, "accounts/invitations", token, null, null, true);
    }

    protected Response resendInvitation(String token, long invitationId) {
        return RestClient.doPost("JSON", scenariqBaseURL,
                "accounts/invitations/" + invitationId + "/resend", token, null, true, null);
    }

    protected Response acceptInvitation(AcceptInvitationRequest request) {
        return RestClient.doPost("JSON", scenariqBaseURL, "accounts/accept-invitation", null, null, true, request);
    }

    protected Response getAccountUsers(String token) {
        return RestClient.doGet("JSON", scenariqBaseURL, "accounts/users", token, null, null, true);
    }

    protected Response updateAccountUser(String token, long userId, UpdateAccountUserRequest request) {
        return RestClient.doPut("JSON", scenariqBaseURL, "accounts/users/" + userId, token, null, true, request);
    }

    protected Response deleteAccountUser(String token, long userId) {
        return RestClient.doDelete("JSON", scenariqBaseURL, "accounts/users/" + userId, token, null, null, true);
    }

    protected Response generateResetLink(String token, long userId) {
        return RestClient.doPost("JSON", scenariqBaseURL,
                "accounts/users/" + userId + "/reset-link", token, null, true, null);
    }

    // ── Service helpers ───────────────────────────────────────────────────

    protected Response registerService(String token, RegisterServiceRequest request) {
        return RestClient.doPost("JSON", scenariqBaseURL, "services", token, null, true, request);
    }

    protected Response getServices(String token) {
        return RestClient.doGet("JSON", scenariqBaseURL, "services", token, null, null, true);
    }

    protected Response getServiceById(String token, long serviceId) {
        return RestClient.doGet("JSON", scenariqBaseURL, "services/" + serviceId, token, null, null, true);
    }

    protected Response updateService(String token, long serviceId, UpdateServiceRequest request) {
        return RestClient.doPut("JSON", scenariqBaseURL, "services/" + serviceId, token, null, true, request);
    }

    protected Response deleteService(String token, long serviceId) {
        return RestClient.doDelete("JSON", scenariqBaseURL, "services/" + serviceId, token, null, null, true);
    }

    protected Response updateServiceToken(String token, long serviceId, UpdateServiceTokenRequest request) {
        return RestClient.doPut("JSON", scenariqBaseURL, "services/" + serviceId + "/token", token, null, true, request);
    }

    // ── Scan helpers ──────────────────────────────────────────────────────

    protected Response triggerScan(String token, long serviceId, boolean withAi, String aiScanMode, boolean forceFullScan) {
        Map<String, String> params = new HashMap<>();
        params.put("withAi", String.valueOf(withAi));
        if (aiScanMode != null) {
            params.put("aiScanMode", aiScanMode);
        }
        params.put("forceFullScan", String.valueOf(forceFullScan));
        return RestClient.doPost("JSON", scenariqBaseURL,
                "services/" + serviceId + "/scan", token, params, true, null);
    }

    protected Response triggerScan(String token, long serviceId) {
        return triggerScan(token, serviceId, false, "DETERMINISTIC", false);
    }

    protected Response resumeEnrichment(String token, long scanId) {
        return RestClient.doPost("JSON", scenariqBaseURL,
                "scans/" + scanId + "/enrich", token, null, true, null);
    }

    protected Response cancelScan(String token, long scanId) {
        return RestClient.doPost("JSON", scenariqBaseURL,
                "scans/" + scanId + "/cancel", token, null, true, null);
    }

    protected Response getAllScans(String token, Long serviceId, String status, int page, int size) {
        Map<String, String> params = new HashMap<>();
        if (serviceId != null) {
            params.put("serviceId", String.valueOf(serviceId));
        }
        if (status != null) {
            params.put("status", status);
        }
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        return RestClient.doGet("JSON", scenariqBaseURL, "scans", token, params, null, true);
    }

    protected Response getActiveScans(String token) {
        return RestClient.doGet("JSON", scenariqBaseURL, "scans/active", token, null, null, true);
    }

    protected Response getScanDetail(String token, long scanId) {
        return RestClient.doGet("JSON", scenariqBaseURL, "scans/" + scanId, token, null, null, true);
    }

    protected Response getScanReport(String token, long scanId) {
        return RestClient.doGet("JSON", scenariqBaseURL, "scans/" + scanId + "/report", token, null, null, true);
    }

    protected Response getServiceScans(String token, long serviceId) {
        return RestClient.doGet("JSON", scenariqBaseURL, "services/" + serviceId + "/scans", token, null, null, true);
    }

    // ── Endpoint helpers ──────────────────────────────────────────────────

    protected Response getAutomationPrompt(String token, long endpointId) {
        return RestClient.doGet("JSON", scenariqBaseURL,
                "endpoints/" + endpointId + "/automation-prompt", token, null, null, true);
    }

    protected Response generateAutomationPrompt(String token, long endpointId, String mode) {
        Map<String, String> params = new HashMap<>();
        if (mode != null) {
            params.put("mode", mode);
        }
        return RestClient.doPost("JSON", scenariqBaseURL,
                "endpoints/" + endpointId + "/automation-prompt", token, params, true, null);
    }

    protected Response recordPromptCopy(String token, long endpointId) {
        return RestClient.doPost("JSON", scenariqBaseURL,
                "endpoints/" + endpointId + "/automation-prompt/copied", token, null, true, null);
    }

    // ── Feedback helpers ──────────────────────────────────────────────────

    protected Response submitFeedback(String token, FeedbackRequest request) {
        return RestClient.doPost("JSON", scenariqBaseURL, "feedback", token, null, true, request);
    }

    // ── Health helpers ────────────────────────────────────────────────────

    protected Response healthCheck() {
        return RestClient.doGet("JSON", scenariqBaseURL, "health", null, null, null, true);
    }

    // ── Utility helpers ───────────────────────────────────────────────────

    /**
     * Creates a fresh ScenarIQ account via the signup endpoint.
     *
     * @return Object array: [token, email, password, userId, accountId, name, accountName]
     */
    protected Object[] createScenariqAccount() {
        JavaFakerScenariq faker = new JavaFakerScenariq();
        String email = faker.getEmail();
        String password = faker.getPassword();
        String name = faker.getName();
        String accountName = faker.getAccountName();

        SignupRequest signupReq = SignupRequest.builder()
                .name(name).email(email).password(password).accountName(accountName).build();
        Response signupResponse = signup(signupReq);
        Assert.assertEquals(signupResponse.getStatusCode(), 201,
                "Prerequisite: ScenarIQ signup failed with status " + signupResponse.getStatusCode());

        String token = signupResponse.jsonPath().getString("token");
        long userId = signupResponse.jsonPath().getLong("user.id");
        long accountId = signupResponse.jsonPath().getLong("user.accountId");

        return new Object[]{token, email, password, userId, accountId, name, accountName};
    }

    /**
     * Creates a fresh ScenarIQ account and stores credentials in ThreadManager
     * for the current test thread.
     *
     * @return the JWT token for the newly created account
     */
    protected String setupScenariqAccount() {
        Object[] accountData = createScenariqAccount();
        String token = (String) accountData[0];
        String email = (String) accountData[1];
        String password = (String) accountData[2];

        ThreadManager.setValue(SCENARIQ_TOKEN_KEY, token);
        ThreadManager.setValue(SCENARIQ_EMAIL_KEY, email);
        ThreadManager.setValue(SCENARIQ_PASSWORD_KEY, password);
        ThreadManager.setValue(SCENARIQ_USER_ID_KEY, accountData[3]);
        ThreadManager.setValue(SCENARIQ_ACCOUNT_ID_KEY, accountData[4]);

        return token;
    }

    protected String getScenariqToken() {
        return (String) ThreadManager.getValue(SCENARIQ_TOKEN_KEY);
    }

    protected String getScenariqEmail() {
        return (String) ThreadManager.getValue(SCENARIQ_EMAIL_KEY);
    }

    protected String getScenariqPassword() {
        return (String) ThreadManager.getValue(SCENARIQ_PASSWORD_KEY);
    }

    /**
     * Registers a test service under the given account and returns the service ID.
     */
    protected long createTestService(String token) {
        RegisterServiceRequest req = RegisterServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName())
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .backendBranch("main")
                .automationBranch("main")
                .build();
        Response response = registerService(token, req);
        Assert.assertEquals(response.getStatusCode(), 201,
                "Prerequisite: Service registration failed with status " + response.getStatusCode());
        return response.jsonPath().getLong("id");
    }

    /**
     * Invites a user to the current account, accepts the invitation, and returns
     * the new member's credentials.
     *
     * @param ownerToken JWT token of the account owner
     * @param role       role to assign (e.g. "MEMBER", "ADMIN")
     * @return Object array: [memberToken, memberUserId, memberEmail, memberPassword]
     */
    protected Object[] inviteAndAcceptUser(String ownerToken, String role) {
        JavaFakerScenariq faker = new JavaFakerScenariq();
        String memberEmail = faker.getEmail();
        String memberPassword = faker.getPassword();
        String memberName = faker.getName();

        InviteUserRequest inviteReq = InviteUserRequest.builder()
                .email(memberEmail).role(role).build();
        Response inviteResp = inviteUser(ownerToken, inviteReq);
        Assert.assertEquals(inviteResp.getStatusCode(), 201,
                "Prerequisite: Invitation failed with status " + inviteResp.getStatusCode());

        String invitationToken = inviteResp.jsonPath().getString("invitationLink");
        // Extract token from invitation link
        if (invitationToken != null && invitationToken.contains("/")) {
            invitationToken = invitationToken.substring(invitationToken.lastIndexOf("/") + 1);
        }
        if (invitationToken != null && invitationToken.contains("token=")) {
            invitationToken = invitationToken.split("token=")[1];
            if (invitationToken.contains("&")) {
                invitationToken = invitationToken.split("&")[0];
            }
        }

        AcceptInvitationRequest acceptReq = AcceptInvitationRequest.builder()
                .invitationToken(invitationToken)
                .name(memberName)
                .password(memberPassword)
                .build();
        Response acceptResp = acceptInvitation(acceptReq);
        Assert.assertEquals(acceptResp.getStatusCode(), 200,
                "Prerequisite: Invitation acceptance failed with status " + acceptResp.getStatusCode());

        String memberToken = acceptResp.jsonPath().getString("token");
        long memberUserId = acceptResp.jsonPath().getLong("user.id");

        return new Object[]{memberToken, memberUserId, memberEmail, memberPassword};
    }

    /**
     * Deletes a ScenarIQ account (cleanup helper).
     */
    protected void deleteScenariqAccount(String token, String password) {
        CancellationFeedbackRequest req = CancellationFeedbackRequest.builder()
                .password(password)
                .reasons(Arrays.asList("Testing cleanup"))
                .build();
        deleteAccount(token, req);
    }
}
