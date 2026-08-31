package io.rcrm.api.javafaker.ContractStaffing;

import com.github.javafaker.Faker;

public class JavaFakerFilter {

    private final Faker faker = new Faker();

    public int getNonExistentEntityId() {
        return faker.number().numberBetween(900_000_000, 999_999_999);
    }

    public int getNonExistentStatusId() {
        return faker.number().numberBetween(10, 999);
    }

    public String getNonExistentSearchLabel(String prefix) {
        return prefix + faker.lorem().word() + faker.number().digits(6);
    }
}
