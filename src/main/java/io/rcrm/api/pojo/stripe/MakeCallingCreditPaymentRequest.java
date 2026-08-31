package io.rcrm.api.pojo.stripe;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MakeCallingCreditPaymentRequest {

	private int price;
	private int qty;
	private String currency;
	private String currencyCode;
}