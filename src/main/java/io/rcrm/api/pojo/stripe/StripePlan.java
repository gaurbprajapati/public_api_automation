package io.rcrm.api.pojo.stripe;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripePlan {

	private PlanDetails plan;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PlanDetails {
		private int seats;
		private String billingCycle;
		private String planid;
		private int recordAddon;
		private int monthlySeats;
	}

}