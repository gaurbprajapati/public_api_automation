package io.rcrm.api.pojo.neptune;

public class EmailTemplate {

	private String prompt;
	private String key;
	private String last_response;
	private Integer related_to;

	public EmailTemplate() {
		super();
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLast_response() {
		return last_response;
	}

	public void setLast_response(String last_response) {
		this.last_response = last_response;
	}

	public Integer getRelated_to() {
		return related_to;
	}

	public void setRelated_to(Integer related_to) {
		this.related_to = related_to;
	}

}
