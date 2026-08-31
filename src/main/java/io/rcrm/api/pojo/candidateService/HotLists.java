package io.rcrm.api.pojo.candidateService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotLists {
    private String name;
    private String related_to_type;
    private int shared;
}
