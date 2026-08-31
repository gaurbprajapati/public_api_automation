package io.recruitcrm.scenariq.auth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.pojo.scenariq.LoginRequest;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class LogoutTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void logoutWithValidTokenTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        Response response = logout(token);

        assertThat("Logout with valid token should return 204",
                response.statusCode(), is(204));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void logoutWithInvalidTokenTest() {
        Response response = logout("invalid.jwt.token.value");

        assertThat("Logout with invalid token should return 401",
                response.statusCode(), is(401));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void logoutWithExpiredTokenTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        Response firstLogout = logout(token);
        assertThat("Prerequisite: first logout should succeed",
                firstLogout.statusCode(), is(204));

        Response response = logout(token);

        assertThat("Logout with already-used (expired/invalidated) token should return 401",
                response.statusCode(), is(401));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void logoutWithMissingTokenTest() {
        Response response = logout(null);

        assertThat("Logout with missing token should return 401",
                response.statusCode(), is(401));
    }
}
