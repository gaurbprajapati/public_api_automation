package io.rcrm.api.pojo.albatross;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetAssociateCount {

	private int activity_id;
	private String activity_type;

}
