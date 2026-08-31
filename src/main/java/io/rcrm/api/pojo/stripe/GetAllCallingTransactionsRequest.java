package io.rcrm.api.pojo.stripe;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetAllCallingTransactionsRequest {

	private int limit = 25;
	private int offset = 0;
}