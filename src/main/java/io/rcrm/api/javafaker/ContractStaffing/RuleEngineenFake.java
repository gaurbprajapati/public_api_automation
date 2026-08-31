package io.rcrm.api.javafaker.ContractStaffing;

import com.github.javafaker.Faker;
import java.util.concurrent.ThreadLocalRandom;

public class RuleEngineenFake {

    private final Faker faker = new Faker();

    public String getRuleTemplateName() {
        return "RuleTemplate_" + faker.company().name().replaceAll("[^a-zA-Z0-9]", "") +
                "_" + faker.number().randomNumber(4, true);
    }

    public String getRuleTemplateName(String prefix) {
        return prefix + "_" + faker.company().name().replaceAll("[^a-zA-Z0-9]", "") +
                "_" + faker.number().randomNumber(4, true);
    }

    public String getRuleName() {
        return faker.job().title().replaceAll("[^a-zA-Z0-9 ]", "") + "_Rule_" +
                faker.number().randomNumber(3, true);
    }

    public String getRuleName(String type) {
        return type + "_" + faker.job().title().replaceAll("[^a-zA-Z0-9 ]", "") +
                "_Rule_" + faker.number().randomNumber(3, true);
    }

    public String getUniqueTemplateName(String scenario) {
        return scenario + "_Template_" + faker.company().name().replaceAll("[^a-zA-Z0-9]", "") +
                "_" + System.currentTimeMillis() % 10000;
    }

    public String getCalculationTemplateName() {
        return "CalcTemplate_" + faker.finance().iban().substring(0, 8) +
                "_" + faker.number().randomNumber(4, true);
    }

    public String getWorkflowTemplateName() {
        return "WorkflowTemplate_" + faker.superhero().name().replaceAll("[^a-zA-Z0-9]", "") +
                "_" + faker.number().randomNumber(3, true);
    }

    public String getTestTemplateName(String testType) {
        return testType + "Test_" + faker.lorem().word() +
                "_" + faker.number().randomNumber(4, true);
    }

    public String getTimeBasedTemplateName() {
        return "Template_" + System.currentTimeMillis() + "_" +
                ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}