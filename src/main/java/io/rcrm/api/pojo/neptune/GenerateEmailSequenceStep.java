package io.rcrm.api.pojo.neptune;

public class GenerateEmailSequenceStep {
    private String entity_type;
    private Integer seq_step_type;
    private String last_response;
    private String prompt;

    public GenerateEmailSequenceStep() {
    }

    public GenerateEmailSequenceStep(String entity_type, Integer seq_step_type, String last_response, String prompt) {
        this.entity_type = entity_type;
        this.seq_step_type = seq_step_type;
        this.last_response = last_response;
        this.prompt = prompt;
    }

    public String getEntity_type() {
        return entity_type;
    }

    public void setEntity_type(String entity_type) {
        this.entity_type = entity_type;
    }

    public Integer getSeq_step_type() {
        return seq_step_type;
    }

    public void setSeq_step_type(Integer seq_step_type) {
        this.seq_step_type = seq_step_type;
    }

    public String getLast_response() {
        return last_response;
    }

    public void setLast_response(String last_response) {
        this.last_response = last_response;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
