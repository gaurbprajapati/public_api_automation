package io.rcrm.api.pojo.albatross;

public class New_email_templatePage {
private String emailcontext;
private String relatedtotypeid;
private String emailsubject;
private String template;

private boolean share;

/*
 * private int Scheduled_email_id;
 */

public New_email_templatePage() {
	super();
	// TODO Auto-generated constructor stub
}
public New_email_templatePage( String emailcontext,String relatedtotypeid,String emailsubject,String template,boolean share) {
	super();
	this.emailcontext = emailcontext;
	this.relatedtotypeid = relatedtotypeid;
	this.emailsubject = emailsubject;
	this.template = template;
	this.share = share;

}

public String getEmailcontext() {
	return emailcontext;
}

public void setEmailcontext(String emailcontext) {
	this.emailcontext = emailcontext;
}
public String getRelatedtotypeid() {
	return relatedtotypeid;
}

public void setRelatedtotypeid(String relatedtotypeid) {
	this.relatedtotypeid = relatedtotypeid;
}
public String getEmailsubject() {
	return emailsubject;
}

public void setEmailsubject(String emailsubject) {
	this.emailsubject = emailsubject;
}
public String getTemplate() {
	return template;
}

public void setTemplate(String template) {
	this.template = template;
}
public boolean getShare() {
	return share;
}

public void setShare(boolean share) {
	this.share = share;

}
}