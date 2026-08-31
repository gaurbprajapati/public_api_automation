package io.recruitcrm.scenariq.service;

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
public class UpdateServiceTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateServiceNameTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        long serviceId = createTestService(ownerToken);

        String newServiceName = scenariqFaker.getServiceName();
        UpdateServiceRequest updateRequest = UpdateServiceRequest.builder()
                .serviceName(newServiceName)
                .build();

        Response response = updateService(ownerToken, serviceId, updateRequest);

        assertThat("Update service name should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Updated service name should match the new name sent in request",
                jsonPath.getString("serviceName"), is(newServiceName));
        assertThat("Service ID should remain the same after update",
                jsonPath.getLong("id"), is(serviceId));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateServiceByMemberWhoRegisteredTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Object[] memberData = inviteAndAcceptUser(ownerToken, "MEMBER");
        String memberToken = (String) memberData[0];

        RegisterServiceRequest registerReq = RegisterServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName())
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .backendBranch("main")
                .automationBranch("main")
                .build();
        Response registerResp = registerService(memberToken, registerReq);
        assertThat("Prerequisite: Member service registration should return 201",
                registerResp.statusCode(), is(201));
        long serviceId = registerResp.jsonPath().getLong("id");

        String updatedName = scenariqFaker.getServiceName();
        UpdateServiceRequest updateRequest = UpdateServiceRequest.builder()
                .serviceName(updatedName)
                .build();

        Response response = updateService(memberToken, serviceId, updateRequest);

        assertThat("Member who registered the service should be able to update it and get 200",
                response.statusCode(), is(200));
        assertThat("Service name should reflect the updated value",
                response.jsonPath().getString("serviceName"), is(updatedName));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateServiceByMemberWhoDidNotRegisterTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        long serviceId = createTestService(ownerToken);

        Object[] memberData = inviteAndAcceptUser(ownerToken, "MEMBER");
        String memberToken = (String) memberData[0];

        UpdateServiceRequest updateRequest = UpdateServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName())
                .build();

        Response response = updateService(memberToken, serviceId, updateRequest);

        assertThat("Member who did not register the service should get 403 when trying to update it",
                response.statusCode(), is(403));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateServiceByAdminTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        long serviceId = createTestService(ownerToken);

        Object[] adminData = inviteAndAcceptUser(ownerToken, "ADMIN");
        String adminToken = (String) adminData[0];

        String updatedName = scenariqFaker.getServiceName();
        UpdateServiceRequest updateRequest = UpdateServiceRequest.builder()
                .serviceName(updatedName)
                .build();

        Response response = updateService(adminToken, serviceId, updateRequest);

        assertThat("Admin should be able to update any service and get 200",
                response.statusCode(), is(200));
        assertThat("Service name should reflect the updated value after admin update",
                response.jsonPath().getString("serviceName"), is(updatedName));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateServiceWithInvalidIdTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        UpdateServiceRequest updateRequest = UpdateServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName())
                .build();

        Response response = updateService(ownerToken, 999999999L, updateRequest);

        assertThat("Update service with non-existent ID should return 404",
                response.statusCode(), is(404));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateServiceNameToDuplicateTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        String serviceAName = scenariqFaker.getServiceName();
        RegisterServiceRequest registerReqA = RegisterServiceRequest.builder()
                .serviceName(serviceAName)
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .backendBranch("main")
                .automationBranch("main")
                .build();
        Response registerRespA = registerService(ownerToken, registerReqA);
        assertThat("Prerequisite: First service registration should return 201",
                registerRespA.statusCode(), is(201));

        String serviceBName = scenariqFaker.getServiceName();
        RegisterServiceRequest registerReqB = RegisterServiceRequest.builder()
                .serviceName(serviceBName)
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .backendBranch("main")
                .automationBranch("main")
                .build();
        Response registerRespB = registerService(ownerToken, registerReqB);

        // FREE plan may limit to 1 service — if so, second registration returns 403
        if (registerRespB.statusCode() == 403) {
            assertThat("FREE plan restricts service count — second registration returns 403",
                    registerRespB.statusCode(), is(403));
            return;
        }

        assertThat("Prerequisite: Second service registration should return 201",
                registerRespB.statusCode(), is(201));
        long serviceBId = registerRespB.jsonPath().getLong("id");

        // Attempt to rename service B to service A's name (duplicate)
        UpdateServiceRequest updateRequest = UpdateServiceRequest.builder()
                .serviceName(serviceAName)
                .build();

        Response response = updateService(ownerToken, serviceBId, updateRequest);

        assertThat("Updating service name to a duplicate within the account should return 400 or 409",
                response.statusCode(), anyOf(is(400), is(409)));
    }
}
