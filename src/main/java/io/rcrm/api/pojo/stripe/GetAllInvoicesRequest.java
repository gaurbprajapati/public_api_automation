package io.rcrm.api.pojo.stripe;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllInvoicesRequest {

	private int limit;
	private int offset;

}