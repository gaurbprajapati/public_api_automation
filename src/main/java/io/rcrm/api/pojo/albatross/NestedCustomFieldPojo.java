package io.rcrm.api.pojo.albatross;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NestedCustomFieldPojo {
	private String entity;
	private int level;
	private String dependency_id;
	private int parent_id;
	private int child_id;
	private List<Mapping> mappings;
	
	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Mapping {
		private int parent_value_id;
		private Integer child_value_id;
		private Boolean child_visibility;
	}
}