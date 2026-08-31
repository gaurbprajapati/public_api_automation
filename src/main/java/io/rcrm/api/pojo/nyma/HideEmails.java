package io.rcrm.api.pojo.nyma;
import lombok.*;

import java.util.List;

@Getter
@Setter
public class HideEmails {

	private List<String> thread_ids;
	private int hide_email;
	private int bulk_action;
	private int linked_email_type;

}
