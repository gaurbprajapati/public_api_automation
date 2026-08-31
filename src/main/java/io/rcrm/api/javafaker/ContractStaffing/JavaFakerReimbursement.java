package io.rcrm.api.javafaker.ContractStaffing;

import com.github.javafaker.Faker;

public class JavaFakerReimbursement {

    private static final Faker faker = new Faker();

    public static int generateFakerInt(int max) {
        return faker.number().numberBetween(0, max + 1);
    }

    public static int generateFakerId() {
        return faker.number().numberBetween(1000000, 9999999);
    }

    public static String generateFakerDescription() {
        return faker.lorem().sentence();
    }

    public static double generateFakerAmount() {
        return faker.number().randomDouble(2, 100, 1000);
    }

    public static String generateFakerEmail() {
        return "test-portal-" + faker.name().firstName() + "." + faker.name().lastName() + "@yopmail.com";
    }

    public static String generateFakerFileName() {
        return faker.lorem().word() + ".pdf";
    }

    public static String generateFakerDocumentToken() {
        return faker.lorem().characters(10);
    }

}

