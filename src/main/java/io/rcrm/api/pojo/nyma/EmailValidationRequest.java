package io.rcrm.api.pojo.nyma;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailValidationRequest {
    private List<Integer> entity_ids;
    private int entity_type_id;
} 