package io.rcrm.api.pojo.executiveSummary;

public class TemplateShareWithTeam {

	private String template_name;
	private String template_content;
	private int is_shared_with_teammates;

	public TemplateShareWithTeam() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TemplateShareWithTeam(String template_name, String template_content, int is_shared_with_teammates) {
		super();
		this.template_name = template_name;
		this.template_content = template_content;
		this.is_shared_with_teammates = is_shared_with_teammates;
	}

	public int getIs_shared_with_teammates() {
		return is_shared_with_teammates;
	}

	public void setIs_shared_with_teammates(int is_shared_with_teammates) {
		this.is_shared_with_teammates = is_shared_with_teammates;
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
