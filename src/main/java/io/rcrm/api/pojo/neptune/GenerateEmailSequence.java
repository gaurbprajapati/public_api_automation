package io.rcrm.api.pojo.neptune;

public class GenerateEmailSequence {
    private String entity_type;
    private String prompt;

    public GenerateEmailSequence() {
    }

    public GenerateEmailSequence(String entity_type, String prompt) {
        this.entity_type = entity_type;
        this.prompt = prompt;
    }

    public String getEntity_type() {
        return entity_type;
    }

    public void setEntity_type(String entity_type) {
        this.entity_type = entity_type;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
