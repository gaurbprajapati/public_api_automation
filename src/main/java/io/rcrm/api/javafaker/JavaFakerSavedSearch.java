package io.rcrm.api.javafaker;

import com.github.javafaker.Faker;

public class JavaFakerSavedSearch {

    private final Faker faker = new Faker();

    public String getSavedSearchName() {
        String searchType = faker.options().option("ID", "Candidate","Name","Profile");
        String descriptor = faker.lorem().word();
        int number = faker.number().numberBetween(1, 1000);

        return searchType + " Search " + descriptor + " " + number;
    }
}
