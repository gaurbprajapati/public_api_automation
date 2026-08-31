package io.rcrm.api.pojo.albatross;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnlinkNestedCustomFieldPojo {
	private String entity;
	private int parent_id;
	private int child_id;
}