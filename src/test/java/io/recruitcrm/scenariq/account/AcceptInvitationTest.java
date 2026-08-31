package io.recruitcrm.scenariq.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.pojo.scenariq.AcceptInvitationRequest;
import io.rcrm.api.pojo.scenariq.InviteUserRequest;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class AcceptInvitationTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void acceptInvitationWithValidTokenTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        String inviteeEmail = scenariqFaker.getEmail();
        InviteUserRequest inviteReq = InviteUserRequest.builder()
                .email(inviteeEmail)
                .role("MEMBER")
                .build();
        Response inviteResp = inviteUser(ownerToken, inviteReq);
        assertThat("Prerequisite: invitation should succeed",
                inviteResp.statusCode(), is(201));

        String invitationToken = inviteResp.jsonPath().getString("invitationToken");
        if (invitationToken == null) {
            String link = inviteResp.jsonPath().getString("invitationLink");
            if (link != null && link.contains("token=")) {
                invitationToken = link.split("token=")[1];
                if (invitationToken.contains("&")) {
                    invitationToken = invitationToken.split("&")[0];
                }
            }
        }

        String memberName = scenariqFaker.getName();
        String memberPassword = scenariqFaker.getPassword();
        AcceptInvitationRequest acceptReq = AcceptInvitationRequest.builder()
                .invitationToken(invitationToken)
                .name(memberName)
                .password(memberPassword)
                .build();

        Response response = acceptInvitation(acceptReq);

        assertThat("Accept invitation with valid token should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Token should be present in accept invitation response",
                jsonPath.getString("token"), notNullValue());
        assertThat("Token should not be empty",
                jsonPath.getString("token"), not(emptyOrNullString()));
        assertThat("User object should be present in accept invitation response",
                jsonPath.getString("user"), notNullValue());
        assertThat("User id should be present",
                jsonPath.get("user.id"), notNullValue());
        assertThat("User name should match the name sent in accept request",
                jsonPath.getString("user.name"), is(memberName));
        assertThat("User email should match the invited email",
                jsonPath.getString("user.email"), is(inviteeEmail));
        assertThat("User role should be MEMBER as assigned during invitation",
                jsonPath.getString("user.role"), is("MEMBER"));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void acceptInvitationWithInvalidTokenTest() {
        AcceptInvitationRequest request = AcceptInvitationRequest.builder()
                .invitationToken("invalid-token-xyz")
                .name(scenariqFaker.getName())
                .password(scenariqFaker.getPassword())
                .build();

        Response response = acceptInvitation(request);

        assertThat("Accept invitation with invalid token should return 400 or 404",
                response.statusCode(), anyOf(is(400), is(404)));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void acceptInvitationWithMissingNameTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        String inviteeEmail = scenariqFaker.getEmail();
        InviteUserRequest inviteReq = InviteUserRequest.builder()
                .email(inviteeEmail)
                .role("MEMBER")
                .build();
        Response inviteResp = inviteUser(ownerToken, inviteReq);
        assertThat("Prerequisite: invitation should succeed",
                inviteResp.statusCode(), is(201));

        String invitationToken = inviteResp.jsonPath().getString("invitationToken");
        if (invitationToken == null) {
            String link = inviteResp.jsonPath().getString("invitationLink");
            if (link != null && link.contains("token=")) {
                invitationToken = link.split("token=")[1];
                if (invitationToken.contains("&")) {
                    invitationToken = invitationToken.split("&")[0];
                }
            }
        }

        AcceptInvitationRequest acceptReq = AcceptInvitationRequest.builder()
                .invitationToken(invitationToken)
                .name(null)
                .password(scenariqFaker.getPassword())
                .build();

        Response response = acceptInvitation(acceptReq);

        assertThat("Accept invitation with missing name should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void acceptInvitationWithShortPasswordTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        String inviteeEmail = scenariqFaker.getEmail();
        InviteUserRequest inviteReq = InviteUserRequest.builder()
                .email(inviteeEmail)
                .role("MEMBER")
                .build();
        Response inviteResp = inviteUser(ownerToken, inviteReq);
        assertThat("Prerequisite: invitation should succeed",
                inviteResp.statusCode(), is(201));

        String invitationToken = inviteResp.jsonPath().getString("invitationToken");
        if (invitationToken == null) {
            String link = inviteResp.jsonPath().getString("invitationLink");
            if (link != null && link.contains("token=")) {
                invitationToken = link.split("token=")[1];
                if (invitationToken.contains("&")) {
                    invitationToken = invitationToken.split("&")[0];
                }
            }
        }

        AcceptInvitationRequest acceptReq = AcceptInvitationRequest.builder()
                .invitationToken(invitationToken)
                .name(scenariqFaker.getName())
                .password("Ab1@")
                .build();

        Response response = acceptInvitation(acceptReq);

        assertThat("Accept invitation with password shorter than 8 characters should return 400",
                response.statusCode(), is(400));
    }
}
