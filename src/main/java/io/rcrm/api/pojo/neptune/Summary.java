package io.rcrm.api.pojo.neptune;

public class Summary {

	private String note;
	private String key;
	private String prompt;

	public Summary(String note, String key, String prompt) {
		super();
		this.note = note;
		this.key = key;
		this.prompt = prompt;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

}
