package io.rcrm.api.pojo.neptune;

import io.rcrm.api.pojo.Job;

public class JobDescription {

	private Job job;
	private String key;
	private String prompt;

	public JobDescription() {
		super();
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
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
