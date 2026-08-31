package io.rcrm.api.javafaker.scenariq;

import com.github.javafaker.Faker;

public class JavaFakerScenariq {

    Faker faker = new Faker();

    public String getName() {
        return faker.name().fullName();
    }

    public String getEmail() {
        return "scenariq_" + faker.name().firstName().toLowerCase() + "_" + faker.number().numberBetween(1000, 9999) + "@yopmail.com";
    }

    public String getPassword() {
        return "Test@" + faker.number().numberBetween(10000, 99999);
    }

    public String getAccountName() {
        return faker.company().name() + " QA";
    }

    public String getServiceName() {
        return "Service_" + faker.app().name().replaceAll("\\s+", "") + "_" + faker.number().numberBetween(100, 999);
    }

    public String getOwnerName() {
        return faker.name().fullName();
    }

    public String getGitToken() {
        return "ghp_test_" + faker.regexify("[a-zA-Z0-9]{36}");
    }

    public String getGitUsername() {
        return faker.name().username();
    }

    public String getBackendRepoUrl() {
        return "https://github.com/test-org/backend-" + faker.number().numberBetween(100, 999);
    }

    public String getAutomationRepoUrl() {
        return "https://github.com/test-org/automation-" + faker.number().numberBetween(100, 999);
    }

    public String getBranchName() {
        return "main";
    }

    public String getTimezone() {
        return "Asia/Kolkata";
    }

    public String getFeedbackMessage() {
        return faker.lorem().sentence(10);
    }

    public String getBindingKeyword() {
        return "baseURL";
    }
}
