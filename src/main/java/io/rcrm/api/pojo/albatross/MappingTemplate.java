package io.rcrm.api.pojo.albatross;

public class MappingTemplate {
	private String template_name;
	private String template_content;
	private String entity_type;
	private int sharewithteammates;

	public MappingTemplate() {
	}

	// Getters and setters
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

	public String getEntity_type() {
		return entity_type;
	}

	public void setEntity_type(String entity_type) {
		this.entity_type = entity_type;
	}

	public int getSharewithteammates() {
		return sharewithteammates;
	}

	public void setSharewithteammates(int sharewithteammates) {
		this.sharewithteammates = sharewithteammates;
	}
}