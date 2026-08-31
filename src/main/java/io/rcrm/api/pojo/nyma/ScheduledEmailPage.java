package io.rcrm.api.pojo.nyma;

import java.util.ArrayList;

public class ScheduledEmailPage  {

	EmailsPage emailsPage;

	private int scheduled_id;
	private String draft_id;
	private int include_opt_out_link;
	private int scheduled_on;
	private int version;
	private int timezone_id;
	private String subject;
	private String body;
	private ArrayList<Object> receivers;
	private ArrayList<Object> cc;
	private ArrayList<Object> bcc;





	public ScheduledEmailPage() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ScheduledEmailPage( int scheduled_id,String draft_id,ArrayList<Object> receivers, ArrayList<Object> cc, ArrayList<Object> bcc,int scheduled_on,int timezone_id,int include_opt_out_link,String subject,String body, int version) {
		super();
		this.scheduled_id = scheduled_id;
		this.draft_id = draft_id;
		this.scheduled_on = scheduled_on;
		this.version = version;
		this.timezone_id = timezone_id;
		this.include_opt_out_link = include_opt_out_link;
		this.receivers = receivers;
		this.cc = cc;
		this.bcc = bcc;
		this.subject = subject;
		this.body = body;

	}
	public int getScheduled_id() {
		return scheduled_id;
	}

	public void setScheduled_id(int scheduled_id) {
		this.scheduled_id = scheduled_id;
	}
	public String getDraft_id() {
		return draft_id;
	}

	public void setDraft_id(String draft_id) {
		this.draft_id = draft_id;
	}
	public void setScheduled_on(int scheduled_on) {
		this.scheduled_on = scheduled_on;
	}
	public int getscheduled_on() {
		return scheduled_on;
	}
	public int getTimezone_id() {
		return timezone_id;
	}

	public void setTimezone_id(int timezone_id) {
		this.timezone_id = timezone_id;
	}
	public int getInclude_opt_out_link() {
		return include_opt_out_link;
	}

	public void setInclude_opt_out_link(int include_opt_out_link) {
		this.include_opt_out_link = include_opt_out_link;
	}
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getBody() {
		return body;
	}
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	public ArrayList<Object> getCC() {
		return cc;
	}

	public void setCC(ArrayList<Object> cc) {
		this.cc = cc;
	}
	public ArrayList<Object> getBCC() {
		return bcc;
	}

	public void setBCC(ArrayList<Object> bcc) {
		this.bcc = bcc;
	}
	public ArrayList<Object> getReceivers() {
		return receivers;
	}

	public void setReceivers(ArrayList<Object> receivers) {
		this.receivers = receivers;
	}
}


