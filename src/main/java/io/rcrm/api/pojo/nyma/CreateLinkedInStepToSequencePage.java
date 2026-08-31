package io.rcrm.api.pojo.nyma;

import lombok.*;

@Getter
@Setter

public class CreateLinkedInStepToSequencePage {

    private String id;
    private int time;
    private String no_of_days;
    private boolean isEdited;
    private String update_type;
    private String linkedin_template_title;
    private String linkedin_template_content;
    private int step_no;
    private int email_sms_linkedin_step_cnt;
    private int type;
    private boolean is_edited;

}
