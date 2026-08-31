package io.rcrm.api.javafaker;

import com.github.javafaker.Faker;

public class JavaFakerTemplate {

    public JavaFakerTemplate(){
        super();
    }

    Faker faker = new Faker();

    public String getTemplateName() {
        return faker.lorem().word();
    }

    public String getFirstName() {
        return faker.name().firstName();
    }

    public String getLastName() {
        return faker.name().lastName();
    }

    public String getEmail() {
        return faker.name().firstName() + "@yopmail.com";
    }

    public String getPhoneNumber() {
        return faker.phoneNumber().cellPhone();
    }

    public String getCity() {
        return faker.address().city();
    }

    public String getLocality() {
        return faker.address().streetName();
    }

    public String getNote() {
        return faker.lorem().sentence();
    }

    public String getEntityType() {
        return String.valueOf(faker.number().numberBetween(5, 5));
    }

    public int getShareWithTeammates() {
        return faker.random().nextInt(2);
    }

}