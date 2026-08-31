package io.rcrm.api.pojo;

public class CandQuesAnsWithoutJob {

	private Boolean unanswered = true;
	private int question_id;
	private String answer;

	public Boolean getUnanswered() {
		return unanswered;
	}

	public void setUnanswered(Boolean unanswered) {
		this.unanswered = unanswered;
	}

	public int getQuestion_id() {
		return question_id;
	}

	public void setQuestion_id(int question_id) {
		this.question_id = question_id;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

}
