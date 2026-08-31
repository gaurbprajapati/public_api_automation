package io.recruitcrm.scenariq.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.pojo.scenariq.InviteUserRequest;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class InviteUserTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void inviteUserAsOwnerTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        String inviteeEmail = scenariqFaker.getEmail();
        InviteUserRequest request = InviteUserRequest.builder()
                .email(inviteeEmail)
                .role("MEMBER")
                .build();

        Response response = inviteUser(ownerToken, request);

        assertThat("Owner inviting a user should return 201",
                response.statusCode(), is(201));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Invitation link should be present in response",
                jsonPath.getString("invitationLink"), notNullValue());
        assertThat("Invitation link should not be empty",
                jsonPath.getString("invitationLink"), not(emptyOrNullString()));
        assertThat("Invitation token should be present in response",
                jsonPath.getString("invitationToken"), notNullValue());
        assertThat("Invited email should match the email sent in request",
                jsonPath.getString("email"), is(inviteeEmail));
        assertThat("Invited role should match the role sent in request",
                jsonPath.getString("role"), is("MEMBER"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void inviteUserAsAdminTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Object[] adminData = inviteAndAcceptUser(ownerToken, "ADMIN");
        String adminToken = (String) adminData[0];

        String inviteeEmail = scenariqFaker.getEmail();
        InviteUserRequest request = InviteUserRequest.builder()
                .email(inviteeEmail)
                .role("MEMBER")
                .build();

        Response response = inviteUser(adminToken, request);

        assertThat("Admin inviting a user should return 201",
                response.statusCode(), is(201));
        assertThat("Invitation link should be present when admin invites",
                response.jsonPath().getString("invitationLink"), notNullValue());
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void inviteUserAsMemberTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Object[] memberData = inviteAndAcceptUser(ownerToken, "MEMBER");
        String memberToken = (String) memberData[0];

        InviteUserRequest request = InviteUserRequest.builder()
                .email(scenariqFaker.getEmail())
                .role("MEMBER")
                .build();

        Response response = inviteUser(memberToken, request);

        assertThat("Member inviting a user should return 403",
                response.statusCode(), is(403));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void inviteWithInvalidEmailTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        InviteUserRequest request = InviteUserRequest.builder()
                .email("not-an-email")
                .role("MEMBER")
                .build();

        Response response = inviteUser(ownerToken, request);

        assertThat("Invite with invalid email should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void inviteWithMissingRoleTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        InviteUserRequest request = InviteUserRequest.builder()
                .email(scenariqFaker.getEmail())
                .role(null)
                .build();

        Response response = inviteUser(ownerToken, request);

        assertThat("Invite with missing role should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void inviteWithInvalidRoleTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        InviteUserRequest request = InviteUserRequest.builder()
                .email(scenariqFaker.getEmail())
                .role("SUPERADMIN")
                .build();

        Response response = inviteUser(ownerToken, request);

        assertThat("Invite with invalid role should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void inviteExistingMemberTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Object[] memberData = inviteAndAcceptUser(ownerToken, "MEMBER");
        String memberEmail = (String) memberData[2];

        InviteUserRequest request = InviteUserRequest.builder()
                .email(memberEmail)
                .role("MEMBER")
                .build();

        Response response = inviteUser(ownerToken, request);

        assertThat("Invite for existing member should return 400 or 409",
                response.statusCode(), anyOf(is(400), is(409)));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void inviteExceedingPlanLimitTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        inviteAndAcceptUser(ownerToken, "MEMBER");
        inviteAndAcceptUser(ownerToken, "MEMBER");

        InviteUserRequest request = InviteUserRequest.builder()
                .email(scenariqFaker.getEmail())
                .role("MEMBER")
                .build();

        Response response = inviteUser(ownerToken, request);

        assertThat("Invite exceeding FREE plan limit should return 403",
                response.statusCode(), is(403));
    }
}
