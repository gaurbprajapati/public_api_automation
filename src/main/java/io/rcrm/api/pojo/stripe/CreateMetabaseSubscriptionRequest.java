package io.rcrm.api.pojo.stripe;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateMetabaseSubscriptionRequest {

	private int selected_plan_id;
	private Integer selected_monthly_plan_id;
	private Integer existing_plan_id;

}