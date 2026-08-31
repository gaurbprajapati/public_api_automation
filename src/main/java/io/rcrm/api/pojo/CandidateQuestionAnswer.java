package io.rcrm.api.pojo;

import java.util.List;

public class CandidateQuestionAnswer {
	private List<CandQuesAnsWithoutJob> question_answers;

	public List<CandQuesAnsWithoutJob> getQuestion_answers() {
		return question_answers;
	}

	public void setQuestion_answers(List<CandQuesAnsWithoutJob> question_answers) {
		this.question_answers = question_answers;
	}

}
