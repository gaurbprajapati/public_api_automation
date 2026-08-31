package io.rcrm.api.tests;

import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;
import io.rcrm.api.testbase.TestBase;
import com.qa.api.util.reaper.ThreadManager;

/**
 * Health check suite for all production services.
 * Missing health check URLs (present in TestBase but not provided): company, contact, job, aries.
 */
@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class HealthCheckSuiteTest extends TestBase{

    private String albatrossAuthToken;
    private String apiAuthToken;
    String env;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        env = System.getProperty("envname");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Albatross service health check")
    public void albatrossHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://albatross-"+env+".recruitcrm.net", "/healthcheck", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Albatross health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "API (core) service health check")
    public void apiHealthCheck() {
        Response response = RestClient.doGet("JSON", baseURL, "", apiAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "API service health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Audit Log service health check")
    public void auditLogHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"-audit-log.recruitcrm.net", "/healthcheck", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Audit Log health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Auth service health check")
    public void authHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"-auth.recruitcrm.net", "/healthcheck", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Auth service health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Bulk Export service health check")
    public void bulkExportHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"bulk-export.recruitcrm.net", "/v1/healthcheck", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Bulk Export health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Comm service health check")
    public void commHealthCheck() {
        Response response = RestClient.doGet("JSON", commURL, "/", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Comm service health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "CV Format service health check")
    public void cvFormatHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://cv-format.recruitcrm.io", "/healthcheck", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "CV Format health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Executive Search Report service health check")
    public void executiveSearchReportHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"-executive-search-report.recruitcrm.net", "/healthcheck", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Executive Search Report health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Import Map Deployer health check")
    public void importMapDeployerHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://import-map-deployer.app.recruitcrm.io", "/health", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Import Map Deployer health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Hiring Pipeline service health check")
    public void hiringPipelineHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"-hiring-pipeline.recruitcrm.net", "/healthcheck", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Hiring Pipeline health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Job Board service health check")
    public void jobBoardHealthCheck() {
        Response response = RestClient.doGet("JSON", jobBoardServiceURL, "/", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Job Board health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Nyma service health check")
    public void nymaHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"nyma.recruitcrm.net", "/healthcheck", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Nyma health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Ostrich service health check")
    public void ostrichHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"-ostrich.recruitcrm.net", "/healthcheck", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Ostrich health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Parser service health check")
    public void parserHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://parser.recruitcrm.io", "/healthcheck", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Parser health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Report service health check")
    public void reportHealthCheck() {
        Response response = RestClient.doGet("JSON", reportServiceURL, "", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Report service health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Email Webhook service health check")
    public void emailWebhookHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://email-webhook.recruitcrm.io", "/healthcheck", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Email Webhook health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Candidate service health check")
    public void candidateHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"candidate.recruitcrm.net", "/actuator/health", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Candidate service health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Invoice service health check")
    public void invoiceHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"invoice.recruitcrm.net", "/actuator/health", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Invoice service health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Notification service health check")
    public void notificationHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"-notifications.recruitcrm.net", "/healthcheck", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Notification service health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Neptune (VMS) service health check")
    public void neptuneHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"-neptune.recruitcrm.net", "/v1/healthcheck", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Neptune service health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Main app (recruitcrm.io) health check")
    public void mainAppHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+".recruitcrm.net", "/healthcheck/", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Main app health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Syncfusion service health check")
    public void syncfusionHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"-syncfusion.recruitcrm.net", "/healthcheck", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Syncfusion health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Shorter (URL shortener) service health check")
    public void shorterHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://shorter.recruitcrm.io", "/", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Shorter service health check failed");
    }

    // ── Services present in TestBase but missing from the provided URL list ──

    @Owner("Akshaya Uppala")
    @Test(description = "Company service health check")
    public void companyHealthCheck() {
        Response response = RestClient.doGet("JSON","https://"+env+"company.recruitcrm.net" , "/actuator/health", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Company service health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Contact service health check")
    public void contactHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"contact.recruitcrm.net", "/actuator/health", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Contact service health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Job service health check")
    public void jobHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"job.recruitcrm.net", "/actuator/health", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Job service health check failed");
    }

    @Owner("Akshaya Uppala")
    @Test(description = "Aries service health check")
    public void ariesHealthCheck() {
        Response response = RestClient.doGet("JSON", "https://"+env+"-aries.recruitcrm.net", "/actuator/health", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Aries service health check failed");
    }
}
