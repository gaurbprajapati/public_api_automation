package io.recruitcrm.scenariq.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class DeleteAccountUserTest extends ScenariqBaseTest {

    // -- DELETE /accounts/users/{id} -- owner deletes member -------------------

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void deleteUserByOwnerTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Object[] member = inviteAndAcceptUser(ownerToken, "MEMBER");
        long memberUserId = (long) member[1];

        Response response = deleteAccountUser(ownerToken, memberUserId);

        assertThat("Owner deleting a member should return 204",
                response.statusCode(), is(204));

        Response usersResponse = getAccountUsers(ownerToken);
        assertThat("Deleted member should no longer appear in users list",
                usersResponse.jsonPath().getList("findAll { it.id == " + memberUserId + " }"),
                is(empty()));
    }

    // -- DELETE /accounts/users/{id} -- member tries to delete another member --

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void deleteUserByNonOwnerTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Object[] firstMember = inviteAndAcceptUser(ownerToken, "MEMBER");
        String firstMemberToken = (String) firstMember[0];

        Object[] secondMember = inviteAndAcceptUser(ownerToken, "MEMBER");
        long secondMemberUserId = (long) secondMember[1];

        Response response = deleteAccountUser(firstMemberToken, secondMemberUserId);

        assertThat("Non-owner deleting another member should return 403",
                response.statusCode(), is(403));
    }

    // -- DELETE /accounts/users/{id} -- delete non-existent user ---------------

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void deleteNonExistentUserTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Response response = deleteAccountUser(ownerToken, 999999999L);

        assertThat("Deleting a non-existent user should return 404",
                response.statusCode(), is(404));
    }

    // -- DELETE /accounts/users/{id} -- owner cannot delete self ---------------

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void ownerCannotDeleteSelfTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];
        long ownerUserId = (long) account[3];

        Response response = deleteAccountUser(ownerToken, ownerUserId);

        assertThat("Owner trying to delete self should return 400 or 403",
                response.statusCode(), anyOf(is(400), is(403)));
    }
}
