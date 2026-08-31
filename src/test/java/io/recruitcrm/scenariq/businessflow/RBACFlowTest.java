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
 * Role-based access control flow tests.
 * Covers: OWNER full access, MEMBER limited access, ADMIN mid-level access.
 */
@AccountType("NotRequired")
public class RBACFlowTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void rbacOwnerFullAccessTest() {

        // Step 1: Create account (owner)
        Object[] ownerData = createScenariqAccount();
        String ownerToken = (String) ownerData[0];
        String ownerPassword = (String) ownerData[2];

        // Step 2: Register service - 201
        long serviceId = createTestService(ownerToken);
        Response getServiceResp = getServiceById(ownerToken, serviceId);
        assertThat("Owner should be able to get service (200)",
                getServiceResp.getStatusCode(), is(200));

        // Step 3: Update service - 200
        UpdateServiceRequest updateReq = UpdateServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName()).build();
        Response updateResp = updateService(ownerToken, serviceId, updateReq);
        assertThat("Owner should be able to update service (200)",
                updateResp.getStatusCode(), is(200));

        // Step 4: Delete service - 204
        Response deleteResp = deleteService(ownerToken, serviceId);
        assertThat("Owner should be able to delete service (204)",
                deleteResp.getStatusCode(), is(204));

        // Step 5: Invite user - 201
        InviteUserRequest inviteReq = InviteUserRequest.builder()
                .email(scenariqFaker.getEmail()).role("MEMBER").build();
        Response inviteResp = inviteUser(ownerToken, inviteReq);
        assertThat("Owner should be able to invite user (201)",
                inviteResp.getStatusCode(), is(201));

        // Cleanup
        deleteScenariqAccount(ownerToken, ownerPassword);
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void rbacMemberLimitedAccessTest() {

        // Step 1: Create account, invite MEMBER
        Object[] ownerData = createScenariqAccount();
        String ownerToken = (String) ownerData[0];
        String ownerPassword = (String) ownerData[2];

        Object[] memberData = inviteAndAcceptUser(ownerToken, "MEMBER");
        String memberToken = (String) memberData[0];
        long memberUserId = (long) memberData[1];

        // Step 2: MEMBER registers service - 201
        long serviceId = createTestService(memberToken);
        assertThat("Member should be able to register a service", serviceId, is(greaterThan(0L)));

        // Step 3: MEMBER updates own service - 200
        UpdateServiceRequest updateReq = UpdateServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName()).build();
        Response updateResp = updateService(memberToken, serviceId, updateReq);
        assertThat("Member should be able to update own service (200)",
                updateResp.getStatusCode(), is(200));

        // Step 4: MEMBER tries to update service token - 403
        UpdateServiceTokenRequest tokenReq = UpdateServiceTokenRequest.builder()
                .gitToken(scenariqFaker.getGitToken())
                .gitUsername(scenariqFaker.getGitUsername()).build();
        Response tokenResp = updateServiceToken(memberToken, serviceId, tokenReq);
        assertThat("Member should not be able to update service token (403)",
                tokenResp.getStatusCode(), is(403));

        // Step 5: MEMBER tries to invite user - 403
        InviteUserRequest inviteReq = InviteUserRequest.builder()
                .email(scenariqFaker.getEmail()).role("MEMBER").build();
        Response inviteResp = inviteUser(memberToken, inviteReq);
        assertThat("Member should not be able to invite users (403)",
                inviteResp.getStatusCode(), is(403));

        // Step 6: MEMBER tries to delete another user - 403
        // Create another member to attempt deletion on
        Object[] otherMemberData = inviteAndAcceptUser(ownerToken, "MEMBER");
        long otherMemberUserId = (long) otherMemberData[1];

        Response deleteUserResp = deleteAccountUser(memberToken, otherMemberUserId);
        assertThat("Member should not be able to delete other users (403)",
                deleteUserResp.getStatusCode(), is(403));

        // Cleanup
        deleteScenariqAccount(ownerToken, ownerPassword);
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void rbacAdminMidAccessTest() {

        // Step 1: Create account, invite ADMIN
        Object[] ownerData = createScenariqAccount();
        String ownerToken = (String) ownerData[0];
        String ownerPassword = (String) ownerData[2];

        Object[] adminData = inviteAndAcceptUser(ownerToken, "ADMIN");
        String adminToken = (String) adminData[0];
        long adminUserId = (long) adminData[1];

        // Step 2: ADMIN registers service - 201
        long serviceId = createTestService(adminToken);
        assertThat("Admin should be able to register a service", serviceId, is(greaterThan(0L)));

        // Step 3: ADMIN updates service token - 200
        UpdateServiceTokenRequest tokenReq = UpdateServiceTokenRequest.builder()
                .gitToken(scenariqFaker.getGitToken())
                .gitUsername(scenariqFaker.getGitUsername()).build();
        Response tokenResp = updateServiceToken(adminToken, serviceId, tokenReq);
        assertThat("Admin should be able to update service token (200)",
                tokenResp.getStatusCode(), is(200));

        // Step 4: ADMIN invites user - 201
        InviteUserRequest inviteReq = InviteUserRequest.builder()
                .email(scenariqFaker.getEmail()).role("MEMBER").build();
        Response inviteResp = inviteUser(adminToken, inviteReq);
        assertThat("Admin should be able to invite users (201)",
                inviteResp.getStatusCode(), is(201));

        // Step 5: ADMIN tries to update user role - 403
        Object[] memberData = inviteAndAcceptUser(ownerToken, "MEMBER");
        long memberUserId = (long) memberData[1];

        UpdateAccountUserRequest updateUserReq = UpdateAccountUserRequest.builder()
                .role("ADMIN").build();
        Response updateUserResp = updateAccountUser(adminToken, memberUserId, updateUserReq);
        assertThat("Admin should not be able to update user roles (403)",
                updateUserResp.getStatusCode(), is(403));

        // Cleanup
        deleteScenariqAccount(ownerToken, ownerPassword);
    }
}
