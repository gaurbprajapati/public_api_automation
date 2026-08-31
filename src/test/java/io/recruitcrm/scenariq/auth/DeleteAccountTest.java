package io.recruitcrm.scenariq.auth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.pojo.scenariq.*;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class DeleteAccountTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void deleteAccountWithValidPasswordTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];
        String password = (String) account[2];
        String email = (String) account[1];

        CancellationFeedbackRequest request = CancellationFeedbackRequest.builder()
                .password(password)
                .reasons(Arrays.asList("Testing", "No longer needed"))
                .improvementSuggestion("This is an automated test deletion")
                .alternativePlatform("None")
                .build();

        Response response = deleteAccount(token, request);

        assertThat("Delete account with valid password should return 204",
                response.statusCode(), is(204));

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();
        Response loginResponse = login(loginRequest);
        assertThat("Login should fail after account deletion",
                loginResponse.statusCode(), anyOf(is(401), is(404)));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void deleteAccountWithWrongPasswordTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        CancellationFeedbackRequest request = CancellationFeedbackRequest.builder()
                .password("WrongPassword@999")
                .reasons(Arrays.asList("Testing"))
                .build();

        Response response = deleteAccount(token, request);

        assertThat("Delete account with wrong password should return 400 or 403",
                response.statusCode(), anyOf(is(400), is(403)));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void deleteAccountByNonOwnerTest() {
        Object[] ownerAccount = createScenariqAccount();
        String ownerToken = (String) ownerAccount[0];

        Object[] memberData = inviteAndAcceptUser(ownerToken, "MEMBER");
        String memberToken = (String) memberData[0];
        String memberPassword = (String) memberData[3];

        CancellationFeedbackRequest request = CancellationFeedbackRequest.builder()
                .password(memberPassword)
                .reasons(Arrays.asList("Testing non-owner deletion"))
                .build();

        Response response = deleteAccount(memberToken, request);

        assertThat("Non-owner (MEMBER) should not be able to delete the account and should get 403",
                response.statusCode(), is(403));
    }
}
