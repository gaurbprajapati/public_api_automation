package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.pojo.invoiceService.InvoiceSettings;
import io.rcrm.api.javafaker.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class InvoiceSettings_Test extends TestBase {

    public InvoiceSettings_Test() {
        super();
    } 

    String apiKeyA;
    commanFunction function;
    String albatrossTknA;
    JavaFakerPlacement placementFaker;
    JavaFakerInvoice fakerInvoice;
    String basePath = "invoice-settings";

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiKeyA = ThreadManager.getAccountApiKey();
        albatrossTknA = ThreadManager.getOwnerAlbatrossToken();
        function = new commanFunction();
        placementFaker = new JavaFakerPlacement();
        fakerInvoice = new JavaFakerInvoice();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getUserId", groups = {"invoice_service", "nightly-build"})
    public void createInvoiceSettingsWithValidToken_Test(int userId) {
        InvoiceSettings invoiceSettings = new InvoiceSettings();
        invoiceSettings.setCompanyName(fakerInvoice.getCompanyName());
        invoiceSettings.setWebsite(fakerInvoice.getWebsite());
        invoiceSettings.setLogo("");
        invoiceSettings.setAddress(null);
        invoiceSettings.setEmail(fakerInvoice.getEmail());
        invoiceSettings.setPhone(null);
        invoiceSettings.setPrefix(fakerInvoice.getPrefix());
        invoiceSettings.setNumber(fakerInvoice.getNumber());
        invoiceSettings.setUserId(userId);

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, invoiceSettings);
        response.then().statusCode(200);
        
        response.then().body("meta.message", Matchers.containsString("Invoice Setting Saved Sucessfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//invoiceSettings.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceSettingsWithInvalidToken_Test() {
        InvoiceSettings invoiceSettings = new InvoiceSettings();

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, true, invoiceSettings);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void createInvoiceSettingsWithInvalidUserId_Test() {
        InvoiceSettings invoiceSettings = new InvoiceSettings();
        invoiceSettings.setCompanyName(fakerInvoice.getCompanyName());
        invoiceSettings.setWebsite(fakerInvoice.getWebsite());
        invoiceSettings.setLogo("");
        invoiceSettings.setAddress(null);
        invoiceSettings.setEmail(fakerInvoice.getEmail());
        invoiceSettings.setPhone(null);
        invoiceSettings.setPrefix(fakerInvoice.getPrefix());
        invoiceSettings.setNumber(fakerInvoice.getNumber());
        invoiceSettings.setUserId(fakerInvoice.getUserId());

        Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, invoiceSettings);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Setting Saved Sucessfully"));
        response.then().assertThat().body("data.userId", Matchers.nullValue());
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//invoiceSettings.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceSettingsData", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSettingsWithValidToken_Test(int userId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.containsString("Invoice Setting Fetched Sucessfully"));
        response.then().assertThat().body("data.userId", Matchers.equalTo(userId));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//invoiceService//invoiceSettings.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getInvoiceSettingsData", groups = {"invoice_service", "nightly-build"})
    public void getInvoiceSettingsWithInvalidToken_Test(int userId) {
        Response response = RestClient.doGet("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @DataProvider
    public Object[][] getUserId() {
        Response users = function.getUsers(baseURL, apiKeyA);
		int userId = users.jsonPath().get("[0].id");
        return new Object[][] { { userId } };
    }

    @DataProvider
    public Object[][] getInvoiceSettingsData() {
        InvoiceSettings invoiceSettings = new InvoiceSettings();
        invoiceSettings.setCompanyName(fakerInvoice.getCompanyName());
        invoiceSettings.setWebsite(fakerInvoice.getWebsite());
        invoiceSettings.setLogo("");
        invoiceSettings.setAddress(null);
        invoiceSettings.setEmail(fakerInvoice.getEmail());
        invoiceSettings.setPhone(null);
        invoiceSettings.setPrefix(fakerInvoice.getPrefix());
        invoiceSettings.setNumber(fakerInvoice.getNumber());
        Response users = function.getUsers(baseURL, apiKeyA);
		int userId = users.jsonPath().get("[0].id");
        invoiceSettings.setUserId(userId);

        Response createResponse = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, invoiceSettings);
        createResponse.then().statusCode(200);

        return new Object[][] { { userId } };
    }
}
