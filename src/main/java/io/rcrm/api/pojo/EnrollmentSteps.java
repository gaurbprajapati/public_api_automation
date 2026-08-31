package io.rcrm.api.pojo;

public class EnrollmentSteps {
    private String update_type;
    private int seq_step_details_id;
    private int no_of_days;
    private Integer include_opt_out_link;
    private int time;
    private int type;
    private String template_content;
    private String template_subject;
    private String template_title;

    // Constructor
    public EnrollmentSteps(String update_type, int seq_step_details_id, int no_of_days, Integer include_opt_out_link, int time, int type,
                           String template_content, String template_subject, String template_title) {
        this.update_type = update_type;
        this.seq_step_details_id = seq_step_details_id;
        this.no_of_days = no_of_days;
        this.include_opt_out_link = include_opt_out_link;
        this.time = time;
        this.type = type;
        this.template_content = template_content;
        this.template_subject = template_subject;
        this.template_title = template_title;
    }

    // Getters and Setters
    public String getUpdate_type() {
        return update_type;
    }

    public void setUpdate_type(String update_type) {
        this.update_type = update_type;
    }

    public int getSeq_step_details_id() {
        return seq_step_details_id;
    }

    public void setSeq_step_details_id(int seq_step_details_id) {
        this.seq_step_details_id = seq_step_details_id;
    }

    public int getNo_of_days() {
        return no_of_days;
    }

    public void setNo_of_days(int no_of_days) {
        this.no_of_days = no_of_days;
    }

    public Integer getInclude_opt_out_link() {
        return include_opt_out_link;
    }

    public void setInclude_opt_out_link(Integer include_opt_out_link) {
        this.include_opt_out_link = include_opt_out_link;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getTemplate_content() {
        return template_content;
    }

    public void setTemplate_content(String template_content) {
        this.template_content = template_content;
    }

    public String getTemplate_subject() {
        return template_subject;
    }

    public void setTemplate_subject(String template_subject) {
        this.template_subject = template_subject;
    }

    public String getTemplate_title() {
        return template_title;
    }

    public void setTemplate_title(String template_title) {
        this.template_title = template_title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
