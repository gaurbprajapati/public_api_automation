package io.recruitcrm.scenariq.businessflow;

import com.qa.api.util.Owner;
import io.rcrm.api.pojo.scenariq.*;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * End-to-end team management lifecycle test.
 * Covers: account creation -> invite admin -> invite member -> user management -> cleanup.
 */
@AccountType("NotRequired")
public class TeamManagementFlowTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void endToEndTeamManagementTest() {

        // Step 1: Create account (owner)
        Object[] ownerData = createScenariqAccount();
        String ownerToken = (String) ownerData[0];
        String ownerPassword = (String) ownerData[2];

        // Step 2: Invite ADMIN user - verify invitation (201)
        Object[] adminData = inviteAndAcceptUser(ownerToken, "ADMIN");
        String adminToken = (String) adminData[0];
        long adminUserId = (long) adminData[1];

        // Step 3: Get invitations - verify pending list accessible (200)
        Response invitationsResp = getInvitations(ownerToken);
        assertThat("Get invitations should return 200", invitationsResp.getStatusCode(), is(200));

        // Step 4: Verify ADMIN token works - get profile (200)
        Response adminProfileResp = getProfile(adminToken);
        assertThat("Admin get profile should return 200", adminProfileResp.getStatusCode(), is(200));
        assertThat("Admin role must be ADMIN",
                adminProfileResp.jsonPath().getString("role"), is("ADMIN"));

        // Step 5: Invite MEMBER user from ADMIN - verify works (201)
        Object[] memberData = inviteAndAcceptUser(adminToken, "MEMBER");
        String memberToken = (String) memberData[0];
        long memberUserId = (long) memberData[1];

        // Step 6: Verify MEMBER accepted - get profile (200)
        Response memberProfileResp = getProfile(memberToken);
        assertThat("Member get profile should return 200", memberProfileResp.getStatusCode(), is(200));
        assertThat("Member role must be MEMBER",
                memberProfileResp.jsonPath().getString("role"), is("MEMBER"));

        // Step 7: Get users - verify 3 users (owner + admin + member) (200)
        Response usersResp = getAccountUsers(ownerToken);
        assertThat("Get account users should return 200", usersResp.getStatusCode(), is(200));
        List<?> users = usersResp.jsonPath().getList("$");
        assertThat("Account must have 3 users (owner + admin + member)",
                users.size(), is(greaterThanOrEqualTo(3)));

        // Step 8: Update MEMBER to ADMIN by OWNER - verify (200)
        UpdateAccountUserRequest updateReq = UpdateAccountUserRequest.builder()
                .role("ADMIN").build();
        Response updateUserResp = updateAccountUser(ownerToken, memberUserId, updateReq);
        assertThat("Update user role should return 200", updateUserResp.getStatusCode(), is(200));

        // Verify role change took effect
        Response updatedMemberProfile = getProfile(memberToken);
        assertThat("Updated member role must now be ADMIN",
                updatedMemberProfile.jsonPath().getString("role"), is("ADMIN"));

        // Step 9: Delete one user by OWNER - verify (204)
        Response deleteUserResp = deleteAccountUser(ownerToken, adminUserId);
        assertThat("Delete account user should return 204", deleteUserResp.getStatusCode(), is(204));

        // Step 10: Get users - verify count decreased (200)
        Response usersAfterDelete = getAccountUsers(ownerToken);
        assertThat("Get account users after deletion should return 200",
                usersAfterDelete.getStatusCode(), is(200));
        List<?> remainingUsers = usersAfterDelete.jsonPath().getList("$");
        assertThat("Account must have 2 users after deletion",
                remainingUsers.size(), is(users.size() - 1));

        // Step 11: Cleanup
        deleteScenariqAccount(ownerToken, ownerPassword);
    }
}
