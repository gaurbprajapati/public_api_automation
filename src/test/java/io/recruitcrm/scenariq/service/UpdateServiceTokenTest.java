package io.recruitcrm.scenariq.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.pojo.scenariq.UpdateServiceTokenRequest;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class UpdateServiceTokenTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateServiceTokenByOwnerTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        long serviceId = createTestService(ownerToken);

        UpdateServiceTokenRequest tokenRequest = UpdateServiceTokenRequest.builder()
                .gitToken(scenariqFaker.getGitToken())
                .gitUsername(scenariqFaker.getGitUsername())
                .build();

        Response response = updateServiceToken(ownerToken, serviceId, tokenRequest);

        assertThat("Owner updating service git token should return 200",
                response.statusCode(), is(200));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateServiceTokenByAdminTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        long serviceId = createTestService(ownerToken);

        Object[] adminData = inviteAndAcceptUser(ownerToken, "ADMIN");
        String adminToken = (String) adminData[0];

        UpdateServiceTokenRequest tokenRequest = UpdateServiceTokenRequest.builder()
                .gitToken(scenariqFaker.getGitToken())
                .gitUsername(scenariqFaker.getGitUsername())
                .build();

        Response response = updateServiceToken(adminToken, serviceId, tokenRequest);

        assertThat("Admin should be able to update service git token and get 200",
                response.statusCode(), is(200));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateServiceTokenByMemberTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        long serviceId = createTestService(ownerToken);

        Object[] memberData = inviteAndAcceptUser(ownerToken, "MEMBER");
        String memberToken = (String) memberData[0];

        UpdateServiceTokenRequest tokenRequest = UpdateServiceTokenRequest.builder()
                .gitToken(scenariqFaker.getGitToken())
                .gitUsername(scenariqFaker.getGitUsername())
                .build();

        Response response = updateServiceToken(memberToken, serviceId, tokenRequest);

        assertThat("Member should not be able to update service git token and should get 403",
                response.statusCode(), is(403));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateServiceTokenWithMissingGitTokenTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        long serviceId = createTestService(ownerToken);

        UpdateServiceTokenRequest tokenRequest = UpdateServiceTokenRequest.builder()
                .gitToken(null)
                .gitUsername(scenariqFaker.getGitUsername())
                .build();

        Response response = updateServiceToken(ownerToken, serviceId, tokenRequest);

        assertThat("Updating service token with null gitToken should return 400",
                response.statusCode(), is(400));
    }
}
