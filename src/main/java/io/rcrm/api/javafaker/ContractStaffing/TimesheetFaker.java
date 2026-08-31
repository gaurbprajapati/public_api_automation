package io.rcrm.api.javafaker.ContractStaffing;

import com.github.javafaker.Faker;

public class TimesheetFaker {

    private static final Faker faker = new Faker();

    public static int generateFakerInt(int max) {
        return faker.number().numberBetween(0, max + 1);
    }

    public static int generateFakerId() {
        return faker.number().numberBetween(1000000, 9999999);
    }

    public static String generateFakerEmail() {
        return faker.name().firstName() + "." + faker.name().lastName() + "@yopmail.com";
    }

}

