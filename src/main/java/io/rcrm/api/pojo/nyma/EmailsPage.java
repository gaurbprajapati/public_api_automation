package io.rcrm.api.pojo.nyma;

import java.util.ArrayList;

public class EmailsPage {
	private String id;
	private ArrayList<Object> recivers;
	private ArrayList<Object> cc;
	private ArrayList<Object> bcc;
	private String subject;
	private int scheduled_email_id;

	private String body;
	private int version;

	public EmailsPage() {

	}
	public EmailsPage(String id, ArrayList<Object> recivers, ArrayList<Object> cc, ArrayList<Object> bcc,String  subject,String body, int version,int scheduled_email_id) {
		super();
		this.id = id;
		this.recivers = recivers;
		this.cc = cc;
		this.bcc = bcc;
		this.subject = subject;
		this.body = body;
		this.version = version;
		this.scheduled_email_id = scheduled_email_id;

	}
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	public int getScheduled_email_id() {
		return scheduled_email_id;
	}

	public void setScheduled_email_id(int scheduled_email_id) {
		this.scheduled_email_id = scheduled_email_id;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	public String getBody() {
		return body;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
	public ArrayList<Object> getRecivers() {
		return recivers;
	}

	public void setRecivers(ArrayList<Object> recivers) {
		this.recivers = recivers;
	}
}

