package io.rcrm.api.pojo.executiveSummary;

public class Template {

	private int type;
	private String template_name;
	private String template_content;

	public Template() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Template(int type, String template_name, String template_content) {
		super();
		this.type = type;
		this.template_name = template_name;
		this.template_content = template_content;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTemplate_name() {
		return template_name;
	}

	public void setTemplate_name(String template_name) {
		this.template_name = template_name;
	}

	public String getTemplate_content() {
		return template_content;
	}

	public void setTemplate_content(String template_content) {
		this.template_content = template_content;
	}

}
