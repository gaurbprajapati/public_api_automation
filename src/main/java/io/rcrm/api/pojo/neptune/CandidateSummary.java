package io.rcrm.api.pojo.neptune;

import io.rcrm.api.pojo.Candidate;

public class CandidateSummary {

	private Candidate candidate;
	private String key;
	private String prompt;

	public CandidateSummary() {
		super();
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

	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

}