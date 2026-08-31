package io.rcrm.api.pojo.albatross;

public class New_sms_templatePage {
	private String template_name;
	private String relatedtotypeid;
	private String template;

	private boolean share;
	


	public New_sms_templatePage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public New_sms_templatePage(String template_name, String relatedtotypeid, String template, boolean share) {
		super();
		this.template_name = template_name;
		this.relatedtotypeid = relatedtotypeid;
		this.template = template;
		this.share = share;
	}
	
	public String getTemplate_name() {
		return template_name;
	}

	public void setTemplate_name(String template_name) {
		this.template_name = template_name;
	}
	public String getRelatedtotypeid() {
		return relatedtotypeid;
	}

	public void setRelatedtotypeid(String relatedtotypeid) {
		this.relatedtotypeid = relatedtotypeid;
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
