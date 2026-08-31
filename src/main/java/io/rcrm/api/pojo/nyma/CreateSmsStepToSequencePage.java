package io.rcrm.api.pojo.nyma;

import lombok.*;

@Getter
@Setter
public class CreateSmsStepToSequencePage {

	int id;
	int step_no;
	int no_of_days;
	int time;
	int type;
	String sms_template_title;
	String sms_template_content;
	String update_type;
}