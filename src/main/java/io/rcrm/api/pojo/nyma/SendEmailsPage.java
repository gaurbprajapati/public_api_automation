package io.rcrm.api.pojo.nyma;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;


public class SendEmailsPage {

	EmailsPage emailsPage;
	private String contextId;
	private boolean is_send;
	private int linked_email_type;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Map<String, List<String>> associated_data;
	/*
	 * private int Scheduled_email_id;
	 */

	public SendEmailsPage() {
		super();
		// TODO Auto-generated constructor stub
		this.linked_email_type = 1;
	}
	public SendEmailsPage( EmailsPage emailsPage,String contextId,boolean is_send, Map<String, List<String>> associated_data) {
		super();
		this.emailsPage = emailsPage;
		this.contextId = contextId;
		this.is_send = is_send;
		this.linked_email_type = 1;
		this.associated_data = associated_data;
	}

	/*
	 * public int getScheduledEmail() { return Scheduled_email_id; }
	 * 
	 * public void setScheduledEmail(int Scheduled_email_id) {
	 * this.Scheduled_email_id = Scheduled_email_id; }
	 */
	public boolean getis_send() {
		return is_send;
	}

	public void setis_send(boolean is_send) {
		this.is_send = is_send;
	}

	public String getContextId() {
		return contextId;
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	public EmailsPage getEmail() {
		return emailsPage;
	}

	public void setEmail(EmailsPage emailsPage) {
		this.emailsPage = emailsPage;
	}

	public int getLinked_email_type() {
		return linked_email_type;
	}

	public void setLinked_email_type(int linked_email_type) {
		this.linked_email_type = linked_email_type;
	}

	public Map<String, List<String>> getAssociated_data() {
		return associated_data;
	}

	public void setAssociated_data(Map<String, List<String>> associated_data) {
		this.associated_data = associated_data;
	}
}

