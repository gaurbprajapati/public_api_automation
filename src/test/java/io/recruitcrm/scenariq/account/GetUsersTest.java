package io.recruitcrm.scenariq.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class GetUsersTest extends ScenariqBaseTest {

    // -- GET /accounts/users -- 200 as owner -----------------------------------

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getAccountUsersAsOwnerTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];
        String email = (String) account[1];

        Response response = getAccountUsers(token);

        assertThat("Get account users as owner should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Users list should not be empty for a new account",
                jsonPath.getList("$"), is(not(empty())));
        assertThat("Users list should contain at least the owner",
                jsonPath.getList("$").size(), greaterThanOrEqualTo(1));
        assertThat("Owner email should be present in the users list",
                jsonPath.getList("email"), hasItem(email));
    }

    // -- GET /accounts/users -- 200 as member ----------------------------------

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getAccountUsersAsMemberTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Object[] member = inviteAndAcceptUser(ownerToken, "MEMBER");
        String memberToken = (String) member[0];

        Response response = getAccountUsers(memberToken);

        assertThat("Get account users as member should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Users list should contain at least two users (owner + member)",
                jsonPath.getList("$").size(), greaterThanOrEqualTo(2));
    }

    // -- GET /accounts/users -- 401 invalid token ------------------------------

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getAccountUsersWithInvalidTokenTest() {
        Response response = getAccountUsers("invalid-token-xyz");

        assertThat("Get account users with invalid token should return 401",
                response.statusCode(), is(401));
    }
}
