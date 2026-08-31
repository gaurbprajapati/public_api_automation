package io.rcrm.api.javafaker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.github.javafaker.Faker;

public class JavaFakerInvoice {

    Faker faker = new Faker();

    public String invoicePrefix() {
        return faker.lorem().word();
    }

    public String invoiceNumber() {
        return String.valueOf(faker.number().numberBetween(0, 9999));
    }

    public int getCurrencyId(){
        return faker.number().numberBetween(1, 99);
    }

    public long getPaidOn(){
        Date pastDate = faker.date().past(1000, java.util.concurrent.TimeUnit.DAYS);
        long issueDateEpoch = pastDate.toInstant().getEpochSecond();
        return issueDateEpoch;
    }

    public long getDueDate(){
        Date futureDate = faker.date().future(1000, java.util.concurrent.TimeUnit.DAYS);
        long dueDateEpoch = futureDate.toInstant().getEpochSecond();
        return dueDateEpoch;
    }

    public long getIssueDate(){
        Date pastDate = faker.date().past(1000, java.util.concurrent.TimeUnit.DAYS);
        long issueDateEpoch = pastDate.toInstant().getEpochSecond();
        return issueDateEpoch;
    }

    public double getTotalAmount(){
        return faker.number().randomDouble(2, 100, 1000);
    }

    public String getRandomPrefix(){
        return faker.lorem().characters(4);
    }

    public String getCompanyName() {
        return faker.company().name();
    }

    public String getWebsite() {
        return "https://" + faker.internet().domainName();
    }

    public String getLogo() {
        return faker.internet().url();
    }

    public String getAddress() {
        return faker.address().fullAddress();
    }

    public String getEmail() {
        return faker.internet().emailAddress();
    }

    public String getPhone() {
        return faker.phoneNumber().phoneNumber();
    }

    public String getPrefix() {
        return faker.lorem().characters(3).toUpperCase();
    }

    public String getNumber() {
        return String.valueOf(faker.number().numberBetween(1, 999));
    }

    public int getUserId() {
        return faker.number().numberBetween(1000000, 9999999);
    }

    public String getDescription() {
        return faker.lorem().sentence();
    }

    public int getInvoiceTemplateId() {
        return faker.number().numberBetween(1, 999);
    }

    public int getInvoiceStatusId() {
        return faker.number().numberBetween(3, 5);
    }

    public int getInvalidStatusId() {
        return faker.number().numberBetween(999, 9999);
    }

    public String getInvoiceTemplateName() {
        return "Custom " + faker.lorem().word();
    }

    public int getRandomInvoiceId() {
        return 999 + faker.number().numberBetween(9999, 99999);
    }

    public String getRandomInvoiceText() {
        return faker.lorem().sentence(10);
    }

    public String getRandomSlug(){
        return "177" + faker.number().digits(15) + faker.lorem().characters(3, false, false);
    }

    public String getRandomInvoiceDate() {
        int offsetDays = faker.number().numberBetween(-3, 4);
        LocalDate date = LocalDate.now().plusDays(offsetDays);
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public String getRandomInvoicePastDate() {
        int daysAgo = faker.number().numberBetween(1, 7);
        LocalDate date = LocalDate.now().minusDays(daysAgo);
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public String getRandomInvoiceFutureDate() {
        int daysAhead = faker.number().numberBetween(1, 7);
        LocalDate date = LocalDate.now().plusDays(daysAhead);
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public String getRandomTextWithMoreThan500Chars() {
        return faker.lorem().characters(600, 700);
    }

    public String getContactNumber() {
        return "+91" + faker.number().digits(10);
    }

    public int getRandomTemplateId() {
        return 99990000 + faker.number().numberBetween(0, 9999);
    }
}