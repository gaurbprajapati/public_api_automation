package io.rcrm.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateInvoiceRequest {

	/**
	 * Entity type id -> list of associated entity ids.
	 * "2" -> contact
	 * "3" -> company
	 * "4" -> job
	 * "5" -> candidate
	 * "11" -> deal
	 */
	private Map<String, List<Integer>> associations;
}