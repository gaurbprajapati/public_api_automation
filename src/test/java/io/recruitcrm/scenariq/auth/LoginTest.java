package io.recruitcrm.scenariq.auth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.pojo.scenariq.LoginRequest;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class LoginTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void loginWithValidCredentialsTest() {
        Object[] account = createScenariqAccount();
        String email = (String) account[1];
        String password = (String) account[2];
        String expectedName = (String) account[5];

        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        Response response = login(request);

        assertThat("Login with valid credentials should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Token should be present in login response",
                jsonPath.getString("token"), notNullValue());
        assertThat("Token should not be empty",
                jsonPath.getString("token"), not(emptyOrNullString()));
        assertThat("User object should be present in login response",
                jsonPath.getString("user"), notNullValue());
        assertThat("User email should match the email used for login",
                jsonPath.getString("user.email"), is(email));
        assertThat("User name should match the name from signup",
                jsonPath.getString("user.name"), is(expectedName));
        assertThat("User id should be present",
                jsonPath.get("user.id"), notNullValue());
        assertThat("Account id should be present",
                jsonPath.get("user.accountId"), notNullValue());
        assertThat("User role should be present",
                jsonPath.getString("user.role"), notNullValue());
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void loginWithInvalidPasswordTest() {
        Object[] account = createScenariqAccount();
        String email = (String) account[1];

        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password("WrongPassword@123")
                .build();

        Response response = login(request);

        assertThat("Login with invalid password should return 401",
                response.statusCode(), is(401));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void loginWithNonExistentEmailTest() {
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent_" + System.currentTimeMillis() + "@yopmail.com")
                .password(scenariqFaker.getPassword())
                .build();

        Response response = login(request);

        assertThat("Login with non-existent email should return 401",
                response.statusCode(), is(401));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void loginWithMissingEmailTest() {
        LoginRequest request = LoginRequest.builder()
                .password(scenariqFaker.getPassword())
                .build();

        Response response = login(request);

        assertThat("Login with missing email should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void loginWithMissingPasswordTest() {
        LoginRequest request = LoginRequest.builder()
                .email(scenariqFaker.getEmail())
                .build();

        Response response = login(request);

        assertThat("Login with missing password should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void loginWithInvalidEmailFormatTest() {
        LoginRequest request = LoginRequest.builder()
                .email("not-an-email")
                .password(scenariqFaker.getPassword())
                .build();

        Response response = login(request);

        assertThat("Login with invalid email format should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void loginWithEmptyBodyTest() {
        LoginRequest request = LoginRequest.builder().build();

        Response response = login(request);

        assertThat("Login with empty body should return 400",
                response.statusCode(), is(400));
    }
}
