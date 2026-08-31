package io.recruitcrm.scenariq.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.pojo.scenariq.RegisterServiceRequest;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class GetServiceTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getAllServicesTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        String serviceName = scenariqFaker.getServiceName();
        RegisterServiceRequest request = RegisterServiceRequest.builder()
                .serviceName(serviceName)
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .build();
        Response registerResponse = registerService(token, request);
        assertThat("Prerequisite: service registration should return 201",
                registerResponse.statusCode(), is(201));

        Response response = getServices(token);

        assertThat("Get all services should return 200",
                response.statusCode(), is(200));
        assertThat("Services list should contain at least 1 element",
                response.jsonPath().getList("$").size(), greaterThanOrEqualTo(1));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getServiceByIdTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        String serviceName = scenariqFaker.getServiceName();
        String serviceType = "SPRING_BOOT";
        String backendRepoUrl = scenariqFaker.getBackendRepoUrl();
        String automationRepoUrl = scenariqFaker.getAutomationRepoUrl();

        RegisterServiceRequest request = RegisterServiceRequest.builder()
                .serviceName(serviceName)
                .serviceType(serviceType)
                .backendRepoUrl(backendRepoUrl)
                .automationRepoUrl(automationRepoUrl)
                .build();
        Response registerResponse = registerService(token, request);
        assertThat("Prerequisite: service registration should return 201",
                registerResponse.statusCode(), is(201));

        long serviceId = registerResponse.jsonPath().getLong("id");

        Response response = getServiceById(token, serviceId);

        assertThat("Get service by id should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Service id should match the registered service id",
                jsonPath.getLong("id"), is(serviceId));
        assertThat("Service name should match the registered service name",
                jsonPath.getString("serviceName"), is(serviceName));
        assertThat("Service type should match the registered service type",
                jsonPath.getString("serviceType"), is(serviceType));
        assertThat("Backend repo URL should match the registered value",
                jsonPath.getString("backendRepoUrl"), is(backendRepoUrl));
        assertThat("Automation repo URL should match the registered value",
                jsonPath.getString("automationRepoUrl"), is(automationRepoUrl));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getServiceWithInvalidIdTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        Response response = getServiceById(token, 999999999L);

        assertThat("Get service with invalid id should return 404",
                response.statusCode(), is(404));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getServiceFromDifferentAccountTest() {
        Object[] account1 = createScenariqAccount();
        String token1 = (String) account1[0];

        long serviceId = createTestService(token1);

        Object[] account2 = createScenariqAccount();
        String token2 = (String) account2[0];

        Response response = getServiceById(token2, serviceId);

        assertThat("Get service from different account should return 404 for tenant isolation",
                response.statusCode(), is(404));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void getServicesWithInvalidTokenTest() {
        Response response = getServices("invalid-token-xyz");

        assertThat("Get services with invalid token should return 401",
                response.statusCode(), is(401));
    }
}
