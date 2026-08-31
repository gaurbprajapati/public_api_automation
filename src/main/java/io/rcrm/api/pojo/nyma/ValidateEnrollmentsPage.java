package io.rcrm.api.pojo.nyma;

import lombok.*;
import java.util.List;

@Getter
@Setter
public class ValidateEnrollmentsPage {
	private List<Integer> enrollments;
	private int entity_type;
	private StepContains step_contains;

	@Getter
	@Setter
	public static class StepContains {
		private int task;
		private int email;
		private int sms;
	}
}