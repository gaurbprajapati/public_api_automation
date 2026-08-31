package io.rcrm.api.pojo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupedByRequest {
    private Integer entityId;
    private String groupedBy;
}
