package io.recruitcrm.scenariq.auth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.pojo.scenariq.SignupRequest;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class SignupTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void signupWithValidDataTest() {
        String name = scenariqFaker.getName();
        String email = scenariqFaker.getEmail();
        String password = scenariqFaker.getPassword();
        String accountName = scenariqFaker.getAccountName();

        SignupRequest request = SignupRequest.builder()
                .name(name)
                .email(email)
                .password(password)
                .accountName(accountName)
                .build();

        Response response = signup(request);

        assertThat("Signup with valid data should return 201",
                response.statusCode(), is(201));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Token should be present in signup response",
                jsonPath.getString("token"), notNullValue());
        assertThat("Token should not be empty",
                jsonPath.getString("token"), not(emptyOrNullString()));
        assertThat("User object should be present in signup response",
                jsonPath.getString("user"), notNullValue());
        assertThat("User id should be present",
                jsonPath.get("user.id"), notNullValue());
        assertThat("User name should match the name sent in signup request",
                jsonPath.getString("user.name"), is(name));
        assertThat("User email should match the email sent in signup request",
                jsonPath.getString("user.email"), is(email));
        assertThat("User role should be OWNER for newly created account",
                jsonPath.getString("user.role"), is("OWNER"));
        assertThat("Account name should match the account name sent in signup request",
                jsonPath.getString("user.accountName"), is(accountName));
        assertThat("Account id should be present",
                jsonPath.get("user.accountId"), notNullValue());
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void signupWithExistingEmailTest() {
        String email = scenariqFaker.getEmail();
        String password = scenariqFaker.getPassword();

        SignupRequest firstRequest = SignupRequest.builder()
                .name(scenariqFaker.getName())
                .email(email)
                .password(password)
                .accountName(scenariqFaker.getAccountName())
                .build();
        Response firstResponse = signup(firstRequest);
        assertThat("Prerequisite: first signup should succeed",
                firstResponse.statusCode(), is(201));

        SignupRequest duplicateRequest = SignupRequest.builder()
                .name(scenariqFaker.getName())
                .email(email)
                .password(password)
                .accountName(scenariqFaker.getAccountName())
                .build();

        Response response = signup(duplicateRequest);

        assertThat("Signup with existing email should return 400 or 409",
                response.statusCode(), anyOf(is(400), is(409)));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void signupWithMissingNameTest() {
        SignupRequest request = SignupRequest.builder()
                .email(scenariqFaker.getEmail())
                .password(scenariqFaker.getPassword())
                .accountName(scenariqFaker.getAccountName())
                .build();

        Response response = signup(request);

        assertThat("Signup with missing name should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void signupWithMissingEmailTest() {
        SignupRequest request = SignupRequest.builder()
                .name(scenariqFaker.getName())
                .password(scenariqFaker.getPassword())
                .accountName(scenariqFaker.getAccountName())
                .build();

        Response response = signup(request);

        assertThat("Signup with missing email should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void signupWithInvalidEmailFormatTest() {
        SignupRequest request = SignupRequest.builder()
                .name(scenariqFaker.getName())
                .email("invalid-email-format")
                .password(scenariqFaker.getPassword())
                .accountName(scenariqFaker.getAccountName())
                .build();

        Response response = signup(request);

        assertThat("Signup with invalid email format should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void signupWithMissingPasswordTest() {
        SignupRequest request = SignupRequest.builder()
                .name(scenariqFaker.getName())
                .email(scenariqFaker.getEmail())
                .accountName(scenariqFaker.getAccountName())
                .build();

        Response response = signup(request);

        assertThat("Signup with missing password should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void signupWithShortPasswordTest() {
        SignupRequest request = SignupRequest.builder()
                .name(scenariqFaker.getName())
                .email(scenariqFaker.getEmail())
                .password("Ab1@")
                .accountName(scenariqFaker.getAccountName())
                .build();

        Response response = signup(request);

        assertThat("Signup with password shorter than 8 characters should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void signupWithMissingAccountNameTest() {
        SignupRequest request = SignupRequest.builder()
                .name(scenariqFaker.getName())
                .email(scenariqFaker.getEmail())
                .password(scenariqFaker.getPassword())
                .build();

        Response response = signup(request);

        assertThat("Signup with missing account name should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void signupWithEmptyBodyTest() {
        SignupRequest request = SignupRequest.builder().build();

        Response response = signup(request);

        assertThat("Signup with empty body should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void signupWithNullFieldsTest() {
        SignupRequest request = SignupRequest.builder()
                .name(null)
                .email(null)
                .password(null)
                .accountName(null)
                .build();

        Response response = signup(request);

        assertThat("Signup with all null fields should return 400",
                response.statusCode(), is(400));
    }
}
