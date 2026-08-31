package io.recruitcrm.scenariq.auth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.pojo.scenariq.*;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class ProfileTest extends ScenariqBaseTest {

    // ── GET /auth/profile ─────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getProfileWithValidTokenTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];
        String email = (String) account[1];
        String name = (String) account[5];
        String accountName = (String) account[6];

        Response response = getProfile(token);

        assertThat("Get profile with valid token should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Profile id should be present",
                jsonPath.get("id"), notNullValue());
        assertThat("Profile name should match the name from signup",
                jsonPath.getString("name"), is(name));
        assertThat("Profile email should match the email from signup",
                jsonPath.getString("email"), is(email));
        assertThat("Profile role should be OWNER for account creator",
                jsonPath.getString("role"), is("OWNER"));
        assertThat("Account id should be present",
                jsonPath.get("accountId"), notNullValue());
        assertThat("Account name should match the account name from signup",
                jsonPath.getString("accountName"), is(accountName));
        assertThat("hasGitToken field should be present and false for new account",
                jsonPath.getBoolean("hasGitToken"), is(false));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getProfileWithInvalidTokenTest() {
        Response response = getProfile("invalid.jwt.token.value");

        assertThat("Get profile with invalid token should return 401",
                response.statusCode(), is(401));
    }

    // ── PUT /auth/profile ─────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateProfileNameTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];
        String newName = scenariqFaker.getName();

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name(newName)
                .build();

        Response response = updateProfile(token, request);

        assertThat("Update profile name should return 200",
                response.statusCode(), is(200));

        Response profileResponse = getProfile(token);
        assertThat("Updated name should be reflected in profile",
                profileResponse.jsonPath().getString("name"), is(newName));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateProfileEmailTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];
        String newEmail = scenariqFaker.getEmail();

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .email(newEmail)
                .build();

        Response response = updateProfile(token, request);

        assertThat("Update profile email should return 200",
                response.statusCode(), is(200));

        Response profileResponse = getProfile(token);
        assertThat("Updated email should be reflected in profile",
                profileResponse.jsonPath().getString("email"), is(newEmail));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateProfileTimezoneTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];
        String newTimezone = "America/New_York";

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .timezone(newTimezone)
                .build();

        Response response = updateProfile(token, request);

        assertThat("Update profile timezone should return 200",
                response.statusCode(), is(200));

        Response profileResponse = getProfile(token);
        assertThat("Updated timezone should be reflected in profile",
                profileResponse.jsonPath().getString("timezone"), is(newTimezone));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateProfileWithInvalidEmailTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .email("not-a-valid-email")
                .build();

        Response response = updateProfile(token, request);

        assertThat("Update profile with invalid email format should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateProfileWithEmptyNameTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("")
                .build();

        Response response = updateProfile(token, request);

        assertThat("Update profile with empty name should return 400",
                response.statusCode(), is(400));
    }

    // ── PUT /auth/profile/password ────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void changePasswordWithValidDataTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];
        String email = (String) account[1];
        String currentPassword = (String) account[2];
        String newPassword = scenariqFaker.getPassword();

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .build();

        Response response = changePassword(token, request);

        assertThat("Change password with valid data should return 200",
                response.statusCode(), is(200));

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(newPassword)
                .build();
        Response loginResponse = login(loginRequest);
        assertThat("Login with new password should succeed after password change",
                loginResponse.statusCode(), is(200));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void changePasswordWithWrongCurrentPasswordTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("WrongOldPassword@123")
                .newPassword(scenariqFaker.getPassword())
                .build();

        Response response = changePassword(token, request);

        assertThat("Change password with wrong current password should return 400 or 401",
                response.statusCode(), anyOf(is(400), is(401)));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void changePasswordWithShortNewPasswordTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];
        String currentPassword = (String) account[2];

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword(currentPassword)
                .newPassword("Ab1@")
                .build();

        Response response = changePassword(token, request);

        assertThat("Change password with new password shorter than 8 characters should return 400",
                response.statusCode(), is(400));
    }

    // ── PUT /auth/profile/git-token ───────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateGitTokenTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];
        String gitToken = scenariqFaker.getGitToken();

        UpdateGitTokenRequest request = UpdateGitTokenRequest.builder()
                .gitToken(gitToken)
                .build();

        Response response = updateGitToken(token, request);

        assertThat("Update git token should return 200",
                response.statusCode(), is(200));

        Response profileResponse = getProfile(token);
        assertThat("hasGitToken should be true after setting git token",
                profileResponse.jsonPath().getBoolean("hasGitToken"), is(true));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateGitTokenWithNullTokenTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        UpdateGitTokenRequest setRequest = UpdateGitTokenRequest.builder()
                .gitToken(scenariqFaker.getGitToken())
                .build();
        Response setResponse = updateGitToken(token, setRequest);
        assertThat("Prerequisite: setting git token should succeed",
                setResponse.statusCode(), is(200));

        UpdateGitTokenRequest clearRequest = UpdateGitTokenRequest.builder()
                .gitToken(null)
                .build();

        Response response = updateGitToken(token, clearRequest);

        assertThat("Clearing git token with null should return 200",
                response.statusCode(), is(200));

        Response profileResponse = getProfile(token);
        assertThat("hasGitToken should be false after clearing git token",
                profileResponse.jsonPath().getBoolean("hasGitToken"), is(false));
    }
}
