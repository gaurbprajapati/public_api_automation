package io.recruitcrm.albatross;


import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.albatross.CustomizeInvoice;
import io.rcrm.api.pojo.albatross.InvoiceSettings;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class InvoiceSettingsTest extends TestBase{

    Faker faker = new Faker();
    JavaFakerCompany javaFakerCompany = new JavaFakerCompany();
    Random random=new Random();
    String generatedString = RandomStringUtils.randomAlphabetic(4);

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void customizeInvoiceSettingsWithValidLimit(){
        String invoiceNotes=faker.lorem().paragraph(100).substring(0, 2000);
        String invoiceTerms=faker.lorem().paragraph(100).substring(0, 500);
        InvoiceSettings invoiceSettings = new InvoiceSettings();
        CustomizeInvoice customizeInvoice = new CustomizeInvoice();
        customizeInvoice.setInvoicename("Invoice " + generatedString);
        customizeInvoice.setWebsite("https://" + faker.company().url());
        customizeInvoice.setPhone(faker.phoneNumber().cellPhone());
        customizeInvoice.setAddress(faker.address().fullAddress());
        customizeInvoice.setNotes(invoiceNotes);
        customizeInvoice.setTermsandconditions(faker.lorem().sentence());
        customizeInvoice.setSignature(random.nextInt(2));
        customizeInvoice.setLogo(javaFakerCompany.getLogoURL());
        invoiceSettings.setInvoicesetting(customizeInvoice);

        Response response = RestClient.doPost("JSON", albatrossURL, "accounts/invoice-settings", ThreadManager.getOwnerAlbatrossToken(), null, true, invoiceSettings);
        Assert.assertEquals(response.getStatusCode(), 200);
        String message=response.jsonPath().getString("message");
        String messageType=response.jsonPath().getString("message_type");
        if(!messageType.equals("is-success") && !message.equals("Invoice Setting Store Successful ")){
            Assert.fail("Failed to save invoice settings: "+messageType);
        }
        else{
        }
        Assert.assertEquals(response.getStatusCode(), 200);

    }


    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void customizeInvoiceSettingsWithInvalidLimit(){
        String invoiceNotes=faker.lorem().paragraph(100).substring(0, 2001);
        InvoiceSettings invoiceSettings = new InvoiceSettings();
        CustomizeInvoice customizeInvoice = new CustomizeInvoice();
        customizeInvoice.setInvoicename("Invoice " + generatedString);
        customizeInvoice.setWebsite("https://" + faker.company().url());
        customizeInvoice.setPhone(faker.phoneNumber().cellPhone());
        customizeInvoice.setAddress(faker.address().fullAddress());
        customizeInvoice.setNotes(invoiceNotes);
        customizeInvoice.setTermsandconditions(faker.lorem().sentence());
        customizeInvoice.setSignature(random.nextInt(2));
        customizeInvoice.setLogo(javaFakerCompany.getLogoURL());
        invoiceSettings.setInvoicesetting(customizeInvoice);

        Response response = RestClient.doPost("JSON", albatrossURL, "accounts/invoice-settings", ThreadManager.getOwnerAlbatrossToken(), null, true, invoiceSettings);
        Assert.assertEquals(response.getStatusCode(), 422);
        String message=response.jsonPath().getString("message");
        String messageType=response.jsonPath().getString("message_type");
        if(!messageType.equals("is-danger") && !message.equals("Failed To Invoice Setting Store : The notes must not be greater than 2000 characters.")){
            Assert.fail("Invoice settings saved Successfully with Invalid Limit");
        }
        else{
        }
    }

    @Owner("Raj Pandey")
    @Test(groups = "nightly-build")
    public void customizeInvoiceSettingsWithInvalidAuth(){
        String invoiceNotes=faker.lorem().paragraph(100).substring(0, 2000);
        InvoiceSettings invoiceSettings = new InvoiceSettings();
        CustomizeInvoice customizeInvoice = new CustomizeInvoice();
        customizeInvoice.setInvoicename("Invoice " + generatedString);
        customizeInvoice.setWebsite("https://" + faker.company().url());
        customizeInvoice.setPhone(faker.phoneNumber().cellPhone());
        customizeInvoice.setAddress(faker.address().fullAddress());
        customizeInvoice.setNotes(invoiceNotes);
        customizeInvoice.setTermsandconditions(faker.lorem().sentence());
        customizeInvoice.setSignature(random.nextInt(2));
        customizeInvoice.setLogo(javaFakerCompany.getLogoURL());
        invoiceSettings.setInvoicesetting(customizeInvoice);

        Response response = RestClient.doPost("JSON", albatrossURL, "accounts/invoice-settings", ThreadManager.getOwnerAlbatrossToken()+"123", null, true, invoiceSettings);
        Assert.assertEquals(response.getStatusCode(), 401);
        String error=response.jsonPath().getString("error");
        Assert.assertEquals(error, "Unauthorized");
    }
}
