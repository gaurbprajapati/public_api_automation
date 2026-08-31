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
 * End-to-end workspace lifecycle test.
 * Covers: signup -> login -> profile ops -> password change -> account deletion.
 */
@AccountType("NotRequired")
public class EndToEndWorkspaceFlowTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void endToEndWorkspaceCreationAndSetupTest() {

        // Step 1: Signup - verify account created (201)
        String email = scenariqFaker.getEmail();
        String password = scenariqFaker.getPassword();
        String name = scenariqFaker.getName();
        String accountName = scenariqFaker.getAccountName();

        SignupRequest signupReq = SignupRequest.builder()
                .name(name).email(email).password(password).accountName(accountName).build();
        Response signupResp = signup(signupReq);
        assertThat("Signup should return 201 for valid data", signupResp.getStatusCode(), is(201));
        String token = signupResp.jsonPath().getString("token");
        assertThat("Signup response must contain a JWT token", token, is(notNullValue()));
        assertThat("Signup response must contain user object",
                signupResp.jsonPath().getString("user.email"), is(email));

        // Step 2: Login with same credentials - verify token (200)
        LoginRequest loginReq = LoginRequest.builder().email(email).password(password).build();
        Response loginResp = login(loginReq);
        assertThat("Login should return 200 for valid credentials", loginResp.getStatusCode(), is(200));
        String loginToken = loginResp.jsonPath().getString("token");
        assertThat("Login response must contain a JWT token", loginToken, is(notNullValue()));

        // Step 3: Get profile - verify account details (200)
        Response profileResp = getProfile(loginToken);
        assertThat("Get profile should return 200", profileResp.getStatusCode(), is(200));
        assertThat("Profile email must match signup email",
                profileResp.jsonPath().getString("email"), is(email));
        assertThat("Profile name must match signup name",
                profileResp.jsonPath().getString("name"), is(name));

        // Step 4: Update profile (name, timezone) - verify updated (200)
        String updatedName = scenariqFaker.getName();
        String updatedTimezone = scenariqFaker.getTimezone();
        UpdateProfileRequest updateReq = UpdateProfileRequest.builder()
                .name(updatedName).timezone(updatedTimezone).build();
        Response updateResp = updateProfile(loginToken, updateReq);
        assertThat("Update profile should return 200", updateResp.getStatusCode(), is(200));

        // Verify profile was actually updated
        Response verifyProfileResp = getProfile(loginToken);
        assertThat("Updated profile name must match",
                verifyProfileResp.jsonPath().getString("name"), is(updatedName));

        // Step 5: Update git token - verify hasGitToken true (200)
        UpdateGitTokenRequest gitTokenReq = UpdateGitTokenRequest.builder()
                .gitToken(scenariqFaker.getGitToken()).build();
        Response gitTokenResp = updateGitToken(loginToken, gitTokenReq);
        assertThat("Update git token should return 200", gitTokenResp.getStatusCode(), is(200));

        Response profileAfterGit = getProfile(loginToken);
        assertThat("hasGitToken must be true after setting git token",
                profileAfterGit.jsonPath().getBoolean("hasGitToken"), is(true));

        // Step 6: Change password - verify success (200)
        String newPassword = scenariqFaker.getPassword();
        ChangePasswordRequest changePwdReq = ChangePasswordRequest.builder()
                .currentPassword(password).newPassword(newPassword).build();
        Response changePwdResp = changePassword(loginToken, changePwdReq);
        assertThat("Change password should return 200", changePwdResp.getStatusCode(), is(200));

        // Step 7: Login with new password - verify works (200)
        LoginRequest newLoginReq = LoginRequest.builder().email(email).password(newPassword).build();
        Response newLoginResp = login(newLoginReq);
        assertThat("Login with new password should return 200", newLoginResp.getStatusCode(), is(200));
        String newToken = newLoginResp.jsonPath().getString("token");
        assertThat("Login with new password must return a JWT token", newToken, is(notNullValue()));

        // Step 8: Delete account - verify deleted (204)
        CancellationFeedbackRequest deleteReq = CancellationFeedbackRequest.builder()
                .password(newPassword).reasons(java.util.Arrays.asList("E2E test cleanup")).build();
        Response deleteResp = deleteAccount(newToken, deleteReq);
        assertThat("Delete account should return 204", deleteResp.getStatusCode(), is(204));

        // Step 9: Login with old credentials - verify fails (401)
        LoginRequest oldLoginReq = LoginRequest.builder().email(email).password(newPassword).build();
        Response oldLoginResp = login(oldLoginReq);
        assertThat("Login after account deletion should return 401",
                oldLoginResp.getStatusCode(), is(401));
    }
}
