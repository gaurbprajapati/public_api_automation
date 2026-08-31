package io.rcrm.api.pojo.albatross;

import java.util.List;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EntityTypeCustomField {
	private List<String> candidateCustomFieldIds;
	private List<String> companyCustomFieldIds;
	private List<String> contactCustomFieldIds;
	private List<String> dealCustomFieldIds;
	private List<String> jobCustomFieldIds;
	private List<Integer> recordIds;
	private int entityTypeId;
}
