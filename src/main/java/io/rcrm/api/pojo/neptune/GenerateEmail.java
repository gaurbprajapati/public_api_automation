package io.rcrm.api.pojo.neptune;

public class GenerateEmail {

	private String tone;
	private String prompt;
	private String key;
	private String last_response;
	private String entity;
	private int record_id;

	public GenerateEmail() {
		super();
	}

	public GenerateEmail(String tone, String prompt, String key, String last_response, String entity, int record_id) {
		super();
		this.tone = tone;
		this.prompt = prompt;
		this.key = key;
		this.last_response = last_response;
		this.entity = entity;
		this.record_id = record_id;

	}

	public String getTone() {
		return tone;
	}

	public void setTone(String tone) {
		this.tone = tone;
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

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public int getRecord_id() {
		return record_id;
	}

	public void setRecord_id(int record_id) {
		this.record_id = record_id;
	}

}
